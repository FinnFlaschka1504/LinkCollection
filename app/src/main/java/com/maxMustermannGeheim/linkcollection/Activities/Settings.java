package com.maxMustermannGeheim.linkcollection.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog_new;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Settings.Space.allSpaces;

public class Settings extends AppCompatActivity {

    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";

//    public static final String SETTING_SHOPPING_LIST_SORT = "SETTING_SHOPPING_LIST_SORT";
//    public static final String SETTING_SHOPPING_LIST_HIERARCHY = "SETTING_SHOPPING_LIST_HIERARCHY";
//    public static final String SETTING_FINANCES_SWAP_LABLES = "SETTING_FINANCES_SWAP_LABLES";
//    public static final String LAST_VERSION = "LAST_VERSION";
//    public static final String SETTING_OTHERS_USER = "SETTING_OTHERS_USER";
//    public static final String SETTING_OTHERS_DARK_MODE = "SETTING_OTHERS_DARK_MODE";
    public static final String SETTING_SPACE_SHOWN_ = "SETTING_SPACE_SHOWN_";
    public static final String SETTING_SPACE_NAMES_ = "SETTING_SPACE_NAMES_";
    public static final String SETTING_SPACE_ORDER = "SETTING_SPACE_ORDER";
    public static Map<String, String> settingsMap = new HashMap<>();

    static Database database = Database.getInstance();
    public static SharedPreferences mySPR_settings;

    RecyclerView spaceRecycler;
    TextView settings_others_databaseCode;
    Button settings_others_changeDatabaseCode;
    TextView settings_others_activeSpaces;
    Button settings_others_spaceSelector;

    Database.DatabaseReloadListener databaseReloadListener;
    CustomRecycler spaceRecycler_customRecycler;
    private boolean spaceOrderChanged;

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

        allSpaces.add(new Space(context.getString(R.string.bottomMenu_video), context.getString(R.string.bottomMenu_videos)).setActivity(VideoActivity.class).setItemId(Space.SPACE_VIDEO).setIconId(R.drawable.ic_videos).setFragmentLayoutId(R.layout.main_fragment_videos)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_label)).setText(space.getPlural());

                    ((TextView) view.findViewById(R.id.main_videoCount)).setText(String.valueOf(database.videoMap.size()));
                    ((TextView) view.findViewById(R.id.main_darstellerCount)).setText(String.valueOf(database.darstellerMap.size()));
                    ((TextView) view.findViewById(R.id.main_genreCount)).setText(String.valueOf(database.genreMap.size()));
                    ((TextView) view.findViewById(R.id.main_studioCount)).setText(String.valueOf(database.studioMap.size()));
                    Set<Date> dateSet = new HashSet<>();
                    database.videoMap.values().forEach(video -> dateSet.addAll(video.getDateList()));
                    ((TextView) view.findViewById(R.id.main_daysCount)).setText(String.valueOf(dateSet.size()));
                    ((TextView) view.findViewById(R.id.main_watchLaterCount)).setText(String.valueOf(database.watchLaterList.size()));
                })
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_knowledge), context.getString(R.string.bottomMenu_knowledge)).setActivity(KnowledgeActivity.class).setItemId(Space.SPACE_KNOWLEDGE).setIconId(R.drawable.ic_knowledge).setFragmentLayoutId(R.layout.main_fragment_knowledge)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_knowledge_label)).setText(space.getPlural());
                    ((TextView) view.findViewById(R.id.main_knowledge_Count)).setText(String.valueOf(database.knowledgeMap.size()));
                    ((TextView) view.findViewById(R.id.main_knowledge_categoryCount)).setText(String.valueOf(database.knowledgeCategoryMap.size()));
                })
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_owe), context.getString(R.string.bottomMenu_owe)).setActivity(OweActivity.class).setItemId(Space.SPACE_OWE).setIconId(R.drawable.ic_euro).setFragmentLayoutId(R.layout.main_fragment_owe)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_owe_label)).setText(space.getPlural());

