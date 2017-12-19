package com.geek.maksim.potapov.weatherviewer;

import java.text.NumberFormat;

public class HourlyWeather {

    private final String mCurrentDescription;
    private final String mCurrentTemperature;
    private final String mCurrentHour;
    private final String mIconURL;


    public HourlyWeather(String currentDescription, double currentTemperature, String dateTime, String iconCode) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        mCurrentDescription = currentDescription;
        mCurrentTemperature = numberFormat.format(currentTemperature) + "\u00B0";
        mCurrentHour = Utilities.convertStringToHour(dateTime);
        mIconURL = "https://www.weatherbit.io/static/img/icons/" + iconCode + ".png";
    }

    public String getCurrentDescription() {
        return mCurrentDescription;
    }

    public String getCurrentTemperature() {
        return mCurrentTemperature;
    }

    public String getCurrentHour() {
        return mCurrentHour;
    }

    public String getIconURL() {
        return mIconURL;
    }
}
