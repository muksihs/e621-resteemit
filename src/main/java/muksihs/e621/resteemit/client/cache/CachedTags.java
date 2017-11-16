package muksihs.e621.resteemit.client.cache;

import java.util.Date;
import java.util.List;

import e621.models.tag.index.Tag;


public class CachedTags implements HasExpiration {
	public CachedTags(List<Tag> tags) {
		this(tags, new Date(System.currentTimeMillis() + 4l * 60l * 60l * 1000l));
	}

	public CachedTags() {
	}

	@Override
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setTags(List<Tag> tags) {
		this.list = tags;
	}

	public CachedTags(List<Tag> tags, Date expires) {
		setTags(tags);
		setExpires(expires);
	}

	private Date expires;
	private List<Tag> list;

	@Override
	public Date getExpires() {
		return expires;
	}

	public List<Tag> getTags() {
		return list;
	}
}