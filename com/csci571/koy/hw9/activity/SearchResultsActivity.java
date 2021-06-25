package com.csci571.koy.hw9.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.adapter.SearchItemRecyclerViewAdapter;
import com.csci571.koy.hw9.fragments.SearchFormFragment;
import com.csci571.koy.hw9.model.SearchResultItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "Search Results Activity";
    private static final String SERVER = "http://hw8-express.appspot.com/api";

    private Intent searchForm;
    private RecyclerView recyclerView;
    public SearchItemRecyclerViewAdapter searchItemRecyclerViewAdapter, pageTwoAdapter, pageThreeAdapter;
    private Button previousPageBtn, nextPageBtn;
    private TextView errorMessageView;
    private ProgressDialog progressDialog;
    private Context context;

    private String searchResultsJSON, pageTwoJSON, pageThreeJSON;
    private int pageNum;
    private String next_token;
    private String errorMessage;
    private boolean hasNextPage, cameFromPreviousPage, hasThirdPage, pageTwoLoaded = false, pageThreeLoaded = false;
    private Gson jsonParser;
    private String myLat, myLng;

    private List<SearchResultItem> searchResultItemList = new ArrayList<>();
    private List<SearchResultItem> searchResultPageTwo = new ArrayList<>();
    private List<SearchResultItem> searchResultPageThree = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the View
        setContentView(R.layout.activity_search_results);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchForm = getIntent();
        context = this;
        if (searchForm != null) {
            if (searchForm.getExtras().get("JSON") != null) {
                setSearchResultsJSON(searchForm.getExtras().get("JSON").toString());
            }

            if (searchForm.getExtras().get("MY_LAT") != null && searchForm.getExtras().get("MY_LNG") != null) {
                setMyLat(searchForm.getExtras().get("MY_LAT").toString());
                setMyLng(searchForm.getExtras().get("MY_LNG").toString());
            }

            // if we have a geocode location then it was a custom search without autocomplete
            if (searchForm.getExtras().get("geo") != null)
                parseGeo(searchForm.getExtras().get("geo").toString());
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        searchItemRecyclerViewAdapter = new SearchItemRecyclerViewAdapter(this, searchResultItemList);
        pageTwoAdapter = new SearchItemRecyclerViewAdapter(this, searchResultPageTwo);
        pageThreeAdapter = new SearchItemRecyclerViewAdapter(this, searchResultPageThree);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(searchItemRecyclerViewAdapter);

        errorMessageView = (TextView) findViewById(R.id.errorMessage);
        errorMessageView.setVisibility(View.GONE);
        pageNum = 1;
        previousPageBtn = (Button) findViewById(R.id.prev_page_btn);
        nextPageBtn = (Button) findViewById(R.id.next_page_btn);

        previousPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPreviousPage();
            }
        });

        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextPage();
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parseJSON(getSearchResultsJSON());
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(searchForm);

    }
    // setup the back button
    public boolean onOptionItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
