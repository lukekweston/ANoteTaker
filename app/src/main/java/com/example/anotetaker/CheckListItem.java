package com.example.anotetaker;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckListItem extends Note{
    Boolean _checked = false;
    String _contents = null;


    public CheckListItem(Boolean checked, String contents, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes){
        _checked = checked;
        _contents = contents;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;
    }


    @Override
    public void createNote(Integer index) {
        _layoutNoteBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_checklist_item, _layoutAllNotes, false);

        _borderViews = new View[]{_layoutNoteBeingAdded.findViewById(R.id.layoutCheckListItem)};
        setBorder();

        final TextView editableText = _layoutNoteBeingAdded.findViewById(R.id.editTextCheckBox);
        CheckBox cB  = _layoutNoteBeingAdded.findViewById(R.id.checkBox);
        cB.setChecked(_checked);

        if(_contents != null){
            editableText.setText(_contents);
            editableText.setTextColor(Color.BLACK);
            editableText.setEnabled(false);
            editableText.setBackgroundResource(android.R.color.transparent);
            editableText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return true;  // Blocks input from hardware keyboards.
                }
            });
        }





        editableText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("text2", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("text", s.toString());
                if(s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\n'){
                    _contents = s.toString().replace("\n","");
                    editableText.setText(_contents);
                    editableText.setTextColor(Color.BLACK);
                    editableText.setEnabled(false);
                    editableText.setBackgroundResource(android.R.color.transparent);
                }

            }

        });


        _layoutAllNotes.addView(_layoutNoteBeingAdded);

    }

    @Override
    public String saveNote() {
        String note = "CheckListItem" + "\n";
        note += "checked#%^$ "  + ((CheckBox) _layoutNoteBeingAdded.findViewById(R.id.checkBox)).isChecked() + "\n";
        note += "contents#%^$ " +((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextCheckBox)).getText() + "\n";
        return note;
    }

    @Override
    public String getReminderTitle() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    public void setContextAndLayout(Context c, LinearLayout layoutAllNotes){
        _c = c;
        _layoutAllNotes = layoutAllNotes;
    }
}
