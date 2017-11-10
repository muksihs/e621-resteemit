package muksihs.e621.resteemit.client;

import java.util.List;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.View;

public interface Event {

	public class ShowPreviews extends GenericEvent {

		private final List<PostPreview> previews;

		public ShowPreviews(List<PostPreview> previews) {
			this.previews=previews;
		}

		public List<PostPreview> getPreviews() {
			return previews;
		}

	}

	public class InitialPreviews extends GenericEvent {

	}

	public class Loading extends GenericEvent {

		private final boolean loading;

		public Loading(boolean loading) {
			this.loading=loading;
		}

		public boolean isLoading() {
			return loading;
		}

	}

	public class ShowView extends GenericEvent {
		private final View view;
		public ShowView(View view) {
			this.view=view;
		}
		public View getView() {
			return view;
		}
	}

	public class AppVersion extends GenericEvent {
		private final String appVersion;
		public AppVersion(String appVersion) {
			this.appVersion=appVersion;
		}
		public String getAppVersion() {
			return appVersion;
		}
	}

	public class GetAppVersion extends GenericEvent {

	}
}
