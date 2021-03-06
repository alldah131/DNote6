package com.dahlstore.dnote6;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity implements Callback {

    public static final int ADD_REQUEST_CODE = 123;
    public static final int EDIT_REQUEST_CODE = 456;
    private int position;
    private int id;
    private EditText titleEditText,contentEditText;
    private boolean changed = false;


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

        checkIfUserChangedOrWroteAnyText();

    }

    public void onSaveClick(View view){

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        if(TextUtils.isEmpty(title) && TextUtils.isEmpty(content)){
            Toast.makeText(DetailActivity.this, "No content to save. Note discarded", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            insertOrUpdate(title, content);
        }
    }

    private void insertOrUpdate(String title, String content){

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.addEncoded("id",id + "");
        bodyBuilder.addEncoded("title", title);
        bodyBuilder.addEncoded("content", content);

        RequestBody body = bodyBuilder.build();

        Request.Builder builder = new Request.Builder();
        builder.url(getString(R.string.host));

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
            resultdata.putExtra("id", id);
            resultdata.putExtra("title", title);
            resultdata.putExtra("content", content);
            setResult(Activity.RESULT_OK, resultdata);
            finish();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void setCancelClick(View view) {
               checkIfUserChangedOrWroteAnyText();
        if(changed = true){
            openDialogFragment(view);
        }
        else{
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        checkIfUserChangedOrWroteAnyText();
        if(changed = true){
            openDialogFragment(new View(getApplicationContext()));
        }
        else{
            finish();
        }
    }


    public void checkIfUserChangedOrWroteAnyText() {
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (hasFocus) {

                    titleEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {


                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            changed = true;

                        }
                    });

                }
            }
        });



        contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (hasFocus) {

                    contentEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {


                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            changed = true;

                        }
                    });

                }
            }
        });

    }

    public void openDialogFragment(final View v){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailActivity.this);
        alertDialogBuilder.setTitle("Save memo before exit?");
        alertDialogBuilder.setMessage("Save memo before exit?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onSaveClick(v);
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialogBuilder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(DetailActivity.this, "Note discarded", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);

    }

}
