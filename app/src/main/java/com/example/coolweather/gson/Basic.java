package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wenwen on 2019/4/1.
 */

public class Basic {
    @SerializedName("cid")
    public String weatherId;
    @SerializedName("city")
    public String cityName;

}
