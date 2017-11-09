package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;

public interface IsSdm {
	default boolean isSdm() {
		boolean sdm=false;
		GWT.log("isDebug="+Boolean.valueOf(sdm=true));
		return sdm;
	}
}
