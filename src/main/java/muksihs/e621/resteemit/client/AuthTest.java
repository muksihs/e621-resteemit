package muksihs.e621.resteemit.client;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import steem.SteemApi;
import steem.SteemAuth;
import steem.model.accountinfo.AccountInfo;
import steem.model.accountinfo.Posting;

public class AuthTest implements ScheduledCommand {
	
	private void checkWif(Map<String, String> error, AccountInfo[] result) {
		if (result==null) {
			GWT.log("NULL result!");
			GWT.log(String.valueOf(error));
			return;
		}
		if (result.length==0) {
			GWT.log("Username not found!");
			GWT.log(String.valueOf(error));
			return;
		}
		AccountInfo accountInfo = result[0];
		if (accountInfo==null) {
			GWT.log("accountInfo==NULL!");
			GWT.log(String.valueOf(error));
			return;
		}
		GWT.log("Valid username: "+accountInfo.getName());
		Posting posting = accountInfo.getPosting();
		if (posting==null) {
			GWT.log("postingKeys==NULL!");
			GWT.log(String.valueOf(error));
			return;
		}
		GWT.log("keyAuths: "+String.valueOf(posting.getKeyAuths()));
		String[][] keyAuths = posting.getKeyAuths();
		if (keyAuths==null || keyAuths.length==0) {
			GWT.log("keyAuths==NULL or isEmpty!");
			GWT.log(String.valueOf(error));
			return;
		}
		String[] keylist = keyAuths[0];
		if (keylist==null || keylist.length==0) {
			GWT.log("keylist==NULL or isEmpty!");
			GWT.log(String.valueOf(error));
			return;
		}
		String publicWif = keylist[0];
		try {
//			GWT.log("Is valid private posting key:"+SteemAuth.wifIsValid(postingKey, publicWif));
		} catch (JavaScriptException e) {
			GWT.log(e.getMessage(),e);
		}
	}
	
	@Override
	public void execute() {
		GWT.log("Executing: "+this.getClass().getName());
//		SteemApi.getAccounts(new String[] {username}, this::checkWif);
	}
	
}
