package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
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
import com.maxMustermannGeheim.linkcollection.Daten.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.DatenObjekt;
import com.maxMustermannGeheim.linkcollection.Daten.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
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
import java.util.Map;

import static com.maxMustermannGeheim.linkcollection.Activitys.MainActivity.SHARED_PREFERENCES_NAME;

public class VideoActivity extends AppCompatActivity {
    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";
    public static final String EXTRA_SEARCH_CATIGORY = "EXTRA_SEARCH_CATOGORY";
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

    private Pair<Dialog, Video> addDialogVideoPair;
    private Pair<Dialog, Video> editDialogVideoPair;
    private Pair<Dialog, Video> dialogVideoPair;

    private CustomRecycler customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;
    private SearchView videos_search;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ToDo: nach löschen liste neu laden und evl. auch aus datenbank löschen

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        database = Database.getInstance();
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);

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

            Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " Video" : " Videos") + " gelöscht", Toast.LENGTH_SHORT).show();
        });
        loadVideoRecycler();

        videos_search = findViewById(R.id.videos_search);
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
                        for (String videoUuid : database.watchLaterList) {
                            filterdVideoList.add(database.videoMap.get(videoUuid));
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

        String extraSearchCatigory = getIntent().getStringExtra(EXTRA_SEARCH_CATIGORY);
        if (extraSearchCatigory != null) {
            filterTypeSet.clear();

            if (extraSearchCatigory.equals(MainActivity.CATIGORYS.Darsteller.name())) {
                filterTypeSet.add(FILTER_TYPE.ACTOR);
            } else if (extraSearchCatigory.equals(MainActivity.CATIGORYS.Genre.name())) {
                filterTypeSet.add(FILTER_TYPE.GENRE);
            } else if (extraSearchCatigory.equals(MainActivity.CATIGORYS.Studios.name())) {
                filterTypeSet.add(FILTER_TYPE.STUDIO);
            }
//            else if (extraSearchCatigory.equals(MainActivity.CATIGORYS.Video.name())) {
//                filterTypeSet.add(FILTER_TYPE.NAME);
//            }

            String extraSearch = getIntent().getStringExtra(EXTRA_SEARCH);
            if (extraSearch != null) {
                videos_search.setQuery(extraSearch, true);
            }
        }
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
//                .setViewList(viewIdList -> {
//                    viewIdList.add(R.id.listItem_video_deleteCheck);
//                    viewIdList.add(R.id.listItem_video_Titel);
//                    viewIdList.add(R.id.listItem_video_Views);
//                    viewIdList.add(R.id.listItem_video_Darsteller);
//                    viewIdList.add(R.id.listItem_video_Studio);
//                    viewIdList.add(R.id.listItem_video_Genre);
//                    viewIdList.add(R.id.listItem_video_rating);
//                    viewIdList.add(R.id.listItem_video_rating_layout);
//                    return viewIdList;
//                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    viewHolder.itemView.findViewById(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE :View.GONE);
                    Video video = (Video) object;
                    ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());
                    if (!video.getDateList().isEmpty()) {
                        viewHolder.itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);
                        ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));
                    } else
                        viewHolder.itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    viewHolder.itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    if (video.getRating() > 0) {
                        viewHolder.itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    }
                    else
                        viewHolder.itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    viewHolder.itemView.findViewById(R.id.listItem_video_Studio).setSelected(scrolling);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) viewHolder.itemView.findViewById(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    viewHolder.itemView.findViewById(R.id.listItem_video_Genre).setSelected(scrolling);
                })
                .setUseCustomRipple(true)
                .setOnClickListener((recycler, view, object, index) -> {
                    if (!delete) {
                        openUrl(object, false);
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
                .addSubOnClickListener(R.id.listItem_video_edit, (recycler, view, object, index) -> {
                    editDialogVideoPair = new Pair<>(showEditOrNewDialog(object), (Video) object);
                }, false)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    dialogVideoPair = new Pair<>(showDetailDialog(object), (Video) object);
                })
                .setShowDivider(false)
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
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", dialog ->
                        editDialogVideoPair = new Pair<>(showEditOrNewDialog(object), (Video) object), false)
                .addButton("Öffnen mit...", dialog -> openUrl(object, true), false)
                .setSetViewContent(view -> {
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
                        Utility.saveAll(mySPR_daten, database);
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
                .setSetViewContent(view -> {
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
//        final Video[] finalVideo = {video[0]};
        Dialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(object == null ? "Neues Video" : "Video Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
//                .addButton(CustomDialog.CANCEL_BUTTON, dialog -> {}, false)
                .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
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
                        .addButton(CustomDialog.YES_BUTTON, dialog1 ->
                                saveVideo(dialog, object, titel, url, false, video))
                        .show();
                    }
                    else
                        saveVideo(dialog, object, titel, url, checked, video);

                }, false)
                .setSetViewContent(view -> {
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
                            showEditCatigoryDialog(video[0] == null ? null : video[0].getDarstellerList(), video[0], DatenObjekt.OBJECT_TYPE.DARSTELLER));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            showEditCatigoryDialog(video[0] == null ? null : video[0].getStudioList(), video[0], DatenObjekt.OBJECT_TYPE.STUDIO ));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            showEditCatigoryDialog(video[0] == null ? null : video[0].getGenreList(), video[0], DatenObjekt.OBJECT_TYPE.GENRE));
                })
                .show();
        DialogInterface.OnKeyListener keylistener = (dialog, keyCode, KEvent) -> {
            int keyaction = KEvent.getAction();

            if(keyaction == KeyEvent.ACTION_DOWN)
            {
                int keycode = KEvent.getKeyCode();
                int keyunicode = KEvent.getUnicodeChar(KEvent.getMetaState() );
                char character = (char) keyunicode;
//                    if(keycode== KeyCode.Enter){
//
//
//                    }
            }
            return false;
        };
        returnDialog.setOnKeyListener(keylistener);
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

        Utility.saveDatabase(mySPR_daten);
        if (Utility.isOnline())
            database.writeAllToFirebase();

        Utility.showCenterdToast(this, "Video gespeichert" + (addedYesterday ? "\nAutomatisch für gestern eingetragen" : ""));

        if (dialogVideoPair == null)
            return;

        dialogVideoPair.first.dismiss();
        dialogVideoPair = new Pair<>(showDetailDialog(object), dialogVideoPair.second);

    }

    private Dialog showEditCatigoryDialog(List<String> preSelectedUuidList, Video video, DatenObjekt.OBJECT_TYPE editType) {
        if (preSelectedUuidList == null)
            preSelectedUuidList = new ArrayList<>();

        List<String> selectedUuidList = new  ArrayList<>(preSelectedUuidList);
        List<String> filterdUuidList;
        String editType_string;
        switch (editType){
            default:
            case DARSTELLER:
                filterdUuidList = new ArrayList<>(database.darstellerMap.keySet());
                editType_string = "Darsteller";
                break;
            case STUDIO:
                filterdUuidList = new ArrayList<>(database.studioMap.keySet());
                editType_string = "Studio";
                break;
            case GENRE:
                filterdUuidList = new ArrayList<>(database.genreMap.keySet());
                editType_string = "Genre";
                break;
        }

        int saveButtonId = View.generateViewId();
        int saveButtonId_add = View.generateViewId();


        String finalEditType_string = editType_string;
        String finalEditType_string1 = editType_string;
        Dialog dialog_AddActorOrGenre = CustomDialog.Builder(this)
                .setTitle(editType_string + " Bearbeiten")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .addButton("Hinzufügen", dialog -> {
                    addDialogVideoPair = new Pair<>(dialog, video);
                    CustomDialog.Builder(this)
                            .setTitle(finalEditType_string + " Hinzufügen")
                            .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                            .addButton(CustomDialog.OK_BUTTON, dialog1 -> {
                                DatenObjekt datenObjekt = new DatenObjekt(editType, CustomDialog.getEditText(dialog1));
                                switch (editType){
                                    case DARSTELLER:
                                        database.darstellerMap.put(datenObjekt.getUuid(), datenObjekt.newDarsteller());
                                        break;
                                    case STUDIO:
                                        database.studioMap.put(datenObjekt.getUuid(), datenObjekt.newStudio());
                                        break;
                                    case GENRE:
                                        database.genreMap.put(datenObjekt.getUuid(), datenObjekt.newGenre());
                                        break;
                                }
                                selectedUuidList.add(datenObjekt.getUuid());
                                addDialogVideoPair.first.dismiss();
                                addDialogVideoPair = new Pair<>(
                                        showEditCatigoryDialog(selectedUuidList, video, editType), video);

                            }, saveButtonId_add)
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setFireButtonOnOK(true, saveButtonId_add)
                                    .setHint(finalEditType_string1 + "-Name")
                                    .setText(((SearchView) dialog.findViewById(R.id.dialogAddPassenger_search)).getQuery().toString()))
                            .show();

                }, false)
                .addButton("Abbrechen", dialog -> {})
                .addButton("Speichern", dialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (editType){
                        case DARSTELLER:
                            video.setDarstellerList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.darstellerMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", nameList));
                            break;
                        case STUDIO:
                            video.setStudioList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.studioMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", nameList));
                            break;
                        case GENRE:
                            video.setGenreList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.genreMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", nameList));
                            break;
                    }
                }, saveButtonId)
                .show();

