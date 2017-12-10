package com.geek.maksim.potapov.weatherviewer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class ListWeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_weather);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.fragment_list_weather_container) == null){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ListWeatherFragment listWeatherFragment = new ListWeatherFragment();
            fragmentTransaction.add(R.id.fragment_list_weather_container, listWeatherFragment);
            fragmentTransaction.commit();
        }
    }
}
