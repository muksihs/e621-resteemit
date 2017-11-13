package muksihs.e621.resteemit.client.cache;

import java.util.List;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

import e621.models.post.index.E621Post;
import elemental2.dom.DomGlobal;

public class IndexCache {
	protected interface Codec extends JsonEncoderDecoder<Cached>{}
	protected final Codec codec = GWT.create(Codec.class);
	private static final String prefix="list-e621post:";
	private StorageMap cache = new StorageMap(Storage.getLocalStorageIfSupported());
	public void put(String key, List<E621Post> posts) {
		expiresCheck();
		Cached value = new Cached(posts);
		JSONValue encode = codec.encode(value);
		cache.put(prefix+key, encode.toString());
	}
	public List<E621Post> get(String key) {
		expiresCheck();
		String json = cache.get(prefix+key);
		if (json==null) {
			return null;
		}
		try {
			List<E621Post> posts = codec.decode(json).getPosts();
			if (posts!=null && !posts.isEmpty()) {
				DomGlobal.console.log("Cache hit: "+key);
				return posts;
			}
		} catch (Exception e) {
		}
		DomGlobal.console.log("Cache miss: "+key);
		return null;
	}
	private void expiresCheck() {
		for (String key: cache.keySet()) {
			if (key.startsWith(prefix)) {
				String json = cache.get(prefix+key);
				if (json==null) {
					continue;
				}
				Cached cached;
				try {
					cached = codec.decode(json);
				} catch (Exception e) {
					continue;
				}
				if (cached.isExpired()) {
					cache.remove(key);
				}
			}
		}
	}
}
