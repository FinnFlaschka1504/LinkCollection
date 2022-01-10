package com.maxMustermannGeheim.linkcollection.Activities.Content.Videos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.innovattic.rangeseekbar.RangeSeekBar;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.CustomCode;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.ImageAdapterItem;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
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
import org.liquidplayer.javascript.JSBaseArray;
import org.liquidplayer.javascript.JSValue;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";

    public static final String SEEN_SEARCH = "SEEN_SEARCH";
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";

    private static final String ADVANCED_SEARCH_CRITERIA_DATE = "dt";
    private static final String ADVANCED_SEARCH_CRITERIA_RATING = "r";
    private static final String ADVANCED_SEARCH_CRITERIA_LENGTH = "l";
    private static final String ADVANCED_SEARCH_CRITERIA_DURATION = "du";
    private static final String ADVANCED_SEARCH_CRITERIA_ACTOR = "a";
    private static final String ADVANCED_SEARCH_CRITERIA_STUDIO = "s";
    private static final String ADVANCED_SEARCH_CRITERIA_GENRE = "g";
    private static final String ADVANCED_SEARCH_CRITERIA_COLLECTION = "c";
    private static final String ADVANCED_SEARCH_CRITERIA_CUSTOM_CODE = "cc";

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
    private boolean isShared;
    private boolean isDialog;
    private Runnable setToolbarTitle;

    List<Video> allVideoList = new ArrayList<>();
    CustomList<Video> filteredVideoList = new CustomList<>();
    private Helpers.AdvancedQueryHelper<Video> advancedQueryHelper;

    private CustomDialog addOrEditDialog = null;
    private CustomDialog detailDialog;

    private CustomRecycler<Video> customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;

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
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_SEEN), R.drawable.ic_videos));
                    items.add(new CustomMenu.MenuItem(String.format(Locale.getDefault(), "Später ansehen (%d)", watchLaterCount), new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, WATCH_LATER_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_WATCH_LATER), R.drawable.ic_time));
                    items.add(new CustomMenu.MenuItem(String.format(Locale.getDefault(), "Bevorstehende (%d)", upcomingCount), new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, UPCOMING_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_UPCOMING), R.drawable.ic_calendar));
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
            Utility.applySelectionSearch(this, CategoriesActivity.CATEGORIES.VIDEO, Utility.getEditTextFromSearchView(videos_search));

            advancedQueryHelper = getAdvancedQueryHelper(this, videos_search, filterTypeSet);

            loadVideoRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
//                    videos_search.setOnQueryTextListener(null);
//                    videos_search.setQuery(new Helpers.SpannableStringHelper().appendColor(query, Color.RED).get(), true);
//                    advancedQueryHelper.applyColoration();
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
                                    .setText(new Helpers.SpannableStringHelper().append("Die geteilte URL ist bereits bei dem " + singular + " '").appendBold(video.getName()).append("' hinterlegt. Was möchtest du tun?").get())
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
                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (extraSearch.equals(SEEN_SEARCH)) {
                        mode = MODE.SEEN;
                        reLoadVideoRecycler();
                    } else if (extraSearch.equals(WATCH_LATER_SEARCH)) {
                        mode = MODE.LATER;
                        reLoadVideoRecycler();
                    } else if (extraSearch.equals(UPCOMING_SEARCH)) {
                        mode = MODE.UPCOMING;
                        reLoadVideoRecycler();
                    } else if (extraSearchCategory == CategoriesActivity.CATEGORIES.VIDEO) {
                        videos_search.setQuery(extraSearch, false);
                        if (extraSearch.matches("video_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}"))
                            database.videoMap.values().stream().filter(video -> video.getUuid().equals(extraSearch)).findFirst().ifPresent(this::showDetailDialog);
                    } else {
                        if (!advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch)) {
                            videos_search.setQuery(extraSearch, true);
                            if (extraSearch.matches("\\w*?_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}"))
                                allVideoList.stream().filter(video -> video.getUuid().equals(extraSearch)).findFirst().ifPresent(video1 -> detailDialog = showDetailDialog(video1));
                        }
                    }
