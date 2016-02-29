package com.codepath.apps.twitfeeder.net;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterRestClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "DPxQCvZTs9pdRHfWflhz8gG7g";       // Change this
	public static final String REST_CONSUMER_SECRET = "2DNzk25nT5dkLUA5Viyuw5qLX9MiMy2p7JO1uWvveZWNrPUlaI"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cptwitfeeder"; // Change this (here and in manifest)

    public static final String REST_UPDATE_STATUS_URL = "statuses/update.json";
    public static final String REST_DETAIL_TWEET_URL = "statuses/show.json";
    public static final String REST_FIND_TWEET_URL = "search/tweets.json";
    public static final String REST_ADD_FAVORITES_URL = "favorites/create.json";
    public static final String REST_REMOVE_FAVORITES_URL = "favorites/destroy.json";
    public static final String REST_VERIFY_CREDENTIALS_URL = "account/verify_credentials.json";

	public TwitterRestClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    public void getHomeTimeline(long since_id, long max_id, long userId, String query, AsyncHttpResponseHandler handler){

        String url="";

        url = userId > 0 ? "statuses/user_timeline.json":"statuses/home_timeline.json";

        if (query != null && query.length() > 0) {
            url = "search/tweets.json";
        }

        String apiUrl = getApiUrl(url);

        Log.i("info","calling api: "+apiUrl);

            //add params
        RequestParams params = new RequestParams();
        params.put("count", 20);

        if (query != null) {
            params.put("q", query);
        }

        if (userId > 0) {
            params.put("user_id", userId);
        }

        if(since_id > 0) {
            params.put("since_id", since_id);
        }

        if(max_id > 0) {
            params.put("max_id", max_id);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(long since_id, long max_id, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");

        //add params
        RequestParams params = new RequestParams();
        params.put("count", 20);
        if(since_id > 0) {
            params.put("since_id", since_id);
        }
        if(max_id > 0) {
            params.put("max_id", max_id);
        }
        getClient().get(apiUrl, params, handler);
    }

    public void getFavoritesList(long since_id, long max_id, long userId, AsyncHttpResponseHandler handler){

        String apiUrl = getApiUrl("favorites/list.json");

        Log.i("info","calling favorites list: "+apiUrl);

        //add params
        RequestParams params = new RequestParams();
        params.put("count", 20);

        if (userId > 0) {
            params.put("user_id", userId);
        }

        if(since_id > 0) {
            params.put("since_id", since_id);
        }

        if(max_id > 0) {
            params.put("max_id", max_id);
        }

        getClient().get(apiUrl, params, handler);
    }

    public void addFavoriteTweet(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(REST_ADD_FAVORITES_URL);
        RequestParams params = new RequestParams();
        if (tweetId > 0) {
            params.put("id", tweetId);
        }
        client.post(apiUrl, params, handler);
    }

    public void removeFavoriteTweet(long tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(REST_REMOVE_FAVORITES_URL);
        RequestParams params = new RequestParams();
        if (tweetId > 0) {
            params.put("id", tweetId);
        }
        client.post(apiUrl, params, handler);
    }

    public void followUser(long userId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friendships/create.json");
        RequestParams params = new RequestParams();
        if (userId > 0) {
            params.put("user_id", userId);
        }
        client.post(apiUrl, params, handler);
    }

    public void unFollowUser(long userId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friendships/destroy.json");
        RequestParams params = new RequestParams();
        if (userId > 0) {
            params.put("user_id", userId);
        }
        client.post(apiUrl, params, handler);
    }

    public void updateStatus(long tweetId, String status, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(REST_UPDATE_STATUS_URL);
        RequestParams params = new RequestParams();
        if (tweetId > 0) {
            Log.i("info","replying to a post");
            params.put("in_reply_to_status_id", tweetId);
        }
        params.put("status", status);
        client.post(apiUrl, params, handler);
    }

    // find if the user is followed by another user
    public void getFriendshipStatus(long source_Id, long target_Id, AsyncHttpResponseHandler handler){

        String apiUrl = getApiUrl("friendships/show.json");
        RequestParams params = new RequestParams();

        if (source_Id > 0) {
            params.put("source_id", source_Id);
        }

        if (target_Id > 0) {
            params.put("target_id", target_Id);
        }

        client.get(apiUrl, params, handler);
    }

    public void getFollowers(long userId, long cursor, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("followers/list.json");

        RequestParams params = new RequestParams();

        if (userId > 0){
            params.put("user_id", userId);
        }

        params.put("cursor", cursor);

        client.get(apiUrl, params, handler);

    }

    public void getFriends(long userId, long cursor, AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("friends/list.json");

        RequestParams params = new RequestParams();

        if (userId > 0){
            params.put("user_id", userId);
        }

        params.put("cursor", cursor);

        client.get(apiUrl, params, handler);

    }

    public void reTweet(long tweetId, AsyncHttpResponseHandler handler) {

        String apiUrl = getApiUrl(String.format("statuses/retweet/%s.json", String.valueOf(tweetId)));

        // eg - https://api.twitter.com/1.1/statuses/retweet/241259202004267009.json
        RequestParams params = new RequestParams();
/*
        if (tweetId > 0){
            params.put("id", tweetId);
        }
*/
        client.post(apiUrl, params, handler);
    }

    public void getTweetDetails(long id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(REST_DETAIL_TWEET_URL);
        RequestParams params = new RequestParams();
        params.put("id", id);
        client.get(apiUrl, params, handler);
    }

    public void verifyCredentials(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(REST_VERIFY_CREDENTIALS_URL);
        client.get(apiUrl, handler);
    }

    public void searchTweets(){

    }

}