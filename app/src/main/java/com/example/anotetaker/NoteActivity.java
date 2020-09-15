package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class NoteActivity extends AppCompatActivity {

    ImageButton buttonAdd;
    ScrollView scrollView;
    LinearLayout layout, layoutAllNotes;
    String currentFolder = "";


    private static final String IMAGE_DIRECTORY = "/data/data/com.example.anotetaker/files/images";
    private static final String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";
    private Context mContext;
    private ImageView displayImage;  // imageview
    private int GALLERY = 1, CAMERA = 2;


    public String lastImageAddedLocation = "null";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_page);

        //Set the animation for opening this intent
        this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);

        //Get the folder/file we are currently working on
        SharedPreferences mPrefs = getSharedPreferences("NotebookNameValue",0);
        String defaultValue = getResources().getString(R.string.workingFolder_default);
        currentFolder = mPrefs.getString(getString(R.string.curWorkingFolder), defaultValue);

        getSupportActionBar().setTitle(currentFolder);


        layout = (LinearLayout) findViewById(R.id.layout);



        layoutAllNotes = (LinearLayout) findViewById(R.id.layoutItems);

        buttonAdd = (ImageButton) findViewById(R.id.buttonAdd);

        loadFolder(currentFolder);


        final View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                selectNoteTypeDialog();
            }
        };


//        Todo make this listener work better
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveItems(layoutAllNotes);
            }
        }, 0, 500);




        buttonAdd.setOnClickListener(listener);


    }

    private void selectNoteTypeDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Note to add");
        String[] pictureDialogItems = {"Add text note and title", "Add image and title", "Add exta notebook"};
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
                            case 2:
                                //TODO:
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


                                builder.setTitle("New notebook");
                                final EditText input = new EditText(this);
                                input.setHint("Notebook name");


                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);





                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        createNoteBookEntry(input.getText().toString());



                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });


                                builder.show();
                        }

                                addNoteBook();
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
        final TextView fileLocationSave = layoutNoteBeingAdded.findViewById(R.id.fileLocation);

        //Listener that updates when the image size updates
        final ImageView myImageView = (ImageView) layoutNoteBeingAdded.findViewById(R.id.imageView);
        final ViewTreeObserver observer = myImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = myImageView.getHeight();
                //Set buttons to invisible if the image displayed is not the empty image
                if(myImageView.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.emptyimage).getConstantState()) {
                    addImageFromFile.setVisibility(View.GONE);
                    addImageFromCamera.setVisibility(View.GONE);
                    if(fileLocationSave.getText().equals("null") && !lastImageAddedLocation.equals("null")){
                        fileLocationSave.setText(lastImageAddedLocation);
                        lastImageAddedLocation = "null";
                    }

                    // Remove the layout listener so we don't waste time on future passes
                    myImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    //observer.removeOnGlobalLayoutListener(this);
                }

            }
        });


        addImageFromFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
                choosePhotoFromGallery();
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

        saveItems(layoutAllNotes);
    }

    private void addNoteBook(){

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

    //Create the menu
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_menu, menu);
        return true;
    }

    //Control the menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rename_notebook:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);


                builder.setTitle("Rename notebook");

                // Set up the input

                //TODO: update the layout

                //Check this https://stackoverflow.com/questions/10903754/input-text-dialog-android

                final EditText input = new EditText(this);
                input.setHint(currentFolder);
