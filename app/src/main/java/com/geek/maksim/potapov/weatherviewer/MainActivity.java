package com.geek.maksim.potapov.weatherviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
    //список объектов Weather с прогнозом погоды
    private List<Weather> mWeatherList = new ArrayList<>();
    //адаптер связывает объект Weather и элемент RecyclerView
    private WeatherAdapter mWeatherAdapter;
    private RecyclerView mWeatherRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWeatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        mWeatherAdapter = new WeatherAdapter(this, mWeatherList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mWeatherRecyclerView.setLayoutManager(layoutManager);
        mWeatherRecyclerView.setAdapter(mWeatherAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        //fab скрывает клавиатуру и выдает запрос к веб-сервису
        fab.setOnClickListener(view -> {
            //получить текст из locationEditText и создать URL
            EditText locationEditText = findViewById(R.id.locationEditText);
            URL url = createURL(locationEditText.getText().toString());

            //скрыть клавиатуру и запустить GetWeatherTask для получения
            //погодных данных от веб-сервиса weatherbit.io в отдельном потоке
            if (url != null) {
                dismissKeyboard(locationEditText);
                GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                getLocalWeatherTask.execute(url);
            } else {
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    //метод закрывает клавиатуру при касании кнопки fab
    private void dismissKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

    //обращение к REST-совместимому веб-сервису за погодными данными
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {

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
                        Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(jsonBuilder.toString());
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject dailyWeather) {
            convertJSONtoArrayList(dailyWeather); //заполнение mWeatherList
            mWeatherAdapter.notifyDataSetChanged(); //связать с RecyclerView
            mWeatherRecyclerView.smoothScrollToPosition(0); //прокрутить до начала
        }
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

}
