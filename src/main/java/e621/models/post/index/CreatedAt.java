
package e621.models.post.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class CreatedAt {

    private String jsonClass;
    private long s;
    private long n;

    public String getJsonClass() {
        return jsonClass;
    }

    public void setJsonClass(String jsonClass) {
        this.jsonClass = jsonClass;
    }

    public long getS() {
        return s;
    }

    public void setS(long s) {
        this.s = s;
    }

    public long getN() {
        return n;
    }

    public void setN(long n) {
        this.n = n;
    }

}
