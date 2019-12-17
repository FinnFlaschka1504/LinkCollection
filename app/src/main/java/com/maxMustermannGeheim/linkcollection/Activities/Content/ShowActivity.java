package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.finn.androidUtilities.CustomRecycler.Expandable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomAdapter.ImageAdapterItem;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;
import com.mikhaellopez.lazydatepicker.LazyDatePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class ShowActivity extends AppCompatActivity {
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";
    public static final String EXTRA_EPISODE = "EXTRA_EPISODE";


    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST
    }

    public enum FILTER_TYPE {
        NAME, GENRE
    }

    private Database database;
    private SharedPreferences mySPR_daten;
    private Show randomShow;
    private boolean scrolling = true;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.GENRE));
    private SearchView.OnQueryTextListener textListener;
    private boolean reverse = false;
    private String singular;
    private String plural;
    private String searchQuery = "";

    CustomList<Show> allShowList = new CustomList<>();

//    private CustomDialog[] addOrEditDialog = new CustomDialog[]{null};
//    private CustomDialog detailDialog;

    private CustomRecycler customRecycler_ShowList;
    private SearchView shows_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
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
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

        CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
        if (extraSearchCategory != null) {
            filterTypeSet.clear();

            switch (extraSearchCategory) {
                case SHOW_GENRES:
                    filterTypeSet.add(FILTER_TYPE.GENRE);
                    break;
                case EPISODE:
                    String episode_string = getIntent().getStringExtra(EXTRA_EPISODE);
                    if (episode_string != null) {
                        Show.Episode episode = new Gson().fromJson(episode_string, Show.Episode.class);
                        findEpisode(episode, () -> {
                            Show.Episode oldEpisode = database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber())
                                    .getEpisodeMap().get(episode.getUuid());

                            if (oldEpisode != null)
                                showEpisodeDetailDialog(null, oldEpisode, true);
                            else
                                apiSeasonRequest(database.showMap.get(episode.getShowId()), episode.getSeasonNumber(), () ->
                                        showEpisodeDetailDialog(null, database.tempShowSeasonEpisodeMap.get(database.showMap.get(episode.getShowId())).get(episode.getSeasonNumber())
                                                .get(episode.getUuid()), true));
                        });
                    }
            }

            String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
            if (extraSearch != null) {
                shows_search.setQuery(extraSearch, true);
            }
        }

    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
