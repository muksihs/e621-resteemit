package e621;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.Options;

import e621.models.post.index.E621List;
import e621.models.post.tags.E621Tag;

/**
 * See <a href='https://e621.net/help/show/api'>E621 Api Documentation</a>.
 * @author muksihs
 *
 */
@Produces("application/json")
@Consumes("application/json")
@Options(serviceRootKey="https://e621.net/")
public interface E621Api {
	@GET
	@Path("post/index.json?typed_tags=true")
	E621List index(@QueryParam("tags") String tags, @QueryParam("before_id") long beforeId, @QueryParam("limit") int limit);
	
	@GET
	@Path("post/index.json?typed_tags=true")
	E621List index(@QueryParam("tags") String tags, @QueryParam("limit") int limit);
	
	@GET
	@Path("post/tags.json")
	List<E621Tag> tags(@QueryParam("id")long id);
	
	@GET
	@Path("post/tags.json")
	List<E621Tag> tags(@QueryParam("md5")String md5);
}
