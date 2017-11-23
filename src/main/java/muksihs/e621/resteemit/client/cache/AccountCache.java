package muksihs.e621.resteemit.client.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;

import blazing.chain.LZSEncoding;
import muksihs.e621.resteemit.shared.SteemPostingInfo;

public class AccountCache {
	private static final String STEEM_ACCOUNT_INFO = "steem-account-info";

	// only deserialize the date property for the expires check loop
	protected interface ExpiresCodec extends JsonEncoderDecoder<CachedExpiration> {
	}

	protected final ExpiresCodec expiresCodec = GWT.create(ExpiresCodec.class);

	// full post list stashing in html5 local storage
	protected interface Codec extends JsonEncoderDecoder<CachedAccountInfo> {
	}

	protected final Codec codec = GWT.create(Codec.class);
	private final String prefix;
	private Map<String, String> storage = new StorageMap(Storage.getLocalStorageIfSupported());
	private Map<String, String> memCache = new HashMap<>();

	public AccountCache() {
		if (storage == null) {
			storage = memCache;
		}
		prefix = STEEM_ACCOUNT_INFO + ":";
	}

	public void put(String key, SteemPostingInfo info) {
		_put(prefix + key, info);
	}

	private void _put(String prefixedKey, SteemPostingInfo info) {
		CachedAccountInfo value = new CachedAccountInfo(info);
		String jsonString = codec.encode(value).toString();
		jsonString = LZSEncoding.compressToUTF16(jsonString);
		memCache.put(prefixedKey, jsonString);
		try {
			storage.put(prefixedKey, jsonString);
			return;
		} catch (Exception e) {
			GWT.log(e.getMessage(), e);
			try {
				storage.put(prefixedKey, jsonString);
				return;
			} catch (Exception e1) {
				GWT.log(e1.getMessage(), e1);
				storage.clear();
				storage.put(prefixedKey, jsonString);
				return;
			}
		}
	}

	public SteemPostingInfo get(String key) {
		String prefixedKey = prefix + key;
		SteemPostingInfo steemPostingInfo = _get(prefixedKey);
		return steemPostingInfo;
	}

	private SteemPostingInfo _get(String prefixedKey) {
		String jsonString = _getJsonString(prefixedKey);
		if (jsonString == null) {
			return null;
		}
		try {
			CachedAccountInfo decoded = codec.decode(jsonString);
			if (decoded == null || decoded.isExpired()) {
				return null;
			}
			SteemPostingInfo steemPostingInfo = decoded.getAccountInfo();
			return steemPostingInfo;
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
			GWT.log("remove legacy data: " + prefixedKey);
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

	private Set<String> prefixedKeySet() {
		Set<String> set = new TreeSet<>(storage.keySet());
		set.addAll(memCache.keySet());
		return set;
	}

	public void clear() {
		for (String prefixedKey : prefixedKeySet()) {
			if (prefixedKey.startsWith(STEEM_ACCOUNT_INFO)) {
				_remove(prefixedKey);
			}
		}
	}

	public void remove(String key) {
		String prefixedKey = prefix + key;
		_remove(prefixedKey);
	}

	private void _remove(String prefixedKey) {
		storage.remove(prefixedKey);
		memCache.remove(prefixedKey);
	}
}
