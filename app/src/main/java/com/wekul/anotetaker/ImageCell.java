package com.wekul.anotetaker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.File;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import static androidx.core.content.FileProvider.getUriForFile;


public class ImageCell extends Note {

    String _fileLocation = null;
    String _date = null;

    //Flag for adding the image into the right layout
    public boolean ADDINGIMAGE = false;

    private ImageView displayImage;
    private Button addImageFromFile, addImageFromCamera;


    private int GALLERY = 1, CAMERA = 2;


    public ImageCell(String title, String date, String fileLocation, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes) {

        _fileLocation = fileLocation;
        _title = title;
        _date = date;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;


    }


    //Creates a note with no title and sets up the border views
    public View ImageCellNoTitle() {
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCellNoTitle), layoutBeingAdded.findViewById(R.id.imageView), layoutBeingAdded.findViewById(R.id.menuButton)};
        return layoutBeingAdded;
    }

    //Creates a note with a title and sets up border views
    public View imageTitle() {
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell_title, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCell), layoutBeingAdded.findViewById(R.id.imageView), layoutBeingAdded.findViewById(R.id.menuButton), layoutBeingAdded.findViewById(R.id.layoutTitleBox)};
        return layoutBeingAdded;
    }


    //Listener that allows the displayed image to be opened in gallery
    public View.OnClickListener openGallery() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayImage.setOnClickListener(null);


                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri photoURI = getUriForFile(_c, "com.mydomain.fileprovider", (new File(_fileLocation)));
                intent.setDataAndType(photoURI, "image/*");



                _c.startActivity(intent);

                displayImage.setOnClickListener(this);





            }

        };

    }


    //Sets the display image and uses openGallery listener to make it so an image can be opened in gallery
    public void setDisplayImage(final String fileLocation, Bitmap image) {

        displayImage.setImageBitmap(image);

        _fileLocation = fileLocation;

        //Deactivate listeners on buttons and make them invisible
        addImageFromCamera.setOnClickListener(null);
        addImageFromFile.setOnClickListener(null);
        addImageFromCamera.setVisibility(View.INVISIBLE);
        addImageFromFile.setVisibility(View.INVISIBLE);


        //Set the opening gallery method
        displayImage.setOnClickListener(openGallery());

        //Image has been added set flag to false
        ADDINGIMAGE = false;

    }


    //Creates the note
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNote(Integer index) {
        //Get the layout type
        _layoutNoteBeingAdded = _noTitle ? ImageCellNoTitle() : imageTitle();

        //Creating with title, then set the title
        if (!_noTitle) {
            TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            dateTimeCreated.setText(_date);


            TextView titleOfNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
            if (_title != null) {
                Log.e("Text Height", titleOfNote.getTextSize() + "");
                titleOfNote.setText(_title);
            }

            //check if the date will fit in the title, if not do not display it or if the text height is too large
            Configuration configuration = _c.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp;
            Log.e("Screen width", screenWidthDp +"");
            Log.e("Threshold", THRESHOLDFORDATEDISPLAYED +"");
            if (screenWidthDp < THRESHOLDFORDATEDISPLAYED || titleOfNote.getTextSize() > THRESHOLDFORTEXTSIZE) {
                dateTimeCreated.setVisibility(View.INVISIBLE);
            }



        }

        //Make the borders
        setBorder();

        //Set up the menu
        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setClickable(true);
        menuButton.setOnClickListener(menuListener);


        //Create buttons and set listeners
        addImageFromFile = _layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
        addImageFromCamera = _layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);

        addImageFromFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ADDINGIMAGE = true;
                choosePhotoFromGallery();
            }
        });

        addImageFromCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ADDINGIMAGE = true;
                takePhotoFromCamera();
            }
        });


        //First time creating so get current date time
        if (_date == null) {
            _date = LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1];
        }


        displayImage = _layoutNoteBeingAdded.findViewById(R.id.imageView);

        //Add image to be displayed
        if (_fileLocation != null) {
            //Create file and check it exists
            File imgFile = new File(_fileLocation);
            if (imgFile.exists()) {
                //Try set the orentation and scale of the image correctly
                try {

                    setDisplayImage(_fileLocation, ((NoteActivity) _c).handleSamplingAndRotationBitmap(_c, Uri.fromFile(imgFile)));
                } catch (Exception e) {
                    //use _fileLocation and a bitmap created from _fileLocation
                    setDisplayImage(_fileLocation, BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                }
            }
            //Image has been deleted or moved, set fileLocation to null and dont display
            else {
                _fileLocation = null;
            }

        }


        //Add view at the right index
        if (index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        } else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }

    //Starts intent to get image from gallery, intents returned result is returned into NoteActivity
    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) _c).startActivityForResult(galleryIntent, GALLERY);
    }

    //Starts intent to get image from camera, intents returned result is returned into NoteActivity
    void takePhotoFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        ((NoteActivity) _c).imageUri = ((Activity) _c).getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ((NoteActivity) _c).imageUri);
        ((Activity) _c).startActivityForResult(intent, CAMERA);

    }

    //#%^$ added to the end of string so user will unlikely put in a string == to this and mess up the loading
    public String saveNote() {
        String file = "LayoutImageCell\n";
        file += "borderColor#%^$ " + _borderColor + "\n";
        file += "highlighted#%^$ " + _highlighted + "\n";
        if (!_noTitle) {
            file += "title#%^$ " + ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText() + "\n";
        }
        file += "date#%^$ " + _date + "\n";
        file += "filelocation#%^$ " + _fileLocation + "\n";
        file += "noTitle#%^$ " + _noTitle + "\n";

        return file;
    }

    @Override
    public String getReminderTitle() {

        if (!_noTitle) {
            _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
            if (_title != null && !_title.equals("") && !_title.equals("Title")) {
                return _title;
            }
        }
        return "Reminder for image";

    }

    @Override
    public String getTitle() {
        return ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }
}


