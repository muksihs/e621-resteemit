package e621.models.tag.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tag {
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
}
