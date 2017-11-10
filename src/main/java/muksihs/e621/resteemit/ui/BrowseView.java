package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import gwt.material.design.client.constants.Color;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.html.Anchor;
import gwt.material.design.client.ui.html.Br;
import gwt.material.design.client.ui.html.Span;
import muksihs.e621.resteemit.client.Event;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.PostPreview;

public class BrowseView extends EventBusComposite {
	
	@UiField MaterialPanel menu;
	@UiField MaterialPanel filterTags;
	@UiField MaterialPanel availableTags;
	@UiField MaterialPanel posts;

	private static BrowseViewUiBinder uiBinder = GWT.create(BrowseViewUiBinder.class);

	interface BrowseViewUiBinder extends UiBinder<Widget, BrowseView> {
	}

	public BrowseView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	interface MyEventBinder extends EventBinder<BrowseView>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		fireEvent(new Event.InitialPreviews());
	}
	
	@EventHandler
	protected void showPreviews(Event.ShowPreviews event) {
		GWT.log("BrowseView#showPreviews");
		GWT.log("previews="+event.getPreviews().size());
		posts.clear();
		for (PostPreview preview: event.getPreviews()) {
			MaterialImage img = new MaterialImage(preview.getSampleUrl());
			img.setHoverable(true);
			Anchor a = new Anchor();
			a.getElement().setAttribute("href", Consts.E621_SHOW_POST+preview.getId());
			a.getElement().setAttribute("target", "_blank");
			a.add(img);
			MaterialPanel panel = new MaterialPanel();
			panel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
			panel.getElement().getStyle().setMargin(1, Unit.PCT);
			String style = panel.getElement().getAttribute("style");
			if (!style.endsWith(";")&&!style.trim().isEmpty()) {
				style+=";";
			}
			style=style+"max-width: 30%;";
			panel.getElement().setAttribute("style", style);
			panel.add(a);
			panel.add(new Br());
			panel.add(new Span(preview.getId()+""));
			posts.add(panel);
		}
	}

}