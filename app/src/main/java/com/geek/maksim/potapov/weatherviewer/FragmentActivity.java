package com.geek.maksim.potapov.weatherviewer;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class FragmentActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private SearchView mSearchView;
    private MenuItem mItemSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mSearchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint_text));
        mItemSearch = menu.findItem(R.id.search_view);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
        //анимация
        LinearLayout searchBar = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String city) {
        //закрытие клавиатуры
        mSearchView.clearFocus();
        mItemSearch.collapseActionView();
        updateWeather(city);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void updateWeather(String city){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WeatherFragment weatherFragment = new WeatherFragment();
        weatherFragment.initArguments(city);
        fragmentTransaction.replace(R.id.fragment_weather_container, weatherFragment);
        fragmentTransaction.commit();
    }
}
