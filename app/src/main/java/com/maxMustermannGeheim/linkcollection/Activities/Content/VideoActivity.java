package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class VideoActivity extends AppCompatActivity {
    public static final String WATCH_LATER_SEARCH = "WATCH_LATER_SEARCH";

    enum SORT_TYPE{
        NAME, VIEWS, RATING, LATEST
    }
    public enum FILTER_TYPE{
        NAME, ACTOR, GENRE, STUDIO
    }
    Database database;
    SharedPreferences mySPR_daten;
    private boolean delete = false;
    private List<String> toDelete = new ArrayList<>();
    Video randomVideo;
    private boolean scrolling = true;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.ACTOR, FILTER_TYPE.GENRE, FILTER_TYPE.STUDIO));
    SearchView.OnQueryTextListener textListener;
    private boolean reverse = false;

    List<Video> allVideoList = new ArrayList<>();
    List<Video> filterdVideoList = new ArrayList<>();

    private Dialog[] addOrEditDialog = new Dialog[]{null};
    private Pair<Dialog, Video> dialogVideoPair;

    private CustomRecycler customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
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
                Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " Video" : " Videos") + " gelöscht", Toast.LENGTH_SHORT).show();
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
                                        .setText((unableToFindList.size() == 1 ? "Ein Video konnte" : unableToFindList.size() + " Videos konnten") + " nicht gefunden werden")
                                        .setObjectExtra(unableToFindList)
                                        .setButtonType(CustomDialog.ButtonType.CUSTOM)
                                        .addButton("Ignorieren", (customDialog, dialog) -> {})
                                        .addButton("Entfernen", (customDialog, dialog) -> {
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
        customRecycler_VideoList = CustomRecycler.Builder(this, findViewById(R.id.videos_recycler))
                .setItemLayout(R.layout.list_item_video)
                .setObjectList(filterdVideoList)
                .setSetItemContent((CustomRecycler.SetItemContent<Video>) (itemView, video) -> {
                    itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE :View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());
                    if (!video.getDateList().isEmpty()) {
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));
                    } else
                        itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);

                    itemView.findViewById(R.id.listItem_video_later).setVisibility(database.watchLaterList.contains(video.getUuid()) ? View.VISIBLE : View.GONE);

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
                        openUrl(((Video) object).getUrl(), false);
                    } else {
                        CheckBox checkBox = view.findViewById(R.id.listItem_video_deleteCheck);
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked())
                            toDelete.add(((Video) object).getUuid());
                        else
                            toDelete.remove(((Video) object).getUuid());
                        String test = null;
                    }
                })
                .addSubOnClickListener(R.id.listItem_video_details, (customRecycler, view, object, index) -> {
                    dialogVideoPair = new Pair<>(showDetailDialog(object), (Video) object);
                }, false)
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog(object);
                })
                .hideDivider()
                .generateCustomRecycler();
    }

    private void reLoadVideoRecycler() {
        sortList(filterdVideoList);
        customRecycler_VideoList.setObjectList(filterdVideoList).reload();
    }

    private Dialog showDetailDialog(Object object) {
        setResult(RESULT_OK);
        Video video = (Video) object;
        List<String> darstellerNames = new ArrayList<>();
        video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
        List<String> studioNames = new ArrayList<>();
        video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
        List<String> genreNames = new ArrayList<>();
        video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
        Dialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", (customDialog, dialog) ->
                        addOrEditDialog[0] = showEditOrNewDialog(object), false)
                .addButton("Öffnen mit...", (customDialog, dialog) -> openUrl(((Video) object).getUrl(), true), false)
                .setSetViewContent((customDialog, view) -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(video.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
                    view.findViewById(R.id.dialog_video_Studio).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
                    view.findViewById(R.id.dialog_video_Genre).setSelected(true);
                    view.findViewById(R.id.dialog_video_details).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.dialog_video_Url)).setText(video.getUrl());
                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(String.valueOf(video.getDateList().size()));
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
                            showCalenderDialog(Arrays.asList(video), dialogVideoPair));
                })
                .show();
        returnDialog.setOnDismissListener(dialogInterface -> dialogVideoPair = null);
        return returnDialog;
    }

    public void showCalenderDialog(List<Video> videoList, Pair<Dialog, Video> dialogVideoPair) {
        CustomDialog.Builder(this)
                .setTitle("Ansichten Bearbeiten")
                .setView(R.layout.dialog_edit_views)
                .setSetViewContent((customDialog, view) -> {
                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
                    stub_groups.setLayoutResource(R.layout.fragment_calender);
                    stub_groups.inflate();
                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
                    Utility.setupCalender(this, calendarView, ((LinearLayout) view), videoList, false);
                })
                .show().setOnDismissListener(dialogInterface -> {
                    ((TextView) dialogVideoPair.first.findViewById(R.id.dialog_video_views))
                            .setText(String.valueOf(videoList.get(0).getDateList().size()));
                    this.reLoadVideoRecycler();
                });

    }


    private Dialog showEditOrNewDialog(Object object) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Video[] video = {(Video) object};
        List<String> darstellerNames = new ArrayList<>();
        List<String> studioNames = new ArrayList<>();
        List<String> genreNames = new ArrayList<>();
        if (video[0] != null) {
            video[0] = ((Video) object).cloneVideo();
            video[0].getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
            video[0].getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
            video[0].getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
        }
        Dialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(object == null ? "Neues Video" : "Video Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.SAVE_BUTTON, (customDialog, dialog) -> {
                    String titel = ((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String url = ((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_Url)).getText().toString().trim();
                    boolean checked = ((CheckBox) dialog.findViewById(R.id.dialog_editOrAddVideo_watchLater)).isChecked();

                    if (url.equals("") && !checked){
                        CustomDialog.Builder(this)
                        .setTitle("Ohne URL speichern?")
                        .setText("Möchtest du wirklich das Video ohne URL speichern")
                        .setButtonType(CustomDialog.ButtonType.YES_NO)
                        .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog1) ->
                                saveVideo(dialog, object, titel, url, false, video))
                        .show();
                    }
                    else
                        saveVideo(dialog, object, titel, url, checked, video);

                }, false)
                .setSetViewContent((customDialog, view) -> {
                    if (video[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Titel)).setText(video[0].getName());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", darstellerNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Darsteller).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", studioNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Studio).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", genreNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Genre).setSelected(true);
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Url)).setText(video[0].getUrl());
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
//        DialogInterface.OnKeyListener keylistener = (dialog, keyCode, KEvent) -> {
//            int keyaction = KEvent.getAction();
//
//            if(keyaction == KeyEvent.ACTION_DOWN)
//            {
//                int keycode = KEvent.getKeyCode();
//                int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
//                char character = (char) keyunicode;
////                    if(keycode== KeyCode.Enter){
////
////
////                    }
//            }
//            return false;
//        };
//        returnDialog.setOnKeyListener(keylistener);
        return returnDialog;
    }

    private void saveVideo(Dialog dialog, Object object, String titel, String url, boolean checked, Video[] video) {
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

        boolean addedYesterday = false;
        if (checked)
            database.watchLaterList.add(videoNeu.getUuid());
        else if (neuesVideo)
            addedYesterday = videoNeu.addDate(new Date(), true);

        database.videoMap.put(videoNeu.getUuid(), videoNeu);
        reLoadVideoRecycler();
        dialog.dismiss();

        allVideoList = new ArrayList<>(database.videoMap.values());
        sortList(allVideoList);
        filterdVideoList = new ArrayList<>(allVideoList);
        textListener.onQueryTextChange(videos_search.getQuery().toString());

        Database.saveAll();

        Utility.showCenterdToast(this, "Video gespeichert" + (addedYesterday ? "\nAutomatisch für gestern eingetragen" : ""));

        if (dialogVideoPair == null)
            return;

        dialogVideoPair.first.dismiss();
        dialogVideoPair = new Pair<>(showDetailDialog(object), dialogVideoPair.second);

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
                    Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
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
                .setTitle("Zufälliges Video")
                .setView(R.layout.dialog_detail_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", (customDialog, dialog) -> {
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomVideo = filterdVideoList.get((int) (Math.random() * filterdVideoList.size()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getName());

                    List<String> darstellerNames_neu = new ArrayList<>();
                    randomVideo.getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames_neu));

                    List<String> studioNames_neu = new ArrayList<>();
                    randomVideo.getStudioList().forEach(uuid -> studioNames_neu.add(database.studioMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames_neu));

                    List<String> genreNames_neu = new ArrayList<>();
                    randomVideo.getGenreList().forEach(uuid -> genreNames_neu.add(database.genreMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames_neu));

                }, false)
                .addButton("Öffnen", (customDialog, dialog) -> openUrl(randomVideo.getUrl(), false), false)
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
