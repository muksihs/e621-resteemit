
package steem.model.accountinfo;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true)
public abstract class Posting {

    @JsProperty
    public abstract int getWeightThreshold();

    @JsProperty
    public abstract void setWeightThreshold(int weightThreshold);

    @JsProperty
    public abstract String[][] getAccountAuths();

    @JsProperty
    public abstract void setAccountAuths(String[][] accountAuths);

    @JsProperty(name="key_auths")
    public abstract String[][] getKeyAuths();

    @JsProperty
    public abstract void setKeyAuths(String[][] keyAuths);

}
