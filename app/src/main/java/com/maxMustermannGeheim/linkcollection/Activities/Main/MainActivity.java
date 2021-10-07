package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.MediaActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.MediaEventActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.CollectionActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomInternetHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.SquareLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.maxMustermannGeheim.linkcollection.Utilities.VersionControl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    public static final int START_MEDIA = ++count;
    public static final int START_MEDIA_PERSON = ++count;
    public static final int START_MEDIA_CATEGORY = ++count;
    public static final int START_MEDIA_TAG = ++count;
    public static final int START_MEDIA_EVENT = ++count;

    private static Database database;
    private static SharedPreferences mySPR_daten;
    private SharedPreferences mySPR_settings;
    private com.finn.androidUtilities.CustomDialog calenderDialog;
    public static boolean isLoadingLayout;

    private static Settings.Space currentSpace;
    private View main_offline;
    private Utility.OnSwipeTouchListener touchListener;

//    public static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        activity = this;
        super.onCreate(savedInstanceState);
//        isLoadingLayout = savedInstanceState == null;

        if (isLoadingLayout = !Database.isReady()) //(isLoadingLayout || !Database.isReady()) && !Database.isReady())
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

//        ImageView imageView = new ImageView(this);
//        CustomDialog.Builder(this)
//                .setTitle("Local Image Test")
//                .setView(imageView)
//                .setDimensionsFullscreen()
//                .setSetViewContent((customDialog, view, reload) -> {
//                    ActivityResultListener.addFileChooserRequest(this, "image/*", o -> {
//                        Intent data = (Intent) o;
//                        Uri uri = data.getData();
////                        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
////                        try {
////                            getContentResolver().takePersistableUriPermission(uri, flags);
////                        }
////                        catch (SecurityException e){
////                            e.printStackTrace();
////                        }
//                        imageView.setImageURI(Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2FIMG_9739.JPG"));
//                    });
//
//                })
//                .show();

//        Test test = Test.create();
//        Test test2 = Test.create();
//
//        test.setTest(0);
//        test2.setTest(1);
//
//        test.getTest();
//        test2.getTest();
//
//        test.setTest(2);
//        test2.setTest(3);
//
//        test.getTest();
//        test2.getTest();

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

        if (Database.isReady()) {
            onInstanceFinishedLoading.runOnInstanceFinishedLoading(Database.getInstance());
        } else if (Database.getInstance(mySPR_daten, onInstanceFinishedLoading, createNew) == null) {
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

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        Utility.applyExpendableToolbar_scrollView(this, findViewById(R.id.scrollView), appBarLayout);

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
            count++;
            if (!space.isShown()) continue;

            if (count >= 5 && Settings.Space.allSpaces.filter(Settings.Space::isShown, false).size() > 5) {
                bottomNavigationView.getMenu().add(Menu.NONE, Settings.Space.SPACE_MORE, Menu.NONE, "Mehr").setIcon(R.drawable.ic_more_horiz);
                break;
            }
            bottomNavigationView.getMenu().add(Menu.NONE, space.getItemId(), Menu.NONE, space.getPlural()).setIcon(space.getIconId());
        }

        currentSpace = Settings.Space.getSpaceById(mySPR_settings.getInt(SETTING_LAST_OPEN_SPACE, Settings.Space.SPACE_VIDEO));

        if (!currentSpace.isShown())
            currentSpace = Settings.Space.getFirstShown();

        bottomNavigationView.setVisibility(bottomNavigationView.getMenu().size() == 1 ? View.GONE : View.VISIBLE);

        if (!currentSpace.hasFragment())
            currentSpace.setFragment(new SpaceFragment(currentSpace.getFragmentLayoutId()));
        SpaceFragment.currentSpace = currentSpace;
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        if (currentSpace.isInMore()) {
            Settings.Space.nextMoreSpace = currentSpace;
            bottomNavigationView.setSelectedItemId(Settings.Space.SPACE_MORE);
        } else
            bottomNavigationView.setSelectedItemId(currentSpace.getItemId());

        main_offline = findViewById(R.id.main_offline);
        main_offline.setOnClickListener(v -> CustomInternetHelper.showActivateInternetDialog(this));
        main_offline.setVisibility(Utility.isOnline() ? View.GONE : View.VISIBLE);
    }

    private void applyDimensionsLayout() {
        ViewGroup fragmentView = (ViewGroup) currentSpace.getFragment().getView();
        if (fragmentView == null)
            return;
        CustomList<SquareLayout> buttonList = new CustomList<>();
        LinearLayout container = (LinearLayout) fragmentView.getChildAt(0);
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout subContainer = (LinearLayout) container.getChildAt(i);
            for (int i1 = 0; i1 < subContainer.getChildCount(); i1++) {
                SquareLayout button = (SquareLayout) subContainer.getChildAt(i1);
                if (button.getVisibility() == View.VISIBLE)
                    buttonList.add(button);
            }
            subContainer.removeAllViews();
        }
        container.removeAllViews();
        buttonList.filter(squareLayout -> squareLayout.getChildCount() > 0, true);
