package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.security.PublicKey;
import java.util.List;

/**
 * Created by wenwen on 2019/4/1.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Update update;

   @SerializedName("daily_forecast")
    public List<Forecast>forecastList;

}
