package com.geek.maksim.potapov.weatherviewer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Weather {

    private final String mDayOfWeek;
    private final String mMinTemp;
    private final String mMaxTemp;
    private final String mHumidity;
    private final String mDescription;
    private final String mIconURL;

    public Weather(long timeStamp, double minTemp, double maxTemp, double humidity, String description, String iconName){
        //NumberFormat для форматирования температур в целое число
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        this.mDayOfWeek = convertTimeStampToDay(timeStamp);
        this.mMinTemp = numberFormat.format(minTemp) + "\u00B0C";
        this.mMaxTemp = numberFormat.format(maxTemp) + "\u00B0C";
        this.mHumidity = NumberFormat.getPercentInstance().format(humidity / 100);
        this.mDescription = description;
        this.mIconURL = "https://www.weatherbit.io/static/img/icons/" + iconName + ".png";
        //this.mIconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
    }

    //преобразование временной метки в название дня недели (Monday, ...)
    private static String convertTimeStampToDay(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000); //получение времени
        //получение дефолтного часового пояса устройства
        TimeZone timeZone = TimeZone.getDefault();
        //поправка на часовой пояс устройства
        calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
        //возвращение названия дня недели
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        return dateFormatter.format(calendar.getTime());
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

    public String getHumidity() {
        return mHumidity;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getIconURL() {
        return mIconURL;
    }
}
