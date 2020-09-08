package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteActivity extends AppCompatActivity {

    Button buttonAdd;
    ScrollView scrollView;
    LinearLayout layout, layoutAllNotes,layoutcell;
    String currentFolder = "";

    //For importing image
    private static final String IMAGE_DIRECTORY = "/YourDirectName";
    private Context mContext;
    private ImageView displayImage;  // imageview
    private int GALLERY = 1, CAMERA = 2;


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

        layoutAllNotes = (LinearLayout) findViewById(R.id.layoutItems);

        buttonAdd = (Button) findViewById(R.id.buttonAdd);

        loadFolder(currentFolder);


        final View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                selectNoteTypeDialog();
            }
        };


//        Todo make this listener work better
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                saveItems(layoutItems);
//            }
//        }, 0, 500);




        buttonAdd.setOnClickListener(listener);


    }

    private void selectNoteTypeDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Note to add");
        String[] pictureDialogItems = {"Add text note and title", "Add image and title"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                addNoteCell();
                                break;
                            case 1:
                                addImageCell();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addNoteCell(){
        final View layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutAllNotes, false);

        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonRemove);
        TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
        dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);

        layoutAllNotes.addView(layoutNoteBeingAdded );
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAllNotes.removeView(layoutNoteBeingAdded);
                saveItems(layoutAllNotes);
            }
        });

        saveItems(layoutAllNotes);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addImageCell(){
        final View layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell, layoutAllNotes, false);

        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonRemove);

        final Button addImageFromFile = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
        final Button addImageFromCamera = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);
        displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);

        addImageFromFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);


                choosePhotoFromGallary();

                //Set buttons to invisible if the image displayed is not the empty image
                if(displayImage.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.emptyimage).getConstantState()){
                    addImageFromFile.setVisibility(View.GONE);
                    addImageFromCamera.setVisibility(View.GONE);
                }

            }
        });

        addImageFromCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
                takePhotoFromCamera();
            }
        });

        TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
        dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);

        layoutAllNotes.addView(layoutNoteBeingAdded );
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAllNotes.removeView(layoutNoteBeingAdded);
                saveItems(layoutAllNotes);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        saveItems(layoutAllNotes);
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
                            if(line.equals("2131230896")) {
                                layoutNoteInserting = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutAllNotes, false);
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
                                    if(line.split(" ")[0].equals("2131230722")){
                                        keepFillingData = false;
                                        TextView dateTimeCreated = layoutNoteInserting.findViewById(R.id.DateTimeCreated);
                                        //@SuppressLint("ResourceType") TextView test = layout2.findViewById(2131165186);
                                        dateTimeCreated.setText(line.split(" ")[1] + " " + line.split(" ")[2]);
                                        //test.setText("hello");

                                    }
                                    //set up remove Button
                                    if(line.split(" ")[0].equals("2131230810")) {
                                        Button removeButton = layoutNoteInserting.findViewById(R.id.buttonRemove);

                                        final View finalLayoutNoteInserting = layoutNoteInserting;
                                        removeButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                layoutAllNotes.removeView(finalLayoutNoteInserting);
                                                saveItems(layoutAllNotes);

                                            }
                                        });


                                    }
                                    //put in the saved title to the note
                                    if(line.split(" ")[0].equals("2131230855")){
                                        keepFillingData = false;
                                        TextView title = layoutNoteInserting.findViewById(R.id.editTextTitle);
                                        line = line.replace("2131230855 ", "");
                                        title.setText(line);
                                    }
                                    if(line.split(" ")[0].equals("2131230854")){
                                        keepFillingData = true;
                                        line = line.replace("2131230854 ", "" );
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
                        layoutAllNotes.addView(layoutNoteInserting);
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





    ////####################################################################################################################################
    //Copied from https://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {"Select photo from gallery", "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    displayImage.setImageBitmap(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            displayImage.setImageBitmap(thumbnail);
            saveImage(thumbnail);
            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }




    ////####################################################################################################################################
}
