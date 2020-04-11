package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.Helpers;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.CollectionActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomInternetHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.SquareLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.maxMustermannGeheim.linkcollection.Utilities.VersionControl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

// --> \/\/(?!  (-|<))
public class MainActivity extends AppCompatActivity implements CustomInternetHelper.InternetStateReceiverListener {
    public static final String SHARED_PREFERENCES_DATA = "LinkCollection_Daten";
    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";
    public static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    public static final String SETTING_LAST_OPEN_SPACE = "SETTING_LAST_OPEN_SPACE";
    public static final String ACTION_SHORTCUT = "ACTION_SHORTCUT";
    public static final String ACTION_SHOW_AS_DIALOG = "ACTION_SHOW_AS_DIALOG";
    public static final String EXTRA_SHOW_RANDOM = "EXTRA_SHOW_RANDOM";


    private static int count; // 19
    public static final int START_VIDEOS = ++count;
    public static final int START_ACTOR = ++count;
    public static final int START_STUDIO = ++count;
    public static final int START_GENRE = ++count;
    public static final int START_VIDEO_FROM_CALENDER = ++count;
    public static final int START_SEEN = ++count;
    public static final int START_WATCH_LATER = ++count;
    public static final int START_UPCOMING = ++count;
    public static final int START_CATEGORIES = ++count;
    public static final int START_SETTINGS = ++count;
    public static final int START_KNOWLEDGE = ++count;
    public static final int START_KNOWLEDGE_CATEGORY = ++count;
    public static final int START_OWE = ++count;
    public static final int START_PERSON = ++count;
    public static final int START_JOKE = ++count;
    public static final int START_JOKE_CATEGORY = ++count;
    public static final int START_SHOW = ++count;
    public static final int START_SHOW_CATEGORY = ++count;
    public static final int START_SHOW_GENRE = ++count;
    public static final int START_SHOW_FROM_CALENDER = ++count;
    public static final int START_SHOW_NEXT_EPISODE = ++count;

    private static Database database;
    private static SharedPreferences mySPR_daten;
    private SharedPreferences mySPR_settings;
    private com.finn.androidUtilities.CustomDialog calenderDialog;
    public static boolean isLoadingLayout;

    private static Settings.Space currentSpace;
    private View main_offline;
    private Utility.OnHorizontalSwipeTouchListener touchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoadingLayout = savedInstanceState == null;

        if (isLoadingLayout)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_main);

        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);
        mySPR_settings = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE);

        Settings.startSettings_ifNeeded(this);

        if (Database.exists())
            Database.removeDatabaseReloadListener(null);
        Database.addDatabaseReloadListener(database_neu -> {
            changeLayout_ifNecessary(this);

            if (database_neu.isOnline())
                Toast.makeText(this, "Datenbank wieder verbunden", Toast.LENGTH_SHORT).show();

            database = database_neu;
            setLayout();
        });

        CustomInternetHelper.initialize(this);

//        List<String> imageUrlsFromHtml = Utility.getImageUrlsFromText(WebisteHtml.websiteHtml);
        String BREAKPOINT = null;

//        CustomUtility.PingTask.simulate(false, 3000);

//        Interpreter interpreter = new Interpreter();
//        String java = "if (url.contains(\"moviesjoy\")) " +
//                "{" +
//                "String last = customList1.add(url.split(\"/\")).getLast(); " +
//                "if (last != null) " +
//                "{" +
//                "customList2.add(last.split(\"-\")); " +
//                "customList2.removeLast(); " +
//                "String result = String.join(\" \", customList2);" +
//                "return result;" +
////                        "video.setName(String.join(\" \", customList2));" +
//                "}" +
////                        "return video; " +
//                "}" +
//                "else {return \"--Leer--\";}";


//        Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "DialogTest")
//                .setShortLabel("Dialog Test")
//                .setIcon(Icon.createWithResource(this, R.drawable.ic_random))
//                .setIntent(new Intent(this, DialogActivity.class).setAction(DialogActivity.ACTION_RANDOM))
//                .build(), null));
//        try {
//            interpreter.set("video", new Video());
//            interpreter.set("url", "https://www1.moviesjoy.net/watch-movie/ad-astra-41379.989936");
//            interpreter.set("customList1", new CustomList<String>());
//            interpreter.set("customList2", new CustomList<String>());
//            interpreter.set("customList3", new CustomList<String>());
//            Object result = interpreter.eval(java);
//            String BREAKPOINT = null;
//        } catch (EvalError evalError) {
//            evalError.printStackTrace();
//        }


        // ------

