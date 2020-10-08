package com.example.anotetaker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDateTime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteCell extends Note{


    public String _date;
    public String _contents;
    public boolean _noTitle;



    public NoteCell(String title, String date, String contents, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes) {
        _title = title;
        _date = date;
        _contents = contents;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
    }

    public View createNoteCellNoTitle(){

        return LayoutInflater.from(_c).inflate(R.layout.layout_note_cell, _layoutAllNotes, false);
    }

    public View createNoteCellTitle(){
        final View layoutNoteBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_note_cell_title, _layoutAllNotes, false);

        //Check if title has been set
        if (_title != null) {
            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            titleOnNote.setText(_title);
        }

        return layoutNoteBeingAdded;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNote() {

        if(_noTitle){
            _layoutNoteBeingAdded = createNoteCellNoTitle();
        }
        else{
            _layoutNoteBeingAdded = createNoteCellTitle();
        }


        //Add date
        TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
        if (_date != null) {
            dateTimeCreated.setText(_date);
        } else {
            _date = LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1];
            dateTimeCreated.setText(_date);
        }

        //Fill out contents
        final EditText contentsOnNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);

        if (_contents != null) {
            contentsOnNote.append(_contents);
        }
        //TODO fix this work around
        //work around because of of weird bug where it was over lapping?
        else {
            contentsOnNote.append("\n");
        }

        final ConstraintLayout note = _layoutNoteBeingAdded.findViewById(R.id.layoutTextCell);

        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(menuListener);

        //setup views for border
        _borderViews = new View[]{note, contentsOnNote, menuButton};

        //Set up the border
        setBorder();


        _layoutAllNotes.addView(_layoutNoteBeingAdded);

    }

    //#%^$ added to the end of string so user will unlikely put in a string == to this and mess up the loading
    public String saveNote(){
        String file = "LayoutNoteCell\n";
        file += "borderColor#%^$ " + Integer.toString(_borderColor) + "\n";
        file += "highlighted#%^$ " + _highlighted + "\n";
        if(!_noTitle){
            file += "title#%^$ " + ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText() + "\n";
        }
        file += "date#%^$ " + _date + "\n";
        file += "contents#%^$ " + ((TextView)_layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText() +"\n";
        file += "noTitle#%^$ " + _noTitle + "\n";

        return file;
    }

    public String getReminderTitle(){
        _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
        _contents = ((TextView)_layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText().toString();
        if(!_title.equals("Title")){
            return _title;
        }
        if(!_contents.equals("") && !_contents.equals(null)){
            //Just get the first line
            return _contents.split("\n")[0];
        }
        else {
            return "Reminder for note";
        }

    }



}


