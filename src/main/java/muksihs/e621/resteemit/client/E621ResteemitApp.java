package muksihs.e621.resteemit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import e621.E621Api;
import e621.models.post.index.E621Post;
import e621.models.post.index.E621PostList;
import muksihs.e621.resteemit.client.Event.Rating;
import muksihs.e621.resteemit.client.cache.IndexCache;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus {


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

	@EventHandler
	protected void removeFromFilter(Event.RemoveFromFilter event) {
		reloadingOnFilterChange = true;
		mustHaveTags.remove(event.getTag().substring(1));
		mustNotHaveTags.remove(event.getTag().substring(1));
		fireEvent(new Event.InitialPreviews());
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
		fireEvent(new Event.InitialPreviews());

	}

	@EventHandler
	protected void addToIncludeFilter(Event.AddToIncludeFilter event) {
		reloadingOnFilterChange = true;
		mustHaveTags.add(event.getTag());
		fireEvent(new Event.InitialPreviews());
	}

	@EventHandler
	protected void addToExcludeFilter(Event.AddToExcludeFilter event) {
		reloadingOnFilterChange = true;
		mustNotHaveTags.add(event.getTag());
		fireEvent(new Event.InitialPreviews());
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
		if (activePage==0) {
			fireEvent(new Event.EnablePreviousButton(false));
		} else {
			fireEvent(new Event.EnablePreviousButton(true));
		}
		fireEvent(new Event.Loading(true));
		Scheduler.get().scheduleDeferred(() -> {
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			Set<String> availableTags = tagsForActiveSet();
			availableTags.removeAll(mustHaveTags);
			availableTags.removeAll(mustNotHaveTags);

			reloadingOnFilterChange = false;
			savedPageStartId = previewsToShow.stream().mapToLong((p) -> p.getId()).max().getAsLong();
			GWT.log("new savedPageStartId: " + savedPageStartId);
			fireEvent(new Event.ShowAvailableTags(availableTags));
			fireEvent(new Event.ShowPreviews(previewsToShow));
			fireEvent(new Event.Loading(false));
		});
	}

	private List<PostPreview> activeSetForPage(int activePage) {
		int start = activePage * Consts.PREVIEWS_TO_SHOW;
		List<PostPreview> previewsToShow = activeSet.subList(start,
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

	private MethodCallback<E621PostList> onPostsLoaded = new MethodCallback<E621PostList>() {

		@Override
		public void onSuccess(Method method, E621PostList response) {
			long pageStartId = 0;
			long nextBeforeId = Long.MAX_VALUE;
			for (E621Post post: response) {
				nextBeforeId=Long.min(nextBeforeId, post.getId());
				pageStartId = Long.max(pageStartId, post.getId());
			}

			Set<Long> ids = new HashSet<>();
			for (PostPreview active : activeSet) {
				ids.add(active.getId());
			}
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				if (ids.contains(next.getId())) {
					GWT.log("Removing already have: "+next.getId());
					iter.remove();
					continue;
				}
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
			iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSampleUrl(), next.getFileUrl(),
						next.getCreatedAt().getS(), next.getTags());
				previews.add(preview);
			}
			// remove previews already in the activeset
			previews.removeAll(activeSet);
			activeSet.addAll(previews);
			boolean moreAvailable = nextBeforeId > 0;
			int activePageEnd = (1 + activePage) * Consts.PREVIEWS_TO_SHOW;
			if ((activeSet.size() < activePageEnd) && moreAvailable) {
				final long beforeId = nextBeforeId;
				Scheduler.get().scheduleDeferred(() -> additionalPreviewsLoad(beforeId));
				return;
			}
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			long beforeId = previewsToShow.stream().mapToLong((p) -> p.getId()).min().getAsLong();
			boolean skipForwards = reloadingOnFilterChange //
					&& beforeId > savedPageStartId //
					&& savedPageStartId > 0;
			GWT.log("skip forwards to savedPageStartId: " + savedPageStartId + " [" + skipForwards + "]");
			if (skipForwards && moreAvailable) {
				fireEvent(new Event.NextPreviewSet());
				return;
			}
			fireEvent(new Event.PreviewsLoaded());
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			GWT.log("EXCEPTION: " + String.valueOf(exception.getMessage()), exception);

		}
	};

	@Override
	public void execute() {
		rp = RootPanel.get("e621resteemit");
		rp.clear();
		MainView mainView = new MainView();
		rp.add(mainView);
		eventBinder.bindEventHandlers(this, eventBus);
		setController(new ViewController(mainView.getPanel()));
		fireEvent(new Event.Loading(true));
		fireEvent(new Event.ShowView(View.BrowseView));
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
		Iterator<String> iRatings = mustHaveRatings.iterator();
		while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add("rating:" + iRatings.next());
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
		beforeId = (long) (Math.ceil((double)beforeId / (double)CACHED_PAGE_SIZE) * CACHED_PAGE_SIZE);
		E621PostList cached = INDEX_CACHE.get(sb.toString() + "," + beforeId);
		if (cached == null) {
			E621Api.api().postIndex(sb.toString(), (int) beforeId, CACHED_PAGE_SIZE,
					cacheIndexResponse(sb.toString(), beforeId));
		} else {
			Scheduler.get().scheduleDeferred(() -> onPostsLoaded.onSuccess(null, cached));
		}
	}

	private MethodCallback<E621PostList> cacheIndexResponse(String tags, long minId) {
		return new MethodCallback<E621PostList>() {

			@Override
			public void onSuccess(Method method, E621PostList response) {
				INDEX_CACHE.put(tags + "," + minId, response);
				onPostsLoaded.onSuccess(method, response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				onPostsLoaded.onFailure(method, exception);
			}
		};
	}

	@EventHandler
	protected void initialPreviewsLoad(Event.InitialPreviews event) {
		fireEvent(new Event.Loading(true));
		updateActiveTagFilters();
		activePage = 0;
		activeSet.clear();
		List<String> tags = new ArrayList<>();
		Iterator<String> iRatings = mustHaveRatings.iterator();
		while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			tags.add("rating:" + iRatings.next());
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

}
