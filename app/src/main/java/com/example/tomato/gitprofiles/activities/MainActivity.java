package com.example.tomato.gitprofiles.activities;

import android.os.AsyncTask;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{

    private ApiInterface apiInterface;
    private List<GitProfile> gitProfiles;
    private RecyclerView recyclerView;
    private ProfilesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private Call<List<GitProfile>> call;
    private FloatingActionButton fab;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private static final int PAGE_START = 0;
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    private static final String TAG = "WTF";
    private static final String KEY_INDEX = "index";
    private DownloadGitProfiles downloadGitProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

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

        downloadGitProfiles = (DownloadGitProfiles) getLastCustomNonConfigurationInstance();

        if (downloadGitProfiles == null) {
            downloadGitProfiles = new DownloadGitProfiles();
            downloadGitProfiles.execute();
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clearProfiles(gitProfiles);
                Id.setId("0");
                getNextProfiles();
                dataLoadingState();
            }
        });
    }

    public class DownloadGitProfiles extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            dataLoadingState();
        }

        @Override
        protected Void doInBackground(Void... params) {
            apiInterface = ApiUtils.getApi();
            call = apiInterface.getProfiles(Id.getId());
            call.enqueue(new Callback<List<GitProfile>>() {
                @Override
                public void onResponse(Call<List<GitProfile>> call, Response<List<GitProfile>> response) {
                    if (response.code() == 200) {
                        gitProfiles = response.body();
                        adapter = new ProfilesAdapter(gitProfiles, MainActivity.this);
                        recyclerView.setAdapter(adapter);
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
                public void onFailure(Call<List<GitProfile>> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                    showErrorDialog(t);
                }
            });
            return null;
        }
    }

    private String getNextId(Response<List<GitProfile>> response) {
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
        Call<List<GitProfile>> nextCall = apiInterface.getProfiles(Id.getId());
        nextCall.enqueue(new Callback<List<GitProfile>>() {
            @Override
            public void onResponse(Call<List<GitProfile>> call, Response<List<GitProfile>> response) {
                if (response.code() == 200){
                    isLoading = false;
                    gitProfiles.addAll(response.body());
                    recyclerView.getAdapter().notifyDataSetChanged();
                    Log.d("WTF", "update profiles");

                    Id.setId(getNextId(response));
                    dataLoadedState();
                    Log.d("WTF", "new id value = " + Id.getId());

                    if (currentPage == TOTAL_PAGES) isLastPage = true;
                } else {
                    showAlertDialog(response);
                }
            }

            @Override
            public void onFailure(Call<List<GitProfile>> call, Throwable t) {
                showErrorDialog(t);
            }
        });
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return downloadGitProfiles;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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

    private void showAlertDialog(Response<List<GitProfile>> response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(response.message());
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
