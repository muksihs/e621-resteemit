package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public interface GlobalEventBus {
	EventBus eventBus = new DeferredEventBus() {
		@Override
		public void fireEvent(com.google.web.bindery.event.shared.Event<?> event) {
			if (event != null) {
			} else {
				GWT.log("null event!");
			}
			super.fireEvent(event);
		};

		@Override
		public void fireEventFromSource(com.google.web.bindery.event.shared.Event<?> event, Object source) {
			if (event != null) {
			} else {
				GWT.log("null event!");
			}
			super.fireEventFromSource(event, source);
		};
	};

	default void fireEvent(GenericEvent event) {
		eventBus.fireEvent(event);
	}
}
