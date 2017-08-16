package com.letsgotoperfection.testinstabugcrashreportingwithretrofit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instabug.library.Instabug;
import com.letsgotoperfection.testinstabugcrashreportingwithretrofit.models.Repo;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edtUserName)
    EditText edtUserName;
    @BindView(R.id.btnGetRepos)
    Button btnGetRepos;
    @BindView(R.id.tvContent)
    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btnGetRepos)
    public void onViewClicked() {
        Call<List<Repo>> listRepos = App.getInstance().getGithubService().listRepos(edtUserName
                .getText().toString());
        listRepos.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                if (response.isSuccessful()) {//True if status code (200-300)
                    if (response.body() != null) {//True if response can be parsed in POJO

                        List<Repo> results = response.body();
                        if (results!=null) {
                            if (results.size() == 0) {
                                tvContent.setText("results don't exists");
                            } else {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (Repo r : results) {
                                    stringBuilder.append(r.toString());
                                }

                                tvContent.setText(stringBuilder);
                            }
                        }

                    }

                }
                if (response.code() != 200) {
                    //Server error
                    Toast.makeText(getApplicationContext(),
                            "Error: " + "Check your internet connection", Toast.LENGTH_SHORT)
                            .show();
                    //Instabug.reportException(new Throwable("Github Server error"));
                }
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {
                String text = t.getMessage() + t.getCause();
                tvContent.setText(text);
                asRetrofitException(t);
                Log.d("hoss","retrofit onFailure");
            }
        });

    }
    private RetrofitException asRetrofitException(final Throwable throwable) {
        // We had non-200 http error
        if (throwable instanceof HttpException) {
            final HttpException httpException = (HttpException) throwable;
            final Response response = httpException.response();

//            Instabug.reportException(throwable);
//            Instabug.reportException(new Throwable("Hello World!"));
            AsyncTask.execute(() -> Instabug.reportException(throwable));
            Log.d("hoss","retrofit HttpException"+throwable.getMessage());


            return RetrofitException.httpError(response.raw().request().url().toString(), response, App.getInstance().getRetrofitInstance());
        }
        // A network error happened
        if (throwable instanceof IOException) {
//            Instabug.reportException(throwable);
//            Instabug.reportException(new Throwable("Hello World!"));
            AsyncTask.execute(() -> Instabug.reportException(throwable));
            Log.d("hoss","retrofit IOException"+throwable.getMessage());

            return RetrofitException.networkError((IOException) throwable);
        }

//        Instabug.reportException(throwable);
//        Instabug.reportException(new Throwable("Hello World!"));
        AsyncTask.execute(() -> Instabug.reportException(throwable));
        Log.d("hoss","retrofit unexpectedError"+throwable.getMessage());

        // We don't know what happened. We need to simply convert to an unknown error

        return RetrofitException.unexpectedError(throwable);
    }
}
