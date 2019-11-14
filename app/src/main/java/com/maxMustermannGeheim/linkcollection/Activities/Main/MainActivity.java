package com.maxMustermannGeheim.linkcollection.Activities.Main;

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFERENCES_DATA = "LinkCollection_Daten";
    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";
    public static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    public static final String SETTING_LAST_OPEN_SPACE = "SETTING_LAST_OPEN_SPACE";
    public static final String ACTION_ADD = "ACTION_ADD";


    public static final int START_VIDEOS = 1;
    public static final int START_ACTOR = 2;
    public static final int START_STUDIO = 3;
    public static final int START_GENRE = 4;
    public static final int START_VIDEO_FROM_CALENDER = 5;
    public static final int START_WATCH_LATER = 6;
    public static final int START_UPCOMING = 7;
    public static final int START_SETTINGS = 8;
    public static final int START_KNOWLEDGE = 9;
    public static final int START_KNOWLEDGE_CATEGORY = 10;
    public static final int START_OWE = 11;
    public static final int START_PERSON = 12;
    public static final int START_JOKE = 13;
    public static final int START_JOKE_CATEGORY = 14;

    Database database;
    SharedPreferences mySPR_daten;
    SharedPreferences mySPR_settings;
    private Dialog calenderDialog;
    private boolean firstTime;

    private static Settings.Space currentSpace;

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

        loadDatabase(false);


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
            }

            setLayout();

            database = database_neu;
        };

        if (Database.getInstance(mySPR_daten, onInstanceFinishedLoading, createNew) == null) {
            getDatabaseCode(databaseCode -> {
                        mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                        loadDatabase(true);
                    }
            );
        }
    }

    private void setLayout() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);

        Settings.startSettings_ifNeeded(this);

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
        int count = 1;
        for (Settings.Space space : Settings.Space.allSpaces) {
            if (count > 5) break;
            shortcutInfoList.add(new ShortcutInfo.Builder(this, space.getName() + ".Shortcut")
                            .setShortLabel(space.getName() + " Hinzufügen")
//                    .setLongLabel("Ein neues VIDEO Hinzufügen")
                            .setIcon(Icon.createWithResource(this, space.getIconId()))
                            .setIntent(new Intent(this, space.getActivity()).setAction(ACTION_ADD))
                            .build()
            );

            count++;
        }
        Collections.reverse(shortcutInfoList);
        shortcutManager.setDynamicShortcuts(shortcutInfoList);

        bottomNavigationView.getMenu().clear();
        count = 0;
        for (Settings.Space space : Settings.Space.allSpaces) {
            if (!space.isShown()) continue;

            if (count == 4)
                break;
            bottomNavigationView.getMenu().add(Menu.NONE, space.getItemId(), Menu.NONE, space.getPlural()).setIcon(space.getIconId());
            count++;
        }

        currentSpace = Settings.Space.getSpaceById(mySPR_settings.getInt(SETTING_LAST_OPEN_SPACE, Settings.Space.SPACE_VIDEO));

        if (!currentSpace.isShown())
            currentSpace = Settings.Space.getFirstShown();

        bottomNavigationView.setVisibility(bottomNavigationView.getMenu().size() == 1 ? View.GONE : View.VISIBLE);

        if (!currentSpace.hasFragment())
            currentSpace.setFragment(new SpaceFragment(currentSpace.getLayoutId()));
        SpaceFragment.currentSpace = currentSpace;
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(currentSpace.getItemId());
    }

    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = menuItem -> {

        Settings.Space selectedSpace = Settings.Space.getSpaceById(menuItem.getItemId());
        if (selectedSpace == null) {
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!selectedSpace.isShown())
            selectedSpace = Settings.Space.getFirstShown();

        if (!selectedSpace.hasFragment())
            selectedSpace.setFragment(new SpaceFragment(selectedSpace.getLayoutId()));
        SpaceFragment.currentSpace = selectedSpace;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_container, selectedSpace.getFragment()).runOnCommit(MainActivity::setCounts).commitAllowingStateLoss();
        currentSpace = selectedSpace;

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
                .addButton(CustomDialog.OK_BUTTON, (customDialog, dialog) ->
                        onFinish.runOndatabaseCodeFinish(CustomDialog.getEditText(dialog)), buttonId)
                .setEdit(new CustomDialog.EditBuilder()
                        .setFireButtonOnOK(buttonId))
                .show();

    }

//  ----- VIDEO ----->
    public void openVideoActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, VideoActivity.class);
        startActivityForResult(intent, START_VIDEOS);
    }

    public void openActorActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.DARSTELLER);
        startActivityForResult(intent, START_ACTOR);
    }

    public void openStudioActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.STUDIOS);
        startActivityForResult(intent, START_STUDIO);
    }

    public void openGenreActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.GENRE);
        startActivityForResult(intent, START_GENRE);
    }

    public void showCalenderDialog(View view1) {
        if (!Database.isReady())
            return;

        calenderDialog = CustomDialog.Builder(this)
                .setTitle("Video Kalender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupCalender(this, calendarView, ((FrameLayout) view), new ArrayList<>(database.videoMap.values()), true);
//                    ViewCompat.setNestedScrollingEnabled(view.findViewById(R.id.fragmentCalender_videoList), false);

                })
                .disableScroll()
                .setDimensions(true, true)
                .show();
    }

    public void showLaterMenu(View view) {
        VideoActivity.showLaterMenu(this, view);
    }
//  <----- VIDEO -----


//  ----- Knowledge ----->
    public void openKnowledgeActivity(View view) {
    if (!Database.isReady())
        return;
    Intent intent = new Intent(this, KnowledgeActivity.class);
    startActivityForResult(intent, START_KNOWLEDGE);
}

    public void openKnowledgeCategoryActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES);
        startActivityForResult(intent, START_KNOWLEDGE_CATEGORY);
    }
//  <----- Knowledge -----


//  ----- Owe ----->
    public void openOweActivity(View view) {
    if (!Database.isReady())
        return;
    Intent intent = new Intent(this, OweActivity.class);
    startActivityForResult(intent, START_OWE);
}

    public void openPersonActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.PERSON);
        startActivityForResult(intent, START_PERSON);
    }

    public void showPopupwindow(View view) {
        OweActivity.showPopupwindow(this, view);
    }

    public void showTradeOffDialog(View view) {
        OweActivity.showTradeOffDialog(this, view);
    }
//  <----- Owe -----


//  ----- Joke ----->
    public void openJokeActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, JokeActivity.class);
        startActivityForResult(intent, START_JOKE);
    }

    public void openJokeCategoryActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.JOKE_CATEGORIES);
        startActivityForResult(intent, START_JOKE_CATEGORY);
    }
//  <----- Joke -----

    public static void setCounts() {
        if (currentSpace != null)
            currentSpace.setLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_main_settings:
                startActivityForResult(new Intent(this, Settings.class), START_SETTINGS);
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
                setCounts();
            }
            else if (requestCode == START_SETTINGS) {
                setLayout();
            }
            else
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
        if (currentSpace != null)
            mySPR_settings.edit().putInt(SETTING_LAST_OPEN_SPACE, currentSpace.getItemId()).apply();
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
