
package e621.models.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(value=Include.ALWAYS)
@JsonPropertyOrder({
    "name",
    "id",
    "level",
    "created_at",
    "avatar_id",
    "stats",
    "artist_tags"
})
public class E621UserInfo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private int id;
    @JsonProperty("level")
    private int level;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("avatar_id")
    private int avatarId;
    @JsonProperty("stats")
    private Stats stats;
    @JsonProperty("artist_tags")
    private List<Object> artistTags = null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("level")
    public int getLevel() {
        return level;
    }

    @JsonProperty("level")
    public void setLevel(int level) {
        this.level = level;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("avatar_id")
    public int getAvatarId() {
        return avatarId;
    }

    @JsonProperty("avatar_id")
    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    @JsonProperty("stats")
    public Stats getStats() {
        return stats;
    }

    @JsonProperty("stats")
    public void setStats(Stats stats) {
        this.stats = stats;
    }

    @JsonProperty("artist_tags")
    public List<Object> getArtistTags() {
        return artistTags;
    }

    @JsonProperty("artist_tags")
    public void setArtistTags(List<Object> artistTags) {
        this.artistTags = artistTags;
    }

}
