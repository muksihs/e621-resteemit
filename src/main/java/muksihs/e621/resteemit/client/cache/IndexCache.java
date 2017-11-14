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
	private static final String LIST_E621POST = "list-e621post-";
	//only deserialize the date property for the expires check loop (speed/work issue)
	protected interface ExpiresCodec extends JsonEncoderDecoder<CachedExpiration>{}
	protected final ExpiresCodec expiresCodec = GWT.create(ExpiresCodec.class);
	//full post stashing in html5 local storage
	protected interface Codec extends JsonEncoderDecoder<Cached>{}
	protected final Codec codec = GWT.create(Codec.class);
	private final String prefix;
	private StorageMap cache = new StorageMap(Storage.getLocalStorageIfSupported());
	public IndexCache(int cachedPageSize) {
		prefix=LIST_E621POST+cachedPageSize+":";
	}
	public void put(String key, List<E621Post> posts) {
		expiresCheck();
		Cached value = new Cached(posts);
		JSONValue encode = codec.encode(value);
		try {
			cache.put(prefix+key, encode.toString());
		} catch (Exception e) {
			clear();
			try {
				cache.put(prefix+key, encode.toString());
			} catch (Exception e1) {
				//panic clear the whole mess
			}
		}
	}
	public List<E621Post> get(String key) {
		expiresCheck();
		String json = cache.get(prefix+key);
		if (json==null) {
			return null;
		}
		try {
			long start=System.currentTimeMillis();
			List<E621Post> posts = codec.decode(json).getPosts();
			DomGlobal.console.log("get()-decode: "+(System.currentTimeMillis()-start));
			if (posts!=null && !posts.isEmpty()) {
				return posts;
			}
		} catch (Exception e) {
		}
		return null;
	}
	private void expiresCheck() {
		for (String key: cache.keySet()) {
			if (key.startsWith(LIST_E621POST)) {
				String json = cache.get(key);
				if (json==null) {
					continue;
				}
				CachedExpiration cached;
				try {
					cached = expiresCodec.decode(json);
				} catch (Exception e) {
					continue;
				}
				if (cached.isExpired()) {
					cache.remove(key);
				}
			}
		}
	}
	private void clear() {
		for (String key: cache.keySet()) {
			if (key.startsWith(LIST_E621POST)) {
				cache.remove(key);
			}
		}
	}
}
