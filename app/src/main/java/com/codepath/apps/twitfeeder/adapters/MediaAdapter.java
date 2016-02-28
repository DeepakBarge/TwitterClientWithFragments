package com.codepath.apps.twitfeeder.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitfeeder.R;
import com.codepath.apps.twitfeeder.models.Tweet;

import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    Context context;

    public ArrayList<Tweet> tweets = new ArrayList<>();

    public MediaAdapter(Context context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        //@Bind(R.id.tvHeadline) TextView mHeadline;
        @Bind(R.id.llMediaContainer) LinearLayout mMediaContainer;
        //@Nullable @Bind(R.id.ivUserImage) ImageView mImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View mediaView = inflater.inflate(R.layout.list_user_media, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(mediaView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Get the data model based on position
        Tweet tweet = tweets.get(position);

        holder.mMediaContainer.removeAllViews();

        if(tweet.isMedia()) {
            if (tweet.getMediaType().equalsIgnoreCase("photo")) {
                View v = LayoutInflater.from(context).inflate(R.layout.user_media_image, null);
                ImageView ivMediaImage = (ImageView) v.findViewById(R.id.ivUserImage);
                //String url = tweet.getPictureUrl();
                //Log.i("info", "media:" + url);

                Glide.with(context)
                        .load(tweet.getPictureUrl())
                        .crossFade()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(ivMediaImage);
                holder.mMediaContainer.addView(v);
            }


            if (tweet.getMediaType().equalsIgnoreCase("video")) {

            }
        }

    }


    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void appendList (ArrayList<Tweet> t) {
        tweets.addAll(t);
        Log.i("info", "Number of tweets appended " + tweets.size());
    }

    public void addAtStartList (ArrayList<Tweet> t) {

        tweets.addAll(0, t);
        Log.i("info", "Number of tweets prepended " + tweets.size());
    }

}