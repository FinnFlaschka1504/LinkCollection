package com.maxMustermannGeheim.linkcollection.Activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.DialogActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.SquareLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.maxMustermannGeheim.linkcollection.Utilities.VersionControl;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private static final String TAG = "Settings";

    public static final String SHARED_PREFERENCES_SETTINGS = "SHARED_PREFERENCES_SETTINGS";

    //    public static final String SETTING_SHOPPING_LIST_SORT = "SETTING_SHOPPING_LIST_SORT";
//    public static final String SETTING_SHOPPING_LIST_HIERARCHY = "SETTING_SHOPPING_LIST_HIERARCHY";
//    public static final String SETTING_FINANCES_SWAP_LABLES = "SETTING_FINANCES_SWAP_LABLES";
//    public static final String LAST_VERSION = "LAST_VERSION";
//    public static final String SETTING_OTHERS_USER = "SETTING_OTHERS_USER";
//    public static final String SETTING_OTHERS_DARK_MODE = "SETTING_OTHERS_DARK_MODE";
    public static final String SETTING_VIDEO_ASK_FOR_GENRE_IMPORT = "SETTING_VIDEO_ASK_FOR_GENRE_IMPORT";
    public static final String SETTING_SHOW_ASK_FOR_GENRE_IMPORT = "SETTING_SHOW_ASK_FOR_GENRE_IMPORT";

    public static final String SETTING_VIDEO_SHOW_RELEASE = "SETTING_VIDEO_SHOW_RELEASE";
    public static final String SETTING_VIDEO_SHOW_AGE_RATING = "SETTING_VIDEO_SHOW_AGE_RATING";
    public static final String SETTING_VIDEO_SHOW_LENGTH = "SETTING_VIDEO_SHOW_LENGTH";
    public static final String SETTING_VIDEO_TMDB_SEARCH = "SETTING_VIDEO_AUTO_SEARCH";
    public static final String SETTING_VIDEO_LOAD_CAST_AND_STUDIOS = "SETTING_VIDEO_LOAD_CAST_AND_STUDIOS";
    public static final String SETTING_VIDEO_WEB_SHORTCUT = "SETTING_VIDEO_WEB_SHORTCUT";
    public static final String SETTING_VIDEO_QUICK_SEARCH = "SETTING_VIDEO_SHOW_SEARCH";
    public static final String SETTING_VIDEO_SHOW_COLLECTIONS = "SETTING_VIDEO_SHOW_COLLECTIONS";
    public static final String SETTING_VIDEO_SHOW_IMAGES = "SETTING_VIDEO_SHOW_IMAGES";
    public static final String SETTING_VIDEO_SCROLL = "SETTING_VIDEO_SCROLL";
    public static final String SETTING_VIDEO_CLICK_MODE = "SETTING_VIDEO_CLICK_MODE";
    public static final String SETTING_VIDEO_WARN_EMPTY_URL = "SETTING_VIDEO_WARN_EMPTY_URL";

    public static final String SETTING_SHOW_EPISODE_PREVIEW = "SETTING_SHOW_EPISODE_PREVIEW";

    public static final String SETTING_SPACE_SHOWN_ = "SETTING_SPACE_SHOWN_";
    public static final String SETTING_SPACE_NAMES_ = "SETTING_SPACE_NAMES_";
    public static final String SETTING_SPACE_ORDER = "SETTING_SPACE_ORDER";
    public static final String SETTING_SPACE_ENCRYPTED_ = "SETTING_SPACE_ENCRYPTED_";
    public static final String SETTING_SPACE_ENCRYPTION_PASSWORD = "SETTING_SPACE_ENCRYPTION_PASSWORD";
    public static final String SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD = "passwort";

    public static final String LAST_VERSION = "LAST_VERSION";
    public static final String UPDATE_FILE_NAME = "UPDATE_FILE_NAME";

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
    Button settings_others_showShortcuts;
    Button settings_others_checkForUpdate;
    TextView settings_others_version;
    TextView settings_others_changeLog;

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
        settingsMap.put(SETTING_VIDEO_SHOW_AGE_RATING, "true");
        settingsMap.put(SETTING_VIDEO_SHOW_LENGTH, "true");
        settingsMap.put(SETTING_VIDEO_TMDB_SEARCH, "true");
        settingsMap.put(SETTING_VIDEO_WEB_SHORTCUT, "true");
        settingsMap.put(SETTING_SPACE_ENCRYPTION_PASSWORD, SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD);
        settingsMap.put(LAST_VERSION, "1.0");
        settingsMap.put(UPDATE_FILE_NAME, "");
        settingsMap.put(SETTING_VIDEO_ASK_FOR_GENRE_IMPORT, "true");
        settingsMap.put(SETTING_SHOW_ASK_FOR_GENRE_IMPORT, "true");
        settingsMap.put(SETTING_VIDEO_LOAD_CAST_AND_STUDIOS, "true");
        settingsMap.put(SETTING_VIDEO_QUICK_SEARCH, "0");
        settingsMap.put(SETTING_VIDEO_SHOW_COLLECTIONS, "true");
        settingsMap.put(SETTING_VIDEO_SHOW_IMAGES, "true");
        settingsMap.put(SETTING_VIDEO_SCROLL, "true");
        settingsMap.put(SETTING_VIDEO_CLICK_MODE, "0");
        settingsMap.put(SETTING_VIDEO_WARN_EMPTY_URL, "true");
        settingsMap.put(SETTING_SHOW_EPISODE_PREVIEW, "1");
    }

    public static boolean changeSetting(String key, String newValue) {
        if (!settingsMap.containsKey(key))
            return false;
        settingsMap.replace(key, newValue);
        saveSettings();
        return true;
    }

    public static String getSingleSetting(Context context, String key) {
        String setting = context.getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE).getString(key, null);
        if (setting == null) {
            if (settingsMap.containsKey(key))
                return settingsMap.get(key);
        }
        return setting;
    }

    public static Boolean getSingleSetting_boolean(Context context, String key) {
        return Boolean.parseBoolean(getSingleSetting(context, key));
    }

    public static int getSingleSetting_int(Context context, String key) {
        return Integer.parseInt(getSingleSetting(context, key));
    }

    private static void saveSettings() {
        SharedPreferences.Editor editor = mySPR_settings.edit();

        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        editor.commit();
    }

    public static void applySpacesList(AppCompatActivity context) {
        if (!allSpaces.isEmpty())
            return;

        allSpaces.add(new Space(context.getString(R.string.bottomMenu_video), context.getString(R.string.bottomMenu_videos)).setActivity(VideoActivity.class).setItemId(Space.SPACE_VIDEO).setIconId(R.drawable.ic_videos).setFragmentLayoutId(R.layout.main_fragment_videos)
                .setKey(Database.VIDEOS)
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
                    Boolean showCollection = getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_COLLECTIONS);
                    if (showCollection)
                        ((TextView) view.findViewById(R.id.main_collectionCount)).setText(String.valueOf(database.collectionMap.size()));
                    view.findViewById(R.id.main_collection_layout).setVisibility(showCollection ? View.VISIBLE : View.GONE);
                })
                .setAssociatedClasses(Video.class, Darsteller.class, Studio.class, Genre.class, UrlParser.class)
                .setSettingsDialog(new Utility.Triple<>(R.layout.dialog_settings_video, (customDialog, view, space) -> {
                    AppCompatActivity settingsContext = (AppCompatActivity) ((ContextThemeWrapper) customDialog.getDialog().getContext()).getBaseContext();

                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_overview_images)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_IMAGES));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_overview_scroll)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SCROLL));
                    ((Spinner) view.findViewById(R.id.dialogSettingsVideo_clickMode)).setSelection(getSingleSetting_int(context, SETTING_VIDEO_CLICK_MODE));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_showRelease)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_RELEASE));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_showAgeRating)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_AGE_RATING));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_showLength)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_LENGTH));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_autoSearch)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_TMDB_SEARCH));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_tmdbShortcut)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_WEB_SHORTCUT));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_loadCastAndStudios)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_LOAD_CAST_AND_STUDIOS));
                    ((Spinner) view.findViewById(R.id.dialogSettingsVideo_more_showSearch)).setSelection(Integer.parseInt(getSingleSetting(context, SETTING_VIDEO_QUICK_SEARCH)));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_more_showCollections)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_SHOW_COLLECTIONS));
                    ((Switch) view.findViewById(R.id.dialogSettingsVideo_edit_warnEmptyURL)).setChecked(getSingleSetting_boolean(context, SETTING_VIDEO_WARN_EMPTY_URL));

                    view.findViewById(R.id.dialogSettingsVideo_more_findImages).setOnClickListener(v -> {
                        Utility.GenericInterface<String> showResult = s -> {
                            CustomDialog.Builder(settingsContext)
                                    .setTitle("Ergebnis")
                                    .setEdit(new CustomDialog.EditBuilder().setHint("Ergebnis-Url").setText(s))
                                    .show();
                        };

                        CustomDialog.Builder(settingsContext)
                                .setTitle("Bilder Finden")
                                .addButton("Auf Webseite", customDialog1 -> {
                                    CustomDialog[] internetDialog = {null};
                                    CustomDialog.Builder(settingsContext)
                                            .setTitle("Webseiten URL Eingeben")
                                            .setEdit(new CustomDialog.EditBuilder().setHint("URL").setRegEx(Utility.urlPattern))
                                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog2 -> {
                                                String url = customDialog2.getEditText();
                                                Utility.showInternetDialog(settingsContext, url, internetDialog, (customDialog3, s) -> {
//                                                    Toast.makeText(settingsContext, s, Toast.LENGTH_SHORT).show();
                                                    showResult.runGenericInterface(s);
                                                }, null);
                                            }, false)
                                            .disableLastAddedButton()
                                            .show();
                                })
                                .addButton("In Datei", customDialog1 -> {
                                    ActivityResultHelper.addFileChooserRequest(settingsContext, "text/*", intent -> {
                                        String text = Utility.getTextFromUri(settingsContext, intent.getData()).toString();
                                        CustomList<String> list = Utility.getImageUrlsFromText(text);
                                        Utility.showSelectImageDialog(settingsContext, list, showResult);
                                    });
                                })
                                .enableStackButtons()
                                .show();

                    });

                    ((TextView) view.findViewById(R.id.dialogSettingsVideo_edit_parseUrl_added)).setText(database.urlParserMap.values().stream().map(UrlParser::getName).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialogSettingsVideo_more_parseUrl_select).setOnClickListener(v -> {

                        CustomRecycler<CustomRecycler.Expandable<UrlParser>> customRecycler = new CustomRecycler<CustomRecycler.Expandable<UrlParser>>(context)
                                .setExpandableHelper(customRecycler1 -> customRecycler1.new ExpandableHelper<UrlParser>(R.layout.list_item_url_parser, (customRecycler2, itemView, urlParser, expanded) -> {
                                    ((TextView) itemView.findViewById(R.id.listItem_urlParser_name)).setText(urlParser.getName());
                                    ((TextView) itemView.findViewById(R.id.listItem_urlParser_codeType)).setText(urlParser.getType().getName() + ":");
                                    TextView listItem_urlParser_code = itemView.findViewById(R.id.listItem_urlParser_code);
                                    listItem_urlParser_code.setText(urlParser.getCode());
                                    listItem_urlParser_code.setSingleLine(!expanded);
                                    if (Utility.stringExists(urlParser.getThumbnailCode())) {
                                        TextView listItem_urlParser_thumbnailCode = itemView.findViewById(R.id.listItem_urlParser_thumbnailCode);
                                        listItem_urlParser_thumbnailCode.setText(urlParser.getThumbnailCode());
                                        listItem_urlParser_thumbnailCode.setSingleLine(!expanded);
                                        itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.VISIBLE);
                                    } else
                                        itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.GONE);
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

                    view.findViewById(R.id.dialogSettingsVideo_more_importGenres).setOnClickListener(v -> {
                        Utility.importTmdbGenre(settingsContext, false, true);
                        context.setResult(RESULT_OK);
                    });

                    view.findViewById(R.id.dialogSettingsVideo_more_refreshVideos).setOnClickListener(v -> {
                        CustomDialog.Builder(settingsContext)
                                .setTitle("Videos Aktualisieren")
                                .setText("Möchtest du wirklich alle Videos aktualisieren? Das kann einige Zeit dauern")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON,customDialog1 -> {
                                        Utility.updateVideos(settingsContext, new CustomList<>(database.videoMap.values()));
                                })
                                .show();
                    });
                }, new Space.OnClick() {
                    @Override
                    public void runOnClick(CustomDialog customDialog, Space space) {
                        changeSetting(SETTING_VIDEO_SHOW_IMAGES, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_overview_images)).isChecked()));
                        changeSetting(SETTING_VIDEO_SCROLL, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_overview_scroll)).isChecked()));
                        changeSetting(SETTING_VIDEO_CLICK_MODE, String.valueOf(((Spinner) customDialog.findViewById(R.id.dialogSettingsVideo_clickMode)).getSelectedItemPosition()));
                        changeSetting(SETTING_VIDEO_SHOW_RELEASE, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_showRelease)).isChecked()));
                        changeSetting(SETTING_VIDEO_SHOW_AGE_RATING, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_showAgeRating)).isChecked()));
                        changeSetting(SETTING_VIDEO_SHOW_LENGTH, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_showLength)).isChecked()));
                        changeSetting(SETTING_VIDEO_TMDB_SEARCH, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_autoSearch)).isChecked()));
                        changeSetting(SETTING_VIDEO_WEB_SHORTCUT, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_tmdbShortcut)).isChecked()));
                        changeSetting(SETTING_VIDEO_LOAD_CAST_AND_STUDIOS, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_loadCastAndStudios)).isChecked()));
                        changeSetting(SETTING_VIDEO_QUICK_SEARCH, String.valueOf(((Spinner) customDialog.findViewById(R.id.dialogSettingsVideo_more_showSearch)).getSelectedItemPosition()));
                        changeSetting(SETTING_VIDEO_SHOW_COLLECTIONS, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_more_showCollections)).isChecked()));
                        changeSetting(SETTING_VIDEO_WARN_EMPTY_URL, String.valueOf(((Switch) customDialog.findViewById(R.id.dialogSettingsVideo_edit_warnEmptyURL)).isChecked()));
                        Toast.makeText(context, space.getName() + " Einstellungen gespeichert", Toast.LENGTH_SHORT).show();
                    }
                })));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_show), context.getString(R.string.bottomMenu_shows)).setActivity(ShowActivity.class).setItemId(Space.SPACE_SHOW).setIconId(R.drawable.ic_shows).setFragmentLayoutId(R.layout.main_fragment_shows)
                .setKey(Database.SHOWS)
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
                .setSettingsDialog(new Utility.Triple<>(R.layout.dialog_settings_show, (customDialog, view, space) -> {
                    ((Spinner) view.findViewById(R.id.dialogSettingsShow_episodes_preview)).setSelection(Integer.parseInt(getSingleSetting(context, SETTING_SHOW_EPISODE_PREVIEW)));
                    view.findViewById(R.id.dialogSettingsShow_episodes_update).setOnClickListener(v -> {
                        com.finn.androidUtilities.CustomList<Show.Episode> episodes = new com.finn.androidUtilities.CustomList<>(Utility.concatenateCollections(Utility.concatenateCollections(database.showMap.values(), Show::getSeasonList), season -> season.getEpisodeMap().values())).filter(episode -> !Utility.stringExists(episode.getImdbId()), false);

//                        episodes.forEach(episode -> {
//                            if (episode.getImdbId() != null && episode.getImdbId().equals("null"))
//                                episode.setImdbId(null);
//                        });
//
//                        Database.saveAll();
//                        if (true)
//                            return;

                        int length = episodes.size();
                        final int[] doneCount = {0};

                        final Show.Episode[] currentEpisode = {episodes.getFirst()};
                        final Runnable[] loadDetails = {null};

                        CustomDialog statusDialog = CustomDialog.Builder(view.getContext())
                                .setTitle("Status")
                                .setText("Fortschritt: 0/" + length)
                                .addButton("Fertig")
                                .markLastAddedButtonAsActionButton()
                                .hideLastAddedButton()
                                .addOnDialogDismiss(customDialog1 -> {
                                    loadDetails[0] = () -> {
                                    };
                                })
                                .show();

                        Runnable updateStatus = () -> {
                            statusDialog.setText("Fortschritt: " + doneCount[0] + "/" + length);

                            if (doneCount[0] < length) {
                                currentEpisode[0] = episodes.next(currentEpisode[0], false);
                                loadDetails[0].run();
                            }
                            else {
                                Database.saveAll();
                                Toast.makeText(context, "Aktualisierung abgeschlossen", Toast.LENGTH_SHORT).show();
                                statusDialog.getActionButton().setVisibility(View.VISIBLE);
                            }
                        };

                        loadDetails[0] = () -> {
                            Utility.getImdbIdFromTmdbId(context, currentEpisode[0].getTmdbId(), "episode", s -> {
                                currentEpisode[0].setImdbId(s);
                                doneCount[0]++;
                                updateStatus.run();
                            });
                        };

                        loadDetails[0].run();

                    });
                    view.findViewById(R.id.dialogSettingsShow_edit_importGenres).setOnClickListener(v -> {
                        Utility.importTmdbGenre(customDialog.getDialog().getContext(), false, false);
                        context.setResult(RESULT_OK);
                    });
                }, new Space.OnClick() {
                    @Override
                    public void runOnClick(CustomDialog customDialog, Space space) {
                        changeSetting(SETTING_SHOW_EPISODE_PREVIEW, String.valueOf(((Spinner) customDialog.findViewById(R.id.dialogSettingsShow_episodes_preview)).getSelectedItemPosition()));
                        Toast.makeText(context, space.getName() + " Einstellungen gespeichert", Toast.LENGTH_SHORT).show();
                    }
                })));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_knowledge), context.getString(R.string.bottomMenu_knowledge)).setActivity(KnowledgeActivity.class).setItemId(Space.SPACE_KNOWLEDGE).setIconId(R.drawable.ic_knowledge).setFragmentLayoutId(R.layout.main_fragment_knowledge)
                .setKey(Database.KNOWLEDGE)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_knowledge_label)).setText(space.getPlural());
                    ((TextView) view.findViewById(R.id.main_knowledge_Count)).setText(String.valueOf(database.knowledgeMap.size()));
                    ((TextView) view.findViewById(R.id.main_knowledge_categoryCount)).setText(String.valueOf(database.knowledgeCategoryMap.size()));
                })
                .setAssociatedClasses(Knowledge.class, KnowledgeCategory.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_mediaSing), context.getString(R.string.bottomMenu_media)).setActivity(MainActivity.class).setItemId(Space.SPACE_MEDIA).setIconId(R.drawable.ic_media).setFragmentLayoutId(R.layout.main_fragment_media)
                .setKey(Database.MEDIA)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_media_count)).setText("" + database.mediaMap.size());
                    ((TextView) view.findViewById(R.id.main_media_eventCount)).setText("" + database.mediaEventMap.size());
                    ((TextView) view.findViewById(R.id.main_media_personCount)).setText("" + database.mediaPersonMap.size());
                    ((TextView) view.findViewById(R.id.main_media_categoryCount)).setText("" + ParentClass_Tree.getAllCount(CategoriesActivity.CATEGORIES.MEDIA_CATEGORY));
                    ((TextView) view.findViewById(R.id.main_media_tagCount)).setText("" + database.mediaTagMap.size());
                })
                .setAssociatedClasses(Media.class, MediaPerson.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_owe), context.getString(R.string.bottomMenu_owe)).setActivity(OweActivity.class).setItemId(Space.SPACE_OWE).setIconId(R.drawable.ic_euro).setFragmentLayoutId(R.layout.main_fragment_owe)
                .setKey(Database.OWE)
                .setSetLayout((space, view) -> {
                    ((TextView) view.findViewById(R.id.main_owe_label)).setText(space.getPlural());
                    ((TextView) view.findViewById(R.id.main_owe_countAll)).setText(String.valueOf(database.oweMap.values().stream().filter(Owe::isOpen).count()));
                    ((TextView) view.findViewById(R.id.main_owe_countPerson)).setText(String.valueOf(database.personMap.size()));
                })
                .setAssociatedClasses(Owe.class, Person.class)
                .setSettingsDialog(null));
        allSpaces.add(new Space(context.getString(R.string.bottomMenu_joke), context.getString(R.string.bottomMenu_jokes)).setActivity(JokeActivity.class).setItemId(Space.SPACE_JOKE).setIconId(R.drawable.ic_jokes).setFragmentLayoutId(R.layout.main_fragment_joke)
                .setKey(Database.JOKE)
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

        // ToDo: wenn geändert doppelclick zum dismiss

        CustomDialog.Builder(context)
                .setTitle("URL-Parser " + (oldUrlParser == null ? "Hinzufügen" : "Bearbeiten"))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .setView(R.layout.dialog_edit_or_add_url_parser)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout dialog_editOrAdd_urlParser_name_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_name_layout);
                    TextInputLayout dialog_editOrAdd_urlParser_url_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_url_layout);
                    TextInputLayout dialog_editOrAdd_urlParser_code_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_code_layout);
                    TextInputLayout dialog_editOrAdd_urlParser_thumbnailCode_layout = view.findViewById(R.id.dialog_editOrAdd_urlParser_thumbnailCode_layout);
                    Spinner dialog_editOrAdd_urlParser_type = view.findViewById(R.id.dialog_editOrAdd_urlParser_type);
                    TextView dialog_editOrAdd_urlParser_javaVariables = view.findViewById(R.id.dialog_editOrAdd_urlParser_javaVariables);
                    TextView dialog_editOrAdd_urlParser_graphVariables = view.findViewById(R.id.dialog_editOrAdd_urlParser_graphVariables);
                    dialog_editOrAdd_urlParser_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            UrlParser.TYPE type = UrlParser.TYPE.getTypeByIndex(dialog_editOrAdd_urlParser_type.getSelectedItemPosition());
                            editUrlParser.setType(type);
                            if (type == UrlParser.TYPE.JAVA) {
                                dialog_editOrAdd_urlParser_javaVariables.setVisibility(View.VISIBLE);
                                dialog_editOrAdd_urlParser_graphVariables.setVisibility(View.GONE);
                            } else if (type == UrlParser.TYPE.GRAPH) {
                                dialog_editOrAdd_urlParser_javaVariables.setVisibility(View.GONE);
                                dialog_editOrAdd_urlParser_graphVariables.setVisibility(View.VISIBLE);
                            } else {
                                dialog_editOrAdd_urlParser_javaVariables.setVisibility(View.GONE);
                                dialog_editOrAdd_urlParser_graphVariables.setVisibility(View.GONE);
                            }
                            dialog_editOrAdd_urlParser_code_layout.setHint(editUrlParser.getType().getName() + "-Code");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    if (Utility.stringExists(editUrlParser.getName())) {
                        dialog_editOrAdd_urlParser_name_layout.getEditText().setText(editUrlParser.getName());
                        dialog_editOrAdd_urlParser_url_layout.getEditText().setText(editUrlParser.getExampleUrl());
                        dialog_editOrAdd_urlParser_code_layout.getEditText().setText(editUrlParser.getCode());
                        dialog_editOrAdd_urlParser_thumbnailCode_layout.getEditText().setText(editUrlParser.getThumbnailCode());
                        dialog_editOrAdd_urlParser_type.setSelection(editUrlParser.getType().getIndex());
                    }


                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper((Button) customDialog.getActionButton().getButton(), dialog_editOrAdd_urlParser_name_layout, dialog_editOrAdd_urlParser_url_layout, dialog_editOrAdd_urlParser_code_layout);
                    helper.setValidation(dialog_editOrAdd_urlParser_url_layout, (validator, text) -> {
                        if (text.isEmpty())
                            validator.setWarning("Keine Beispiel-Url eingegeben");
                        else if (Utility.isUrl(text))
                            validator.setValid();
                        else
                            validator.setInvalid("Eine URL eingeben!");
                    });

                    helper.setInputType(dialog_editOrAdd_urlParser_code_layout, Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE);
                })
                .addButton("Testen", customDialog -> {
                    String name = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_name)).getText().toString();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_url)).getText().toString();
                    final String[] code = {((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_code)).getText().toString()};
                    String thumbnailCode = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_thumbnailCode)).getText().toString();

                    editUrlParser.setExampleUrl(url).setCode(code[0]).setThumbnailCode(Utility.stringExists(thumbnailCode)? thumbnailCode : null).setName(name);

                    final int[] count = {(Utility.stringExists(code[0]) ? 1 : 0) + (Utility.stringExists(thumbnailCode) ? 1 : 0)};
                    final String[] resultName = {""};
                    final String[] resultThumbnail = {""};

                    Runnable showResult = () -> {
                        CustomDialog.Builder(context)
                                .setTitle("Ergebnis")
                                .setText(Utility.stringExists(resultName[0]) ? resultName[0] : "--Kein Ergebnis--")
                                .addOptionalModifications(customDialog1 -> {
                                    String path = resultThumbnail[0];
                                    if (!Utility.stringExists(path))
                                        return;
                                    if (path.matches(CategoriesActivity.pictureRegex)) {
                                        ImageView imageView = new ImageView(context);
                                        Utility.setDimensions(imageView, true, false);
                                        imageView.setAdjustViewBounds(true);
                                        Utility.loadUrlIntoImageView(context, imageView, (path.contains("https") ? "" : "https://image.tmdb.org/t/p/w92/") + path, null);
                                        imageView.setPadding(Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16));
                                        customDialog1.setView(imageView);

                                    } else {
                                        TextView textView = new TextView(context);
                                        textView.setText(resultThumbnail[0]);
                                        textView.setPadding(Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16));
                                        customDialog1.setView(textView);
                                    }
                                })
                                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                                .show();
                    };

                    editUrlParser.parseUrl(context, url, s -> {
                        resultName[0] = s;
                        if (count[0] <= 1) {
                            showResult.run();
                            count[0]--;
                        } else
                            count[0]--;
                    }, s -> {
                        resultThumbnail[0] = s;
                        if (count[0] <= 1) {
                            showResult.run();
                            count[0]--;
                        } else
                            count[0]--;
                    });

                }, false)
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String name = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_name)).getText().toString();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_url)).getText().toString();
                    String code = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_code)).getText().toString();
                    String thumbnailCode = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_thumbnailCode)).getText().toString();
                    UrlParser.TYPE type = UrlParser.TYPE.getTypeByIndex(((Spinner) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_type)).getSelectedItemPosition());

                    if (oldUrlParser != null) {
                        oldUrlParser.setExampleUrl(url).setCode(code).setThumbnailCode(Utility.stringExists(thumbnailCode)? thumbnailCode : null).setType(type).setName(name);
                    } else {
                        UrlParser urlParser1 = new UrlParser(name).setExampleUrl(url).setCode(code).setType(type);
                        database.urlParserMap.put(urlParser1.getUuid(), urlParser1);
                    }

                    customRecycler.reload();

                    Toast.makeText(context, (Database.saveAll_simple() ? "URL-Parser Gespeichert" : "Nichts zum speichern"), Toast.LENGTH_SHORT).show();
                })
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String name = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_name)).getText().toString().trim();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_url)).getText().toString().trim();
                    String code = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_code)).getText().toString().trim();
                    String thumbnailCode = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_thumbnailCode)).getText().toString().trim();
                    int type = ((Spinner) customDialog.findViewById(R.id.dialog_editOrAdd_urlParser_type)).getSelectedItemPosition();

                    if (oldUrlParser == null)
                        return !name.isEmpty() || !url.isEmpty() || !code.isEmpty() || !thumbnailCode.isEmpty() || type != 0;
                    else
                        return !name.equals(oldUrlParser.getName()) || !url.equals(oldUrlParser.getExampleUrl()) || !code.equals(oldUrlParser.getCode()) || !CustomUtility.boolOr(thumbnailCode, oldUrlParser.getThumbnailCode(), "") || type != oldUrlParser.getType().getIndex() || !editUrlParser.equals(oldUrlParser);
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        Utility.applyExpendableToolbar_scrollView(this, findViewById(R.id.scrollView), appBarLayout);


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
        settings_others_changeDatabaseCode = findViewById(R.id.settings_others_datebaseSettings);
        settings_others_activeSpaces = findViewById(R.id.settings_others_activeSpaces);
        settings_others_spaceSelector = findViewById(R.id.settings_others_spaceSelector);
        settings_others_encryptedSpaces = findViewById(R.id.settings_others_encryptedSpaces);
        settings_others_encryptedSelector = findViewById(R.id.settings_others_encryptedSelector);
        settings_others_showShortcuts = findViewById(R.id.settings_others_showShortcuts);
        settings_others_version = findViewById(R.id.settings_others_version);
        settings_others_checkForUpdate = findViewById(R.id.settings_others_checkForUpdate);
        settings_others_changeLog = findViewById(R.id.settings_others_changeLog);

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
                .addSubOnClickListener(R.id.list_spaceSetting_lock, (customRecycler, itemView, space, index) -> settings_others_encryptedSelector.callOnClick())
                .enableDivider()
                .disableCustomRipple()
                .removeLastDivider()
                .setOnClickListener((customRecycler, itemView, space, index) -> space.showSettingsDialog(this))
                .setDividerMargin_inDp(16)
                .hideOverscroll()
                .generate();


        settings_others_databaseCode.setText(Database.databaseCode);


        settings_others_activeSpaces.setText(
                allSpaces.stream().filter(Space::isShown).map(Space::getPlural).collect(Collectors.joining(", ")));


        settings_others_encryptedSpaces.setText(
                allSpaces.stream().filter(Space::isEncrypted).map(Space::getPlural).collect(Collectors.joining(", ")));


        settings_others_version.setText(VersionControl.getVersion(this));

    }

    private void setListeners() {
        SharedPreferences mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        Runnable changeCode = () -> {
            CustomDialog.Builder(this)
                    .setTitle("Datenbank Wechseln")
                    .setView(R.layout.dialog_database_login)
                    .setSetViewContent((customDialog, view, reload) -> {
                        TextInputLayout dialog_databaseLogin_name_layout = customDialog.findViewById(R.id.dialog_databaseLogin_name_layout);
                        TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);
                        customDialog.findViewById(R.id.dialog_databaseLogin_passwordSecond_layout).setVisibility(View.GONE);

                        Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
                        helper.addValidator(dialog_databaseLogin_name_layout, dialog_databaseLogin_passwordFirst_layout)
                                .defaultDialogValidation(customDialog)
//                                .setValidation(dialog_databaseLogin_passwordSecond_layout, (validator, text) -> {
//                                    if (Utility.stringExists(text) && !text.equals(dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim()))
//                                        validator.setInvalid("Die Passwörter müssen gleich sein");
//                                })
                                .addActionListener(dialog_databaseLogin_passwordFirst_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                    View button = customDialog.getActionButton().getButton();
                                    if (button.isEnabled())
                                        button.callOnClick();
                                }, Helpers.TextInputHelper.IME_ACTION.DONE);

                        dialog_databaseLogin_name_layout.requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
                    })
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
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
                                            customDialog.dismiss();

                                            mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                                            Database.getInstance(mySPR_daten, database1 -> {
                                            }, true);

                                            Database.databaseCall_write(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString(), Database.databaseCode, Database.PASSWORD);
                                        })
                                        .show();
                            } else if (Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString().equals(dataSnapshot.getValue())) {
                                List<String> encryptedSpaces = new ArrayList<>();
                                Runnable setEncryptedSpaces = () -> {
                                    allSpaces.forEach(space -> space.setEncrypted(encryptedSpaces.contains(space.getKey())));
                                    Settings.saveEncryption();
                                    updateSpaceStatusSettings();
                                };


                                Database.databaseCall_read(dataSnapshot1 -> {
                                    Object value = dataSnapshot1.getValue();
                                    if (value == null) {
                                        setEncryptedSpaces.run();
                                        Settings.resetEncryption();
                                        mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                                        Database.getInstance(mySPR_daten, database1 -> {
                                        }, false);
                                        customDialog.dismiss();
                                    } else if (Utility.hash(Settings.getSingleSetting(this, Settings.SETTING_SPACE_ENCRYPTION_PASSWORD)).equals(((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTION_PASSWORD))) {
                                        if (((HashMap) dataSnapshot1.getValue()).containsKey(Database.ENCRYPTED_SPACES))
                                            encryptedSpaces.addAll((Collection<? extends String>) ((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTED_SPACES));
                                        setEncryptedSpaces.run();

                                        mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                                        Database.getInstance(mySPR_daten, database1 -> {
                                        }, false);
                                        customDialog.dismiss();
                                    } else {
                                        CustomDialog.Builder(this)
                                                .setTitle("Passwort Eingeben")
                                                .setText("Die Datenbank enthält verschlüsselte Bereiche und das hinterlegte Passwort sitmmt nicht mit dem der Datenbank überein.\nBitte das richtige Passwort eingeben.")
                                                .setEdit(new CustomDialog.EditBuilder().setInputType(Helpers.TextInputHelper.INPUT_TYPE.PASSWORD))
                                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                                    String inputPassword = customDialog1.getEditText();
                                                    if (Utility.hash(inputPassword).equals(((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTION_PASSWORD))) {
                                                        Settings.changeSetting(Settings.SETTING_SPACE_ENCRYPTION_PASSWORD, inputPassword);

                                                        if (((HashMap) dataSnapshot1.getValue()).containsKey(Database.ENCRYPTED_SPACES))
                                                            encryptedSpaces.addAll((Collection<? extends String>) ((HashMap) dataSnapshot1.getValue()).get(Database.ENCRYPTED_SPACES));
                                                        setEncryptedSpaces.run();

                                                        mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                                                        Database.getInstance(mySPR_daten, database1 -> {
                                                        }, false);
                                                        customDialog.dismiss();
                                                        customDialog1.dismiss();
                                                    } else
                                                        Toast.makeText(this, "Das Passwort ist Falsch", Toast.LENGTH_SHORT).show();
                                                }, false)
                                                .show();
                                    }
                                }, databaseCode, Database.ENCRYPTION);



                                if (true)
                                    return;

                                mySPR_daten.edit().putString(Database.DATABASE_CODE, databaseCode).commit();
                                Database.getInstance(mySPR_daten, database1 -> {
                                }, false);
                                customDialog.dismiss();
                            } else
                                Toast.makeText(this, "Das Passwort ist falsch", Toast.LENGTH_SHORT).show();
                        }, databaseError -> {
                            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                        }, databaseCode, Database.PASSWORD);

                    }, false)
                    .disableLastAddedButton()
                    .setOnDialogDismiss(customDialog -> settings_others_databaseCode.setText(Database.databaseCode))
                    .show();
        };
        Runnable renameCode = () -> {
            CustomDialog.Builder(this)
                    .setTitle("Datenbank Umbenennen")
                    .standardEdit()
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                    .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                        String oldCode = Database.databaseCode;
                        String newCode = customDialog.getEditText();
                        Database.databaseCall_read(dataSnapshot -> {
                            if (dataSnapshot.getValue() != null) {
                                Toast.makeText(this, "Fehler: Eine Datenbank mit dem Code ist bereits vorhanden", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Database.databaseCall_read(dataSnapshot1 -> {
                                Database.accessChilds(Database.databaseReference, newCode).setValue(dataSnapshot1.getValue(), (databaseError, databaseReference) -> {
                                    if (databaseError != null) {
                                        Toast.makeText(Settings.this, "Fehler: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Toast.makeText(Settings.this, "Verschoben", Toast.LENGTH_SHORT).show();

                                    mySPR_daten.edit().putString(Database.DATABASE_CODE, newCode).commit();
                                    Database.getInstance(mySPR_daten, database1 -> {
                                    }, false);

                                    customDialog.dismiss();

                                    Database.databaseCall_delete(oldCode);
                                });
                            }, oldCode);
                        }, newCode);
                    }, false)
                    .disableLastAddedButton()
                    .show();
        };
        Runnable changePassword = () -> {
            CustomDialog.Builder(this)
                    .setTitle("Passwort Ändern")
                    .setView(R.layout.dialog_database_login)
                    .setSetViewContent((customDialog, view, reload) -> {
                        customDialog.findViewById(R.id.dialog_databaseLogin_name_layout).setVisibility(View.GONE);
                        TextInputLayout dialog_databaseLogin_oldPassword_layout = customDialog.findViewById(R.id.dialog_databaseLogin_oldPassword_layout);
                        dialog_databaseLogin_oldPassword_layout.setVisibility(View.VISIBLE);
                        TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);
                        TextInputLayout dialog_databaseLogin_passwordSecond_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordSecond_layout);

                        Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
                        helper.addValidator(dialog_databaseLogin_oldPassword_layout, dialog_databaseLogin_passwordFirst_layout, dialog_databaseLogin_passwordSecond_layout)
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

                        dialog_databaseLogin_oldPassword_layout.requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
                    })
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                    .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                        TextInputLayout dialog_databaseLogin_oldPassword_layout = customDialog.findViewById(R.id.dialog_databaseLogin_oldPassword_layout);
                        TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);

                        String oldPassword = dialog_databaseLogin_oldPassword_layout.getEditText().getText().toString().trim();
                        String password = dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim();
                        String databaseCode = Database.databaseCode;

                        if (!Utility.stringExists(databaseCode))
                            return;

                        Database.databaseCall_read(dataSnapshot -> {
                            if (dataSnapshot.getValue() == null) {
                                Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                            } else if (Hashing.sha256().hashString(oldPassword, StandardCharsets.UTF_8).toString().equals(dataSnapshot.getValue())) {
                                Database.databaseCall_write(Utility.hash(password), databaseCode, Database.PASSWORD);
                                Toast.makeText(this, "Passwort geändert", Toast.LENGTH_SHORT).show();
                                customDialog.dismiss();
                            } else
                                Toast.makeText(this, "Das Passwort ist falsch", Toast.LENGTH_SHORT).show();
                        }, databaseError -> {
                            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                        }, databaseCode, Database.PASSWORD);

                    }, false)
                    .disableLastAddedButton()
                    .setOnDialogDismiss(customDialog -> settings_others_databaseCode.setText(Database.databaseCode))
                    .show();

        };
        Runnable deleteDatabase = () -> {
            final long[] time = {System.currentTimeMillis()};
            CustomDialog.Builder(this)
                    .setTitle("Datenbank Löschen")
                    .setText("Willst du wirklich die Datenbank löschen?\nEine wiederherstellung der Daten ist aufwändig und nur für ca. 30 Tage möglich!\nMit einem Doppelclick bestätigen und die App startet danach neu.")
                    .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                    .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                        if (System.currentTimeMillis() - time[0] > 300) {
                            Toast.makeText(this, "Doppelclick zum bestätigen", Toast.LENGTH_SHORT).show();
                            time[0] = System.currentTimeMillis();
                            return;
                        }

                        Database.databaseCall_delete(Database.databaseCode);
                        mySPR_daten.edit().clear().commit();

                        Toast.makeText(this, "Datenbank gelöscht", Toast.LENGTH_SHORT).show();
                        customDialog.dismiss();

                        Utility.restartApp(this);
                    }, false)
                    .show();
        };


        settings_others_changeDatabaseCode.setOnClickListener(v -> {
            CustomDialog.Builder(this)
                    .setTitle("Datenbank Einstellungen")
                    .addButton("Datenbank wechseln", customDialog -> changeCode.run())
                    .addButton("Datenbank-Namen umbenennen", customDialog -> renameCode.run())
                    .addButton("Passwort ändern", customDialog -> changePassword.run())
                    .addButton("Datenbank löschen", customDialog -> deleteDatabase.run())
                    .enableTitleBackButton()
                    .disableButtonAllCaps()
                    .enableStackButtons()
                    .show();
        });

        settings_others_spaceSelector.setOnClickListener(v -> {
            CustomDialog.Builder(this)
                    .setTitle("Anzuzeigende Bereiche Auswählen")
                    .setView(new CustomRecycler<Space>(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces)
                            .setSetItemContent((customRecycler, itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isShown());
                            })
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
                            .enableDivider()
                            .disableCustomRipple()
                            .removeLastDivider()
                            .generateRecyclerView())
                    .setOnDialogDismiss(dialog -> updateSpaceStatusSettings())
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK)
                    .show();
        });


        settings_others_encryptedSelector.setOnClickListener(v -> {
            Set<String> spaceSet = allSpaces.stream().filter(Space::isEncrypted).map(Space::getKey).collect(Collectors.toSet());
            HashSet<String> prevSet = new HashSet<>(spaceSet);
            CustomDialog.Builder(this)
                    .setTitle("Zu Verschlüsselnde Bereiche Auswählen")
                    .setView(new CustomRecycler<Space>(this)
                            .setItemLayout(R.layout.list_item_space_shown)
                            .setObjectList(allSpaces.stream().filter(Space::isShown).collect(Collectors.toList()))
                            .setSetItemContent((customRecycler, itemView, space) -> {
                                ((TextView) itemView.findViewById(R.id.list_spaceSetting_name)).setText(space.getPlural());

                                ((CheckBox) itemView.findViewById(R.id.list_spaceSetting_shown)).setChecked(space.isEncrypted());
                            })
                            .setOnClickListener((customRecycler, itemView, space, index) -> {
                                CheckBox list_spaceSetting_shown = itemView.findViewById(R.id.list_spaceSetting_shown);
                                boolean checked = list_spaceSetting_shown.isChecked();

                                list_spaceSetting_shown.setChecked(!checked);

                                if (!checked) {
                                    spaceSet.add(space.getKey());
                                } else {
                                    spaceSet.remove(space.getKey());
                                }

                            })
                            .setDividerMargin_inDp(16)
                            .enableDivider()
                            .disableCustomRipple()
                            .removeLastDivider()
                            .generateRecyclerView())
                    .addButton("Passwort Ändern", customDialog -> {
                        CustomDialog.Builder(this)
                                .setTitle("Passwort Ändern")
                                .setView(R.layout.dialog_database_login)
                                .setSetViewContent((customDialog1, view, reload) -> {
                                    customDialog1.findViewById(R.id.dialog_databaseLogin_name_layout).setVisibility(View.GONE);
                                    TextInputLayout dialog_databaseLogin_oldPassword_layout = customDialog1.findViewById(R.id.dialog_databaseLogin_oldPassword_layout);
                                    dialog_databaseLogin_oldPassword_layout.setVisibility(View.VISIBLE);
                                    TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog1.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);
                                    TextInputLayout dialog_databaseLogin_passwordSecond_layout = customDialog1.findViewById(R.id.dialog_databaseLogin_passwordSecond_layout);

                                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
                                    helper.addValidator(dialog_databaseLogin_oldPassword_layout, dialog_databaseLogin_passwordFirst_layout, dialog_databaseLogin_passwordSecond_layout)
                                            .defaultDialogValidation(customDialog1)
                                            .setValidation(dialog_databaseLogin_passwordSecond_layout, (validator, text) -> {
                                                if (Utility.stringExists(text) && !text.equals(dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim()))
                                                    validator.setInvalid("Die Passwörter müssen gleich sein");
                                            })
                                            .addActionListener(dialog_databaseLogin_passwordSecond_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                                View button = customDialog1.getActionButton().getButton();
                                                if (button.isEnabled())
                                                    button.callOnClick();
                                            }, Helpers.TextInputHelper.IME_ACTION.DONE);

                                    dialog_databaseLogin_oldPassword_layout.requestFocus();
                                    Utility.changeWindowKeyboard(customDialog1.getDialog().getWindow(), true);
                                })
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                .setText("Das Standardpasswort lautet: \"" + Settings.SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD + "\"")
                                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                    TextInputLayout dialog_databaseLogin_oldPassword_layout = customDialog1.findViewById(R.id.dialog_databaseLogin_oldPassword_layout);
                                    TextInputLayout dialog_databaseLogin_passwordFirst_layout = customDialog1.findViewById(R.id.dialog_databaseLogin_passwordFirst_layout);

                                    String oldPassword = dialog_databaseLogin_oldPassword_layout.getEditText().getText().toString().trim();
                                    String password = dialog_databaseLogin_passwordFirst_layout.getEditText().getText().toString().trim();
                                    String databaseCode = Database.databaseCode;

                                    if (!Utility.stringExists(databaseCode))
                                        return;

                                    if (!getSingleSetting(this, SETTING_SPACE_ENCRYPTION_PASSWORD).equals(oldPassword)) {
                                        Toast.makeText(this, "Das alte Passwort ist falsch", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    Database.databaseCall_write(Utility.hash(password), Database.databaseCode, Database.ENCRYPTION, Database.ENCRYPTION_PASSWORD);

                                    changeSetting(SETTING_SPACE_ENCRYPTION_PASSWORD, password);
                                    Toast.makeText(this, "Passwort wurde zu '" + password + "' geändert", Toast.LENGTH_SHORT).show();
                                    Database.saveAll(true);
                                    customDialog1.dismiss();
                                }, false)
                                .disableLastAddedButton()
                                .setOnDialogDismiss(customDialog2 -> settings_others_databaseCode.setText(Database.databaseCode))
                                .show();

                    }, false)
                    .alignPreviousButtonsLeft()
                    .colorLastAddedButton()
                    .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                        CustomUtility.isOnline(this, () -> {
                            allSpaces.forEach(space -> space.setEncrypted(spaceSet.contains(space.getKey())));

                            updateSpaceStatusSettings();
                            if (!prevSet.equals(spaceSet)) {
                                Database.saveAll(true);

                                if (spaceSet.isEmpty()) {
                                    Database.databaseCall_delete(Database.databaseCode, Database.ENCRYPTION);
                                } else {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put(Database.ENCRYPTION_PASSWORD, Utility.hash(getSingleSetting(this, SETTING_SPACE_ENCRYPTION_PASSWORD)));
                                    map.put(Database.ENCRYPTED_SPACES, new ArrayList<>(spaceSet));
                                    Database.databaseCall_write(map, Database.databaseCode, Database.ENCRYPTION);
                                }
                            }

                            customDialog.dismiss();
                        });
                    }, false)
                    .show();
        });

        settings_others_showShortcuts.setOnClickListener(v ->
                CustomDialog.Builder(this)
                        .setTitle("Shortcuts-Hinzufügen")
                        .enableTitleBackButton()
                        .setView(R.layout.dialog_settings_show_shortcuts)
                        .setSetViewContent((customDialog, view, reload) -> {
                            view.findViewById(R.id.dialog_settingsShowShortcuts_randomVideo).setOnClickListener(v1 -> {
                                Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "RandomVideoShortcut")
                                        .setShortLabel("Zufälliges Video")
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_videos))
                                        .setIntent(new Intent(this, VideoActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true))
                                        .build(), null));
                            });
                            view.findViewById(R.id.dialog_settingsShowShortcuts_randomKnowledge).setOnClickListener(v1 -> {
                                Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "RandomKnowledgeShortcut")
                                        .setShortLabel("Zufälliges Wissen")
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_knowledge))
                                        .setIntent(new Intent(this, KnowledgeActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true))
                                        .build(), null));
                            });
                            view.findViewById(R.id.dialog_settingsShowShortcuts_randomJoke).setOnClickListener(v1 -> {
                                Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "RandomJokeShortcut")
                                        .setShortLabel("Zufälliger Witz")
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_jokes))
                                        .setIntent(new Intent(this, JokeActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true))
                                        .build(), null));
                            });
                            view.findViewById(R.id.dialog_settingsShowShortcuts_random).setOnClickListener(v1 -> {
                                Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "RandomShortcut")
                                        .setShortLabel("Zufällig Auswahl")
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_random_grey))
                                        .setIntent(new Intent(this, DialogActivity.class).setAction(DialogActivity.ACTION_RANDOM))
                                        .build(), null));
                            });

                            view.findViewById(R.id.dialog_settingsShowShortcuts_nextEpisode).setOnClickListener(v1 -> {
                                Utility.ifNotNull(getSystemService(ShortcutManager.class), shortcutManager -> shortcutManager.requestPinShortcut(new ShortcutInfo.Builder(this, "NextEpisodeShortcut")
                                        .setShortLabel("Nächste Folge")
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_play_next))
                                        .setIntent(new Intent(this, ShowActivity.class).setAction(ShowActivity.ACTION_NEXT_EPISODE))
                                        .build(), null));
                            });
                        })
                        .show()
        );

        settings_others_checkForUpdate.setOnClickListener(v -> {
            if (VersionControl.hasPermissions(this, true))
                updateApp();
        });


        settings_others_changeLog.setOnClickListener(v -> VersionControl.showChangeLog(this, true));
    }

    public static class Space extends ParentClass {
        public static CustomList<Space> allSpaces = new CustomList<>();
        public static final int SPACE_MORE = 0;
        public static final int SPACE_VIDEO = 1;
        public static final int SPACE_KNOWLEDGE = 2;
        public static final int SPACE_OWE = 3;
        public static final int SPACE_JOKE = 4;
        public static final int SPACE_SHOW = 5;
        public static final int SPACE_MEDIA = 6;

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
        private String key;
        public static Space nextMoreSpace;

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

        public String getKey() {
            return key;
        }

        public Space setKey(String key) {
            this.key = key;
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
            // fragment.getView() gibt manchmal null
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

        public boolean isInMore() {
            CustomList<Space> visibleSpaces = allSpaces.filter(Space::isShown, false);
            if (visibleSpaces.size() <= 5)
                return false;
            return visibleSpaces.indexOf(this) >= 4;
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


    //  ------------------------- language ------------------------->
    public static int getIndexByLanguage(Context context, String language){
        String[] array = context.getResources().getStringArray(R.array.languages);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(language))
                return i;
        }
        return 0;
    }

    public static String getLanguageByIndex(Context context, int index){
        if (index == 0) return null;
        return context.getResources().getStringArray(R.array.languages)[index];
    }

    public static String getDefaultLanguage(){
        return "de";
    }
    //  <------------------------- language -------------------------


    //  ------------------------- Export and Import Settings ------------------------->
    private String saveSharedPreferences(SharedPreferences sharedPreferences) {
        File myPath = new File(Environment.getExternalStorageDirectory().toString());
        File myFile = new File(myPath, String.format("SecondMind Settings Export %s.txt", Utility.formatDate("yyyy-MM-dd-HH-mm-ss", new Date())));

        try
        {
            FileWriter fw = new FileWriter(myFile);
            PrintWriter pw = new PrintWriter(fw);

            pw.println(new GsonBuilder().setPrettyPrinting().create().toJson(settingsMap));

            pw.close();
            fw.close();
            
            return myFile.getAbsolutePath();
        }
        catch (Exception e)
        {
            Log.wtf(getClass().getName(), e.toString());
            return null;
        }
    }

    private void exportSettings() {
        String exportPath = saveSharedPreferences(mySPR_settings);
        if (CustomUtility.stringExists(exportPath))
            CustomDialog.Builder(this)
                    .setTitle("Export Erfolgreich")
                    .setText("Die Date wurde in: '" + exportPath + "' gespeichert")
                    .addButton("OK")
                    .addButton("Öffnen", customDialog1 -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(exportPath));
                            intent.setDataAndType(uri, "text/plain");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Datei konnte nicht geöffnet werden", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .markLastAddedButtonAsActionButton()
                    .show();
        else
            Toast.makeText(this, "Fehler beim Exportieren", Toast.LENGTH_SHORT).show();
    }


//    private void importSettings() {
////        CustomDialog.Builder(this)
////                .setTitle("Einstellungen Als Text Einfügen")
////                .setEdit(new CustomDialog.EditBuilder().setHint("Einstellungs-Text einfügen")) //.setInputType(Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE))
//////                .setSetViewContent((customDialog, view, reload) -> {
//////                    ((EditText) customDialog.findViewById(R.id.dialog_custom_edit)).setMaxLines(15);
//////                })
////                .addButton("OK")
////                .addButton("Importieren", customDialog -> {
////                    String text = customDialog.getEditText();
//////                    for (String line : text.split("\n")) {
//////                        String[] split = line.split(":", 2);
//////                        if (settingsMap.containsKey(split[0]))
//////                            settingsMap.put(split[0], split[1]);
//////                    }
////                    settingsMap = new Gson().fromJson(text, TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
////                    saveSettings();
////
////                    Toast.makeText(this, "Einstellungen erfolgreich importiert", Toast.LENGTH_SHORT).show();
////
////                    Utility.restartApp(this);
////                })
////                .colorLastAddedButton()
////                .show();
////
////        if (true)
////            return;
//        ActivityResultHelper.addFileChooserRequest(this, "text/plain", o -> importSettings(((Intent) o).getData()), o -> Toast.makeText(this, "Abgebrochen", Toast.LENGTH_SHORT).show());
//    }


    private void importSettings(Uri uri) {
        if (uri == null) {
            ActivityResultHelper.addFileChooserRequest(this, "text/plain", o -> importSettings(((Intent) o).getData()), o -> Toast.makeText(this, "Abgebrochen", Toast.LENGTH_SHORT).show());
            return;
        }

        StringBuffer buf = Utility.getTextFromUri(this, uri);

        HashMap<String, String> importedMap = new Gson().fromJson(buf.toString(), TypeToken.getParameterized(HashMap.class, String.class, String.class).getType());
        for (String key : settingsMap.keySet()) {
            if (importedMap.containsKey(key))
                settingsMap.put(key, importedMap.get(key));
        }
        saveSettings();
        Utility.showCenteredToast(this, "Einstellungen erfolgreich importiert\nApp wird neugestartet");
        new Handler().postDelayed(() -> Utility.restartApp(this), 1500);

////        CustomDialog.Builder(this)
////                .setTitle("Result")
////                .setText(buf)
////                .show();
//        if (true)
//            return;
//        File file = new File(Environment.getExternalStorageDirectory().toString(), uri.getPath().split(":")[1]);
//
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                String[] split = line.split(":", 2);
//                if (settingsMap.containsKey(split[0]))
//                    settingsMap.put(split[0], split[1]);
//            }
//            br.close();
//            saveSettings();
//
//            Toast.makeText(this, "Einstellungen erfolgreich importiert\nApp wird neugestartet", Toast.LENGTH_SHORT).show();
//
//            new Handler().postDelayed(() -> Utility.restartApp(this), 1500)
//
//            ;
//        }
//        catch (IOException e) {
//            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
//        }
    }
    //  <------------------------- Export and Import Settings -------------------------


    //  ------------------------- Encryption ------------------------->
    public static void resetEncryption() {
        for (Space space : allSpaces) {
            changeSetting(SETTING_SPACE_ENCRYPTED_ + space.getItemId(), String.valueOf(false));
        }
        changeSetting(SETTING_SPACE_ENCRYPTION_PASSWORD, SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD);
    }

    public static void saveEncryption(){
        for (Space space : allSpaces) {
            changeSetting(SETTING_SPACE_ENCRYPTED_ + space.getItemId(), String.valueOf(space.isEncrypted()));
        }
    }
    //  <------------------------- Encryption -------------------------

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_settings_exportImport:
                CustomDialog.Builder(this)
                        .setTitle("Einstellungen Exportieren/ Importieren")
                        .setText("Möchtest du die Einstellungen Exportieren, oder Importieren?")
                        .addButton("Exportieren", customDialog -> {
                            exportSettings();
                        })
                        .addButton("Importieren", customDialog -> {
                            importSettings(null);
                        })
                        .enableExpandButtons()
                        .show();

                break;

            case android.R.id.home:
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;

        }
        return true;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case ActivityResultHelper.FILE_SELECT_CODE:
//                if (resultCode == RESULT_OK) {
//                    importSettings(data.getData());
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

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


    //  ------------------------- Update ------------------------->
    void updateApp() {
        VersionControl.checkForUpdate(this, true);
//        VersionControl.updateApp(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            if (VersionControl.hasPermissions(this, true))
                updateApp();
        }
    }
    //  <------------------------- Update -------------------------
}
