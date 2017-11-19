package muksihs.e621.resteemit.client;

import java.util.ArrayList;
import java.util.Arrays;
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
import e621.models.tag.index.Tag;
import elemental2.dom.DomGlobal;
import muksihs.e621.resteemit.client.Event.Rating;
import muksihs.e621.resteemit.client.Event.SteemPost;
import muksihs.e621.resteemit.client.cache.IndexCache;
import muksihs.e621.resteemit.client.cache.TagsCache;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.SavedState;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus, ValueChangeHandler<String> {

	private static final int CACHED_PAGE_SIZE = 50;
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

	@EventHandler
	protected void steemPost(Event.SteemPost event) {
		if (!isLoggedIn()) {
			fireEvent(new Event.Login<SteemPost>(event));
		} else {
			// TODO
		}

	}

	private boolean loggedIn;

	private boolean isLoggedIn() {
		return loggedIn;
	}

	@EventHandler
	protected <T extends GenericEvent> void login(Event.Login<T> event) {
		// fireEvent(new Event.ShowLoginUi());
	}

	@EventHandler
	protected void browseViewLoaded(Event.BrowseViewLoaded event) {
		DomGlobal.console.log("History.getToken()=" + History.getToken());
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
		hash.setPostId(savedPageStartId);
		hash.setRatings(mustHaveRatings);
		return hash;
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
		if (activePage == 0) {
			fireEvent(new Event.EnablePreviousButton(false));
		} else {
			fireEvent(new Event.EnablePreviousButton(true));
		}
		fireEvent(new Event.Loading(true));
		Scheduler.get().scheduleDeferred(() -> {
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			Set<String> availableTags = tagsForActiveSet();
			availableTags.addAll(topAvailableTags);
			availableTags.removeAll(mustHaveTags);
			availableTags.removeAll(mustNotHaveTags);

			reloadingOnFilterChange = false;
			savedPageStartId = previewsToShow.stream().mapToLong((p) -> p.getId()).max().getAsLong();
			GWT.log("new savedPageStartId: " + savedPageStartId);
			fireEvent(new Event.ShowAvailableTags(availableTags));
			fireEvent(new Event.ShowPreviews(previewsToShow));
			fireEvent(new Event.Loading(false));
			updateHash();
		});
	}

	private final Set<String> topAvailableTags = new TreeSet<>();

	private List<PostPreview> activeSetForPage(int activePage) {
		int start = activePage * Consts.PREVIEWS_TO_SHOW;
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
			final int responseSize = response.size();
			long pageStartId = 0;
			long nextBeforeId = Long.MAX_VALUE;
			for (E621Post post : response) {
				nextBeforeId = Long.min(nextBeforeId, post.getId());
				pageStartId = Long.max(pageStartId, post.getId());
			}

			if (!mustHaveRatings.isEmpty()) {
				response.removeIf((p) -> !mustHaveRatings.contains(p.getRating()));
			}

			response.removeIf((p) -> !extensionsWhitelist.contains(p.getFileExt().toLowerCase()));
			if (!mustNotHaveTags.isEmpty()) {
				response.removeIf((p) -> {
					String[] tags = p.getTags().split("\\s+");
					for (String tag : tags) {
						tag = tag.toLowerCase().trim();
						if (mustNotHaveTags.contains(tag)) {
							return true; // remove post
						}
					}
					return false; // keep post
				});
			}

			if (!mustHaveTags.isEmpty()) {
				response.removeIf((p) -> {
					String[] tags = p.getTags().split("\\s+");
					for (String tag : tags) {
						tag = tag.toLowerCase().trim();
						if (mustHaveTags.contains(tag)) {
							return false; // keep post
						}
					}
					return true; // remove post
				});
			}

			List<PostPreview> previews = new ArrayList<>();
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSampleUrl(), next.getFileUrl(),
						next.getCreatedAt().getS(), next.getTags());
				previews.add(preview);
			}
			// remove previews already in the activeset
			previews.removeAll(activeSet);
			activeSet.addAll(previews);
			boolean moreAvailable = nextBeforeId > 0 && responseSize > 0;
			int activePageEnd = (1 + activePage) * Consts.PREVIEWS_TO_SHOW;
			if ((activeSet.size() < activePageEnd) && moreAvailable) {
				final long beforeId;
				if (nextBeforeId == pageStartId) {
					GWT.log("Small cache block work around...");
					beforeId = nextBeforeId - CACHED_PAGE_SIZE;
				} else {
					if (prevBeforeId==nextBeforeId) {
						GWT.log("Dont' get stuck in a search loop!");
						beforeId = nextBeforeId - CACHED_PAGE_SIZE;
					} else {
						beforeId = nextBeforeId;
					}
				}
				prevBeforeId=beforeId;
				Scheduler.get().scheduleDeferred(() -> additionalPreviewsLoad(beforeId));
				return;
			}
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			long beforeId = previewsToShow.stream().mapToLong((p) -> p.getId()).min().getAsLong();
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
			fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
			GWT.log("EXCEPTION: " + String.valueOf(exception.getMessage()), exception);
			DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
			DomGlobal.console.log("HISTORY TOKEN: " + SavedState.asHistoryToken(getSavedStateHash()));
			DomGlobal.console.log(exception);
			DomGlobal.console.log(method);
			DomGlobal.console.log(method.getResponse());
			DomGlobal.console.log(method.getResponse().getText());
		}
	};

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
			for (Tag tag : tags) {
				topAvailableTags.add(tag.getName());
			}
			fireEvent(new Event.ShowView(View.BrowseView));
		} else {
			E621Api.api().tagList(1, limit, new MethodCallback<List<Tag>>() {
				@Override
				public void onSuccess(Method method, List<Tag> response) {
					topAvailableTags.clear();
					for (Tag tag : response) {
						topAvailableTags.add(tag.getName());
					}
					tagsCache.put(limit + "", response);
					GWT.log("Have " + topAvailableTags.size() + " top tags loaded.");
					fireEvent(new Event.ShowView(View.BrowseView));
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					// try reloading everything from scratch
					fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
					GWT.log("EXCEPTION: " + String.valueOf(exception.getMessage()), exception);
					DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
					DomGlobal.console.log("HISTORY TOKEN: " + SavedState.asHistoryToken(getSavedStateHash()));
					DomGlobal.console.log(exception);
					DomGlobal.console.log(method);
					DomGlobal.console.log(method.getResponse());
					DomGlobal.console.log(method.getResponse().getText());
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
		activePage++;
		long beforeId = tmp;
		Scheduler.get().scheduleDeferred(() -> {
			additionalPreviewsLoad(beforeId);
		});
	}

	protected void additionalPreviewsLoad(long beforeId) {
		List<String> tags = new ArrayList<>();
		if (mustHaveRatings.size() == 1) {
			// querying E621 with multiple ratings doesn't seem to give correct results
			// so rely strictly on client side filtering if more than one rating provided
			Iterator<String> iRatings = mustHaveRatings.iterator();
			while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
				tags.add("rating:" + iRatings.next());
			}
		}
		Iterator<String> iMust = mustHaveTags.iterator();
		while (iMust.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add(iMust.next());
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		while (iMustNot.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add("-" + iMustNot.next());
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		// keep beforeId aligned with multiples of CACHED_PAGE_SIZE so the cache works
		// correctly
		beforeId = (long) (Math.ceil((double) beforeId / (double) CACHED_PAGE_SIZE) * (double) CACHED_PAGE_SIZE);
		String queriedTags = sb.toString();
		String cachedPostsKey = queriedTags + "," + beforeId;
		List<E621Post> cached = INDEX_CACHE.get(cachedPostsKey);
		if (cached == null) {
			fireEvent(new Event.QuickMessage("Searching E621..."));
			E621Api.api().postIndex(queriedTags, (int) beforeId, CACHED_PAGE_SIZE, cacheIndexResponse(cachedPostsKey));
		} else {
			fireEvent(new Event.QuickMessage("Searching cache... "+beforeId));
			Scheduler.get().scheduleDeferred(() -> onPostsLoaded.onSuccess(null, cached));
		}
	}

	private MethodCallback<List<E621Post>> cacheIndexResponse(String cachedPostsKey) {
		return new MethodCallback<List<E621Post>>() {

			@Override
			public void onSuccess(Method method, List<E621Post> response) {
				Scheduler.get().scheduleDeferred(() -> INDEX_CACHE.put(cachedPostsKey, response));
				onPostsLoaded.onSuccess(method, response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// try reloading everything from scratch
				fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
				GWT.log("EXCEPTION: " + String.valueOf(exception.getMessage()), exception);
				DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
				DomGlobal.console.log("HISTORY TOKEN: " + SavedState.asHistoryToken(getSavedStateHash()));
				DomGlobal.console.log(exception);
				DomGlobal.console.log(method);
				DomGlobal.console.log(method.getResponse());
				DomGlobal.console.log(method.getResponse().getText());
			}
		};
	}

	@EventHandler
	protected void initialPreviewsLoad(Event.LoadInitialPreviews event) {
		fireEvent(new Event.Loading(true));
		updateActiveTagFilters();
		activePage = 0;
		activeSet.clear();
		List<String> tags = new ArrayList<>();
		if (mustHaveRatings.size() == 1) {
			Iterator<String> iRatings = mustHaveRatings.iterator();
			while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
				tags.add("rating:" + iRatings.next());
			}
		}
		Iterator<String> iMust = mustHaveTags.iterator();
		while (iMust.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add(iMust.next());
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		while (iMustNot.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add("-" + iMustNot.next());
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		E621Api.api().postIndex(sb.toString(), CACHED_PAGE_SIZE, onPostsLoaded);
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		DomGlobal.console.log("RECONSTRUCTING VIEW STATE: " + token);
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
		validateTags();
	}

	private void validateTags() {
		String tags = String.join(" ", mustHaveTags) + String.join(" ", mustNotHaveTags);
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
				for (String tag : response.keySet()) {
					topAvailableTags.add(tag);
				}
				Iterator<String> iter = mustHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!topAvailableTags.contains(next)) {
						DomGlobal.console.log("Removing invalid tag: " + next);
						iter.remove();
					}
				}
				iter = mustNotHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!topAvailableTags.contains(next)) {
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
				fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
				GWT.log("EXCEPTION: " + String.valueOf(exception.getMessage()), exception);
				DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
				DomGlobal.console.log("HISTORY TOKEN: " + SavedState.asHistoryToken(getSavedStateHash()));
				DomGlobal.console.log(exception);
				DomGlobal.console.log("Method", method);
			}
		};
		E621Api.api().tagRelated(tags, validated);
	}

}
