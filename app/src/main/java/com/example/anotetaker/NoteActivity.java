/*
todo
1. fix the border setting, cannot create a default black border when color is -1,
work around at the moment is just not setting a border programatically and using the layout border
makes code spagehtti for adding an image
2. make the notes into classes?


 */


package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

public class NoteActivity extends AppCompatActivity {

    ImageButton buttonAdd;
    ScrollView scrollView;
    LinearLayout layout, layoutAllNotes;

    //border used in all layouts
    GradientDrawable border;
    String currentFolder = "";

    Uri imageUri = null;


    private static final String IMAGE_DIRECTORY = "/data/data/com.example.anotetaker/files/images";
    private static final String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";
    private Context mContext;
    private ImageView displayImage;  // imageview
    private int GALLERY = 1, CAMERA = 2;

    public int notesColour = -1;


    public String lastImageAddedLocation = "null";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_page);


        //get if the animation direction has been stored
        Intent intent = getIntent();
        String direction = intent.getStringExtra("activity");

        //Set the animation direction if right has been stored, else left
        if (direction != null && direction.equals("right")) {
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        }
        //Turn off animation if we are reloading a new color
        else if (direction != null && direction.equals("reload")) {
            this.overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
        } else {
            //Set the animation for opening this intent
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }

        //Get the folder/file we are currently working on
        SharedPreferences mPrefs = getSharedPreferences("NotebookNameValue", 0);
        String defaultValue = getResources().getString(R.string.workingFolder_default);
        currentFolder = mPrefs.getString(getString(R.string.curWorkingFolder), defaultValue);

