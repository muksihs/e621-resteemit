package muksihs.e621.resteemit.shared;

public enum E621Rating {
	SAFE("s"), QUESTIONABLE("q"), EXPLICIT("e");
	private final String tag;

	private E621Rating(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}
}