
package e621.models.post.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "json_class",
    "s",
    "n"
})
public class CreatedAt {

    @JsonProperty("json_class")
    private String jsonClass;
    @JsonProperty("s")
    private long s;
    @JsonProperty("n")
    private long n;

    @JsonProperty("json_class")
    public String getJsonClass() {
        return jsonClass;
    }

    @JsonProperty("json_class")
    public void setJsonClass(String jsonClass) {
        this.jsonClass = jsonClass;
    }

    @JsonProperty("s")
    public long getS() {
        return s;
    }

    @JsonProperty("s")
    public void setS(long s) {
        this.s = s;
    }

    @JsonProperty("n")
    public long getN() {
        return n;
    }

    @JsonProperty("n")
    public void setN(long n) {
        this.n = n;
    }

}