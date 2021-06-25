package com.csci571.koy.hw9.fragments;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.activity.PlacesDetailActivity;

public class InfoFragment extends Fragment {

    private static final String TAG = "Info Fragment";

    private Button btnTEST;
    private String addressText, phoneText, priceText, ratingText, googlepageText, websiteText;
//    private TextView addressLabel, phoneNumberLabel, priceLevelLabel, ratingLabel, googlePageLabel, websiteLabel;
    private TextView address, phoneNumber, priceLevel, googlePage, ratingTextView, website, errorMessage;
    private RatingBar rating;
    private float ratingVal;

    public InfoFragment() {
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
        View view = inflater.inflate(R.layout.info_fragment, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int width = displayMetrics.widthPixels;

        // set the text view sizes.
//        addressLabel = (TextView) view.findViewById(R.id.addressLabel);
        address = (TextView) view.findViewById(R.id.address);
//        phoneNumberLabel = (TextView) view.findViewById(R.id.phoneNumberLabel);
        phoneNumber = (TextView) view.findViewById(R.id.phoneNumber);
//        priceLevelLabel = (TextView) view.findViewById(R.id.priceLevelLabel);
        priceLevel = (TextView) view.findViewById(R.id.priceLevel);
//        ratingLabel = (TextView) view.findViewById(R.id.ratingLabel);
        ratingTextView = (TextView) view.findViewById(R.id.rating);
        rating = (RatingBar) view.findViewById(R.id.ratingBar);
//        googlePageLabel = (TextView) view.findViewById(R.id.googlePageLabel);
        googlePage = (TextView) view.findViewById(R.id.googlePage);
//        websiteLabel = (TextView) view.findViewById(R.id.websiteLabel);
        website = (TextView) view.findViewById(R.id.website);
        errorMessage = (TextView) view.findViewById(R.id.errorMessage);

        ((TextView) view.findViewById(R.id.addressLabel)).setWidth(width/4);
        ((TextView) view.findViewById(R.id.phoneNumberLabel)).setWidth(width/4);
        ((TextView) view.findViewById(R.id.ratingLabel)).setWidth(width/4);
        ((TextView) view.findViewById(R.id.priceLevelLabel)).setWidth(width/4);
        ((TextView) view.findViewById(R.id.googlePageLabel)).setWidth(width/4);
        ((TextView) view.findViewById(R.id.websiteLabel)).setWidth(width/4);

//        addressLabel.setWidth(width/4);
//        phoneNumberLabel.setWidth(width/4);
//        ratingLabel.setWidth(width/4);
//        priceLevelLabel.setWidth(width/4);
//        googlePageLabel.setWidth(width/4);
//        websiteLabel.setWidth(width/4);

//        Intent i = new Intent(getContext(), PlacesDetailActivity.class);
        if (getArguments() != null) {
            errorMessage.setVisibility(View.GONE);

            if (getArguments().get("formatted_address") != null)
                addressText = getArguments().get("formatted_address").toString();
            else
                addressText = "No Address Found";
            if (getArguments().get("formatted_phone_number") != null)
                phoneText = getArguments().get("formatted_phone_number").toString();
            else
                phoneText = " No Phone Number Found";
            if (getArguments().get("price") != null) {
                Double p = Double.parseDouble(getArguments().get("price").toString());

                if (p == 0)
                    priceText = " ";
                else if (p == 1)
                    priceText = "$";
                else if (p == 2)
                    priceText = "$$";
                else if (p == 3)
                    priceText = "$$$";
                else if (p == 4)
                    priceText = "$$$$";
                else if (p == 5)
                    priceText = "$$$$$";

            }
            else
                priceText = "No Price Level Found";
            if (getArguments().get("google_page") != null)
                googlepageText = getArguments().get("google_page").toString();
            else
                googlepageText = "No Google Page URL found";
            if (getArguments().get("rating") != null) {
                ratingTextView.setVisibility(View.GONE);
                Float r = Float.parseFloat(getArguments().get("rating").toString());
                ratingText = getArguments().get("rating").toString();
                ratingVal = r;
                rating.setRating(ratingVal);
            }
            else {
                ratingText = "No Rating Found";
                ratingTextView.setVisibility(View.VISIBLE);
                ratingTextView.setText(ratingText);
                rating.setVisibility(View.GONE);
            }
            if (getArguments().get("website") != null)
                websiteText = getArguments().get("website").toString();
            else
                websiteText = "No Website URL Found";

            address.setText(addressText);
            phoneNumber.setText(phoneText);
            priceLevel.setText(priceText);
            googlePage.setText(googlepageText);
            website.setText(websiteText);
        } else { // No places info found
            errorMessage.setVisibility(View.VISIBLE);
        }

        
        return view;
    }
}
