package e621;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.JSONP;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import e621.models.post.index.E621Post;
import e621.models.post.tags.E621Tag;

/**
 * See <a href='https://e621.net/help/show/api'>E621 Api Documentation</a>.
 * @author muksihs
 *
 */
@Options(serviceRootKey="E621")
public interface E621RestApi extends RestService {
	@Path("post/index.json")
	@JSONP
	void index(@QueryParam("tags") String tags, @QueryParam("before_id") long beforeId, @QueryParam("limit") int limit, @QueryParam("typed_tags") boolean typedTags, MethodCallback<List<E621Post>> callback);
	
	@Path("post/index.json")
	@JSONP
	void index(@QueryParam("tags") String tags, @QueryParam("limit") int limit, @QueryParam("typed_tags") boolean typedTags, MethodCallback<List<E621Post>> callback);
	
	@Path("post/tags.json")
	@JSONP
	void tags(@QueryParam("id")long id, MethodCallback<List<E621Tag>> callback);
	
	@Path("post/tags.json")
	@JSONP
	void tags(@QueryParam("md5")String md5, MethodCallback<List<E621Tag>> callback);
}
