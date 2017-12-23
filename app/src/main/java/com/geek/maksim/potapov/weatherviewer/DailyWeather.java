package com.geek.maksim.potapov.weatherviewer;

import java.math.RoundingMode;
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
        if (numberFormat.format(minTemp).equals("-0")){
            this.mMinTemp = "0" + "\u00B0";
        } else {
            this.mMinTemp = numberFormat.format(minTemp) + "\u00B0";
        }
        if (numberFormat.format(maxTemp).equals("-0")){
            this.mMaxTemp = "0" + "\u00B0";
        } else {
            this.mMaxTemp = numberFormat.format(maxTemp) + "\u00B0";
        }
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
