package e621;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.ServiceRoots;

import com.google.gwt.core.client.GWT;

import muksihs.e621.resteemit.client.IsSdm;

public class E621Api implements IsSdm {
	private static E621RestApi instance;
	protected E621Api() {};
	public static E621RestApi api() {
		if (instance==null) {
			Defaults.setAddXHttpMethodOverrideHeader(false);
			Defaults.setRequestTimeout(5000);
			ServiceRoots.add("E621", "https://e621.net");
			instance=GWT.create(E621RestApi.class);
		}
		return instance;
	}
}
