package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialModal;

public class AboutUi extends EventBusComposite {

	private static AboutUiUiBinder uiBinder = GWT.create(AboutUiUiBinder.class);

	interface AboutUiUiBinder extends UiBinder<Widget, AboutUi> {
	}
	
	@UiField
	protected MaterialModal modal;
	@UiField
	protected MaterialButton btnOk;

	public AboutUi() {
		initWidget(uiBinder.createAndBindUi(this));
		btnOk.addClickHandler((e)->modal.close());
		modal.addCloseHandler((e)->this.removeFromParent());
	}

	interface MyEventBinder extends EventBinder<AboutUi>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	public void open() {
		modal.open();
	}
}
