package com.geek.maksim.potapov.weatherviewer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DailyWeather {

    private final String mDayOfWeek;
    private final String mMinTemp;
    private final String mMaxTemp;
    private final String mIconURL;

    public DailyWeather(long timeStamp, double minTemp, double maxTemp, double humidity, String description, String iconName){
        //NumberFormat для форматирования температур в целое число
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        this.mDayOfWeek = Utilities.convertTimeStampToDay(timeStamp);
        this.mMinTemp = numberFormat.format(minTemp) + "\u00B0";
        this.mMaxTemp = numberFormat.format(maxTemp) + "\u00B0";
        this.mIconURL = "https://www.weatherbit.io/static/img/icons/" + iconName + ".png";
    }

    public String getDayOfWeek() {
        return mDayOfWeek;
    }

    public String getMinTemp() {
        return mMinTemp;
    }

    public String getMaxTemp() {
        return mMaxTemp;
    }

    public String getIconURL() {
        return mIconURL;
    }
}
