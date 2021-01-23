package com.example.whatstheweather;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    public TextView locationTextView;
    public TextView descriptionTextView;
    public TextView temperatureTextView;
    public TextView otherTempTextView;

    public void checkWeather(View view){
        EditText editText = findViewById(R.id.cityText);
        String userInput = editText.getText().toString();
        DownloadJSON task = new DownloadJSON();
        String json = null;
        try {
            Log.i("Info", "JSON Data: Sending url...");
            json = task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + userInput + "&units=metric&appid=81058edabded05118c8d2885b7b98408").get();
            Log.i("Info", "JSON Data: Received! - " + json);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assert json != null;
        if(userInput.isEmpty() || json.contains("Failed") || json.contains("\"cod\":\"4")){
            Toast.makeText(this, "This city doesn't exist!!! \n Enter a valid City", Toast.LENGTH_LONG).show();
        }else {
            locationTextView.setText(new StringBuffer("~ Current Weather in " + userInput.toUpperCase() + " ~"));
            displayWeatherReport(json);
            displayTemperatureReport(json);
            dismissKeyboard(this);
        }
    }


    /**
     * executes method to display Weather Report
     *
     * @param json containing string of json data downloaded from the internet
     */

    private void displayWeatherReport(String json) {
        try{
            JSONObject jsonObject = new JSONObject(json);
            String weatherInfo = jsonObject.getString("weather");
            Log.i("Info", "WeatherJSON: " + weatherInfo);
            JSONArray jsonArray = new JSONArray(weatherInfo);
            String main = "", description = "";
            for(int i= 0; i < jsonArray.length(); i++){
                JSONObject partObject = jsonArray.getJSONObject(i);
                main = partObject.getString("main");
                Log.i("Info", "main: " + main);
                description = partObject.getString("description");
                Log.i("Info","description: " + description);
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(main).append(": ").append(description);
            descriptionTextView.setText(stringBuffer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * dismisses kwyboard after button press
     * @param activity current activity
     */
    public void dismissKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != activity.getCurrentFocus()) {
            assert imm != null;
            imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getApplicationWindowToken(), 0);
        }
    }


    private void displayTemperatureReport(String json) {
        try{
            JSONObject jsonObject = new JSONObject(json);
            String tempInfo = jsonObject.getString("main");
            Log.i("Info", "TemperatureJSON: " + tempInfo);

            String temp, feels_like, temp_min, temp_max;
            JSONObject tempObject = new JSONObject(tempInfo);

            temp = tempObject.getString("temp");
            feels_like = tempObject.getString("feels_like");
            temp_min = tempObject.getString("temp_min");
            temp_max = tempObject.getString("temp_max");
            int[] tempList = {(int) Double.parseDouble(temp), (int) Double.parseDouble(feels_like), (int) Double.parseDouble(temp_min), (int) Double.parseDouble(temp_max)};

            Log.i("Info", "temp: " + temp);
            Log.i("Info", "feels_like: " + feels_like);
            Log.i("Info", "temp_min: " + temp_min);
            Log.i("Info", "temp_max: " + temp_max);

            temperatureTextView.setText(new StringBuffer(tempList[0] + "\u00B0C"));

            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Feels Like").append(": ").append(tempList[1]).append("\u00B0C")
                    .append("\n").append("min: ").append(tempList[2]).append("\u00B0C  --  ")
                    .append("max: ").append(tempList[3]).append("\u00B0C");
            otherTempTextView.setText(stringBuffer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTextView = findViewById(R.id.locationTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        otherTempTextView = findViewById(R.id.otherTempTextView);
    }


    /**
     *  DownloadJSON class to execute background process for downloading JSON from the internet
     */

    public static class DownloadJSON extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder json = new StringBuilder();
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                Log.i("Info", "JSON download: Starting...");
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char current = (char)data;
                    json.append(current);
                    data = reader.read();
                }
                Log.i("Info", "JSON download: Completed successfully.");
                return json.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Error", "JSON download: Failed!!!");
                return "Failed";
            }
        }
    }
}
