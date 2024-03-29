package com.maxMustermannGeheim.linkcollection.Activities.Content.Show;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomRecycler.Expandable;
import com.finn.androidUtilities.CustomUtility;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowLabel;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.ImageAdapterItem;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.externalCode.ExternalCode;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.MinDimensionLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.mikhaellopez.lazydatepicker.LazyDatePicker;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSObject;
import org.liquidplayer.javascript.JSValue;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShowActivity extends AppCompatActivity {
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";
    public static final String EXTRA_EPISODE = "EXTRA_EPISODE";
    public static final String ACTION_NEXT_EPISODE = "ACTION_NEXT_EPISODE";
    public static final String EXTRA_NEXT_EPISODE_SELECT = "EXTRA_NEXT_EPISODE_SELECT";
    private final String ADVANCED_SEARCH_CRITERIA_GENRE = "g";
    private final String ADVANCED_SEARCH_CRITERIA_FILTER = "f";
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private Database.OnChangeListener<Object> onChangeListener;
//    private CustomUtility.LogTiming logTiming;

    {
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST
    }

    public enum FILTER_TYPE {
        NAME("Titel"), GENRE("Genre");

        String name;

        FILTER_TYPE() {
        }

        FILTER_TYPE(String name) {
            this.name = name;
        }

        public boolean hasName() {
            return name != null;
        }

        public String getName() {
            return name;
        }
    }

    public enum SHOW_STATUS_FILTER {
        OPEN("Weiterschaubar"), CURRENT("Aktuell"), FINISHED("Abgeschlossen"), UNSEEN("Ungesehen");

        private final String uiText;

        SHOW_STATUS_FILTER(String uiText) {
            this.uiText = uiText;
        }

        public String getUiText() {
            return uiText;
        }

        public static SHOW_STATUS_FILTER getEnum(String text) {
            try {
                return SHOW_STATUS_FILTER.valueOf(text);
            } catch (IllegalArgumentException ignored) {
            }
            return Arrays.stream(values()).filter(filter -> filter.getUiText().equals(text)).findFirst().orElse(null);
        }
    }

    private Database database;
    private SharedPreferences mySPR_data;
    private boolean scrolling = true;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.GENRE));
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;
    private boolean reverse = false;
    private String singular;
    private String plural;
    private String searchQuery = "";
    private Runnable setToolbarTitle;
    private Helpers.AdvancedQueryHelper<Show> advancedQueryHelper;

    CustomList<Show> allShowList = new CustomList<>();

    private CustomRecycler customRecycler_ShowList;
    private SearchView shows_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        ExternalCode.initialize_ifNecessary(this);
        String stringExtra = Settings.getSingleSetting(this, Settings.SETTING_SPACE_NAMES_ + Settings.Space.SPACE_SHOW);
        if (stringExtra != null) {
            String[] singPlur = stringExtra.split("\\|");

            singular = singPlur[0];
            plural = singPlur[1];
            setTitle(plural);
        }

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_show);
        mySPR_data = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

//        logTiming = CustomUtility.logTiming();
        loadDatabase();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
//            for (Show show : database.showMap.values()) {
//                if (show.getDateList().isEmpty() && !show.isUpcoming() && !database.watchLaterList.contains(show.getUuid()))
//                    database.watchLaterList.add(show.getUuid());
//            }

            setContentView(R.layout.activity_show);
//            logTiming.run(true);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            shows_search = findViewById(R.id.search);
            advancedQueryHelper = new Helpers.AdvancedQueryHelper<Show>(this, shows_search)
                    .setRestFilter((restQuery, showList) -> {
                        if (restQuery.contains("|")) {
                            showList.filterOr(restQuery.split("\\|"), (show, s) -> Utility.containedInShow(s.trim(), show, filterTypeSet), true);
                        } else {
                            showList.filterAnd(restQuery.split("&"), (show, s) -> Utility.containedInShow(s.trim(), show, filterTypeSet), true);
                        }
                    })
                    .addCriteria_defaultName()
                    .enableColoration()
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA_GENRE, CategoriesActivity.CATEGORIES.SHOW_GENRES, Show::getGenreIdList)
                    .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show, SHOW_STATUS_FILTER>(ADVANCED_SEARCH_CRITERIA_FILTER, Helpers.AdvancedQueryHelper.FREE_TEXT_MATCHER)
                            .setCategory(CategoriesActivity.CATEGORIES.SHOW)
                            .setParser((text, matcher) -> SHOW_STATUS_FILTER.getEnum(text))
                            .setBuildPredicate(filterEnum -> {
                                if (filterEnum == null)
                                    return null;

                                switch (filterEnum) {
                                    case OPEN:
                                        return show -> (!show._isLatestEpisodeWatched() || show._isNextEpisodeReleasedAndNotWatched()) && !show.getSeasonList().stream().allMatch(season -> season.getEpisodeMap().isEmpty());
                                    case CURRENT:
                                        return show -> show._isLatestEpisodeWatched() && show._isBeforeNextEpisodeAir() && !show._isLatestSeasonCompleted();
                                    case FINISHED:
                                        return Show::_isLatestSeasonCompleted;
                                    case UNSEEN:
                                        return show -> show.getSeasonList().stream().allMatch(season -> season.getEpisodeMap().isEmpty());
                                }

                                return null;
                            }));

//            logTiming.run(true);
            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s.trim();
//                    if (!s.trim().equals("")) {
//                        if (s.trim().equals(WATCH_LATER_SEARCH)) {
//                            List<String> unableToFindList = new ArrayList<>();
//                            for (String showUuid : database.watchLaterList) {
//                                Show show = database.showMap.get(showUuid);
//                                if (show == null)
//                                    unableToFindList.add(showUuid);
//                                else
//                                    filterdShowList.add(show);
//                            }
//                            if (!unableToFindList.isEmpty()) {
//                                CustomDialog.Builder(that)
//                                        .setTitle("Problem beim Laden der Liste!")
//                                        .setName((unableToFindList.size() == 1 ? "Ein " + singular + " konnte" : unableToFindList.size() + " " + plural + " konnten") + " nicht gefunden werden")
//                                        .setObjectExtra(unableToFindList)
//                                        .addButton("Ignorieren", null)
//                                        .addButton("Entfernen", customDialog -> {
//                                            database.watchLaterList.removeAll(((ArrayList<String>) customDialog.getObjectExtra()));
//                                            Toast.makeText(that, "Entfernt", Toast.LENGTH_SHORT).show();
//                                            Database.saveAll();
//                                            setResult(RESULT_OK);
//                                        })
//                                        .show();
//                            }
//                            reLoadRecycler();
//                            return true;
//                        }
////                        if (s.trim().equals(UPCOMING_SEARCH)) {
////                            filterdShowList = allShowList.stream().filter(Show::isUpcoming).collect(Collectors.toList());
////                            reLoadRecycler();
////                            return true;
////                        }
//
//                        for (String subQuery : s.split("\\|")) {
//                            subQuery = subQuery.trim();
//                            List<Show> subList = new ArrayList<>(filterdShowList);
//                            for (Show show : subList) {
//                                if (!Utility.containedInShow(subQuery, show, filterTypeSet))
//                                    filterdShowList.remove(show);
//                            }
//                        }
//                    }
                    reLoadRecycler();
                    return true;
                }
            };
            shows_search.setOnQueryTextListener(textListener);

            if (database.showGenreMap.isEmpty() && Settings.getSingleSetting_boolean(this, Settings.SETTING_SHOW_ASK_FOR_GENRE_IMPORT)) {
                CustomDialog.Builder(this)
                        .setTitle("Genres Importieren")
                        .setText("Es wurde bisher noch kein Genre hinzugefügt. Sollen die Genres aus der TMDb importiert werden?\nDies kann auch jederzeit in den Einstellungen getan werden.")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton("Nicht erneut Fragen", customDialog -> {
                            Settings.changeSetting(Settings.SETTING_SHOW_ASK_FOR_GENRE_IMPORT, "false");
                            Toast.makeText(this, "Du wirst nicht erneut gefragt", Toast.LENGTH_SHORT).show();
                        })
                        .alignPreviousButtonsLeft()
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog -> {
                            Utility.importTmdbGenre(this, true, false);
                            setResult(RESULT_OK);
                        })
                        .show();
