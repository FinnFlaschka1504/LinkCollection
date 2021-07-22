package com.maxMustermannGeheim.linkcollection.Activities.Content.Videos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.appbar.AppBarLayout;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.ImageAdapterItem;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.MinDimensionLayout;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.mikhaellopez.lazydatepicker.LazyDatePicker;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class VideoActivity extends AppCompatActivity {
    public static final String SEEN_SEARCH = "SEEN_SEARCH";
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";

    public static final String EXTRA_INTENT_TEST = "EXTRA_INTENT_TEST";

    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST
    }

    enum MODE {
        ALL, SEEN, LATER, UPCOMING
    }

    public enum FILTER_TYPE {
        NAME("Titel"), ACTOR("Darsteller"), GENRE("Genre"), STUDIO("Studio"), COLLECTION("Sammlung");

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

    private Database database;
    private SharedPreferences mySPR_daten;
    private boolean delete = false;
    private List<String> toDelete = new ArrayList<>();
    private Video randomVideo;
    private boolean scrolling = true;
    private boolean showImages;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.ACTOR, FILTER_TYPE.GENRE, FILTER_TYPE.STUDIO, FILTER_TYPE.COLLECTION));
    private MODE mode = MODE.ALL;
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;
    private String searchQuery = "";
    private boolean reverse = false;
    private String singular;
    private String plural;
    private boolean isBrowserActive;
    private WebView webView;
    private boolean isShared;
    private boolean isDialog;
    private Runnable setToolbarTitle;

    List<Video> allVideoList = new ArrayList<>();
    CustomList<Video> filteredVideoList = new CustomList<>();

    private CustomDialog addOrEditDialog = null;
    private CustomDialog detailDialog;

    private CustomRecycler<Video> customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;
    private Utility.AdvancedQueryHelper previousAdvancedQueryHelper;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(isDialog = Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHOW_AS_DIALOG)))
            setTheme(R.style.AppTheme_NoTitle);

        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        String stringExtra = Settings.getSingleSetting(this, Settings.SETTING_SPACE_NAMES_ + Settings.Space.SPACE_VIDEO);
        if (stringExtra != null) {
            String[] singPlur = stringExtra.split("\\|");

            singular = singPlur[0];
            plural = singPlur[1];
            setTitle(plural);
        }
        showImages = Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_IMAGES);
        scrolling = Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SCROLL);
//        Database.destroy();

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_video);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

//        if (getIntent().getBooleanExtra(EXTRA_INTENT_TEST, true)) {
//            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
//            getIntent().putExtra(EXTRA_INTENT_TEST, false);
//        }

    }

    public static void showModeMenu(AppCompatActivity activity, View view) {
        if (!Database.isReady())
            return;

        int seenCount = (int) Database.getInstance().videoMap.values().stream().filter(video -> !video.getDateList().isEmpty()).count();
        int watchLaterCount = Utility.getWatchLaterList().size();
        int upcomingCount = (int) Database.getInstance().videoMap.values().stream().filter(Video::isUpcoming).count();
        CustomMenu.Builder(activity, view.findViewById(R.id.main_watchLaterCount_label))
                .setMenus((customMenu, items) -> {
                    items.add(new CustomMenu.MenuItem(String.format(Locale.getDefault(), "Bereits gesehen (%d)", seenCount), new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, SEEN_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_SEEN)));
                    items.add(new CustomMenu.MenuItem(String.format(Locale.getDefault(), "Später ansehen (%d)", watchLaterCount), new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, WATCH_LATER_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_WATCH_LATER)));
                    items.add(new CustomMenu.MenuItem(String.format(Locale.getDefault(), "Bevorstehende (%d)", upcomingCount), new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, UPCOMING_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_UPCOMING)));
                })
                .setOnClickListener((customRecycler, itemView, item, index) -> {
                    Pair<Intent, Integer> pair = (Pair<Intent, Integer>) item.getContent();
                    activity.startActivityForResult(pair.first, pair.second);
                })
                .dismissOnClick()
                .show();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            for (Video video : database.videoMap.values()) {
                if (video.getDateList().isEmpty() && !video.isUpcoming() && !Utility.getWatchLaterList().contains(video))
                    video.setWatchLater(true);
//                    database.watchLaterList.add(video.getUuid());
            }

            setContentView(R.layout.activity_video);
            allVideoList = new ArrayList<>(database.videoMap.values());
            sortList(allVideoList);
            filteredVideoList = new CustomList<>(allVideoList);

            videos_confirmDelete = findViewById(R.id.videos_confirmDelete);
            videos_confirmDelete.setOnClickListener(view -> {
                for (String uuidVideo : toDelete) {
                    filteredVideoList.remove(database.videoMap.get(uuidVideo));
                    allVideoList.remove(database.videoMap.get(uuidVideo));
                    database.videoMap.remove(uuidVideo);

                    database.collectionMap.values().forEach(collection -> collection.getFilmIdList().remove(uuidVideo));
                }
                delete = false;
                videos_confirmDelete.setVisibility(View.GONE);

                reLoadVideoRecycler();
                setResult(RESULT_OK);

                Database.saveAll();
                Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " " + singular : " " + plural) + " gelöscht", Toast.LENGTH_SHORT).show();
            });

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            videos_search = findViewById(R.id.search);
//            videos_search.setQuery("{}", false);

//            int searchTextViewId = videos_search.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//            TextView mSearchTextView = (TextView) videos_search.findViewById(searchTextViewId);
            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
