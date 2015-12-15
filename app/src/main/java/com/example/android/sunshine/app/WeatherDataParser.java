package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by huangq on 14/12/2015.
 */
public class WeatherDataParser {
    private static final String LOG_TAG = "WeatherDataParser";

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static String[] getWeatherdata(Context context, String weatherJsonStr)
            throws JSONException {
        ArrayList<String> result = new ArrayList<String>();
        JSONObject weather = new JSONObject(weatherJsonStr);
        JSONArray weathers = weather.getJSONArray("list");

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        for(int i =0; i<weathers.length(); i++){
            String day;

            JSONObject targetedDay = weathers.getJSONObject(i);
            JSONObject temperature = targetedDay.getJSONObject("temp");
            double max = temperature.getDouble("max");
            double min = temperature.getDouble("min");

            String description = targetedDay.getJSONArray("weather").getJSONObject(0).getString("description");

            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            result.add(day + " - " + description + " - " + formatHighLows(context, max, min));
        }

        for (String s : result) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }

        return result.toArray(new String[result.size()]);
    }

    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private static String formatHighLows(Context context, double high, double low) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = pref.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.pref_unit_default));

        if(unit.equals(context.getString(R.string.pref_unit_imperial))){
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }
}