//        if(currentFolder.split("/").length > 1){
//            //Set the animation for opening this intent
//            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
//        }

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
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                saveItems(layoutAllNotes);
//            }
//        }, 0, 500);


        buttonAdd.setOnClickListener(listener);


    }

    private void selectNoteTypeDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Note to add");
        String[] pictureDialogItems = {"Add text note and title", "Add image", "Add image with title", "Add exta notebook"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                addNoteCell(null, null, null);
                                break;
                            case 1:
                                addImageCell(true, null, null, null);
                                break;
                            case 2:
                                addImageCell(false, null, null, null);
                                break;
                            case 3:
                                //TODO: make this pop up better
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);

                                builder.setTitle("New notebook");
                                final EditText input = new EditText(NoteActivity.this);
                                input.setHint("Notebook name");

                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);


                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        addNoteBook(currentFolder + "/" + input.getText().toString().replace("/", "-"));

                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });


                                builder.show();
                            }

                            break;
                        }
                    }
                });
        pictureDialog.show();
    }

    @SuppressLint("ResourceAsColor")
    private void createBorder() {
        border = new GradientDrawable();
        border.setColor(0xFFFFFFFF);
        if (notesColour != -1) {

            border.setStroke(10, notesColour);
            buttonAdd.setColorFilter(notesColour);
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addNoteCell(String title, String date, String contents) {
        final View layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_note_cell, layoutAllNotes, false);

        if (title != null) {
            TextView titleOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            titleOnNote.setText(title);
        }


        TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
        if (date != null) {
            dateTimeCreated.setText(date);
        } else {
            dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);
        }


        EditText contentsOnNote = layoutNoteBeingAdded.findViewById(R.id.editTextTextMultiLine);

        if (contents != null) {
            contentsOnNote.append(contents);
        }
        //TODO fix this work around
        //work around because of of weird bug where it was over lapping?
        else {
            contentsOnNote.append("\n");
        }

        if (border != null) {
            //set border of whole layout
            ConstraintLayout note = layoutNoteBeingAdded.findViewById(R.id.layoutTextCell);
            note.setBackground(border);
            //set border of multiline
            contentsOnNote.setBackground(border);
        }


        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonRemove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAllNotes.removeView(layoutNoteBeingAdded);
                saveItems(layoutAllNotes);
            }
        });

        layoutAllNotes.addView(layoutNoteBeingAdded);
        saveItems(layoutAllNotes);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addImageCell(Boolean noTitle, String fileLocation, String title, String date) {

        View layoutNoteBeingAdded = null;

        if (noTitle) {
            layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell_no_title, layoutAllNotes, false);
            //set large border
            if (border != null) {
                ConstraintLayout outsideArea = layoutNoteBeingAdded.findViewById(R.id.layoutImageCellNoTitle);
                outsideArea.setBackground(border);
            }

        } else {
            layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell, layoutAllNotes, false);
            //set large border
            if (border != null) {
                ConstraintLayout outsideArea = layoutNoteBeingAdded.findViewById(R.id.layoutImageCell);
                outsideArea.setBackground(border);
            }
            ;
        }


        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonRemove);

        final Button addImageFromFile = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
        final Button addImageFromCamera = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);

        displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
        //set the images border
        if (border != null) {
            displayImage.setBackground(border);
        }
        final TextView fileLocationSave = layoutNoteBeingAdded.findViewById(R.id.fileLocation);
        if (fileLocation != null) {
            fileLocationSave.setText(fileLocation);
            File imgFile = new File(fileLocation);

            //Important - sets the file location of the image so that this layout can be saved and reloaded!
            fileLocationSave.setText(imgFile.toString());

            if (imgFile.exists()) {
                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                displayImage.setImageBitmap(myBitmap);
                addImageFromFile.setVisibility(View.INVISIBLE);
                addImageFromCamera.setVisibility(View.INVISIBLE);

            }

        }

        //Listener that updates when the image size updates
        final ImageView myImageView = (ImageView) layoutNoteBeingAdded.findViewById(R.id.imageView);
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


        final View finalLayoutNoteBeingAdded = layoutNoteBeingAdded;
        addImageFromFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayImage = finalLayoutNoteBeingAdded.findViewById(R.id.imageView);
                choosePhotoFromGallery();
            }
        });

        final View finalLayoutNoteBeingAdded1 = layoutNoteBeingAdded;
        addImageFromCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayImage = finalLayoutNoteBeingAdded1.findViewById(R.id.imageView);
                takePhotoFromCamera();
            }
        });

        //Creating with title
        if (!noTitle) {
            TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            TextView titleOfNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            //First time creating so get current date time
            if (date == null) {
                dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);
            }
            //Loading already created cell so put in saved date
            else {
                dateTimeCreated.setText(date);
            }
            //If loading file already has a title
            if (title != null) {
                titleOfNote.setText(title);
            }


        }

        layoutAllNotes.addView(layoutNoteBeingAdded);
        final View finalLayoutNoteBeingAdded2 = layoutNoteBeingAdded;
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutAllNotes.removeView(finalLayoutNoteBeingAdded2);
                saveItems(layoutAllNotes);
            }
        });

        saveItems(layoutAllNotes);
    }

    private void addNoteBook(String noteBookFile) {

        final View noteBookBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.activity_open_note_cell, layoutAllNotes, false);
        final TextView noteBookName = noteBookBeingAdded.findViewById(R.id.noteNametextView);
        noteBookName.setText(noteBookFile);

        ImageButton openButton = noteBookBeingAdded.findViewById(R.id.openNoteImageButton);

        int noteBookColor = -1;
        //get the color of the notebook
        try {
            BufferedReader reader;
            File file = new File(NOTEBOOK_DIRECTORY + "/" + noteBookFile +".txt");
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = reader.readLine();
                while (line != null) {
                    if (line.split(" ")[0].equals("color")) {
                        noteBookColor = Integer.parseInt(line.split(" ")[1]);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("io exception", e.toString());

        }


        if (noteBookColor != -1) {
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
                saveItems(layoutAllNotes);
                SharedPreferences mPrefs = getSharedPreferences("NotebookNameValue", 0);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(getString(R.string.curWorkingFolder), (String) noteBookName.getText());
                editor.commit();
                startActivity(new Intent(NoteActivity.this, NoteActivity.class));

            }

        };

        openButton.setOnClickListener(openNotebook);
        noteBookName.setOnClickListener(openNotebook);

        layoutAllNotes.addView(noteBookBeingAdded);
        saveItems(layoutAllNotes);


    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //Check if the back button has been pressed
            case android.R.id.home:
                goBacktoPreviousLayout();
                return true;


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

                //set the color of the note
                notesColour = randomAndroidColor;
                //save note with the new color
                saveItems(layoutAllNotes);
                //load the items to redraw
                Intent intent = getIntent();
                this.overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
                //set extra value so the animation will be disabled of a new intent
                intent.putExtra("activity", "reload");
                finish();
                startActivity(intent);

                //loadFolder(currentFolder);

                return true;

            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(randomAndroidColor));


