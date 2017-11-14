package muksihs.e621.resteemit.client.cache;

import java.util.Date;
import java.util.List;

import e621.models.post.index.E621Post;

public class Cached implements HasExpiration {
	public Cached(List<E621Post> posts) {
		this(posts, new Date(System.currentTimeMillis() + 30l * 60l * 1000l));
	}

	public Cached() {
	}

	@Override
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setPosts(List<E621Post> posts) {
		this.list = posts;
	}

	public Cached(List<E621Post> posts, Date expires) {
		setPosts(posts);
		setExpires(expires);
	}

	private Date expires;
	private List<E621Post> list;

	@Override
	public Date getExpires() {
		return expires;
	}

	public List<E621Post> getPosts() {
		return list;
	}
}