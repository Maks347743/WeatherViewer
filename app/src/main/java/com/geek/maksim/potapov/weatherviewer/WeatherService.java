package com.geek.maksim.potapov.weatherviewer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends Service {
    private final WeatherBinder mWeatherBinder = new WeatherBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("WeatherService", "onBind");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable getWeatherTask = () -> {
            URL url = (URL) intent.getSerializableExtra("URL");
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            jsonBuilder.append(line).append("\n");
                        }
                        // сообщаем об окончании задачи
                        Intent result = new Intent(ListWeatherFragment.BROADCAST_ACTION);
                        //если 1 - значит таск успешно завершен
                        result.putExtra(ListWeatherFragment.TASK_STATUS, 1);
                        result.putExtra(ListWeatherFragment.JSON, jsonBuilder.toString());
                        sendBroadcast(result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d("ConnectionFailed", "Connection failed");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null)
                connection.disconnect();
            }
        };
        executor.execute(getWeatherTask);
        return mWeatherBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("WeatherService", "onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("WeatherService", "onRebind");
        super.onRebind(intent);
    }

    class WeatherBinder extends Binder {
        public WeatherService getService(){
            return WeatherService.this;
        }
    }
}
