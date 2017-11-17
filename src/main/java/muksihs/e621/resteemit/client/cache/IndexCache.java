package muksihs.e621.resteemit.client.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.JsonEncoderDecoder.DecodingException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

import e621.models.post.index.E621Post;
import elemental2.dom.DomGlobal;


public class IndexCache {
	private static final String LIST_E621POST = "list-e621post-";
	//only deserialize the date property for the expires check loop
	protected interface ExpiresCodec extends JsonEncoderDecoder<CachedExpiration>{}
	protected final ExpiresCodec expiresCodec = GWT.create(ExpiresCodec.class);
	//full post list stashing in html5 local storage
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
		String jsonString = codec.encode(value).toString();
		try {
			cache.put(prefix+key, jsonString);
		} catch (JavaScriptException e) {
			GWT.log("=== Javascript Exception");
			GWT.log(e.getMessage(), e);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			clearOldest();
			try {
				cache.put(prefix+key, jsonString);
			} catch (Exception e1) {
				GWT.log(e1.getMessage(), e1);
				//panic clear the whole mess
				cache.clear();
				cache.put(prefix+key, jsonString);
			}
		}
	}
	private void clearOldest() {
		class KeyDate {
			String key;
			Date expires;
		}
		List<KeyDate> forRemoval = new ArrayList<>();
		for (String prefixedKey: cache.keySet()) {
			String json = cache.get(prefixedKey);
			if (json==null) {
				cache.remove(prefixedKey);
				continue;
			}
			if (!json.contains("\"expires\"")){
				continue;
			}
			CachedExpiration cached;
			try {
				cached = expiresCodec.decode(json);
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				cache.remove(prefixedKey);
				continue;
			}
			KeyDate k = new KeyDate();
			k.expires=cached.getExpires();
			k.key=prefixedKey;
			if (k.expires==null) {
				cache.remove(prefixedKey);
				continue;
			}
			forRemoval.add(k);
		}
		Collections.sort(forRemoval, (a,b)->a.expires.compareTo(b.expires));
		int size=forRemoval.size();
		
	}
	public List<E621Post> get(String key) {
		expiresCheck();
		String jsonString = cache.get(prefix+key);
		if (jsonString==null) {
			return null;
		}
		try {
			List<E621Post> posts = codec.decode(jsonString).getPosts();
			if (posts!=null && !posts.isEmpty()) {
				return posts;
			}
		} catch (Exception e) {
		}
		return null;
	}
	private void expiresCheck() {
		for (String prefixedKey: cache.keySet()) {
			String json = cache.get(prefixedKey);
			if (json==null) {
				cache.remove(prefixedKey);
				continue;
			}
			if (!json.contains("\"expires\"")){
				continue;
			}
			CachedExpiration cached;
			try {
				cached = expiresCodec.decode(json);
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				continue;
			}
			if (cached.isExpired()) {
				DomGlobal.console.log("Expired: ", prefixedKey);
				cache.remove(prefixedKey);
			}
		}
	}
	private void clear() {
		DomGlobal.console.log("Index Cache Clear");
		for (String key: cache.keySet()) {
			if (key.startsWith(LIST_E621POST)) {
				cache.remove(key);
			}
		}
	}
}
