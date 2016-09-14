package com.dahlstore.dnote6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Note {

    public String title, content;
    public int id;
    public static ArrayList<Note> parse(JSONArray array){
        ArrayList<Note> notes = new ArrayList<>();

        int length = array.length();
        for(int i = 0; i<length; i++) {
            JSONObject object = array.optJSONObject(i);
            Note note = new Note();
            note.id = object.optInt("id");
            note.title =object.optString("title");
            note.content =object.optString("content");

            notes.add(note);
        }
        return notes;
    }
}
