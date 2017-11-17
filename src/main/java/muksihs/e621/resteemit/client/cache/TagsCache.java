package muksihs.e621.resteemit.client.cache;

import java.util.List;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

import blazing.chain.LZSEncoding;
import e621.models.tag.index.Tag;

public class TagsCache {
	private static final String LIST_E621TAGS = "list-e621tags-";

	// only deserialize the date property for the expires check loop
	protected interface ExpiresCodec extends JsonEncoderDecoder<CachedExpiration> {
	}

	protected final ExpiresCodec expiresCodec = GWT.create(ExpiresCodec.class);

	// full post list stashing in html5 local storage
	protected interface Codec extends JsonEncoderDecoder<CachedTags> {
	}

	protected final Codec codec = GWT.create(Codec.class);
	private final String prefix;
	private StorageMap cache = new StorageMap(Storage.getLocalStorageIfSupported());

	public TagsCache(int cachedPageSize) {
		prefix = LIST_E621TAGS + cachedPageSize + ":";
	}

	public void put(String key, List<Tag> tags) {
		expiresCheck();
		CachedTags value = new CachedTags(tags);
		String jsonString = codec.encode(value).toString();
		jsonString = LZSEncoding.compressToUTF16(jsonString);
		try {
			cache.put(prefix + key, jsonString);
		} catch (Exception e) {
			clear();
			try {
				cache.put(prefix + key, jsonString);
			} catch (Exception e1) {
				// panic clear the whole mess
				cache.clear();
				cache.put(prefix + key, jsonString);
			}
		}
	}

	public List<Tag> get(String key) {
		expiresCheck();
		String jsonString = cache.get(prefix + key);
		if (jsonString == null) {
			return null;
		}
		// remove legacy data
		if (jsonString.contains("\"expires\"")) {
			cache.remove(prefix + key);
			return null;
		}
		try {
			try {
				jsonString = LZSEncoding.decompressFromUTF16(jsonString);
			} catch (Exception e) {
				cache.remove(prefix + key);
				return null;
			}
			List<Tag> tags = codec.decode(jsonString).getTags();
			if (tags != null && !tags.isEmpty()) {
				return tags;
			}
		} catch (Exception e) {
		}
		return null;
	}

	private void expiresCheck() {
		for (String key : cache.keySet()) {
			if (key.startsWith(LIST_E621TAGS)) {
				String jsonString = cache.get(key);
				if (jsonString == null) {
					continue;
				}
				// remove legacy data
				if (jsonString.contains("\"expires\"")) {
					cache.remove(prefix + key);
					continue;
				}

				CachedExpiration cached;
				try {
					jsonString = LZSEncoding.decompressFromUTF16(jsonString);
				} catch (Exception e) {
					cache.remove(key);
					continue;
				}
				try {
					cached = expiresCodec.decode(jsonString);
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
		for (String key : cache.keySet()) {
			if (key.startsWith(LIST_E621TAGS)) {
				cache.remove(key);
			}
		}
	}
}
