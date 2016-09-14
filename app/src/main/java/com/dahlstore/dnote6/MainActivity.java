package com.dahlstore.dnote6;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    private ListView listView;
    private NoteAdapter adapter;
    private String[] mPermission = {Manifest.permission.CAMERA};
    private static final int REQUEST_CODE_PERMISSION = 5;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new NoteAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        readAllNotes();
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        Note note = adapter.getItem(position);
        deleteNote(note.id);


        adapter.remove(position);
        adapter.notifyDataSetChanged();

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
        builder.url(getString(R.string.url));
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

                try{
                    JSONObject result = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    int statusCode = result.optJSONObject("header").optInt("code");

                    if(statusCode != 200)
                        return;

                    JSONArray body = result.optJSONArray("body");

                    ArrayList<Note> notes = Note.parse(body);
                    adapter.set(notes);

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteNote(int id){

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.addEncoded("id", id +"");

        RequestBody body = bodyBuilder.build();

        Request.Builder builder = new Request.Builder();
        builder.url(getString(R.string.url));
        builder.delete(body);

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
                System.out.println(json);

            }
        });
    }

}
