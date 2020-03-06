package muksihs.e621.resteemit.client;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;

import e621.E621Api;
import e621.E621RestApi;
import e621.models.posts.E621Post;
import e621.models.posts.E621PostsResponse;
import e621.models.posts.Post;

public class BasicApiTests implements ScheduledCommand, IsSdm {

	@Override
	public void execute() {
		GWT.log(this.getClass().getSimpleName());
		E621RestApi api = E621Api.api();
		MethodCallback<E621PostsResponse> callback = new MethodCallback<E621PostsResponse>() {
			@Override
			public void onSuccess(Method method, E621PostsResponse response) {
				if (response == null || response.getPosts() == null) {
					GWT.log("null response.");
					onFailure(method, new RuntimeException("null response"));
					return;
				}
				GWT.log("Raw Response Size: " + response.getPosts().size());
				for (Post post : response.getPosts()) {
					GWT.log("id: " + post.getId());
//					GWT.log("description: " + post.getDescription());
					GWT.log("sample url: " + post.getSample().getUrl());
					GWT.log("file url: " + post.getFile().getUrl());
					GWT.log("preview url: " + post.getPreview().getUrl());
					GWT.log("");
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				GWT.log("Failure: " + method.getResponse().getText(), exception);
			}
		};
		api.postIndex("", 1, callback);
	}

}
