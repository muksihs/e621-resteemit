package muksihs.e621.resteemit.shared;

import java.util.Set;
import java.util.TreeSet;

public class SteemPostingInfo {
	public SteemPostingInfo(String username, String wif) {
		super();
		this.username = username;
		this.wif = wif;
	}

	private String username;
	private String wif;
	private boolean upvote;
	private final Set<String> savedFilters = new TreeSet<>();

	public SteemPostingInfo() {
		setSavedFilters(new TreeSet<>());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getWif() {
		return wif;
	}

	public void setWif(String wif) {
		this.wif = wif;
	}

	public boolean isUpvote() {
		return upvote;
	}

	public void setUpvote(boolean upvote) {
		this.upvote = upvote;
	}

	public Set<String> getSavedFilters() {
		return savedFilters;
	}

	public void setSavedFilters(Set<String> savedFilters) {
		this.savedFilters.clear();
		this.savedFilters.addAll(savedFilters);
	}
}
