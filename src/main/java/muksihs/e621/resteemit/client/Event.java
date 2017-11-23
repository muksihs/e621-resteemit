package muksihs.e621.resteemit.client;

import java.util.List;
import java.util.Set;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import muksihs.e621.resteemit.shared.MatchingTagsState;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.View;

public interface Event {

	public class ConfirmPost extends GenericEvent {
		private final MatchingTagsState state;
		public ConfirmPost(MatchingTagsState state) {
			this.state=state;
		}
		public MatchingTagsState getState() {
			return state;
		}

	}

	public class ShowAbout extends GenericEvent {

	}

	public class LoginLogout extends GenericEvent {

	}

	public class TryLogin extends GenericEvent {
		private final String username;
		private final String wif;
		private final boolean silent;

		public TryLogin(String username, String wif) {
			this(username, wif, false);
		}

		public TryLogin(String username, String wif, boolean silent) {
			this.username = username;
			this.wif = wif;
			this.silent = silent;
		}

		public String getUsername() {
			return username;
		}

		public String getWif() {
			return wif;
		}

		public boolean isSilent() {
			return silent;
		}
	}

	public class LoginComplete extends GenericEvent {
		private final boolean loggedIn;

		public LoginComplete(boolean loggedIn) {
			this.loggedIn = loggedIn;
		}

		public boolean isLoggedIn() {
			return loggedIn;
		}
	}

	public class ShowLoginUi extends GenericEvent {
	}

	public class ClearSearch extends GenericEvent {

	}

	public class AlertMessage extends GenericEvent {
		private final String message;

		public AlertMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class MostRecentSet extends GenericEvent {

	}

	public class QuickMessage extends GenericEvent {
		private final String message;

		public QuickMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class Login<T extends GenericEvent> extends GenericEvent {
		private final T refireEvent;

		public Login(T event) {
			this.refireEvent = event;
		}

		public T getRefireEvent() {
			return refireEvent;
		}
	}

	public class SteemPost extends GenericEvent {
		private final PostPreview preview;

		public SteemPost(PostPreview preview) {
			this.preview = preview;
		}

		public PostPreview getPreview() {
			return preview;
		}

	}

	public class FatalError extends GenericEvent {

		private final String message;

		public FatalError(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

	public class SetRatingsBoxes extends GenericEvent {

		private final Set<String> mustHaveRatings;

		public SetRatingsBoxes(Set<String> mustHaveRatings) {
			this.mustHaveRatings = mustHaveRatings;
		}

		public Set<String> getMustHaveRatings() {
			return mustHaveRatings;
		}

	}

	public class BrowseViewLoaded extends GenericEvent {

	}

	public class EnablePreviousButton extends GenericEvent {

		private final boolean enable;

		public EnablePreviousButton(boolean enable) {
			this.enable = enable;
		}

		public boolean isEnable() {
			return enable;
		}

	}

	public class RemoveFromFilter extends GenericEvent {
		private final String tag;

		public RemoveFromFilter(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

	}

	public static enum Rating {
		SAFE("s"), QUESTIONABLE("q"), EXPLICIT("e");
		private final String tag;

		private Rating(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}
	}

	public class SetRating extends GenericEvent {
		private final Set<Rating> rating;

		public SetRating(Set<Rating> rating) {
			this.rating = rating;
		}

		public Set<Rating> getRating() {
			return rating;
		}
	}

	public class ShowFilterTags extends GenericEvent {
		private final Set<String> tags;

		public ShowFilterTags(Set<String> tags) {
			this.tags = tags;
		}

		public Set<String> getTags() {
			return tags;
		}
	}

	public class AddToExcludeFilter extends GenericEvent {
		private final String tag;

		public AddToExcludeFilter(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

	}

	public class AddToIncludeFilter extends GenericEvent {
		private final String tag;

		public AddToIncludeFilter(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

	}

	public class PreviewsLoaded extends GenericEvent {

	}

	public class PreviousPreviewSet extends GenericEvent {
		public PreviousPreviewSet() {
		}
	}

	public class NextPreviewSet extends GenericEvent {
		public NextPreviewSet() {
		}
	}

	public class ShowAvailableTags extends GenericEvent {
		private final Set<String> tags;

		public ShowAvailableTags(Set<String> availableTags) {
			this.tags = availableTags;
		}

		public Set<String> getTags() {
			return tags;
		}

	}

	public class ShowPreviews extends GenericEvent {

		private final List<PostPreview> previews;

		public ShowPreviews(List<PostPreview> previews) {
			this.previews = previews;
		}

		public List<PostPreview> getPreviews() {
			return previews;
		}

	}

	public class LoadInitialPreviews extends GenericEvent {

	}

	public class Loading extends GenericEvent {

		private final boolean loading;

		public Loading(boolean loading) {
			this.loading = loading;
		}

		public boolean isLoading() {
			return loading;
		}

	}

	public class ShowView extends GenericEvent {
		private final View view;

		public ShowView(View view) {
			this.view = view;
		}

		public View getView() {
			return view;
		}
	}

	public class AppVersion extends GenericEvent {
		private final String appVersion;

		public AppVersion(String appVersion) {
			this.appVersion = appVersion;
		}

		public String getAppVersion() {
			return appVersion;
		}
	}

	public class GetAppVersion extends GenericEvent {

	}
}
