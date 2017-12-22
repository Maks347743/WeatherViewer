package com.geek.maksim.potapov.weatherviewer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Utilities {

    //преобразование временной метки в название дня недели (Monday, ...)
    public static String convertTimeStampToDay(long timeStamp) {
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

    public static String convertStringToHour(String datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
        Date date = null;
        try {
            date = dateFormat.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = GregorianCalendar.getInstance(timeZone);
        int offset = Math.abs(timeZone.getOffset(calendar.getTimeInMillis())/3600000);
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, offset);
        return String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
    }
}
