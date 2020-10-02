package com.example.anotetaker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;



public class ImageCell extends Note {

    String _filelocation = null;
    String _title = null;
    String _date = null;
    boolean _noTitle = true;
    private ImageView displayImage;  // imageview
    public String lastImageAddedLocation = "null";


    private int GALLERY = 1, CAMERA = 2;
    Uri imageUri = null;
    private static final String IMAGE_DIRECTORY = "/data/data/com.example.anotetaker/files/images";

    public ImageCell(String title, String date, String filelocation, boolean noTitle, int borderColor, boolean highlighted, Context c, LinearLayout layoutAllNotes){

        _filelocation = filelocation;
        _title = title;
        _date = date;
        _noTitle = noTitle;
        _borderColor = borderColor;
        _highlighted = highlighted;
        _c = c;
        _layoutAllNotes = layoutAllNotes;


    }


    public void createNote(){

    }

    public View ImageCellNoTitle(){
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCellNoTitle), layoutBeingAdded.findViewById(R.id.imageView)};
        return layoutBeingAdded;
    }

    public View imageTitle(){
        View layoutBeingAdded = LayoutInflater.from(_c).inflate(R.layout.layout_image_cell_title, _layoutAllNotes, false);
        _borderViews = new View[]{layoutBeingAdded.findViewById(R.id.layoutImageCell), layoutBeingAdded.findViewById(R.id.imageView)};
        return layoutBeingAdded;
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addImageCell(Boolean noTitle, String fileLocation, String title, String date) {

        View layoutNoteBeingAdded = noTitle ? ImageCellNoTitle() : imageTitle();



        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonMenu);

        final Button addImageFromFile = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
        final Button addImageFromCamera = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);

        displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);


        if (_filelocation != null) {
            File imgFile = new File(_filelocation);

            //Important - sets the file location of the image so that this layout can be saved and reloaded!
            //fileLocationSave.setText(imgFile.toString());

            if (imgFile.exists()) {
                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                displayImage.setImageBitmap(myBitmap);
                addImageFromFile.setVisibility(View.INVISIBLE);
                addImageFromCamera.setVisibility(View.INVISIBLE);

            }

        }


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

        //Listener that updates when the image size updates
        final ImageView myImageView = (ImageView) layoutNoteBeingAdded.findViewById(R.id.imageView);
