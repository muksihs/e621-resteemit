package muksihs.e621.resteemit.shared;

public class PostPreview {
	private final long id;
	private final String sampleUrl;
	private final String fileUrl;
	private final long created;

	public long getId() {
		return id;
	}

	public String getSampleUrl() {
		return sampleUrl;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public long getCreated() {
		return created;
	}

	public PostPreview(long id, String sampleUrl, String fileUrl, long created) {
		this.id = id;
		this.sampleUrl = sampleUrl;
		this.fileUrl = fileUrl;
		this.created = created;
	}
}