//                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
//                    videos_search.setOnQueryTextListener(null);
//                    mSearchTextView.setText(new Helpers.SpannableStringHelper().appendColor(query, Color.RED).get());
//                    videos_search.setOnQueryTextListener(this);
                    searchQuery = query;
                    reLoadVideoRecycler();
                    return true;
                }
            };
            videos_search.setOnQueryTextListener(textListener);

            if (database.genreMap.isEmpty() && Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_ASK_FOR_GENRE_IMPORT)) {
                CustomDialog.Builder(this)
                        .setTitle("Genres Importieren")
                        .setText("Es wurde bisher noch kein Genre hinzugefügt. Sollen die Genres aus der TMDb importiert werden?\nDies kann auch jederzeit in den Einstellungen getan werden.")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton("Nicht erneut Fragen", customDialog -> {
                            Settings.changeSetting(Settings.SETTING_VIDEO_ASK_FOR_GENRE_IMPORT, "false");
                            Toast.makeText(this, "Du wirst nicht erneut gefragt", Toast.LENGTH_SHORT).show();
                        })
                        .alignPreviousButtonsLeft()
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog -> {
                            Utility.importTmdbGenre(this, true, true);
                            setResult(RESULT_OK);
                        })
                        .show();
                return;
            }


            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                addOrEditDialog = showEditOrNewDialog(null).first;

            if (getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.SEND")) {
                CharSequence text;
                text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
//                if (text == null) {
//                    text = getIntent().getClipData().getItemAt(0).getText();
//                }

                if (text != null) {
                    isShared = true;
                    String trim = text.toString().trim();
                    if (Utility.isUrl(trim)) {
                        String url = trim;
                        Runnable openEdit = () -> {
                            Video video = new Video("").setUrl(url);
                            Video[] editVideo = {null};

                            getDetailsFromUrl(url, video, editVideo);

                            Pair<CustomDialog, Video> pair = showEditOrNewDialog(video);
                            addOrEditDialog = pair.first;
                            editVideo[0] = pair.second;
                        };

                        Optional<Video> optional = database.videoMap.values().stream().filter(video -> Objects.equals(video.getUrl(), url)).findFirst();
                        if (!optional.isPresent()) {
                            openEdit.run();
                        } else {
                            Video video = optional.get();
                            CustomDialog.Builder(this)
                                    .setTitle("URL Bereits Vorhanden")
                                    .setText(new Helpers.SpannableStringHelper().append("Die geteilte URL ist bereits bei dem " + singular + " '").appendBold(video.getName()).append("' hinterlegt. Was möschtest du tun?").get())
                                    .addButton("Die Video Details öffnen", customDialog -> {
                                        isShared = false;
                                        detailDialog = showDetailDialog(video);
                                    })
                                    .addButton("Das Video bearbeiten", customDialog -> {
                                        isShared = false;
                                        addOrEditDialog = showEditOrNewDialog(video).first;
                                    })
                                    .addButton("Ein neues Video hinzufügen", customDialog -> openEdit.run())
                                    .enableTitleBackButton()
                                    .enableStackButtons()
                                    .show();
                        }

                    } else
                        addOrEditDialog = showEditOrNewDialog(new Video(text.toString())).first;
                }
            }

            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {
                if (!extraSearchCategory.equals(CategoriesActivity.CATEGORIES.VIDEO)) {
                    filterTypeSet.clear();

                    switch (extraSearchCategory) {
                        case DARSTELLER:
                            filterTypeSet.add(FILTER_TYPE.ACTOR);
                            break;
                        case GENRE:
                            filterTypeSet.add(FILTER_TYPE.GENRE);
                            break;
                        case STUDIOS:
                            filterTypeSet.add(FILTER_TYPE.STUDIO);
                            break;
                        case COLLECTION:
                            filterTypeSet.add(FILTER_TYPE.COLLECTION);
                            break;
                    }
                }

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (extraSearch.equals(SEEN_SEARCH))
                        mode = MODE.SEEN;
                    else if (extraSearch.equals(WATCH_LATER_SEARCH))
                        mode = MODE.LATER;
                    else if (extraSearch.equals(UPCOMING_SEARCH))
                        mode = MODE.UPCOMING;
                    else {
                        videos_search.setQuery(extraSearch, false);
                        if (extraSearch.matches("\\w*?_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}"))
                            allVideoList.stream().filter(video -> video.getUuid().equals(extraSearch)).findFirst().ifPresent(video1 -> detailDialog = showDetailDialog(video1));
                    }
                    textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                }
            }
            setSearchHint();

            if (isDialog) {
                findViewById(R.id.recycler).setVisibility(View.GONE);
                videos_search.setVisibility(View.GONE);
                findViewById(R.id.divider).setVisibility(View.GONE);
                findViewById(R.id.appBarLayout).setVisibility(View.GONE);
                if (getIntent().getBooleanExtra(MainActivity.EXTRA_SHOW_RANDOM, false))
                    showRandomDialog();
            }

            loadVideoRecycler();
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }

    private List<Video> filterList() {
        filteredVideoList = new CustomList<>(allVideoList);
//        if (true)
//            return filterdVideoList;
        if (mode.equals(MODE.SEEN)) {
            filteredVideoList = allVideoList.stream().filter(video -> !video.getDateList().isEmpty()).collect(Collectors.toCollection(CustomList::new));
        } else if (mode.equals(MODE.LATER)) {
            filteredVideoList = Utility.getWatchLaterList();
        } else if (mode.equals(MODE.UPCOMING)) {
            filteredVideoList = allVideoList.stream().filter(Video::isUpcoming).collect(Collectors.toCollection(CustomList::new));
        }
        if (!searchQuery.trim().equals("")) {

            Utility.AdvancedQueryHelper advancedQueryHelper = Utility.AdvancedQueryHelper.getAdvancedQuery(searchQuery);
            previousAdvancedQueryHelper = advancedQueryHelper;
            String subQuery = advancedQueryHelper.restQuery;


            if (advancedQueryHelper.hasAnyAdvancedQuery()) {
                Predicate<Video> ratingVideoCheck = null;
                Predicate<Video> dateVideoCheck = null;
                Predicate<Video> lengthVideoCheck = null;

                if (advancedQueryHelper.hasRatingSub()) {
                    Pair<Float, Float> ratingMinMax = advancedQueryHelper.getRatingMinMax();
                    ratingVideoCheck = video -> video.getRating() >= ratingMinMax.first && video.getRating() <= ratingMinMax.second;
                }

                if (advancedQueryHelper.hasDateOrDurationSub()) {
                    Pair<Date, Date> dateMinMax = advancedQueryHelper.getDateOrDurationMinMax();
                    Pair<Long, Long> dateMinMaxTime = Pair.create(dateMinMax.first.getTime(), dateMinMax.second.getTime());

                    dateVideoCheck = video -> (
                            video.getDateList().stream().anyMatch(date -> {
                                        long time = CustomUtility.removeTime(date).getTime();
                                        return time >= dateMinMaxTime.first && time <= dateMinMaxTime.second;
                                    }
                            )
                    );
                }

                if (advancedQueryHelper.hasLengthSub()) {
                    Pair<Integer, Integer> lengthMinMax = advancedQueryHelper.getLengthMinMax();
                    lengthVideoCheck = video -> video.getLength() >= lengthMinMax.first && video.getLength() <= lengthMinMax.second;
                }

                Predicate<Video> finalRatingVideoCheck = ratingVideoCheck;
                Predicate<Video> finalDateVideoCheck = dateVideoCheck;
                Predicate<Video> finalLengthVideoCheck = lengthVideoCheck;
                filteredVideoList.filter(video -> {
                    if (finalRatingVideoCheck != null && !finalRatingVideoCheck.test(video))
                        return false;
                    if (finalDateVideoCheck != null && !finalDateVideoCheck.test(video))
                        return false;
                    if (finalLengthVideoCheck != null && !finalLengthVideoCheck.test(video))
                        return false;
                    return true;
                }, true);
            }

            if (CustomUtility.stringExists(subQuery)) {
                if (subQuery.contains("|")) {
                    filteredVideoList = filteredVideoList.filterOr(subQuery.split("\\|"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
                } else {
                    filteredVideoList.filterAnd(subQuery.split("&"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
                }
            }

//            ((EditText) ((LinearLayout) ((LinearLayout) ((LinearLayout) videos_search.getChildAt(0)).getChildAt(2)).getChildAt(1)).getChildAt(0)).setText(new Helpers.SpannableStringHelper().appendColor(searchQuery, Color.RED).get());
        }
        return filteredVideoList;
    }

    private List<Video> sortList(List<Video> videoList) {
        switch (sort_type) {
            case NAME:
                videoList.sort((video1, video2) -> video1.getName().compareTo(video2.getName()));
                if (reverse)
                    Collections.reverse(videoList);
                break;
            case VIEWS:
                videoList.sort((video1, video2) -> {
                    if (video1.getDateList().size() == video2.getDateList().size())
                        return video1.getName().compareTo(video2.getName());
                    else
                        return Integer.compare(video1.getDateList().size(), video2.getDateList().size()) * (reverse ? 1 : -1);
                });
                break;
            case RATING:
                videoList.sort((video1, video2) -> {
                    if (video1.getRating().equals(video2.getRating()))
                        return video1.getName().compareTo(video2.getName());
                    else
                        return video1.getRating().compareTo(video2.getRating()) * (reverse ? 1 : -1);
                });
                break;
            case LATEST:
                final Pair<Date, Date>[] datePair = new Pair[]{null};
                final Pair<Long, Long>[] datePairTime = new Pair[]{null};

                if (CustomUtility.stringExists(searchQuery) && previousAdvancedQueryHelper != null && previousAdvancedQueryHelper.hasDateOrDurationSub()) {
                    if ((datePair[0] = previousAdvancedQueryHelper.datePair) == null)
                        datePair[0] = previousAdvancedQueryHelper.getDateOrDurationMinMax();

                    datePairTime[0] = Pair.create(datePair[0].first.getTime(), datePair[0].second.getTime());
                }
                videoList.sort((video1, video2) -> {
                    List<Date> dateList1 = video1.getDateList();
                    List<Date> dateList2 = video2.getDateList();
                    if (dateList1.isEmpty() && dateList2.isEmpty()) {
                        if (video1.isUpcoming() && video2.isUpcoming())
                            return video1.getRelease().compareTo(video2.getRelease());
                        else if (!video1.isUpcoming() && !video2.isUpcoming())
                            return video1.getName().compareTo(video2.getName());
                        else if (video1.isUpcoming())
                            return reverse ? -1 : 1;
                        else if (video2.isUpcoming())
                            return reverse ? 1 : -1;
                    } else if (dateList1.isEmpty())
                        return reverse ? -1 : 1;
                    else if (dateList2.isEmpty())
                        return reverse ? 1 : -1;

                    if (datePairTime[0] != null) {
                        dateList1 = dateList1.parallelStream().filter(date -> {
                            long time = CustomUtility.removeTime(date).getTime();
                            return time >= datePairTime[0].first && time <= datePairTime[0].second;
                        }).collect(Collectors.toList());
                        dateList2 = dateList2.parallelStream().filter(date -> {
                            long time = CustomUtility.removeTime(date).getTime();
                            return time >= datePairTime[0].first && time <= datePairTime[0].second;
                        }).collect(Collectors.toList());
                    }

                    Date new1 = Collections.max(dateList1);
                    Date new2 = Collections.max(dateList2);
                    if (new1.equals(new2))
                        return video1.getName().compareTo(video2.getName());
                    else
                        return new1.compareTo(new2) * (reverse ? 1 : -1);
                });
                break;

        }
        return videoList;
    }

    private void loadVideoRecycler() {
        customRecycler_VideoList = new CustomRecycler<Video>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_video)
                .setGetActiveObjectList(() -> {
                    List<Video> filteredList = sortList(filterList());
                    TextView noItem = findViewById(R.id.no_item);
                    String text = videos_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText);
                    if (size > 0) {
                        builder.append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                        final int[] watchedMinutes = {0};
                        final int[] viewSum = {0};
                        final double[] ratingSum = {0};
                        final double[] ratingCount = {0};
                        if (CustomUtility.stringExists(searchQuery) && previousAdvancedQueryHelper != null && previousAdvancedQueryHelper.hasDateOrDurationSub()) {
                            final Pair<Date, Date>[] datePair = new Pair[]{null};
                            final Pair<Long, Long>[] datePairTime = new Pair[]{null};

                            if ((datePair[0] = previousAdvancedQueryHelper.datePair) == null)
                                datePair[0] = previousAdvancedQueryHelper.getDateOrDurationMinMax();

                            datePairTime[0] = Pair.create(datePair[0].first.getTime(), datePair[0].second.getTime());

                            filteredList.forEach(video -> {
                                int videoViews = (int) video.getDateList().stream().filter(date -> {
                                    long time = CustomUtility.removeTime(date).getTime();
                                    return time >= datePairTime[0].first && time <= datePairTime[0].second;
                                }).count();
                                viewSum[0] += videoViews;
                                watchedMinutes[0] += video.getLength() * videoViews;

                                Float rating = video.getRating();
                                if (rating > 0) {
                                    ratingSum[0] += rating;
                                    ratingCount[0]++;
                                }
                            });
                        } else {
                            filteredList.forEach(video -> {
                                int videoViews = video.getDateList().size();
                                viewSum[0] += videoViews;
                                watchedMinutes[0] += video.getLength() * videoViews;

                                Float rating = video.getRating();
                                if (rating > 0) {
                                    ratingSum[0] += rating;
                                    ratingCount[0]++;
                                }
                            });
                        }

                        String timeString = Utility.formatDuration(Duration.ofMinutes(watchedMinutes[0]), null);
                        if (Utility.stringExists(timeString))
                            builder.append(timeString).append("\n");

                        String viewSumText = viewSum[0] > 1 ? viewSum[0] + " Ansichten" : (viewSum[0] == 1 ? "Eine" : "Keine") + " Ansicht";
                        builder.append(viewSumText);

                        if (ratingCount[0] > 0)
                            builder.append("  |  ").append(String.format(Locale.getDefault(), "Ø %.2f ☆", ratingSum[0] / ratingCount[0]).replaceAll("([.,]?0*)(?= ☆)", ""));
                    }
                    elementCount.setText(builder);
                    return filteredList;

                })
                .setSetItemContent((customRecycler, itemView, video) -> {
                    itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE : View.GONE);

                    MinDimensionLayout listItem_video_image_layout = itemView.findViewById(R.id.listItem_video_image_layout);
                    if (showImages && CustomUtility.stringExists(video.getImagePath())) {
                        listItem_video_image_layout.setVisibility(View.VISIBLE);
                        ImageView image = itemView.findViewById(R.id.listItem_video_image);
                        Utility.loadUrlIntoImageView(this, image, Utility.getTmdbImagePath_ifNecessary(video.getImagePath(), false), Utility.getTmdbImagePath_ifNecessary(video.getImagePath(), true), null, () -> Utility.roundImageView(image, 4), this::removeFocusFromSearch);
                    } else
                        listItem_video_image_layout.setVisibility(View.GONE);


                    String searchQuery = Utility.AdvancedQueryHelper.removeAdvancedSearch(this.searchQuery).toLowerCase();
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(Helpers.SpannableStringHelper.highlightText(searchQuery, video.getName()));
                    if (!video.getDateList().isEmpty()) {
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));
                    } else
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);

                    itemView.findViewById(R.id.listItem_video_later).setVisibility(Utility.getWatchLaterList().contains(video) ? View.VISIBLE : View.GONE);
                    itemView.findViewById(R.id.listItem_video_upcoming).setVisibility(video.isUpcoming() ? View.VISIBLE : View.GONE);

                    Comparator<String> containsComparator = (name1, name2) -> {
                        boolean contains1 = name1.toLowerCase().contains(searchQuery);
                        boolean contains2 = name2.toLowerCase().contains(searchQuery);

                        return Boolean.compare(contains1, contains2) * -1;
                    };

                    String darstellerNames = video.getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).sorted(containsComparator).collect(Collectors.joining(", "));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(Helpers.SpannableStringHelper.highlightText(searchQuery, darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);

                    String studioNames = video.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).sorted(containsComparator).collect(Collectors.joining(", "));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Studio)).setText(Helpers.SpannableStringHelper.highlightText(searchQuery, studioNames));
                    itemView.findViewById(R.id.listItem_video_Studio).setSelected(scrolling);

                    String genreNames = video.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).sorted(containsComparator).collect(Collectors.joining(", "));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Genre)).setText(Helpers.SpannableStringHelper.highlightText(searchQuery, genreNames));
                    itemView.findViewById(R.id.listItem_video_Genre).setSelected(scrolling);

                    int clickMode = Settings.getSingleSetting_int(this, Settings.SETTING_VIDEO_CLICK_MODE);
                    ImageView listItem_video_internetOrDetails = itemView.findViewById(R.id.listItem_video_internetOrDetails);
                    if (clickMode == 0)
                        listItem_video_internetOrDetails.setImageResource(R.drawable.ic_internet);
                    else if (clickMode == 1)
                        listItem_video_internetOrDetails.setImageResource(R.drawable.ic_info);

                })
                .setOnClickListener((customRecycler, view, object, index) -> {
                    if (!delete) {
                        int clickMode = Settings.getSingleSetting_int(this, Settings.SETTING_VIDEO_CLICK_MODE);
                        if (clickMode == 0)
                            detailDialog = showDetailDialog(object);
                        else if (clickMode == 1)
                            openUrl(object.getUrl(), false);
                    } else {
                        CheckBox checkBox = view.findViewById(R.id.listItem_video_deleteCheck);
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked())
                            toDelete.add(object.getUuid());
                        else
                            toDelete.remove(object.getUuid());
                    }
                })
                .addSubOnClickListener(R.id.listItem_video_internetOrDetails, (customRecycler, view, object, index) -> {
                    int clickMode = Settings.getSingleSetting_int(this, Settings.SETTING_VIDEO_CLICK_MODE);
                    if (clickMode == 0)
                        openUrl(object.getUrl(), false);
                    else if (clickMode == 1)
                        detailDialog = showDetailDialog(object);
                })
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog = showEditOrNewDialog(object).first;
                })
                .hideDivider()
                .generate();
    }

    private void reLoadVideoRecycler() {
//        customRecycler_VideoList.reload();
        if (customRecycler_VideoList != null)
            customRecycler_VideoList.reload();
    }

    private CustomDialog showDetailDialog(@NonNull Video video) {
        removeFocusFromSearch();
        final int[] views = {video.getDateList().size()};
//        int openWithButtonId = View.generateViewId();
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(video.getName())
                .setView(R.layout.dialog_detail_video)
                .addOptionalModifications(customDialog -> {
                    if (Utility.boolOr(Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_VIDEO_SHOW_SEARCH)), 0, 1))
                        customDialog
                                .addButton(R.drawable.ic_search, customDialog1 -> showSearchDialog(video, null), false)
                                .alignPreviousButtonsLeft();
                })
                .addButton("Bearbeiten", customDialog ->
                        addOrEditDialog = showEditOrNewDialog(video).first, false)
//                .addButton("Öffnen mit...", customDialog -> openUrl(video.getUrl(), true), openWithButtonId, false)
                .setSetViewContent((customDialog, view, reload) -> {
                    if (reload && views[0] != video.getDateList().size()) {
                        if (views[0] < video.getDateList().size() && Utility.getWatchLaterList().contains(video)) {
                            video.setWatchLater(false);
                            Database.saveAll();
                            setResult(RESULT_OK);
                            textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                        }
                        views[0] = video.getDateList().size();
                    }

                    if (reload)
                        customDialog.setTitle(video.getName());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.DARSTELLER, view.findViewById(R.id.dialog_video_Darsteller), video.getDarstellerList(), database.darstellerMap);
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.STUDIOS, view.findViewById(R.id.dialog_video_Studio), video.getStudioList(), database.studioMap);
                    view.findViewById(R.id.dialog_video_Studio).setSelected(true);
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.GENRE, view.findViewById(R.id.dialog_video_Genre), video.getGenreList(), database.genreMap);
                    view.findViewById(R.id.dialog_video_Genre).setSelected(true);

                    SpannableStringBuilder builder = new SpannableStringBuilder();

                    String collectionNames;
                    database.collectionMap.values().stream().filter(collection -> collection.getFilmIdList().contains(video.getUuid())).map(com.finn.androidUtilities.ParentClass::getName)
                            .forEach(s -> {
                                if (builder.length() != 0)
                                    builder.append(", ");
                                builder.append(s, new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Toast.makeText(VideoActivity.this, s, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(VideoActivity.this, CollectionActivity.class).putExtra(CategoriesActivity.EXTRA_SEARCH, s));
                                    }

                                    @Override
                                    public void updateDrawState(TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setUnderlineText(false);
                                    }
                                }, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            });
