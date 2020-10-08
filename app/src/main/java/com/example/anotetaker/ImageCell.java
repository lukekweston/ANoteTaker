package com.example.anotetaker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.RequiresApi;


public class ImageCell extends Note {

    String _fileLocation = null;
    String _date = null;


    public boolean ADDINGIMAGE = false;
    private ImageView displayImage;  // imageview
    private Button addImageFromFile, addImageFromCamera;


    private int GALLERY = 1, CAMERA = 2;


    public ImageCell(String title, String date, String fileLocation, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes) {

        _fileLocation = fileLocation;
        _title = title;
        _date = date;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        Log.e("highlighted", _highlighted + "");
        _c = c;
        _layoutAllNotes = layoutAllNotes;


    }


    public View ImageCellNoTitle() {
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCellNoTitle), layoutBeingAdded.findViewById(R.id.imageView), layoutBeingAdded.findViewById(R.id.menuButton)};
        return layoutBeingAdded;
    }

    public View imageTitle() {
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell_title, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCell), layoutBeingAdded.findViewById(R.id.imageView), layoutBeingAdded.findViewById(R.id.menuButton)};
        return layoutBeingAdded;
    }


    public void setDisplayImage(String filelocation, Bitmap image) {
        displayImage.setImageBitmap(image);
        _fileLocation = filelocation;
        addImageFromCamera.setVisibility(View.INVISIBLE);
        addImageFromFile.setVisibility(View.INVISIBLE);
        ADDINGIMAGE = false;


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNote(Integer index) {
        Log.e("dd",_noTitle + "");

        _layoutNoteBeingAdded = _noTitle ? ImageCellNoTitle() : imageTitle();


        setBorder();

        ImageButton menuButton = _layoutNoteBeingAdded.findViewById(R.id.menuButton);
        menuButton.setClickable(true);

        addImageFromFile = _layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
        addImageFromCamera = _layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);

        displayImage = _layoutNoteBeingAdded.findViewById(R.id.imageView);


        if (_fileLocation != null) {
            File imgFile = new File(_fileLocation);


            if (imgFile.exists()) {
                displayImage = _layoutNoteBeingAdded.findViewById(R.id.imageView);
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                displayImage.setImageBitmap(myBitmap);
                addImageFromFile.setVisibility(View.INVISIBLE);
                addImageFromCamera.setVisibility(View.INVISIBLE);

            }

        }


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


        //Creating with title
        if (! _noTitle) {

            TextView dateTimeCreated = _layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
            dateTimeCreated.setText(_date);
            if(_title != null) {
                TextView titleOfNote = _layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
                titleOfNote.setText(_title);
            }
        }



        menuButton.setOnClickListener(menuListener);


        if(index == null) {
            _layoutAllNotes.addView(_layoutNoteBeingAdded);
        }
        else {
            _layoutAllNotes.addView(_layoutNoteBeingAdded, index);
        }


    }


    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) _c).startActivityForResult(galleryIntent, GALLERY);
    }

    void takePhotoFromCamera() {
//https://www.semicolonworld.com/question/45696/low-picture-image-quality-when-capture-from-camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        ((NoteActivity) _c).imageUri = ((Activity) _c).getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ((NoteActivity) _c).imageUri);
        ((Activity) _c).startActivityForResult(intent, CAMERA);

//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        ((Activity) _c).startActivityForResult(intent, CAMERA);

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
        _title = ((EditText) _layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
        if(!_title.equals("Title") && !_title.equals(null)){
            return _title;
        }
        else{
            return "Reminder for image";
        }
    }

    @Override
    public String getTitle() {
        return ((EditText)_layoutNoteBeingAdded.findViewById(R.id.editTextTitle)).getText().toString();
    }
}
