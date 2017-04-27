package com.example.tomato.gitprofiles.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.tomato.gitprofiles.R;
import com.example.tomato.gitprofiles.adapters.ProfilesAdapter;
import com.example.tomato.gitprofiles.model.GitProfile;
import com.example.tomato.gitprofiles.remote.ApiInterface;
import com.example.tomato.gitprofiles.utils.ApiUtils;
import com.example.tomato.gitprofiles.utils.EndlessScrollListener;
import com.example.tomato.gitprofiles.utils.Id;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ApiInterface apiInterface;
    private ProfilesAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fab;

    private boolean isLoading;
    private boolean isLastPage;
    private static final int PAGE_START = 0;
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ProfilesAdapter(this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                getNextProfiles();
                currentPage += 1;
                Log.d(TAG, "isLoading = " + isLoading);
                dataLoadingState();

            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        apiInterface = ApiUtils.getApi();
        Call<ArrayList<GitProfile>> call = apiInterface.getProfiles(Id.getId());
        call.enqueue(new Callback<ArrayList<GitProfile>>() {
            @Override
            public void onResponse(Call<ArrayList<GitProfile>> call, Response<ArrayList<GitProfile>> response) {
                if (response.code() == 200) {
                    adapter.updateProfiles(response.body());
                    String idValue = getNextId(response);

                    Id.setId(idValue);
                    Log.d(TAG, "id value = " + idValue);
                    Log.d(TAG, "response message " + response.message());
                    isLoading = false;

                    if (currentPage == TOTAL_PAGES) isLastPage = true;
                    dataLoadedState();
                }else{
                    showAlertDialog(response);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<GitProfile>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                showErrorDialog(t);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clearProfiles();
                Id.setId("0");
                getNextProfiles();
                dataLoadingState();
            }
        });
    }

    private String getNextId(Response<ArrayList<GitProfile>> response) {
        String newUrl = response.headers().get("Link");
        String[] tempStr = newUrl.split(";");
        String url = tempStr[0].replaceAll("[<>]","");
        String idValue = "";
        try {
            URL url1 = new URL(url);
            url = url1.getQuery();
            String[] tempVal = url.split("=");
            idValue = tempVal[1];
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return idValue;
    }

    private void getNextProfiles() {
        Call<ArrayList<GitProfile>> nextCall = apiInterface.getProfiles(Id.getId());
        nextCall.enqueue(new Callback<ArrayList<GitProfile>>() {
            @Override
            public void onResponse(Call<ArrayList<GitProfile>> call, Response<ArrayList<GitProfile>> response) {
                if (response.code() == 200){
                    isLoading = false;
                    adapter.addProfiles(response.body());
                    Log.d(TAG, "update profiles");

                    Id.setId(getNextId(response));
                    dataLoadedState();
                    Log.d(TAG, "new id value = " + Id.getId());

                    if (currentPage == TOTAL_PAGES){
                        isLastPage = true;
                    }
                } else {
                    showAlertDialog(response);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<GitProfile>> call, Throwable t) {
                showErrorDialog(t);
            }
        });
    }

    private void dataLoadedState() {
        progressBar.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
    }

    private void dataLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
    }

    private void showErrorDialog(Throwable t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(t.getMessage());
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showAlertDialog(Response<ArrayList<GitProfile>> response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(response.message());
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
