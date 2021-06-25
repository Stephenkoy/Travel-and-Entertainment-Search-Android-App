package com.csci571.koy.hw9.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.model.PlaceReviewItem;
import com.squareup.picasso.Picasso;


import java.util.List;

/**
 * TODO: Implement something similar to SearchItemRecyclerViewAdapter class for the review items
 *
 */
public class ReviewItemViewAdapter extends RecyclerView.Adapter<ReviewItemViewAdapter.ViewHolder> {

    private List<PlaceReviewItem> reviewItemList;
    private static final String SERVER = "http://hw8-express.appspot.com/api";
    private static final String GMAPS_API_CALL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    private static final String API_KEY = "AIzaSyBbLH6p0js-mnX1Pj4jHY7Ui6jfY0su4_w";
    private static final String TAG = "Review View Adapter";

    private Context context;
    private Intent i;


    public ReviewItemViewAdapter(Context context, List<PlaceReviewItem> items) {
        this.reviewItemList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final PlaceReviewItem reviewItem = reviewItemList.get(position);

        Picasso.get().load(reviewItem.getProfilePic()).resize(125,125).into(holder.profilePic);
        holder.authorNameView.setText(reviewItem.getAuthorName());
        holder.ratingBar.setRating((float)reviewItem.getreviewRating());
        // TODO: Make sure the time is parsed in the google review async task then set to the text.
        holder.reviewTimeView.setText(reviewItem.getTimeCreated());
        holder.reviewTextView.setText(reviewItem.getReviewText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = reviewItem.getUrl();
                Uri uri = Uri.parse(url);
                i = new Intent(Intent.ACTION_VIEW, uri);
                holder.itemView.getContext().startActivity(i);
            }
        });
    }

    public void swapData(List<PlaceReviewItem> sortedList) {
        if (reviewItemList != null) {
            reviewItemList.clear();
            reviewItemList.addAll(sortedList);
        } else
            reviewItemList = sortedList;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reviewItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profilePic;
        public TextView authorNameView, reviewTimeView, reviewTextView ;
        public RatingBar ratingBar;

        public ViewHolder(View view) {

            super(view);

            profilePic = (ImageView) view.findViewById(R.id.profile_pic);
            authorNameView = (TextView) view.findViewById(R.id.author_name);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
            reviewTimeView = (TextView) view.findViewById(R.id.review_time);
            reviewTextView = (TextView) view.findViewById(R.id.review_text);
        }
    }
}
