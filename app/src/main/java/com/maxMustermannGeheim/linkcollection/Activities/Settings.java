package com.maxMustermannGeheim.linkcollection.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.SquareLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

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
    public static final String SETTING_VIDEO_SHOW_RELEASE = "SETTING_VIDEO_SHOW_RELEASE";
    public static final String SETTING_VIDEO_AUTO_SEARCH = "SETTING_VIDEO_AUTO_SEARCH";
    public static final String SETTING_VIDEO_TMDB_SHORTCUT = "SETTING_VIDEO_TMDB_SHORTCUT";

    public static final String SETTING_SPACE_SHOWN_ = "SETTING_SPACE_SHOWN_";
    public static final String SETTING_SPACE_NAMES_ = "SETTING_SPACE_NAMES_";
    public static final String SETTING_SPACE_ORDER = "SETTING_SPACE_ORDER";
    public static Map<String, String> settingsMap = new HashMap<>();

    static public Database database = Database.getInstance();
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
    public static boolean startSettings_ifNeeded(Context context) {
        if (mySPR_settings != null)
            return false;

        mySPR_settings = context.getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE);

        applySettingsMap(context);
        applySpacesList(context);
        getFromSpr();
        updateSpaces();
        return true;
    }

    private static void applySettingsMap(Context context) {
        if (!Database.isReady())
            return;

        settingsMap.put(SETTING_VIDEO_SHOW_RELEASE, "true");
        settingsMap.put(SETTING_VIDEO_AUTO_SEARCH, "true");
        settingsMap.put(SETTING_VIDEO_TMDB_SHORTCUT, "true");
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

    public static String getSingleSetting(Context context, String key) {
        return context.getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE).getString(key, null);
    }
    public static Boolean getSingleSetting_boolean(Context context, String key) {
        return Boolean.parseBoolean(getSingleSetting(context, key));
    }

        private static void saveSettings() {
        SharedPreferences.Editor editor = mySPR_settings.edit();

        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    public static void applySpacesList(Context context) {
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
                    database.videoMap.values().forEach(video -> video.getDateList().forEach(date -> dateSet.add(Utility.removeTime(date))));
                    ((TextView) view.findViewById(R.id.main_daysCount)).setText(String.valueOf(dateSet.size()));
                    ((TextView) view.findViewById(R.id.main_watchLaterCount)).setText(String.valueOf(Utility.getWatchLaterList().size()));
                })
                .setSettingsDialog(new Utility.Triple<>(R.layout.dialog_settings_video, (customDialog, view, space) -> {
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_showRelease)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_RELEASE));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_autoSearch)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_AUTO_SEARCH));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_tmdbShortcut)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_TMDB_SHORTCUT));
                }, new Space.OnClick() {
                    @Override
                    public void runOnClick(CustomDialog customDialog, Space space) {
                        changeSetting(SETTING_VIDEO_SHOW_RELEASE, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_showRelease)).isChecked()));
                        changeSetting(SETTING_VIDEO_AUTO_SEARCH, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_autoSearch)).isChecked()));
                        changeSetting(SETTING_VIDEO_TMDB_SHORTCUT, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_tmdbShortcut)).isChecked()));
                    }
                })));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_show), context.getString(R.string.bottomMenu_shows)).setActivity(ShowActivity.class).setItemId(Space.SPACE_SHOW).setIconId(R.drawable.ic_shows).setFragmentLayoutId(R.layout.main_fragment_shows)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_shows_label)).setText(space.getPlural());

                    ((TextView) view.findViewById(R.id.main_shows_count)).setText(String.valueOf(database.showMap.size()));
                    ((TextView) view.findViewById(R.id.main_shows_genreCount)).setText(String.valueOf(database.showGenreMap.size()));

                    Set<Date> dateSet = new HashSet<>();
                    for (Show show : database.showMap.values()) {
                        for (Show.Season season : show.getSeasonList()) {
                            season.getEpisodeMap().values().forEach(episode -> episode.getDateList()
                                    .forEach(date -> dateSet.add(Utility.removeTime(date))));
                        }
                    }
                    List<Show.Episode> episodeList = Utility.concatenateCollections(database.showMap.values(), show ->
                            show.getAlreadyAiredList().stream().filter(episode -> !episode.isWatched()).collect(Collectors.toList()));

                    ((TextView) view.findViewById(R.id.main_shows_viewCount)).setText(String.valueOf(dateSet.size()));
                    TextView main_shows_notificationIndicator = view.findViewById(R.id.main_shows_notificationIndicator);

                    SquareLayout main_shows_notificationIndicator_layout = view.findViewById(R.id.main_shows_notificationIndicator_layout);
