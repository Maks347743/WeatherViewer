package com.geek.maksim.potapov.weatherviewer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

import static android.content.Context.BIND_AUTO_CREATE;

public class ListWeatherFragment extends Fragment {

    private WeatherDataSource mWeatherDataSource;
    //список объектов Weather с прогнозом погоды
    private List<Weather> mWeatherList = new ArrayList<>();
    //адаптер связывает объект Weather и элемент RecyclerView
    private WeatherAdapter mWeatherAdapter;
    private RecyclerView mWeatherRecyclerView;
    private EditText mLocationEditText;
    //ссылка на заполняемый из фрагмента view
    private View mView;
    //сервис для загрузки погоды
    private WeatherService mWeatherService;
    //переменная для проверки привязки к сервису
    boolean mBound = false;
    //BroadcastReceiver для получение результата работы сервиса по получению погоды
    BroadcastReceiver mBroadcastReceiver;
    private static final int TASK_SUCCESS = 1;
    public static final String TASK_STATUS = "success";
    public static final String JSON = "JSON";
    public static final String BROADCAST_ACTION = "com.geek.maksim.potapov.ACTION_GETWEATHER";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_list_weather, container, false);
        mWeatherRecyclerView = mView.findViewById(R.id.weatherRecyclerView);
        mWeatherAdapter = new WeatherAdapter(getActivity(), mWeatherList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mWeatherRecyclerView.setLayoutManager(layoutManager);
        mWeatherRecyclerView.setAdapter(mWeatherAdapter);

        FloatingActionButton fab = mView.findViewById(R.id.fab);
        //fab скрывает клавиатуру и выдает запрос к веб-сервису
        fab.setOnClickListener(v -> {
            //получить текст из mLocationEditText и создать URL
            mLocationEditText = mView.findViewById(R.id.locationEditText);
            URL url = createURL(mLocationEditText.getText().toString());

            //скрыть клавиатуру и запустить GetWeatherTask для получения
            //погодных данных от веб-сервиса weatherbit.io в отдельном потоке
            if (url != null) {
                dismissKeyboard(mLocationEditText);
                Intent intent = new Intent(getContext(), WeatherService.class);
                intent.putExtra("URL", url);
                if (mBound) {
                    getActivity().unbindService(mConnection);
                    mBound = false;
                }
                getActivity().bindService(intent, mConnection, BIND_AUTO_CREATE);
                mBound = true;
            } else {
                Snackbar.make(v.findViewById(R.id.rootFragmentListWeather), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(TASK_STATUS, 0);

                // если выполнение успешно
                if (status == TASK_SUCCESS) {
                    String result = intent.getStringExtra(JSON);
                    try {
                        JSONObject weatherData = new JSONObject(result);
                        convertJSONtoArrayList(weatherData); //заполнение mWeatherList
                        //добавление или обновление данных в БД, в зависимости от того, есть ли там уже такой город или нет
                        mWeatherDataSource.updateCityWeather(mLocationEditText.getText().toString(), mWeatherList);
                        mWeatherAdapter.notifyDataSetChanged(); //связать с RecyclerView
                        mWeatherRecyclerView.smoothScrollToPosition(0); //прокрутить до начала
                    } catch (JSONException e) {
                        Snackbar.make(mView.findViewById(R.id.rootFragmentListWeather), R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        };
        //создаем фильтр для рисивера
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeatherDataSource = new WeatherDataSource(getActivity().getApplicationContext());
    }

    //метод закрывает клавиатуру при касании кнопки fab
    private void dismissKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //создание URL веб-сервиса weatherbit.io для названия города
    private URL createURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            String urlString = String.format(baseUrl, URLEncoder.encode(city, "UTF-8"), apiKey);
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //некорректный URL
    }

    // создание объектов Weather на базу JSONObject с прогнозом
    private void convertJSONtoArrayList(JSONObject forecast) {
        mWeatherList.clear(); //стирание предыдущих данных
        try {
            //получение свойства "data" JSONArray
            if (forecast != null) {
                JSONArray weatherList = forecast.getJSONArray("data");
                //преобразовать каждый элемент списка в объект Weather
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
                    //добавить новый объект Weather в mWeatherList
                    mWeatherList.add(new Weather(time, minTemp, maxTemp, humidity, description, icon));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // есть привязка к сервису, возврат сервиса
            WeatherService.WeatherBinder binder = (WeatherService.WeatherBinder) service;
            mWeatherService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onDestroyView() {
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }
}