//        com.finn.androidUtilities.CustomDialog.Builder(this)
//                .setTitle("Rating-Test")
//                .setView(R.layout.custom_rating)
//                .setSetViewContent((customDialog, view, reload) -> {
//                    new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout));
//                })
//                .disableScroll()
//                .show();

//        new CustomRecycler<String>(this).isRecyclerScrollable();

//        int buttonId = View.generateViewId();
//        CustomDialog.Builder(this)
//                .setTitle("Test")
////                .setView(new Button(this))
////                .setEdit(new CustomDialog.EditBuilder().disableSelectAll().setName("Text").disableButtonByDefault().setHint("Test").setInputType(Helpers.TextInputHelper.INPUT_TYPE.E_MAIL)
//////                        .allowEmpty()
//////                        .setValidation((validator, text) -> {
//////                            if (text.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"))
//////                                validator.setValid();
//////
//////                        })
////                )
//                .standardEdit()
////                .addButton("Zu viel")
////                .addButton("Langer Text")
//                .addButton("MöP", null, buttonId)
//                .disableLastAddedButton()
//                .alignPreviousButtonsLeft()
//                .hideLastAddedButton()
//                .addButton("Mehr!", customDialog -> customDialog.findViewById(buttonId).setVisibility(View.VISIBLE), false)
//                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
//                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> Toast.makeText(this, customDialog.getEditText(), Toast.LENGTH_SHORT).show())
////                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
////                .disableButtonAllCaps()
//                .show();
//        ImageView imageView = new ImageView(this);
//        imageView.setImageResource(R.drawable.simpsons_movie_poster);
////        Glide
////                .with(this)
////                .load("http://image.tmdb.org/t/p/w92//dKAx5Vt9V8Qfbm3aIaLcAjUxIS4.jpg")
////                .listener(new RequestListener<Drawable>() {
////                    @Override
////                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
////                        return false;
////                    }
////
////                    @Override
////                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                        return false;
////                    }
////                })
////                .error(R.drawable.simpsons_movie_poster)
////                .into(imageView);
//        Picasso
//                .get()
////                .load("https://images-na.ssl-images-amazon.com/images/I/511Ex1uIeZL.jpg")
//                .load("https://image.tmdb.org/t/p/w92//dKAx5Vt9V8Qfbm3aIaLcAjUxIS4.jpg")
//                .error(R.drawable.ic_show_as_grid)
//                .placeholder(R.drawable.simpsons_movie_poster)
//
//                .into(imageView);
//        com.finn.androidUtilities.CustomDialog.Builder(this)
//                .setTitle("Bild-Test")
//                .setView(imageView)
//                .setDimensions(true, true)
//                .show();
    }


    void loadDatabase(boolean createNew) {

        Database.OnInstanceFinishedLoading onInstanceFinishedLoading = database_neu -> {
            VersionControl.showChangeLog(this, false);
            VersionControl.checkForUpdate(this, false);

            if (changeLayout_ifNecessary(this))
                Utility.showCenteredToast(this, "Datenbank:\n" + Database.databaseCode);


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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
        int count = 1;
        for (Settings.Space space : Settings.Space.allSpaces) {
            if (count > 5) break;
            shortcutInfoList.add(new ShortcutInfo.Builder(this, space.getName() + ".Shortcut")
                    .setShortLabel(space.getName() + " Hinzufügen")
                    .setIcon(Icon.createWithResource(this, space.getIconId()))
                    .setIntent(new Intent(this, space.getActivity()).setAction(ACTION_SHORTCUT))
                    .build()
            );

            count++;
        }
//        Collections.reverse(shortcutInfoList);
        shortcutManager.setDynamicShortcuts(shortcutInfoList);

        bottomNavigationView.getMenu().clear();
        count = 0;
        for (Settings.Space space : Settings.Space.allSpaces) {
            if (!space.isShown()) continue;

            if (count == 4 && Settings.Space.allSpaces.size() > 5)
                break;
            bottomNavigationView.getMenu().add(Menu.NONE, space.getItemId(), Menu.NONE, space.getPlural()).setIcon(space.getIconId());
            count++;
        }

        currentSpace = Settings.Space.getSpaceById(mySPR_settings.getInt(SETTING_LAST_OPEN_SPACE, Settings.Space.SPACE_VIDEO));

        if (!currentSpace.isShown())
            currentSpace = Settings.Space.getFirstShown();

        bottomNavigationView.setVisibility(bottomNavigationView.getMenu().size() == 1 ? View.GONE : View.VISIBLE);

        if (!currentSpace.hasFragment())
            currentSpace.setFragment(new SpaceFragment(currentSpace.getFragmentLayoutId()));
        SpaceFragment.currentSpace = currentSpace;
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(currentSpace.getItemId());

        main_offline = findViewById(R.id.main_offline);
        main_offline.setOnClickListener(v -> CustomInternetHelper.showActivateInternetDialog(this));
        main_offline.setVisibility(Utility.isOnline() ? View.GONE : View.VISIBLE);
    }

    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = menuItem -> {
        Settings.Space selectedSpace = Settings.Space.getSpaceById(menuItem.getItemId());
        if (selectedSpace == null) {
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!selectedSpace.isShown())
            selectedSpace = Settings.Space.getFirstShown();

//        if (true || !selectedSpace.hasFragment() || selectedSpace != currentSpace || ((FrameLayout) findViewById(R.id.main_frame_container)).getChildCount() == 0) {
            if (!selectedSpace.hasFragment())
                selectedSpace.setFragment(new SpaceFragment(selectedSpace.getFragmentLayoutId()));
            SpaceFragment.currentSpace = selectedSpace;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_container, selectedSpace.getFragment()).runOnCommit(() -> {
                setCounts(this);
                if (currentSpace != null)
                    mySPR_settings.edit().putInt(SETTING_LAST_OPEN_SPACE, currentSpace.getItemId()).apply();
                if (Settings.Space.allSpaces.filter(Settings.Space::isShown, false).size() > 1) {
                    ViewGroup view = (ViewGroup) currentSpace.getFragment().getView();
                    touchListener = new Utility.OnHorizontalSwipeTouchListener(MainActivity.this) {
                        @Override
                        public void onSwipeRight() {
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(Settings.Space.allSpaces.filter(Settings.Space::isShown, false).previous(currentSpace).getItemId());
                        }

                        @Override
                        public void onSwipeLeft() {
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(Settings.Space.allSpaces.filter(Settings.Space::isShown, false).next(currentSpace).getItemId());
                        }
                    };
                    findViewById(R.id.scrollView).setOnTouchListener(touchListener);
                    view.setOnTouchListener(touchListener);
                    Utility.applyToAllViews(view, SquareLayout.class, squareLayout -> squareLayout.setOnTouchListener(touchListener));
                }
            }).commitAllowingStateLoss();
            currentSpace = selectedSpace;
//        } else {
//            SpaceFragment.currentSpace = selectedSpace;
//            setCounts(this);
//        }
        return true;
    };

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (touchListener != null && touchListener.onTouch(null, ev)) {
////            return true;
//            String BREAKPOINT = null;
//        }
//        return super.dispatchTouchEvent(ev);
//    }


    interface OnDatabaseCodeFinish {
        void runOndatabaseCodeFinish(String databaseCode);
    }

    public void getDatabaseCode(OnDatabaseCodeFinish onFinish) {
        CustomDialog.Builder(MainActivity.this)
                .setTitle("Anmelden")
                .setView(R.layout.dialog_database_login)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout dialog_databaseLogin_name_layout = customDialog.findViewById(R.id.dialog_databaseLogin_name_layout);
                    TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);
                    TextInputLayout dialog_databaseLogin_passwordSecond_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordSecond_layout);

                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
                    helper.addValidator(dialog_databaseLogin_name_layout, dialog_databaseLogin_passwordFirst_layout, dialog_databaseLogin_passwordSecond_layout)
                            .defaultDialogValidation(customDialog)
                            .setValidation(dialog_databaseLogin_passwordSecond_layout, (validator, text) -> {
                                if (Utility.stringExists(text) && !text.equals(dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim()))
                                    validator.setInvalid("Die Passwörter müssen gleich sein");
                            })
                            .addActionListener(dialog_databaseLogin_passwordSecond_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                View button = customDialog.getActionButton().getButton();
                                if (button.isEnabled())
                                    button.callOnClick();
                            }, Helpers.TextInputHelper.IME_ACTION.DONE);

                    dialog_databaseLogin_name_layout.requestFocus();
                    Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
