package com.example.anotetaker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteCell extends Note {


    public String _contents;


    public NoteCell(String title, String date, String contents, Type type, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes) {
        _title = title;
        _date = date;
        _contents = contents;
        _type = type;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
    }

    public View createNoteCellNoTitle() {

        return LayoutInflater.from(_c).inflate(R.layout.layout_note_cell, _layoutAllNotes, false);
    }

    public View createNoteCellTitle() {
        final View layoutNoteBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_note_cell_title, _layoutAllNotes, false);

        //Check if title has been set
        if (_title != null) {
            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            titleOnNote.setText(_title);
        }

        return layoutNoteBeingAdded;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNote(Integer index) {

        if (_noTitle) {
            _layoutNoteBeingAdded = createNoteCellNoTitle();
        } else {
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
        EditText contentsOnNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);


        final ConstraintLayout note = _layoutNoteBeingAdded.findViewById(R.id.layoutTextCell);

        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(menuListener);

        //setup views for border
        _borderViews = new View[]{note, contentsOnNote, menuButton};

        //Set up the border
        setBorder();

        //Set the listener to format the text
        if (_type == Type.text) {
            setPlainTextListener(contentsOnNote);
        }
        if (_type == Type.bulletpoint) {
            setBulletPointListener(contentsOnNote);
        }
        if (_type == Type.list) {
            setListListener(contentsOnNote);
        }



        if (_contents != null && _contents.length() > 0) {
            contentsOnNote.setText(_contents);
        }
        //Work around for the title layout wrapping back over the stuff above it if its only one line and a title
        //TODO fix the layout so this does not happen
        else{
            if(!_noTitle){
                contentsOnNote.setText("\n");
            }
            else {
                contentsOnNote.setText("");
            }

        }



        if (index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        } else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }


    public void setPlainTextListener(final EditText contents) {

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                int cursorPosition = contents.getSelectionStart();
                //remove bullet points
                text = text.replace("• ", "").replace("•", "");
                contents.removeTextChangedListener(this);
                //contents.setText(text);
                //Update the Contents by changing the editable text, IMPORTANT, is faster
                editable.replace(0, editable.length(), text);
                contents.addTextChangedListener(this);
                //Saves contents each time this is updated
                _contents = contents.getText().toString();
            }
        });


    }


    public void setBulletPointListener(final EditText contents) {

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            //String that it was before update
            String previous = "";
            public void afterTextChanged(Editable editable) {
                //formatted string to use
                String output = "";
                //Get current string
                String textAll = editable.toString();
                int cursorPosition = contents.getSelectionStart();
                //if empty set the contents to a bullet point
                if (textAll.isEmpty() || textAll.equals("•") || textAll.equals(" ")){
                    output = "• \n";
                    cursorPosition = 2;
                }
                //String is increasing in size, or character is being replaced
                else {
                    if (textAll.length() >= previous.length()) {
                        //Appending to the end
                        output = textAll;
                        if (cursorPosition == textAll.length()) {
                            //adding new line (last char is new line)
                            if (textAll.charAt(textAll.length() - 1) == '\n') {
                                output += "• ";
                                cursorPosition += 2;
                            }
                        } else {
                            String[] lines = textAll.split("\n");
                            output = "";
                            for (String line : lines) {
                                if (line.equals("") || line.equals(" ") || line.equals("•")) {
                                    line = "• ";
                                }
                                if (line.charAt(0) != '•') {
                                    String letterToInsert = Character.toString(line.charAt(0));
                                    line = line.replace("• ", "").replace("•", "");
                                    line = "• " + line;
                                    cursorPosition += 2;

                                } else if (line.charAt(1) != ' ') {
                                    line = line.replace("•", "");
                                    line = "• " + line.substring(0, 1) + line.substring(2, line.length());
                                    cursorPosition += 1;
                                }
                                output += line + "\n";
                            }

                        }


                    }

                    //String is smaller or character is being replaced
                    if (textAll.length() <= previous.length()) {
                        String[] lines = textAll.split("\n");
                        output = "";
                        for (String line : lines) {
                            //Boolean value used later because line gets changed
                            boolean firstline = line == lines[0];

                            //deleting empty line
                            if (line.length() <= 1) {
                                continue;
                            }


                            //deleting the bullet point
                            else if (line.charAt(0) != '•' || line.charAt(1) != ' ') {

                                if (output.length() >= 1 && output.charAt(output.length() - 1) == '\n') {
                                    output = output.substring(0, output.length() - 1);
                                }
                                if (line.charAt(0) != '•') {
                                    line = line.substring(1, line.length());
                                    cursorPosition -= 1;
                                    //line = line.substring(0, 1) + line.substring(2,line.length());
                                } else {
                                    line = line.substring(1, line.length());
                                    cursorPosition -= 2;
                                }
                                //Special case for first line
                                if(firstline){
                                    line = "• " + line;
                                    cursorPosition = 2;
                                }


                            }
                            //For deleting the just the new line character between lines (deletes 2nd bullet point)
                            if(line.contains("• ") && line.replace("• ","").length() != line.length() -2){
                                line = "• " + line.replace("• ","");
                                Log.e("not equal", "not equal");
                            }

                            output += line + "\n";
                        }

                    }
                }

                //Remove last end line character
                if (output.length() > 0 && output.charAt(output.length() - 1) == '\n') {
                    output = output.substring(0, output.length() - 1);
                }



                contents.removeTextChangedListener(this);

                //Update the Contents by changing the editable text, IMPORTANT, is faster
                editable.replace(0, editable.length(), output);

                //Set the cursor to the last position if there was an error
                try {
                    if (cursorPosition >= output.length()) {
                        contents.setSelection(output.length());
                    } else {
                        //Add 2 to the cursor, for adding a bullet point mid way
                        if (output.length() - textAll.length() >= 2) {
                            cursorPosition += 2;
                        }
                        contents.setSelection(cursorPosition);
                    }
                }
                catch(Exception e){
                    Log.e("Cursor broke", "cursor broke");
                }
                //Add the listener back
                contents.addTextChangedListener(this);
                _contents = contents.getText().toString();
                previous = output;

            }

    });


    }


    //Todo: make a list type
    public void setListListener(final EditText contents) {
        return;
    }
