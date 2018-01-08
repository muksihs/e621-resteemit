package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;

public class E621EP implements EntryPoint, IsSdm {

	private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void onUncaughtException(Throwable e) {
			GWT.log(e.getMessage() == null ? "" : e.getMessage(), e);
		}
	};
	private Callback<Void, Exception> injected = new Callback<Void, Exception>() {
		@Override
		public void onFailure(Exception reason) {
			GWT.log(reason.getMessage(), reason);
			setSteemJsOptions();
			Scheduler.get().scheduleDeferred(new E621ResteemitApp());
		}

		@Override
		public void onSuccess(Void result) {
			setSteemJsOptions();
			Scheduler.get().scheduleDeferred(new E621ResteemitApp());
		}
	};

	private static native void setSteemJsOptions()/*-{
		$wnd.steem.api.setOptions({
			url : 'https://api.steemit.com'
		});
	}-*/;

	@Override
	public void onModuleLoad() {
		GWT.log("onModuleLoad");
		GWT.setUncaughtExceptionHandler(handler);
		try {
			// Location.getProtocol() +
			String scriptUrl = "https://cdn.steemjs.com/lib/latest/steem.min.js";
			GWT.log("steemjs CDN: " + scriptUrl);
			ScriptInjector.fromUrl(scriptUrl)//
					.setRemoveTag(false)//
					.setWindow(ScriptInjector.TOP_WINDOW)//
					.setCallback(injected).inject();
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
	}

}
