
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
    "id",
    "created_at",
    "updated_at",
    "file",
    "preview",
    "sample",
    "score",
    "tags",
    "locked_tags",
    "change_seq",
    "flags",
    "rating",
    "fav_count",
    "sources",
    "pools",
    "relationships",
    "approver_id",
    "uploader_id",
    "description",
    "comment_count",
    "is_favorited"
})
public class Post {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("file")
    private File file;
    @JsonProperty("preview")
    private Preview preview;
    @JsonProperty("sample")
    private Sample sample;
    @JsonProperty("score")
    private Score score;
    @JsonProperty("tags")
    private Tags tags;
    @JsonProperty("locked_tags")
    private List<Object> lockedTags = null;
    @JsonProperty("change_seq")
    private Integer changeSeq;
    @JsonProperty("flags")
    private Flags flags;
    @JsonProperty("rating")
    private String rating;
    @JsonProperty("fav_count")
    private Integer favCount;
    @JsonProperty("sources")
    private List<String> sources = null;
    @JsonProperty("pools")
    private List<Object> pools = null;
    @JsonProperty("relationships")
    private Relationships relationships;
    @JsonProperty("approver_id")
    private Object approverId;
    @JsonProperty("uploader_id")
    private Integer uploaderId;
    @JsonProperty("description")
    private String description;
    @JsonProperty("comment_count")
    private Integer commentCount;
    @JsonProperty("is_favorited")
    private Boolean isFavorited;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("file")
    public File getFile() {
        return file;
    }

    @JsonProperty("file")
    public void setFile(File file) {
        this.file = file;
    }

    @JsonProperty("preview")
    public Preview getPreview() {
        return preview;
    }

    @JsonProperty("preview")
    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    @JsonProperty("sample")
    public Sample getSample() {
        return sample;
    }

    @JsonProperty("sample")
    public void setSample(Sample sample) {
        this.sample = sample;
    }

    @JsonProperty("score")
    public Score getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(Score score) {
        this.score = score;
    }

    @JsonProperty("tags")
    public Tags getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Tags tags) {
        this.tags = tags;
    }

    @JsonProperty("locked_tags")
    public List<Object> getLockedTags() {
        return lockedTags;
    }

    @JsonProperty("locked_tags")
    public void setLockedTags(List<Object> lockedTags) {
        this.lockedTags = lockedTags;
    }

    @JsonProperty("change_seq")
    public Integer getChangeSeq() {
        return changeSeq;
    }

    @JsonProperty("change_seq")
    public void setChangeSeq(Integer changeSeq) {
        this.changeSeq = changeSeq;
    }

    @JsonProperty("flags")
    public Flags getFlags() {
        return flags;
    }

    @JsonProperty("flags")
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    @JsonProperty("rating")
    public String getRating() {
        return rating;
    }

    @JsonProperty("rating")
    public void setRating(String rating) {
        this.rating = rating;
    }

    @JsonProperty("fav_count")
    public Integer getFavCount() {
        return favCount;
    }

    @JsonProperty("fav_count")
    public void setFavCount(Integer favCount) {
        this.favCount = favCount;
    }

    @JsonProperty("sources")
    public List<String> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    @JsonProperty("pools")
    public List<Object> getPools() {
        return pools;
    }

    @JsonProperty("pools")
    public void setPools(List<Object> pools) {
        this.pools = pools;
    }

    @JsonProperty("relationships")
    public Relationships getRelationships() {
        return relationships;
    }

    @JsonProperty("relationships")
    public void setRelationships(Relationships relationships) {
        this.relationships = relationships;
    }

    @JsonProperty("approver_id")
    public Object getApproverId() {
        return approverId;
    }

    @JsonProperty("approver_id")
    public void setApproverId(Object approverId) {
        this.approverId = approverId;
    }

    @JsonProperty("uploader_id")
    public Integer getUploaderId() {
        return uploaderId;
    }

    @JsonProperty("uploader_id")
    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("comment_count")
    public Integer getCommentCount() {
        return commentCount;
    }

    @JsonProperty("comment_count")
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    @JsonProperty("is_favorited")
    public Boolean getIsFavorited() {
        return isFavorited;
    }

    @JsonProperty("is_favorited")
    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
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
