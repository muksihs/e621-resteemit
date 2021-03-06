package muksihs.e621.resteemit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.google.web.bindery.event.shared.binder.GenericEvent;

import e621.E621Api;
import e621.models.post.tags.E621Tag;
import e621.models.post.tags.E621TagTypes;
import e621.models.posts.E621PostsResponse;
import e621.models.posts.Post;
import e621.models.posts.PostsResponse;
import e621.models.tag.index.Tag;
import elemental2.dom.DomGlobal;
import muksihs.e621.resteemit.client.cache.AccountCache;
import muksihs.e621.resteemit.client.cache.IndexCache;
import muksihs.e621.resteemit.client.cache.TagsCache;
import muksihs.e621.resteemit.shared.Consts;
import muksihs.e621.resteemit.shared.E621Rating;
import muksihs.e621.resteemit.shared.MatchingTagsState;
import muksihs.e621.resteemit.shared.MostRecentPostInfo;
import muksihs.e621.resteemit.shared.PostPreview;
import muksihs.e621.resteemit.shared.SavedState;
import muksihs.e621.resteemit.shared.SteemPostingInfo;
import muksihs.e621.resteemit.shared.View;
import muksihs.e621.resteemit.ui.MainView;
import steem.CommentMetadata;
import steem.CommentResult;
import steem.SteemApi;
import steem.SteemAuth;
import steem.SteemBroadcast;
import steem.SteemBroadcast.Beneficiary;
import steem.SteemBroadcast.CommentOptionsExtensions;
import steem.SteemCallback;
import steem.SteemCallbackArray;
import steem.TrendingTagsResult;
import steem.VoteResult;
import steem.model.accountinfo.AccountInfo;
import steem.model.accountinfo.Posting;

public class E621ResteemitApp implements ScheduledCommand, GlobalEventBus, ValueChangeHandler<String> {

	private static final int MAX_TAGS_PER_POST = 17;
	private static final String BENEFICIARY_ACCOUNT = "muksihs";
	private static final Beneficiary BENEFICIARY = new Beneficiary(BENEFICIARY_ACCOUNT, 10);
	private static final String DEFAULT_USER = "default-user";
	/**
	 * A non-empirical and non-arbitrary number to skew lower usage E621 tags higher
	 * as part of the automatic steem tag selection process.
	 */
	private static final double E621_TAG_WEIGHT = 2.65d;
	// private static final int CACHED_PAGE_SIZE = 20;
	private static final int CACHED_PAGE_SIZE = 50;
	private static final IndexCache INDEX_CACHE = new IndexCache(CACHED_PAGE_SIZE);

	public E621ResteemitApp() {
		extensionsWhitelist.addAll(Arrays.asList("png", "jpg", "gif", "jpeg"));
		e621logout();
	}

	private static final int MAX_TAGS_PER_QUERY = 6;

	interface MyEventBinder extends EventBinder<E621ResteemitApp> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private RootPanel rp;
	private ViewController controller;

	private final Set<String> mustHaveRatings = new TreeSet<>();
	private final Set<String> mustHaveTags = new TreeSet<>();
	private final Set<String> mustNotHaveTags = new TreeSet<>();
	private final List<PostPreview> activeSet = new ArrayList<>();
	private final Set<String> extensionsWhitelist = new TreeSet<>();
	private int activePage;
	private PostPreview zoomPreview;

	public static class TrendingTag {
		public TrendingTag() {
		}
		public TrendingTag(String name) {
			this.name=name;
		}
		public String name;
		public int topPosts;
		public int comments;
		public double totalPayouts;
		public Long e621Count;
		public double weight;
	}

	public void matchingTagsCollector(Map<String, String> error, TrendingTagsResult[] tags, E621Tag e621tag,
			MatchingTagsState state) {
		if (error != null) {
			GWT.log(String.valueOf(error));
		}
		if (tags != null) {
			for (TrendingTagsResult trendingTag : tags) {
				if (!trendingTag.getName().equals(e621tag.getName())) {
					continue;
				}
				TrendingTag tag = new TrendingTag();
				tag.e621Count = e621tag.getCount();
				tag.comments = trendingTag.getComments();
				tag.name = trendingTag.getName();
				tag.topPosts = trendingTag.getTop_posts();
				try {
					tag.totalPayouts = Double.valueOf(trendingTag.getTotal_payouts().replace(" SBD", ""));
				} catch (NumberFormatException e) {
				}
				state.matchingSteemTags.add(tag);
			}
		}
		getMatchingTrendingTags(state);
	}

	private void getMatchingTrendingTags(MatchingTagsState state) {
		if (!state.iter.hasNext()) {
			selectBestTagsAndConfirmPost(state);
			return;
		}
		E621Tag e621tag = state.iter.next();
		SteemApi.getTrendingTags(e621tag.getName(), 1, (e, r) -> matchingTagsCollector(e, r, e621tag, state));
	}

	private void selectBestTagsAndConfirmPost(MatchingTagsState state) {
		// calculator divisor
		double maxPayout = 1d;// 1 is min to prevent divide by zero
		long maxE621Count = 1l;// 1 is min to prevent divide by zero
		for (TrendingTag tag : state.matchingSteemTags) {
			maxPayout = Double.max(maxPayout, tag.totalPayouts);
			maxE621Count = Long.max(maxE621Count, tag.e621Count);
		}
		// calculate percent weights
		for (TrendingTag tag : state.matchingSteemTags) {
			// caclulate e621 weighting then reverse it so that less used tags get more
			// weight
			tag.weight = (1d - (double) tag.e621Count / (double) maxE621Count) * E621_TAG_WEIGHT;
			// add total payout weight
			tag.weight += tag.totalPayouts / maxPayout;
		}
		// sort descending order to most valuable at top of list
		Collections.sort(state.matchingSteemTags, (a, b) -> {
			// sort by per post payout desc (is weighted heavily towards less used E621
			// tags)
			if (Double.compare(a.weight, b.weight) != 0) {
				return Double.compare(b.weight, a.weight);
			}
			// sort by payout (raw value of topic)
			if (a.totalPayouts != b.totalPayouts) {
				return Double.compare(b.totalPayouts, a.totalPayouts);
			}
			// sort by number of recent posts
			if (a.topPosts != b.topPosts) {
				return Integer.compare(b.topPosts, a.topPosts);
			}
			// sort by total number of comments
			if (a.comments != b.comments) {
				return Integer.compare(b.comments, a.comments);
			}
			return a.name.compareToIgnoreCase(b.name);
		});
		List<TrendingTag> selectedTags;
		boolean isSafe = !state.post.getRating().equals(E621Rating.EXPLICIT.getTag());
		if (!isSafe) {
			int min = Math.min(state.matchingSteemTags.size(), MAX_TAGS_PER_POST - 5);
			selectedTags = state.matchingSteemTags.subList(0, min);
			TrendingTag yiffTag = new TrendingTag("yiff");
			state.matchingSteemTags.add(yiffTag);
			selectedTags.add(yiffTag);
			TrendingTag nsfwTag = new TrendingTag("nsfw");
			state.matchingSteemTags.add(nsfwTag);
			selectedTags.add(nsfwTag);
		} else {
			int min = Math.min(state.matchingSteemTags.size(), MAX_TAGS_PER_POST - 3);
			selectedTags = state.matchingSteemTags.subList(0, min);
		}
		TrendingTag arttag = new TrendingTag("art");
		selectedTags.add(0, arttag);
		TrendingTag e621tag = new TrendingTag("e621");
		selectedTags.add(0, e621tag);
		TrendingTag furryTag = new TrendingTag("furry");
		selectedTags.add(0, furryTag);
		state.tagsForPost = new ArrayList<>();
		Iterator<TrendingTag> iter = selectedTags.iterator();
		while (iter.hasNext()) {
			state.tagsForPost.add(iter.next().name);
		}
		StringBuilder sb = new StringBuilder();
		for (TrendingTag selectedTag : selectedTags) {
			int tmp = (int) (selectedTag.weight * 100d);
			sb.append(selectedTag.name + " [" + tmp + "/" + (int) (selectedTag.totalPayouts / maxPayout * 100d)
					+ "], ");
		}
		DomGlobal.console.log(String.join(" ", state.tagsForPost));
		DomGlobal.console.log(sb.toString());
		fireEvent(new Event.Loading(false));
		/*
		 * In theory, there should never be more than one pending post possible.
		 */
		pendingPost = state;
		fireEvent(new Event.ConfirmPost());
	}
	
