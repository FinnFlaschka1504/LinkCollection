package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;
import com.mikhaellopez.lazydatepicker.LazyDatePicker;

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

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class VideoActivity extends AppCompatActivity {
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";
    public static final String UPCOMING_SEARCH = "UPCOMING_SEARCH";

    enum SORT_TYPE{
        NAME, VIEWS, RATING, LATEST
    }
    public enum FILTER_TYPE{
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
    private SearchView.OnQueryTextListener textListener;
    private boolean reverse = false;
    private String singular;
    private String plural;

    List<Video> allVideoList = new ArrayList<>();
    List<Video> filterdVideoList = new ArrayList<>();

    private CustomDialog[] addOrEditDialog = new CustomDialog[]{null};
    private CustomDialog detailDialog;

    private CustomRecycler customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        String stringExtra =Settings.getSingleSetting(this, Settings.SETTING_SPACE_NAMES_ + Settings.Space.SPACE_VIDEO) ;
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

            String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
            if (extraSearch != null) {
                videos_search.setQuery(extraSearch, true);
            }
        }

    }

    public static void showLaterMenu(AppCompatActivity activity, View view){
        if (!Database.isReady())
            return;
        CustomMenu.Builder(activity, view.findViewById(R.id.main_watchLaterCount_label))
                .setMenus((customMenu, items) -> {
                    items.add(new CustomMenu.MenuItem("Später ansehen", new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, WATCH_LATER_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_WATCH_LATER)));
                    items.add(new CustomMenu.MenuItem("Bevorstehende", new Pair<>(new Intent(activity, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, UPCOMING_SEARCH)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_UPCOMING)));
                })
                .setOnClickListener((customRecycler, itemView, item, index) -> {
                    Pair<Intent,Integer> pair = (Pair<Intent, Integer>) item.getContent();
                    activity.startActivityForResult(pair.first, pair.second);
                })
                .dismissOnClick()
                .show();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            for (Video video : database.videoMap.values()) {
                if (video.getDateList().isEmpty() && !video.isUpcomming() && !database.watchLaterList.contains(video.getUuid()))
                    database.watchLaterList.add(video.getUuid());
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

            Context that = this;
            videos_search = findViewById(R.id.search);
            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    filterdVideoList = new ArrayList<>(allVideoList);

                    if (!s.trim().equals("")) {
                        if (s.trim().equals(WATCH_LATER_SEARCH)) {
                            filterdVideoList = new ArrayList<>();
                            List<String> unableToFindList = new ArrayList<>();
                            for (String videoUuid : database.watchLaterList) {
                                Video video = database.videoMap.get(videoUuid);
                                if (video == null)
                                    unableToFindList.add(videoUuid);
                                else
                                    filterdVideoList.add(video);
                            }
                            if (!unableToFindList.isEmpty()) {
                                CustomDialog.Builder(that)
                                        .setTitle("Problem beim Laden der Liste!")
                                        .setText((unableToFindList.size() == 1 ? "Ein " + singular + " konnte" : unableToFindList.size() + " " + plural + " konnten") + " nicht gefunden werden")
                                        .setObjectExtra(unableToFindList)
                                        .addButton("Ignorieren", null)
                                        .addButton("Entfernen", customDialog -> {
                                            database.watchLaterList.removeAll(((ArrayList<String>) customDialog.getObjectExtra()));
                                            Toast.makeText(that, "Entfernt", Toast.LENGTH_SHORT).show();
                                            Database.saveAll();
                                            setResult(RESULT_OK);
                                        })
                                        .show();
                            }
                            reLoadVideoRecycler();
                            return true;
                        }
                        if (s.trim().equals(UPCOMING_SEARCH)) {
                            filterdVideoList = allVideoList.stream().filter(Video::isUpcomming).collect(Collectors.toList());
                            reLoadVideoRecycler();
                            return true;
                        }

                        for (String subQuery : s.split("\\|")) {
                            subQuery = subQuery.trim();
                            List<Video> subList = new ArrayList<>(filterdVideoList);
                            for (Video video : subList) {
                                if (!Utility.containedInVideo(subQuery, video, filterTypeSet))
                                    filterdVideoList.remove(video);
                            }
                        }
                    }
                    reLoadVideoRecycler();
                    return true;
                }
            };
            videos_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_ADD))
                showEditOrNewDialog(null);
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        }
        else
            whenLoaded.run();
    }

    private void sortList(List<Video> videoList) {
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
                    if (video1.getDateList().isEmpty() && video1.getDateList().isEmpty())
                        return video1.getName().compareTo(video2.getName());
                    else if (video1.getDateList().isEmpty())
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
    }

    private void loadVideoRecycler() {
        customRecycler_VideoList = new CustomRecycler<Video>(this, findViewById(R.id.videos_recycler))
                .setItemLayout(R.layout.list_item_video)
                .setObjectList(filterdVideoList)
                .setSetItemContent((itemView, video) -> {
                    itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE :View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());
                    if (!video.getDateList().isEmpty()) {
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));
                    } else
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);

                    itemView.findViewById(R.id.listItem_video_later).setVisibility(database.watchLaterList.contains(video.getUuid()) || video.isUpcomming() ? View.VISIBLE : View.GONE);

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    }
                    else
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
                .useCustomRipple()
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
                    detailDialog = showDetailDialog((Video) object);
                }, false)
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog(object);
                })
                .hideDivider()
                .generate();
    }

    private void reLoadVideoRecycler() {
        sortList(filterdVideoList);
//        customRecycler_VideoList.reload();
        customRecycler_VideoList.reload(filterdVideoList);
    }

    private CustomDialog showDetailDialog(Video video) {
        setResult(RESULT_OK);
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_video)
                .addButton("Bearbeiten", customDialog ->
                        addOrEditDialog[0] = showEditOrNewDialog(video), false)
                .addButton("Öffnen mit...", customDialog -> openUrl(video.getUrl(), true), false)
                .setSetViewContent((customDialog, view) -> {
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
                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(String.valueOf(video.getDateList().size()));
                    ((TextView) view.findViewById(R.id.dialog_video_release)).setText(video.getRelease() != null ? new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(video.getRelease()) : "");
                    ((RatingBar) view.findViewById(R.id.dialog_video_rating)).setRating(video.getRating());

                    final boolean[] isInWatchLater = {database.watchLaterList.contains(video.getUuid())};
                    view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
                    view.findViewById(R.id.dialog_video_watchLater).setOnClickListener(view1 -> {
                        isInWatchLater[0] = !isInWatchLater[0];
                        view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
                        if (isInWatchLater[0]) {
                            database.watchLaterList.add(video.getUuid());
                            Toast.makeText(this, "Zu 'Später-Ansehen' hinzugefügt", Toast.LENGTH_SHORT).show();
                        } else {
                            database.watchLaterList.remove(video.getUuid());
                            Toast.makeText(this, "Aus 'Später-Ansehen' entfernt", Toast.LENGTH_SHORT).show();
                        }
                        textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                        setResult(RESULT_OK);
                        Database.saveAll();
                    });

                    view.findViewById(R.id.dialog_video_editViews).setOnClickListener(view1 ->
                            showCalenderDialog(Arrays.asList(video), detailDialog));
                })
                .setOnDialogDismiss(customDialog -> detailDialog = null);
        returnDialog.show();
        return returnDialog;
    }

    public void showCalenderDialog(List<Video> videoList, CustomDialog detailDialog) {
        CustomDialog.Builder(this)
                .setTitle("Ansichten Bearbeiten")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupCalender(this, calendarView, ((FrameLayout) view), videoList, false);
                })
                .disableScroll()
                .setDimensions(true, true)
                .setOnDialogDismiss(customDialog -> {
                    ((TextView) detailDialog.findViewById(R.id.dialog_video_views))
                            .setText(String.valueOf(videoList.get(0).getDateList().size()));
                    this.reLoadVideoRecycler();
                })
                .show();

    }


    private CustomDialog showEditOrNewDialog(Object object) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Video[] video = {(Video) object};
        if (video[0] != null) {
            video[0] = ((Video) object).cloneVideo();
        }
        CustomDialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(object == null ? "Neu: " + singular : singular + " Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddVideo_Url)).getText().toString().trim();
                    boolean checked = ((CheckBox) customDialog.findViewById(R.id.dialog_editOrAddVideo_watchLater)).isChecked();

                    if (url.equals("") && !checked){
                        CustomDialog.Builder(this)
                        .setTitle("Ohne URL speichern?")
                        .setText("Möchtest du wirklich ohne URL speichern?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 ->
                                saveVideo(customDialog, object, titel, url, false, video))
                        .show();
                    }
                    else
                        saveVideo(customDialog, object, titel, url, checked, video);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view) -> {
                    TextInputLayout dialog_editOrAddVideo_Titel_layout = view.findViewById(R.id.dialog_editOrAddVideo_Titel_layout);
                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(dialog_editOrAddVideo_Titel_layout)
                            .addActionListener(dialog_editOrAddVideo_Titel_layout, (textInputHelper, textInputLayout, actionId, text) -> {
                                apiRequest(text, customDialog, video[0]);
                            }, Helpers.TextInputHelper.IME_ACTION.SEARCH);
                    if (video[0] != null) {
                        ((AutoCompleteTextView) view.findViewById(R.id.dialog_editOrAddVideo_Titel)).setText(video[0].getName());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(
                                video[0].getDarstellerList().stream().map(uuid -> database.darstellerMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Darsteller).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(
                                video[0].getStudioList().stream().map(uuid -> database.studioMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Studio).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(
                                video[0].getGenreList().stream().map(uuid -> database.genreMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAddVideo_Genre).setSelected(true);
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Url)).setText(video[0].getUrl());
                        if (video[0].getRelease() != null)
                            ((LazyDatePicker) view.findViewById(R.id.dialog_editOrAddVideo_datePicker)).setDate(video[0].getRelease());
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(video[0].getRating());
                    }
                    else {
                        view.findViewById(R.id.dialog_editOrAddVideo_watchLater).setVisibility(View.VISIBLE);
                        video[0] = new Video();
                    }


                    view.findViewById(R.id.dialog_editOrAddVideo_editActor).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, video[0] == null ? null : video[0].getDarstellerList(), video[0], CategoriesActivity.CATEGORIES.DARSTELLER));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, video[0] == null ? null : video[0].getStudioList(), video[0], CategoriesActivity.CATEGORIES.STUDIOS ));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog, video[0] == null ? null : video[0].getGenreList(), video[0], CategoriesActivity.CATEGORIES.GENRE));
                })
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
//        try {
//            JSONObject jsonObject = new JSONObject("{\"page\":1,\"total_results\":16,\"total_pages\":1,\"results\":[{\"popularity\":46.912,\"id\":299534,\"video\":false,\"vote_count\":10216,\"vote_average\":8.3,\"title\":\"Avengers: Endgame\",\"release_date\":\"2019-04-24\",\"original_language\":\"en\",\"original_title\":\"Avengers: Endgame\",\"genre_ids\":[12,878,28],\"backdrop_path\":\"\\/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg\",\"adult\":false,\"overview\":\"Thanos hat also tatsächlich Wort gehalten, seinen Plan in die Tat umgesetzt und die Hälfte allen Lebens im Universum ausgelöscht. Die Avengers? Machtlos. Iron Man und Nebula sitzen auf dem Planeten Titan fest, während auf der Erde absolutes Chaos herrscht. Doch dann finden Captain America und die anderen überlebenden Helden auf der Erde heraus, dass Nick Fury vor den verheerenden Ereignissen gerade noch ein Notsignal absetzen konnte, um Verstärkung auf den Plan zu rufen. Die Superhelden-Gemeinschaft bekommt mit Captain Marvel kurzerhand tatkräftige Unterstützung im Kampf gegen ihren vermeintlich übermächtigen Widersacher. Und dann ist da auch noch Ant-Man, der wie aus dem Nichts auftaucht und sich der Truppe erneut anschließt, um die ganze Sache womöglich doch noch zu einem guten Ende zu bringen...\",\"poster_path\":\"\\/mrh5A3uIE9wDDzPSiBe70YSHvrK.jpg\"},{\"popularity\":8.698,\"vote_count\":190,\"video\":false,\"poster_path\":\"\\/fmDt68Pj9OiR3pfvNL9qkuhKvi2.jpg\",\"id\":12211,\"adult\":false,\"backdrop_path\":\"\\/xYdzbB3TksChZLuZ9qq7iq5fFux.jpg\",\"original_language\":\"en\",\"original_title\":\"Highlander: Endgame\",\"genre_ids\":[28,14],\"title\":\"Highlander: Endgame\",\"vote_average\":4.6,\"overview\":\"Heute: Auf der Suche nach seinem Vetter Connor MacLeod findet der unsterbliche Highlander Duncan MacLeod das geheime Refugium. Hier werden Unsterbliche, die des Lebens müde geworden sind, in einen künstlichen Schlaf versetzt. Das Refugium ist auch dafür da, dass der Kampf der Unsterblichen um die Weltherrschaft nie ein Ende finden kann, denn solange es noch mehr als einen gibt ist nichts entschieden. Auch darum ist der Standort des Refugiums geheim. Doch der kaltblütigste Unsterbliche hat es gefunden, zerstört, seine \\\"Einwohner\\\" geköpft. Nur Connor MacLeod hat er laufen lassen, nicht um ihn zu verschonen, sondern um sich an ihm zu rächen...\",\"release_date\":\"2000-09-01\"},{\"popularity\":3.429,\"id\":15102,\"video\":false,\"vote_count\":24,\"vote_average\":6.2,\"title\":\"Endgame\",\"release_date\":\"2009-01-18\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[80,18],\"backdrop_path\":\"\\/mVW8eCgXuWKHcBL9tRxboHtKNi.jpg\",\"adult\":false,\"overview\":\"Im Jahr 1985 hatten viele jede Hoffnung auf Frieden in Südafrika fast aufgegeben. Das weiße Apartheid-Regime wurde zunehmend brutaler bei den Versuchen, die schwarze Bevölkerung zu kontrollieren. Die Widerstandsbewegung wurde als Folge der Weigerung der Regierung, Nelson Mandela, Botschafter der friedlichen Opposition frei zu lassen zunehmend militant und militärisch. Das System der Apartheid war offensichtlich am Ende. Alles, was es noch mit dessen Vertretern zu besprechen gab, waren die Bedingungen, unter denen die Apartheid ein für alle Mal begraben sein würde.\",\"poster_path\":\"\\/btRzeSEX2KptiBFvfzXF3amutDK.jpg\"},{\"popularity\":8.098,\"id\":41135,\"video\":false,\"vote_count\":98,\"vote_average\":5.2,\"title\":\"Operation: Endgame\",\"release_date\":\"2010-07-20\",\"original_language\":\"en\",\"original_title\":\"Operation: Endgame\",\"genre_ids\":[12,28,35,53],\"backdrop_path\":\"\\/3bTXS5nwn1Qy49NZbvLIUZ0a5xo.jpg\",\"adult\":false,\"overview\":\"Zwei rivalisierende Teams von Regierungs-Agenten werden in einer geheimen Anlage trainert. Die Killer werden alle mit Decknamen aus einem Tarot-Kartenset benannt. Als “Der Narr” an seinem ersten Tag dort ankommt, stellt sich gleich schon heraus, dass der Boss unter mysteriösen Umständen umgebracht wurde. Nun muss der Mörder gefunden werden, bevor der ganze Laden hochgeht.\",\"poster_path\":\"\\/qYEcPpNbqSjlVF5qJIj2AmbFapp.jpg\"},{\"popularity\":7.312,\"id\":400605,\"video\":false,\"vote_count\":94,\"vote_average\":4.9,\"title\":\"Dead Rising: Endgame\",\"release_date\":\"2016-06-20\",\"original_language\":\"en\",\"original_title\":\"Dead Rising: Endgame\",\"genre_ids\":[28,27],\"backdrop_path\":\"\\/23KUOxHUD9oI0eesmgFNWWOnMcV.jpg\",\"adult\":false,\"overview\":\"Nachdem er den blutrünstigen Zombie-Horden von East Mission City entkommen ist, setzt der investigative Reporter Chase Carter alles daran, die Regierungsverschwörung öffentlich zu machen, die überhaupt erst zur Verbreitung des Zombie-Virus‘ geführt hat. Doch dazu muss er in die abgeriegelte und todbringende Quarantänezone zurückkehren. Dort bekommt er es aber nicht nur mit den gefährlichen Untoten, sondern auch mit dem gnadenlosen General Lyons zu tun, der die Beteiligung der Regierung und des US-Militärs an der Zombie-Seuche um jeden Preis vertuschen will und dafür auch über Leichen geht. Gemeinsam mit seinen Verbündeten muss sich Chase einmal mehr seinen blutigen Weg durch Zombies und Lügen bahnen, um schließlich die Wahrheit ans Licht zu bringen.\",\"poster_path\":\"\\/yWp0xCKFl2IGBsvyYR7GmI7VUdE.jpg\"},{\"popularity\":2.303,\"id\":51491,\"video\":false,\"vote_count\":8,\"vote_average\":4.8,\"title\":\"Endgame\",\"release_date\":\"2001-01-01\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[80,18,53],\"backdrop_path\":\"\\/9ZscJdBhYb5RWBG0qsJD3Cy07oH.jpg\",\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/52zpNbVVQpWem8rqHYaMWQjLB34.jpg\"},{\"popularity\":2.898,\"id\":28850,\"video\":false,\"vote_count\":15,\"vote_average\":5.3,\"title\":\"Endgame - Das letzte Spiel mit dem Tod\",\"release_date\":\"1983-11-05\",\"original_language\":\"it\",\"original_title\":\"Endgame - Bronx lotta finale\",\"genre_ids\":[878],\"backdrop_path\":\"\\/1jO21YbiYN7J4znNLZhgzdYmDcY.jpg\",\"adult\":false,\"overview\":\"Nach einem Atomkrieg ist die Erde verwüstet. Durch die Strahlung ist eine neue Menschenrasse entstanden: die Mutanten. Diese werden von den „normalen“ Menschen erbarmungslos gejagt. Um das Volk bei Laune zu halten, haben die neuen Diktatoren das „Endgame“ geschaffen, einen blutigen Gladiatorenkampf, bei dem nur der Stärkste überlebt. Bei einem dieser Kämpfe trifft der Favorit und mehrfache Sieger Shannon auf die hübsche Mutantin Lilith, die ihn bittet, sie und ihre Freunde aus der Stadt zu bringen. Nach anfänglichem Zögern willigt Shannon ein, denn Lilith verspricht ihm 50 Kg Gold als Belohnung. Zusammen mit einigen anderen unerschrockenen Kämpfern macht sich der Trupp auf den gefahrvollen Weg aus der Stadt.\",\"poster_path\":\"\\/97CnaUf0w8cXY4hkLyoYQzSVoF5.jpg\"},{\"popularity\":1.296,\"id\":376651,\"video\":false,\"vote_count\":4,\"vote_average\":5.8,\"title\":\"Endgame\",\"release_date\":\"2015-09-25\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[18],\"backdrop_path\":\"\\/cTROgsyfGK4IhMgsMa1YmzeinpC.jpg\",\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/lU76TBQbYMFKinr8eNi1cS2zLI6.jpg\"},{\"popularity\":0.6,\"id\":233407,\"video\":false,\"vote_count\":0,\"vote_average\":0,\"title\":\"Endgame\",\"release_date\":\"1989-01-01\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[],\"backdrop_path\":null,\"adult\":false,\"overview\":\"\",\"poster_path\":null},{\"popularity\":0.981,\"id\":68139,\"video\":false,\"vote_count\":3,\"vote_average\":6.7,\"title\":\"Endgame\",\"release_date\":\"2000-09-10\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[35,18,10770],\"backdrop_path\":\"\\/z0gor69sJrkAi60NUCtAVR6QRlG.jpg\",\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/fyIdFwCFnUhe6O9H5gXo0ksiRvr.jpg\"},{\"popularity\":3.943,\"id\":18312,\"video\":false,\"vote_count\":17,\"vote_average\":6.9,\"title\":\"Endgame: Blueprint for Global Enslavement\",\"release_date\":\"2007-11-01\",\"original_language\":\"en\",\"original_title\":\"Endgame: Blueprint for Global Enslavement\",\"genre_ids\":[99],\"backdrop_path\":\"\\/t4WxWKzEQ4L6g86XmIy4oNqtDBi.jpg\",\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/4NI59KbXWE0AaefHD8CG9KyuTD1.jpg\"},{\"popularity\":1.037,\"id\":353227,\"video\":false,\"vote_count\":2,\"vote_average\":1,\"title\":\"Horror 102: Endgame\",\"release_date\":\"2004-09-04\",\"original_language\":\"en\",\"original_title\":\"Horror 102: Endgame\",\"genre_ids\":[27],\"backdrop_path\":null,\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/fvPYC0pqmTr94JfZn9Vs7taFdkI.jpg\"},{\"popularity\":0.6,\"id\":401828,\"video\":false,\"vote_count\":0,\"vote_average\":0,\"title\":\"Endgame\",\"release_date\":\"2015-01-01\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[16],\"backdrop_path\":null,\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/jntVd28HefooS3ykVMADPAWgY8Q.jpg\"},{\"popularity\":0.6,\"id\":320200,\"video\":false,\"vote_count\":2,\"vote_average\":7,\"title\":\"Endgame\",\"release_date\":\"1999-12-01\",\"original_language\":\"en\",\"original_title\":\"Endgame\",\"genre_ids\":[],\"backdrop_path\":null,\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/bcGuIgKwafDNfbUIaawuug7o1CM.jpg\"},{\"popularity\":0.6,\"id\":545817,\"video\":false,\"vote_count\":0,\"vote_average\":0,\"title\":\"Britten's Endgame\",\"release_date\":\"2015-01-01\",\"original_language\":\"en\",\"original_title\":\"Britten's Endgame\",\"genre_ids\":[99,10402],\"backdrop_path\":null,\"adult\":false,\"overview\":\"\",\"poster_path\":\"\\/j94qqELDTsOxYxXZOpNKYWhwzdh.jpg\"},{\"popularity\":5.87,\"id\":256740,\"video\":false,\"vote_count\":54,\"vote_average\":5.6,\"title\":\"Wicked Blood\",\"release_date\":\"2014-03-04\",\"original_language\":\"en\",\"original_title\":\"Wicked Blood\",\"genre_ids\":[28,18,53],\"backdrop_path\":\"\\/se6pj7NARpwviPNjPrrtyIxINvV.jpg\",\"adult\":false,\"overview\":\"Für Hannah Lee ist das kriminelle Baton Rouge alltägliche Umgebung. Dennoch ist auch sie nicht vor Gefahr gefeit, als ihre ältere Schwester Amber sich ausgerechnet in Wild Bill verliebt, einen Konkurrent des rücksichtslosen Onkels Frank. Und so muss Hannah noch so manche bittere Erfahrung in Sachen Blutsbande machen…\",\"poster_path\":\"\\/dR4gPntr3aQEl9jUvg7WsAuYqr.jpg\"}]}");
//            JSONArray results = jsonObject.getJSONArray("results");
//            for (int i = 0; i < results.length(); i++) {
//                JSONObject object = results.getJSONObject(i);
//
//                jsonObjectList.add(new Pair<>(object.getString("title"), object));
//
////                new Video(object.getString("title"))
////                        .setRelease(new SimpleDateFormat("yyyy-MM-dd").parse(object.getString("release_date")))
//            }
//        } catch (JSONException ignored) {
//        }
        AutoCompleteTextView dialog_editOrAddVideo_Titel = customDialog.findViewById(R.id.dialog_editOrAddVideo_Titel);

        dialog_editOrAddVideo_Titel.setOnItemClickListener((parent, view2, position, id) -> {
            JSONObject jsonObject = jsonObjectList.get(position).second;
            try {
                video.setRelease(new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(jsonObject.getString("release_date"))).setName(jsonObject.getString("original_title"));
                JSONArray genre_ids = jsonObject.getJSONArray("genre_ids");
                CustomList<Integer> integerList = new CustomList<>();
                for (int i = 0; i < genre_ids.length(); i++) {
                    integerList.add(genre_ids.getInt(i));
                }
                Map<Integer,String> idUuidMap = database.genreMap.values().stream().collect(Collectors.toMap(Genre::getTmdbGenreId, ParentClass::getUuid));

                CustomList uuidList = integerList.map((Function<Integer, Object>) idUuidMap::get).filter(Objects::nonNull);
                video.setGenreList(uuidList);
                String BREAKPOINT = null;
                customDialog.reloadView();
            } catch (JSONException | ParseException ignored) {
            }
        });

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);

                    String release = object.getString("release_date");
                    if (!release.isEmpty())
                        release= String.format(" (%s)", release.substring(0, 4));
                    jsonObjectList.add(new Pair<>(object.getString("original_title") + release, object));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jsonObjectList
                        .map(stringJSONObjectPair -> stringJSONObjectPair.first));

                dialog_editOrAddVideo_Titel.setAdapter(adapter);
                dialog_editOrAddVideo_Titel.showDropDown();
            } catch (JSONException ignored) {
            }

        }, error -> {
            Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);

    }

    private void saveVideo(CustomDialog dialog, Object object, String titel, String url, boolean checked, Video[] video) {
        boolean neuesVideo = object == null;
        Video videoNeu;
        if (object == null)
            videoNeu = new Video(titel);
        else
            videoNeu = database.videoMap.get(((Video) object).getUuid());

        videoNeu.setName(titel);
        videoNeu.setDarstellerList(video[0].getDarstellerList());
        videoNeu.setStudioList(video[0].getStudioList());
        videoNeu.setGenreList(video[0].getGenreList());
        videoNeu.setUrl(url);
        videoNeu.setRating(((RatingBar) dialog.findViewById(R.id.dialog_editOrAddVideo_rating)).getRating());
        videoNeu.setRelease(((LazyDatePicker) dialog.findViewById(R.id.dialog_editOrAddVideo_datePicker)).getDate());

        boolean addedYesterday = false;
        boolean upcomming = false;
        if (checked)
            database.watchLaterList.add(videoNeu.getUuid());
        else if (neuesVideo) {
            if (!(upcomming = videoNeu.isUpcomming()))
                addedYesterday = videoNeu.addDate(new Date(), true);
        }

        database.videoMap.put(videoNeu.getUuid(), videoNeu);
        reLoadVideoRecycler();
        dialog.dismiss();

        allVideoList = new ArrayList<>(database.videoMap.values());
        sortList(allVideoList);
        filterdVideoList = new ArrayList<>(allVideoList);
        textListener.onQueryTextChange(videos_search.getQuery().toString());

        Database.saveAll();

        Utility.showCenterdToast(this, singular + " gespeichert" + (addedYesterday ? "\nAutomatisch für gestern eingetragen" : upcomming ? "\n(Bevorstehend)" : ""));

        if (detailDialog != null)
            detailDialog.reloadView();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_video, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_video_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_video_filterByDarsteller)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.ACTOR));
        subMenu.findItem(R.id.taskBar_video_filterByStudio)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.STUDIO));
        subMenu.findItem(R.id.taskBar_video_filterByGenre)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_video_filterByDarsteller:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.ACTOR);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.ACTOR);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_video_filterByGenre:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.GENRE);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.GENRE);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_video_filterByStudio:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.STUDIO);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.STUDIO);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(videos_search.getQuery().toString());
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

    private void showRandomDialog() {
        if (filterdVideoList.isEmpty()) {
            Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
            return;
        }
        randomVideo = filterdVideoList.get((int) (Math.random() * filterdVideoList.size()));
        List<String> darstellerNames = new ArrayList<>();
        randomVideo.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
        List<String> studioNames = new ArrayList<>();
        randomVideo.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
        List<String> genreNames = new ArrayList<>();
        randomVideo.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));

        CustomDialog.Builder(this)
                .setTitle("Zufällig")
                .setView(R.layout.dialog_detail_video)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Nochmal", customDialog -> {
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomVideo = filterdVideoList.get((int) (Math.random() * filterdVideoList.size()));
                    ((TextView) customDialog.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getName());

                    List<String> darstellerNames_neu = new ArrayList<>();
                    randomVideo.getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) customDialog.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames_neu));

                    List<String> studioNames_neu = new ArrayList<>();
                    randomVideo.getStudioList().forEach(uuid -> studioNames_neu.add(database.studioMap.get(uuid).getName()));
                    ((TextView) customDialog.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames_neu));

                    List<String> genreNames_neu = new ArrayList<>();
                    randomVideo.getGenreList().forEach(uuid -> genreNames_neu.add(database.genreMap.get(uuid).getName()));
                    ((TextView) customDialog.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames_neu));

                }, false)
                .addButton("Öffnen", customDialog -> openUrl(randomVideo.getUrl(), false), false)
                .setSetViewContent((customDialog, view) -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

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
