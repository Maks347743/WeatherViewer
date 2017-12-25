package com.geek.maksim.potapov.weatherviewer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class FragmentActivity extends AppCompatActivity{

    public static String CITY_PREFERENCES = "com.geek.maksim.potapov.city_preferences";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        WeatherFragment weatherFragment = new WeatherFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_weather_container, weatherFragment);
        fragmentTransaction.commit();
    }

}
