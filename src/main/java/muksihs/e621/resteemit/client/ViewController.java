package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialLoader;
import muksihs.e621.resteemit.ui.BrowseView;

public class ViewController implements GlobalEventBus {

	interface MyEventBinder extends EventBinder<ViewController>{}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private final Panel view;

	public ViewController(Panel view) {
		this.view=view;
		eventBinder.bindEventHandlers(this, eventBus);
	}
	
	@EventHandler
	protected void showLoading(Event.Loading event) {
		MaterialLoader.loading(event.isLoading());
	}
	
	private Composite activeView;
	private void replaceView(Composite newView) {
		if (activeView!=null) {
			activeView.removeFromParent();
		}
		activeView=newView;
		this.view.add(newView);
	}
	
	@EventHandler
	protected void showView(Event.ShowView event) {
		switch(event.getView()) {
		case BrowseView:
			replaceView(new BrowseView());
			break;
		}
	}

}
