package com.csci571.koy.hw9.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.adapter.SectionsPagerAdapter;
import com.csci571.koy.hw9.fragments.InfoFragment;
import com.csci571.koy.hw9.fragments.MapFragment;
import com.csci571.koy.hw9.fragments.PhotoFragment;
import com.csci571.koy.hw9.fragments.ReviewsFragment;

import com.csci571.koy.hw9.interfaces.SharedPreference;
import com.csci571.koy.hw9.model.SearchResultItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by koyst on 4/19/2018.
 */

public class PlacesDetailActivity extends AppCompatActivity {

    private static final String TAG = "PlaceDetailsActivity";
    private static final String SERVER = "http://hw8-express.appspot.com/api";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Intent placeDetailIntent;

    // incase I want to use bundles
    private Bundle infoBundle, photoBundle, mapBundle, reviewBundle;

    // location service variables
    private BroadcastReceiver broadcastReceiver;
    private String myLat, myLng;
    private TextView errorMessageView;
    private String placeDetailsJSON;
    private Gson jsonParser;
    private JsonParser parser;
    private ProgressDialog progressDialog;
    private Context context;
    private RequestQueue httpQueue;
    private SharedPreference favorites;

    private InfoFragment infoFragment;
    private PhotoFragment photoFragment;
    private MapFragment mapFragment;
    private ReviewsFragment reviewsFragment;
    private ImageView twitterButton, favoritesButton;

    private double placeLat, placeLng;
    private String errorMessage, placeId, iconUrl, placeLatS, placeLngS, placeName, placePhone = "empty";

    private List<SearchResultItem> favoritesList;

