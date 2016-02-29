package com.codepath.apps.twitfeeder.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.activities.UserProfileDetailsActivity;
import com.codepath.apps.twitfeeder.models.Tweet;
import com.codepath.apps.twitfeeder.models.User;
import com.codepath.apps.twitfeeder.net.TwitApplication;
import com.codepath.apps.twitfeeder.net.TwitterRestClient;
import com.codepath.apps.twitfeeder.utils.ApplicationHelper;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    Context context;

    private TwitterRestClient client = TwitApplication.getRestClient();

    public boolean isFollowing = false;

    public boolean  isFollowed = false;

    public ArrayList<User> users = new ArrayList<>();

    public UserListAdapter(Context context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        @Bind(R.id.tvScreenName) TextView mScreenName;
        @Bind(R.id.tvName) TextView mName;
        @Bind(R.id.tvDescription) TextView mDescription;
        @Bind(R.id.ivProfileImage) ImageView mImage;
        @Bind(R.id.bAddUser) Button mAddUser;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mImage.setOnClickListener(this);
            mAddUser.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getLayoutPosition(); // gets item position

            Intent i;

            switch (v.getId()) {

                case R.id.ivProfileImage:

                    Log.i("info", "profile image clicked :" + users.get(position).getScreenName());
                    Log.i("info", "profile banner url :" + users.get(position).getBanner_image_url());

                    //listener.onProfileImageClicked(tweets.get(position).getUser().getUserId(), position);

                    i = new Intent(context, UserProfileDetailsActivity.class);
                    i.putExtra("user", users.get(position));
                    context.startActivity(i);
                    ((Activity)context).overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    break;

                case R.id.bAddUser:

                    toggleUserRelationship(users.get(position), mAddUser);

                    break;

                default:
                    break;

            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View mediaView = inflater.inflate(R.layout.list_profile_details, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(mediaView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Get the data model based on position
        User user = users.get(position);

        holder.mAddUser.setVisibility(View.INVISIBLE);
        findUserRelationship(user, holder.mAddUser);

        holder.mDescription.setText(user.getDescription());
        holder.mName.setText(user.getName());
        holder.mScreenName.setText("@" + user.getScreenName());

        final ImageView ivTemp = holder.mImage;
        ivTemp.setImageResource(0);

        Glide.with(context)
            .load(user.getProfile_image_url())
            .asBitmap()
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(new BitmapImageViewTarget(ivTemp) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    //circularBitmapDrawable.setCircular(true);
                    circularBitmapDrawable.setCornerRadius(10);
                    //circularBitmapDrawable.setCornerRadius(Math.max(resource.getWidth(), resource.getHeight()) / 2.0f);
                    ivTemp.setImageDrawable(circularBitmapDrawable);
                }
            });

    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public void appendList (ArrayList<User> u) {
        users.addAll(u);
        Log.i("info", "Number of users appended " + users.size());
    }

    public void addAtStartList (ArrayList<User> u) {

        users.addAll(0, u);
        Log.i("info", "Number of users prepended " + users.size());
    }

    public void toggleUserRelationship(User user, final Button button){

        if(isFollowing) {

            client.unFollowUser(user.getUserId(), new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    //mAddUser.setVisibility(View.VISIBLE);
                    button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_hollow));
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_user_filled, 0, 0, 0);
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
                    button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_filled));
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_added, 0, 0, 0);
                    isFollowing = true;
                    Log.i("info", "Successful call to follow user");

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                }

            });
        }
    }

    public void findUserRelationship(User user, final Button button) {

        Log.i("info", "name: " + ApplicationHelper.getOwner().getName());

        client.getFriendshipStatus(ApplicationHelper.getOwner().getUserId(), user.getUserId(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                button.setVisibility(View.VISIBLE);

                Log.i("info", "Successful call to get user relation");
                Log.i("info", "Relation: " + response.toString());
                try {
                    isFollowing = response.getJSONObject("relationship").getJSONObject("source").getBoolean("following");
                    if (isFollowing) {
                        Log.i("info", "Following this user");
                        button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_filled));
                        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_added, 0, 0, 0);
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
}