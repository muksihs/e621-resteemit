
package e621.models.posts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "parent_id",
    "has_children",
    "has_active_children",
    "children"
})
public class Relationships {

    @JsonProperty("parent_id")
    private Object parentId;
    @JsonProperty("has_children")
    private Boolean hasChildren;
    @JsonProperty("has_active_children")
    private Boolean hasActiveChildren;
    @JsonProperty("children")
    private List<Integer> children = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("parent_id")
    public Object getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(Object parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("has_children")
    public Boolean getHasChildren() {
        return hasChildren;
    }

    @JsonProperty("has_children")
    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @JsonProperty("has_active_children")
    public Boolean getHasActiveChildren() {
        return hasActiveChildren;
    }

    @JsonProperty("has_active_children")
    public void setHasActiveChildren(Boolean hasActiveChildren) {
        this.hasActiveChildren = hasActiveChildren;
    }

    @JsonProperty("children")
    public List<Integer> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<Integer> children) {
        this.children = children;
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