//                Intent openMainActivity = new Intent(getApplicationContext(), MainActivity.class);
//                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivityIfNeeded(openMainActivity, 0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void parseGeo (String _geo) {
        jsonParser = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject rootJSON = parser.parse(_geo).getAsJsonObject();
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
        } else {
            // get our latitude and longitude
            if (rootJSON.has("results")) {
                JsonArray results = rootJSON.getAsJsonArray("results");
                JsonObject result = results.get(0).getAsJsonObject();
                if (result.has("geometry")) {
                    JsonObject geo = result.get("geometry").getAsJsonObject();
                    if (geo.has("location")) {
                        JsonObject location = geo.get("location").getAsJsonObject();
                        String lat = location.get("lat").getAsString();
                        String lng = location.get("lng").getAsString();
                        setMyLat(lat);
                        setMyLng(lng);
                    }
                }
            }
        }
    }

    private void parseJSON(String json) {
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
        } else {
            // go ahead and parse the rest of the JSON array and get all the stuffs we need.
            if (rootJSON.has("results")) {

                JsonArray resultsJSON = rootJSON.getAsJsonArray("results");

                // check for next page token
                if (rootJSON.has("next_page_token")) {
                    setNextPageToken(rootJSON.get("next_page_token").getAsString());
                    nextPageBtn.setEnabled(true);
                    hasNextPage = true;
                }
                for (int i = 0; i < resultsJSON.size(); i++ ) {
                    JsonObject r = resultsJSON.get(i).getAsJsonObject();
                    String icon = "", name = "", placeId = "", vicinity = "";
                    if (r.has("icon"))
                        icon = r.get("icon").getAsString();
                    if (r.has("name"))
                        name = r.get("name").getAsString();
                    if (r.has("place_id"))
                        placeId = r.get("place_id").getAsString();
                    if (r.has("vicinity"))
                        vicinity = r.get("vicinity").getAsString();
                    // TODO: Access Saved Favorites List to see if the place is already in the favorites

                    // temporarily set the isFavorite to false.  What we want to do here is to access
                    // our Favorites list to see if there is a favorite with the same place id and if there is set it to true!
                    boolean isFavorite = false;

                    SearchResultItem item = new SearchResultItem(icon, name, placeId, vicinity, getMyLat(), getMyLng(), isFavorite);
                    searchResultItemList.add(item);
                }
            }
        }
        if (searchItemRecyclerViewAdapter != null) {
            searchItemRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    // TODO: Implement the previous page no loading spinner
    public void getPreviousPage() {
        /** TODO:  Might need to create a new searchItemRecyclerViewAdapters for each page.
         *  TODO:  In order to avoid double HTTP calls.
         *  Just create a new adapter and then swap adapters on page calls.
         */
        if (searchItemRecyclerViewAdapter != null && pageTwoAdapter != null && pageThreeAdapter != null) {
            if (pageNum == 3) {
                nextPageBtn.setEnabled(true);
                previousPageBtn.setEnabled(true);
                recyclerView.setAdapter(pageTwoAdapter);
            }
            else if (pageNum == 2) {
                nextPageBtn.setEnabled(true);
                previousPageBtn.setEnabled(false);
                recyclerView.setAdapter(searchItemRecyclerViewAdapter);
            }
            else {
                // we shouldn't be here at all.
            }
        } else if (searchItemRecyclerViewAdapter != null && pageTwoAdapter != null) {
            if (pageNum == 2) {
                recyclerView.setAdapter(searchItemRecyclerViewAdapter);
                nextPageBtn.setEnabled(true);
                previousPageBtn.setEnabled(false);

            }
        }
        pageNum--;
    }

    // TODO: Implement the next page loading spinner
    public void getNextPage() {
        // TODO: Try and implement ASYNC Task to get the JSON?
        // Create a new adapter and then swap adapters on page calls.
        if (pageTwoLoaded == false) {
            // Only load progress dialog first time.
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Fetching results");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // execute the task
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    GetNextPageTask getNextPageTask = new GetNextPageTask();
                    getNextPageTask.execute();
                }
            }, 2000);
        }
        else if (pageTwoLoaded == true && pageThreeLoaded == false && pageNum == 2) {
            // Only load progress dialog first time.
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Fetching results");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // execute the task
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    GetThirdPageTask getThirdPageTask = new GetThirdPageTask();
                    getThirdPageTask.execute();
                }
            }, 2000);
        }
        else {
            // on run the async task if it has not been run before!
            if (pageThreeAdapter != null && pageNum == 2) {
                nextPageBtn.setEnabled(false);
                previousPageBtn.setEnabled(true);
                recyclerView.setAdapter(pageThreeAdapter);
            } else if (pageTwoAdapter != null && pageNum == 1 && hasThirdPage) {
                nextPageBtn.setEnabled(true);
                previousPageBtn.setEnabled(true);
                recyclerView.setAdapter(pageTwoAdapter);
            } else if (pageTwoAdapter != null && pageNum ==1 && !hasThirdPage) {
                nextPageBtn.setEnabled(false);
                previousPageBtn.setEnabled(true);
                recyclerView.setAdapter(pageTwoAdapter);
            }
        }
        pageNum++;
    }

    public void displayErrorMessage() {
        recyclerView.setVisibility(View.GONE);
        errorMessageView.setText(getErrorMessage());
        errorMessageView.setVisibility(View.VISIBLE);
    }

    public String getSearchResultsJSON() {
        return searchResultsJSON;
    }
    public void setSearchResultsJSON(String _s) {
        this.searchResultsJSON = _s;
    }

    public boolean getHasNextPage() { return hasNextPage; }
    public void setHasNextPage(boolean _t) { this.hasNextPage = _t; }

    public String getNextPageToken() { return next_token; }
    public void setNextPageToken(String _t) { this.next_token = _t; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String _e) { this.errorMessage = _e; }

    public String getMyLat() { return myLat; }
    public void setMyLat(String _lat) { this.myLat = _lat; }

    public String getMyLng() { return myLng; }
    public void setMyLng(String _lng) { this.myLng = _lng; }


    // Use this class to parse the JSON for the page calls.
    private class GetNextPageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... strings) {
            // get the next page JSON and parse it.
            String nextPage = "/places/search/token/"+getNextPageToken();
            String searchURL = SERVER+nextPage;

            RequestQueue httpQueue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pageTwoJSON = response.toString();
                            parseNextPageJSON(pageTwoJSON);

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
            return null;
        }

        //
        public void parseNextPageJSON(String json) {
            jsonParser = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject rootJSON = parser.parse(json).getAsJsonObject();
            String status_message = rootJSON.get("status").getAsString();
            Log.d(TAG, "Next Page JSON: " + json);
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
            } else {
                // go ahead and parse the rest of the JSON array and get all the stuffs we need.
                if (rootJSON.has("results")) {

                    JsonArray resultsJSON = rootJSON.getAsJsonArray("results");

                    // check for next page token
                    if (rootJSON.has("next_page_token")) {
                        setNextPageToken(rootJSON.get("next_page_token").getAsString());
                        cameFromPreviousPage = true;
                        hasThirdPage = true;

                    }
                    else {
                        cameFromPreviousPage = true;
                        hasThirdPage = false;
                    }

                    for (int i = 0; i < resultsJSON.size(); i++ ) {
                        JsonObject r = resultsJSON.get(i).getAsJsonObject();
                        String icon = "", name = "", placeId = "", vicinity = "";
                        if (r.has("icon"))
                            icon = r.get("icon").getAsString();
                        if (r.has("name"))
                            name = r.get("name").getAsString();
                        if (r.has("place_id"))
                            placeId = r.get("place_id").getAsString();
                        if (r.has("vicinity"))
                            vicinity = r.get("vicinity").getAsString();
                        // TODO: Access Saved Favorites List to see if the place is already in the favorites

                        // temporarily set the isFavorite to false.  What we want to do here is to access
                        // our Favorites list to see if there is a favorite with the same place id and if there is set it to true!
                        boolean isFavorite = false;

                        SearchResultItem item = new SearchResultItem(icon, name, placeId, vicinity, getMyLat(), getMyLng(), isFavorite);

                        searchResultPageTwo.add(item);
                    }
                    pageTwoLoaded = true;

                    if (cameFromPreviousPage && hasThirdPage) {
                        nextPageBtn.setEnabled(true);
                        previousPageBtn.setEnabled(true);
                    } else if (!hasThirdPage) {
                        nextPageBtn.setEnabled(false);
                        previousPageBtn.setEnabled(true);
                    }
                }
                if (pageTwoAdapter != null) {
                    pageTwoAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            recyclerView.setAdapter(pageTwoAdapter);
            // execute the task
            // depending on response from the HTTP server
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 1000);
        }
    }
    private class GetThirdPageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... strings) {
            // get the next page JSON and parse it.
            String nextPage = "/places/search/tokenTwo/"+getNextPageToken();
            String searchURL = SERVER+nextPage;

            RequestQueue httpQueue = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pageThreeJSON = response.toString();
                            parseNextPageJSON(pageThreeJSON);

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
            return null;
        }

        //
        public void parseNextPageJSON(String json) {
            jsonParser = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject rootJSON = parser.parse(json).getAsJsonObject();
            String status_message = rootJSON.get("status").getAsString();
            Log.d(TAG, "Next Page JSON: " + json);
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
            } else {
                // go ahead and parse the rest of the JSON array and get all the stuffs we need.
                if (rootJSON.has("results")) {

                    JsonArray resultsJSON = rootJSON.getAsJsonArray("results");

                    // check for next page token
                    if (rootJSON.has("next_page_token")) {
                        setNextPageToken(rootJSON.get("next_page_token").getAsString());
                        cameFromPreviousPage = true;
                        hasThirdPage = true;
                    }
                    else {
                        cameFromPreviousPage = true;
                    }

                    for (int i = 0; i < resultsJSON.size(); i++ ) {
                        JsonObject r = resultsJSON.get(i).getAsJsonObject();
                        String icon = "", name = "", placeId = "", vicinity = "";
                        if (r.has("icon"))
                            icon = r.get("icon").getAsString();
                        if (r.has("name"))
                            name = r.get("name").getAsString();
                        if (r.has("place_id"))
                            placeId = r.get("place_id").getAsString();
                        if (r.has("vicinity"))
                            vicinity = r.get("vicinity").getAsString();
                        // TODO: Access Saved Favorites List to see if the place is already in the favorites

                        // temporarily set the isFavorite to false.  What we want to do here is to access
                        // our Favorites list to see if there is a favorite with the same place id and if there is set it to true!
                        boolean isFavorite = false;

                        SearchResultItem item = new SearchResultItem(icon, name, placeId, vicinity, getMyLat(), getMyLng(), isFavorite);

                        searchResultPageThree.add(item);
                    }
                    pageThreeLoaded = true;
                }
                if (pageThreeAdapter != null) {
                    pageThreeAdapter.notifyDataSetChanged();

                }
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            recyclerView.setAdapter(pageThreeAdapter);
            nextPageBtn.setEnabled(false);
            previousPageBtn.setEnabled(true);

            // execute the task
            // depending on response from the HTTP server
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 1000);
        }
    }

}
