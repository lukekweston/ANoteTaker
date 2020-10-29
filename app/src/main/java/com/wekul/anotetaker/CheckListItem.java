package com.wekul.anotetaker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;


public class CheckListItem {

    //Default values for an empty/new checklistitem
    Boolean _checked = false;
    String _contents = null;
    int _borderColor = Color.BLACK;
    boolean _highlighted = false;
    public GradientDrawable _border;

    //Boolean variable to check if the note has been deleted
    public boolean _deleted = false;

    public CheckListItem(Boolean checked, String contents, int borderColor, boolean highlighted) {
        _checked = checked;
        _contents = contents;
        _borderColor = borderColor;
        _highlighted = highlighted;
        createBorder();
    }

    //For creating a blank/new checklist item
    public CheckListItem(int borderColor) {
        _borderColor = borderColor;
        createBorder();
    }

    //Creates the border for this checklist item
    public void createBorder() {
        _border = new GradientDrawable();
        _border.setColor(0xFFFFFFFF);
        if (_highlighted) {
            _border.setStroke(10, Color.parseColor("#FFFF00"));
        } else {
            _border.setStroke(10, _borderColor);
        }

    }

    //Saves the note
    public String saveNote() {
        String note = "CheckListItem" + "\n";
        note += "checked#%^$ " + _checked + "\n";
        note += "contents#%^$ " + _contents + "\n";
        return note;
    }


}
