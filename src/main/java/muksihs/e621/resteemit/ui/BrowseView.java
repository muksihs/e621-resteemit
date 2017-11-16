package muksihs.e621.resteemit.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.ButtonType;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.TextAlign;
import gwt.material.design.client.ui.MaterialAnchorButton;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCheckBox;
import gwt.material.design.client.ui.MaterialCollapsible;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialModal;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.html.Anchor;
import gwt.material.design.client.ui.html.Br;
import muksihs.e621.resteemit.client.Event;
import muksihs.e621.resteemit.client.Event.Rating;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;

public class BrowseView extends EventBusComposite {

	@UiField
	protected MaterialCheckBox ratingSafe;
	@UiField
	protected MaterialCheckBox ratingQuestionable;
	@UiField
	protected MaterialCheckBox ratingExplicit;

	@UiField
	MaterialPanel filterTags;
	@UiField
	MaterialPanel availableTags;
	@UiField
	MaterialPanel posts;
	@UiField
	MaterialButton previous;
	@UiField
	MaterialButton next;

	private static BrowseViewUiBinder uiBinder = GWT.create(BrowseViewUiBinder.class);

	interface BrowseViewUiBinder extends UiBinder<Widget, BrowseView> {
	}

	private void getPrevious(ClickEvent e) {
		fireEvent(new Event.PreviousPreviewSet());
	}

	private void getNext(ClickEvent e) {
		fireEvent(new Event.NextPreviewSet());
	}

	private void updateRatings(MaterialCheckBox checkbox) {
		// if all the checkboxes are being unchecked, reverse them all
		if (ratingSafe.getValue() == false && ratingQuestionable.getValue() == false
				&& ratingExplicit.getValue() == false) {
			ratingSafe.setValue(true);
			ratingQuestionable.setValue(true);
			ratingExplicit.setValue(true);
			checkbox.setValue(false);
		}
		Set<Rating> ratings = new HashSet<>();
		if (ratingSafe.getValue()) {
			ratings.add(Rating.SAFE);
		}
		if (ratingQuestionable.getValue()) {
			ratings.add(Rating.QUESTIONABLE);
		}
		if (ratingExplicit.getValue()) {
			ratings.add(Rating.EXPLICIT);
		}
		// update app
		fireEvent(new Event.SetRating(ratings));
	}

	public BrowseView() {
		initWidget(uiBinder.createAndBindUi(this));
		previous.addClickHandler(this::getPrevious);
		next.addClickHandler(this::getNext);

		ratingSafe.addClickHandler((e) -> updateRatings(ratingSafe));
		ratingQuestionable.addClickHandler((e) -> updateRatings(ratingQuestionable));
		ratingExplicit.addClickHandler((e) -> updateRatings(ratingExplicit));
	}

