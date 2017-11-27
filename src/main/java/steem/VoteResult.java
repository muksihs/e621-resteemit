package steem;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface VoteResult {
	public String getId();

	public Long getBlock_num();

	public Integer getTrx_num();

	public Boolean isExpired();
}
