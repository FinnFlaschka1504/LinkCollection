package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.finn.androidUtilities.CustomUtility;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.ImageAdapterItem;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.mikhaellopez.lazydatepicker.LazyDatePicker;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class VideoActivity extends AppCompatActivity {
    public static final String SEEN_SEARCH = "SEEN_SEARCH";
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";

    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST
    }

    enum MODE {
        ALL, SEEN, LATER, UPCOMING
    }

    public enum FILTER_TYPE {
        NAME, ACTOR, GENRE, STUDIO
    }

    private Database database;
    private SharedPreferences mySPR_daten;
    private boolean delete = false;
    private List<String> toDelete = new ArrayList<>();
    private Video randomVideo;
    private boolean scrolling = true;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.ACTOR, FILTER_TYPE.GENRE, FILTER_TYPE.STUDIO));
    private MODE mode = MODE.ALL;
    private SearchView.OnQueryTextListener textListener;
    private boolean reverse = false;
    private String singular;
    private String plural;
    private boolean isBrowserActive;
    private WebView webView;
    private boolean isShared;

    List<Video> allVideoList = new ArrayList<>();
    List<Video> filterdVideoList = new ArrayList<>();

    private CustomDialog[] addOrEditDialog = new CustomDialog[]{null};
    private CustomDialog detailDialog;

    private CustomRecycler<Video> customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        String stringExtra = Settings.getSingleSetting(this, Settings.SETTING_SPACE_NAMES_ + Settings.Space.SPACE_VIDEO);
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
            setContentView(R.layout.activity_video);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

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
                else
                    videos_search.setQuery(extraSearch, false);
                textListener.onQueryTextSubmit(videos_search.getQuery().toString());
            }
        }
    }

    public static void showModeMenu(AppCompatActivity activity, View view) {
        if (!Database.isReady())
            return;

        int seenCount = (int) Database.getInstance().videoMap.values().stream().filter(video -> !video.getDateList().isEmpty()).count();
        int watchLaterCount = Utility.getWatchLaterList().size();
        int upcomingCount = (int) Database.getInstance().videoMap.values().stream().filter(Video::isUpcomming).count();
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
                if (video.getDateList().isEmpty() && !video.isUpcomming() && !Utility.getWatchLaterList().contains(video))
                    video.setWatchLater(true);
//                    database.watchLaterList.add(video.getUuid());
            }

            setContentView(R.layout.activity_video);
            allVideoList = new ArrayList<>(database.videoMap.values());
            sortList(allVideoList);
            filterdVideoList = new ArrayList<>(allVideoList);

            videos_confirmDelete = findViewById(R.id.videos_confirmDelete);
            videos_confirmDelete.setOnClickListener(view -> {
                for (String uuidVideo : toDelete) {
                    filterdVideoList.remove(database.videoMap.get(uuidVideo));
                    allVideoList.remove(database.videoMap.get(uuidVideo));
                    database.videoMap.remove(uuidVideo);
                }
                delete = false;
                videos_confirmDelete.setVisibility(View.GONE);

                reLoadVideoRecycler();
                setResult(RESULT_OK);

                Database.saveAll();
                Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " " + singular : " " + plural) + " gelöscht", Toast.LENGTH_SHORT).show();
            });
            loadVideoRecycler();

            videos_search = findViewById(R.id.search);
            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    filterdVideoList = new ArrayList<>(allVideoList);
                    if (mode.equals(MODE.SEEN)) {
                        filterdVideoList = allVideoList.stream().filter(video -> !video.getDateList().isEmpty()).collect(Collectors.toList());
                    }
                    else if (mode.equals(MODE.LATER)) {
                        filterdVideoList = Utility.getWatchLaterList();
                    }
                    else if (mode.equals(MODE.UPCOMING)) {
                        filterdVideoList = allVideoList.stream().filter(Video::isUpcomming).collect(Collectors.toList());
                    }
                    if (!query.trim().equals("")) {
                        if (query.contains("|")) {
                            Stream<Video> resultStream = null;
                            for (String subQuery : query.split("\\|")) {
                                if (resultStream == null)
                                    resultStream = filterdVideoList.stream().filter(video -> Utility.containedInVideo(subQuery.trim(), video, filterTypeSet));
                                else
                                    resultStream = Stream.concat(resultStream, filterdVideoList.stream().filter(video -> Utility.containedInVideo(subQuery.trim(), video, filterTypeSet)));
                            }
                            if (resultStream != null) {
                                filterdVideoList = resultStream.collect(Collectors.toList());
                            }
                        } else {
                            Stream<Video> filteredStream = filterdVideoList.stream();
                            for (String subQuery : query.split("&"))
                                filteredStream = filteredStream.filter(video -> Utility.containedInVideo(subQuery.trim(), video, filterTypeSet));
                            filterdVideoList = filteredStream.collect(Collectors.toList());
                        }
                    }
                    reLoadVideoRecycler();
                    return true;
                }
            };
            videos_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_ADD))
                addOrEditDialog[0] = showEditOrNewDialog(null);

            if (getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.SEND")) {
                CharSequence text;
                text = getIntent().getClipData().getItemAt(0).getText();
                if (text == null) {
                    text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                }

                if (text != null) {
                    isShared = true;
                    if (Utility.isUrl(text.toString())) {
                        String url = text.toString();
                        Video video = new Video("").setUrl(url);
                        if (url.contains("lookmovie")) {
                            String last = new CustomList<>(url.split("/")).getLast();
                            if (last != null) {
                                CustomList<String> list = new CustomList<>(last.split("-"));
                                if (list.getLast().matches("\\d+"))
                                    list.removeLast();
                                video.setName(String.join(" ", list));
                            }
                        }
                        addOrEditDialog[0] = showEditOrNewDialog(video);
                    } else
                        addOrEditDialog[0] = showEditOrNewDialog(new Video(text.toString()));
                }
            }

