package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus {

	interface MyEventBinder extends EventBinder<E621ResteemitApp>{}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private RootPanel rp;
	private ViewController controller;

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

}
