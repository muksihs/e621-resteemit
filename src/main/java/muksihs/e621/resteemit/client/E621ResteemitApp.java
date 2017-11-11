package muksihs.e621.resteemit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import e621.E621Api;
import e621.models.post.index.E621Post;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus {
	
	public E621ResteemitApp() {
		extensionsWhitelist.addAll(Arrays.asList("png", "jpg", "gif", "jpeg"));
	}

	private static final int MAX_TAGS_PER_QUERY = 6;

	interface MyEventBinder extends EventBinder<E621ResteemitApp>{}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private RootPanel rp;
	private ViewController controller;
	
	private final Set<String> mustHaveTags=new TreeSet<>();
	private final Set<String> mustNotHaveTags=new TreeSet<>();
	private final List<PostPreview> activeSet=new ArrayList<>();
	private final Set<String> extensionsWhitelist= new TreeSet<>();
	private final Set<String> availableTags = new TreeSet<>();

	private MethodCallback<List<E621Post>> onPostsLoaded=new MethodCallback<List<E621Post>>() {

		@Override
		public void onSuccess(Method method, List<E621Post> response) {
			long minId=0;
			try {
				minId = response.stream().mapToLong((p)->p.getId()).min().getAsLong();
				GWT.log("minId: "+minId);
			} catch (Exception e) {
			}
			int size = response.size();
			GWT.log("have "+size+" posts to examine");
			response.removeIf((p)->!extensionsWhitelist.contains(p.getFileExt().toLowerCase()));
			GWT.log("removed "+(size-response.size())+" non-image posts");
			size = response.size();
			response.removeIf((p)->{
				String[] tags = p.getTags().split("\\s+");
				//first scan for must remove tags
				if (!mustNotHaveTags.isEmpty()) {
					for (String tag:tags) {
						tag=tag.toLowerCase().trim();
						if (mustNotHaveTags.contains(tag)) {
							return true; //remove post
						}
					}
				}
				//second scan for must have tags
				for (String tag:tags) {
					tag=tag.toLowerCase().trim();
					if (mustHaveTags.contains(tag)) {
						return false; //keep post
					}
				}
				 //remove post only if we have tags that must be matched
				return !mustHaveTags.isEmpty(); 
			});
			GWT.log("removed "+(size-response.size())+" unwanted posts");
			List<PostPreview> previews = new ArrayList<>();
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSampleUrl(), next.getFileUrl(), next.getCreatedAt().getS());
				previews.add(preview);
				String[] tags = next.getTags().split("\\s+");
				for (String tag: tags) {
					availableTags.add(tag);
				}
			}
			activeSet.addAll(previews);
			GWT.log("Have "+activeSet.size()+" previews to display.");
			if (activeSet.size()<Consts.MIN_PREVIEWS && minId>0) {
				additionalPreviewsLoad(minId);
				return;
			}
			if (activeSet.size()>Consts.MIN_PREVIEWS) {
				activeSet.subList(Consts.MIN_PREVIEWS, activeSet.size()).clear();
			}
			fireEvent(new Event.ShowPreviews(activeSet));
			fireEvent(new Event.ShowAvailableTags(availableTags));
			fireEvent(new Event.Loading(false));
		}
		
		@Override
		public void onFailure(Method method, Throwable exception) {
			GWT.log("EXCEPTION: "+String.valueOf(exception.getMessage()), exception);
			
		}
	};

	@Override
	public void execute() {
//		mustHaveTags.add("male/male");
//		mustHaveTags.add("anal_penetration");
//		mustHaveTags.add("canine");
//		mustHaveTags.add("bondage");
		
//		mustNotHaveTags.add("young");
//		mustNotHaveTags.add("girly");
//		mustNotHaveTags.add("blood");
//		mustNotHaveTags.add("feline");
//		mustNotHaveTags.add("equine");
//		mustNotHaveTags.add("dragon");
//		mustNotHaveTags.add("pokémon_(species)");
//		mustNotHaveTags.add("avian");
//		mustNotHaveTags.add("fish");
//		mustNotHaveTags.add("alien");
//		mustNotHaveTags.add("scalie");
//		mustNotHaveTags.add("reptile");
//		mustNotHaveTags.add("size_difference");
		
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
	
	protected void additionalPreviewsLoad(long minId) {
		GWT.log("additionalPreviewsLoad: "+minId);
		List<String> tags = new ArrayList<>();
		Iterator<String> iMust = mustHaveTags.iterator();
		while (iMust.hasNext() && tags.size()<MAX_TAGS_PER_QUERY) {
			tags.add(iMust.next());
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		while (iMustNot.hasNext() && tags.size()<MAX_TAGS_PER_QUERY) {
			tags.add("-"+iMustNot.next());
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		E621Api.api().index(sb.toString(), (int) minId, 3, onPostsLoaded);
	}
	
	@EventHandler
	protected void initialPreviewsLoad(Event.InitialPreviews event) {
		activeSet.clear();
		availableTags.clear();
		List<String> tags = new ArrayList<>();
		Iterator<String> iMust = mustHaveTags.iterator();
		while (iMust.hasNext() && tags.size()<MAX_TAGS_PER_QUERY) {
			tags.add(iMust.next());
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		while (iMustNot.hasNext() && tags.size()<MAX_TAGS_PER_QUERY) {
			tags.add("-"+iMustNot.next());
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		E621Api.api().index(sb.toString(), 3, onPostsLoaded);
	}

}
