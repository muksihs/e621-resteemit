package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.ui.MaterialAnchorButton;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCheckBox;
import gwt.material.design.client.ui.MaterialInput;
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
	protected MaterialPanel preview;
	@UiField
	protected MaterialButton btnPost;
	@UiField
	protected MaterialButton btnCancel;
	@UiField
	protected MaterialInput title;
	@UiField
	protected MaterialPanel tags;
	@UiField
	protected MaterialCheckBox upvote;

	public ConfirmPostUi() {
		initWidget(uiBinder.createAndBindUi(this));
		modal.addCloseHandler((e) -> this.removeFromParent());
		btnPost.addClickHandler((e) -> {
			fireEvent(new Event.DoPost(title.getValue()));
			modal.close();
		});
		btnCancel.addClickHandler((e) -> modal.close());
		upvote.addClickHandler((e)->fireEvent(new Event.UpdateUpvotePreference(upvote.getValue())));
	}

	interface MyEventBinder extends EventBinder<ConfirmPostUi> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@EventHandler
	protected void setAutomaticTags(Event.SetAutomaticTags event) {
		tags.clear();
		for (String tag : event.getTagsForpost()) {
			MaterialAnchorButton tagLabel = new MaterialAnchorButton(tag);
			tagLabel.setMargin(1);
			tagLabel.setEnabled(false);
			tags.add(tagLabel);
			tags.getElement().appendChild(Document.get().createTextNode(" "));
		}
	}

	@EventHandler
	protected void postPreviewContent(Event.PostPreviewContent event) {
		preview.getElement().setInnerHTML(event.getHtml());
	}

	@EventHandler
	protected void setTitle(Event.SetPostTitle event) {
		title.setValue(event.getTitle());
	}
	
	@EventHandler
	protected void setUpvotePreference(Event.SetUpvotePreference event) {
		upvote.setValue(event.isUpvote());
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.GetPostPreview());
		fireEvent(new Event.GetAutomaticTitle());
		fireEvent(new Event.GetAutomaticTags());
		fireEvent(new Event.GetUpvotePreference());
	}

	public void open() {
		modal.open();
	}
}
