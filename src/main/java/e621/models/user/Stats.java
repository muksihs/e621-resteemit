
package e621.models.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(value=Include.ALWAYS)
@JsonPropertyOrder({
    "post_count",
    "del_post_count",
    "edit_count",
    "favorite_count",
    "wiki_count",
    "forum_post_count",
    "note_count",
    "comment_count",
    "blip_count",
    "set_count",
    "pool_update_count",
    "pos_user_records",
    "neutral_user_records",
    "neg_user_records"
})
public class Stats {

    @JsonProperty("post_count")
    private int postCount;
    @JsonProperty("del_post_count")
    private int delPostCount;
    @JsonProperty("edit_count")
    private int editCount;
    @JsonProperty("favorite_count")
    private int favoriteCount;
    @JsonProperty("wiki_count")
    private int wikiCount;
    @JsonProperty("forum_post_count")
    private int forumPostCount;
    @JsonProperty("note_count")
    private int noteCount;
    @JsonProperty("comment_count")
    private int commentCount;
    @JsonProperty("blip_count")
    private int blipCount;
    @JsonProperty("set_count")
    private int setCount;
    @JsonProperty("pool_update_count")
    private int poolUpdateCount;
    @JsonProperty("pos_user_records")
    private int posUserRecords;
    @JsonProperty("neutral_user_records")
    private int neutralUserRecords;
    @JsonProperty("neg_user_records")
    private int negUserRecords;

    @JsonProperty("post_count")
    public int getPostCount() {
        return postCount;
    }

    @JsonProperty("post_count")
    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    @JsonProperty("del_post_count")
    public int getDelPostCount() {
        return delPostCount;
    }

    @JsonProperty("del_post_count")
    public void setDelPostCount(int delPostCount) {
        this.delPostCount = delPostCount;
    }

    @JsonProperty("edit_count")
    public int getEditCount() {
        return editCount;
    }

    @JsonProperty("edit_count")
    public void setEditCount(int editCount) {
        this.editCount = editCount;
    }

    @JsonProperty("favorite_count")
    public int getFavoriteCount() {
        return favoriteCount;
    }

    @JsonProperty("favorite_count")
    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    @JsonProperty("wiki_count")
    public int getWikiCount() {
        return wikiCount;
    }

    @JsonProperty("wiki_count")
    public void setWikiCount(int wikiCount) {
        this.wikiCount = wikiCount;
    }

    @JsonProperty("forum_post_count")
    public int getForumPostCount() {
        return forumPostCount;
    }

    @JsonProperty("forum_post_count")
    public void setForumPostCount(int forumPostCount) {
        this.forumPostCount = forumPostCount;
    }

    @JsonProperty("note_count")
    public int getNoteCount() {
        return noteCount;
    }

    @JsonProperty("note_count")
    public void setNoteCount(int noteCount) {
        this.noteCount = noteCount;
    }

    @JsonProperty("comment_count")
    public int getCommentCount() {
        return commentCount;
    }

    @JsonProperty("comment_count")
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @JsonProperty("blip_count")
    public int getBlipCount() {
        return blipCount;
    }

    @JsonProperty("blip_count")
    public void setBlipCount(int blipCount) {
        this.blipCount = blipCount;
    }

    @JsonProperty("set_count")
    public int getSetCount() {
        return setCount;
    }

    @JsonProperty("set_count")
    public void setSetCount(int setCount) {
        this.setCount = setCount;
    }

    @JsonProperty("pool_update_count")
    public int getPoolUpdateCount() {
        return poolUpdateCount;
    }

    @JsonProperty("pool_update_count")
    public void setPoolUpdateCount(int poolUpdateCount) {
        this.poolUpdateCount = poolUpdateCount;
    }

    @JsonProperty("pos_user_records")
    public int getPosUserRecords() {
        return posUserRecords;
    }

    @JsonProperty("pos_user_records")
    public void setPosUserRecords(int posUserRecords) {
        this.posUserRecords = posUserRecords;
    }

    @JsonProperty("neutral_user_records")
    public int getNeutralUserRecords() {
        return neutralUserRecords;
    }

    @JsonProperty("neutral_user_records")
    public void setNeutralUserRecords(int neutralUserRecords) {
        this.neutralUserRecords = neutralUserRecords;
    }

    @JsonProperty("neg_user_records")
    public int getNegUserRecords() {
        return negUserRecords;
    }

    @JsonProperty("neg_user_records")
    public void setNegUserRecords(int negUserRecords) {
        this.negUserRecords = negUserRecords;
    }

}
