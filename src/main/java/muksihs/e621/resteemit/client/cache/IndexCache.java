package muksihs.e621.resteemit.client.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import blazing.chain.LZSEncoding;
import e621.models.post.index.E621Post;
import elemental2.dom.DomGlobal;

public class IndexCache {
	private static final String LIST_E621POST = "list-e621post-";

	// only deserialize the date property for the expires check loop
	protected interface ExpiresCodec extends JsonEncoderDecoder<CachedExpiration> {
	}

	protected final ExpiresCodec expiresCodec = GWT.create(ExpiresCodec.class);

	// full post list stashing in html5 local storage
	protected interface Codec extends JsonEncoderDecoder<Cached> {
	}

	protected final Codec codec = GWT.create(Codec.class);
	private final String prefix;
	private StorageMap cache = new StorageMap(Storage.getLocalStorageIfSupported());

	public IndexCache(int cachedPageSize) {
		prefix = LIST_E621POST + cachedPageSize + ":";
	}

	public void put(String key, List<E621Post> posts) {
		expiresCheck();
		Cached value = new Cached(posts);
		// cache older data longer than newer data...
		long pastDateSec = 0l;
		for (E621Post post : posts) {
			if (post.getCreatedAt() == null) {
				continue;
			}
			long createdSec = post.getCreatedAt().getS();
			pastDateSec = Long.max(createdSec, pastDateSec);
		}
		long dateDiffMs = System.currentTimeMillis() - pastDateSec * 1000l;
		if (dateDiffMs > 0l) {
			long expiresDateMs = System.currentTimeMillis() + dateDiffMs/4l;
			Date futureDate = new Date(expiresDateMs);
			// max cache time is restricted to 3 months
			int daysBetween = CalendarUtil.getDaysBetween(new Date(), futureDate);
			if (Math.abs(daysBetween) > 63) {
				CalendarUtil.addMonthsToDate(futureDate = new Date(), 1);
			}
			GWT.log("Cache put expire date: " + new java.sql.Date(futureDate.getTime()));
			value.setExpires(futureDate);
		}
		String jsonString = codec.encode(value).toString();
		String prefixedKey = prefix + key;
		try {
			jsonString = LZSEncoding.compressToBase64(jsonString);
			cache.put(prefixedKey, jsonString);
		} catch (Exception e) {
			DomGlobal.console.log(e.getMessage());
			GWT.log(e.getMessage(), e);
			clearOldest();
			try {
				cache.put(prefixedKey, jsonString);
			} catch (Exception e1) {
				DomGlobal.console.log(e1.getMessage());
				GWT.log(e1.getMessage(), e1);
				// panic clear the whole mess
				cache.clear();
				cache.put(prefixedKey, jsonString);
			}
		}
	}

	private void clearOldest() {
		DomGlobal.console.log("Force Early Expire (put exception)");
		class KeyDate {
			String key;
			Date expires;
		}
		List<KeyDate> forRemoval = new ArrayList<>();
		for (String prefixedKey : cache.keySet()) {
			String jsonString = cache.get(prefixedKey);
			if (jsonString == null) {
				cache.remove(prefixedKey);
				continue;
			}
			// remove legacy data
			if (jsonString.contains("\"expires\"")) {
				cache.remove(prefixedKey);
				continue;
			}
			try {
				jsonString = LZSEncoding.decompressFromBase64(jsonString);
			} catch (Exception e) {
				cache.remove(prefixedKey);
				continue;
			}
			if (!jsonString.contains("\"expires\"")) {
				continue;
			}
			CachedExpiration cached;
			try {
				cached = expiresCodec.decode(jsonString);
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				cache.remove(prefixedKey);
				continue;
			}
			KeyDate k = new KeyDate();
			k.expires = cached.getExpires();
			k.key = prefixedKey;
			if (k.expires == null) {
				cache.remove(prefixedKey);
				continue;
			}
			forRemoval.add(k);
		}
		try {
			Collections.sort(forRemoval, (a, b) -> a.expires.compareTo(b.expires));
			DomGlobal.console.log("Have "+cache.size()+" entries in the cache.");
			int size = forRemoval.size() / 4 + 1;
			DomGlobal.console.log("Force expiring "+size+" entries.");
			forRemoval.subList(0, size).forEach((k) -> cache.remove(k.key));
			DomGlobal.console.log("Have "+cache.size()+" remaining entries in the cache.");
		} catch (Exception e) {
			DomGlobal.console.log(e.getMessage());
			GWT.log(e.getMessage(),e);
		}
	}

	public List<E621Post> get(String key) {
		String prefixedKey = prefix + key;
		String jsonString = cache.get(prefixedKey);
		if (jsonString == null) {
			return null;
		}
		// remove legacy data
		if (jsonString.contains("\"expires\"")) {
			cache.remove(prefixedKey);
			return null;
		}

		try {
			jsonString = LZSEncoding.decompressFromBase64(jsonString);
			Cached decoded = codec.decode(jsonString);
			if (decoded.isExpired()) {
				cache.remove(prefixedKey);
				return null;
			}
			List<E621Post> posts = decoded.getPosts();
			if (posts != null && !posts.isEmpty()) {
				return posts;
			}
		} catch (Exception e) {
			cache.remove(key);
		}
		return null;
	}

	private void expiresCheck() {
		//only run expires check 1 in 10 times called to keep system load low
		if (new Random().nextInt(10)!=0) {
			return;
		}
		for (String prefixedKey : cache.keySet()) {
			String jsonString = cache.get(prefixedKey);
			if (jsonString == null) {
				cache.remove(prefixedKey);
				continue;
			}
			// remove legacy data
			if (jsonString.contains("\"expires\"")) {
				cache.remove(prefixedKey);
				continue;
			}

			try {
				jsonString = LZSEncoding.decompressFromBase64(jsonString);
			} catch (Exception e) {
				cache.remove(prefixedKey);
				continue;
			}

			if (!jsonString.contains("\"expires\"")) {
				continue;
			}
			CachedExpiration cached;
			try {
				cached = expiresCodec.decode(jsonString);
			} catch (Exception e) {
				cache.remove(prefixedKey);
				GWT.log(e.getMessage(), e);
				continue;
			}
			if (cached.isExpired()) {
				DomGlobal.console.log("Expired: ", prefixedKey);
				cache.remove(prefixedKey);
			}
		}

	}

	@SuppressWarnings("unused")
	private void clear() {
		DomGlobal.console.log("Index Cache Clear");
		for (String key : cache.keySet()) {
			if (key.startsWith(LIST_E621POST)) {
				cache.remove(key);
			}
		}
	}

	public String remove(String key) {
		String prefixedKey = prefix + key;
		return cache.remove(prefixedKey);
	}
}