//            Database.saveAll();

        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
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
                videoList.sort((video1, video2) -> {
                    if (video1.getDateList().isEmpty() && video2.getDateList().isEmpty()) {
                        if (video1.isUpcomming() && video2.isUpcomming() || !video1.isUpcomming() && !video2.isUpcomming())
                            return video1.getName().compareTo(video2.getName());
                        else if (video1.isUpcomming())
                            return reverse ? -1 : 1;
                        else if (video2.isUpcomming())
                            return reverse ? 1 : -1;
                    } else if (video1.getDateList().isEmpty())
                        return reverse ? -1 : 1;
                    else if (video2.getDateList().isEmpty())
                        return reverse ? 1 : -1;

                    Date new1 = Collections.max(video1.getDateList());
                    Date new2 = Collections.max(video2.getDateList());
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
        customRecycler_VideoList = new CustomRecycler<Video>(this, findViewById(R.id.videos_recycler))
                .setItemLayout(R.layout.list_item_video)
                .setGetActiveObjectList(() -> sortList(filterdVideoList))
//                .setObjectList(filterdVideoList)
                .setSetItemContent((customRecycler, itemView, video) -> {
                    itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE : View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());
                    if (!video.getDateList().isEmpty()) {
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));
                    } else
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);

                    itemView.findViewById(R.id.listItem_video_later).setVisibility(Utility.getWatchLaterList().contains(video) ? View.VISIBLE : View.GONE);
                    itemView.findViewById(R.id.listItem_video_upcoming).setVisibility(video.isUpcomming() ? View.VISIBLE : View.GONE);

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    itemView.findViewById(R.id.listItem_video_Studio).setSelected(scrolling);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    itemView.findViewById(R.id.listItem_video_Genre).setSelected(scrolling);
                })
                .setOnClickListener((customRecycler, view, object, index) -> {
                    if (!delete) {
                        openUrl(object.getUrl(), false);
                    } else {
                        CheckBox checkBox = view.findViewById(R.id.listItem_video_deleteCheck);
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked())
                            toDelete.add(object.getUuid());
                        else
                            toDelete.remove(object.getUuid());
                        String test = null;
                    }
                })
                .addSubOnClickListener(R.id.listItem_video_details, (customRecycler, view, object, index) -> {
                    detailDialog = showDetailDialog(object);
                }, false)
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog(object);
                })
                .hideDivider()
                .generate();
    }

    private void reLoadVideoRecycler() {
//        customRecycler_VideoList.reload();
        customRecycler_VideoList.reload();
    }

    private CustomDialog showDetailDialog(@NonNull Video video) {
        setResult(RESULT_OK);
        final int[] views = {video.getDateList().size()};
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Detail Ansicht")
                .setView(R.layout.dialog_detail_video)
                .addButton("Bearbeiten", customDialog ->
                        addOrEditDialog[0] = showEditOrNewDialog(video), false)
                .addButton("Öffnen mit...", customDialog -> openUrl(video.getUrl(), true), false)
                .setSetViewContent((customDialog, view, reload) -> {
                    if (reload && views[0] != video.getDateList().size()) {
                        if (views[0] < video.getDateList().size() && Utility.getWatchLaterList().contains(video)) {
                            video.setWatchLater(false);
                            Database.saveAll();
                            textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                        }
                        views[0] = video.getDateList().size();
                    }

                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(video.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(
                            video.getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(
                            video.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialog_video_Studio).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(
                            video.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialog_video_Genre).setSelected(true);
                    view.findViewById(R.id.dialog_video_details).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.dialog_video_Url)).setText(video.getUrl());

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
                    ((TextView) view.findViewById(R.id.dialog_video_release)).setText(video.getRelease() != null ? new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(video.getRelease()) : "");
                    ((RatingBar) view.findViewById(R.id.dialog_video_rating)).setRating(video.getRating());

                    if (video.getImagePath() != null && !video.getImagePath().isEmpty()) {
                        ImageView dialog_video_poster = view.findViewById(R.id.dialog_video_poster);
                        dialog_video_poster.setVisibility(View.VISIBLE);
                        Glide
                                .with(this)
                                .load("https://image.tmdb.org/t/p/w92/" + video.getImagePath())
                                .placeholder(R.drawable.ic_download)
                                .into(dialog_video_poster);
                        dialog_video_poster.setOnClickListener(v -> {
                            com.finn.androidUtilities.CustomDialog posterDialog = com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setView(R.layout.dialog_poster)
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        ImageView dialog_poster_poster = view1.findViewById(R.id.dialog_poster_poster);
                                        Glide
                                                .with(this)
                                                .load("https://image.tmdb.org/t/p/original/" + video.getImagePath())
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
                        customDialog.reloadView();
                        reLoadVideoRecycler();
                        return true;
                    });
                    view.findViewById(R.id.dialog_video_editViews).setOnContextClickListener(view1 -> {
                        Toast.makeText(this, "Context Click", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    ImageView dialog_video_internet = view.findViewById(R.id.dialog_video_internet);
                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_TMDB_SHORTCUT))
                        dialog_video_internet.setOnClickListener(v -> Utility.openUrl(this, "https://www.themoviedb.org/movie/" + video.getTmdId(), true));
                    else
                        dialog_video_internet.setVisibility(View.GONE);

                })
                .setOnDialogDismiss(customDialog -> detailDialog = null);
        returnDialog.show();
        return returnDialog;
    }

    public void showCalenderDialog(List<Video> videoList, CustomDialog detailDialog) {
        com.finn.androidUtilities.CustomDialog.Builder(this)
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
//                    ((TextView) detailDialog.findViewById(R.id.dialog_video_views))
//                            .setName(String.valueOf(videoList.get(0).getDateList().size()));
                    detailDialog.reloadView();
                    this.reLoadVideoRecycler();
                })
                .enableTitleBackButton()
                .show();
    }


    private CustomDialog showEditOrNewDialog(Video video) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Video[] editVideo = {video};
        if (editVideo[0] != null) {
            editVideo[0] = video.clone();
        }
        Helpers.TextInputHelper helper = new Helpers.TextInputHelper();
        final boolean[] checked = {video != null && Utility.getWatchLaterList().contains(video)};
        @SuppressLint("SetJavaScriptEnabled") CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(video == null ? "Neu: " + singular : singular + " Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_Titel)).getText().toString().trim();
//                    if (titel.isEmpty()) {
//                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_url)).getText().toString().trim();
//                    if (url.equals("") && !checked){
//                        CustomDialog.Builder(this)
//                        .setTitle("Ohne URL speichern?")
//                        .setName("Möchtest du wirklich ohne URL speichern?")
//                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
//                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 ->
//                                saveVideo(customDialog, video, titel, url, false, editVideo))
//                        .show();
//                    }
//                    else
                    if (editVideo[0].isWatchLater() && !editVideo[0].hasRating() && !Utility.boolOr(((RatingBar) customDialog.findViewById(R.id.customRating_ratingBar)).getRating(),-1f, 0f))
                        com.finn.androidUtilities.CustomDialog.Builder(this)
                                .setTitle("Ansicht Hinzufügen?")
                                .setText("Soll eine neue Ansicht zu dem Video hinzugefügt werden?")
                                .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.NO_BUTTON, customDialog1 -> saveVideo(customDialog, video, titel, url, checked[0], editVideo))
                                .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                    boolean before = editVideo[0].addDate(new Date(), true);
                                    editVideo[0].setWatchLater(false);
                                    checked[0] = false;
                                    Utility.showCenteredToast(this, "Ansicht Hinzugefügt" + (before ? "\n(Gestern)" : ""));
                                    saveVideo(customDialog, video, titel, url, checked[0], editVideo);
                                })
                                .enableColoredActionButtons()
                                .show();
                    else
                        saveVideo(customDialog, video, titel, url, checked[0], editVideo);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    final com.finn.androidUtilities.CustomDialog[] internetDialog = {null};
                    TextInputLayout dialog_editOrAddVideo_Titel_layout = view.findViewById(R.id.dialog_editOrAddVideo_Titel_layout);

                    TextInputLayout dialog_editOrAddVideo_Url_layout = view.findViewById(R.id.dialog_editOrAddVideo_url_layout);

                    view.findViewById(R.id.dialog_editOrAddVideo_internet).setOnClickListener(v -> {
                        final boolean[] isJsEnabled = {false};
                        if (internetDialog[0] == null) {
                            internetDialog[0] = com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setView(R.layout.dialog_video_internet)
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        webView = view1.findViewById(R.id.dialog_videoInternet_webView);
                                        String url = dialog_editOrAddVideo_Url_layout.getEditText().getText().toString();
                                        webView.loadUrl(!url.isEmpty() ? url : "https://www.google.de/");
                                        webView.setWebViewClient(new WebViewClient());
                                        WebSettings webSettings = webView.getSettings();
//                                        webSettings.setJavaScriptEnabled(true);
                                        webSettings.setBuiltInZoomControls(true);
//                                        view1.findViewById(R.id.dialog_videoInternet_toggleJavaScript).setOnClickListener(v1 -> {
//                                            isJsEnabled[0] = !isJsEnabled[0];
//                                            webSettings.setJavaScriptEnabled(isJsEnabled[0]);
//                                            if(isJsEnabled[0])
//                                                webView.reload();
////                                            ((FloatingActionButton) v1).setbati
//                                        });
                                        Runnable getSelection = () -> {
                                            webSettings.setJavaScriptEnabled(true);
                                            webView.evaluateJavascript("(function(){return window.getSelection().toString()})()", value -> {
                                                value = Utility.subString(value, 1, -1);
                                                if (!value.isEmpty()) {
                                                    Toast.makeText(VideoActivity.this, value, Toast.LENGTH_SHORT).show();
                                                    dialog_editOrAddVideo_Titel_layout.getEditText().setText(value);
                                                    if (Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_AUTO_SEARCH))
                                                        dialog_editOrAddVideo_Titel_layout.getEditText().onEditorAction(Helpers.TextInputHelper.IME_ACTION.SEARCH.getCode());
                                                    customDialog1.dismiss();
                                                }
                                            });
                                            webSettings.setJavaScriptEnabled(false);
                                        };

                                        View.OnContextClickListener listener = v1 -> {
                                            getSelection.run();
                                            customDialog1.dismiss();
                                            return true;
                                        };
                                        webView.setOnContextClickListener(listener);

                                        FloatingActionButton dialog_videoInternet_getSelection = view1.findViewById(R.id.dialog_videoInternet_getSelection);
                                        dialog_videoInternet_getSelection.setOnContextClickListener(listener);
                                        dialog_videoInternet_getSelection.setOnClickListener(v1 -> getSelection.run());
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
                                    .setOnDialogShown(customDialog1 -> isBrowserActive = true) // ToDo: umbenennen
                                    .setOnDialogDismiss(customDialog1 -> isBrowserActive = false)
                                    .show();
                        } else
                            internetDialog[0].show();
                    });

                    CheckBox dialog_editOrAddVideo_watchLater = customDialog.findViewById(R.id.dialog_editOrAddVideo_watchLater);
                    helper.defaultDialogValidation(customDialog).addValidator(dialog_editOrAddVideo_Titel_layout, dialog_editOrAddVideo_Url_layout)
                            .addActionListener(dialog_editOrAddVideo_Titel_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                apiRequest(text, customDialog, editVideo[0]);
                            }, Helpers.TextInputHelper.IME_ACTION.SEARCH)
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
                    View dialog_editOrAddVideo_title_label = customDialog.findViewById(R.id.dialog_editOrAddVideo_title_label);
                    if (video == null || isShared) {
                        helper.setValidation(dialog_editOrAddVideo_Titel_layout, (validator, text) -> {
                            if (database.videoMap.values().stream().anyMatch(video1 -> video1.getName().toLowerCase().equals(text.toLowerCase()))) {
                                dialog_editOrAddVideo_title_label.setClickable(true);
                                validator.setInvalid("Schon vorhanden! (Klicke auf das Label)");
                            }
                            else
                                dialog_editOrAddVideo_title_label.setClickable(false);
                        });
                    }
                    dialog_editOrAddVideo_title_label.setOnClickListener(v -> {
                        String text = dialog_editOrAddVideo_Titel_layout.getEditText().getText().toString().toLowerCase();
                        database.videoMap.values().stream().filter(video1 -> video1.getName().toLowerCase().equals(text)).findAny().ifPresent(video1 -> {
                            Runnable openEdit = () -> {
                                customDialog.dismiss();
                                isShared = false;
                                CustomDialog newDialog = showEditOrNewDialog(video1);
                                if (!Utility.stringExists(video1.getUrl())) {
                                    ((EditText) newDialog.findViewById(R.id.dialog_editOrAddVideo_url)).setText(dialog_editOrAddVideo_Url_layout.getEditText().getText().toString());
                                }
                            };

                            if (video1.getDateList().isEmpty())
                                openEdit.run();
                            else {
                                com.finn.androidUtilities.CustomDialog.Builder(this)
                                        .setTitle("Details Oder Bearbeiten")
                                        .setText("Mochtest du Details, oder Bearbeiten öffen?")
                                        .addButton("Details", customDialog1 -> {
                                            customDialog.dismiss();
                                            showDetailDialog(video1);
                                        })
                                        .addButton("Bearbeiten", customDialog1 -> openEdit.run())
                                        .enableExpandButtons()
                                        .show();
                            }
                        });
                    });

                    Helpers.RatingHelper ratingHelper = new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout));

                    if (editVideo[0] != null) {
                        AutoCompleteTextView dialog_editOrAddVideo_Titel = view.findViewById(R.id.dialog_editOrAddVideo_Titel);
                        dialog_editOrAddVideo_Titel.setText(editVideo[0].getName());
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
//                                        editVideo[0].setName(item.getName());
                                        dialog_editOrAddVideo_Titel.setText(item.getName());
                                    })
                                    .dismissOnClick()
                                    .show();
                        });

                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(
                                editVideo[0].getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Darsteller).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(
                                editVideo[0].getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Studio).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(
                                editVideo[0].getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Genre).setSelected(true);
                        if (!reload)
                            ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_url)).setText(editVideo[0].getUrl());
                        if (editVideo[0].getRelease() != null) {
                            ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAddVideo_datePicker)).setDate(editVideo[0].getRelease());
                        }

                        dialog_editOrAddVideo_watchLater.setVisibility(Utility.isUpcoming(editVideo[0].getRelease()) || (video != null && !video.getName().isEmpty()) ? View.GONE : View.VISIBLE);
                        int visibility = Utility.isUpcoming(editVideo[0].getRelease())/* && (video == null || !video.getName().isEmpty())*/ ? View.GONE : View.VISIBLE;
                        view.findViewById(R.id.dialog_editOrAddVideo_rating_layout).setVisibility(visibility);
                        view.findViewById(R.id.dialog_editOrAddVideo_url_allLayout).setVisibility(visibility);

                        ratingHelper.setRating(editVideo[0].getRating());
