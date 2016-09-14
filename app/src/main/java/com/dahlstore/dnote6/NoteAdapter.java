package com.dahlstore.dnote6;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter {

    private ArrayList<Note> notes = new ArrayList<>();

    public void set(ArrayList<Note> notes){
        this.notes.clear();
        this.notes.addAll(notes);
    }

    public void add(Note note){
        notes.add(0,note);
    }

    public void remove(int position){
        notes.remove(position);
    }


    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(null==convertView){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.layout_note_item, parent, false);
            ViewHolder vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();
        Note note = getItem(position);
        vh.title.setText(note.title);
        vh.content.setText(note.content);

        return convertView;
    }

    class ViewHolder {
        public TextView title, content;

        public ViewHolder(View convertView){
            title = (TextView) convertView.findViewById(R.id.title_text_view);
            content = (TextView) convertView.findViewById(R.id.content_text_view);
        }
    }
}
