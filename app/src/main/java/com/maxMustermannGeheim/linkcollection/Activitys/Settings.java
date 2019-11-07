package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activitys.Settings.Space.allSpaces;

public class Settings extends AppCompatActivity {

    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";

    public static final String SETTING_SHOPPING_LIST_SORT = "SETTING_SHOPPING_LIST_SORT";
    public static final String SETTING_SHOPPING_LIST_HIERARCHY = "SETTING_SHOPPING_LIST_HIERARCHY";
    public static final String SETTING_FINANCES_SWAP_LABLES = "SETTING_FINANCES_SWAP_LABLES";
    public static final String LAST_VERSION = "LAST_VERSION";
    public static final String SETTING_OTHERS_USER = "SETTING_OTHERS_USER";
    public static final String SETTING_OTHERS_DARK_MODE = "SETTING_OTHERS_DARK_MODE";
    public static final String SETTING_SPACE_SHOWN_ = "SETTING_SPACE_SHOWN_";
    public static Map<String, String> settingsMap = new HashMap<>();

    static Database database = Database.getInstance();
    public static SharedPreferences mySPR_settings;

    RecyclerView spaceRecycler;
    TextView settings_others_databaseCode;
    Button settings_others_changeDatabaseCode;
    TextView settings_others_aktiveSpaces;
    Button settings_others_spaceSelector;

    Database.DatabaseReloadListener databaseReloadListener;
    CustomRecycler spaceRecycler_customRecycler;

    //  ----- Static ----->
    public static void startSettings_ifNeeded(Context context) {
        if (mySPR_settings != null)
            return;

        mySPR_settings = context.getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE);