//                    customDialog.getDialog().setCanceledOnTouchOutside(false);
                })
                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                    TextInputLayout dialog_databaseLogin_name_layout = customDialog.findViewById(R.id.dialog_databaseLogin_name_layout);
                    TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);

                    String databaseCode = dialog_databaseLogin_name_layout.getEditText().getText().toString().trim();
                    String password = dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim();

                    if (!Utility.stringExists(databaseCode))
                        return;

                    Database.databaseCall_read(dataSnapshot -> {
                        if (dataSnapshot.getValue() == null) {
                            CustomDialog.Builder(this)
                                    .setTitle("Batenbank Noch Nicht Vorhanden")
                                    .setText(new Helpers.SpannableStringHelper().append("Die Datenbank '").appendBold(databaseCode).append("' existiert noch nicht.\nMochtest du sie hinzufügen?").get())
                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                        Settings.resetEncryption();
                                        customDialog.dismiss();
                                        onFinish.runOndatabaseCodeFinish(databaseCode);
                                        Database.databaseCall_write(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString(), Database.databaseCode, Database.PASSWORD);
                                    })
                                    .show();
                        } else if (Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString().equals(dataSnapshot.getValue())) {
                            List<String> encryptedSpaces = new ArrayList<>();
                            Runnable setEncryptedSpaces = () -> {
                                Settings.Space.allSpaces.forEach(space -> space.setEncrypted(encryptedSpaces.contains(space.getKey())));
                                Settings.saveEncryption();

                            };


                            Database.databaseCall_read(dataSnapshot1 -> {
                                Object value = dataSnapshot1.getValue();
                                if (value == null) {
                                    setEncryptedSpaces.run();
                                    Settings.resetEncryption();
                                    onFinish.runOndatabaseCodeFinish(databaseCode);
                                    customDialog.dismiss();
                                } else if (Utility.hash(Settings.getSingleSetting(this, Settings.SETTING_SPACE_ENCRYPTION_PASSWORD)).equals(((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTION_PASSWORD))) {
                                    encryptedSpaces.addAll((Collection<? extends String>) ((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTED_SPACES));
                                    setEncryptedSpaces.run();

                                    onFinish.runOndatabaseCodeFinish(databaseCode);
                                    customDialog.dismiss();
                                } else {
                                    CustomDialog.Builder(this)
                                            .setTitle("Passwort Eingeben")
                                            .setText("Die Datenbank enthält verschlüsselte Bereiche und das hinterlegte Passwort sitmmt nicht.\nBitte das richtige Passwort eingeben.")
                                            .setEdit(new CustomDialog.EditBuilder().setInputType(Helpers.TextInputHelper.INPUT_TYPE.PASSWORD))
                                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                                String inputPassword = customDialog1.getEditText();
                                                if (Utility.hash(inputPassword).equals(((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTION_PASSWORD))) {
                                                    Settings.changeSetting(Settings.SETTING_SPACE_ENCRYPTION_PASSWORD, inputPassword);

                                                    encryptedSpaces.addAll((Collection<? extends String>) ((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTED_SPACES));
                                                    setEncryptedSpaces.run();

                                                    onFinish.runOndatabaseCodeFinish(databaseCode);
                                                    customDialog.dismiss();
                                                    customDialog1.dismiss();
                                                } else
                                                    Toast.makeText(this, "Das Passwort ist Falsch", Toast.LENGTH_SHORT).show();
                                            }, false)
                                            .show();
                                }
                            }, databaseCode, Database.ENCRYPTION);
                        } else
                            Toast.makeText(this, "Das Passwort ist falsch", Toast.LENGTH_SHORT).show();
                    }, databaseError -> {
                        Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                    }, databaseCode, Database.PASSWORD);

                }, false)
                .disableLastAddedButton()
                .enablePermanentDialog()
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

    public void showFilmCalenderDialog(View view1) {
        if (!Database.isReady())
            return;

        calenderDialog = com.finn.androidUtilities.CustomDialog.Builder(this)
                .setTitle(currentSpace.getName() + " Kalender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupFilmCalender(this, calendarView, ((FrameLayout) view), new ArrayList<>(database.videoMap.values()), true);
//                    ViewCompat.setNestedScrollingEnabled(view.findViewById(R.id.fragmentCalender_videoList), false);

                })
                .disableScroll()
                .setDimensions(true, true)
                .enableTitleBackButton()
                .show();
    }

    public void showLaterMenu(View view) {
        VideoActivity.showModeMenu(this, view);
    }

    public void openCollectionActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CollectionActivity.class);
        startActivityForResult(intent, START_CATEGORIES);
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


    //  ----- Shows ----->
    public void openShowActivity(View view) {
        if (!Database.isReady())
            return;
        startActivityForResult(new Intent(this, ShowActivity.class), START_SHOW);
    }

    public void openShowGenreActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.SHOW_GENRES);
        startActivityForResult(intent, START_SHOW_GENRE);
    }

    public void showShowCalenderDialog(View view1) {
        if (!Database.isReady())
            return;

        calenderDialog = com.finn.androidUtilities.CustomDialog.Builder(this)
                .setTitle(currentSpace.getName() + " Kalender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    List<Show.Episode> episodeList = new ArrayList<>();
                    for (Show show : database.showMap.values()) {
                        for (Show.Season season : show.getSeasonList()) {
                            episodeList.addAll(season.getEpisodeMap().values());
                        }
                    }
                    Utility.setupEpisodeCalender(this, calendarView, ((FrameLayout) view), episodeList, true);
//                    ViewCompat.setNestedScrollingEnabled(view.findViewById(R.id.fragmentCalender_videoList), false);

                })
                .disableScroll()
                .enableTitleBackButton()
                .setDimensions(true, true)
                .show();
    }

    public void showLaterMenu_show(View view) {
        ShowActivity.showLaterMenu(this, view);
    }

    public void showNextEpisode(View view) {
        ShowActivity.showNextEpisode(this, view, false);
    }

    public static boolean showNextEpisode_longClick(View view) {
        ShowActivity.showNextEpisode((AppCompatActivity) view.getContext(), view, true);
        return true;
    }

    public void showNewEpisodesDialog(View view) {
        if (!Database.isReady())
            return;

        ShowActivity.showNewEpisodesDialog(this);
    }
    //  <----- Shows -----


    public static void setCounts(MainActivity activity) {
        if (currentSpace != null) {
            if (Settings.database != null) {
                if (!isLayoutSet(activity))
                    activity.setContentView(R.layout.activity_main); // nein
                currentSpace.setLayout();
            } else if (database != null) {
                if (!isLayoutSet(activity))
                    activity.setContentView(R.layout.activity_main); // nein

                Settings.database = database;
                currentSpace.setLayout();
            } else {
                if (activity != null) {
                    activity.setContentView(R.layout.loading_screen);
                    isLoadingLayout = true;
                }
                Database.getInstance(mySPR_daten, database1 -> {
                    if (activity != null) {
                        activity.setContentView(R.layout.activity_main); // nein
                        isLoadingLayout = false;
                    }

                    Settings.database = database1;
                    database = database1;

                    currentSpace.setLayout();
                });
            }
        }
    }

    private static boolean isLayoutSet(MainActivity activity) {
        return activity != null && activity.findViewById(R.id.main_frame_container) != null;
    }

    private static boolean changeLayout_ifNecessary(MainActivity activity) {
        if (isLoadingLayout || !isLayoutSet(activity)) {
            activity.setContentView(R.layout.activity_main);
            isLoadingLayout = false;
            return true;
        }
        return false;
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
                showFilmCalenderDialog(null);
                changeLayout_ifNecessary(this);
                setCounts(this);
            } else if (requestCode == START_SETTINGS) {
                changeLayout_ifNecessary(this);
                setLayout();
            } else {
                changeLayout_ifNecessary(this);
                setCounts(this);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        if (main_offline != null) {
            if (main_offline.getVisibility() == View.VISIBLE) {
                main_offline.setVisibility(View.GONE);
                loadDatabase(false);
            }
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        Database.saveAll();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        if (currentSpace != null)
//            mySPR_settings.edit().putInt(SETTING_LAST_OPEN_SPACE, currentSpace.getItemId()).apply();
        CustomInternetHelper.destroyInstance(this);
        super.onDestroy();
    }

    @Override
    public void networkAvailable() {
        loadDatabase(false);
        if (main_offline != null)
            main_offline.setVisibility(View.GONE);
    }

    @Override
    public void networkUnavailable() {
        loadDatabase(false);
        if (main_offline != null)
            main_offline.setVisibility(View.VISIBLE);
    }

}
