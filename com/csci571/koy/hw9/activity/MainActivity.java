package com.csci571.koy.hw9.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.adapter.SectionsPagerAdapter;
import com.csci571.koy.hw9.fragments.SearchFormFragment;
import com.csci571.koy.hw9.fragments.FavoritesFragment;
import com.csci571.koy.hw9.services.GPS_Service;

/**
 * Code Snippets Borrowed From: Ravi Tamada
 * https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
 *
 * Some Code Ideas used from Mitch Tabian
 * https://github.com/mitchtabian/TabFragments/tree/master/TabFragments
 *
 *
 * https://www.androidhive.info/2012/07/android-gps-location-manager-tutorial/
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    // location service variables
    private BroadcastReceiver broadcastReceiver;
    private double myLat, myLng;

    private SearchFormFragment searchFormFragment;
    private FavoritesFragment favoritesFragment;
    private boolean pause;
    private Bundle geoBundle;

    @Override
    protected void onResume() {
        super.onResume();
        // check to prevent memory leaks
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setLatitude(Double.parseDouble(intent.getExtras().get("latitude").toString()));
                    setLongitude(Double.parseDouble(intent.getExtras().get("longitude").toString()));
                    sendDataToSearchForm();
                    pause = true;
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        stopService(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pause = false;
        geoBundle = new Bundle();

        searchFormFragment = new SearchFormFragment();
        favoritesFragment = new FavoritesFragment();

        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting.");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // sets the support action bar for a back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.container);
        // Defines the number of tabs by setting appropriate fragment and tab name.
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        // Assigns the ViewPager to TabLayout.
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();

        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setSize(1,1);
        linearLayout.setDividerPadding(10);
        linearLayout.setDividerDrawable(drawable);

        if (!runtime_permissions()) {
            startLocationService();
        }
    }

    private void setupTabIcons() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setWidth(width/2);
        tabOne.setGravity(Gravity.CENTER);
        tabOne.setText("SEARCH");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search, 0, 0, 0);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setWidth(width/2);
        tabTwo.setGravity(Gravity.CENTER);
        tabTwo.setText("FAVORITES");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_fill_white,0,0,0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(searchFormFragment, "SEARCH");
        adapter.addFragment(favoritesFragment, "FAVORITES");
        viewPager.setAdapter(adapter);
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "We have permission", Toast.LENGTH_SHORT);
                startLocationService();
                sendDataToSearchForm();
            } else {
                runtime_permissions();
            }
        }
    }

    private void startLocationService() {
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        startService(i);
        sendDataToSearchForm();
    }

    private void sendDataToSearchForm() {

        if (!pause) {
            geoBundle.putDouble("MY_LAT", getLatitude());
            geoBundle.putDouble("MY_LNG", getLongitude());

            searchFormFragment.setArguments(geoBundle);
        }
    }

    public void setLatitude(double _lat) { this.myLat = _lat; }

    public Double getLatitude() { return myLat; }

    public void setLongitude(double _lng) { this.myLng = _lng; }

    public Double getLongitude() { return myLng; }
}
