package hk.edu.ouhk.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static hk.edu.ouhk.project.WeatherInfo.city;
import static hk.edu.ouhk.project.WeatherInfo.curLocale;
import static hk.edu.ouhk.project.WeatherInfo.iconList;

public class MainActivity extends AppCompatActivity {
    private static int key = 1;
    private String TAG = "MainActivity";
    private List<ChooseCityBean> list1;
    private Spinner spinner;
    private ImageView setting;
    private AlertDialog alertDialog;
    private Handler handler = new Handler();
    private static Calendar calendar;  // get the week of the day
    private static int week; // store the week of day
    private static HashMap<Integer, String> weekMap = new HashMap<>(7);
    private static HashMap cityMap = new HashMap();
    public Context context;
    NetWorkStateReceiver netWorkStateReceiver;
    JsonHandlerThread jsonHandlerThread;
    ConstraintLayout background_picture;
    ImageView weather_icon1, weather_icon2, weather_icon3, weather_icon4, weather_icon5, weather_icon6, weather_icon7,search_city;
    EditText weather_enter_city;
    TextView weather_currenttemp, weather_temp_city, weather_condition, weather_date, weather_today_tip, weather_tips;
    TextView weather_today, weather_today_temp, weather_nextoneday, weather_nextoneday_temp, weather_nexttwoday, weather_nexttwoday_temp,
            weather_nextthreeday, weather_nextthreeday_temp, weather_nextfourday, weather_nextfourday_temp, weather_nextfiveday, weather_nextfiveday_temp, weather_nextsixday, weather_nextsixday_temp;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        city = JsonHandlerThread.getCityJson("city.json", MainActivity.this);

        //用于解决网络连接不能放在主线程上的问题
        //to solve the problem of Internet connection can not be put on the main thread.
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        JsonHandlerThread.initialCityList();

        initialWeekMap();

        initializeView();

        initialCityHashmap();

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(setting);
            }
        });

        WeatherInfo.curLocale = getResources().getConfiguration().locale;
