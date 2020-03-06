
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
    "general",
    "species",
    "character",
    "copyright",
    "artist",
    "invalid",
    "lore",
    "meta"
})
public class Tags {

    @JsonProperty("general")
    private List<String> general = null;
    @JsonProperty("species")
    private List<String> species = null;
    @JsonProperty("character")
    private List<String> character = null;
    @JsonProperty("copyright")
    private List<String> copyright = null;
    @JsonProperty("artist")
    private List<String> artist = null;
    @JsonProperty("invalid")
    private List<Object> invalid = null;
    @JsonProperty("lore")
    private List<Object> lore = null;
    @JsonProperty("meta")
    private List<Object> meta = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("general")
    public List<String> getGeneral() {
        return general;
    }

    @JsonProperty("general")
    public void setGeneral(List<String> general) {
        this.general = general;
    }

    @JsonProperty("species")
    public List<String> getSpecies() {
        return species;
    }

    @JsonProperty("species")
    public void setSpecies(List<String> species) {
        this.species = species;
    }

    @JsonProperty("character")
    public List<String> getCharacter() {
        return character;
    }

    @JsonProperty("character")
    public void setCharacter(List<String> character) {
        this.character = character;
    }

    @JsonProperty("copyright")
    public List<String> getCopyright() {
        return copyright;
    }

    @JsonProperty("copyright")
    public void setCopyright(List<String> copyright) {
        this.copyright = copyright;
    }

    @JsonProperty("artist")
    public List<String> getArtist() {
        return artist;
    }

    @JsonProperty("artist")
    public void setArtist(List<String> artist) {
        this.artist = artist;
    }

    @JsonProperty("invalid")
    public List<Object> getInvalid() {
        return invalid;
    }

    @JsonProperty("invalid")
    public void setInvalid(List<Object> invalid) {
        this.invalid = invalid;
    }

    @JsonProperty("lore")
    public List<Object> getLore() {
        return lore;
    }

    @JsonProperty("lore")
    public void setLore(List<Object> lore) {
        this.lore = lore;
    }

    @JsonProperty("meta")
    public List<Object> getMeta() {
        return meta;
    }

    @JsonProperty("meta")
    public void setMeta(List<Object> meta) {
        this.meta = meta;
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
