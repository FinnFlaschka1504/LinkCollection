package com.maxMustermannGeheim.linkcollection.Activitys.Main;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.maxMustermannGeheim.linkcollection.Activitys.Knowledge.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Videos.CatigorysActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFERENCES_DATA = "LinkCollection_Daten";
    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";
    public static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    public static final String SETTING_LAST_OPEN_CATEGORY = "SETTING_LAST_OPEN_CATEGORY";
    public static final int VIDEO_INT = 1;
    public static final int KNOWLEDGE_INT = 2;


    final int START_VIDEOS = 001;
    final int START_ACTOR = 002;
    final int START_STUDIO = 003;
    final int START_GENRE = 004;
    public final int START_VIDEO_FROM_CALENDER = 005;
    public final int START_WATCH_LATER = 006;
    private final int START_KNOWLEDGE = 007;

    Database database;
    SharedPreferences mySPR_daten;
    SharedPreferences mySPR_settings;
    private Dialog calenderDialog;
    private boolean firstTime;
    private Fragment videoFragment;
    private Fragment knowledgeFragment;

    public enum CATEGORIES {
        Video, Darsteller, Studios, Genre, WatchLater
    }
    private enum  FRAGMENT_TYPE {
        VIDEOS(VIDEO_INT), KNOWLEDGE(KNOWLEDGE_INT);

        private int id;

        FRAGMENT_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
        static FRAGMENT_TYPE getType(int id) {
            for (FRAGMENT_TYPE fragmentType : FRAGMENT_TYPE.values()) {
                if (fragmentType.id == id)
                    return fragmentType;
            }
            return null;
        }
    }
    private FRAGMENT_TYPE currentFragmentType;

    // ToDo: serien (als expandeble Layout)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_main);
        firstTime = savedInstanceState == null;

        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);
        mySPR_settings = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE);

        currentFragmentType = FRAGMENT_TYPE.getType(mySPR_settings.getInt(SETTING_LAST_OPEN_CATEGORY, VIDEO_INT));
        loadDatabase(false);

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, getLocalClassName() + ".Shortcut")
                .setShortLabel("Video Hinzufügen")
                .setLongLabel("Ein neues Video Hinzufügen")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_add_video_shortcut))
                .setIntent(new Intent(this, VideoActivity.class).setAction(VideoActivity.ACTION_ADD_VIDEO))
                .build();
        shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));

        if (Database.exists())
            Database.removeDatabaseReloadListener(null);
        Database.addDatabaseReloadListener(database_neu -> {
            if (firstTime)
                setContentView(R.layout.activity_main);

            if (database_neu.isOnline())
                Toast.makeText(this, "Datenbank wieder verbunden", Toast.LENGTH_SHORT).show();

            database = database_neu;
            setLayout();

        });

    }

    void loadDatabase(boolean createNew) {

        Database.OnInstanceFinishedLoading onInstanceFinishedLoading = database_neu -> {

            if (firstTime) {
                setContentView(R.layout.activity_main);
                Utility.showCenterdToast(this, "Datenbank:\n" + Database.databaseCode);
                setTheme(R.style.AppTheme);
            }

            setLayout();

            database = database_neu;
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

    private void setLayout() {
        Fragment fragment = null;
        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);
        switch (currentFragmentType) {
            case VIDEOS:
                videoFragment = new FragmentVideos();
                fragment = videoFragment;
                bottomNavigationView.setSelectedItemId(R.id.drawer_videos);
                break;
            case KNOWLEDGE:
                knowledgeFragment = new FragmentKnowledge();
                fragment = knowledgeFragment;
                bottomNavigationView.setSelectedItemId(R.id.drawer_knowledge);
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_container, fragment).runOnCommit(this::setCounts).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = menuItem -> {
        Fragment selectedFragment;
        switch (menuItem.getItemId()) {
            case R.id.drawer_videos:
                if (videoFragment == null)
                    videoFragment = new FragmentVideos();
                selectedFragment = videoFragment;
                currentFragmentType = FRAGMENT_TYPE.VIDEOS;
                break;
            case R.id.drawer_knowledge:
                if (knowledgeFragment == null)
                    knowledgeFragment = new FragmentKnowledge();
                selectedFragment = knowledgeFragment;
                currentFragmentType = FRAGMENT_TYPE.KNOWLEDGE;
                break;
            default:
                Toast.makeText(this, "Noch nicht zugewiesen", Toast.LENGTH_SHORT).show();
                return false;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_container, selectedFragment).runOnCommit(this::setCounts).commit();

        return true;
    };

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

//  ----- Video ----->
    public void openVideoActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, VideoActivity.class);
        startActivityForResult(intent, START_VIDEOS);
    }

    public void openActorActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CATEGORIES.Darsteller.name());
        startActivityForResult(intent, START_ACTOR);
    }

    public void openStudioActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CATEGORIES.Studios.name());
        startActivityForResult(intent, START_STUDIO);
    }

    public void openGenreActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CatigorysActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CATEGORIES.Genre.name());
        startActivityForResult(intent, START_GENRE);
    }

    public void showCalenderDialog(View view1) {
        if (!Database.isReady())
            return;

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
        if (!Database.isReady())
            return;
        startActivityForResult(new  Intent(this, VideoActivity.class)
                .putExtra(VideoActivity.EXTRA_SEARCH, VideoActivity.WATCH_LATER_SEARCH)
                .putExtra(VideoActivity.EXTRA_SEARCH_CATIGORY, CATEGORIES.WatchLater.name()), START_WATCH_LATER);
    }
//  <----- Video -----


//  ----- Knowledge ----->
    public void openKnowledgeActivity(View view) {
    if (!Database.isReady())
        return;
    Intent intent = new Intent(this, KnowledgeActivity.class);
    startActivityForResult(intent, START_KNOWLEDGE);
}
//  <----- Knowledge -----


    private void setCounts() {
        switch (currentFragmentType) {
            case VIDEOS:
                ((TextView) findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
                ((TextView) findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
                ((TextView) findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
                ((TextView) findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
                Set<Date> dateSet = new HashSet<>();
                database.videoMap.values().forEach(video -> dateSet.addAll(video.getDateList()));
                ((TextView) findViewById(R.id.main_daysCount)).setText(String.valueOf(dateSet.size()));
                ((TextView) findViewById(R.id.main_watchLaterCount)).setText(String.valueOf(database.watchLaterList.size()));
            break;
            case KNOWLEDGE:
                ((TextView) findViewById(R.id.main_knowledgeCount)).setText(String.valueOf(database.knowledgeMap.size()));
                ((TextView) findViewById(R.id.main_categoryCount)).setText(String.valueOf(database.categoryMap.size()));
                break;
        }
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
        Database.saveAll();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        mySPR_settings.edit().putInt(SETTING_LAST_OPEN_CATEGORY, currentFragmentType.getId()).apply();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START))
//            drawerLayout.closeDrawer(GravityCompat.START);
//        else
            super.onBackPressed();
    }
}