	@EventHandler
	protected void loadFilter(Event.LoadFilter event) {
		
	}
	
	@EventHandler
	protected void saveFilter(Event.SaveFilter event) {
		SavedState hash = getSavedStateHash();
		hash.setPostId(0);
		AccountCache accountCache = new AccountCache();
		SteemPostingInfo info = accountCache.get(DEFAULT_USER);
		info.getSavedFilters().add(SavedState.asHistoryToken(hash));
		accountCache.put(DEFAULT_USER, info);
		fireEvent(new Event.QuickMessage("Filter Saved!"));
	}
			
	@EventHandler
	protected void getUpvotePreference(Event.GetUpvotePreference event) {
		SteemPostingInfo user = new AccountCache().get(DEFAULT_USER);
		if (user == null) {
			fireEvent(new Event.SetUpvotePreference(false));
			return;
		}
		fireEvent(new Event.SetUpvotePreference(user.isUpvote()));
	}

	@EventHandler
	protected void updateUpvotePreference(Event.UpdateUpvotePreference event) {
		AccountCache accountCache = new AccountCache();
		SteemPostingInfo user = accountCache.get(DEFAULT_USER);
		if (user == null) {
			return;
		}
		user.setUpvote(event.isUpvote());
		accountCache.put(DEFAULT_USER, user);
	}

	@EventHandler
	protected void getModalImage(Event.GetModalImage event) {
		fireEvent(new Event.SetModalImage(zoomPreview));
	}

	@EventHandler
	protected void zoomImage(Event.ZoomImage event) {
		zoomPreview = event.getPreview();
		fireEvent(new Event.ImageModal());
	}

	@EventHandler
	protected void getAutomaticTags(Event.GetAutomaticTags event) {
		fireEvent(new Event.SetAutomaticTags(pendingPost.tagsForPost));
	}

	/*
	 * In theory, there should never be more than one pending post possible.
	 */
	private MatchingTagsState pendingPost;

	@EventHandler
	protected void automaticTitle(Event.GetAutomaticTitle event) {
		List<E621Tag> withAlternateForms = pendingPost.asSteemFormatted;
		long id = pendingPost.post.getId();
		fireEvent(new Event.SetPostTitle(generateTitle(withAlternateForms, id)));
	}

	@EventHandler
	protected void doPost(Event.DoPost event) {
		AccountCache cache = new AccountCache();
		SteemPostingInfo info = cache.get(DEFAULT_USER);
		if (info == null) {
			fireEvent(new Event.Loading(false));
			fireEvent(new Event.Login<GenericEvent>(new Event.ConfirmPost()));
			return;
		}
		List<String> tagsForPost = pendingPost.tagsForPost;
		// //always add author names to tags list if not already in list
		// Set<String> artists = getArtists(pendingPost.e621tags);
		// tagsForPost.addAll(artists);

		String username = info.getUsername();
		final CommentMetadata metadata = new CommentMetadata();
		final String body;
		String title;
		final String permLink;
		final String author;
		final String firstTag;
		final String parentAuthor;
		final String wif;
		try {
			metadata.setApp("MuksihsE621Browser/" + Consts.APP_VERSION);
			metadata.setFormat("html");
			metadata.setTags(tagsForPost.toArray(new String[0]));
			body = generatePostHtml();
			title = event.getTitle();
			if (title == null || title.trim().isEmpty()) {
				List<E621Tag> tagList = pendingPost.asSteemFormatted;
				long id = pendingPost.post.getId();
				title = generateTitle(tagList, id);
			}
			permLink = generatePermaLink(title);
			author = username;
			firstTag = tagsForPost.get(0);
			parentAuthor = "";
			wif = info.getWif();
		} catch (Exception e1) {
			fireEvent(new Event.Loading(false));
			fireEvent(new Event.AlertMessage("An error occurred during post preperation: " + e1.getMessage()));
			GWT.log(e1.getMessage(), e1);
			return;
		}
		SteemCallback<VoteResult> voteCb = (error, result) -> {
		};
		SteemCallback<CommentResult> benifCb = (error, result) -> {
			if (error != null) {
				GWT.log("ERROR: " + error);
				fireEvent(new Event.AlertMessage("ERROR: " + error.toString()));
			}
			mostRecent = new MostRecentPostInfo();
			mostRecent.author = author;
			mostRecent.firstTag = firstTag;
			mostRecent.permLink = permLink;
			fireEvent(new Event.PostDone());
			fireEvent(new Event.Loading(false));
		};
		SteemCallback<CommentResult> commentCb = (error, result) -> {
			if (error != null) {
				fireEvent(new Event.Loading(false));
				fireEvent(new Event.AlertMessage("ERROR: " + error.toString()));
			}
			if (result != null) {
				setBeneficiary(permLink, author, wif, benifCb);
				upvoteOwnPost(username, permLink, wif, voteCb);
			}
		};
		fireEvent(new Event.Loading(true));
		try {
			DomGlobal.console.log("SteemBroadcast.comment");
			SteemBroadcast.comment(wif, parentAuthor, firstTag, author, permLink, title, body, metadata, commentCb);
		} catch (Exception e) {
			fireEvent(new Event.Loading(false));
			GWT.log(e.getMessage(), e);
		}
	}

