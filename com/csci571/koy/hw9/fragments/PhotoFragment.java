package com.csci571.koy.hw9.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csci571.koy.hw9.R;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PhotoFragment extends Fragment {
    private static final String TAG = "Photo Fragment";

    private TextView errorMessage;
    private GeoDataClient mGeoDataClient;

    private String photoJSON, placeId, errorText;
    private boolean has_photos;

    private int i;
    public PhotoFragment() {
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
        View view = inflater.inflate(R.layout.photo_fragment, container, false);

        // set the text view
        errorMessage = (TextView) view.findViewById(R.id.errorMessage);
        // by default set the view to gone.
        errorMessage.setVisibility(View.GONE);

        mGeoDataClient = Places.getGeoDataClient(getActivity());

        if (getArguments() != null) {
            Bundle b = getArguments();
            if (b.get("place_id") != null) {
                placeId = getArguments().get("place_id").toString();
            }
            
            if (b.get("has_photos") != null && b.get("place_id") != null) {
                if (b.getBoolean("has_photos") == false) {
                    errorText = "No Photos Found";
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText(errorText);
                }
                else { // we found photos
                    errorMessage.setVisibility(View.GONE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // do all the photo generation
                            generatePhotos(placeId);
                        }
                    }).start();
                }
            }
        }
        return view;
    }

    /**
     * TODO: Fix Image sizes to scale the screen properly, add padding to photos.
     * @param _placeId
     */
    public void generatePhotos(String _placeId) {
        final String placeId = _placeId;
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // get the place photo metadata buffer
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.

                Log.d(TAG, ""+photoMetadataBuffer.getCount());
                PlacePhotoMetadata photoMetadata;
                DisplayMetrics metrics = new DisplayMetrics();
                final int width = metrics.widthPixels;
                for (i = 0; i < photoMetadataBuffer.getCount(); i++) {
                    photoMetadata = photoMetadataBuffer.get(i);
                    // Get the attribution text.
                    CharSequence attribution = photoMetadata.getAttributions();
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
//                            Bitmap resized = Bitmap.createBitmap(bitmap, width, true);
//                            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800,  600, false);
                            LinearLayout linearLayout = (LinearLayout) getView().findViewById(R.id.photoLayout);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ImageView imageView = new ImageView(getContext());

                            if (linearLayout.getChildCount() == 0) {
                                params.setMargins(0, 14, 0, 14);
                            } else
                                params.setMargins(0, 14, 0, 14);

                            imageView.setLayoutParams(params);
                            imageView.setImageBitmap(bitmap);
                            imageView.setAdjustViewBounds(true);
                            linearLayout.addView(imageView);
                        }
                    });
                }
                // once finished release the data buffer
                photoMetadataBuffer.release();
            }
        });


    }
}