//        View frameContainer = findViewById(R.id.main_frame_container);
//        frameContainer.measure(0, 0);
//        int width = frameContainer.getWidth();
        int width = Utility.getScreenAvailableWidth(this);


        int maxButtonWidth = CustomUtility.dpToPx(180);
        int columnCount = (int) Math.round(width / (double) maxButtonWidth);
        int rowCount = (int) Math.ceil(buttonList.size() / (double) columnCount);

//        CustomUtility.logD("GenericTag", "applyDimensionsLayout: %d | %d | %d | %d", buttonList.size(), width, rowCount, columnCount);

        for (int ri = 0; ri < rowCount; ri++) {
            LinearLayout horizontalLayout = new LinearLayout(this);
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int ci = 0; ci < columnCount; ci++) {
                SquareLayout button = buttonList.removeFirst();
                if (button != null)
                    horizontalLayout.addView(button);
                else {
                    SquareLayout filler = new SquareLayout(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    filler.setLayoutParams(layoutParams);
                    horizontalLayout.addView(filler);
                }
            }
            container.addView(horizontalLayout);
        }
    }

    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = menuItem -> {
        Settings.Space selectedSpace = Settings.Space.getSpaceById(menuItem.getItemId());
        if (selectedSpace == null && Settings.Space.nextMoreSpace == null) {
            if (menuItem.getItemId() == Settings.Space.SPACE_MORE) {
                CustomMenu customMenu = CustomMenu.Builder(this)
                        .setMenus((customMenu1, items) -> {
                            Settings.Space.allSpaces.filter(Settings.Space::isInMore, false).forEach(space -> {
                                items.add(new CustomMenu.MenuItem(space.getPlural(), space, space.getIconId()));
                            });

                        })
                        .setOnClickListener((customRecycler, itemView, item, index) -> {
                            Settings.Space.nextMoreSpace = (Settings.Space) item.getContent();
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(Settings.Space.SPACE_MORE);
                        })
                        .dismissOnClick();

                customMenu.setPopupWindow(CustomPopupWindow.Builder(findViewById(R.id.main_bottom_navigation), customMenu.buildRecyclerView().generateRecyclerView())
                        .setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.TOP_RIGHT)
                        .show_popupWindow());

                return false;
            } else {
                Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (Settings.Space.nextMoreSpace != null) {
            selectedSpace = Settings.Space.nextMoreSpace;
            menuItem.setTitle(selectedSpace.getPlural());
            Settings.Space.nextMoreSpace = null;
        }

        if (!selectedSpace.isShown())
            selectedSpace = Settings.Space.getFirstShown();

        if (!selectedSpace.hasFragment())
            selectedSpace.setFragment(new SpaceFragment(selectedSpace.getFragmentLayoutId()));
        SpaceFragment.currentSpace = selectedSpace;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_container, selectedSpace.getFragment()).runOnCommit(() -> {
            setCounts(this);
            applyDimensionsLayout();
            if (currentSpace != null)
                mySPR_settings.edit().putInt(SETTING_LAST_OPEN_SPACE, currentSpace.getItemId()).apply();
            if (Settings.Space.allSpaces.filter(Settings.Space::isShown, false).size() > 1) {
                ViewGroup view = (ViewGroup) currentSpace.getFragment().getView();
                touchListener = new Utility.OnSwipeTouchListener(MainActivity.this) {
                    @Override
                    public boolean onSwipeRight() {
                        Settings.Space previous = Settings.Space.allSpaces.filter(Settings.Space::isShown, false).previous(currentSpace);
                        if (previous.isInMore()) {
                            Settings.Space.nextMoreSpace = previous;
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(Settings.Space.SPACE_MORE);
                        } else
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(previous.getItemId());
                        return true;
                    }

                    @Override
                    public boolean onSwipeLeft() {
                        Settings.Space next = Settings.Space.allSpaces.filter(Settings.Space::isShown, false).next(currentSpace);
                        if (next.isInMore()) {
                            Settings.Space.nextMoreSpace = next;
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(Settings.Space.SPACE_MORE);
                        } else
                            ((BottomNavigationView) findViewById(R.id.main_bottom_navigation)).setSelectedItemId(next.getItemId());
                        return true;
                    }
                };

                FrameLayout frameLayout = findViewById(R.id.main_frame_container);
                if (frameLayout.getChildCount() == 0 && view != null) {
                    ViewParent parent = view.getParent();
                    if (parent != null)
                        ((FrameLayout) parent).removeAllViews();
                    frameLayout.addView(view);
                }

                findViewById(R.id.scrollView).setOnTouchListener(touchListener);
                view.setOnTouchListener(touchListener);
                Utility.applyToAllViews(view, SquareLayout.class, squareLayout -> squareLayout.setOnTouchListener(touchListener));
            }
        }).commitAllowingStateLoss();
        currentSpace = selectedSpace;
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
                .setTitle("Anmelden oder Erstellen")
                .setView(R.layout.dialog_database_login)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout dialog_databaseLogin_name_layout = customDialog.findViewById(R.id.dialog_databaseLogin_name_layout);
                    TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);
                    TextInputLayout dialog_databaseLogin_passwordSecond_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordSecond_layout);
                    dialog_databaseLogin_passwordSecond_layout.setVisibility(View.GONE);

                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
                    helper.addValidator(dialog_databaseLogin_name_layout, dialog_databaseLogin_passwordFirst_layout)
                            .defaultDialogValidation(customDialog)
