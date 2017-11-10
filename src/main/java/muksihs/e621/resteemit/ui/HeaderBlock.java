package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialTitle;
import muksihs.e621.resteemit.client.Event;

public class HeaderBlock extends EventBusComposite {

	interface HeaderBlockUiBinder extends UiBinder<Widget, HeaderBlock> {
	}

	interface MyEventBinder extends EventBinder<HeaderBlock> {
	}

	private static String versionTxt = "19000101";

	private static HeaderBlockUiBinder uiBinder = GWT.create(HeaderBlockUiBinder.class);

	@UiField
	protected MaterialLabel version;
	@UiField
	protected MaterialTitle title;

	public HeaderBlock() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		version.setText(versionTxt);
		title.getElement().getStyle().setMarginTop(-50, Unit.PX);
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetAppVersion());
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}

	@EventHandler
	public void setAppVersion(Event.AppVersion event) {
		version.setText(event.getAppVersion());
		versionTxt = event.getAppVersion();
	}
}
