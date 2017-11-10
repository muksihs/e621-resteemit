package e621.models.post.index;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "tags",
    "locked_tags",
    "description",
    "created_at",
    "creator_id",
    "author",
    "change",
    "source",
    "score",
    "fav_count",
    "md5",
    "file_size",
    "file_url",
    "file_ext",
    "preview_url",
    "preview_width",
    "preview_height",
    "sample_url",
    "sample_width",
    "sample_height",
    "rating",
    "status",
    "width",
    "height",
    "has_comments",
    "has_notes",
    "has_children",
    "children",
    "parent_id",
    "artist",
    "sources"
})
public class E621Post {

    @JsonProperty("id")
    private long id;
    @JsonProperty("tags")
    private Tags tags;
    @JsonProperty("locked_tags")
    private Object lockedTags;
    @JsonProperty("description")
    private String description;
    @JsonProperty("created_at")
    private CreatedAt createdAt;
    @JsonProperty("creator_id")
    private long creatorId;
    @JsonProperty("author")
    private String author;
    @JsonProperty("change")
    private long change;
    @JsonProperty("source")
    private String source;
    @JsonProperty("score")
    private long score;
    @JsonProperty("fav_count")
    private long favCount;
    @JsonProperty("md5")
    private String md5;
    @JsonProperty("file_size")
    private long fileSize;
    @JsonProperty("file_url")
    private String fileUrl;
    @JsonProperty("file_ext")
    private String fileExt;
    @JsonProperty("preview_url")
    private String previewUrl;
    @JsonProperty("preview_width")
    private long previewWidth;
    @JsonProperty("preview_height")
    private long previewHeight;
    @JsonProperty("sample_url")
    private String sampleUrl;
    @JsonProperty("sample_width")
    private long sampleWidth;
    @JsonProperty("sample_height")
    private long sampleHeight;
    @JsonProperty("rating")
    private String rating;
    @JsonProperty("status")
    private String status;
    @JsonProperty("width")
    private long width;
    @JsonProperty("height")
    private long height;
    @JsonProperty("has_comments")
    private boolean hasComments;
    @JsonProperty("has_notes")
    private boolean hasNotes;
    @JsonProperty("has_children")
    private boolean hasChildren;
    @JsonProperty("children")
    private String children;
    @JsonProperty("parent_id")
    private Object parentId;
    @JsonProperty("artist")
    private List<String> artist = new ArrayList<String>();
    @JsonProperty("sources")
    private List<String> sources = new ArrayList<String>();

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
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
    public Object getLockedTags() {
        return lockedTags;
    }

    @JsonProperty("locked_tags")
    public void setLockedTags(Object lockedTags) {
        this.lockedTags = lockedTags;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("created_at")
    public CreatedAt getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(CreatedAt createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("creator_id")
    public long getCreatorId() {
        return creatorId;
    }

    @JsonProperty("creator_id")
    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @JsonProperty("change")
    public long getChange() {
        return change;
    }

    @JsonProperty("change")
    public void setChange(long change) {
        this.change = change;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("score")
    public long getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(long score) {
        this.score = score;
    }

    @JsonProperty("fav_count")
    public long getFavCount() {
        return favCount;
    }

    @JsonProperty("fav_count")
    public void setFavCount(long favCount) {
        this.favCount = favCount;
    }

    @JsonProperty("md5")
    public String getMd5() {
        return md5;
    }

    @JsonProperty("md5")
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @JsonProperty("file_size")
    public long getFileSize() {
        return fileSize;
    }

    @JsonProperty("file_size")
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @JsonProperty("file_url")
    public String getFileUrl() {
        return fileUrl;
    }

    @JsonProperty("file_url")
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @JsonProperty("file_ext")
    public String getFileExt() {
        return fileExt;
    }

    @JsonProperty("file_ext")
    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    @JsonProperty("preview_url")
    public String getPreviewUrl() {
        return previewUrl;
    }

    @JsonProperty("preview_url")
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    @JsonProperty("preview_width")
    public long getPreviewWidth() {
        return previewWidth;
    }

    @JsonProperty("preview_width")
    public void setPreviewWidth(long previewWidth) {
        this.previewWidth = previewWidth;
    }

    @JsonProperty("preview_height")
    public long getPreviewHeight() {
        return previewHeight;
    }

    @JsonProperty("preview_height")
    public void setPreviewHeight(long previewHeight) {
        this.previewHeight = previewHeight;
    }

    @JsonProperty("sample_url")
    public String getSampleUrl() {
        return sampleUrl;
    }

    @JsonProperty("sample_url")
    public void setSampleUrl(String sampleUrl) {
        this.sampleUrl = sampleUrl;
    }

    @JsonProperty("sample_width")
    public long getSampleWidth() {
        return sampleWidth;
    }

    @JsonProperty("sample_width")
    public void setSampleWidth(long sampleWidth) {
        this.sampleWidth = sampleWidth;
    }

    @JsonProperty("sample_height")
    public long getSampleHeight() {
        return sampleHeight;
    }

    @JsonProperty("sample_height")
    public void setSampleHeight(long sampleHeight) {
        this.sampleHeight = sampleHeight;
    }

    @JsonProperty("rating")
    public String getRating() {
        return rating;
    }

    @JsonProperty("rating")
    public void setRating(String rating) {
        this.rating = rating;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("width")
    public long getWidth() {
        return width;
    }

    @JsonProperty("width")
    public void setWidth(long width) {
        this.width = width;
    }

    @JsonProperty("height")
    public long getHeight() {
        return height;
    }

    @JsonProperty("height")
    public void setHeight(long height) {
        this.height = height;
    }

    @JsonProperty("has_comments")
    public boolean isHasComments() {
        return hasComments;
    }

    @JsonProperty("has_comments")
    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    @JsonProperty("has_notes")
    public boolean isHasNotes() {
        return hasNotes;
    }

    @JsonProperty("has_notes")
    public void setHasNotes(boolean hasNotes) {
        this.hasNotes = hasNotes;
    }

    @JsonProperty("has_children")
    public boolean isHasChildren() {
        return hasChildren;
    }

    @JsonProperty("has_children")
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @JsonProperty("children")
    public String getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(String children) {
        this.children = children;
    }

    @JsonProperty("parent_id")
    public Object getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(Object parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("artist")
    public List<String> getArtist() {
        return artist;
    }

    @JsonProperty("artist")
    public void setArtist(List<String> artist) {
        this.artist = artist;
    }

    @JsonProperty("sources")
    public List<String> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

}