//        Button saveButton = dialog_AddActorOrGenre.findViewById(saveButtonId);
//        saveButton.setEnabled(false);

        CustomRecycler customRecycler_selectList = CustomRecycler.Builder(this, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = CustomRecycler.Builder(this, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemLayout(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .setShowDivider(false)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.list_bubble_name);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.list_bubble_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.list_bubble_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.list_bubble_name)).setText(genre.getName());
                            break;
                    }

                    dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(this,
                            "Halten zum abwählen" , Toast.LENGTH_SHORT).show();
                })
                .setOnLongClickListener((recycler, view, object, index) -> {
                    ((CustomRecycler.MyAdapter) recycler.getAdapter()).removeItemAt(index);
                    selectedUuidList.remove(object);

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectList.setObjectList(filterdUuidList).reload();
                })
                .setUseCustomRipple(true)
                .generateCustomRecycler();

        List<String> sortedAllObjectIdList;
        switch (editType){
            default:
            case DARSTELLER:
                sortedAllObjectIdList = new ArrayList(database.darstellerMap.keySet());
                sortedAllObjectIdList.sort((id1, id2) -> database.darstellerMap.get(id1).getName().compareTo(database.darstellerMap.get(id2).getName()));
                break;
            case STUDIO:
                sortedAllObjectIdList = new ArrayList(database.studioMap.keySet());
                sortedAllObjectIdList.sort((id1, id2) -> database.studioMap.get(id1).getName().compareTo(database.studioMap.get(id2).getName()));
                break;
            case GENRE:
                sortedAllObjectIdList = new ArrayList(database.genreMap.keySet());
                sortedAllObjectIdList.sort((id1, id2) -> database.genreMap.get(id1).getName().compareTo(database.genreMap.get(id2).getName()));
                break;
        }


        customRecycler_selectList
                .setItemLayout(R.layout.list_item_select_actor)
                .setMultiClickEnabled(true)
                .setObjectList(sortedAllObjectIdList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.selectList_name);
                    viewIdList.add(R.id.selectList_selected);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.selectList_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.selectList_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) viewHolder.itemView.findViewById(R.id.selectList_name)).setText(genre.getName());
                            break;
                    }

                    if (selectedUuidList.contains(object))
                        ((CheckBox) viewHolder.itemView.findViewById(R.id.selectList_selected)).setChecked(true);
                })
                .setOnClickListener((recycler, view, object, index) -> {
                    CheckBox checkBox = view.findViewById(R.id.selectList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedUuidList.contains(object))
                        selectedUuidList.remove(object);
                    else
                        selectedUuidList.add((String) object);

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectedList.setObjectList(selectedUuidList).reload();
                })
                .generateCustomRecycler();

        SearchView searchView = dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_search);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.trim();
                if (s.equals("")) {
//                    List<String> objectIdList = new ArrayList<>();
//                    switch (editType){
//                        case DARSTELLER:
//                            objectIdList = new ArrayList<>(database.darstellerMap.keySet());
//                            objectIdList.sort((id1, id2) -> database.darstellerMap.get(id1).getName().compareTo(database.darstellerMap.get(id2).getName()));
//                            break;
//                        case STUDIO:
//                            objectIdList = new ArrayList<>(database.studioMap.keySet());
//                            objectIdList.sort((id1, id2) -> database.studioMap.get(id1).getName().compareTo(database.studioMap.get(id2).getName()));
//                            break;
//                        case GENRE:
//                            objectIdList = new ArrayList<>(database.genreMap.keySet());
//                            objectIdList.sort((id1, id2) -> database.genreMap.get(id1).getName().compareTo(database.genreMap.get(id2).getName()));
//                            break;
//                    }
                    customRecycler_selectList.setObjectList(sortedAllObjectIdList).reload();
                }
                switch (editType){
                    case DARSTELLER:
                        Map<String, Darsteller> darstellerMap = database.darstellerMap;
                        filterdUuidList.clear();
                        for (String uuidActor : sortedAllObjectIdList) {
                            if (darstellerMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                    case STUDIO:
                        Map<String, Studio> studioMap = database.studioMap;
                        filterdUuidList.clear();
                        for (String uuidActor : sortedAllObjectIdList) {
                            if (studioMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                    case GENRE:
                        Map<String, Genre> genreMap = database.genreMap;
                        filterdUuidList.clear();
                        for (String uuidActor : sortedAllObjectIdList) {
                            if (genreMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                }

                customRecycler_selectList.setObjectList(filterdUuidList).reload();

                return true;
            }
        });

        return dialog_AddActorOrGenre;

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar_video, menu);

        Menu subMenu = menu.getItem(((MenuBuilder) menu).findItemIndex(R.id.taskBar_filter)).getSubMenu();
        subMenu.getItem(((MenuBuilder) subMenu).findItemIndex(R.id.taskBar_video_filterByName))
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.getItem(((MenuBuilder) subMenu).findItemIndex(R.id.taskBar_video_filterByDarsteller))
                .setChecked(filterTypeSet.contains(FILTER_TYPE.ACTOR));
        subMenu.getItem(((MenuBuilder) subMenu).findItemIndex(R.id.taskBar_video_filterByStudio))
                .setChecked(filterTypeSet.contains(FILTER_TYPE.STUDIO));
        subMenu.getItem(((MenuBuilder) subMenu).findItemIndex(R.id.taskBar_video_filterByGenre))
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_video_add:
                editDialogVideoPair = new Pair<>(showEditOrNewDialog(null), null);
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
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", dialog -> {
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
                .addButton("Öffnen", dialog -> openUrl(randomVideo, false), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

                })
                .show();

    }

    public void openUrl(Object object, boolean select) {
        String url = ((Video) object).getUrl();
        if (url == null || url.equals("")) {
            Toast.makeText(this, "Keine URL hinterlegt", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.contains("http://") && !url.contains("https://"))
            url = "http://".concat(url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        if (!select) {
            startActivity(i);
        } else {
            Intent chooser = Intent.createChooser(i, "Öffnen mit...");
            startActivity(chooser);
        }
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
        Utility.saveAll(mySPR_daten, database);
        super.onDestroy();
    }

}