//                            .setValidation(dialog_databaseLogin_passwordSecond_layout, (validator, text) -> {
//                                if (Utility.stringExists(text) && !text.equals(dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim()))
//                                    validator.setInvalid("Die Passwörter müssen gleich sein");
//                            })
                            .addActionListener(dialog_databaseLogin_passwordFirst_layout, (textInputHelper, textInputLayout, actionId, text) -> {
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
                                    .setTitle("Datenbank Noch Nicht Vorhanden")
                                    .setText(new Helpers.SpannableStringHelper().append("Die Datenbank '").appendBold(databaseCode).append("' existiert noch nicht.\nMochtest du sie hinzufügen?").get())
                                    .setEdit(new CustomDialog.EditBuilder()
                                            .setHint("Passwort bestätigen")
                                            .setInputType(Helpers.TextInputHelper.INPUT_TYPE.PASSWORD)
                                            .setValidation((validator, text) -> {
                                                validator.asWhiteList();
                                                if (text.equals(((EditText) customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst)).getText().toString()))
                                                    validator.setValid();
                                                else
                                                    validator.setInvalid("Die Passwörter müssen übereinstimmen");
                                            }))
                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                        Settings.resetEncryption();
                                        customDialog.dismiss();
                                        onFinish.runOndatabaseCodeFinish(databaseCode);
                                        Database.databaseCall_write(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString(), Database.databaseCode, Database.PASSWORD);
                                    })
                                    .disableLastAddedButton()
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
//        CropImage.activity(Uri.fromFile(new File(pathname)))
//                .setAllowFlipping(false)
//                .setAllowRotation(false)
//                .setActivityTitle("Ausschnitt Auswählen")
//                .setCropMenuCropButtonIcon(R.drawable.ic_check)
////                .setInitialCropWindowRectangle(new Rect())
//                // ToDo: letzte selektion am Anfang
//                .start(this);
//        CustomDialog.Builder(this)
//                .setTitle("Custom Crop Test")
//                .setView(customDialog -> {
//                    ImageView imageView = new PhotoView(this);
//                    imageView.setAdjustViewBounds(true);
//                    Glide.with(this)
//                            .load(new File("/storage/emulated/0/Download/tmp_1623759094073.jpg"))
////                            // TODO remove after transformation is done
////                            .diskCacheStrategy(DiskCacheStrategy.NONE) // override default RESULT cache and apply transform always
////                            .skipMemoryCache(true) // do not reuse the transformed result while running
//                            .transform(new Utility.CustomCropTransformation(new Utility.ImageCrop(//200, 100, 100, 100)))
//                                    CustomUtility.randomInteger(50, 400),
//                                    CustomUtility.randomInteger(50, 400),
//                                    CustomUtility.randomInteger(50, 400),
//                                    CustomUtility.randomInteger(50, 400))))
//                            .into(imageView);
//                    return imageView;
//                })
//                .setDimensionsFullscreen()
//                .show();
//
//        if (true)
//            return;
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

        CustomList<Video> allVideos = new CustomList<>(database.videoMap.values());
        CustomList<Video> shownVideos = new CustomList<>(allVideos);
        Utility.GenericReturnInterface<CompactCalendarView, Date> getCurrentDate = compactCalendarView -> (Date) CustomUtility.reflectionGet(compactCalendarView, "compactCalendarController", "currentDate");
        Utility.DoubleGenericInterface<CompactCalendarView, Date> setCurrentDate = (compactCalendarView, date) -> {
            CompactCalendarView.CompactCalendarViewListener listener = (CompactCalendarView.CompactCalendarViewListener) CustomUtility.reflectionGet(compactCalendarView, "compactCalendarController", "listener");
            if (listener != null) {
                listener.onDayClick(date);
                listener.onMonthScroll(date);
            }
        };
        final com.maxMustermannGeheim.linkcollection.Utilities.Helpers.AdvancedQueryHelper<Video>[] advancedQueryHelper = new com.maxMustermannGeheim.linkcollection.Utilities.Helpers.AdvancedQueryHelper[]{null};

        calenderDialog = CustomDialog.Builder(this)
                .setTitle(currentSpace.getName() + " Kalender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    if (!reload) {
                        ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                        stub_groups.setLayoutResource(R.layout.fragment_calender);
                        stub_groups.inflate();
                    }
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);

                    Date currentDate = null;
                    if (reload) {
                        currentDate = getCurrentDate.run(calendarView);
                    }

                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupFilmCalender(this, calendarView, ((FrameLayout) view), shownVideos, true);
                    if (currentDate != null)
                        setCurrentDate.run(calendarView, currentDate);

                })
                .disableScroll()
                .setDimensionsFullscreen()
                .enableTitleBackButton()
                .enableTitleRightButton( R.drawable.ic_filter, customDialog -> {

                    if (advancedQueryHelper[0] == null) {
                        HashSet<VideoActivity.FILTER_TYPE> filterTypeSet = new HashSet<>();
                        filterTypeSet.add(VideoActivity.FILTER_TYPE.NAME);
                        filterTypeSet.add(VideoActivity.FILTER_TYPE.ACTOR);
                        filterTypeSet.add(VideoActivity.FILTER_TYPE.GENRE);
                        filterTypeSet.add(VideoActivity.FILTER_TYPE.STUDIO);
                        filterTypeSet.add(VideoActivity.FILTER_TYPE.COLLECTION);

                        SearchView searchView = new SearchView(this);
                        searchView.setIconified(false);
                        searchView.setQueryHint(currentSpace.getPlural() + " Filtern");
                        searchView.setPadding(0, CustomUtility.dpToPx(4), 0, CustomUtility.dpToPx(8));
                        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        searchView.setIconifiedByDefault(false);

                        advancedQueryHelper[0] = VideoActivity.getAdvancedQueryHelper(this, searchView, shownVideos, filterTypeSet);
                        advancedQueryHelper[0].disableColoration();
                    }
//                    else
//                        advancedQueryHelper[0].setSearchView(searchView);

                    CustomDialog.Builder(this)
                            .setTitle("Kalender Filtern")
                            .setView(advancedQueryHelper[0].getSearchView())
                            .setSetViewContent((customDialog1, view, reload) -> view.requestFocus())
                            .addButton(R.drawable.ic_settings_dialog, customDialog1 -> advancedQueryHelper[0].showAdvancedSearchDialog(), false)
                            .alignPreviousButtonsLeft()
                            .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                            .addButton("Filtern", customDialog1 -> {
                                shownVideos.replaceWith(allVideos);
                                advancedQueryHelper[0].filterFull(shownVideos);
                                customDialog.reloadView();
                            })
                            .addIconDecorationToLastAddedButton(R.drawable.ic_filter, CustomDialog.IconDecorationPosition.LEFT)
                            .colorLastAddedButton()
                            .setOnDialogDismiss(customDialog1 -> {
                                SearchView searchView = advancedQueryHelper[0].getSearchView();
                                ((ViewGroup) searchView.getParent()).removeView(searchView);
                            })
                            .show();
//                    Toast.makeText(this, "Filter", Toast.LENGTH_SHORT).show();
//                    shownVideos.filter(video -> video.getName().toLowerCase().contains("the"), true);
                })
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

