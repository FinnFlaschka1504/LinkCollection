package com.maxMustermannGeheim.linkcollection.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

public class MainActivity extends AppCompatActivity {
    Database database;
    SharedPreferences mySPR_daten;

    enum CATIGORYS{
        Darsteller, Studios, Genre

    }

    public static final String EXTRA_CATIGORY = "EXTRA_CATIGORY";
    final int START_VIDEOS = 001;
    final int START_ACTOR = 002;
    final int START_STUDIO = 003;
    final int START_GENRE = 004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySPR_daten = getSharedPreferences("LinkCollection_Daten", 0);
//        mySPR_daten.edit().clear().commit();
        loadDatabase(false);
    }

    void loadDatabase(boolean createNew) {
        Database.OnInstanceFinishedLoading onInstanceFinishedLoading = database_neu -> {
            Toast.makeText(this, "Datenbank:\n" + Database.databaseCode, Toast.LENGTH_SHORT).show();

            if (false/*database_neu.isLoaded()*/)
                database_neu.generateData();
            database = database_neu;
            ((TextView) findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
            ((TextView) findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
            ((TextView) findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
            ((TextView) findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
        };

        if (Database.getInstance(mySPR_daten, onInstanceFinishedLoading, createNew) == null) {
            getDatabaseCode(databaseCode -> {
                        mySPR_daten.edit()
                                .putString(Database.DATABASE_CODE, databaseCode).commit();
                        loadDatabase(true);
                    }
            );
        }
    }

    interface OnDatabaseCodeFinish {
        void runOndatabaseCodeFinish(String databaseCode);
    }

    public void getDatabaseCode(OnDatabaseCodeFinish onFinish) {
        int buttonId = View.generateViewId();
        CustomDialog.Builder(MainActivity.this)
                .setTitle("DatenBank-Code Eingeben")
                .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                .addButton(CustomDialog.OK_BUTTON, dialog ->
                        onFinish.runOndatabaseCodeFinish(CustomDialog.getEditText(dialog)), buttonId)
                .setEdit(new CustomDialog.EditBuilder()
                        .setFireButtonOnOK(true, buttonId))
                .show();

    }

    public void openVideoActivity(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivityForResult(intent, START_VIDEOS);
    }

    public void openActorActivity(View view) {
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Darsteller.name());
        startActivityForResult(intent, START_ACTOR);
    }

    public void openStudioActivity(View view) {
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Studios.name());
        startActivityForResult(intent, START_STUDIO);
    }

    public void openGenreActivity(View view) {
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Genre.name());
        startActivityForResult(intent, START_GENRE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK /*&& requestCode == START_VIDEOS*/) {
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
        if (Utility.isOnline())
            database.writeAllToFirebase();
        super.onDestroy();
    }
}
