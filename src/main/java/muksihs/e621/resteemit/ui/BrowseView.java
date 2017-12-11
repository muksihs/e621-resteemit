package muksihs.e621.resteemit.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
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
import gwt.material.design.client.ui.html.Br;
import muksihs.e621.resteemit.client.Event;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.E621Rating;
import muksihs.e621.resteemit.shared.PostPreview;

public class BrowseView extends EventBusComposite {

	@UiField
	MaterialPanel posts;
	@UiField
	MaterialPanel filterTags;
	@UiField
	MaterialPanel availableTags;
	
	@UiField
	protected MaterialCheckBox ratingSafe;
	@UiField
	protected MaterialCheckBox ratingQuestionable;
	@UiField
	protected MaterialCheckBox ratingExplicit;
	@UiField
	MaterialButton loadFilter;
	@UiField
	MaterialButton saveFilter;
	@UiField
	MaterialButton clearSearch;
	@UiField
	MaterialButton mostRecent;
	@UiField
	MaterialButton previous;
	@UiField
	MaterialButton next;
	
	@UiField
	MaterialButton previousBtm;
	@UiField
	MaterialButton nextBtm;

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
		Set<E621Rating> e621Ratings = new HashSet<>();
		if (ratingSafe.getValue()) {
			e621Ratings.add(E621Rating.SAFE);
		}
		if (ratingQuestionable.getValue()) {
			e621Ratings.add(E621Rating.QUESTIONABLE);
		}
		if (ratingExplicit.getValue()) {
			e621Ratings.add(E621Rating.EXPLICIT);
		}
		// update app
		fireEvent(new Event.SetRating(e621Ratings));
	}

	public BrowseView() {
		initWidget(uiBinder.createAndBindUi(this));
		previous.addClickHandler(this::getPrevious);
		next.addClickHandler(this::getNext);
		previousBtm.addClickHandler(this::getPrevious);
		nextBtm.addClickHandler(this::getNext);
		mostRecent.addClickHandler((e) -> fireEvent(new Event.MostRecentSet()));
		ratingSafe.addClickHandler((e) -> updateRatings(ratingSafe));
		ratingQuestionable.addClickHandler((e) -> updateRatings(ratingQuestionable));
		ratingExplicit.addClickHandler((e) -> updateRatings(ratingExplicit));
		clearSearch.addClickHandler((e)->fireEvent(new Event.ClearSearch()));
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
		fireEvent(new Event.BrowseViewLoaded());
	}

	@UiField
	protected MaterialCollapsible tags;

	@EventHandler
	protected void setRatingsBoxes(Event.SetRatingsBoxes event) {
		GWT.log("setRatingsBoxes: " + event.getMustHaveRatings());
		if (event.getMustHaveRatings() == null || event.getMustHaveRatings().isEmpty()) {
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
		this.previousBtm.setEnabled(event.isEnable());
	}

	@EventHandler
	protected void showAvailableTags(Event.ShowAvailableTags event) {
		availableTags.clear();
		tags.closeAll();
		Scheduler.get().scheduleDeferred(() -> {
			MaterialPanel panel = new MaterialPanel();
			for (String tag : event.getTags()) {
				MaterialAnchorButton tagLabel = new MaterialAnchorButton(tag);
				if (tag.startsWith("-")||tag.startsWith("+")) {
					tagLabel.setEnabled(false);
				} else {
					tagLabel.addClickHandler((e) -> showAddToFilterDialog(tag));
				}
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
				if (tag.startsWith("-")) {
					tagLabel.setBackgroundColor(Color.RED);
				}
				if (tag.startsWith("+")) {
					tagLabel.setBackgroundColor(Color.GREEN);
				}
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
		Window.scrollTo(0, 0);
		posts.clear();
		for (PostPreview preview : event.getPreviews()) {
			MaterialImage img = new MaterialImage(preview.getSampleUrl());
			img.setWidth("100%");
			img.setMaxWidth("100%");
			img.setMargin(2);
			img.setHoverable(true);
			img.setTitle("#" + preview.getId() + " " + preview.getTags());
			img.addClickHandler((e)->fireEvent(new Event.ZoomImage(preview)));
			String href = Consts.E621_SHOW_POST + preview.getId();
			MaterialButton viewTags = new MaterialButton();
			viewTags.setWidth("45%");
			viewTags.setMargin(2);
			viewTags.setText("TAGS VIEW");
			viewTags.addClickHandler((e) -> showPostTags(preview.getTags()));
			MaterialButton zoomImage = new MaterialButton();
			zoomImage.setWidth("45%");
			zoomImage.setMargin(2);
			zoomImage.setText("ZOOM IMAGE");
			zoomImage.addClickHandler((e)->fireEvent(new Event.ZoomImage(preview)));
			MaterialButton steemPost = new MaterialButton();
			steemPost.setWidth("45%");
			steemPost.setMargin(2);
			steemPost.setText("STEEMIT!");
			steemPost.addClickHandler((e) -> fireEvent(new Event.SteemPost(preview)));
			MaterialLink e621Post = new MaterialLink();
			e621Post.setWidth("45%");
			e621Post.setMargin(2);
			e621Post.setTarget("_blank");
			e621Post.setHref(href);
			e621Post.setText("E621 POST");
			e621Post.setType(ButtonType.RAISED);
			
			MaterialPanel panel = new MaterialPanel();
			panel.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
			panel.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
			panel.getElement().getStyle().setMargin(4, Style.Unit.PX);
			String style = panel.getElement().getAttribute("style");
			if (!style.endsWith(";") && !style.trim().isEmpty()) {
				style += ";";
			}
			style += "max-width: 400px; max-height: 100%;";
			panel.getElement().setAttribute("style", style);
			panel.add(img);
			panel.add(new Br());
			panel.add(viewTags);
			panel.add(zoomImage);
			panel.add(e621Post);
			panel.add(steemPost);
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
			if (activeFilterTags.contains("+" + tag)) {
				tagLabel.setEnabled(false);
				tagLabel.setBackgroundColor(Color.LIGHT_GREEN);
			}
			panel.add(tagLabel);
		}
		MaterialButton cancel = new MaterialButton("DISMISS");
		cancel.setBackgroundColor(Color.GREEN_LIGHTEN_1);
		cancel.setTextColor(Color.WHITE);
		cancel.setMargin(1);
		cancel.addClickHandler((e) -> modal.close());
		panel.add(cancel);
		modal.add(panel);
		RootPanel.get().add(modal);
		modal.open();
	}

}
