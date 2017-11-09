
package e621.models.post.index;

import java.util.ArrayList;

public class List {

    private long id;
    private Tags tags;
    private Object lockedTags;
    private String description;
    private CreatedAt createdAt;
    private long creatorId;
    private String author;
    private long change;
    private String source;
    private long score;
    private long favCount;
    private String md5;
    private long fileSize;
    private String fileUrl;
    private String fileExt;
    private String previewUrl;
    private long previewWidth;
    private long previewHeight;
    private String sampleUrl;
    private long sampleWidth;
    private long sampleHeight;
    private String rating;
    private String status;
    private long width;
    private long height;
    private boolean hasComments;
    private boolean hasNotes;
    private boolean hasChildren;
    private String children;
    private Object parentId;
    private java.util.List<String> artist = new ArrayList<String>();
    private java.util.List<String> sources = new ArrayList<String>();
    private String delreason;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Object getLockedTags() {
        return lockedTags;
    }

    public void setLockedTags(Object lockedTags) {
        this.lockedTags = lockedTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CreatedAt getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(CreatedAt createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getChange() {
        return change;
    }

    public void setChange(long change) {
        this.change = change;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public long getFavCount() {
        return favCount;
    }

    public void setFavCount(long favCount) {
        this.favCount = favCount;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public long getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(long previewWidth) {
        this.previewWidth = previewWidth;
    }

    public long getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(long previewHeight) {
        this.previewHeight = previewHeight;
    }

    public String getSampleUrl() {
        return sampleUrl;
    }

    public void setSampleUrl(String sampleUrl) {
        this.sampleUrl = sampleUrl;
    }

    public long getSampleWidth() {
        return sampleWidth;
    }

    public void setSampleWidth(long sampleWidth) {
        this.sampleWidth = sampleWidth;
    }

    public long getSampleHeight() {
        return sampleHeight;
    }

    public void setSampleHeight(long sampleHeight) {
        this.sampleHeight = sampleHeight;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public boolean isHasComments() {
        return hasComments;
    }

    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    public boolean isHasNotes() {
        return hasNotes;
    }

    public void setHasNotes(boolean hasNotes) {
        this.hasNotes = hasNotes;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }

    public Object getParentId() {
        return parentId;
    }

    public void setParentId(Object parentId) {
        this.parentId = parentId;
    }

    public java.util.List<String> getArtist() {
        return artist;
    }

    public void setArtist(java.util.List<String> artist) {
        this.artist = artist;
    }

    public java.util.List<String> getSources() {
        return sources;
    }

    public void setSources(java.util.List<String> sources) {
        this.sources = sources;
    }

    public String getDelreason() {
        return delreason;
    }

    public void setDelreason(String delreason) {
        this.delreason = delreason;
    }

}
