package com.example.anotetaker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;


//This is the main menu activity, displays the top level of all notebooks
public class MainMenuActivity extends AppCompatActivity {


    String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";
    LinearLayout menuItems;

    //Keeps track of what cells are displayed/exist in this layout
    public ArrayList<NewNoteBookCell> notesBooksDisplayed = new ArrayList<NewNoteBookCell>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        saveCurrentLocation();
        setContentView(R.layout.activity_main_menu);


        //Set the animation for opening this intent
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

        menuItems = (LinearLayout) findViewById(R.id.layoutItems);


        loadNoteBooks();


        ImageButton addButton = (ImageButton) findViewById(R.id.addBtn);

        //Creates pop up for creating new Notebook
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainMenuActivity.this);
                alertDialog.setTitle("New Notebook");
                final EditText input = new EditText(MainMenuActivity.this);
                input.setHint("Notebook name");
                //Add a linear layout to pad the view
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(36, 36, 36, 36);
                input.setLayoutParams(lp);
                RelativeLayout container = new RelativeLayout(MainMenuActivity.this);
                RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                container.setLayoutParams(rlParams);
                container.addView(input);

                alertDialog.setView(container);

                //Set the confirm button
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String newNoteBook = input.getText().toString().replace("/", "-");

                        //Check if the notebook with this name already exits
                        for(NewNoteBookCell n : notesBooksDisplayed){
                            //If it does display error Toast and return
                            if(n._noteBookFile.equals(newNoteBook)){
                                Toast.makeText(getApplicationContext(), "Note book with title " + newNoteBook + " already exists", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        //If not create the note and add it to the list of items displayed
                        NewNoteBookCell nC = new NewNoteBookCell(newNoteBook, MainMenuActivity.this, menuItems);
                        notesBooksDisplayed.add(nC);
                        nC.createNote(null);


                    }
                });

                //Cancel, do nothing
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                alertDialog.show();

            }

        });


    }

    //Save the current location on save/pause for reopeneing
    @Override
    protected void onStop() {
        saveCurrentLocation();
        super.onStop();
    }

    @Override
    protected void onPause() {
        saveCurrentLocation();
        super.onPause();
    }

    //Saves that we are in the main menu
    public void saveCurrentLocation() {
        try {
            //make or edit existing file
            File noteBookFile = new File("/data/data/com.example.anotetaker/files" + "/" + "lastImageAddedLocation.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(noteBookFile));
            bw.write("MainMenu");
            bw.close();
            Log.e("Main menu saved", ":-)");
        } catch (Exception e) {

        }

    }


    //Loads the notebooks that are in the main menu and creates newNoteBookcells to link to them
    public void loadNoteBooks() {

        File notebookDirectory = new File(NOTEBOOK_DIRECTORY);
        // have the object build the directory structure, if needed, for if something got deleted
        if (!notebookDirectory.exists()) {
            notebookDirectory.mkdirs();
        }
        final ArrayList<String> notebooks = new ArrayList<String>();

        String path = NOTEBOOK_DIRECTORY;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        //get the .txt (notebook) files
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".txt")) {
                notebooks.add(files[i].getName().split(".txt")[0]);
            }
        }

        //Create the notebooks, add them to the displayed list and populate the main menu
        for (String nb : notebooks) {
            NewNoteBookCell nC = new NewNoteBookCell(nb, MainMenuActivity.this, menuItems);
            notesBooksDisplayed.add(nC);
            nC.createNote(null);
        }


    }


}

