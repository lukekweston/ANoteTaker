package com.example.anotetaker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean _noTitle = false;
    public String _date;

    enum Type {text, bulletpoint, list}

    Type _type = null;

    Context _c;
    LinearLayout _layoutAllNotes;
    View _layoutNoteBeingAdded;




    //Creates the cell
    //Index for if the note is being inserted
    public abstract void createNote(Integer index);
    //Creates a string containing all of the notes data
    public abstract String saveNote();
    //Gets the title that should be used for a reminder for a specific note
    public abstract String getReminderTitle();

    //Gets the current title
    public abstract String getTitle();



    //Creates and sets a border around the note
    public void setBorder(){
        if(_borderColor == -1){
            _borderColor = Color.BLACK;
        }
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
            final AlertDialog.Builder pictureDialog = new AlertDialog.Builder(_c);
            pictureDialog.setTitle("Select Note to add");
            //"Move note up", "Move note down"
            String[] pictureDialogItems = {"Set reminder", "Highlight note", "Add title", "Delete note"};
            if(_highlighted){
                pictureDialogItems[1] = "Unhighlight note";
            }
            if(!_noTitle){
                pictureDialogItems[2] = "Remove title";
            }
            //Set up final item in picture Dialog if we are using a note item
            if(_type != null) {
                pictureDialogItems = Arrays.copyOf(pictureDialogItems, pictureDialogItems.length + 1);
                if (_type == Type.text) {
                    pictureDialogItems[pictureDialogItems.length - 1] = "Convert to bullet points";
                }
                if (_type == Type.bulletpoint) {
                    pictureDialogItems[pictureDialogItems.length - 1] = "Convert to plain text";
                }
            }
            final String[] finalPictureDialogItems = pictureDialogItems;
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
                                    highlightChange();
                                    setBorder();

                                    break;
                                //duplicate note
                                case 2:
                                    //Get and save the previous title if there was one
                                    if(!_noTitle){
                                        _title = getTitle();
                                    }
                                    //Change title status
                                    _noTitle = !_noTitle;
                                    int i;
                                    for(i =0; i < _layoutAllNotes.getChildCount(); i++){
                                        if(_layoutAllNotes.getChildAt(i) == _layoutNoteBeingAdded){
                                            Log.e("int i = ", Integer.toString(i));
                                            break;
                                        }
                                    }
                                    _layoutAllNotes.removeView(_layoutNoteBeingAdded);
                                    createNote(i);
                                    break;

                                //Delete note
                                case 3:
                                    _layoutAllNotes.removeView(_layoutNoteBeingAdded);
                                    _deleted = true;
                                    break;
                                case 4:
                                    if(finalPictureDialogItems[4].equals("Convert to bullet points")){
                                        _type = Type.bulletpoint;
                                    }
                                    if(finalPictureDialogItems[4].equals("Convert to plain text")){
                                        _type = Type.text;
                                    }
                                    //Redraw using set border

                                    for(i =0; i < _layoutAllNotes.getChildCount(); i++){
                                        if(_layoutAllNotes.getChildAt(i) == _layoutNoteBeingAdded){
                                            Log.e("int i = ", Integer.toString(i));
                                            break;
                                        }
                                    }
                                    _layoutAllNotes.removeView(_layoutNoteBeingAdded);
                                    createNote(i);

                            }
                        }
                    });
            pictureDialog.show();

        }
    };

    public void highlightChange(){
        _highlighted = !_highlighted;
    }

}
