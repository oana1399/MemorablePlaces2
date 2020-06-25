package com.example.memorableplaces;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(urls[0]); //luam url
            urlConnection = (HttpURLConnection)url.openConnection(); //deschidem conexiune
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();

            while(data != -1) {
                char current = (char)data;

                result+=current; //adaugam datele la string result

                data = reader.read();
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();

            return "Failed!";
        }

    }
}
