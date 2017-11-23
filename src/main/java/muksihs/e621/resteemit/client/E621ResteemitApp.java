package muksihs.e621.resteemit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import e621.E621Api;
import e621.models.post.index.E621Post;
import e621.models.post.tags.E621Tag;
import e621.models.tag.index.Tag;
import elemental2.dom.DomGlobal;
import gwt.material.design.client.MaterialWithJQuery;
import muksihs.e621.resteemit.client.Event.Rating;
import muksihs.e621.resteemit.client.Event.SteemPost;
import muksihs.e621.resteemit.client.cache.IndexCache;
import muksihs.e621.resteemit.client.cache.TagsCache;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.SavedState;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;
import steem.SteemApi;
import steem.TrendingTagsResult;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus, ValueChangeHandler<String> {

	/**
	 * A non-empirical and non-arbitrary number to skew tags in the "must have" set
	 * higher as part of the automatic steem tag selection process.
	 */
	private static final int TAG_SKEW = 43;
	private static final int CACHED_PAGE_SIZE = 20;
	private static final IndexCache INDEX_CACHE = new IndexCache(CACHED_PAGE_SIZE);

	public E621ResteemitApp() {
		extensionsWhitelist.addAll(Arrays.asList("png", "jpg", "gif", "jpeg"));
	}

	private static final int MAX_TAGS_PER_QUERY = 6;

	interface MyEventBinder extends EventBinder<E621ResteemitApp> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private RootPanel rp;
	private ViewController controller;

	private final Set<String> mustHaveRatings = new TreeSet<>();
	private final Set<String> mustHaveTags = new TreeSet<>();
	private final Set<String> mustNotHaveTags = new TreeSet<>();
	private final List<PostPreview> activeSet = new ArrayList<>();
	private final Set<String> extensionsWhitelist = new TreeSet<>();
	private int activePage;

	public static class TrendingTag {
		public String name;
		public int topPosts;
		public int comments;
		public double totalPayouts;
	}

	public void matchingTagsCollector(Map<String, String> error, TrendingTagsResult[] tags, String name,
			MatchingTagsState state) {
		if (error != null) {
			GWT.log(String.valueOf(error));
		}
		if (tags != null) {
			for (TrendingTagsResult trendingTag : tags) {
				if (!trendingTag.getName().equals(name)) {
					continue;
				}
				TrendingTag tag = new TrendingTag();
				tag.comments = trendingTag.getComments();
				tag.name = trendingTag.getName();
				tag.topPosts = trendingTag.getTop_posts();
				try {
					tag.totalPayouts = Double.valueOf(trendingTag.getTotal_payouts().replace(" SBD", ""));
				} catch (NumberFormatException e) {
				}
				state.collector.add(tag);
			}
		}
		getMatchingTrendingTags(state);
	}

	private void getMatchingTrendingTags(MatchingTagsState state) {
		if (!state.iter.hasNext()) {
			// sort descending order to most valuable at top of list
			Collections.sort(state.collector, (a, b) -> {
				// sort by per top post payout average
				double p1 = a.totalPayouts / ((double) a.topPosts + (double) a.comments + 1d);
				double p2 = b.totalPayouts / ((double) b.topPosts + (double) b.comments + 1d);
				// make must have tags more likely to sort to preferred use
				if (mustHaveTags.contains(a.name)) {
					p1 += TAG_SKEW;
				}
				if (mustHaveTags.contains(b.name)) {
					p2 += TAG_SKEW;
				}
				if (Double.compare(p1, p2) != 0) {
					return Double.compare(p2, p1);
				}
				// sort by payout (raw value of topic)
				if (a.totalPayouts != b.totalPayouts) {
					return Double.compare(b.totalPayouts, a.totalPayouts);
				}
				// sort by number of posts (popularity of tag/audience by subject)
				if (a.topPosts != b.topPosts) {
					return Integer.compare(b.topPosts, a.topPosts);
				}
				// sort by number of comments (indicates audience response level?)
				if (a.comments != b.comments) {
					return Integer.compare(b.comments, a.comments);
				}
				return a.name.compareToIgnoreCase(b.name);
			});
			if (!state.post.getRating().equals(Rating.SAFE.getTag())) {
				state.collector = state.collector.subList(0, Math.min(state.collector.size(), 4));
				TrendingTag nsfwTag = new TrendingTag();
				nsfwTag.name = "nsfw";
				state.collector.add(nsfwTag);
			} else {
				state.collector = state.collector.subList(0, Math.min(state.collector.size(), 5));
			}
			if (state.collector.size() < 5) {
				TrendingTag artTag = new TrendingTag();
				artTag.name = "art";
				state.collector.add(artTag);
			}
			if (state.collector.size() < 5) {
				TrendingTag furryTag = new TrendingTag();
				furryTag.name = "furry";
				state.collector.add(furryTag);
			}
			if (state.collector.size() < 5) {
				TrendingTag lifeTag = new TrendingTag();
				lifeTag.name = "life";
				state.collector.add(lifeTag);
			}
			Iterator<TrendingTag> iter = state.collector.iterator();
			StringBuilder sb = new StringBuilder();
			while (iter.hasNext()) {
				sb.append(iter.next().name);
				if (iter.hasNext()) {
					sb.append(" ");
				}
			}
			DomGlobal.console.log(sb.toString());
			fireEvent(new Event.Loading(false));
			return;
		}
		String name = state.iter.next().getName();
		SteemApi.getTrendingTags(name, 1, (e, r) -> matchingTagsCollector(e, r, name, state));
	}

	private void pickBestTagsThenPostConfirm(PostPreview preview, List<E621Tag> response) {
		MatchingTagsState state = new MatchingTagsState();
		state.post = preview;
		state.withAlternateForms = new ArrayList<>();
		state.collector = new ArrayList<>();
		state.withAlternateForms.addAll(response);
		for (E621Tag tag : response) {
			String name = tag.getName().toLowerCase();
			String altName;
			E621Tag alt;
			altName = name.replaceAll("[^a-z0-9\\-]", "-");
			if (!altName.equalsIgnoreCase(name) && !altName.isEmpty()) {
				alt = new E621Tag();
				alt.setName(altName);
				state.withAlternateForms.add(alt);
			}
			altName = name.replaceAll("[^a-z0-9\\-]", "");
			if (!altName.equalsIgnoreCase(name) && !altName.isEmpty()) {
				alt = new E621Tag();
				alt.setName(altName);
				state.withAlternateForms.add(alt);
			}
		}
		state.iter = state.withAlternateForms.iterator();
		getMatchingTrendingTags(state);
	}

	private void fatalError(Method method, Throwable exception) {
		fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
		DomGlobal.console.log(exception);
		DomGlobal.console.log(method);
		DomGlobal.console.log(method.getResponse());
		DomGlobal.console.log(method.getResponse().getText());
	}

	@EventHandler
	protected void showAccountDialog(Event.ShowAccountDialog event) {

	}

	@EventHandler
	protected void mostRecent(Event.MostRecentSet event) {
		activePage = 0;
		savedPageStartId = 0;
		reloadingOnFilterChange = true;
		fireEvent(new Event.LoadInitialPreviews());
	}

	private static class MatchingTagsState {
		public List<TrendingTag> collector;
		public Iterator<E621Tag> iter;
		public PostPreview post;
		public List<E621Tag> withAlternateForms;

	}

	@EventHandler
	protected void steemPost(Event.SteemPost event) {
		fireEvent(new Event.Loading(true));
		MethodCallback<List<E621Tag>> cb = new MethodCallback<List<E621Tag>>() {
			@Override
			public void onSuccess(Method method, List<E621Tag> response) {
				pickBestTagsThenPostConfirm(event.getPreview(), response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				fireEvent(new Event.FatalError(exception.getMessage()));
			}
		};
		if (!isLoggedIn()) {
			fireEvent(new Event.Login<SteemPost>(event));
		} else {
			E621Api.api().postTags(event.getPreview().getId(), cb);
		}

	}

	private boolean loggedIn;

	private boolean isLoggedIn() {
		return loggedIn;
	}

	@EventHandler
	protected void loginComplete(Event.LoginComplete event) {
		loggedIn=event.isLoggedIn();
		if (afterLoginPendingEvent!=null) {
			fireEvent(afterLoginPendingEvent);
			afterLoginPendingEvent=null;
		}
	}
	private GenericEvent afterLoginPendingEvent;
	@EventHandler
	protected <T extends GenericEvent> void login(Event.Login<T> event) {
		afterLoginPendingEvent=event.getRefireEvent();
		fireEvent(new Event.ShowLoginUi());
	}

	@EventHandler
	protected void browseViewLoaded(Event.BrowseViewLoaded event) {
		if (History.getToken().trim().isEmpty()) {
			Set<String> boxes = new HashSet<>();
			boxes.add(Rating.SAFE.getTag());
			fireEvent(new Event.SetRatingsBoxes(boxes));
			Set<Rating> safe = new HashSet<>();
			safe.add(Rating.SAFE);
			fireEvent(new Event.SetRating(safe));
		} else {
			History.fireCurrentHistoryState();
		}
	}

	private void updateAvailableTagsDisplay() {
		Set<String> availableTags = new TreeSet<>();
		for (Tag tag : topAvailableTags) {
			availableTags.add(tag.getName());
		}
		// remove active tags from list of activatable tags
		availableTags.removeAll(mustHaveTags);
		availableTags.removeAll(mustNotHaveTags);
		// add back active tags as in use tags
		for (String tag : mustHaveTags) {
			availableTags.add("+" + tag);
		}
		for (String tag : mustNotHaveTags) {
			availableTags.add("-" + tag);
		}

		fireEvent(new Event.ShowAvailableTags(availableTags));
	}

	@EventHandler
	protected void removeFromFilter(Event.RemoveFromFilter event) {
		reloadingOnFilterChange = true;
		mustHaveTags.remove(event.getTag().substring(1));
		mustNotHaveTags.remove(event.getTag().substring(1));
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void setRating(Event.SetRating event) {
		reloadingOnFilterChange = true;
		mustHaveRatings.clear();
		/*
		 * if ALL ratings, then leave settings clear so that more tags can be used as
		 * part of filtering query
		 */
		if (event.getRating().size() != Rating.values().length) {
			for (Rating rating : event.getRating()) {
				mustHaveRatings.add(rating.getTag());
			}
		}
		fireEvent(new Event.LoadInitialPreviews());
	}

	private void updateHash() {
		SavedState hash = getSavedStateHash();
		History.newItem(SavedState.asHistoryToken(hash), false);
	}

	private SavedState getSavedStateHash() {
		SavedState hash = new SavedState();
		hash.setMustHave(mustHaveTags);
		hash.setMustNotHave(mustNotHaveTags);
		// don't lock view at a specific post when on first page
		if (activePage > 0) {
			hash.setPostId(savedPageStartId);
		}
		// } else {
		// }
		hash.setRatings(mustHaveRatings);
		return hash;
	}

	@EventHandler
	protected void clearSearch(Event.ClearSearch event) {
		reloadingOnFilterChange = true;
		mustHaveRatings.clear();
		mustHaveTags.clear();
		mustNotHaveTags.clear();
		mustHaveRatings.add(Rating.SAFE.getTag());
		activePage = 0;
		savedPageStartId = 0;
		fireEvent(new Event.SetRatingsBoxes(mustHaveRatings));
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void addToIncludeFilter(Event.AddToIncludeFilter event) {
		reloadingOnFilterChange = true;
		mustHaveTags.add(event.getTag());
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void addToExcludeFilter(Event.AddToExcludeFilter event) {
		reloadingOnFilterChange = true;
		mustNotHaveTags.add(event.getTag());
		fireEvent(new Event.LoadInitialPreviews());
	}

	private void updateActiveTagFilters() {
		Set<String> tags = new TreeSet<>();
		for (String tag : mustHaveTags) {
			tags.add("+" + tag);
		}
		for (String tag : mustNotHaveTags) {
			tags.add("-" + tag);
		}
		fireEvent(new Event.ShowFilterTags(tags));
	}

	@EventHandler
	protected void showPreviews(Event.PreviewsLoaded event) {
		Scheduler.get().scheduleDeferred(this::updateAvailableTags);
		if (activePage == 0) {
			fireEvent(new Event.EnablePreviousButton(false));
		} else {
			fireEvent(new Event.EnablePreviousButton(true));
		}
		fireEvent(new Event.Loading(true));
		List<PostPreview> previewsToShow = activeSetForPage(activePage);
		reloadingOnFilterChange = false;
		savedPageStartId = previewsToShow.stream().mapToLong((p) -> p.getId()).max().orElse(0);
		fireEvent(new Event.ShowPreviews(previewsToShow));
		fireEvent(new Event.Loading(false));
		updateHash();
		if (previewsToShow.isEmpty()) {
			fireEvent(new Event.AlertMessage("No Posts Match Your Filter Settings!"));
		}
	}

	private void updateAvailableTags() {
		Set<String> availableTags = tagsForActiveSet();
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = availableTags.iterator();
		while (iTags.hasNext() && sb.length() < 1024) {
			String next = iTags.next();
			Tag tmp = new Tag();
			tmp.setName(next);
			if (topAvailableTags.contains(tmp)) {
				continue;
			}
			sb.append(next);
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		if (sb.length() > 0) {
			E621Api.api().tagRelated(sb.toString(), updateTopTagsCallback);
		} else {
			updateAvailableTagsDisplay();
		}
	}

	private MethodCallback<Map<String, List<List<String>>>> updateTopTagsCallback = new MethodCallback<Map<String, List<List<String>>>>() {
		@Override
		public void onFailure(Method method, Throwable exception) {
			DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
			DomGlobal.console.log(exception);
			DomGlobal.console.log(method);
		}

		@Override
		public void onSuccess(Method method, Map<String, List<List<String>>> response) {
			for (String tagName : response.keySet()) {
				Tag tag = new Tag();
				tag.setName(tagName);
				List<List<String>> list = response.get(tagName);
				if (list != null) {
					// use related tag count for query weighting purposes
					tag.setCount(list.size());
				}
				topAvailableTags.add(tag);
			}
			updateAvailableTagsDisplay();
		}
	};

	private final Set<Tag> topAvailableTags = new TreeSet<>();

	private List<PostPreview> activeSetForPage(int activePage) {
		int start = activePage * Consts.PREVIEWS_TO_SHOW;
		if (activeSet.size() < Consts.PREVIEWS_TO_SHOW) {
			return activeSet;
		}
		List<PostPreview> previewsToShow = activeSet.subList(Math.min(start, activeSet.size() - 1),
				Math.min(start + Consts.PREVIEWS_TO_SHOW, activeSet.size()));
		return previewsToShow;
	}

	private Set<String> tagsForActiveSet() {
		Set<String> tags = new TreeSet<>();
		for (PostPreview post : activeSet) {
			tags.addAll(Arrays.asList(post.getTags().split("\\s+")));
		}
		return tags;
	}

	private boolean reloadingOnFilterChange = false;
	private long savedPageStartId = 0;
	private long prevBeforeId = 0;

	private MethodCallback<List<E621Post>> onPostsLoaded = new MethodCallback<List<E621Post>>() {

		@Override
		public void onSuccess(Method method, List<E621Post> response) {
			// do NOT alter response object, it screws up the cache if done!
			final int responseSize = response.size();
			long pageStartId = 0;
			long nextBeforeId = Long.MAX_VALUE;
			for (E621Post post : response) {
				nextBeforeId = Long.min(nextBeforeId, post.getId());
				pageStartId = Long.max(pageStartId, post.getId());
			}

			List<PostPreview> previews = new ArrayList<>();
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSampleUrl(), next.getFileUrl(),
						next.getCreatedAt().getS(), next.getTags(), next.getRating(), next.getFileExt());
				previews.add(preview);
			}
			// remove previews already in the activeset
			previews.removeAll(activeSet);
			activeSet.addAll(previews);

			// filter the active set
			if (!mustHaveRatings.isEmpty()) {
				activeSet.removeIf((p) -> !mustHaveRatings.contains(p.getRating()));
			}

			activeSet.removeIf((p) -> !extensionsWhitelist.contains(p.getFileExt().toLowerCase()));
			if (!mustNotHaveTags.isEmpty()) {
				activeSet.removeIf((p) -> {
					Set<String> tags = new HashSet<>(Arrays.asList(p.getTags().split("\\s+")));
					return !Collections.disjoint(tags, mustNotHaveTags);
				});
			}

			if (!mustHaveTags.isEmpty()) {
				activeSet.removeIf((p) -> {
					Set<String> tags = new HashSet<>(Arrays.asList(p.getTags().split("\\s+")));
					return !tags.containsAll(mustHaveTags);
				});
			}

			boolean moreAvailable = nextBeforeId > 0 && responseSize > 0;
			int activePageEnd = (1 + activePage) * Consts.PREVIEWS_TO_SHOW;
			if ((activeSet.size() < activePageEnd) && moreAvailable) {
				final long beforeId;
				if (nextBeforeId == pageStartId) {
					beforeId = nextBeforeId - CACHED_PAGE_SIZE;
				} else {
					if (prevBeforeId == nextBeforeId) {
						beforeId = nextBeforeId - CACHED_PAGE_SIZE;
					} else {
						beforeId = nextBeforeId;
					}
				}
				prevBeforeId = beforeId;
				Scheduler.get().scheduleDeferred(() -> additionalPreviewsLoad(beforeId));
				return;
			}
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			long beforeId = previewsToShow.stream().mapToLong((p) -> p.getId()).min().orElse(0);
			boolean skipForwards = reloadingOnFilterChange //
					&& beforeId > savedPageStartId //
					&& savedPageStartId > 0;
			if (skipForwards && moreAvailable) {
				fireEvent(new Event.NextPreviewSet());
				return;
			}
			fireEvent(new Event.PreviewsLoaded());
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			// try reloading everything from scratch
			fatalError(method, exception);
		}
	};
	private boolean initialPageLoad;

	@EventHandler
	protected void fatalError(Event.FatalError event) {
		new Timer() {
			@Override
			public void run() {
				Location.reload();
			}
		}.schedule(45000);
	}

	@Override
	public void execute() {
		waitForMaterialLoad();
	}

	private void waitForMaterialLoad() {
		if (MaterialWithJQuery.isjQueryLoaded()) {
			if (MaterialWithJQuery.isMaterializeLoaded()) {
				onReady();
				return;
			}
		}
		Scheduler.get().scheduleDeferred(this::waitForMaterialLoad);
	}

	private void onReady() {
		rp = RootPanel.get("e621resteemit");
		rp.clear();
		MainView mainView = new MainView();
		rp.add(mainView);
		eventBinder.bindEventHandlers(this, eventBus);
		setController(new ViewController(mainView.getPanel()));
		fireEvent(new Event.Loading(true));
		// hash parsing
		History.addValueChangeHandler(this);
		Scheduler.get().scheduleDeferred(this::startApp);
	}

	private void startApp() {
		// load most common available tags, then show the view
		int limit = 500;
		TagsCache tagsCache = new TagsCache(limit);
		List<Tag> tags = tagsCache.get(limit + "");
		if (tags != null && !tags.isEmpty()) {
			topAvailableTags.addAll(tags);
			fireEvent(new Event.ShowView(View.BrowseView));
		} else {
			E621Api.api().tagList(1, limit, new MethodCallback<List<Tag>>() {
				@Override
				public void onSuccess(Method method, List<Tag> response) {
					topAvailableTags.clear();
					for (Tag tag : response) {
						if (tag == null) {
							continue;
						}
						topAvailableTags.add(tag);
					}
					tagsCache.put(limit + "", response);
					fireEvent(new Event.ShowView(View.BrowseView));
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					// try reloading everything from scratch
					fatalError(method, exception);
				}
			});
		}
	}

	public ViewController getController() {
		return controller;
	}

	public void setController(ViewController controller) {
		this.controller = controller;
	}

	@EventHandler
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.AppVersion(Consts.APP_VERSION));
	}

	@EventHandler
	protected void showPreviousSet(Event.PreviousPreviewSet event) {
		if (activePage > 0) {
			fireEvent(new Event.Loading(true));
			activePage--;
			fireEvent(new Event.PreviewsLoaded());
		}
	}

	@EventHandler
	protected void showNextSet(Event.NextPreviewSet event) {
		fireEvent(new Event.Loading(true));
		List<PostPreview> previewsToShow = activeSetForPage(activePage);
		long tmp = Long.MAX_VALUE;
		for (PostPreview preview : previewsToShow) {
			tmp = Long.min(tmp, preview.getId());
		}
		long beforeId = tmp;
		Scheduler.get().scheduleDeferred(() -> {
			activePage++;
			additionalPreviewsLoad(beforeId);
		});
	}

	private static class AlignedQuery {
		public long beforeId;
		public String query;
		public String cachedQueryKey;
	}

	protected void additionalPreviewsLoad(long beforeId) {
		AlignedQuery q = align(beforeId);
		List<E621Post> cached = INDEX_CACHE.get(q.cachedQueryKey);
		if (cached == null) {
			fireEvent(new Event.QuickMessage("Searching E621... " + q.beforeId));
			E621Api.api().postIndex(q.query, (int) beforeId, CACHED_PAGE_SIZE, cacheIndexResponse(q.cachedQueryKey));
		} else {
			fireEvent(new Event.QuickMessage("Searching cache... " + q.beforeId));
			Scheduler.get().scheduleDeferred(() -> onPostsLoaded.onSuccess(null, cached));
		}
	}

	private AlignedQuery align(long beforeId) {
		AlignedQuery q = new AlignedQuery();
		// keep beforeId aligned with multiples of CACHED_PAGE_SIZE so the cache works
		// correctly
		String query = buildQuery();
		q.beforeId = getAlignedBeforeId(beforeId);
		q.cachedQueryKey = query + "," + q.beforeId;
		q.query = query;
		return q;
	}

	private long getAlignedBeforeId(final long _beforeId) {
		long beforeId = _beforeId;
		beforeId = (long) (Math.ceil((double) beforeId / (double) CACHED_PAGE_SIZE) * (double) CACHED_PAGE_SIZE);
		return beforeId;
	}

	private String buildQuery() {
		List<String> tags = new ArrayList<>();
		if (mustHaveRatings.size() == 1) {
			// querying E621 with multiple ratings doesn't seem to give correct results
			// so rely strictly on client side filtering if more than one rating provided
			Iterator<String> iRatings = mustHaveRatings.iterator();
			while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
				tags.add("rating:" + iRatings.next());
			}
		}
		List<Tag> tmpMustHave = new ArrayList<>();
		List<Tag> tmpMustNotHave = new ArrayList<>();
		Iterator<String> iMust = mustHaveTags.iterator();
		must: while (iMust.hasNext()) {
			String next = iMust.next();
			for (Tag tag : topAvailableTags) {
				if (tag.getName().equals(next)) {
					tmpMustHave.add(tag);
					continue must;
				}
			}
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		mustNot: while (iMustNot.hasNext()) {
			String next = iMustNot.next();
			for (Tag tag : topAvailableTags) {
				if (tag.getName().equals(next)) {
					tmpMustNotHave.add(tag);
					continue mustNot;
				}
			}
		}
		List<Tag> queryTags = new ArrayList<>();
		queryTags.addAll(tmpMustHave);
		queryTags.addAll(tmpMustNotHave);
		/*
		 * try and sort tags to produce better queries: must have should be those with
		 * least count of posts and must not have should be those with the most count of
		 * posts we seem to perform slightly less queries preferring must have tag over
		 * must not have tags
		 */
		Collections.sort(queryTags, (a, b) -> {
			// sort musthave before mustnothave
			if (tmpMustHave.contains(a) && tmpMustNotHave.contains(b)) {
				return -1;
			}
			if (tmpMustNotHave.contains(a) && tmpMustHave.contains(b)) {
				return 1;
			}
			// sort musthave vs musthave asc
			if (tmpMustHave.contains(a) && tmpMustHave.contains(b)) {
				return Long.compare(a.getCount(), b.getCount());
			}
			// musthavenot vs musthavenot desc
			if (tmpMustNotHave.contains(a) && tmpMustNotHave.contains(b)) {
				return Long.compare(b.getCount(), a.getCount());
			}
			// sort the rest asc
			return Long.compare(a.getCount(), b.getCount());
		});

		Iterator<Tag> iQuery = queryTags.iterator();
		while (iQuery.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			Tag next = iQuery.next();
			if (tmpMustHave.contains(next)) {
				tags.add(next.getName());
			}
			if (tmpMustNotHave.contains(next)) {
				tags.add("-" + next.getName());
			}
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		String query = sb.toString();
		return query;
	}

	private MethodCallback<List<E621Post>> cacheIndexResponse(String cachedPostsKey) {
		return new MethodCallback<List<E621Post>>() {

			@Override
			public void onSuccess(Method method, List<E621Post> response) {
				// do NOT alter response object, it screws up the cache if done!
				Scheduler.get().scheduleDeferred(() -> INDEX_CACHE.put(cachedPostsKey, response));
				onPostsLoaded.onSuccess(method, response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// try reloading everything from scratch
				fatalError(method, exception);
			}
		};
	}

	@EventHandler
	protected void initialPreviewsLoad(Event.LoadInitialPreviews event) {
		fireEvent(new Event.Loading(true));
		updateActiveTagFilters();
		/*
		 * if on first view, make sure we stay on first view and don't try and navigate
		 * deep into a reduced filter set because of a large jump up in available posts
		 * with higher numbers. If this is an initialPageLoad from a restored state,
		 * activePage is always 0, so don't apply this rule for that particular case as
		 * an exception.
		 */
		if (activePage == 0 && !initialPageLoad) {
			savedPageStartId = 0;
			initialPageLoad = false;
		}
		activePage = 0;
		activeSet.clear();
		String query = buildQuery();
		E621Api.api().postIndex(query, CACHED_PAGE_SIZE, onPostsLoaded);
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		GWT.log("Hash Change: " + token);
		SavedState state = SavedState.parseHistoryToken(token);
		mustHaveTags.clear();
		mustNotHaveTags.clear();
		mustHaveRatings.clear();
		savedPageStartId = state.getPostId();
		if (state.getMustHave() != null) {
			mustHaveTags.addAll(state.getMustHave());
		}
		if (state.getMustNotHave() != null) {
			mustNotHaveTags.addAll(state.getMustNotHave());
		}
		if (state.getRatings() != null) {
			mustHaveRatings.addAll(state.getRatings());
		}
		fireEvent(new Event.SetRatingsBoxes(state.getRatings()));
		initialPageLoad = true;
		validateTagsThenLoadPreviews();
	}

	private void validateTagsThenLoadPreviews() {
		String tags = String.join(" ", mustHaveTags) + " " + String.join(" ", mustNotHaveTags);
		tags = tags.trim();
		MethodCallback<Map<String, List<List<String>>>> validated = new MethodCallback<Map<String, List<List<String>>>>() {
			@Override
			public void onSuccess(Method method, Map<String, List<List<String>>> response) {
				if (response.size() == 0) {
					mustHaveTags.clear();
					mustNotHaveTags.clear();
					reloadingOnFilterChange = true;
					fireEvent(new Event.LoadInitialPreviews());
					return;
				}
				/*
				 * add all to the in memory available tags then remove any invalid ones
				 */
				for (String tagName : response.keySet()) {
					Tag tag = new Tag();
					tag.setName(tagName);
					tag.setCount(response.get(tagName).size());
					if (!topAvailableTags.contains(tag)) {
						topAvailableTags.add(tag);
					}
				}
				/*
				 * build up quick lookup set for valid tag names
				 */
				Set<String> validTagNames = new HashSet<>();
				for (Tag tag : topAvailableTags) {
					validTagNames.add(tag.getName());
				}
				/*
				 * remove tags not in the valid tag names set
				 */
				Iterator<String> iter = mustHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!validTagNames.contains(next)) {
						DomGlobal.console.log("Removing invalid tag: " + next);
						iter.remove();
					}
				}
				/*
				 * remove tags not in the valid tag names set
				 */
				iter = mustNotHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!validTagNames.contains(next)) {
						DomGlobal.console.log("Removing invalid tag: " + next);
						iter.remove();
					}
				}
				reloadingOnFilterChange = true;
				fireEvent(new Event.LoadInitialPreviews());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// try reloading everything from scratch
				fatalError(method, exception);
			}
		};
		if (tags.length() > 0) {
			E621Api.api().tagRelated(tags, validated);
		} else {
			reloadingOnFilterChange = true;
			fireEvent(new Event.LoadInitialPreviews());
		}
	}

}
