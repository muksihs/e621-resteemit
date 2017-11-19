
package steem.model.accountinfo;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true)
public abstract class Active {

    @JsProperty
    public abstract int getWeightThreshold();

    @JsProperty
    public abstract void setWeightThreshold(int weightThreshold);

    @JsProperty
    public abstract Object[] getAccountAuths();

    @JsProperty
    public abstract void setAccountAuths(Object[] accountAuths);

    @JsProperty
    public abstract String[][] getKeyAuths();

    @JsProperty
    public abstract void setKeyAuths(String[][] keyAuths);

}
