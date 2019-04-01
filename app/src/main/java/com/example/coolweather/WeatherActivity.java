package com.example.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private Button navButton;
    private ScrollView weatherlayout;
    private LinearLayout forcastlayout;
    private TextView titileCity;
    private TextView titleUpdateTime;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipRefresh;
    private  String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        swipRefresh=(SwipeRefreshLayout)findViewById(R.id.swip_refresh);
        swipRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_btn);

        weatherlayout=(ScrollView)findViewById(R.id.weather_layout);
        forcastlayout=(LinearLayout)findViewById(R.id.forecaset_layout);
        titileCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if (weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else
        {   mWeatherId=getIntent().getStringExtra("weather_id");
            weatherlayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

    }
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=163dc158b39c4ac9b3b4bfd78d9897be";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        swipRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseTest=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseTest);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseTest);
                            editor.apply();
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                        swipRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

private void showWeatherInfo(Weather weather){
    String cityname=weather.basic.cityName;
    String updatetime=weather.update.updateTime;
    titileCity.setText(cityname);
    titleUpdateTime.setText(updatetime);
    forcastlayout.removeAllViews();
    for (Forecast forecast:weather.forecastList){
        View view = LayoutInflater.from(this).inflate(R.layout.forecaset_item,forcastlayout,false);
        TextView dateText=(TextView)view.findViewById(R.id.data_text);
        TextView tmpmintext=(TextView)view.findViewById(R.id.tmpmin);
        TextView tmpmaxtext=(TextView)view.findViewById(R.id.tempmax);
        TextView dayweathertext=(TextView)view.findViewById(R.id.dayweather);
        dateText.setText(forecast.date);
        tmpmaxtext.setText(forecast.temperture.max);
        tmpmintext.setText(forecast.temperture.min);
        dayweathertext.setText(forecast.more.info);
        forcastlayout.addView(view);

    }
      weatherlayout.setVisibility(View.VISIBLE);
}
private void  loadBingPic(){
    String requestBingPic="http://guolin.tech/api/bing_pic";
    HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          final String bingpic=response.body().string();
            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("bing_pic",bingpic);
            editor.apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(WeatherActivity.this).load(bingpic).into(bingPicImg);
                }
            });
        }
    });
}

}
