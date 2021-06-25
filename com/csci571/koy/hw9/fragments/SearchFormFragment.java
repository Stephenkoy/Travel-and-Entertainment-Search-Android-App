package com.csci571.koy.hw9.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.text.Html;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.activity.SearchResultsActivity;
import com.csci571.koy.hw9.adapter.PlaceArrayAdapter;
import com.csci571.koy.hw9.model.SearchResultItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;

/**
 * Created by koyst on 4/16/2018.
 */

/**
 * Code Snippets Borrowed From: Ravi Tamada
 * https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
 *
 * Some Code Ideas used from Mitch Tabian
 * https://github.com/mitchtabian/TabFragments/tree/master/TabFragments
 */
public class SearchFormFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "Search Form Fragment";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final String API_KEY = "AIzaSyBbLH6p0js-mnX1Pj4jHY7Ui6jfY0su4_w";
    private static final String NODE_JS_API_KEY = "AIzaSyDcfR8nWZRzrGF2wov2vWNApMPvRu_UCKU";
    private static final String SERVER_URL = "http://hw8-express.appspot.com/api";

    private List<SearchResultItem> searchResults;

    private Button searchBTN;
    private Button clearBTN;
    private HashMap<String, String> categoryList = new HashMap<>();
    private TextView keywordInput, distanceInput, customLocationInput, keywordError, customLocationInputError;
    private Spinner categorySelected;
    private boolean customSearch = false;
    private boolean autocompleteSelected = false;
    private RadioButton currentLocation;
    private RadioButton otherLocation;
    private RadioGroup locationGroup;
    private ProgressDialog progressDialog;
    private String keyword, category, customLocation, fieldError;
    private String  jsonObject = "";
    private String locationSearchType = "current_location";
    private double distance;
    private Place place;

    // autocomplete vars
    private AutoCompleteTextView mAutoCompleteTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private Intent i;
    private double myLat, myLng, customLat, customLng;

    public SearchFormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            myLat = getArguments().getDouble("MY_LAT");
            myLng = getArguments().getDouble("MY_LNG");
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.search_form_fragment, container, false);

        // Initialize the spinner
        categorySelected = (Spinner) view.findViewById(R.id.category_spinner);
        setCategorySpinner();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.category_array, android.R.layout.simple_spinner_dropdown_item);

        // specify the layout for spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelected.setAdapter(adapter);

        // add an onSelected Listener to the spinner
        categorySelected.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "We selected a category");
                Log.d(TAG, "At Position: " + position);
                Log.d(TAG, categorySelected.getSelectedItem().toString());
                getCategoryValue(categorySelected.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // lolwut
            }
        });

        // Init the text views
        keywordError = (TextView) view.findViewById(R.id.keywordError);
        customLocationInputError = (TextView) view.findViewById(R.id.customLocationError);

        // Initialize all the edit text fields
        keywordInput = (TextView) view.findViewById(R.id.inputKeyword);
        distanceInput = (TextView) view.findViewById(R.id.inputDistance);
