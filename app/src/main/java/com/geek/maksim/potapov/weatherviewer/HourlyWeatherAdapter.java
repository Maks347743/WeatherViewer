package com.geek.maksim.potapov.weatherviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherViewHolder>  {

    private Context mContext;
    //кэш для уже загруженных объектов Bitmap
    private static Map<String, Bitmap> mConditionImagesBank = new HashMap<>();
    private List<HourlyWeather> mHourlyForecast;

    HourlyWeatherAdapter(Context context, List<HourlyWeather> hourlyForecast){
        mContext = context;
        mHourlyForecast = hourlyForecast;
    }

    @Override
    public HourlyWeatherAdapter.HourlyWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.recycler_hourly_item, parent, false);
        return new HourlyWeatherAdapter.HourlyWeatherViewHolder(itemView, mContext);
    }

    @Override
    public void onBindViewHolder(HourlyWeatherAdapter.HourlyWeatherViewHolder holder, int position) {
        HourlyWeather hourlyWeather = mHourlyForecast.get(position);
        holder.bind(hourlyWeather, mConditionImagesBank);
    }

    @Override
    public int getItemCount() {
        return mHourlyForecast.size();
    }

    static class HourlyWeatherViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView mConditionImageView;
        private TextView mTempTextView;
        private TextView mHourTextView;

        public HourlyWeatherViewHolder(View itemView, Context context) {
            super(itemView);
            mContext = context;
            mConditionImageView = itemView.findViewById(R.id.hourly_condition_image_view);
            mTempTextView = itemView.findViewById(R.id.hourly_temperature_text_view);
            mHourTextView = itemView.findViewById(R.id.hour_text_view);
        }

        public void bind(HourlyWeather hourlyWeather, Map<String, Bitmap> images){
            //если значок погодных условий уже загружен, использовать его
            //иначе - загрузить в отдельном потоке
            if (images.containsKey(hourlyWeather.getIconURL())){
                mConditionImageView.setImageBitmap(images.get(hourlyWeather.getIconURL()));
            } else {
                //загрузить и вывести значок погодных условий
                new HourlyWeatherAdapter.HourlyWeatherViewHolder.LoadImageTask(mConditionImageView).execute(hourlyWeather.getIconURL());
            }
            //заполнить view необходимым текстом
            mHourTextView.setText(hourlyWeather.getCurrentHour());
            mTempTextView.setText(hourlyWeather.getCurrentTemperature());
        }

        //AsyncTask для загрузки изображений в отдельном потоке
        private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
            private ImageView mImageView; //для вывода изображения

            public LoadImageTask(ImageView imageView) {
                this.mImageView = imageView;
            }

            //загрузить изображение, params[0] содержит URL-адрес изображения
            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap bitmap;
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();

                    try(InputStream inputStream = connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        mConditionImagesBank.put(params[0], bitmap); //кэширование
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
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
