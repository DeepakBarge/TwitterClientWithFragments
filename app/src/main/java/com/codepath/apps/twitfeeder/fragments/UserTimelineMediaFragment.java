package com.codepath.apps.twitfeeder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.adapters.MediaAdapter;
import com.codepath.apps.twitfeeder.adapters.TweetAdapter;
import com.codepath.apps.twitfeeder.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitfeeder.models.Tweet;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class UserTimelineMediaFragment extends Fragment{

    public static final String USER_ID = "userId";

    private long userID;

    final static int REFRESH_OPERATION = 1;
    final static int SCROLL_OPERATION = 0;

    private TwitterRestClient client;


    RecyclerView rvTweets;
    SwipeRefreshLayout swipeContainer;

    //@Bind(R.id.rvTweets) RecyclerView rvTweets;
    //@Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    MediaAdapter adapter;
    ArrayList<Tweet> fetchedTweets;
    long since_id, max_id;
    //LinearLayoutManager linearLayoutManager;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    public static UserTimelineMediaFragment newInstance(long userId) {
        Bundle args = new Bundle();
        args.putLong(USER_ID, userId);
        UserTimelineMediaFragment userTimelineMediaFragment = new UserTimelineMediaFragment();
        userTimelineMediaFragment.setArguments(args);
        return userTimelineMediaFragment;
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_media, container, false);
        //ButterKnife.bind(view);

        rvTweets = (RecyclerView) view.findViewById(R.id.rvTweets);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        Log.i("info", "oncreateview");

        // Set layout manager to position the items
        /*linearLayoutManager =
                new LinearLayoutManager(getContext());

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvTweets.setLayoutManager(linearLayoutManager);*/

        // Attach the adapter to the recyclerview to populate items

        //rvTweets.setAdapter(adapter);

        // Set layout manager to position the items
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvTweets.setLayoutManager(staggeredGridLayoutManager);


        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        rvTweets.setAdapter(new SlideInBottomAnimationAdapter(alphaAdapter));

        //ScaleInLeftAnimator animator = new ScaleInLeftAnimator();
        //FlipInLeftYAnimator animator = new FlipInLeftYAnimator();
        SlideInLeftAnimator animator = new SlideInLeftAnimator();
        //animator.setInterpolator(new OvershootInterpolator());
        rvTweets.setItemAnimator(animator);
        rvTweets.getItemAnimator().setAddDuration(200);
        rvTweets.getItemAnimator().setRemoveDuration(400);


        if(!ApplicationHelper.isNetworkAvailable(getContext()) || !ApplicationHelper.isOnline()){
            //ApplicationHelper.showWarning(HomeTimelineActivity.this);
            ArrayList<Tweet> twts = Tweet.getDBTweets();

            for(Tweet twt: twts){
                Log.i("info","db tweet: "+twt.getTweetText() +" "+twt.getTweetId());
            }

            adapter.addAtStartList(twts);
            Log.i("info", "adapter size " + adapter.tweets.size());
            //adapter.notifyItemRangeInserted(0, twts.size()-1);
            adapter.notifyDataSetChanged();

        } else {
            getTimeline(since_id, 0, REFRESH_OPERATION);
        }

        setupScrollListener();

        setupSwipeRefreshListener();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("info", "oncreate");

        client = TwitApplication.getRestClient();
        userID = getArguments().getLong(USER_ID);

        fetchedTweets = new ArrayList<>();

        // Create adapter passing in the sample user data
        adapter = new MediaAdapter(getContext());


        if (!ApplicationHelper.isNetworkAvailable(getContext()) || !ApplicationHelper.isOnline()) {
            ApplicationHelper.showWarning(getContext());
        } else {
            //getUserCredentials();
        }

        since_id = 1;
        max_id = 1;


    }

    private void getTimeline(final long sinceId, final long maxId, final int operation) {

        client.getHomeTimeline(sinceId, maxId, userID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Log.i("info", "timeline: " + response.toString());
                Log.i("info", "Tweets: " + response.length());

                try {
                    fetchedTweets = Tweet.getAllTweets(response);

                    if (fetchedTweets.size() > 0) {

                        if (operation == SCROLL_OPERATION) {
                            // get current size of the adapter
                            int curSize = adapter.getItemCount();
                            adapter.appendList(fetchedTweets);
                            adapter.notifyItemRangeInserted(curSize, adapter.getItemCount() - 1);

                                /* this doesnt work because if we scroll down to new elements the adapter thinks more
                                   elements are needed so it keeps scrolling infinitely making n/w calls to get new data */
                            //rvArticles.scrollToPosition(adapter.getItemCount() - 1);
                            //rvArticles.scrollToPosition(curSize+2);

                            Log.i("info", fetchedTweets.toString());
                            Log.i("info", "Scroll - Range inserted [" + curSize + "-" + adapter.getItemCount() + "]");
                        } else {
                            // get current size of the adapter
                            int curSize = fetchedTweets.size() - 1;
                            adapter.addAtStartList(fetchedTweets);
                            adapter.notifyItemRangeInserted(0, curSize);
                            rvTweets.smoothScrollToPosition(0);

                            Log.i("info", fetchedTweets.toString());
                            Log.i("info", "REFRESH - Range inserted [0-" + curSize + "]");

                        }
                        since_id = fetchedTweets.get(0).tweetId;
                        max_id = fetchedTweets.get(fetchedTweets.size() - 1).tweetId;
                        ApplicationHelper.persistData(adapter.tweets);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //hideProgressBar();
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                /// when internet not there this log statement crashes .. NULL pointer..
                Log.i("info", "error: " + errorResponse.toString());
                try {

                } catch (Exception e) {

                }
            }
        });

    }

    public void setupSwipeRefreshListener(){
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                Log.i("info", "refresh - new items needed " + since_id);
                getTimeline(since_id, 0, REFRESH_OPERATION);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    public void setupScrollListener()
    {
        rvTweets.addOnScrollListener(new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i("info", "scroll - new tweets needed " + since_id);
                getTimeline(0, max_id, SCROLL_OPERATION);
            }
        });
    }

}