//                return;
            }

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                showEditOrNewDialog(null);

            if (Objects.equals(getIntent().getAction(), ACTION_NEXT_EPISODE)) {
                Show[] show = {null};

                Runnable onDecided = () -> {
                    setResult(RESULT_OK);
                    if (show[0] == null)
                        return;
                    List<Pair<Date, Show.Episode>> list = new ArrayList<>();
                    for (Show.Season season : show[0].getSeasonList()) {
                        season.getEpisodeMap().values().forEach(episode -> episode.getDateList()
                                .forEach(date -> list.add(new Pair<>(date, episode))));
                    }

                    if (list.isEmpty()) {
                        Show.Season season = show[0].getSeasonList().get(1);
                        apiSeasonRequest(show[0], 1, () -> {
                            CustomRecycler<Show.Season> seasonRecycler = showSeasonsDialog(show[0]);
                            Map<String, Show.Episode> episodeMap = database.tempShowSeasonEpisodeMap.get(show[0].getUuid()).get(1);
                            showEpisodesDialog(season, episodeMap, seasonRecycler).goTo((search, episode2) -> episode2.getUuid().equals(episodeMap.get("E:1").getUuid()), "");
                            Toast.makeText(this, "Noch keine Ansichten für diese Serie", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    list.sort((o1, o2) -> o1.first.compareTo(o2.first) * -1);
                    final Show.Episode episode = list.get(0).second;

                    getNextEpisode(episode, nextEpisode -> {
                        if (nextEpisode == null) {
                            Toast.makeText(this, "Keine nächste Episode", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        CustomRecycler<Show.Season> seasonRecycler = showSeasonsDialog(show[0]);
                        int seasonNumber = nextEpisode.getSeasonNumber();
                        showEpisodesDialog(show[0].getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show[0].getUuid()).get(seasonNumber), seasonRecycler).goTo((search, episode2) -> episode2.getUuid().equals(nextEpisode.getUuid()), "");
                    });
                };

                ArrayList<Show> showList = new ArrayList<>(database.showMap.values());
                Map<Show, List<Show.Episode>> showEpisodeMap = showList.stream().collect(Collectors.toMap(show1 -> show1, this::getEpisodeList));

                new Helpers.SortHelper<>(showList)
                        .addSorter()
                        .changeType(show1 -> getLatest(showEpisodeMap.get(show1)))
                        .enableReverseDefaultComparable()
                        .sort();

                if (!getIntent().getBooleanExtra(EXTRA_NEXT_EPISODE_SELECT, false)) {
                    if (showList.isEmpty()) {
                        Toast.makeText(this, "Es wurde noch keine Serie hinterlegt", Toast.LENGTH_SHORT).show();
                    } else {
                        show[0] = showList.get(0);
                        onDecided.run();
                    }
                } else {
                    com.finn.androidUtilities.CustomDialog selectDialog = com.finn.androidUtilities.CustomDialog.Builder(this);
                    com.finn.androidUtilities.CustomRecycler<Show> customRecycler = new CustomRecycler<Show>(this)
                            .enableDivider(12)
                            .setItemLayout(R.layout.list_item_select_next_episode)
                            .setSetItemContent((customRecycler1, itemView, show1, index) -> {
                                Utility.simpleLoadUrlIntoImageView(this, itemView.findViewById(R.id.listItem_selectNextEpisode_image), show1.getImagePath(), show1.getImagePath(), 4);
                                ((TextView) itemView.findViewById(R.id.listItem_selectNextEpisode_name)).setText(show1.getName());
                            })
                            .disableCustomRipple()
                            .setGetActiveObjectList(customRecycler1 -> showList)
                            .setOnClickListener((customRecycler1, itemView, s, index) -> {
                                show[0] = showList.get(index);
                                onDecided.run();
                                selectDialog.dismiss();
                            });

                    selectDialog
                            .setTitle("Serie Auswählen")
//                            .alignPreviousButtonsLeft()
                            .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.BACK)
                            .setView(customRecycler.generateRecyclerView())
                            .disableScroll()
                            .show();

                }
            }

            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {
                if (extraSearchCategory == CategoriesActivity.CATEGORIES.EPISODE) {
                    String episode_string = getIntent().getStringExtra(EXTRA_EPISODE);
                    if (episode_string != null) {
                        Show.Episode episode = new Gson().fromJson(episode_string, Show.Episode.class);
                        findEpisode(episode, () -> {
                            Show.Episode oldEpisode = database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber())
                                    .getEpisodeMap().get(episode.getUuid());

                            if (oldEpisode != null)
                                showEpisodeDetailDialog(this, null, oldEpisode);
                            else
                                apiSeasonRequest(database.showMap.get(episode.getShowId()), episode.getSeasonNumber(), () ->
                                        showEpisodeDetailDialog(this, null, database.tempShowSeasonEpisodeMap.get(episode.getShowId()).get(episode.getSeasonNumber())
                                                .get(episode.getUuid())));
                        });
                    }
                }

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (extraSearchCategory == CategoriesActivity.CATEGORIES.SHOW_GENRES || extraSearchCategory == CategoriesActivity.CATEGORIES.SHOW)
                        advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch);
                    else
                        shows_search.setQuery(extraSearch, true);
                }
            }
            setSearchHint();

            onChangeListener = Database.addOnChangeListener(Database.SHOW_MAP, change -> reLoadRecycler());
//            logTiming.run(false);
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_data, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }

    private CustomList<Show> filterList(CustomList<Show> showList) {
        advancedQueryHelper.filterFull(showList);
        return showList;
    }

    private CustomList<Show> sortList(CustomList<Show> showList) {
//        logTiming.run(true);
        Map<Show, List<Show.Episode>> showEpisodeMap = showList.stream().collect(Collectors.toMap(show -> show, this::getEpisodeList));
//        logTiming.run(true);

        new Helpers.SortHelper<>(showList)
                .setAllReversed(reverse)
                .addSorter(SORT_TYPE.NAME, (show1, show2) -> show1.getName().compareTo(show2.getName()) * (reverse ? -1 : 1))

                .addSorter(SORT_TYPE.VIEWS)
                .changeType(show -> getViews(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.RATING)
                .changeType(show -> getRatingWithTendency(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.LATEST)
                .changeType(show -> getLatest(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()

                .finish()
                .sort(() -> sort_type);
        return showList;
    }

    private void loadRecycler() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        customRecycler_ShowList = new CustomRecycler<Show>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_show)
                .setGetActiveObjectList(customRecycler -> {
//                    logTiming.run(true);
                    CustomList<Show> filteredList = sortList(filterList(new CustomList<>(database.showMap.values())));
//                    logTiming.run(true);
                    TextView noItem = findViewById(R.id.no_item);
                    String text = shows_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    List<Show.Episode> episodeList = Utility.concatenateCollections(filteredList, show -> Utility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values()));
                    int views = getViews(episodeList);
                    String viewsCountText = views + " Episoden " + (views > 1 ? "Ansichten" : "Ansicht");
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//                    logTiming.run(true);

                    int watchedMinutes = filteredList.stream().mapToInt(show -> {
                        int averageRuntime = CustomUtility.isNotValueOrElse(show.getAverageRuntime(), 0, -1);
                        List<Show.Episode> episodeList1 = getEpisodeList(show);
                        return episodeList1.stream().mapToInt(episode -> CustomUtility.isNotValueOrElse(episode.getLength(), averageRuntime, -1, 0) * episode.getDateList().size()).sum();
                    }).sum();
                    String timeString = Utility.formatDuration(Duration.ofMinutes(watchedMinutes), null);
                    if (Utility.stringExists(timeString))
                        builder.append(timeString).append("\n");

                    elementCount.setText(builder.append(viewsCountText));
//                    logTiming.run(true);
                    return filteredList;
                })
                .setSetItemContent((customRecycler, itemView, show, index) -> {
                    MinDimensionLayout listItem_show_image_layout = itemView.findViewById(R.id.listItem_show_image_layout);
                    if (CustomUtility.stringExists(show.getImagePath())) {
                        listItem_show_image_layout.setVisibility(View.VISIBLE);
                        ImageView image = itemView.findViewById(R.id.listItem_show_image);
                        Utility.loadUrlIntoImageView(this, image, Utility.getTmdbImagePath_ifNecessary(show.getImagePath(), false), Utility.getTmdbImagePath_ifNecessary(show.getImagePath(), true), null, () -> Utility.roundImageView(image, 4), this::removeFocusFromSearch);
                    } else
                        listItem_show_image_layout.setVisibility(View.GONE);


                    ((TextView) itemView.findViewById(R.id.listItem_show_Titel)).setText(show.getName());
                    List<Show.Episode> episodeList = getEpisodeList(show);
                    int watchedEpisodes = (int) episodeList.stream().filter(episode -> episode.getSeasonNumber() != 0).count();
                    boolean releasedEpisodes = show.isNotifyNew() && !show.getAlreadyAiredList().isEmpty() && show.getAlreadyAiredList().stream().allMatch(Show.Episode::isWatched);
                    int views = getViews(episodeList);
                    if (views > 0) {
                        itemView.findViewById(R.id.listItem_show_Views_layout).setVisibility(View.VISIBLE);
                        Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                        helper.appendColor(show._isLatestSeasonCompleted() ? "✓  " : "", getColor(R.color.colorGreen));
                        if (releasedEpisodes || show.getAllEpisodesCount() <= watchedEpisodes || (show._isLatestEpisodeWatched() && !show._isNextEpisodeReleasedAndNotWatched()))
                            helper.appendColor(String.valueOf(views), getColor(R.color.colorGreen));
                        else
                            helper.append(String.valueOf(views));
                        ((TextView) itemView.findViewById(R.id.listItem_show_views)).setText(helper.get());
                    } else
                        itemView.findViewById(R.id.listItem_show_Views_layout).setVisibility(View.GONE);

                    double rating = getRatingWithTendency(episodeList);
                    if (rating > 0) {
                        itemView.findViewById(R.id.listItem_show_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_show_rating)).setText(decimalFormat.format(rating));
                    } else
                        itemView.findViewById(R.id.listItem_show_rating_layout).setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.listItem_show_Genre)).setText(
                            show.getGenreIdList().stream().map(uuid -> database.showGenreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    itemView.findViewById(R.id.listItem_show_Genre).setSelected(scrolling);
                })
                .setOnClickListener((customRecycler, view, show, index) -> showDetailDialog(show))
                .addSubOnClickListener(R.id.listItem_show_list, (customRecycler, view, show, index) -> showSeasonsDialog(show))
                .addSubOnLongClickListener(R.id.listItem_show_list, (customRecycler, view, show, index) -> {
                    List<Pair<Date, Show.Episode>> list = new ArrayList<>();
                    for (Show.Season season : show.getSeasonList()) {
                        season.getEpisodeMap().values().forEach(episode -> episode.getDateList()
                                .forEach(date -> list.add(new Pair<>(date, episode))));
                    }
                    if (list.isEmpty())
                        return;

                    list.sort((o1, o2) -> o1.first.compareTo(o2.first) * -1);
                    final Show.Episode[] episode = {list.get(0).second};

                    Runnable onDecided = () -> {
                        CustomRecycler<Show.Season> seasonRecycler = showSeasonsDialog(show);
                        int seasonNumber = episode[0].getSeasonNumber();
                        if (episode[0].equals(list.get(0).second)) {
                            apiSeasonRequest(show, seasonNumber, () -> {
                                showEpisodesDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo(episode[0]);
                            });
                        } else
                            showEpisodesDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode[0].getUuid()), "");
                    };

                    CustomDialog.Builder(this)
                            .setTitle("Gehe Zu")
                            .addButton("Ansichten-Historie", customDialog -> showViewHistoryDialog(list))
                            .addButton("Ansichten-Kalender", customDialog -> showCalenderDialog(show))
                            .addButton("Gesehene Episoden", customDialog -> showSeenEpisodesForShow(show, null))
                            .addOnLongClickToLastAddedButton(customDialog -> showSeenEpisodesForShow(show))
                            .addButton("Zuletzt gesehen", customDialog -> onDecided.run())
                            .addButton("Nächste Episode", customDialog -> {
                                getNextEpisode(episode[0], episode1 -> {
                                    episode[0] = episode1;
                                    if (episode[0] == null)
                                        Toast.makeText(this, "Keine nächste Episode", Toast.LENGTH_SHORT).show();
                                    else
                                        onDecided.run();
                                });
                            })
                            .enableStackButtons()
                            .enableButtonDividerAll()
                            .show();
                })
                .setOnLongClickListener((customRecycler, view, show, index) -> {
                    showEditOrNewDialog(show);
                })
                .enableFastScroll((customRecycler, show, integer) -> {
                    switch (sort_type) {
                        case NAME:
                            return show.getName().substring(0, 1).toUpperCase();
                        case VIEWS:
                            int size = show.getSeasonList().stream().mapToInt(season -> season.getEpisodeMap().values().stream().mapToInt(value -> value.getDateList().size()).sum()).sum();
                            if (size == 0)
                                return "Keine Ansichten";
                            return size + (size > 1 ? " Ansichten" : " Ansicht");
                        case RATING:
                            double rating = CustomUtility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values()).stream().mapToDouble(ParentClass_Ratable::getRating).filter(value -> value > 0).average().orElse(-1);
                            if (rating <= 0)
                                return "Keine Bewertung";
                            if (rating % 1 == 0)
                                return rating + " ☆";
                            else
                                return decimalFormat.format(rating) + " ☆";
                        case LATEST:
                            Date max = show.getSeasonList().stream().map(season -> season.getEpisodeMap().values().stream().map(value -> value.getDateList().stream().max(Date::compareTo).orElse(null)).filter(Objects::nonNull).max(Date::compareTo).orElse(null)).filter(Objects::nonNull).max(Date::compareTo).orElse(null);
                            if (max != null)
                                return dateFormat.format(max);
                            return "Keine Ansicht";
                        default:
                            return null;
                    }
                })
                .setPadding(16)
                .generate();
    }

    private void showViewHistoryDialog(List<Pair<Date, Show.Episode>> list) {
        List<Expandable<Show.Episode>> expendables = new Expandable.ToGroupExpandableList<Show.Episode, Pair<Date, Show.Episode>, Date>()
                .setSort((o1, o2) -> ((Date) o1.getPayload()).compareTo(((Date) o2.getPayload())) * -1)
                .runToGroupExpandableList(list, dateEpisodePair -> Utility.removeTime(dateEpisodePair.first), (date, pairList) -> String.format(Locale.getDefault(), "%s (%d)", new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(date), pairList.size())
                        , item -> item.second);

        CustomDialog.Builder(this)
                .setTitle(String.format(Locale.getDefault(), "Ansichten-Historie (%d)", expendables.size()))
                .setView(new com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>>(this)
                        .setObjectList(expendables)
                        .setExpandableHelper(customRecycler1 ->
                                        customRecycler1
                                                .new ExpandableHelper<Show.Episode>()
                                                .enableExpandByDefault()
                                                .customizeRecycler((subRecycler, expandable, index0) -> {
                                                    subRecycler
                                                            .setSetItemContent((customRecycler2, itemView, episode, index) -> {
                                                                Utility.setMargins(itemView, 8, 5, 8, 5);
                                                                itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);

                                                                ImageView listItem_episode_image = itemView.findViewById(R.id.listItem_episode_image);
//                                                                int showPreviewSetting = Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_SHOW_EPISODE_PREVIEW));
                                                                if (Utility.stringExists(episode.getStillPath()) /*&& (showPreviewSetting == 0 || showPreviewSetting == 1 && episode.isWatched())*/) {
                                                                    listItem_episode_image.setVisibility(View.VISIBLE);
                                                                    Utility.simpleLoadUrlIntoImageView(this, listItem_episode_image, episode.getStillPath(), episode.getStillPath(), 2);
                                                                } else
                                                                    listItem_episode_image.setVisibility(View.GONE);


                                                                itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                                                                itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.GONE);
                                                                ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));

                                                                ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                                                                ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());
                                                                if (episode.getAirDate() != null)
                                                                    ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));
                                                                ParentClass_Ratable.applyRatingTendencyIndicator(itemView.findViewById(R.id.listItem_episode_ratingTendency), episode, episode.isWatched(), false);
                                                                ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode.getRating() != -1 ? episode.getRating() + " ☆" : "");

