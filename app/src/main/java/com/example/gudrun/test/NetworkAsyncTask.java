package com.example.gudrun.test;

import android.os.AsyncTask;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import okhttp3.OkHttpClient;
import okhttp3.Response;

class NetworkAsyncTask extends AsyncTask {

    String myResponse = "";
    String request = "";

    NetworkAsyncTask(String url) {
        this.request = url;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            URL url = new URL(request);
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                myResponse = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return myResponse;
    }
}
