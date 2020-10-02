package com.example.anotetaker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    ListView simpleList;

    ArrayList<String> notebooks = new ArrayList<>();

    String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";

    LinearLayout layoutItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Set the animation for opening this intent
        this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

        layoutItems = (LinearLayout) findViewById(R.id.layoutItems);

        loadNoteBooks();

        requestMultiplePermissions();

        ImageButton addButton = (ImageButton) findViewById(R.id.addBtn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


                builder.setTitle("New notebook");

                //TODO: update the layout
                //Check this https://stackoverflow.com/questions/10903754/input-text-dialog-android

                final EditText input = new EditText(MainActivity.this);
                input.setHint("Notebook name");
//                input.setPadding(
//                        19, // if you look at android alert_dialog.xml, you will see the message textview have margin 14dp and padding 5dp. This is the reason why I use 19 here
//                        0,
//                        19,
//                        0
//                );

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        createNoteBookEntry(input.getText().toString().replace("/","-"));

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                builder.show();
            }
        });



    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) { // check for permanent denial of any permission
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    public void loadNoteBooks(){

        File notebookDirectory = new File(NOTEBOOK_DIRECTORY);
        if (!notebookDirectory.exists()) {  // have the object build the directory structure, if needed.
            notebookDirectory.mkdirs();
        }
        final ArrayList<String> notebooks = new ArrayList<String>();

        String path = NOTEBOOK_DIRECTORY;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            if(files[i].getName().endsWith(".txt")) {
                notebooks.add(files[i].getName());
            }
        }

       // simpleList = (ListView)findViewById(R.id.list);

        for (String nb : notebooks) {

            createNoteBookEntry(nb);
        }




    }

    public void createNoteBookEntry(String noteBookFile){

            final View noteBookBeingAdded = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_open_note_cell, layoutItems, false);
            final TextView noteBookName = noteBookBeingAdded.findViewById(R.id.noteNametextView);

            int noteBookColor = -1;
            //get the color of the notebook
            try {
                BufferedReader reader;
                File file = new File(NOTEBOOK_DIRECTORY +"/" + noteBookFile);
                if (file.exists()) {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String line = reader.readLine();
                    while (line != null) {
                        if (line.split(" ")[0].equals("color")){
                            noteBookColor = Integer.parseInt(line.split(" ")[1]);
                            break;
                        }
                    }
                }
                    }catch (Exception e){
                Log.e("io exception",e.toString());

            }

        //Display the file without .txt extension
        if(noteBookFile.contains(".txt")) {
            noteBookFile = noteBookFile.substring(0, noteBookFile.length() - 4);

        }




            noteBookName.setText(noteBookFile);

            ImageButton openButton = noteBookBeingAdded.findViewById(R.id.openNoteImageButton);
            if(noteBookColor != -1){
                openButton.setColorFilter(noteBookColor);
                openButton.setBackgroundColor(0x000000);
                //Create border
                GradientDrawable border = new GradientDrawable();
                border.setColor(0xFFFFFFFF);
                border.setStroke(10, noteBookColor);
                ConstraintLayout layoutNote = noteBookBeingAdded.findViewById(R.id.layoutOpenNoteCell);
                layoutNote.setBackground(border);
            }


            //Listener to open activity
            View.OnClickListener openNotebook = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences mPrefs = getSharedPreferences("NotebookNameValue", 0);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(getString(R.string.curWorkingFolder), (String) noteBookName.getText());
                    editor.commit();
                    startActivity(new Intent(MainActivity.this, NoteActivity.class));

                }

            };

            openButton.setOnClickListener(openNotebook);
            noteBookName.setOnClickListener(openNotebook);

            layoutItems.addView(noteBookBeingAdded);

        }




}

