package e621.models.tag.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(value=Include.ALWAYS)
public class Tag implements Comparable<Tag> {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (name != null) {
			builder.append(name);
		}
		builder.append("[");
		builder.append(count);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Tag objects are considered the same if their names are the same. Change this
	 * assumption and the app will break.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Tag)) {
			return false;
		}
		Tag other = (Tag) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@JsonProperty("id")
	private long id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("count")
	private long count;
	@JsonProperty("type")
	private int type;
	@JsonProperty("type_locked")
	private Object typeLocked;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getTypeLocked() {
		return typeLocked;
	}

	public void setTypeLocked(Object typeLocked) {
		this.typeLocked = typeLocked;
	}

	/**
	 * Required for TreeSet, comparison is only by case insensitive name
	 */
	@Override
	public int compareTo(Tag o) {
		if (o==null) {
			return 1;
		}
		if (name==null && o.name==null) {
			return 0;
		}
		if (name==null && o.name!=null) {
			return 1;
		}
		return name.compareToIgnoreCase(o.name);
	}
}
