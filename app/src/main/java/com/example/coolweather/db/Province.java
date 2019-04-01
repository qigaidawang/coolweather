package com.example.coolweather.db;

import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

/**
 * Created by wenwen on 2019/3/28.
 */

public class Province extends LitePalSupport {
    private int id;
    private  String provinceName;
    private  int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
