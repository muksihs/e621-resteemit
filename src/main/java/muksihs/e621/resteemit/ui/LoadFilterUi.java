package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class LoadFilterUi extends EventBusComposite {

	private static LoadFilterUiUiBinder uiBinder = GWT.create(LoadFilterUiUiBinder.class);

	interface LoadFilterUiUiBinder extends UiBinder<Widget, LoadFilterUi> {
	}

	public LoadFilterUi() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	interface MyEventBinder extends EventBinder<LoadFilterUi> {
	}

	@Override
	protected <T extends EventBinder<EventBusComposite>> T getEventBinder() {
		return GWT.create(MyEventBinder.class);
	}

}