//                input.setPadding(
//                        19, // if you look at android alert_dialog.xml, you will see the message textview have margin 14dp and padding 5dp. This is the reason why I use 19 here
//                        0,
//                        19,
//                        0
//                );

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                //builder.set



                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });


                builder.show();


                return true;
                //TODO: make this change the primary dark colour, so the whole theme can change, also make the colour save/loadable
            case R.id.setColour:
                int[] androidColors = getResources().getIntArray(R.array.androidcolors);
                int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
                getTheme().applyStyle(randomAndroidColor, true);

                layout.setBackgroundColor(randomAndroidColor);
                layoutAllNotes.setBackgroundColor(randomAndroidColor);

                ///startActivity(new Intent(this, EmailIntent.class));
                return true;
            case R.id.delete:

                builder = new AlertDialog.Builder(this);


                builder.setMessage("Are you sure you want to delete this notebook?")
                        .setTitle("Delete notebook");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();

                Log.e("dsad","hello");

                //DeleteFileDialogFragment delete = new DeleteFileDialogFragment().onCreateDialog();
                return true;




            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("ResourceType")
    public void loadFolder(String folderName) {

        try {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(NOTEBOOK_DIRECTORY +"/" + folderName);

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
                            //Title and text note
                            if(line.equals("2131296435")) {
                                layoutNoteInserting = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutAllNotes, false);
                                //Is of this type of layout so this stuff can be inside the if statement
                                //Bool for checking if it is multiline textData for the note feild
                                Boolean keepFillingData = false;
                                while(!line.equals("Layout end")) {
                                    line = reader.readLine();
                                    Log.e("test", line);
                                    //view that will be filled with note data
                                    EditText note = layoutNoteInserting.findViewById(R.id.editTextTextMultiLine);

                                    //put in the date
                                    if(line.split(" ")[0].equals("2131296258")){
                                        keepFillingData = false;
                                        TextView dateTimeCreated = layoutNoteInserting.findViewById(R.id.DateTimeCreated);
                                        //@SuppressLint("ResourceType") TextView test = layout2.findViewById(2131165186);
                                        dateTimeCreated.setText(line.split(" ")[1] + " " + line.split(" ")[2]);
                                        //test.setText("hello");

                                    }
                                    //set up remove Button
                                    if(line.split(" ")[0].equals("2131296345")) {
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
                                    if(line.split(" ")[0].equals("2131296391")){
                                        keepFillingData = false;
                                        TextView title = layoutNoteInserting.findViewById(R.id.editTextTitle);
                                        line = line.replace("2131296391 ", "");
                                        title.setText(line);
                                    }
                                    //Fill out the text box
                                    if(line.split(" ")[0].equals("2131296390")){
                                        keepFillingData = true;
                                        line = line.replace("2131296390 ", "" );
                                                                       note.append(line + "\n");
                                    }
                                    //keep filling multiline text if no id
                                    else if(keepFillingData){
                                        note.append(line + "\n");
                                        //note.setText(line);
                                    }
                                }
                            }

                            //Title and image note
                            if(line.equals("2131296433")){

                                layoutNoteInserting = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell, layoutAllNotes, false);
                                //Is of this type of layout so this stuff can be inside the if statement
                                //Bool for checking if it is multiline textData for the note feild
                                while(!line.equals("Layout end")) {
                                    line = reader.readLine();
                                    Log.e("test", line);
                                    //view that will be filled with note data
                                    EditText note = layoutNoteInserting.findViewById(R.id.editTextTextMultiLine);

                                    //set up remove Button
                                    if(line.split(" ")[0].equals("2131296345")) {
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
                                    if(line.split(" ")[0].equals("2131296391")){
                                        TextView title = layoutNoteInserting.findViewById(R.id.editTextTitle);
                                        line = line.replace("2131296391 ", "");
                                        title.setText(line);
                                    }

                                    //put in the date
                                    if (line.split(" ")[0].equals("2131296258")) {
                                        TextView dateTimeCreated = layoutNoteInserting.findViewById(R.id.DateTimeCreated);
                                        //@SuppressLint("ResourceType") TextView test = layout2.findViewById(2131165186);
                                        dateTimeCreated.setText(line.split(" ")[1] + " " + line.split(" ")[2]);
                                        //test.setText("hello");

                                    }

                                    //insert the image and set up the buttons
                                    if (line.split(" ")[0].equals("2131296399")) {
                                        {
                                            //set up buttons and imageview as if no image has been added

                                            final Button addImageFromFile = layoutNoteInserting.findViewById(R.id.buttonImageFromFile);
                                            final Button addImageFromCamera = layoutNoteInserting.findViewById(R.id.buttonImageFromCamera);

                                            final TextView fileLocationSave = layoutNoteInserting.findViewById(R.id.fileLocation);

                                            displayImage = layoutNoteInserting.findViewById(R.id.imageView);
                                            //Listener that updates when the image size updates
                                            final ImageView myImageView = (ImageView) layoutNoteInserting.findViewById(R.id.imageView);
                                            final ViewTreeObserver observer = myImageView.getViewTreeObserver();
                                            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                @Override
                                                public void onGlobalLayout() {
                                                    int height = myImageView.getHeight();
                                                    //Set buttons to invisible if the image displayed is not the empty image
                                                    if (myImageView.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.emptyimage).getConstantState()) {
                                                        addImageFromFile.setVisibility(View.GONE);
                                                        addImageFromCamera.setVisibility(View.GONE);
                                                        if (fileLocationSave.getText().equals("null") && !lastImageAddedLocation.equals("null")) {
                                                            fileLocationSave.setText(lastImageAddedLocation);
                                                            lastImageAddedLocation = "null";
                                                        }

                                                        // Remove the layout listener so we don't waste time on future passes
                                                        myImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                                        //observer.removeOnGlobalLayoutListener(this);
                                                    }

                                                }
                                            });
                                            final View finalLayoutNoteInserting1 = layoutNoteInserting;
                                            addImageFromFile.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    displayImage = finalLayoutNoteInserting1.findViewById(R.id.imageView);
                                                    choosePhotoFromGallery();
                                                }
                                            });
                                            addImageFromCamera.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    displayImage = finalLayoutNoteInserting1.findViewById(R.id.imageView);
                                                    takePhotoFromCamera();
                                                }
                                            });


                                            //Check if an image has already been loaded (set up buttons can be reused if this image gets removed in the future)
                                            if (!line.split(" ")[1].equals("null")) {
                                                addImageFromFile.setVisibility(View.INVISIBLE);
                                                addImageFromCamera.setVisibility(View.INVISIBLE);
                                                //TODO: remove the size change listener to increase performance, also should remove button listeners
                                                //observer.removeOnGlobalLayoutListener();
                                                Log.e("sdasd",line.split(" ")[1]);
                                                File imgFile = new  File(line.split(" ")[1]);

                                                if(imgFile.exists()){
                                                    displayImage = layoutNoteInserting.findViewById(R.id.imageView);
                                                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                                    displayImage.setImageBitmap(myBitmap);

                                                }


                                            }
                                        }




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
        Log.e("filename", fileName);
        int itemCount = layoutItems.getChildCount();
        try {

            //Check directory exists
            File noteBookDirectory = new File(NOTEBOOK_DIRECTORY);
            if (!noteBookDirectory.exists()) {  // have the object build the directory structure, if needed.
                noteBookDirectory.mkdirs();
            }
            //make or edit existing file
            File noteBookFile = new File(noteBookDirectory, fileName);

            Log.e("hmm",NOTEBOOK_DIRECTORY +"/" + fileName);

            BufferedWriter bw = new BufferedWriter(new FileWriter(noteBookFile));

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(NoteActivity.this.openFileOutput(NOTEBOOK_DIRECTORY +"/" + fileName, NoteActivity.this.MODE_PRIVATE));

            for (int i = 0; i < itemCount; i++) {
                bw.write("Layout start" + "\n");

                Log.e("save", "Layout start");
                if (layoutItems.getChildAt(i) instanceof ConstraintLayout) {
                    ConstraintLayout cell = (ConstraintLayout) layoutItems.getChildAt(i);
                    Log.e("save", Integer.toString(cell.getId()));
                    int count = cell.getChildCount();
                    for (int j = 0; j < count; j++) {

                        ConstraintLayout cellLayout = (ConstraintLayout) cell.getChildAt(j);
                        bw.write(Integer.toString(cellLayout.getId()) + "\n");
                        Log.e("save", Integer.toString(cellLayout.getId()));
                        int count2 = cellLayout.getChildCount();
                        View v = null;
                        for (int m = 0; m < count2; m++) {
                            v = cellLayout.getChildAt(m);
                            if (v instanceof TextView || v instanceof EditText) {
                                bw.write(Integer.toString(v.getId()) + " " + ((TextView) v).getText() + "\n");
                                Log.e("save", Integer.toString(v.getId()) + " " + ((TextView) v).getText());

                            }

                        }
                    }
                }
                bw.write("Layout end" + "\n");

                Log.e("save", "Layout wnd");

            }
            bw.close();
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
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
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

        //File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        File wallpaperDirectory = new File(IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(IMAGE_DIRECTORY + "/"+ Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            lastImageAddedLocation = f.getAbsolutePath();
            fo.close();
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    ////####################################################################################################################################
}