//        customLocationInput = (TextView) view.findViewById(R.id.inputCustomLocation);
        customLocationInput = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);

        // init the radio button group
        locationGroup = (RadioGroup) view.findViewById(R.id.locationGroup);
        currentLocation = (RadioButton) view.findViewById(R.id.radioHere);
        otherLocation = (RadioButton) view.findViewById(R.id.radioCustomLooation);

        // set an on change listener for the radio group
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // What was selected?
                Log.d(TAG, "Radio Button Change!");
                Log.d(TAG, "Radio Checked: " + checkedId);
                if (checkedId == R.id.radioCustomLooation) {
                    customLocationInput.setEnabled(true);
                    customSearch = true;
                    setLocationType("custom_location");
                }
                else if (checkedId == R.id.radioHere) {
                    customLocationInput.setEnabled(false);
                    customSearch = false;
                    autocompleteSelected = false;
                    setLocationType("current_location");
                }
            }
        });

        // Init the places autocomplete some how
        initAutoComplete(view);

        searchBTN = (Button) view.findViewById(R.id.searchBTN);
        clearBTN = (Button) view.findViewById(R.id.clearBTN);

        // Add the click event listener for the search button
        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Search Button Clicked!", Toast.LENGTH_SHORT).show();
                // set the variables
                checkFields();

                if (checkFields()) {
                    keywordError.setVisibility(View.GONE);
                    customLocationInputError.setVisibility(View.GONE);
//                    new DoSearchStuff().execute();
                    search();
                }

                else {
                    // display the necessary warnings
                    if (fieldError.equals("BOTH")) {
                        keywordError.setVisibility(View.VISIBLE);
                        customLocationInputError.setVisibility(View.VISIBLE);
                    }
                    else if (fieldError.equals("CUSTOMLOCATION")) {
                        customLocationInputError.setVisibility(View.VISIBLE);
                        keywordError.setVisibility(View.GONE);
                    }
                    else if (fieldError.equals("KEYWORD")) {
                        keywordError.setVisibility(View.VISIBLE);
                        customLocationInputError.setVisibility(View.GONE);
                    }
                }
            }
        });

        // Add the click event listener for the clear button
        clearBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Clear Button Clicked!", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        });

        return view;
    }

    private boolean checkFields() {
        String _keyword = keywordInput.getText().toString().trim();
        String _customLocation;

        if (customSearch) {
            _customLocation = customLocationInput.getText().toString().trim();
            if (_keyword.matches("") && _customLocation.matches("")) {
                fieldError = "BOTH";
                Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if (_keyword.matches("")) {
                fieldError = "KEYWORD";
                Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if (_customLocation.matches("")) {
                fieldError = "CUSTOMLOCATION";
                Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (_keyword.matches("")) {
            fieldError = "KEYWORD";
            Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    private void search() {

        // create a progress dialog.
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Fetching results");
        progressDialog.setCancelable(false);
        progressDialog.show();

        setKeyword(keywordInput.getText().toString());
        if (distanceInput.getText().toString() == null || distanceInput.getText().toString().trim().matches("")) {
            setDistance(10.0);
            Log.d(TAG, getDistance().toString());
        } else {
            setDistance(Double.parseDouble(distanceInput.getText().toString()));
        }
        if (customSearch && autocompleteSelected) {
            Log.d(TAG, "autocomplete place selected");
            setDataForSearchActivity();
            placesNearbySearch(getKeyword(), getCategory(), getDistance(), getLocationSearchType(), getCustomLat(), getCustomLng());
        }
        else if (customSearch) { // custom search perform a geolocation search
            setCustomLocation(customLocationInput.getText().toString());
            Log.d(TAG, getDistance().toString());
            setDataForSearchActivity();
                    placesCustomNearbySearch(getKeyword(), getCategory(), getDistance(), getLocationSearchType(), getCustomLocation());
        }
        else {
            // go ahead and send use volley to grab the JSON data
            setDataForSearchActivity();
            placesNearbySearch(getKeyword(), getCategory(), getDistance(), getLocationSearchType(), getMyLat(), getMyLng());

        }
    }

    private void placesNearbySearch(String _keyword, String _category, Double _distance, String _location, final Double _myLat, final Double _myLng) {
//        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
//        String _k = "&keyword="+_keyword;
//        String _c = "&type="+_category;
//        String _d = "&radius="+(_distance*1609.34);
//        String _l = "location="+_myLat+","+_myLng;
//        String key = "&key="+API_KEY;

        String _k = "/keyword/"+_keyword;
        String _c = "/category/"+_category;
        String _d = "/radius/"+(_distance*1609.34);
        String _l = "/location/"+_location;
        String _lat = "/lat/"+_myLat;
        String _lng = "/lon/"+_myLng;

        String searchURL = SERVER_URL+"/places/search"+_k+_c+_d+_l+_lat+_lng;
        searchURL.replaceAll(" ", "%20");
        i = new Intent(SearchFormFragment.this.getContext(), SearchResultsActivity.class);

        RequestQueue httpQueue = Volley.newRequestQueue(getContext());

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        jsonObject = response.toString();
                        i.putExtra("JSON", response.toString());
                        i.putExtra("MY_LAT", _myLat.toString());
                        i.putExtra("MY_LNG", _myLng.toString());
                        startActivity(i);
                        Thread progress = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        try {
                            progress.sleep(2000);
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

    private void placesCustomNearbySearch(String _keyword, String _category, Double _distance, String _location, String _customLocation) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

//        String _k = "&keyword="+_keyword;
//        String _c = "&type="+_category;
//        String _d = "&radius="+(_distance*1609.34);
//        String _l = "location="+_myLat+","+_myLng;
//        String key = "&key="+API_KEY;

        String _k = "/keyword/"+_keyword;
        String _c = "/category/"+_category;
        String _d = "/radius/"+(_distance*1609.34);
        String _l = "/location/"+_location;
        String _cl = "/customlocation/" + _customLocation;
//        String _lat = "/lat/"+_myLat;
//        String _lng = "/lon/"+_myLng;

        String searchURL = SERVER_URL+"/places/custom/search"+_k+_c+_d+_l+_cl;
        i = new Intent(SearchFormFragment.this.getContext(), SearchResultsActivity.class);

        RequestQueue httpQueue = Volley.newRequestQueue(getContext());
        String geoCodeURL = SERVER_URL + "/places/search/geocode/customLocation/"+_customLocation;

        // we need to get the custom location lat lng
        JsonObjectRequest geoCode = new JsonObjectRequest(Request.Method.GET, geoCodeURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Geocoding", response.toString());
                        jsonObject = response.toString();
                        i.putExtra("geo", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        );

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, searchURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.d("Places Nearby Search", response.toString());
                        jsonObject = response.toString();
                        i.putExtra("JSON", response.toString());
                        // we actually need to get the geocoded location
                        startActivity(i);
                        Thread progress = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        try {
                            progress.sleep(2000);
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
        httpQueue.add(geoCode);
        httpQueue.add(getRequest);
    }

    private void clearForm() {
        setKeyword("");
        setDistance(10.0);
        setCustomLocation("");
        keywordInput.setText("");
        distanceInput.setText("");
        categorySelected.setSelection(0);
        customLocationInput.setEnabled(false);
        this.customSearch = false;
        currentLocation.setChecked(true);
        otherLocation.setChecked(false);
        customLocationInput.setText("");
        keywordError.setVisibility(View.GONE);
        customLocationInputError.setVisibility(View.GONE);
        this.autocompleteSelected = false;
    }

    // set data for searchActivity
    private void setDataForSearchActivity() {

        if (getArguments() != null) {
            setMyLat(Double.parseDouble(getArguments().get("MY_LAT").toString()));
            setMyLng(Double.parseDouble(getArguments().get("MY_LNG").toString()));
//            Log.d(TAG, "Lat: " + getMyLat());
//            Log.d(TAG, "Lng: " + getMyLng());
        }
        if (place != null)
//            Log.d(TAG, place.toString());

        if (customSearch && autocompleteSelected ) {
//            Log.d(TAG, "Custom Lat: " + getCustomLat());
//            Log.d(TAG, "Custom Lng: " + getCustomLng());
        }
        else if (customSearch && !autocompleteSelected) {
//            Log.d(TAG, getCustomLocation());
        }
    }

    // Places Autocomplete
    private void initAutoComplete(View view) {
        mAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        mAutoCompleteTextView.setThreshold(3);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAutoCompleteTextView.setOnItemClickListener(this.mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, null);
        mAutoCompleteTextView.setAdapter(mPlaceArrayAdapter);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getActivity(), "Google Places API connection failed with error code:"
                + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    private void setCategorySpinner() {
        // set all of the categories inside a hash map
        categoryList.put("Default", "default");
        categoryList.put("Airport", "airport");
        categoryList.put("Amusement Park", "amusement_park");
        categoryList.put("Aquarium", "aquarium");
        categoryList.put("Art Gallery", "art_gallery");
        categoryList.put("Bakery", "bakery" );
        categoryList.put( "Bar", "bar" );
        categoryList.put( "Beauty Salon", "beauty_salon" );
        categoryList.put( "Bowling Alley", "bowling_alley" );
        categoryList.put( "Bus Station", "bus_station" );
        categoryList.put( "Cafe", "cafe" );
        categoryList.put( "Campground", "campground" );
        categoryList.put( "Car Rental", "car_rental" );
        categoryList.put( "Casino", "casino" );
        categoryList.put( "Lodging", "lodging" );
        categoryList.put( "Movie Theater", "movie_theater" );
        categoryList.put( "Museum", "museum" );
        categoryList.put( "Night Club", "night_club" );
        categoryList.put( "Park", "park" );
        categoryList.put( "Parking", "parking" );
        categoryList.put( "Restaurant", "restaurant" );
        categoryList.put( "Shopping Mall", "shopping_mall" );
        categoryList.put( "Stadium", "stadium" );
        categoryList.put( "Subway Station", "subway_station" );
        categoryList.put( "Taxi Stand", "taxi_stand" );
        categoryList.put( "Train Station", "train_station" );
        categoryList.put( "Transit Station", "transit_station" );
        categoryList.put( "Travel Agency", "travel_agency" );
        categoryList.put( "Zoo", "zoo" );
    }

    public void setKeyword(String k) { this.keyword = k; }

    public String getKeyword() { return keyword; }

    public void setCategory(String c) { this.category = c; }

    public String getCategory() { return category; }

    public void getCategoryValue(String category) {
        this.setCategory(this.categoryList.get(category).toString());
    }

    public void setDistance(Double d) { this.distance = d; }

    public Double getDistance() { return distance; }

    public void setLocationType (String l) { this.locationSearchType = l; }

    public String getLocationSearchType () { return locationSearchType; }

    public void setCustomLocation(String custom) { this.customLocation = custom; }

    public String getCustomLocation() { return customLocation; }

    public void setMyLat(Double d) { this.myLat = d; }

    public Double getMyLat() { return myLat; }

    public void setMyLng(Double d) { this.myLng = d; }

    public Double getMyLng() { return myLng; }

    public void setCustomLat(Double d) { this.customLat = d; }

    public Double getCustomLat() { return customLat; }

    public void setCustomLng(Double d) { this.customLng = d; }

    public Double getCustomLng() { return customLng; }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
//            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
//            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            place = places.get(0);
            CharSequence attributions = places.getAttributions();

//            mNameView.setText(Html.fromHtml(place.getAddress() + ""));

            String textFromAutoComplete = Html.fromHtml(place.getAddress() + "").toString();
            autocompleteSelected = true;
//            Log.d(TAG, textFromAutoComplete);
//            Log.d(TAG, "Lat: " + place.getLatLng().latitude);
//            Log.d(TAG, "Lng: " + place.getLatLng().longitude);
            setCustomLat(place.getLatLng().latitude);
            setCustomLng(place.getLatLng().longitude);
        }
    };
}
