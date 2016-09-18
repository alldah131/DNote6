package com.dahlstore.dnote6;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONTask extends AsyncTask<String,String, String> {
    SendJSONTaskActivity dataSendToActivity;
    Context context;

    // single constructor to initialize both the context and dataSendToActivity
    public JSONTask(Context context) {
        this.context = context;
        dataSendToActivity = (SendJSONTaskActivity) ((Activity) context);
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject parentObject = new JSONObject(result);
            JSONObject query = parentObject.getJSONObject("query").optJSONObject("results").optJSONObject("channel").optJSONObject("item");
            String temperature = query.getJSONObject("condition").optString("temp");
            String text = query.getJSONObject("condition").optString("text");
            int code = query.getJSONObject("condition").optInt("code");
            temperature += " °C " + " \n" + text;

            if (dataSendToActivity != null) {
                dataSendToActivity.sendData(temperature, code);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}