//        Application app = (Application) getApplication();
        WeatherInfo.context = getApplicationContext();

        loadCity();
        spinner.setOnItemSelectedListener(new SpinnerClickListener());
        weather_enter_city.setOnEditorActionListener(new OnEditorActionListener());

        createThread();

        getImageByReflect(WeatherInfo.WEATHER_background.toLowerCase());
        background_picture.getBackground().setAlpha(140);

        showInfo();

        weather_temp_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.dialog);
                builder.setTitle(R.string.More_Index);
                builder.setMessage(getString(R.string.Pressure) +" : "+ WeatherInfo.PRESSURE + "\n" + getString(R.string.Humidity) +" : "+ WeatherInfo.HUMIDITY
                        + "\n" + getString(R.string.Visibility) +" : "+ WeatherInfo.VISIBILITY + "\n" + getString(R.string.Wind_Speed) +" : "+ WeatherInfo.WINDSPEED
                        + "\n" +  getString(R.string.spf) + " : " + WeatherInfo.shai + "\n" + getString(R.string.uvIndex) + " : " + WeatherInfo.ray);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


    }

    //show the loading page
    public void showLoadingDialog() {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    //dismiss the loading page
    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public void initialWeekMap() {
        weekMap.put(0, getString(R.string.sun));
        weekMap.put(1, getString(R.string.mon));
        weekMap.put(2, getString(R.string.tue));
        weekMap.put(3, getString(R.string.wed));
        weekMap.put(4, getString(R.string.thu));
        weekMap.put(5, getString(R.string.fri));
        weekMap.put(6, getString(R.string.sat));
        calendar = Calendar.getInstance();
        week = calendar.get(Calendar.DAY_OF_WEEK) - 1;

    }

    public void initializeView() {

        search_city=findViewById(R.id.search_city);
        background_picture = findViewById(R.id.background_picture);

        setting = (ImageView) findViewById(R.id.weather_setting);

        weather_enter_city = (EditText) findViewById(R.id.weather_enter_city);

        spinner = (Spinner) findViewById(R.id.weather_spinner_city);

        weather_currenttemp = (TextView) findViewById(R.id.weather_currecttemp);
        weather_temp_city = (TextView) findViewById(R.id.weather_temp_city);
        weather_temp_city.setClickable(true);
        weather_condition = (TextView) findViewById(R.id.weather_condition);
        weather_date = (TextView) findViewById(R.id.weather_date);

        weather_today = (TextView) findViewById(R.id.weather_today);
        weather_today_temp = (TextView) findViewById(R.id.weather_today_temp);
        weather_nextoneday = (TextView) findViewById(R.id.weather_nextoneday);
        weather_nextoneday_temp = (TextView) findViewById(R.id.weather_nextoneday_temp);
        weather_nexttwoday = (TextView) findViewById(R.id.weather_nexttwoday);
        weather_nexttwoday_temp = (TextView) findViewById(R.id.weather_nexttwoday_temp);
        weather_nextthreeday = (TextView) findViewById(R.id.weather_nextthreeday);
        weather_nextthreeday_temp = (TextView) findViewById(R.id.weather_nextthreeday_temp);
        weather_nextfourday = (TextView) findViewById(R.id.weather_nextfourday);
        weather_nextfourday_temp = (TextView) findViewById(R.id.weather_nextfourday_temp);
        weather_nextfiveday = (TextView) findViewById(R.id.weather_nextfiveday);
        weather_nextfiveday_temp = (TextView) findViewById(R.id.weather_nextfiveday_temp);
        weather_nextsixday = (TextView) findViewById(R.id.weather_nextsixday);
        weather_nextsixday_temp = (TextView) findViewById(R.id.weather_nextsixday_temp);


        weather_today_tip = (TextView) findViewById(R.id.weather_today_tip);
        weather_tips = (TextView) findViewById(R.id.weather_tip);

        weather_icon1 = (ImageView) findViewById(R.id.weather_icon1);
        weather_icon2 = (ImageView) findViewById(R.id.weather_icon2);
        weather_icon3 = (ImageView) findViewById(R.id.weather_icon3);
        weather_icon4 = (ImageView) findViewById(R.id.weather_icon4);
        weather_icon5 = (ImageView) findViewById(R.id.weather_icon5);
        weather_icon6 = (ImageView) findViewById(R.id.weather_icon6);
        weather_icon7 = (ImageView) findViewById(R.id.weather_icon7);
    }

    // create thread for sending API request and store the data returned by the API
    public void createThread() {
        jsonHandlerThread = new JsonHandlerThread();
        jsonHandlerThread.start();
        try {
            jsonHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //city hashmap stores the cities that are showed in the drop-down spinner menu
    public void initialCityHashmap() {
        cityMap.put(0, "hongkong");
        cityMap.put(1, "macao");
        cityMap.put(2, "taiwan");
        cityMap.put(3, "beijing");
        cityMap.put(4, "shenzhen");
        cityMap.put(5, "guangzhou");
        cityMap.put(6, "shanghai");
    }

    //put the cities on the drop-down spinner menu layout.
    public void loadCity() {
        list1 = new ArrayList<>();
        ChooseCityBean spinnerCity;

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.hongkong));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.Macao));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.TaiWan));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.beijing));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.shenzhen));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.guangzhou));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.shanghai));
        list1.add(spinnerCity);

        spinnerCity = new ChooseCityBean();
        spinnerCity.setTitle(getString(R.string.enterCity));
        list1.add(spinnerCity);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, list1, R.layout.activity_item);
        spinner.setAdapter(adapter);

    }

    //method for rounding the temperature into integer
    public String floorTemp(String temp){
        double t = Double.valueOf(temp);
        return  "" + (int)Math.floor(t);
    }

    //method for showing the main Info in the main page
    public void showInfo() {

        weather_temp_city.setText(WeatherInfo.LOCATION);
        weather_condition.setText(WeatherInfo.WEATHER);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // HH:mm
        long l = new Long(WeatherInfo.DATE);
        Date d = new Date(l * 1000L);
        weather_date.setText(simpleDateFormat.format(d));

        weather_today_tip.setText( getString(R.string.tip));
        weather_tips.setText(
                "\n" + getString(R.string.sport) +": "+ WeatherInfo.sport + "\n" +
                        getString(R.string.dressing) +": "+ WeatherInfo.wear + "\n" +
                        getString(R.string.travel) +": "+ WeatherInfo.travel + "\n" +
                        getString(R.string.comfort) +": "+ WeatherInfo.comfort

        );

        if (WeatherInfo.units.equals("c")) {
            weather_currenttemp.setText(floorTemp(WeatherInfo.TEMPERATURE) + "℃");
            HashMap<String, String> weather = WeatherInfo.weatherList.get(0);
            weather_today.setText(R.string.Today);
            weather_today_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(1);
            weather_nextoneday.setText(weekMap.get((week + 1) % 7));
            weather_nextoneday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));


            weather = WeatherInfo.weatherList.get(2);
            weather_nexttwoday.setText(weekMap.get((week + 2) % 7));
            weather_nexttwoday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(3);
            weather_nextthreeday.setText(weekMap.get((week + 3) % 7));
            weather_nextthreeday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(4);
            weather_nextfourday.setText(weekMap.get((week + 4) % 7));
            weather_nextfourday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(5);
            weather_nextfiveday.setText(weekMap.get((week + 5) % 7));
            weather_nextfiveday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(6);
            weather_nextsixday.setText(weekMap.get((week + 6) % 7));
            weather_nextsixday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

        } else {
            weather_currenttemp.setText(floorTemp(WeatherInfo.fTEMPERATURE) + "°F");
            HashMap<String, String> fweather = WeatherInfo.fweatherList.get(0);
            weather_today_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(1);
            weather_nextoneday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(2);
            weather_nexttwoday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(3);
            weather_nextthreeday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(4);
            weather_nextfourday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(5);
            weather_nextfiveday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(6);
            weather_nextsixday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

        }

        try {
            weather_icon1.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(0))));
            weather_icon2.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(1))));
            weather_icon3.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(2))));
            weather_icon4.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(3))));
            weather_icon5.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(4))));
            weather_icon6.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(5))));
            weather_icon7.setImageBitmap(getBitmap(JsonHandlerThread.convertImage(iconList.get(6))));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //method for downloading a weather icon from a given URL
    public static Bitmap getBitmap(String path) throws IOException {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200){
            InputStream inputStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }

    // method for changing the background of the main page according to weather of the current city
    private void getImageByReflect(String imageName){
        try {
            Field field = Class.forName("hk.edu.ouhk.project.R$drawable").getField(imageName);
            background_picture.setBackgroundResource(field.getInt(field));
        } catch (Exception e) {
            background_picture.setBackgroundResource(R.drawable.clear);
        }
    }

    //set up the click listener of cities in drop-down spinner
    public class SpinnerClickListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    showLoadingDialog();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    ChooseCityBean model = (ChooseCityBean) adapterView.getItemAtPosition(i);
                    if (model.getTitle().equals("Enter City") || model.getTitle().equals("輸入城市")) {
                        weather_enter_city.setVisibility(View.VISIBLE);
                        search_city.setVisibility(View.VISIBLE);
                    } else {
                        weather_enter_city.setVisibility(View.INVISIBLE);
                        search_city.setVisibility(View.INVISIBLE);
                        weather_temp_city.setText(model.getTitle());
                        WeatherInfo.LOCATION = cityMap.get(i).toString();

                        if (key == 1) {
                            key -= 1;
                        } else {
                            createThread();
                            getImageByReflect(WeatherInfo.WEATHER_background.toLowerCase());
                            background_picture.getBackground().setAlpha(140);
                            showInfo();
                            weather_temp_city.setText(model.getTitle());
                        }

                    }
                    Runnable r3 = new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            Toast.makeText(getApplicationContext(), model.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    handler.postDelayed(r3, 1000);
                }
            };
            handler.post(r1);
            handler.postDelayed(r2, 2000);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    //set up the edit action listener of the edit text view that allow users to enter whatever city they want
    public class OnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEND || (event != null || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                WeatherInfo.LOCATION = weather_enter_city.getText().toString().trim();

                Resources resources = getResources();
                Configuration config = resources.getConfiguration();
                DisplayMetrics dm = resources.getDisplayMetrics();
                WeatherInfo.curLocale = getResources().getConfiguration().locale;
                if (curLocale.equals(Locale.TRADITIONAL_CHINESE)) {
                    showLoadingDialog();
                    createThread();
                    getImageByReflect(WeatherInfo.WEATHER_background.toLowerCase());
                    background_picture.getBackground().setAlpha(140);
                    for (int i = 0; i < WeatherInfo.cityList.size(); i++) {
                        HashMap<String, String> citymap = WeatherInfo.cityList.get(i);
                        Iterator<String> iter = citymap.keySet().iterator();
                        while (iter.hasNext()) {
                            String py = String.valueOf(iter.next());
                            if (WeatherInfo.LOCATION.equals(py)) {
                                WeatherInfo.LOCATION = citymap.get(py);
                                break;
                            }
                        }
                    }
                    dismissLoadingDialog();
                } else {
                    showLoadingDialog();
                    createThread();
                    getImageByReflect(WeatherInfo.WEATHER_background.toLowerCase());
                    background_picture.getBackground().setAlpha(140);
                    dismissLoadingDialog();
                }


                if (WeatherInfo.data == null) {
                    Toast.makeText(getApplicationContext(), R.string.invalidEnter, Toast.LENGTH_LONG).show();
                } else {
                    showInfo();
                }
            }

            return true;
        }


    }

    protected void switchLanguage(Locale language) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals(Locale.TRADITIONAL_CHINESE)) {
            config.locale = Locale.ENGLISH;
        } else {
            config.locale = Locale.TRADITIONAL_CHINESE;
        }
        resources.updateConfiguration(config, dm);

    }

    public void convertTemperature() {

        if (WeatherInfo.units.equals("c")) {

            weather_currenttemp.setText(floorTemp(WeatherInfo.fTEMPERATURE) + "°F");
            HashMap<String, String> fweather = WeatherInfo.fweatherList.get(0);
            weather_today_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(1);
            weather_nextoneday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(2);
            weather_nexttwoday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(3);
            weather_nextthreeday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(4);
            weather_nextfourday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(5);
            weather_nextfiveday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            fweather = WeatherInfo.fweatherList.get(6);
            weather_nextsixday_temp.setText(String.format("%s°F ~ %s°F", floorTemp(fweather.get(WeatherInfo.TEMP_MIN)), floorTemp(fweather.get(WeatherInfo.TEMP_MAX))));

            WeatherInfo.units = "f";

        } else {
            weather_currenttemp.setText(floorTemp(WeatherInfo.TEMPERATURE) + "℃");
            HashMap<String, String> weather = WeatherInfo.weatherList.get(0);
            weather_today_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)), floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(1);
            weather_nextoneday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(2);
            weather_nexttwoday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(3);
            weather_nextthreeday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(4);
            weather_nextfourday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(5);
            weather_nextfiveday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            weather = WeatherInfo.weatherList.get(6);
            weather_nextsixday_temp.setText(String.format("%s℃ ~ %s℃", floorTemp(weather.get(WeatherInfo.TEMP_MIN)),floorTemp(weather.get(WeatherInfo.TEMP_MAX))));

            WeatherInfo.units = "c";

        }

    }

    //method for showing the pop up menu in the top right corner
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        // menu layout
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.switch_lang:
                        WeatherInfo.curLocale = getResources().getConfiguration().locale;
                        switchLanguage(WeatherInfo.curLocale);
                        finish();
                        Intent it = new Intent(MainActivity.this, MainActivity.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(it);
//                        recreate();
                        Toast.makeText(getApplicationContext(), R.string.changeSuccessfully, Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.conver_tem:
                        convertTemperature();
                        break;

                    case R.id.lo_la:
                        String lonlat = getString(R.string.lon)+":" + JsonHandlerThread.coor2 + "\n"+getString(R.string.lat)+": " + JsonHandlerThread.coor1;
                        LonLatToast.showToast(MainActivity.this, lonlat);
                        break;

                    default:
                        break;

                }
                return false;
            }
        });
        //set up the dismiss listener to close the PopupMenu
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
//                Toast.makeText(getApplicationContext(), "off PopupMenu", Toast.LENGTH_SHORT).show();
            }
        });
        popupMenu.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkState_23orNew(){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = connMgr.getAllNetworks();
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < networks.length; i++){
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
        }
    }

    @Override
    protected void onResume() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
        System.out.println("注册");
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(netWorkStateReceiver);
        System.out.println("注销");
        super.onPause();
    }

}
