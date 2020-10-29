package com.wekul.anotetaker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.wekul.anotetaker.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


//Recycler view / adapter that is used for creating checklistitems for the checklistcell
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    public ArrayList<CheckListItem> _checkListItems = new ArrayList<>();

    private Context mContext;


    public RecyclerViewAdapter(ArrayList<CheckListItem> checkListItems, Context context){
        _checkListItems = checkListItems;
        mContext = context;
    }



    //Creates the checklistitem layout
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_checklist_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    //Sets up all the layouts/listeners for a checklist item
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {


        CheckListItem cLI = _checkListItems.get(position);
        //Listener for the check box button
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CheckListItem cLI = _checkListItems.get(position);
                boolean _checked = compoundButton.isChecked();
                compoundButton.requestFocus();
                //check if its highlighted
                if(cLI._highlighted){
                    cLI._border.setStroke(10, Color.parseColor("#FFFF00"));
                }
                else{
                    cLI._border.setStroke(10, cLI._borderColor);
                }
                //Change the background and the button to green if the check box is checked
                if (_checked) {
                    compoundButton.setButtonTintList(ColorStateList.valueOf(Color.GREEN));
                    cLI._border.setColor(Color.rgb(204,255,204));
                    holder.layout.setBackground(cLI._border);


                }
                //Else set the background to white and the button to black
                else {
                    compoundButton.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
                    cLI._border.setColor(Color.rgb(255,255,255));
                    holder.layout.setBackground(cLI._border);

                }
                //update the _checked value of the checklist item
                _checkListItems.get(position)._checked = _checked;

            }
        });

        //Todo: fix this hack, sets up the check box button correctly
        holder.checkBox.setChecked(cLI._checked);
        holder.checkBox.setChecked(!cLI._checked);
        holder.checkBox.setChecked(cLI._checked);
        holder.checkBox.setChecked(!cLI._checked);

        //get the contents od this checklistitem
        final String _contents = cLI._contents;

        //Set the the text and make it uneditable if it is not null
        if (_contents != null && !_contents.equals("null")) {
            holder.editText.setText(_contents);
            holder.editText.setTextColor(Color.BLACK);
            holder.editText.setEnabled(false);
            holder.editText.setBackgroundResource(android.R.color.transparent);

        } else {
            //set the selection on this edit text
            holder.editText.requestFocus();
            //Listener to check if the text has been set
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    //update saved value
                    _checkListItems.get(position)._contents = s.toString().replace("\n", "");
                    //is the string has a new line character meaning enter has been pressed, set the text and make it uneditable
                    if (s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\n') {
                        holder.editText.setText(_checkListItems.get(position)._contents);
                        holder.editText.setTextColor(Color.BLACK);
                        holder.editText.setEnabled(false);
                        holder.editText.setBackgroundResource(android.R.color.transparent);
                        //remove the listener
                        holder.editText.removeTextChangedListener(this);
                    }

                }

            });
        }


    }

    //Returns how many items are in the list
    @Override
    public int getItemCount() {
        return _checkListItems.size();
    }

    //Creates the viewholder that has the layouts that will be edited for each checklist item
    public class ViewHolder extends RecyclerView.ViewHolder{


        ConstraintLayout layout;
        CheckBox checkBox;
        EditText editText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Get the layouts
            layout = itemView.findViewById(R.id.layoutCheckListItem);
            checkBox = itemView.findViewById(R.id.checkBox);
            editText = itemView.findViewById(R.id.editTextCheckBox);


        }
    }

}
