package muksihs.e621.resteemit.client.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import blazing.chain.LZSEncoding;
import e621.models.post.index.E621Post;
import elemental2.dom.DomGlobal;

public class IndexCache {
	private static final int MAX_CACHE_AGE = 31;
	private static final long DAY_ms = 1000l * 60l * 60l * 24l;
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
	private Map<String, String> storage = new StorageMap(Storage.getLocalStorageIfSupported());
	private Map<String, String> memCache = new HashMap<>();

	public IndexCache(int cachedPageSize) {
		if (storage == null) {
			storage = memCache;
		}
		prefix = LIST_E621POST + cachedPageSize + ":";
	}

	public String put(String key, List<E621Post> posts) {
		return _put(prefix + key, posts);
	}

	private String _put(String prefixedKey, List<E621Post> posts) {
		maybeStartExpiresCheck();
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
		if (dateDiffMs > DAY_ms) {
			Date futureDate = new Date(System.currentTimeMillis() + dateDiffMs / 5l);
			// max cache time is restricted to MAX_CACHE_AGE days
			int daysBetween = CalendarUtil.getDaysBetween(new Date(), futureDate);
			if (Math.abs(daysBetween) > MAX_CACHE_AGE) {
				CalendarUtil.addDaysToDate(futureDate = new Date(), MAX_CACHE_AGE);
			}
			value.setExpires(futureDate);
		}
		Date expiration = value.getExpires();
		DomGlobal.console.log("Cache put: " + prefixedKey + " [" + new java.sql.Date(expiration.getTime()) + " "
				+ new java.sql.Time(expiration.getTime()) + "]");
		String jsonString = codec.encode(value).toString();
		jsonString = LZSEncoding.compressToUTF16(jsonString);
		String s1 = memCache.put(prefixedKey, jsonString);
		try {
			String s2 = storage.put(prefixedKey, jsonString);
			return s1 == null ? s2 : s1;
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			clearOldestFromLocalStorage();
			try {
				String s2 = storage.put(prefixedKey, jsonString);
				return s1 == null ? s2 : s1;
			} catch (Exception e1) {
				GWT.log(e1.getMessage(), e1);
				storage.clear();
				storage.put(prefixedKey, jsonString);
				return s1;
			}
		}
	}

	private void clearOldestFromLocalStorage() {
		DomGlobal.console.log("Force Early Expire (put exception)");
		class KeyDate {
			String key;
			Date expires;
		}
		List<KeyDate> forRemoval = new ArrayList<>();
		for (String prefixedKey : storage.keySet()) {
			String jsonString = storage.get(prefixedKey);
			if (jsonString == null) {
				storage.remove(prefixedKey);
				continue;
			}
			// remove legacy data
			if (jsonString.contains("\"expires\"")) {
				storage.remove(prefixedKey);
				continue;
			}
			try {
				jsonString = LZSEncoding.decompressFromUTF16(jsonString);
			} catch (Exception e) {
				storage.remove(prefixedKey);
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
				storage.remove(prefixedKey);
				continue;
			}
			if (cached == null || cached.getExpires() == null) {
				storage.remove(prefixedKey);
			}
			KeyDate k = new KeyDate();
			k.expires = cached.getExpires();
			k.key = prefixedKey;
			forRemoval.add(k);
		}
		try {
			Collections.sort(forRemoval, (a, b) -> a.expires.compareTo(b.expires));
			DomGlobal.console.log("Have " + storage.size() + " entries in the cache.");
			int size = forRemoval.size() / 4 + 1;
			DomGlobal.console.log("Force expiring " + size + " entries.");
			forRemoval.subList(0, size).forEach((k) -> storage.remove(k.key));
			DomGlobal.console.log("Have " + storage.size() + " remaining entries in the storage.");
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
		}
	}

	public List<E621Post> get(String key) {
		String prefixedKey = prefix + key;
		return _get(prefixedKey);
	}

