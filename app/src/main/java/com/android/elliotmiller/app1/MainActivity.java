package com.android.elliotmiller.app1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.TestLooperManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner spinnerCities;
    private AsyncHandler handler;
    private View progress;
    private View output;
    private TextView tvCity, tvOtherDetails, tvTemprature, tvLastUpdated, tvSearchHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.view_progress);
        output = findViewById(R.id.view_output);

        tvCity = (TextView)findViewById(R.id.tv_city);
        tvOtherDetails = (TextView)findViewById(R.id.tv_other_details);
        tvTemprature = (TextView)findViewById(R.id.tv_temprature);
        tvLastUpdated = (TextView)findViewById(R.id.tv_last_updated);
        tvSearchHint = (TextView)findViewById(R.id.tv_search_hint);

        spinnerCities = (Spinner) findViewById(R.id.spinner_city);
        populateSpinner();
        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 1: {
                        // LA
                        clearOutputs();
                        updateWeatherData("5368381");
                        break;
                    }
                    case 2: {
                        // NY
                        clearOutputs();
                        updateWeatherData("5128581");
                        break;
                    }
                    default: {
                        clearOutputs();
                        showNoSearch();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        showNoSearch();
    }

    private void showNoSearch() {
        output.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        tvSearchHint.setVisibility(View.VISIBLE);
    }

    private void showSearchProgress() {
        tvSearchHint.setVisibility(View.GONE);
        output.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    private void showSearchOutput() {
        tvSearchHint.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        output.setVisibility(View.VISIBLE);
    }
    private void setOutputs(String city, String temptrature, String otherDetails, String lastUpdated) {
        tvCity.setText(city);
        tvTemprature.setText(temptrature);
        tvOtherDetails.setText(otherDetails);
        tvLastUpdated.setText("Last update: " + lastUpdated);
    }
    private void clearOutputs() {
        this.setOutputs("", "", "", "");
    }

    private void updateWeatherData(final String cityId) {
        if (handler != null) {
            handler.cancel(true);
            handler = null;
        }
        handler = new AsyncHandler();
        handler.execute(cityId);
    }

    private void renderWeather(JSONObject json) {
        try {
            String cityData = json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country");

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            String detailsData =
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa";

            String tempratureData = String.format("%.2f", main.getDouble("temp")) + " ℃";

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            setOutputs(cityData, tempratureData, detailsData, updatedOn);
        } catch (Exception e) {
            Log.e("App1 Error", e.toString());
            setOutputs("N/A", "N/A", "N/A", "N/A");
        }
    }

    private void populateSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCities.setAdapter(adapter);
    }

    private class AsyncHandler extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSearchProgress();
        }

        protected JSONObject doInBackground(String... cityIds) {
            return ReportFetcher.getCityWeatherJSON(getApplicationContext(), cityIds[0]);
        }


        protected void onPostExecute(JSONObject result) {
            showSearchOutput();
            renderWeather(result);
        }
    }
}
