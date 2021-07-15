package hk.edu.ouhk.project;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class WeatherInfo {
    public static String LOCATION = "HongKong";
    public static String TEMPERATURE = "temperature";
    public static String TEMP_MAX = "tempe_max";
    public static String TEMP_MIN = "tempe_min";

    public static String PRESSURE = "";
    public static String HUMIDITY = "";
    public static String VISIBILITY = "";
    public static String WINDSPEED = "";

    public static String fTEMPERATURE = "temperature";
    public static String WEATHER = "weather";
    public static String WEATHER_background = "weather";
    public static String DATE = "date";
    public static Context context;
    public static Locale curLocale;
    public static String data = "success";
    public static String city;
    public static String units="c";
    public static String sport="";
    public static String wear="";
    public static String ray="";
    public static String travel="";
    public static String comfort="";
    public static String shai="";

    public static ArrayList<HashMap<String,String>> weatherList = new ArrayList<>(7);
    public static ArrayList<HashMap<String,String>> fweatherList = new ArrayList<>(7);
    public static ArrayList<HashMap<String,String>> cityList = new ArrayList<>();
    public static ArrayList<String> iconList = new ArrayList<>(7);

    public static void setWeatherInfo(String location, String temperature, String weather1, String weather2, String date,
                                      String pressure, String humidity, String visibility, String windspeed){
        LOCATION = location;
        TEMPERATURE = temperature;
        WEATHER = weather1;
        WEATHER_background = weather2;
        DATE = date;
        PRESSURE = pressure;
        HUMIDITY = humidity;
        VISIBILITY = visibility;
        WINDSPEED = windspeed;
    }

    public static void initialWeatherList(){
        HashMap<String,String> init = new HashMap<>();
        for(int i=0;i<3;i++){
            init.put("","");
        }
        weatherList.add(init);
        fweatherList.add(init);
    }

    public static void initialIconList(){
        for (int i=0;i<7;i++){
            iconList.add("");
        }
    }
//    public static void setLocation(String location){
//        LOCATION = location;
//    }

    public static void addWeather(int index, String temp_min,String temp_max){
        //add the weather of a day
        initialWeatherList();
        HashMap<String, String> weather = new HashMap<>();
        weather.put(TEMP_MIN,temp_min);
        weather.put(TEMP_MAX,temp_max);
        //add the weather of a day to weatherList
        weatherList.set(index,weather);

    }
    public static void faddWeather(int index, String temp_min,String temp_max){
        //add the weather of a day
        initialWeatherList();
        HashMap<String, String> weather = new HashMap<>();
        weather.put(TEMP_MIN,temp_min);
        weather.put(TEMP_MAX,temp_max);
        //add the weather of a day to weatherList
        fweatherList.set(index,weather);

    }


    public static void settips(String asport,String awear,String aray,String atravel,String acomfort,String ashai){
        sport=asport;
        wear=awear;
        ray=aray;
        travel=atravel;
        comfort=acomfort;
        shai=ashai;

    }
}