//                    textListener.onQueryTextSubmit(videos_search.getQuery().toString());
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

        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }


    /** ------------------------- AdvancedQuery -------------------------> */
    public static Helpers.AdvancedQueryHelper<Video> getAdvancedQueryHelper(AppCompatActivity context, SearchView searchView, HashSet<FILTER_TYPE> filterTypeSet) {
        return new Helpers.AdvancedQueryHelper<Video>(context, searchView)
                .setRestFilter((restQuery, videos) -> {
                    if (restQuery.contains("|")) {
                        videos.filterOr(restQuery.split("\\|"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
                    } else {
                        videos.filterAnd(restQuery.split("&"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
                    }
                })
                .addCriteria_defaultName(R.id.dialog_advancedSearch_video_name, R.id.dialog_advancedSearch_video_negationLayout_name)
                .enableColoration()
                .enableHistory("ADVANCED_QUERY_VIDEO")
                .setDialogOptions(R.layout.dialog_advanced_search_video, null)
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<Float, Float>>(ADVANCED_SEARCH_CRITERIA_RATING, "(([0-4]((\\.|,)\\d{1,2})?)|5((\\.|,)00?)?)(-(([0-4]((\\5|\\7|(?<![,.]\\d{1,2}-\\d)[,.])\\d{1,2})?)|5((\\5|\\7|(?<![,.]\\d{1,2}-\\d)[,.])00?)?))?")
                        .setParser(sub -> {
                            String[] range = sub.replaceAll(",", ".").split("-");
                            float min = Float.parseFloat(range[0]);
                            float max = Float.parseFloat(range.length < 2 ? range[0] : range[1]);

                            return Pair.create(min, max);
                        })
                        .setBuildPredicate(ratingMinMax -> video -> video.getRating() >= ratingMinMax.first && video.getRating() <= ratingMinMax.second)
                        .setApplyDialog((customDialog, ratingMinMax, criteria) -> {
                            boolean[] negated = {false};
                            boolean[] singleMode = {false};
                            final int[] min = {0};
                            final int[] max = {20};

                            // ---------------

                            if (criteria.has()) {
                                min[0] = Math.round(ratingMinMax.first * 4);
                                max[0] = Math.round(ratingMinMax.second * 4);
                                singleMode[0] = ratingMinMax.first.equals(ratingMinMax.second);
                                negated[0] = criteria.isNegated();
                            }
                            Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(R.id.dialog_advancedSearch_video_negationLayout_rating), negated);

                            // ---------------

                            TextView rangeText = customDialog.findViewById(R.id.dialog_advancedSearch_video_range);
                            RangeSeekBar rangeBar = customDialog.findViewById(R.id.dialog_advancedSearch_video_rangeBar);
                            SeekBar singleBar = customDialog.findViewById(R.id.dialog_advancedSearch_video_singleBar);
                            CustomUtility.GenericInterface<Pair<Integer, Integer>> setText = pair -> {
                                singleBar.setEnabled(singleMode[0]);
                                if (singleMode[0])
                                    rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆", pair.first / 4d));
                                else
                                    rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆ – %.2f ☆", pair.first / 4d, pair.second / 4d));
                            };
                            rangeBar.setVisibility(singleMode[0] ? View.INVISIBLE : View.VISIBLE);
                            singleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser) {
                                        rangeBar.setMinThumbValue(progress);
                                        setText.run(Pair.create(progress, progress));
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });
                            rangeText.setOnClickListener(v -> {
                                if (singleMode[0] && singleBar.getProgress() == 20) {
                                    singleBar.setProgress(19);
                                    rangeBar.setMaxThumbValue(20);
                                }
                                singleMode[0] = !singleMode[0];
                                rangeBar.setVisibility(singleMode[0] ? View.INVISIBLE : View.VISIBLE);
                                setText.run(Pair.create(rangeBar.getMinThumbValue(), rangeBar.getMaxThumbValue()));
                            });
                            singleBar.setProgress(min[0]);
                            rangeBar.setMaxThumbValue(max[0]);
                            rangeBar.setMinThumbValue(min[0]);
                            setText.run(Pair.create(min[0], max[0]));

                            rangeBar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
                                @Override
                                public void onStartedSeeking() {

                                }

                                @Override
                                public void onStoppedSeeking() {
                                }

                                @Override
                                public void onValueChanged(int min, int max) {
                                    setText.run(Pair.create(min, max));
                                    singleBar.setProgress(min);
                                }
                            });

                            // ---------------

                            return customDialog1 -> {
                                min[0] = rangeBar.getMinThumbValue();
                                max[0] = rangeBar.getMaxThumbValue();

                                if (min[0] != 0 || max[0] != 20) {
                                    String ratingFilter;
                                    if (singleMode[0])
                                        ratingFilter = String.format(Locale.getDefault(), "%s%s:%.2f", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_RATING, min[0] / 4d);
                                    else
                                        ratingFilter = String.format(Locale.getDefault(), "%s%s:%.2f-%.2f", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_RATING, min[0] / 4d, max[0] / 4d);

                                    return ratingFilter;
                                } else
                                    return null;
                            };
                        }))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<Integer, Integer>>(ADVANCED_SEARCH_CRITERIA_LENGTH, "(((\\d+)-?(\\d+)?)|((\\d+)?-?(\\d+)))")
                        .setParser(getNumberRangeParser())
                        .setBuildPredicate(lengthMinMax -> video -> video.getLength() >= lengthMinMax.first && (lengthMinMax.second == -1 || video.getLength() <= lengthMinMax.second))
                        .setApplyDialog((customDialog, lengthMinMax, criteria) -> {
                            boolean[] negated = {false};
                            final Integer[] minLength = {null};
                            final Integer[] maxLength = {null};

                            // ---------------

                            if (criteria.has()) {
                                minLength[0] = lengthMinMax.first;
                                maxLength[0] = lengthMinMax.second;
                                negated[0] = criteria.isNegated();
                            }
                            Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(R.id.dialog_advancedSearch_video_negationLayout_length), negated);

                            // ---------------

                            TextInputEditText minLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_length_min_edit);
                            TextInputEditText maxLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_length_max_edit);

                            if (minLength[0] != null) {
                                minLength_edit.setText(CustomUtility.isNotValueReturnOrElse(minLength[0], String::valueOf, integer -> null, -1));
                                maxLength_edit.setText(CustomUtility.isNotValueReturnOrElse(maxLength[0], String::valueOf, integer -> null, -1));
                            }

                            // ---------------

                            return customDialog1 -> {
                                String minLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_video_length_min_edit)).getText().toString().trim();
                                String maxLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_video_length_max_edit)).getText().toString().trim();

                                if (CustomUtility.stringExists(minLength_str) && CustomUtility.stringExists(maxLength_str)) {
                                    if (Objects.equals(minLength_str, maxLength_str))
                                        return String.format(Locale.getDefault(), "l:%s", minLength_str);
                                    else
                                        return String.format(Locale.getDefault(), "l:%s-%s", minLength_str, maxLength_str);
                                } else if (CustomUtility.stringExists(minLength_str))
                                    return String.format(Locale.getDefault(), "%s%s:%s-", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_LENGTH, minLength_str);
                                else if (CustomUtility.stringExists(maxLength_str))
                                    return String.format(Locale.getDefault(), "%s%s:-%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_LENGTH, maxLength_str);
                                return null;
                            };
                        }))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<Date, Date>>(ADVANCED_SEARCH_CRITERIA_DATE, "(\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2}))(-\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2}))?")
                        .setParser(sub -> {
                            String[] range = sub.split("-");
                            Date min = null;
                            Date max = null;
                            try {
                                Utility.GenericReturnInterface<String, String> expandYear = s -> {
                                    String[] split = s.split("\\.");
                                    if (split[2].length() == 2) {
                                        split[2] = "20" + split[2];
                                        return String.join(".", split);
                                    } else
                                        return s;
                                };
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                                min = dateFormat.parse(expandYear.run(range[0]));
                                if (range.length > 1)
                                    max = dateFormat.parse(expandYear.run(range[1]));
                            } catch (ParseException ignored) {
                            }

                            return Pair.create(min, CustomUtility.isNotNullOrElse(max, min));
                        })
                        .setBuildPredicate(dateDatePair -> {
                            Pair<Long, Long> dateMinMaxTime = Pair.create(dateDatePair.first.getTime(), dateDatePair.second.getTime());
                            return video -> (
                                    video.getDateList().stream().anyMatch(date -> {
                                                long time = CustomUtility.removeTime(date).getTime();
                                                return time >= dateMinMaxTime.first && time <= dateMinMaxTime.second;
                                            }
                                    )
                            );
                        }))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<Date, Date>>(ADVANCED_SEARCH_CRITERIA_DURATION, "((-?\\d+[dmy])|(-?\\d+[dmy]|_(-?\\d+)?[my])(;-?\\d+[dmy]))")
                        .setParser(getDurationParser())
                        .setBuildPredicate_fromLastAdded(helper)
                        .setApplyDialog((customDialog, dateDatePair, durationCriteria) -> {
                            Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<Date, Date>> dateCriteria = helper.getSearchCriteriaByKey(ADVANCED_SEARCH_CRITERIA_DATE);

                            // ---------------

                            boolean[] negated = {false};
                            final Date[] from = {null};
                            final Date[] to = {null};

                            final String[] pivot = {""};
                            final String[] duration = {""};

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                            int timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000;
                            final Runnable[] applyStrings = {() -> {
                            }};

                            // ---------------

                            if (dateCriteria.has()) {
                                Pair<Date, Date> dateMinMax = dateCriteria.parse();
                                from[0] = dateMinMax.first;
                                to[0] = dateMinMax.second;
                            }

                            // ---------------

                            if (durationCriteria.has()) {
                                String[] split = durationCriteria.sub.split(";");
                                if (split.length == 1) {
                                    duration[0] = split[0];
                                } else {
                                    pivot[0] = split[0];
                                    duration[0] = split[1];
                                }
                            }

                            negated[0] = durationCriteria.isNegated() || dateCriteria.isNegated();
                            Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(R.id.dialog_advancedSearch_video_negationLayout_dateRangeOrDuration), negated);

                            /**  ------------------------- DateRange ------------------------->  */
                            TextView dialog_advancedSearch_viewed_text = customDialog.findViewById(R.id.dialog_advancedSearch_video_dateRange_text);
                            Runnable setDateRangeTextView = () -> {
                                if (from[0] != null && to[0] != null) {
                                    dialog_advancedSearch_viewed_text.setText(String.format("%s - %s", dateFormat.format(from[0]), dateFormat.format(to[0])));
                                } else if (from[0] != null) {
                                    dialog_advancedSearch_viewed_text.setText(dateFormat.format(from[0]));
                                } else {
                                    dialog_advancedSearch_viewed_text.setText("Nicht ausgewählt");
                                }
                            };
                            setDateRangeTextView.run();

                            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
                            builder.setTitleText("Zeitraum Auswählen");
                            if (from[0] != null && to[0] != null) {
                                builder.setSelection(androidx.core.util.Pair.create(from[0].getTime() - timezoneOffset, to[0].getTime() - timezoneOffset));
                            } else if (from[0] != null) {
                                builder.setSelection(androidx.core.util.Pair.create(from[0].getTime() - timezoneOffset, from[0].getTime() - timezoneOffset));
                            }
                            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

                            picker.addOnPositiveButtonClickListener(selection -> {
                                from[0] = new Date(selection.first + timezoneOffset);
                                if (!Objects.equals(selection.first, selection.second))
                                    to[0] = new Date(selection.second + timezoneOffset);
                                else
                                    to[0] = null;
                                setDateRangeTextView.run();
                                pivot[0] = "";
                                duration[0] = "";
                                applyStrings[0].run();
                            });

                            customDialog.findViewById(R.id.dialog_advancedSearch_video_dateRange_change).setOnClickListener(v -> picker.show(context.getSupportFragmentManager(), picker.toString()));
                            Runnable resetDateRange = () -> {
                                if (from[0] != null || to[0] != null) {
                                    from[0] = null;
                                    to[0] = null;
                                    setDateRangeTextView.run();
                                }
                            };

                            customDialog.findViewById(R.id.dialog_advancedSearch_video_dateRange_change).setOnLongClickListener(v -> {
                                resetDateRange.run();
                                return true;
                            });
                            /**  <------------------------- DateRange -------------------------  */


                            /**  ------------------------- Duration ------------------------->  */
                            TextInputEditText since_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_since_edit);
                            Spinner since_unit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_since_unit);
                            TextInputEditText duration_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_duration_edit);
                            Spinner duration_unit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_duration_unit);
                            applyDurationDialog(durationCriteria, pivot, duration, applyStrings, dialog_advancedSearch_viewed_text, resetDateRange, since_edit, since_unit, duration_edit, duration_unit);
                            /**  <------------------------- Duration -------------------------  */


                            return customDialog1 -> {
                                if (from[0] != null) {
                                    String dateFilter;
                                    if (to[0] != null)
                                        dateFilter = String.format(Locale.getDefault(), "%s%s:%s-%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_DATE, dateFormat.format(from[0]), dateFormat.format(to[0]));
                                    else
                                        dateFilter = String.format(Locale.getDefault(), "%s%s:%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_DATE, dateFormat.format(from[0]));

                                    return dateFilter;
                                }

                                // ---------------

                                if (CustomUtility.stringExists(duration[0])) {
                                    String dateDurationFilter;
                                    if (CustomUtility.stringExists(pivot[0]))
                                        dateDurationFilter = String.format(Locale.getDefault(), "%s%s:%s;%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_DURATION, pivot[0], duration[0]);
                                    else
                                        dateDurationFilter = String.format(Locale.getDefault(), "%s%s:%s", negated[0] ? "!" : "", ADVANCED_SEARCH_CRITERIA_DURATION, duration[0]);
                                    return dateDurationFilter;
                                }

                                return null;
                            };
                        }))
                .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA_ACTOR, CategoriesActivity.CATEGORIES.DARSTELLER, Video::getDarstellerList, context, R.id.dialog_advancedSearch_video_actor, R.id.dialog_advancedSearch_video_actor_connector, R.id.dialog_advancedSearch_video_editActor)
                .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA_STUDIO, CategoriesActivity.CATEGORIES.STUDIOS, Video::getStudioList, context, R.id.dialog_advancedSearch_video_studio, R.id.dialog_advancedSearch_video_studio_connector, R.id.dialog_advancedSearch_video_editStudio)
                .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA_GENRE, CategoriesActivity.CATEGORIES.GENRE, Video::getGenreList, context, R.id.dialog_advancedSearch_video_genre, R.id.dialog_advancedSearch_video_genre_connector, R.id.dialog_advancedSearch_video_editGenre)
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, String>(ADVANCED_SEARCH_CRITERIA_COLLECTION, "[^]]+?")
                        .setCategory(CategoriesActivity.CATEGORIES.COLLECTION)
                        .setParser(sub -> sub)
                        .setBuildPredicate(sub -> video -> Utility.containedInCollection(sub, video.getUuid(), true)))
                .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<Video, Pair<CustomCode.CustomCode_Video, String[]>>(ADVANCED_SEARCH_CRITERIA_CUSTOM_CODE, "\\w+(= *([^,]+)(, *[^,]+)*)?")
                        .setParser(sub -> {
                            String[] split = sub.split("=");
                            String name = split[0];
                            if (CustomUtility.stringExists(name)) {
                                CustomCode.CustomCode_Video customCode = (CustomCode.CustomCode_Video) CustomCode.getCustomCodeByName(CategoriesActivity.CATEGORIES.CUSTOM_CODE_VIDEO, name);
                                if (customCode == null)
                                    return null;
                                String[] params = split.length > 1 ? CustomCode.parseParams(split[1]) : new String[]{};
                                return Pair.create(customCode, params);
                            }
                            return null;
                        })
                        .setBuildPredicate(pair -> {
                            if (pair == null || pair.first == null)
                                return null;
//                            CustomUtility.logTiming("CustomCode", true);
                            JSValue result = pair.first.executeCode(context, pair.second);
//                            CustomUtility.logTiming("CustomCode", true);
                            if (result.isArray()) {
                                List<String> idList = result.toJSArray();
                                return video -> idList.contains(video.getUuid());
                            } else {
                                if (result.isString())
                                    Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
                                return null;
                            }
                        })
                        .setApplyDialog((customDialog, pair, criteria) -> {
                            Database database = Database.getInstance();
                            CustomList<CustomCode.CustomCode_Video> customCodeList = database.customCodeVideoMap.values().stream().filter(CustomCode::returnsList).sorted(ParentClass::compareByName).collect(Collectors.toCollection(CustomList::new));

                            TextInputLayout parameterEditLayout = customDialog.findViewById(R.id.dialog_advancedSearch_video_customCode_parameter_layout);
                            Spinner selectCustomCode = customDialog.findViewById(R.id.dialog_advancedSearch_video_customCode_select);


                            CustomList<String> selectionList = customCodeList.map(com.finn.androidUtilities.ParentClass::getName);
                            selectionList.add(0, "- Keins -");
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, selectionList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            selectCustomCode.setAdapter(adapter);

                            if (pair != null) {
                                if (pair.second.length > 0) {
                                    parameterEditLayout.getEditText().setText(String.join(", ", pair.second));
                                }

                                selectCustomCode.setSelection(selectionList.indexOf(pair.first.getName()));
                            }

                            return customDialog1 -> {
                                if (selectCustomCode.getSelectedItemPosition() == 0)
                                    return null;
                                String parameter = parameterEditLayout.getEditText().getText().toString().trim();
                                return String.format("%s:%s%s", ADVANCED_SEARCH_CRITERIA_CUSTOM_CODE, selectCustomCode.getSelectedItem().toString(), CustomUtility.stringExists(parameter) ? "=" + parameter : "");
                            };
                        }));
    }

    public static void applyDurationDialog(Helpers.AdvancedQueryHelper.SearchCriteria<?, Pair<Date, Date>> durationCriteria, String[] pivot, String[] duration, Runnable[] applyStrings, TextView dialog_advancedSearch_viewed_text, Runnable resetDateRange, TextInputEditText since_edit, Spinner since_unit, TextInputEditText duration_edit, Spinner duration_unit) {
        Map<String, Integer> modeMap = new HashMap<>();
        modeMap.put("d", 0);
        modeMap.put("m", 1);
        modeMap.put("y", 2);
        modeMap.put("_m", 3);
        modeMap.put("_y", 4);

        applyStrings[0] = () -> {
            if (CustomUtility.stringExists(duration[0])) {
                duration_edit.setText(CustomUtility.subString(duration[0], 0, -1));
                String durationMode = CustomUtility.subString(duration[0], -1);
                duration_unit.setSelection(modeMap.get(durationMode));
            } else {
                if (!duration_edit.getText().toString().equals(""))
                    duration_edit.setText("");
                if (duration_unit.getSelectedItemPosition() != 0)
                    duration_unit.setSelection(0);
            }

            if (CustomUtility.stringExists(pivot[0])) {
                boolean floored = pivot[0].contains("_");
                since_edit.setText(CustomUtility.subString(pivot[0], floored ? 1 : 0, -1));
                String sinceMode = CustomUtility.subString(pivot[0], -1);
                since_unit.setSelection(modeMap.get((floored ? "_" : "") + sinceMode));
            } else {
                if (!since_edit.getText().toString().equals(""))
                    since_edit.setText("");
                if (since_unit.getSelectedItemPosition() != 0)
                    since_unit.setSelection(0);
            }
        };
        applyStrings[0].run();

        int[] ignoreCount = {2};
        Runnable updateStrings = () -> {
            Set<String> since_keysByValue = Utility.getKeysByValue(modeMap, since_unit.getSelectedItemPosition());
            String since_mode = since_keysByValue.toArray(new String[0])[0];
            String since_text = since_edit.getText().toString();

            Set<String> duration_keysByValue = Utility.getKeysByValue(modeMap, duration_unit.getSelectedItemPosition());
            String duration_mode = duration_keysByValue.toArray(new String[0])[0];
            String duration_text = duration_edit.getText().toString();

            boolean sinceExists = (CustomUtility.stringExists(since_text) && !since_text.equals("-")) || since_mode.contains("_");
            boolean durationExists = CustomUtility.stringExists(duration_text) && !duration_text.equals("-");

            if (durationExists) {
                duration[0] = (duration_mode.contains("_") ? "_" : "") + duration_text + duration_mode.replaceAll("_", "");
                if (sinceExists)
                    pivot[0] = (since_mode.contains("_") ? "_" : "") + since_text + since_mode.replaceAll("_", "");
                else
                    pivot[0] = "";
            } else {
                pivot[0] = "";
                duration[0] = "";
            }

            if (CustomUtility.stringExists(duration[0])) {
                String dateDurationFilter;
                if (CustomUtility.stringExists(pivot[0]))
                    dateDurationFilter = String.format(Locale.getDefault(), "%s;%s", pivot[0], duration[0]);
                else
                    dateDurationFilter = duration[0];
                Pair<Date, Date> parseResult = durationCriteria.parse(dateDurationFilter);
                if (parseResult != null) {
                    Date today = CustomUtility.removeTime(new Date());
                    dialog_advancedSearch_viewed_text.setText(String.format("%s - %s",
                            parseResult.first.equals(today) ? "Heute" : Utility.formatDate(Utility.DateFormat.DATE_DOT, parseResult.first),
                            parseResult.second.equals(today) ? "Heute" : Utility.formatDate(Utility.DateFormat.DATE_DOT, parseResult.second)));
                }
            } else {
                if (ignoreCount[0] <= 0)
                    dialog_advancedSearch_viewed_text.setText("Nicht ausgewählt");
            }
            ignoreCount[0]--;
        };

        since_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (CustomUtility.stringExists(s.toString())) {
                    resetDateRange.run();
                } else
                    pivot[0] = "";

                updateStrings.run();
            }
        });
        duration_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (CustomUtility.stringExists(s.toString()))
                    resetDateRange.run();

                updateStrings.run();
            }
        });

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStrings.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        since_unit.setOnItemSelectedListener(spinnerListener);
        duration_unit.setOnItemSelectedListener(spinnerListener);
    }

    public static Utility.GenericReturnInterface<String, Pair<Integer, Integer>> getNumberRangeParser() {
        return sub -> {
            String[] range = sub.split("-");
            int min = range.length > 0 ? Integer.parseInt(CustomUtility.isNotValueOrElse(range[0], "-1", "")) : -1;
            int max = range.length > 1 ? Integer.parseInt(CustomUtility.isNotValueOrElse(range[1], "-1", "")) : (sub.endsWith("-") ? -1 : min);

            return Pair.create(min, max);
        };
    }

    public static Utility.GenericReturnInterface<String, Pair<Date, Date>> getDurationParser() {
        return sub -> {
            String[] range = sub.split(";");
            Date pivot;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Map<String, Integer> modeMap = new HashMap<>();
            modeMap.put("d", Calendar.DAY_OF_MONTH);
            modeMap.put("m", Calendar.MONTH);
            modeMap.put("y", Calendar.YEAR);


            if (range.length > 1) {
                String pivotString = range[0];
                if (pivotString.startsWith("_")) {
                    if (pivotString.endsWith("m")) {
                        if (pivotString.length() > 2)
                            cal.add(Calendar.MONTH, Integer.parseInt(CustomUtility.subString(pivotString, 1, -1)) * -1);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        pivot = cal.getTime();
                    } else {
                        if (pivotString.length() > 2)
                            cal.add(Calendar.YEAR, Integer.parseInt(CustomUtility.subString(pivotString, 1, -1)) * -1);
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        pivot = cal.getTime();
                    }
                } else {
                    cal.add(modeMap.get(CustomUtility.subString(pivotString, -1)), Integer.parseInt(CustomUtility.subString(pivotString, 0, -1)) * -1);
                    pivot = cal.getTime();
                }
            } else
                pivot = cal.getTime();

            String durationString = range.length > 1 ? range[1] : range[0];
            int durationInt = Integer.parseInt(CustomUtility.subString(durationString, 0, -1));
            String durationMode = CustomUtility.subString(durationString, -1);
//            if (durationMode.equals("d"))
//                durationInt = (Math.abs(durationInt) - 1) * (durationInt < 0 ? -1 : 1);
            cal.add(modeMap.get(durationMode), durationInt * -1);

            Date duration = cal.getTime();

            Pair<Date, Date> datePair = pivot.before(duration) ? Pair.create(pivot, duration) : Pair.create(duration, pivot);

//            if (!durationMode.equals("d")) {
//                cal.setTime(datePair.second);
//                cal.add(Calendar.DAY_OF_MONTH, -1);
//                datePair = Pair.create(datePair.first, cal.getTime());
//            }

            return datePair;
        };
    }
    /** <------------------------- AdvancedQuery ------------------------- */

    private List<Video> filterList() {
        filteredVideoList = new com.finn.androidUtilities.CustomList<>(allVideoList);

        if (mode.equals(MODE.SEEN)) {
            filteredVideoList = allVideoList.stream().filter(video -> !video.getDateList().isEmpty()).collect(Collectors.toCollection(CustomList::new));
        } else if (mode.equals(MODE.LATER)) {
            filteredVideoList = Utility.getWatchLaterList();
        } else if (mode.equals(MODE.UPCOMING)) {
            filteredVideoList = allVideoList.stream().filter(Video::isUpcoming).collect(Collectors.toCollection(CustomList::new));
        }
        if (!searchQuery.trim().equals("")) {
//            CustomUtility.logD(null, "filterList: ");
//            CustomUtility.logTiming("CustomCode", null);
            advancedQueryHelper.filterFull(filteredVideoList);
//            CustomUtility.logTiming("CustomCode", false);
        } else
            advancedQueryHelper.clean();
        return filteredVideoList;
    }

    private List<Video> sortList(List<Video> videoList) {
        SORT_TYPE sort_type = this.sort_type;
        Pair<CustomCode.CustomCode_Video, String[]> parse;
        if (advancedQueryHelper != null && advancedQueryHelper.has(ADVANCED_SEARCH_CRITERIA_CUSTOM_CODE) && (parse = (Pair<CustomCode.CustomCode_Video, String[]>) advancedQueryHelper.parse(ADVANCED_SEARCH_CRITERIA_CUSTOM_CODE)) != null) {
            ;
            CustomCode.CustomCode_Video customCode = parse.first;
            String sortType = customCode._getSortType();
            if (sortType != null) {
                if (sortType.equalsIgnoreCase("RESULT")) {
                    String jsValue = customCode._getTempResult();
                    List<String> array;
                    if (jsValue.matches("\\[.*]") && (array = new Gson().fromJson(jsValue, List.class)) != null) {
                        CustomList.replace(videoList, array.stream().map(id -> (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, id)).collect(Collectors.toList()));
                        return videoList;
                    }
                } else {
                    try {
                        sort_type = SORT_TYPE.valueOf(sortType);
                        // ToDo: vielleicht auch feldVariable ändern oder Menü deaktivieren
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        CustomUtility.showCenteredToast(VideoActivity.this, "Von CustomCode bestimmt\n" + sortType);
                        return true;
                    }
                });
                ((ActionMenuItemView) findViewById(R.id.taskBar_video_sort)).setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
//                ((ActionMenuItemView) findViewById(R.id.taskBar_video_sort)).setEnabled(false);
            } else
                ((ActionMenuItemView) findViewById(R.id.taskBar_video_sort)).setOnTouchListener(null);
        } else
            ((ActionMenuItemView) findViewById(R.id.taskBar_video_sort)).setOnTouchListener(null);

        switch (sort_type) {
            case NAME:
                videoList.sort((video1, video2) -> video1.getName().compareToIgnoreCase(video2.getName()));
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
                boolean[] negated = {false};

                if (CustomUtility.stringExists(searchQuery) && advancedQueryHelper != null && (advancedQueryHelper.has(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION))) {
                    if ((datePair[0] = (Pair<Date, Date>) advancedQueryHelper.parse(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) != null) {
                        datePairTime[0] = Pair.create(datePair[0].first.getTime(), datePair[0].second.getTime());
                        negated[0] = advancedQueryHelper.isNegated(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION);
                    }
                }

                videoList.sort((video1, video2) -> {
                    List<Date> dateList1 = video1.getDateList();
                    List<Date> dateList2 = video2.getDateList();

                    if (datePairTime[0] != null) {
                        dateList1 = dateList1.parallelStream().filter(date -> {
                            long time = CustomUtility.removeTime(date).getTime();
                            return (time >= datePairTime[0].first && time <= datePairTime[0].second) ^ negated[0];
                        }).collect(Collectors.toList());
                        dateList2 = dateList2.parallelStream().filter(date -> {
                            long time = CustomUtility.removeTime(date).getTime();
                            return (time >= datePairTime[0].first && time <= datePairTime[0].second) ^ negated[0];
                        }).collect(Collectors.toList());
                    }

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
        RecyclerView recycler = findViewById(R.id.recycler);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        customRecycler_VideoList = new CustomRecycler<Video>(this, recycler)
                .setItemLayout(R.layout.list_item_video)
                .setGetActiveObjectList((customRecycler) -> {
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

                        if (CustomUtility.stringExists(searchQuery) && advancedQueryHelper != null && advancedQueryHelper.has(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) {
                            final Pair<Date, Date>[] datePair = new Pair[]{null};
                            final Pair<Long, Long>[] datePairTime = new Pair[]{null};
                            boolean[] negated = {false};

                            if ((datePair[0] = (Pair<Date, Date>) advancedQueryHelper.parse(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) != null) {
                                datePairTime[0] = Pair.create(datePair[0].first.getTime(), datePair[0].second.getTime());
                                negated[0] = advancedQueryHelper.isNegated(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION);
                            }

                            filteredList.forEach(video -> {
                                int videoViews = (int) video.getDateList().stream().filter(date -> {
                                    long time = CustomUtility.removeTime(date).getTime();
                                    return (time >= datePairTime[0].first && time <= datePairTime[0].second) ^ negated[0];
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
                .setSetItemContent((customRecycler, itemView, video, index) -> {
                    itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE : View.GONE);

                    MinDimensionLayout listItem_video_image_layout = itemView.findViewById(R.id.listItem_video_image_layout);
                    if (showImages && CustomUtility.stringExists(video.getImagePath())) {
                        listItem_video_image_layout.setVisibility(View.VISIBLE);
                        ImageView image = itemView.findViewById(R.id.listItem_video_image);
                        Utility.loadUrlIntoImageView(this, image, Utility.getTmdbImagePath_ifNecessary(video.getImagePath(), false), Utility.getTmdbImagePath_ifNecessary(video.getImagePath(), true), null, () -> Utility.roundImageView(image, 4), this::removeFocusFromSearch);
                    } else
                        listItem_video_image_layout.setVisibility(View.GONE);


                    String searchQuery = Helpers.AdvancedQueryHelper.removeAdvancedSearch(this.searchQuery).toLowerCase();
                    String titleSub = advancedQueryHelper.getSearchCriteriaByKey(Helpers.AdvancedQueryHelper.ADVANCED_SEARCH_CRITERIA_NAME).getSub();
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(Helpers.SpannableStringHelper.highlightText(CustomUtility.stringExistsOrElse(titleSub, searchQuery), video.getName()));
                    if (!video.getDateList().isEmpty()) {
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        if (CustomUtility.stringExists(this.searchQuery) && advancedQueryHelper != null && advancedQueryHelper.has(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) {
                            final Pair<Date, Date>[] datePair = new Pair[]{null};
                            boolean[] negated = {false};
                            datePair[0] = (Pair<Date, Date>) advancedQueryHelper.parse(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION);
                            negated[0] = advancedQueryHelper.isNegated(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION);
                            ((TextView) itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().stream().filter(date -> (!date.before(datePair[0].first) && !date.after(datePair[0].second)) ^ negated[0]).count()));
                        } else
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
                    String actorSub = advancedQueryHelper.getSearchCriteriaByKey(ADVANCED_SEARCH_CRITERIA_ACTOR).getSub();
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(Helpers.SpannableStringHelper.highlightText(CustomUtility.stringExistsOrElse(actorSub, searchQuery), darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);

                    String studioNames = video.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).sorted(containsComparator).collect(Collectors.joining(", "));
                    String studioSub = advancedQueryHelper.getSearchCriteriaByKey(ADVANCED_SEARCH_CRITERIA_STUDIO).getSub();
                    ((TextView) itemView.findViewById(R.id.listItem_video_Studio)).setText(Helpers.SpannableStringHelper.highlightText(studioSub, studioNames));
                    itemView.findViewById(R.id.listItem_video_Studio).setSelected(scrolling);

                    String genreNames = video.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).sorted(containsComparator).collect(Collectors.joining(", "));
                    String genreSub = advancedQueryHelper.getSearchCriteriaByKey(ADVANCED_SEARCH_CRITERIA_GENRE).getSub();
                    ((TextView) itemView.findViewById(R.id.listItem_video_Genre)).setText(Helpers.SpannableStringHelper.highlightText(genreSub, genreNames));
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
                .enableFastScroll((customRecycler, video, integer) -> {
                    switch (sort_type) {
                        case NAME:
                            return video.getName().substring(0, 1).toUpperCase();
                        case VIEWS:
                            int size = video.getDateList().size();
                            if (size == 0)
                                return "Keine Ansichten";
                            return size + (size > 1 ? " Ansichten" : " Ansicht");
                        case RATING:
                            float rating = video.getRating();
                            if (rating == 0)
                                return "Keine Bewertung";
                            return rating + " ☆";
                        case LATEST:
                            CustomList<Date> dateList = new CustomList<>(video.getDateList());
                            if (CustomUtility.stringExists(searchQuery) && advancedQueryHelper != null && advancedQueryHelper.has(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) {
                                final Pair<Date, Date>[] datePair = new Pair[]{null};
                                final Pair<Long, Long>[] datePairTime = new Pair[]{null};
                                boolean[] negated = {false};

                                if ((datePair[0] = (Pair<Date, Date>) advancedQueryHelper.parse(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION)) != null) {
                                    datePairTime[0] = Pair.create(datePair[0].first.getTime(), datePair[0].second.getTime());
                                    negated[0] = advancedQueryHelper.isNegated(ADVANCED_SEARCH_CRITERIA_DATE, ADVANCED_SEARCH_CRITERIA_DURATION);
                                }

                                dateList.filter(date -> {
                                    long time = CustomUtility.removeTime(date).getTime();
                                    return (time >= datePairTime[0].first && time <= datePairTime[0].second) ^ negated[0];
                                }, true);
                            }
                            Date max = dateList.stream().max(Date::compareTo).orElse(null);
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

    private void reLoadVideoRecycler() {
//        customRecycler_VideoList.reload();
        if (customRecycler_VideoList != null)
            customRecycler_VideoList.reload();
    }

    private CustomDialog showDetailDialog(@NonNull Video video) {
        removeFocusFromSearch();
        final int[] views = {video.getDateList().size()};
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(video.getName())
                .addOptionalModifications(customDialog -> {
                    if (Utility.isUuid(searchQuery)) {
                        final boolean[] openedByUuidState = {true};
                        customDialog
                                .enableTitleBackButton(customDialog1 -> {
                                    customDialog1.dismiss();
                                    openedByUuidState[0] = false;
                                })
                                .addOnDialogDismiss(customDialog1 -> {
                                    if (openedByUuidState[0])
                                        finish();
                                });
                    }
                })
                .setView(R.layout.dialog_detail_video)
                .addOptionalModifications(customDialog -> {
                    if (Utility.boolOr(Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_VIDEO_QUICK_SEARCH)), 0, 1))
                        customDialog
                                .addButton(R.drawable.ic_search, customDialog1 -> showSearchDialog(video, null), false)
                                .alignPreviousButtonsLeft();
                })
                .addButton(CustomDialog.BUTTON_TYPE.EDIT_BUTTON, customDialog -> addOrEditDialog = showEditOrNewDialog(video).first, false)
                .markLastAddedButtonAsActionButton()
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
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.DARSTELLER, view.findViewById(R.id.dialog_video_Darsteller), video.getDarstellerList());
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.STUDIOS, view.findViewById(R.id.dialog_video_Studio), video.getStudioList());
                    view.findViewById(R.id.dialog_video_Studio).setSelected(true);
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.GENRE, view.findViewById(R.id.dialog_video_Genre), video.getGenreList());
                    view.findViewById(R.id.dialog_video_Genre).setSelected(true);

                    SpannableStringBuilder builder = new SpannableStringBuilder();

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
                    if (builder.length() > 0) {
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
                    } else
                        urlTextView.setClickable(true);

                    TextView viewsTextView = view.findViewById(R.id.dialog_video_views);
                    Utility.GenericInterface<Boolean> setViewsText = flip -> {
                        Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
                        SpannableStringBuilder viewsText = helper.quickItalic("Keine Ansichten");
                        if (views[0] > 0) {
                            Date lastWatched = new CustomList<>(video.getDateList()).getBiggest();
                            helper.append(String.valueOf(views[0]));
                            long days = Days.daysBetween(new LocalDate(lastWatched), new LocalDate(new Date())).getDays();
                            if (viewsTextView.getText().toString().contains("–") == flip) {
                                viewsText = helper.appendItalic(String.format(Locale.getDefault(), "   (%s)", Helpers.DurationFormatter.formatDefault(Duration.ofDays(days), "'%y% Jahr§e§~, ~''%w% Woche§n§~, ~''%d% Tag§e§~, ~'"))).get();
                            } else {
                                viewsText = helper.appendItalic(String.format(Locale.getDefault(), "   (%s – %dd)", new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(lastWatched), days)).get();
                            }
                        }
                        viewsTextView.setText(viewsText);
                    };
                    setViewsText.run(!reload);
                    if (views[0] > 0) {
                        viewsTextView.setOnClickListener(v -> setViewsText.run(true));
                        viewsTextView.setClickable(true);
                    } else
                        viewsTextView.setClickable(false);

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
                        Runnable addView = () -> {
                            boolean before = video.addDate(new Date(), true);
                            Utility.showCenteredToast(this, "Ansicht Hinzugefügt" + (before ? "\n(Gestern)" : ""));
                            Database.saveAll();
                            setResult(RESULT_OK);
                            customDialog.reloadView();
                            reLoadVideoRecycler();
                        };
                        WatchListActivity.checkWatchList(this, video, addView);
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
                                    .enableButtonDividerAll()
                                    .show();
                        });
                    else
                        dialog_video_internet.setVisibility(View.GONE);

                    Utility.applySelectionSearch(this, CategoriesActivity.CATEGORIES.VIDEO, customDialog.getTitleTextView());
                })
                .addOnDialogDismiss(customDialog -> {
                    detailDialog = null;
                    Utility.ifNotNull(customDialog.getPayload(), o -> ((CustomDialog) o).reloadView());
                });
        returnDialog.show();
        detailDialog = returnDialog; // da sowieso immer gespeichert
        return returnDialog;
    }

    public void showCalenderDialog(List<Video> videoList, CustomDialog detailDialog) {
        List<Date> oldDates = Utility.concatenateCollections(videoList, Video::getDateList);
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
                .addOnDialogDismiss(customDialog -> {
                    List<Date> newDates = Utility.concatenateCollections(videoList, Video::getDateList);
                    if (!oldDates.equals(newDates))
                        setResult(RESULT_OK);
                })
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
                    Utility.searchQueryOnInternet(this, query);

                }, false)
                .colorLastAddedButton()
                .show();
    }

    public static void showIntersectionsDialog(AppCompatActivity context) {
        CustomList<String> resultList = new CustomList<>();
        final CustomList<CustomUtility.Triple<Integer, Integer, String>>[] tripleList = new CustomList[1];
        Runnable[] showDialog = {null};

        Thread th = new Thread(() -> {
            for (Video video : new CustomList<>(Database.getInstance().videoMap.values()))
                resultList.addAll(CustomUtility.getAllSubTextMutations(video.getName()).distinct());

            tripleList[0] = resultList.stream().distinct().map(s -> CustomUtility.Triple.create(Collections.frequency(resultList, s), s.split(" ").length, s)).sorted((o1, o2) -> {
                int compResult;
                if ((compResult = o1.second.compareTo(o2.second)) != 0)
                    return compResult * -1;
                if ((compResult = o1.first.compareTo(o2.first)) != 0)
                    return compResult * -1;
                return o1.third.compareTo(o2.third);
            }).filter(pair -> pair.first > 1).collect(Collectors.toCollection(CustomList::new));

            context.runOnUiThread(showDialog[0]);
        });
        th.setPriority(Thread.NORM_PRIORITY);
        th.start();

//                CustomList<CustomUtility.Triple<Integer, Integer, String>> tripleList = Stream.iterate(1, count -> count + 1).limit(300).map(integer -> CustomUtility.Triple.create(integer, integer, "Test " + integer)).collect(Collectors.toCollection(CustomList::new));
        Utility.GenericReturnInterface<String, Boolean> alreadyExists = text -> {
            if (Utility.findObjectByName(CategoriesActivity.CATEGORIES.DARSTELLER, text, true) != null)
                return true;
            else if (Utility.findObjectByName(CategoriesActivity.CATEGORIES.STUDIOS, text, true) != null)
                return true;
            else if (Utility.findObjectByName(CategoriesActivity.CATEGORIES.GENRE, text, true) != null)
                return true;
            else
                return false;
        };


        showDialog[0] = () -> {
            int maxLength = tripleList[0].stream().mapToInt(value -> value.second).max().orElse(0);
            int[] lengthRange = {0, maxLength};
            int[] countRange = {3, 11};
            final String[] regex = {""};
            final boolean[] isBlacklist = {true};

            Utility.GenericInterface<View> setLengthTexts = view -> {
                ((TextView) view.findViewById(R.id.dialog_intersections_maxLength)).setText("" + maxLength);
                ((TextView) view.findViewById(R.id.dialog_intersections_minMaxLength)).setText(String.format("%d–%d", lengthRange[0], lengthRange[1]));
            };

            Utility.GenericInterface<View> setCountTexts = view -> {
                ((TextView) view.findViewById(R.id.dialog_intersections_minMaxCount)).setText(String.format("%d–%s", countRange[0], countRange[1] != 11 ? countRange[1] + "" : "Max."));
            };

            CustomRecycler<CustomUtility.Triple<Integer, Integer, String>> customRecycler = new CustomRecycler<CustomUtility.Triple<Integer, Integer, String>>(context)
                    .setGetActiveObjectList(customRecycler1 -> {
                        return tripleList[0].filter(triple -> {
                            if (triple.second < lengthRange[0] || triple.second > lengthRange[1])
                                return false;
                            if (triple.first < countRange[0] || (triple.first > countRange[1] && countRange[1] != 11))
                                return false;
                            if (CustomUtility.stringExists(regex[0]))
                                try {
                                    if (triple.third.matches("(?i).*(" + regex[0] + ").*")) {
                                        return !isBlacklist[0];
                                    } else
                                        return isBlacklist[0];
                                } catch (Exception ignored) {
                                }
                            return true;
                        }, false);
                    })
                    .setItemLayout(R.layout.list_item_intersection)
                    .enableDivider(12)
                    .disableCustomRipple()
                    .setSetItemContent((customRecycler1, itemView, triple, index) -> {
                        ((TextView) itemView.findViewById(R.id.listItem_intersection_count)).setText(String.format("(%d)", triple.first));
                        ((TextView) itemView.findViewById(R.id.listItem_intersection_text)).setText(triple.third);

                        itemView.findViewById(R.id.listItem_intersection_exists).setVisibility(alreadyExists.run(triple.third) ? View.VISIBLE : View.GONE);
                    })
                    .setOnClickListener((customRecycler1, itemView, triple, index) -> {
                        context.startActivity(
                                new Intent(context, VideoActivity.class)
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, triple.third)
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO)
                        );
                    })
                    .setOnLongClickListener((customRecycler1, view, triple, index) -> {

                        CustomUtility.GenericInterface<CategoriesActivity.CATEGORIES> showAddCategoryDialog = category -> {
                            CustomList<Video> allMatchesList = new CustomList<>();
                            CustomList<Video> selectedMatchesList = new CustomList<>();
                            CategoriesActivity.showEditCategoryDialog(context, category, null, parentClass -> {
                                if (!selectedMatchesList.isEmpty()) {
                                    selectedMatchesList.forEach(video -> {
                                        switch (category) {
                                            case DARSTELLER:
                                                video.getDarstellerList().add(parentClass.getUuid());
                                                break;
                                            case STUDIOS:
                                                video.getStudioList().add(parentClass.getUuid());
                                                break;
                                            case GENRE:
                                                video.getGenreList().add(parentClass.getUuid());
                                                break;
                                        }
                                    });
                                }
                                customRecycler1.reload();
                            }, null, customDialog -> {
                                customDialog
                                        .addOnDialogShown(customDialog1 -> ((EditText) customDialog1.findViewById(R.id.dialog_editTmdbCategory_name)).setText(CustomUtility.capitalizeFirstAllFirstLetter(triple.third)))
                                        .transformLastAddedButtonToImageButton()
                                        .enableTransformAutoGeneratedButtonsToImageButtons()
                                        .addButton(R.drawable.ic_add, customDialog1 -> {
                                            if (allMatchesList.isEmpty()) {
                                                allMatchesList.addAll(Database.getInstance().videoMap.values().stream()
                                                        .filter(video -> video.getName().replaceAll("[^\\wöäüß]+", " ").toLowerCase().matches(".*(\\b" + triple.third + "\\b).*"))
                                                        .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                                                        .collect(Collectors.toList()));
                                            }
                                            CustomRecycler<Video> selectRecycler = new CustomRecycler<Video>(context)
                                                    .setObjectList(allMatchesList)
                                                    .setItemLayout(R.layout.list_item_select)
                                                    .enableDivider(12)
                                                    .removeLastDivider()
                                                    .setSetItemContent((customRecycler2, itemView, video, index1) -> {
                                                        ImageView imageView = itemView.findViewById(R.id.selectList_thumbnail);
                                                        imageView.setVisibility(View.VISIBLE);
                                                        Utility.simpleLoadUrlIntoImageView(context, imageView, video.getImagePath(), video.getImagePath(), 4);
                                                        ((TextView) itemView.findViewById(R.id.selectList_name)).setText(video.getName());
                                                        ((CheckBox) itemView.findViewById(R.id.selectList_selected)).setChecked(selectedMatchesList.contains(video));
                                                    })
                                                    .setOnClickListener((customRecycler2, itemView, video, index1) -> {
                                                        if (selectedMatchesList.contains(video))
                                                            selectedMatchesList.remove(video);
                                                        else
                                                            selectedMatchesList.add(video);
                                                        customRecycler2.update(index1);
                                                    });

                                            CustomDialog.Builder(context)
                                                    .setTitle("Kategorie Zu Videos Hinzufügen")
//                                                .setText(allMatchesList.join("\n", com.finn.androidUtilities.ParentClass::getName))
                                                    .setView(customDialog2 -> selectRecycler.generateRecyclerView())
                                                    .addButton("Alle", customDialog2 -> {
                                                        selectedMatchesList.replaceWith(allMatchesList);
                                                        selectRecycler.reload();
                                                    }, false)
                                                    .addButton("Keine", customDialog2 -> {
                                                        selectedMatchesList.clear();
                                                        selectRecycler.reload();
                                                    }, false)
                                                    .alignPreviousButtonsLeft()
                                                    .addButton(CustomDialog.BUTTON_TYPE.CLOSE_BUTTON)
                                                    .addOnDialogDismiss(customDialog2 -> {
                                                        String text;
                                                        if (selectedMatchesList.isEmpty())
                                                            text = "Kien Video ausgewählt";
                                                        else if (selectedMatchesList.size() == allMatchesList.size())
                                                            text = "Alle Videos ausgewählt";
                                                        else
                                                            text = (selectedMatchesList.size() == 1 ? "Ein Video" : selectedMatchesList.size() + " Videos") + " ausgewählt";
                                                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                                                    })
                                                    .show();
                                        }, false)
                                        .addOptionalModifications(customDialog1 -> customDialog1.getLastAddedButton().enableAlignLeft())
                                        .moveLastAddedButton(1)
//                            .addButton("Test", customDialog1 -> {
//                                CustomDialog.Builder(context)
//                                        .setTitle("Result")
//                                        .setText(selectedMatchesList.join("\n", com.finn.androidUtilities.ParentClass::getName))
//                                        .show();
//                            }, false)
                                ;
                            });
                        };

                        CustomUtility.GenericInterface<CategoriesActivity.CATEGORIES> showAddAliasDialog = category -> {
                            CustomDialog.Builder(context)
                                    .setTitle("Alias Hinzufügen")
                                    .setEdit((customDialog, editBuilder) -> editBuilder
                                            .setText(CustomUtility.capitalizeFirstAllFirstLetter(triple.third))
                                            .setHint(category.getSingular() + " Alias")
                                            .setValidation((validator, text) -> {
                                                if (Utility.findObjectByName(category, text, true) != null)
                                                    validator.setInvalid("Der Name ist bereits vorhanden");
                                            }))
                                    .setView(customDialog -> {
                                        CustomRecycler<ParentClass> selectRecycler = new CustomRecycler<ParentClass>(context)
                                                .setObjectList(Utility.getMapFromDatabase(category).values().stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList()))
                                                .setItemLayout(R.layout.list_item_select)
                                                .enableDivider(12)
                                                .removeLastDivider()
                                                .enableFastScroll()
                                                .setSetItemContent((customRecycler2, itemView, parentClass, index1) -> {
                                                    ImageView imageView = itemView.findViewById(R.id.selectList_thumbnail);
                                                    if (parentClass instanceof ParentClass_Image) {
                                                        imageView.setVisibility(View.VISIBLE);
                                                        Utility.simpleLoadUrlIntoImageView(context, imageView, ((ParentClass_Image) parentClass).getImagePath(), ((ParentClass_Image) parentClass).getImagePath(), 4);
                                                    } else
                                                        imageView.setVisibility(View.GONE);
                                                    ((TextView) itemView.findViewById(R.id.selectList_name)).setText(parentClass.getName());
                                                    itemView.findViewById(R.id.selectList_selected).setVisibility(View.GONE);
                                                })
                                                .setOnClickListener((customRecycler2, itemView, parentClass, index1) -> {
                                                    String text = customDialog.getEditText();
                                                    if (!CustomUtility.stringExists(text) || !customDialog.isEditValid()) {
                                                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    boolean result = ParentClass_Alias.addAlias(parentClass, text);
                                                    Toast.makeText(context, result ? "Erfolgreich Hinzugefügt" : "Fehler", Toast.LENGTH_SHORT).show();
                                                    customRecycler1.reload();
                                                    customDialog.dismiss();
                                                    Database.saveAll();
                                                });
                                        return selectRecycler.generateRecyclerView();
                                    })
                                    .disableScroll()
                                    .setDimensionsFullscreen()
                                    .show();

                        };

                        CustomDialog.Builder(context)
                                .setTitle("Aktion Auswählen")
                                .setText(new Helpers.SpannableStringHelper().appendBoldItalic("Hinzufügen: ").append(triple.third).append("\n").appendMultiple("(Für Alias lange Button klicken)", multipleSpans -> multipleSpans.italic().relativeSize(.75f)).get())
                                .enableTextAlignmentCenter()
                                .addButton("Darsteller Hinzufügen", customDialog -> showAddCategoryDialog.run(CategoriesActivity.CATEGORIES.DARSTELLER))
                                .setLastAddedButtonEnabled(Utility.findObjectByName(CategoriesActivity.CATEGORIES.DARSTELLER, triple.third, true) == null)
                                .addOnDisabledLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.DARSTELLER))
                                .addOnLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.DARSTELLER))
                                .addButton("Studio Hinzufügen", customDialog -> showAddCategoryDialog.run(CategoriesActivity.CATEGORIES.STUDIOS))
                                .setLastAddedButtonEnabled(Utility.findObjectByName(CategoriesActivity.CATEGORIES.STUDIOS, triple.third, true) == null)
                                .addOnDisabledLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.STUDIOS))
                                .addOnLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.STUDIOS))
                                .addButton("Genre Hinzufügen", customDialog -> showAddCategoryDialog.run(CategoriesActivity.CATEGORIES.GENRE))
                                .setLastAddedButtonEnabled(Utility.findObjectByName(CategoriesActivity.CATEGORIES.GENRE, triple.third, true) == null)
                                .addOnDisabledLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.GENRE))
                                .addOnLongClickToLastAddedButton(customDialog -> showAddAliasDialog.run(CategoriesActivity.CATEGORIES.GENRE))
                                .addButtonDivider(3)
                                .disableLastAddedButton()
                                .addButton("Websuche", customDialog -> Utility.searchQueryOnInternet(context, triple.third))
                                .enableStackButtons()
                                .enableButtonDividerAll()
                                .show();
                    })
                    .enableFastScroll()
                    .generate();

            CustomDialog.Builder(context)
                    .setTitle("Überschneidungen")
                    .setView(R.layout.dialog_intersection)
                    .setSetViewContent((customDialog, view, reload) -> {
                        RangeSeekBar lengthSeekBar = view.findViewById(R.id.dialog_intersections_lengthLimit);
                        lengthSeekBar.setMax(maxLength + 1);
                        setLengthTexts.run(view);
                        lengthSeekBar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
                            @Override
                            public void onStartedSeeking() {

                            }

                            @Override
                            public void onStoppedSeeking() {

                            }

                            @Override
                            public void onValueChanged(int i, int i1) {
                                lengthRange[0] = i;
                                lengthRange[1] = i1 - 1;
                                customRecycler.reload();
                                setLengthTexts.run(view);
                            }
                        });

                        RangeSeekBar countSeekBar = view.findViewById(R.id.dialog_intersections_countLimit);
                        setCountTexts.run(view);
                        countSeekBar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
                            @Override
                            public void onStartedSeeking() {

                            }

                            @Override
                            public void onStoppedSeeking() {

                            }

                            @Override
                            public void onValueChanged(int i, int i1) {
                                countRange[0] = i + 1;
                                countRange[1] = i1;
                                customRecycler.reload();
                                setCountTexts.run(view);
                            }
                        });

                        EditText regexEdit = view.findViewById(R.id.dialog_intersections_regex);
                        regexEdit.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                regex[0] = s.toString();
                                customRecycler.reload();
                            }
                        });
                        view.findViewById(R.id.dialog_intersections_regexSwap).setOnClickListener(v -> {
                            isBlacklist[0] = !isBlacklist[0];
                            ((TextInputLayout) view.findViewById(R.id.dialog_intersections_regex_layout)).setHint(isBlacklist[0] ? "BlackList-RegEx" : "WhiteList-RegEx");
                            customRecycler.reload();
                        });
                        view.findViewById(R.id.dialog_intersections_regexSwap).setOnLongClickListener(v -> {
                            ((TextInputLayout) view.findViewById(R.id.dialog_intersections_regex_layout)).getEditText().setText("\\b(ist?|a|the|are|and|or|und|oder|\\d)\\b");
                            return true;
                        });

                        customRecycler.setRecycler(view.findViewById(R.id.dialog_intersections_list)).generate();
                    })
                    .setDimensionsFullscreen()
                    .disableScroll()
                    .enableTitleBackButton()
                    .setOnBackPressedListener(customDialog -> {
                        Toast.makeText(context, "TitleBackButton benutzen", Toast.LENGTH_SHORT).show();
                        return true;
                    })
                    .show();
        };

        Toast.makeText(context, "Einen Moment...", Toast.LENGTH_SHORT).show();
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
                            WatchListActivity.checkWatchList(this, editVideo[0], () -> saveVideo(customDialog, video, titel, url, checked[0], editVideo[0]));
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
                    if (Utility.boolOr(Integer.parseInt(Settings.getSingleSetting(this, Settings.SETTING_VIDEO_QUICK_SEARCH)), 0, 2))
                        customDialog
                                .addButton(R.drawable.ic_search, customDialog1 -> showSearchDialog(editVideo[0], customDialog1), false)
                                .alignPreviousButtonsLeft();
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, onClickSave, false)
                .addOnLongClickToLastAddedButton(onClickSave)
                .disableLastAddedButton()
                .setSetViewContent((editDialog, view, reload) -> {
                    final CustomDialog[] internetDialogClick = {null};
                    final CustomDialog[] internetDialogLongClick = {null};
                    TextInputLayout dialog_editOrAddVideo_Title_layout = view.findViewById(R.id.dialog_editOrAddVideo_Title_layout);
                    Utility.applySelectionSearch(this, CategoriesActivity.CATEGORIES.VIDEO, dialog_editOrAddVideo_Title_layout.getEditText());
                    TextInputLayout dialog_editOrAddVideo_Url_layout = view.findViewById(R.id.dialog_editOrAddVideo_url_layout);

                    Boolean useTmdbSearch = Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_TMDB_SEARCH);

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
                                        currentResult[0] = resultList.previous(currentResult[0], true);
                                        customDialog1.reloadView();
                                    });
                                    selectNext.setOnClickListener(v1 -> {
                                        currentResult[0] = resultList.next(currentResult[0], true);
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
                                                        .setUserAgent("Mozilla/5.0 (Linux; Android 11; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.6 Mobile Safari/537.36")
                                                        .addRequest("document.querySelectorAll(\".BIB1wf .n3VNCb\")[0].getAttribute(\"src\")", result -> {
                                                            resultList.clear();
                                                            if (!result.startsWith("null"))
                                                                resultList.add(result);
//                                                                resultList.addAll(new Gson().fromJson(result, CustomList.class));
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
                                            editText.setText(ActivityResultHelper.getPath(this, ((Intent) o).getData()));
                                            customDialog1.getButton(showButtonId).click();
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

                    Utility.DoubleGenericInterface<CustomDialog, String> onImagePathResult = (customDialog, s) -> {
                        editVideo[0].setImagePath(s);
                        setThumbnailButton(editVideo[0], editDialog);
                        customDialog.dismiss();
                    };
                    Utility.DoubleGenericInterface<CustomDialog, String> onTitleResult = (customDialog, value) -> {
                        if (!value.isEmpty()) {
                            Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
                            dialog_editOrAddVideo_Title_layout.getEditText().setText(value);
                            if (useTmdbSearch)
                                dialog_editOrAddVideo_Title_layout.getEditText().onEditorAction(Helpers.TextInputHelper.IME_ACTION.SEARCH.getCode());
                            customDialog.dismiss();
                            parseTitleToDetails(video, editVideo, value);
                        }
                    };
                    view.findViewById(R.id.dialog_editOrAddVideo_internet).setOnClickListener(v -> {
                        String url = dialog_editOrAddVideo_Url_layout.getEditText().getText().toString();
                        Utility.showInternetDialog(this, url, internetDialogClick, false, false, onImagePathResult, null, onTitleResult, null);
                    });
                    view.findViewById(R.id.dialog_editOrAddVideo_internet).setOnLongClickListener(v -> {
                        String title = dialog_editOrAddVideo_Title_layout.getEditText().getText().toString();

                        title = "https://www.google.com/search?tbm=isch&q=" + CustomUtility.encodeTextForUrl(title);

                        Utility.showInternetDialog(this, title, internetDialogLongClick, true, false, onImagePathResult, null, onTitleResult, (webView, isThumbnails, onResult) -> {
                            if (isThumbnails) {
                                List<String> urls = new ArrayList<>();
                                webView.evaluateJavascript("(function() {\n" +
                                        "    return document.querySelectorAll(\".BIB1wf .n3VNCb\")[0].getAttribute(\"src\")\n" +
                                        "})();", value -> {
                                    if (CustomUtility.stringExists(value) && !value.startsWith("null"))
                                        urls.add(Utility.subString(value, 1, -1));
//                                        urls.addAll(new Gson().fromJson(value, CustomList.cl  ass));
                                    onResult.run(urls, !urls.isEmpty());
                                });
                            }
                        });
                        return true;
                    });


                    CheckBox dialog_editOrAddVideo_watchLater = editDialog.findViewById(R.id.dialog_editOrAddVideo_watchLater);
                    boolean warnEmptyUrl = Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_WARN_EMPTY_URL);
                    helper.defaultDialogValidation(editDialog)
                            .addValidator(dialog_editOrAddVideo_Title_layout, dialog_editOrAddVideo_Url_layout)
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
                                } else if (!warnEmptyUrl)
                                    validator.setValid();
                                validator.asWhiteList();
                            });
                    if (warnEmptyUrl)
                        helper.warnIfEmpty(dialog_editOrAddVideo_Url_layout);
                    else
                        helper.allowEmpty(dialog_editOrAddVideo_Url_layout);

                    if (useTmdbSearch)
                        helper.addActionListener(dialog_editOrAddVideo_Title_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                            apiSearchRequest(text, editDialog, editVideo[0]);
                        }, com.finn.androidUtilities.Helpers.TextInputHelper.IME_ACTION.SEARCH);


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
                                        .setText("Möchtest du 'Details', oder 'Bearbeiten' öffnen?")
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

                    CustomList<Video> videos = new CustomList<>(database.videoMap.values());
                    if (video != null)
                        videos.remove(video);
                    helper.setValidation(dialog_editOrAddVideo_Title_layout, (validator, text) -> {

                        if (videos.stream().anyMatch(video1 -> video1.getName().equalsIgnoreCase(text) || (editVideo[0].getTmdbId() != 0 && editVideo[0].getTmdbId() == video1.getTmdbId()))) {
                            dialog_editOrAddVideo_title_label.setOnClickListener(switchToSimilarVideo);
                            dialog_editOrAddVideo_title_label.setTextColor(getColorStateList(R.color.clickable_text_color));
                            validator.setInvalid("Schon vorhanden! (Klicke auf das Label)");
                        } else {
                            dialog_editOrAddVideo_title_label.setOnClickListener(v -> {
                            });
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
                        editDialog.updateDynamicWrapHeight();

                        ratingHelper.setRating(editVideo[0].getRating());
//                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(editVideo.getRating());
                    } else {
                        dialog_editOrAddVideo_watchLater.setVisibility(View.VISIBLE);
                        editVideo[0] = new Video("");
                        if (Utility.stringExists(searchQuery))
                            dialog_editOrAddVideo_Title_layout.getEditText().setText(advancedQueryHelper.getFreeSearchOrName());
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
                            Utility.showEditItemDialog(this, CustomUtility.isNullReturnOrElse(editVideo[0], null, Video::getDarstellerList), CategoriesActivity.CATEGORIES.DARSTELLER, (customDialog1, selectedIds) -> {
                                editVideo[0].setDarstellerList(selectedIds);
                                ((TextView) editDialog.findViewById(R.id.dialog_editOrAddVideo_actor)).setText(
                                        CategoriesActivity.joinCategoriesIds(selectedIds, CategoriesActivity.CATEGORIES.DARSTELLER));
                            }));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, CustomUtility.isNullReturnOrElse(editVideo[0], null, Video::getStudioList), CategoriesActivity.CATEGORIES.STUDIOS, (customDialog1, selectedIds) -> {
                                editVideo[0].setStudioList(selectedIds);
                                ((TextView) editDialog.findViewById(R.id.dialog_editOrAddVideo_studio)).setText(
                                        CategoriesActivity.joinCategoriesIds(selectedIds, CategoriesActivity.CATEGORIES.STUDIOS));
                            }));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, CustomUtility.isNullReturnOrElse(editVideo[0], null, Video::getGenreList), CategoriesActivity.CATEGORIES.GENRE, (customDialog1, selectedIds) -> {
                                editVideo[0].setGenreList(selectedIds);
                                ((TextView) editDialog.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(
                                        CategoriesActivity.joinCategoriesIds(selectedIds, CategoriesActivity.CATEGORIES.GENRE));
                            }));

                    view.findViewById(R.id.dialog_editOrAddVideo_url_label).setOnLongClickListener(v -> {
                        String url = dialog_editOrAddVideo_Url_layout.getEditText().getText().toString();

                        if (!CustomUtility.stringExists(url) && Utility.isUrl(url))
                            Toast.makeText(this, "Keine URL Vorhanden", Toast.LENGTH_SHORT).show();
                        else
                            getDetailsFromUrl(url, null, editVideo);

                        return true;
                    });
                })
                .setOnDialogShown(customDialog -> {
                    Toast toast = Utility.centeredToast(this, "");
                    helper.interceptDialogActionForValidation(customDialog, true, inputHelper -> {
                        toast.setText("Warnung: " + helper.getMessage().get(0) + "\n(Doppel-Click zum Fortfahren)");
                        toast.show();
                    }, t -> toast.cancel());
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
                    applyName.run(title);
            }
        });

        Utility.ifNotNull(UrlParser.getMatchingParser(url), urlParser -> {
            urlParser.parseUrl(this, url, name -> {
                if (!Utility.stringExists(name))
                    return;
                applyName.run(name);
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
                    try {
                        if (stringJSONObjectPair.second.has("poster_path"))
                            adapterItem.setImagePath(stringJSONObjectPair.second.getString("poster_path"));
                        if (stringJSONObjectPair.second.has("title"))
                            adapterItem.setAlias(stringJSONObjectPair.second.getString("title"));

                    } catch (JSONException ignored) {
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

        new Handler().post(() -> {
            final View v = findViewById(R.id.taskBar_filter);

            if (v != null) {
                v.setOnLongClickListener(v1 -> {
                    advancedQueryHelper.showAdvancedSearchDialog();
                    return true;
                });
            }
        });
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
//                Utility.showAdvancedSearchDialog(this, videos_search, database.videoMap.values());
                advancedQueryHelper.showAdvancedSearchDialog();
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

            case R.id.taskBar_video_customCode:
                CustomCode.showDetailDialog(this, (Map) database.customCodeVideoMap, (customCode, idList) -> {
                    CustomRecycler.SetItemContent<Video> setItemContent = customRecycler_VideoList.getSetItemContent();
                    return new CustomRecycler<Video>(this)
                            .setItemLayout(R.layout.list_item_video)
                            .setGetActiveObjectList(customRecycler -> idList.stream().map(jsValue -> database.videoMap.get(jsValue.toString())).collect(Collectors.toList()))
                            .setSetItemContent((customRecycler, itemView, video, index) -> {
                                setItemContent.runSetCellContent(customRecycler, itemView, video, index);
                                itemView.findViewById(R.id.listItem_video_Genre).setSelected(false);
                                itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(false);
                                itemView.findViewById(R.id.listItem_video_Studio).setSelected(false);

                            })
                            .setOnClickListener((customRecycler, itemView, video, index) -> {
                                int clickMode = Settings.getSingleSetting_int(this, Settings.SETTING_VIDEO_CLICK_MODE);
                                if (clickMode == 0)
                                    detailDialog = showDetailDialog(video);
                                else if (clickMode == 1)
                                    openUrl(video.getUrl(), false);
                            })
                            .addSubOnClickListener(R.id.listItem_video_internetOrDetails, (customRecycler, view, object, index) -> {
                                int clickMode = Settings.getSingleSetting_int(this, Settings.SETTING_VIDEO_CLICK_MODE);
                                if (clickMode == 0)
                                    openUrl(object.getUrl(), false);
                                else if (clickMode == 1)
                                    detailDialog = showDetailDialog(object);
                            })
                            .enableFastScroll()
                            .generateRecyclerView();
                });

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
        int unmarkedButtonId = View.generateViewId();

        com.finn.androidUtilities.Helpers.DoubleClickHelper doubleClickHelper = com.finn.androidUtilities.Helpers.DoubleClickHelper.create();

        CustomDialog.OnDialogCallback showMarkedFilms = customDialog -> {
            if (markedVideos.isEmpty()) {
                if (doubleClickHelper.check()) {
                    customDialog.dismiss();
                } else
                    Toast.makeText(this, "Doppelklick zum Schließen", Toast.LENGTH_SHORT).show();
            } else {
                CustomDialog.Builder(this)
                        .setTitle(String.format(Locale.getDefault(), "Markierte %s (%d)", plural, markedVideos.size()))
                        .setView(new CustomRecycler<Video>(this, customDialog.findViewById(R.id.dialogDetail_collection_videos))
                                        .setItemLayout(R.layout.list_item_collection_video)
                                        .setGetActiveObjectList(customRecycler -> markedVideos)
                                        .setSetItemContent((customRecycler1, itemView, video, index) -> {
                                            String imagePath = video.getImagePath();
                                            ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                            if (Utility.stringExists(imagePath)) {
                                                imagePath = Utility.getTmdbImagePath_ifNecessary(imagePath, true);
                                                Utility.loadUrlIntoImageView(this, thumbnail,
                                                        imagePath, imagePath, null, () -> Utility.roundImageView(thumbnail, 8));
                                                thumbnail.setVisibility(View.VISIBLE);

                                                thumbnail.setOnLongClickListener(v -> {
                                                    showDetailDialog(video);
//                                            startActivityForResult(new Intent(this, VideoActivity.class)
//                                                            .putExtra(CategoriesActivity.EXTRA_SEARCH, video.getUuid())
//                                                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO),
//                                                    CategoriesActivity.START_CATEGORY_SEARCH);

                                                    return true;
                                                });
                                            } else
                                                thumbnail.setImageResource(R.drawable.ic_no_image);


                                            ((TextView) itemView.findViewById(R.id.listItem_collectionVideo_text)).setText(video.getName());
                                        })
                                        .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                                        .generateRecyclerView()
                        )
                        .enableTitleBackButton()
                        .enableTitleRightButton(R.drawable.ic_save, customDialog1 -> {
                            WatchListActivity.editWatchList(this, new WatchList().setVideoIdList(markedVideos.map(com.finn.androidUtilities.ParentClass::getUuid)));
//                            Toast.makeText(this, "Videos: " + markedVideos.map(com.finn.androidUtilities.ParentClass::getName).join(", "), Toast.LENGTH_SHORT).show();
                        })
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
                    customDialog.getButton(unmarkedButtonId).setVisibility(View.VISIBLE);
                    customDialog.getButton(markButtonId).setVisibility(View.GONE);
                    if (!markedVideos.contains(randomVideo))
                        markedVideos.add(randomVideo);
                    Toast.makeText(this, String.format(Locale.getDefault(), "Markierung hinzugefügt (%d)", markedVideos.size()), Toast.LENGTH_SHORT).show();
                }, markButtonId, false)
                .addButton(R.drawable.ic_bookmark_filled, customDialog -> {
                    customDialog.getButton(unmarkedButtonId).setVisibility(View.GONE);
                    customDialog.getButton(markButtonId).setVisibility(View.VISIBLE);
                    markedVideos.remove(randomVideo);
                    Toast.makeText(this, String.format(Locale.getDefault(), "Markierung entfernt (%d)", markedVideos.size()), Toast.LENGTH_SHORT).show();
                }, unmarkedButtonId, false)
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
                    if (!reload)
                        Utility.applySelectionSearch(this, CategoriesActivity.CATEGORIES.VIDEO, customDialog.getTitleTextView());

                    if (reload)
                        customDialog.setTitle(randomVideo.getName());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.DARSTELLER, view.findViewById(R.id.dialog_video_Darsteller), randomVideo.getDarstellerList());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.STUDIOS, view.findViewById(R.id.dialog_video_Studio), randomVideo.getStudioList());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.GENRE, view.findViewById(R.id.dialog_video_Genre), randomVideo.getGenreList());
//                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(
//                            randomVideo.getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
//                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(
//                            randomVideo.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
//                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(
//                            randomVideo.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
//                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

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
                    customDialog.getButton(unmarkedButtonId).setVisibility(markedVideos.contains(randomVideo) ? View.VISIBLE : View.GONE);

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

        if (advancedQueryHelper.handleBackPress(this))
            return;

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Database.saveAll();
        super.onDestroy();
    }

}