    // yelp search Strings
    private String address, city, state, postal_code, country, phoneNum, vicinity, website;
    private String yelpBusinessJSON, yelpReviewsJSON, yelpId;
    private boolean hasYelpReviews;
    private boolean isFavorited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_detail);

        errorMessageView = findViewById(R.id.errorMessage);
        infoFragment = new InfoFragment();
        photoFragment = new PhotoFragment();
        mapFragment = new MapFragment();
        reviewsFragment = new ReviewsFragment();

        context = this;
        httpQueue = Volley.newRequestQueue(context);
        jsonParser = new Gson();
        parser = new JsonParser();
        placeDetailIntent = getIntent();

        if (placeDetailIntent != null) {
            if (placeDetailIntent.getExtras().get("JSON") != null)
                setPlacesDetailsJSON(placeDetailIntent.getExtras().get("JSON").toString());

            if (placeDetailIntent.getExtras().get("my_lat") != null) {
                setLatitude(placeDetailIntent.getExtras().get("my_lat").toString());
            }

            if (placeDetailIntent.getExtras().get("my_lat") != null) {
                setLongitude(placeDetailIntent.getExtras().get("my_lng").toString());
            }
            if (placeDetailIntent.getExtras().get("vicinity")!= null) {
                setVicinity(placeDetailIntent.getExtras().get("vicinity").toString());
            }
            if (placeDetailIntent.getExtras().get("place_id") != null)
                placeId = placeDetailIntent.getExtras().get("place_id").toString();
            if (placeDetailIntent.getExtras().get("icon_url") != null)
                iconUrl = placeDetailIntent.getExtras().get("icon_url").toString();
            if (placeDetailIntent.getExtras().get("place_lat") != null)
                placeLngS = placeDetailIntent.getExtras().get("place_lat").toString();
            if (placeDetailIntent.getExtras().get("place_lng") != null)
                placeLngS = placeDetailIntent.getExtras().get("place_lng").toString();

            errorMessageView.setVisibility(View.GONE);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (placeDetailIntent.getExtras().get("place_name") != null) {
            placeName = placeDetailIntent.getExtras().get("place_name").toString();
            toolbar.setTitle(placeName);
        }
        // sets the support action bar for a back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        twitterButton = (ImageView) findViewById(R.id.shareImg);
        favoritesButton = (ImageView) findViewById(R.id.favoriteImg);
        favorites = new SharedPreference();
        favoritesList = favorites.getFavorites(this);
        if (favoritesList != null) {
            if (favoritesList.size() > 0) { // we have a favorites item run check.
                for (int i = 0; i < favoritesList.size(); i++) {
                    if (favoritesList.get(i).getPlaceId().equals(placeId)) {
                        // set drawable to white heart
                        favoritesButton.setImageResource(R.drawable.heart_fill_white);
                        break;
                    } else {
                        favoritesButton.setImageResource(R.drawable.heart_outline_black);
                    }
                }
            }
        }
        if (placeDetailsJSON != null) {
            final JsonParser parser = new JsonParser();
            final JsonObject rootJSON = parser.parse(placeDetailsJSON).getAsJsonObject();
            if (rootJSON.has("result")) {
                final JsonObject resultJSON = rootJSON.getAsJsonObject("result");
                if (resultJSON.has("website"))
                    website = resultJSON.get("website").getAsString();
            }
        }

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String twitterText = "Check out " + placeName + " located at " + vicinity +" Website: " + website;
                String url = "https://twitter.com/intent/tweet?text="+twitterText;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoritesList != null) {
                    final SearchResultItem thisItem = new SearchResultItem(iconUrl, placeName, placeId, vicinity, placeLatS, placeLngS, false);
                    if (!checkFavoriteItem(thisItem)) {
                        favorites.addFavorite(context, thisItem);
                        favoritesButton.setImageResource(R.drawable.heart_fill_white);
                        Toast.makeText(context, ""+thisItem.getPlaceName()+", added to Favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        favorites.removeFavorite(context, thisItem);
                        favoritesButton.setImageResource(R.drawable.heart_outline_white);
                        Toast.makeText(context, ""+thisItem.getPlaceName()+", removed to Favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        viewPager = (ViewPager) findViewById(R.id.container);
        // Defines the number of tabs by setting appropriate fragment and tab name.
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        // Assigns the ViewPager to TabLayout.
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        setupTabIcons();

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setSize(1,1);
        linearLayout.setDividerPadding(10);
        linearLayout.setDividerDrawable(drawable);

        // parse the JSON results in a new thread
        // create a progress dialog.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Details");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                parseJSON(getPlacesDetailJSON());
            }
        }).start();
        stopService(placeDetailIntent);
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

    private void setupTabIcons() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setGravity(Gravity.CENTER);
        tabOne.setWidth(width/4);
        tabOne.setText("INFO");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.info_outline, 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setGravity(Gravity.CENTER);
        tabTwo.setWidth(width/4);
        tabTwo.setText("PHOTOS");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.photos,0,0,0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setGravity(Gravity.CENTER);
        tabThree.setWidth(width/4);
        tabThree.setText("MAP");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.maps,0,0,0);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        TextView tabFour = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabFour.setGravity(Gravity.CENTER);
        tabFour.setWidth(width/4);
        tabFour.setText("REVIEWS");
        tabFour.setCompoundDrawablesWithIntrinsicBounds(R.drawable.review,0,0,0);
        tabLayout.getTabAt(3).setCustomView(tabFour);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(infoFragment, "INFO");
        adapter.addFragment(photoFragment, "PHOTOS");
        adapter.addFragment(mapFragment, "MAP");
        adapter.addFragment(reviewsFragment, "REVIEWS");
        viewPager.setAdapter(adapter);
    }

    public boolean onOptionItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void parseJSON (String json) {
        jsonParser = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject rootJSON = parser.parse(json).getAsJsonObject();
        String status_message = rootJSON.get("status").getAsString();
        Log.d(TAG, "Status Message: " + status_message);

        if (status_message.equals("ZERO_RESULTS")) {            // no items
            setErrorMessage("No Results Found");
            displayErrorMessage();
            // Should set the view to display the message.
        } else if (status_message.equals("OVER_QUERY_LIMIT")) { // over limit
            setErrorMessage("Over allocated quota for searches");
            displayErrorMessage();
            // Should set the view to display the message.
        } else if (status_message.equals("REQUEST_DENIED")) { // denied
            setErrorMessage("Request Denied");
            displayErrorMessage();
            // Should set the view to display the message.
        } else if (status_message.equals("INVALID_REQUEST")) { //invalid request
            setErrorMessage("Invalid Request, check your search parameters");
            displayErrorMessage();
            // Should set the view to display the message.
        } else if (status_message.equals("UNKNOWN_ERROR")) { //unknown error
            setErrorMessage("Unknown Error, Something exploded on the server!");
            displayErrorMessage();
            // Should set the view to display the message.
        }
        else {
            // no error
            if (rootJSON.has("result")) {
                final JsonObject resultJSON = rootJSON.getAsJsonObject("result");

                // Parse Json for the info fragment!
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        parseInfoFragment(resultJSON);

                        // Parse Json for the photos fragment
                        parsePhotoFragment(resultJSON);

                        // Parse Map stuff
                        parseMapFragment(resultJSON);

                        // parse Json for the reviews fragment
                        parseReviewsFragment(resultJSON);
                    }
                }).start();

                // TODO: Access Saved Favorites List to see if the place is already in the favorites
                // temporarily set the isFavorite to false.  What we want to do here is to access
                // our Favorites list to see if there is a favorite with the same place id and if there is set it to true!
                boolean isFavorite = false;
            }
        }
    }

    public void parseInfoFragment(JsonObject j) {
        String formatted_address = "", phone_num = "", google_page = "", website = "";
        String price_level ="" , rating= "";

        infoBundle = new Bundle();

        if (j.has("formatted_address")) {
            formatted_address = j.get("formatted_address").getAsString();
            infoBundle.putString("formatted_address", formatted_address);
        }
        if (j.has("formatted_phone_number")) {
            phone_num = j.get("formatted_phone_number").getAsString();
            infoBundle.putString("formatted_phone_number", phone_num);
        }
        if (j.has("url")) {
            google_page = j.get("url").getAsString();
            infoBundle.putString("google_page", google_page);
        }
        if (j.has("website")) {
            website = j.get("website").getAsString();
            infoBundle.putString("website", website);
        }
        if (j.has("price_level")) {
            price_level = j.get("price_level").getAsString();
            infoBundle.putString("price", price_level);
        }
        if (j.has("rating")) {
            rating = j.get("rating").getAsString();
            infoBundle.putString("rating", rating);
        }
//        startActivity(infoIntent);
        infoFragment.setArguments(infoBundle);
    }

    public void parsePhotoFragment(JsonObject j) {
        String placeID = "";
        photoBundle = new Bundle();

        if (j.has("photos")) {
            photoBundle.putBoolean("has_photos", true);
            JsonArray photosArray = j.getAsJsonArray("photos");
            if (j.has("place_id")) {
                placeID = j.get("place_id").getAsString();
                photoBundle.putString("place_id", placeID);
            }
            photoFragment.setArguments(photoBundle);

        } else { // no photos
            photoBundle.putBoolean("has_photos", false);
            photoBundle.putString("place_id", placeID);
            photoFragment.setArguments(photoBundle);
        }
    }

    public void parseMapFragment(JsonObject j) {
        // TODO: make sure we get our current location from the searchFormFragment and pass it to the searchResultActivity and pass it into here.
        mapBundle = new Bundle();

        if (j.has("geometry")) {
            JsonObject locationObject = j.get("geometry").getAsJsonObject().get("location").getAsJsonObject();
            Double place_lat = locationObject.get("lat").getAsDouble();
            Double place_lng = locationObject.get("lng").getAsDouble();
            placeLat = place_lat;
            placeLng = place_lng;
            mapBundle.putString("place_name", placeName);
            mapBundle.putDouble("place_lat", place_lat);
            mapBundle.putDouble("place_lng", place_lng);

//            startActivity(mapIntent);
            mapFragment.setArguments(mapBundle);

            Thread progress= new Thread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
            try {
                progress.sleep(3000);
                progress.start();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void parseReviewsFragment(JsonObject j) {
        reviewBundle = new Bundle();


        // parse the address components for the yelp reviews
        if (j.has("address_components")) {
            JsonArray addr_comps = j.getAsJsonArray("address_components");
            String streetNum = "empty";
            String streetName = "empty";
            for (int i = 0; i < addr_comps.size(); i++) {

                JsonObject addr_comp = addr_comps.get(i).getAsJsonObject();

                if (addr_comp.has("types")) {
                    JsonArray typesArray = addr_comp.get("types").getAsJsonArray();
                    // always check the first position of the array
                    if (typesArray.get(0).getAsString().equals("street_number")) {
                        streetNum = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("street_num", streetNum);
                    }
                    else if (typesArray.get(0).getAsString().equals("route")) {
                        streetName = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("street_name", streetName);
                    }
                    else if (typesArray.get(0).getAsString().equals("locality")) {
                        city = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("city", city);
                    }
                    else if (typesArray.get(0).getAsString().equals("administrative_area_level_1")) {
                        state = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("state", state);
                    }
                    else if (typesArray.get(0).getAsString().equals("country")) {
                        country = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("country", country);
                    }
                    else if (typesArray.get(0).getAsString().equals("postal_code")) {
                        postal_code = addr_comp.get("short_name").getAsString();
                        reviewBundle.putString("postal_code", postal_code);
                    }
                }
            }
            if (streetNum.equals("empty") && streetName.equals("empty"))
                address = city;
            else if (!streetName.equals("empty"))
                address = streetName;
            else
                address = streetNum + " " + streetName;
        }

        if (j.has("international_phone_number"))
            placePhone = j.get("international_phone_number").getAsString();

        reviewBundle.putString("place_phone", placePhone);
        reviewBundle.putString("place_name", placeName);
        reviewBundle.putDouble("place_lat", placeLat);
        reviewBundle.putDouble("place_lmg", placeLng);

        // check to see if there are google reviews
        if (j.has("reviews")) {
            reviewBundle.putBoolean("google_reviews", true);
            // send the reviews json data to the reviews fragment!
            reviewBundle.putString("reviewJSON", j.toString());
        }
        else {
            // no reviews
            reviewBundle.putBoolean("google_reviews", false);
        }
//        startActivity(reviewIntent);
        // parse yelp reviews?
        GetYelpBusiness getYelp = new GetYelpBusiness();
        getYelp.execute();
        reviewsFragment.setArguments(reviewBundle);
    }

    public void displayErrorMessage() {
        viewPager.setVisibility(View.GONE);
        errorMessageView.setText(getErrorMessage());
        errorMessageView.setVisibility(View.VISIBLE);
    }

    public void setErrorMessage(String _e) { this.errorMessage = _e; }

    public String getErrorMessage() { return errorMessage; }

    public void setLatitude(String _lat) { this.myLat = _lat; }

    public String getLatitude() { return myLat; }

    public void setLongitude(String _lng) { this.myLng = _lng; }

    public String getLongitude() { return myLng; }

    public void setVicinity(String p) { this.vicinity = p; }

    public String getVicinity() { return vicinity; }

    public String getPlacesDetailJSON() { return placeDetailsJSON; }

    public void setPlacesDetailsJSON(String _s) { this.placeDetailsJSON = _s; }

    public void dismissDialog() {
        this.progressDialog.dismiss();
    }

    private class GetYelpBusiness extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            // Perform a Yelp Business Search
            String reference = "/yelp/search/business/name/:name/address/:address/city/:city/state/:state/country/:country/lat/:lat/lon/:lon/phone/:phone/zip/:zip";
            String yelpBusinessSearch = "/yelp/search/business";
            String name = "/name/" + placeName;
            String address_ = "/address/"+address;
            String city_ = "/city/"+city;
            String state_ = "/state/"+state.toUpperCase();
            String country_="/country/"+country;
            String lat = "/lat/"+placeLat;
            String lng = "/lon/"+placeLng;
            String phone = "/phone/"+placePhone;
            String zip = "/zip/"+postal_code;
            String searchURL = SERVER+yelpBusinessSearch+name+address_+city_+state_+country_+lat+lng+phone+zip;

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            yelpBusinessJSON = response.toString();
                            reviewBundle.putString("yelpB", yelpBusinessJSON);
                            getYelpReviews(yelpBusinessJSON);
                            reviewsFragment.setArguments(reviewBundle);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", ""+error.getMessage());
                        }
                    }
            );

            // add it to the RequestQueue
            httpQueue.add(getRequest);

            return null;
        }
    }

    private boolean getYelpReviews(String json) {
        if (json != null) {
            JsonObject object = parser.parse(json).getAsJsonObject();
            if (object.has("businesses")) {
                JsonArray array = object.get("businesses").getAsJsonArray();
                if (array.size() == 0) {
                    hasYelpReviews = false;
                    return false;
                } else {
                    hasYelpReviews = true;
                    JsonObject o = array.get(0).getAsJsonObject();
                    String id = o.get("id").getAsString();
                    // make http request call
                    String searchURL = SERVER+"/yelp/search/reviews/id/"+id;

                    JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    yelpReviewsJSON = response.toString();
                                    reviewBundle.putString("yelpReviews", yelpReviewsJSON);
                                    reviewsFragment.setArguments(reviewBundle);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Error.Response", ""+error.getMessage());
                                }
                            }
                    );
                    httpQueue.add(getRequest);
                    return true;
                }
            }
        }
        return false;
    }
}
