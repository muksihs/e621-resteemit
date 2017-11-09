
package e621.models.post.index;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tags {

    private List<String> general = new ArrayList<String>();
    private List<String> artist = new ArrayList<String>();
    private List<String> copyright = new ArrayList<String>();
    private List<String> character = new ArrayList<String>();
    private List<String> species = new ArrayList<String>();

    public List<String> getGeneral() {
        return general;
    }

    public void setGeneral(List<String> general) {
        this.general = general;
    }

    public List<String> getArtist() {
        return artist;
    }

    public void setArtist(List<String> artist) {
        this.artist = artist;
    }

    public List<String> getCopyright() {
        return copyright;
    }

    public void setCopyright(List<String> copyright) {
        this.copyright = copyright;
    }

    public List<String> getCharacter() {
        return character;
    }

    public void setCharacter(List<String> character) {
        this.character = character;
    }

    public List<String> getSpecies() {
        return species;
    }

    public void setSpecies(List<String> species) {
        this.species = species;
    }

}
