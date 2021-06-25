package com.csci571.koy.hw9.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.adapter.ReviewItemViewAdapter;
import com.csci571.koy.hw9.model.PlaceReviewItem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import java.util.List;

/**
 * TODO: Add Spinner Data and Listeners
 * TODO: Add dummy data to fix the views... add flags for google reviews, yelp reviews and their respective sort options.
 * TODO: Pass JSON through from bundle and Create AsyncTasks to parse the JSON into review item objects in a List.
 * TODO: **Use SearchResultsActivity as reference
 * TODO: Create Async Task to get the Yelp Business Reviews, call method inside of Yelp Business Id fetch callback.
 * TODO: Parse the time objects
 * TODO: Implement the sort methods per each specific search option
 */
public class ReviewsFragment extends Fragment {

    private static final String TAG = "Reviews Fragment";

    private String reviewJSON;
    private String yelpReviewJSON, yelpBusiness;
    private boolean hasGoogleReviews;
    private boolean hasYelpReviews;

    private TextView noReviewMessage;
    private Spinner reviewTypeSpinner, reviewOrderTypeSpinner;
    private RecyclerView recyclerView;
    private Context context;
    private ReviewItemViewAdapter googleReviewAdapter;
    private ReviewItemViewAdapter yelpReviewAdapter;

    // Review Item Lists
    private List<PlaceReviewItem> defaultGoogleReviews = new ArrayList<>();
    private List<PlaceReviewItem> defaultYelpReviews = new ArrayList<>();
    private List<PlaceReviewItem> googleReviews = new ArrayList<>();
    private List<PlaceReviewItem> yelpReviews = new ArrayList<>();

    private String streetNum = "", streetName = "", city ="", state ="", country="", zipCode="",
                    placePhone ="", placeName="";
    private Double placeLat, placeLng;
    private String currentReviewType, googleReviewOrder, yelpReviewOrder, reviewType, reviewOrder;
    private Bundle b;

    private String url, text, timeCreated, authorName, profilePic;
    private Double rating;

    private Gson gson;
    private JsonParser parser;

    // Comparison Variables

