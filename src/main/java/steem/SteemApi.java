package steem;

import jsinterop.annotations.JsType;
import steem.model.accountinfo.AccountInfo;

@JsType(namespace = "steem", name = "api", isNative=true)
public class SteemApi {
	public static native void getTrendingTags(String afterTag, int limit,
			SteemCallbackArray<TrendingTagsResult> callback);
	
	public static native void getAccounts(String[] username, SteemCallbackArray<AccountInfo> callback);
}