//            for (Show show : database.showMap.values()) {
//                if (show.getDateList().isEmpty() && !show.isUpcoming() && !database.watchLaterList.contains(show.getUuid()))
//                    database.watchLaterList.add(show.getUuid());
//            }

            setContentView(R.layout.activity_show);
            allShowList = new CustomList<>(database.showMap.values());
            sortList(allShowList);

            loadRecycler();

            shows_search = findViewById(R.id.search);
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
//                                        .setText((unableToFindList.size() == 1 ? "Ein " + singular + " konnte" : unableToFindList.size() + " " + plural + " konnten") + " nicht gefunden werden")
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

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_ADD))
                showEditOrNewDialog(null);
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }

    private CustomList<Show> filterList(CustomList<Show> showList) {
        if (searchQuery.isEmpty())
            return showList;
        else
            return showList.filter(show -> Utility.containedInShow(searchQuery, show, filterTypeSet));
    }

    private CustomList<Show> sortList(CustomList<Show> showList) {
        Map<Show, List<Show.Episode>> showEpisodeMap = showList.stream().collect(Collectors.toMap(show -> show, this::getEpisodeList));

        new Helpers.SortHelper<>(showList)
                .setAllReversed(reverse)
                .addSorter(SORT_TYPE.NAME, (show1, show2) -> show1.getName().compareTo(show2.getName()) * (reverse ? -1 : 1))

                .addSorter(SORT_TYPE.VIEWS)
                .changeType(show -> getViews(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()
//                .addCondition((views1, views2) -> {
//
////                    List<Show.Episode> episodeList1 = showEpisodeMap.get(views1);
////                    List<Show.Episode> episodeList2 = showEpisodeMap.get(views2);
////                    int views1 = getViews(episodeList1);
////                    int views2 = getViews(episodeList2);
//
////                    if (views1 == views2)
////                        return views1.getName().compareTo(views2.getName());
////                    else
//                        return Integer.compare(views1, views2) * (reverse ? 1 : -1);
//                })

                .addSorter(SORT_TYPE.RATING/*, (show1, show2) -> {
                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);

                    double rating1 = getRating(episodeList1);
                    double rating2 = getRating(episodeList2);

                    if (rating1 == rating2)
                        return show1.getName().compareTo(show2.getName());
                    else
                        return Double.compare(rating1, rating2) * (reverse ? 1 : -1);
                }*/)
                .changeType(show -> getRating(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.LATEST/*, (show1, show2) -> {
                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);

                    Date latest1 = getLatest(episodeList1);
                    Date latest2 = getLatest(episodeList2);

                    if (latest1 == null && latest2 == null)
                        return show1.getName().compareTo(show2.getName());
                    else if (latest1 == null)
                        return reverse ? -1 : 1;
                    else if (latest2 == null)
                        return reverse ? 1 : -1;

                    if (latest1.equals(latest2))
                        return show1.getName().compareTo(show2.getName());
                    else
                        return latest1.compareTo(latest2) * (reverse ? 1 : -1);
                }*/)
                .changeType(show -> getLatest(showEpisodeMap.get(show)))
                .enableReverseDefaultComparable()

                .finish()
                .sort(() -> sort_type);
        // ToDo: ansichten reversed funktioniert nicht

//        switch (sort_type) {
//            case NAME:
//                showList.sort((show1, show2) -> show1.getName().compareTo(show2.getName()) * (reverse ? -1 : 1));
//                break;
//            case VIEWS:
//                showList.sort((show1, show2) -> {
//                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
//                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);
//                    int views1 = getViews(episodeList1);
//                    int views2 = getViews(episodeList2);
//
//                    if (views1 == views2)
//                        return show1.getName().compareTo(show2.getName());
//                    else
//                        return Integer.compare(views1, views2) * (reverse ? 1 : -1);
//                });
//                break;
//            case RATING:
//                showList.sort((show1, show2) -> {
//                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
//                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);
//
//                    double rating1 = getRating(episodeList1);
//                    double rating2 = getRating(episodeList2);
//
//                    if (rating1 == rating2)
//                        return show1.getName().compareTo(show2.getName());
//                    else
//                        return Double.compare(rating1, rating2) * (reverse ? 1 : -1);
//                });
//                break;
//            case LATEST:
//                showList.sort((show1, show2) -> {
//                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
//                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);
//
//                    Date latest1 = getLatest(episodeList1);
//                    Date latest2 = getLatest(episodeList2);
//
//                    if (latest1 == null && latest2 == null)
//                        return show1.getName().compareTo(show2.getName());
//                    else if (latest1 == null)
//                        return reverse ? -1 : 1;
//                    else if (latest2 == null)
//                        return reverse ? 1 : -1;
//
//                    if (latest1.equals(latest2))
//                        return show1.getName().compareTo(show2.getName());
//                    else
//                        return latest1.compareTo(latest2) * (reverse ? 1 : -1);
//                });
//                break;
//
//        }
        return showList;
    }

    private void loadRecycler() {
        customRecycler_ShowList = new CustomRecycler<Show>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_show)
                .setGetActiveObjectList(() -> {
                    CustomList<Show> filterdList = sortList(filterList(new CustomList<>(database.showMap.values())));
                    TextView noItem = findViewById(R.id.no_item);
                    noItem.setText(searchQuery.isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche");
                    noItem.setVisibility(filterdList.isEmpty() ? View.VISIBLE : View.GONE);
                    return filterdList;
                })
                .setSetItemContent((itemView, show) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_show_Titel)).setText(show.getName());
                    int views = getViews(getEpisodeList(show));
                    if (views > 0) {
                        itemView.findViewById(R.id.listItem_show_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_show_Views)).setText(String.valueOf(views));
                    } else
                        itemView.findViewById(R.id.listItem_show_Views_layout).setVisibility(View.GONE);

                    double rating = getRating(getEpisodeList(show));
                    if (rating > 0) {
                        itemView.findViewById(R.id.listItem_show_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_show_rating)).setText(new DecimalFormat("#.##").format(rating));
                    }
                    else
                        itemView.findViewById(R.id.listItem_show_rating_layout).setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.listItem_show_Genre)).setText(
                            show.getGenreIdList().stream().map(uuid -> database.showGenreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    itemView.findViewById(R.id.listItem_show_Genre).setSelected(scrolling);
                })
                .setOnClickListener((customRecycler, view, show, index) -> {
                    showDetailDialog(show);
                })
                .addSubOnClickListener(R.id.listItem_show_list, (customRecycler, view, show, index) -> showSeasonDialog(show))
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
                        CustomRecycler<Show.Season> seasonRecycler = showSeasonDialog(show);
                        int seasonNumber = episode[0].getSeasonNumber();
                        if (episode[0].equals(list.get(0).second)) {
                            apiSeasonRequest(show, seasonNumber, () -> {
                                showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo(episode[0]);
                            });
                        } else
                            showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode[0].getUuid()), "");
                    };

                    CustomDialog.Builder(this)
                            .setTitle("Gehe Zu")
                            .addButton("Ansichten-Historie", customDialog -> showViewHistoryDialog(list))
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
                            .show();
                })
                .setOnLongClickListener((customRecycler, view, show, index) -> {
                    showEditOrNewDialog(show);
                })
                .hideDivider()
                .generate();
    }

    private void showViewHistoryDialog(List<Pair<Date, Show.Episode>> list) {
        List<Expandable<Show.Episode>> expendables = new Expandable.ToGroupExpandableList<Show.Episode, Pair<Date, Show.Episode>, Date>()
                .setSort((o1, o2) -> ((Date) o1.getPayload()).compareTo(((Date) o2.getPayload())) * -1)
                .runToGroupExpandableList(list, dateEpisodePair -> Utility.removeTime(dateEpisodePair.first), (date, pairList) -> String.format(Locale.getDefault(), "%s (%d)", new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(date), pairList.size())
                        , item -> item.second);

        CustomDialog.Builder(this)
                .setTitle("Ansichten-Historie")
                .setView(new com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>>(this)
                        .setObjectList(expendables)
                        .setExpandableHelper(customRecycler1 ->
                                customRecycler1
                                        .new ExpandableHelper<Show.Episode>()
                                        .enableExpandByDefault()
                                        .customizeRecycler(subRecycler -> {
                                            subRecycler
                                                    .setSetItemContent((itemView, episode1) -> {
                                                        Utility.setMargins(itemView, 8, 5, 8, 5);
                                                        itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);

                                                        itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                                                        itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.GONE);
                                                        ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode1.getSeasonNumber()));

                                                        ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode1.getEpisodeNumber()));
                                                        ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode1.getName());
                                                        if (episode1.getAirDate() != null)
                                                            ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode1.getAirDate()));
                                                        ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode1.getRating() != -1 ? episode1.getRating() + " ☆" : "");

                                                    })
                                                    .setOnClickListener((customRecycler2, itemView, episode1, index1) -> {
                                                        Toast.makeText(this, episode1.getName(), Toast.LENGTH_SHORT).show();
                                                        findEpisode(episode1, () -> {});
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


    //  ------------------------- ShowInfo ------------------------->
    private List<Show.Episode> getEpisodeList(Show show) {
        return Utility.concatenateCollections(show.getSeasonList(), season -> season.getEpisodeMap().values());
    }

    private double getRating(List<Show.Episode> episodeList) {
        return episodeList.stream().filter(episode -> !(episode.getRating().equals(-1f) || episode.getRating().equals(0f))).mapToDouble(Show.Episode::getRating).average().orElse(-1);
    }

    private Date getLatest(List<Show.Episode> episodeList) {
        List<Date> dateList = Utility.concatenateCollections(episodeList, Show.Episode::getDateList);
        if (dateList.isEmpty())
            return null;
        return Collections.max(dateList);
    }

    private int getViews(List<Show.Episode> episodeList) {
        return episodeList.stream().map(episode -> episode.getDateList().size()).reduce(0, Integer::sum);
    }
    //  <------------------------- ShowInfo -------------------------

    //  --------------- Static from Main --------------->
    public static void showLaterMenu(AppCompatActivity activity, View view) {
        if (!Database.isReady())
            return;
        CustomMenu.Builder(activity, view.findViewById(R.id.main_shows_watchLater_label))
                .setMenus((customMenu, items) -> {
                    items.add(new CustomMenu.MenuItem("Später ansehen", new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, WATCH_LATER_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW), MainActivity.START_WATCH_LATER)));
                    items.add(new CustomMenu.MenuItem("Bevorstehende", new Pair<>(new Intent(activity, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, UPCOMING_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.SHOW), MainActivity.START_UPCOMING)));
                })
                .setOnClickListener((customRecycler, itemView, item, index) -> {
                    Pair<Intent, Integer> pair = (Pair<Intent, Integer>) item.getContent();
                    activity.startActivityForResult(pair.first, pair.second);
                })
                .dismissOnClick()
                .show();
    }

    public static void showNewEpisodesDialog(AppCompatActivity activity) {
        Database database = Database.getInstance();
        CustomDialog customDialog = CustomDialog.Builder(activity);
        com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>> expandableCustomRecycler = new com.finn.androidUtilities.CustomRecycler<Expandable<Show.Episode>>(activity)
                .setGetActiveObjectList(() -> {
                    List<Show.Episode> alreadyAiredList = Utility.concatenateCollections(database.showMap.values(), Show::getAlreadyAiredList);
                    return new Expandable.ToGroupExpandableList<Show.Episode, Show.Episode, String>()
                            .runToGroupExpandableList(alreadyAiredList, Show.Episode::getShowId, (s, m) -> String.format(Locale.getDefault(), "%s (%d)",
                                    database.showMap.get(s).getName(), m.size()), episode -> episode);
                })
                .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<Show.Episode>()
                        .setExpandMatching(expandable -> expandable.getList().stream().anyMatch(episode -> !episode.isWatched()))
                        .customizeRecycler(subRecycler -> {
                            subRecycler
                                    .setSetItemContent((itemView, episode) -> {
                                        Utility.setMargins(itemView, 8, 5, 8, 5);
                                        itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);


                                        itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                                        itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.GONE);
                                        ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));

                                        ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                                        ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());
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
                                    .enableSwiping((objectList, direction, episode) -> {
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
                .addButton("Alle Aktuallisieren", customDialog1 -> {

                    List<Show> showList = database.showMap.values().stream().filter(Show::isNotifyNew).collect(Collectors.toList());
                    final int[] pending = {showList.size()};
                    showList.forEach(show ->
                            ShowActivity.apiDetailRequest(activity, show.getTmdbId(), show, () -> {
                                if (pending[0] > 1) {
                                    pending[0]--;
                                    return;
                                }
                                Toast.makeText(activity, "Alle aktuallisiert", Toast.LENGTH_SHORT).show();
                                expandableCustomRecycler.reload();
                            }, true));
                }, false)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                .setOnDialogDismiss(customDialog1 -> Database.saveAll())
                .show();
    }
    //  <--------------- Static from Main ---------------

    //  --------------- NextEpisode --------------->
    private void getNextEpisode(Show.Episode previousEpisode, OnNextEpisode onNextEpisode) {
        Show show = database.showMap.get(previousEpisode.getShowId());
        Show.Season season = show.getSeasonList().get(previousEpisode.getSeasonNumber());

        if (season.getEpisodesCount() > previousEpisode.getEpisodeNumber()) {
            apiSeasonRequest(show, season.getSeasonNumber(), () -> {
                onNextEpisode.runOnNextEpisode(database.tempShowSeasonEpisodeMap.get(show).get(season.getSeasonNumber()).get("E:" + (previousEpisode.getEpisodeNumber() + 1)));
            });
        } else if (show.getSeasonsCount() > season.getSeasonNumber()) {
            apiSeasonRequest(show, season.getSeasonNumber() + 1, () -> {
                onNextEpisode.runOnNextEpisode(database.tempShowSeasonEpisodeMap.get(show).get(season.getSeasonNumber() + 1).get("E:1"));
            });
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
        return CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_show)
                .addButton("Historie", customDialog -> {
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
                .alignPreviousButtonsLeft()
                .addButton("Bearbeiten", customDialog -> {
                    CustomDialog customDialog_edit = showEditOrNewDialog(show);
                    if (customDialog_edit != null)
                        customDialog_edit.setObjectExtra(customDialog);
                }, false)
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailShow_title)).setText(show.getName());
                    ((TextView) view.findViewById(R.id.dialog_detailShow_genre)).setText(
                            show.getGenreIdList().stream().map(uuid -> database.showGenreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
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
                    view.findViewById(R.id.dialog_detailShow_list).setOnClickListener(view1 -> showSeasonDialog(show));

                    if (show.getImagePath() != null && !show.getImagePath().isEmpty()) {
                        ImageView dialog_video_poster = view.findViewById(R.id.dialog_detailShow_poster);
                        dialog_video_poster.setVisibility(View.VISIBLE);
                        Glide
                                .with(this)
                                .load("https://image.tmdb.org/t/p/w92/" + show.getImagePath())
                                .placeholder(R.drawable.ic_download)
                                .into(dialog_video_poster);
                        dialog_video_poster.setOnClickListener(v -> {
                            com.finn.androidUtilities.CustomDialog posterDialog = com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setView(R.layout.dialog_poster)
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        ImageView dialog_poster_poster = view1.findViewById(R.id.dialog_poster_poster);
                                        Glide
                                                .with(this)
                                                .load("https://image.tmdb.org/t/p/original/" + show.getImagePath())
                                                .placeholder(R.drawable.ic_download)
                                                .into(dialog_poster_poster);
                                        dialog_poster_poster.setOnContextClickListener(v1 -> {
                                            customDialog1.dismiss();
                                            return true;
                                        });
                                    });
                            posterDialog
                                    .removeBackground()
                                    .disableScroll()
                                    .show();
                        });
                    }


                    if (show.getLastUpdated() == null || System.currentTimeMillis() - show.getLastUpdated().getTime() > 86400000)
                        apiDetailRequest(this, show.getTmdbId(), show, customDialog::reloadView, true);
                })
                .show();
    }

    private CustomDialog showEditOrNewDialog(Show show) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);


        final Show editShow = show == null ? new Show("") : show.clone();

        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(show == null ? "Neu: " + singular : singular + " Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_show);

        if (show != null) {
            returnDialog.addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + show.getName() + "' löschen?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                            database.showMap.remove(show.getUuid());
                            reLoadRecycler();
                        })
                        .show();
            }, false)
                    .alignPreviousButtonsLeft();
        }
        return returnDialog
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    boolean checked = ((CheckBox) customDialog.findViewById(R.id.dialog_editOrAdd_show_watchLater)).isChecked();
                    saveShow(customDialog, checked, show, editShow);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout dialog_editOrAdd_show_Title_layout = view.findViewById(R.id.dialog_editOrAdd_show_Title_layout);
                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(dialog_editOrAdd_show_Title_layout)
                            .addActionListener(dialog_editOrAdd_show_Title_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                apiSearchRequest(text, customDialog, editShow);
                            }, Helpers.TextInputHelper.IME_ACTION.SEARCH)
                            .setValidation(dialog_editOrAdd_show_Title_layout, (validator, text) -> {
                                if (show == null && database.showMap.values().stream().anyMatch(show1 -> show1.getName().toLowerCase().equals(text.toLowerCase())))
                                    validator.setInvalid("Gleiche Serie schon vorhanden!");
                            });
                    AutoCompleteTextView dialog_editOrAdd_show_title = view.findViewById(R.id.dialog_editOrAdd_show_title);
                    if (!editShow.getName().isEmpty()) {
                        dialog_editOrAdd_show_title.setText(editShow.getName());

                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_show_Genre)).setText(
                                editShow.getGenreIdList().stream().map(uuid -> database.showGenreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAdd_show_Genre).setSelected(true);

                        if (editShow.getFirstAirDate() != null)
                            ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAdd_show_datePicker)).setDate(editShow.getFirstAirDate());

                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_show_details)).setText(getDetailText(editShow));

                        if (!reload)
                            apiDetailRequest(this, show.getTmdbId(), editShow, customDialog::reloadView, true);
                    } else {
                        view.findViewById(R.id.dialog_editOrAdd_show_watchLater).setVisibility(View.VISIBLE);
                        dialog_editOrAdd_show_title.requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
//                        editShow = new Show();
                    }


                    view.findViewById(R.id.dialog_editOrAdd_show_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, customDialog, editShow.getGenreIdList(), editShow, CategoriesActivity.CATEGORIES.SHOW_GENRES));
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
        // ToDo: releaseDate etc.
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

        if (dialog.getObjectExtra() != null)
            ((CustomDialog) dialog.getObjectExtra()).reloadView();

    }

    private static boolean alreadySeen(Show show, Show.Episode episode) {
        final boolean[] result = {false};
        show.getSeasonList().stream().filter(season -> episode.getSeasonNumber() == season.getSeasonNumber()).findFirst().ifPresent(season -> {
            result[0] = season.getEpisodeMap().containsKey("E:" + episode.getEpisodeNumber());
        });
        return result[0];
    }

    //  --------------- Season & Episode --------------->
    private CustomRecycler<Show.Season> showSeasonDialog(Show show) {
        CustomDialog customDialog = CustomDialog.Builder(this);

        CustomRecycler<Show.Season> seasonRecycler = new CustomRecycler<Show.Season>(this)
                .setGetActiveObjectList(show::getSeasonList)
                .setItemLayout(R.layout.list_item_season)
                .setSetItemContent((itemView, season) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_season_number)).setText(String.valueOf(season.getSeasonNumber()));
                    ((TextView) itemView.findViewById(R.id.listItem_season_name)).setText(season.getName());
                    if (season.getAirDate() != null)
                        ((TextView) itemView.findViewById(R.id.listItem_season_release)).setText(new SimpleDateFormat("(yyyy)", Locale.getDefault()).format(season.getAirDate()));
                    Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                    int size = (int) season.getEpisodeMap().values().stream().filter(Show.Episode::isWatched).count();
                    if (size == 0) {
                        helper.append("Folgen:  ", Helpers.SpannableStringHelper.SPAN_TYPE.BOLD_ITALIC).append(String.valueOf(season.getEpisodesCount()));
                    } else {
                        helper.append("Gesehen:  ", Helpers.SpannableStringHelper.SPAN_TYPE.BOLD_ITALIC);
                        if (size < season.getEpisodesCount())
                            helper.append(size + " / " + season.getEpisodesCount());
                        else
                            helper.appendColor("Alle " + season.getEpisodesCount(), getColor(R.color.colorGreen));
                    }
                    ((TextView) itemView.findViewById(R.id.listItem_season_episodes)).setText(helper.get());
                })
                .setOnClickListener((customRecycler, itemView, season, index) -> {
                    Map<String, Show.Episode> episodeMap;
                    Map<Integer, Map<String, Show.Episode>> seasonEpisodeMap;

                    if ((seasonEpisodeMap = database.tempShowSeasonEpisodeMap.get(show)) != null && (episodeMap = seasonEpisodeMap.get(season.getSeasonNumber())) != null)
                        showEpisodeDialog(season, episodeMap, customRecycler);
                    else
                        apiSeasonRequest(show, season.getSeasonNumber(), () -> {
                            showEpisodeDialog(season, database.tempShowSeasonEpisodeMap.get(show).get(season.getSeasonNumber()), customRecycler);
                        });
                })
                .setOnLongClickListener((CustomRecycler.OnLongClickListener<Show.Season>) (customRecycler, view, season, index) -> {
                    String BREAKPOINT = null;
                    showResetDialog(new CustomList<>(((Show.Season) season).getEpisodeMap().values()), season, null, customRecycler).setOnDialogDismiss(customDialog1 -> {
                        Database.saveAll();
                        reLoadRecycler();
                    });
                })
                .hideDivider()
                .generate();

        customDialog
                .setTitle("Staffeln")
                .addButton("Historie", customDialog1 -> {
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
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK)
                .setObjectExtra(seasonRecycler)
                .setView(seasonRecycler.getRecycler())
                .disableScroll()
                .show();

        if (show.getLastUpdated() == null || System.currentTimeMillis() - show.getLastUpdated().getTime() > 86400000)
            apiDetailRequest(this, show.getTmdbId(), show, customDialog::reloadView, true);

        return seasonRecycler;
    }

    private CustomRecycler<Show.Episode> showEpisodeDialog(Show.Season season, Map<String, Show.Episode> episodeMap, CustomRecycler<Show.Season> seasonCustomRecycler) {
        final Show.Episode[] selectedEpisode = {null};
        Runnable addView = () -> {
            Show.Episode episode = selectedEpisode[0];
            episode.setWatched(true);
            season.getEpisodeMap().put("E:" + episode.getEpisodeNumber(), episode);
            boolean before = episode.addDate(new Date(), true);
            Utility.showCenteredToast(this, "Ansicht hinzugefügt" + (before ? "\nAutomatisch für gestern hinzugefügt" : ""));
            Show show = database.showMap.get(episode.getShowId());
            show.setAlreadyAiredList(show.getAlreadyAiredList().stream().filter(episode1 -> episode.getTmdbId() != episode1.getTmdbId()).collect(Collectors.toList()));
        };
        CustomRecycler<Show.Episode> episodeRecycler = new CustomRecycler<Show.Episode>(this)
                .setGetActiveObjectList(() -> {
                    episodeMap.putAll(season.getEpisodeMap());
                    ArrayList<Show.Episode> episodeList = new ArrayList<>(episodeMap.values());
                    episodeList.sort((o1, o2) -> Integer.compare(o1.getEpisodeNumber(), o2.getEpisodeNumber()));
                    return episodeList;
                })
                .setItemLayout(R.layout.list_item_episode)
                .setSetItemContent((itemView, episode) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                    ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());
                    if (episode.getAirDate() != null)
                        ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));
                    ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode.getRating() != -1 ? episode.getRating() + " ☆" : "");
                    ((TextView) itemView.findViewById(R.id.listItem_episode_viewCount)).setText(
                            episode.getDateList().size() >= 2 || (!episode.getDateList().isEmpty() && !episode.isWatched()) ? "| " + episode.getDateList().size() : "");

                    ImageView listItem_episode_seen = itemView.findViewById(R.id.listItem_episode_seen);
                    if (episode.isWatched()) {
                        listItem_episode_seen.setColorFilter(getColor(R.color.colorGreen), PorterDuff.Mode.SRC_IN);
                        listItem_episode_seen.setAlpha(1f);
                        listItem_episode_seen.setClickable(false);
                        listItem_episode_seen.setForeground(null);
                    } else {
                        listItem_episode_seen.setColorFilter(getColor(R.color.colorDrawable), PorterDuff.Mode.SRC_IN);
                        listItem_episode_seen.setAlpha(0.2f);
                        listItem_episode_seen.setClickable(true);
                        listItem_episode_seen.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.ripple, null));
                    }
                })
                .addSubOnClickListener(R.id.listItem_episode_seen, (customRecycler, itemView, episode, index) -> {
                    if (episode.isWatched())
                        return;
                    selectedEpisode[0] = episode;
                    if (episode.getRating() == -1 || episode.getRating() == 0) {
                        RatingBar ratingBar = new RatingBar(this);
                        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        ratingBar.setRating(-1);
                        ratingBar.setMax(5);
                        ratingBar.setStepSize(0.5f);
                        ratingBar.setNumStars(5);
                        ratingBar.setBackground(getDrawable(R.drawable.tile_background));
                        CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(itemView, ratingBar).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.TOP).show();
                        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
                            selectedEpisode[0].setRating(rating);
                            addView.run();
                            customRecycler.reload();
                            customPopupWindow.dismiss();
                        });
                    } else {
                        addView.run();
                        customRecycler.reload();
                    }
                })
                .addSubOnLongClickListener(R.id.listItem_episode_seen, (customRecycler, itemView, episode, index) -> {
                    if (episode.isWatched())
                        return;
                    selectedEpisode[0] = episode;
                    addView.run();
                    customRecycler.reload();
                })
                .setOnClickListener((customRecycler, itemView, episode, index) -> {
                    showEpisodeDetailDialog(customRecycler, episode, false);
                })
                .setOnLongClickListener((customRecycler, view, episode, index) -> showResetDialog(Arrays.asList(episode), null, null, customRecycler))
                .hideDivider();
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
                .setView(episodeRecycler.generateRecyclerView())
                .disableScroll()
                .setOnDialogDismiss(customDialog -> {
                    Database.saveAll();
                    reLoadRecycler();
                    if (seasonCustomRecycler != null)
                        seasonCustomRecycler.reload();
                })
                .show();
        return episodeRecycler;
    }

    private void showEpisodeDetailDialog(CustomRecycler customRecycler, Show.Episode episode, boolean startedDirectly) {
        setResult(RESULT_OK);
        CustomDialog.Builder(this)
                .setTitle(episode.getName())
                .setView(R.layout.dialog_detail_episode)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    episode.setRating(((RatingBar) customDialog.findViewById(R.id.dialog_detailEpisode_rating)).getRating());

                    if (!episode.isWatched() && episode.getDateList().isEmpty()) {
                        episode.setWatched(true);
                        episode.getDateList().add(new Date());
                    }
                })
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_title)).setText(episode.getName());
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                    Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                    helper.append(String.valueOf(episode.getDateList().size()));
                    if (episode.isWatched())
                        helper.appendColor("  ✓", getColor(R.color.colorGreen));
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_views)).setText(helper.get());
                    ((TextView) view.findViewById(R.id.dialog_detailEpisode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));
                    RatingBar dialog_detailEpisode_rating = view.findViewById(R.id.dialog_detailEpisode_rating);
                    dialog_detailEpisode_rating.setRating(episode.getRating());
                    CustomDialog.ButtonHelper actionButton = customDialog.getActionButton();
                    dialog_detailEpisode_rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        actionButton.setEnabled(rating != episode.getRating());
                    });
                    view.findViewById(R.id.dialog_detailEpisode_editViews).setOnClickListener(v -> showCalenderDialog(episode, customDialog));
                    view.findViewById(R.id.dialog_detailEpisode_editViews).setOnLongClickListener(v -> {
                        showResetDialog(Arrays.asList(episode), null, customDialog, null);
                        return true;
                    });
                })
                .setOnDialogDismiss(customDialog -> {
                    if (customRecycler != null)
                        customRecycler.reload();
                    if (startedDirectly)
                        Database.saveAll();
                })
                .show();
    }

    private CustomDialog showResetDialog(List<Show.Episode> episodeList_all, Show.Season season, CustomDialog customDialog, CustomRecycler customRecycler) {
        List<Show.Episode> episodeList = episodeList_all.stream().filter(episode -> !episode.getDateList().isEmpty()).collect(Collectors.toList());
        CustomDialog returnDialog = CustomDialog.Builder(this);
        if (episodeList.isEmpty())
            return returnDialog;

        return returnDialog
                .setTitle("Zurücksetzen")
                .setText("Willst du nur " + (season != null ? "die" : "den") + " Status, oder die Komplette " + (season != null ? "Staffel" : "Folge") + " zurücksetzen")
                .addButton("Nur Status", customDialog1 -> {
                    CustomDialog.Builder(this)
                            .setTitle("Status Zurücksetzen")
                            .setText((season != null ? "Die Status der kompletten Staffel '" + season.getName() + "' werden"
                                    : "Der Status der Episode '" + episodeList.get(0).getName() + "' wird") + " auf 'ungesehen' gesetzt")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog2 -> {
                                for (Show.Episode episode : episodeList) {
                                    episode.setWatched(false);
                                }
                                if (customDialog != null)
                                    customDialog.reloadView();
                                else
                                    customRecycler.reload();
                                customDialog1.dismiss();
                            })
                            .show();
                }, false)
                .addButton("Alles", customDialog1 -> {
                    CustomDialog.Builder(this)
                            .setTitle("Komplett Zurücksetzen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                            .setText("Bist du sicher, dass du die " + (season != null ? "Staffel '" + season.getName() : "Episode '" + episodeList.get(0).getName()) + "' komplett zurücksetzen willst?")
                            .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog2 -> {
                                for (Show.Episode episode : episodeList) {
                                    episode.getDateList().clear();
                                    episode.setWatched(false);
                                    episode.setRating(-1f);
                                    database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber()).getEpisodeMap().remove(episode.getUuid());
                                }
                                if (customDialog != null)
                                    customDialog.reloadView();
                                else
                                    customRecycler.reload();
                                customDialog1.dismiss();
                            })
                            .show();

                }, false)
                .show();
    }

    public void showCalenderDialog(Show.Episode episode, CustomDialog detailDialog) {
        int viewCount = episode.getDateList().size();
        com.finn.androidUtilities.CustomDialog.Builder(this)
                .setTitle("Ansichten Bearbeiten")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupEpisodeCalender(this, calendarView, ((FrameLayout) view), Arrays.asList(episode), false);
                })
                .disableScroll()
                .setDimensions(true, true)
                .setOnDialogDismiss(customDialog -> {
                    Map<String, Show.Episode> episodeMap = database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber()).getEpisodeMap();
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
        CustomRecycler<Show.Season> seasonRecycler = showSeasonDialog(show);
        int seasonNumber = episode.getSeasonNumber();
        if (database.tempShowSeasonEpisodeMap.get(show) == null) {
            apiSeasonRequest(show, seasonNumber, () -> {
                showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");
                onFinished.run();
            });
        } else {
            showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");
            onFinished.run();
        }

//        if (episode.equals(list.get(0).second)) {
//            apiSeasonRequest(show, seasonNumber, () -> {
//                showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo(episode);
//            });
//        } else
//            showEpisodeDialog(show.getSeasonList().get(seasonNumber), database.tempShowSeasonEpisodeMap.get(show).get(seasonNumber), seasonRecycler).goTo((search, episode1) -> episode1.getUuid().equals(episode.getUuid()), "");

    }
    //  <--------------- Season & Episode ---------------

    //  --------------- TMDb Api --------------->
    private void apiSearchRequest(String queue, CustomDialog customDialog, Show show) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/search/tv?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&query=" +
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
                Map<Integer, String> idUuidMap = database.showGenreMap.values().stream().collect(Collectors.toMap(ShowGenre::getTmdbGenreId, ParentClass::getUuid));

                CustomList uuidList = integerList.map((Function<Integer, Object>) idUuidMap::get).filter(Objects::nonNull);
                show.setGenreIdList(uuidList);
                apiDetailRequest(this, show.getTmdbId(), show, customDialog::reloadView, false);
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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jsonObjectList
                        .map(stringJSONObjectPair -> stringJSONObjectPair.first));

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

    private static void apiDetailRequest(AppCompatActivity activity, int id, Show show, Runnable onFinish, boolean update) {
        if (!Utility.isOnline(activity))
            return;

        String requestUrl = "https://api.themoviedb.org/3/tv/" +
                id +
                "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        Toast toast = Toast.makeText(activity, "Deteils werden geladen..", Toast.LENGTH_LONG);
        if (!update)
            toast.show();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            toast.cancel();
            try {
                if (response.has("number_of_seasons"))
                    show.setSeasonsCount(response.getInt("number_of_seasons"));
                if (response.has("number_of_episodes"))
                    show.setAllEpisodesCount(response.getInt("number_of_episodes"));
                if (response.has("in_production"))
                    show.setInProduction(response.getBoolean("in_production"));
                if (response.has("next_episode_to_air")) // ToDo: fixen
                    show.setNextEpisodeAir(Utility.getDateFromJsonString("next_episode_to_air", response));
                if (response.has("status"))
                    show.setStatus(response.getString("status"));
                if (response.has("poster_path"))
                    show.setImagePath(response.getString("poster_path"));

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
                    seasonList.add(season);
                }

                show.getSeasonList().forEach(season -> seasonList.stream().filter(season1 -> season.getTmdbId() == season1.getTmdbId())
                        .findFirst().ifPresent(newSeason -> {
                            newSeason.setEpisodeMap(season.getEpisodeMap());
                            newSeason.setUuid(season.getUuid());
                        }));


                show.setSeasonList(seasonList);

                if (show.isNotifyNew()) {
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

        String requestUrl = "https://api.themoviedb.org/3/tv/" + show.getTmdbId() + "/season/" + seasonNumber + "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

//        PopupWindow loadingWindow = Utility.showLoadingWindow(this, view);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
//            loadingWindow.dismiss();
            try {
                JSONArray episodes_json = response.getJSONArray("episodes");
                Map<String, Show.Episode> episodeMap = new HashMap<>();
                for (int i = 0; i < episodes_json.length(); i++) {
                    JSONObject episode_json = episodes_json.getJSONObject(i);
                    jsonToEpisode(show, episodeMap, episode_json);
                }
                Map<Integer, Map<String, Show.Episode>> map = new HashMap<>();
                map.put(seasonNumber, episodeMap);
                database.tempShowSeasonEpisodeMap.put(show, map);
                onLoaded.run();
            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            String BREAKPOINT = null;

        }, error -> {
//            loadingWindow.dismiss();
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);

    }

    private static Show.Episode jsonToEpisode(Show show, @Nullable Map<String, Show.Episode> episodeMap, JSONObject episode_json) {
        try {
            int episodeNumber = episode_json.getInt("episode_number");
            Show.Episode episode = (Show.Episode) new Show.Episode(episode_json.getString("name")).setAirDate(Utility.getDateFromJsonString("air_date", episode_json))
                    .setEpisodeNumber(episodeNumber).setTmdbId(episode_json.getInt("id")).setShowId(show.getUuid()).setSeasonNumber(episode_json.getInt("season_number")).setUuid("E:" + episodeNumber);
            if (episodeMap != null)
                episodeMap.put("E:" + episodeNumber, episode);
            return episode;
        } catch (JSONException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    //  <--------------- TMDb Api ---------------


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_show, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_show_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_show_filterByGenre)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));
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
                customRecycler_ShowList.reload();
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
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