//                            .collect(Collectors.joining(", "));
                    if (builder.length() > 0/*Utility.stringExists(collectionNames)*/) {
                        TextView dialog_video_collection = (TextView) view.findViewById(R.id.dialog_video_collection);
                        dialog_video_collection.setText(builder);
                        dialog_video_collection.setMovementMethod(LinkMovementMethod.getInstance());
                        dialog_video_collection.setHighlightColor(Color.TRANSPARENT);
                        dialog_video_collection.setLinkTextColor(dialog_video_collection.getTextColors());
                        view.findViewById(R.id.dialog_video_collection_layout).setVisibility(View.VISIBLE); // ToDo: Link to collection
                    }
                    view.findViewById(R.id.dialog_video_details).setVisibility(View.VISIBLE);

                    TextView urlTextView = view.findViewById(R.id.dialog_video_Url);
                    urlTextView.setText(video.getUrl());
                    if (CustomUtility.stringExists(video.getUrl())) {
                        urlTextView.setClickable(true);
                        urlTextView.setOnClickListener(v -> openUrl(video.getUrl(), true));
                    }
                    else
                        urlTextView.setClickable(true);

                    Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                    SpannableStringBuilder viewsText = helper.quickItalic("Keine Ansichten");
                    if (views[0] > 0) {
                        Date lastWatched = CustomList.cast(video.getDateList()).getBiggest();
                        viewsText = helper.append(String.valueOf(views[0])).append(
                                String.format(Locale.getDefault(), "   (%s – %dd)",
                                        new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(lastWatched),
                                        Days.daysBetween(new LocalDate(lastWatched), new LocalDate(new Date())).getDays())
                                , Helpers.SpannableStringHelper.SPAN_TYPE.ITALIC).get();
                    }
                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(viewsText);

                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_RELEASE))
                        ((TextView) view.findViewById(R.id.dialog_video_release)).setText(video.getRelease() != null ? new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(video.getRelease()) : "");
                    else
                        view.findViewById(R.id.dialog_video_release_layout).setVisibility(View.GONE);

                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_AGE_RATING)) {
                        if (video.getAgeRating() != -1)
                            ((TextView) view.findViewById(R.id.dialog_video_ageRating)).setText(String.valueOf(video.getAgeRating()));
                    } else
                        view.findViewById(R.id.dialog_video_ageRating_layout).setVisibility(View.GONE);

                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_LENGTH)) {
                        int length = video.getLength();
                        int intHours = (int) (length / 60d);
                        int intMin = length - intHours * 60;
                        ((TextView) view.findViewById(R.id.dialog_video_length)).setText(length > 0 ? (intHours > 0 ? intHours + "h " : "") + (intMin > 0 ? intMin + "m" : "") : "");
                    } else
                        view.findViewById(R.id.dialog_video_length_layout).setVisibility(View.GONE);

                    ((RatingBar) view.findViewById(R.id.dialog_video_rating)).setRating(video.getRating());

                    String imagePath = video.getImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        ImageView dialog_video_poster = view.findViewById(R.id.dialog_video_poster);
                        dialog_video_poster.setVisibility(View.VISIBLE);
                        Utility.loadUrlIntoImageView(this, dialog_video_poster, Utility.getTmdbImagePath_ifNecessary(imagePath, false), Utility.getTmdbImagePath_ifNecessary(imagePath, true), null, () -> Utility.roundImageView(dialog_video_poster, 4));
                    }


                    final boolean[] isInWatchLater = {Utility.getWatchLaterList().contains(video)};
