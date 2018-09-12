package e621;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.JSONP;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import com.google.gwt.core.client.GWT;

import e621.models.post.index.E621Post;
import e621.models.post.tags.E621Tag;
import e621.models.tag.index.Tag;
import e621.models.user.E621UserLoginResult;
import e621.models.user.E621UserInfo;

/**
 * See <a href='https://e621.net/help/show/api'>E621 Api Documentation</a>.
 * 
 * @author muksihs
 *
 */
@Options(serviceRootKey = "E621")
public interface E621RestApi extends RestService {
	public static final int TIMEOUT = 15000;
	public static final int SHORT_TIMEOUT = 1000;

	@Options(timeout = TIMEOUT)
	@Path("post/index.json")
	@JSONP
	void postIndex(@QueryParam("tags") String tags, @QueryParam("before_id") int beforeId,
			@QueryParam("limit") int limit, MethodCallback<List<E621Post>> callback);

	@Options(timeout = TIMEOUT)
	@Path("post/index.json")
	@JSONP
	void postIndex(@QueryParam("tags") String tags, @QueryParam("limit") int limit,
			MethodCallback<List<E621Post>> callback);

	@Options(timeout = TIMEOUT)
	@Path("post/tags.json")
	@JSONP
	void postTags(@QueryParam("id") long id, MethodCallback<List<E621Tag>> callback);

	@Options(timeout = TIMEOUT)
	@Path("post/tags.json")
	@JSONP
	void postTags(@QueryParam("md5") String md5, MethodCallback<List<E621Tag>> callback);

	@Options(timeout = TIMEOUT)
	@Path("tag/index.json")
	@JSONP
	void tagList(@QueryParam("page") int page, @QueryParam("limit") int limit, MethodCallback<List<Tag>> callback);

	@Options(timeout = SHORT_TIMEOUT)
	@Path("tag/related.json")
	@JSONP
	void tagRelated(@QueryParam("tags") String tags, MethodCallback<Map<String, List<List<String>>>> callback);

	@Options(timeout = TIMEOUT)
	@Path("user/logout.json")
	@JSONP
	void logout(MethodCallback<Void> noop);

	@Options(timeout = TIMEOUT)
	@Path("user/login.json")
	@JSONP
	void userLogin(@QueryParam("name") String name, @QueryParam("password") String password,
			MethodCallback<E621UserLoginResult> callback);

	@Options(timeout = TIMEOUT)
	@Path("user/show.json")
	@JSONP
	void userShow(MethodCallback<E621UserInfo> callback);
}
