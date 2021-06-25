package com.csci571.koy.hw9.fragments;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Button;
import android.widget.TextView;

import com.csci571.koy.hw9.R;
import com.csci571.koy.hw9.adapter.FavoritesListAdapter;
import com.csci571.koy.hw9.adapter.SearchItemRecyclerViewAdapter;
import com.csci571.koy.hw9.interfaces.SharedPreference;
import com.csci571.koy.hw9.model.SearchResultItem;

import java.util.List;

/**
 * Created by koyst on 4/16/2018.
 */

/**
 * Code Snippets Borrowed From: Ravi Tamada
 * https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
 *
 * Some Code Ideas used from Mitch Tabian
 * https://github.com/mitchtabian/TabFragments/tree/master/TabFragments
 *
 */
public class FavoritesFragment extends Fragment {

    private static final String TAG = "Favorites Fragment";

    RecyclerView recyclerView;
    private TextView favoritesText;
    SharedPreference favoritesPreference;
    List<SearchResultItem> favoritesList;
    RecyclerView.OnChildAttachStateChangeListener listener;
    Context context;
    Activity activity;
    public FavoritesListAdapter adapter;
//    public SearchItemRecyclerViewAdapter adapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.favorites_list_fragment, container, false);

        favoritesText = (TextView) view.findViewById(R.id.favoritesText);
        context = getActivity().getApplicationContext();
//        btnTEST.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "TESTING FAVORITES BUTTON CLICK",Toast.LENGTH_SHORT).show();
//            }
//        });
        favoritesPreference = new SharedPreference();
        favoritesList = favoritesPreference.getFavorites(getActivity());

        if (favoritesList == null) {
            favoritesText.setVisibility(View.VISIBLE);
        }
        else {
            if (favoritesList.size() == 0) {
                favoritesText.setVisibility(View.VISIBLE);
            }
            else {
                favoritesText.setVisibility(View.GONE);
                recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
                adapter = new FavoritesListAdapter(context, favoritesList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(View view) {
                        // Do nothing since there is no add action inside the favorites fragment
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(View view) {
                        if (favoritesList.size() == 0) {
                            // No Favorites in the list so show the view text.
                            favoritesText.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            Log.d(TAG, ""+favoritesList.size());
        }
    }


}
