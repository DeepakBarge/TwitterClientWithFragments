package com.codepath.apps.twitfeeder.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.fragments.HomeTimelineTweetsFragment;
import com.codepath.apps.twitfeeder.fragments.UserFavouritesListFragment;
import com.codepath.apps.twitfeeder.fragments.UserFollowerListFragment;
import com.codepath.apps.twitfeeder.fragments.UserFollowingListFragment;
import com.codepath.apps.twitfeeder.fragments.UserTimelineMediaFragment;
import com.codepath.apps.twitfeeder.models.User;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileDetailsActivity extends AppCompatActivity {

    public User user;

    private TwitterRestClient client;

    public boolean isFollowing = false;

    public boolean  isFollowed = false;

    @Bind(R.id.profile) ImageView mProfileImage;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.banner) ImageView mBanner;
    @Bind(R.id.name) TextView mName;
    @Bind(R.id.screenName) TextView mScreenName;
    @Bind(R.id.description) TextView mDescription;
    @Bind(R.id.followersCount) TextView mFollowersCount;
    @Bind(R.id.followingCount) TextView mFollowingCount;
    @Bind(R.id.bAddUser) Button mAddUser;

    public class UserProfilePagerAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        private int tabIcons[] = {R.drawable.ic_tweets_tab, R.drawable.ic_photos_tab,
                R.drawable.ic_favorite_tab, R.drawable.ic_followers_tab, R.drawable.ic_following_tab};

        String tabs [] = {"Tweets", "Photos", "Favourites", "Followers", "Following"};

        public UserProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return tabs[0];

                case 1:
                    return tabs[1];

                case 2:
                    return tabs[2];

                case 3:
                    return tabs[3];

                case 4:
                    return tabs[4];

                default:
                    return "";
            }
            //return (position == 0)? "Tweets" : "Photos" ;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){

                case 0:
                    return HomeTimelineTweetsFragment.newInstance(user.getUserId());
                    //break;

                case 1:
                    return UserTimelineMediaFragment.newInstance(user.getUserId());
                    //break;

                case 2:
                    return UserFavouritesListFragment.newInstance(user.getUserId());
                //break;

                case 3:
                    return UserFollowerListFragment.newInstance(user.getUserId());
                //break;

                case 4:
                    return UserFollowingListFragment.newInstance(user.getUserId());
                //break;

                default:
                    return null;
                    //break;
            }
            //return (position == 0)? HomeTimelineTweetsFragment.newInstance(user.getUserId()) : UserTimelineMediaFragment.newInstance(user.getUserId()) ;
        }

        @Override
        public int getPageIconResId(int position) {
            return tabIcons[position];
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_details);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = TwitApplication.getRestClient();

        Intent intent = getIntent();

        user = intent.getParcelableExtra("user");

        // never check if its the owner's profile
        if(ApplicationHelper.getOwner().getUserId() != user.getUserId()) {
            findUserRelationship();
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(user.getName());

        Glide.with(getApplicationContext())
                .load(user.getProfile_image_url())
                .asBitmap()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(new BitmapImageViewTarget(mProfileImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        //circularBitmapDrawable.setCircular(true);
                        circularBitmapDrawable.setCornerRadius(10);
                        //circularBitmapDrawable.setCornerRadius(Math.max(resource.getWidth(), resource.getHeight()) / 2.0f);
                        mProfileImage.setImageDrawable(circularBitmapDrawable);
                    }
                });

        Glide.with(this).load(user.getBanner_image_url()).centerCrop().into(mBanner);

        mName.setText(user.getName());
        mScreenName.setText("@"+user.getScreenName());
        mDescription.setText(user.getDescription());
        mFollowersCount.setText(String.valueOf(user.getFollowersCount()));
        mFollowingCount.setText(String.valueOf(user.getFriendsCount()));

        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new UserProfilePagerAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        tabs.setIndicatorHeight(8);
        tabs.setIndicatorColor(0xFF55ACEE);
        tabs.setTextColor(0xFF55ACEE);
        //tabs.setPadding(5,5,5,5);

        mAddUser.setVisibility(View.INVISIBLE);

        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("info","toggle user clicked");
                toggleUserRelationship();
            }
        });
    }

    public void toggleUserRelationship(){

        if(isFollowing) {

            client.unFollowUser(user.getUserId(), new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    //mAddUser.setVisibility(View.VISIBLE);
                    mAddUser.setBackground(ContextCompat.getDrawable(UserProfileDetailsActivity.this, R.drawable.button_hollow));
                    mAddUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_user_filled, 0, 0, 0);
                    isFollowing = false;
                    Log.i("info", "Successful call to unfollow user");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                }

            });

        } else {

            client.followUser(user.getUserId(), new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    //mAddUser.setVisibility(View.VISIBLE);
                    mAddUser.setBackground(ContextCompat.getDrawable(UserProfileDetailsActivity.this, R.drawable.button_filled));
                    mAddUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_added, 0, 0, 0);
                    isFollowing = true;
                    Log.i("info", "Successful call to follow user");

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                }

            });
        }
    }

    public void findUserRelationship() {

        Log.i("info", "name: "+ApplicationHelper.getOwner().getName());

        client.getFriendshipStatus(ApplicationHelper.getOwner().getUserId(), user.getUserId(), new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                mAddUser.setVisibility(View.VISIBLE);

                Log.i("info","Successful call to get user relation");
                Log.i("info","Relation: "+response.toString());
                try {
                    isFollowing = response.getJSONObject("relationship").getJSONObject("source").getBoolean("following");
                    if(isFollowing){
                        Log.i("info","Following this user");
                        mAddUser.setBackground(ContextCompat.getDrawable(UserProfileDetailsActivity.this, R.drawable.button_filled));
                        mAddUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_added, 0, 0, 0);
                    }
                    isFollowed = response.getJSONObject("relationship").getJSONObject("source").getBoolean("followed_by");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    /* this method is overridden to prevent the UP/BACK button_hollow from creating a new activity
instead of showing the old activity */
    @Override
    public Intent getSupportParentActivityIntent() {
        finish();
        return null;
    }


}
