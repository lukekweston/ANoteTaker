package com.example.anotetaker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {



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


//        requestMultiplePermissions();

        ImageButton addButton = (ImageButton) findViewById(R.id.addBtn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);


                builder.setTitle("New notebook");

                //TODO: update the layout
                //Check this https://stackoverflow.com/questions/10903754/input-text-dialog-android

                final EditText input = new EditText(MainMenuActivity.this);
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


                        new NewNoteBookCell(input.getText().toString().replace("/","-"), MainMenuActivity.this, layoutItems).createNote(null);

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

//    private void requestMultiplePermissions() {
//        Dexter.withActivity(this)
//                .withPermissions(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        if (report.areAllPermissionsGranted()) {  // check if all permissions are granted
//                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
//                        }
//
//                        if (report.isAnyPermissionPermanentlyDenied()) { // check for permanent denial of any permission
//                            // show alert dialog navigating to Settings
//                            //openSettingsDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).
//                withErrorListener(new PermissionRequestErrorListener() {
//                    @Override
//                    public void onError(DexterError error) {
//                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .onSameThread()
//                .check();
//    }


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
                notebooks.add(files[i].getName().split(".txt")[0]);
            }
        }


        for (String nb : notebooks) {

            new NewNoteBookCell(nb, MainMenuActivity.this, layoutItems).createNote(null);
        }


    }





}