    public ReviewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.review_fragment, container, false);
        String string = "/yelp/search/business/name/:name/address/:address/city/:city/state/:state/country/:country/lat/:lat/lon/:lon/phone/:phone/zip/:zip";

        context = getActivity().getApplicationContext();
        gson = new Gson();

        // get arguments for the review bundle!
        if (getArguments() != null) {
            b = getArguments();
            noReviewMessage = (TextView) view.findViewById(R.id.reviewErrorMessage);
            if (b.getBoolean("google_reviews") == false) {
                noReviewMessage.setVisibility(View.VISIBLE);
            }
                // TODO: add Parse Google JSON Async Task
            if (b.getString("reviewJSON") != null) {
                reviewJSON = b.getString("reviewJSON");
                Log.d(TAG,"reviewJSON:" + reviewJSON);
                GetGoogleReviews getGoogleReviews = new GetGoogleReviews();
                getGoogleReviews.execute();
            }
            if (b.getBoolean("google_reviews") != false) {
                hasGoogleReviews = b.getBoolean("google_reviews");
                Log.d(TAG, ""+hasGoogleReviews);
            }
            else {
                hasGoogleReviews = b.getBoolean("google_reviews");
            }
            if (b.getString("street_num") != null)
                streetNum = b.getString("street_num");
            Log.d(TAG, "Street Num: " + streetNum);
            if (b.getString("street_name") != null)
                streetNum = b.getString("street_name");
            if (b.getString("city") != null)
                streetNum = b.getString("city");
            if (b.getString("state") != null)
                streetNum = b.getString("state");
            if (b.getString("city") != null)
                streetNum = b.getString("city");
            if (b.getString("place_phone") != null)
                placePhone = b.getString("place_phone");
            if (b.getString("place_name") != null)
                placeName = b.getString("place_name");
            placeLat = b.getDouble("place_lat");
            placeLng = b.getDouble("place_lng");

            if (b.getString("yelpReviews") != null) {
                yelpReviewJSON = b.getString("yelpReviews");
                GetYelpReviews getYelpReviews = new GetYelpReviews();
                getYelpReviews.execute();
            }
            if (b.getString("yelpB") != null)
                yelpBusiness = b.getString("yelpB");

//            Log.d(TAG, "Yelp Reviews: " + yelpReviewJSON);
//            Log.d(TAG, "Yelp Business: " + yelpBusiness);

            // only run yelp reviews if we can get these fields I guess.
        }
        // init view items
        reviewTypeSpinner = (Spinner) view.findViewById(R.id.reviewTypeSpinner);
        reviewOrderTypeSpinner = (Spinner) view.findViewById(R.id.reviewOrderTypeSpinner);

        ArrayAdapter<CharSequence> reviewTypeAdapter = ArrayAdapter.createFromResource(context,
                R.array.reviewType, android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> reviewOrderAdapter = ArrayAdapter.createFromResource(context,
                R.array.reviewOrderType, android.R.layout.simple_spinner_dropdown_item);

        reviewTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reviewOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reviewTypeSpinner.setAdapter(reviewTypeAdapter);
        reviewOrderTypeSpinner.setAdapter(reviewOrderAdapter);

        // add an onSelected Listener to the review type spinner
        reviewTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "We selected a review type");
                Log.d(TAG, "At Position: " + position);
                Log.d(TAG, reviewTypeSpinner.getSelectedItem().toString());

                if (reviewTypeSpinner.getSelectedItem().toString().equals("Google reviews")) {
                    setCurrentReviewType(reviewTypeSpinner.getSelectedItem().toString());
                    // change to the googleReviewAdapter
                    //googleReviewAdapter = new ReviewItemViewAdapter(context, googleReviews);
                    recyclerView.setAdapter(googleReviewAdapter);
                    if (googleReviews != null && googleReviews.size() > 0) {
                        noReviewMessage.setVisibility(View.GONE);
                    } else {
                        // no reviews or null
                        noReviewMessage.setVisibility(View.VISIBLE);
                    }

                } else if (reviewTypeSpinner.getSelectedItem().toString().equals("Yelp reviews")) {
                    setCurrentReviewType(reviewTypeSpinner.getSelectedItem().toString());
                    //yelpReviewAdapter = new ReviewItemViewAdapter(context, yelpReviews);
                    recyclerView.setAdapter(yelpReviewAdapter);
                    if (yelpReviews != null && yelpReviews.size() > 0) {
                        noReviewMessage.setVisibility(View.GONE);
                    } else {
                        noReviewMessage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // lolwut
            }
        });
        // recycler view stuff
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        googleReviewAdapter = new ReviewItemViewAdapter(getContext(), googleReviews);
        yelpReviewAdapter = new ReviewItemViewAdapter(getContext(), yelpReviews);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

        // set the google review adapter by default
        recyclerView.setAdapter(googleReviewAdapter);

        // add an onSelected Listener to the review type spinner for
        reviewOrderTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "We selected a review order type");
                Log.d(TAG, "At position: " + position);
                Log.d(TAG, reviewOrderTypeSpinner.getSelectedItem().toString());

                if (reviewOrderTypeSpinner.getSelectedItem().toString().equals("Default order"))
                    defaultSortOrder();
                else if (reviewOrderTypeSpinner.getSelectedItem().toString().equals("Highest rating"))
                    sortHighestRating();
                else if (reviewOrderTypeSpinner.getSelectedItem().toString().equals("Lowest rating"))
                    sortLowestRating();
                else if (reviewOrderTypeSpinner.getSelectedItem().toString().equals("Most recent"))
                    sortMostRecent();
                else if (reviewOrderTypeSpinner.getSelectedItem().toString().equals("Least recent"))
                    sortLeastRecent();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void defaultSortOrder() {
        // just set the google and yelp review arrays to the stored default ones and call adapter change
        if (googleReviews != null) {
            googleReviewAdapter = new ReviewItemViewAdapter(context, defaultGoogleReviews);
//            googleReviewAdapter.swapData(defaultGoogleReviews);
            if (getCurrentReviewType().equals("Google reviews"))
                recyclerView.setAdapter(googleReviewAdapter);
        }

        if (yelpReviews != null) {
            yelpReviewAdapter = new ReviewItemViewAdapter(context, defaultYelpReviews);
//            yelpReviewAdapter.swapData(defaultYelpReviews);
            if (getCurrentReviewType().equals("Yelp reviews"))
                recyclerView.setAdapter(yelpReviewAdapter);
        }
    }

    private void sortHighestRating() {

        if (googleReviews != null) {
            Collections.sort(googleReviews, new Comparator<PlaceReviewItem>() {
                @Override
                public int compare(PlaceReviewItem obj1, PlaceReviewItem obj2) {
//                    return Double.valueOf(obj2.getreviewRating()).compareTo(obj1.getreviewRating());
                    if (obj1.getreviewRating() < obj2.getreviewRating())
                        return 1;
                    else if (obj1.getreviewRating() > obj2.getreviewRating())
                        return -1;
                    else
                        return 0;
                }
            });
            googleReviewAdapter = new ReviewItemViewAdapter(context, googleReviews);
//            googleReviewAdapter.swapData(googleReviews);
            googleReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Google reviews"))
                recyclerView.setAdapter(googleReviewAdapter);
        }
        if (yelpReviews != null) {
            Collections.sort(yelpReviews, new Comparator<PlaceReviewItem>() {
                @Override
                public int compare(PlaceReviewItem obj1, PlaceReviewItem obj2) {
                    if (obj1.getreviewRating() < obj2.getreviewRating())
                        return 1;
                    else if (obj1.getreviewRating() > obj2.getreviewRating())
                        return -1;
                    else
                        return 0;
                }
            });
            yelpReviewAdapter = new ReviewItemViewAdapter(context, yelpReviews);
//            yelpReviewAdapter.swapData(yelpReviews);
            yelpReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Yelp reviews"))
                recyclerView.setAdapter(yelpReviewAdapter);
        }
    }

    private void sortLowestRating() {
        if (googleReviews != null) {
            Collections.sort(googleReviews, new Comparator<PlaceReviewItem>() {
                @Override
                public int compare(PlaceReviewItem obj1, PlaceReviewItem obj2) {
//                    return Double.valueOf(obj2.getreviewRating()).compareTo(obj1.getreviewRating());
                    if (obj1.getreviewRating() > obj2.getreviewRating())
                        return 1;
                    else if (obj1.getreviewRating() < obj2.getreviewRating())
                        return -1;
                    else
                        return 0;
                }
            });
            googleReviewAdapter = new ReviewItemViewAdapter(context, googleReviews);
//            googleReviewAdapter.swapData(googleReviews);
            googleReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Google reviews"))
                recyclerView.setAdapter(googleReviewAdapter);
        }
        if (yelpReviews != null) {
            Collections.sort(yelpReviews, new Comparator<PlaceReviewItem>() {
                @Override
                public int compare(PlaceReviewItem obj1, PlaceReviewItem obj2) {
                    if (obj1.getreviewRating() > obj2.getreviewRating())
                        return 1;
                    else if (obj1.getreviewRating() < obj2.getreviewRating())
                        return -1;
                    else
                        return 0;
                }
            });
            yelpReviewAdapter = new ReviewItemViewAdapter(context, yelpReviews);
//            yelpReviewAdapter.swapData(yelpReviews);
            yelpReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Yelp reviews"))
                recyclerView.setAdapter(yelpReviewAdapter);
        }
    }

    private void sortMostRecent() {

        if (googleReviews != null) {
            Collections.sort(googleReviews, new Comparator<PlaceReviewItem>() {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1, date2;

                @Override
                public int compare(PlaceReviewItem o1, PlaceReviewItem o2) {
                    try {
                        date1 = format.parse(o1.getTimeCreated());
                        date2 = format.parse(o2.getTimeCreated());
                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 15;
                }
            });
            googleReviewAdapter = new ReviewItemViewAdapter(context, googleReviews);
//            googleReviewAdapter.swapData(googleReviews);
            googleReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Google reviews"))
                recyclerView.setAdapter(googleReviewAdapter);
        }
        if (yelpReviews != null) {
            Collections.sort(yelpReviews, new Comparator<PlaceReviewItem>() {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1, date2;

                @Override
                public int compare(PlaceReviewItem o1, PlaceReviewItem o2) {
                    try {
                        date1 = format.parse(o1.getTimeCreated());
                        date2 = format.parse(o2.getTimeCreated());
                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 15;
                }
            });
            yelpReviewAdapter = new ReviewItemViewAdapter(context, yelpReviews);
//            yelpReviewAdapter.swapData(yelpReviews);
            yelpReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Yelp reviews"))
                recyclerView.setAdapter(yelpReviewAdapter);
        }
    }

    private void sortLeastRecent() {
        if (googleReviews != null) {
            Collections.sort(googleReviews, new Comparator<PlaceReviewItem>() {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1, date2;

                @Override
                public int compare(PlaceReviewItem o1, PlaceReviewItem o2) {
                    try {
                        date1 = format.parse(o1.getTimeCreated());
                        date2 = format.parse(o2.getTimeCreated());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 15;
                }
            });
            googleReviewAdapter = new ReviewItemViewAdapter(context, googleReviews);
//            googleReviewAdapter.swapData(googleReviews);
            googleReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Google reviews"))
                recyclerView.setAdapter(googleReviewAdapter);
        }
        if (yelpReviews != null) {
            Collections.sort(yelpReviews, new Comparator<PlaceReviewItem>() {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1, date2;

                @Override
                public int compare(PlaceReviewItem o1, PlaceReviewItem o2) {
                    try {
                        date1 = format.parse(o1.getTimeCreated());
                        date2 = format.parse(o2.getTimeCreated());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 15;
                }
            });
            yelpReviewAdapter = new ReviewItemViewAdapter(context, yelpReviews);
//            yelpReviewAdapter.swapData(yelpReviews);
            yelpReviewAdapter.notifyDataSetChanged();
            if (getCurrentReviewType().equals("Yelp reviews"))
                recyclerView.setAdapter(yelpReviewAdapter);
        }
    }


    public void setCurrentReviewType(String s) { this.currentReviewType = s; }
    public String getCurrentReviewType() { return currentReviewType; }

    public void setGoogleReviewOrder(String s) { this.googleReviewOrder = s; }
    public String getGoogleReviewOrder() { return googleReviewOrder; }

    public void setYelpReviewOrder(String s) { this.yelpReviewOrder = s; }
    public String getYelpReviewOrder() { return yelpReviewOrder; }

    private class GetGoogleReviews extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String... strings) {
            String authorName, url, profilePic, reviewText, reviewTimeText;
            double rating, reviewTime;
            JsonObject reviewItem;
            if (reviewJSON != null) {
                parser = new JsonParser();
                JsonObject gReviews = parser.parse(reviewJSON).getAsJsonObject();
                if (gReviews.has("reviews")) {
                    JsonArray reviews = gReviews.getAsJsonArray("reviews");
                    for (int i = 0; i < reviews.size(); i++ ) {
                        reviewItem = reviews.get(i).getAsJsonObject();
                        if (reviewItem.has("author_name")) {
                            authorName = reviewItem.get("author_name").getAsString();
                            Log.d(TAG, "Author Name: " + authorName);
                        }
                        else{
                            authorName = "No Author Name";
                        }
                        if (reviewItem.has("author_url"))
                            url = reviewItem.get("author_url").getAsString();
                        else
                            url = "";
                        if (reviewItem.has("profile_photo_url")) {
                            profilePic = reviewItem.get("profile_photo_url").getAsString();
                            Log.d(TAG, profilePic);
                        }
                        else {
                            profilePic = "https://www.publicdomainpictures.net/view-image.php?image=28763";
                        }
                        if (reviewItem.has("rating"))
                            rating = reviewItem.get("rating").getAsDouble();
                        else
                            rating = 0;
                        if (reviewItem.has("text"))
                            reviewText = reviewItem.get("text").getAsString();
                        else
                            reviewText = "No review text";
                        if (reviewItem.has("time")) {
                            reviewTime = reviewItem.get("time").getAsDouble();
                            // we need to parse the time object from Unix timestamp to specified format of
                            // YYYY-MM-DD HH-MM-SS (24 hour time period)
                            // TODO: Parse as normally until we run first check. If needed use UTC offset
                            // Convert the unix timestamp into milliseconds
                            long timestamp = (long) reviewTime * 1000L;
                            //reviewTimeText = "";
                            try {
                                DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date netDate = (new Date(timestamp));
                                reviewTimeText = date.format(netDate).toString();
                            } catch (Exception e) {
                                Log.d(TAG, "Error:" + e.toString());
                                reviewTimeText = "Error";
                            }
                        } else {
                            reviewTimeText = "No Review Time Found!";
                            reviewTime = 0;
                        }
                        // Create new PlaceReviewItem object
                        PlaceReviewItem item = new PlaceReviewItem(authorName, url, profilePic, rating, reviewText, reviewTimeText);
                        googleReviews.add(item);
                        defaultGoogleReviews.add(item);
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Alrighty update the views
            // with no code because adapter lul.
            if (googleReviewAdapter != null)
                googleReviewAdapter.notifyDataSetChanged();
        }
    }

    private class GetYelpReviews extends  AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            // just parse the reviews
            if (yelpReviewJSON != null) {
                JsonObject reviews = parser.parse(yelpReviewJSON).getAsJsonObject();
                if (reviews.has("reviews")) {
                    JsonArray array = reviews.get("reviews").getAsJsonArray();
                    if (array.size() == 0) {
                        hasYelpReviews = false;
                    } else {
                        for (int i = 0; i < array.size(); i++) {
                            JsonObject review = array.get(i).getAsJsonObject();

                            if (review.has("url"))
                                url = review.get("url").getAsString();
                            if (review.has("text"))
                                text = review.get("text").getAsString();
                            if (review.has("rating"))
                                rating = review.get("rating").getAsDouble();
                            if (review.has("time_created"))
                                timeCreated = review.get("time_created").getAsString();
                            if (review.has("user")) {
                                JsonObject user = review.get("user").getAsJsonObject();
                                if (user.has("image_url")) {
                                    if (user.get("image_url") != null) {
                                        profilePic = user.get("image_url").getAsString();
                                    } else {
                                        profilePic = "https://www.publicdomainpictures.net/view-image.php?image=28763";
                                    }
                                    if (user.has("name"))
                                        authorName = user.get("name").getAsString();
                                }
                            }
                            PlaceReviewItem yelpItem = new PlaceReviewItem(authorName, url, profilePic, rating, text, timeCreated);
                            yelpReviews.add(yelpItem);
                            defaultYelpReviews.add(yelpItem);
                        }
                        hasYelpReviews = true;
                    }
                } else {
                    hasYelpReviews = false;
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (hasYelpReviews == false)
                noReviewMessage.setVisibility(View.VISIBLE);
            if (yelpReviewAdapter != null)
                yelpReviewAdapter.notifyDataSetChanged();
        }
    }
}
