package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;

public class Test implements ScheduledCommand {

	@Override
	public void execute() {
		/*
		 * GWT.log is compiled out for production mode, so calling the tests via GWT.log
		 * will cause them to be compiled out for production mode and they will only
		 * exist and run during SDM mode.
		 */
		GWT.log(sdmModeTests());
	}

	private String sdmModeTests() {

		return "=== sdmModeTests#done";
	}

}
