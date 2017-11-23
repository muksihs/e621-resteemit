package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.Color;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialTitle;
import gwt.material.design.client.ui.MaterialToast;
import muksihs.e621.resteemit.ui.AboutUi;
import muksihs.e621.resteemit.ui.BrowseView;
import muksihs.e621.resteemit.ui.LoginUi;

public class ViewController implements GlobalEventBus {

	interface MyEventBinder extends EventBinder<ViewController>{}
	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	private final Panel view;

	public ViewController(Panel view) {
		this.view=view;
		eventBinder.bindEventHandlers(this, eventBus);
	}
	
	@EventHandler
	protected void quickMessage(Event.QuickMessage event) {
		MaterialToast.fireToast(event.getMessage(), 1000);
	}
	
	@EventHandler
	protected void showAboutui(Event.ShowAbout event) {
		AboutUi about = new AboutUi();
		RootPanel.get().add(about);
		about.open();
	}
	
	@EventHandler
	protected void showLoginUi(Event.ShowLoginUi event) {
		LoginUi loginUi = new LoginUi();
		RootPanel.get().add(loginUi);
		loginUi.open();
	}
	
	@EventHandler
	protected void alertMessage(Event.AlertMessage event) {
		MaterialModal modal = new MaterialModal();
		modal.setDismissible(true);
		modal.setTitle("Alert!");
		modal.addCloseHandler((e)->modal.removeFromParent());
		MaterialTitle title = new MaterialTitle(event.getMessage());
		title.setMarginTop(-50);
		modal.add(title);
		MaterialButton btnOk = new MaterialButton("OK");
		btnOk.setMargin(4);
		btnOk.addClickHandler((e)->modal.close());
		modal.add(btnOk);
		RootPanel.get().add(modal);
		modal.open();
	}
	
	@EventHandler
	protected void fatalError(Event.FatalError event) {
		MaterialModal modal = new MaterialModal();
		modal.setTitle("FATAL ERROR!");
		modal.setBackgroundColor(Color.RED);
		MaterialTitle title = new MaterialTitle("FATAL ERROR!");
		title.setMarginTop(-50);
		modal.add(title);
		MaterialLabel label = new MaterialLabel(event.getMessage());
		modal.add(label);
		label = new MaterialLabel("* App will reload in a moment *");
		modal.add(label);
		modal.setDismissible(false);
		modal.addCloseHandler((e)->modal.removeFromParent());
		RootPanel.get().add(modal);
		modal.open();
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
