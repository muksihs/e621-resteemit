package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialModal;
import muksihs.e621.resteemit.client.E621ResteemitApp.MostRecentPostInfo;
import muksihs.e621.resteemit.client.Event;

public class PostDoneUi extends EventBusComposite {

	private static PostDoneUiUiBinder uiBinder = GWT.create(PostDoneUiUiBinder.class);

	interface PostDoneUiUiBinder extends UiBinder<Widget, PostDoneUi> {
	}

	@UiField
	protected MaterialModal modal;
	@UiField
	protected MaterialButton btnOk;
	@UiField
	protected MaterialLink steemit;

	@UiField
	protected MaterialLink chainbb;

	@UiField
	protected MaterialLink busyorg;
	
	public PostDoneUi() {
		initWidget(uiBinder.createAndBindUi(this));
		btnOk.addClickHandler((e)->modal.close());
		modal.addCloseHandler((e)->this.removeFromParent());
	}

	interface MyEventBinder extends EventBinder<PostDoneUi>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	public void open() {
		modal.open();
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetMostRecentPostInfo());
	}

	@EventHandler
	protected void linkInfo(Event.SetMostRecentPostInfo event) {
		MostRecentPostInfo info = event.getInfo();
		String href = info.firstTag + "/" + "@" + info.author + "/" + info.permLink;
		steemit.setHref("https://www.steemit.com/" + href);
		steemit.setEnabled(true);
		chainbb.setHref("https://chainbb.com/" + href);
		chainbb.setEnabled(true);
		busyorg.setHref("https://www.busy.org/" + href);
		busyorg.setEnabled(true);
	}

}