	private MostRecentPostInfo mostRecent;

	@EventHandler
	protected void getMostRecentPostInfo(Event.GetMostRecentPostInfo event) {
		if (mostRecent != null) {
			fireEvent(new Event.SetMostRecentPostInfo(mostRecent));
		}
	}

	private String generateTitle(List<E621Tag> tagList, long id) {
		// String autoTitle = "E621 Artwork";
		String autoTitle = "";
		String atArtists = getAtArtists(tagList);
		atArtists = atArtists.replace("@", "");
		if (getArtistCount(tagList) > 0) {
			atArtists = atArtists.replaceAll("_?\\(.*?\\)", "").replace("_", " ").replace("/", " ");
			String[] tmp = atArtists.split("\\s+");
			for (int ix = 0; ix < tmp.length; ix++) {
				String tmpName = tmp[ix];
				if (tmpName.length() > 1) {
					tmp[ix] = tmpName.substring(0, 1).toUpperCase() + tmpName.substring(1);
				}
			}
			atArtists = String.join(" ", tmp);
			tmp = atArtists.split("-+");
			for (int ix = 0; ix < tmp.length; ix++) {
				String tmpName = tmp[ix];
				if (tmpName.length() > 1) {
					tmp[ix] = tmpName.substring(0, 1).toUpperCase() + tmpName.substring(1);
				}
			}
			atArtists = String.join("-", tmp);
			if (getArtistCount(tagList) > 1) {
				autoTitle += "Artists: " + atArtists;
			} else {
				autoTitle += "Artist: " + atArtists;
			}
		}
		String characters = getCharacters(tagList);
		if (!characters.trim().isEmpty()) {
			characters = characters.replaceAll("_?\\(.*?\\)", "").replace("_", " ").replace("/", " ");
			String[] tmp = characters.split("\\s+");
			for (int ix = 0; ix < tmp.length; ix++) {
				String tmpName = tmp[ix];
				if (tmpName.length() > 1) {
					tmp[ix] = tmpName.substring(0, 1).toUpperCase() + tmpName.substring(1);
				}
			}
			characters = String.join(" ", tmp);
			autoTitle = autoTitle.trim() + ", ";
			autoTitle += "Featuring: " + characters;
		} else {
			String species = getSpecies(tagList);
			if (!species.trim().isEmpty()) {
				species = species.replaceAll("_?\\(.*?\\)", "").replace("_", " ").replace("/", " ");
				String[] tmp = species.split("\\s+");
				for (int ix = 0; ix < tmp.length; ix++) {
					String tmpName = tmp[ix];
					if (tmpName.length() > 1) {
						tmp[ix] = tmpName.substring(0, 1).toUpperCase() + tmpName.substring(1);
					}
				}
				species = String.join(" ", tmp);
				autoTitle = autoTitle.trim() + ", ";
				autoTitle += "Subject Material: " + species;
			}
		}
		// autoTitle = autoTitle.trim() + ", Post Id# " + id;
		return autoTitle;
	}

	@EventHandler
	protected void getPostPreview(Event.GetPostPreview event) {
		if (pendingPost == null) {
			return;
		}
		fireEvent(new Event.PostPreviewContent(generatePostHtml()));
	}

	private String generatePostHtml() {
		Document doc = Document.get();

		ImageElement img = doc.createImageElement();
		img.setSrc(pendingPost.post.getFileUrl());
		img.setAttribute("style", "max-width: 100%; margin: 0px;");

		AnchorElement imgA = doc.createAnchorElement();
		imgA.setTarget("_blank");
		imgA.setHref(pendingPost.post.getFileUrl());
		imgA.appendChild(img);

		DivElement imgDiv = doc.createDivElement();
		imgDiv.setAttribute("style", "max-width: 100%; margin: 4px; text-align: center;");
		imgDiv.appendChild(imgA);

		DivElement postDiv = doc.createDivElement();
		postDiv.appendChild(imgDiv);

		for (E621TagTypes tagType : E621TagTypes.values()) {
			StringBuilder sb = new StringBuilder();
			Iterator<E621Tag> iter = pendingPost.e621tags.iterator();
			Set<String> already = new HashSet<>();
			while (iter.hasNext()) {
				E621Tag e621Tag = iter.next();
				if (e621Tag.getType() != tagType.getId()) {
					continue;
				}
				String name = e621Tag.getName();
				name = name.replaceAll("_?\\(.*?\\)", "");
				name = name.replace("_", "-").replace("/", "-");
				if (already.contains(name)) {
					continue;
				}
				already.add(name);
				sb.append("#");
				sb.append(name);
				if (iter.hasNext()) {
					sb.append(" ");
				}
			}
			if (sb.length() != 0) {
				ParagraphElement p3 = doc.createPElement();
				p3.appendChild(doc.createTextNode(tagType.name()));
				String tags = sb.toString();
				if (tags.contains(" ")) {
					if (tagType == E621TagTypes.Artist || tagType == E621TagTypes.Character
							|| tagType == E621TagTypes.Copyright) {
						p3.appendChild(doc.createTextNode("s"));
					}
				}
				p3.appendChild(doc.createTextNode(": "));
				p3.appendChild(doc.createTextNode(tags));
				postDiv.appendChild(p3);
			}
		}
		
		AnchorElement muksihsLink = doc.createAnchorElement();
		muksihsLink.setHref("http://e621.muksihs.com/e621-resteemit/");
		muksihsLink.setTarget("_blank");
		muksihsLink.appendChild(doc.createTextNode("http://e621.muksihs.com/e621-resteemit/"));

		ParagraphElement p1 = doc.createPElement();
		p1.appendChild(doc.createTextNode("Curated using Muksihs' E621 Browser: "));
		p1.appendChild(muksihsLink);
		p1.appendChild(doc.createTextNode("."));
		
		long postId = pendingPost.post.getId();
		AnchorElement e621Link = doc.createAnchorElement();
		e621Link.setHref(Consts.E621_SHOW_POST + postId);
		e621Link.setTarget("_blank");
		e621Link.appendChild(doc.createTextNode("#" + postId));
		ParagraphElement p2 = doc.createPElement();
		p2.appendChild(doc.createTextNode("E621: "));
		p2.appendChild(e621Link);
		p2.appendChild(doc.createTextNode("."));
		postDiv.appendChild(p2);

		Element curatedBy = doc.createElement("center");
		curatedBy.getStyle().setTextAlign(TextAlign.CENTER);
		curatedBy.appendChild(p1);
		postDiv.appendChild(curatedBy);

		DivElement tmpDiv = doc.createDivElement();
		tmpDiv.appendChild(postDiv);
		String html = "<html>" + tmpDiv.getInnerHTML() + "</html>";
		return html;
	}

