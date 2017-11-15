package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;

import elemental2.dom.DomGlobal;

public class E621EP implements EntryPoint, IsSdm {

	private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void onUncaughtException(Throwable e) {
			GWT.log(e.getMessage() == null ? "" : e.getMessage(), e);
		}
	};

	@Override
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(handler);
		Scheduler.get().scheduleDeferred(new E621ResteemitApp());
	}

}
