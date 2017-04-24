package com.example.tomato.gitprofiles.utils;

import com.example.tomato.gitprofiles.remote.ApiInterface;
import com.example.tomato.gitprofiles.remote.RetrofitClient;

public class ApiUtils {
    public static String BASE_URL = "https://api.github.com/";
    public static ApiInterface getApi(){
        return RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
    }
}