//                layout.setBackgroundColor(randomAndroidColor);
//                layoutAllNotes.setBackgroundColor(randomAndroidColor);

            ///startActivity(new Intent(this, EmailIntent.class));
            //return true;
            case R.id.delete:

                builder = new AlertDialog.Builder(this);


                builder.setMessage("Are you sure you want to delete this notebook?")
                        .setTitle("Delete notebook");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteNoteBook();
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

                Log.e("dsad", "hello");

                //DeleteFileDialogFragment delete = new DeleteFileDialogFragment().onCreateDialog();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    public void loadFolder(String folderName) {
        //Add file extenstion
        folderName = folderName + ".txt";
        try {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(NOTEBOOK_DIRECTORY + "/" + folderName);

            if (file.exists()) {
                Log.e("loading", "loading");
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {

                    //Get the color of the notebook
                    if (line.split(" ")[0].equals("color") && !line.split(" ")[1].equals("-1")) {
                        notesColour = Integer.parseInt(line.split(" ")[1]);
                        //set the toolbar to the correct color
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(notesColour));
                        createBorder();
                    }


                    Log.d("StackOverflow", line);


                    if (line.equals("Layout start")) {

                        //Set up the note that is being inserted
                        while (!line.equals("Layout end")) {
                            line = reader.readLine();

                            //Bool to no use the layoutnotes.addview


                            //Todo: make this work with more layouts
                            //Title and text note
                            if (line.equals("2131296436")) {

                                String title = null;
                                String date = null;
                                String contents = null;

                                Boolean keepFillingData = false;
                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();

                                    //put in the date
                                    if (line.split(" ")[0].equals("2131296258")) {
                                        keepFillingData = false;
                                        date = (line.split(" ")[1] + " " + line.split(" ")[2]);


                                    }
                                    //put in the saved title to the note
                                    if (line.split(" ")[0].equals("2131296391")) {
                                        keepFillingData = false;
                                        title = line.replace("2131296391 ", "");

                                    }
                                    //Fill out the text box
                                    if (line.split(" ")[0].equals("2131296390")) {
                                        keepFillingData = true;
                                        line = line.replace("2131296390 ", "");
                                        contents = line + "\n";
                                    }
                                    //keep filling multiline text if no id
                                    else if (keepFillingData) {
                                        contents += line + "\n";
                                    }
                                }
                                //Remove the last new line character from contents
                                contents = contents.substring(0, contents.length() - 1);
                                addNoteCell(title, date, contents);
                            }

                            //Title and image note
                            if (line.equals("2131296432")) {

                                String fileLocation = null;
                                String title = null;
                                String date = null;

                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();

                                    //Get the title
                                    if (line.split(" ")[0].equals("2131296391")) {
                                        title = line.replace("2131296391 ", "");
                                    }

                                    //Get the date
                                    if (line.split(" ")[0].equals("2131296258")) {
                                        date = (line.split(" ")[1] + " " + line.split(" ")[2]);
                                    }

                                    //Get the fileLocation
                                    if (line.split(" ")[0].equals("2131296399")) {
                                        fileLocation = line.split(" ")[1];
                                    }
                                }
                                addImageCell(false, fileLocation, title, date);

                            }
                            //Image note with no title
                            if (line.equals("2131296433")) {

                                String fileLocation = null;

                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();
                                    //insert the image and set up the buttons
                                    if (line.split(" ")[0].equals("2131296399")) {
                                        String linesData = line.split(" ")[1];
                                        if (!linesData.equals("null")) {
                                            fileLocation = linesData;
                                            break;
                                        }

                                    }
                                }
                                addImageCell(true, fileLocation, null, null);

                            }


                            //Extra noteBook note
                            if (line.equals("2131296435")) {

                                while (!line.equals("Layout end")) {
                                    Log.e("lay", line);
                                    line = reader.readLine();
                                    if (line.split(" ")[0].equals("2131296455")) {
                                        String noteBookName = line.replace("2131296455 ", "");

                                        //insert the noteBook
                                        addNoteBook(noteBookName);

                                    }


                                }

                            }

                        }

                    }


                    line = reader.readLine();

                }
                reader.close();
            }
        } catch (Exception e) {
            Log.e("Exception", "File load failed: " + e.toString());
        }

    }


    public void saveItems(LinearLayout layoutItems) {
        //todo: make folders and save files as the folder name
        String fileName = currentFolder + ".txt";
        //  Log.e("filename", fileName);
        int itemCount = layoutItems.getChildCount();
        try {

            //Check directory exists
            if (fileName.contains("/")) {
                Log.e("dir", fileName);
                Log.e("dir", NOTEBOOK_DIRECTORY + "/" + fileName.split("/")[0]);

                //Get path to file ( splits off the last element, the file name)
                //TODO: is there a simpler way to do this
                String filePath = "";
                String[] filePathSplit = fileName.split("/");
                for (int i = 0; i < filePathSplit.length - 1; i++) {
                    filePath += "/" + filePathSplit[i];
                }
                Log.e("dir", filePath);


                File noteBookDirectory = new File(NOTEBOOK_DIRECTORY + filePath);
                if (!noteBookDirectory.exists()) {  // have the object build the directory structure, if needed.
                    noteBookDirectory.mkdirs();
                }
            }

            File noteBookDirectory = new File(NOTEBOOK_DIRECTORY);
            if (!noteBookDirectory.exists()) {  // have the object build the directory structure, if needed.
                noteBookDirectory.mkdirs();
            }
            //make or edit existing file
            File noteBookFile = new File(noteBookDirectory, fileName);


            BufferedWriter bw = new BufferedWriter(new FileWriter(noteBookFile));

            bw.write("color " + Integer.toString(notesColour) + "\n");

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(NoteActivity.this.openFileOutput(NOTEBOOK_DIRECTORY +"/" + fileName, NoteActivity.this.MODE_PRIVATE));

            for (int i = 0; i < itemCount; i++) {
                bw.write("Layout start" + "\n");

                //      Log.e("save", "Layout start");
                if (layoutItems.getChildAt(i) instanceof ConstraintLayout) {
                    ConstraintLayout cell = (ConstraintLayout) layoutItems.getChildAt(i);
                    //        Log.e("save", Integer.toString(cell.getId()));
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

                //    Log.e("save", "Layout wnd");

            }
            bw.close();
        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }


    //Deletes current notebook and sub notebooks
    public void deleteNoteBook() {
        Log.e("delete", NOTEBOOK_DIRECTORY + "/" + currentFolder);
        //Delete sub notebooks
        File dir = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder);
        if (dir.exists() && dir.isDirectory()) {
            deleteFilesInDir(dir);
        }
        //Delete current notebook file
        File currentNoteBook = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder + ".txt");
        currentNoteBook.delete();

        //Call back button to return to the layout above this
        goBacktoPreviousLayout();

    }

    //Recursively deletes files in dirctory/sub directorys
    public void deleteFilesInDir(File dir) {
        String[] children = dir.list();
        for (String child : children) {
            File f = new File(dir, child);
            if (f.isDirectory()) {
                deleteFilesInDir(f);
            }
            //TODO delete images that are used in these notebooks?
//            if(child.endsWith(".txt")){
//                Log.e("notebook", child);
//            }

            new File(dir, child).delete();
        }
        dir.delete();

    }

    public void goBacktoPreviousLayout() {
        if (currentFolder.contains("/")) {
            saveItems(layoutAllNotes);
            //Split out the last notebook in the chain
            String previousFolder = currentFolder.substring(0, currentFolder.lastIndexOf("/"));
            //pass the new working notebook folder into memory
            SharedPreferences mPrefs = getSharedPreferences("NotebookNameValue", 0);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(getString(R.string.curWorkingFolder), (String) previousFolder);
            editor.commit();

            Intent intent = new Intent(new Intent(NoteActivity.this, NoteActivity.class));
            //Store animation direction so it can be set in the next activity
            intent.putExtra("activity", "right");
            startActivity(intent);


            return;
        }
        startActivity(new Intent(NoteActivity.this, MainActivity.class));

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
//https://www.semicolonworld.com/question/45696/low-picture-image-quality-when-capture-from-camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
            //TODO: make this faster
            //Try here
            //https://stackoverflow.com/questions/32043222/how-to-get-full-size-picture-and-thumbnail-from-camera-in-the-same-intent
            // Bitmap thumbnailSmall = (Bitmap) data.getExtras().get("data");
            Log.e("processing", "processing");
            //loading
//            ProgressDialog dialog = ProgressDialog.show(this, "",
//                    "Loading. Please wait...", true);
            //
            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("thumbnail", Integer.toString(thumbnail.getWidth()));

            Bitmap rotatedBitmap = null;

            //Check if width > height
            if (thumbnail.getWidth() > thumbnail.getHeight()) {
                //Rotate the image 90
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                //rotated bitmap
                rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
            }
            //Else dont rotate
            else {
                rotatedBitmap = thumbnail;
            }


            displayImage.setImageBitmap(rotatedBitmap);
            saveImage(rotatedBitmap);
            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Log.e("ds", Integer.toString(myBitmap.getWidth()));
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        Log.e("ds", Integer.toString(myBitmap.getWidth()));


        //File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        File wallpaperDirectory = new File(IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(IMAGE_DIRECTORY + "/" + Calendar.getInstance().getTimeInMillis() + ".jpg");
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


