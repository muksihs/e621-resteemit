package e621;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.JSONP;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import e621.models.post.index.E621PostList;
import e621.models.post.tags.E621TagList;
import e621.models.tag.index.TagList;

/**
 * See <a href='https://e621.net/help/show/api'>E621 Api Documentation</a>.
 * 
 * @author muksihs
 *
 */
@Options(serviceRootKey = "E621")
public interface E621RestApi extends RestService {
	@Path("post/index.json")
	@JSONP
	void postIndex(@QueryParam("tags") String tags, @QueryParam("before_id") int beforeId, @QueryParam("limit") int limit,
			MethodCallback<E621PostList> callback);

	@Path("post/index.json")
	@JSONP
	void postIndex(@QueryParam("tags") String tags, @QueryParam("limit") int limit,
			MethodCallback<E621PostList> callback);

	@Path("post/tags.json")
	@JSONP
	void postTags(@QueryParam("id") long id, MethodCallback<E621TagList> callback);

	@Path("post/tags.json")
	@JSONP
	void postTags(@QueryParam("md5") String md5, MethodCallback<E621TagList> callback);
	
	@Path("tag/index.json")
	@JSONP
	void tagList(@QueryParam("page")int page, @QueryParam("limit")int limit, MethodCallback<TagList> callback);
}
