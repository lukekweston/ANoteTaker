/*
todo
1. fix the border setting, cannot create a default black border when color is -1,
work around at the moment is just not setting a border programatically and using the layout border
makes code spagehtti for adding an image
2. make the notes into classes?


 */


package com.wekul.anotetaker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;


import com.wekul.anotetaker.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static androidx.core.content.FileProvider.getUriForFile;

//Activity that all the notes are displayed in
public class NoteActivity extends AppCompatActivity {

    //Button for adding a new cell
    ImageButton buttonAdd;

    public LinearLayout layout, layoutAllNotes;
    public ScrollView scrollView;

    //Folder/filelocation that this note is in
    String currentFolder = "";

    public static Uri imageUri = null;


    private static final String IMAGE_DIRECTORY = "/data/data/com.wekul.anotetaker/files/images";
    private static final String NOTEBOOK_DIRECTORY = "/data/data/com.wekul.anotetaker/files/notebooks";
    private int GALLERY = 1;
    private static int CAMERA = 2;
    private static int REMINDER = 3;

    //Flag set for deleting this notebook, stops this note from being saved when killed
    public boolean deleting = false;

    public int notesColour = -1;

    //Notes in this display
    public ArrayList<Note> notesDisplayed = new ArrayList<Note>();


    public String lastImageAddedLocation = "null";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {

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

        //Set the title
        getSupportActionBar().setTitle(currentFolder);

        //Get the views that will be used for this layout
        layout = (LinearLayout) findViewById(R.id.layout);
        layoutAllNotes = (LinearLayout) findViewById(R.id.layoutItems);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        buttonAdd = (ImageButton) findViewById(R.id.buttonAdd);

        //Load the existing items
        loadFolder();

        //Set the buttons listener to open a menu to select the type of item to add
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //Set the scroll view listener to update if an item is added
                scrollView.addOnLayoutChangeListener(scrollViewListener());
                selectNoteTypeDialog();
            }
        });

        //remove text focus of loaded items
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    //Listener for scroll view that moves the scroll to the bottom after it gets updated
    //This listener then removes its self to stop its self interfering with adding titles
    //and check list items
    public View.OnLayoutChangeListener scrollViewListener() {
        return (new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                scrollView.fullScroll(View.FOCUS_DOWN);
                scrollView.removeOnLayoutChangeListener(this);
            }
        });
    }


    //On pause and stop if not deleting then saveItems
    public void onPause() {
        if (!deleting) {
            saveItems();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (!deleting) {
            saveItems();
        }
        super.onStop();
    }

    //When back button is pressed then return to previous layout method
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBackParentLayout();
            return true;
        }
        return false;
    }

    //Creates a pop up menu for adding a new item
    private void selectNoteTypeDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Note to add");
        String[] pictureDialogItems = {"Text note", "Text note - bullet point", "Image", "Add checklist", "Extra notebook"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            //Add a noteCell
                            case 0:
                                NoteCell nC = new NoteCell(null, null, null, NoteCell.Type.text, true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                nC.createNote(null);
                                break;
                            //NoteCell with bullet points
                            case 1:
                                nC = new NoteCell(null, null, null, NoteCell.Type.bulletpoint, true, notesColour, false, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                nC.createNote(null);
                                break;
                            //Imagecell
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
                            //New sub note
                            case 4: {
                                //Create the pop up for getting the title of the new sub note book
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteActivity.this);
                                alertDialog.setTitle("New Notebook");
                                final EditText input = new EditText(NoteActivity.this);
                                input.setHint("Notebook name");
                                //Add a linear layout to pad the view
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(36, 36, 36, 36);
                                input.setLayoutParams(lp);
                                RelativeLayout container = new RelativeLayout(NoteActivity.this);
                                RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                container.setLayoutParams(rlParams);
                                container.addView(input);

                                alertDialog.setView(container);

                                //Set the confirm button
                                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Get the name of the new note book, remove / as it will interfer with the folder structures
                                        String newNoteBook = currentFolder + "/" + input.getText().toString().replace("/", "-");
                                        //Check a note book with this name doesnt already exist
                                        for (Note n : notesDisplayed) {
                                            if (n instanceof NewNoteBookCell) {
                                                //If it does exist, print error toast and return
                                                if (((NewNoteBookCell) n)._noteBookFile.equals(newNoteBook)) {
                                                    Toast.makeText(getApplicationContext(), "Note book with title " + newNoteBook + " already exists", Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                            }
                                        }
                                        //Else create the new notebook cell
                                        NewNoteBookCell nNNB = new NewNoteBookCell(newNoteBook, NoteActivity.this, layoutAllNotes);
                                        notesDisplayed.add(nNNB);
                                        nNNB.createNote(null);

                                    }
                                });

                                //Cancel, do nothing
                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                                alertDialog.show();

                                break;
                            }
                        }
                    }
                });
        pictureDialog.show();


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
                goBackParentLayout();
                return true;


            case R.id.rename_notebook:

                //Create the pop up for getting the title of the new sub note book
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(NoteActivity.this);
                alertDialog.setTitle("New Rename notebook");
                final EditText input = new EditText(NoteActivity.this);
                input.setHint(currentFolder.split("/").length > 0 ? currentFolder.split("/")[currentFolder.split("/").length - 1] : currentFolder);
                //Add a linear layout to pad the view
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(36, 36, 36, 36);
                input.setLayoutParams(lp);
                RelativeLayout container = new RelativeLayout(NoteActivity.this);
                RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                container.setLayoutParams(rlParams);
                container.addView(input);

                alertDialog.setView(container);

                //Set the confirm button
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the new notebook name and rename
                        String newNoteBookName = input.getText().toString().replace("/", "-");
                        rename(newNoteBookName);
                    }
                });

                //Cancel, do nothing
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                alertDialog.show();

                return true;

            case R.id.setColour: {
                int[] androidColors = getResources().getIntArray(R.array.androidcolors);
                int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
                getTheme().applyStyle(randomAndroidColor, true);

                //set the color of the note
                notesColour = randomAndroidColor;
                //save note with the new color
                saveItems();
                //load the items to redraw
                Intent intent = getIntent();
                this.overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
                //set extra value so the animation will be disabled of a new intent
                intent.putExtra("activity", "reload");
                //   timer.cancel();
                //Was not shutting intent while a task was operating, was causing errors, this fixes it
                while (true) {
                    try {
                        NoteActivity.this.finishAndRemoveTask();
                        break;
                    } catch (Exception e) {
                    }

                }

                finish();
                startActivity(intent);


                return true;
            }

            case R.id.setReminder: {
                Calendar cal = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", cal.getTimeInMillis());
                intent.putExtra("allDay", false);
                intent.putExtra("rrule", "FREQ=DAILY");
                intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);

                intent.putExtra("title", currentFolder);


                this.startActivity(intent);
                return true;
            }
            case R.id.delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);


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


                //DeleteFileDialogFragment delete = new DeleteFileDialogFragment().onCreateDialog();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Loads the notes and displayes it from a file
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    public void loadFolder() {
        try {
            FileInputStream is;
            BufferedReader reader;
            final File file = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder + ".txt");
            //Check file actually exists
            if (file.exists()) {

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
                    //Get the position that the view was last saved in
                    else if (line.split(" ")[0].equals("lastPosition") && !line.split(" ")[1].equals("-1")) {
                        final int scrollY = Integer.parseInt(line.split(" ")[1]);
                        //Add a listener to update the position to the last position when the views are loaded
                        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                scrollView.scrollTo(0, scrollY);
                                scrollView.removeOnLayoutChangeListener(this);
                            }
                        });
                    }

                    //Find when a new layout starts
                    if (line.equals("Layout start")) {


                        //Loop till the end of the current layout being loaded
                        while (!line.equals("Layout end")) {
                            line = reader.readLine();


                            //load in and create a note cell
                            if (line.equals("LayoutNoteCell")) {

                                //Variables used to create the noteCell
                                boolean highlighted = false;
                                String title = null;
                                String date = null;
                                String contents = null;
                                NoteCell.Type type = null;
                                boolean noTitle = false;

                                //Bool for adding multiple lines of text to the contents
                                Boolean keepFillingData = false;
                                //Loop till the end of the layout note
                                while (!line.equals("Layout end")) {
                                    line = reader.readLine();


                                    //Get if the note is highlighted
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
                                    if (line.split(" ")[0].equals("type#%^$")) {
                                        keepFillingData = false;

                                        if (line.split(" ")[1].equals("text")) {
                                            type = NoteCell.Type.text;
                                        } else if (line.split(" ")[1].equals("bulletpoint")) {
                                            type = NoteCell.Type.bulletpoint;
                                        } else if (line.split(" ")[1].equals("list")) {
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
                                //Create the note add it to the list
                                NoteCell nC = new NoteCell(title, date, contents, type, noTitle, notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                notesDisplayed.add(nC);
                                //Display the added note
                                nC.createNote(null);

                            }

                            //Add image note
                            if (line.equals("LayoutImageCell")) {

                                //Variables for creating a new image cell
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
                                //Create the cell add it to the list and display it
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
                                if (!noteBookName.equals("!@#$deleted$#@!")) {
                                    NewNoteBookCell nNBC = new NewNoteBookCell(noteBookName, NoteActivity.this, layoutAllNotes);
                                    notesDisplayed.add(nNBC);
                                    nNBC.createNote(null);
                                }

                            }

                            //Add checklist note
                            if (line.equals("CheckListCell")) {

                                Boolean highlighted = false;
                                String title = null;
                                String date = null;
                                Boolean noTitle = false;

                                //flag for checking if a checklistcell has been added with sub items
                                Boolean added = false;

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

                                    //Adds a checklistcell that contains sub items
                                    //get the check list items
                                    if (line.equals("CheckListItem")) {
                                        CheckListCell cLC = new CheckListCell(title, date, noTitle, notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                        notesDisplayed.add(cLC);
                                        added = true;
                                        cLC.createNote(null);

                                        Boolean checked = false;
                                        String contents = "";
                                        //Loop through and add each of the checklist items
                                        while (!line.equals("Layout end")) {
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
                                //Add the Checklist cell if there was no items to add to it
                                if (!added) {
                                    CheckListCell cLC = new CheckListCell(title, date, noTitle, notesColour, highlighted, NoteActivity.this, layoutAllNotes);
                                    notesDisplayed.add(cLC);
                                    cLC.createNote(null);
                                }


                            }

                        }

                    }


                    line = reader.readLine();

                }
                reader.close();

                //layoutAllNotes.scrollTo(0, layoutAllNotes.getHeight());

            }
        } catch (Exception e) {

        }

    }



    //renames the current noteBook and all the links associated with this notebook
    public void rename(String newNoteBookName) {

        String previousFolder = currentFolder;

        //Rename currentNoteBook
        String[] currentFolderSplit = currentFolder.split("/");
        currentFolder = currentFolderSplit.length > 0 ?
                currentFolder.replaceAll(currentFolderSplit[currentFolderSplit.length - 1]+"$", newNoteBookName) :
                newNoteBookName;

        //Rename the links in the current notebook
        int i = 0;
        for (Note note : notesDisplayed) {
            if (note instanceof NewNoteBookCell) {
                ((NewNoteBookCell) note)._noteBookFile = ((NewNoteBookCell) note)._noteBookFile.replace(previousFolder, currentFolder);
                layoutAllNotes.removeView(note._layoutNoteBeingAdded);
                note.createNote(i);
            }
            i += 1;
        }

        //Save updated file
        saveItems();

        //Rename current directory
        File dirOld = new File(NOTEBOOK_DIRECTORY + "/" + previousFolder);
        File dirNew = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder);
        dirOld.renameTo(dirNew);


        //Rename links in sub files
        renameFilesInSubDirectories(previousFolder, currentFolder, dirNew);

        //Rename links in super files
        renameFilesInParentDirectories(previousFolder, currentFolder, dirNew.getParentFile());

        //delete the previous file
        File f = new File(NOTEBOOK_DIRECTORY + "/" + previousFolder + ".txt");
        f.delete();

        //Update the file name at the top
        getSupportActionBar().setTitle(currentFolder);


        //Save updated file
        saveItems();


    }

    //Renames all the links in the txt files in the parent directories
    public void renameFilesInParentDirectories(String previousFolder, String renamedFolder, File dir) {

        String[] children = dir.list();
        for (String child : children) {
            if (child.endsWith(".txt")) {
                //Update txt files
                updateNoteBookLinksInFile(previousFolder, renamedFolder, dir.toString() + "/" + child);
            }
        }

        //Check if we have go to the top level
        if (dir.toString().equals(NOTEBOOK_DIRECTORY)) {
            return;
        } else {
            //If not check the next parent folder
            renameFilesInParentDirectories(previousFolder, renamedFolder, dir.getParentFile());
        }
    }

    //Renames the folder links in all the sub directories
    public void renameFilesInSubDirectories(String previousFolder, String renamedFolder, File dir) {

        //Error checking
        if (!dir.exists()) {
            return;
        }

        String[] children = dir.list();

        for (String child : children) {
            File f = new File(dir, child);
            //If a directory call this method again on that level
            if (f.isDirectory()) {
                renameFilesInSubDirectories(previousFolder, renamedFolder, f);
            }
            //if txt file, update the links
            if (child.endsWith(".txt")) {
                updateNoteBookLinksInFile(previousFolder, renamedFolder, (dir.toString() + "/" + child));
            }


        }

    }

    //Updates the links in the current file
    public void updateNoteBookLinksInFile(String previousFolder, String renamedFolder, String fileToBeUpdated) {


        String lines = "";
        final File file = new File(fileToBeUpdated);
        //Load and update current file
        try {
            FileInputStream is;
            BufferedReader reader;


            if (file.exists()) {
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while (line != null) {

                    //Find the file name lines and update it
                    if (line.split(" ")[0].equals("filename#%^$")) {
                        line = line.replace(previousFolder, renamedFolder);
                    }
                    lines += line + "\n";
                    line = reader.readLine();

                }
                reader.close();
            }
        } catch (Exception e) {

        }


        //Save the updated file
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            bw.write(lines);
            bw.close();

        } catch (Exception e) {

        }


    }


    //Saves the location that we are currently in for loading next time
    public void saveCurrentLocation() {
        try {
            //make or edit existing file
            File noteBookFile = new File("/data/data/com.wekul.anotetaker/files" + "/" + "lastImageAddedLocation.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(noteBookFile));
            bw.write(currentFolder);
            bw.close();
        } catch (Exception e) {

        }

    }

    //Saves all the items that are displayed in the display
    public void saveItems() {


        //Save the location we are in for loading
        saveCurrentLocation();

        String fileName = currentFolder + ".txt";

        try {


            //Check directory exists
            if (fileName.contains("/")) {


                //Get path to file ( splits off the last element, the file name)
                String filePath = "";
                String[] filePathSplit = fileName.split("/");
                for (int i = 0; i < filePathSplit.length - 1; i++) {
                    filePath += "/" + filePathSplit[i];
                }

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
            bw.write("lastPosition " + Integer.toString(scrollView.getScrollY()) + "\n");


            //Write into the saved file the information from all of the notes displayed
            for (Note n : notesDisplayed) {
                if (!n._deleted) {
                    bw.write("Layout start" + "\n");
                    bw.write(n.saveNote());
                }

                bw.write("Layout end" + "\n");


            }
            bw.close();


        } catch (Exception e) {


        }


    }


    //Deletes current notebook and sub notebooks
    public void deleteNoteBook() {
        try {

            //Delete sub notebooks
            File dir = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder);
            if (dir.exists() && dir.isDirectory()) {
                deleteFilesInDir(dir);
            }


            //Delete current notebook file
            File currentNoteBook = new File(NOTEBOOK_DIRECTORY + "/" + currentFolder + ".txt");

            currentNoteBook.delete();


            if (currentFolder.contains("/")) {
                removeLinkToSelfFromPreviousLayout();
            }

            //Call back button to return to the layout above this
            deleting = true;
            goBackParentLayout();
        } catch (Exception e) {

        }


    }

    //Removes the link from the previous layout by renaming the filnename in the newnotebookcell to !@#$deleted$#@!
    public void removeLinkToSelfFromPreviousLayout() {

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
                    newFile += line + "\n";
                    if (line.equals("LayoutNewNoteBookCell")) {
                        while (!line.equals("Layout end")) {
                            line = reader.readLine();
                            if (line.split(" ")[0].equals("filename#%^$") && line.equals("filename#%^$ " + currentFolder)) {

                                newFile += "filename#%^$" + " !@#$deleted$#@!" + "\n";

                            } else {
                                newFile += line + "\n";
                            }
                        }
                        newFile += line + "\n";
                    }
                    line = reader.readLine();
                }
                is.close();
            }

        } catch (Exception e) {

        }
        //Save the file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(previousFolder));
            bw.write(newFile);
            bw.close();

        } catch (Exception e) {

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

            new File(dir, child).delete();
        }
        dir.delete();

    }

    //Return to the parent layout to this one
    public void goBackParentLayout() {

        //If not deleting save the layout
        if (!deleting) {
            saveItems();
        }
        //If parent layout will be a noteActivity
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
        //Else will be a mainmenu activity
        startActivity(new Intent(NoteActivity.this, MainMenuActivity.class));


    }


    //Handles images
    ////####################################################################################################################################
    //Copied and modified from https://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity

    //Get path from contentURI
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

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
                    String path = getRealPathFromURI(this, contentURI);
                    bitmap = handleSamplingAndRotationBitmap(this, contentURI);

