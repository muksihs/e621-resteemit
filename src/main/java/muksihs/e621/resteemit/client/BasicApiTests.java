package muksihs.e621.resteemit.client;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;

import e621.E621Api;
import e621.E621RestApi;
import e621.models.post.index.E621Post;
import e621.models.post.index.E621PostList;

public class BasicApiTests implements ScheduledCommand, IsSdm {

	@Override
	public void execute() {
		GWT.log(this.getClass().getSimpleName());
		E621RestApi api = E621Api.api();
		MethodCallback<E621PostList> callback = new MethodCallback<E621PostList>() {
			@Override
			public void onSuccess(Method method, E621PostList response) {
				GWT.log("Raw Response Size: " + response.size());
				for (E621Post post : response) {
					GWT.log("id: " + post.getId());
//					GWT.log("description: " + post.getDescription());
					GWT.log("sample url: " + post.getSampleUrl());
					GWT.log("file url: " + post.getFileUrl());
					GWT.log("preview url: " + post.getPreviewUrl());
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