//        final ViewTreeObserver observer = myImageView.getViewTreeObserver();
////        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////            @Override
////            public void onGlobalLayout() {
////                int height = myImageView.getHeight();
////                //Set buttons to invisible if the image displayed is not the empty image
////                if (myImageView.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.emptyimage).getConstantState()) {
////                    addImageFromFile.setVisibility(View.GONE);
////                    addImageFromCamera.setVisibility(View.GONE);
////                    if (_filelocation.equals("null") && !lastImageAddedLocation.equals("null")) {
////                        fileLocationSave.setText(lastImageAddedLocation);
////                        lastImageAddedLocation = "null";
////                    }
////
////                    // Remove the layout listener so we don't waste time on future passes
////                    myImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
////                    //observer.removeOnGlobalLayoutListener(this);
////                }
////
////            }
////        });




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

        _layoutAllNotes.addView(layoutNoteBeingAdded);
        final View finalLayoutNoteBeingAdded2 = layoutNoteBeingAdded;
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _layoutAllNotes.removeView(finalLayoutNoteBeingAdded2);
            }
        });

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
        imageUri = ((Activity) _c).getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        ((Activity) _c).startActivityForResult(intent, CAMERA);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        (Activity).super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ((Activity) _c).RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(_c.getContentResolver(), contentURI);
                    String path = saveImage(bitmap);
                    Toast.makeText(((Activity) _c).getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
                    displayImage.setImageBitmap(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(((Activity) _c).getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
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
                        ((Activity) _c).getContentResolver(), imageUri);
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
            Toast.makeText(((Activity) _c).getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
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

            MediaScannerConnection.scanFile(_c,
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


//@RequiresApi(api = Build.VERSION_CODES.O)
//    private void addImageCell(Boolean noTitle, String fileLocation, String title, String date) {
//
//        View layoutNoteBeingAdded = null;
//        GradientDrawable border = null;
//
//        //create a new border to be used for this object (borders cannot be shared)
//        if (notesColour != -1) {
//            Log.e("dasd", "creating border");
//            border = new GradientDrawable();
//            border.setColor(0xFFFFFFFF);
//            border.setStroke(10, notesColour);
//        }
//
//        if (noTitle) {
//            layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell, layoutAllNotes, false);
//            //set large border
//            if (border != null) {
//                ConstraintLayout outsideArea = layoutNoteBeingAdded.findViewById(R.id.layoutImageCellNoTitle);
//                outsideArea.setBackground(border);
//            }
//
//        } else {
//            layoutNoteBeingAdded = LayoutInflater.from(NoteActivity.this).inflate(R.layout.layout_image_cell_title, layoutAllNotes, false);
//            //set large border
//            if (border != null) {
//                ConstraintLayout outsideArea = layoutNoteBeingAdded.findViewById(R.id.layoutImageCell);
//                outsideArea.setBackground(border);
//            }
//            ;
//        }
//
//
//        Button removeButton = layoutNoteBeingAdded.findViewById(R.id.buttonMenu);
//
//        final Button addImageFromFile = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromFile);
//        final Button addImageFromCamera = layoutNoteBeingAdded.findViewById(R.id.buttonImageFromCamera);
//
//        displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
//        //set the images border
//        if (border != null) {
//            displayImage.setBackground(border);
//        }
//        final TextView fileLocationSave = layoutNoteBeingAdded.findViewById(R.id.fileLocation);
//        if (fileLocation != null) {
//            fileLocationSave.setText(fileLocation);
//            File imgFile = new File(fileLocation);
//
//            //Important - sets the file location of the image so that this layout can be saved and reloaded!
//            fileLocationSave.setText(imgFile.toString());
//
//            if (imgFile.exists()) {
//                displayImage = layoutNoteBeingAdded.findViewById(R.id.imageView);
//                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                displayImage.setImageBitmap(myBitmap);
//                addImageFromFile.setVisibility(View.INVISIBLE);
//                addImageFromCamera.setVisibility(View.INVISIBLE);
//
//            }
//
//        }
//
//        //Listener that updates when the image size updates
//        final ImageView myImageView = (ImageView) layoutNoteBeingAdded.findViewById(R.id.imageView);
//        final ViewTreeObserver observer = myImageView.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int height = myImageView.getHeight();
//                //Set buttons to invisible if the image displayed is not the empty image
//                if (myImageView.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.emptyimage).getConstantState()) {
//                    addImageFromFile.setVisibility(View.GONE);
//                    addImageFromCamera.setVisibility(View.GONE);
//                    if (fileLocationSave.getText().equals("null") && !lastImageAddedLocation.equals("null")) {
//                        fileLocationSave.setText(lastImageAddedLocation);
//                        lastImageAddedLocation = "null";
//                    }
//
//                    // Remove the layout listener so we don't waste time on future passes
//                    myImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    //observer.removeOnGlobalLayoutListener(this);
//                }
//
//            }
//        });
//
//
//        final View finalLayoutNoteBeingAdded = layoutNoteBeingAdded;
//        addImageFromFile.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                displayImage = finalLayoutNoteBeingAdded.findViewById(R.id.imageView);
//                choosePhotoFromGallery();
//            }
//        });
//
//        final View finalLayoutNoteBeingAdded1 = layoutNoteBeingAdded;
//        addImageFromCamera.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                displayImage = finalLayoutNoteBeingAdded1.findViewById(R.id.imageView);
//                takePhotoFromCamera();
//            }
//        });
//
//        //Creating with title
//        if (!noTitle) {
//            TextView dateTimeCreated = layoutNoteBeingAdded.findViewById(R.id.DateTimeCreated);
//            TextView titleOfNote = layoutNoteBeingAdded.findViewById(R.id.editTextTitle);
//            //First time creating so get current date time
//            if (date == null) {
//                dateTimeCreated.setText(LocalDateTime.now().toLocalDate() + " " + LocalDateTime.now().toLocalTime().toString().split(":")[0] + ":" + LocalDateTime.now().toLocalTime().toString().split(":")[1]);
//            }
//            //Loading already created cell so put in saved date
//            else {
//                dateTimeCreated.setText(date);
//            }
//            //If loading file already has a title
//            if (title != null) {
//                titleOfNote.setText(title);
//            }
//
//
//        }
//
//        layoutAllNotes.addView(layoutNoteBeingAdded);
//        final View finalLayoutNoteBeingAdded2 = layoutNoteBeingAdded;
//        removeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                layoutAllNotes.removeView(finalLayoutNoteBeingAdded2);
//                saveItems(layoutAllNotes);
//            }
//        });
//
//        saveItems(layoutAllNotes);
//    }
