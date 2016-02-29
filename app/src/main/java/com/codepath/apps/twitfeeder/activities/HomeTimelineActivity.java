package com.codepath.apps.twitfeeder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.adapters.SmartFragmentStatePagerAdapter;
import com.codepath.apps.twitfeeder.fragments.HomeTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.fragments.MentionsTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.models.User;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeTimelineActivity extends AppCompatActivity {

    private TwitterRestClient client;

    public class HomeTimelinePagerAdapter extends SmartFragmentStatePagerAdapter {

        private String tabs [] = {"Home", "Mentions"};

        //private int tabIcons[] = {R.drawable.ic_home_tab, R.drawable.ic_home_tab, R.drawable.ic_home_tab};

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

        }

    }

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        ButterKnife.bind(this);

        client = TwitApplication.getRestClient();
        getUserCredentials();

        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);

        final HomeTimelinePagerAdapter homeTimelinePagerAdapter = new HomeTimelinePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(homeTimelinePagerAdapter);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        tabs.setIndicatorHeight(6);
        tabs.setIndicatorColor(0xFF55ACEE);
        tabs.setTextColor(0xFF55ACEE);

        ApplicationHelper.setContext(HomeTimelineActivity.this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Timeline");

        getSupportActionBar().setLogo(R.drawable.ic_twitter_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setImageResource(R.drawable.ic_action_composetweet_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                HomeTimelineTweetsFragment temp = (HomeTimelineTweetsFragment) homeTimelinePagerAdapter.getRegisteredFragment(0);

                temp.showComposeTweetDialog(0,0);
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

                client.searchTweets();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()) {

            case R.id.action_profile:

                Intent i = new Intent(HomeTimelineActivity.this, UserProfileDetailsActivity.class);
                i.putExtra("user", ApplicationHelper.getOwner());
                (HomeTimelineActivity.this).startActivity(i);
                (HomeTimelineActivity.this).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                return true;

            case R.id.action_search:

                return true;

            case R.id.action_messages:

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void getUserCredentials() {
        client.verifyCredentials(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.i("info", "Successful to get credentials " + response.toString());
                User owner = User.fromJSON(response);
                Log.i("info", "Owner name: " + owner.getName() + " " + owner.getProfile_image_url());
                getSupportActionBar().setTitle(" @" + owner.getScreenName());
                //ApplicationHelper.persistData(adapter.tweets);
                ApplicationHelper.setOwner(owner);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

}