//                    Utility.squareView(main_shows_notificationIndicator_layout);
                    main_shows_notificationIndicator_layout.setBackground(Utility.drawableBuilder_oval(context.getColor(R.color.colorPrimary)));
                    main_shows_notificationIndicator.setText(String.valueOf(episodeList.size()));
                    main_shows_notificationIndicator_layout.setVisibility(episodeList.isEmpty() ? View.GONE : View.VISIBLE);

                    view.findViewById(R.id.main_show_nextEpisode).setOnLongClickListener(MainActivity::showNextEpisode_longClick);
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
            Map<Integer, Integer> spaceOrder = Arrays.stream(spaceOrder_string.split(";")).map(s -> {
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
        spaceRecycler_customRecycler = new CustomRecycler<Space>(this, spaceRecycler)
                .setItemLayout(R.layout.list_item_space_setting)
                .setGetActiveObjectList(() -> allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                .setSetItemContent((customRecycler, itemView, space) -> ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural()))
                .removeLastDivider()
                .setOnClickListener((customRecycler, itemView, space, index) -> space.showSettingsDialog(this))
                .setDividerMargin_inDp(16)
                .deaktivateCustomRipple()
                .hideOverscroll()
                .enableSwiping((objectList, direction, space) -> {
                    Toast.makeText(this, space.getPlural(), Toast.LENGTH_SHORT).show();
                }, true, false)
                .generate();


        settings_others_databaseCode.setText(Database.databaseCode);


        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
        );
    }

    private void setListeners() {
        settings_others_changeDatabaseCode.setOnClickListener(v -> {
            CustomDialog.Builder(this)
                    .setTitle("Datenbank-Code Ändern")
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                    .setEdit(new CustomDialog.EditBuilder().setText(Database.databaseCode).setHint("Datenbank-Code"))
                    .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                        String code = customDialog.getEditText().trim();
                        SharedPreferences mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);
                        mySPR_daten.edit().putString(Database.DATABASE_CODE, code).commit();
                        Database.getInstance(mySPR_daten, database1 -> {
                        }, false);
                    })
                    .setOnDialogDismiss(customDialog -> settings_others_databaseCode.setText(customDialog.getEditText()))
                    .show();
        });

        settings_others_spaceSelector.setOnClickListener(v -> {
            CustomDialog.Builder(this)
                    .setTitle("Bereiche Auswählen")
                    .setView(new CustomRecycler<Space>(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces)
                            .setSetItemContent((customRecycler, itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isShown());
                            })
                            .removeLastDivider()
                            .setOnClickListener((customRecycler, itemView, space, index) -> {
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
                            .enableDragAndDrop(spaceList -> {
                                spaceOrderChanged = true;
                                setResult(RESULT_OK);
                            })
                            .deaktivateCustomRipple()
                            .setDividerMargin_inDp(16)
                            .generateRecyclerView())
                    .setOnDialogDismiss(dialog -> updateSpaceStatusSettings())
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                    .show();
        });
    }

    public static class Space extends ParentClass {
        public static List<Space> allSpaces = new ArrayList<>();
        public static final int SPACE_VIDEO = 1;
        public static final int SPACE_KNOWLEDGE = 2;
        public static final int SPACE_OWE = 3;
        public static final int SPACE_JOKE = 4;
        public static final int SPACE_SHOW = 5;

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
            CustomDialog runBuildSettingsDialog(Settings settings, Space space);
        }

        public Space setSettingsDialog(Utility.Triple<Integer, SetViewContent, OnClick> layoutId_SetViewContent_OnClick_quadruple) {
            buildSettingsDialog = (context1, space) -> {
                CustomDialog customDialog = CustomDialog.Builder(context1).setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                        .setTitle(space.getPlural() + "-Einstellungen");
                if (layoutId_SetViewContent_OnClick_quadruple != null)
                    customDialog
                            .setView(layoutId_SetViewContent_OnClick_quadruple.first)
                            .setSetViewContent((customDialog1, view, reload) -> layoutId_SetViewContent_OnClick_quadruple.second.runSetViewContent(customDialog1, view, this))
                            .addButton("Umbenennen", customDialog1 -> {
                                CustomDialog renameDialog = CustomDialog.Builder(context1).setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                        .setTitle("Bereich Umbenennen")
                                        .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog2 -> {
                                            new OnClick() {
                                            }.runOnClick(customDialog2, this);
                                            context1.spaceRecycler_customRecycler.reload();
                                            context1.setResult(RESULT_OK);
                                            Settings.changeSetting(SETTING_SPACE_NAMES_ + space.getItemId(), space.getName() + "|" + space.getPlural());
                                        }, false)
                                        .setEdit(new CustomDialog.EditBuilder().setHint("Singular|Plural").setText(space.getName() + "|" + space.getPlural()).setValidation("\\w+\\|\\w+"))
                                        .show();

                            }, false)
                            .alignPreviousButtonsLeft()
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> layoutId_SetViewContent_OnClick_quadruple.third.runOnClick(customDialog1, this));
                else
                    customDialog
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                new OnClick() {
                                }.runOnClick(customDialog1, this);
                                context1.spaceRecycler_customRecycler.reload();
                                context1.setResult(RESULT_OK);
                                Settings.changeSetting(SETTING_SPACE_NAMES_ + space.getItemId(), space.getName() + "|" + space.getPlural());
                            }, false)
                            .setEdit(new CustomDialog.EditBuilder().setHint("Singular|Plural").setText(space.getName() + "|" + space.getPlural()).setValidation("\\w+\\|\\w+"));
                return customDialog;
            };

            return this;
        }

        public interface SetViewContent {
            void runSetViewContent(CustomDialog customDialog, View view, Space space);
        }

        public interface OnClick {
            default void runOnClick(CustomDialog customDialog, Space space) {
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

        public CustomDialog showSettingsDialog(Settings settings) {
            if (buildSettingsDialog == null) {
                return null;
            }
            return buildSettingsDialog.runBuildSettingsDialog(settings, this).show();
        }
        //  <----- SettingsDialog -----

        public static Space getSpaceById(int id) {
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