//                    view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
                    view.findViewById(R.id.dialog_video_watchLater_background).setBackground(isInWatchLater[0] ? CustomUtility.drawableBuilder_rectangle(0x96868686, 7, false) : null);
                    view.findViewById(R.id.dialog_video_watchLater).setOnClickListener(view1 -> {
                        CustomUtility.isOnline(this, () -> {
                            isInWatchLater[0] = !isInWatchLater[0];
                            view.findViewById(R.id.dialog_video_watchLater_background).setBackground(isInWatchLater[0] ? CustomUtility.drawableBuilder_rectangle(0x96868686, 7, false) : null);
                            if (isInWatchLater[0]) {
                                video.setWatchLater(true);
                                Toast.makeText(this, "Zu 'Später-Ansehen' hinzugefügt", Toast.LENGTH_SHORT).show();
                            } else {
                                video.setWatchLater(false);
                                Toast.makeText(this, "Aus 'Später-Ansehen' entfernt", Toast.LENGTH_SHORT).show();
                            }
                            textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                            setResult(RESULT_OK);
                            Database.saveAll();
                        });
                    });

                    view.findViewById(R.id.dialog_video_editViews).setOnClickListener(view1 -> showCalenderDialog(Arrays.asList(video), detailDialog));
                    view.findViewById(R.id.dialog_video_editViews).setOnLongClickListener(view1 -> {
                        boolean before = video.addDate(new Date(), true);
                        Utility.showCenteredToast(this, "Ansicht Hinzugefügt" + (before ? "\n(Gestern)" : ""));
                        Database.saveAll();
                        setResult(RESULT_OK);
                        customDialog.reloadView();
                        reLoadVideoRecycler();
                        return true;
                    });
                    view.findViewById(R.id.dialog_video_editViews).setOnContextClickListener(view1 -> {
                        Toast.makeText(this, "Context Click", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    ImageView dialog_video_internet = view.findViewById(R.id.dialog_video_internet);
                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_WEB_SHORTCUT))
                        dialog_video_internet.setOnClickListener(v -> {
                            CustomDialog.Builder(this)
                                    .setTitle("Öffnen mit...")
                                    .addButton("TMDb", customDialog1 -> Utility.openUrl(this, "https://www.themoviedb.org/movie/" + video.getTmdbId(), true))
                                    .addButton("WerStreamt.es", customDialog1 -> Utility.doWerStreamtEsRequest(this, video.getName()))
                                    .addButton("IMDB", customDialog1 -> Utility.openUrl(this, "https://www.imdb.com/title/" + video.getImdbId(), true))
                                    .disableButtonAllCaps()
                                    .enableExpandButtons()
                                    .show();
                        });
                    else
                        dialog_video_internet.setVisibility(View.GONE);

//                    customDialog.getButton(openWithButtonId).setEnabled(Utility.stringExists(video.getUrl()));
                })
                .setOnDialogDismiss(customDialog -> {
                    detailDialog = null;
                    Utility.ifNotNull(customDialog.getPayload(), o -> ((CustomDialog) o).reloadView());
                });
        returnDialog.show();
        detailDialog = returnDialog; // da sowieso immer gespeichert
        return returnDialog;
    }

    public void showCalenderDialog(List<Video> videoList, CustomDialog detailDialog) {
        CustomDialog.Builder(this)
                .setTitle("Ansichten Bearbeiten")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view, reload) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupFilmCalender(this, calendarView, ((FrameLayout) view), videoList, false);
                })
                .disableScroll()
                .setDimensions(true, true)
                .setOnDialogDismiss(customDialog -> {
                    detailDialog.reloadView();
                    this.reLoadVideoRecycler();
                })
                .enableTitleBackButton()
                .show();

    }

    private void showSearchDialog(Video video, @Nullable CustomDialog editDialog) {
        CustomDialog.Builder(this)
                .setTitle("Nach " + singular + " Suchen")
                .setView(R.layout.dialog_search_video)
                .setSetViewContent((customDialog, view, reload) -> {
                    String name;
                    if (editDialog != null)
                        name = ((EditText) editDialog.findViewById(R.id.dialog_editOrAddVideo_Title)).getText().toString().trim();
                    else
                        name = video.getName();
                    String actors = video.getDarstellerList().stream().map(s -> database.darstellerMap.get(s).getName()).collect(Collectors.joining(", "));
                    String studios = video.getStudioList().stream().map(s -> database.studioMap.get(s).getName()).collect(Collectors.joining(", "));
                    String genre = video.getGenreList().stream().map(s -> database.genreMap.get(s).getName()).collect(Collectors.joining(", "));

                    EditText editTitle = customDialog.findViewById(R.id.dialog_searchVideo_title);
                    CheckBox editTitle_check = customDialog.findViewById(R.id.dialog_searchVideo_title_check);
                    EditText editActor = customDialog.findViewById(R.id.dialog_searchVideo_actor);
                    CheckBox editActor_check = customDialog.findViewById(R.id.dialog_searchVideo_actor_check);
                    EditText editStudio = customDialog.findViewById(R.id.dialog_searchVideo_studio);
                    CheckBox editStudio_check = customDialog.findViewById(R.id.dialog_searchVideo_studio_check);
                    EditText editGenre = customDialog.findViewById(R.id.dialog_searchVideo_genre);
                    CheckBox editGenre_check = customDialog.findViewById(R.id.dialog_searchVideo_genre_check);

                    editTitle.setText(name);
                    editTitle_check.setChecked(Utility.stringExists(name));
                    editActor.setText(actors);
//                    editActor_check.setChecked(Utility.stringExists(actors));
                    editStudio.setText(studios);
//                    editStudio_check.setChecked(Utility.stringExists(studios));
                    editGenre.setText(genre);
//                    editGenre_check.setChecked(Utility.stringExists(genre));
                })
                .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                .addButton("Suchen", customDialog -> {
                    CustomList<String> searchList = new CustomList<>();

                    EditText editTitle = customDialog.findViewById(R.id.dialog_searchVideo_title);
                    CheckBox editTitle_check = customDialog.findViewById(R.id.dialog_searchVideo_title_check);
                    EditText editActor = customDialog.findViewById(R.id.dialog_searchVideo_actor);
                    CheckBox editActor_check = customDialog.findViewById(R.id.dialog_searchVideo_actor_check);
                    EditText editStudio = customDialog.findViewById(R.id.dialog_searchVideo_studio);
                    CheckBox editStudio_check = customDialog.findViewById(R.id.dialog_searchVideo_studio_check);
                    EditText editGenre = customDialog.findViewById(R.id.dialog_searchVideo_genre);
                    CheckBox editGenre_check = customDialog.findViewById(R.id.dialog_searchVideo_genre_check);

                    if (editTitle_check.isChecked()) {
                        String title = editTitle.getText().toString();
                        if (Utility.stringExists(title))
                            searchList.add(title);
                    }
                    if (editActor_check.isChecked()) {
                        String actor = editActor.getText().toString();
                        if (Utility.stringExists(actor))
                            searchList.add(actor);
                    }
                    if (editStudio_check.isChecked()) {
                        String studio = editStudio.getText().toString();
                        if (Utility.stringExists(studio))
                            searchList.add(studio);
                    }
                    if (editGenre_check.isChecked()) {
                        String genre = editGenre.getText().toString();
                        if (Utility.stringExists(genre))
                            searchList.add(genre);
                    }


                    if (searchList.isEmpty()) {
                        Toast.makeText(this, "Nix zum Suchen", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String query = String.join(", ", searchList);
                    try {
                        query = URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }
                    String url = "https://www.google.de/search?q=" + query;

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    Intent chooser = Intent.createChooser(intent, "Suchen mit...");
                    if (chooser.resolveActivity(getPackageManager()) != null)
                        startActivity(chooser);
                    else
                        Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();

                }, false)
                .colorLastAddedButton()
                .show();
    }

    //  ------------------------- EditVideo ------------------------->
    private Pair<CustomDialog, Video> showEditOrNewDialog(Video video) {
        if (!Utility.isOnline(this))
            return Pair.create(null, null);
        setResult(RESULT_OK);
        removeFocusFromSearch();


        final Video[] editVideo = {video == null ? null : video.clone()};
        if (editVideo[0] != null) {
            editVideo[0].setDarstellerList(new ArrayList<>(editVideo[0].getDarstellerList()));
            editVideo[0].setStudioList(new ArrayList<>(editVideo[0].getStudioList()));
            editVideo[0].setGenreList(new ArrayList<>(editVideo[0].getGenreList()));
        }

        com.finn.androidUtilities.Helpers.TextInputHelper helper = new com.finn.androidUtilities.Helpers.TextInputHelper();
        final boolean[] checked = {video != null && video.isWatchLater()}; // Utility.getWatchLaterList().contains(video)};
        CustomDialog.OnClick onClickSave = customDialog -> {
            String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_Title)).getText().toString().trim();
            String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_url)).getText().toString().trim();
            if (editVideo[0].isWatchLater() && !editVideo[0].hasRating() && !Utility.boolOr(((RatingBar) customDialog.findViewById(R.id.customRating_ratingBar)).getRating(), -1f, 0f))
                CustomDialog.Builder(this)
                        .setTitle("Ansicht Hinzufügen?")
                        .setText("Soll eine neue Ansicht zu dem Video hinzugefügt werden?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton(CustomDialog.BUTTON_TYPE.NO_BUTTON, customDialog1 -> saveVideo(customDialog, video, titel, url, checked[0], editVideo[0]))
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                            boolean before = editVideo[0].addDate(new Date(), true);
                            editVideo[0].setWatchLater(false);
                            checked[0] = false;
                            Utility.showCenteredToast(this, "Ansicht Hinzugefügt" + (before ? "\n(Gestern)" : ""));
                            saveVideo(customDialog, video, titel, url, checked[0], editVideo[0]);
                        })
                        .enableColoredActionButtons()
                        .show();
            else
                saveVideo(customDialog, video, titel, url, checked[0], editVideo[0]);

        };
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(video == null ? "Neu: " + singular : singular + " Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .addOptionalModifications(customDialog -> {
                    if (Utility.boolOr(Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_VIDEO_SHOW_SEARCH)), 0, 2))
                        customDialog
                                .addButton(R.drawable.ic_search, customDialog1 -> showSearchDialog(editVideo[0], customDialog1), false)
                                .alignPreviousButtonsLeft();
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, onClickSave, false)
                .addOnLongClickToLastAddedButton(onClickSave)
                .disableLastAddedButton()
                .setSetViewContent((editDialog, view, reload) -> {
                    final CustomDialog[] internetDialog = {null};
                    TextInputLayout dialog_editOrAddVideo_Title_layout = view.findViewById(R.id.dialog_editOrAddVideo_Title_layout);
                    TextInputLayout dialog_editOrAddVideo_Url_layout = view.findViewById(R.id.dialog_editOrAddVideo_url_layout);


                    ImageView thumbnailButton = view.findViewById(R.id.dialog_editOrAddVideo_thumbnail_button);
                    thumbnailButton.setOnClickListener(v -> {
                        int showButtonId = View.generateViewId();
//                        ImageView imageView = new ImageView(this);
//                        Utility.setDimensions(imageView, true, false);
//                        imageView.setAdjustViewBounds(true);
//                        imageView.setPadding(Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16));
                        CustomList<String> resultList = new CustomList<>();
                        String[] currentResult = {null};

                        CustomDialog.Builder(this)
                                .setTitle("Thumbnail-URL Bearbeiten")
                                .addButton("Testen", CustomDialog::reloadView, showButtonId, false)
                                .alignPreviousButtonsLeft()
                                .enableDynamicWrapHeight(this)
                                .enableAutoUpdateDynamicWrapHeight()
                                .setView(R.layout.dialog_edit_thumbnail)
                                .setSetViewContent((customDialog1, view1, reload1) -> {
                                    TextInputLayout inputLayout = view1.findViewById(R.id.dialog_editThumbnail_url_layout);
                                    EditText editText = inputLayout.getEditText();
                                    Utility.removeTextListeners(editText);

                                    LinearLayout selectLayout = view1.findViewById(R.id.dialog_editThumbnail_select_layout);
                                    selectLayout.setVisibility(resultList.size() < 2 ? View.GONE : View.VISIBLE);
                                    ImageView selectPrevious = view1.findViewById(R.id.dialog_editThumbnail_select_previous);
                                    ImageView selectNext = view1.findViewById(R.id.dialog_editThumbnail_select_next);
                                    selectPrevious.setOnClickListener(v1 -> {
                                        currentResult[0] = resultList.previous(currentResult[0]);
                                        customDialog1.reloadView();
                                    });
                                    selectNext.setOnClickListener(v1 -> {
                                        currentResult[0] = resultList.next(currentResult[0]);
                                        customDialog1.reloadView();
                                    });

                                    if (CustomUtility.stringExists(currentResult[0]))
                                        editText.setText(currentResult[0]);

                                    editText.addTextChangedListener(new TextWatcher() {
                                        String previous;

                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                            previous = s.toString();
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            String now = s.toString();
                                            if (!previous.contains(".google.") && now.contains(".google.") && CustomUtility.isUrl(now)) {
                                                new Helpers.WebViewHelper(VideoActivity.this, now)
//                                                        .setDebug(true)
                                                        .enableLoadImages()
                                                        .setMobileVersion(true)
                                                        .setUserAgent("Mozilla/5.0 (Linux; Android 11; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.181 Mobile Safari/537.36")
                                                        .addRequest("{\n" +
                                                                "    let images = document.getElementsByClassName(\"n3VNCb\")\n" +
                                                                "    let results = []\n" +
                                                                "    for (let image of images) {\n" +
                                                                "        let result = image.getAttribute(\"src\")\n" +
                                                                "        if (result && !result.includes(\"data\"))\n" +
                                                                "            results.push(result);\n" +
                                                                "    };\n" +
                                                                "    if (results.length)\n" +
                                                                "        return results\n" +
                                                                "    else\n" +
                                                                "        return undefined\n" +
                                                                "}", result -> {
                                                            resultList.clear();
                                                            if (!result.startsWith("null"))
                                                                resultList.addAll(new Gson().fromJson(result, CustomList.class));
                                                            if (resultList.isEmpty()) {
                                                                Utility.openUrl(VideoActivity.this, now, true);
                                                            } else {
                                                                editText.setText(currentResult[0] = resultList.get(0));
                                                                customDialog1.getButton(showButtonId).click();
                                                            }
                                                        })
                                                        .go();
                                            }
                                        }
                                    });

                                    String regex = String.format("(%s)|(%s)|", CategoriesActivity.pictureRegexAll, ActivityResultHelper.uriRegex);
                                    new Helpers.TextInputHelper().addValidator(inputLayout).setValidation(inputLayout, (validator, text) -> {
                                        validator.asWhiteList();
                                        if (text.contains("http") && !text.contains("https"))
                                            validator.setInvalid("Die URL muss 'https' sein");
                                        else if (text.matches(regex))
                                            validator.setValid();
                                    }).defaultDialogValidation(customDialog1).allowEmpty();
                                    if (!reload1)
                                        editText.setText(Utility.stringExistsOrElse(editVideo[0].getImagePath(), ""));

                                    view1.findViewById(R.id.dialog_editThumbnail_localStorage).setOnClickListener(v1 -> {
                                        ActivityResultHelper.addFileChooserRequest(this, "image/*", o -> {
                                            editText.setText(((Intent) o).getData().toString());
                                        });
                                    });

                                    ImageView imageView = view1.findViewById(R.id.dialog_editThumbnail_image);
                                    String url = editText.getText().toString();

                                    if (!customDialog1.getActionButton().getButton().isEnabled()) {
                                        if (!url.isEmpty())
                                            Toast.makeText(this, "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
                                        imageView.setVisibility(View.GONE);
                                        return;
                                    }

                                    if (Utility.stringExists(url)) {
                                        imageView.setVisibility(View.VISIBLE);
                                        Utility.setMargins(imageView, 16, resultList.size() < 2 ? 16 : 8, 16, 16);
                                        Utility.loadUrlIntoImageView(this, imageView, Utility.getTmdbImagePath_ifNecessary(url, true), null);
                                    } else
                                        imageView.setVisibility(View.GONE);
                                })
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                    String url = ((TextInputLayout) customDialog1.findViewById(R.id.dialog_editThumbnail_url_layout)).getEditText().getText().toString();
                                    if (url.isEmpty())
                                        url = null;

                                    editVideo[0].setImagePath(url);
                                    setThumbnailButton(editVideo[0], addOrEditDialog);
                                })
                                .show();
                    });
                    setThumbnailButton(editVideo[0], editDialog);
                    view.findViewById(R.id.dialog_editOrAddVideo_internet).setOnClickListener(v -> {
                        String url = dialog_editOrAddVideo_Url_layout.getEditText().getText().toString();
                        if (internetDialog[0] == null) {
                            internetDialog[0] = CustomDialog.Builder(this)
                                    .setView(R.layout.dialog_video_internet)
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        webView = view1.findViewById(R.id.dialog_videoInternet_webView);
                                        webView.loadUrl(!url.isEmpty() ? url : "https://www.google.de/");
                                        webView.setWebViewClient(new WebViewClient());
                                        WebSettings webSettings = webView.getSettings();
                                        webSettings.setJavaScriptEnabled(true);
                                        webSettings.setUserAgentString(Helpers.WebViewHelper.USER_AGENT);
                                        webSettings.setUseWideViewPort(true);
                                        webSettings.setLoadWithOverviewMode(true);

                                        webSettings.setSupportZoom(true);
                                        webSettings.setBuiltInZoomControls(true);
                                        webSettings.setDisplayZoomControls(false);

                                        FloatingActionButton dialog_videoInternet_getThumbnails = view1.findViewById(R.id.dialog_videoInternet_getThumbnails);
                                        dialog_videoInternet_getThumbnails.setOnClickListener(v1 -> {
                                            showSelectThumbnailDialog(url, editVideo[0], editDialog, customDialog1);
                                        });
//                                        view1.findViewById(R.id.dialog_videoInternet_toggleJavaScript).setOnClickListener(v1 -> {
//                                            isJsEnabled[0] = !isJsEnabled[0];
//                                            webSettings.setJavaScriptEnabled(isJsEnabled[0]);
//                                            if(isJsEnabled[0])
//                                                webView.reload();
////                                            ((FloatingActionButton) v1).setbati
//                                        });
                                        Runnable getSelection = () -> {
//                                            webSettings.setJavaScriptEnabled(true);
                                            webView.evaluateJavascript("(function(){return window.getSelection().toString()})()", value -> {
                                                value = Utility.subString(value, 1, -1);
                                                if (!value.isEmpty()) {
                                                    Toast.makeText(VideoActivity.this, value, Toast.LENGTH_SHORT).show();
                                                    dialog_editOrAddVideo_Title_layout.getEditText().setText(value);
                                                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_AUTO_SEARCH))
                                                        dialog_editOrAddVideo_Title_layout.getEditText().onEditorAction(Helpers.TextInputHelper.IME_ACTION.SEARCH.getCode());
                                                    customDialog1.dismiss();
                                                    parseTitleToDetails(video, editVideo, value);
                                                }
                                            });
//                                            webSettings.setJavaScriptEnabled(false);
                                        };

                                        View.OnContextClickListener listener = v1 -> {
                                            getSelection.run();
                                            customDialog1.dismiss();
                                            return true;
                                        };
                                        webView.setOnContextClickListener(listener);

                                        ImageView dialog_videoInternet_goButton = customDialog1.findViewById(R.id.dialog_videoInternet_goButton);
                                        FloatingActionButton dialog_videoInternet_getSelection = view1.findViewById(R.id.dialog_videoInternet_getSelection);
                                        dialog_videoInternet_getSelection.setOnContextClickListener(listener);
                                        dialog_videoInternet_getSelection.setOnClickListener(v1 -> getSelection.run());

                                        TextInputLayout dialog_videoInternet_url_layout = customDialog1.findViewById(R.id.dialog_videoInternet_url_layout);
                                        TextInputEditText dialog_videoInternet_url = (TextInputEditText) dialog_videoInternet_url_layout.getEditText();
                                        dialog_videoInternet_url.setOnEditorActionListener((v12, actionId, event) -> {
                                            if (actionId == EditorInfo.IME_ACTION_GO) {
                                                dialog_videoInternet_goButton.callOnClick();
                                                CustomUtility.changeWindowKeyboard(this, dialog_videoInternet_url, false);
                                                dialog_videoInternet_url.requestFocus();
                                                return true;
                                            }
                                            return false;
                                        });
                                        dialog_videoInternet_url.setText(webView.getUrl());

                                        dialog_videoInternet_goButton.setOnClickListener(v1 -> {
                                            String urlOrSearch = dialog_videoInternet_url.getText().toString();
                                            if (CustomUtility.stringExists(urlOrSearch)) {
                                                if (CustomUtility.isUrl(urlOrSearch)) {
                                                    webView.loadUrl(urlOrSearch);
                                                } else {
                                                    try {
                                                        urlOrSearch = URLEncoder.encode(urlOrSearch, "UTF-8");
                                                    } catch (UnsupportedEncodingException ignored) {
                                                    }
                                                    webView.loadUrl("https://www.google.de/search?q=" + urlOrSearch);
                                                }
                                            }
                                        });

                                        customDialog1.findViewById(R.id.dialog_videoInternet_close).setOnClickListener(v1 -> customDialog1.dismiss());

                                        webView.setWebViewClient(new WebViewClient() {
                                            @Override
                                            public void onPageFinished(WebView view, String url) {
//                                                if (view.getProgress() == 100) {
                                                    dialog_videoInternet_url.setText(url);
//                                                }
                                            }
                                        });

                                    })
                                    .setOnBackPressedListener(customDialog1 -> {
                                        if (webView != null && webView.canGoBack()) {
                                            webView.goBack();
                                            return true;
                                        }
                                        return false;
                                    })
                                    .disableScroll()
                                    .setDimensions(true, true)
                                    .setOnDialogShown(customDialog1 -> isBrowserActive = true)
                                    .setOnDialogDismiss(customDialog1 -> isBrowserActive = false)
                                    .show();
                        } else {
                            internetDialog[0].show();
                            if (!webView.getOriginalUrl().equals(url))
                                webView.loadUrl(!url.isEmpty() ? url : "https://www.google.de/");
                        }
                    });

                    CheckBox dialog_editOrAddVideo_watchLater = editDialog.findViewById(R.id.dialog_editOrAddVideo_watchLater);
                    helper.defaultDialogValidation(editDialog).addValidator(dialog_editOrAddVideo_Title_layout, dialog_editOrAddVideo_Url_layout)
                            .addActionListener(dialog_editOrAddVideo_Title_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                apiSearchRequest(text, editDialog, editVideo[0]);
                            }, com.finn.androidUtilities.Helpers.TextInputHelper.IME_ACTION.SEARCH)
                            .setValidation(dialog_editOrAddVideo_Url_layout, (validator, text) -> {
                                if (text.isEmpty() && (checked[0] || Utility.isUpcoming(editVideo[0].getRelease())))
                                    validator.setValid();
                                else if (!text.isEmpty()) {
                                    if (Utility.isUrl(text)) {
                                        if (dialog_editOrAddVideo_watchLater.isChecked())
                                            validator.setWarning("Url bei 'Später-Ansehen'!");
                                        else
                                            validator.setValid();
                                    } else
                                        validator.setInvalid("Ungültige eingabe!");
                                }
                                validator.asWhiteList();
                            })
                            .warnIfEmpty(dialog_editOrAddVideo_Url_layout);


                    dialog_editOrAddVideo_watchLater.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        checked[0] = isChecked;
                        helper.validate(dialog_editOrAddVideo_Url_layout);
                    });
                    TextView dialog_editOrAddVideo_title_label = editDialog.findViewById(R.id.dialog_editOrAddVideo_title_label);
                    View.OnClickListener switchToSimilarVideo = v -> {
                        String text = dialog_editOrAddVideo_Title_layout.getEditText().getText().toString();
                        database.videoMap.values().stream().filter(video1 -> video1.getName().toLowerCase().equals(text.toLowerCase()) || (editVideo[0].getTmdbId() != 0 && editVideo[0].getTmdbId() == video1.getTmdbId())).findAny().ifPresent(oldVideo -> {
                            Runnable openEdit = () -> {
                                editDialog.dismiss();
                                isShared = false;
                                Pair<CustomDialog, Video> pair = showEditOrNewDialog(oldVideo);
                                CustomDialog newDialog = pair.first;
                                Video newEditVideo = pair.second;
                                if (!editVideo[0].getDarstellerList().isEmpty()) {
                                    if (newEditVideo.getDarstellerList().isEmpty()) {
                                        newEditVideo.setDarstellerList(editVideo[0].getDarstellerList());
                                    } else {
                                        editVideo[0].getDarstellerList().forEach(s -> {
                                            if (!newEditVideo.getDarstellerList().contains(s))
                                                newEditVideo.getDarstellerList().add(s);
                                        });
                                    }
                                }
                                if (!editVideo[0].getStudioList().isEmpty()) {
                                    if (newEditVideo.getStudioList().isEmpty()) {
                                        newEditVideo.setStudioList(editVideo[0].getStudioList());
                                    } else {
                                        editVideo[0].getStudioList().forEach(s -> {
                                            if (!newEditVideo.getStudioList().contains(s))
                                                newEditVideo.getStudioList().add(s);
                                        });
                                    }
                                }
                                if (!editVideo[0].getGenreList().isEmpty()) {
                                    if (newEditVideo.getGenreList().isEmpty()) {
                                        newEditVideo.setGenreList(editVideo[0].getGenreList());
                                    } else {
                                        editVideo[0].getGenreList().forEach(s -> {
                                            if (!newEditVideo.getGenreList().contains(s))
                                                newEditVideo.getGenreList().add(s);
                                        });
                                    }
                                }
                                newDialog.reloadView();
                                if (!Utility.stringExists(oldVideo.getUrl()))
                                    ((EditText) newDialog.findViewById(R.id.dialog_editOrAddVideo_url)).setText(dialog_editOrAddVideo_Url_layout.getEditText().getText().toString());
                                if (editVideo[0].getAgeRating() == -1)
                                    ((EditText) newDialog.findViewById(R.id.dialog_editOrAddVideo_ageRating)).setText(((EditText) editDialog.findViewById(R.id.dialog_editOrAddVideo_ageRating)).getText());
                                if (oldVideo.getLength() == 0)
                                    ((EditText) newDialog.findViewById(R.id.dialog_editOrAddVideo_length)).setText(((EditText) editDialog.findViewById(R.id.dialog_editOrAddVideo_length)).getText());
                                if (Utility.boolOr(oldVideo.getRating(), 0f, -1f))
                                    ((SeekBar) newDialog.findViewById(R.id.customRating_seekBar)).setProgress(((SeekBar) editDialog.findViewById(R.id.customRating_seekBar)).getProgress());
                            };

                            if (oldVideo.getDateList().isEmpty())
                                openEdit.run();
                            else {
                                CustomDialog.Builder(this)
                                        .setTitle("Details Oder Bearbeiten")
                                        .setText("Möchtest du 'Details', oder 'Bearbeiten' öffen?")
                                        .addButton("Details", customDialog1 -> {
                                            editDialog.dismiss();
                                            detailDialog = showDetailDialog(oldVideo);
                                        })
                                        .addButton("Bearbeiten", customDialog1 -> openEdit.run())
                                        .enableExpandButtons()
                                        .show();
                            }
                        });
                    };
                    dialog_editOrAddVideo_title_label.setOnLongClickListener(v -> {
                        String title = dialog_editOrAddVideo_Title_layout.getEditText().getText().toString();
                        if (CustomUtility.stringExists(title)) {
                            Toast.makeText(this, "Titel wird ausgewertet", Toast.LENGTH_SHORT).show();
                            parseTitleToDetails(null, editVideo, title);
                        }
                        return true;
                    });

                    if (video == null || isShared)
                        helper.setValidation(dialog_editOrAddVideo_Title_layout, (validator, text) -> {
                            if (database.videoMap.values().stream().anyMatch(video1 -> video1.getName().toLowerCase().equals(text.toLowerCase()) || (editVideo[0].getTmdbId() != 0 && editVideo[0].getTmdbId() == video1.getTmdbId()))) {
                                dialog_editOrAddVideo_title_label.setOnClickListener(switchToSimilarVideo);
                                dialog_editOrAddVideo_title_label.setTextColor(getColorStateList(R.color.clickable_text_color));
                                validator.setInvalid("Schon vorhanden! (Klicke auf das Label)");
                            } else {
                                dialog_editOrAddVideo_title_label.setOnClickListener(v -> {});
                                dialog_editOrAddVideo_title_label.setTextColor(getColorStateList(R.color.clickable_text_color_normal));
                            }
                        });


                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_LOAD_CAST_AND_STUDIOS)) {
                        TextView dialog_editOrAddVideo_actor_label = view.findViewById(R.id.dialog_editOrAddVideo_actor_label);
                        dialog_editOrAddVideo_actor_label.setEnabled(editVideo[0] != null && !editVideo[0]._getTempCastList().isEmpty());
                        dialog_editOrAddVideo_actor_label.setOnClickListener(v -> {
                            showImportCategoriesDialog(this, editDialog, editVideo[0], CategoriesActivity.CATEGORIES.DARSTELLER);
                        });

                        TextView dialog_editOrAddVideo_studio_label = view.findViewById(R.id.dialog_editOrAddVideo_studio_label);
                        dialog_editOrAddVideo_studio_label.setEnabled(editVideo[0] != null && !editVideo[0]._getTempStudioList().isEmpty());
                        dialog_editOrAddVideo_studio_label.setOnClickListener(v -> {
                            showImportCategoriesDialog(this, editDialog, editVideo[0], CategoriesActivity.CATEGORIES.STUDIOS);
                        });
                    }

                    Helpers.RatingHelper ratingHelper = new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout));

                    if (editVideo[0] != null) {
                        AutoCompleteTextView dialog_editOrAddVideo_Titel = view.findViewById(R.id.dialog_editOrAddVideo_Title);
                        dialog_editOrAddVideo_Titel.setText(editVideo[0].getName());
                        if (reload)
                            dialog_editOrAddVideo_Titel.dismissDropDown();
                        if (isShared && !reload && !editVideo[0].getName().isEmpty())
                            dialog_editOrAddVideo_Titel.onEditorAction(Helpers.TextInputHelper.IME_ACTION.SEARCH.getCode());


                        ImageView dialog_editOrAddVideo_translate = view.findViewById(R.id.dialog_editOrAddVideo_translate);
                        dialog_editOrAddVideo_translate.setVisibility(editVideo[0].getTranslationList().isEmpty() ? View.GONE : View.VISIBLE);
                        dialog_editOrAddVideo_translate.setOnClickListener(v -> {
                            CustomMenu.Builder(this, dialog_editOrAddVideo_translate)
                                    .setMenus((customMenu, items) -> {
                                        for (String translation : editVideo[0].getTranslationList())
                                            items.add(new CustomMenu.MenuItem(translation));
                                    })
                                    .setOnClickListener((customRecycler, itemView, item, index) -> {
//                                        editVideo.setName(item.getName());
                                        dialog_editOrAddVideo_Titel.setText(item.getName());
                                    })
                                    .dismissOnClick()
                                    .show();
                        });

                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_actor)).setText(
                                editVideo[0].getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_actor).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_studio)).setText(
                                editVideo[0].getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_studio).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(
                                editVideo[0].getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Genre).setSelected(true);
                        if (!reload)
                            ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_url)).setText(editVideo[0].getUrl());
                        if (editVideo[0].getRelease() != null) {
                            ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAddVideo_datePicker)).setDate(editVideo[0].getRelease());
                        }
                        EditText dialog_editOrAddVideo_ageRating = view.findViewById(R.id.dialog_editOrAddVideo_ageRating);
                        String ageRating = dialog_editOrAddVideo_ageRating.getText().toString().trim();
                        if (editVideo[0].getAgeRating() != -1 && ageRating.isEmpty())
                            dialog_editOrAddVideo_ageRating.setText(String.valueOf(editVideo[0].getAgeRating()));
                        EditText dialog_editOrAddVideo_length = view.findViewById(R.id.dialog_editOrAddVideo_length);
                        String length = dialog_editOrAddVideo_length.getText().toString().trim();
                        if (editVideo[0].getLength() > 0 && length.isEmpty())
                            dialog_editOrAddVideo_length.setText(String.valueOf(editVideo[0].getLength()));

                        dialog_editOrAddVideo_watchLater.setVisibility(Utility.isUpcoming(editVideo[0].getRelease()) ||
                                (video != null && !video.getName().isEmpty() && !isShared) ? View.GONE : View.VISIBLE);
                        int visibility = Utility.isUpcoming(editVideo[0].getRelease())/* && (video == null || !video.getName().isEmpty())*/ ? View.GONE : View.VISIBLE;
                        view.findViewById(R.id.dialog_editOrAddVideo_rating_layout).setVisibility(visibility);
                        view.findViewById(R.id.dialog_editOrAddVideo_url_allLayout).setVisibility(visibility);

                        ratingHelper.setRating(editVideo[0].getRating());
