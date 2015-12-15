package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by huangq on 11/12/2015.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> weatherForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            FetchWeatherTask task = new FetchWeatherTask();
            task.execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weather_data = new ArrayList<String>();

        weather_data.add("Today - Sunny - 88/63");
        weather_data.add("Tomorrow - Foggy - 70/64");
        weather_data.add("Weds - Cloudy - 72/63");
        weather_data.add("Thurs - Rainy - 64/51");
        weather_data.add("Fri - Foggy - 70/46");
        weather_data.add("Sat - Sunny - 76/68");

        weatherForecastAdapter = new ArrayAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weather_data);

        ListView list = (ListView) rootView.findViewById(R.id.listview_forecast);
        list.setAdapter(weatherForecastAdapter);

        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String forecast = weatherForecastAdapter.getItem(position);
                        //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra("forecast", forecast);

                        startActivity(intent);
                    }
                }
        );

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;
            String APPID = "fff8503d0d822e73277a1bd17c141f14";

            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, APPID)
                        .build();

                Log.d(LOG_TAG, "BUILD URI: " + builtUri.toString());

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }
                forecastJsonStr = buffer.toString();

            }catch(IOException e){
                Log.e(LOG_TAG, "Error", e);
                return null;
            }finally{
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            Log.v(LOG_TAG, "Forecast JSON string: " + forecastJsonStr);

            try {
                String[] parsedWeatherData = WeatherDataParser.getWeatherdata(forecastJsonStr);

                return parsedWeatherData;
            }catch(JSONException e){
                Log.e(LOG_TAG, "JSON error", e);
            }
            return null;
        }

        protected void onPostExecute(String[] weatherData) {
            if(weatherData != null){
                weatherForecastAdapter.clear();
                for(String data: weatherData)
                    weatherForecastAdapter.add(data);

            }

        }
    }
}