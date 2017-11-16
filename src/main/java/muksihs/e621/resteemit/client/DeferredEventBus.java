package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class DeferredEventBus extends SimpleEventBus {
	private static void defer(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}

	public DeferredEventBus() {
		super();
	}

	@Override
	public void fireEvent(Event<?> event) {
		GWT.log("event: "+event.getClass().getSimpleName());
		defer(() -> super.fireEvent(event));
	}

	@Override
	public void fireEventFromSource(Event<?> event, Object source) {
		defer(() -> super.fireEventFromSource(event, source));
	}

}
