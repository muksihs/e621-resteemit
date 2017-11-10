package muksihs.e621.resteemit.client;

import java.util.ArrayList;
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

	private static final int MAX_TAGS_PER_QUERY = 6;

	interface MyEventBinder extends EventBinder<E621ResteemitApp>{}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private RootPanel rp;
	private ViewController controller;
	
	private final Set<String> mustHaveTags=new TreeSet<>();
	private final Set<String> mustNotHaveTags=new TreeSet<>();
	private final List<E621Post> activeSet=new ArrayList<>();

	private MethodCallback<List<E621Post>> onPostsLoaded=new MethodCallback<List<E621Post>>() {
		@Override
		public void onSuccess(Method method, List<E621Post> response) {
			try {
				long minId = response.stream().mapToLong((p)->p.getId()).min().getAsLong();
				GWT.log("minId: "+minId);
			} catch (Exception e) {
			}
			int size = response.size();
			GWT.log("have "+size+" posts to examine");
			response.removeIf((p)->p.getFileExt().endsWith("swf"));
			GWT.log("removed "+(size-response.size())+" non-image posts");
			size = response.size();
			response.removeIf((p)->{
				String[] tags = p.getTags().split("\\s+");
				//first scan for must remove tags
				for (String tag:tags) {
					tag=tag.toLowerCase().trim();
					if (mustNotHaveTags.contains(tag)) {
						return true; //remove post
					}
				}
				//second scan for must have tags
				for (String tag:tags) {
					tag=tag.toLowerCase().trim();
					if (mustHaveTags.contains(tag)) {
						return false; //keep post
					}
				}
				return true; //remove post
			});
			GWT.log("removed "+(size-response.size())+" unwanted posts");
			List<PostPreview> previews = new ArrayList<>();
			Iterator<E621Post> iter = response.iterator();
			while (iter.hasNext()) {
				E621Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSampleUrl(), next.getFileUrl(), next.getCreatedAt().getS());
				previews.add(preview);
			}
			GWT.log("Have "+previews.size()+" previews to display.");
			fireEvent(new Event.ShowPreviews(previews));
			fireEvent(new Event.Loading(false));
		}
		
		@Override
		public void onFailure(Method method, Throwable exception) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	public void execute() {
		mustHaveTags.add("gay");
		mustHaveTags.add("anal_penetration");
		mustHaveTags.add("canine");
		mustHaveTags.add("bestiality");
//		mustHaveTags.add("forced");
		mustHaveTags.add("from_behind_position");
		
		mustNotHaveTags.add("young");
		mustNotHaveTags.add("feline");
		
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
	protected void initialPreviewsLoad(Event.InitialPreviews event) {
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
		E621Api.api().index(sb.toString(), 16, onPostsLoaded);
	}

}
