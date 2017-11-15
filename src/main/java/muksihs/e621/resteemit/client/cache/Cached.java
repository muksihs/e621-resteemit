package muksihs.e621.resteemit.client.cache;

import java.util.Date;

import e621.models.post.index.E621PostList;

public class Cached implements HasExpiration {
	public Cached(E621PostList posts) {
		this(posts, new Date(System.currentTimeMillis() + 30l * 60l * 1000l));
	}

	public Cached() {
	}

	@Override
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setPosts(E621PostList posts) {
		this.list = posts;
	}

	public Cached(E621PostList posts, Date expires) {
		setPosts(posts);
		setExpires(expires);
	}

	private Date expires;
	private E621PostList list;

	@Override
	public Date getExpires() {
		return expires;
	}

	public E621PostList getPosts() {
		return list;
	}
}