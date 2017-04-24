package com.example.tomato.gitprofiles.remote;

import com.example.tomato.gitprofiles.model.GitProfile;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("users")
    Call<ArrayList<GitProfile>> getProfiles (@Query("since") String id);
}
