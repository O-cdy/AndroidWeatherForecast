package hk.edu.ouhk.project;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;


public class JsonHandlerThread extends Thread{
    private static final String TAG = "JsonHandlerThread";
    public static String location="";
    private static String lat = null;
    private static String lon = null;
    public static String coor1;
    public static String coor2;
    private static String jsonUrl_1, jsonUrl_2, jsonUrl_3, jsonUrl_4 = "";


    //concatenate the given icon id to form different API request
    public static String convertImage(String id){
        jsonUrl_4 = "http://openweathermap.org/img/wn/" + id + "@2x.png";
        return jsonUrl_4;
    }

    //concatenate the given city's name to form different API request
    public void convertCity(String loca){
        location = loca;

        if(WeatherInfo.curLocale.equals(Locale.TRADITIONAL_CHINESE)){
            jsonUrl_1 = "http://api.openweathermap.org/data/2.5/weather?q="+location+"&units=metric&lang=zh_tw&APPID=6ebc0b56445628de28cd6a12402b448c";
        } else {
            jsonUrl_1 = "http://api.openweathermap.org/data/2.5/weather?q="+location+"&units=metric&APPID=6ebc0b56445628de28cd6a12402b448c";
        }
    }

    //concatenate the given Longitude and latitude to form different API request
    public void convertLatLon(String la, String lo){
        lat = la;
        lon = lo;
        jsonUrl_2 = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=minutely,hourly&units=metric&appid=6ebc0b56445628de28cd6a12402b448c";

        if(WeatherInfo.curLocale.equals(Locale.TRADITIONAL_CHINESE)){
            jsonUrl_3 = "https://devapi.qweather.com/v7/indices/1d?type=1,3,5,6,8,16&location="+lon+","+lat+"&key=bf7be9be29f6473ca32585e576723c6b";
        } else {
            jsonUrl_3 = "https://devapi.qweather.com/v7/indices/1d?type=1,3,5,6,8,16&location="+lon+","+lat+"&lang=en&key=bf7be9be29f6473ca32585e576723c6b";
        }
    }

    //make API request
    public static String makeRequest(String jsonurl){
        String response = null;
        try {
            URL url = new URL(jsonurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(conn.getInputStream());

            response = inputStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    //convert the inputstream returned from URL into String
    private static String inputStreamToString(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            }
        }
        return sb.toString();
    }

    //get the pinyin and corresponding Chinese of cities of China from the file in assets folder
    public static String getCityJson(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    // initialize the city list of China with the pinyin and corresponding Chinese
    public static void initialCityList(){
        try {
            JSONArray cityArray = new JSONArray(WeatherInfo.city);
            for(int i=0;i<cityArray.length();i++){
                JSONObject cityInfo = cityArray.getJSONObject(i);
                String pinyin = cityInfo.getString("pinyin");
                String name = cityInfo.getString("name");
                HashMap<String, String> citymap = new HashMap<>();
                citymap.put(pinyin,name);

                WeatherInfo.cityList.add(citymap);
            }
        }catch (Exception e){

        }
    }

    //get the data from API in a thread
    public void run(){
        Log.e(TAG, "WeatherInfo.LOCATION: " + WeatherInfo.LOCATION);
        this.convertCity(WeatherInfo.LOCATION);
        String Info = makeRequest(jsonUrl_1);
        Log.e(TAG, "url_1: " + jsonUrl_1);
        Log.e(TAG, "Response from url_1: " + Info);
        JSONObject jsonObj = null;
        coor1 = null;
        coor2 = null;
        if(Info != null){
            try{
                jsonObj = new JSONObject(Info);
                String visibility = jsonObj.getString("visibility");
                JSONArray weather = jsonObj.getJSONArray("weather");
                JSONObject w = weather.getJSONObject(0);
                String w1 = w.getString("description");
                String w2 = w.getString("main");

                JSONObject main = jsonObj.getJSONObject("main");
                String temp = main.getString("temp");
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                JSONObject wind = jsonObj.getJSONObject("wind");
                String windspeed = wind.getString("speed");

                double ftemp=Double.parseDouble(main.getString("temp"))*33.8;
                ftemp=Math.round(ftemp*1000)/1000.0;
                String ftemp1 = String.valueOf(ftemp);

                String date = jsonObj.getString("dt");
                String location = jsonObj.getString("name");

                JSONObject coord = jsonObj.getJSONObject("coord");
                coor1 = coord.getString("lat");
                coor2 = coord.getString("lon");
                WeatherInfo.setWeatherInfo(location, temp, w1, w2, date,pressure,humidity,visibility,windspeed);
                WeatherInfo.data = "success";
                WeatherInfo.fTEMPERATURE=ftemp1;
            }catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        } else {
            Log.e(TAG,"Couldn't get json from server.");
            WeatherInfo.data = null;
        }

        convertLatLon(coor1,coor2);
        Log.e(TAG, "url_2: " + jsonUrl_2);
        String Info2 = makeRequest(jsonUrl_2);
        Log.e(TAG, "Response from url_2: " + Info2);
        WeatherInfo.initialIconList();
        if(Info2 != null){
            try{
                JSONObject jsonObj2 = new JSONObject(Info2);
                JSONArray daily = jsonObj2.getJSONArray("daily");

                for(int i = 0;i < 7;i++){
                    JSONObject d = daily.getJSONObject(i);
                    // get the temperature data
                    String time = d.getString("dt");
                    JSONObject t = d.getJSONObject(("temp"));
                    String tmin = t.getString("min");
                    String tmax = t.getString("max");
                    JSONArray w = d.getJSONArray("weather");
                    JSONObject id = w.getJSONObject(0);
                    String icon = id.getString("icon");
                    WeatherInfo.iconList.set(i,icon);

                    double ftmin=Double.parseDouble(tmin)*33.8;
                    ftmin=Math.round(ftmin*1000)/1000.0;
                    double ftmax=Double.parseDouble(tmax)*33.8;
                    ftmax=Math.round(ftmax*1000)/1000.0;
                    String ftmin1= String.valueOf(ftmin);
                    String ftmax1= String.valueOf(ftmax);

                    WeatherInfo.addWeather(i,tmin,tmax);
                    WeatherInfo.faddWeather(i,ftmin1,ftmax1);

                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        } else {
            Log.e(TAG,"Couldn't get json from server.");
        }


        Log.e(TAG, "url_3: " + jsonUrl_3);
        String Info3 = makeRequest(jsonUrl_3);
        Log.e(TAG, "Response from url_3: " + Info3);
        if(Info3!= null){
            try{
                jsonObj = new JSONObject(Info3);
                JSONArray daily = jsonObj.getJSONArray("daily");
                String ray = daily.getJSONObject(0).getString("category");
                String comfort = daily.getJSONObject(1).getString("category");
                String sport = daily.getJSONObject(2).getString("category");
                String wear = daily.getJSONObject(3).getString("category");
                String travel = daily.getJSONObject(4).getString("category");
                String shai = daily.getJSONObject(5).getString("category");


                WeatherInfo.settips(sport,wear,ray,travel,comfort,shai);

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        } else {
            Log.e(TAG,"Couldn't get json from server.");
        }



    }
}
