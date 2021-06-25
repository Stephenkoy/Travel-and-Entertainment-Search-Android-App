package com.csci571.koy.hw9.interfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.csci571.koy.hw9.model.SearchResultItem;
import com.google.gson.Gson;

public class SharedPreference {

    public static final String PREFS_NAME = "PLACES_APP";
    public static final String FAVORITES = "Places_Favorite";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveFavorites(Context context, List<SearchResultItem> favorites) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, SearchResultItem item) {
        List<SearchResultItem> favorites = getFavorites(context);
        if (favorites == null) {
            favorites = new ArrayList<SearchResultItem>();
        }
        favorites.add(item);
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, SearchResultItem item) {
        ArrayList<SearchResultItem> favorites = getFavorites(context);
        if (favorites != null) {
            for (int i = 0; i < favorites.size(); i++) {
                if (favorites.get(i).getPlaceId().equals(item.getPlaceId())) {
                    favorites.remove(i);
                    saveFavorites(context, favorites);
                    break;
                }
            }
        }
    }

    public ArrayList<SearchResultItem> getFavorites(Context context) {
        SharedPreferences settings;
        List<SearchResultItem> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            SearchResultItem[] favoriteItems = gson.fromJson(jsonFavorites,
                    SearchResultItem[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<SearchResultItem>(favorites);
        } else
            return null;

        return (ArrayList<SearchResultItem>) favorites;
    }
}
