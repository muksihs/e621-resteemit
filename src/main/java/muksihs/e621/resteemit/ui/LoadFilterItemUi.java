package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class LoadFilterItemUi extends EventBusComposite {

	private static LoadFilterItemUiUiBinder uiBinder = GWT.create(LoadFilterItemUiUiBinder.class);

	interface LoadFilterItemUiUiBinder extends UiBinder<Widget, LoadFilterItemUi> {
	}

	public LoadFilterItemUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	interface MyEventBinder extends EventBinder<LoadFilterItemUi>{}
	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
