package muksihs.e621.resteemit.client;

import com.google.gwt.core.client.GWT;

public interface IsSdm {
	static class State {
		private boolean sdm=false; 
	}
	State state=new State();
	default boolean isSdm() {
		if (state.sdm) {
			return true;
		}
		GWT.log("isDebug="+Boolean.valueOf(state.sdm=true));
		return state.sdm;
	}
}