        applySettingsMap(context);
        applySpacesList(context);
        getFromSpr();
        updateSpaces();
    }

    private static void applySettingsMap(Context context) {
        if (!Database.isReady())
            return;

//        settingsMap.put(SETTING_SHOPPING_LIST_SORT, "Nach Häufigkeit");
//        settingsMap.put(SETTING_SHOPPING_LIST_HIERARCHY, "true");
//        settingsMap.put(SETTING_FINANCES_SWAP_LABLES, "false");
////        settingsMap.put(SETTING_OTHERS_USER, Database.getInstance().loggedInUserId);
////        settingsMap.put(SETTING_OTHERS_DARK_MODE, context.getResources().getString(R.string.automatically));
//        settingsMap.put(LAST_VERSION, "1.0");
    }

    public static boolean changeSetting(String key, String newValue) {
        if (!settingsMap.containsKey(key))
            return false;
        settingsMap.replace(key, newValue);
        saveSettings();
        return true;
    }

    public static String getSingleSetting(Context context, String key){
        return context.getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE).getString(key, "");
    }

    private static void saveSettings() {
        SharedPreferences.Editor editor =  mySPR_settings.edit();

        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

//    public static void applyDarkmode(Context context){
//        String s;
//        if (settingsMap.isEmpty())
//            s = getSingleSetting(context, SETTING_OTHERS_DARK_MODE);
//        else
//            s = settingsMap.get(SETTING_OTHERS_DARK_MODE);
////        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (context.getResources().getString(R.string.automatically).equals(s) &&
//                !(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED || AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)){ // && nightModeFlags != Configuration.UI_MODE_NIGHT_UNDEFINED) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        } else if (context.getResources().getString(R.string.on).equals(s) && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES){ // && nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else if (context.getResources().getString(R.string.off).equals(s) && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO){ // && nightModeFlags != Configuration.UI_MODE_NIGHT_NO) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//    }

    public static void applySpacesList(Context context){
        if (!allSpaces.isEmpty())
            return;

        allSpaces.add(new Space(context.getString(R.string.bottomMenu_video)).setItemId(Space.SPACE_VIDEO).setIconId(R.drawable.ic_videos).setLayoutId(R.layout.main_fragment_videos)
                .setSetLayout(view -> {
                    ((TextView) view.findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
                    ((TextView) view.findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
                    ((TextView) view.findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
                    ((TextView) view.findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
                    Set<Date> dateSet = new HashSet<>();
                    database.videoMap.values().forEach(video -> dateSet.addAll(video.getDateList()));
                    ((TextView) view.findViewById(R.id.main_daysCount)).setText(String.valueOf(dateSet.size()));
                    ((TextView) view.findViewById(R.id.main_watchLaterCount)).setText(String.valueOf(database.watchLaterList.size()));
                }));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_knowledge)).setItemId(Space.SPACE_KNOWLEDGE).setIconId(R.drawable.ic_knowledge).setLayoutId(R.layout.main_fragment_knowledge)
                .setSetLayout(view -> {
                    ((TextView) view.findViewById(R.id.main_knowledgeCount)).setText(String.valueOf(database.knowledgeMap.size()));
                    ((TextView) view.findViewById(R.id.main_categoryCount)).setText(String.valueOf(database.knowledgeCategoryMap.size()));
                }));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_owe)).setItemId(Space.SPACE_OWE).setIconId(R.drawable.ic_euro).setLayoutId(R.layout.main_fragment_owe)
                .setSetLayout(view -> {
//                    RoundCornerProgressBar main_owe_progressBarOwn = view.findViewById(R.id.main_owe_progressBarOwn);
//                    main_owe_progressBarOwn.setProgress(70);
//                    main_owe_progressBarOwn.setMax(100);
                    RoundCornerProgressBar main_owe_progressBarOthers = view.findViewById(R.id.main_owe_progressBarOthers);
                    main_owe_progressBarOthers.setProgress(30);
                    main_owe_progressBarOthers.setMax(100);
//                    ((TextView) view.findViewById(R.id.main_knowledgeCount)).setText(String.valueOf(database.knowledgeMap.size()));
//                    ((TextView) view.findViewById(R.id.main_categoryCount)).setText(String.valueOf(database.knowledgeCategoryMap.size()));
                }));

        for (Space space : allSpaces) {
            settingsMap.put(SETTING_SPACE_SHOWN_ + space.getName().toUpperCase(), String.valueOf(space.isShown()));
        }
    }
    private static void updateSpaces() {
        for (Space space : allSpaces) {
            String key = SETTING_SPACE_SHOWN_ + space.getName().toUpperCase();
            space.setShown(Boolean.parseBoolean(settingsMap.get(key)));
        }
        // ToDo: settingseintrag verändern
    }


    private static void getFromSpr() {
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            entry.setValue(mySPR_settings.getString(entry.getKey(), entry.getValue()));
        }
    }
//  <----- Static -----


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        startSettings_ifNeeded(this);

        getViews();

        setSettings();
        setListeners();

