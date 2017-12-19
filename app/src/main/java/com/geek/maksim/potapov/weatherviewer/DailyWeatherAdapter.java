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

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder> {
    private Context mContext;
    //кэш для уже загруженных объектов Bitmap
    private static Map<String, Bitmap> mConditionImagesBank = new HashMap<>();
    private List<DailyWeather> mDailyForecast;

    DailyWeatherAdapter(Context context, List<DailyWeather> dailyForecast){
        mContext = context;
        mDailyForecast = dailyForecast;
    }

    @Override
    public DailyWeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.recycler_daily_item, parent, false);
        return new DailyWeatherViewHolder(itemView, mContext);
    }

    @Override
    public void onBindViewHolder(DailyWeatherViewHolder holder, int position) {
        DailyWeather dailyWeather = mDailyForecast.get(position);
        holder.bind(dailyWeather, mConditionImagesBank);
    }

    @Override
    public int getItemCount() {
        return mDailyForecast.size();
    }

    static class DailyWeatherViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView mConditionImageView;
        private TextView mDayTextView;
        private TextView mLowTextView;
        private TextView mHiTextView;

        public DailyWeatherViewHolder(View itemView, Context context) {
            super(itemView);
            mContext = context;
            mConditionImageView = itemView.findViewById(R.id.conditionImageView);
            mDayTextView = itemView.findViewById(R.id.day_of_week_text_view);
            mLowTextView = itemView.findViewById(R.id.min_temp_text_view);
            mHiTextView = itemView.findViewById(R.id.max_temp_text_view);
        }

        public void bind(DailyWeather dailyWeather, Map<String, Bitmap> images){
            //если значок погодных условий уже загружен, использовать его
            //иначе - загрузить в отдельном потоке
            if (images.containsKey(dailyWeather.getIconURL())){
                mConditionImageView.setImageBitmap(images.get(dailyWeather.getIconURL()));
            } else {
                //загрузить и вывести значок погодных условий
                new LoadImageTask(mConditionImageView).execute(dailyWeather.getIconURL());
            }
            //заполнить view необходимым текстом
            mDayTextView.setText(dailyWeather.getDayOfWeek());
            mLowTextView.setText(mContext.getString(R.string.low_temp, dailyWeather.getMinTemp()));
            mHiTextView.setText(mContext.getString(R.string.high_temp, dailyWeather.getMaxTemp()));
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
