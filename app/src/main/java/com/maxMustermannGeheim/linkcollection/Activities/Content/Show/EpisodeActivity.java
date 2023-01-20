package com.maxMustermannGeheim.linkcollection.Activities.Content.Show;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.WatchListActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.ExternalCode;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import org.intellij.lang.annotations.Language;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EpisodeActivity extends AppCompatActivity {

    private static final String TAG = "AppCompatActivity";

    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST, LENGTH, EPISODE_NUMBER
    }

    public static final String ADVANCED_SEARCH_CRITERIA_SHOW = "s";
    @Language("RegExp")
    public static final String ADVANCED_SEARCH_CRITERIA_SHOW_REGEX_SEASON_AND_EPISODE = "(?:(?:\\d+(?:\\[(?:(?:\\d+|-\\d+|\\d+-|\\d+-\\d+)(?:,(?=[\\d-])|(?![\\d-])))+\\])?|E?(-\\d+|\\d+-|\\d+-\\d+)|E\\d+)(?:,(?=[^)])|(?![^)])))+";
    @Language("RegExp")
    public static final String ADVANCED_SEARCH_CRITERIA_SHOW_REGEX = "((?:((?:(?<=\\\\)\\||(?<=\\\\)\\(|[^|(\\]\\n])+?)(?:\\((" + ADVANCED_SEARCH_CRITERIA_SHOW_REGEX_SEASON_AND_EPISODE + ")\\))?)(?:\\|(?=[^|\\n])|(?![^|\\n\\]])))+";
    public static final String EXTRA_SEARCH_SEASONS_AND_EPISODES = "EXTRA_SEARCH_SEASONS_AND_EPISODES";

    private Database database;
    private SharedPreferences mySPR_data;
    private String singular;
    private String plural;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private Runnable setToolbarTitle;
    private String searchQuery = "";
    private SearchView.OnQueryTextListener textListener;
    private Helpers.AdvancedQueryHelper<Show.Episode> advancedQueryHelper;
    private CustomRecycler<Show.Episode> recycler;
    private final Map<String, Integer> showRuntimeMap = new HashMap<>();
    private boolean reverse = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");


    private TextView elementCount;
    private SearchView searchView;

    /** <------------------------- Start ------------------------- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        Settings.startSettings_ifNeeded(this);
        ExternalCode.initialize_ifNecessary(this);
        singular = CategoriesActivity.CATEGORIES.EPISODE.getSingular();
        plural = CategoriesActivity.CATEGORIES.EPISODE.getPlural();

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_episode);
        mySPR_data = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {

            setContentView(R.layout.activity_episode);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            searchView = findViewById(R.id.search);
//            media_search.setQuery(new com.finn.androidUtilities.Helpers.SpannableStringHelper().appendColor("Test", Color.RED).appendBold(" Dick").appendItalic(" Schräg").get(), false);

            advancedQueryHelper = getEpisodeAdvancedQueryHelper();

            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    removeFocusFromSearch();
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s.trim();
                    reloadRecycler();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(textListener);


            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearchCategory == CategoriesActivity.CATEGORIES.SHOW && extraSearch != null) {
                    extraSearch = extraSearch.replaceAll("\\|", "\\\\|").replaceAll("\\(", "\\\\(");

                    String seasonsAndEpisodes = getIntent().getStringExtra(EXTRA_SEARCH_SEASONS_AND_EPISODES);
                    if (CustomUtility.stringExists(seasonsAndEpisodes))
                        extraSearch += "(" + seasonsAndEpisodes + ")";

                    extraSearch = String.format(Locale.getDefault(), "{[%s:%s]} ", ADVANCED_SEARCH_CRITERIA_SHOW, extraSearch);

                    searchView.setQuery(extraSearch, true);
                } else if (extraSearch != null) {
                    if (!advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch))
                        searchView.setQuery(extraSearch, true);
                }
            }
            setSearchHint();
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_data, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }
    /**  ------------------------- Start ------------------------->  */


    /** ------------------------- Recycler -------------------------> */
    private CustomList<Show.Episode> filterList(CustomList<Show.Episode> episodeList) {
        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(episodeList);
        }
        return episodeList;
    }

    private CustomList<Show.Episode> sortList(CustomList<Show.Episode> episodeList) {

        episodeList.sort((episode1, episode2) -> {
            int result = 0;
            if ((result = getLatestViewTime(episode1).compareTo(getLatestViewTime(episode2))) != 0)
                return result * -1;
//            if ((result = episode1.getLastModified().compareTo(episode2.getLastModified())) != 0)
//                return result * -1;

            return episode1.compareByName(episode2);
        });
        new Helpers.SortHelper<>(episodeList)
                .setAllReversed(reverse)
                .addSorter(SORT_TYPE.NAME, ParentClass::compareByName)

                .addSorter(SORT_TYPE.VIEWS)
                .changeType(episode -> episode.getDateList().size())
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.RATING)
                .changeType(episode1 -> episode1._getRatingWithTendency().orElse(0f))
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.LATEST)
                .changeType(this::getLatestViewTime)
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.LENGTH)
                .changeType(this::getEpisodeRuntime)
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.EPISODE_NUMBER)
                .changeType(episode -> episode.getAbsoluteEpisodeNumber(database.showMap.get(episode.getShowId())))

                .finish()
                .sort(() -> sort_type);

        return episodeList;
    }

    private void loadRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recycler);

        boolean showShowName = true;

        recycler = new CustomRecycler<Show.Episode>(this, recyclerView)
                .setItemLayout(R.layout.list_item_episode)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<Show.Episode> filteredList = filterList(new CustomList<>(database.showMap.values().stream().flatMap(
                            show -> show.getSeasonList().stream().flatMap(
                                    season -> season.getEpisodeMap().values().stream())).collect(Collectors.toList())));

                    sortList(filteredList);

                    TextView noItem = findViewById(R.id.no_item);
                    String text = searchView.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = (size > 1 ? size + " Episoden" : (size == 1 ? "Eine" : "Keine") + " Episode");
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                    final long[] watchedMinutes = {0};
                    final double[] ratingSum = {0};
                    final double[] ratingCount = {0};
                    final int[] viewsSum = {0};
                    filteredList.forEach(episode -> {
                        int views = episode.getDateList().size();
                        viewsSum[0] += views;
                        watchedMinutes[0] += (long) getEpisodeRuntime(episode) * views;
                        Optional<Float> rating = episode._getRatingWithTendency();
                        if (rating.isPresent()) {
                            ratingSum[0] += rating.get();
                            ratingCount[0]++;
                        }
                    });

                    String timeString = Utility.formatDuration(Duration.ofMinutes(watchedMinutes[0]), null);
                    if (Utility.stringExists(timeString))
                        builder.append(timeString).append("\n");
                    if (viewsSum[0] > 0)
                        builder.append(viewsSum[0] + " Episoden " + (viewsSum[0] > 1 ? "Ansichten" : "Ansicht"));

                    if (ratingCount[0] > 0)
                        builder.append("  |  ").append(String.format(Locale.getDefault(), "Ø %.2f ☆", ratingSum[0] / ratingCount[0]).replaceAll("([.,]?0*)(?= ☆)", ""));

                    elementCount.setText(builder);

                    return filteredList;
                })
                .setSetItemContent((customRecycler, itemView, episode, index) -> {
                    itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);

                    itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                    if (showShowName) {
                        Show show = database.showMap.get(episode.getShowId());
                        itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.VISIBLE);
                        TextView showNameTextView = itemView.findViewById(R.id.listItem_episode_showName);
                        showNameTextView.setText(show.getName());
                        showNameTextView.setSingleLine();
                    } else
                        itemView.findViewById(R.id.listItem_episode_showName_layout).setVisibility(View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));

                    ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));

                    ImageView listItem_episode_image = itemView.findViewById(R.id.listItem_episode_image);
                    int showPreviewSetting = Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_SHOW_EPISODE_PREVIEW));
                    if (Utility.stringExists(episode.getStillPath()) && (showPreviewSetting == 0 || showPreviewSetting == 1 && episode.isWatched())) {
                        listItem_episode_image.setVisibility(View.VISIBLE);
                        Utility.loadUrlIntoImageView(this, listItem_episode_image, Utility.getTmdbImagePath_ifNecessary(episode.getStillPath(), false), Utility.getTmdbImagePath_ifNecessary(episode.getStillPath(), true), null, () -> Utility.roundImageView(listItem_episode_image, 2), this::removeFocusFromSearch);
                    } else
                        listItem_episode_image.setVisibility(View.GONE);


                    TextView episodeNameTextView = itemView.findViewById(R.id.listItem_episode_name);
                    episodeNameTextView.setText(episode.getName());
                    episodeNameTextView.setSingleLine();
                    episodeNameTextView.setEllipsize(TextUtils.TruncateAt.END);
                    if (episode.getAirDate() != null)
                        ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));
                    ParentClass_Ratable.applyRatingTendencyIndicator(itemView.findViewById(R.id.listItem_episode_ratingTendency), episode, episode.isWatched(), false);
                    ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode.getRating() != -1 ? episode.getRating() + " ☆" : "");

                    ((TextView) itemView.findViewById(R.id.listItem_episode_viewCount)).setText(
                            episode.getDateList().size() >= 2 || (!episode.getDateList().isEmpty() && !episode.isWatched()) ? "| " + episode.getDateList().size() : "");
                })
                .setOnClickListener((customRecycler, itemView, episode, index) -> {
                    removeFocusFromSearch();
                    ShowActivity.showEpisodeDetailDialog(this, null, episode);
                })
                .setOnLongClickListener((customRecycler, view, episode, index) -> {
                    removeFocusFromSearch();
                    startActivityForResult(new Intent(this, ShowActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, episode.getShowId())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.EPISODE)
                            .putExtra(ShowActivity.EXTRA_EPISODE, new Gson().toJson(episode)), MainActivity.START_SHOW_FROM_CALENDER);
                })
                .enableFastScroll((episodeCustomRecycler, episode, integer) -> {
                    switch (sort_type) {
                        case NAME:
                            return episode.getName().substring(0, 1);
                        case RATING:
                            return episode._getRatingWithTendency().map(rating -> decimalFormat.format(rating) + " ☆").orElse("Keine Bewertung");
                        case LATEST:
                            return getLatestView(episode).map(dateFormat::format).orElse("Keine Ansichten");
                        case VIEWS:
                            return !episode.getDateList().isEmpty() ? String.valueOf(episode.getDateList().size()) : "Keine Ansichten";
                        case LENGTH:
                            int runtime;
                            return (runtime = getEpisodeRuntime(episode)) != 0 ? runtime + " Min." : "Keine Laufzeit";
                        case EPISODE_NUMBER:
                            return episode.getAbsoluteEpisodeNumber_withoutSpecials(database.showMap.get(episode.getShowId())).map(number -> "E" + number).orElse("Specials");
                        default:
                            return null;
                    }
                })
                .setPadding(16)
                .generate();

    }

    private void reloadRecycler() {
        recycler.reload();
    }

    @NonNull
    private Helpers.AdvancedQueryHelper<Show.Episode> getEpisodeAdvancedQueryHelper() {
        return new Helpers.AdvancedQueryHelper<Show.Episode>(this, searchView)
                .setRestFilter((restQuery, episodes) -> {
                    String lowerRestQuery = restQuery.toLowerCase();
                    episodes.filter(episode -> {
                        if (episode.getName().toLowerCase().contains(lowerRestQuery))
                            return true;
                        if (database.showMap.get(episode.getShowId()).getName().toLowerCase().contains(lowerRestQuery))
                            return true;
                        return false;
                    }, true);
                })
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show.Episode, Pair<Float, Float>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_RATING, VideoActivity.ADVANCED_SEARCH_CRITERIA_RATING_REGEX)
                        .setParser(VideoActivity.getRatingParser())
                        .setBuildPredicate(ratingMinMax -> episode -> episode.getRating() >= ratingMinMax.first && episode.getRating() <= ratingMinMax.second)
                        .setApplyDialog(VideoActivity.getRatingApplyDialog()))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show.Episode, Pair<Date, Date>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_DATE, VideoActivity.ADVANCED_SEARCH_CRITERIA_DATE_REGEX)
                        .setParser(VideoActivity.getDateRangeParser())
                        .setBuildPredicate(dateDatePair -> {
                            Pair<Long, Long> dateMinMaxTime = Pair.create(dateDatePair.first.getTime(), dateDatePair.second.getTime());
                            return episode -> (
                                    episode.getDateList().stream().anyMatch(date -> {
                                                long time = CustomUtility.removeTime(date).getTime();
                                                return time >= dateMinMaxTime.first && time <= dateMinMaxTime.second;
                                            }
                                    )
                            );
                        }))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show.Episode, Pair<Date, Date>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_DURATION, VideoActivity.ADVANCED_SEARCH_CRITERIA_DURATION_REGEX)
                        .setParser(VideoActivity.getDurationParser())
                        .setBuildPredicate_fromLastAdded(helper)
                        .setApplyDialog(VideoActivity.getApplyDialogDateRangeAndDuration(this, helper)))
                .addCriteria(episodeAdvancedQueryHelper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show.Episode, List<CustomUtility.Triple<Show, Map<Integer, List<Integer>>, String>>>(ADVANCED_SEARCH_CRITERIA_SHOW, ADVANCED_SEARCH_CRITERIA_SHOW_REGEX)
                        .setParser((s, matcher) -> {
                            List<Pair<Show, String>> showSeasonsPair = Arrays.stream(s.split("(?<!\\\\)\\|"))
                                    .map(part -> part.split("(?<!\\\\)[()]"))
                                    .map(showNameSeasonsString ->
                                            Pair.create(database.showMap.values().stream().filter(show -> show.getName().equals(showNameSeasonsString[0].replaceAll("\\\\\\|", "|").replaceAll("\\\\\\(", "("))).findFirst().orElse(null),
                                                    showNameSeasonsString.length > 1 ? showNameSeasonsString[1] : null))
                                    .filter(pair -> pair.first != null)
                                    .collect(Collectors.toList());

                            List<CustomUtility.Triple<Show, Map<Integer, List<Integer>>, String>> resultList = new ArrayList<>();

                            for (Pair<Show, String> pair : showSeasonsPair) {
                                Show show = pair.first;
                                String seasonsString = pair.second;

                                if (seasonsString != null) {
                                    Map<Integer, List<Integer>> seasonMap = new HashMap<>();
                                    for (String part : seasonsString.split(",")) {
                                        if (part.contains("E")) {
                                            Pair<Integer, Integer> range = parseNumberRange(part.substring(1), show.getAllEpisodesCount());
                                            int currentAbs = 1;
                                            for (Show.Season season : show.getSeasonList()) {
                                                if (season.getSeasonNumber() < 1)
                                                    continue;
                                                if (currentAbs + season.getEpisodesCount() < range.first) {
                                                    currentAbs += season.getEpisodesCount();
                                                    continue;
                                                }

                                                int episodeNumber = 1;
                                                if (currentAbs < range.first) {
                                                    episodeNumber += range.first - currentAbs;
                                                    currentAbs = range.first;
                                                }

                                                for (; episodeNumber <= season.getEpisodesCount() && currentAbs <= range.second; episodeNumber++) {
                                                    if (seasonMap.containsKey(season.getSeasonNumber()) && seasonMap.get(season.getSeasonNumber()) != null) {
                                                        seasonMap.get(season.getSeasonNumber()).add(episodeNumber);
                                                    } else {
                                                        ArrayList<Integer> episodes = new ArrayList<>();
                                                        episodes.add(episodeNumber);
                                                        seasonMap.put(season.getSeasonNumber(), episodes);
                                                    }
                                                    currentAbs++;
                                                }

                                                if (currentAbs >= range.second)
                                                    break;
                                            }
                                            continue;
                                        }

                                        if (part.contains("-") && !part.contains("[")) {
                                            parseNumberRange_toList(part, show.getSeasonsCount()).forEach(seasonNumber -> seasonMap.putIfAbsent(seasonNumber, null));
                                        } else {
                                            if (!part.contains("["))
                                                seasonMap.putIfAbsent(Integer.parseInt(part), null);
                                            else {
                                                String[] split = part.split("[\\[\\]]");
                                                int seasonNumber = Integer.parseInt(split[0]);
                                                if (seasonNumber <= show.getSeasonList().size()) {
                                                    int episodesCount = show.getSeasonList().get(seasonNumber).getEpisodesCount();
                                                    seasonMap.put(seasonNumber, parseNumberRange_toList(split[1], episodesCount));
                                                }
                                            }
                                        }
                                    }
                                    resultList.add(CustomUtility.Triple.create(show, seasonMap, seasonsString));
                                } else
                                    resultList.add(CustomUtility.Triple.create(show, null, null));
                            }

                            return resultList;
                        })
                        .setBuildPredicate(list -> episode -> {
                            for (CustomUtility.Triple<Show, Map<Integer, List<Integer>>, String> triple : list) {
                                boolean inShow = episode.getShowId().equals(triple.first.getUuid());
                                if (!inShow)
                                    continue;
                                if (triple.second == null)
                                    return true;
                                List<Integer> episodeList;
                                if (triple.second.containsKey(episode.getSeasonNumber())) {
                                    return (episodeList = triple.second.get(episode.getSeasonNumber())) == null || episodeList.contains(episode.getEpisodeNumber());
                                }
                                return false;
                            }
                            return false;
                        })
                        .setApplyDialog((customDialog, triples, criteria) -> {
                            boolean[] negated = {false};
                            Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(R.id.dialog_advancedSearch_episode_negationLayout_show), negated);

                            Map<String, String> showIdSeasonStringMap = new HashMap<>();
                            List<String> selectedShowsIds = new ArrayList<>();
                            if (triples != null) {
                                triples.forEach(triple -> {
                                    String id = triple.first.getUuid();
                                    selectedShowsIds.add(id);
                                    showIdSeasonStringMap.put(id, triple.third);
                                });
                            }

                            FrameLayout selectShowParent = customDialog.findViewById(R.id.dialog_advancedSearch_episode_selectShow_parent);
                            WatchListActivity.videoSelectorFragmentBuilder(this, selectShowParent, CategoriesActivity.CATEGORIES.SHOW, selectedShowsIds, null, (customRecycler, id) -> {
                                Show show = database.showMap.get(id);
                                showAdvancedQuerySelectSeasonsAnsEpisodesDialog(this, show, showIdSeasonStringMap.get(id), seasonsString -> {
                                    showIdSeasonStringMap.put(id, seasonsString);
                                    customRecycler.update(id);
                                });
                            }, (itemView, id) -> {
                                ImageView icon = itemView.findViewById(R.id.listItem_collectionVideo_icon);
                                icon.setVisibility(View.VISIBLE);
                                icon.setImageTintList(ColorStateList.valueOf(Color.WHITE));

                                ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                thumbnail.setImageTintList(ColorStateList.valueOf(0x66000000));
                                if (showIdSeasonStringMap.get(id) != null)
                                    icon.setImageResource(R.drawable.ic_filter_edit);
                                else
                                    icon.setImageResource(R.drawable.ic_filter_add);

                            });

                            return customDialog1 -> {
                                if (selectedShowsIds.isEmpty())
                                    return null;

                                String query = selectedShowsIds.stream().map(id -> {
                                    String showName = database.showMap.get(id).getName()
                                            .replaceAll("\\|", "\\\\|")
                                            .replaceAll("\\(", "\\\\(");
                                    String seasonsString;
                                    if ((seasonsString = showIdSeasonStringMap.get(id)) != null)
                                        return String.format("%s(%s)", showName, seasonsString);
                                    return showName;
                                }).collect(Collectors.joining("|"));

                                return String.format(Locale.getDefault(), "%s%s:%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_SHOW, query);
                            };
                        }))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Show.Episode, Pair<Integer, Integer>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_LENGTH, VideoActivity.ADVANCED_SEARCH_CRITERIA_NUMBER_RANGE_REGEX)
                        .setParser(VideoActivity.getNumberRangeParser())
                        .setBuildPredicate(lengthMinMax -> video -> video.getLength() >= lengthMinMax.first && (lengthMinMax.second == -1 || video.getLength() <= lengthMinMax.second))
                        .setApplyDialog(VideoActivity.getLengthApplyDialog()))
                .addCriteria_defaultName(R.id.dialog_advancedSearch_episode_name, R.id.dialog_advancedSearch_episode_negationLayout_name, (helper, sub) -> {
                    String finalSub = sub.toLowerCase();
                    return episode -> {
                        if (episode.getName().toLowerCase().contains(finalSub))
                            return true;
                        Show show = database.showMap.get(episode.getShowId());
                        if (show == null)
                            return false;
                        if (show.getName().toLowerCase().contains(finalSub))
                            return true;
                        if (show.getSeasonList().get(episode.getSeasonNumber()).getName().toLowerCase().contains(finalSub))
                            return true;
                        return false;
                    };
                })
                .setDialogOptions(R.layout.dialog_advanced_search_episode, null)
                .enableColoration();
    }

    public static void showAdvancedQuerySelectSeasonsAnsEpisodesDialog(Context context, Show show, @Nullable String previousText, Utility.GenericInterface<String> onSave) {
        CustomDialog.Builder(context)
                .setTitle("Seasons Und Episoden Auswählen")
                .setText(String.format(Locale.getDefault(), "*Serie:* %s\n\n", show.getName()) +
                        "^Hinweise:^\n" +
                        "_Seasons_ können in einer Komma getrennten Liste angegeben werden:\n" +
                        "*a:*     Season a\n" +
                        "*a-b:* Von Season a bis Season b\n" +
                        "*-a:*   Von Season 1 bis Season a\n" +
                        "*a-:*   Von Season a bis maximale Season\n\n" +
                        "_Episoden_ können spezifiziert werden, indem sie nach selbigem Schema, von Eckigen-Klammern umgeben, nach einer einzelnen Season angegeben werden.\n\n" +
                        "_Absolute Episoden Nummern_ können angegeben werden, indem ein 'E' vor das obrige Schema geschrieben wird.\n\n" +
                        "_Beispiel:_\n" +
                        "/-3, 5[5, 10-], 8-10, 12-, E12-17/ ⇒ Ausgewählte Seasons: 1,2,3,5,8,9,10,12,…; Bei Season 5 die Episoden: 5,10,…; Die 12te bis 17te Episode der Serie")
                .enableTextFormatting()
                .setEdit(new CustomDialog.EditBuilder()
                        .setText(previousText)
                        .setRegEx(ADVANCED_SEARCH_CRITERIA_SHOW_REGEX_SEASON_AND_EPISODE + "|")
                        .setHint(show.getSeasonList().stream()
                                .filter(season -> !season.getName().equals(Show.EMPTY_SEASON))
                                .map(season -> String.format(Locale.getDefault(), "S%d: %dEs", season.getSeasonNumber(), season.getEpisodesCount()))
                                .collect(Collectors.joining(", "))))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                    String seasonsString = CustomUtility.stringExistsOrElse(customDialog1.getEditText(), null);
                    onSave.run(seasonsString);
                })
                .show();
    }
    /**  <------------------------- Recycler -------------------------  */


    /** ------------------------- Convenience -------------------------> */
    private int getEpisodeRuntime(Show.Episode episode) {
        int length = episode.getLength();
        if (length != 0 && length != -1)
            return length;
        String showId = episode.getShowId();
        if (showRuntimeMap.containsKey(showId))
            return showRuntimeMap.get(showId);

        Show show = database.showMap.get(showId);
        if (show.hasAverageRuntime()) {
            showRuntimeMap.put(showId, show.getAverageRuntime());
            return show.getAverageRuntime();
        }
        showRuntimeMap.put(showId, 0);
        return 0;
    }

    @NonNull
    private Optional<Date> getLatestView(Show.Episode episode) {
        return episode.getDateList().stream().max(Date::compareTo);
    }

    @NonNull
    private Long getLatestViewTime(Show.Episode episode1) {
        return getLatestView(episode1).map(Date::getTime).orElse(0L);
    }

    private Pair<Integer, Integer> parseNumberRange(String text, int max) {
        String[] startEnd = text.split("-");
        int startNr = CustomUtility.isValidReturnOrElse(startEnd[0], CustomUtility::stringExists, Integer::parseInt, s1 -> 1);
        int endNr = Math.min(CustomUtility.isValidReturnOrElse(startEnd, arr -> arr.length > 1 && CustomUtility.stringExists(arr[1]), arr -> Integer.parseInt(arr[1]), s1 -> text.endsWith("-") ? max : Integer.parseInt(startEnd[0])), max);
        return new Pair<>(startNr, endNr);
    }

    private List<Integer> parseNumberRange_toList(String text, int max) {
        Pair<Integer, Integer> pair = parseNumberRange(text, max);
        return IntStream.rangeClosed(pair.first, pair.second).boxed().collect(Collectors.toList());
    }
    /** <------------------------- Convenience ------------------------- */

    /** ------------------------- Toolbar -------------------------> */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_episode, menu);

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.taskBar_episode_sortByName) {
            sort_type = SORT_TYPE.NAME;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortByViews) {
            sort_type = SORT_TYPE.VIEWS;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortByRating) {
            sort_type = SORT_TYPE.RATING;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortByLength) {
            sort_type = SORT_TYPE.LENGTH;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortByLatest) {
            sort_type = SORT_TYPE.LATEST;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortByEpisodeNumber) {
            sort_type = SORT_TYPE.EPISODE_NUMBER;
            item.setChecked(true);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_sortReverse) {
            reverse = !reverse;
            item.setChecked(reverse);
            reloadRecycler();
        } else if (id == R.id.taskBar_episode_filter) {
            removeFocusFromSearch();
            advancedQueryHelper.showAdvancedSearchDialog();
        } else if (id == android.R.id.home) {
            if (getCallingActivity() == null)
                startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return true;
    }

    // --------------- Search

    private void setSearchHint() {
//        String join = filterTypeSet.stream().filter(ShowActivity.FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(ShowActivity.FILTER_TYPE::getName).collect(Collectors.joining(", "));
//        shows_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
//        Utility.applyToAllViews(shows_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }

    private void removeFocusFromSearch() {
        searchView.clearFocus();
    }
    /** <------------------------- Toolbar ------------------------- */

}