package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.MediaActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Show.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaEvent;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaTag;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowLabel;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomCode;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.ExternalCode;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.ImageCropUtility;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.intellij.lang.annotations.Language;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class CategoriesActivity extends AppCompatActivity {
    public static final int START_CATEGORY_SEARCH = 001;
    public static final String EXTRA_SEARCH_CATEGORY = "EXTRA_SEARCH_CATEGORY";
    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";
    private static final String ADVANCED_SEARCH_CRITERIA_DURATION = "du";
    private static final String ADVANCED_SEARCH_CRITERIA_COUNT = "c";
    @Language("RegExp")
    public static String pictureRegex = "(https?:|\\/)(([\\w$\\-_.+!*'(),/?=&@:%#]|(?<=\\S)\\s(?=\\S))+?)\\.(jpe?g|png|svg|gif)"; //"(https?:|\\/)(([^\\s\\\\<>{}]|(?<=\\S)\\s(?=\\S))+?)\\.(jpe?g|png|svg)";//"((https:)|/)[^\\n]+?\\.(?:jpe?g|png|svg)";
    //    public static String pictureRegex = "((https:)|/)([,+%&?=()/|.|\\w|\\s|-])+\\.(?:jpe?g|png|svg)";
    public static String pictureRegexAll = pictureRegex.split("\\\\\\.")[0];
    @Language("RegExp")
    public static final String uuidRegex = "\\b([a-zA-Z]+_)?[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private Helpers.SortHelper<Pair<ParentClass, Integer>> sortHelper;
    private boolean reverse = false;
    private boolean showTime = false;
    private Menu toolBarMenu;

    enum SORT_TYPE {
        NAME, COUNT, TIME
    }

    public enum CATEGORIES {
        VIDEO("Video", "Videos", Video.class, VideoActivity.class), DARSTELLER("Darsteller", "Darsteller", Darsteller.class, VideoActivity.class), STUDIOS("Studio", "Studios", Studio.class, VideoActivity.class),
        GENRE("Genre", "Genres", Genre.class, VideoActivity.class), COLLECTION("Sammlung", "Sammlungen", com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection.class, VideoActivity.class), WATCH_LIST("WatchList", "WatchLists", WatchList.class, VideoActivity.class),
        KNOWLEDGE_CATEGORIES("Kategorie", "Kategorien", KnowledgeCategory.class, KnowledgeActivity.class), CUSTOM_CODE_VIDEO("VideoCustomCode", "VideoCustomCodes", CustomCode.CustomCode_Video.class, VideoActivity.class),
        PERSON("Person", "Personen", Person.class, OweActivity.class), JOKE_CATEGORIES("Witz", "Witze", Joke.class, JokeActivity.class), SHOW_GENRES("Genre", "Genres", ShowGenre.class, ShowActivity.class),
        SHOW("Serie", "Serien", Show.class, ShowActivity.class), SHOW_LABEL("Label", "Labels", ShowLabel.class, ShowActivity.class), EPISODE("Episode", "Episoden", Show.Episode.class, ShowActivity.class),
        MEDIA("Medium", "Medien", Media.class, MediaActivity.class), MEDIA_PERSON("Person", "Personen", MediaPerson.class, MediaActivity.class),
        MEDIA_CATEGORY("Kategorie", "Kategorien", MediaCategory.class, MediaActivity.class), MEDIA_TAG("Tag", "Tags", MediaTag.class, MediaActivity.class), MEDIA_EVENT("Event", "Events", MediaEvent.class, MediaActivity.class);

        private String singular;
        private String plural;
        private Class objectClass;
        private Class searchIn;

        CATEGORIES(String singular, String plural, Class objectClass, Class searchIn) {
            this.singular = singular;
            this.plural = plural;
            this.objectClass = objectClass;
            this.searchIn = searchIn;
        }

        public String getSingular() {
            return singular;
        }

        public String getPlural() {
            return plural;
        }

        public Class getSearchIn() {
            return searchIn;
        }

        public Class getObjectClass() {
            return objectClass;
        }

        public boolean isTreeCategory() {
            return CustomUtility.boolOr(this, CATEGORIES.MEDIA_CATEGORY, MEDIA_EVENT);
        }

        public static CATEGORIES getById(String id) {
            switch (id.split("_")[0]) {
                case "video":
                    return VIDEO;
                case "darsteller":
                    return DARSTELLER;
                case "studio":
                    return STUDIOS;
                case "genre":
                    return GENRE;
                case "collection":
                    return COLLECTION;
                case "watchList":
                    return WATCH_LIST;
            }
            return null;
            // ToDo: Vervollständigen
        }
    }


    private int columnCount = 2;
    private CATEGORIES category;
    private SORT_TYPE sort_type = SORT_TYPE.NAME;
    private Database database = Database.getInstance();
    SharedPreferences mySPR_daten;
    private String searchQuery = "";
    private boolean multiSelectMode;
    private com.finn.androidUtilities.CustomList<ParentClass> selectedList = new com.finn.androidUtilities.CustomList<>();
    private com.finn.androidUtilities.CustomList<String> selectedTreeList = new com.finn.androidUtilities.CustomList<>();
    private Runnable setToolbarTitle;
    private boolean isTreeCategory;

    private CustomRecycler customRecycler;
    private SearchView categories_search;
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;

    private List<Pair<ParentClass, Integer>> allDatenObjektPairList = new ArrayList<>();
    private Map<String, Integer> treeObjectCountMap = new HashMap<>();
    private Helpers.AdvancedQueryHelper<Pair<ParentClass, Integer>> advancedQueryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_catigories);
        Settings.startSettings_ifNeeded(this);
        ExternalCode.initialize_ifNecessary(this);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        sortHelper = new Helpers.SortHelper<Pair<ParentClass, Integer>>()
                .addSorter(SORT_TYPE.NAME)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.first.getName())

                .addSorter(SORT_TYPE.COUNT)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.second)
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.TIME)
                .changeType(parentClassIntegerPair -> {
                    ParentClass parentClass = parentClassIntegerPair.first;
                    Date date = getDateFromParentClass(parentClass);
                    return date != null ? date : parentClass.getName();
                })
                .addCondition((o1, o2) -> {
                    int result;
                    if (o1 instanceof Date && o2 instanceof Date)
                        result = ((Date) o1).compareTo((Date) o2) * -1;
                    else if (o1 instanceof String && o2 instanceof String)
                        result = ((String) o1).compareTo((String) o2);
                    else
                        result = o1 instanceof Date ? -1 : 1;
                    return result * (reverse ? -1 : 1);
                })
                .enableReverseDefaultComparable()
                .finish();

        category = (CATEGORIES) getIntent().getSerializableExtra(MainActivity.EXTRA_CATEGORY);

        isTreeCategory = category.isTreeCategory();

        loadDatabase();
    }

    @Nullable
    private Date getDateFromParentClass(ParentClass parentClass) {
        List<Video> allRelatedVideos = new CustomList<>();
        switch (category) {
            case DARSTELLER:
                for (Video video : database.videoMap.values()) {
                    if (video.getDarstellerList().contains(parentClass.getUuid()))
                        allRelatedVideos.add(video);
                }
                break;
            case GENRE:
                for (Video video : database.videoMap.values()) {
                    if (video.getGenreList().contains(parentClass.getUuid()))
                        allRelatedVideos.add(video);
                }
                break;
            case STUDIOS:
                for (Video video : database.videoMap.values()) {
                    if (video.getStudioList().contains(parentClass.getUuid()))
                        allRelatedVideos.add(video);
                }
                break;
        }
        return CustomUtility.concatenateCollections(allRelatedVideos, Video::getDateList).stream().max(Date::compareTo).orElse(null);
    }

    private void loadDatabase() {
        Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_catigories);

            setAdvancedQuery();

            setObjectAndCount();
            if (!isTreeCategory)
                sortList(allDatenObjektPairList);

            setLayout();
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }

    private void setAdvancedQuery() {
        advancedQueryHelper = new Helpers.AdvancedQueryHelper<Pair<ParentClass, Integer>>(this, findViewById(R.id.search))
                .setRestFilter((restQuery, pairList) -> {
                    if (!restQuery.equals("")) {
                        String lowerCase = restQuery.toLowerCase();
                        pairList.filter(pair -> ParentClass_Alias.containsQuery(pair.first, lowerCase), true);
                    }
                })
                .addCriteria_defaultName(R.id.dialog_advancedSearch_category_name, R.id.dialog_advancedSearch_category_negationLayout_name, pair -> pair.first.getName())
                .enableColoration()
                .enableHistory("ADVANCED_QUERY_CATEGORIES")
                .setDialogOptions(R.layout.dialog_advanced_search_category, null)
                .optionalModification(helper -> {
                    if (!category.getSearchIn().equals(VideoActivity.class))
                        return;
                    helper.addCriteria(helper1 -> new Helpers.AdvancedQueryHelper.SearchCriteria<Pair<ParentClass, Integer>, Pair<Date, Date>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_DATE, VideoActivity.ADVANCED_SEARCH_CRITERIA_DATE_REGEX)
                                    .setParser(VideoActivity.getDateRangeParser())
                                    .setBuildPredicate(dateDatePair -> {
                                        Pair<Long, Long> dateMinMaxTime = Pair.create(dateDatePair.first.getTime(), dateDatePair.second.getTime());
                                        return pair -> {
                                            Date date = getDateFromParentClass(pair.first);
                                            if (date == null)
                                                return false;
                                            return date.getTime() >= dateMinMaxTime.first && date.getTime() <= dateMinMaxTime.second;
                                        };
                                    }))
                            .addCriteria(helper1 -> new Helpers.AdvancedQueryHelper.SearchCriteria<Pair<ParentClass, Integer>, Pair<Date, Date>>(VideoActivity.ADVANCED_SEARCH_CRITERIA_DURATION, VideoActivity.ADVANCED_SEARCH_CRITERIA_DURATION_REGEX)
                                    .setParser(VideoActivity.getDurationParser())
                                    .setBuildPredicate_fromLastAdded(helper)
                                    .setApplyDialog((customDialog, dateDatePair, criteria) -> {
                                        customDialog.findViewById(R.id.dialog_advancedSearch_category_dateRangeOrDuration_layout).setVisibility(View.VISIBLE);
                                        return VideoActivity.getApplyDialogDateRangeAndDuration(this, helper).runApplyDialog(customDialog, dateDatePair, criteria);
                                    }));

                })
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Pair<ParentClass, Integer>, Pair<Integer, Integer>>(ADVANCED_SEARCH_CRITERIA_COUNT, "(((\\d+)-?(\\d+)?)|((\\d+)?-?(\\d+)))")
                        .setParser(VideoActivity.getNumberRangeParser())
                        .setBuildPredicate(countMinMax -> pair -> pair.second >= countMinMax.first && (countMinMax.second == -1 || pair.second <= countMinMax.second))
                        .setApplyDialog((customDialog, countMinMax, criteria) -> {
                            boolean[] negated = {false};
                            final Integer[] minCount = {null};
                            final Integer[] maxCount = {null};

                            // ---------------

                            if (criteria.has()) {
                                minCount[0] = countMinMax.first;
                                maxCount[0] = countMinMax.second;
                                negated[0] = criteria.isNegated();
                            }
                            Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(R.id.dialog_advancedSearch_category_negationLayout_count), negated);

                            // ---------------

                            TextInputEditText minLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_category_count_min_edit);
                            TextInputEditText maxLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_category_count_max_edit);

                            if (minCount[0] != null) {
                                minLength_edit.setText(CustomUtility.isNotValueReturnOrElse(minCount[0], String::valueOf, integer -> null, -1));
                                maxLength_edit.setText(CustomUtility.isNotValueReturnOrElse(maxCount[0], String::valueOf, integer -> null, -1));
                            }

                            // ---------------

                            return customDialog1 -> {
                                String minLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_category_count_min_edit)).getText().toString().trim();
                                String maxLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_category_count_max_edit)).getText().toString().trim();

                                if (CustomUtility.stringExists(minLength_str) && CustomUtility.stringExists(maxLength_str)) {
                                    if (Objects.equals(minLength_str, maxLength_str))
                                        return String.format(Locale.getDefault(), "%s:%s", ADVANCED_SEARCH_CRITERIA_COUNT, minLength_str);
                                    else
                                        return String.format(Locale.getDefault(), "%s:%s-%s", ADVANCED_SEARCH_CRITERIA_COUNT, minLength_str, maxLength_str);
                                } else if (CustomUtility.stringExists(minLength_str))
                                    return String.format(Locale.getDefault(), "%s%s:%s-", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_COUNT, minLength_str);
                                else if (CustomUtility.stringExists(maxLength_str))
                                    return String.format(Locale.getDefault(), "%s%s:-%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_COUNT, maxLength_str);
                                return null;
                            };
                        }));

    }

    private void setLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(category.getPlural());
        setSupportActionBar(toolbar);
        elementCount = findViewById(R.id.elementCount);

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        TextView noItem = findViewById(R.id.no_item);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, toolbar.getTitle().toString());

        categories_search = findViewById(R.id.search);

        if (category.getSearchIn() == VideoActivity.class && Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_CATEGORY_SHOW_TIME)) {
            showTime = true;
            if (toolBarMenu != null)
                toolBarMenu.findItem(R.id.taskBar_category_showTime).setChecked(true);
        }

        if (isTreeCategory)
            loadTreeRecycler();
        else
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

                reloadRecycler();
                return true;
            }
        };
        categories_search.setOnQueryTextListener(textListener);
        categories_search.setQueryHint(category.getPlural() + " filtern");

        if (getIntent().hasExtra(EXTRA_SEARCH)) {
            categories_search.setQuery(getIntent().getStringExtra(EXTRA_SEARCH), false);
        }
    }

    private List<Pair<ParentClass, Integer>> filterList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
        com.finn.androidUtilities.CustomList<Pair<ParentClass, Integer>> filteredList = new com.finn.androidUtilities.CustomList<>(datenObjektPairList);
        if (CustomUtility.stringExists(searchQuery))
            advancedQueryHelper.filterFull(filteredList);
        return filteredList;
    }

    private List<Pair<ParentClass, Integer>> sortList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
        sortHelper
                .setAllReversed(reverse)
                .setList(datenObjektPairList)
                .sort(sort_type);
        return datenObjektPairList;
    }

    private void setObjectAndCount() {
        if (isTreeCategory) {
            // ToDo: Als Pairs auch summe von allen Kindern speichern
            Collection<? extends ParentClass> parentObjects;
            Utility.GenericReturnInterface<ParentClass, List<String>> getList;
            switch (category) {
                default:
                    return;
                case MEDIA_CATEGORY:
                    parentObjects = Utility.getMapFromDatabase(CATEGORIES.MEDIA).values();
                    getList = parentClass -> ((Media) parentClass).getCategoryIdList();
                    break;
            }

            List<ParentClass_Tree> allTreeObjects = ParentClass_Tree.getAll(category);
            treeObjectCountMap = allTreeObjects.stream().collect(Collectors.toMap(parentClass_tree -> ((ParentClass) parentClass_tree).getUuid(), object -> (int) parentObjects.stream().filter(o -> getList.run(o).contains(((ParentClass) object).getUuid())).count()));
        } else {
            List<Pair<ParentClass, Integer>> pairList = new ArrayList<>();
            switch (category) {
                case DARSTELLER:
                    for (ParentClass parentClass : database.darstellerMap.values()) {
                        int count = 0;
                        for (Video video : database.videoMap.values()) {
                            if (video.getDarstellerList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case GENRE:
                    for (ParentClass parentClass : database.genreMap.values()) {
                        int count = 0;
                        for (Video video : database.videoMap.values()) {
                            if (video.getGenreList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case STUDIOS:
                    for (ParentClass parentClass : database.studioMap.values()) {
                        int count = 0;
                        for (Video video : database.videoMap.values()) {
                            if (video.getStudioList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case KNOWLEDGE_CATEGORIES:
                    for (ParentClass parentClass : database.knowledgeCategoryMap.values()) {
                        int count = 0;
                        for (Knowledge knowledge : database.knowledgeMap.values()) {
                            if (knowledge.getCategoryIdList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case PERSON:
                    for (ParentClass parentClass : database.personMap.values()) {
                        final int[] count = {0};
                        for (Owe owe : database.oweMap.values()) {
                            owe.getItemList().stream().filter(item -> item.getPersonId().equals(parentClass.getUuid())).forEach(item -> count[0] += Math.round(item.getAmount()));
                        }
                        pairList.add(new Pair<>(parentClass, count[0]));
                    }
                    break;
                case JOKE_CATEGORIES:
                    for (ParentClass parentClass : database.jokeCategoryMap.values()) {
                        int count = 0;
                        for (Joke joke : database.jokeMap.values()) {
                            if (joke.getCategoryIdList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case SHOW_GENRES:
                    for (ParentClass parentClass : database.showGenreMap.values()) {
                        int count = 0;
                        for (Show show : database.showMap.values()) {
                            if (show.getGenreIdList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case MEDIA_PERSON:
                    for (ParentClass parentClass : database.mediaPersonMap.values()) {
                        int count = 0;
                        for (Media media : database.mediaMap.values()) {
                            if (media.getPersonIdList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;
                case MEDIA_TAG:
                    for (ParentClass parentClass : database.mediaTagMap.values()) {
                        int count = 0;
                        for (Media media : database.mediaMap.values()) {
                            if (media.getTagIdList().contains(parentClass.getUuid()))
                                count++;
                        }
                        pairList.add(new Pair<>(parentClass, count));
                    }
                    break;

            }
            // ToDo: Abstrahieren ^^
            allDatenObjektPairList = pairList;
        }
    }


    //  ------------------------- Recycler ------------------------->
    private void loadRecycler() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        int topMargin = CustomUtility.dpToPx(5);
        int bottomMargin = CustomUtility.dpToPx(21);
        int defaultMargin = CustomUtility.dpToPx(3);
        int itemHeight = CustomUtility.dpToPx(70);
        final int[] size = new int[1];
        customRecycler = new CustomRecycler<Pair<ParentClass, Integer>>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setGetActiveObjectList((customRecycler) -> {
                    List<Pair<ParentClass, Integer>> filteredList = sortList(filterList(allDatenObjektPairList));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = categories_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    size[0] = filteredList.size();

                    noItem.setText(size[0] == 0 ? text : "");
                    String elementCountText = size[0] > 1 ? size[0] + " Elemente" : (size[0] == 1 ? "Ein" : "Kein") + " Element";
                    elementCount.setText(elementCountText);
                    return filteredList;

                })
                .setSetItemContent((customRecycler, itemView, parentClassIntegerPair, index) -> {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                    if (index < columnCount)
                        params.setMargins(defaultMargin, topMargin + defaultMargin, defaultMargin, defaultMargin);
                    else if (index + 1 >= size[0] - ((size[0] - 1) % columnCount))
                        params.setMargins(defaultMargin, defaultMargin, defaultMargin, bottomMargin + defaultMargin);
                    else
                        params.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
                    itemView.setLayoutParams(params);

                    // ---------------

                    ParentClass parentClass = parentClassIntegerPair.first;
                    ((TextView) itemView.findViewById(R.id.listItem_categoryItem_name)).setText(parentClass.getName());

                    if (category == CATEGORIES.PERSON) {
                        final double[] allOwn = {0};
                        final double[] allOther = {0};
                        database.oweMap.values().forEach(owe -> owe.getItemList().forEach(item -> {
                            if (item.isOpen()) {
                                if (item.getPersonId().equals(parentClass.getUuid())) {
                                    if (owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
                                        allOwn[0] += item.getAmount();
                                    else
                                        allOther[0] += item.getAmount();
                                }
                            }
                        }));
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                        if (allOwn[0] != 0)
                            stringBuilder.append("E: " + Utility.formatToEuro(allOwn[0]), new ForegroundColorSpan(Color.RED), Spannable.SPAN_COMPOSING);
                        if (allOther[0] != 0) {
                            if (!stringBuilder.toString().isEmpty())
                                stringBuilder.append(" & ");
                            stringBuilder.append("F: " + Utility.formatToEuro(allOther[0]), new ForegroundColorSpan(getColor(R.color.colorGreen)), Spannable.SPAN_COMPOSING);
                        }
                        if (stringBuilder.toString().isEmpty())
                            stringBuilder.append("<Keine Schulden>", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_COMPOSING);

                        ((TextView) itemView.findViewById(R.id.userListItem_categoryItem_count)).setText(stringBuilder);
                    } else {
                        TextView itemCountTextView = itemView.findViewById(R.id.userListItem_categoryItem_count);
                        String timeExtra = "";
                        Date date;
                        if (showTime && (date = getDateFromParentClass(parentClass)) != null) {
                            long days = Days.daysBetween(new LocalDate(date), new LocalDate(new Date())).getDays();
                            timeExtra = " – " + Helpers.DurationFormatter.formatDefault(Duration.ofDays(days), "'%d% d'");
                        }
                        itemCountTextView.setText(parentClassIntegerPair.second + timeExtra);
                    }

                    ImageView listItem_categoryItem_image = itemView.findViewById(R.id.listItem_categoryItem_image);
                    if (parentClass instanceof ParentClass_Image && Utility.stringExists(((ParentClass_Image) parentClass).getImagePath())) {
                        listItem_categoryItem_image.setVisibility(View.VISIBLE);
                        String imagePath = ((ParentClass_Image) parentClass).getImagePath();
                        Utility.loadUrlIntoImageView(this, listItem_categoryItem_image, ImageCropUtility.applyCropTransformation(parentClass), Utility.getTmdbImagePath_ifNecessary(imagePath, false),
                                Utility.getTmdbImagePath_ifNecessary(imagePath, true),
                                () -> {
                                    if (parentClass instanceof ParentClass_Tmdb)
                                        ((ParentClass_Tmdb) parentClass).tryUpdateData(this, () ->
                                                customRecycler.update(customRecycler.getRecycler().getChildAdapterPosition(itemView)));
                                },
                                () -> Utility.roundImageView(listItem_categoryItem_image, 4), this::removeFocusFromSearch);
                    } else
                        listItem_categoryItem_image.setVisibility(View.GONE);

                    CheckBox userListItem_categoryItem_check = itemView.findViewById(R.id.userListItem_categoryItem_check);
                    userListItem_categoryItem_check.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
                    userListItem_categoryItem_check.setChecked(selectedList.contains(parentClass));
                })
                .setRowOrColumnCount(columnCount)
                .setOnClickListener((customRecycler, view, parentClassIntegerPair, index) -> {
                    if (!multiSelectMode) {
                        removeFocusFromSearch();
                        startActivityForResult(new Intent(this, category.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, escapeForSearchExtra(parentClassIntegerPair.first.getName()))
                                        .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                START_CATEGORY_SEARCH);
                    } else {
                        if (selectedList.contains(parentClassIntegerPair.first))
                            selectedList.remove(parentClassIntegerPair.first);
                        else
                            selectedList.add(parentClassIntegerPair.first);
                        customRecycler.update(index);
                    }
                })
                .setOnLongClickListener((customRecycler, view, item, index) -> {
                    if (!Utility.isOnline(this))
                        return;

                    ParentClass parentClass = item.first;
                    removeFocusFromSearch();
                    showEditCategoryDialog(this, category, parentClass, editObject -> reloadRecycler(), editObject -> {
                        allDatenObjektPairList.removeIf(pair -> Objects.equals(pair.first, parentClass));
                        treeObjectCountMap.remove(editObject.getUuid());
                        setResult(RESULT_OK);
                        reloadRecycler();
                    }, null, null, null, null);
                })
                .enableFastScroll(Pair.create(topMargin, bottomMargin), (customRecycler1, pair, integer) -> {
                    switch (sort_type) {
                        case NAME:
                            return pair.first.getName().substring(0, 1);
                        case COUNT:
                            return String.valueOf(pair.second);
                        case TIME:
                            Date date = getDateFromParentClass(pair.first);
                            if (date == null)
                                return "Keine Ansichten";
                            return dateFormat.format(date);
                        default:
                            return null;
                    }
                })
                .generate();
    }

    private void loadTreeRecycler() {
        // ToDo: Löschen doppelt bestätigen wenn Kinder noch vorhanden
        customRecycler = new CustomRecycler<Pair<ParentClass, Integer>>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.empty_layout)
                .setGetActiveObjectList((customRecycler) -> new CustomList<>(Pair.create(null, null)))
                .setSetItemContent((customRecycler1, itemView, parentClassIntegerPair, index) -> {
                    Pair<TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener> clickListenerPair = Pair.create((node, value) -> {
                        startActivityForResult(new Intent(this, category.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, escapeForSearchExtra(((ParentClass) value).getName()))
                                        .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                START_CATEGORY_SEARCH);
                    }, (node, value) -> {
                        if (!Utility.isOnline(this))
                            return false;

                        ParentClass parentClass = (ParentClass) value;
                        removeFocusFromSearch();
                        showEditCategoryDialog(this, category, parentClass, editObject -> reloadRecycler(), editObject -> {
                            allDatenObjektPairList.removeIf(pair -> Objects.equals(pair.first, parentClass));
                            treeObjectCountMap.remove(editObject.getUuid());
                            setResult(RESULT_OK);
                            reloadRecycler();
                        }, null, null, null, null);
                        return true;
                    });
                    CustomUtility.Triple<AndroidTreeView, View, TreeNode> triple = ParentClass_Tree.buildTreeView((ViewGroup) itemView, category,
                            (multiSelectMode ? selectedTreeList : null), searchQuery, null,
                            (o1, o2) -> {
                                switch (sort_type) {
                                    default:
                                        return 0;
                                    case NAME:
                                        return o1.getName().compareTo(o2.getName());
                                    case COUNT:
                                        return treeObjectCountMap.get(o1.getUuid()).compareTo(treeObjectCountMap.get(o2.getUuid())) * (reverse ? -1 : 1);
                                }
                            }, findViewById(R.id.no_item), (multiSelectMode ? null : clickListenerPair),
                            (viewGroup, node) -> {
                                TextView textSecondary = viewGroup.findViewById(R.id.customTreeNode_text_secondary);
                                ParentClass value = (ParentClass) node.getValue();
                                if (!CustomUtility.stringExists(searchQuery) || Utility.containsIgnoreCase(value.getName(), searchQuery)) {
                                    textSecondary.setVisibility(View.VISIBLE);
//                                    ((LinearLayout) textSecondary.getParent().getParent()).setClickable(true);
                                } else {
                                    textSecondary.setVisibility(View.GONE);
//                                    ((LinearLayout) textSecondary.getParent().getParent()).setClickable(false);
                                }
                                textSecondary.setText("" + treeObjectCountMap.get(value.getUuid()));
                            });

                    int size = ParentClass_Tree.getAllCount(triple.third, searchQuery);
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    elementCount.setText(elementCountText);
                })
                .enableFastScroll(null, true, null, null)
                .generate();

    }

    private void reloadRecycler() {
        customRecycler.reload();
    }
    //  <------------------------- Recycler -------------------------


    //  ------------------------- Edit ------------------------->
    public static void showEditCategoryDialog(AppCompatActivity context, CATEGORIES category, @Nullable ParentClass oldObject, @Nullable CustomUtility.GenericInterface<ParentClass> onSave, @Nullable CustomUtility.GenericInterface<ParentClass> onDelete, @Nullable CustomUtility.GenericInterface<CustomDialog> optionalModifications, @Nullable CustomUtility.GenericInterface<ParentClass> performAddNew, @Nullable CustomUtility.GenericInterface<ParentClass> performDelete, @Nullable CustomUtility.GenericReturnInterface<String, ParentClass> getObjByName) {
        if (!CustomUtility.isOnline(context))
            return;

        boolean isEdit = oldObject != null;
        ParentClass editObject = isEdit ? (ParentClass) oldObject.clone() : ParentClass.newCategory(category, "");

        Utility.GenericInterface<CustomDialog> setPreviewCrop = (customDialog) -> {
            View preview = customDialog.findViewById(R.id.dialog_editTmdbCategory_preview);
            View previewCrop = customDialog.findViewById(R.id.dialog_editTmdbCategory_previewCrop);
            ImageCropUtility.ImageCrop imageCrop;
            if (editObject instanceof ImageCropUtility && (imageCrop = ((ImageCropUtility) editObject).getImageCrop()) != null && customDialog.findViewById(R.id.dialog_editTmdbCategory_preview).getVisibility() == View.VISIBLE) {
                previewCrop.setVisibility(View.VISIBLE);
                int width = preview.getWidth();
                int height = (int) (((double) imageCrop.getFullHeight() / imageCrop.getFullWidth()) * preview.getWidth());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (width * imageCrop.getWidth()), (int) (height * imageCrop.getHeight()));
                params.setMargins((int) (width * imageCrop.getX()), (int) (height * imageCrop.getY()), 0, 0);
                previewCrop.setLayoutParams(params);
            } else
                previewCrop.setVisibility(View.GONE);
        };

        CustomDialog.Builder(context)
                .setTitle(category.getSingular() + (isEdit ? " Bearbeiten, oder Löschen" : " Hinzufügen"))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog -> {
                    if (isEdit)
                        customDialog
                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                    if (category.isTreeCategory() && !((ParentClass_Tree) editObject).getChildren().isEmpty()) {
                                        Toast.makeText(context, "Kategorie mit Unterkategorien kann noch nicht gelöscht werden", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    CustomDialog.Builder(context)
                                            .setTitle(category.getSingular() + " Löschen")
                                            .setText(new Helpers.SpannableStringHelper().append("Möchtest du wirklich '").appendBold(editObject.getName()).append("' löschen?").get())
                                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.DELETE_CANCEL)
                                            .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog2 -> {
                                                customDialog1.dismiss();
                                                if (performDelete == null)
                                                    removeCategory(category, oldObject, onDelete);
                                                else
                                                    performDelete.run(oldObject);
                                            })
                                            .show();
                                }, false)
                                .transformLastAddedButtonToImageButton();
                })
                .enableDynamicWrapHeight(context)
                .enableAutoUpdateDynamicWrapHeight()
                .addOptionalModifications(customDialog -> {
                    if (editObject instanceof ParentClass_Image) {
                        customDialog
                                .addButton("Testen", customDialog1 -> {
                                    String url = ((EditText) customDialog1.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                                    ImageView preview = customDialog1.findViewById(R.id.dialog_editTmdbCategory_preview);
                                    if (CustomUtility.stringExists(url)) {
                                        Utility.loadUrlIntoImageView(context, preview, Utility.getTmdbImagePath_ifNecessary(url, true), null, null, () -> {
                                            setPreviewCrop.run(customDialog);
                                        });
                                        preview.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "Nichts eingegeben", Toast.LENGTH_SHORT).show();
                                        preview.setVisibility(View.GONE);
                                        setPreviewCrop.run(customDialog);
                                    }
                                }, false);
                    }
                })
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    if (!Utility.isOnline(context))
                        return;
                    ParentClass_Alias.applyNameAndAlias(editObject, ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim());
                    if (editObject instanceof ParentClass_Image) {
                        String url = ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                        ((ParentClass_Image) editObject).setImagePath(CustomUtility.stringExistsOrElse(url, null));
                    }
                    if (isEdit)
                        oldObject.getChangesFrom(editObject);
                    else {
                        if (performAddNew == null)
                            ((Map<String, ParentClass>) Utility.getMapFromDatabase(category)).put(editObject.getUuid(), editObject);
                        else
                            performAddNew.run(editObject);
                    }
                    CustomUtility.runGenericInterface(onSave, isEdit ? oldObject : editObject);
                    Database.saveAll(context);
                })
                .setView(R.layout.dialog_edit_tmdb_category)
                .setSetViewContent((customDialog, view1, reload) -> {
                    TextInputLayout dialog_editTmdbCategory_name_layout = view1.findViewById(R.id.dialog_editTmdbCategory_name_layout);
                    dialog_editTmdbCategory_name_layout.getEditText().setText(ParentClass_Alias.combineNameAndAlias(editObject));
                    Utility.applySelectionSearch(context, category, dialog_editTmdbCategory_name_layout.getEditText());

                    com.finn.androidUtilities.Helpers.TextInputHelper helper =
                            new com.finn.androidUtilities.Helpers.TextInputHelper(dialog_editTmdbCategory_name_layout)
                                    .defaultDialogValidation(customDialog)
                                    .setValidation(dialog_editTmdbCategory_name_layout, (validator, text) -> {
                                        ParentClass parentClass = getObjByName == null ? Utility.findObjectByName(category, text) : getObjByName.run(text);
                                        if (parentClass != null && parentClass != oldObject)
                                            validator.setInvalid(category.singular + " bereits vorhanden");
                                    });

                    if (editObject instanceof ParentClass_Alias) {
//                                    helper.setInputType(dialog_editTmdbCategory_name_layout, com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE);
                        dialog_editTmdbCategory_name_layout.getEditText().setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    }

                    if (editObject instanceof ParentClass_Image) {
                        if (editObject instanceof ParentClass_Tmdb) {
                            ImageView dialog_editTmdbCategory_internet = view1.findViewById(R.id.dialog_editTmdbCategory_internet);
                            if (((ParentClass_Tmdb) editObject).getTmdbId() != 0) {
                                dialog_editTmdbCategory_internet.setOnClickListener(v -> {
                                    CustomDialog.Builder(context)
                                            .setTitle("Öffnen mit...")
                                            .addButton("TMDB", customDialog1 -> Utility.openUrl(context, "https://www.themoviedb.org/person/" + ((ParentClass_Tmdb) editObject).getTmdbId(), true))
                                            .addButton("IMDB", customDialog1 -> {
                                                String requestUrl = "https://api.themoviedb.org/3/person/" +
                                                        ((ParentClass_Tmdb) editObject).getTmdbId() +
                                                        "/external_ids?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
                                                RequestQueue requestQueue = Volley.newRequestQueue(context);

                                                Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();


                                                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
                                                    try {
                                                        String imdb_id = response.getString("imdb_id");
                                                        Utility.openUrl(context, "https://www.imdb.com/name/" + imdb_id, true);
                                                    } catch (JSONException ignored) {
                                                    }

                                                }, error -> Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show());

                                                requestQueue.add(jsonArrayRequest);

                                            })
                                            .enableButtonDividerAll()
                                            .enableExpandButtons()
                                            .enableButtonDividerAll()
                                            .show();
                                });
                                dialog_editTmdbCategory_internet.setVisibility(View.VISIBLE);
                            }
                        }

                        CustomUtility.setMargins(view1.findViewById(R.id.dialog_editTmdbCategory_nameLayout), -1, -1, -1, 0);
                        view1.findViewById(R.id.dialog_editTmdbCategory_urlLayout).setVisibility(View.VISIBLE);
                        TextInputLayout dialog_editTmdbCategory_url_layout = view1.findViewById(R.id.dialog_editTmdbCategory_url_layout);
                        String imagePath = ((ParentClass_Image) editObject).getImagePath();
                        dialog_editTmdbCategory_url_layout.getEditText().setText(imagePath);
                        ImageView preview = customDialog.findViewById(R.id.dialog_editTmdbCategory_preview);
                        if (CustomUtility.stringExists(imagePath)) {
                            Utility.loadUrlIntoImageView(context, preview, Utility.getTmdbImagePath_ifNecessary(imagePath, true), null, null, () -> {
                                setPreviewCrop.run(customDialog);
                            });
                            preview.setVisibility(View.VISIBLE);
//                            setPreviewCrop.run(customDialog);
                        }
                        helper.addValidator(dialog_editTmdbCategory_url_layout).setValidation(dialog_editTmdbCategory_url_layout, (validator, text) -> {
                            validator.asWhiteList();
                            if (text.isEmpty() || text.matches(pictureRegexAll) || text.matches(ActivityResultHelper.uriRegex))
                                validator.setValid();
                            if (text.toLowerCase().contains("http") && !text.toLowerCase().contains("https"))
                                validator.setInvalid("Die URL muss 'https' sein!");
                            if (editObject instanceof ImageCropUtility) {
                                if (!Objects.equals(text, ((ParentClass_Image) editObject).getImagePath())) {
                                    ((ImageCropUtility) editObject).setImageCrop(null);
                                    setPreviewCrop.run(customDialog);
                                }
                            }
                        }).validate((TextInputLayout[]) null);

                        view1.findViewById(R.id.dialog_editTmdbCategory_localStorage).setOnClickListener(v -> {
                            ActivityResultHelper.addFileChooserRequest(context, "image/*", o -> {
                                String path = ActivityResultHelper.getPath(context, o.getData());
                                dialog_editTmdbCategory_url_layout.getEditText().setText(path);
                                customDialog.getButtonByName("Testen").click();
                            });
                        });

                        if (editObject instanceof ImageCropUtility) {
                            View cropButton = view1.findViewById(R.id.dialog_editTmdbCategory_crop);
                            cropButton.setVisibility(View.VISIBLE);
                            cropButton.setOnClickListener(v -> {
                                ((ParentClass_Image) editObject).setImagePath(CustomUtility.stringExistsOrElse(dialog_editTmdbCategory_url_layout.getEditText().getText().toString(), null));
                                ((ImageCropUtility) editObject).selectCropForImage(context, ((ImageCropUtility) editObject).getImageCrop(), imageCrop -> {
                                    ((ImageCropUtility) editObject).setImageCrop(imageCrop);
                                    setPreviewCrop.run(customDialog);
                                });
                            });
                            cropButton.setOnLongClickListener(v -> {
                                ((ImageCropUtility) editObject).setImageCrop(null);
                                Toast.makeText(context, "Crop Zurückgesetzt", Toast.LENGTH_SHORT).show();
                                setPreviewCrop.run(customDialog);
                                return true;
                            });

                        } else {
                            view1.findViewById(R.id.dialog_editTmdbCategory_crop).setVisibility(View.GONE);
                            setPreviewCrop.run(customDialog);
                        }
                    }
                })
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String editNameText = ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim();
                    if (isEdit) {
                        boolean nameResult = !editNameText.equals(ParentClass_Alias.combineNameAndAlias(editObject));
                        return nameResult
                                || (editObject instanceof ParentClass_Image && !Utility.boolOr(((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim(), CustomUtility.stringExistsOrElse(((ParentClass_Image) oldObject).getImagePath(), "")))
                                || editObject instanceof ImageCropUtility && !Objects.equals(((ImageCropUtility) oldObject).getImageCrop(), ((ImageCropUtility) editObject).getImageCrop());
                    } else {
                        return CustomUtility.stringExists(editNameText) || (editObject instanceof ParentClass_Image && CustomUtility.stringExists(((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim()));
                    }
                })
                .addOptionalModifications(customDialog -> CustomUtility.runGenericInterface(optionalModifications, customDialog))
                .show();
    }

    public static void removeCategory(CategoriesActivity.CATEGORIES category, ParentClass parentClass, @Nullable CustomUtility.GenericInterface<ParentClass> onDelete) {
        Database database = Database.getInstance();
        switch (category) {
            case DARSTELLER:
                database.darstellerMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getDarstellerList().remove(parentClass.getUuid());
                }
                break;
            case STUDIOS:
                database.studioMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getStudioList().remove(parentClass.getUuid());
                }
                break;
            case GENRE:
                database.genreMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getGenreList().remove(parentClass.getUuid());
                }
                break;
            case KNOWLEDGE_CATEGORIES:
                database.knowledgeCategoryMap.remove(parentClass.getUuid());
                for (Knowledge knowledge : database.knowledgeMap.values()) {
                    knowledge.getCategoryIdList().remove(parentClass.getUuid());
                }
                break;
            case PERSON:
                database.personMap.remove(parentClass.getUuid());
                for (Owe owe : database.oweMap.values()) {
                    owe.setItemList(owe.getItemList().stream().filter(item1 -> !item1.getPersonId().equals(parentClass.getUuid())).collect(Collectors.toList()));
                }
                break;
            case JOKE_CATEGORIES:
                database.jokeCategoryMap.remove(parentClass.getUuid());
                for (Joke joke : database.jokeMap.values()) {
                    joke.getCategoryIdList().remove(parentClass.getUuid());
                }
                break;
            case SHOW_GENRES:
                database.showGenreMap.remove(parentClass.getUuid());
                for (Show show : database.showMap.values()) {
                    show.getGenreIdList().remove(parentClass.getUuid());
                }
                break;
            case MEDIA_PERSON:
                database.mediaPersonMap.remove(parentClass.getUuid());
                for (Media media : database.mediaMap.values()) {
                    media.getPersonIdList().remove(parentClass.getUuid());
                }
                break;
            case MEDIA_CATEGORY:
                String parentId = ((ParentClass_Tree) parentClass).getParentId();
                if (CustomUtility.stringExists(parentId)) {
                    ((ParentClass_Tree) ParentClass_Tree.findObjectById(category, parentId)).getChildren().remove(parentClass);
                } else
                    database.mediaCategoryMap.remove(parentClass.getUuid());

                for (Media media : database.mediaMap.values()) {
                    media.getCategoryIdList().remove(parentClass.getUuid());
                }
                break;
            case MEDIA_TAG:
                database.mediaTagMap.remove(parentClass.getUuid());
                for (Media media : database.mediaMap.values()) {
                    media.getTagIdList().remove(parentClass.getUuid());
                }
                break;
        }
        // ToDo: abstrahieren
        Database.saveAll();
        CustomUtility.runGenericInterface(onDelete, parentClass);
    }
    //  <------------------------- Edit -------------------------


    private void showRandomDialog() {
        // ToDo: auch Bilder anzeigen
        CustomList<Pair<ParentClass, Integer>> filteredDatenObjektPairList = new CustomList<>(sortList(filterList(allDatenObjektPairList)));
        if (filteredDatenObjektPairList.isEmpty()) {
            Toast.makeText(this, "Die Auswahl ist leer", Toast.LENGTH_SHORT).show();
            return;
        }
        removeFocusFromSearch();
        final Pair<ParentClass, Integer>[] randomPair = new Pair[]{filteredDatenObjektPairList.removeRandom()};
        CustomDialog
                .Builder(this)
                .setTitle("Zufall")
                .setText(randomPair[0].first.getName() + " (" + randomPair[0].second + ")")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Nochmal", customDialog -> {
                    if (filteredDatenObjektPairList.isEmpty()) {
                        Toast.makeText(this, "Alle wurden bereits vorgeschlagen", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    randomPair[0] = filteredDatenObjektPairList.removeRandom();
                    CustomDialog.changeText(customDialog, randomPair[0].first.getName() + " (" + randomPair[0].second + ")");
                }, false)
                .addButton("Suchen", customDialog -> {
                    startActivityForResult(new Intent(this, category.getSearchIn())
                                    .putExtra(EXTRA_SEARCH, escapeForSearchExtra(randomPair[0].first.getName()))
                                    .putExtra(EXTRA_SEARCH_CATEGORY, category),
                            START_CATEGORY_SEARCH);
                })
                .colorLastAddedButton()
                .show();
    }

    public static <T> com.finn.androidUtilities.CustomList<String> getCategoriesIntersection(List<T> list, CategoriesActivity.CATEGORIES category) {
        com.finn.androidUtilities.CustomList<String> intersectionList;
        Utility.GenericReturnInterface<T, List<String>> getCategoryList;
        if (list.isEmpty()) {
            return new com.finn.androidUtilities.CustomList<>();
        } else {
            switch (category) {
                default:
                    return new com.finn.androidUtilities.CustomList<>();
                case MEDIA_PERSON:
                    getCategoryList = t -> ((Media) t).getPersonIdList();
                    break;
                case MEDIA_CATEGORY:
                    getCategoryList = t -> ((Media) t).getCategoryIdList();
                    break;
                case MEDIA_TAG:
                    getCategoryList = t -> ((Media) t).getTagIdList();
                    break;
            }
            intersectionList = new com.finn.androidUtilities.CustomList<>(getCategoryList.run(list.get(0)));
            if (list.size() > 1)
                list.forEach(media -> intersectionList.retainAll(getCategoryList.run(media)));
            return intersectionList;
        }
    }

    public static String joinCategoriesIds(List<String> idList, CATEGORIES category) {
        return joinCategoriesIds(idList, category, ", ");
    }

    public static String joinCategoriesIds(List<String> idList, CATEGORIES category, String delimiter) {
        return joinCategoriesIds(idList, category, delimiter, false);
    }

    public static String joinCategoriesIds(List<String> idList, CATEGORIES category, String delimiter, boolean escape) {
        // ToDo: Auch ParentClass_Tree unterstützen

        return idList.stream().map(id -> {
            if (escape)
                return CategoriesActivity.escapeForSearchExtra(Utility.findObjectById(category, id).getName());
            return Utility.findObjectById(category, id).getName();
        }).collect(Collectors.joining(delimiter));
    }


    //  ------------------------- ToolBar ------------------------->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolBarMenu = menu;
        getMenuInflater().inflate(R.menu.task_bar_catigory, toolBarMenu);

        if (setToolbarTitle != null) setToolbarTitle.run();
        menu.findItem(R.id.taskBar_category_sortByTime).setVisible(category.getSearchIn().equals(VideoActivity.class));
        menu.findItem(R.id.taskBar_category_showTime).setVisible(category.getSearchIn().equals(VideoActivity.class));
        menu.findItem(R.id.taskBar_category_sortTree).setVisible(isTreeCategory);
        if (isTreeCategory) {
            menu.findItem(R.id.taskBar_category_random).setVisible(false);
            menu.findItem(R.id.taskBar_category_showAs).setVisible(false);
        }

        if (showTime)
            toolBarMenu.findItem(R.id.taskBar_category_showTime).setChecked(true);

        new Handler().post(() -> {
            final View v = findViewById(R.id.taskBar_category_sort);

            if (v != null) {
                v.setOnLongClickListener(v1 -> {
                    advancedQueryHelper.showAdvancedSearchDialog();
                    return true;
                });
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_category_add:
//                Utility.currentMode = Utility.modeList.next(Utility.currentMode, true);
//                Toast.makeText(this, Utility.currentMode.name(), Toast.LENGTH_LONG).show();
//                reloadRecycler();
//
//                if (true)
//                    return true;
                removeFocusFromSearch();
                showEditCategoryDialog(this, category, null, parentClass -> {
                    if (parentClass instanceof ParentClass_Tree)
                        treeObjectCountMap.put(parentClass.getUuid(), 0);
                    else
                        allDatenObjektPairList.add(new Pair<>(parentClass, 0));
                    reloadRecycler();
                }, null, null, null, null, null);
                break;
            case R.id.taskBar_category_multiSelect:
                if ((selectedList.isEmpty() || isTreeCategory) && selectedTreeList.isEmpty()) {
                    multiSelectMode = !multiSelectMode;
                    reloadRecycler();
                } else {
                    if (!selectedTreeList.isEmpty())
                        selectedList.replaceWith(selectedTreeList.map(uuid -> (ParentClass) ParentClass_Tree.findObjectById(category, uuid)));
                    if (selectedList.size() == 1) {
                        startActivityForResult(new Intent(this, category.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, escapeForSearchExtra(selectedList.get(0).getName()))
                                        .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                START_CATEGORY_SEARCH);
                        break;
                    }
                    CustomDialog.Builder(this)
                            .setTitle("Aktion Auswählen")
                            .setText("Möchtest du als '&', oder '|' suchen, oder die Auswahl zurücksetzen?")
                            .addButton("Zurücksetzen", customDialog -> {
                                multiSelectMode = false;
                                selectedList.clear();
                                reloadRecycler();
                            })
                            .alignPreviousButtonsLeft()
                            .addButton("&", customDialog -> {
                                startActivityForResult(new Intent(this, category.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(parentClass -> escapeForSearchExtra(parentClass.getName())).collect(Collectors.joining(" & ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                        START_CATEGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .addButton("|", customDialog -> {
                                startActivityForResult(new Intent(this, category.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(parentClass -> escapeForSearchExtra(parentClass.getName())).collect(Collectors.joining(" | ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                        START_CATEGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .show();
                }
                break;
            case R.id.taskBar_category_random:
                showRandomDialog();
                break;
            case R.id.taskBar_category_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reloadRecycler();
                break;
            case R.id.taskBar_category_sortByViews:
                sort_type = SORT_TYPE.COUNT;
                item.setChecked(true);
                reloadRecycler();
                break;
            case R.id.taskBar_category_sortByTime:
                sort_type = SORT_TYPE.TIME;
                item.setChecked(true);
                reverse = true;
                toolBarMenu.findItem(R.id.taskBar_category_sortReverse).setChecked(true);
                showTime = true;
                toolBarMenu.findItem(R.id.taskBar_category_showTime).setChecked(true);
                reloadRecycler();
                break;
            case R.id.taskBar_category_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reloadRecycler();
                break;

            case R.id.taskBar_category_showAs:
                if (columnCount == 2) {
                    columnCount = 1;
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_as_grid));
                } else {
                    columnCount = 2;
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_as_list));
                }
                customRecycler.setRowOrColumnCount(columnCount).reloadNew();
                break;
            case R.id.taskBar_category_showTime:
                showTime = !item.isChecked();
                item.setChecked(showTime);
                reloadRecycler();
                break;
            case R.id.taskBar_category_sortTree:
                ParentClass_Tree.showReorderTreeDialog(this, category, customDialog -> reloadRecycler());
                break;
            case R.id.taskBar_category_advancedQuery:
                advancedQueryHelper.showAdvancedSearchDialog();
                break;

            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void removeFocusFromSearch() {
        categories_search.clearFocus();
    }
    //  <------------------------- ToolBar -------------------------


    /**
     * ------------------------- Convenience ------------------------->
     */
    public static String escapeForSearchExtra(String s) {
        if (s.contains("&"))
            return s.replaceAll("&", "\\\\&");
        else if (s.contains("|"))
            return s.replaceAll("\\|", "\\\\|");
        else
            return s;
    }

    public static String deEscapeForSearchExtra(String s) {
        if (s.contains("\\&"))
            return s.replaceAll("\\\\&", "&");
        else if (s.contains("\\|"))
            return s.replaceAll("\\\\\\|", "\\|");
        else
            return s;
    }
    /**
     * <------------------------- Convenience -------------------------
     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK /*&& requestCode == START_VIDEOS*/) {
            setObjectAndCount();
            textListener.onQueryTextChange(categories_search.getQuery().toString());
            reloadRecycler();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        if (Utility.stringExists(categories_search.getQuery().toString()) && !Objects.equals(categories_search.getQuery().toString(), getIntent().getStringExtra(EXTRA_SEARCH))) {
            categories_search.setQuery("", false);
            return;
        } else if (multiSelectMode) {
            multiSelectMode = false;
            selectedList.clear();
            selectedTreeList.clear();
            reloadRecycler();
            return;
        }

        super.onBackPressed();
    }
}