//
//        contents.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//
//            //String that it was before update
//            String previous = "";
//            @Override
//            public void afterTextChanged(Editable editable) {
//                //formatted string to use
//                String output = "";
//                //Get current string
//                String textAll = editable.toString();
//                int cursorPosition = contents.getSelectionStart();
//                //if empty set the contents to a bullet point
//                if (textAll.isEmpty() || textAll.equals("1. ") || textAll.equals("1.") || textAll.equals(". ") || textAll.equals("1 ")) {
//                    output = "1. \n";
//                    cursorPosition = 3;
//                }
//                //String is increasing in size, or character is being replaced
//                else {
//                    if (textAll.length() >= previous.length()) {
//                        //Appending to the end
//                        output = textAll;
//                        if (cursorPosition == textAll.length()) {
//                            //adding new line (last char is new line)
//                            if (textAll.charAt(textAll.length() - 1) == '\n') {
//
//                                output += (textAll.split("\n").length + 1) + ". ";
//                                cursorPosition += 1;
//                            }
//                        } else {
//                            String[] lines = textAll.split("\n");
//                            int index = 0;
//                            output = "";
//                            for (String line : lines) {
//
//                                index += 1;
//                                String lineNumber = Integer.toString(index);
//                                //Todo: make this work on lists of size > 100
//                                if (index > 99) {
//                                    break;
//                                }
//                                if (index < 10) {
//                                    //Check if the start is not correct
//                                    if (!line.substring(0, 3).equals(lineNumber + ". ")) {
//                                        if (line.length() <= 3) {
//                                            line = lineNumber + ". ";
//                                        } else {
//                                            line = lineNumber + ". " + line.substring(3, line.length());
//                                        }
//                                        Log.e("line", line);
//                                        Log.e("number", lineNumber);
////                                    if (line.equals("") || line.equals(lineNumber + ".") || line.equals(". ") || line.equals(lineNumber + " ")) {
////                                        line = lineNumber + ". ";
////                                        cursorPosition += 3;
////                                    }
//                                        //                            else if (line.charAt(0) != '•') {
//                                        //                                String letterToInsert = Character.toString(line.charAt(0));
//                                        //                                line = line.replace("• ", "").replace("•", "");
//                                        //                                line = "• " + line;
//                                        //                                cursorPosition += 2;
//                                        //
//                                        //                            } else if (line.charAt(1) != ' ') {
//                                        //                                line = line.replace("•", "");
//                                        //                                line = "• " + line.substring(0, 1) + line.substring(2, line.length());
//                                        //                                cursorPosition += 1;
//                                        //                            }
//                                    }
//                                } else {
//
//                                }
//                                output += line + "\n";
//                            }
//
//                        }
//
//                    }
//            }
//
//                    //String is smaller or character is being replaced
//                Log.e("output", output);
//                    if (textAll.length() <= previous.length()) {
//                        String[] lines = textAll.split("\n");
//                        output = "";
//                        int index = 0;
//                        for (String line : lines) {
//                            index += 1;
//                            String lineNumber = Integer.toString(index);
//                            //Todo: make this work on lists of size > 100
//                            if (index > 99) {
//                                break;
//                            }
//                            if (index < 10) {
//                                Log.e("line", line);
//                                Log.e("index", index +"");
//                                if(line.length() <= 2) {
//                                    continue;
//                                }
//                                if(line.length() >= 3 && !line.substring(0,3).equals(lineNumber +". ")) {
//                                    Log.e("substring", line.substring(0,3));
//                                        if(output.length() > 1){
//                                            output = output.substring(0, output.length() - 1) + line.substring(3, line.length());
//                                            continue;
//                                        }
////                                        else{
////                                            output = "1. ";
////                                        }
//                                    }
//
//                                }
//
//                            output += line + "\n";
//
//
//
//
//                            }
//                        }
////                            //Boolean value used later because line gets changed
////                            boolean firstline = line == lines[0];
////
////                            //deleting empty line
////                            if (line.length() <= 1) {
////                                continue;
////                            }
////
////
////                            //deleting the bullet point
////                            else if (line.charAt(0) != '•' || line.charAt(1) != ' ') {
////
////                                if (output.length() >= 1 && output.charAt(output.length() - 1) == '\n') {
////                                    output = output.substring(0, output.length() - 1);
////                                }
////                                if (line.charAt(0) != '•') {
////                                    line = line.substring(1, line.length());
////                                    cursorPosition -= 1;
////                                    //line = line.substring(0, 1) + line.substring(2,line.length());
////                                } else {
////                                    line = line.substring(1, line.length());
////                                    cursorPosition -= 2;
////                                }
////                                //Special case for first line
////                                if(firstline){
////                                    line = "• " + line;
////                                    cursorPosition = 2;
////                                }
////
////
////                            }
////                            //For deleting the just the new line character between lines (deletes 2nd bullet point)
////                            if(line.contains("• ") && line.replace("• ","").length() != line.length() -2){
////                                line = "• " + line.replace("• ","");
////                                Log.e("not equal", "not equal");
////                            }
////
////                            output += line + "\n";
////                        }
////
////                    }
//                //}
//
//                //Remove last end line character
//                if (output.length() > 0 && output.charAt(output.length() - 1) == '\n') {
//                    output = output.substring(0, output.length() - 1);
//                }
//
//
//
//                contents.removeTextChangedListener(this);
//
//                //Update the Contents by changing the editable text, IMPORTANT, is faster
//                editable.replace(0, editable.length(), output);
//
//                //Set the cursor to the last position if there was an error
//                if (cursorPosition >= output.length()) {
//                    contents.setSelection(output.length());
//                } else {
//                    //Add 2 to the cursor, for adding a bullet point mid way
//                    if (output.length() - textAll.length() >= 2) {
//                        cursorPosition += 2;
//                    }
//                    contents.setSelection(cursorPosition);
//                }
//                //Add the listener back
//                contents.addTextChangedListener(this);
//                _contents = contents.getText().toString();
//                previous = output;
//
//            }
//        });
//
//
//    }

    //#%^$ added to the end of string so user will unlikely put in a string == to this and mess up the loading
    public String saveNote() {
        String file = "LayoutNoteCell\n";
        file += "borderColor#%^$ " + Integer.toString(_borderColor) + "\n";
        file += "highlighted#%^$ " + _highlighted + "\n";
        if (!_noTitle) {
            file += "title#%^$ " + ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText() + "\n";
        }
        file += "date#%^$ " + _date + "\n";
        Log.e("type", _type.name());
        file += "type#%^$ " + _type.name() + "\n";
        file += "contents#%^$ " + ((TextView) _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText() + "\n";
        file += "noTitle#%^$ " + _noTitle + "\n";

        return file;
    }

    public String getReminderTitle() {
        _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
        _contents = ((TextView) _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText().toString();
        if (!_title.equals("Title")) {
            return _title;
        }
        if (!_contents.equals("") && !_contents.equals(null)) {
            //Just get the first line
            return _contents.split("\n")[0];
        } else {
            return "Reminder for note";
        }

    }

    @Override
    public String getTitle() {
        return ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }
}


