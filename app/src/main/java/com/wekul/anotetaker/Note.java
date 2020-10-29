package com.wekul.anotetaker;

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


import java.util.Arrays;
import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;


//Parent class to all the cells that are in the note
public abstract class Note {

    public final int THRESHOLDFORDATEDISPLAYED = 380;

    //Color of the border
    public int _borderColor;
    //Border drawable that will be drawn on all borderviews that take a border
    public GradientDrawable border;
    //Array of views that need a border or to be colored
    public View[] _borderViews = null;
    //Bool to display the layout as highlighted
    public boolean _highlighted = false;

    //Boolean variable to check if the note has been deleted
    public boolean _deleted = false;
    public String _title = null;
    //Boolean value to check if we are adding a title to the layout or not
    public boolean _noTitle = false;
    //Date/time that this cell was created at
    public String _date;

    //Enum to select the type of note displayed in a note cell
    enum Type {text, bulletpoint, list}

    //the type of the note cell
    Type _type = null;

    //context that the note is in
    Context _c;
    //the parent layout that this will be added to
    LinearLayout _layoutAllNotes;
    //this layout being added
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
    public void setBorder() {
        //Sets the border to be black if it is unset (default)
        if (_borderColor == -1) {
            _borderColor = Color.BLACK;
        }
        if (_borderColor != -1 || _highlighted) {
            //Create the border
            border = new GradientDrawable();
            border.setColor(0xFFFFFFFF);
            //Set the stroke color depending on if its highlighted or not
            if (_highlighted) {
                border.setStroke(10, Color.parseColor("#FFFF00"));
            } else {
                border.setStroke(10, _borderColor);
            }
            //Loop through all views in the border view
            for (View v : _borderViews) {
                //If the view is an image button (menu/add buttons) just set the color of the image button
                if (v instanceof ImageButton) {
                    if (_highlighted) {
                        ((ImageButton) v).setColorFilter(Color.parseColor("#FFFF00"));
                    } else {
                        ((ImageButton) v).setColorFilter(_borderColor);
                    }
                }
                //Else set the border of the view to the border that has been created
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


            final AlertDialog.Builder pictureDialog = new AlertDialog.Builder(_c);
            pictureDialog.setTitle("Select Note to add");
            //"Move note up", "Move note down"
            String[] pictureDialogItems = {"Set reminder", "Highlight note", "Add title", "Delete note"};
            if (_highlighted) {
                pictureDialogItems[1] = "Unhighlight note";
            }
            if (!_noTitle) {
                pictureDialogItems[2] = "Remove title";
            }
            //Set up final item in picture Dialog if we are using a NoteCell
            if (_type != null) {
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
                                    intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
                                    String reminderTitle = getReminderTitle();
                                    if (reminderTitle != null) {
                                        intent.putExtra("title", reminderTitle);
                                    } else {
                                        intent.putExtra("title", "Reminder for note");
                                    }

                                    ((Activity) _c).startActivity(intent);
                                    break;
                                //Highlight note
                                case 1:
                                    highlightChange();
                                    setBorder();

                                    break;
                                //Add/remove the title
                                case 2:
                                    //Get and save the previous title if there was one
                                    if (!_noTitle) {
                                        _title = getTitle();
                                    }
                                    //Change title status
                                    _noTitle = !_noTitle;
                                    int i;
                                    for (i = 0; i < _layoutAllNotes.getChildCount(); i++) {
                                        if (_layoutAllNotes.getChildAt(i) == _layoutNoteBeingAdded) {
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
                                //Change the Note cell to have or not have bullet points
                                case 4:
                                    if (finalPictureDialogItems[4].equals("Convert to bullet points")) {
                                        _type = Type.bulletpoint;
                                    }
                                    if (finalPictureDialogItems[4].equals("Convert to plain text")) {
                                        _type = Type.text;
                                    }

                                    //Get the index of note being changed
                                    for (i = 0; i < _layoutAllNotes.getChildCount(); i++) {
                                        if (_layoutAllNotes.getChildAt(i) == _layoutNoteBeingAdded) {
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

    //Swap the highlighted value
    public void highlightChange() {
        _highlighted = !_highlighted;
    }

}
