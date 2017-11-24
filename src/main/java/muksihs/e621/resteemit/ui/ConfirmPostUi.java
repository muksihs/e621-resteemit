package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import muksihs.e621.resteemit.client.Event;

public class ConfirmPostUi extends EventBusComposite {

	private static ConfirmPostUiBinder uiBinder = GWT.create(ConfirmPostUiBinder.class);

	interface ConfirmPostUiBinder extends UiBinder<Widget, ConfirmPostUi> {
	}

	@UiField
	protected MaterialModal modal;
	@UiField
	MaterialPanel preview;
	@UiField
	MaterialButton btnPost;
	@UiField
	MaterialButton btnCancel;

	public ConfirmPostUi() {
		initWidget(uiBinder.createAndBindUi(this));
		modal.addCloseHandler((e)->this.removeFromParent());
		btnPost.addClickHandler((e)->{
			fireEvent(new Event.DoPost());
			modal.close();
			}
		);
		btnCancel.addClickHandler((e)->modal.close());
	}

	interface MyEventBinder extends EventBinder<ConfirmPostUi> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	@EventHandler
	protected void postPreviewContent(Event.PostPreviewContent event) {
		preview.getElement().setInnerHTML(event.getHtml());
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetPostPreview());
	}

	public void open() {
		modal.open();
	}
}