//                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(editVideo.getRating());
                    } else {
                        dialog_editOrAddVideo_watchLater.setVisibility(View.VISIBLE);
                        editVideo[0] = new Video("");
                        if (Utility.stringExists(searchQuery))
                            dialog_editOrAddVideo_Title_layout.getEditText().setText(searchQuery);
                    }

                    ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAddVideo_datePicker)).setOnDatePickListener(dateSelected -> {
                        editVideo[0].setRelease(dateSelected);
                        editDialog.reloadView();
                    });

                    if (!Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_RELEASE))
                        view.findViewById(R.id.dialog_editOrAddVideo_datePicker_layout).setVisibility(View.GONE);
                    if (!Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_AGE_RATING))
                        view.findViewById(R.id.dialog_editOrAddVideo_ageRating_layout).setVisibility(View.GONE);
                    if (!Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_LENGTH))
                        view.findViewById(R.id.dialog_editOrAddVideo_length_layout).setVisibility(View.GONE);


                    view.findViewById(R.id.dialog_editOrAddVideo_editActor).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, editVideo[0] == null ? null : editVideo[0].getDarstellerList(), editVideo[0], CategoriesActivity.CATEGORIES.DARSTELLER));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, editVideo[0] == null ? null : editVideo[0].getStudioList(), editVideo[0], CategoriesActivity.CATEGORIES.STUDIOS));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, editVideo[0] == null ? null : editVideo[0].getGenreList(), editVideo[0], CategoriesActivity.CATEGORIES.GENRE));

                    view.findViewById(R.id.dialog_editOrAddVideo_url_label).setOnLongClickListener(v -> {
                        String url = dialog_editOrAddVideo_Url_layout.getEditText().getText().toString();

                        if (!CustomUtility.stringExists(url) && Utility.isUrl(url))
                            Toast.makeText(this, "Keine URL Vorhanden", Toast.LENGTH_SHORT).show();
                        else
                            getDetailsFromUrl(url,  null, editVideo);

                        return true;
                    });
                })
                .setOnDialogShown(customDialog -> {
                    Toast toast = Utility.centeredToast(this, "");
                    helper.interceptDialogActionForValidation(customDialog, () -> {
                        toast.setText("Warnung: " + helper.getMessage().get(0) + "\n(Doppel-Click zum Fortfahren)");
                        toast.show();
                    }, toast::cancel);
                })
                .setOnDialogDismiss(customDialog -> isShared = false)
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String title = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_Title)).getText().toString().trim();
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_url)).getText().toString().trim();
                    String ageRating = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_ageRating)).getText().toString().trim();
                    String length = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_length)).getText().toString().trim();
                    float rating = ((RatingBar) customDialog.findViewById(R.id.customRating_ratingBar)).getRating();
                    if (video == null)
                        return !title.isEmpty() || !url.isEmpty() || !Utility.boolOr(rating, -1f, 0f) || !ageRating.isEmpty() || !length.isEmpty() || !editVideo[0].getDarstellerList().isEmpty() || !editVideo[0].getStudioList().isEmpty() || !editVideo[0].getGenreList().isEmpty();
                    else
                        return !title.equals(video.getName()) || !url.equals(video.getUrl()) || rating != video.getRating() || (ageRating.isEmpty() ? -1 : Integer.parseInt(ageRating)) != video.getAgeRating() || (length.isEmpty() ? 0 : Integer.parseInt(length)) != video.getLength() || !editVideo[0].equals(video);
                })
                .enableDynamicWrapHeight(videos_search.getRootView())
                .show();
        return Pair.create(returnDialog, editVideo[0]);
    }

    private void showSelectThumbnailDialog(String url, Video video, CustomDialog editDialog, CustomDialog internetDialog) {
        final int[] count = {3};
        CustomList<String> imageUrlList = new CustomList<>();
        CustomList<String> imageFromUrlParser = new CustomList<>();
        CustomList<String> imageFromOpenGraph = new CustomList<>();
        CustomList<String> imagesFromHtml = new CustomList<>();

        Runnable showDialog = () -> {
            CustomDialog selectThumbnailDialog = CustomDialog.Builder(this);
            selectThumbnailDialog
                    .setTitle("Thumbnail Auswählen")
                    .enableTitleBackButton()
                    .setView(new CustomRecycler<String>(this)
                            .setItemLayout(R.layout.list_item_select_thumbnail)
                            .setObjectList(imageUrlList)
                            .setSetItemContent((customRecycler, itemView, s) -> {
                                ImageView thumbnail = itemView.findViewById(R.id.listItem_selectThumbnail_thumbnail);
                                thumbnail.setVisibility(View.VISIBLE);
                                itemView.findViewById(R.id.listItem_selectThumbnail_editLayout).setVisibility(View.GONE);

                                Utility.loadUrlIntoImageView(this, thumbnail, s, null, () -> {
                                    thumbnail.setVisibility(View.GONE);
                                    itemView.findViewById(R.id.listItem_selectThumbnail_editLayout).setVisibility(View.VISIBLE);
                                    TextInputLayout listItem_selectThumbnail_url_layout = itemView.findViewById(R.id.listItem_selectThumbnail_url_layout);
                                    EditText listItem_selectThumbnail_url = listItem_selectThumbnail_url_layout.getEditText();
                                    Button listItem_selectThumbnail_test = itemView.findViewById(R.id.listItem_selectThumbnail_test);
                                    listItem_selectThumbnail_url.setText(s);
                                    new com.finn.androidUtilities.Helpers.TextInputHelper(listItem_selectThumbnail_test::setEnabled, listItem_selectThumbnail_url_layout)
                                            .setValidation(listItem_selectThumbnail_url_layout, (validator, text) -> validator.setRegEx(CategoriesActivity.pictureRegex));
                                    listItem_selectThumbnail_test.setOnClickListener(v2 -> {
                                        String newUrl = listItem_selectThumbnail_url.getText().toString().trim();
                                        int position = customRecycler.getRecycler().getChildAdapterPosition(itemView);
                                        imageUrlList.remove(position);
                                        imageUrlList.add(position, newUrl);
                                        customRecycler.update(position);
                                    });
                                });
                            })
                            .addSubOnClickListener(R.id.listItem_selectThumbnail_thumbnail, (customRecycler, itemView, s, index) -> {
                                video.setImagePath(s);
                                setThumbnailButton(video, editDialog);
                                selectThumbnailDialog.dismiss();
                                internetDialog.dismiss();
                            })
                            .generateRecyclerView())
                    .setDimensionsFullscreen()
                    .disableScroll()
                    .show();
        };

        Runnable connectLists = () -> {
            imageUrlList.addAll(imageFromUrlParser);
            imageUrlList.addAll(imageFromOpenGraph);
            imageUrlList.addAll(imagesFromHtml);
            imageUrlList.replaceAll(path -> path.startsWith("//") ? "https:" + path : path);
            showDialog.run();
        };

        Runnable lowerCount = () -> {
            count[0]--;
            if (count[0] <= 0)
                connectLists.run();
        };


        //       -------------------- Getter -------------------->
        Runnable getFromUrlParser = () -> {
            Utility.ifNotNull(UrlParser.getMatchingParser(url), urlParser -> {
                String script = urlParser.getThumbnailCode();
                if (!Utility.stringExists(script)) {
                    lowerCount.run();
                    return;
                }
                if (script.startsWith("{") && script.endsWith("}")) {
                    script = "(function() " + script + ")();";

                } else {
                    if (!script.endsWith(";"))
                        script += ";";
                }
                webView.evaluateJavascript(script, s -> {
                    if (s.startsWith("\"") && s.endsWith("\""))
                        s = Utility.subString(s, 1, -1);

                    if (s.matches(CategoriesActivity.pictureRegex))
                        imageFromUrlParser.add(s);
                    lowerCount.run();

                });
            }, lowerCount);
        };

        Runnable getFromOpenGraph = () -> {
            Utility.getOpenGraphFromWebsite(url, openGraph -> {
                String path;
                if (openGraph != null && (path = openGraph.getContent("image")) != null && path.matches(CategoriesActivity.pictureRegex)) {
                    imageFromOpenGraph.add(path);
                }

                lowerCount.run();

            });
        };

        final int[] htmlTries = {3};
        Runnable[] getFromHtml = {null};
        getFromHtml[0] = () -> {
            webView.evaluateJavascript(CustomUtility.SwitchExpression.setInput(htmlTries[0])
                    .addCase(3, "document.getElementsByTagName('html')[0].innerHTML;")
                    .addCase(2, "$('html').html();")
                    .addCase(1, "$('html').innerHTML;")
                    .addCase(0, "$('html').outerHTML;").evaluate(), value -> {
                if (!value.equals("null") || htmlTries[0] <= 0) {
                    imagesFromHtml.addAll(Utility.getImageUrlsFromText(value));
                    lowerCount.run();
                } else {
                    htmlTries[0]--;
                    getFromHtml[0].run();
                }
            });
        };
        //       <-------------------- Getter --------------------


        getFromUrlParser.run();
        getFromOpenGraph.run();
        getFromHtml[0].run();


//        webView.evaluateJavascript("$('html').html();"/*"(function() { " +
//                "var elements = document.getElementsByTagName('img');\n" +
//                "var arr = [];\n" +
//                "for (var i = 0; i < elements.length; i++) {\n" +
//                "    arr.push(elements[i].src);\n" +
//                "}\n" +
//                "return arr; })();"*/, value -> {
//
////                                                CustomList<String> imageUrlList = Arrays.stream(Utility.subString(value, 1, -1).split(","))
////                                                        .map(s -> Utility.subString(s, 1, -1)).collect(Collectors.toCollection(CustomList::new));
//            imagesFromHtml.addAll(Utility.getImageUrlsFromText(value));
//
//        });

    }

    private void setThumbnailButton(Video video, CustomDialog customDialog) {
        if (video == null || customDialog == null)
            return;
        String path = video.getImagePath();
        boolean stringExists = Utility.stringExists(path);
        ImageView thumbnail = customDialog.findViewById(R.id.dialog_editOrAddVideo_thumbnail);
        CustomUtility.ifNotNull(thumbnail, view -> {
            view.setAlpha(stringExists ? 1f : 0.4f);
        });
        if (stringExists)
            Utility.loadUrlIntoImageView(this, thumbnail, Utility.getTmdbImagePath_ifNecessary(path, false), null, null, () -> Utility.roundImageView(thumbnail, 3));
    }

    private void saveVideo(CustomDialog dialog, Video video, String titel, String url, boolean checked, Video editVideo) {
        boolean neuesVideo = video == null || isShared;
        if (video == null)
            video = editVideo;


        Video finalVideo = video;
        CustomUtility.isOnline(this, () -> {
            if (finalVideo != editVideo)
                finalVideo.getChangesFrom(editVideo);

            finalVideo.setName(titel);
            finalVideo.setUrl(url);
            finalVideo.setRating(((RatingBar) dialog.findViewById(R.id.customRating_ratingBar)).getRating());
            finalVideo.setRelease(((LazyDatePicker) dialog.findViewById(R.id.dialog_editOrAddVideo_datePicker)).getDate());
            String ageRating = ((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_ageRating)).getText().toString().trim();
            finalVideo.setAgeRating(ageRating.isEmpty() ? -1 : Integer.parseInt(ageRating));
            String length = ((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_length)).getText().toString().trim();
            finalVideo.setLength(length.isEmpty() ? 0 : Integer.parseInt(length));

            boolean addedYesterday = false;
            boolean upcoming = false;
            if (checked)
                finalVideo.setWatchLater(true);
            else if (neuesVideo) {
                if (!(upcoming = finalVideo.isUpcoming()))
                    addedYesterday = finalVideo.addDate(new Date(), true);
            }

            database.videoMap.put(finalVideo.getUuid(), finalVideo);
            allVideoList = new ArrayList<>(database.videoMap.values());
            reLoadVideoRecycler();
            dialog.dismiss();

            boolean finalAddedYesterday = addedYesterday;
            boolean finalUpcoming = upcoming;
            Database.saveAll(() -> Utility.showCenteredToast(this, singular + " gespeichert" + (finalAddedYesterday ? "\nAutomatisch für gestern eingetragen" : finalUpcoming ? "\n(Bevorstehend)" : "")), null,
                    () -> Toast.makeText(this, "Speichern fehlgeschlagen", Toast.LENGTH_SHORT).show());

            if (detailDialog != null)
                detailDialog.reloadView();
        });


    }

    private void getDetailsFromUrl(String url, Video video, Video[] editVideo) {
        Utility.GenericInterface<String> applyName = name -> parseTitleToDetails(video, editVideo, name);

        Utility.getOpenGraphFromWebsite(url, openGraph -> {
            String path;
            if (openGraph == null) {
//                                    Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean isImage = (path = openGraph.getContent("image")) != null && path.matches(CategoriesActivity.pictureRegex);

            if (editVideo[0] != null && isImage) {
                if (path.startsWith("//"))
                    path = "https:" + path;
                editVideo[0].setImagePath(path);
                setThumbnailButton(editVideo[0], addOrEditDialog);
            }

            if (UrlParser.getMatchingParser(url) == null) {
                String title = openGraph.getContent("title");
                if (title == null)
                    title = openGraph.getContent("description");
                if (title != null)
                    applyName.runGenericInterface(title);
            }
        });

        Utility.ifNotNull(UrlParser.getMatchingParser(url), urlParser -> {
            urlParser.parseUrl(this, url, name -> {
                if (!Utility.stringExists(name))
                    return;
                applyName.runGenericInterface(name);
            }, s -> {
                if (editVideo[0] != null) {
                    editVideo[0].setImagePath(s);
                    setThumbnailButton(editVideo[0], addOrEditDialog);
                }
            });
        });
    }

    private void parseTitleToDetails(Video video, Video[] editVideo, String name) {
        CustomList<String> actorIdList = database.darstellerMap.values().stream().filter(darsteller -> ParentClass_Alias.containedInQuery(darsteller, name)).map(ParentClass::getUuid).collect(Collectors.toCollection(CustomList::new));
        CustomList<String> studioIdList = database.studioMap.values().stream().filter(studio -> ParentClass_Alias.containedInQuery(studio, name)).map(ParentClass::getUuid).collect(Collectors.toCollection(CustomList::new));
        CustomList<String> genreIdList = database.genreMap.values().stream().filter(genre -> ParentClass_Alias.containedInQuery(genre, name)).map(ParentClass::getUuid).collect(Collectors.toCollection(CustomList::new));

        if (addOrEditDialog != null) {
            ((EditText) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_Title)).setText(name);
            if (editVideo[0] != null) {
                editVideo[0].setDarstellerList(actorIdList.add(editVideo[0].getDarstellerList().toArray(new String[0])).distinct());
                editVideo[0].setStudioList(studioIdList.add(editVideo[0].getStudioList().toArray(new String[0])).distinct());
                editVideo[0].setGenreList(genreIdList.add(editVideo[0].getGenreList().toArray(new String[0])).distinct());
                ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_actor)).setText(
                        editVideo[0].getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_studio)).setText(
                        editVideo[0].getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(
                        editVideo[0].getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));

            }
        } else {
            video.setName(name);
            video.setDarstellerList(actorIdList);
            video.setStudioList(studioIdList);
            video.setGenreList(genreIdList);
        }
    }
    //  <------------------------- EditVideo -------------------------


    //  ------------------------- Api ------------------------->
    private void apiSearchRequest(String queue, CustomDialog customDialog, Video video) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/search/movie?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&query=" +
                queue +
                "&page=1&include_adult=false";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Toast.makeText(this, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();

        CustomList<Pair<String, JSONObject>> jsonObjectList = new CustomList<>();
        AutoCompleteTextView dialog_editOrAddVideo_Titel = customDialog.findViewById(R.id.dialog_editOrAddVideo_Title);

        dialog_editOrAddVideo_Titel.setOnItemClickListener((parent, view2, position, id) -> {
            JSONObject jsonObject = (JSONObject) ((ImageAdapterItem) parent.getItemAtPosition(position)).getPayload();
            try {
                video.setRelease(new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(jsonObject.getString("release_date"))).setTmdbId(jsonObject.getInt("id"))
                        .setName(jsonObject.getString("original_title"));
                if (jsonObject.has("poster_path")) {
                    try {
                        video.setImagePath(jsonObject.getString("poster_path"));
                    } catch (JSONException ignored) {
                    }
                }


                video.setTranslationList(Arrays.asList(queue, jsonObject.getString("title"), jsonObject.getString("original_title")));

                JSONArray genre_ids = jsonObject.getJSONArray("genre_ids");
                CustomList<Integer> integerList = new CustomList<>();
                for (int i = 0; i < genre_ids.length(); i++) {
                    integerList.add(genre_ids.getInt(i));
                }
                Map<Integer, String> idUuidMap = database.genreMap.values().stream().filter(genre -> genre.getTmdbGenreId() != 0).collect(Collectors.toMap(Genre::getTmdbGenreId, ParentClass::getUuid));

                CustomList uuidList = integerList.map((Function<Integer, Object>) idUuidMap::get).filter(Objects::nonNull, false);
                video.setGenreList(uuidList);

                if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_LOAD_CAST_AND_STUDIOS)) {
                    apiDetailRequest(video.getTmdbId(), customDialog, video);
//                    apiCastRequest(video.getTmdbId(), customDialog, video);
//                    apiStudioRequest(video.getTmdbId(), customDialog, video);
                } else
                    customDialog.reloadView();
            } catch (JSONException | ParseException ignored) {
            }
        });

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
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
                    if (object.has("release_date")) {
                        release = object.getString("release_date");
                        if (!release.isEmpty())
                            release = String.format(" (%s)", release.substring(0, 4));
                    }
                    jsonObjectList.add(new Pair<>(object.getString("original_title") + release, object));
                }

                CustomList<ImageAdapterItem> itemList = jsonObjectList.map(stringJSONObjectPair -> {
                    ImageAdapterItem adapterItem = new ImageAdapterItem(stringJSONObjectPair.first).setPayload(stringJSONObjectPair.second);
                    if (stringJSONObjectPair.second.has("poster_path")) {
                        try {
                            adapterItem.setImagePath(stringJSONObjectPair.second.getString("poster_path"));
                        } catch (JSONException ignored) {
                        }
                    }
                    return adapterItem;
                });

                CustomAutoCompleteAdapter autoCompleteAdapter = new CustomAutoCompleteAdapter(this, itemList);

                dialog_editOrAddVideo_Titel.setAdapter(autoCompleteAdapter);


                View rootView = videos_search.getRootView();

                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int[] pos = new int[2];
                dialog_editOrAddVideo_Titel.getLocationOnScreen(pos);
                int height = r.bottom - (pos[1] + dialog_editOrAddVideo_Titel.getHeight());

                dialog_editOrAddVideo_Titel.setDropDownHeight(height);

                dialog_editOrAddVideo_Titel.showDropDown();

            } catch (JSONException ignored) {
            }

        }, error -> Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);

    }

    private void apiDetailRequest(int tmdbId, CustomDialog customDialog, Video video) {
        String requestUrl = "https://api.themoviedb.org/3/movie/" +
                tmdbId +
                "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&append_to_response=credits%2Crelease_dates";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomList<ParentClass_Tmdb> tempCastList = new CustomList<>();
        CustomList<ParentClass_Tmdb> tempStudioList = new CustomList<>();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                if (response.has("runtime"))
                    video.setLength(response.getInt("runtime"));
                if (response.has("imdb_id"))
                    video.setImdbId(response.getString("imdb_id"));
            } catch (JSONException ignored) {
            }

            try {
                if (response.has("release_dates")) {
                    JSONArray array = response.getJSONObject("release_dates").getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        if (array.getJSONObject(i).getString("iso_3166_1").equals("DE")) {
                            JSONArray releaseDates = array.getJSONObject(i).getJSONArray("release_dates");
                            for (int i1 = 0; i1 < releaseDates.length(); i1++) {
                                if (Utility.stringExists(releaseDates.getJSONObject(i1).get("certification").toString()))
                                    video.setAgeRating(releaseDates.getJSONObject(i1).getInt("certification"));
                            }
                            break;
                        }
                    }
                }
            } catch (JSONException ignored) {
            }

            try {
                results = response.getJSONArray("production_companies");

                if (results.length() != 0) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject object = results.getJSONObject(i);
                        String name = object.getString("name");

                        Optional<Studio> optional = database.studioMap.values().stream().filter(studio -> studio.getName().equals(name)).findFirst();

                        if (optional.isPresent()) {
                            if (!video.getStudioList().contains(optional.get().getUuid()))
                                video.getStudioList().add(optional.get().getUuid());
                        } else {
                            ParentClass_Tmdb studio = (ParentClass_Tmdb) new Studio(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("logo_path"));
                            if (studio.getImagePath().equals("null"))
                                studio.setImagePath(null);

                            tempStudioList.add(studio);
                        }
                    }

                    video._setTempStudioList(tempStudioList);
                }
            } catch (JSONException ignored) {
            }

            try {
                results = response.getJSONObject("credits").getJSONArray("cast");

                if (results.length() != 0) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject object = results.getJSONObject(i);
                        String name = object.getString("name");

                        Optional<Darsteller> optional = database.darstellerMap.values().stream().filter(darsteller -> darsteller.getName().equals(name)).findFirst();

                        if (optional.isPresent()) {
                            if (!video.getDarstellerList().contains(optional.get().getUuid()))
                                video.getDarstellerList().add(optional.get().getUuid());
                        } else {
                            ParentClass_Tmdb actor = (ParentClass_Tmdb) new Darsteller(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("profile_path"));
                            if (actor.getImagePath().equals("null"))
                                actor.setImagePath(null);

                            tempCastList.add(actor);
                        }
                    }

                    video._setTempCastList(tempCastList);
                }
            } catch (JSONException ignored) {
            }

            customDialog.reloadView();
        }, error -> {
        });

        requestQueue.add(jsonArrayRequest);

    }

    private void apiCastRequest(int tmdbId, CustomDialog customDialog, Video video) {
        String requestUrl = "https://api.themoviedb.org/3/movie/" +
                tmdbId +
                "/credits?api_key=09e015a2106437cbc33bf79eb512b32d";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomList<ParentClass_Tmdb> tempCastList = new CustomList<>();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("cast");

                if (results.length() == 0) {
                    return;
                }
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    String name = object.getString("name");

                    Optional<Darsteller> optional = database.darstellerMap.values().stream().filter(darsteller -> darsteller.getName().equals(name)).findFirst();

                    if (optional.isPresent()) {
                        if (!video.getDarstellerList().contains(optional.get().getUuid()))
                            video.getDarstellerList().add(optional.get().getUuid());
                    } else {
                        ParentClass_Tmdb actor = (ParentClass_Tmdb) new Darsteller(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("profile_path"));
                        if (actor.getImagePath().equals("null"))
                            actor.setImagePath(null);

                        tempCastList.add(actor);
                    }
                }

                video._setTempCastList(tempCastList);

                customDialog.reloadView();

            } catch (JSONException ignored) {
            }

        }, error -> {
        });

        requestQueue.add(jsonArrayRequest);

    }

    private void apiStudioRequest(int tmdbId, CustomDialog customDialog, Video video) {
        String requestUrl = "https://api.themoviedb.org/3/movie/" +
                tmdbId +
                "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomList<ParentClass_Tmdb> tempStudioList = new CustomList<>();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("production_companies");

                if (results.length() == 0) {
                    return;
                }
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    String name = object.getString("name");

                    Optional<Studio> optional = database.studioMap.values().stream().filter(studio -> studio.getName().equals(name)).findFirst();

                    if (optional.isPresent()) {
                        if (!video.getStudioList().contains(optional.get().getUuid()))
                            video.getStudioList().add(optional.get().getUuid());
                    } else {
                        ParentClass_Tmdb studio = (ParentClass_Tmdb) new Studio(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("logo_path"));
                        if (studio.getImagePath().equals("null"))
                            studio.setImagePath(null);

                        tempStudioList.add(studio);
                    }
                }

                video._setTempStudioList(tempStudioList);

                if (response.has("runtime"))
                    video.setLength(response.getInt("runtime"));
                if (response.has("imdb_id"))
                    video.setImdbId(response.getString("imdb_id"));

                customDialog.reloadView();

            } catch (JSONException ignored) {
            }

        }, error -> {
        });

        requestQueue.add(jsonArrayRequest);

    }

    public static void addActorToAll(Context context, ParentClass_Tmdb tmdbObject, CategoriesActivity.CATEGORIES category) {
        Database database = Database.getInstance();
//        if (category == CategoriesActivity.CATEGORIES.DARSTELLER) {
        String requestUrl = "https://api.themoviedb.org/3/person/" +
                tmdbObject.getTmdbId() +
                "/movie_credits?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        Collection<Video> videos = database.videoMap.values();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("cast");

                if (results.length() == 0) {
                    return;
                }
                final int[] count = {0};
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    int id = object.getInt("id");
                    videos.stream().filter(video -> video.getTmdbId() == id).findFirst().ifPresent(video -> {
                        if (!video.getDarstellerList().contains(tmdbObject.getUuid())) {
                            video.getDarstellerList().add(tmdbObject.getUuid());
                            count[0]++;
                        }
                    });
                }

                Toast.makeText(context, count[0] > 0 ? count[0] + " Aktualisiert" : "Nix zum Aktualisieren", Toast.LENGTH_SHORT).show();

                if (count[0] > 0 && context instanceof VideoActivity)
                    ((VideoActivity) context).reLoadVideoRecycler();

                if (count[0] > 0)
                    Database.saveAll();

            } catch (JSONException ignored) {
            }

        }, error -> {
        });

        requestQueue.add(jsonArrayRequest);
