package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.maxMustermannGeheim.linkcollection.Activitys.Knowledge.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maxMustermannGeheim.linkcollection.Activitys.Videos.VideoActivity.EXTRA_SEARCH;
import static com.maxMustermannGeheim.linkcollection.Activitys.Videos.VideoActivity.EXTRA_SEARCH_CATIGORY;

public class Utility {

    static public boolean isOnline(Context context) {
        boolean isOnleine = isOnline();
        if (isOnleine) {
            return true;
        } else {
            Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    static public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void restartApp(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void changeDialogKeyboard(Dialog dialog, boolean show) {
        if (show)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        else
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//  ----- Filter ----->
    private static boolean contains(String all, String sub) {
    return all.toLowerCase().contains(sub.toLowerCase());
}

    //  ----- ... in Videos ----->
    public static boolean containedInVideo(String query, Video video, HashSet<VideoActivity.FILTER_TYPE> filterTypeSet) {
        if (video.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.NAME)) {
            if (contains(video.getName(), query))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.ACTOR)) {
            if (containedInActors(query, video.getDarstellerList()))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.GENRE)) {
            if (containedInGenre(query, video.getGenreList()))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.STUDIO)) {
            return containedInStudio(query, video.getStudioList());
        }
        return false;
    }

    private static boolean  containedInActors(String query, List<String> actorUuids) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (contains(database.darstellerMap.get(actorUUid).getName(), query))
                return true;
        }
        return false;
    }
    private static boolean  containedInGenre(String query, List<String> genreUuids) {
        Database database = Database.getInstance();
        for (String genreUUid : genreUuids) {
            if (contains(database.genreMap.get(genreUUid).getName(), query))
                return true;
        }
        return false;
    }
    private static boolean  containedInStudio(String query, List<String> studioUuids) {
        Database database = Database.getInstance();
        for (String studioUUid : studioUuids) {
            if (contains(database.studioMap.get(studioUUid).getName(), query))
                return true;
        }
        return false;
    }
    //  <----- ... in Videos -----

    //  ----- ... in Knowledge ----->
    public static boolean containedInKnowledge(String query, Knowledge video, HashSet<KnowledgeActivity.FILTER_TYPE> filterTypeSet) {
        return true;
    }
        //  <----- ... in Knowledge -----
