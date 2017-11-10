package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialLoader;

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

}
