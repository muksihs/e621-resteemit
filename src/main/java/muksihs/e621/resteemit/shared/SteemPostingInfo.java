package muksihs.e621.resteemit.shared;

public class SteemPostingInfo {
	public SteemPostingInfo(String username, String wif) {
		super();
		this.username = username;
		this.wif = wif;
	}
	private String username;
	private String wif;
	public SteemPostingInfo() {
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
}
