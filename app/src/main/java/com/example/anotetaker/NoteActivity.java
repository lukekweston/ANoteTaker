package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteActivity extends AppCompatActivity {

    Button buttonAdd;
    ScrollView scrollView;
    LinearLayout layout, layoutItems,layoutcell;
    String currentFolder = "";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_page);

        //Get the folder/file we are currently working on
        SharedPreferences mPrefs = getSharedPreferences("IDvalue",0);
        String defaultValue = getResources().getString(R.string.workingFolder_default);
        currentFolder = mPrefs.getString(getString(R.string.curWorkingFolder), defaultValue);

        getSupportActionBar().setTitle(currentFolder);


        layout = (LinearLayout) findViewById(R.id.layout);

        layoutcell = (LinearLayout) findViewById(R.id.layoutcelltext);

        layoutItems = (LinearLayout) findViewById(R.id.layoutItems);

        buttonAdd = (Button) findViewById(R.id.buttonAdd);

        loadFolder(currentFolder);


        final View.OnClickListener listner = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {


                final View layout2 = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutItems, false);

                Button removeButton = layout2.findViewById(R.id.buttonRemove);

                TextView dateTimeCreated = layout2.findViewById(R.id.DateTimeCreated);
                dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);

                layoutItems.addView(layout2 );
                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        layoutItems.removeView(layout2);
                        saveItems(layoutItems);
                    }
                });

                saveItems(layoutItems);


                final View layout3 = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell, layoutItems, false);

                removeButton = layout3.findViewById(R.id.buttonRemove);

                Button addImageFromFile = layout3.findViewById(R.id.buttonImageFromFile);
                addImageFromFile.setOnClickListener(new View.OnClickListener() {

                };
                                                    }


                dateTimeCreated = layout3.findViewById(R.id.DateTimeCreated);
                dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);

                layoutItems.addView(layout3 );
                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        layoutItems.removeView(layout3);
                        saveItems(layoutItems);
                    }
                });



            }
        };




//        Todo make this listener work better
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                saveItems(layoutItems);
//            }
//        }, 0, 500);




        buttonAdd.setOnClickListener(listner);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        saveItems(layoutItems);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        //saveItems(layoutItems);
        super.onDestroy();

    }

    @SuppressLint("ResourceType")
    public void loadFolder(String folderName) {

        try {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File("/data/data/com.example.anotetaker/files/" + folderName);

            if (file.exists()) {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {

                    Log.d("StackOverflow", line);


                    if(line.equals("Layout start")){

                        View layoutNoteInserting = null;
                        //Set up the note that is being inserted
                        while(!line.equals("Layout end")) {
                            line = reader.readLine();

                            //Todo: make this work with more layouts
                            if(line.equals("2131165333")) {
                                layoutNoteInserting = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutItems, false);
                                //Is of this type of layout so this stuff can be inside the if statement
                                Log.e("hello","hello");
                                //Bool for checking if it is multiline textData for the note feild
                                Boolean keepFillingData = false;
                                while(!line.equals("Layout end")) {
                                    line = reader.readLine();
                                    Log.e("test", line);
                                    //view that will be filled with note data
                                    EditText note = layoutNoteInserting.findViewById(R.id.editTextTextMultiLine);

                                    //put in the date
                                    if(line.split(" ")[0].equals("2131165186")){
                                        keepFillingData = false;
                                        TextView dateTimeCreated = layoutNoteInserting.findViewById(R.id.DateTimeCreated);
                                        //@SuppressLint("ResourceType") TextView test = layout2.findViewById(2131165186);
                                        dateTimeCreated.setText(line.split(" ")[1] + " " + line.split(" ")[2]);
                                        //test.setText("hello");

                                    }
                                    //set up remove Button
                                    if(line.split(" ")[0].equals("2131165270")) {
                                        Button removeButton = layoutNoteInserting.findViewById(R.id.buttonRemove);

                                        final View finalLayoutNoteInserting = layoutNoteInserting;
                                        removeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                layoutItems.removeView(finalLayoutNoteInserting);
                                                saveItems(layoutItems);

                                            }
                                        });


                                    }
                                    //put in the saved title to the note
                                    if(line.split(" ")[0].equals("2131165305")){
                                        keepFillingData = false;
                                        TextView title = layoutNoteInserting.findViewById(R.id.editTextTitle);
                                        line = line.replace("2131165305 ", "");
                                        title.setText(line);
                                    }
                                    if(line.split(" ")[0].equals("2131165304")){
                                        keepFillingData = true;
                                        line = line.replace("2131165304 ", "" );
                                        note.append(line + "\n");


                                    }
                                    else if(keepFillingData){
                                        note.append(line + "\n");
                                        //note.setText(line);
                                    }
                                }
                            }

                        }
                        //End of note found so now insert it
                        layoutItems.addView(layoutNoteInserting);
                    }



                    line = reader.readLine();

                }
                reader.close();
            }
        }
        catch(Exception e){
            Log.e("Exception", "File load failed: " + e.toString());
        }


    }

    public void saveItems(LinearLayout layoutItems){
        //todo: make folders and save files as the folder name
        String fileName = currentFolder;
        int itemCount = layoutItems.getChildCount();
        try {


            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(NoteActivity.this.openFileOutput(fileName, NoteActivity.this.MODE_PRIVATE));

            for (int i = 0; i < itemCount; i++) {
                outputStreamWriter.write("Layout start" + "\n");

                Log.e("save", "Layout start");
                if (layoutItems.getChildAt(i) instanceof ConstraintLayout) {
                    ConstraintLayout cell = (ConstraintLayout) layoutItems.getChildAt(i);
                    Log.e("save", Integer.toString(cell.getId()));
                    int count = cell.getChildCount();
                    for (int j = 0; j < count; j++) {

                        ConstraintLayout cellLayout = (ConstraintLayout) cell.getChildAt(j);
                        outputStreamWriter.write(Integer.toString(cellLayout.getId()) + "\n");
                        Log.e("save", Integer.toString(cellLayout.getId()));
                        int count2 = cellLayout.getChildCount();
                        View v = null;
                        for (int m = 0; m < count2; m++) {
                            v = cellLayout.getChildAt(m);
                            if (v instanceof TextView || v instanceof EditText) {
                                outputStreamWriter.write(Integer.toString(v.getId()) + " " + ((TextView) v).getText() + "\n");
                                Log.e("save", Integer.toString(v.getId()) + " " + ((TextView) v).getText());


                            }


                            //do something with your child element
                        }
                    }
                }
                outputStreamWriter.write("Layout end" + "\n");

                Log.e("save", "Layout wnd");

            }
            outputStreamWriter.close();
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }



    }
}
