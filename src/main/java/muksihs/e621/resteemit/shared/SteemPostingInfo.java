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
	private Set<E621Filter> savedFilters;
	
	public SteemPostingInfo() {
		savedFilters = new TreeSet<>();
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
}
