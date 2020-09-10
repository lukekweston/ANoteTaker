package com.example.anotetaker;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    ListView simpleList;

    ArrayList<String> notebooks = new ArrayList<>();

    String NOTEBOOK_DIRECTORY = "/data/data/com.example.anotetaker/files/notebooks";

    LinearLayout layoutItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    layoutItems = (LinearLayout) findViewById(R.id.layout);

//        for (int i =0; i <12; i++){
//            test.add(Integer.toString(i));
//        }

        loadNoteBooks();

        requestMultiplePermissions();
//

//
//        countryList[0] = "sdas";

        //TODO: https://stackoverflow.com/questions/4540754/how-do-you-dynamically-add-elements-to-a-listview-on-android

        //https://abhiandroid.com/ui/listview#:~:text=List%20of%20scrollable%20items%20can%20be%20displayed%20in%20Android%20using%20ListView.&text=ListView%20in%20Android%20Studio%3A%20Listview,XML%20code%20to%20create%20it.


        //ImageButton b1 = (ImageButton) findViewById(R.id.buttonAdd);



//        b1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//            }
//        });

//        Button notebook1 = (Button) findViewById(R.id.buttonNote1);
//        notebook1.setOnClickListener(new View.OnClickListener() {
//                                       @Override
//                                       public void onClick(View view) {
//                                           SharedPreferences mPrefs = getSharedPreferences("IDvalue", 0);
//                                           SharedPreferences.Editor editor = mPrefs.edit();
//                                           editor.putString(getString(R.string.curWorkingFolder), "NoteBook1");
//                                           editor.commit();
//                                           startActivity(new Intent(MainActivity.this, NoteActivity.class));
//                                       }
//                                   }
//        );
//
//        Button notebook2 = (Button) findViewById(R.id.buttonNote2);
//        notebook2.setOnClickListener(new View.OnClickListener() {
//                                         @Override
//                                         public void onClick(View view) {
//                                             SharedPreferences mPrefs = getSharedPreferences("IDvalue", 0);
//                                             SharedPreferences.Editor editor = mPrefs.edit();
//                                             editor.putString(getString(R.string.curWorkingFolder), "NoteBook2");
//                                             editor.commit();
//                                             startActivity(new Intent(MainActivity.this, NoteActivity.class));
//                                         }
//                                     }
//        );


    }

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
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) { // check for permanent denial of any permission
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
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


    public void loadNoteBooks(){

        File wallpaperDirectory = new File(NOTEBOOK_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }
        ArrayList<String> notebooks = new ArrayList<String>();

        String path = NOTEBOOK_DIRECTORY;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
           notebooks.add(files[i].getName());
        }

       // simpleList = (ListView)findViewById(R.id.list);

        final View layoutNoteBeingAdded = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_listview, layoutItems, false);
        TextView test = (TextView) findViewById(R.id.textView);
        //test.setText("hello");

        ImageButton b1 = layoutNoteBeingAdded.findViewById(R.id.imageButton);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), ("asdsadasdasd"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        layoutItems.addView(layoutNoteBeingAdded);


//        ListView items = (ListView) layoutNoteBeingAdded.findViewById(R.id.list);
//
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, notebooks);
//        items.setAdapter(arrayAdapter);
//        items.setClickable(true);
//
//
//        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.e("hele", Integer.toString(i));
//                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
//                        Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//        layoutItems.addView(layoutNoteBeingAdded);





    }



}

