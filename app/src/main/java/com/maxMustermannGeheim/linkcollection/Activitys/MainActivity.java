package com.maxMustermannGeheim.linkcollection.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

public class MainActivity extends AppCompatActivity {
    Database database;
    SharedPreferences mySPR_daten;

    final int START_VIDEOS = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySPR_daten = getSharedPreferences("LinkCollection_Daten", 0);
//        mySPR_daten.edit().clear().commit();

        Database.getInstance(mySPR_daten, database1 -> {
            if (false/*database1.isLoaded()*/)
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
        startActivityForResult(intent, START_VIDEOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == START_VIDEOS) {
            ((TextView) findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
            ((TextView) findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
            ((TextView) findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
            ((TextView) findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Utility.saveDatabase(mySPR_daten);
        super.onDestroy();
    }
}
