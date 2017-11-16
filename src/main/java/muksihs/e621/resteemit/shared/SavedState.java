package muksihs.e621.resteemit.shared;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class SavedState {
	private static final String RATINGS = "ratings:";
	private static final String TAGS = "tags:";
	private static final String POST_ID = "postId:";

	public static String asHistoryToken(SavedState param) {
		StringBuilder sb = new StringBuilder();
		if (param.ratings != null && !param.ratings.isEmpty()) {
			sb.append(">");
			sb.append(RATINGS);
			Iterator<String> iter = param.ratings.iterator();
			while (iter.hasNext()) {
				sb.append(escape(iter.next()));
				if (iter.hasNext()) {
					sb.append("<");
				}
			}
		}
		if (param.postId > 0) {
			sb.append(">");
			sb.append(POST_ID);
			sb.append(param.postId);
		}
		Iterator<String> must = param.mustHave == null ? new HashSet<String>().iterator() : param.mustHave.iterator();
		Iterator<String> mustNot = param.mustNotHave == null ? new HashSet<String>().iterator()
				: param.mustNotHave.iterator();
		if (must.hasNext() || mustNot.hasNext()) {
			sb.append(">");
			sb.append(TAGS);
			while (must.hasNext() || mustNot.hasNext()) {
				if (must.hasNext()) {
					sb.append(escape(must.next()));
					if (mustNot.hasNext()) {
						sb.append("<");
					}
				}
				if (mustNot.hasNext()) {
					sb.append("-");
					sb.append(escape(mustNot.next()));
				}
				if (must.hasNext() || mustNot.hasNext()) {
					sb.append("<");
				}
			}
		}
		return sb.toString();

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SavedState [postId=");
		builder.append(postId);
		builder.append(", ");
		if (mustHave != null) {
			builder.append("mustHave=");
			builder.append(mustHave);
			builder.append(", ");
		}
		if (mustNotHave != null) {
			builder.append("mustNotHave=");
			builder.append(mustNotHave);
			builder.append(", ");
		}
		if (ratings != null) {
			builder.append("ratings=");
			builder.append(ratings);
		}
		builder.append("]");
		return builder.toString();
	}

	private static String escape(String token) {
		return token.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;");
	}

	private static String unescape(String token) {
		return token.replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&");
	}

	public static SavedState parseHistoryToken(String token) {
		SavedState param = new SavedState();
		if (token == null || token.trim().isEmpty()) {
			return param;
		}
		String[] parts = token.split(">");
		for (String part : parts) {
			if (part.toLowerCase().startsWith(TAGS.toLowerCase())) {
				part = part.substring(TAGS.length());
				String[] tags = part.split("<");
				for (String tag : tags) {
					tag=tag.toLowerCase().trim();
					if (tag.startsWith("-")) {
						param.mustNotHave.add(unescape(tag.substring(1)));
					} else {
						param.mustHave.add(unescape(tag));
					}
				}
				continue;
			}
			if (part.toLowerCase().startsWith(RATINGS.toLowerCase())) {
				part = part.substring(RATINGS.length());
				String[] tags = part.split("<");
				for (String tag : tags) {
					tag=tag.toLowerCase().trim();
					param.ratings.add(unescape(tag));
				}
				continue;
			}
			if (part.toLowerCase().startsWith(POST_ID.toLowerCase())) {
				part = part.substring(POST_ID.length()).trim();
				try {
					param.postId = Long.valueOf(part);
				} catch (NumberFormatException e) {
					param.postId = 0;
				}
				continue;
			}
		}
		return param;
	}

	private long postId=0;
	private Set<String> mustHave=new TreeSet<>();
	private Set<String> mustNotHave=new TreeSet<>();
	private Set<String> ratings=new TreeSet<>();

	public void setMustHave(Set<String> mustHave) {
		this.mustHave = mustHave;
	}

	public void setMustNotHave(Set<String> mustNotHave) {
		this.mustNotHave = mustNotHave;
	}

	public void setRatings(Set<String> ratings) {
		this.ratings = ratings;
	}

	public long getPostId() {
		return postId;
	}

	public void setPostId(long postId) {
		this.postId = postId;
	}

	public Set<String> getMustHave() {
		return mustHave;
	}

	public Set<String> getMustNotHave() {
		return mustNotHave;
	}

	public Set<String> getRatings() {
		return ratings;
	}
}
