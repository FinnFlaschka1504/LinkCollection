package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Pair;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activitys.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.DatenObjekt;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.maxMustermannGeheim.linkcollection.Activitys.MainActivity.SHARED_PREFERENCES_NAME;
import static com.maxMustermannGeheim.linkcollection.Activitys.VideoActivity.EXTRA_SEARCH;
import static com.maxMustermannGeheim.linkcollection.Activitys.VideoActivity.EXTRA_SEARCH_CATIGORY;

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

    public static void saveDatabase(SharedPreferences mySPR_daten) {
        Gson gson = new Gson();
        Database database = Database.getInstance();
        if (database == null)
            return;

        mySPR_daten.edit()
                .putString(Database.VIDEO_MAP, gson.toJson(database.videoMap))
                .putString(Database.DARSTELLER_MAP, gson.toJson(database.darstellerMap))
                .putString(Database.STUDIO_MAP, gson.toJson(database.studioMap))
                .putString(Database.GENRE_MAP, gson.toJson(database.genreMap))
                .putString(Database.WATCH_LATER_LIST, gson.toJson(database.watchLaterList))
                .apply();
    }

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
            if (containedInStudio(query, video.getStudioList()))
                return true;
        }
        return false;
    }

    private static boolean contains(String all, String sub) {
        return all.toLowerCase().contains(sub.toLowerCase());
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

    public static void saveAll(SharedPreferences mySPR_daten, Database database) {
        Utility.saveDatabase(mySPR_daten);
        if (Utility.isOnline())
            database.writeAllToFirebase();
    }

//    public static void showCalenderDialog(Context context, List<Video> videoList, boolean showAll, Pair<Dialog, Video> dialogVideoPair) {
//        CustomDialog.Builder(context)
//                .setTitle(showAll ? "Video Calender" : "Ansichten Bearbeiten")
//                .setView(R.layout.dialog_edit_views)
//                .setSetViewContent(view -> {
//                    ViewStub stub_groups = view.findViewById(R.id.dialog_editViews_calender);
//                    stub_groups.setLayoutResource(R.layout.fragment_calender);
//                    stub_groups.inflate();
//                    CompactCalendarView calendarView = view.findViewById(R.id.fragmentCalender_calendar);
//                    calendarView.setFirstDayOfWeek(Calendar.MONDAY);
//                    setupCalender(context, calendarView, ((LinearLayout) view), videoList, showAll);
//                })
//                .addButton(CustomDialog.BACK_BUTTON, dialog -> {
//                    if (dialogVideoPair != null)
//                        dialogVideoPair.first.re
//                })
//                .show();
//
//    }

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
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.listItem_video_Titel);
                    viewIdList.add(R.id.listItem_video_Views_layout);
                    viewIdList.add(R.id.listItem_video_Darsteller);
                    viewIdList.add(R.id.listItem_video_Studio);
                    viewIdList.add(R.id.listItem_video_Genre);
                    viewIdList.add(R.id.listItem_video_rating_layout);
                    viewIdList.add(R.id.listItem_video_edit);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    viewIdMap.get(R.id.listItem_video_edit).setVisibility(View.GONE);
                    viewIdMap.get(R.id.listItem_video_Views_layout).setVisibility(View.GONE);


                    Video video = ((Video) ((Event) object).getData());
                    ((TextView) viewIdMap.get(R.id.listItem_video_Titel)).setText(video.getName());

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    viewIdMap.get(R.id.listItem_video_Darsteller).setSelected(true);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    viewIdMap.get(R.id.listItem_video_Studio).setSelected(true);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    viewIdMap.get(R.id.listItem_video_Genre).setSelected(true);
                })
                .setUseCustomRipple(true)
                .setShowDivider(false);

        if (openVideo)
            customRecycler.setOnClickListener((recycler, view, object, index) ->
                    ((MainActivity) context).startActivityForResult(new Intent(context, VideoActivity.class)
                            .putExtra(EXTRA_SEARCH, ((DatenObjekt) ((Event) object).getData()).getUuid())
                            .putExtra(EXTRA_SEARCH_CATIGORY, MainActivity.CATIGORYS.Video.name()), ((MainActivity) context).START_VIDEO_FROM_CALENDER));

        for (Video video : videoList) {
            for (Date date : video.getDateList()) {
                Event ev1 = new Event(Color.BLACK
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
            videoList.get(0).getDateList().add(selectedDate[0]);
            calendarView.addEvent(new Event(Color.BLACK
                    , selectedDate[0].getTime(), videoList.get(0)));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1);
            saveAll(context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0), database);
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            videoList.get(0).getDateList().remove(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0);
            saveAll(context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0), database);
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
}
