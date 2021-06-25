package com.csci571.koy.hw9.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.activity.PlacesDetailActivity;
import com.csci571.koy.hw9.adapter.PlaceArrayAdapter;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MapFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, RoutingListener {

    private static final String TAG = "Map Fragment";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private Spinner travelModeSelected;
    private TextView customLocationInput;
    private AutoCompleteTextView mAutoCompleteTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private Place place;
    private GoogleMap mMap;
    private Polyline line = null;

    private String customLocation, placeName, customPlaceName, vicinity;
    private AbstractRouting.TravelMode travelMode;
    private double placeLat, placeLng, customLat, customLng;
    private Marker customMarker;


    private boolean autocompleteSelected = false;

    private HashMap<String, AbstractRouting.TravelMode> travelModeList = new HashMap<>();


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        // get the place lat place lng and place name
        Bundle b = new Bundle();

        if (getArguments() != null) {
            b = getArguments();

            if (b.get("place_name") != null) {
                setPlaceName(b.get("place_name").toString());
            }
            if (b.get("place_lat") != null) {
                setPlaceLat(b.getDouble("place_lat"));
            }
            if (b.get("place_lng") != null) {
                setPlaceLng(b.getDouble("place_lng"));
            }

    }

        // set the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize the spinner
        travelModeSelected = (Spinner) view.findViewById(R.id.travel_mode_spinner);
        setTravelModeSpinner();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.travel_mode_array, android.R.layout.simple_spinner_dropdown_item);

        // specify the layout for spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        travelModeSelected.setAdapter(adapter);

        // add an onSelected Listener to the spinner
        travelModeSelected.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, travelModeSelected.getSelectedItem().toString());
                getTravelModeValue(travelModeSelected.getSelectedItem().toString());

                if (autocompleteSelected) {
                    // perform directions matrix stuff
                    runTheDirections(getTravelModeValue(travelModeSelected.getSelectedItem().toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // lolwut
            }
        });
        customLocationInput = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);

//         autcomplete
        initAutoComplete(view);

        return view;
    }

    public void runTheDirections(AbstractRouting.TravelMode t) {
        LatLng start = new LatLng(customLat,customLng);
        LatLng place = new LatLng(placeLat, placeLng);
        Routing routing = new Routing.Builder()
                .travelMode(getTravelModeValue(travelModeSelected.getSelectedItem().toString()))
                .withListener(this)
                .waypoints(start, place)
                .key("AIzaSyBbLH6p0js-mnX1Pj4jHY7Ui6jfY0su4_w")
                .build();

        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d(TAG, e.getMessage().toString());
        Log.d(TAG, e.getStatusCode().toString());
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        // Get all the points and plots the polyLine route.
        List<LatLng> listPoints = route.get(0).getPoints();
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        Iterator<LatLng> iterator = listPoints.iterator();
        while(iterator.hasNext())
        {
            LatLng data = iterator.next();
            options.add(data);
        }

        // If line not null then remove old polyline routing.
        if (line != null) {
            line.remove();
        }
        line = mMap.addPolyline(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(route.get(0).getLatLgnBounds().getCenter()));
        LatLng start = new LatLng(getCustomLat(), getCustomLng());
        LatLng dest = new LatLng(getPlaceLat(), getPlaceLng());
        customMarker = mMap.addMarker(new MarkerOptions().position(start).title(customPlaceName));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(dest);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.get(0).getLatLgnBounds().getCenter(), 16));

    }

    @Override
    public void onRoutingCancelled() {

    }

    public void setTravelModeSpinner() {
        travelModeList.put("Driving",  Routing.TravelMode.DRIVING);
        travelModeList.put("Bicycling", Routing.TravelMode.BIKING);
        travelModeList.put("Transit", Routing.TravelMode.TRANSIT);
        travelModeList.put("Walking", Routing.TravelMode.WALKING);
    }

    public AbstractRouting.TravelMode getTravelModeValue(String _travelMode) {
        travelMode = travelModeList.get(_travelMode);
        return travelMode;
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

    // Google Maps
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at the current place.
        LatLng placeLocation = new LatLng(getPlaceLat(), getPlaceLng());
        mMap.addMarker(new MarkerOptions().position(placeLocation).title(getPlaceName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 15));
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
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
            // remove old marker
            if (customMarker != null) {
                customMarker.remove();
            }
            // Selecting the first object buffer.
            place = places.get(0);
            CharSequence attributions = places.getAttributions();

            String textFromAutoComplete = Html.fromHtml(place.getAddress() + "").toString();
            autocompleteSelected = true;
            Log.d(TAG, textFromAutoComplete);
            Log.d(TAG, "Lat: " + place.getLatLng().latitude);
            Log.d(TAG, "Lng: " + place.getLatLng().longitude);
            customPlaceName = place.getName().toString();
            setCustomLat(place.getLatLng().latitude);
            setCustomLng(place.getLatLng().longitude);

        }
    };


    public void setCustomLocation(String custom) { this.customLocation = custom; }

    public String getCustomLocation() { return customLocation; }

    public void setPlaceName(String p) { this.placeName = p; }
    public void setVicinity(String p) { this.vicinity = p; }

    public String getPlaceName() { return placeName; }
    public String getVicinity() { return vicinity; }

    public void setCustomLat(Double d) { this.customLat = d; }

    public Double getCustomLat() { return customLat; }

    public void setCustomLng(Double d) { this.customLng = d; }

    public Double getCustomLng() { return customLng; }

    public void setPlaceLat(Double d) { this.placeLat = d; }

    public Double getPlaceLat() { return placeLat; }

    public void setPlaceLng(Double d) { this.placeLng = d; }

    public Double getPlaceLng() { return placeLng; }

    private class getDirections extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }
    }
}
