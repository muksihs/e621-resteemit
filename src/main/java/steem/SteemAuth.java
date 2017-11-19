package steem;

import com.google.gwt.core.client.JavaScriptException;

import jsinterop.annotations.JsType;

@JsType(namespace = "steem", name = "auth", isNative=true)
public class SteemAuth {

	public static native boolean wifIsValid(String privateWif, String publicWif) throws JavaScriptException;
}