	private String getAtArtists(List<E621Tag> taglist) {
		Iterator<E621Tag> ialt = taglist.iterator();
		StringBuilder atAuthor = new StringBuilder();
		while (ialt.hasNext()) {
			E621Tag tag = ialt.next();
			if (tag.getType() != E621TagTypes.Artist.getId()) {
				continue;
			}
			if (tag.getCount() == null || tag.getCount() == 0) {
				continue;
			}
			atAuthor.append("@");
			atAuthor.append(tag.getName().replaceAll("_\\(.*?\\)", ""));
			if (ialt.hasNext()) {
				atAuthor.append(" ");
			}
		}
		return atAuthor.toString();
	}

	private int getArtistCount(List<E621Tag> taglist) {
		int count = 0;
		Iterator<E621Tag> ialt = taglist.iterator();
		while (ialt.hasNext()) {
			E621Tag tag = ialt.next();
			if (tag.getType() != E621TagTypes.Artist.getId()) {
				continue;
			}
			count++;
		}
		return count;
	}

	@SuppressWarnings("unused")
	private Set<String> getArtists(List<E621Tag> taglist) {
		Iterator<E621Tag> iter = taglist.iterator();
		Set<String> artists = new TreeSet<>();
		while (iter.hasNext()) {
			E621Tag tag = iter.next();
			if (tag.getType() != E621TagTypes.Artist.getId()) {
				continue;
			}
			if (tag.getCount() == null || tag.getCount() == 0) {
				continue;
			}
			artists.add(tag.getName());
		}
		return artists;
	}

	private String getSpecies(List<E621Tag> taglist) {
		Iterator<E621Tag> ialt = taglist.iterator();
		StringBuilder species = new StringBuilder();
		while (ialt.hasNext()) {
			E621Tag tag = ialt.next();
			if (tag.getType() != E621TagTypes.Species.getId()) {
				continue;
			}
			if (tag.getCount() == null || tag.getCount() == 0) {
				continue;
			}
			if (species.length() != 0) {
				species.append(", ");
			}
			species.append(tag.getName().replaceAll("_\\(.*?\\)", ""));
		}
		return species.toString();
	}

	private String getCharacters(List<E621Tag> taglist) {
		Iterator<E621Tag> ialt = taglist.iterator();
		StringBuilder characters = new StringBuilder();
		while (ialt.hasNext()) {
			E621Tag tag = ialt.next();
			if (tag.getType() != E621TagTypes.Character.getId()) {
				continue;
			}
			if (tag.getCount() == null || tag.getCount() == 0) {
				continue;
			}
			if (characters.length() != 0) {
				characters.append(", ");
			}
			characters.append(tag.getName().replaceAll("_\\(.*?\\)", ""));
		}
		return characters.toString();
	}

	private void pickBestTagsThenPostConfirm(PostPreview preview, List<E621Tag> response) {
		// sort descending by count,
		Collections.sort(response, (a, b) -> Long.compare(b.getCount(), a.getCount()));
		MatchingTagsState state = new MatchingTagsState();
		state.post = preview;
		state.asSteemFormatted = new ArrayList<>();
		state.matchingSteemTags = new ArrayList<>();
		state.asSteemFormatted = new ArrayList<>(response);
		state.e621tags = new ArrayList<>(response);
		Set<String> already = new HashSet<>();
		ListIterator<E621Tag> liter = state.asSteemFormatted.listIterator();
		while (liter.hasNext()) {
			E621Tag tag = liter.next();
			String altName = tag.getName().toLowerCase();
			altName = altName.replaceAll("_?\\(.*?\\)", "");
			altName = altName.replaceAll("[^a-z0-9]", "-");
			// altName = altName.replace("/", "-");
			altName = altName.replaceAll("-+", "-");
			//skip tags that don't start with letters
			if (!altName.matches("^[a-z].*")) {
				liter.remove();
				continue;
			}
			if (altName.replace("-", "").length() < altName.length() - 1) {
				altName = altName.replace("-", "");
			}
			if (already.contains(altName)) {
				liter.remove();
				continue;
			}
			already.add(altName);
			tag.setName(altName);
		}
		state.iter = state.asSteemFormatted.iterator();
		getMatchingTrendingTags(state);
	}

	private void fatalError(Method method, Throwable exception) {
		fireEvent(new Event.FatalError(String.valueOf(exception.getMessage())));
		DomGlobal.console.log(exception);
		DomGlobal.console.log(method);
		DomGlobal.console.log(method.getResponse());
		DomGlobal.console.log(method.getResponse().getText());
	}

	private String generatePermaLink(final String title) {
		String tmp = title == null ? "" : title;
		long currentTimeMillis = System.currentTimeMillis();
		tmp += "-" + new java.sql.Date(currentTimeMillis).toString();
		tmp += "-" + currentTimeMillis;
		tmp = tmp.toLowerCase().replaceAll("[^a-z0-9]", "-");
		tmp = tmp.toLowerCase().replaceAll("-+", "-");
		while (tmp.endsWith("-")) {
			tmp = tmp.substring(0, tmp.length() - 1);
		}
		while (tmp.startsWith("-")) {
			tmp = tmp.substring(1);
		}
		return tmp;
	}

	@EventHandler
	protected void loginLogoutToggle(Event.LoginLogout event) {
		if (isLoggedIn()) {
			AccountCache accountCache = new AccountCache();
			SteemPostingInfo steemPostingInfo = accountCache.get(DEFAULT_USER);
			steemPostingInfo.setWif(null);
			accountCache.put(DEFAULT_USER, steemPostingInfo);
			loggedIn = false;
			fireEvent(new Event.LoginComplete(false));
		} else {
			fireEvent(new Event.Login<>(null));
		}
	}