//                    RoundCornerProgressBar main_owe_progressBarOwn = view.findViewById(R.id.main_owe_progressBarOwn);
//                    main_owe_progressBarOwn.setProgress(70);
//                    main_owe_progressBarOwn.setMax(100);
//                    RoundCornerProgressBar main_owe_progressBarOthers = view.findViewById(R.id.main_owe_progressBarOthers);
//                    main_owe_progressBarOthers.setProgress(30);
//                    main_owe_progressBarOthers.setMax(100);
                    ((TextView) view.findViewById(R.id.main_owe_countAll)).setText(String.valueOf(database.oweMap.values().stream().filter(Owe::isOpen).count()));
                    ((TextView) view.findViewById(R.id.main_owe_countPerson)).setText(String.valueOf(database.personMap.size()));
                })
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_joke), context.getString(R.string.bottomMenu_jokes)).setActivity(JokeActivity.class).setItemId(Space.SPACE_JOKE).setIconId(R.drawable.ic_jokes).setFragmentLayoutId(R.layout.main_fragment_joke)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_joke_label)).setText(space.getPlural());

                    ((TextView) view.findViewById(R.id.main_joke_Count)).setText(String.valueOf(database.jokeMap.size()));
                    ((TextView) view.findViewById(R.id.main_joke_categoryCount)).setText(String.valueOf(database.jokeCategoryMap.size()));
                })
                .setSettingsDialog(null));


        for (Space space : allSpaces) {
            settingsMap.put(SETTING_SPACE_SHOWN_ + space.getItemId(), String.valueOf(space.isShown()));
            settingsMap.put(SETTING_SPACE_NAMES_ + space.getItemId(), space.getName() + "|" + space.getPlural());
        }
    }
    private static void updateSpaces() {
        for (Space space : allSpaces) {
            String key = SETTING_SPACE_SHOWN_ + space.getItemId();
            space.setShown(Boolean.parseBoolean(settingsMap.get(key)));

            String spaceNames_string = mySPR_settings.getString(SETTING_SPACE_NAMES_ + space.getItemId(), null);
            if (spaceNames_string != null) {
                String[] singPlur = spaceNames_string.split("\\|");
                space.setPlural(singPlur[1]).setName(singPlur[0]);
            }

        }
        String spaceOrder_string = mySPR_settings.getString(SETTING_SPACE_ORDER, null);
        if (spaceOrder_string != null) {
            Map<Integer,Integer> spaceOrder = Arrays.stream(spaceOrder_string.split(";")).map(s -> {
                String[] strings = s.split(":");
                return new Pair<>(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
            }).collect(Collectors.toMap(pair -> pair.first, pair -> pair.second));
            allSpaces.sort((space1, space2) -> {
                Integer pos1 = spaceOrder.get(space1.getItemId());
                Integer pos2 = spaceOrder.get(space2.getItemId());

                if (pos1 == null || pos2 == null)
                    return 0;
                else
                    return pos1.compareTo(pos2);
            });
        }
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
        settings_others_activeSpaces = findViewById(R.id.settings_others_activeSpaces);
        settings_others_spaceSelector = findViewById(R.id.settings_others_spaceSelector);
    }

    private void setSettings() {
        spaceRecycler_customRecycler = CustomRecycler.Builder(this, spaceRecycler)
                .setItemLayout(R.layout.list_item_space_setting)
                .setGetActiveObjectList(() -> allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                .setSetItemContent((CustomRecycler.SetItemContent<Space>) (itemView, space) -> ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural()))
                .removeLastDivider()
                .setOnClickListener((CustomRecycler.OnClickListener<Space>)(customRecycler, itemView, space, index) -> space.showSettingsDialog(this))
                .setDividerMargin_inDp(16)
                .generateCustomRecycler();


        settings_others_databaseCode.setText(Database.databaseCode);


        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
        );
    }

    private void setListeners() {
        settings_others_changeDatabaseCode.setOnClickListener(v -> {
            CustomDialog_new.Builder(this)
                    .setTitle("Datenbank-Code Ändern")
                    .setButtonConfiguration(CustomDialog_new.BUTTON_CONFIGURATION.OK_CANCEL)
                    .setEdit(new CustomDialog_new.EditBuilder().setText(Database.databaseCode).setHint("Datenbank-Code"))
                    .addButton(CustomDialog_new.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                        String code = customDialog.getEditText().trim();
                        SharedPreferences mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);
                        mySPR_daten.edit().putString(Database.DATABASE_CODE, code).commit();
                        Database.getInstance(mySPR_daten, database1 -> {}, false);
                    })
                    .setOnDialogDismiss(customDialog -> settings_others_databaseCode.setText(customDialog.getEditText()))
                    .show();
        });

        settings_others_spaceSelector.setOnClickListener(v -> {
            CustomDialog_new.Builder(this)
                    .setTitle("Bereiche Auswählen")
                    .setView(new CustomRecycler<Space>(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces)
                            .setSetItemContent((CustomRecycler.SetItemContent<Space>)(itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isShown());
                            })
                            .removeLastDivider()
                            .setOnClickListener((CustomRecycler.OnClickListener<Space>) (customRecycler, itemView, space, index) -> {
                                CheckBox list_spaceSetting_shown = itemView.findViewById(R.id.list_spaceSetting_shown);
                                boolean checked = list_spaceSetting_shown.isChecked();

                                if (checked && allSpaces.stream().filter(Space::isShown).count() == 1) {
                                    Toast.makeText(this, "Es muss mindestens ein Bereich ausgewählt sein", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                list_spaceSetting_shown.setChecked(!checked);
                                space.setShown(!checked);
//                                spaceRecycler_customRecycler.reload();
                                settings_others_activeSpaces.setText(
                                        allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
                                );
                                setResult(RESULT_OK);
                            })
                            .enableDragAndDrop(objectList -> {
                                spaceOrderChanged = true;
                                setResult(RESULT_OK);
                            })
                            .setDividerMargin_inDp(16)
                            .generate())
                    .setOnDialogDismiss(dialog -> updateSpaceStatusSettings())
                    .setButtonConfiguration(CustomDialog_new.BUTTON_CONFIGURATION.BACK)
                    .show();
        });
    }

    public static class Space extends ParentClass {
        public static List<Space> allSpaces = new ArrayList<>();
        public static final int SPACE_VIDEO = 1;
        public static final int SPACE_KNOWLEDGE = 2;
        public static final int SPACE_OWE = 3;
        public static final int SPACE_JOKE = 4;

        private String plural;
        private int itemId;
        private boolean shown = true;
        private int iconId;
        private int fragmentLayoutId;
        private Fragment fragment;
        private SetLayout setLayout;
        private Class activity;
        BuildSettingsDialog buildSettingsDialog;

        public Space(String name, String plural) {
            this.name = name;
            this.plural = plural;
        }

        public String getPlural() {
            return plural;
        }

        public Space setPlural(String plural) {
            this.plural = plural;
            return this;
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

        public int getFragmentLayoutId() {
            return fragmentLayoutId;
        }

        public Space setFragmentLayoutId(int fragmentLayoutId) {
            this.fragmentLayoutId = fragmentLayoutId;
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
            void runSetLayout(Space space, View view);
        }

        public void setLayout() {
            setLayout.runSetLayout(this, fragment.getView());
        }

        public Space setSetLayout(SetLayout setLayout) {
            this.setLayout = setLayout;
            return this;
        }

        public Space setActivity(Class activity) {
            this.activity = activity;
            return this;
        }

        public Class getActivity() {
            return activity;
        }

        //  ----- SettingsDialog ----->
        public interface BuildSettingsDialog {
            CustomDialog_new runBuildSettingsDialog(Settings settings, Space space);
        }

        public Space setSettingsDialog(Utility.Triple<Integer, SetViewContent, OnClick> id_SetViewContent_OnClick_quadruple) {
            buildSettingsDialog = (context1, space) -> {
                CustomDialog_new customDialog = CustomDialog_new.Builder(context1).setButtonConfiguration(CustomDialog_new.BUTTON_CONFIGURATION.OK_CANCEL)
                        .setTitle(space.getPlural() + "-Einstellungen")
                        .setEdit(new CustomDialog_new.EditBuilder().setHint("Singular|Plural").setText(space.getName() + "|" + space.getPlural()).setValidation("\\w+\\|\\w+"));
                if (id_SetViewContent_OnClick_quadruple != null)
                    customDialog.setView(id_SetViewContent_OnClick_quadruple.first)
                            .setSetViewContent((customDialog1, view) -> id_SetViewContent_OnClick_quadruple.second.runSetViewContent(customDialog1, view, this))
                            .addButton(CustomDialog_new.BUTTON_TYPE.OK_BUTTON, customDialog1 -> id_SetViewContent_OnClick_quadruple.third.runOnClick(customDialog1, this));
                else
                    customDialog.addButton(CustomDialog_new.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                        new OnClick() {}.runOnClick(customDialog1, this);
                        context1.spaceRecycler_customRecycler.reload();
                        context1.setResult(RESULT_OK);
                        Settings.changeSetting(SETTING_SPACE_NAMES_ + space.getItemId(), space.getName() + "|" + space.getPlural());
                    }, false);
                return customDialog;
            };

            return this;
        }

        public interface SetViewContent{
            void runSetViewContent(CustomDialog_new customDialog, View view, Space space);
        }

        public interface OnClick {
            default void runOnClick(CustomDialog_new customDialog, Space space){
                String text = customDialog.getEditText();
                if (text.isEmpty())
                    return;

                String[] singPlur = text.split("\\|");
                if (singPlur.length != 2)
                    return;

                space.setPlural(singPlur[1].trim()).setName(singPlur[0].trim());
                customDialog.dismiss();

            }
        }

        public CustomDialog_new showSettingsDialog(Settings settings) {
            if (buildSettingsDialog == null) {
                return null;
            }
            return buildSettingsDialog.runBuildSettingsDialog(settings, this).show();
        }
        //  <----- SettingsDialog -----

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
            changeSetting(SETTING_SPACE_SHOWN_ + space.getItemId(), String.valueOf(space.isShown()));
        }

        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", ")));

        spaceRecycler_customRecycler.reload();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        if (spaceOrderChanged) {
            List<String> spaceOrder = new ArrayList<>();
            for (Space space : allSpaces)
                spaceOrder.add(space.getItemId() + ":" + allSpaces.indexOf(space));
            mySPR_settings.edit().putString(SETTING_SPACE_ORDER, String.join(";", spaceOrder)).apply();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }
}
