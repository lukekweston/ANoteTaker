package com.example.anotetaker;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;

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
import java.util.List;


import androidx.appcompat.app.AppCompatActivity;


//This activity is only started when the app is opened, it checks the permissions and opens the app back up to the last window
public class InitialStartActivity extends AppCompatActivity {


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestMultiplePermissions();

    }

    //Checks that the permissions are enabled, if so it will load the last seen layout
    //Else it will kill the app
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
                            loadPreviousLayout();
                        } else {
                            Log.e("bad", "is bad");
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {


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

    //Loads the last opened layout
    public void loadPreviousLayout() {

        final File file = new File("/data/data/com.example.anotetaker/files" + "/" + "lastImageAddedLocation.txt");
        try {
            //Check if file exists, wont exist for the first time running this app
            if (file.exists()) {

                FileInputStream is;
                BufferedReader reader;

                Log.e("loading", "loading");
                is = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(is));
                String lastFile = reader.readLine();
                is.close();

                Log.e("lastFile", lastFile);
                //Checks if we were last in the main menu
                if (lastFile.equals("MainMenu")) {
                    this.startActivity(new Intent(context, MainMenuActivity.class));
                    return;
                }

                //Else opens the note book
                //Open the note book
                SharedPreferences mPrefs = this.getSharedPreferences("NotebookNameValue", 0);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(this.getString(R.string.curWorkingFolder), lastFile);
                editor.commit();

                this.startActivity(new Intent(this, NoteActivity.class));
                return;
            }

        } catch (Exception e) {

        }
        //Any errors will default back to opening main menu
        this.startActivity(new Intent(this, MainMenuActivity.class));

    }

}
