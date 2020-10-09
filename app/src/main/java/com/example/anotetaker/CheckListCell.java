package com.example.anotetaker;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

public class CheckListCell extends Note{

    ArrayList<CheckListItem> _checkedItems;
    LinearLayout _layoutItems;


    public CheckListCell(String title, String date, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes){
        _title = title;
        _date = date;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
        _checkedItems = new ArrayList<>();
    }


    public void addItem(CheckListItem cLI){
        cLI.setContextAndLayout(_c, _layoutItems);
        cLI.createNote(null);
    }

    public void addItem(Boolean checked, String contents, LinearLayout layoutItems){
        CheckListItem cLI = new CheckListItem(checked, contents, _borderColor, _highlighted, _c, layoutItems);
        _checkedItems.add(cLI);
        cLI.createNote(null);
    }


    public View createCheckListNoTitle(){



        return LayoutInflater.from(_c).inflate(R.layout.layout_checklist_cell, _layoutAllNotes, false);
    }

    public View createCheckListTitle(){
        final View layoutNoteBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_checklist_cell_title, _layoutAllNotes, false);


        //Check if title has been set
        if (_title != null) {
            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            titleOnNote.setText(_title);
        }

        return layoutNoteBeingAdded;

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void createNote(Integer index) {

        //Get the date
        if (_date == null) {
            _date = LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1];
        }


        if(_noTitle){
            _layoutNoteBeingAdded = createCheckListNoTitle();
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutChecklist), _layoutNoteBeingAdded.findViewById(R.id.layoutBottom), _layoutNoteBeingAdded.findViewById(R.id.menuButton),
                    _layoutNoteBeingAdded.findViewById(R.id.buttonAdd)};
        }
        else{
            _layoutNoteBeingAdded = createCheckListTitle();
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutTextCell), _layoutNoteBeingAdded.findViewById(R.id.layoutBottom),
                    _layoutNoteBeingAdded.findViewById(R.id.menuButton), _layoutNoteBeingAdded.findViewById(R.id.buttonAdd)};

            //Set the date
            TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            dateTimeCreated.setText(_date);
        }



        setBorder();

        _layoutItems = _layoutNoteBeingAdded.findViewById(R.id.layoutItems);

        for(CheckListItem cLi : _checkedItems){
            Log.e("add", "add");
            addItem(cLi);
        }



        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);

        menuButton.setOnClickListener(menuListener);

        ImageButton addButton = _layoutNoteBeingAdded.findViewById(R.id.buttonAdd);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(false, null, _layoutItems);
            }
        });




        if(index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        }
        else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }







    }

    @Override
    public String saveNote() {
        String note = "CheckListCell" + "\n";
        note += "borderColor#%^$ " + Integer.toString(_borderColor) + "\n";
        note += "highlighted#%^$ " + _highlighted + "\n";
        if(!_noTitle){
            note += "title#%^$ " + ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText() + "\n";
        }
        note += "date#%^$ " + _date + "\n";
        note += "noTitle#%^$ " + _noTitle + "\n";
        for(CheckListItem cli : _checkedItems){
            note += cli.saveNote();
        }
        return note;
    }

    public String getReminderTitle(){
        _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
        if(!_title.equals("Title")){
            return _title;
        }
        else {
            return "Reminder for checklist";
        }

    }

    @Override
    public String getTitle() {
        return ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }
}
