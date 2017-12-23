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
        this.mCurrentDescription = currentDescription;
        if (numberFormat.format(currentTemperature).equals("-0")){
            this.mCurrentTemperature = "0" + "\u00B0";
        } else {
            this.mCurrentTemperature = numberFormat.format(currentTemperature) + "\u00B0";
        }
        this.mCurrentHour = Utilities.convertStringToHour(dateTime);
        this.mIconURL = "https://www.weatherbit.io/static/img/icons/" + iconCode + ".png";
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
