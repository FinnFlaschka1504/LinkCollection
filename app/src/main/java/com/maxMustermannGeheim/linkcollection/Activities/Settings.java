package com.maxMustermannGeheim.linkcollection.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.Helpers;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.finn.androidUtilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
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

import bsh.EvalError;
import bsh.Interpreter;

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
    public static final String SETTING_SPACE_ENCRYPTED_ = "SETTING_SPACE_ENCRYPTED_";
    public static final String SETTING_SPACE_ENCRYPTION_PASSWORD = "SETTING_SPACE_ENCRYPTION_PASSWORD";

    public static Map<String, String> settingsMap = new HashMap<>();

    static public Database database = Database.getInstance();
    public static SharedPreferences mySPR_settings;

    RecyclerView spaceRecycler;
    TextView settings_others_databaseCode;
    Button settings_others_changeDatabaseCode;
    TextView settings_others_activeSpaces;
    Button settings_others_spaceSelector;
    TextView settings_others_encryptedSpaces;
    Button settings_others_encryptedSelector;

    Database.DatabaseReloadListener databaseReloadListener;
    CustomRecycler spaceRecycler_customRecycler;
    private boolean spaceOrderChanged;

    //  ----- Static ----->
    public static boolean startSettings_ifNeeded(AppCompatActivity context) {
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
//        if (!Database.isReady())
//            return;

        settingsMap.put(SETTING_VIDEO_SHOW_RELEASE, "true");
        settingsMap.put(SETTING_VIDEO_AUTO_SEARCH, "true");
        settingsMap.put(SETTING_VIDEO_TMDB_SHORTCUT, "true");
        settingsMap.put(SETTING_SPACE_ENCRYPTION_PASSWORD, "Passwort");
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

    public static void applySpacesList(AppCompatActivity context) {
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
                .setAssociatedClasses(Video.class, Darsteller.class, Studio.class, Genre.class, UrlParser.class)
                .setSettingsDialog(new Utility.Triple<>(R.layout.dialog_settings_video, (customDialog, view, space) -> {
                    Context settingsContext = customDialog.getDialog().getContext();

                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_showRelease)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_RELEASE));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_autoSearch)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_AUTO_SEARCH));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_tmdbShortcut)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_TMDB_SHORTCUT));

                    ((TextView) view.findViewById(R.id.dialogSettingsVideo_edit_parseUrl_added)).setText(database.urlParserMap.values().stream().map(UrlParser::getName).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialogSettingsVideo_edit_parseUrl_select).setOnClickListener(v -> {

                        CustomRecycler<CustomRecycler.Expandable<UrlParser>> customRecycler = new CustomRecycler<CustomRecycler.Expandable<UrlParser>>(context)
                                .setExpandableHelper(customRecycler1 -> customRecycler1.new ExpandableHelper<UrlParser>(R.layout.list_item_url_parser, (customRecycler2, itemView, urlParser, expanded) -> {
                                    ((TextView) itemView.findViewById(R.id.listItem_urlParser_name)).setText(urlParser.getName());
                                    ((TextView) itemView.findViewById(R.id.listItem_urlParser_codeType)).setText(urlParser.getType().getName());
                                    TextView listItem_urlParser_code = itemView.findViewById(R.id.listItem_urlParser_code);
                                    listItem_urlParser_code.setText(urlParser.getCode());
                                    listItem_urlParser_code.setSingleLine(!expanded);
                                }))
                                .addSubOnClickListener(R.id.listItem_urlParser_delete, (customRecycler1, itemView, urlParserExpandable, index) -> {
                                    CustomDialog.Builder(settingsContext)
                                            .setTitle("Auswerter Löschen")
                                            .setText("Möchtest du wirklich '" + urlParserExpandable.getObject().getName() + "' löschen?")
                                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                            .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                                database.urlParserMap.remove(urlParserExpandable.getObject().getUuid());
                                                customRecycler1.reload();
                                                Database.saveAll();
                                            })
                                            .show();
                                })
                                .setOnLongClickListener((customRecycler1, view1, urlParserExpandable, index) -> showAddOrEditUrlParserDialog(urlParserExpandable.getObject(), settingsContext, customRecycler1))
                                .setGetActiveObjectList(customRecycler1 -> new CustomRecycler.Expandable.ToExpandableList<UrlParser, UrlParser>()
                                        .setSort((o1, o2) -> o1.getName().compareTo(o2.getName()))
                                        .keepExpandedState(customRecycler1)
                                        .runToExpandableList(new ArrayList<>(database.urlParserMap.values()), null));

                        CustomDialog.Builder(settingsContext)
                                .setTitle("URLs Auswerter")
                                .setDimensions(true, true)
                                .disableScroll()
                                .addButton("Hinzufügen", customDialog1 -> {
                                    showAddOrEditUrlParserDialog(null, settingsContext, customRecycler);
                                }, false)
                                .alignPreviousButtonsLeft()
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                                .setView(customRecycler.generateRecyclerView())
                                .setOnDialogDismiss(customDialog1 -> UrlParser.webViewMap.clear())
                                .show();
                    });
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
                .setAssociatedClasses(Show.class, ShowGenre.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_knowledge), context.getString(R.string.bottomMenu_knowledge)).setActivity(KnowledgeActivity.class).setItemId(Space.SPACE_KNOWLEDGE).setIconId(R.drawable.ic_knowledge).setFragmentLayoutId(R.layout.main_fragment_knowledge)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_knowledge_label)).setText(space.getPlural());
                    ((TextView) view.findViewById(R.id.main_knowledge_Count)).setText(String.valueOf(database.knowledgeMap.size()));
                    ((TextView) view.findViewById(R.id.main_knowledge_categoryCount)).setText(String.valueOf(database.knowledgeCategoryMap.size()));
                })
                .setAssociatedClasses(Knowledge.class, KnowledgeCategory.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_owe), context.getString(R.string.bottomMenu_owe)).setActivity(OweActivity.class).setItemId(Space.SPACE_OWE).setIconId(R.drawable.ic_euro).setFragmentLayoutId(R.layout.main_fragment_owe)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_owe_label)).setText(space.getPlural());
                    ((TextView) view.findViewById(R.id.main_owe_countAll)).setText(String.valueOf(database.oweMap.values().stream().filter(Owe::isOpen).count()));
                    ((TextView) view.findViewById(R.id.main_owe_countPerson)).setText(String.valueOf(database.personMap.size()));
                })
                .setAssociatedClasses(Owe.class, Person.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_joke), context.getString(R.string.bottomMenu_jokes)).setActivity(JokeActivity.class).setItemId(Space.SPACE_JOKE).setIconId(R.drawable.ic_jokes).setFragmentLayoutId(R.layout.main_fragment_joke)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_joke_label)).setText(space.getPlural());

                    ((TextView) view.findViewById(R.id.main_joke_Count)).setText(String.valueOf(database.jokeMap.size()));
                    ((TextView) view.findViewById(R.id.main_joke_categoryCount)).setText(String.valueOf(database.jokeCategoryMap.size()));
                })
                .setAssociatedClasses(Joke.class, JokeCategory.class)
                .setSettingsDialog(null));


        for (Space space : allSpaces) {
            settingsMap.put(SETTING_SPACE_SHOWN_ + space.getItemId(), String.valueOf(space.isShown()));
            settingsMap.put(SETTING_SPACE_NAMES_ + space.getItemId(), space.getName() + "|" + space.getPlural());
            settingsMap.put(SETTING_SPACE_ENCRYPTED_ + space.getItemId(), String.valueOf(space.isEncrypted()));
        }
    }

    private static void showAddOrEditUrlParserDialog(@Nullable UrlParser oldUrlParser, Context context, CustomRecycler<CustomRecycler.Expandable<UrlParser>> customRecycler) {
        UrlParser editUrlParser = oldUrlParser != null ? oldUrlParser.clone() : new UrlParser("");

        CustomDialog.Builder(context)
                .setTitle("URL-Parser " + (oldUrlParser == null ? "Hinzufügen" : "Bearbeiten"))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .setView(R.layout.dialog_edit_or_add_url_parser)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout dialog_editOrAdd_urlParser_name_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_name_layout);
                    TextInputLayout dialog_editOrAdd_urlParser_url_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_url_layout);
                    TextInputLayout dialog_editOrAdd_urlParser_code_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_code_layout);
                    Spinner dialog_editOrAdd_urlParser_type = view.findViewById(R.id.dialog_editOrAdd_urlParser_type);
                    dialog_editOrAdd_urlParser_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            editUrlParser.setType(UrlParser.TYPE.getTypeByIndex(dialog_editOrAdd_urlParser_type.getSelectedItemPosition()));
                            dialog_editOrAdd_urlParser_code_layout.setHint(editUrlParser.getType().getName() + "-Code:");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    if (Utility.stringExists(editUrlParser.getName())) {
                        dialog_editOrAdd_urlParser_name_layout.getEditText().setText(editUrlParser.getName());
                        dialog_editOrAdd_urlParser_url_layout.getEditText().setText(editUrlParser.getExampleUrl());
                        dialog_editOrAdd_urlParser_code_layout.getEditText().setText(editUrlParser.getCode());
                        dialog_editOrAdd_urlParser_type.setSelection(editUrlParser.getType().getIndex());
                    }


                    com.maxMustermannGeheim.linkcollection.Utilities.Helpers.TextInputHelper helper = new com.maxMustermannGeheim.linkcollection.Utilities.Helpers.TextInputHelper((Button) customDialog.getActionButton().getButton(), dialog_editOrAdd_urlParser_name_layout, dialog_editOrAdd_urlParser_url_layout, dialog_editOrAdd_urlParser_code_layout);
                    helper.setValidation(dialog_editOrAdd_urlParser_url_layout, (validator, text) -> {
                        if (text.isEmpty())
                            validator.setWarning("Keine Beispiel-Url eingegeben");
                        else if (Utility.isUrl(text))
                            validator.setValid();
                        else
                            validator.setInvalid("Eine URL eingeben!");
                    });

                    helper.setInputType(dialog_editOrAdd_urlParser_code_layout, com.maxMustermannGeheim.linkcollection.Utilities.Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE);
                })
                .addButton("Testen", customDialog -> {
                    String name = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_name)).getText().toString();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_url)).getText().toString();
                    @SuppressLint("CutPasteId") String code = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_code)).getText().toString();

                    editUrlParser.setExampleUrl(url).setCode(code).setName(name);

                    editUrlParser.parseUrl(context, url, s ->
                            CustomDialog.Builder(context)
                                    .setTitle("Ergebnis")
                                    .setText(Utility.stringExists(s) ? s : "--Kein Ergebnis--")
//                                    .addButton("Falsch")
//                                    .addButton("Richtig")
//                                    .colorLastAddedButton()
                                    .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                                    .show());

                }, false)
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String name = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_name)).getText().toString();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_url)).getText().toString();
                    @SuppressLint("CutPasteId") String code = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_code)).getText().toString();
                    UrlParser.TYPE type = UrlParser.TYPE.getTypeByIndex(((Spinner) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_type)).getSelectedItemPosition());

                    if (oldUrlParser != null) {
                        oldUrlParser.setExampleUrl(url).setCode(code).setType(type).setName(name);
                    } else {
                        UrlParser urlParser1 = new UrlParser(name).setExampleUrl(url).setCode(code).setType(type);
                        database.urlParserMap.put(urlParser1.getUuid(), urlParser1);
                    }

                    customRecycler.reload();
                    Database.saveAll();
                    Toast.makeText(context, "Url-Parser gespeichert", Toast.LENGTH_SHORT).show();
                })
                .show();

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

            space.setEncrypted(Boolean.parseBoolean(settingsMap.get(SETTING_SPACE_ENCRYPTED_ + space.getItemId())));
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

    public static boolean isLoaded() {
        return mySPR_settings != null;
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
        settings_others_encryptedSpaces = findViewById(R.id.settings_others_encryptedSpaces);
        settings_others_encryptedSelector = findViewById(R.id.settings_others_encryptedSelector);
    }

    private void setSettings() {
        spaceRecycler_customRecycler = new CustomRecycler<Space>(this, spaceRecycler)
                .setItemLayout(R.layout.list_item_space_setting)
                .setGetActiveObjectList(customRecycler -> allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                .setSetItemContent((customRecycler, itemView, space) -> {
                    ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());
                    ImageView list_spaceSetting_lock = itemView.findViewById(R.id.list_spaceSetting_lock);
                    list_spaceSetting_lock.setImageResource(space.isEncrypted() ? R.drawable.ic_lock_closed : R.drawable.ic_lock_open);
                    list_spaceSetting_lock.setColorFilter(space.isEncrypted() ? getColor(R.color.colorGreen) : Color.RED);
                })
                .enableDivider()
                .disableCustomRipple()
                .removeLastDivider()
                .setOnClickListener((customRecycler, itemView, space, index) -> space.showSettingsDialog(this))
                .setDividerMargin_inDp(16)
                .hideOverscroll()
                .enableSwiping((objectList, direction, space) -> {
                    Toast.makeText(this, space.getPlural(), Toast.LENGTH_SHORT).show();
                }, true, false)
                .generate();


        settings_others_databaseCode.setText(Database.databaseCode);


        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", "))
        );


        settings_others_encryptedSpaces.setText(
                allSpaces.stream().filter(Space::isEncrypted).map(ParentClass::getName).collect(Collectors.joining(", "))
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
                            .enableDragAndDrop((customRecycler, objectList) -> {
                                spaceOrderChanged = true;
                                setResult(RESULT_OK);
                            })
                            .setDividerMargin_inDp(16)
                            .generateRecyclerView())
                    .setOnDialogDismiss(dialog -> updateSpaceStatusSettings())
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                    .show();
        });

        settings_others_encryptedSelector.setOnClickListener(v -> {
            Set<String> spaceSet = allSpaces.stream().filter(Space::isEncrypted).map(ParentClass::getName).collect(Collectors.toSet());
            com.finn.androidUtilities.CustomDialog.Builder(this)
                    .setTitle("Bereiche Auswählen")
                    .setView(new CustomRecycler<Space>(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                            .setSetItemContent((customRecycler, itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isEncrypted());
                            })
                            .removeLastDivider()
                            .setOnClickListener((customRecycler, itemView, space, index) -> {
                                CheckBox list_spaceSetting_shown = itemView.findViewById(R.id.list_spaceSetting_shown);
                                boolean checked = list_spaceSetting_shown.isChecked();

                                list_spaceSetting_shown.setChecked(!checked);
                                space.setEncrypted(!checked);
                                settings_others_activeSpaces.setText(
                                        allSpaces.stream().filter(Space::isEncrypted).map(ParentClass::getName).collect(Collectors.joining(", "))
                                );
                            })
                            .setDividerMargin_inDp(16)
                            .generateRecyclerView())
                    .setOnDialogDismiss(dialog -> {
                        updateSpaceStatusSettings();
                        if (!spaceSet.equals(allSpaces.stream().filter(Space::isEncrypted).map(ParentClass::getName).collect(Collectors.toSet()))) {
                            Database.saveAll(true);
                        }
                    })
                    .addButton("Passwort Ändern", customDialog -> {
                        com.finn.androidUtilities.CustomDialog.Builder(this)
                                .setTitle("Passwort Festlegen")
                                .setEdit(new com.finn.androidUtilities.CustomDialog.EditBuilder().setHint("Neues Passwort eingeben").setInputType(Helpers.TextInputHelper.INPUT_TYPE.PASSWORD))
                                .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                    String newPassword = customDialog1.getEditText();
                                    changeSetting(SETTING_SPACE_ENCRYPTION_PASSWORD, newPassword);
                                    Toast.makeText(this, "Passwort wurde zu '" + newPassword + "' geändert", Toast.LENGTH_SHORT).show();
                                    Database.saveAll(true);
                                })
                                .show();
                    }, false)
                    .alignPreviousButtonsLeft()
                    .colorLastAddedButton()
                    .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.BACK_BUTTON)
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
        private boolean encrypted;
        BuildSettingsDialog buildSettingsDialog;
        private CustomList<Class> associatedClasses = new CustomList<>();

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

        public Space setAssociatedClasses(Class... classes) {
            associatedClasses = new CustomList<>(classes);
            return this;
        }

        public CustomList<Class> getAssociatedClasses() {
            return associatedClasses;
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

        public boolean isEncrypted() {
            return encrypted;
        }

        public Space setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        public static boolean needsToBeEncrypted(Class aClass) {
            return Utility.concatenateCollections(allSpaces.stream().filter(Space::isEncrypted).collect(Collectors.toList()), Space::getAssociatedClasses).contains(aClass);
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
            changeSetting(SETTING_SPACE_ENCRYPTED_ + space.getItemId(), String.valueOf(space.isEncrypted()));
        }

        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(ParentClass::getName).collect(Collectors.joining(", ")));

        settings_others_encryptedSpaces.setText(
                allSpaces.stream().filter(Space::isEncrypted).map(ParentClass::getName).collect(Collectors.joining(", ")));

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
