package muksihs.e621.resteemit.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;

import muksihs.e621.resteemit.client.GlobalEventBus;

public abstract class EventBusComposite extends Composite implements GlobalEventBus {

	interface MyEventBinder extends EventBinder<EventBusComposite> {
	}

	protected HandlerRegistration registration;

	public EventBusComposite() {
	}

	protected abstract <T extends EventBinder<EventBusComposite>> T getEventBinder();

	@Override
	protected void onLoad() {
		GWT.log(this.getClass().getSimpleName()+"#onLoad");
		try {
			registration = getEventBinder().bindEventHandlers(this, eventBus);
			super.onLoad();
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
	}

	@Override
	protected void onUnload() {
		try {
			registration.removeHandler();
			super.onUnload();
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
	};
}
