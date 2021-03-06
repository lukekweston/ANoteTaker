package com.wekul.anotetaker;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wekul.anotetaker.R;

import java.time.LocalDateTime;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

//Note for displaying text
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

        //Set the date on intial creation
        if(_date == null){
            _date = LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1];
        }

        if (_noTitle) {
            _layoutNoteBeingAdded = createNoteCellNoTitle();

            //setup views for border
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutTextCell), _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine),
                    _layoutNoteBeingAdded.findViewById(R.id.menuButton)};
        } else {
            _layoutNoteBeingAdded = createNoteCellTitle();

            //setup views for border
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutTextCell), _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine),
                    _layoutNoteBeingAdded.findViewById(R.id.menuButton), _layoutNoteBeingAdded.findViewById(R.id.layoutTitleBox)};

            //Add date
            TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            dateTimeCreated.setText(_date);

            TextView titleOnNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            //check if the date will fit in the title, if not do not display it
            Configuration configuration = _c.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp;
            if (screenWidthDp < THRESHOLDFORDATEDISPLAYED || titleOnNote.getTextSize() > THRESHOLDFORTEXTSIZE) {
                dateTimeCreated.setVisibility(View.INVISIBLE);
            }

        }



        //Fill out contents
        EditText contentsOnNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);



        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(menuListener);



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


        //Forces there to be a bullet point dispalyed
        if (_contents != null && _contents.length() > 0) {
            contentsOnNote.setText(_contents);
        }
        else{
            contentsOnNote.setText("");
        }

        contentsOnNote.requestFocus();


        if (index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        } else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }

    //Listener for handling plain text
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

    //Listener that formats bullet points
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
//
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
//
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
//
//
//                                if(line.length() <= 2) {
//                                    continue;
//                                }
//                                if(line.length() >= 3 && !line.substring(0,3).equals(lineNumber +". ")) {
//
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
////
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

        file += "type#%^$ " + _type.name() + "\n";
        file += "contents#%^$ " + ((TextView) _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText() + "\n";
        file += "noTitle#%^$ " + _noTitle + "\n";

        return file;
    }

    public String getReminderTitle() {

        if(!_noTitle) {
            _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
            if (_title != null && !_title.equals("") && !_title.equals("Title") ) {
                return _title;
            }
        }
        _contents = ((TextView) _layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine)).getText().toString();
        if (!_contents.equals("") && !_contents.equals(null)) {
            //Just get the first line and delete bullet point if they exist
            return _contents.split("\n")[0].replace("• ", "");
        } else {
            return "Reminder for note";
        }


    }

    @Override
    public String getTitle() {
        return ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }
}


