package com.geek.maksim.potapov.weatherviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public class WeatherDataSource {
    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;

    public WeatherDataSource(Context context){
        this.mDbHelper = new DbHelper(context);
        this.mDb = mDbHelper.getWritableDatabase();
        mDb.setForeignKeyConstraintsEnabled(true);
    }

    public void updateCityWeather(String city, List<Weather> weatherList){
        //проверяем, есть ли такой город в таблице городов
        int cityId = getCityIdFromDb(city);
        if (cityId != -1){
            updateDailyCityWeather(weatherList, cityId);
        } else {
            mDb.beginTransaction();
            ContentValues newCity = new ContentValues();
            newCity.put(DbHelper.Tables.ColumnsTableCity.CITY.getColumnName(), city);
            mDb.insert(DbHelper.Tables.CITY.getTableName(), null, newCity);
            cityId = getCityIdFromDb(city);
            for (int i = 0; i < weatherList.size(); i++) {
                ContentValues dailyWeather = new ContentValues();
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.CITY_ID.getColumnName(), cityId);
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.DAY_OF_WEEK.getColumnName(), weatherList.get(i).getDayOfWeek());
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.MIN_TEMP.getColumnName(), weatherList.get(i).getMinTemp());
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.MAX_TEMP.getColumnName(), weatherList.get(i).getMaxTemp());
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.HUMIDITY.getColumnName(), weatherList.get(i).getHumidity());
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.DESCRIPTION.getColumnName(), weatherList.get(i).getDescription());
                dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.ICON_URL.getColumnName(), weatherList.get(i).getIconURL());
                mDb.insert(DbHelper.Tables.DAILY_WEATHER.getTableName(), null, dailyWeather);
            }
            mDb.endTransaction();
        }
    }

    //если есть город в таблице, то возвращается поле city_id,
    //если города нет, возвращается -1
    private int getCityIdFromDb(String city) {
        Cursor cursor = mDb.query(DbHelper.Tables.CITY.getTableName(), null, DbHelper.Tables.ColumnsTableCity.CITY.getColumnName() + "= ?", new String[] {city}, null, null, null);
        if (cursor.getCount() == 0) {
            return -1;
        } else {
            cursor.moveToFirst();
            int cityId = cursor.getInt(0);
            cursor.close();
            return cityId;
        }
    }


    public void updateDailyCityWeather(List<Weather> weatherList, int cityId){
        //получаем предыдущий список погоды за 16 дней этого города
        Cursor cursor = mDb.query(DbHelper.Tables.DAILY_WEATHER.getTableName(),
                new String[]{DbHelper.Tables.ColumnsTableDailyWeather.ID.getColumnName()},
                DbHelper.Tables.ColumnsTableDailyWeather.CITY_ID + "= ?",
                new String[]{String.valueOf(cityId)}, null, null,
                DbHelper.Tables.ColumnsTableDailyWeather.ID.getColumnName());
        cursor.moveToFirst();

        mDb.beginTransaction();
        for (int i = 0; i < weatherList.size(); i++) {
            //кладем новые данные Map
            ContentValues dailyWeather = new ContentValues();
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.CITY_ID.getColumnName(), cityId);
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.DAY_OF_WEEK.getColumnName(), weatherList.get(i).getDayOfWeek());
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.MIN_TEMP.getColumnName(), weatherList.get(i).getMinTemp());
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.MAX_TEMP.getColumnName(), weatherList.get(i).getMaxTemp());
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.HUMIDITY.getColumnName(), weatherList.get(i).getHumidity());
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.DESCRIPTION.getColumnName(), weatherList.get(i).getDescription());
            dailyWeather.put(DbHelper.Tables.ColumnsTableDailyWeather.ICON_URL.getColumnName(), weatherList.get(i).getIconURL());
            int id = cursor.getInt(0);
            mDb.update(DbHelper.Tables.DAILY_WEATHER.getTableName(), dailyWeather, DbHelper.Tables.ColumnsTableDailyWeather.ID.getColumnName() + "= ?", new String[]{String.valueOf(id)});
            cursor.moveToNext();
        }
        cursor.close();
        mDb.endTransaction();
    }
}
