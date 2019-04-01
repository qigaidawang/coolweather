package com.example.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by wenwen on 2019/3/29.
 */

public class ChooseAreaFragment extends android.support.v4.app.Fragment {
    public static final int LEVE_PROVINCE=0;
    public static final int   LEVE_CITY=1;
    public static final int  LEVE_COUNTRY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backbutton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Province>provinceList;
    private List<City>cityList;
    private List<Country>countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView)view.findViewById(R.id.title_text);
        backbutton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
       adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
       listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVE_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    quryCities();
                }else if(currentLevel==LEVE_CITY){
                   selectedCity=cityList.get(position);
                    quryCountries();
                }else if (currentLevel==LEVE_COUNTRY){
                    String weatherId=countryList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVE_COUNTRY){
                    quryCities();
                }else if (currentLevel==LEVE_CITY){
                    quryProvinces();
                }
            }
        });
       quryProvinces();

    }

    private void quryProvinces(){
        titleText.setText("中国");
        backbutton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVE_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private  void  quryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backbutton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);//??????????
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVE_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    private void quryCountries(){
        titleText.setText(selectedCity.getCityName());
        backbutton.setVisibility(View.VISIBLE);
        countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);//????????
        if (countryList.size()>0){
            dataList.clear();
            for (Country country:countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVE_COUNTRY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }
    }

private void queryFromServer(String address,final String type){
  showProgessDialog();
    HttpUtil.sendOkHttpRequest(address, new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            String responseText=response.body().string();
            boolean result=false;
            if ("province".equals(type)){
                result= Utility.handleProvinceResponse(responseText);
            }else if ("city".equals(type)){
                result=Utility.handleCityResponse(responseText,selectedProvince.getId());
            }else if ("country".equals(type)){
                result=Utility.handleCountryResponse(responseText,selectedCity.getId());
            }
            if (result){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        if ("province".equals(type)){
                            quryProvinces();
                        }else if ("city".equals(type)){
                            quryCities();
                        }else if ("country".equals(type)){
                            quryCountries();
                        }
                    }
                });
            }

        }
    });

}


    private void   showProgessDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
private void  closeProgressDialog(){
    if (progressDialog!=null){
        progressDialog.dismiss();
    }
}

}