//        databaseReloadListener = Database.addDatabaseReloadListener(database1 -> {
//            Toast.makeText(this, "Datenbank neu geladen", Toast.LENGTH_SHORT).show();
//        });
    }

    private void getViews() {
        spaceRecycler = findViewById(R.id.settings_spaces_recycler);
        settings_others_databaseCode = findViewById(R.id.settings_others_databaseCode);
        settings_others_changeDatabaseCode = findViewById(R.id.settings_others_changeDatabaseCode);
        settings_others_aktiveSpaces = findViewById(R.id.settings_others_aktiveSpaces);
        settings_others_spaceSelector = findViewById(R.id.settings_others_spaceSelector);
    }

    private void setSettings() {
        spaceRecycler_customRecycler = CustomRecycler.Builder(this)
                .setRecycler(spaceRecycler)
                .setItemLayout(R.layout.list_item_space_setting)
                .setGetActiveObjectList(() -> allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                .setSetItemContent((CustomRecycler.SetItemContent<Space>) (itemView, space) -> ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getName()))
                .removeLastDivider()
                .generateCustomRecycler();


        settings_others_databaseCode.setText(Database.databaseCode);


        settings_others_aktiveSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
        );
    }

    private void setListeners() {
        settings_others_spaceSelector.setOnClickListener(v -> {
            CustomDialog.Builder(this)
                    .setTitle("Bereiche Auswählen")
                    .setView(CustomRecycler.Builder(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces)
                            .setSetItemContent((CustomRecycler.SetItemContent<Space>)(itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getName());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isShown());
                            })
                            .removeLastDivider()
                            .setOnClickListener((CustomRecycler.OnClickListener<Space>) (recycler, itemView, space, index) -> {
                                CheckBox list_spaceSetting_shown = itemView.findViewById(R.id.list_spaceSetting_shown);
                                boolean checked = list_spaceSetting_shown.isChecked();

                                if (checked && allSpaces.stream().filter(Space::isShown).count() == 1) {
                                    Toast.makeText(this, "Es muss mindestens ein Bereich ausgewählt sein", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                list_spaceSetting_shown.setChecked(!checked);
                                space.setShown(!checked);
                                spaceRecycler_customRecycler.reload();
                                settings_others_aktiveSpaces.setText(
                                        allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
                                );
                                setResult(RESULT_OK);
                            })
                            .generate())
                    .show()
                    .setOnDismissListener(dialog -> updateSpaceStatusSettings());
        });

        settings_others_changeDatabaseCode.setOnClickListener(v -> {
            int okButtonId = View.generateViewId();
            CustomDialog.Builder(this)
                    .setTitle("Datenbank-Code Ändern")
                    .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                    .setEdit(new CustomDialog.EditBuilder().setText(Database.databaseCode).setFireButtonOnOK(okButtonId))
                    .addButton(CustomDialog.OK_BUTTON, (customDialog, dialog) -> {
                        String code = CustomDialog.getEditText(dialog).trim();
                        SharedPreferences mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);
                        mySPR_daten.edit().putString(Database.DATABASE_CODE, code).commit();
                        Database.getInstance(mySPR_daten, database1 -> {}, false);
                    }, okButtonId)
                    .setOnDialogDismiss(customDialog -> settings_others_databaseCode.setText(CustomDialog.getEditText(customDialog.getDialog())))
                    .show();
        });
    }

    public static class Space extends ParentClass {
        public static List<Space> allSpaces = new ArrayList<>();
        public static final int SPACE_VIDEO = 1;
        public static final int SPACE_KNOWLEDGE = 2;
        public static final int SPACE_OWE = 3;

        private int itemId;
        private boolean shown = true;
        private int iconId;
        private int layoutId;
        private Fragment fragment;
        private SetLayout setLayout;

        public Space(String name) {
            this.name = name;
        }

        public boolean isShown() {
            return shown;
        }

        public Space setShown(boolean shown) {
            this.shown = shown;
            return this;
        }

        public int getIconId() {
            return iconId;
        }

        public Space setIconId(int iconId) {
            this.iconId = iconId;
            return this;
        }

        public int getLayoutId() {
            return layoutId;
        }

        public Space setLayoutId(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public int getItemId() {
            return itemId;
        }

        public Space setItemId(int itemId) {
            this.itemId = itemId;
            return this;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public Space setFragment(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public boolean hasFragment() {
            return fragment != null;
        }

        private interface SetLayout {
            void runSetLayout(View view);
        }

        public void setLayout() {
            setLayout.runSetLayout(fragment.getView());
        }

        public Space setSetLayout(SetLayout setLayout) {
            this.setLayout = setLayout;
            return this;
        }

        // ------------------

        public static Space getSpaceById(int id){
            for (Space space : allSpaces) {
                if (space.getItemId() == id)
                    return space;
            }
            return null;
        }

        public static Space getFirstShown() {
            Optional<Space> first = allSpaces.stream().filter(Space::isShown).findFirst();
            return first.orElse(null);
        }
    }

    void updateSpaceStatusSettings() {
        for (Space space : allSpaces) {
            changeSetting(SETTING_SPACE_SHOWN_ + space.getName().toUpperCase(), String.valueOf(space.isShown()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }
}
