/*
todo
1. fix the border setting, cannot create a default black border when color is -1,
work around at the moment is just not setting a border programatically and using the layout border
makes code spagehtti for adding an image
2. make the notes into classes?


 */


package com.example.anotetaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class NoteActivity extends AppCompatActivity {

    ImageButton buttonAdd;
    ScrollView scrollView;
    public LinearLayout layout, layoutAllNotes;

    //border used in all layouts
    GradientDrawable border;
    String currentFolder = "";

    public static Uri imageUri = null;


    private static final String IMAGE_DIRECTORY = "/data/data/com.example.anotetaker/files/images";
    private static final String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";
    private Context mContext;
    private ImageView displayImage;  // imageview
    private int GALLERY = 1;
    private static int CAMERA = 2;

    public int notesColour = -1;

    public ArrayList<Note> notesDisplayed = new ArrayList<Note>();

    Timer timer;
    TimerTask autoSaveEvent;


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
        timer = new Timer();
        int delay = 0;
        int period = 2000;
        autoSaveEvent = new TimerTask() {
            @Override
            public void run() {
                saveItems(layoutAllNotes);
            }
        };
        timer.scheduleAtFixedRate(autoSaveEvent, delay, period);

        buttonAdd.setOnClickListener(listener);


    }

    public void removeAutoSave(){
        timer.cancel();
    }

    public void onPause(){
        removeAutoSave();
        super.onPause();
    }


    private void selectNoteTypeDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Note to add");
        String[] pictureDialogItems = {"Text note", "Text note - bullet point", "Image", "Add Checklist", "Exta notebook"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                NoteCell nC = new NoteCell(null, null, null, NoteCell.Type.text,true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                nC.createNote(null);
                                break;
                            case 1:
                                nC = new NoteCell(null, null, null, NoteCell.Type.bulletpoint,true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                nC.createNote(null);
                                break;
                            case 2:
                                ImageCell iC = new ImageCell(null, null, null, true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(iC);
                                iC.createNote(null);
                                break;
                                //Check list
                            case 3:
                                CheckListCell cLC = new CheckListCell(null, null, true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(cLC);
                                cLC.createNote(null);
                                break;
                            case 4:
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


                                        //addNoteBook(currentFolder + "/" + input.getText().toString().replace("/", "-"));
                                        NewNoteBookCell nNNB = new NewNoteBookCell(currentFolder + "/" + input.getText().toString().replace("/", "-"), NoteActivity.this, layoutAllNotes);
                                        notesDisplayed.add(nNNB);
                                        nNNB.createNote(null);




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
                goBacktoPreviousLayout(false);
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
                timer.cancel();
                //Was not shutting intent while a task was operating, was causing errors, this fixes it
                while (true) {
                    try {
                        NoteActivity.this.finishAndRemoveTask();
                        break;
                    } catch (Exception e) {
                        Log.e("hmm", "onOptionsItemSelected: ", e);
                    }

                }

                finish();
                startActivity(intent);


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
                        buttonAdd.setColorFilter(notesColour);

                    }


                    Log.d("StackOverflow", line);


                    if (line.equals("Layout start")) {

                        //Set up the note that is being inserted
                        while (!line.equals("Layout end")) {
                            line = reader.readLine();

                            //Bool to no use the layoutnotes.addview


                            //Todo: make this work with more layouts
                            //Title and text note
                            if (line.equals("LayoutNoteCell")) {

                                Log.e("line", line);


                                boolean highlighted = false;
                                String title = null;
                                String date = null;
                                String contents = null;
                                NoteCell.Type type = null;
                                boolean noTitle = false;


                                Boolean keepFillingData = false;
                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();
                                    Log.e("line", line);


                                    if (line.split(" ")[0].equals("highlighted#%^$")) {
                                        keepFillingData = false;
                                        highlighted = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }

                                    //put in the saved title to the note
                                    if (line.split(" ")[0].equals("title#%^$")) {
                                        keepFillingData = false;
                                        title = line.substring(10, line.length());
                                        continue;
                                    }


                                    //put in the date
                                    if (line.split(" ")[0].equals("date#%^$")) {
                                        keepFillingData = false;
                                        date = (line.split(" ")[1] + " " + line.split(" ")[2]);
                                        continue;
                                    }

                                    //get if the note has or hasnt got a title
                                    if (line.split(" ")[0].equals("noTitle#%^$")) {
                                        keepFillingData = false;
                                        noTitle = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }

                                    //get the type of the note
                                    if(line.split(" ")[0].equals("type#%^$")){
                                        keepFillingData = false;
                                        Log.e("split", line.split(" ")[1]);
                                        if(line.split(" ")[1].equals("text")){
                                            type = NoteCell.Type.text;
                                        }
                                        else if(line.split( " ")[1].equals("bulletpoint")){
                                            type = NoteCell.Type.bulletpoint;
                                        }
                                        else if(line.split(" ")[1].equals("list")){
                                            type = NoteCell.Type.list;
                                        }
                                        continue;
                                    }


                                    //Fill out the text box
                                    if (line.split(" ")[0].equals("contents#%^$")) {
                                        keepFillingData = true;
                                        line = line.replace("contents#%^$ ", "");
                                        contents = line + "\n";
                                    }
                                    //keep filling multiline text if no id
                                    else if (keepFillingData) {
                                        contents += line + "\n";
                                    }


                                }
                                //Remove the last new line character from contents
                                contents = contents.substring(0, contents.length() - 1);
                                NoteCell nC = new NoteCell(title, date, contents, type, noTitle,  notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                nC.createNote(null);

                            }

                            //Add image note
                            if (line.equals("LayoutImageCell")) {

                                Boolean highlighted = false;
                                String title = null;
                                String date = null;
                                String fileLocation = null;
                                Boolean noTitle = false;

                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();


                                    //Get the title
                                    if (line.split(" ")[0].equals("title#%^$")) {
                                        title = line.replace("title#%^$ ", "");
                                        continue;
                                    }


                                    //Get the date
                                    if (line.split(" ")[0].equals("date#%^$")) {
                                        date = (line.split(" ")[1] + " " + line.split(" ")[2]);
                                        continue;
                                    }

                                    //get if its highlighted
                                    if (line.split(" ")[0].equals("highlighted#%^$")) {
                                        highlighted = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }


                                    //Get the fileLocation
                                    if (line.split(" ")[0].equals("filelocation#%^$")) {
                                        fileLocation = line.split(" ")[1];
                                        continue;
                                    }


                                    //get if the note has or hasnt got a title
                                    if (line.split(" ")[0].equals("noTitle#%^$")) {
                                        noTitle = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }


                                }
                                ImageCell iC = new ImageCell(title, date, fileLocation, noTitle, notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(iC);
                                iC.createNote(null);


                            }


                            //Extra noteBook note
                            if (line.equals("LayoutNewNoteBookCell")) {


                                String noteBookName = null;

                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();

                                    if (line.split(" ")[0].equals("filename#%^$")) {
                                        noteBookName = line.replace("filename#%^$ ", "");
                                    }


                                }
                                //insert the noteBook if it has not been deleted
                                if(!noteBookName.equals("!@#$deleted$#@!")) {
                                    NewNoteBookCell nNBC = new NewNoteBookCell(noteBookName, NoteActivity.this, layoutAllNotes);
                                    notesDisplayed.add(nNBC);
                                    nNBC.createNote(null);
                                }

                            }

                            if(line.equals("CheckListCell")){

                                Boolean highlighted = false;
                                String title = null;
                                String date = null;
                                Boolean noTitle = false;

                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();


                                    //Get the title
                                    if (line.split(" ")[0].equals("title#%^$")) {
                                        title = line.replace("title#%^$ ", "");
                                        continue;
                                    }


                                    //Get the date
                                    if (line.split(" ")[0].equals("date#%^$")) {
                                        date = (line.split(" ")[1] + " " + line.split(" ")[2]);
                                        continue;
                                    }

                                    //get if its highlighted
                                    if (line.split(" ")[0].equals("highlighted#%^$")) {
                                        highlighted = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }




                                    //get if the note has or hasnt got a title
                                    if (line.split(" ")[0].equals("noTitle#%^$")) {
                                        noTitle = Boolean.parseBoolean(line.split(" ")[1]);
                                        continue;
                                    }


                                    //get the check list items
                                    if(line.equals("CheckListItem")){
                                        CheckListCell cLC = new CheckListCell(title, date, noTitle, notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                        notesDisplayed.add(cLC);
                                        cLC.createNote(null);

                                        Boolean checked = false;
                                        String contents = "";
                                        while(!line.equals("Layout end")){
                                            line = reader.readLine();
                                            if (line.split(" ")[0].equals("checked#%^$")) {
                                                checked = Boolean.parseBoolean(line.split(" ")[1]);
                                                continue;
                                            }

                                            if (line.split(" ")[0].equals("contents#%^$")) {
                                                contents = line.replace("contents#%^$ ", "");
                                                cLC.addItem(checked, contents, cLC._layoutItems);
                                                continue;
                                            }
                                        }
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
            Log.e("what", Integer.toString(notesColour));

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(NoteActivity.this.openFileOutput(NOTEBOOK_DIRECTORY +"/" + fileName, NoteActivity.this.MODE_PRIVATE));

            for (Note n : notesDisplayed) {
                if(!n._deleted) {
                    bw.write("Layout start" + "\n");
                    bw.write(n.saveNote());
                }

                bw.write("Layout end" + "\n");



            }
            bw.close();


        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }


    //Deletes current notebook and sub notebooks
    public void deleteNoteBook() {
        try {
            Log.e("delete", NOTEBOOK_DIRECTORY + "/" + currentFolder);
            //Delete sub notebooks
            File dir = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder);
            if (dir.exists() && dir.isDirectory()) {
                deleteFilesInDir(dir);
            }
            //Delete current notebook file
            File currentNoteBook = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder + ".txt");
            Log.e("gi", currentNoteBook.toString());

            currentNoteBook.delete();
            if (currentFolder.contains("/")) {
                removeLinkToSelfFromPreviousLayout();
            }

            //Call back button to return to the layout above this
            goBacktoPreviousLayout(true);
        }
        catch (Exception e){

        }


    }

    //Removes the link from the previous layout by renaming the filnename in the newnotebookcell to !@#$deleted$#@!
    public void removeLinkToSelfFromPreviousLayout(){
        Log.e("previous", currentFolder);
        String previousFolder = NOTEBOOK_DIRECTORY + "/" + currentFolder.substring(0, currentFolder.lastIndexOf("/")) + ".txt";
        String newFile = "";
        //Load and change the file
        try {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(previousFolder);
            if (file.exists()) {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    Log.e("line", line);
                    newFile += line + "\n";
                    if(line.equals("LayoutNewNoteBookCell")){
                        while(!line.equals("Layout end")){
                            line = reader.readLine();
                            Log.e("hmm", line);
                            if(line.split(" ")[0].equals("filename#%^$") && line.split(" ")[1].equals(currentFolder)){

                                Log.e(currentFolder, line.split(" ")[1]);
                                newFile += "filename#%^$" + " !@#$deleted$#@!" +"\n";

                            }
                            else {
                                newFile += line + "\n";
                            }
                        }
                        newFile += line + "\n";
                    }
                    line = reader.readLine();
                }
                is.close();
            }

        }catch (Exception e){

        }
        //Save the file
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(previousFolder));
            bw.write(newFile);
            bw.close();

        }catch (Exception e){

        }


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

    public void goBacktoPreviousLayout(Boolean deleting) {

        //Remove the auto save and save the layout
        removeAutoSave();
        //If not deleting save the layout
        if(!deleting) {
            saveItems(layoutAllNotes);
        }

        if (currentFolder.contains("/")) {

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
            this.finishAndRemoveTask();



            return;
        }
        startActivity(new Intent(NoteActivity.this, MainActivity.class));

    }


    ////####################################################################################################################################
    //Copied from https://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity

    //Activity result from the camera called from an image cell object
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            cancelAddingImage();
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    setDisplayImage(path, bitmap);
                    saveItems(layoutAllNotes);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            //TODO: make this faster
            //Try here
            //https://stackoverflow.com/questions/32043222/how-to-get-full-size-picture-and-thumbnail-from-camera-in-the-same-intent
            //Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

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


            //displayImage.setImageBitmap(rotatedBitmap);
            String fileLocation = saveImage(rotatedBitmap);
            setDisplayImage(fileLocation, rotatedBitmap);
            saveItems(layoutAllNotes);

            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    //Sets the image in the correct cell
    public void setDisplayImage(String path, Bitmap image) {
        for (Note n : notesDisplayed) {
            if (n instanceof ImageCell) {
                if (((ImageCell) n).ADDINGIMAGE) {
                    ((ImageCell) n).setDisplayImage(path, image);
                }
            }
        }
    }

    //Sets the adding image to false if we are canceling adding an image
    public void cancelAddingImage() {
        for (Note n : notesDisplayed) {
            if (n instanceof ImageCell) {
                if (((ImageCell) n).ADDINGIMAGE) {
                    ((ImageCell) n).ADDINGIMAGE = false;
                }
            }
        }

    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);


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


