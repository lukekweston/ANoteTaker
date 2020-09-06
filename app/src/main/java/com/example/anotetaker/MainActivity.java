package com.example.anotetaker;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button notebook1 = (Button) findViewById(R.id.buttonNote1);
        notebook1.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           SharedPreferences mPrefs = getSharedPreferences("IDvalue", 0);
                                           SharedPreferences.Editor editor = mPrefs.edit();
                                           editor.putString(getString(R.string.curWorkingFolder), "NoteBook1");
                                           editor.commit();
                                           startActivity(new Intent(MainActivity.this, NoteActivity.class));
                                       }
                                   }
        );

        Button notebook2 = (Button) findViewById(R.id.buttonNote2);
        notebook2.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             SharedPreferences mPrefs = getSharedPreferences("IDvalue", 0);
                                             SharedPreferences.Editor editor = mPrefs.edit();
                                             editor.putString(getString(R.string.curWorkingFolder), "NoteBook2");
                                             editor.commit();
                                             startActivity(new Intent(MainActivity.this, NoteActivity.class));
                                         }
                                     }
        );


    }
}

