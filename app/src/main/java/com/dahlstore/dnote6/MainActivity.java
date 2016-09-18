package com.dahlstore.dnote6;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,SendJSONTaskActivity {

    private ListView listView;
    private NoteAdapter adapter;
    public TextView temperatureTextView,textView;
    ImageView weatherIconImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new NoteAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        weatherIconImageView= (ImageView) findViewById(R.id.weatherIconImageView);
        new JSONTask(this).execute(String.valueOf("https://query.yahooapis.com/v1/public/yql?q=select%20item%20from%20weather.forecast%20where%20woeid%3D906057%20and%20u%3D%27c%27&format=json"));
        readAllNotes();
    }

    @Override
    public void sendData(String str, int code) {
        temperatureTextView.setText(str);
        int resource = getResources().getIdentifier("drawable/icon_" + code, null, getPackageName());
        @SuppressWarnings("deprecation")
        Drawable weatherIconDrawable = getResources().getDrawable(resource);
        weatherIconImageView.setImageDrawable(weatherIconDrawable);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Note note = adapter.getItem(position);

        Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("id", note.id);
        intent.putExtra("title", note.title);
        intent.putExtra("content", note.content);
        startActivityForResult(intent, DetailActivity.EDIT_REQUEST_CODE);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Delete Note?");
            alertDialogBuilder.setMessage("Delete Note?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Note note = adapter.getItem(position);
                    deleteNote(note.id);
                    adapter.remove(position);
                    adapter.notifyDataSetChanged();


                }
            });
            alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);
        return true;
        }



    public void onAddClick(View view){
        Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
        startActivityForResult(intent, DetailActivity.ADD_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != AppCompatActivity.RESULT_OK){
            return;
        }
        if(requestCode == DetailActivity.ADD_REQUEST_CODE){

            Note note = new Note();

            note.id = data.getIntExtra("id", 0);
            note.title = data.getStringExtra("title");
            note.content = data.getStringExtra("content");

            adapter.add(note);
            adapter.notifyDataSetChanged();
        }
        else if(requestCode == DetailActivity.EDIT_REQUEST_CODE){
            int position = data.getIntExtra("position", -1);

            Note note = adapter.getItem(position);

            note.title = data.getStringExtra("title");
            note.content = data.getStringExtra("content");

            adapter.notifyDataSetChanged();

        }
    }
    //HTTP REQUEST
    private void readAllNotes(){
        Request.Builder builder = new Request.Builder();
        builder.url(getString(R.string.host));
        builder.get();

        Request request = builder.build();

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject result = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    int statusCode = result.optJSONObject("header").optInt("code");

                    if (statusCode != 200)
                        return;

                    JSONArray body = result.optJSONArray("body");

                    ArrayList<Note> notes = Note.parse(body);
                    adapter.set(notes);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteNote(int id){
        String i = String.valueOf(id);
        String server_url = "http://192.168.0.22/notes.php?id="+ i;
        final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.DELETE, server_url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));
                            int statusCode = result.optJSONObject("header").optInt("code");

                            if (statusCode == 204) {
                                Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                        requestQueue.stop();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                requestQueue.stop();
            }
        });
        requestQueue.add(stringRequest);

    }
}
