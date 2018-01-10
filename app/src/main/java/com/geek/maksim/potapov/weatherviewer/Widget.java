package com.geek.maksim.potapov.weatherviewer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;


public class Widget extends AppWidgetProvider {

    private Context mContext;
    private RemoteViews widgetView;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mContext = context;
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        SharedPreferences preferences = context.getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
        String city;
        ArrayList<String> cities = PreferencesHelper.loadFavoriteCities(preferences);
        int cityPosition = preferences.getInt("position", -1);
        if (cities.size() != 0) {
            if (cityPosition != -1) {
                city = cities.get(cityPosition);
                for (int id : appWidgetIds) {
                    updateWidget(city, context, appWidgetManager, preferences, id);
                }
            } else {
                for (int id : appWidgetIds) {
                    updateWidget(cities.get(cities.size() - 1), context, appWidgetManager, preferences, id);
                }
            }
        }
    }

    private void updateWidget(String city, Context context, AppWidgetManager appWidgetManager, SharedPreferences preferences, int widgetId) {
        widgetView = new RemoteViews(mContext.getPackageName(), R.layout.layout_widget);
        URL currentUrl = createCurrentURL(city);
        new LoadWidgetWeatherTask(appWidgetManager, widgetId).execute(currentUrl);
    }

    private URL createCurrentURL(String city) {
        String apiKey = mContext.getString(R.string.api_key);
        String baseUrl = mContext.getString(R.string.current_web_service_url);
        try {
            String urlString = String.format(baseUrl, URLEncoder.encode(city, "UTF-8"), apiKey);
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //некорректный URL
    }

    //AsyncTask для загрузки изображений в отдельном потоке
    private class LoadWidgetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        private AppWidgetManager mWidgetManager;
        private int mWidgetId;

        public LoadWidgetWeatherTask(AppWidgetManager appWidgetManager, int widgetId) {
            this.mWidgetManager = appWidgetManager;
            this.mWidgetId = widgetId;
        }

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
                        e.printStackTrace();
                    }
                    return new JSONObject(jsonBuilder.toString());
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject rawJson) {
            try {
                JSONArray weatherData = rawJson.getJSONArray("data");
                JSONObject currentWeather = weatherData.getJSONObject(0);
                String cityName = currentWeather.getString("city_name");
                double temperature = currentWeather.getDouble("temp");
                String iconCode = currentWeather.getJSONObject("weather").getString("icon");
                widgetView.setTextViewText(R.id.widget_city_text_view, cityName);
                widgetView.setTextViewText(R.id.widget_temperature_text_view, Utilities.getFormatTemperature(temperature));
                mWidgetManager.updateAppWidget(mWidgetId, widgetView);
                URL iconUrl = new URL(String.format(mContext.getString(R.string.icon_service_url), iconCode));
                new LoadWidgetWeatherImageTask(mWidgetManager, mWidgetId).execute(iconUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class LoadWidgetWeatherImageTask extends AsyncTask<URL, Void, Bitmap> {
        private AppWidgetManager mWidgetManager;
        private int mWidgetId;

        public LoadWidgetWeatherImageTask(AppWidgetManager appWidgetManager, int widgetId) {
            this.mWidgetManager = appWidgetManager;
            this.mWidgetId = widgetId;
        }

        @Override
        protected Bitmap doInBackground(URL... params) {
            Bitmap bitmap;
            HttpURLConnection connection = null;

            try {
                URL url = params[0];
                connection = (HttpURLConnection) url.openConnection();
                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect(); //закрыть HttpURLConnection
                }
            }
            return bitmap;
        }

        //назначить изображение погодных условий элементу recyclerView
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            widgetView.setImageViewBitmap(R.id.widget_icon_image_view, bitmap);
            mWidgetManager.updateAppWidget(mWidgetId, widgetView);
        }
    }
}
