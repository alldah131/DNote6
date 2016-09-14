package com.dahlstore.dnote6;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity implements Callback {

    public static final int ADD_REQUEST_CODE = 123;
    public static final int EDIT_REQUEST_CODE = 456;
    private int position;
    private int id;
    private EditText titleEditText;
    private EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        id = intent.getIntExtra("id", -1);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        titleEditText = (EditText) findViewById(R.id.title_edit_text);
        titleEditText.setText(title);

        contentEditText = (EditText) findViewById(R.id.content_edit_text);
        contentEditText.setText(content);

    }

    public void onSaveClick(View view){

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        //Http Request
        insertOrUpdate(title, content);

    }

    private void insertOrUpdate(String title, String content){

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.addEncoded("id",id + "");
        bodyBuilder.addEncoded("title", title);
        bodyBuilder.addEncoded("content", content);

        RequestBody body = bodyBuilder.build();

        Request.Builder builder = new Request.Builder();
        builder.url(getString(R.string.url));

        if(position >= 0){
            //Update
            builder.post(body);
        }
        else{
            //Insert
            builder.put(body);
        }

        Request request = builder.build();

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        //JSON
        String json = response.body().string();
        System.out.println(json);

        try
        {
            JSONObject result = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            JSONObject body = result.optJSONObject("body");

            if(position <0){
                //Insert
                id = body.optInt("id");

            }

            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();

            Intent resultdata = new Intent();
            resultdata.putExtra("position",position);
            resultdata.putExtra("id",id);
            resultdata.putExtra("title",title);
            resultdata.putExtra("content", content);
            setResult(Activity.RESULT_OK, resultdata);
            finish();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
