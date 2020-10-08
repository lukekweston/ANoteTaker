package com.example.anotetaker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

public abstract class Note {

    public int _borderColor;
    public GradientDrawable border;
    public boolean _highlighted = false;
    public View[] _borderViews = null;
    //Boolean variable to check if the note has been deleted
    public boolean _deleted = false;
    public String _title = null;

    Context _c;
    LinearLayout _layoutAllNotes;
    View _layoutNoteBeingAdded;


    //Creates the cell
    public abstract void createNote();
    //Creates a string containing all of the notes data
    public abstract String saveNote();
    //Gets the title that should be used for a reminder for a specific note
    public abstract String getReminderTitle();



    //Creates and sets a border around the note
    public void setBorder(){
        if (_borderColor != -1 || _highlighted) {
            border = new GradientDrawable();
            border.setColor(0xFFFFFFFF);
            if(_highlighted){
                border.setStroke(10, Color.parseColor("#FFFF00"));
            }
            else {
                border.setStroke(10, _borderColor);
            }
            for(View v: _borderViews){
                if(v instanceof ImageButton){
                    if(_highlighted){
                        ((ImageButton)v).setColorFilter(Color.parseColor("#FFFF00"));
                    }
                    else {
                        ((ImageButton) v).setColorFilter(_borderColor);
                    }
                }

                else {
                   v.setBackground(border);
                }
            }

        }

    }

    //Listener for menu
    final View.OnClickListener menuListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {

            //                layoutAllNotes.removeView(layoutNoteBeingAdded);
//                saveItems(layoutAllNotes);
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(_c);
            pictureDialog.setTitle("Select Note to add");
            //"Move note up", "Move note down"
            String[] pictureDialogItems = {"Set reminder", "Highlight note", "Duplicate note", "Delete note"};
            if(_highlighted){
                pictureDialogItems[1] = "Unhighlight Note";
            }
            pictureDialog.setItems(pictureDialogItems,
                    new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                //Reminder
                                case 0:
                                    Calendar cal = Calendar.getInstance();
                                    Intent intent = new Intent(Intent.ACTION_EDIT);
                                    intent.setType("vnd.android.cursor.item/event");
                                    intent.putExtra("beginTime", cal.getTimeInMillis());
                                    intent.putExtra("allDay", false);
                                    intent.putExtra("rrule", "FREQ=DAILY");
                                    intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
                                    if(_title != null){
                                        intent.putExtra("title", getReminderTitle());
                                    }
                                    else{
                                        intent.putExtra("title", "Reminder for note");
                                    }

                                    ((Activity)_c).startActivity(intent);
                                    break;
                                //Highlight note
                                case 1:
                                    _highlighted = !_highlighted;
                                    setBorder();


                                    break;
//                                    //Move note up
//                                    case 2:
//
//
//                                        break;
//                                    //move note down
//                                    case 3:
//                                        //TODO: make this pop up better
//                                    {
////                                        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
////
////                                        builder.setTitle("New notebook");
////                                        final EditText input = new EditText(NoteActivity.this);
////                                        input.setHint("Notebook name");
////
////                                        input.setInputType(InputType.TYPE_CLASS_TEXT);
////                                        builder.setView(input);
////
////
////                                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
////                                            public void onClick(DialogInterface dialog, int id) {
////
////                                                addNoteBook(currentFolder + "/" + input.getText().toString().replace("/", "-"));
////
////                                            }
////                                        });
////
////                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
////                                            public void onClick(DialogInterface dialog, int id) {
////                                                // User cancelled the dialog
////                                            }
////                                        });
////
////
////                                        builder.show();
//                                    }
//
//                                    break;
                                //duplicate note
                                case 2:
                                    break;

                                //Delete note
                                case 3:
                                    _layoutAllNotes.removeView(_layoutNoteBeingAdded);
                                    _deleted = true;
                                    break;

                            }
                        }
                    });
            pictureDialog.show();

        }
    };


}
