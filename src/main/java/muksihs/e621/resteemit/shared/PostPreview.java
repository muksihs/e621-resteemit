package muksihs.e621.resteemit.shared;

public class PostPreview {
	private final long id;
	private final String sampleUrl;
	private final String fileUrl;
	private final long created;
	private String tags;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/**
	 * Two "previews" are considered equal if they reference the same post number!
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PostPreview)) {
			return false;
		}
		PostPreview other = (PostPreview) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

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

	public PostPreview(long id, String sampleUrl, String fileUrl, long created, String tags) {
		this.id = id;
		this.sampleUrl = sampleUrl;
		this.fileUrl = fileUrl;
		this.created = created;
		this.tags = tags;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
}