//                    String path = saveImage(bitmap);

                    setDisplayImage(path, bitmap);

                    Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    saveItems();


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            Bitmap thumbnail = null;
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
            } catch (IOException e) {

                e.printStackTrace();
            }
            Bitmap rotatedBitmap = null;

            try {
                rotatedBitmap = handleSamplingAndRotationBitmap(this, imageUri);
            }
            catch (Exception e){
                e.printStackTrace();
            }



//            //Rotate the bit map so it is displayed correctly
//            //Check if width > height
//            if (thumbnail.getWidth() > thumbnail.getHeight()) {
//                //Rotate the image 90
//                Matrix matrix = new Matrix();
//                matrix.postRotate(90);
//                //rotated bitmap
//                rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
//            }
//            //Else dont rotate
//            else {
//                rotatedBitmap = thumbnail;
//            }


            //Dont save the rotated image, save originial
            String fileLocation = saveImage(rotatedBitmap);

            setDisplayImage(fileLocation, rotatedBitmap);
            saveItems();

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

        //Use JPEG format, faster and put into byteoutputstream - faster than png
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);


        File wallpaperDirectory = new File(IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(IMAGE_DIRECTORY + "/" + Calendar.getInstance().getTimeInMillis() +".jpg");
            f.createNewFile();

            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();


            lastImageAddedLocation = f.getAbsolutePath();
            fo.close();
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    ////####################################################################################################################################

    ///#######################################################################################################################################
    //Handle image rotation
    //https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


}