//    private void showRandomDialog() {
//        if (filterdShowList.isEmpty()) {
//            Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        randomShow = filterdShowList.get((int) (Math.random() * filterdShowList.size()));
//        List<String> darstellerNames = new ArrayList<>();
//        randomShow.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
//        List<String> studioNames = new ArrayList<>();
//        randomShow.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
//        List<String> genreNames = new ArrayList<>();
//        randomShow.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
//
//        CustomDialog.Builder(this)
//                .setTitle("Zufällig")
//                .setView(R.layout.dialog_detail_show)
//                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
//                .addButton("Nochmal", customDialog -> {
//                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
//                    randomShow = filterdShowList.get((int) (Math.random() * filterdShowList.size()));
//                    ((TextView) customDialog.findViewById(R.id.dialog_show_Titel)).setText(randomShow.getName());
//
//                    List<String> darstellerNames_neu = new ArrayList<>();
//                    randomShow.getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
//                    ((TextView) customDialog.findViewById(R.id.dialog_show_Darsteller)).setText(String.join(", ", darstellerNames_neu));
//
//                    List<String> studioNames_neu = new ArrayList<>();
//                    randomShow.getStudioList().forEach(uuid -> studioNames_neu.add(database.studioMap.get(uuid).getName()));
//                    ((TextView) customDialog.findViewById(R.id.dialog_show_Studio)).setText(String.join(", ", studioNames_neu));
//
//                    List<String> genreNames_neu = new ArrayList<>();
//                    randomShow.getGenreList().forEach(uuid -> genreNames_neu.add(database.genreMap.get(uuid).getName()));
//                    ((TextView) customDialog.findViewById(R.id.dialog_show_Genre)).setText(String.join(", ", genreNames_neu));
//
//                }, false)
//                .addButton("Öffnen", customDialog -> openUrl(randomShow.getUrl(), false), false)
//                .setSetViewContent((customDialog, view) -> {
//                    ((TextView) view.findViewById(R.id.dialog_show_Titel)).setText(randomShow.getName());
//                    ((TextView) view.findViewById(R.id.dialog_show_Darsteller)).setText(String.join(", ", darstellerNames));
//                    ((TextView) view.findViewById(R.id.dialog_show_Studio)).setText(String.join(", ", studioNames));
//                    ((TextView) view.findViewById(R.id.dialog_show_Genre)).setText(String.join(", ", genreNames));
//                    view.findViewById(R.id.dialog_show_Darsteller).setSelected(true);
//
//                })
//                .show();
//
//    }

    private void openUrl(String url, boolean select) {
        if (url == null || url.equals("")) {
            Toast.makeText(this, "Keine URL hinterlegt", Toast.LENGTH_SHORT).show();
            return;
        }
        Utility.openUrl(this, url, select);
    }

    @Override
    protected void onDestroy() {
        Database.saveAll();
        super.onDestroy();
    }

}