	@EventHandler
	protected void mostRecent(Event.MostRecentSet event) {
		activePage = 0;
		savedPageStartId = 0;
		reloadingOnFilterChange = true;
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void tryLogin(Event.TryLogin event) {
		String wif = event.getWif();
		wif = wif == null ? "" : wif.trim();
		if (!wif.equals(wif.replaceAll("[^123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]", ""))) {
			if (!event.isSilent()) {
				fireEvent(new Event.AlertMessage("That is not a valid key."));
			}
			fireEvent(new Event.Loading(false));
			return;
		}
		if (wif.startsWith("STM")) {
			if (!event.isSilent()) {
				fireEvent(new Event.AlertMessage(
						"That is not the PRIVATE posting key. Please visit your wallet => permissions => select \"show private key\" next to your posting key, to obtain your private posting key. It will not start with 'STM'."));
			}
			fireEvent(new Event.Loading(false));
			return;
		}
		SteemCallbackArray<AccountInfo> cb = (error, result) -> {
			fireEvent(new Event.Loading(false));
			if (error != null) {
				if (!event.isSilent()) {
					fireEvent(new Event.AlertMessage(error.toString()));
				}
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			if (result.length == 0) {
				if (!event.isSilent()) {
					fireEvent(new Event.AlertMessage("Username not found!"));
				}
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			AccountInfo accountInfo = result[0];
			if (accountInfo == null) {
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			Posting posting = accountInfo.getPosting();
			if (posting == null) {
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			String[][] keyAuths = posting.getKeyAuths();
			if (keyAuths == null || keyAuths.length == 0) {
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			String[] keylist = keyAuths[0];
			if (keylist == null || keylist.length == 0) {
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			String publicWif = keylist[0];
			try {
				if (!SteemAuth.wifIsValid(event.getWif(), publicWif)) {
					new AccountCache().remove(DEFAULT_USER);
					if (!event.isSilent()) {
						fireEvent(new Event.AlertMessage("THAT IS NOT YOUR PRIVATE POSTING KEY"));
					}
					fireEvent(new Event.LoginComplete(false));
					return;
				}
			} catch (JavaScriptException e) {
				DomGlobal.console.log(e.getMessage());
				DomGlobal.console.log(e);
				if (!event.isSilent()) {
					fireEvent(new Event.AlertMessage(e.getMessage()));
				}
				fireEvent(new Event.LoginComplete(false));
				return;
			}
			AccountCache cache = new AccountCache();
			SteemPostingInfo info = new SteemPostingInfo();
			info.setUsername(accountInfo.getName());
			info.setWif(event.getWif());
			cache.put(DEFAULT_USER, info);
			fireEvent(new Event.LoginComplete(true));
			DomGlobal.console.log("Logged in as: " + accountInfo.getName());
		};
		fireEvent(new Event.Loading(true));
		String username = event.getUsername();
		while (username.trim().startsWith("@")) {
			username = username.trim().substring(1);
		}
		SteemApi.getAccounts(new String[] { username }, cb);
	}

	@EventHandler
	protected void steemPost(Event.SteemPost event) {
		MethodCallback<List<E621Tag>> cb = new MethodCallback<List<E621Tag>>() {
			@Override
			public void onSuccess(Method method, List<E621Tag> response) {
				pickBestTagsThenPostConfirm(event.getPreview(), response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				fireEvent(new Event.FatalError(exception.getMessage()));
			}
		};
		if (!isLoggedIn()) {
			fireEvent(new Event.Login<>(event));
		} else {
			fireEvent(new Event.Loading(true));
			E621Api.api().postTags(event.getPreview().getId(), cb);
		}

	}

	private boolean loggedIn;

	private boolean isLoggedIn() {
		return loggedIn;
	}

	@EventHandler
	protected void loginComplete(Event.LoginComplete event) {
		loggedIn = event.isLoggedIn();
		if (afterLoginPendingEvent != null && event.isLoggedIn()) {
			fireEvent(afterLoginPendingEvent);
		}
		afterLoginPendingEvent = null;
	}

	private GenericEvent afterLoginPendingEvent;

	@EventHandler
	protected <T extends GenericEvent> void login(Event.Login<T> event) {
		afterLoginPendingEvent = event.getRefireEvent();
		fireEvent(new Event.ShowLoginUi());
	}

	@EventHandler
	protected void browseViewLoaded(Event.BrowseViewLoaded event) {
		if (History.getToken().trim().isEmpty()) {
			Set<String> boxes = new HashSet<>();
			boxes.add(E621Rating.SAFE.getTag());
			fireEvent(new Event.SetRatingsBoxes(boxes));
			Set<E621Rating> safe = new HashSet<>();
			safe.add(E621Rating.SAFE);
			fireEvent(new Event.SetRating(safe));
		} else {
			History.fireCurrentHistoryState();
		}
	}

	private void updateAvailableTagsDisplay() {
		Set<String> availableTags = new TreeSet<>();
		for (Tag tag : topAvailableTags) {
			availableTags.add(tag.getName());
		}
		// remove active tags from list of activatable tags
		availableTags.removeAll(mustHaveTags);
		availableTags.removeAll(mustNotHaveTags);
		// add back active tags as in use tags
		for (String tag : mustHaveTags) {
			availableTags.add("+" + tag);
		}
		for (String tag : mustNotHaveTags) {
			availableTags.add("-" + tag);
		}

		fireEvent(new Event.ShowAvailableTags(availableTags));
	}

	@EventHandler
	protected void removeFromFilter(Event.RemoveFromFilter event) {
		if (activePage==0) {
			savedPageStartId = 0;
		}
		reloadingOnFilterChange = true;
		mustHaveTags.remove(event.getTag().substring(1));
		mustNotHaveTags.remove(event.getTag().substring(1));
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void setRating(Event.SetRating event) {
		if (activePage==0) {
			savedPageStartId = 0;
		}
		reloadingOnFilterChange = true;
		mustHaveRatings.clear();
		if (event.getRating().size() != E621Rating.values().length) {
			for (E621Rating e621Rating : event.getRating()) {
				mustHaveRatings.add(e621Rating.getTag());
			}
		}
		fireEvent(new Event.LoadInitialPreviews());
	}

	private void updateHash() {
		SavedState hash = getSavedStateHash();
		History.newItem(SavedState.asHistoryToken(hash), false);
	}

	private SavedState getSavedStateHash() {
		SavedState hash = new SavedState();
		hash.setMustHave(mustHaveTags);
		hash.setMustNotHave(mustNotHaveTags);
		// don't lock view at a specific post when on first page
		if (activePage > 0) {
			hash.setPostId(savedPageStartId);
		}
		// } else {
		// }
		hash.setRatings(mustHaveRatings);
		return hash;
	}

	@EventHandler
	protected void clearSearch(Event.ClearSearch event) {
		reloadingOnFilterChange = true;
		mustHaveRatings.clear();
		mustHaveTags.clear();
		mustNotHaveTags.clear();
		mustHaveRatings.add(E621Rating.SAFE.getTag());
		activePage = 0;
		savedPageStartId = 0;
		fireEvent(new Event.SetRatingsBoxes(mustHaveRatings));
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void addToIncludeFilter(Event.AddToIncludeFilter event) {
		if (activePage==0) {
			savedPageStartId = 0;
		}
		reloadingOnFilterChange = true;
		mustHaveTags.add(event.getTag());
		fireEvent(new Event.LoadInitialPreviews());
	}

	@EventHandler
	protected void addToExcludeFilter(Event.AddToExcludeFilter event) {
		if (activePage==0) {
			savedPageStartId = 0;
		}
		reloadingOnFilterChange = true;
		mustNotHaveTags.add(event.getTag());
		fireEvent(new Event.LoadInitialPreviews());
	}

	private void updateActiveTagFilters() {
		Set<String> tags = new TreeSet<>();
		for (String tag : mustHaveTags) {
			tags.add("+" + tag);
		}
		for (String tag : mustNotHaveTags) {
			tags.add("-" + tag);
		}
		fireEvent(new Event.ShowFilterTags(tags));
	}

	@EventHandler
	protected void showPreviews(Event.PreviewsLoaded event) {
		Scheduler.get().scheduleDeferred(this::updateAvailableTags);
		if (activePage == 0) {
			fireEvent(new Event.EnablePreviousButton(false));
		} else {
			fireEvent(new Event.EnablePreviousButton(true));
		}
		fireEvent(new Event.Loading(true));
		List<PostPreview> previewsToShow = activeSetForPage(activePage);
		reloadingOnFilterChange = false;
		savedPageStartId = previewsToShow.stream().mapToLong((p) -> p.getId()).max().orElse(0);
		fireEvent(new Event.ShowPreviews(previewsToShow));
		fireEvent(new Event.Loading(false));
		updateHash();
		if (previewsToShow.isEmpty()) {
			fireEvent(new Event.AlertMessage("No Posts Match Your Filter Settings!"));
		}
	}

	private void updateAvailableTags() {
		Set<String> availableTags = tagsForActiveSet();
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = availableTags.iterator();
		while (iTags.hasNext() && sb.length() < 1024) {
			String next = iTags.next();
			Tag tmp = new Tag();
			tmp.setName(next);
			if (topAvailableTags.contains(tmp)) {
				continue;
			}
			sb.append(next);
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		if (sb.length() > 0) {
			E621Api.api().tagRelated(sb.toString(), updateTopTagsCallback);
		} else {
			updateAvailableTagsDisplay();
		}
	}

	private MethodCallback<Map<String, List<List<String>>>> updateTopTagsCallback = new MethodCallback<Map<String, List<List<String>>>>() {
		@Override
		public void onFailure(Method method, Throwable exception) {
			DomGlobal.console.log("EXCEPTION: " + exception.getMessage());
			DomGlobal.console.log(exception);
			DomGlobal.console.log(method);
			updateAvailableTagsDisplay();
		}

		@Override
		public void onSuccess(Method method, Map<String, List<List<String>>> response) {
			for (String tagName : response.keySet()) {
				Tag tag = new Tag();
				tag.setName(tagName);
				List<List<String>> list = response.get(tagName);
				if (list != null) {
					// use related tag count for query weighting purposes
					tag.setCount(list.size());
				}
				topAvailableTags.add(tag);
			}
			updateAvailableTagsDisplay();
		}
	};

	private final Set<Tag> topAvailableTags = new TreeSet<>();

	private List<PostPreview> activeSetForPage(int activePage) {
		int start = activePage * Consts.PREVIEWS_TO_SHOW;
		if (activeSet.size() < Consts.PREVIEWS_TO_SHOW) {
			return activeSet;
		}
		List<PostPreview> previewsToShow = activeSet.subList(Math.min(start, activeSet.size() - 1),
				Math.min(start + Consts.PREVIEWS_TO_SHOW, activeSet.size()));
		return previewsToShow;
	}

	private Set<String> tagsForActiveSet() {
		Set<String> tags = new TreeSet<>();
		for (PostPreview post : activeSet) {
			tags.addAll(Arrays.asList(post.getTags().split("\\s+")));
		}
		return tags;
	}

	private boolean reloadingOnFilterChange = false;
	private long savedPageStartId = 0;
	private long prevBeforeId = 0;

	private MethodCallback<E621PostsResponse> onPostsLoaded = new MethodCallback<E621PostsResponse>() {

		@Override
		public void onSuccess(Method method, E621PostsResponse response) {
			// do NOT alter response object, it screws up the cache if done!
			final int responseSize = response.getPosts().size();
			long pageStartId = 0;
			long nextBeforeId = Long.MAX_VALUE;
			for (Post post : response.getPosts()) {
				nextBeforeId = Long.min(nextBeforeId, post.getId());
				pageStartId = Long.max(pageStartId, post.getId());
			}

			List<PostPreview> previews = new ArrayList<>();
			Iterator<Post> iter = response.getPosts().iterator();
			while (iter.hasNext()) {
				Post next = iter.next();
				PostPreview preview = new PostPreview(next.getId(), next.getSample().getUrl(), next.getFile().getUrl(),
						0l /* next.getCreatedAt() */, next.getTags().toString(), next.getRating(),
						next.getFile().getExt());
				previews.add(preview);
			}
			// remove previews already in the activeset
			previews.removeAll(activeSet);
			activeSet.addAll(previews);

			// filter the active set
			if (!mustHaveRatings.isEmpty()) {
				activeSet.removeIf((p) -> !mustHaveRatings.contains(p.getRating()));
			}

			activeSet.removeIf((p) -> !extensionsWhitelist.contains(p.getFileExt().toLowerCase()));
			if (!mustNotHaveTags.isEmpty()) {
				activeSet.removeIf((p) -> {
					Set<String> tags = new HashSet<>(Arrays.asList(p.getTags().split("\\s+")));
					return !Collections.disjoint(tags, mustNotHaveTags);
				});
			}

			if (!mustHaveTags.isEmpty()) {
				activeSet.removeIf((p) -> {
					Set<String> tags = new HashSet<>(Arrays.asList(p.getTags().split("\\s+")));
					return !tags.containsAll(mustHaveTags);
				});
			}

			boolean moreAvailable = nextBeforeId > 0 && responseSize > 0;
			int activePageEnd = (1 + activePage) * Consts.PREVIEWS_TO_SHOW;
			if (activeSet.size() < activePageEnd && moreAvailable) {
				final long beforeId;
				if (nextBeforeId == pageStartId) {
					beforeId = nextBeforeId - CACHED_PAGE_SIZE;
				} else {
					if (prevBeforeId == nextBeforeId) {
						beforeId = nextBeforeId - CACHED_PAGE_SIZE;
					} else {
						beforeId = nextBeforeId;
					}
				}
				prevBeforeId = beforeId;
				Scheduler.get().scheduleDeferred(() -> additionalPreviewsLoad(beforeId));
				return;
			}
			List<PostPreview> previewsToShow = activeSetForPage(activePage);
			long beforeId = previewsToShow.stream().mapToLong((p) -> p.getId()).min().orElse(0);
			boolean skipForwards = reloadingOnFilterChange //
					&& beforeId > savedPageStartId //
					&& savedPageStartId > 0;
			if (skipForwards && moreAvailable) {
				fireEvent(new Event.NextPreviewSet());
				return;
			}
			fireEvent(new Event.PreviewsLoaded());
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			// try reloading everything from scratch
			fatalError(method, exception);
		}
	};
	private boolean initialPageLoad;

	private void e621logout() {
		if (true) {
			return;
		}
		MethodCallback<Void> noop = new MethodCallback<Void>() {
			@Override
			public void onSuccess(Method method, Void response) {
				GWT.log("E621 logout done");
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				GWT.log("E621 logout done");
			}
		};
		E621Api.api().logout(noop);
	}
	
	@EventHandler
	protected void fatalError(Event.FatalError event) {
		e621logout();
		new Timer() {
			@Override
			public void run() {
				Location.reload();
			}
		}.schedule(45000);
	}

	@Override
	public void execute() {
		rp = RootPanel.get("e621resteemit");
		rp.clear();
		MainView mainView = new MainView();
		rp.add(mainView);
		eventBinder.bindEventHandlers(this, eventBus);
		setController(new ViewController(mainView.getPanel()));
		fireEvent(new Event.Loading(true));
		// hash parsing
		History.addValueChangeHandler(this);
		Scheduler.get().scheduleDeferred(this::startApp);
	}

	private void startApp() {
		// validate cached login credentials (if any)
		AccountCache cache = new AccountCache();
		SteemPostingInfo info = cache.get(DEFAULT_USER);
		if (info != null) {
			fireEvent(new Event.TryLogin(info.getUsername(), info.getWif(), true));
		} else {
			fireEvent(new Event.LoginComplete(false));
		}
		// load most common available tags, then show the view
		int limit = 500;
		TagsCache tagsCache = new TagsCache(limit);
		List<Tag> tags = tagsCache.get(limit + "");
		if (tags != null && !tags.isEmpty()) {
			topAvailableTags.addAll(tags);
			fireEvent(new Event.ShowView(View.BrowseView));
		} else {
			E621Api.api().tagList(1, limit, new MethodCallback<List<Tag>>() {
				@Override
				public void onSuccess(Method method, List<Tag> response) {
					topAvailableTags.clear();
					for (Tag tag : response) {
						if (tag == null) {
							continue;
						}
						topAvailableTags.add(tag);
					}
					tagsCache.put(limit + "", response);
					fireEvent(new Event.ShowView(View.BrowseView));
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					// try reloading everything from scratch
					fatalError(method, exception);
				}
			});
		}
	}

	public ViewController getController() {
		return controller;
	}

	public void setController(ViewController controller) {
		this.controller = controller;
	}

	@EventHandler
	protected void getAppVersion(Event.GetAppVersion event) {
		fireEvent(new Event.AppVersion(Consts.APP_VERSION));
	}

	@EventHandler
	protected void showPreviousSet(Event.PreviousPreviewSet event) {
		if (activePage > 0) {
			fireEvent(new Event.Loading(true));
			activePage--;
			fireEvent(new Event.PreviewsLoaded());
		}
	}

	@EventHandler
	protected void showNextSet(Event.NextPreviewSet event) {
		fireEvent(new Event.Loading(true));
		List<PostPreview> previewsToShow = activeSetForPage(activePage);
		long tmp = Long.MAX_VALUE;
		for (PostPreview preview : previewsToShow) {
			tmp = Long.min(tmp, preview.getId());
		}
		long beforeId = tmp;
		Scheduler.get().scheduleDeferred(() -> {
			activePage++;
			additionalPreviewsLoad(beforeId);
		});
	}

	private static class AlignedQuery {
		public long beforeId;
		public String query;
		public String cachedQueryKey;
	}

	protected void additionalPreviewsLoad(long beforeId) {
		AlignedQuery q = align(beforeId);
		List<Post> cached = INDEX_CACHE.get(q.cachedQueryKey);
		if (cached == null) {
			fireEvent(new Event.QuickMessage("Searching E621... " + q.beforeId));
			E621Api.api().postIndex(q.query, (int) beforeId, CACHED_PAGE_SIZE, cacheIndexResponse(q.cachedQueryKey));
		} else {
			fireEvent(new Event.QuickMessage("Searching cache... " + q.beforeId));
			Scheduler.get().scheduleDeferred(() -> onPostsLoaded.onSuccess(null, cached));
		}
	}

	private AlignedQuery align(long beforeId) {
		AlignedQuery q = new AlignedQuery();
		// keep beforeId aligned with multiples of CACHED_PAGE_SIZE so the cache works
		// correctly
		String query = buildQuery();
		q.beforeId = getAlignedBeforeId(beforeId);
		q.cachedQueryKey = query + "," + q.beforeId;
		q.query = query;
		return q;
	}

	private long getAlignedBeforeId(final long _beforeId) {
		long beforeId = _beforeId;
		beforeId = (long) (Math.ceil((double) beforeId / (double) CACHED_PAGE_SIZE) * CACHED_PAGE_SIZE);
		return beforeId;
	}

	private String buildQuery() {
		List<String> tags = new ArrayList<>();
		// if (mustHaveRatings.size() == 1) {
		// // querying E621 with multiple ratings doesn't seem to give correct results
		// // so rely strictly on client side filtering if more than one rating provided
		// Iterator<String> iRatings = mustHaveRatings.iterator();
		// while (iRatings.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
		// tags.add("rating:" + iRatings.next());
		// }
		// }
		List<Tag> tmpMustHave = new ArrayList<>();
		List<Tag> tmpMustNotHave = new ArrayList<>();
		Iterator<String> iMust = mustHaveTags.iterator();
		must: while (iMust.hasNext()) {
			String next = iMust.next();
			for (Tag tag : topAvailableTags) {
				if (tag.getName().equals(next)) {
					tmpMustHave.add(tag);
					continue must;
				}
			}
		}
		Iterator<String> iMustNot = mustNotHaveTags.iterator();
		mustNot: while (iMustNot.hasNext()) {
			String next = iMustNot.next();
			for (Tag tag : topAvailableTags) {
				if (tag.getName().equals(next)) {
					tmpMustNotHave.add(tag);
					continue mustNot;
				}
			}
		}
		List<Tag> queryTags = new ArrayList<>();
		queryTags.addAll(tmpMustHave);
		queryTags.addAll(tmpMustNotHave);
		/*
		 * try and sort tags to produce better queries: must have should be those with
		 * least count of posts and must not have should be those with the most count of
		 * posts we seem to perform slightly less queries preferring must have tag over
		 * must not have tags
		 */
		Collections.sort(queryTags, (a, b) -> {
			// sort musthave before mustnothave
			if (tmpMustHave.contains(a) && tmpMustNotHave.contains(b)) {
				return -1;
			}
			if (tmpMustNotHave.contains(a) && tmpMustHave.contains(b)) {
				return 1;
			}
			// sort musthave vs musthave asc
			if (tmpMustHave.contains(a) && tmpMustHave.contains(b)) {
				return Long.compare(a.getCount(), b.getCount());
			}
			// musthavenot vs musthavenot desc
			if (tmpMustNotHave.contains(a) && tmpMustNotHave.contains(b)) {
				return Long.compare(b.getCount(), a.getCount());
			}
			// sort the rest asc
			return Long.compare(a.getCount(), b.getCount());
		});

		Iterator<Tag> iQuery = queryTags.iterator();
		while (iQuery.hasNext() && tags.size() < MAX_TAGS_PER_QUERY) {
			Tag next = iQuery.next();
			if (tmpMustHave.contains(next)) {
				tags.add(next.getName());
			}
			if (tmpMustNotHave.contains(next)) {
				tags.add("-" + next.getName());
			}
		}
		StringBuilder sb = new StringBuilder();
		Iterator<String> iTags = tags.iterator();
		while (iTags.hasNext()) {
			sb.append(iTags.next());
			if (iTags.hasNext()) {
				sb.append(" ");
			}
		}
		String query = sb.toString();
		return query;
	}

	private MethodCallback<E621PostsResponse> cacheIndexResponse(String cachedPostsKey) {
		return new MethodCallback<E621PostsResponse>() {

			@Override
			public void onSuccess(Method method, E621PostsResponse response) {
				// do NOT alter response object, it screws up the cache if done!
				Scheduler.get().scheduleDeferred(() -> INDEX_CACHE.put(cachedPostsKey, response));
				onPostsLoaded.onSuccess(method, response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// try reloading everything from scratch
				fatalError(method, exception);
			}
		};
	}

	@EventHandler
	protected void initialPreviewsLoad(Event.LoadInitialPreviews event) {
		fireEvent(new Event.Loading(true));
		updateActiveTagFilters();
		/*
		 * if on first view, make sure we stay on first view and don't try and navigate
		 * deep into a reduced filter set because of a large jump up in available posts
		 * with higher numbers. If this is an initialPageLoad from a restored state,
		 * activePage is always 0, so don't apply this rule for that particular case as
		 * an exception.
		 */
		if (activePage == 0 && !initialPageLoad) {
			savedPageStartId = 0;
			initialPageLoad = false;
		}
		activePage = 0;
		activeSet.clear();
		String query = buildQuery();
		E621Api.api().postIndex(query, CACHED_PAGE_SIZE, onPostsLoaded);
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		GWT.log("Hash Change: " + token);
		SavedState state = SavedState.parseHistoryToken(token);
		mustHaveTags.clear();
		mustNotHaveTags.clear();
		mustHaveRatings.clear();
		savedPageStartId = state.getPostId();
		if (state.getMustHave() != null) {
			mustHaveTags.addAll(state.getMustHave());
		}
		if (state.getMustNotHave() != null) {
			mustNotHaveTags.addAll(state.getMustNotHave());
		}
		if (state.getRatings() != null) {
			mustHaveRatings.addAll(state.getRatings());
		}
		fireEvent(new Event.SetRatingsBoxes(state.getRatings()));
		initialPageLoad = true;
		validateTagsThenLoadPreviews();
	}

	private void validateTagsThenLoadPreviews() {
		String tags = String.join(" ", mustHaveTags) + " " + String.join(" ", mustNotHaveTags);
		tags = tags.trim();
		MethodCallback<Map<String, List<List<String>>>> validated = new MethodCallback<Map<String, List<List<String>>>>() {
			@Override
			public void onSuccess(Method method, Map<String, List<List<String>>> response) {
				if (response.size() == 0) {
					mustHaveTags.clear();
					mustNotHaveTags.clear();
					reloadingOnFilterChange = true;
					fireEvent(new Event.LoadInitialPreviews());
					return;
				}
				/*
				 * add all to the in memory available tags then remove any invalid ones
				 */
				for (String tagName : response.keySet()) {
					Tag tag = new Tag();
					tag.setName(tagName);
					tag.setCount(response.get(tagName).size());
					if (!topAvailableTags.contains(tag)) {
						topAvailableTags.add(tag);
					}
				}
				/*
				 * build up quick lookup set for valid tag names
				 */
				Set<String> validTagNames = new HashSet<>();
				for (Tag tag : topAvailableTags) {
					validTagNames.add(tag.getName());
				}
				/*
				 * remove tags not in the valid tag names set
				 */
				Iterator<String> iter = mustHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!validTagNames.contains(next)) {
						DomGlobal.console.log("Removing invalid tag: " + next);
						iter.remove();
					}
				}
				/*
				 * remove tags not in the valid tag names set
				 */
				iter = mustNotHaveTags.iterator();
				while (iter.hasNext()) {
					String next = iter.next();
					if (!validTagNames.contains(next)) {
						DomGlobal.console.log("Removing invalid tag: " + next);
						iter.remove();
					}
				}
				reloadingOnFilterChange = true;
				fireEvent(new Event.LoadInitialPreviews());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				fireEvent(new Event.LoadInitialPreviews());
				//getting access denied?
				// try reloading everything from scratch
//				fatalError(method, exception);
			}
		};
		if (tags.length() > 0) {
			E621Api.api().tagRelated(tags, validated);
		} else {
			reloadingOnFilterChange = true;
			fireEvent(new Event.LoadInitialPreviews());
		}
	}

	private void upvoteOwnPost(String username, final String permLink, final String wif,
			SteemCallback<VoteResult> voteCb) {
		Scheduler.get().scheduleDeferred(() -> {
			// only if user wants to upvote their own post...
			SteemPostingInfo user = new AccountCache().get(DEFAULT_USER);
			if (user != null && user.isUpvote()) {
				SteemBroadcast.vote(wif, username, username, permLink, 10000, voteCb);
			}
		});
	}

	private void setBeneficiary(final String permLink, final String author, final String wif,
			SteemCallback<CommentResult> benifCb) {
		CommentOptionsExtensions extensions = new CommentOptionsExtensions();
		extensions.beneficiaries.beneficiaries.add(BENEFICIARY);
		Scheduler.get()
				.scheduleDeferred(() -> SteemBroadcast.commentOptions(wif, author, permLink, extensions, benifCb));
	}

}
