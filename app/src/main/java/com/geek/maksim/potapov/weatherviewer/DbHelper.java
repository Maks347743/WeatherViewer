package com.geek.maksim.potapov.weatherviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {
    Context mContext;
    private static final String DATABASE_NAME = "weather.db";
    static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    //перечисление с названиями таблиц и методом для получения строки с названием
    enum Tables {
        CITY, DAILY_WEATHER;
        public String getTableName(){
           return this == CITY ? "cities" : "daily_weather";
        }

        //внутреннее перечисление с именами столбцов таблицы "cities" и методом для получения строки с названием столбцов
        public enum ColumnsTableCity {
            ID, CITY;
            public String getColumnName(){
                return this == ID ? "_id" : "city";
            }
        }

        //внутреннее перечисление с именами столбцов таблицы "daily_weather" и методом для получения строки с названием столбцов
        public enum ColumnsTableDailyWeather {
            ID, CITY_ID, DAY_OF_WEEK, MIN_TEMP, MAX_TEMP, HUMIDITY, DESCRIPTION, ICON_URL;
            public String getColumnName(){
                switch (this){
                    case ID:
                        return "_id";
                    case CITY_ID:
                        return "city_id";
                    case DAY_OF_WEEK:
                        return "day_of_week";
                    case MIN_TEMP:
                        return "min_temp";
                    case MAX_TEMP:
                        return "max_temp";
                    case HUMIDITY:
                        return "humidity";
                    case DESCRIPTION:
                        return "description";
                    case ICON_URL:
                        return "icon_url";
                    default:
                        return null;
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //первая таблица содержит только город и уникальный id города
        db.execSQL("CREATE TABLE " + Tables.CITY.getTableName() + "(" +
                Tables.ColumnsTableCity.ID.getColumnName() + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Tables.ColumnsTableCity.CITY.getColumnName() + " TEXT NOT NULL UNIQUE);");

        //вторая таблица содержит погоду каждого выбранного города за 16 дней и связь с первой таблицей с помощью Foreign key
        db.execSQL("CREATE TABLE " + Tables.DAILY_WEATHER.getTableName() + "(" +
                Tables.ColumnsTableDailyWeather.ID.getColumnName() + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Tables.ColumnsTableDailyWeather.CITY_ID.getColumnName() + " INTEGER NOT NULL," +
                Tables.ColumnsTableDailyWeather.DAY_OF_WEEK.getColumnName() + " TEXT NOT NULL, " +
                Tables.ColumnsTableDailyWeather.MIN_TEMP.getColumnName() + " TEXT NOT NULL, " +
                Tables.ColumnsTableDailyWeather.MAX_TEMP.getColumnName() + " TEXT NOT NULL, " +
                Tables.ColumnsTableDailyWeather.HUMIDITY.getColumnName() + " TEXT NOT NULL, " +
                Tables.ColumnsTableDailyWeather.DESCRIPTION.getColumnName() + " TEXT NOT NULL, " +
                Tables.ColumnsTableDailyWeather.ICON_URL.getColumnName() + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //полностью удаляем БД и создаем заново
        mContext.deleteDatabase(DATABASE_NAME);
        onCreate(db);
    }
}
