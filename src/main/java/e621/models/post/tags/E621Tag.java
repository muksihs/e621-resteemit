
package e621.models.post.tags;


public class E621Tag {

    private long id;
    private String name;
    private long count;
    private long type;
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

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public Object getTypeLocked() {
        return typeLocked;
    }

    public void setTypeLocked(Object typeLocked) {
        this.typeLocked = typeLocked;
    }

}
