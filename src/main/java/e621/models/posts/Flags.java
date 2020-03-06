
package e621.models.posts;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pending",
    "flagged",
    "note_locked",
    "status_locked",
    "rating_locked",
    "deleted"
})
public class Flags {

    @JsonProperty("pending")
    private Boolean pending;
    @JsonProperty("flagged")
    private Boolean flagged;
    @JsonProperty("note_locked")
    private Boolean noteLocked;
    @JsonProperty("status_locked")
    private Boolean statusLocked;
    @JsonProperty("rating_locked")
    private Boolean ratingLocked;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("pending")
    public Boolean getPending() {
        return pending;
    }

    @JsonProperty("pending")
    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    @JsonProperty("flagged")
    public Boolean getFlagged() {
        return flagged;
    }

    @JsonProperty("flagged")
    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    @JsonProperty("note_locked")
    public Boolean getNoteLocked() {
        return noteLocked;
    }

    @JsonProperty("note_locked")
    public void setNoteLocked(Boolean noteLocked) {
        this.noteLocked = noteLocked;
    }

    @JsonProperty("status_locked")
    public Boolean getStatusLocked() {
        return statusLocked;
    }

    @JsonProperty("status_locked")
    public void setStatusLocked(Boolean statusLocked) {
        this.statusLocked = statusLocked;
    }

    @JsonProperty("rating_locked")
    public Boolean getRatingLocked() {
        return ratingLocked;
    }

    @JsonProperty("rating_locked")
    public void setRatingLocked(Boolean ratingLocked) {
        this.ratingLocked = ratingLocked;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
