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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDateTime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteCell {

    public String _title;
    public String _date;
    public String _contents;
    public boolean _noTitle;
    public int _borderColor;
    public GradientDrawable border;
    public boolean _highlighted = false;


    public NoteCell(String title, String date, String contents, boolean noTitle, int borderColor) {
        _title = title;
        _date = date;
        _contents = contents;
        _noTitle = noTitle;
        _borderColor = borderColor;
    }

    public View createNoteCellNoTitle(Context c, LinearLayout layoutAllNotes){

        return LayoutInflater.from(c).inflate(R.layout.layout_note_cell, layoutAllNotes, false);
    }

    public View createNoteCellTitle(Context c, LinearLayout layoutAllNotes){
        final View layoutNoteBeingAdded = LayoutInflater.from(c).inflate(R.layout.layout_note_cell_title, layoutAllNotes, false);

        //Check if title has been set
        if (_title != null) {
            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            titleOnNote.setText(_title);
        }

        return layoutNoteBeingAdded;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNoteCell(final Context c, final LinearLayout layoutAllNotes) {


        final View layoutNoteBeingAdded;

        if(_noTitle){
            layoutNoteBeingAdded = createNoteCellNoTitle(c, layoutAllNotes);
        }
        else{
            layoutNoteBeingAdded = createNoteCellTitle(c, layoutAllNotes);
        }


        //Add date
        TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
        if (_date != null) {
            dateTimeCreated.setText(_date);
        } else {
            dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);
        }

        //Fill out contents
        final EditText contentsOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);

        if (_contents != null) {
            contentsOnNote.append(_contents);
        }
        //TODO fix this work around
        //work around because of of weird bug where it was over lapping?
        else {
            contentsOnNote.append("\n");
        }

        final ConstraintLayout note = layoutNoteBeingAdded.findViewById(R.id.layoutTextCell);


        //Set up the border
        setBorder(note, contentsOnNote);


        Button menuButton = layoutNoteBeingAdded.findViewById(R.id.buttonMenu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                layoutAllNotes.removeView(layoutNoteBeingAdded);
//                saveItems(layoutAllNotes);
                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(c);
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
                                        break;
                                    //Highlight note
                                    case 1:
                                        _highlighted = !_highlighted;
                                        setBorder(note, contentsOnNote);


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
                                        layoutAllNotes.removeView(layoutNoteBeingAdded);
                                        break;

                                }
                            }
                        });
                pictureDialog.show();


            }
        });

        layoutAllNotes.addView(layoutNoteBeingAdded);

    }


    //Creates and sets a border around the note
    public void setBorder(ConstraintLayout wholeNoteBorder, EditText textBorder){
        if (_borderColor != -1 || _highlighted) {
            border = new GradientDrawable();
            border.setColor(0xFFFFFFFF);
            if(_highlighted){
                border.setStroke(10, Color.parseColor("#FFFF00"));
            }
            else {
                border.setStroke(10, _borderColor);
            }
            //set border of whole layout
            wholeNoteBorder.setBackground(border);
            //set border of multiline
            textBorder.setBackground(border);
        }

    }

}




//    private void addNoteCell(String title, String date, String contents) {
//        final View layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutAllNotes, false);
//
//        if (title != null) {
//            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
//            titleOnNote.setText(title);
//        }
//
//
//        TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
//        if (date != null) {
//            dateTimeCreated.setText(date);
//        } else {
//            dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);
//        }
//
//
//        EditText contentsOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);
//
//        if (contents != null) {
//            contentsOnNote.append(contents);
//        }
//        //TODO fix this work around
//        //work around because of of weird bug where it was over lapping?
//        else {
//            contentsOnNote.append("\n");
//        }
//
//        //Create a new border and use it for this layout (border can not be shared)
//        if (notesColour != -1) {
//            GradientDrawable border = new GradientDrawable();
//            border.setColor(0xFFFFFFFF);
//            border.setStroke(10, notesColour);
//            //set border of whole layout
//            ConstraintLayout note = layoutNoteBeingAdded.findViewById(R.id.layoutTextCell);
//            note.setBackground(border);
//            //set border of multiline
//            contentsOnNote.setBackground(border);
//        }
//
//
//        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonRemove);
//        removeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                layoutAllNotes.removeView(layoutNoteBeingAdded);
////                saveItems(layoutAllNotes);
//                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(NoteActivity.this);
//                pictureDialog.setTitle("Select Note to add");
//                String[] pictureDialogItems = {"Add text note and title", "Add image", "Add image with title", "Add exta notebook"};
//                pictureDialog.setItems(pictureDialogItems,
//                        new DialogInterface.OnClickListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                switch (which) {
//                                    case 0:
//                                        addNoteCell(null, null, null);
//                                        break;
//                                    case 1:
//                                        addImageCell(true, null, null, null);
//                                        break;
//                                    case 2:
//                                        addImageCell(false, null, null, null);
//                                        break;
//                                    case 3:
//                                        //TODO: make this pop up better
//                                    {
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
//
//                                        builder.setTitle("New notebook");
//                                        final EditText input = new EditText(NoteActivity.this);
//                                        input.setHint("Notebook name");
//
//                                        input.setInputType(InputType.TYPE_CLASS_TEXT);
//                                        builder.setView(input);
//
//
//                                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//
//                                                addNoteBook(currentFolder + "/" + input.getText().toString().replace("/", "-"));
//
//                                            }
//                                        });
//
//                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                                // User cancelled the dialog
//                                            }
//                                        });
//
//
//                                        builder.show();
//                                    }
//
//                                    break;
//                                }
//                            }
//                        });
//                pictureDialog.show();
//
//
//            }
//        });
//
//        layoutAllNotes.addView(layoutNoteBeingAdded);
//        saveItems(layoutAllNotes);
//
//    }
//
//}
