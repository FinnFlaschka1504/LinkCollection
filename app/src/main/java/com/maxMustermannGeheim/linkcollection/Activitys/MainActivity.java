package com.maxMustermannGeheim.linkcollection.Activitys;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Database database;
    SharedPreferences mySPR_daten;
    public static final String SHARED_PREFERENCES_NAME = "LinkCollection_Daten";
    private Dialog calenderDialog;
    private boolean firstTime;

    public enum CATIGORYS{
        Video, Darsteller, Studios, Genre, WatchLater

    }

    public static final String EXTRA_CATIGORY = "EXTRA_CATIGORY";
    final int START_VIDEOS = 001;
    final int START_ACTOR = 002;
    final int START_STUDIO = 003;
    final int START_GENRE = 004;
    public final int START_VIDEO_FROM_CALENDER = 005;
    public final int START_WATCH_LATER = 006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_main);
        firstTime = savedInstanceState == null;

        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
//        mySPR_daten.edit().clear().commit();
        loadDatabase(false);
    }

    void loadDatabase(boolean createNew) {
        Database.OnInstanceFinishedLoading onInstanceFinishedLoading = database_neu -> {

            if (firstTime) {
                setContentView(R.layout.activity_main);
                Utility.showCenterdToast(this, "Datenbank:\n" + Database.databaseCode);
            }

            if (false/*database_neu.isLoaded()*/)
                database_neu.generateData();
            database = database_neu;
            setCounts();
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
        if (!database.isLoaded())
            return;
        Intent intent = new Intent(this, VideoActivity.class);
        startActivityForResult(intent, START_VIDEOS);
    }

    public void openActorActivity(View view) {
        if (!database.isLoaded())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Darsteller.name());
        startActivityForResult(intent, START_ACTOR);
    }

    public void openStudioActivity(View view) {
        if (!database.isLoaded())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Studios.name());
        startActivityForResult(intent, START_STUDIO);
    }

    public void openGenreActivity(View view) {
        if (!database.isLoaded())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATIGORY, CATIGORYS.Genre.name());
        startActivityForResult(intent, START_GENRE);
    }

    public void showCalenderDialog(View view1) {
        calenderDialog = CustomDialog.Builder(this)
                .setTitle("Video Calender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent(view -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupCalender(this, calendarView, ((LinearLayout) view), new ArrayList<>(database.videoMap.values()), true);
                })
                .show();
    }

    public void showWatchLater(View view) {
        if (!database.isLoaded())
            return;
        startActivityForResult(new  Intent(this, VideoActivity.class)
                .putExtra(VideoActivity.EXTRA_SEARCH, VideoActivity.WATCH_LATER_SEARCH)
                .putExtra(VideoActivity.EXTRA_SEARCH_CATIGORY, CATIGORYS.WatchLater.name()), START_WATCH_LATER);
    }


    private void setCounts() {
        ((TextView) findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
        ((TextView) findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
        ((TextView) findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
        ((TextView) findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
        Set<Date> dateSet = new HashSet<>();
        database.videoMap.values().forEach(video -> dateSet.addAll(video.getDateList()));
        ((TextView) findViewById(R.id.main_daysCount)).setText(String.valueOf(dateSet.size()));
        ((TextView) findViewById(R.id.main_watchLaterCount)).setText(String.valueOf(database.watchLaterList.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_main_settings:
                CustomDialog.Builder(this)
                        .setTitle("Einstellungen")
                        .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                        .setEdit(new CustomDialog.EditBuilder()
                                .setHint("Datenbank-Code")
                                .setText(Database.databaseCode))
                        .show();
                // ToDo: datenbank-code ändern
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK /*&& requestCode == START_VIDEOS*/) {
            if (requestCode == START_VIDEO_FROM_CALENDER) {
                calenderDialog.dismiss();
                showCalenderDialog(null);
            }
            setCounts();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        Utility.saveAll(mySPR_daten, database);
        super.onStop();
    }
}
