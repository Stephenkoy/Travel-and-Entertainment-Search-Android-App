package com.csci571.koy.hw9.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.activity.PlacesDetailActivity;
import com.csci571.koy.hw9.interfaces.SharedPreference;
import com.csci571.koy.hw9.model.SearchResultItem;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class SearchItemRecyclerViewAdapter extends RecyclerView.Adapter<SearchItemRecyclerViewAdapter.ViewHolder> {


//    private final OnListFragmentInteractionListener mListener;
    private List<SearchResultItem> searchResultItemList;
    private static final String SERVER = "http://hw8-express.appspot.com/api";
    private static final String GMAPS_API_CALL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    private static final String API_KEY = "AIzaSyBbLH6p0js-mnX1Pj4jHY7Ui6jfY0su4_w";
    private static final String TAG = "Recycler View Adapter";

    private ProgressDialog progressDialog;
    private Context context;
    private String placeDetailJSON;
    private SharedPreference favorites;

//    public SearchItemRecyclerViewAdapter(List<SearchResultItem> items, OnListFragmentInteractionListener listener) {
//        this.searchResultItemList = items;
//        this.mListener = listener;
//    }
    public SearchItemRecyclerViewAdapter(Context context, List<SearchResultItem> items) {
        this.searchResultItemList = items;
        favorites = new SharedPreference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final SearchResultItem resultItem = searchResultItemList.get(position);
        // set the image icon
//        holder.icon.setMinimumWidth(125);
//        holder.icon.setMaxWidth(125);
//        holder.icon.setMaxHeight(125);
//        holder.icon.setMinimumHeight(125);
        progressDialog = new ProgressDialog(holder.itemView.getContext());
        Picasso.get().load(resultItem.getIconUrl()).resize(125,125).into(holder.icon);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        int width = displayMetrics.widthPixels;
        holder.placeName.setText(resultItem.getPlaceName());
        holder.vicinity.setText(resultItem.getVicinity());
        if (checkFavoriteItem(resultItem)) {
            holder.favoritesImgBtn.setImageResource(R.drawable.heart_fill_red);
        }
        else { // Not favorited.
            holder.favoritesImgBtn.setImageResource(R.drawable.heart_outline_black);
        }

        holder.favoritesImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if not favorited add it to the favorites list
                if (!checkFavoriteItem(resultItem)) {
                    // Do necessary Favorites stuff here
                    addToFavorites(context, resultItem);
                    holder.favoritesImgBtn.setImageResource(R.drawable.heart_fill_red);
                    Toast.makeText(context, ""+resultItem.getPlaceName()+", added to Favorites", Toast.LENGTH_SHORT).show();
                }
                else {
                    holder.favoritesImgBtn.setImageResource(R.drawable.heart_outline_black);
                    removeFromFavorites(context, resultItem);
                    Toast.makeText(context, ""+resultItem.getPlaceName()+", removed to Favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an HTTP Volley call to the server to get the places details id.
                progressDialog.setMessage("Fetching details");
                progressDialog.setCancelable(false);
                progressDialog.show();
                final String nodeURL = SERVER + "/places/details/search/placeId/"+resultItem.getPlaceId();
                Log.d(TAG, ""+resultItem.getPlaceId());
                // TODO: REPLACE THIS WITH NODE JS SERVER URLS.
                final String directCallURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+resultItem.getPlaceId()+"&key="+API_KEY;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Intent i = new Intent(holder.itemView.getContext(), PlacesDetailActivity.class);
                        i.putExtra("place_id", resultItem.getPlaceId());

                        RequestQueue httpQueue = Volley.newRequestQueue(context);

                        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, nodeURL, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        placeDetailJSON = response.toString();
                                        i.putExtra("JSON", response.toString());
                                        i.putExtra("place_name", resultItem.getPlaceName());
                                        i.putExtra("IS_FAVORITE", resultItem.getIsFavorited());
                                        i.putExtra("my_lat", resultItem.getMyLat());
                                        i.putExtra("my_lng", resultItem.getMyLng());
                                        i.putExtra("vicinity", resultItem.getVicinity());
                                        i.putExtra("icon_url", resultItem.getIconUrl());
                                        i.putExtra("place_lat", resultItem.getPlaceLat());
                                        i.putExtra("place_lng", resultItem.getPlaceLng());
                                        holder.itemView.getContext().startActivity(i);
                                        Thread progress = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                            }
                                        });
                                        try {
                                            progress.sleep(4000);
                                            progress.start();
                                        } catch (InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Error.Response", error.getMessage());
                                    }
                                }
                        );
                        // add it to the RequestQueue
                        httpQueue.add(getRequest);
                    }
                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResultItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon, favoritesImgBtn;
        public TextView placeName, vicinity;
        public SearchResultItem mItem;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.category_icon);
            placeName = (TextView) view.findViewById(R.id.place_name);
            vicinity = (TextView) view.findViewById(R.id.vicinity);
            favoritesImgBtn = (ImageView) view.findViewById(R.id.favorites_item_button);
        }
    }

    // check for Favorites
    public boolean checkFavoriteItem(SearchResultItem item) {
        boolean check = false;
        List<SearchResultItem> favoriteList = favorites.getFavorites(context);
        if (favoriteList != null) {
            for (SearchResultItem checkItem : favoriteList) {
                if (checkItem.getPlaceId().equals(item.getPlaceId())) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    // adds to the favorites list
    public void addToFavorites(Context context, SearchResultItem item) {
        // use the placeId to remove the favorites
        if (!checkFavoriteItem(item)) {
            favorites.addFavorite(context, item);
//            SearchItemRecyclerViewAdapter.super.notifyDataSetChanged();
        }
    }

    public void removeFromFavorites(Context context, SearchResultItem item) {
        // use the placeId to remove the favorites
        if (checkFavoriteItem(item)) {
            favorites.removeFavorite(context, item);
//            SearchItemRecyclerViewAdapter.this.notifyDataSetChanged();
        }
    }
}
