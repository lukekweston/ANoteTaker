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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheckListCell extends Note {

    private static final String TAG = "CheckListCell";

    ArrayList<CheckListItem> _checkedItems;
    LinearLayout _layoutItems;
    RecyclerViewAdapter _adapter;

    RecyclerView recyclerView;


    public CheckListCell(String title, String date, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes) {
        _title = title;
        _date = date;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
        _checkedItems = new ArrayList<CheckListItem>();
        _adapter = new RecyclerViewAdapter(_checkedItems, _c);
    }




    //For loading in checklist items
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addItem(Boolean checked, String contents, LinearLayout layoutItems) {
        CheckListItem cLI = new CheckListItem(checked, contents, _borderColor, _highlighted);
        _checkedItems.add(cLI);
    }


    //Creates a layout with no title
    public View createCheckListNoTitle() {
        return LayoutInflater.from(_c).inflate(R.layout.layout_checklist_cell, _layoutAllNotes, false);
    }

    //Creates a view with a titile
    public View createCheckListTitle() {
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
            _date = LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString()
                    .split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1];
        }


        //Create the layout and set up the views for the border
        if (_noTitle) {
            _layoutNoteBeingAdded = createCheckListNoTitle();
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutChecklist),
                    _layoutNoteBeingAdded.findViewById(R.id.layoutBottom), _layoutNoteBeingAdded.findViewById(R.id.menuButton),
                    _layoutNoteBeingAdded.findViewById(R.id.buttonAdd)};
        } else {
            _layoutNoteBeingAdded = createCheckListTitle();
            _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutTextCell),
                    _layoutNoteBeingAdded.findViewById(R.id.layoutBottom),
                    _layoutNoteBeingAdded.findViewById(R.id.menuButton), _layoutNoteBeingAdded.findViewById(R.id.buttonAdd)};

            //Set the date
            TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            dateTimeCreated.setText(_date);
        }

        //Create the border
        setBorder();

        _layoutItems = _layoutNoteBeingAdded.findViewById(R.id.layoutItems);

        //Set up the recyclerView for the checkListItems, se the itemtouch helper to swipe and delete items
        recyclerView = _layoutNoteBeingAdded.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(_c));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(_adapter);

        //Set up the drop down menu
        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(menuListener);

        //Set up the add button
        ImageButton addButton = _layoutNoteBeingAdded.findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add a new checkListItem to the layout
                _checkedItems.add(new CheckListItem(_borderColor));
                //refresh the layout
                recyclerView.setAdapter(_adapter);
                _adapter.notifyDataSetChanged();
            }
        });

        //Insert the layout at the correct index, for when recreating the layout with and without title
        if (index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        } else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }

    //Listener for swiping and deleting a checklist item from the recycler view
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    _checkedItems.remove(viewHolder.getAdapterPosition());
                    recyclerView.setAdapter(_adapter);
                    //adapter._checkListItems.remove(viewHolder.getAdapterPosition());
                    _adapter.notifyDataSetChanged();

                }
            };

    //Saves note in a standard format
    @Override
    public String saveNote() {
        String note = "CheckListCell" + "\n";
        note += "borderColor#%^$ " + Integer.toString(_borderColor) + "\n";
        note += "highlighted#%^$ " + _highlighted + "\n";
        if (!_noTitle) {
            note += "title#%^$ " + ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText() + "\n";
        }
        note += "date#%^$ " + _date + "\n";
        note += "noTitle#%^$ " + _noTitle + "\n";
        //Save each of the checklist items in the layout
        for (CheckListItem cli : _checkedItems) {
            note += cli.saveNote();
        }
        return note;
    }

    //Get the title for creating a reminder
    public String getReminderTitle() {
        _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
        if (!_title.equals("Title")) {
            return _title;
        } else {
            return "Reminder for checklist";
        }

    }

    @Override
    public String getTitle() {
        return ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }

    //Update the highlight values for all the checklist items
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void highlightChange() {
        _highlighted = !_highlighted;
        for (CheckListItem CLI : _checkedItems) {
            //change highlight values
            CLI._highlighted = _highlighted;

            //redraw the layout
            recyclerView.setAdapter(_adapter);
            _adapter.notifyDataSetChanged();

        }

    }
}