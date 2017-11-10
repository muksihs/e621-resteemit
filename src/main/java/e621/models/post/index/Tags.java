
package e621.models.post.index;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "general",
    "artist",
    "copyright",
    "character",
    "species"
})
public class Tags {

    @JsonProperty("general")
    private List<String> general = new ArrayList<String>();
    @JsonProperty("artist")
    private List<String> artist = new ArrayList<String>();
    @JsonProperty("copyright")
    private List<String> copyright = new ArrayList<String>();
    @JsonProperty("character")
    private List<String> character = new ArrayList<String>();
    @JsonProperty("species")
    private List<String> species = new ArrayList<String>();

    @JsonProperty("general")
    public List<String> getGeneral() {
        return general;
    }

    @JsonProperty("general")
    public void setGeneral(List<String> general) {
        this.general = general;
    }

    @JsonProperty("artist")
    public List<String> getArtist() {
        return artist;
    }

    @JsonProperty("artist")
    public void setArtist(List<String> artist) {
        this.artist = artist;
    }

    @JsonProperty("copyright")
    public List<String> getCopyright() {
        return copyright;
    }

    @JsonProperty("copyright")
    public void setCopyright(List<String> copyright) {
        this.copyright = copyright;
    }

    @JsonProperty("character")
    public List<String> getCharacter() {
        return character;
    }

    @JsonProperty("character")
    public void setCharacter(List<String> character) {
        this.character = character;
    }

    @JsonProperty("species")
    public List<String> getSpecies() {
        return species;
    }

    @JsonProperty("species")
    public void setSpecies(List<String> species) {
        this.species = species;
    }

}