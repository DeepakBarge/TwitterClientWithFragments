package com.codepath.apps.twitfeeder.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.adapters.TweetAdapter;
import com.codepath.apps.twitfeeder.fragments.ComposeNewTweetFragment;
import com.codepath.apps.twitfeeder.fragments.HomeTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.fragments.MentionsTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.fragments.MentionsTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.fragments.UserFavouritesListFragment;
import com.codepath.apps.twitfeeder.fragments.UserTimelineMediaFragment;
import com.codepath.apps.twitfeeder.listeners.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitfeeder.models.Tweet;
import com.codepath.apps.twitfeeder.models.User;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeTimelineActivity extends AppCompatActivity {

    public class HomeTimelinePagerAdapter extends FragmentPagerAdapter {

        String tabs [] = {"Home", "Mentions"};


        public HomeTimelinePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return tabs[0];

                case 1:
                    return tabs[1];

                default:
                    return "";
            }
            //return (position == 0)? "Home" : "Mentions" ;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0:
                    return HomeTimelineTweetsFragment.newInstance(0);
                //break;

                case 1:
                    return MentionsTimelineTweetsFragment.newInstance(0);
                //break;

                default:
                    return null;
                //break;
            }

            //return (position == 0)? HomeTimelineTweetsFragment.newInstance(0) : MentionsTimelineTweetsFragment.newInstance(0) ;
        }
    }

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        ButterKnife.bind(this);

        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new HomeTimelinePagerAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        tabs.setIndicatorHeight(4);
        tabs.setIndicatorColor(0xFF55ACEE);
        tabs.setTextColor(0xFF55ACEE);

        ApplicationHelper.setContext(HomeTimelineActivity.this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Timeline");

        getSupportActionBar().setLogo(R.drawable.ic_twitter_icon);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setImageResource(R.drawable.ic_action_composetweet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                //showComposeTweetDialog(0, 0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //client.searchTweets();
                // search the tweets searchtweetsfragment.searchTweets();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
