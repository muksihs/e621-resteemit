package e621.models.post.tags;

public enum E621TagTypes {
	/*
	 * The order of the enums determines the order they are added to the post.
	 */
	Artist(1), Copyright(3), Character(4), Species(5), General(0);
	private final int id;

	private E621TagTypes(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