//        } else if (category == CategoriesActivity.CATEGORIES.STUDIOS) {
//            String requestUrl = "https://api.themoviedb.org/3/person/" +
//                    tmdbObject.getTmdbId() +
//                    "/movie_credits?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
//
//            RequestQueue requestQueue = Volley.newRequestQueue(context);
//            Collection<Video> videos = database.videoMap.values();
//
//            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
//                JSONArray results;
//                try {
//                    results = response.getJSONArray("cast");
//
//                    if (results.length() == 0) {
//                        return;
//                    }
//                    final int[] count = {0};
//                    for (int i = 0; i < results.length(); i++) {
//                        JSONObject object = results.getJSONObject(i);
//                        int id = object.getInt("id");
//                        videos.stream().filter(video -> video.getTmdbId() == id).findFirst().ifPresent(video -> {
//                            if (!video.getDarstellerList().contains(tmdbObject.getUuid())) {
//                                video.getDarstellerList().add(tmdbObject.getUuid());
//                                count[0]++;
//                            }
//                        });
//                    }
//
//                    Toast.makeText(context, count[0] > 0 ? count[0] + " Aktualisiert" : "Nix zum Aktualisieren", Toast.LENGTH_SHORT).show();
//
//                    if (count[0] > 0 && context instanceof VideoActivity)
//                        ((VideoActivity) context).reLoadVideoRecycler();
//
//                    if (count[0] > 0)
//                        Database.saveAll();
//
//                } catch (JSONException ignored) {}
//
//            }, error -> {});
//
//            requestQueue.add(jsonArrayRequest);
//        }
    }
    //  <------------------------- Api -------------------------

    private CustomDialog showImportCategoriesDialog(Context context, CustomDialog addOrEditDialog, Video editVideo, CategoriesActivity.CATEGORIES category) {
        CustomList<ParentClass_Tmdb> allObjectsList = Utility.SwitchExpression.setInput(category)
                .addCase(CategoriesActivity.CATEGORIES.DARSTELLER, editVideo._getTempCastList())
                .addCase(CategoriesActivity.CATEGORIES.STUDIOS, editVideo._getTempStudioList())
                .setDefault(new CustomList<ParentClass_Tmdb>())
                .evaluate();

        AutoCompleteTextView[] dialog_custom_edit = {null};

        CustomDialog dialog = CustomDialog.Builder(this)
                .setTitle(category.getSingular() + " Importieren")
                .setEdit(new CustomDialog.EditBuilder().allowEmpty().setShowKeyboard(false))
                .setOnDialogShown(customDialog -> dialog_custom_edit[0].showDropDown())
                .show();

        TextInputLayout dialog_custom_edit_editLayout = dialog.findViewById(R.id.dialog_custom_edit_editLayout);
        dialog_custom_edit[0] = (AutoCompleteTextView) dialog_custom_edit_editLayout.getEditText();


        CustomList<ImageAdapterItem> itemList = allObjectsList.map(parentClassTmdb -> {
            ImageAdapterItem adapterItem = new ImageAdapterItem(parentClassTmdb.getName()).setPayload(parentClassTmdb);
            if (parentClassTmdb.getImagePath() != null) {
                adapterItem.setImagePath(parentClassTmdb.getImagePath());
            }
            return adapterItem;
        });

        CustomAutoCompleteAdapter autoCompleteAdapter = new CustomAutoCompleteAdapter(this, itemList);
        dialog_custom_edit[0].setOnItemClickListener((parent, view, position, id) -> {
            ParentClass_Tmdb payload = (ParentClass_Tmdb) autoCompleteAdapter.getItem(position).getPayload();
            switch (category) {
                case DARSTELLER:
                    editVideo._getTempCastList().remove(payload);
                    database.darstellerMap.put(payload.getUuid(), (Darsteller) payload);
                    editVideo.getDarstellerList().add(payload.getUuid());
                    addActorToAll(this, payload, CategoriesActivity.CATEGORIES.DARSTELLER);
                    break;
                case STUDIOS:
                    editVideo._getTempStudioList().remove(payload);
                    database.studioMap.put(payload.getUuid(), (Studio) payload);
                    editVideo.getStudioList().add(payload.getUuid());
                    break;
            }
            dialog.dismiss();
            addOrEditDialog.reloadView();
            Toast.makeText(context, payload.getName() + " erfolgreich importiert", Toast.LENGTH_SHORT).show();
            Database.saveAll();
        });
        dialog_custom_edit[0].setAdapter(autoCompleteAdapter);

        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_video, menu);

        menu.findItem(R.id.taskBar_video_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        menu.findItem(R.id.taskBar_video_filterByDarsteller)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.ACTOR));
        menu.findItem(R.id.taskBar_video_filterByStudio)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.STUDIO));
        menu.findItem(R.id.taskBar_video_filterByGenre)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));
        menu.findItem(R.id.taskBar_video_filterByCollection)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.COLLECTION));

        if (mode.equals(MODE.ALL))
            menu.findItem(R.id.taskBar_video_modeAll).setChecked(true);
        else if (mode.equals(MODE.SEEN))
            menu.findItem(R.id.taskBar_video_modeSeen).setChecked(true);
        else if (mode.equals(MODE.LATER))
            menu.findItem(R.id.taskBar_video_modeLater).setChecked(true);
        else if (mode.equals(MODE.UPCOMING))
            menu.findItem(R.id.taskBar_video_modeUpcoming).setChecked(true);

        menu.findItem(R.id.taskBar_video_image).setChecked(showImages);
        menu.findItem(R.id.taskBar_video_scroll).setChecked(scrolling);

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!Database.isReady())
            return true;

        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_video_add:
                addOrEditDialog = showEditOrNewDialog(null).first;
                break;
            case R.id.taskBar_video_random:
                showRandomDialog();
                break;
            case R.id.taskBar_video_scroll:
                if (item.isChecked()) {
                    item.setChecked(false);
                    scrolling = false;
                } else {
                    item.setChecked(true);
                    scrolling = true;
                }
                customRecycler_VideoList.reload();
                break;
            case R.id.taskBar_video_image:
                if (item.isChecked()) {
                    item.setChecked(false);
                    showImages = false;
                } else {
                    item.setChecked(true);
                    showImages = true;
                }
                customRecycler_VideoList.reload();
                break;
            case R.id.taskBar_video_delete:
                if (database.videoMap.isEmpty()) {
                    Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (delete) {
                    videos_confirmDelete.setVisibility(View.GONE);
                } else {
                    videos_confirmDelete.setVisibility(View.VISIBLE);
                }
                customRecycler_VideoList.reload();
                delete = !delete;
                break;

            case R.id.taskBar_video_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadVideoRecycler();
                break;
            case R.id.taskBar_video_sortByViews:
                sort_type = SORT_TYPE.VIEWS;
                item.setChecked(true);
                reLoadVideoRecycler();
                break;
            case R.id.taskBar_video_sortByRating:
                sort_type = SORT_TYPE.RATING;
                item.setChecked(true);
                reLoadVideoRecycler();
                break;
            case R.id.taskBar_video_sortByLatest:
                sort_type = SORT_TYPE.LATEST;
                item.setChecked(true);
                reLoadVideoRecycler();
                break;
            case R.id.taskBar_video_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadVideoRecycler();
                break;

            case R.id.taskBar_video_filterByName:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.NAME);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.NAME);
                    item.setChecked(true);
                }
                commitSearch();
                setSearchHint();
                break;
            case R.id.taskBar_video_filterByDarsteller:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.ACTOR);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.ACTOR);
                    item.setChecked(true);
                }
                commitSearch();
                setSearchHint();
                break;
            case R.id.taskBar_video_filterByGenre:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.GENRE);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.GENRE);
                    item.setChecked(true);
                }
                commitSearch();
                setSearchHint();
                break;
            case R.id.taskBar_video_filterByStudio:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.STUDIO);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.STUDIO);
                    item.setChecked(true);
                }
                commitSearch();
                setSearchHint();
                break;
            case R.id.taskBar_video_filterByCollection:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.COLLECTION);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.COLLECTION);
                    item.setChecked(true);
                }
                commitSearch();
                setSearchHint();
                break;
            case R.id.taskBar_video_advancedSearch:
                removeFocusFromSearch();
                Utility.showAdvancedSearchDialog(this, videos_search, database.videoMap.values());
                break;

            case R.id.taskBar_video_modeAll:
                mode = MODE.ALL;
                item.setChecked(true);
                commitSearch();
                break;
            case R.id.taskBar_video_modeSeen:
                mode = MODE.SEEN;
                item.setChecked(true);
                commitSearch();
                break;
            case R.id.taskBar_video_modeLater:
                mode = MODE.LATER;
                item.setChecked(true);
                commitSearch();
                break;
            case R.id.taskBar_video_modeUpcoming:
                mode = MODE.UPCOMING;
                item.setChecked(true);
                commitSearch();
                break;

            case android.R.id.home:
                if (delete) {
                    videos_confirmDelete.setVisibility(View.GONE);
                    reLoadVideoRecycler();
                    delete = false;
                    return true;
                }
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return true;
    }

    private boolean commitSearch() {
        return textListener.onQueryTextChange(videos_search.getQuery().toString());
    }

    private void showRandomDialog() {
        removeFocusFromSearch();

        com.finn.androidUtilities.CustomList<Video> randomList = new com.finn.androidUtilities.CustomList<>(filteredVideoList).filter(video -> !video.isUpcoming(), false);
        if (randomList.isEmpty()) {
            Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
            return;
        }
        Collections.shuffle(randomList);
        randomVideo = randomList.get(0);

        com.finn.androidUtilities.CustomList<Video> markedVideos = new com.finn.androidUtilities.CustomList<>();

        int previousButtonId = View.generateViewId();
        int nextButtonId = View.generateViewId();
        int markButtonId = View.generateViewId();
        int unmarkButtonId = View.generateViewId();

        CustomDialog.OnDialogCallback showMarkedFilms = customDialog -> {
            if (markedVideos.isEmpty())
                customDialog.dismiss();
            else {
                CustomDialog.Builder(this)
                        .setTitle(String.format(Locale.getDefault(), "Markierte %s (%d)", plural, markedVideos.size()))
                        .setView(new com.finn.androidUtilities.CustomRecycler<Video>(this, customDialog.findViewById(R.id.dialogDetail_collection_videos))
                                .setItemLayout(R.layout.list_item_collection_video)
                                .setGetActiveObjectList(customRecycler -> markedVideos)
                                .setSetItemContent((customRecycler1, itemView, video) -> {
                                    String imagePath = video.getImagePath();
                                    ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                    if (Utility.stringExists(imagePath)) {
                                        imagePath = Utility.getTmdbImagePath_ifNecessary(imagePath, true);
                                        Utility.loadUrlIntoImageView(this, thumbnail,
                                                imagePath, imagePath, null, () -> Utility.roundImageView(thumbnail, 8));
                                        thumbnail.setVisibility(View.VISIBLE);

                                        thumbnail.setOnLongClickListener(v -> {
                                            startActivityForResult(new Intent(this, VideoActivity.class)
                                                            .putExtra(CategoriesActivity.EXTRA_SEARCH, video.getUuid())
                                                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO),
                                                    CategoriesActivity.START_CATEGORY_SEARCH);

                                            return true;
                                        });
                                    } else
                                        thumbnail.setImageResource(R.drawable.ic_no_image);


                                    ((TextView) itemView.findViewById(R.id.listItem_collectionVideo_text)).setText(video.getName());
                                })
                                .setOrientation(com.finn.androidUtilities.CustomRecycler.ORIENTATION.HORIZONTAL)
                                .generateRecyclerView()
                        )
                        .show();
            }
        };
        CustomDialog randomDialog = CustomDialog.Builder(this)
                .setTitle(randomVideo.getName())
                .setView(R.layout.dialog_detail_video)
                .addButton(R.drawable.ic_info, customDialog -> detailDialog = showDetailDialog(randomVideo).setPayload(customDialog), false)
                .addOptionalModifications(customDialog -> {
                    if (isDialog) {
                        int switchModeButtonId = View.generateViewId();
                        customDialog
                                .addButton(R.drawable.ic_time, customDialog1 -> {
                                    randomList.clear();
                                    randomList.addAll(new CustomList<>(filteredVideoList).filter(Video::isWatchLater, false));
                                    if (randomList.isEmpty()) {
                                        Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
                                        customDialog1.dismiss();
                                        return;
                                    } else {
                                        Toast.makeText(this, "Zu Später-Ansehen gewechselt", Toast.LENGTH_SHORT).show();
                                        customDialog1.getButton(switchModeButtonId).setVisibility(View.GONE);
                                    }
                                    Collections.shuffle(randomList);
                                    randomVideo = randomList.get(0);
                                    customDialog1.reloadView();
                                }, switchModeButtonId, false);
                    }

                })
                .addButton(R.drawable.ic_bookmark_empty, customDialog -> {
                    customDialog.getButton(unmarkButtonId).setVisibility(View.VISIBLE);
                    customDialog.getButton(markButtonId).setVisibility(View.GONE);
                    if (!markedVideos.contains(randomVideo))
                        markedVideos.add(randomVideo);
                    Toast.makeText(this, String.format(Locale.getDefault(), "Markierung hinzugefügt (%d)", markedVideos.size()), Toast.LENGTH_SHORT).show();
                }, markButtonId, false)
                .addButton(R.drawable.ic_bookmark_filled, customDialog -> {
                    customDialog.getButton(unmarkButtonId).setVisibility(View.GONE);
                    customDialog.getButton(markButtonId).setVisibility(View.VISIBLE);
                    markedVideos.remove(randomVideo);
                    Toast.makeText(this, String.format(Locale.getDefault(), "Markierung entfernt (%d)", markedVideos.size()), Toast.LENGTH_SHORT).show();
                }, unmarkButtonId, false)
                .alignPreviousButtonsLeft()
                .addButton("Zurück", customDialog -> {
                    randomVideo = randomList.previous(randomVideo, false);
                    customDialog.reloadView();
                }, previousButtonId, false)
                .addButton("Weiter", customDialog -> {
                    randomVideo = randomList.next(randomVideo, false);
                    customDialog.reloadView();
                }, nextButtonId, false)
                .colorLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    if (reload)
                        customDialog.setTitle(randomVideo.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(
                            randomVideo.getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(
                            randomVideo.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(
                            randomVideo.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

                    String imagePath = randomVideo.getImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        ImageView dialog_video_poster = view.findViewById(R.id.dialog_video_poster);
                        dialog_video_poster.setVisibility(View.VISIBLE);

                        Utility.loadUrlIntoImageView(this, dialog_video_poster, Utility.getTmdbImagePath_ifNecessary(imagePath, false), Utility.getTmdbImagePath_ifNecessary(imagePath, true));
                    } else
                        customDialog.findViewById(R.id.dialog_video_poster).setVisibility(View.GONE);


                    customDialog.getButton(previousButtonId).setEnabled(!randomList.isFirst(randomVideo));
                    customDialog.getButton(nextButtonId).setEnabled(!randomList.isLast(randomVideo));
                    customDialog.getButton(markButtonId).setVisibility(markedVideos.contains(randomVideo) ? View.GONE : View.VISIBLE);
                    customDialog.getButton(unmarkButtonId).setVisibility(markedVideos.contains(randomVideo) ? View.VISIBLE : View.GONE);

                })
                .addOnDialogDismiss(customDialog -> {
                    if (isDialog)
                        finish();
                })
                .setDismissWhenClickedOutside(false)
                .enableTitleBackButton()
                .setOnBackPressedListener(customDialog -> {
                    showMarkedFilms.runOnDialogCallback(customDialog);
                    return true;
                })
                .setOnTouchOutside(showMarkedFilms)
                .show();

        // ToDo: in der übersicht beenden und Zufall

    }

    private void openUrl(String url, boolean select) {
        if (!Utility.stringExists(url)) {
            Toast.makeText(this, "Keine URL hinterlegt", Toast.LENGTH_SHORT).show();
            return;
        }
        Utility.openUrl(this, url, select);
    }

    private void removeFocusFromSearch() {
        videos_search.clearFocus();
    }

    private void setSearchHint() {
        String join = filterTypeSet.stream().filter(FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(FILTER_TYPE::getName).collect(Collectors.joining(", "));
        videos_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
        Utility.applyToAllViews(videos_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (delete) {
            videos_confirmDelete.setVisibility(View.GONE);
            reLoadVideoRecycler();
            delete = false;
            return;
        }

        if (Utility.stringExists(videos_search.getQuery().toString()) && !Objects.equals(videos_search.getQuery().toString(), getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH))) {//&& !videos_search.getQuery().toString().matches(CategoriesActivity.uuidRegex) && Utility.isNullReturnOrElse(getCallingActivity(), true, componentName -> !componentName.getClassName().equals(CategoriesActivity.class.getName()))) {
            videos_search.setQuery("", false);
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Database.saveAll();
        super.onDestroy();
    }

}
