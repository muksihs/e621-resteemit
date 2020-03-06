package e621;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import e621.models.post.tags.E621Tag;
import e621.models.posts.E621PostsResponse;
import e621.models.tag.index.Tag;
import e621.models.user.E621UserInfo;
import e621.models.user.E621UserLoginResult;

/**
 * See <a href='https://e621.net/help/show/api'>E621 Api Documentation</a>.
 * 
 * @author muksihs
 *
 */
@Options(serviceRootKey = "E621")
public interface E621RestApi extends RestService {
	int TIMEOUT = 15000;
	int SHORT_TIMEOUT = 1000;

	@GET
	@Path("posts.json")
	void postIndex(@QueryParam("tags") String tags, @QueryParam("before_id") int beforeId,
			@QueryParam("limit") int limit, MethodCallback<E621PostsResponse> callback);

	@GET
	@Path("posts.json")
	void postIndex(@QueryParam("tags") String tags, @QueryParam("limit") int limit,
			MethodCallback<E621PostsResponse> callback);

	@GET
	@Path("post/tags.json")
	void postTags(@QueryParam("id") long id, MethodCallback<List<E621Tag>> callback);

	@GET
	@Path("post/tags.json")
	void postTags(@QueryParam("md5") String md5, MethodCallback<List<E621Tag>> callback);

	@GET
	@Path("tags.json")
	void tagList(@QueryParam("page") int page, @QueryParam("limit") int limit, MethodCallback<List<Tag>> callback);

	@GET
	@Path("tags/related.json")
	void tagRelated(@QueryParam("tags") String tags, MethodCallback<Map<String, List<List<String>>>> callback);

	@GET
	@Path("user/logout.json")
	void logout(MethodCallback<Void> noop);

	@GET
	@Path("user/login.json")
	void userLogin(@QueryParam("name") String name, @QueryParam("password") String password,
			MethodCallback<E621UserLoginResult> callback);

	@GET
	@Path("user/show.json")
	void userShow(MethodCallback<E621UserInfo> callback);
}
