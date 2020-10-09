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

        //format text
        if (_type == Type.text) {
            setPlainTextListener(contentsOnNote);
        }
        if (_type == Type.bulletpoint) {
            setBulletPointListener(contentsOnNote);
        }
        if (_type == Type.list) {
            setListListener(contentsOnNote);
        }

        if (_contents != null) {
            contentsOnNote.append(_contents);
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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                //remove bullet points
                text = text.replace("• ", "").replace("•", "");
                contents.removeTextChangedListener(this);
                contents.setText(text);
                contents.addTextChangedListener(this);
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

            @Override
            public void afterTextChanged(Editable editable) {
                if (contents.hasFocus()) {
                    String textAll = contents.getText().toString();
                    //Case deleting last line
                    if ((textAll.charAt(textAll.length() - 1) == '•' ||
                            (textAll.length() > 2 && textAll.charAt(textAll.length() - 2) == '\n' && textAll.charAt(textAll.length() - 1) == ' '))) {
                        textAll = textAll.substring(0, textAll.length() - 1);
                    }
                    //Case if adding a new line
                    else if (textAll.charAt(textAll.length() - 1) == '\n') {
                        textAll += "• ";
                    }
                    String[] text = textAll.split("\n");
                    String output = "";
                    int cursorPostition = contents.getSelectionStart();
                    for (String line : text) {
                        //Edgecase for if user tries to delete the bullet point on the first line
                        if (line == text[0]) {
                            line = line.replace("• ", "").replace("•", "");
                            if (line.length() > 0 && line.charAt(0) == ' ') {
                                line = line.substring(1);
                            }
                            line = "• " + line;
                            output += line + "\n";
                            continue;
                        }
                        //Del last line
                        if (line.equals("•")) {
                            continue;
                        }
                        //Deleting a line mid section
                        //Deleting the space
                        if ((line.length() >= 2 && line.charAt(0) == '•' && line.charAt(1) != ' ')) {
                            //Remove bullet point
                            line = line.replace("•", "");
                            //add to previous line by removing "\n" at the end of previous line
                            output = output.substring(0, output.length() - 1) + line + "\n";
                            //increment cursor -2, for the size of the bullet point
                            cursorPostition -= 2;
                        }
                        //deleting the bullet point
                        else if ((line.length() > 2 && line.charAt(0) == ' ' && line.charAt(1) != '•')) {
                            line = line.substring(1, line.length());
                            output = output.substring(0, output.length() - 1) + line + "\n";
                            cursorPostition -= 1;

                        }
                        //normal lines
                        else {
                            line = line.replace("• ", "").replace("•", "");
                            line = "• " + line;
                            output += line + "\n";
                        }
                    }
                    //Check if cursor is being written in the bullet point area
                    try {
                        if (output.charAt(cursorPostition - 1) == '•') {
                            cursorPostition += 2;
                        }
                    } catch (Exception e) {

                    }
                    //Make the cursor position the same as the output length if we are adding to the last character
                    if (cursorPostition == contents.getText().toString().length()) {
                        cursorPostition = output.length();
                    }

                    //Remove last end line character
                    if (output.charAt(output.length() - 1) == '\n') {
                        output = output.substring(0, output.length() - 1);
                    }
                    //Remove this listener so when we update the text it does not trigger this making an infinite loop
                    contents.removeTextChangedListener(this);

                    //Update the Contents by changing the editable text, IMPORTANT, is faster
                    editable.replace(0, editable.length(), output);

                    //Set the cursor to the last position if there was an error
                    if (cursorPostition >= output.length()) {
                        contents.setSelection(output.length());
                    } else {
                        //Add 2 to the cursor, for adding a bullet point mid way
                        if (output.length() - textAll.length() >= 2) {
                            cursorPostition += 2;
                        }
                        contents.setSelection(cursorPostition);
                    }
                    //Add the listener back
                    contents.addTextChangedListener(this);
                    _contents = contents.getText().toString();
                }

            }
        });


    }


    public void setListListener(final EditText contents) {

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                String out = "";
                int i = 0;
                for (String s : text.split("\n")) {
                    i++;
                    out += i + ". " + s + "\n";
                }
                contents.setText(out);

            }
        });


    }

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


