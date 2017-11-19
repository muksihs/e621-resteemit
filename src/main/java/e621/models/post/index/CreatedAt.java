
package e621.models.post.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
    "json_class",
    "s",
    "n"
})
public class CreatedAt {

    @JsonProperty("json_class")
    private String jsonClass;
    @JsonProperty("s")
    private Long s;
    @JsonProperty("n")
    private Long n;

    @JsonProperty("json_class")
    public String getJsonClass() {
        return jsonClass;
    }

    @JsonProperty("json_class")
    public void setJsonClass(String jsonClass) {
        this.jsonClass = jsonClass;
    }

    @JsonProperty("s")
    public Long getS() {
        return s;
    }

    @JsonProperty("s")
    public void setS(Long s) {
        this.s = s;
    }

    @JsonProperty("n")
    public Long getN() {
        return n;
    }

    @JsonProperty("n")
    public void setN(Long n) {
        this.n = n;
    }

}