//  <----- Filter -----

    public static void setupCalender(Context context, CompactCalendarView calendarView, LinearLayout layout, List<Video> videoList, boolean openVideo) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Database database = Database.getInstance();
        CustomRecycler customRecycler = CustomRecycler.Builder(context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_video)
                .setSetItemContent((itemView, object) -> {
                    itemView.findViewById(R.id.listItem_video_details).setVisibility(View.GONE);
                    itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.GONE);


                    Video video = ((Video) ((Event) object).getData());
                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(true);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    itemView.findViewById(R.id.listItem_video_Studio).setSelected(true);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    itemView.findViewById(R.id.listItem_video_Genre).setSelected(true);
                })
                .setUseCustomRipple(true)
                .setShowDivider(false);

        if (openVideo)
            customRecycler.setOnClickListener((recycler, view, object, index) ->
                    ((MainActivity) context).startActivityForResult(new Intent(context, VideoActivity.class)
                            .putExtra(EXTRA_SEARCH, ((ParentClass) ((Event) object).getData()).getUuid())
                            .putExtra(EXTRA_SEARCH_CATIGORY, MainActivity.CATEGORIES.Video.name()), ((MainActivity) context).START_VIDEO_FROM_CALENDER));

        for (Video video : videoList) {
            for (Date date : video.getDateList()) {
                Event ev1 = new Event(context.getColor(R.color.colorDrawable)
                        , date.getTime(), video);
                calendarView.addEvent(ev1);

            }

        }

        final Date[] selectedDate = {removeTime(new Date())};
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDate[0] = dateClicked;
                if (videoList.size() == 1)
                    setButtons(layout, calendarView.getEvents(dateClicked).size());
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
//                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                SimpleDateFormat sdfmt = new SimpleDateFormat();
                sdfmt.applyPattern( "MMMM yyyy" );
                calender_month.setText(sdfmt.format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        if (videoList.size() == 1)
            setButtons(layout, calendarView.getEvents(new Date()).size());

        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            videoList.get(0).addDate(selectedDate[0], false);
            calendarView.addEvent(new Event(Color.BLACK
                    , selectedDate[0].getTime(), videoList.get(0)));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1);
            Database.saveAll();
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            videoList.get(0).getDateList().remove(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0);
            Database.saveAll();
        });
    }

    private static void setButtons(LinearLayout layout, int size) {
        layout.findViewById(R.id.dialog_editViews_add).setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.dialog_editViews_remove).setVisibility(size != 0 ? View.VISIBLE : View.GONE);
    }

    private static void loadVideoList(List<Event> eventList, LinearLayout layout, CustomRecycler customRecycler) {
        TextView calender_noTrips = layout.findViewById(R.id.fragmentCalender_noTrips);

        if (eventList.isEmpty())
            calender_noTrips.setVisibility(View.VISIBLE);
        else
            calender_noTrips.setVisibility(View.GONE);

        customRecycler
                .setObjectList(eventList)
                .generate();

    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static void showCenterdToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        TextView v = toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public static Dialog showEditCatigoryDialog(Context context, Dialog[] addOrEditDialog, List<String> preSelectedUuidList, Object o, ParentClass.OBJECT_TYPE editType) {
        Database database = Database.getInstance();
        
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
        Dialog dialog_AddActorOrGenre = CustomDialog.Builder(context)
                .setTitle(editType_string + " Bearbeiten")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .addButton("Hinzufügen", dialog -> {
                    addOrEditDialog[0] = dialog;
                    CustomDialog.Builder(context)
                            .setTitle(finalEditType_string + " Hinzufügen")
                            .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                            .addButton(CustomDialog.OK_BUTTON, dialog1 -> {
                                ParentClass parentClass = new ParentClass(editType, CustomDialog.getEditText(dialog1));
                                switch (editType){
                                    case DARSTELLER:
                                        database.darstellerMap.put(parentClass.getUuid(), parentClass.newDarsteller());
                                        break;
                                    case STUDIO:
                                        database.studioMap.put(parentClass.getUuid(), parentClass.newStudio());
                                        break;
                                    case GENRE:
                                        database.genreMap.put(parentClass.getUuid(), parentClass.newGenre());
                                        break;
                                }
                                selectedUuidList.add(parentClass.getUuid());
                                addOrEditDialog[0].dismiss();
                                addOrEditDialog[0] = showEditCatigoryDialog(context, addOrEditDialog,selectedUuidList, o, editType);

                            }, saveButtonId_add)
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setFireButtonOnOK(saveButtonId_add)
                                    .setHint(finalEditType_string1 + "-Name")
                                    .setText(((SearchView) dialog.findViewById(R.id.dialogAddPassenger_search)).getQuery().toString()))
                            .show();

                }, false)
                .addButton("Abbrechen", dialog -> {})
                .addButton("Speichern", dialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (editType){
                        case DARSTELLER:
                            ((Video) o).setDarstellerList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.darstellerMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog[0].findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", nameList));
                            break;
                        case STUDIO:
                            ((Video) o).setStudioList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.studioMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog[0].findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", nameList));
                            break;
                        case GENRE:
                            ((Video) o).setGenreList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.genreMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog[0].findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", nameList));
                            break;
                    }
                }, saveButtonId)
                .show();


        CustomRecycler customRecycler_selectList = CustomRecycler.Builder(context, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = CustomRecycler.Builder(context, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemLayout(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .setShowDivider(false)
                .setSetItemContent((itemView, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(genre.getName());
                            break;
                    }

                    dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(context,
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
                .setSetItemContent((itemView, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) itemView.findViewById(R.id.selectList_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) itemView.findViewById(R.id.selectList_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) itemView.findViewById(R.id.selectList_name)).setText(genre.getName());
                            break;
                    }

                    ((CheckBox) itemView.findViewById(R.id.selectList_selected)).setChecked(selectedUuidList.contains(object));
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

}