//                                                        ImageView listItem_episode_image = itemView.findViewById(R.id.listItem_episode_image);
//                                                        if (Utility.stringExists(episode._getStillPath())) {
//                                                            listItem_episode_image.setVisibility(View.VISIBLE);
//                                                            Utility.loadUrlIntoImageView(this, listItem_episode_image, Utility.getTmdbImagePath_ifNecessary(episode._getStillPath(), false ), Utility.getTmdbImagePath_ifNecessary(episode._getStillPath(), true ));
//                                                        } else
//                                                            listItem_episode_image.setVisibility(View.GONE);

                                                            })
                                                            .setOnClickListener((customRecycler2, itemView, episode1, index1) -> {
                                                                Toast.makeText(this, episode1.getName(), Toast.LENGTH_SHORT).show();
                                                                findEpisode(episode1, () -> {
                                                                });
                                                            })
                                                            .setItemLayout(R.layout.list_item_episode);
                                                })
                        )
                        .generateRecyclerView())
                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                .setDimensions(true, true)
                .disableScroll()
                .show();
    }

    private void showSeenEpisodesForShow(Show show) {
        EpisodeActivity.showAdvancedQueryShowHelperDialog(this, show, null, pair -> showSeenEpisodesForShow(show, pair));
    }

    private void showSeenEpisodesForShow(Show show, @Nullable Pair<String, String> pair) {
        Intent intent = new Intent(this, EpisodeActivity.class)
                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW)
                .putExtra(CategoriesActivity.EXTRA_SEARCH, show.getName());

        if (pair != null && CustomUtility.stringExists(pair.first))
            intent.putExtra(EpisodeActivity.EXTRA_SEARCH_SEASONS_AND_EPISODES, pair.first);
        if (pair != null && CustomUtility.stringExists(pair.second))
            intent.putExtra(EpisodeActivity.EXTRA_SEARCH_SHOW_LABELS, pair.second);
        startActivity(intent);
    }

    //  ------------------------- ShowInfo ------------------------->
    private List<Show.Episode> getEpisodeList(Show show) {
        return Utility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values());
    }

    private double getRating(List<Show.Episode> episodeList) {
        return episodeList.stream().filter(episode -> !(episode.getRating().equals(-1f) || episode.getRating().equals(0f))).mapToDouble(Show.Episode::getRating).average().orElse(-1);
    }

    private double getRatingWithTendency(List<Show.Episode> episodeList) {
        return episodeList.stream().map(ParentClass_Ratable::_getRatingWithTendency).filter(Optional::isPresent).mapToDouble(optRating -> (double) optRating.get()).average().orElse(-1);
    }

    private Date getLatest(List<Show.Episode> episodeList) {
        List<Date> dateList = Utility.concatenateCollections(episodeList, Show.Episode::getDateList);
        if (dateList.isEmpty())
            return null;
        Date max = Collections.max(dateList);
        return max;
    }

    private int getViews(List<Show.Episode> episodeList) {
        return episodeList.stream().map(episode -> episode.getDateList().size()).reduce(0, Integer::sum);
    }
    //  <------------------------- ShowInfo -------------------------


    //  --------------- Static --------------->
    public static void showNewEpisodesDialog(AppCompatActivity activity) {
        Database database = Database.getInstance();
        CustomDialog customDialog = CustomDialog.Builder(activity);
        com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>> expandableCustomRecycler = new com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>>(activity)
                .setGetActiveObjectList(customRecycler -> {
                    List<Show.Episode> alreadyAiredList = Utility.concatenateCollections(database.showMap.values(), Show::getAlreadyAiredList);
                    return new Expandable.ToGroupExpandableList<Show.Episode, Show.Episode, String>()
                            .keepExpandedState(customRecycler)
                            .enableUseExpandMatching(customRecycler)
                            .runToGroupExpandableList(alreadyAiredList, Show.Episode::getShowId, (s, m) -> String.format(Locale.getDefault(), "%s (%d)",
                                    database.showMap.get(s).getName(), m.size()), episode -> episode);
                })
                .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<Show.Episode>()
                        .setExpandMatching(expandable -> expandable.getList().stream().anyMatch(episode -> !episode.isWatched()))
                        .customizeRecycler((subRecycler, expandable, index0) -> {
                            subRecycler
                                    .setSetItemContent((customRecycler1, itemView, episode, index) -> {
                                        Utility.setMargins(itemView, 8, 5, 8, 5);
                                        itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);


                                        itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                                        itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.GONE);
                                        ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));

                                        ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                                        ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(CustomUtility.stringExistsOrElse(episode.getName(), "Episode " + episode.getEpisodeNumber()));
                                        if (episode.getAirDate() != null)
                                            ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));

                                        Object what = null;
                                        if (episode.isWatched())
                                            what = new StrikethroughSpan();
                                        Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper().setQuickWhat(what);
                                        Utility.applyToAllViews(((ViewGroup) itemView), TextView.class, textView -> {
                                            textView.setText(helper.quick(textView.getText().toString()));
                                            textView.setAlpha(episode.isWatched() ? 0.4f : 1f);
                                        });
                                    })
                                    .setOnClickListener((customRecycler2, itemView, episode, index1) -> {
                                        Toast.makeText(activity, episode.getName(), Toast.LENGTH_SHORT).show();
                                        activity.startActivityForResult(new Intent(activity, ShowActivity.class)
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH, episode.getShowId())
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.EPISODE)
                                                .putExtra(ShowActivity.EXTRA_EPISODE, new Gson().toJson(episode)), MainActivity.START_SHOW_FROM_CALENDER);

                                    })
                                    .setItemLayout(R.layout.list_item_episode)
                                    .enableSwiping((customRecycler1, objectList, direction, episode, index) -> {
                                        if (direction == 32) {
                                            Show show = database.showMap.get(episode.getShowId());
                                            show.setAlreadyAiredList(show.getAlreadyAiredList().stream().filter(episode1 -> episode.getTmdbId() != episode1.getTmdbId()).collect(Collectors.toList()));

                                            List<Expandable<Show.Episode>> rest = customRecycler.getObjectList().stream().filter(listExpandable -> !listExpandable.getList().isEmpty()).collect(Collectors.toList());
                                            if (rest.isEmpty())
                                                customDialog.dismiss();
                                            else
                                                customRecycler.reload(rest);
                                            MainActivity.setCounts((MainActivity) activity);
                                            return;
                                        }
                                        Database.getInstance().showMap.values().stream().filter(show -> show.getUuid().equals(episode.getShowId())).findFirst()
                                                .ifPresent(show -> show.getAlreadyAiredList().stream().filter(episode1 -> episode1.equals(episode)).findFirst()
                                                        .ifPresent(episode1 -> episode1.setWatched(!episode1.isWatched())));
                                        MainActivity.setCounts((MainActivity) activity);
                                    }, true, true)
                                    .setSwipeBackgroundHelper(new com.finn.androidUtilities.CustomRecycler.SwipeBackgroundHelper<Show.Episode>(R.drawable.ic_delete, Color.RED)
                                            .enableBouncyThreshold(2, true, false)
                                            .setDynamicResources((swipeBackgroundHelper, episode) -> {
                                                swipeBackgroundHelper
                                                        .setIconResId_left(episode.isWatched() ? R.drawable.ic_notification_active : R.drawable.ic_notification)
                                                        .setFarEnoughColor_circle_left(activity.getColor(R.color.colorGreen));
                                            })
                                    );
                        })
                );
        customDialog
                .setTitle("Neue Folgen")
                .setView(expandableCustomRecycler.generateRecyclerView())
                .disableScroll()
                .setDimensions(true, true)
                .addButton(R.drawable.ic_sync, customDialog1 -> {

                    List<Show> showList = database.showMap.values().stream().filter(Show::isNotifyNew).collect(Collectors.toList());
                    final int[] pending = {showList.size()};

                    if (showList.isEmpty())
                        Toast.makeText(activity, "Nichts zum aktualisieren markiert", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(activity, "Aktualisieren", Toast.LENGTH_SHORT).show();

                    showList.forEach(show ->
                            apiDetailRequest(activity, show.getTmdbId(), show, () -> {
                                if (pending[0] > 1) {
                                    pending[0]--;
                                    return;
                                }
                                Toast.makeText(activity, "Alle aktualisiert", Toast.LENGTH_SHORT).show();
                                Database.saveAll();
                                expandableCustomRecycler.reload();
                            }, true, false));
                }, false)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                .setOnDialogDismiss(customDialog1 -> Database.saveAll())
                .show();
    }

    public static void showNextEpisode(AppCompatActivity activity, View view, boolean longClick) {
        activity.startActivityForResult(new Intent(activity, ShowActivity.class).setAction(ACTION_NEXT_EPISODE).putExtra(EXTRA_NEXT_EPISODE_SELECT, longClick), MainActivity.START_SHOW_NEXT_EPISODE);
    }

    public static void showFilterShowsMenu(AppCompatActivity activity, View view) {
        if (!Database.isReady())
            return;

        CustomMenu.Builder(activity, view.findViewById(R.id.main_shows_filterLabel))
                .setMenus((customMenu, items) -> {
                    items.add(new CustomMenu.MenuItem(SHOW_STATUS_FILTER.OPEN.getUiText(), new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, SHOW_STATUS_FILTER.OPEN.getUiText())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW)
                            , MainActivity.START_SHOW_STATUS_FILTER), R.drawable.ic_play_next));
                    items.add(new CustomMenu.MenuItem(SHOW_STATUS_FILTER.CURRENT.getUiText(), new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, SHOW_STATUS_FILTER.CURRENT.getUiText())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW)
                            , MainActivity.START_SHOW_STATUS_FILTER), R.drawable.ic_calendar));
                    items.add(new CustomMenu.MenuItem(SHOW_STATUS_FILTER.FINISHED.getUiText(), new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, SHOW_STATUS_FILTER.FINISHED.getUiText())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW)
                            , MainActivity.START_SHOW_STATUS_FILTER), R.drawable.ic_check));
                    items.add(new CustomMenu.MenuItem(SHOW_STATUS_FILTER.UNSEEN.getUiText(), new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, SHOW_STATUS_FILTER.UNSEEN.getUiText())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW)
                            , MainActivity.START_SHOW_STATUS_FILTER), R.drawable.ic_hide_password));
                })
                .setOnClickListener((customRecycler, itemView, item, index) -> {
                    Pair<Intent, Integer> pair = (Pair<Intent, Integer>) item.getContent();
                    activity.startActivityForResult(pair.first, pair.second);
                })
                .dismissOnClick()
                .show();

    }
    //  <--------------- Static ---------------


    //  --------------- NextEpisode --------------->
    private void getNextEpisode(Show.Episode previousEpisode, OnNextEpisode onNextEpisode) {
        Show show = database.showMap.get(previousEpisode.getShowId());

        Show.Season season;
        List<Show.Season> seasonList = show.getSeasonList();

        season = seasonList.get(previousEpisode.getSeasonNumber());

//        Map<Integer, Map<String, Show.Episode>> tempShowSeasonMap = database.tempShowSeasonEpisodeMap.get(show.getUuid());

        Runnable requestNextSeason = () -> {
            apiSeasonRequest(show, season.getSeasonNumber() + 1, () -> {
                onNextEpisode.runOnNextEpisode(database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(season.getSeasonNumber() + 1).values()
                        .stream().min((o1, o2) -> Integer.compare(o1.getEpisodeNumber(), o2.getEpisodeNumber())).orElse(null));
            });
        };

        if (season.getEpisodesCount() > previousEpisode.getEpisodeNumber() || season.getEpisodesCount() > (new CustomList<>(season.getEpisodeMap().values()).sorted((o1, o2) -> Integer.compare(o1.getEpisodeNumber(), o2.getEpisodeNumber())).indexOf(previousEpisode) + 1)) {
            apiSeasonRequest(show, season.getSeasonNumber(), () -> {
                Show.Episode episode = database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(season.getSeasonNumber()).get("E:" + (previousEpisode.getEpisodeNumber() + 1));
                if (episode == null && show.getSeasonsCount() > season.getSeasonNumber())
                    requestNextSeason.run();
                else
                    onNextEpisode.runOnNextEpisode(episode);
            });
        } else if (show.getSeasonsCount() > season.getSeasonNumber()) {
            requestNextSeason.run();
        } else
            onNextEpisode.runOnNextEpisode(null);

    }

    private interface OnNextEpisode {
        void runOnNextEpisode(Show.Episode episode);
    }
    //  <--------------- NextEpisode ---------------


    private void reLoadRecycler() {
        customRecycler_ShowList.reload();
    }

    private CustomDialog showDetailDialog(Show show) {
        setResult(RESULT_OK);
        removeFocusFromSearch();
        return CustomDialog.Builder(this)
                .setTitle(show.getName())
                .setView(R.layout.dialog_detail_show)
                .addButton("Ansichten", customDialog -> {
                    CustomDialog.Builder(this)
                            .setTitle("Ansichten")
                            .addButton("Ansichten-Historie", customDialog1 -> {
                                List<Pair<Date, Show.Episode>> list = new ArrayList<>();
                                for (Show.Season season : show.getSeasonList()) {
                                    season.getEpisodeMap().values().forEach(episode -> episode.getDateList()
                                            .forEach(date -> list.add(new Pair<>(date, episode))));
                                }
                                if (list.isEmpty()) {
                                    Toast.makeText(this, "Keine Ansichten eingetragen", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                list.sort((o1, o2) -> o1.first.compareTo(o2.first) * -1);
                                showViewHistoryDialog(list);
                            })
                            .addButton("Ansichten-Kalender", customDialog1 -> {
                                showCalenderDialog(show);
                            })
                            .enableExpandButtons()
                            .show();
                }, false)
                .alignPreviousButtonsLeft()
                .addButton("Bearbeiten", customDialog -> {
                    CustomDialog customDialog_edit = showEditOrNewDialog(show);
                    if (customDialog_edit != null)
                        customDialog_edit.setPayload(customDialog);
                }, false)
                .setSetViewContent((customDialog, view, reload) -> {
                    if (reload)
                        customDialog.setTitle(show.getName());
//                    ((TextView) view.findViewById(R.id.dialog_detailShow_title)).setText(show.getName());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.SHOW_GENRES, view.findViewById(R.id.dialog_detailShow_genre), show.getGenreIdList());
                    view.findViewById(R.id.dialog_detailShow_genre).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_detailShow_release))
                            .setText(show.getFirstAirDate() != null ? new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(show.getFirstAirDate()) : "");
                    ((TextView) view.findViewById(R.id.dialog_detailShow_details)).setText(getDetailText(show));

                    ImageView dialog_detailShow_notification = view.findViewById(R.id.dialog_detailShow_notification);
                    Runnable setImage = () -> dialog_detailShow_notification.setImageResource(show.isNotifyNew() ? R.drawable.ic_notification_active : R.drawable.ic_notification);
                    setImage.run();
                    dialog_detailShow_notification.setOnClickListener(v -> {
                        show.setNotifyNew(!show.isNotifyNew());
                        Toast.makeText(this, "Benachrichtigungen " + (show.isNotifyNew() ? "aktiviert" : "deaktiviert"), Toast.LENGTH_SHORT).show();
                        setImage.run();
                        Database.saveAll();
                        reLoadRecycler();
                    });
                    view.findViewById(R.id.dialog_detailShow_list).setOnClickListener(view1 -> showSeasonsDialog(show));
                    view.findViewById(R.id.dialog_detailShow_list).setOnLongClickListener(view1 -> {
                        showSeenEpisodesForShow(show);
                        return true;
                    });
                    view.findViewById(R.id.dialog_detailShow_internet).setOnClickListener(v -> {
                        if (!Utility.stringExists(show.getImdbId()))
                            Utility.openUrl(this, "https://www.themoviedb.org/tv/" + show.getTmdbId(), true);
                        else
                            CustomDialog.Builder(this)
                                    .setTitle("Öffnen mit...")
                                    .addButton("TMDb", customDialog1 -> Utility.openUrl(this, "https://www.themoviedb.org/tv/" + show.getTmdbId(), true))
                                    .addButton("WerStreamt.es", customDialog1 -> Utility.doWerStreamtEsRequest(this, show.getName()))
                                    .addButton("IMDB", customDialog1 -> Utility.openUrl(this, "https://www.imdb.com/title/" + show.getImdbId(), true))
                                    .disableButtonAllCaps()
                                    .enableExpandButtons()
                                    .enableButtonDividerAll()
                                    .show();
                    });
                    view.findViewById(R.id.dialog_detailShow_internet).setOnLongClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("IMDB-ID " + (Utility.stringExists(show.getImdbId()) ? "Ändern" : "Hinzufügen"))
                                .setEdit(new CustomDialog.EditBuilder()
                                        .setInputType(com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.TEXT)
                                        .setHint("IMDB-ID")
                                        .setText(Utility.stringExistsOrElse(show.getImdbId(), ""))
                                        .setRegEx(Utility.imdbPattern_full))
                                .addOptionalModifications(customDialog1 -> {
                                    if (Utility.stringExists(show.getImdbId()))
                                        customDialog1.addButton("Löschen", customDialog2 -> {
                                                    show.setImdbId(null);
                                                    Toast.makeText(this, "IMDB-ID gelöscht", Toast.LENGTH_SHORT).show();
                                                    Database.saveAll();
                                                })
                                                .alignPreviousButtonsLeft();

                                })
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                    show.setImdbId(customDialog1.getEditText());
                                    Toast.makeText(this, "IMDB-ID gespeichert", Toast.LENGTH_SHORT).show();
                                    Database.saveAll();
                                })
                                .disableLastAddedButton()
                                .setSetViewContent((customDialog1, view1, reload1) -> {
                                    EditText editFilmId = customDialog1.findViewById(R.id.dialog_custom_edit);
                                    editFilmId.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                            if (s.toString().matches("https://.*" + Utility.imdbPattern + ".*")) {
                                                Matcher matcher = Pattern.compile(Utility.imdbPattern).matcher(s);
                                                if (matcher.find())
                                                    editFilmId.setText(matcher.group(0));
                                            }
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                        }
                                    });
                                })
                                .show();
                        return true;
                    });
                    view.findViewById(R.id.dialog_detailShow_reset).setOnClickListener(v -> {
                        showResetDialog(new CustomList<>(Utility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values())), show, null, null, null, 1);
                        customDialog.setOnDialogDismiss(customDialog1 -> reLoadRecycler());
                    });
                    view.findViewById(R.id.dialog_detailShow_sync).setOnClickListener(v -> {
                        Toast.makeText(this, "Show wird aktualisiert", Toast.LENGTH_SHORT).show();
                        apiDetailRequest(this, show.getTmdbId(), show, () -> {
                            customDialog.reloadView();
                            Toast.makeText(this, "Fertig", Toast.LENGTH_SHORT).show();
                        }, true, true);
                    });
                    view.findViewById(R.id.dialog_detailShow_sync).setOnLongClickListener(v -> {
                        Utility.GenericInterface<Boolean> updateEpisodes = all -> {
                            CustomList<Show.Episode> episodes = new CustomList<>(Utility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values()));
                            if (!all)
                                episodes.filter(episode -> !episode.hasAnyExtraDetails(), true);
                            getImdbIdAndDetails(episodes, true, null);
//                            getDetailsFromImdb(episodes, true, null);
                        };
                        CustomDialog.Builder(this)
                                .setTitle("EpisodenDetails Aktualisieren")
                                .addButton("Alle", customDialog1 -> updateEpisodes.run(true))
                                .addButton("Wo Nötig", customDialog1 -> updateEpisodes.run(false))
                                .markLastAddedButtonAsActionButton()
                                .enableExpandButtons(false)
                                .show();
                        return true;
                    });
                    view.findViewById(R.id.dialog_detailShow_labels).setOnClickListener(v -> {
                        List<ShowLabel> showLabels = show.getShowLabels();
                        Utility.showEditItemDialog(this, new ArrayList<>(), CategoriesActivity.CATEGORIES.SHOW_LABEL, show::getShowLabels,
                                (customDialog1, newIds) -> {
                                },
                                id -> showLabels.stream().filter(showLabel -> showLabel.getUuid().equals(id)).findFirst().orElse(null),
                                (name, url) -> {
                                    ShowLabel showLabel = new ShowLabel(name);
                                    showLabels.add(showLabel);
                                    return showLabel;
                                }, (customDialog1, parentClassCustomRecycler, showLabel) -> {
                                    showSeenEpisodesForShow(show, Pair.create(null, showLabel.getName()));
                                }, (selectDialog, customRecycler1, showLabel) -> {
                                    CustomDialog.Builder(this)
                                            .setTitle("Aktion")
                                            .addButton("Bearbeiten", customDialog1 -> {
                                                CategoriesActivity.showEditCategoryDialog(this, CategoriesActivity.CATEGORIES.SHOW_LABEL, showLabel, parentClass1 -> {
                                                    selectDialog.dismiss();
                                                }, null, null, null, parentClass1 -> {
                                                    selectDialog.dismiss();
                                                    show.getSeasonList().stream().flatMap(season -> season.getEpisodeMap().values().stream()).forEach(episode1 -> episode1.getShowLabelIds().remove(showLabel.getUuid()));
                                                    show.getShowLabels().remove(((ShowLabel) showLabel));
                                                    Database.saveAll(this, "Gelöscht", null, "Löschen fehlgeschlagen");
                                                }, s -> show.getShowLabels().stream().filter(showLabel1 -> showLabel1.getName().equalsIgnoreCase(s)).findFirst().orElse(null));
                                            })
                                            .addButton("Bulk Edit", customDialog1 -> {
                                                CustomDialog.Builder(this)
                                                        .setTitle("Label Bulk-Edit")
                                                        .setText(String.format(Locale.getDefault(), "*Label:* %s\n", showLabel.getName()) +
                                                                EpisodeActivity.getAdvancedQueryShowHelperDialogText(show, true, true, false))
                                                        .enableTextFormatting()
                                                        .setView(customDialog2 -> {
                                                            LinearLayout linearLayout = new LinearLayout(this);
                                                            CustomUtility.setPadding(linearLayout, 16);
                                                            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                            TextView textView = new TextView(this);
                                                            textView.setText("Modus: ");
                                                            Spinner spinner = new Spinner(this);
                                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Hinzufügen", "Entfernen"));
                                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                            spinner.setAdapter(adapter);
                                                            linearLayout.addView(textView);
                                                            linearLayout.addView(spinner);
                                                            return linearLayout;
                                                        })
                                                        .setEdit(new CustomDialog.EditBuilder()
                                                                .setHint(show.getSeasonList().stream()
                                                                        .filter(season -> !season.getName().equals(Show.EMPTY_SEASON))
                                                                        .map(season -> String.format(Locale.getDefault(), "S%d: %dEs", season.getSeasonNumber(), season.getEpisodesCount()))
                                                                        .collect(Collectors.joining(", ")))
                                                                .setRegEx(EpisodeActivity.ADVANCED_SEARCH_CRITERIA_SHOW_REGEX_SEASON_AND_EPISODE))
                                                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                                        .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, bulkEditDialog -> {
                                                            Spinner spinner = (Spinner) ((LinearLayout) bulkEditDialog.getView()).getChildAt(1);
                                                            boolean modeAdd = spinner.getSelectedItemPosition() == 0;

                                                            String query = String.format("%s(%s)", show.getName(), bulkEditDialog.getEditText());
                                                            List<CustomUtility.Triple<Show, CustomUtility.Triple<Map<Integer, List<Integer>>, Map<Integer, List<Integer>>, String>, Pair<List<Pair<List<String>, List<String>>>, String>>> resTriple =
                                                                    EpisodeActivity.getShowParser().run(query, null);
                                                            Predicate<Show.Episode> predicate = EpisodeActivity.getShowPredicate().run(resTriple);
                                                            List<Show.Episode> episodes = show.getSeasonList().stream()
                                                                    .flatMap(season -> season.getEpisodeMap().values().stream())
                                                                    .filter(predicate)
                                                                    .filter(episode -> episode.getShowLabelIds().contains(showLabel.getUuid()) ^ modeAdd)
                                                                    .sorted(Comparator.comparing(episode -> episode.getAbsoluteEpisodeNumber(show)))
                                                                    .collect(Collectors.toList());
                                                            if (episodes.isEmpty()) {
                                                                Toast.makeText(this, "Keine Episoden für die Eingaben gefunden", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }
                                                            String selectedEpisodesString = episodes.stream()
                                                                    .map(episode -> String.format(Locale.getDefault(), "S%d;E%d (E%d): %s", episode.getSeasonNumber(), episode.getEpisodeNumber(), episode.getAbsoluteEpisodeNumber(show), CustomUtility.getEllipsedString(episode.getName(), 35)))
                                                                    .collect(Collectors.joining("\n"));
                                                            CustomDialog.Builder(this)
                                                                    .setTitle("Bulk-Edit Bestätigen")
                                                                    .setText(String.format(Locale.getDefault(), "Willst du wirklich das Label '%s' %s?: (%d %s)\n\n%s", showLabel.getName(),
                                                                            modeAdd ? "zu folgenden Episoden hinzufügen" : "von folgenden Episoden entfernen",
                                                                            episodes.size(),
                                                                            episodes.size() == 1 ? "Episode" : "Episoden",
                                                                            selectedEpisodesString))
                                                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                                                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, confirmDialog -> {
                                                                        episodes.forEach(episode -> {
                                                                            if (modeAdd)
                                                                                episode.getShowLabelIds().add(showLabel.getUuid());
                                                                            else
                                                                                episode.getShowLabelIds().remove(showLabel.getUuid());
                                                                        });
                                                                        Database.saveAll(this, "Bulk-Edit erfolgreich", null, "Fehler beim Bearbeiten", bulkEditDialog::dismiss);
                                                                    })
                                                                    .show();
                                                        }, false)
                                                        .disableLastAddedButton()
                                                        .show();
                                            })
                                            .disableButtonAllCaps()
                                            .enableStackButtons()
                                            .show();

                                });
                    });

                    if (show.getImagePath() != null && !show.getImagePath().isEmpty()) {
                        ImageView dialog_show_poster = view.findViewById(R.id.dialog_detailShow_poster);
                        dialog_show_poster.setVisibility(View.VISIBLE);
                        Utility.loadUrlIntoImageView(this, dialog_show_poster, Utility.getTmdbImagePath_ifNecessary(show.getImagePath(), false), Utility.getTmdbImagePath_ifNecessary(show.getImagePath(), true), null, () -> Utility.roundImageView(dialog_show_poster, 2));
                    }


                    if (show.getLastUpdated() == null || System.currentTimeMillis() - show.getLastUpdated().getTime() > 86400000)
                        apiDetailRequest(this, show.getTmdbId(), show, customDialog::reloadView, true, true);
                })
                .addLifeCycleCallback(getLifeCycleCallback(show::getUuid))
                .show();
    }

    private CustomDialog showEditOrNewDialog(Show show) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);
        removeFocusFromSearch();

        final Show editShow = show == null ? new Show("") : show.clone();

        return CustomDialog.Builder(this)
                .setTitle(show == null ? "Neu: " + singular : singular + " Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_show)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog0 -> {
                    if (show != null) {
                        customDialog0.addButton(R.drawable.ic_delete, customDialog -> {
                                    CustomDialog.Builder(this)
                                            .setTitle("Löschen")
                                            .setText("Willst du wirklich '" + show.getName() + "' löschen?")
                                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                            .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                                database.showMap.remove(show.getUuid());
                                                reLoadRecycler();
                                                customDialog.dismiss();
                                                Object payload = customDialog.getPayload();
                                                if (payload != null) {
                                                    ((CustomDialog) payload).dismiss();
                                                }
                                                Database.saveAll(this, "Show gelöscht", null, null);
                                            })
                                            .show();
                                }, false)
                                .alignPreviousButtonsLeft();
                    }
                })
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    boolean checked = ((CheckBox) customDialog.findViewById(R.id.dialog_editOrAdd_show_watchLater)).isChecked();
                    saveShow(customDialog, checked, show, editShow);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    CustomList<Show> shows = new CustomList<>(database.showMap.values());
                    if (show != null)
                        shows.remove(show);
                    TextInputLayout dialog_editOrAdd_show_Title_layout = view.findViewById(R.id.dialog_editOrAdd_show_Title_layout);
                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(dialog_editOrAdd_show_Title_layout)
                            .addActionListener(dialog_editOrAdd_show_Title_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                apiSearchRequest(text, customDialog, editShow);
                            }, Helpers.TextInputHelper.IME_ACTION.SEARCH)
                            .setValidation(dialog_editOrAdd_show_Title_layout, (validator, text) -> {
                                if (shows.stream().anyMatch(show1 -> show1.getName().equalsIgnoreCase(text)))
                                    validator.setInvalid("Gleiche Serie schon vorhanden!");
                            });
                    AutoCompleteTextView dialog_editOrAdd_show_title = view.findViewById(R.id.dialog_editOrAdd_show_title);
                    if (!editShow.getName().isEmpty()) {
                        dialog_editOrAdd_show_title.setText(editShow.getName());
                        if (reload)
                            dialog_editOrAdd_show_title.dismissDropDown();


                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_show_Genre)).setText(
                                editShow.getGenreIdList().stream().map(uuid -> database.showGenreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAdd_show_Genre).setSelected(true);

                        if (editShow.getFirstAirDate() != null)
                            ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAdd_show_datePicker)).setDate(editShow.getFirstAirDate());

                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_show_details)).setText(getDetailText(editShow));


                        if (!reload && (editShow.getLastUpdated() == null || System.currentTimeMillis() - editShow.getLastUpdated().getTime() > 86400000))
                            apiDetailRequest(this, show.getTmdbId(), editShow, customDialog::reloadView, true, true);
                    } else {
//                        view.findViewById(R.id.dialog_editOrAdd_show_watchLater).setVisibility(View.VISIBLE);
                        if (CustomUtility.stringExists(searchQuery))
                            dialog_editOrAdd_show_title.setText(searchQuery);
                        dialog_editOrAdd_show_title.requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
//                        editShow = new Show();
                    }

                    Spinner dialog_editOrAdd_show_language = view.findViewById(R.id.dialog_editOrAdd_show_language);
                    dialog_editOrAdd_show_language.setSelection(Settings.getIndexByLanguage(this, editShow.getLanguage()));
                    dialog_editOrAdd_show_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            editShow.setLanguage(Settings.getLanguageByIndex(ShowActivity.this, position));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Spinner dialog_editOrAdd_show_requestImdbIdType = view.findViewById(R.id.dialog_editOrAdd_show_requestImdbIdType);
                    dialog_editOrAdd_show_requestImdbIdType.setSelection(Show.REQUEST_IMDB_ID_TYPE.indexOf(editShow.getRequestImdbIdType()));
                    dialog_editOrAdd_show_requestImdbIdType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            editShow.setRequestImdbIdType(Show.REQUEST_IMDB_ID_TYPE.byIndex(position));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    view.findViewById(R.id.dialog_editOrAdd_show_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, editShow.getGenreIdList(), CategoriesActivity.CATEGORIES.SHOW_GENRES, (customDialog1, selectedIds) -> {
                                editShow.setGenreIdList(selectedIds);
                                ((TextView) customDialog.findViewById(R.id.dialog_editOrAdd_show_Genre)).setText(
                                        CategoriesActivity.joinCategoriesIds(selectedIds, CategoriesActivity.CATEGORIES.SHOW_GENRES));
                            }));
                })
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String title = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_show_title)).getText().toString().trim();
                    if (show == null)
                        return (!title.isEmpty() && !title.equals(searchQuery)) || !editShow.getGenreIdList().isEmpty();
                    else
                        return !title.equals(show.getName()) || !editShow.equals(show);
                })
                .show();
    }

    private SpannableStringBuilder getDetailText(Show show) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!show.getSeasonList().isEmpty()) {
            builder.append("Staffeln: ", new StyleSpan(Typeface.BOLD_ITALIC), Spanned.SPAN_COMPOSING)
                    .append(String.valueOf(show.getSeasonsCount()));
        }
        if (show.getAllEpisodesCount() != -1) {
            if (!builder.toString().isEmpty())
                builder.append(", ");
            builder.append("Folgen: ", new StyleSpan(Typeface.BOLD_ITALIC), Spanned.SPAN_COMPOSING)
                    .append(String.valueOf(show.getAllEpisodesCount()));
        }
        if (show.hasAverageRuntime()) {
            if (!builder.toString().isEmpty())
                builder.append(", ");
            builder.append("je: ", new StyleSpan(Typeface.BOLD_ITALIC), Spanned.SPAN_COMPOSING)
                    .append(Helpers.DurationFormatter.formatDefault(Duration.ofMinutes(show.getAverageRuntime()), "'%h% h~ ~''%m% min~ ~'"));
        }
        return builder;
    }

    private void saveShow(CustomDialog dialog, boolean checked, Show show, Show editShow) {
        boolean neueShow = show == null;
        if (show == null)
            show = editShow;

//        show.getChangesFrom(editShow);
//        show.copy(editShow);
        if (show != editShow)
            show.getChangesFrom(editShow);
        show.setName(((AutoCompleteTextView) dialog.findViewById(R.id.dialog_editOrAdd_show_title)).getText().toString());
//        show.setStatus(editShow.getStatus());
//        show.setGenreIdList(editShow.getGenreIdList());
//        show.setSeasonList(editShow.getSeasonList());
//        show.setNextEpisodeAir(editShow.getNextEpisodeAir());
        show.setFirstAirDate(((LazyDatePicker) dialog.findViewById(R.id.dialog_editOrAdd_show_datePicker)).getDate());
//        show.setAllEpisodesCount(editShow.getAllEpisodesCount());
//        show.setSeasonsCount(editShow.getSeasonsCount());
//        show.setInProduction(editShow.isInProduction());
//        show.setTmdbId(editShow.getTmdbId());

        boolean upcomming = false;
        if (checked || (neueShow && (upcomming = show.isUpcoming())))
            database.showWatchLaterList.add(show.getUuid());

        database.showMap.put(show.getUuid(), show);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenteredToast(this, singular + " gespeichert" + (checked || upcomming ? "\n(Später ansehen)" : ""));

        if (dialog.getPayload() != null)
            ((CustomDialog) dialog.getPayload()).reloadView();

    }

    private static boolean alreadySeen(Show show, Show.Episode episode) {
        final boolean[] result = {false};
        show.getSeasonList().stream().filter(season -> episode.getSeasonNumber() == season.getSeasonNumber()).findFirst().ifPresent(season -> {
            result[0] = season.getEpisodeMap().containsKey("E:" + episode.getEpisodeNumber());
        });
        return result[0];
    }


    //  --------------- Season & Episode --------------->
    private CustomRecycler<Show.Season> showSeasonsDialog(Show show) {
        CustomDialog seasonDialog = CustomDialog.Builder(this);
        removeFocusFromSearch();

        CustomRecycler<Show.Season> seasonRecycler = new CustomRecycler<Show.Season>(this)
                .setGetActiveObjectList(customRecycler1 -> new CustomList<>(show.getSeasonList()).executeIf(seasons -> !seasons.isEmpty() && seasons.getFirst().getName().equals(Show.EMPTY_SEASON)
                        , CustomList::removeFirst))
                .setItemLayout(R.layout.list_item_season)
                .setSetItemContent((customRecycler, itemView, season, index) -> {
                    int watchedSize = (int) season.getEpisodeMap().values().stream().filter(Show.Episode::isWatched).count();
                    ((TextView) itemView.findViewById(R.id.listItem_season_number)).setText(String.valueOf(season.getSeasonNumber()));

                    int showEpisodePreview = Settings.getSingleSetting_int(this, Settings.SETTING_SHOW_EPISODE_PREVIEW);
                    MinDimensionLayout listItem_season_image_layout = itemView.findViewById(R.id.listItem_season_image_layout);
                    ImageView listItem_season_image = itemView.findViewById(R.id.listItem_season_image);
                    if (CustomUtility.stringExists(season.getImagePath()) && (showEpisodePreview == 0 || (showEpisodePreview == 1 && watchedSize > 0))) {
                        Utility.loadUrlIntoImageView(this, listItem_season_image, Utility.getTmdbImagePath_ifNecessary(season.getImagePath(), false), Utility.getTmdbImagePath_ifNecessary(season.getImagePath(), true), null, () -> Utility.roundImageView(listItem_season_image, 2));
                        listItem_season_image_layout.setVisibility(View.VISIBLE);
                    } else
                        listItem_season_image_layout.setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.listItem_season_name)).setText(season.getName());
                    if (season.getAirDate() != null)
                        ((TextView) itemView.findViewById(R.id.listItem_season_release)).setText(new SimpleDateFormat("(yyyy)", Locale.getDefault()).format(season.getAirDate()));
                    else
                        ((TextView) itemView.findViewById(R.id.listItem_season_release)).setText("");
                    Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                    if (watchedSize == 0) {
                        helper.append("Folgen:  ", Helpers.SpannableStringHelper.SPAN_TYPE.BOLD_ITALIC).append(String.valueOf(season.getEpisodesCount()));
                    } else {
                        helper.append("Gesehen:  ", Helpers.SpannableStringHelper.SPAN_TYPE.BOLD_ITALIC);
                        if (watchedSize < season.getEpisodesCount())
                            helper.append(watchedSize + " / " + season.getEpisodesCount());
                        else
                            helper.appendColor("Alle " + season.getEpisodesCount(), getColor(R.color.colorGreen));
                    }
                    ((TextView) itemView.findViewById(R.id.listItem_season_episodes)).setText(helper.get());

                    double averageRating = season.getEpisodeMap().values().stream().map(ParentClass_Ratable::_getRatingWithTendency).filter(Optional::isPresent).mapToDouble(optRating -> (double) optRating.get()).average().orElse(-1);
                    ((TextView) itemView.findViewById(R.id.listItem_season_rating)).setText(averageRating != -1 ? decimalFormat.format(averageRating) + " ☆" : "");
                })
                .setOnClickListener((customRecycler, itemView, season, index) -> {
                    Map<String, Show.Episode> episodeMap;
                    Map<Integer, Map<String, Show.Episode>> seasonEpisodeMap;

                    if ((seasonEpisodeMap = database.tempShowSeasonEpisodeMap.get(show.getUuid())) != null && (episodeMap = seasonEpisodeMap.get(season.getSeasonNumber())) != null)
                        showEpisodesDialog(season, episodeMap, customRecycler);
                    else
                        apiSeasonRequest(show, season.getSeasonNumber(), () -> {
                            showEpisodesDialog(season, database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(season.getSeasonNumber()), customRecycler);
                        });
                })
                .setOnLongClickListener((customRecycler, view, season, index) -> {
                    showResetDialog(new CustomList<>(((Show.Season) season).getEpisodeMap().values()), null, season, null, customRecycler, 2).setOnDialogDismiss(customDialog1 -> {
                        Database.saveAll();
                        reLoadRecycler();
                    });
                })
                .generate();

        seasonDialog
                .setTitle("Staffeln")
                .addButton("Historie", customDialog1 -> {
                    CustomDialog.Builder(this)
                            .setTitle("Ansichten")
                            .addButton("Ansichten-Historie", customDialog2 -> {
                                List<Pair<Date, Show.Episode>> list = new ArrayList<>();
                                for (Show.Season season : show.getSeasonList()) {
                                    season.getEpisodeMap().values().forEach(episode -> episode.getDateList()
                                            .forEach(date -> list.add(new Pair<>(date, episode))));
                                }
                                if (list.isEmpty()) {
                                    Toast.makeText(this, "Keine Ansichten eingetragen", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                list.sort((o1, o2) -> o1.first.compareTo(o2.first) * -1);
                                showViewHistoryDialog(list);

                                customDialog1.dismiss();
                            })
                            .addButton("Ansichten-Kalender", customDialog2 -> {
                                showCalenderDialog(show);
                                customDialog1.dismiss();
                            })
                            .enableExpandButtons()
                            .show();
                }, false)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                .setView(customDialog1 -> seasonRecycler, false)
                .disableScroll()
                .addLifeCycleCallback(getLifeCycleCallback(show::getUuid))
                .show();

        if (show.getLastUpdated() == null || System.currentTimeMillis() - show.getLastUpdated().getTime() > 86400000)
            apiDetailRequest(this, show.getTmdbId(), show, seasonDialog::reloadView, true, true);

        return seasonRecycler;
    }

    private CustomRecycler<Show.Episode> showEpisodesDialog(Show.Season season, Map<String, Show.Episode> episodeMap, CustomRecycler<Show.Season> seasonCustomRecycler) {
        if (episodeMap.isEmpty()) {
            Toast.makeText(this, "Staffel hat keine Episoden", Toast.LENGTH_SHORT).show();
            return null;
        }

        int initialHash = season.hashCode();
        final Show.Episode[] selectedEpisode = {null};
        Runnable addView = () -> {
            Show.Episode episode = selectedEpisode[0];
            episode.setWatched(true);
            season.getEpisodeMap().put("E:" + episode.getEpisodeNumber(), episode);
            boolean before = episode.addDate(new Date(), true, this);
            Utility.showCenteredToast(this, "Ansicht hinzugefügt" + (before ? "\nAutomatisch für gestern hinzugefügt" : ""));
            Show show = database.showMap.get(episode.getShowId());
            show.setAlreadyAiredList(show.getAlreadyAiredList().stream().filter(episode1 -> episode.getTmdbId() != episode1.getTmdbId()).collect(Collectors.toList()));

            Database.saveAll(() -> setResult(RESULT_OK));
        };

        Map<Show.Episode, CustomUtility.Triple<ImageView, Integer, Integer>> imageDimensions = new HashMap<>();

        CustomRecycler<Show.Episode> episodeRecycler = new CustomRecycler<Show.Episode>(this) {
            @Override
            public CustomRecycler<Show.Episode> reload() {
                for (CustomUtility.Triple<ImageView, Integer, Integer> triple : imageDimensions.values()) {
                    ImageView imageView = triple.first;
                    imageView.measure(0, 0);
                    triple.setSecond(imageView.getMeasuredWidth());
                    triple.setThird(imageView.getMeasuredHeight());
                }
                return super.reload();
            }
        }
                .setGetActiveObjectList(customRecycler -> {
                    ArrayList<Show.Episode> episodeList = new ArrayList<>(episodeMap.values());
                    episodeList.sort(Comparator.comparingInt(Show.Episode::getEpisodeNumber));
                    return episodeList;
                })
                .setItemLayout(R.layout.list_item_episode)
                .enableTrackReloading()
                .setOnReload(customRecycler -> {
                    for (CustomUtility.Triple<ImageView, Integer, Integer> triple : imageDimensions.values()) {
                        ImageView imageView = triple.first;
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        imageView.setLayoutParams(layoutParams);
                    }
                })
                .setSetItemContent((customRecycler, itemView, episode, index) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                    ImageView listItem_episode_image = itemView.findViewById(R.id.listItem_episode_image);
                    if (customRecycler.isReloading() && imageDimensions.containsKey(episode)) {
                        CustomUtility.Triple<ImageView, Integer, Integer> triple = imageDimensions.get(episode);
                        ViewGroup.LayoutParams layoutParams = listItem_episode_image.getLayoutParams();
                        layoutParams.width = triple.second;
                        layoutParams.height = triple.third;
                        listItem_episode_image.setLayoutParams(layoutParams);
                    }
                    imageDimensions.put(episode, CustomUtility.Triple.create(listItem_episode_image, listItem_episode_image.getWidth(), listItem_episode_image.getHeight()));
                    int showPreviewSetting = Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_SHOW_EPISODE_PREVIEW));
                    if (Utility.stringExists(episode.getStillPath()) && (showPreviewSetting == 0 || showPreviewSetting == 1 && episode.isWatched())) {
                        listItem_episode_image.setVisibility(View.VISIBLE);
                        Utility.loadUrlIntoImageView(this, listItem_episode_image, Utility.getTmdbImagePath_ifNecessary(episode.getStillPath(), false), Utility.getTmdbImagePath_ifNecessary(episode.getStillPath(), true), null, () -> Utility.roundImageView(listItem_episode_image, 2));
                    } else
                        listItem_episode_image.setVisibility(View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());

                    if (episode.hasAgeRating() || episode.hasLength()) {
                        itemView.findViewById(R.id.listItem_episode_extraInformation_layout).setVisibility(View.VISIBLE);
                        if (episode.hasAgeRating()) {
                            itemView.findViewById(R.id.listItem_episode_ageRating_layout).setVisibility(View.VISIBLE);
                            ((TextView) itemView.findViewById(R.id.listItem_episode_ageRating)).setText(episode.getAgeRating());
                        } else
                            itemView.findViewById(R.id.listItem_episode_ageRating_layout).setVisibility(View.GONE);

                        if (episode.hasLength()) {
                            itemView.findViewById(R.id.listItem_episode_length_layout).setVisibility(View.VISIBLE);
                            ((TextView) itemView.findViewById(R.id.listItem_episode_length)).setText(Utility.formatDuration(Duration.ofMinutes(episode.getLength()), "'%h% h~ ~''%m% min~ ~'"));
                        } else
                            itemView.findViewById(R.id.listItem_episode_length_layout).setVisibility(View.GONE);
                    } else
                        itemView.findViewById(R.id.listItem_episode_extraInformation_layout).setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(Utility.isNullReturnOrElse(episode.getAirDate(), "", date -> new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)));
                    ParentClass_Ratable.applyRatingTendencyIndicator(itemView.findViewById(R.id.listItem_episode_ratingTendency), episode, episode.isWatched(), false);
                    ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(!CustomUtility.boolOr(episode.getRating(), -1f, 0f) && episode.isWatched() ? episode.getRating() + " ☆" : "");
                    ((TextView) itemView.findViewById(R.id.listItem_episode_viewCount)).setText(
                            episode.getDateList().size() >= 2 || (!episode.getDateList().isEmpty() && !episode.isWatched()) ? "| " + episode.getDateList().size() : "");

                    Utility.applyToAllViews(itemView.findViewById(R.id.listItem_episode_detailLayout), View.class, view -> view.setAlpha(Utility.isNullReturnOrElse(episode.getAirDate(), true, date -> episode.isUpcomming()) && !episode.isWatched() ? 0.6f : 1f));

                    ImageView listItem_episode_seen = itemView.findViewById(R.id.listItem_episode_seen);
                    FrameLayout listItem_episode_seen_touchZone = itemView.findViewById(R.id.listItem_episode_seen_touchZone);
                    if (episode.isWatched()) {
                        listItem_episode_seen.setColorFilter(getColor(R.color.colorGreen), PorterDuff.Mode.SRC_IN);
                        listItem_episode_seen.setAlpha(1f);
                        listItem_episode_seen.setClickable(false);
                        listItem_episode_seen.setForeground(null);
                        listItem_episode_seen.setOnTouchListener((view, motionEvent) -> itemView.onTouchEvent(motionEvent));
                        listItem_episode_seen_touchZone.setOnTouchListener(null);
                    } else {
                        listItem_episode_seen.setColorFilter(getColor(R.color.colorDrawable), PorterDuff.Mode.SRC_IN);
                        listItem_episode_seen.setAlpha(0.2f);
                        listItem_episode_seen.setClickable(true);
                        listItem_episode_seen.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.ripple, null));
                        listItem_episode_seen.setOnTouchListener(null);
                        listItem_episode_seen_touchZone.setOnTouchListener((v, event) -> listItem_episode_seen.onTouchEvent(event));
                    }
                })
                .addSubOnClickListener(R.id.listItem_episode_seen, (customRecycler, itemView, episode, index) -> {
                    if (episode.isWatched())
                        return;
                    selectedEpisode[0] = episode;
                    if (!episode.hasAnyExtraDetails()) {
                        Runnable runnable = getImdbIdAndDetails(new CustomList<>(episode), false, () -> customRecycler.update(selectedEpisode[0]));
                        new Handler().postDelayed(() -> runnable.equals(null), 10000);
                    }
                    ParentClass_Ratable.showRatingDialog(this, selectedEpisode[0], itemView, true, true, () -> {
                        customRecycler.update(selectedEpisode[0]);
                        addView.run();
                    });
                })
                .addSubOnLongClickListener(R.id.listItem_episode_seen, (customRecycler, itemView, episode, index) -> {
                    if (episode.isWatched())
                        return;
                    selectedEpisode[0] = episode;
                    if (!episode.hasAnyExtraDetails()) {
                        Runnable runnable = getImdbIdAndDetails(new CustomList<>(episode), false, () -> customRecycler.update(selectedEpisode[0]));
                        new Handler().postDelayed(() -> runnable.equals(null), 10000);
                    }
                    addView.run();
                    customRecycler.update(selectedEpisode[0]);

                })
                .setOnClickListener((customRecycler, itemView, episode, index) -> {
                    showEpisodeDetailDialog(this, customRecycler, episode);
                })
                .setOnLongClickListener((customRecycler, view, episode, index) -> showResetDialog(Arrays.asList(episode), null, null, null, customRecycler, 3));

        CustomDialog.Builder(this)
                .setTitle(season.getName() + " - Folgen")
                .addGoToButton((CustomRecycler.GoToFilter<Show.Episode>) (search, episode) -> {
                    if (String.valueOf(episode.getEpisodeNumber()).equals(search)) {
                        return true;
                    }
                    return episode.getName().toLowerCase().contains(search.toLowerCase());
                }, episodeRecycler)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                .setView(customDialog -> episodeRecycler, true)
                .disableScroll()
                .setOnDialogDismiss(customDialog -> {
                    if (season.hashCode() == initialHash)
                        return;
                    Database.saveAll(() -> setResult(RESULT_OK));
                    reLoadRecycler();
                    if (seasonCustomRecycler != null)
                        seasonCustomRecycler.reload();
                })
                .addLifeCycleCallback(getLifeCycleCallback(season::getShowId))
                .show();
        return episodeRecycler;
    }

    private void applyEpisodeMap(Map<String, Show.Episode> oldMap, Map<String, Show.Episode> newMap) {
        for (Show.Episode oldEpisode : oldMap.values()) {
            Utility.ifNotNull(newMap.get(oldEpisode.getUuid()), newEpisode -> {
//                newEpisode.setDateList(oldEpisode.getDateList());
//                newEpisode.setWatched(oldEpisode.isWatched());
//                newEpisode.setRating(oldEpisode.getRating());
                oldEpisode.setStillPath(newEpisode.getStillPath());
                oldEpisode.setAirDate(newEpisode.getAirDate());
                oldEpisode.setName(newEpisode.getName());
                if (oldEpisode.getLength() <= 0)
                    oldEpisode.setLength(newEpisode.getLength());
            });
        }
        newMap.putAll(oldMap);
    }

    public static void showEpisodeDetailDialog(AppCompatActivity context, @Nullable CustomRecycler customRecycler, Show.Episode episode) {
        if (episode == null) {
            Toast.makeText(context, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
            return;
        }

        Database database = Database.getInstance();
//        setResult(RESULT_OK); // vielleich wichtig?
        final Runnable[] destroyGetImdbIdAndDetails = {null};

        int index = customRecycler != null ? customRecycler.getObjectList().indexOf(episode) : -1;
        Show.Episode editEpisode = episode.clone();
        editEpisode.setDateList(new ArrayList<>(episode.getDateList()));
        editEpisode.setShowLabelIds(new ArrayList<>(editEpisode.getShowLabelIds()));

        Utility.GenericReturnOnlyInterface<Boolean> hasChanges = () -> !episode.equals(editEpisode);

        CustomUtility.GenericInterface<CustomDialog> updateActionButton = customDialog -> customDialog.getActionButton().setEnabled(hasChanges.run());


        CustomDialog.Builder(context)
                .setTitle(editEpisode.getName())
                .setView(R.layout.dialog_detail_episode)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog -> {
                    if (episode.isWatched() || episode.getDateList().isEmpty()) {
                        return;
                    }
                    customDialog
                            .addButton("Ansicht", customDialog1 -> {
                                editEpisode.setWatched(true);
                                boolean before = editEpisode.addDate(new Date(), true, context);
                                Utility.showCenteredToast(context, "Ansicht hinzugefügt" + (before ? "\nAutomatisch für gestern hinzugefügt" : ""));
                                Show show = database.showMap.get(editEpisode.getShowId());
                                show.getAlreadyAiredList().removeIf(episode1 -> episode.getTmdbId() != episode1.getTmdbId());

                                // execute save
                                customDialog1.getButtonByType(CustomDialog.BUTTON_TYPE.SAVE_BUTTON).click();
                            })
                            .addIconDecorationToLastAddedButton(R.drawable.ic_add, CustomDialog.IconDecorationPosition.LEFT)
                            .addConfirmationDialogToLastAddedButton("Ansicht Hinzufügen", "Möchtest du wirklich eine neue Ansicht zu der Episode hinzufügen? Alle vorgenommenen Änderungen werden ebenfalls gespeichert.")
                            .alignPreviousButtonsLeft();
                })
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    episode.setRating(editEpisode.getRating());
                    if (episode.hasRating())
                        episode.setRatingTendency(editEpisode.getRatingTendency());
                    episode.setDateList(editEpisode.getDateList());
                    episode.setLength(editEpisode.getLength());
                    episode.setAgeRating(editEpisode.getAgeRating());
                    episode.setImdbId(editEpisode.getImdbId());
                    episode.setShowLabelIds(editEpisode.getShowLabelIds());
                    episode.setWatched(editEpisode.isWatched());

                    if (!episode.isWatched() && episode.getDateList().isEmpty()) {
                        Show show = database.showMap.get(editEpisode.getShowId());
                        Show.Season season = show.getSeasonList().get(editEpisode.getSeasonNumber());
                        episode.setWatched(true);
                        season.getEpisodeMap().put("E:" + episode.getEpisodeNumber(), episode);
                        boolean before = episode.addDate(new Date(), true, context);
                        Utility.showCenteredToast(context, "Ansicht hinzugefügt" + (before ? "\nAutomatisch für gestern hinzugefügt" : ""));
                        show.getAlreadyAiredList().removeIf(episode1 -> episode.getTmdbId() != episode1.getTmdbId());
                    }

                    Database.saveAll(() -> {
                        if (customRecycler != null)
                            customRecycler.update(index);
                        context.setResult(RESULT_OK);
                        Toast.makeText(context, "Episode gespeichert", Toast.LENGTH_SHORT).show();
                    });
                })
