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
import muksihs.e621.resteemit.client.Event.Rating;
import muksihs.e621.resteemit.client.cache.IndexCache;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus {

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
		mustHaveTags.remove(event.getTag().substring(1));
		mustNotHaveTags.remove(event.getTag().substring(1));
		fireEvent(new Event.InitialPreviews());
	}

	@EventHandler
	protected void setRating(Event.SetRating event) {
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
		mustHaveTags.add(event.getTag());
		fireEvent(new Event.InitialPreviews());
	}

	@EventHandler
	protected void addToExcludeFilter(Event.AddToExcludeFilter event) {
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
		List<PostPreview> previewsToShow = activeSetForPage(activePage);
		Set<String> availableTags = new TreeSet<>();
		for (PostPreview preview : previewsToShow) {
			String[] tags = preview.getTags().split("\\s+");
			for (String tag : tags) {
				availableTags.add(tag);
			}
		}
		availableTags.removeAll(mustHaveTags);
		availableTags.removeAll(mustNotHaveTags);
		fireEvent(new Event.ShowAvailableTags(availableTags));
		fireEvent(new Event.ShowPreviews(previewsToShow));
		fireEvent(new Event.Loading(false));
	}

	public List<PostPreview> activeSetForPage(int activePage) {
		int start = activePage * Consts.PREVIEWS_TO_SHOW;
		List<PostPreview> previewsToShow = activeSet.subList(start,
				Math.min(start + Consts.PREVIEWS_TO_SHOW, activeSet.size()));
		return previewsToShow;
	}

	private MethodCallback<List<E621Post>> onPostsLoaded = new MethodCallback<List<E621Post>>() {

		@Override
		public void onSuccess(Method method, List<E621Post> response) {
			long maxId = 0;
			long minId = 0;
			try {
				minId = response.stream().mapToLong((p) -> p.getId()).min().getAsLong();
				GWT.log("minId: " + minId);
				maxId = response.stream().mapToLong((p) -> p.getId()).max().getAsLong();
				GWT.log("maxId: " + maxId);
			} catch (Exception e) {
			}
			int size = response.size();
			GWT.log("have " + size + " posts to examine");

			Set<Long> ids = new HashSet<>();
			for (PostPreview active : activeSet) {
				ids.add(active.getId());
			}
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				if (ids.contains(next.getId())) {
					iter.remove();
				}
			}
			GWT.log("Removed " + (size - response.size()) + " posts already in active set");
			size = response.size();

			if (!mustHaveRatings.isEmpty()) {
				response.removeIf((p) -> !mustHaveRatings.contains(p.getRating()));
			}
			GWT.log("removed " + (size - response.size()) + " posts that did not match desired ratings");
			size = response.size();

			response.removeIf((p) -> !extensionsWhitelist.contains(p.getFileExt().toLowerCase()));
			GWT.log("removed " + (size - response.size()) + " non-image posts");
			size = response.size();
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
			GWT.log("removed " + (size - response.size()) + " must not have tag posts");
			size = response.size();

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
			GWT.log("removed " + (size - response.size()) + " did not have tag posts");

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
			GWT.log("Have " + activeSet.size() + " previews available for display.");
			if (activeSet.size() < (1 + activePage) * Consts.PREVIEWS_TO_SHOW && minId > 0) {
				final long beforeId = minId;
				Scheduler.get().scheduleDeferred(() -> additionalPreviewsLoad(beforeId));
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
		long minId = previewsToShow.stream().mapToLong((p) -> p.getId()).min().getAsLong();
		activePage++;
		additionalPreviewsLoad(minId);
	}

	protected void additionalPreviewsLoad(long beforeId) {
		fireEvent(new Event.Loading(true));
		GWT.log("additionalPreviewsLoad: " + beforeId);
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
		// keep beforeId aligned with multiples of 10 so the cache works correctly
		beforeId = (long) (Math.ceil(beforeId / 10) * 10);
		List<E621Post> cached = new IndexCache().get(sb.toString() + "," + beforeId);
		if (cached == null) {
			E621Api.api().index(sb.toString(), (int) beforeId, 10, cacheIndexResponse(sb.toString(), beforeId));
		} else {
			Scheduler.get().scheduleDeferred(() -> onPostsLoaded.onSuccess(null, cached));
		}
	}

	private MethodCallback<List<E621Post>> cacheIndexResponse(String tags, long minId) {
		return new MethodCallback<List<E621Post>>() {

			@Override
			public void onSuccess(Method method, List<E621Post> response) {
				new IndexCache().put(tags + "," + minId, response);
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
		E621Api.api().index(sb.toString(), 10, onPostsLoaded);
	}

}
