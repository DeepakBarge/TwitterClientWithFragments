package com.codepath.apps.twitfeeder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.adapters.UserListAdapter;
import com.codepath.apps.twitfeeder.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitfeeder.models.User;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserFollowingListFragment extends Fragment {

    public static final String USER_ID = "userId";

    private long userID;

    private TwitterRestClient client;

    RecyclerView rvUsers;

    //@Bind(R.id.rvTweets) RecyclerView rvTweets;
    //@Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    UserListAdapter adapter;

    ArrayList<User> fetchedUsers;

    long newCursor = -1;

    LinearLayoutManager linearLayoutManager;

    //static User owner = new User();

    public static UserFollowingListFragment newInstance(long userId) {
        Bundle args = new Bundle();
        args.putLong(USER_ID, userId);
        UserFollowingListFragment userFollowingListFragment = new UserFollowingListFragment();
        userFollowingListFragment.setArguments(args);
        return userFollowingListFragment;
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        //ButterKnife.bind(view);

        rvUsers = (RecyclerView) view.findViewById(R.id.rvUsers);

        Log.i("info", "oncreateview");

        // Set layout manager to position the items
        linearLayoutManager =
                new LinearLayoutManager(getContext());

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvUsers.setLayoutManager(linearLayoutManager);

        // Attach the adapter to the recyclerview to populate items
        rvUsers.setAdapter(adapter);

        if(!ApplicationHelper.isNetworkAvailable(getContext()) || !ApplicationHelper.isOnline()){
            //ApplicationHelper.showWarning(HomeTimelineActivity.this);
            ArrayList<User> usrs = User.getDBUsers();

            for(User usr: usrs){
                Log.i("info","db user: "+usr.getName());
            }

            adapter.addAtStartList(usrs);
            Log.i("info", "adapter size " + adapter.users.size());
            //adapter.notifyItemRangeInserted(0, twts.size()-1);
            adapter.notifyDataSetChanged();

        } else {
            if(newCursor != 0) {
                getFollowingList(userID, newCursor);
            }
        }

        setupScrollListener();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("info", "oncreate");

        client = TwitApplication.getRestClient();
        userID = getArguments().getLong(USER_ID);

        fetchedUsers = new ArrayList<>();

        // Create adapter passing in the sample user data
        adapter = new UserListAdapter(getContext());


        if (!ApplicationHelper.isNetworkAvailable(getContext()) || !ApplicationHelper.isOnline()) {
            ApplicationHelper.showWarning(getContext());
        } else {
            //getUserCredentials();
        }

    }

    private void getFollowingList(final long userId, final long cursor) {

        client.getFriends(userId, cursor, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.i("info", "following list: " + response.toString());
                Log.i("info", "following : " + response.length());

                try {
                    fetchedUsers = User.getAllUsers(response.getJSONArray("users"));

                    if (fetchedUsers.size() > 0) {

                        // get current size of the adapter
                        int curSize = adapter.getItemCount();
                        adapter.appendList(fetchedUsers);
                        adapter.notifyItemRangeInserted(curSize, adapter.getItemCount() - 1);

                            /* this doesnt work because if we scroll down to new elements the adapter thinks more
                               elements are needed so it keeps scrolling infinitely making n/w calls to get new data */
                        //rvArticles.scrollToPosition(adapter.getItemCount() - 1);
                        //rvArticles.scrollToPosition(curSize+2);

                        Log.i("info", fetchedUsers.toString());
                        Log.i("info", "Scroll - Range inserted [" + curSize + "-" + adapter.getItemCount() + "]");
                    }

                    newCursor = response.getLong("next_cursor");

                    //ApplicationHelper.persistData(adapter.users);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //hideProgressBar();
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


    public void setupScrollListener()
    {
        rvUsers.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.i("info", "scroll - new followers needed " + newCursor);
                getFollowingList(userID, newCursor);
            }
        });
    }

}
