package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Database database;
    SharedPreferences mySPR_daten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySPR_daten = getSharedPreferences("LinkCollection_Daten", 0);

        // ToDo: zufall

        Database.getInstance(mySPR_daten, database1 -> {
            database1.generateData();
            database = database1;
            ((TextView) findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
            ((TextView) findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
            ((TextView) findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
            ((TextView) findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
        });
    }

    public void openVideoActivity(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

}