//                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    Show show = database.showMap.get(episode.getShowId());
                    if (show == null) {
                        Toast.makeText(context, "Fehler beim laden der Serie", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_title)).setText(editEpisode.getName());

                    String totalEpisodeNumber = editEpisode.getAbsoluteEpisodeNumber_withoutSpecials(show).map(number -> String.format(Locale.getDefault(), " (%d)", number)).orElse("");
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_number)).setText(
                            editEpisode.getEpisodeNumber() + totalEpisodeNumber
                    );

                    ImageView listItem_episode_image = view.findViewById(R.id.dialog_detailEpisode_preview);
                    Utility.simpleLoadUrlIntoImageView(context, listItem_episode_image, null, editEpisode.getStillPath(), editEpisode.getStillPath(), 3);

                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_ageRating)).setText(editEpisode.getAgeRating());
                    TextView dialog_detailEpisode_ageRating_label = view.findViewById(R.id.dialog_detailEpisode_ageRating_label);
                    dialog_detailEpisode_ageRating_label.setTextColor(Utility.stringExists(editEpisode.getAgeRating()) ? context.getColorStateList(R.color.clickable_text_color_normal) : context.getColorStateList(R.color.clickable_text_color));
                    dialog_detailEpisode_ageRating_label.setOnClickListener(v -> {
                        CustomDialog.Builder(context)
                                .setTitle("Altersfreigabe " + (Utility.stringExists(editEpisode.getAgeRating()) ? "Ändern" : "Hinzufügen"))
                                .setEdit(new CustomDialog.EditBuilder()
                                        .setInputType(com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.CAPS_LOCK)
                                        .setHint("Altersfreigabe")
                                        .setText(Utility.stringExistsOrElse(editEpisode.getAgeRating(), ""))
                                        .setRegEx("^0|6|12|16|18$|^TV-(Y|G|Y7|PG|14|MA)$"))
                                .addOptionalModifications(customDialog1 -> {
                                    if (Utility.stringExists(editEpisode.getAgeRating()))
                                        customDialog1
                                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog2 -> {
                                                    editEpisode.setAgeRating(null);
                                                    Toast.makeText(context, "Altersfreigabe gelöscht", Toast.LENGTH_SHORT).show();
                                                    customDialog.reloadView();
                                                })
                                                .transformLastAddedButtonToImageButton()
                                                .alignPreviousButtonsLeft();
                                })
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                    editEpisode.setAgeRating(customDialog1.getEditText());
                                    customDialog.reloadView();
                                })
                                .disableLastAddedButton()
                                .show();
                    });

                    CharSequence episodeLength;
                    if (editEpisode.hasLength())
                        episodeLength = Utility.formatDuration(Duration.ofMinutes(editEpisode.getLength()), "'%h% h~ ~''%m% min~ ~'");
                    else if (show.hasAverageRuntime())
                        episodeLength = com.finn.androidUtilities.Helpers.SpannableStringHelper.quickColor(
                                Helpers.DurationFormatter.formatDefault(Duration.ofMinutes(show.getAverageRuntime()), "'%h% h~ ~''%m% min~ ~'"),
                                CustomUtility.setAlphaOfColor(context.getColor(R.color.colorText), 100));
                    else
                        episodeLength = "";
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_length)).setText(episodeLength);
                    TextView dialog_detailEpisode_length_label = view.findViewById(R.id.dialog_detailEpisode_length_label);
                    dialog_detailEpisode_length_label.setTextColor(editEpisode.getLength() != -1 ? context.getColorStateList(R.color.clickable_text_color_normal) : context.getColorStateList(R.color.clickable_text_color));
                    dialog_detailEpisode_length_label.setOnClickListener(v -> {
                        CustomDialog.Builder(context)
                                .setTitle("Länge " + (editEpisode.getLength() != -1 ? "Ändern" : "Hinzufügen"))
                                .setEdit(new CustomDialog.EditBuilder()
                                        .setInputType(com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.NUMBER)
                                        .setHint("Länge")
                                        .setText(editEpisode.getLength() == -1 ? "" : String.valueOf(editEpisode.getLength()))
                                        .setRegEx("\\d+"))
                                .addOptionalModifications(customDialog1 -> {
                                    if (editEpisode.getLength() != -1)
                                        customDialog1
                                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog2 -> {
                                                    editEpisode.setLength(-1);
                                                    Toast.makeText(context, "Länge gelöscht", Toast.LENGTH_SHORT).show();
                                                    customDialog.reloadView();
                                                })
                                                .transformLastAddedButtonToImageButton()
                                                .alignPreviousButtonsLeft();
                                })
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                    editEpisode.setLength(Integer.parseInt(customDialog1.getEditText()));
                                    customDialog.reloadView();
                                })
                                .disableLastAddedButton()
                                .show();
                    });

                    if (context instanceof ShowActivity) {
                        view.findViewById(R.id.dialog_detailEpisode_sync).setOnClickListener(v -> {
                            ((ShowActivity) context).getImdbIdAndDetails(new CustomList<>(editEpisode), true, customDialog::reloadView);
                        });
                        view.findViewById(R.id.dialog_detailEpisode_internet).setOnLongClickListener(v -> {
                            CustomDialog.Builder(context)
                                    .setTitle("IMDB-ID " + (Utility.stringExists(editEpisode.getImdbId()) ? "Ändern" : "Hinzufügen"))
                                    .setEdit(new CustomDialog.EditBuilder()
                                            .setInputType(com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.TEXT)
                                            .setHint("IMDB-ID")
                                            .setText(Utility.stringExistsOrElse(editEpisode.getImdbId(), ""))
                                            .setRegEx(Utility.imdbPattern_full))
                                    .addOptionalModifications(customDialog1 -> {
                                        if (Utility.stringExists(editEpisode.getImdbId()))
                                            customDialog1
                                                    .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog2 -> {
                                                        editEpisode.setImdbId(null);
                                                        Toast.makeText(context, "IMDB-ID gelöscht", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .transformLastAddedButtonToImageButton()
                                                    .alignPreviousButtonsLeft();

                                    })
                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                    .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                        editEpisode.setImdbId(customDialog1.getEditText());
                                        Toast.makeText(context, "IMDB-ID gespeichert", Toast.LENGTH_SHORT).show();
                                        Database.saveAll();
                                    })
                                    .disableLastAddedButton()
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        EditText editFilmId = customDialog1.findViewById(R.id.dialog_custom_edit);
                                        editFilmId.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                if (s.toString().matches("https://.*" + Utility.imdbPattern + ".*")) {
                                                    Matcher matcher = Pattern.compile(Utility.imdbPattern).matcher(s);
                                                    if (matcher.find())
                                                        editFilmId.setText(matcher.group(0));
                                                }
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {
                                            }
                                        });
                                    })
                                    .show();
                            return true;
                        });
                    } else
                        view.findViewById(R.id.dialog_detailEpisode_sync).setVisibility(View.GONE);
                    view.findViewById(R.id.dialog_detailEpisode_internet).setOnClickListener(v -> {
                        String tmdbUrl = String.format(Locale.getDefault(), "https://www.themoviedb.org/tv/%d/season/%d/editEpisode/%d", database.showMap.get(editEpisode.getShowId()).getTmdbId(), editEpisode.getSeasonNumber(), editEpisode.getEpisodeNumber());
                        if (!Utility.stringExists(editEpisode.getImdbId()))
                            Utility.openUrl(context, tmdbUrl, true);
                        else
                            CustomDialog.Builder(context)
                                    .setTitle("Öffnen mit...")
                                    .addButton("TMDb", customDialog1 -> Utility.openUrl(context, tmdbUrl, true))
                                    .addButton("IMDB", customDialog1 -> Utility.openUrl(context, "https://www.imdb.com/title/" + editEpisode.getImdbId(), true))
                                    .enableExpandButtons()
                                    .enableButtonDividerAll()
                                    .show();

                    });

                    TextView viewsTextView = view.findViewById(R.id.dialog_detailEpisode_views);
                    Utility.GenericInterface<Boolean> setViewsText = flip -> {
                        Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                        SpannableStringBuilder viewsText = helper.quickItalic("Keine Ansichten");
                        if (editEpisode.getDateList().size() > 0) {
                            Date lastWatched = new CustomList<>(editEpisode.getDateList()).getBiggest();
                            helper.append(String.valueOf(editEpisode.getDateList().size()));
                            long days = Days.daysBetween(new LocalDate(lastWatched), new LocalDate(new Date())).getDays();
                            if (viewsTextView.getText().toString().contains("–") == flip)
                                viewsText = helper.appendItalic(String.format(Locale.getDefault(), "   (%s)", Helpers.DurationFormatter.formatDefault(Duration.ofDays(days), "'%y% Jahr§e§~, ~''%w% Woche§n§~, ~''%d% Tag§e§~, ~'"))).get();
                            else
                                viewsText = helper.appendItalic(String.format(Locale.getDefault(), "   (%s – %dd)", new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(lastWatched), days)).get();
                        }
                        viewsTextView.setText(viewsText);
                    };
                    setViewsText.run(!reload);
                    if (editEpisode.getDateList().size() > 0) {
                        viewsTextView.setOnClickListener(v -> setViewsText.run(true));
                        viewsTextView.setClickable(true);
                    } else
                        viewsTextView.setClickable(false);

                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_release)).setText(Utility.isNullReturnOrElse(editEpisode.getAirDate(), "", date -> new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)));


                    // labels
                    TextView labelsText = view.findViewById(R.id.dialog_detailEpisode_labels_text);
                    ViewGroup labelsLayout = view.findViewById(R.id.dialog_detailEpisode_labels_layout);
                    List<ShowLabel> showLabels = show.getShowLabels();
                    Runnable setLabelText = () -> labelsText.setText(editEpisode.getShowLabels().stream().map(ParentClass::getName).collect(Collectors.joining(", ")));
                    setLabelText.run();

                    labelsLayout.setOnClickListener(v -> {
                        Utility.showEditItemDialog(context, editEpisode.getShowLabelIds(), CategoriesActivity.CATEGORIES.SHOW_LABEL, show::getShowLabels,
                                (customDialog1, newIds) -> {
                                    editEpisode.getShowLabelIds().clear();
                                    editEpisode.getShowLabelIds().addAll(newIds);
                                    setLabelText.run();
                                    updateActionButton.run(customDialog);
                                },
                                id -> showLabels.stream().filter(showLabel -> showLabel.getUuid().equals(id)).findFirst().orElse(null),
                                (name, url) -> {
                                    ShowLabel showLabel = new ShowLabel(name);
                                    showLabels.add(showLabel);
                                    return showLabel;
                                },
                                null, (selectDialog, customRecycler1, parentClass) -> {
                                    CategoriesActivity.showEditCategoryDialog(context, CategoriesActivity.CATEGORIES.SHOW_LABEL, parentClass, parentClass1 -> {
                                        selectDialog.dismiss();
                                        setLabelText.run();
                                    }, null, null, null, parentClass1 -> {
                                        customDialog.dismiss();
                                        selectDialog.dismiss();
                                        show.getSeasonList().stream().flatMap(season -> season.getEpisodeMap().values().stream()).forEach(episode1 -> episode1.getShowLabelIds().remove(parentClass.getUuid()));
                                        setLabelText.run();
                                        show.getShowLabels().remove(((ShowLabel) parentClass));
                                        Database.saveAll(context, "Gelöscht", null, "Löschen fehlgeschlagen");
                                    }, s -> show.getShowLabels().stream().filter(showLabel -> showLabel.getName().equalsIgnoreCase(s)).findFirst().orElse(null));
                                });
                    });


                    // ratingTendency
                    View ratingTendencyButton = view.findViewById(R.id.dialog_detailEpisode_ratingTendency);
                    Runnable setRatingTendencyIcon = () -> ParentClass_Ratable.applyRatingTendencyIndicator(view.findViewById(R.id.dialog_detailEpisode_ratingTendency_icon), editEpisode, true, true);
                    setRatingTendencyIcon.run();
                    ratingTendencyButton.setOnClickListener(v -> {
                        ParentClass_Ratable.showRatingTendencyDialog(context, editEpisode, ratingTendencyButton, null, parentClass_ratable -> {
                            updateActionButton.run(customDialog);
                            setRatingTendencyIcon.run();
                        });
                    });

                    RatingBar dialog_detailEpisode_rating = new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout)).setRating(editEpisode.getRating()).getRatingBar();
                    dialog_detailEpisode_rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        editEpisode.setRating(rating);
                        updateActionButton.run(customDialog);
                    });

                    view.findViewById(R.id.dialog_detailEpisode_editViews).setOnClickListener(v -> showEpisodeCalenderDialog(context, editEpisode, customDialog));
                    if (context instanceof ShowActivity) {
                        view.findViewById(R.id.dialog_detailEpisode_editViews).setOnLongClickListener(v -> {
                            ((ShowActivity) context).showResetDialog(Arrays.asList(editEpisode), null, null, customDialog, null, 3);
                            return true;
                        });
                    }

                    updateActionButton.run(customDialog);
                })
                .enableDoubleClickOutsideToDismiss(customDialog -> hasChanges.run())
                .addOnDialogShown(customDialog -> {
                    if (context instanceof ShowActivity) {
                        if (!editEpisode.hasAnyExtraDetails()) {
                            destroyGetImdbIdAndDetails[0] = ((ShowActivity) context).getImdbIdAndDetails(new CustomList<>(editEpisode), false, customDialog::reloadView);
                        }
                    }
                })
                .addOnDialogDismiss(customDialog -> {
                    Utility.runRunnable(destroyGetImdbIdAndDetails[0]);
                    if (customRecycler != null && hasChanges.run())
                        customRecycler.update(index);
                })
                .addLifeCycleCallback(getLifeCycleCallback((customDialog, change) -> {
                    if (change instanceof Show && ((Show) change).getUuid().equals(editEpisode.getShowId())) {
                        Show.Episode savedEpisode = database.showMap.get(editEpisode.getShowId()).getSeasonList().get(editEpisode.getSeasonNumber()).getEpisodeMap().get("E:" + editEpisode.getEpisodeNumber());
                        showEpisodeDetailDialog(context, customRecycler, savedEpisode);
                        new Handler().postDelayed(customDialog::dismiss, 1000);
                    }
                }/*, customDialog -> {
                    currentRating[0] = editEpisode.getRating();
                    TextView textView = customDialog.findViewById(R.id.dialog_detailEpisode_views);
                    textView.setText(textView.getText().toString().contains("–") ? "" : "–");
                }*/, null))
                .show();
    }

    private CustomDialog showResetDialog(List<Show.Episode> episodeList_all, Show show, Show.Season season, CustomDialog customDialog, CustomRecycler customRecycler, int type) {
        // 1: Show; 2: Season; 3: Episode
        List<Show.Episode> episodeList = episodeList_all.stream().filter(episode -> !episode.getDateList().isEmpty()).collect(Collectors.toList());
        CustomDialog returnDialog = CustomDialog.Builder(this);
        if (episodeList.isEmpty())
            return returnDialog;


        return returnDialog
                .setTitle("Zurücksetzen")
                .setText("Willst du nur " + (type != 3 ? "die" : "den") + " Status, oder die Komplette " +
                        Utility.SwitchExpression.setInput(type)
                                .addCase(1, "Serie")
                                .addCase(2, "Staffel")
                                .addCase(3, "Folge")
                                .evaluate() + " zurücksetzen?")
                .addButton("Nur Status", customDialog1 -> {
                    CustomDialog.Builder(this)
                            .setTitle("Status Zurücksetzen")
                            .setText(Utility.SwitchExpression.setInput(type)
                                    .addCase(1, integer -> "Die Status der kompletten Serie *'" + show.getName() + "'* werden")
                                    .addCase(2, integer -> "Die Status der kompletten Staffel *'" + season.getName() + "'* werden")
                                    .addCase(3, integer -> "Der Status der Episode *'" + episodeList.get(0).getName() + "'* wird")
                                    .evaluate() + " auf *'ungesehen'* gesetzt")
                            .enableTextFormatting()
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog2 -> {
                                for (Show.Episode episode : episodeList)
                                    episode.setWatched(false);

                                if (customDialog != null)
                                    customDialog.reloadView();

                                if (customRecycler != null)
                                    customRecycler.reload();
                                customDialog1.dismiss();
                                Database.saveAll();
                            })
                            .show();
                }, false)
                .addButton("Alles", customDialog1 -> {
                    CustomDialog.Builder(this)
                            .setTitle("Komplett Zurücksetzen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                            .setText("Bist du sicher, dass du die " +
                                    Utility.SwitchExpression.setInput(type)
                                            .addCase(1, integer -> "Serie *'" + show.getName())
                                            .addCase(2, integer -> "Staffel *'" + season.getName())
                                            .addCase(3, integer -> "Episode *'" + episodeList.get(0).getName())
                                            .evaluate() + "'* _komplett zurücksetzen_ willst?")
                            .enableTextFormatting()
                            .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog2 -> {
                                for (Show.Episode episode : episodeList) {
                                    episode.getDateList().clear();
                                    episode.setWatched(false);
                                    episode.setRating(-1f);
                                    database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber()).getEpisodeMap().remove(episode.getUuid());
                                }
                                if (customDialog != null)
                                    customDialog.reloadView();

                                if (customRecycler != null)
                                    customRecycler.reload();
                                customDialog1.dismiss();
                                Database.saveAll();
                            })
                            .show();

                }, false)
                .enableExpandButtons()
                .show();
    }

    public void showCalenderDialog(Show show) {
        com.finn.androidUtilities.CustomDialog.Builder(this)
                .setTitle(show.getName() + " - Kalender")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    Utility.applyDialogChangeCallback(show, customDialog, this::reLoadRecycler);

                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    List<Show.Episode> episodeList = new ArrayList<>();
                    for (Show.Season season : show.getSeasonList()) {
                        episodeList.addAll(season.getEpisodeMap().values());
                    }
                    Utility.setupEpisodeCalender(this, calendarView, ((FrameLayout) view), episodeList, true, false);
                })
                .disableScroll()
                .enableTitleBackButton()
                .setDimensions(true, true)
                .show();

    }

    public static void showEpisodeCalenderDialog(AppCompatActivity context, Show.Episode episode, CustomDialog detailDialog) {
        boolean editMode = context instanceof ShowActivity;
        int viewCount = episode.getDateList().size();
        com.finn.androidUtilities.CustomDialog.Builder(context)
                .setTitle("Ansichten" + (editMode ? " Bearbeiten" : ""))
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupEpisodeCalender(context, calendarView, ((FrameLayout) view), Arrays.asList(episode), false, editMode);
                })
                .disableScroll()
                .setDimensions(true, true)
                .setOnDialogDismiss(customDialog -> {
                    Map<String, Show.Episode> episodeMap = Database.getInstance().showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber()).getEpisodeMap();
                    if (episode.getDateList().isEmpty()) {
                        episode.setWatched(false);
                        episode.setRating(-1f);
                        episodeMap.remove(episode.getUuid());
                    } else {
                        if (!episodeMap.containsKey(episode.getUuid()))
                            episodeMap.put(episode.getUuid(), episode);
                        if (viewCount < episode.getDateList().size())
                            episode.setWatched(true);
                    }
                    detailDialog.reloadView();
                })
                .enableTitleBackButton()
                .show();
    }

    public void findEpisode(Show.Episode episode, Runnable onFinished) {
        Show show = database.showMap.get(episode.getShowId());
        CustomRecycler<Show.Season> seasonRecycler = showSeasonsDialog(show);
        int seasonNumber = episode.getSeasonNumber();
        if (database.tempShowSeasonEpisodeMap.get(show.getUuid()) == null) {
            apiSeasonRequest(show, seasonNumber, () -> {
                showEpisodesDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");
                onFinished.run();
            });
        } else {
            showEpisodesDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");
            onFinished.run();
        }

//        if (episode.equals(list.get(0).second)) {
//            apiSeasonRequest(show, seasonNumber, () -> {
//                showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo(episode);
//            });
//        } else
//            showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");

    }
    //  <--------------- Season & Episode ---------------


    //  --------------- Api --------------->
    private void apiSearchRequest(String queue, CustomDialog customDialog, Show show) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/search/tv?api_key=09e015a2106437cbc33bf79eb512b32d&language=" +
                Utility.SwitchExpression.setInput(show.getLanguage()).addCase(null, Settings.getDefaultLanguage()).setDefault(show.getLanguage()).evaluate() +
                "&query=" +
                queue +
                "&page=1";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Toast toast = Toast.makeText(this, "Einen Moment bitte..", Toast.LENGTH_LONG);
        toast.show();

        CustomList<Pair<String, JSONObject>> jsonObjectList = new CustomList<>();
        AutoCompleteTextView dialog_editOrAddShow_Titel = customDialog.findViewById(R.id.dialog_editOrAdd_show_title);

        dialog_editOrAddShow_Titel.setOnItemClickListener((parent, view2, position, id) -> {
            String text = ((ImageAdapterItem) parent.getItemAtPosition(position)).getText();
            JSONObject jsonObject = jsonObjectList.stream().filter(stringJSONObjectPair -> stringJSONObjectPair.first.equals(text)).findFirst().orElse(jsonObjectList.get(position)).second;
            try {
                show.setFirstAirDate(Utility.getDateFromJsonString("first_air_date", jsonObject))
                        .setTmdbId(jsonObject.getInt("id")).setName(jsonObject.getString("name"));
                JSONArray genre_ids = jsonObject.getJSONArray("genre_ids");
                CustomList<Integer> integerList = new CustomList<>();
                for (int i = 0; i < genre_ids.length(); i++) {
                    integerList.add(genre_ids.getInt(i));
                }

                List<String> genreIdList = integerList.stream().map(tmdbId -> database.showGenreMap.values().stream().filter(showGenre -> Objects.equals(showGenre.getTmdbGenreId(), tmdbId)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .map(com.finn.androidUtilities.ParentClass::getUuid)
                        .collect(Collectors.toList());

                show.setGenreIdList(genreIdList);
                apiDetailRequest(this, show.getTmdbId(), show, customDialog::reloadView, false, false);
                Utility.getImdbIdFromTmdbId(this, show.getTmdbId(), "show", s -> {
                    if (Utility.stringExists(s))
                        show.setImdbId(s);
                });
                customDialog.reloadView();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            toast.cancel();
            JSONArray results;
            try {
                results = response.getJSONArray("results");

                if (results.length() == 0) {
                    Toast.makeText(this, "Keine Ergebnisse gefunden", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);

                    String release = "";
                    if (object.has("first_air_date")) {
                        release = object.getString("first_air_date");
                        if (!release.isEmpty())
                            release = String.format(" (%s)", release.substring(0, 4));
                    }
                    jsonObjectList.add(new Pair<>(object.getString("name") + release, object));
                }

                CustomList<ImageAdapterItem> itemList = jsonObjectList.map(stringJSONObjectPair -> {
                    ImageAdapterItem adapterItem = new ImageAdapterItem(stringJSONObjectPair.first);
                    if (stringJSONObjectPair.second.has("poster_path")) {
                        try {
                            adapterItem.setImagePath(stringJSONObjectPair.second.getString("poster_path"));
                        } catch (JSONException ignored) {
                        }
                    }
                    return adapterItem;
                });

                CustomAutoCompleteAdapter autoCompleteAdapter = new CustomAutoCompleteAdapter(this, itemList);

                dialog_editOrAddShow_Titel.setAdapter(autoCompleteAdapter);
                dialog_editOrAddShow_Titel.showDropDown();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            toast.cancel();
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);

    }

    public static void apiDetailRequest(AppCompatActivity activity, int id, Show show, Runnable onFinish, boolean update, boolean saveOnCompletion) {
        if (!Utility.isOnline(activity))
            return;

        String requestUrl = "https://api.themoviedb.org/3/tv/" +
                id +
                "?api_key=09e015a2106437cbc33bf79eb512b32d&language=" +
                Utility.SwitchExpression.setInput(show.getLanguage()).addCase(null, Settings.getDefaultLanguage()).setDefault(show.getLanguage()).evaluate();
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        Toast toast = Toast.makeText(activity, "Details werden geladen..", Toast.LENGTH_LONG);
        if (!update)
            toast.show();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            toast.cancel();
            try {
                // check changed seasons
                Set<Integer> watchedSeasonsIdSet = show.getSeasonList().stream().filter(season -> !season.getEpisodeMap().isEmpty()).map(ParentClass_Tmdb::getTmdbId).collect(Collectors.toSet());
                Set<Integer> newSeasonsIdSet = new HashSet<>();
                JSONArray seasons = response.getJSONArray("seasons");
                for (int i = 0; i < seasons.length(); i++)
                    newSeasonsIdSet.add(seasons.getJSONObject(i).getInt("id"));
                if (!newSeasonsIdSet.containsAll(watchedSeasonsIdSet)) {
                    CustomDialog.Builder(activity)
                            .setTitle("Probleme Beim Updaten")
                            .setText("Es wurde festgestellt, dass eine bereits angesehene Season in der aktuellen Form nicht mehr in der geupdateten Show vorhanden ist. " +
                                    "Somit würden bei dem Update die darin beinhalteten Episoden verworfen werden.\n\n" +
                                    "Um dies zu verhindern wurde der Updateprozess abgebrochen.")
//                            .addButton("Reparieren", customDialog -> {
//                                watchedSeasonsIdSet.removeAll(newSeasonsIdSet);
//                                show.getSeasonList();
//                                Map<String, Map<Integer, Map<String, Show.Episode>>> tempShowSeasonEpisodeMap = Database.getInstance().tempShowSeasonEpisodeMap;
//                            })
                            .alignPreviousButtonsLeft()
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON)
                            .show();
                    return;
                }

                if (response.has("number_of_seasons"))
                    show.setSeasonsCount(response.getInt("number_of_seasons"));
                if (response.has("number_of_episodes"))
                    show.setAllEpisodesCount(response.getInt("number_of_episodes"));
                if (response.has("in_production"))
                    show.setInProduction(response.getBoolean("in_production"));
                if (response.has("next_episode_to_air") && !response.isNull("next_episode_to_air")) {
                    JSONObject lastEpisode = response.getJSONObject("next_episode_to_air");
                    String nextEpisodeString = String.format(Locale.getDefault(), "%d|%d|%s|%s",
                            lastEpisode.getInt("season_number"),
                            lastEpisode.getInt("episode_number"),
                            lastEpisode.getString("air_date"),
                            lastEpisode.getString("name").replaceAll("\\|", "\\\\|"));
                    show.setNextEpisode(nextEpisodeString);
                    show.setNextEpisodeAir(Utility.getDateFromJsonString("air_date", response.getJSONObject("next_episode_to_air")));
                } else
                    show.setNextEpisodeAir(null);
                if (response.has("last_episode_to_air") && !response.isNull("last_episode_to_air")) {
                    JSONObject lastEpisode = response.getJSONObject("last_episode_to_air");
                    String lastEpisodeString = String.format(Locale.getDefault(), "%d|%d|%s|%s", lastEpisode.getInt("season_number"), lastEpisode.getInt("episode_number"), lastEpisode.getString("air_date"), lastEpisode.getString("name").replaceAll("\\|", "\\\\|"));
                    show.setLatestEpisode(lastEpisodeString);
                } else
                    show.setLatestEpisode(null);
                if (response.has("status"))
                    show.setStatus(response.getString("status"));
                if (response.has("poster_path"))
                    show.setImagePath(response.getString("poster_path"));
                JSONArray episode_run_time;
                if (response.has("episode_run_time") && (episode_run_time = response.getJSONArray("episode_run_time")).length() > 0) {
                    int sum = 0;
                    for (int i = 0; i < episode_run_time.length(); i++)
                        sum += episode_run_time.getInt(i);
                    show.setAverageRuntime(sum / episode_run_time.length());
                }

                JSONArray seasonArray_json = response.getJSONArray("seasons");
                List<Show.Season> seasonList = new ArrayList<>();
                for (int i = 0; i < seasonArray_json.length(); i++) {
                    JSONObject season_json = seasonArray_json.getJSONObject(i);
                    Show.Season season = new Show.Season(season_json.getString("name")).setShowId(show.getUuid());
                    if (season_json.has("season_number"))
                        season.setSeasonNumber(season_json.getInt("season_number"));
                    if (season_json.has("air_date"))
                        season.setAirDate(Utility.getDateFromJsonString("air_date", season_json));
                    if (season_json.has("id"))
                        season.setTmdbId(season_json.getInt("id"));
                    if (season_json.has("episode_count"))
                        season.setEpisodesCount(season_json.getInt("episode_count"));
                    if (season_json.has("poster_path"))
                        season.setImagePath(season_json.getString("poster_path"));
                    seasonList.add(season);
                }

                show.getSeasonList().forEach(season -> seasonList.stream().filter(season1 -> season.getTmdbId() == season1.getTmdbId())
                        .findFirst().ifPresent(newSeason -> {
                            newSeason.setEpisodeMap(season.getEpisodeMap());
                            newSeason.setUuid(season.getUuid());
                        }));


                show.setSeasonList(seasonList);

                if (show.isNotifyNew() && !response.isNull("last_episode_to_air")) {
                    Show.Episode latest = jsonToEpisode(show, null, response.getJSONObject("last_episode_to_air"));
                    if (show.getAlreadyAiredList().stream().noneMatch(episode -> episode.createRaw().equals(latest)) && !alreadySeen(show, latest)) {
                        show.getAlreadyAiredList().add(latest);
                        activity.setResult(RESULT_OK);
                        Database.saveAll();
                        if (activity instanceof ShowActivity)
                            ((ShowActivity) activity).reLoadRecycler();
                    }
                }

                show.setLastUpdated(new Date());
                onFinish.run();
                if (saveOnCompletion)
                    Database.saveAll();
                if (activity instanceof ShowActivity)
                    ((ShowActivity) activity).reLoadRecycler();
                else if (activity instanceof MainActivity)
                    MainActivity.setCounts(((MainActivity) activity));
            } catch (JSONException e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            toast.cancel();
            Toast.makeText(activity, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);

    }

    private void apiSeasonRequest(Show show, int seasonNumber, Runnable onLoaded) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/tv/" + show.getTmdbId() + "/season/" + seasonNumber + "?api_key=09e015a2106437cbc33bf79eb512b32d&language=" +
                Utility.SwitchExpression.setInput(show.getLanguage()).addCase(null, Settings.getDefaultLanguage()).setDefault(show.getLanguage()).evaluate();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            try {
                JSONArray episodes_json = response.getJSONArray("episodes");
                Map<String, Show.Episode> episodeMap = new HashMap<>();
                for (int i = 0; i < episodes_json.length(); i++) {
                    JSONObject episode_json = episodes_json.getJSONObject(i);
                    jsonToEpisode(show, episodeMap, episode_json);
                }

                Show.Season season = show.getSeasonList().get(seasonNumber);
                applyEpisodeMap(season.getEpisodeMap(), episodeMap);
                season._getImdbIdBuffer_List().forEachCount((s, count) -> {
                    Show.Episode episode = episodeMap.get("E:" + (count + 1));
                    if (episode != null && !Utility.stringExists(episode.getImdbId()))
                        episode.setImdbId(s);
                });

                Map<Integer, Map<String, Show.Episode>> map;
                if (database.tempShowSeasonEpisodeMap.containsKey(show.getUuid()))
                    map = database.tempShowSeasonEpisodeMap.get(show.getUuid());
                else
                    map = new HashMap<>();
                map.put(seasonNumber, episodeMap);
                database.tempShowSeasonEpisodeMap.put(show.getUuid(), map);
                onLoaded.run();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, error -> {
//            loadingWindow.dismiss();
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);

    }

    private void getTempSeason(Show show, int seasonNumber, Utility.GenericInterface<Map<String, Show.Episode>> onTempSeason) {
        Map<String, Show.Episode> episodeMap;
        Map<Integer, Map<String, Show.Episode>> seasonEpisodeMap;
        if ((seasonEpisodeMap = database.tempShowSeasonEpisodeMap.get(show.getUuid())) != null && (episodeMap = seasonEpisodeMap.get(seasonNumber)) != null)
            onTempSeason.run(episodeMap);
        else
            apiSeasonRequest(show, seasonNumber, () -> {
                onTempSeason.run(database.tempShowSeasonEpisodeMap.get(show.getUuid()).get(seasonNumber));
            });
    }

    private static Show.Episode jsonToEpisode(Show show, @Nullable Map<String, Show.Episode> episodeMap, JSONObject episode_json) {
        try {
            int episodeNumber = episode_json.getInt("episode_number");
            Show.Episode episode = (Show.Episode)
                    new Show.Episode(episode_json.getString("name"))
                            .setAirDate(Utility.getDateFromJsonString("air_date", episode_json))
                            .setEpisodeNumber(episodeNumber)
                            .setTmdbId(episode_json.getInt("id"))
                            .setShowId(show.getUuid())
                            .setSeasonNumber(episode_json.getInt("season_number"))
                            .setLength(episode_json.isNull("runtime") ? -1 : episode_json.getInt("runtime"))
                            .setUuid("E:" + episodeNumber);
            if (episode_json.has("still_path")) {
                String stillPath;
                if (!(stillPath = episode_json.getString("still_path")).equals("null"))
                    episode.setStillPath(stillPath);
            }
            if (episodeMap != null)
                episodeMap.put("E:" + episodeNumber, episode);
            return episode;
        } catch (JSONException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    // --------------- imdb

    private Runnable getImdbIdAndDetails(List<Show.Episode> episodeList, boolean showMessages, Runnable onComplete) {
        final Runnable[] destroyGetDetailsFromImdb = {null};
        final Runnable[] destroyRequestImdbId = {null};
        Show.Episode[] currentEpisode = {null};

        Runnable destroy = () -> {
            Utility.runRunnable(destroyGetDetailsFromImdb[0]);
            Utility.runRunnable(destroyRequestImdbId[0]);
        };

        episodeList.sort((e1, e2) -> {
            int compare;
            if ((compare = Integer.compare(e1.getSeasonNumber(), e2.getSeasonNumber())) != 0)
                return compare;
            if ((compare = Integer.compare(e1.getEpisodeNumber(), e2.getEpisodeNumber())) != 0)
                return compare;
            return 0;
        });
        Iterator<Show.Episode> iterator = episodeList.iterator();

        Runnable step = new Runnable() {
            @Override
            public void run() {

                Runnable next = () -> {
                    if (iterator.hasNext()) {
                        currentEpisode[0] = iterator.next();
                        this.run();
                    } else {
                        destroyGetDetailsFromImdb[0] = ShowActivity.this.getDetailsFromImdb(new com.finn.androidUtilities.CustomList<>(episodeList), showMessages, () -> {
                            Utility.runRunnable(onComplete);
                            Database.saveAll();
                        });
                    }
                };

                if (Utility.isImdbId(currentEpisode[0].getImdbId()))
                    next.run();
                else
                    destroyRequestImdbId[0] = currentEpisode[0].requestImdbId(ShowActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            if (Utility.isImdbId(currentEpisode[0].getImdbId())) {
                                next.run();
                            } else {
                                Runnable showOptionsDialog = () -> {
                                    CustomDialog.Builder(ShowActivity.this)
                                            .setTitle("Die IMDb-ID konnte nicht ermittelt werden")
                                            .setText(currentEpisode[0].getName())
                                            .addButton("TMDB", customDialog -> currentEpisode[0].requestImdbId(ShowActivity.this, this, Show.REQUEST_IMDB_ID_TYPE.TMDB))
                                            .addButton("TRAKT", customDialog -> currentEpisode[0].requestImdbId(ShowActivity.this, this, Show.REQUEST_IMDB_ID_TYPE.TRAKT))
                                            .addButton("TVDB", customDialog -> currentEpisode[0].requestImdbId(ShowActivity.this, this, Show.REQUEST_IMDB_ID_TYPE.TVDB))
                                            .addButton("Staffel", customDialog -> currentEpisode[0].requestImdbId(ShowActivity.this, this, Show.REQUEST_IMDB_ID_TYPE.SEASON))
                                            .addButton("Vorheriger", customDialog -> currentEpisode[0].requestImdbId(ShowActivity.this, this, Show.REQUEST_IMDB_ID_TYPE.PREVIOUS))
                                            .addOptionalModifications(customDialog -> {
                                                if (episodeList.size() > 1)
                                                    customDialog.addButton("Überspringen", customDialog1 -> next.run());
                                            })
                                            .addButtonDivider(5)
                                            .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                                            .enableStackButtons()
                                            .show();
                                };
                                if (showMessages)
                                    showOptionsDialog.run();
                                else
                                    Utility.showOnClickToast(ShowActivity.this, "Fehler beim Laden der IMDb-ID", v -> showOptionsDialog.run());
                            }
                        }
                    }, null);
            }
        };

        if (iterator.hasNext()) {
            currentEpisode[0] = iterator.next();
            step.run();
        }


        return destroy;
    }

    private Runnable getDetailsFromImdb(com.finn.androidUtilities.CustomList<Show.Episode> episodeList, boolean showDialog, Runnable onComplete) {
        episodeList.filter(episode -> Utility.stringExists(episode.getImdbId()), true);
        if (episodeList.isEmpty())
            return () -> {
            };
        episodeList.sort((e1, e2) -> {
            int compare;
            if ((compare = Integer.compare(e1.getSeasonNumber(), e2.getSeasonNumber())) != 0)
                return compare;
            if ((compare = Integer.compare(e1.getEpisodeNumber(), e2.getEpisodeNumber())) != 0)
                return compare;
            return 0;
        });

        ExternalCode.CodeEntry codeEntry = ExternalCode.getEntry(ExternalCode.ENTRY.GET_IMDB_EPISODE_DETAILS);
        String[] urls = episodeList.map(episode -> "https://www.imdb.com/title/" + episode.getImdbId()).toArray(new String[0]);
        final int[] counter = {0};
        List<String> resultList = new ArrayList<>();
        Helpers.WebViewHelper helper = new Helpers.WebViewHelper(this, urls)
                .setUserAgent(codeEntry.getString("userAgent"))
                .addRequest(codeEntry.getString("getTextApp"), s -> {
                    JSContext jsContext = new JSContext();
                    jsContext.property("text", s);
                    String found = null;

                    try {
                        JSValue result = jsContext.evaluateScript(Helpers.WebViewHelper.wrapScript(codeEntry.getString("parseText")));
                        if (result.isObject()) {
                            JSObject jsObject = result.toObject();
                            found = jsObject.toJSON();
                            if (jsObject.hasProperty("ageRating"))
                                episodeList.get(counter[0]).setAgeRating(jsObject.property("ageRating").toString());
                            if (jsObject.hasProperty("length"))
                                episodeList.get(counter[0]).setLength(jsObject.property("length").toNumber().intValue());
                        }
                    } catch (Exception e) {
                        CustomUtility.logD(null, "getDetailsFromImdb: <js err.> %s", e.getMessage());
                    }
                    if (found != null)
                        resultList.add((episodeList.size() > 1 ? (counter[0] + 1) + ": " : "") + found);
                    counter[0]++;
                })
                .setDebug(showDialog)
                .setOnAllComplete(() -> {
                    Database.saveAll();
                    if (showDialog)
                        CustomDialog.Builder(this)
                                .setTitle("Alle Ergebnisse")
                                .setText(String.join("\n", resultList))
                                .show();
                    Utility.runRunnable(onComplete);
                })
                .go();
        return helper::destroy;
    }
    //  <--------------- Api ---------------


    /** ------------------------- ChangeManagement -------------------------> */
    public static Database.ChangeHandler<Map, Show> getShowChangeHandler() {
        return (newShow, content) -> {
            Database database = Database.getInstance();
            Show oldShow = database.showMap.get(newShow.getUuid());

            com.finn.androidUtilities.Helpers.ApplyChangesHelper.Builder(oldShow, newShow)
                    .setAddApplyHandler((applyTo, where, newObject) -> {
                        if (newObject instanceof Show.Episode) {
                            Show.Episode episode = (Show.Episode) newObject;
                            Map<Integer, Map<String, Show.Episode>> seasonMap = database.tempShowSeasonEpisodeMap.get(episode.getShowId());
                            Map<String, Show.Episode> episodeMap;
                            if (seasonMap != null && (episodeMap = seasonMap.get(episode.getSeasonNumber())) != null) {
                                episodeMap.put("E:" + episode.getEpisodeNumber(), episode);
                            }
                        }
                        return false;
                    })
                    .apply();

            return true;
        };
    }

    // ---------------

    private CustomDialog.LifeCycleCallback getLifeCycleCallback(Utility.GenericReturnOnlyInterface<String> getUuid) {
        return getLifeCycleCallback((customDialog, change) -> {
            if (change instanceof Show && ((Show) change).getUuid().equals(getUuid.run()))
                customDialog.reloadView(false);
        }, null);
    }

    private static CustomDialog.LifeCycleCallback getLifeCycleCallback(Utility.DoubleGenericInterface<CustomDialog, Object> onChangeListener, @Nullable CustomDialog.DialogCallback dialogCallback) {
        return new CustomDialog.LifeCycleCallback() {
            Database.OnChangeListener<Object> listener;

            @Override
            public void onDialogShown(CustomDialog customDialog) {
                listener = Database.addOnChangeListener(Database.SHOW_MAP, change -> onChangeListener.run(customDialog, change));
            }

            @Override
            public void onDialogReloadBefore(CustomDialog customDialog) {
                if (dialogCallback != null)
                    dialogCallback.run(customDialog);
            }

            @Override
            public void onDialogReloadAfter(CustomDialog customDialog) {
            }

            @Override
            public void onDialogDismiss(CustomDialog customDialog) {
                Database.removeOnChangeListener(Database.SHOW_MAP, listener);
            }
        };
    }

    /** <------------------------- ChangeManagement ------------------------- */

    private void removeFocusFromSearch() {
        shows_search.clearFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_show, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_show_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_show_filterByGenre)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_show_add:
                showEditOrNewDialog(null);
                break;
            case R.id.taskBar_show_random:
//                showRandomDialog();
                break;
            case R.id.taskBar_show_scroll:
                if (item.isChecked()) {
                    item.setChecked(false);
                    scrolling = false;
                } else {
                    item.setChecked(true);
                    scrolling = true;
                }
                reLoadRecycler();
                break;

            case R.id.taskBar_show_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_show_sortByViews:
                sort_type = SORT_TYPE.VIEWS;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_show_sortByRating:
                sort_type = SORT_TYPE.RATING;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_show_sortByLatest:
                sort_type = SORT_TYPE.LATEST;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_show_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadRecycler();
                break;

            case R.id.taskBar_show_filterByName:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.NAME);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.NAME);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(shows_search.getQuery().toString());
                setSearchHint();
                break;
            case R.id.taskBar_show_filterByGenre:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.GENRE);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.GENRE);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(shows_search.getQuery().toString());
                setSearchHint();
                break;

            case android.R.id.home:
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;

        }
        return true;
    }

    private void setSearchHint() {
        String join = filterTypeSet.stream().filter(FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(FILTER_TYPE::getName).collect(Collectors.joining(", "));
        shows_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
        Utility.applyToAllViews(shows_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }

    @Override
    protected void onDestroy() {
        Database.saveAll();
        Database.removeOnChangeListener(Database.SHOW_MAP, onChangeListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        if (Utility.stringExists(shows_search.getQuery().toString()) && !Objects.equals(shows_search.getQuery().toString(), getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH))) {
//            shows_search.setQuery("", false);
//            return;
//        }

        if (advancedQueryHelper.handleBackPress(this))
            return;
        super.onBackPressed();
    }
}