	interface MyEventBinder extends EventBinder<BrowseView> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
//		if (History.getToken().trim().isEmpty()) {
//			ratingSafe.setValue(true);
//			ratingQuestionable.setValue(false);
//			ratingExplicit.setValue(false);
//			updateRatings(ratingSafe);
//		}
		fireEvent(new Event.BrowseViewLoaded());
	}

	@UiField
	protected MaterialCollapsible tags;

	@EventHandler
	protected void setRatingsBoxes(Event.SetRatingsBoxes event) {
		GWT.log("setRatingsBoxes: "+event.getMustHaveRatings());
		if (event.getMustHaveRatings()==null||event.getMustHaveRatings().isEmpty()) {
			ratingExplicit.setValue(true);
			ratingQuestionable.setValue(true);
			ratingSafe.setValue(true);
			return;
		}
		ratingExplicit.setValue(event.getMustHaveRatings().contains("e"));
		ratingQuestionable.setValue(event.getMustHaveRatings().contains("q"));
		ratingSafe.setValue(event.getMustHaveRatings().contains("s"));
	}
	
	@EventHandler
	protected void enablePreviousButton(Event.EnablePreviousButton event) {
		this.previous.setEnabled(event.isEnable());
	}

	@EventHandler
	protected void showAvailableTags(Event.ShowAvailableTags event) {
		availableTags.clear();
		tags.closeAll();
		Scheduler.get().scheduleDeferred(() -> {
			MaterialPanel panel = new MaterialPanel();
			for (String tag : event.getTags()) {
				MaterialAnchorButton tagLabel = new MaterialAnchorButton(tag);
				tagLabel.addClickHandler((e) -> showAddToFilterDialog(tag));
				tagLabel.setMargin(1);
				panel.add(tagLabel);
			}
			availableTags.add(panel);
		});
	}

	private final Set<String> activeFilterTags = new TreeSet<>();
	@EventHandler
	protected void showFilterTags(Event.ShowFilterTags event) {
		filterTags.clear();
		activeFilterTags.clear();
		tags.closeAll();
		Scheduler.get().scheduleDeferred(() -> {
			for (String tag : event.getTags()) {
				MaterialAnchorButton tagLabel = new MaterialAnchorButton(tag);
				tagLabel.addClickHandler((e) -> showRemoveFromFilterDialog(tag));
				tagLabel.setMargin(1);
				filterTags.add(tagLabel);
				activeFilterTags.add(tag);
			}
		});
	}

	private Void showRemoveFromFilterDialog(String tag) {
		MaterialModal dialog = new MaterialModal();
		dialog.addCloseHandler((e) -> dialog.removeFromParent());
		dialog.setTitle("Remove From Filter");
		MaterialPanel buttons = new MaterialPanel();
		buttons.setTextAlign(TextAlign.CENTER);
		MaterialButton remove = new MaterialButton("Remove From Filter: " + tag);
		MaterialButton cancel = new MaterialButton("Cancel");
		remove.addClickHandler((e) -> {
			dialog.close();
			fireEvent(new Event.RemoveFromFilter(tag));
		});
		remove.setMargin(2);
		cancel.addClickHandler((e) -> dialog.close());
		cancel.setMargin(2);
		buttons.add(remove);
		buttons.add(cancel);
		dialog.add(buttons);
		RootPanel.get().add(dialog);
		dialog.open();
		return null;
	}

	private Void showAddToFilterDialog(String tag) {
		MaterialModal dialog = new MaterialModal();
		dialog.addCloseHandler((e) -> dialog.removeFromParent());
		dialog.setTitle("Add To Filter");
		MaterialPanel buttons = new MaterialPanel();
		buttons.setTextAlign(TextAlign.CENTER);
		MaterialButton include = new MaterialButton("Only Show Posts With " + tag);
		MaterialButton exclude = new MaterialButton("Do Not Show Posts With " + tag);
		MaterialButton cancel = new MaterialButton("Cancel");
		include.addClickHandler((e) -> {
			dialog.close();
			fireEvent(new Event.AddToIncludeFilter(tag));
		});
		include.setMargin(2);
		exclude.addClickHandler((e) -> {
			dialog.close();
			fireEvent(new Event.AddToExcludeFilter(tag));
		});
		exclude.setMargin(2);
		cancel.addClickHandler((e) -> dialog.close());
		cancel.setMargin(2);
		buttons.add(exclude);
		buttons.add(include);
		buttons.add(cancel);
		dialog.add(buttons);
		RootPanel.get().add(dialog);
		dialog.open();
		return null;
	}

	@EventHandler
	protected void showPreviews(Event.ShowPreviews event) {
		posts.clear();
		for (PostPreview preview : event.getPreviews()) {
			MaterialImage img = new MaterialImage(preview.getSampleUrl());
			img.setMargin(2);
			img.setHoverable(true);
			img.setTitle("#" + preview.getId() + " " + preview.getTags());
			Anchor a = new Anchor();
			String href = Consts.E621_SHOW_POST + preview.getId();
			a.getElement().setAttribute("href", href);
			a.getElement().setAttribute("target", "_blank");
			a.add(img);
			MaterialPanel panel = new MaterialPanel();
			panel.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
			panel.getElement().getStyle().setPadding(1, Style.Unit.PCT);
			panel.getElement().getStyle().setMargin(0, Style.Unit.PCT);
			String style = panel.getElement().getAttribute("style");
			if (!style.endsWith(";") && !style.trim().isEmpty()) {
				style += ";";
			}
			style += "max-width: 400px; max-height: 400px; margin: 6px;";
			panel.getElement().setAttribute("style", style);
			panel.add(a);
			panel.add(new Br());
			MaterialButton showTagFilterDialog = new MaterialButton();
			showTagFilterDialog.setMargin(2);
			showTagFilterDialog.setText("VIEW TAGS");
			showTagFilterDialog.addClickHandler((e) -> showPostTags(preview.getTags()));
			panel.add(showTagFilterDialog);
			MaterialLink viewPostLink = new MaterialLink();
			viewPostLink.setMargin(2);
			viewPostLink.setTarget("_blank");
			viewPostLink.setHref(href);
			viewPostLink.setText("VIEW POST");
			viewPostLink.setType(ButtonType.RAISED);
			panel.add(viewPostLink);
			posts.add(panel);
		}
	}

	private void showPostTags(String tags) {
		MaterialModal modal = new MaterialModal();
		modal.addCloseHandler((e) -> modal.removeFromParent());
		MaterialPanel panel = new MaterialPanel();
		for (String tag : tags.split("\\s+")) {
			MaterialAnchorButton tagLabel = new MaterialAnchorButton(tag);
			tagLabel.addClickHandler((e) -> showAddToFilterDialog(tag));
			tagLabel.addClickHandler((e) -> modal.close());
			tagLabel.setMargin(1);
			if (activeFilterTags.contains("+"+tag)) {
				tagLabel.setEnabled(false);
				tagLabel.setBackgroundColor(Color.LIGHT_GREEN);
			}
			panel.add(tagLabel);
		}
		MaterialButton cancel = new MaterialButton("DISMISS");
		cancel.setBackgroundColor(Color.GREEN_LIGHTEN_1);
		cancel.setTextColor(Color.WHITE);
		cancel.setMargin(1);
		cancel.addClickHandler((e)->modal.close());
		panel.add(cancel);
		modal.add(panel);
		RootPanel.get().add(modal);
		modal.open();
	}

}