	private List<E621Post> _get(String prefixedKey) {
		String jsonString = _getJsonString(prefixedKey);
		if (jsonString == null) {
			return null;
		}
		try {
			Cached decoded = codec.decode(jsonString);
			if (decoded == null || decoded.isExpired()) {
				startExpiresCheck();
				return null;
			}
			List<E621Post> posts = decoded.getPosts();
			if (posts != null && !posts.isEmpty()) {
				return posts;
			}
			return null;
		} catch (Exception e) {
			GWT.log("cache exception: " + prefixedKey, e);
			_remove(prefixedKey);
			return null;
		}
	}

	private String _getJsonString(String prefixedKey) {
		String jsonString = memCache.get(prefixedKey);
		if (jsonString == null) {
			jsonString = storage.get(prefixedKey);
			if (jsonString != null) {
				// copy into memCache the item it did not have
				memCache.put(prefixedKey, jsonString);
			}
		}
		if (jsonString == null) {
			return null;
		}
		// remove legacy data
		if (jsonString.contains("\"expires\"")) {
			GWT.log("remove legacy data: " + _remove(prefixedKey));
			return null;
		}
		try {
			return LZSEncoding.decompressFromUTF16(jsonString);
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			_remove(prefixedKey);
		}
		return null;
	}

	private void maybeStartExpiresCheck() {
		// only run expires check 1 in 10 times called to keep system load low
		if (new Random().nextInt(10) != 0) {
			return;
		}
		startExpiresCheck();
	}
	
	private boolean expiresCheckRunning=false;
	private void startExpiresCheck() {
		if (expiresCheckRunning) {
			return;
		}
		expiresCheckRunning=true;
		DomGlobal.console.log("Expires check started");
		// one item checked per javascript event loop to prevent browser hangs
		Iterator<String> iter = prefixedKeySet().iterator();
		expiresCheck(iter);
	}

	private void expiresCheck(Iterator<String> iter) {
		if (!iter.hasNext()) {
			DomGlobal.console.log("Expires check completed");
			expiresCheckRunning=false;
			return;
		}
		Scheduler.get().scheduleDeferred(() -> _expiresCheck(iter));
	}

	private void _expiresCheck(Iterator<String> iter) {
		String prefixedKey = iter.next();
		String jsonString = _getJsonString(prefixedKey);
		if (jsonString == null) {
			expiresCheck(iter);
			return;
		}
		if (!jsonString.contains("\"expires\"")) {
			expiresCheck(iter);
			return;
		}
		CachedExpiration decoded;
		try {
			decoded = expiresCodec.decode(jsonString);
		} catch (Exception e) {
			_remove(prefixedKey);
			GWT.log(e.getMessage(), e);
			expiresCheck(iter);
			return;
		}
		if (decoded==null || decoded.getExpires()==null) {
			_remove(prefixedKey);
			expiresCheck(iter);
			return;
		}
		if (decoded.isExpired()) {
			String expired = "";
			long expiresMs = decoded.getExpires().getTime();
			expired = " [" + new java.sql.Date(expiresMs);
			expired += " " + new java.sql.Time(expiresMs) + "]";
			DomGlobal.console.log("Expired: " + prefixedKey + expired);
			_remove(prefixedKey);
			expiresCheck(iter);
			return;
		}
		expiresCheck(iter);
	}

	private Set<String> prefixedKeySet() {
		Set<String> set = new TreeSet<>(storage.keySet());
		set.addAll(memCache.keySet());
		return set;
	}

	public void clear() {
		DomGlobal.console.log("Index Cache Clear");
		for (String prefixedKey : prefixedKeySet()) {
			if (prefixedKey.startsWith(LIST_E621POST)) {
				_remove(prefixedKey);
			}
		}
	}

	public String remove(String key) {
		String prefixedKey = prefix + key;
		return _remove(prefixedKey);
	}

	private String _remove(String prefixedKey) {
		String v1 = storage.remove(prefixedKey);
		String v2 = memCache.remove(prefixedKey);
		return v1 == null ? v2 : v1;
	}
}
