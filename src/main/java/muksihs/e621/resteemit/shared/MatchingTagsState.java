package muksihs.e621.resteemit.shared;

import java.util.Iterator;
import java.util.List;

import e621.models.post.tags.E621Tag;
import muksihs.e621.resteemit.client.E621ResteemitApp;
import muksihs.e621.resteemit.client.E621ResteemitApp.TrendingTag;

public class MatchingTagsState {
	public List<TrendingTag> collector;
	public Iterator<E621Tag> iter;
	public PostPreview post;
	public List<E621Tag> withAlternateForms;

}