//    public void showLaterMenu_show(View view) {
//        ShowActivity.showLaterMenu(this, view);
//    }

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


    //  ------------------------- Media ------------------------->
    public void openMediaActivity(View view) {
        if (!Database.isReady())
            return;
        startActivityForResult(new Intent(this, MediaActivity.class), START_MEDIA);
    }

    public void openMediaPersonActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
        startActivityForResult(intent, START_MEDIA_PERSON);
    }

    public void openMediaCategoryActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
        startActivityForResult(intent, START_MEDIA_CATEGORY);
    }

    public void openMediaTagActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_TAG);
        startActivityForResult(intent, START_MEDIA_TAG);
    }

    public void openMediaEventActivity(View view) {
        if (!Database.isReady())
            return;
        Intent intent = new Intent(this, MediaEventActivity.class);
        intent.putExtra(EXTRA_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_EVENT);
        startActivityForResult(intent, START_MEDIA_EVENT);
    }
    //  <------------------------- Media -------------------------


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
            case R.id.browser:
//                String url = "https://www.imdb.com/title/tt8135530/";
//                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                builder.setToolbarColor(getColor(R.color.colorPrimary));
//                builder.addDefaultShareMenuItem();
//
//                CustomTabsIntent customTabsIntent = builder.build();
//                customTabsIntent.launchUrl(this, Uri.parse(url));
                // ToDo: https://stackoverflow.com/questions/23426113/how-to-select-multiple-images-from-gallery-in-android/23426985#:~:text=setAction(Intent.-,ACTION_GET_CONTENT)%3B%20startActivityForResult(Intent.,Android%20API%2018%20and%20higher.&text=Here%20is%20the%20code%20for,and%20video%20from%20Default%20Gallery.
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                //i.setType("video/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(i, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, 1);
                break;
            case R.id.taskBar_main_settings:
                startActivityForResult(new Intent(this, Settings.class), START_SETTINGS);
                break;
        }
        return true;
    }

    String pathname = "/storage/emulated/0/Download/117749174_o.jpg";

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
//        if (requestCode == 1) {
//            data.getExtras();
//            String BREAKPOINT = null;
//        }
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
//        Database.saveAll();
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyDimensionsLayout();
    }
}
