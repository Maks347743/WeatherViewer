package com.geek.maksim.potapov.weatherviewer;

import android.animation.LayoutTransition;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WeatherFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private static final String CITY_KEY = "city";
    //список объектов DailyWeather с прогнозом погоды
    private List<DailyWeather> mDailyWeatherList = new ArrayList<>();
    private List<HourlyWeather> mHourlyWeatherList = new ArrayList<>();
    //адаптер связывает объект DailyWeather и элемент RecyclerView
    private DailyWeatherAdapter mDailyWeatherAdapter;
    private HourlyWeatherAdapter mHourlyWeatherAdapter;
    private RecyclerView mDailyWeatherRecyclerView;
    private RecyclerView mHourlyWeatherRecyclerView;
    private TextView mCurrentDescriptionTextView;
    private TextView mCurrentTemperatureTextView;
    private TextView mCurrentDayOfWeekTextView;
    private TextView mTodayTextView;
    private TextView mCurrentMaxTempTextView;
    private TextView mCurrentMinTempTextView;
    //ссылка на заполняемый из фрагмента view
    private View mView;
    private SwipeRefreshLayout mRefreshLayout;
    private SearchView mSearchView;
    private MenuItem mItemSearch;
    private TextView mCurrentCityTextView;
    private LinearLayout mHourlyLinearTitle;
    private String mCity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_weather, container, false);
        Toolbar toolbar = mView.findViewById(R.id.toolbar);
        ((FragmentActivity)getActivity()).setSupportActionBar(toolbar);
        mRefreshLayout = mView.findViewById(R.id.refresh);
        mRefreshLayout.setOnRefreshListener(this);
        mDailyWeatherRecyclerView = mView.findViewById(R.id.daily_weather_recycler_view);
        mHourlyWeatherRecyclerView = mView.findViewById(R.id.hourly_weather_recycler_view);
        mDailyWeatherAdapter = new DailyWeatherAdapter(getActivity(), mDailyWeatherList);
        mHourlyWeatherAdapter = new HourlyWeatherAdapter(getActivity(), mHourlyWeatherList);
        RecyclerView.LayoutManager dailyLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mDailyWeatherRecyclerView.setLayoutManager(dailyLayoutManager);
        mDailyWeatherRecyclerView.setAdapter(mDailyWeatherAdapter);

        RecyclerView.LayoutManager hourlyLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mHourlyWeatherRecyclerView.setLayoutManager(hourlyLayoutManager);
        mHourlyWeatherRecyclerView.setAdapter(mHourlyWeatherAdapter);
        setHasOptionsMenu(true);
        mCurrentCityTextView = mView.findViewById(R.id.current_city_text_view);

        mCurrentDescriptionTextView = mView.findViewById(R.id.current_weather_description_text_view);
        mCurrentTemperatureTextView = mView.findViewById(R.id.current_temperature_text_view);
        mCurrentDayOfWeekTextView = mView.findViewById(R.id.current_day_of_week_text_view);
        mTodayTextView = mView.findViewById(R.id.today_text_view);
        mCurrentMaxTempTextView = mView.findViewById(R.id.current_max_temp_text_view);
        mCurrentMinTempTextView = mView.findViewById(R.id.current_min_temp_text_view);
        mHourlyLinearTitle = mView.findViewById(R.id.hourly_linear_title);
        if (mCity == null){
            mRefreshLayout.setEnabled(false);
            mHourlyLinearTitle.setVisibility(View.INVISIBLE);
        }
        return mView;
    }

    //создание URL веб-сервиса weatherbit.io для названия города
    private URL createDailyURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.daily_web_service_url);

        try {
            String urlString = String.format(baseUrl, URLEncoder.encode(city, "UTF-8"), apiKey);
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //некорректный URL
    }

    //обращение к REST-совместимому веб-сервису за погодными данными
    private class GetDailyWeatherTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            jsonBuilder.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(jsonBuilder.toString());
                } else {
                    Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject dailyWeather) {
            convertDailyJSONtoArrayList(dailyWeather); //заполнение mDailyWeatherList
            if (mDailyWeatherList.size() == 16) {
                mCurrentDayOfWeekTextView.setText(mDailyWeatherList.get(0).getDayOfWeek());
                mCurrentMaxTempTextView.setText(mDailyWeatherList.get(0).getMaxTemp());
                mCurrentMinTempTextView.setText(mDailyWeatherList.get(0).getMinTemp());
                mDailyWeatherAdapter.notifyDataSetChanged(); //связать с RecyclerView
                mDailyWeatherRecyclerView.smoothScrollToPosition(0); //прокрутить до начала
            } else {
                Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // создание объектов DailyWeather на базу JSONObject с прогнозом
    private void convertDailyJSONtoArrayList(JSONObject forecast) {
        mDailyWeatherList.clear(); //стирание предыдущих данных
        try {
            //получение свойства "data" JSONArray
            if (forecast != null) {
                JSONArray weatherList = forecast.getJSONArray("data");
                //преобразовать каждый элемент списка в объект DailyWeather
                for (int i = 0; i < weatherList.length(); i++) {
                    JSONObject day = weatherList.getJSONObject(i); //данные за день
                    //получить временную метку даты/времени
                    long time = day.getLong("ts");
                    //получить минимальную температуру дня
                    double minTemp = day.getDouble("min_temp");
                    //получить максимальную температуру дня
                    double maxTemp = day.getDouble("max_temp");
                    //получить влажность воздуха
                    double humidity = day.getDouble("rh");
                    //получить JSONObject с описанием погоды в этот день и значком
                    JSONObject weather = day.getJSONObject("weather");
                    //получить описание погоды
                    String description = weather.getString("description");
                    //получить значок погоды
                    String icon = weather.getString("icon");
                    //добавить новый объект DailyWeather в mDailyWeatherList
                    mDailyWeatherList.add(new DailyWeather(time, minTemp, maxTemp, humidity, description, icon));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //создание URL веб-сервиса weatherbit.io для названия города
    private URL createHourlyURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.hourly_web_service_url);

        try {
            String urlString = String.format(baseUrl, URLEncoder.encode(city, "UTF-8"), apiKey);
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //некорректный URL
    }

    //обращение к REST-совместимому веб-сервису за погодными данными
    private class GetHourlyWeatherTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            jsonBuilder.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(jsonBuilder.toString());
                } else {
                    Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject hourlyForecast) {
            convertHourlyJSONtoArrayList(hourlyForecast); //заполнение mHourlyWeatherList
            if (mHourlyWeatherList.size() > 0){
                mCurrentDescriptionTextView.setText(mHourlyWeatherList.get(0).getCurrentDescription());
                mCurrentTemperatureTextView.setText(mHourlyWeatherList.get(0).getCurrentTemperature());
                mHourlyWeatherAdapter.notifyDataSetChanged(); //связать с RecyclerView
                mHourlyWeatherRecyclerView.smoothScrollToPosition(0); //прокрутить до начала
            } else {
                Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.connect_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // создание объектов HourlyWeather на базу JSONObject с прогнозом
    private void convertHourlyJSONtoArrayList(JSONObject forecast) {
        mHourlyWeatherList.clear(); //стирание предыдущих данных
        try {
            //получение свойства "data" JSONArray
            if (forecast != null) {
                JSONArray weatherList = forecast.getJSONArray("data");
                //преобразовать каждый элемент списка в объект DailyWeather
                for (int i = 0; i < weatherList.length(); i++) {
                    JSONObject hour = weatherList.getJSONObject(i); //данные за час
                    //получить временную метку даты/времени
                    String dateTime = hour.getString("datetime");
                    //получить температуру часа
                    double temp = hour.getDouble("temp");
                    //получить JSONObject с описанием погоды в этот час и значком
                    JSONObject weather = hour.getJSONObject("weather");
                    //получить описание погоды
                    String description = weather.getString("description");
                    //получить значок погоды
                    String icon = weather.getString("icon");
                    //добавить новый объект DailyWeather в mDailyWeatherList
                    mHourlyWeatherList.add(new HourlyWeather(description, temp, dateTime, icon));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initArguments(String city){
        Bundle bundle = new Bundle();
        bundle.putString(CITY_KEY, city);
        setArguments(bundle);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(false);
        if (mCity != null && !mCity.isEmpty()){
            mRefreshLayout.setRefreshing(true);
            updateWeather(mCity);
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        mSearchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint_text));
        mItemSearch = menu.findItem(R.id.search_view);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
        //анимация
        LinearLayout searchBar = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());
        super.onCreateOptionsMenu(menu, inflater);
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
        URL dailyUrl = createDailyURL(city);
        //запустить GetDailyWeatherTask для получения ежедневных
        //погодных данных от веб-сервиса weatherbit.io в отдельном потоке
        if (dailyUrl != null) {
            GetDailyWeatherTask getDailyWeatherTask = new GetDailyWeatherTask();
            getDailyWeatherTask.execute(dailyUrl);
        } else {
            Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            return;
        }

        URL hourlyUrl = createHourlyURL(city);

        if (hourlyUrl != null) {
            GetHourlyWeatherTask getHourlyWeatherTask = new GetHourlyWeatherTask();
            getHourlyWeatherTask.execute(hourlyUrl);
        } else {
            Snackbar.make(mView.findViewById(R.id.root_fragment_weather), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            return;
        }
        mRefreshLayout.setEnabled(true);
        mHourlyLinearTitle.setVisibility(View.VISIBLE);
        mCurrentCityTextView.setText(city);
        mTodayTextView.setText(R.string.today);
        this.mCity = city;
    }
}
