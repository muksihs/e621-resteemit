package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;

import gwt.material.design.client.ui.MaterialLoader;

public class E621EP implements EntryPoint, IsSdm {

	@Override
	public void onModuleLoad() {
		if (isSdm()) {
			Scheduler.get().scheduleDeferred(new BasicApiTests());
		}
		Scheduler.get().scheduleDeferred(new E621ResteemitApp());
	}

}
