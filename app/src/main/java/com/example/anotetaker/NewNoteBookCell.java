package com.example.anotetaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


//Cell that will open another sub notebook
public class NewNoteBookCell extends Note {


    private static final String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";
    String _noteBookFile = null;

    public NewNoteBookCell(String noteBookFile, Context c, LinearLayout layoutAllNotes){
        _noteBookFile = noteBookFile;
        //Initially creating, border set to -1 (black)
        //File for the sub layout will not be created, so there will be no border colour to get
        _borderColor = -1;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
    }


    //Create the layout
    @Override
    public void createNote(Integer index) {
        _layoutNoteBeingAdded = LayoutInflater.from(_c).inflate(R.layout.activity_open_note_cell, _layoutAllNotes, false);
        final TextView noteBookName = _layoutNoteBeingAdded.findViewById(R.id.noteNametextView);
        noteBookName.setText(_noteBookFile);

        ImageButton openButton = _layoutNoteBeingAdded.findViewById(R.id.openNoteImageButton);


        //get the color of the notebook if it already exists
        try {
            BufferedReader reader;
            File file = new File(NOTEBOOK_DIRECTORY + "/" + _noteBookFile + ".txt");
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = reader.readLine();
                while (line != null) {
                    if (line.split(" ")[0].equals("color")) {
                        _borderColor = Integer.parseInt(line.split(" ")[1]);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("io exception", e.toString());

        }

        //setup views for border
        _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutOpenNoteCell), openButton};

        //Set up the border
        setBorder();



        //Listener to open activity
        View.OnClickListener openNotebook = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((NoteActivity) _c).saveItems(((NoteActivity) _c).layoutAllNotes);
                    ((NoteActivity) _c).finishAndRemoveTask();

                }
                catch (Exception e){
                    Log.e("Main Actvity note","Do not need to save");
                }
                    SharedPreferences mPrefs = _c.getSharedPreferences("NotebookNameValue", 0);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    Log.e("hello", _c.getString(R.string.curWorkingFolder) + "!"+ (String) noteBookName.getText());
                    editor.putString(_c.getString(R.string.curWorkingFolder), (String) noteBookName.getText());
                    editor.commit();

                    _c.startActivity(new Intent(_c, NoteActivity.class));


            }

        };

        //Set all the touchable areas on the cell to the openNoteBook listener
        openButton.setOnClickListener(openNotebook);
        noteBookName.setOnClickListener(openNotebook);


        //Insert the cell at the right index if not null
        if(index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        }
        else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }

    //#%^$ added to the end of string so user will unlikely put in a string == to this and mess up the loading
    @Override
    public String saveNote() {
        String file = "LayoutNewNoteBookCell\n";
        file += "borderColor#%^$ " + Integer.toString(_borderColor) + "\n";
        file += "filename#%^$ " + _noteBookFile +"\n";
        return file;
    }

    //Won't ever be used
    @Override
    public String getReminderTitle() {
        return null;
    }

    public String getTitle(){
        return null;
    }
}