//                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(editVideo[0].getRating());
                    } else {
                        dialog_editOrAddVideo_watchLater.setVisibility(View.VISIBLE);
                        editVideo[0] = new Video("");
                    }

                    ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAddVideo_datePicker)).setOnDatePickListener(dateSelected -> {
                        editVideo[0].setRelease(dateSelected);
                        customDialog.reloadView();
                    });

                    if (!Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_SHOW_RELEASE))
                        view.findViewById(R.id.dialog_editOrAddVideo_datePicker_layout).setVisibility(View.GONE);


                    view.findViewById(R.id.dialog_editOrAddVideo_editActor).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog[0], editVideo[0] == null ? null : editVideo[0].getDarstellerList(), editVideo[0], CategoriesActivity.CATEGORIES.DARSTELLER));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog[0], editVideo[0] == null ? null : editVideo[0].getStudioList(), editVideo[0], CategoriesActivity.CATEGORIES.STUDIOS));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog[0], editVideo[0] == null ? null : editVideo[0].getGenreList(), editVideo[0], CategoriesActivity.CATEGORIES.GENRE));
                })
                .setOnDialogShown(customDialog -> {
                    Toast toast = Utility.centeredToast(this, "");
                    helper.interceptDialogActionForValidation(customDialog, () -> {
                        toast.setText("Warnung: " + helper.getMessage().get(0) + "\n(Doppel-Click zum Fortfahren)");
                        toast.show();
                    }, toast::cancel);
                })
                .setOnDialogDismiss(customDialog -> isShared = false)
                .show();
        return returnDialog;
    }

    private void apiRequest(String queue, CustomDialog customDialog, Video video) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/search/movie?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&query=" +
                queue +
                "&page=1&include_adult=true";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Toast.makeText(this, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();

        CustomList<Pair<String, JSONObject>> jsonObjectList = new CustomList<>();
        AutoCompleteTextView dialog_editOrAddVideo_Titel = customDialog.findViewById(R.id.dialog_editOrAddVideo_Titel);

        dialog_editOrAddVideo_Titel.setOnItemClickListener((parent, view2, position, id) -> {
            String text = ((ImageAdapterItem) parent.getItemAtPosition(position)).getText();
            JSONObject jsonObject = jsonObjectList.stream().filter(stringJSONObjectPair -> stringJSONObjectPair.first.equals(text)).findFirst().orElse(jsonObjectList.get(position)).second;

            try {
                video.setRelease(new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(jsonObject.getString("release_date"))).setTmdId(jsonObject.getInt("id")).setName(jsonObject.getString("original_title"));
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
                Map<Integer, String> idUuidMap = database.genreMap.values().stream().collect(Collectors.toMap(Genre::getTmdbGenreId, ParentClass::getUuid));

                CustomList uuidList = integerList.map((Function<Integer, Object>) idUuidMap::get).filter(Objects::nonNull);
                video.setGenreList(uuidList);
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

                dialog_editOrAddVideo_Titel.setAdapter(autoCompleteAdapter);
                dialog_editOrAddVideo_Titel.showDropDown();

            } catch (JSONException ignored) {
            }

        }, error -> Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);

    }

    private void saveVideo(CustomDialog dialog, Video video, String titel, String url, boolean checked, Video[] editVideo) {
        boolean neuesVideo = video == null || isShared;
        if (video == null)
            video = editVideo[0];

        if (video != editVideo[0])
            video.getChangesFrom(editVideo[0]);

        video.setName(titel);
//        videoNeu.setDarstellerList(editVideo[0].getDarstellerList());
//        videoNeu.setStudioList(editVideo[0].getStudioList());
//        videoNeu.setGenreList(editVideo[0].getGenreList());
        video.setUrl(url);
        video.setRating(((RatingBar) dialog.findViewById(R.id.customRating_ratingBar)).getRating());
//        videoNeu.setImagePath(editVideo[0].getImagePath());
        video.setRelease(((LazyDatePicker) dialog.findViewById(R.id.dialog_editOrAddVideo_datePicker)).getDate());

        boolean addedYesterday = false;
        boolean upcomming = false;
        if (checked)
            video.setWatchLater(true);
        else if (neuesVideo) {
            if (!(upcomming = video.isUpcomming()))
                addedYesterday = video.addDate(new Date(), true);
        }

        database.videoMap.put(video.getUuid(), video);
        reLoadVideoRecycler();
        dialog.dismiss();

        allVideoList = new ArrayList<>(database.videoMap.values());
        sortList(allVideoList);
        filterdVideoList = new ArrayList<>(allVideoList);
        commitSearch();

        Database.saveAll();

        Utility.showCenteredToast(this, singular + " gespeichert" + (addedYesterday ? "\nAutomatisch für gestern eingetragen" : upcomming ? "\n(Bevorstehend)" : ""));

        if (detailDialog != null)
            detailDialog.reloadView();

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

        if (mode.equals(MODE.ALL))
            menu.findItem(R.id.taskBar_video_modeAll).setChecked(true);
        else if (mode.equals(MODE.SEEN))
            menu.findItem(R.id.taskBar_video_modeSeen).setChecked(true);
        else if (mode.equals(MODE.LATER))
            menu.findItem(R.id.taskBar_video_modeLater).setChecked(true);
        else if (mode.equals(MODE.UPCOMING))
            menu.findItem(R.id.taskBar_video_modeUpcoming).setChecked(true);
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
                addOrEditDialog[0] = showEditOrNewDialog(null);
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
                finish();
                break;
        }
        return true;
    }

    private boolean commitSearch() {
        return textListener.onQueryTextChange(videos_search.getQuery().toString());
    }

    private void showRandomDialog() {
        CustomList<Video> randomList = new CustomList<>(filterdVideoList).filter(video -> !video.isUpcomming());
        if (randomList.isEmpty()) {
            Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
            return;
        }
        randomVideo = randomList.removeRandom();

        com.finn.androidUtilities.CustomDialog.Builder(this)
                .setTitle("Zufällig")
                .setView(R.layout.dialog_detail_video)
                .addButton("Öffnen", customDialog -> openUrl(randomVideo.getUrl(), false), false)
                .addButton("Nochmal", customDialog -> {
                    if (randomList.isEmpty()) {
                        Toast.makeText(this, "Kein neuer " + singular + " vorhanden", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomVideo = randomList.removeRandom();
                    customDialog.reloadView();
                }, false)
                .colorLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(
                            randomVideo.getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(
                            randomVideo.getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(
                            randomVideo.getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

                    if (randomVideo.getImagePath() != null && !randomVideo.getImagePath().isEmpty()) {
                        ImageView dialog_video_poster = view.findViewById(R.id.dialog_video_poster);
                        dialog_video_poster.setVisibility(View.VISIBLE);
                        Glide
                                .with(this)
                                .load("https://image.tmdb.org/t/p/w92/" + randomVideo.getImagePath())
                                .placeholder(R.drawable.ic_download)
                                .into(dialog_video_poster);

                        if (!reload) {
                            dialog_video_poster.setOnClickListener(v -> {
                                com.finn.androidUtilities.CustomDialog posterDialog = com.finn.androidUtilities.CustomDialog.Builder(this)
                                        .setView(R.layout.dialog_poster)
                                        .setSetViewContent((customDialog1, view1, reload1) -> {
                                            ImageView dialog_poster_poster = view1.findViewById(R.id.dialog_poster_poster);
                                            Glide
                                                    .with(this)
                                                    .load("https://image.tmdb.org/t/p/original/" + randomVideo.getImagePath())
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
                    } else
                        customDialog.findViewById(R.id.dialog_video_poster).setVisibility(View.GONE);

                })
                .show();

    }

    private void openUrl(String url, boolean select) {
        if (url == null || url.equals("")) {
            Toast.makeText(this, "Keine URL hinterlegt", Toast.LENGTH_SHORT).show();
            return;
        }
        Utility.openUrl(this, url, select);
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

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Database.saveAll();
        super.onDestroy();
    }

}
