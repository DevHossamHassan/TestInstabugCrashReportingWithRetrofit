package com.letsgotoperfection.testinstabugcrashreportingwithretrofit;

import android.app.Application;

import com.instabug.library.Instabug;
import com.letsgotoperfection.testinstabugcrashreportingwithretrofit.network.GitHubService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author hossam.
 */

public class App extends Application {
    private Retrofit retrofit;
    private static App app;
    GitHubService service;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;

        new Instabug.Builder(this,"ANDROID_TOKEN")
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();



         service = retrofit.create(GitHubService.class);
    }

    public static App getInstance() {
        return app;
    }

    public Retrofit getRetrofitInstance() {
        return retrofit;
    }

    public GitHubService getGithubService() {
        return service;
    }
}
