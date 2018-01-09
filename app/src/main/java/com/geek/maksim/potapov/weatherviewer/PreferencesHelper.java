package com.geek.maksim.potapov.weatherviewer;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PreferencesHelper {

    static ArrayList<String> loadFavoriteCities(SharedPreferences preferences){
        Gson gson = new Gson();
        String cities = preferences.getString("cities", null);
        if (cities == null) return new ArrayList<>();
        String[] favorite = gson.fromJson(cities, String[].class);
        List<String> temp = Arrays.asList(favorite);
        return new ArrayList<>(temp);
    }

    static void saveFavoriteCities(List<String> favoriteList, Context context){
        Gson gson = new Gson();
        String json = gson.toJson(favoriteList);
        SharedPreferences preferences = context.getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cities", json);
        editor.apply();
    }

    public static void saveCity(String city, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", city);
        editor.apply();
    }
}
