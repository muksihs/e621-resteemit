package muksihs.e621.resteemit.client.cache;

import java.util.Date;

public class CachedExpiration implements HasExpiration {
	private Date expires;
	@Override
	public void setExpires(Date expires) {
		this.expires=expires;
	}

	@Override
	public Date getExpires() {
		return expires;
	}

}
