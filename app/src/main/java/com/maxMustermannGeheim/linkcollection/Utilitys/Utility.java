package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import top.defaults.drawabletoolbox.DrawableBuilder;


public class Utility implements java.io.Serializable{

    //  --------------- isOnline --------------->
    static public boolean isOnline(Context context) {
        if (isOnline()) {
            return true;
        } else {
            Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    // ToDo: in seperatem Thread
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

    public static void isOnline(Runnable onTrue, Runnable onFalse){

    }

    public static void isOnline(OnResult onResult){

    }

    public interface OnResult {
        void runOnResult(boolean status);
    }

    private class PingThread extends Thread {
//        String name;
        Runnable onTrue;
        Runnable onFalse;
        OnResult onResult;
        Context context;

        //  ------------------------- Constructors ------------------------->
        public PingThread(Runnable onTrue, Runnable onFalse) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
        }

        public PingThread(Runnable onTrue, Runnable onFalse, Context context) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
            this.context = context;
        }

        public PingThread(OnResult onResult) {
            this.onResult = onResult;
        }

        public PingThread(OnResult onResult, Context context) {
            this.onResult = onResult;
            this.context = context;
        }
        //  <------------------------- Constructors -------------------------

        @Override
        public void run() {
            if (checkConnection()) {
                if (onTrue != null) onTrue.run();
                if (onResult != null) onResult.runOnResult(true);
            } else {
                if (onFalse != null) onFalse.run();
                if (onResult != null) onResult.runOnResult(false);
                if (context != null) Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean checkConnection() {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int exitValue = ipProcess.waitFor();


                Thread.sleep(2000);

                return false;
//                return (exitValue == 0);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    //  <--------------- isOnline ---------------

    public static void restartApp(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void changeWindowKeyboard(Window window, boolean show) {
        if (show)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        else
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void changeWindowKeyboard(Context context, View view, boolean show) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        else
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void openUrl(Context context, String url, boolean select) {
        if (!url.contains("http://") && !url.contains("https://"))
            url = "http://".concat(url);
        if (!select) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            Intent chooser = Intent.createChooser(i, "Öffnen mit...");
            if (chooser.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(chooser);
        }
    }

    public static String formatToEuro(double amount) {
        if (amount == 0)
            return "N/A";
        if (amount % 1 == 0)
            return String.format(Locale.GERMANY, "%.0f €", amount);
        else
            return String.format(Locale.GERMANY, "%.2f €", amount);
    }


    //  --------------- Copy --------------->
    public static <T> T deepCopy(T t) {
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
//            outputStrm.writeObject(t);
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
//            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
//            return (T) objInputStream.readObject();
//        } catch (Exception e) {
//            return null;
//        }

//        Gson gson = new Gson();
//        return (T) gson.fromJson(gson.toJson(t, t.getClass()), t.getClass());


//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .excludeFieldsWithModifiers(TRANSIENT) // STATIC|TRANSIENT in the default configuration
//                .create();
//
//        return (T) gson.fromJson(gson.toJson(t), t.getClass());

//        final ObjectMapper objMapper = new ObjectMapper();
//        String jsonStr = null;
//        try {
//            jsonStr = objMapper.writeValueAsString(t);
//            return (T) objMapper.readValue(jsonStr, t.getClass());
//        } catch (IOException e) {
//            String BREAKPOINT = null;
//            return null;
//        }

        return t;
    }
    //  <--------------- Copy ---------------


    //  --------------- OnClickListener --------------->
    public static View.OnClickListener getOnClickListener(View view) {
        View.OnClickListener retrievedListener = null;
        String viewStr = "android.view.View";
        String lInfoStr = "android.view.View$ListenerInfo";

        try {
            Field listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnClickListener");

            if (clickListenerField != null && listenerInfo != null) {
                retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
            }
        } catch (NoSuchFieldException ex) {
            Log.e("Reflection", "No Such Field.");
        } catch (IllegalAccessException ex) {
            Log.e("Reflection", "Illegal Access.");
        } catch (ClassNotFoundException ex) {
            Log.e("Reflection", "Class Not Found.");
        }
        return retrievedListener;
    }

    public static void interceptOnClick(View view, InterceptOnClick interceptOnClick) {
        View.OnClickListener oldListener = getOnClickListener(view);
        view.setOnClickListener(v -> {
            if (!interceptOnClick.runInterceptOnClick(view))
                oldListener.onClick(view);
        });
    }

    interface InterceptOnClick {
        boolean runInterceptOnClick(View view);
    }
    //  <--------------- OnClickListener ---------------

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

    private static boolean containedInActors(String query, List<String> actorUuids) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (database.darstellerMap.get(actorUUid).getName().equals(query))
                return true;
        }
        return false;
    }

    private static boolean containedInGenre(String query, List<String> genreUuids) {
        Database database = Database.getInstance();
        for (String genreUUid : genreUuids) {
            if (database.genreMap.get(genreUUid).getName().equals(query))
                return true;
        }
        return false;
    }

    private static boolean containedInStudio(String query, List<String> studioUuids) {
        Database database = Database.getInstance();
        for (String studioUUid : studioUuids) {
            if (database.studioMap.get(studioUUid).getName().equals(query))
                return true;
        }
        return false;
    }
    //  <----- ... in Videos -----

    //  ----- ... in Knowledge ----->
    public static boolean containedInKnowledge(String query, Knowledge knowledge, HashSet<KnowledgeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.NAME) && knowledge.getName().toLowerCase().contains(query))
            return true;
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
//            for (ParentClass category : getMapFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES).values()) {
            for (String categoryId : knowledge.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().contains(query))
                    return true;
            }
        }

        return false;
    }
    //  <----- ... in Knowledge -----

    //  ----- ... in Owe ----->
    public static boolean containedInOwe(String query, Owe owe, HashSet<OweActivity.FILTER_TYPE> filterTypeSet) {
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OWN) && owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
            return false;
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OTHER) && owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OTHER)
            return false;

        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OPEN) && owe.getItemList().stream().anyMatch(Owe.Item::isOpen))
            return false;
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.CLOSED) && owe.getItemList().stream().noneMatch(Owe.Item::isOpen))
            return false;

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.NAME) && owe.getName().toLowerCase().contains(query))
            return true;

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.DESCRIPTION) && owe.getDescription().toLowerCase().contains(query))
            return true;

        return filterTypeSet.contains(OweActivity.FILTER_TYPE.PERSON) && owe.getItemList().stream().anyMatch(item -> Database.getInstance().personMap.get(item.getPersonId()).getName().toLowerCase().contains(query));

//        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
////            for (ParentClass category : getMapFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES).values()) {
//            for (String categoryId : owe.getCategoryIdList()) {
//                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().contains(query))
//                    return true;
//            }
//        }
    }
    //  <----- ... in Owe -----

    //  ----- ... in Joke ----->
    public static boolean containedInJoke(String query, Joke joke, HashSet<JokeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.NAME) && joke.getName().toLowerCase().contains(query))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.PUNCHLINE) && joke.getPunchLine().toLowerCase().contains(query))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.CATEGORY)) {
            for (String categoryId : joke.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.JOKE_CATEGORIES, categoryId).getName().toLowerCase().contains(query))
                    return true;
            }
        }

        return false;
    }
    //  <----- ... in Joke -----

    //  ----- ... in Show ----->
    public static boolean containedInShow(String query, Show show, HashSet<ShowActivity.FILTER_TYPE> filterTypeSet) {
        if (show.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.NAME) && show.getName().toLowerCase().contains(query))
            return true;
        Database database = Database.getInstance();
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.GENRE) && show.getGenreIdList().stream().anyMatch(uuid -> {
            return database.showGenreMap.get(uuid).getName().toLowerCase().contains(query.toLowerCase());
        }))
            return true;
        return false;
    }
    //  <----- ... in Show -----
//  <----- Filter -----


    private static Date currentDate;
    //  --------------- FilmCalender --------------->
    public static void setupFilmCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Video> videoList, boolean openVideo) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

        currentDate = new Date();
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Database database = Database.getInstance();
        CustomRecycler customRecycler = new CustomRecycler<>(context, layout.findViewById(R.id.fragmentCalender_videoList))
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
                .hideDivider();

        if (openVideo)
            customRecycler.setOnClickListener((customRecycler1, view, object, index) ->
                    ((MainActivity) context).startActivityForResult(new Intent(context, VideoActivity.class)
                            .putExtra(CategoriesActivity.EXTRA_SEARCH, ((ParentClass) ((Event) object).getData()).getUuid())
                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_VIDEO_FROM_CALENDER));

        for (Video video : videoList) {
            for (Date date : video.getDateList()) {
                Event ev1 = new Event(context.getColor(R.color.colorDayNightContent)
                        , date.getTime(), video);
                calendarView.addEvent(ev1);

            }

        }

        final Date[] selectedDate = {removeTime(new Date())};
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
                selectedDate[0] = dateClicked;
//                if (videoList.size() == 1)
                    setButtons(layout, calendarView.getEvents(dateClicked).size(), calendarView, videoList, customRecycler);
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        setButtons(layout, calendarView.getEvents(new Date()).size(), calendarView, videoList, customRecycler);

        if (videoList.size() != 1)
            return;

        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            videoList.get(0).addDate(selectedDate[0], false);
            calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                    , selectedDate[0].getTime(), videoList.get(0)));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1, calendarView, videoList, customRecycler);
            Database.saveAll();
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            videoList.get(0).removeDate(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0, calendarView, videoList, customRecycler);
            Database.saveAll();
        });
    }
    //  <--------------- FilmCalender ---------------

    // ToDo: Alignment von den Buttons ändern

    //  --------------- EpisodeCalender --------------->
    public static void setupEpisodeCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Show.Episode> episodeList, boolean openEpisode) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

        currentDate = new Date();
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Database database = Database.getInstance();
        CustomRecycler<Event> customRecycler = new CustomRecycler<Event>(context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_episode)
                .setSetItemContent((itemView, event) -> {
                    itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);


                    Show.Episode episode = ((Show.Episode) event.getData());

                    if (openEpisode) {
                        itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_episode_showName)).setText(database.showMap.get(episode.getShowId()).getName());
                        ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));
                    }

                    ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                    ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(episode.getAirDate()));
                    ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode.getRating() != -1 ? episode.getRating() + " ☆" : "");

                })
                .hideDivider();

        if (openEpisode)
            customRecycler.setOnClickListener((customRecycler1, view, event, index) ->
            {
                Show.Episode episode = (Show.Episode) event.getData();
                ((MainActivity) context).startActivityForResult(new Intent(context, ShowActivity.class)
                        .putExtra(CategoriesActivity.EXTRA_SEARCH, episode.getShowId())
                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.EPISODE)
                        .putExtra(ShowActivity.EXTRA_EPISODE, new Gson().toJson(episode)), MainActivity.START_SHOW_FROM_CALENDER);
            });

        for (Show.Episode episode : episodeList) {
            for (Date date : episode.getDateList()) {
                Event ev1 = new Event(context.getColor(R.color.colorDayNightContent)
                        , date.getTime(), episode);
                calendarView.addEvent(ev1);
            }
        }

        final Date[] selectedDate = {removeTime(new Date())};
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
                selectedDate[0] = dateClicked;
//                if (episodeList.size() == 1)
                    setButtons(layout, calendarView.getEvents(dateClicked).size(), calendarView, episodeList, customRecycler);
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        setButtons(layout, calendarView.getEvents(new Date()).size(), calendarView, episodeList, customRecycler);

        if (episodeList.size() != 1)
            return;

        Show.Episode episode = episodeList.get(0);
        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            episode.addDate(selectedDate[0], false);
            calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                    , selectedDate[0].getTime(), episode));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1, calendarView, episodeList, customRecycler);
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            episode.removeDate(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0, calendarView, episodeList, customRecycler);
        });
    }
    //  <--------------- EpisodeCalender ---------------

    private static void loadVideoList(List<Event> eventList, FrameLayout layout, CustomRecycler<Event> customRecycler) {
        eventList = new ArrayList<>(eventList);
        eventList.sort((o1, o2) -> Long.compare(o1.getTimeInMillis(), o2.getTimeInMillis()));
        TextView calender_noTrips = layout.findViewById(R.id.fragmentCalender_noTrips);
        RecyclerView calender_videoList = layout.findViewById(R.id.fragmentCalender_videoList);

        if (eventList.isEmpty()) {
            calender_videoList.setVisibility(View.GONE);
            calender_noTrips.setVisibility(View.VISIBLE);
        } else {
            calender_videoList.setVisibility(View.VISIBLE);
            calender_noTrips.setVisibility(View.GONE);
        }

        if (customRecycler.getObjectList().isEmpty()) {
            customRecycler.setObjectList(eventList).generate();
        } else
            customRecycler.reload(eventList);

    }

    private static void setButtons(FrameLayout layout, int size, CompactCalendarView calendarView, List list, CustomRecycler<Event> customRecycler) {
        if (list.size() == 1) {
            layout.findViewById(R.id.dialog_editViews_add).setVisibility(size == 0 ? View.VISIBLE : View.GONE);
            layout.findViewById(R.id.dialog_editViews_remove).setVisibility(size != 0 ? View.VISIBLE : View.GONE);
        }

        CustomList<Date> dateList = new CustomList<>();
        for (Object o : list) {
            if (o instanceof Video) {
                Video video = (Video) o;
                dateList.addAll(video.getDateList());
//                video.getDateList().forEach(date -> dateSet.add(Utility.removeTime(date)));
            } else if (o instanceof Show.Episode) {
                Show.Episode episode = (Show.Episode) o;
                dateList.addAll(episode.getDateList());
//                episode.getDateList().forEach(date -> dateSet.add(Utility.removeTime(date)));
            }
        }
        dateList.sorted(Date::compareTo).replaceAll(Utility::removeTime);
        dateList.distinct();
//        dateList = dateSet.stream().sorted(Date::compareTo).collect(Collectors.toCollection(CustomList::new));

        layout.findViewById(R.id.dialog_editViews_previous).setOnClickListener(v -> {
            Date previous = dateList.stream().filter(date -> date.before(currentDate)).collect(Collectors.toCollection(CustomList::new)).getLast();
            if (previous != null) {
                calendarView.setCurrentDate(previous);
                loadVideoList(calendarView.getEvents(previous), layout, customRecycler);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(previous));
                currentDate = previous;
//                v.setVisibility(dateList.isFirst(previous) ? View.GONE : View.VISIBLE);
                if (list.size() == 1) {
                    layout.findViewById(R.id.dialog_editViews_add).setVisibility(View.GONE);
                    layout.findViewById(R.id.dialog_editViews_remove).setVisibility(View.VISIBLE);
                }

            }
        });

        layout.findViewById(R.id.dialog_editViews_next).setOnClickListener(v -> {
            Date next = dateList.stream().filter(date -> date.after(currentDate)).findFirst().orElse(null);
            if (next != null) {
                calendarView.setCurrentDate(next);
                loadVideoList(calendarView.getEvents(next), layout, customRecycler);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(next));
                currentDate = next;
//                v.setVisibility(dateList.isLast(next) ? View.GONE : View.VISIBLE);
                if (list.size() == 1) {
                    layout.findViewById(R.id.dialog_editViews_add).setVisibility(View.GONE);
                    layout.findViewById(R.id.dialog_editViews_remove).setVisibility(View.VISIBLE);
                }
            }
        });

    }


    //  ------------------------- Checks ------------------------->
    public static boolean isUrl(String text){
        return text.matches("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$");
    }
    //  <------------------------- Checks -------------------------


    //  ------------------------- SubString ------------------------->
    public static String subString(String s, int start){
        if (start < 0)
            start = s.length() + start;
        return s.substring(start);
    }

    public static String subString(String s, int start, int end){
        if (start < 0)
            start = s.length() + start;
        if (end < 0)
            end = s.length() + end;

        return s.substring(start, end);
    }
    //  <------------------------- SubString -------------------------

    //  --------------- Time --------------->
    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date removeMilliseconds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date shiftTime(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();

    }

    public static boolean isUpcoming(Date date){
        if (date == null)
            return false;
        return new Date().before(date);
    }
    //  <--------------- Time ---------------


    //  --------------- Toast --------------->
    public static Toast centeredToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        TextView v = toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        return toast;
    }

    public static void showCenteredToast(Context context, String text) {
        centeredToast(context, text).show();
    }
    //  <--------------- Toast ---------------

    public static CustomDialog showEditItemDialog(Context context, CustomDialog addOrEditDialog, List<String> preSelectedUuidList, Object o, CategoriesActivity.CATEGORIES category) {
        Database database = Database.getInstance();

        if (preSelectedUuidList == null)
            preSelectedUuidList = new ArrayList<>();

        List<String> selectedUuidList = new ArrayList<>(preSelectedUuidList);
        List<ParentClass> allObjectsList;
        String editType_string = category.getPlural();
        final String[] searchQuery = {""};
        switch (category) {
            default:
            case DARSTELLER:
                allObjectsList = new ArrayList<>(database.darstellerMap.values());
                break;
            case STUDIOS:
                allObjectsList = new ArrayList<>(database.studioMap.values());
                break;
            case GENRE:
                allObjectsList = new ArrayList<>(database.genreMap.values());
                break;
            case KNOWLEDGE_CATEGORIES:
                allObjectsList = new ArrayList<>(database.knowledgeCategoryMap.values());
                break;
            case JOKE_CATEGORIES:
                allObjectsList = new ArrayList<>(database.jokeCategoryMap.values());
                break;
            case SHOW_GENRES:
                allObjectsList = new ArrayList<>(database.showGenreMap.values());
                break;
        }

        int saveButtonId = View.generateViewId();

        String finalEditType_string = editType_string;
        CustomDialog dialog_AddActorOrGenre = CustomDialog.Builder(context)
                .setTitle(editType_string + " Bearbeiten")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .disableScroll()
                .addButton("Hinzufügen", customDialog -> {
                    CustomDialog.Builder(context)
                            .setTitle(finalEditType_string + " Hinzufügen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                ParentClass parentClass = ParentClass.newCategoy(category, customDialog1.getEditText());
                                switch (category) {
                                    case DARSTELLER:
                                        database.darstellerMap.put(parentClass.getUuid(), (Darsteller) parentClass);
                                        break;
                                    case STUDIOS:
                                        database.studioMap.put(parentClass.getUuid(), (Studio) parentClass);
                                        break;
                                    case GENRE:
                                        database.genreMap.put(parentClass.getUuid(), (Genre) parentClass);
                                        break;
                                    case KNOWLEDGE_CATEGORIES:
                                        database.knowledgeCategoryMap.put(parentClass.getUuid(), (KnowledgeCategory) parentClass);
                                        break;
                                    case JOKE_CATEGORIES:
                                        database.jokeCategoryMap.put(parentClass.getUuid(), (JokeCategory) parentClass);
                                        break;
                                    case SHOW_GENRES:
                                        database.showGenreMap.put(parentClass.getUuid(), (ShowGenre) parentClass);
                                        break;
                                }
                                selectedUuidList.add(parentClass.getUuid());
                                customDialog1.dismiss();
                                showEditItemDialog(context, addOrEditDialog, selectedUuidList, o, category);
                                Database.saveAll();
                            })
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setHint(finalEditType_string + "-Name")
                                    .setText(((SearchView) customDialog.findViewById(R.id.dialogAddPassenger_search)).getQuery().toString()))
                            .show();

                }, false)
                .addButton("Abbrechen", customDialog -> {
                })
                .addButton("Speichern", customDialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (category) {
                        case DARSTELLER:
                            ((Video) o).setDarstellerList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.darstellerMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", nameList));
                            break;
                        case STUDIOS:
                            ((Video) o).setStudioList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.studioMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", nameList));
                            break;
                        case GENRE:
                            ((Video) o).setGenreList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.genreMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", nameList));
                            break;
                        case KNOWLEDGE_CATEGORIES:
                            ((Knowledge) o).setCategoryIdList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.knowledgeCategoryMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddKnowledge_categories)).setText(String.join(", ", nameList));
                            break;
                        case JOKE_CATEGORIES:
                            ((Joke) o).setCategoryIdList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.jokeCategoryMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddJoke_categories)).setText(String.join(", ", nameList));
                            break;
                        case SHOW_GENRES:
                            ((Show) o).setGenreIdList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.showGenreMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAdd_show_Genre)).setText(String.join(", ", nameList));
                            break;
                    }
                }, saveButtonId)
                .show();


        CustomRecycler<ParentClass> customRecycler_selectList = new CustomRecycler<>(context, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = new CustomRecycler<String>(context, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemLayout(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .hideDivider()
                .setSetItemContent((itemView, uuid) -> {
                    ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(getObjectFromDatabase(category, uuid).getName());
                    dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((customRecycler, view, object, index) -> {
                    Toast.makeText(context,
                            "Halten zum abwählen", Toast.LENGTH_SHORT).show();
                })
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    ((CustomRecycler.MyAdapter) customRecycler.getRecycler().getAdapter()).removeItemAt(index);
                    selectedUuidList.remove(object);

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectList.reload();
                })
                .generate();


        customRecycler_selectList
                .setItemLayout(R.layout.list_item_select_actor)
                .setMultiClickEnabled(true)
                .setGetActiveObjectList(() -> {
                    if (searchQuery[0].equals("")) {
                        allObjectsList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                        return allObjectsList;
                    }
                    return getMapFromDatabase(category).values().stream().filter(parentClass -> parentClass.getName().toLowerCase().contains(searchQuery[0].toLowerCase()))
                            .sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
                })
                .setSetItemContent((itemView, parentClass) -> {
                    ((TextView) itemView.findViewById(R.id.selectList_name)).setText(parentClass.getName());

                    ((CheckBox) itemView.findViewById(R.id.selectList_selected)).setChecked(selectedUuidList.contains(parentClass.getUuid()));
                })
                .setOnClickListener((customRecycler, view, parentClass, index) -> {
                    CheckBox checkBox = view.findViewById(R.id.selectList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedUuidList.contains(parentClass.getUuid()))
                        selectedUuidList.remove(parentClass.getUuid());
                    else
                        selectedUuidList.add(parentClass.getUuid());

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectedList.reload();
                })
                .generate();

        SearchView searchView = dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_search);
        searchView.setQueryHint(category.getPlural() + " durchsuchen");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchQuery[0] = s.trim();
                customRecycler_selectList.reload();

                return true;
            }
        });

        return dialog_AddActorOrGenre;
    }

    public static ParentClass getObjectFromDatabase(CategoriesActivity.CATEGORIES category, String uuid) {
        return getMapFromDatabase(category).get(uuid);
    }

    private static Map<String, ? extends ParentClass> getMapFromDatabase(CategoriesActivity.CATEGORIES category) {
        Database database = Database.getInstance();
        switch (category) {
            case DARSTELLER:
                return database.darstellerMap;
            case STUDIOS:
                return database.studioMap;
            case GENRE:
                return database.genreMap;
            case KNOWLEDGE_CATEGORIES:
                return database.knowledgeCategoryMap;
            case JOKE_CATEGORIES:
                return database.jokeCategoryMap;
            case SHOW_GENRES:
                return database.showGenreMap;
        }
        return null;
    }


    public static class Triple<A, B, C> {
        public A first;
        public B second;
        public C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    public static class Quadruple<A, B, C, D> {
        public A first;
        public B second;
        public C third;
        public D fourth;

        public Quadruple(A first, B second, C third, D fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }
    }


    //  ----- Pixels ----->
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void setMargins(View v, int links, int oben, int rechts, int unten) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(dpToPx(links), dpToPx(oben), dpToPx(rechts), dpToPx(unten));
            v.requestLayout();
        }
    }
//  <----- Pixels -----


    public static void sendText(AppCompatActivity activity, String text) {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, text);//
            activity.startActivity(Intent.createChooser(waIntent, "App auswählen"));
        } else {
            Toast.makeText(activity, "WhatsApp not found", Toast.LENGTH_SHORT).show();
        }

    }

    public static Date getDateFromJsonString(String key, JSONObject jsonObject) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(jsonObject.getString(key));
        } catch (ParseException | JSONException e) {
            return null;
        }
    }


    //  --------------- getViews --------------->
    public static <T extends View> ArrayList<T> getViewsByType(ViewGroup root, Class<T> tClass) {
        final ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup)
                result.addAll(getViewsByType((ViewGroup) child, tClass));

            if (tClass.isInstance(child))
                result.add(tClass.cast(child));
        }
        return result;
    }

    public static <T extends View> void applyToAllViews(ViewGroup root, Class<T> tClass, ApplyToAll<T> applyToAll) {
        getViewsByType(root, tClass).forEach(applyToAll::runApplyToAll);
    }

    public interface ApplyToAll<T extends View> {
        void runApplyToAll(T t);
    }
    //  <--------------- getViews ---------------


    //  --------------- SquareView --------------->
    enum EQUAL_MODE {
        HEIGHT, WIDTH, MAX, MIN
    }

    public static void squareView(View view) {
        squareView(view, EQUAL_MODE.MAX);
    }

    public static void squareView(View view, EQUAL_MODE equal_mode) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        int height = view.getMeasuredHeight();

        int matchParentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getHeight(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(wrapContentMeasureSpec_width, matchParentMeasureSpec_width);
        int width = view.getMeasuredWidth();
        switch (equal_mode) {
            case WIDTH:
                layoutParams.height = width;
                break;
            case HEIGHT:
                layoutParams.width = height;
                break;
            case MIN:
                int min = width < height ? width : height;
                layoutParams.width = min;
                layoutParams.height = min;
                break;
            case MAX:
                int max = width > height ? width : height;
                layoutParams.width = max;
                layoutParams.height = max;
                break;
        }
    }
    //  <--------------- SquareView ---------------



//    //  ------------------------- ViewAspectRatio ------------------------->
//    public static void applyAspectRatioWidth(View view, double aspectRatio){
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//        int height = view.getMeasuredHeight();
//
//        layoutParams.width = (int) (height * aspectRatio);
//    }
//    public static void applyAspectRatioHeight(View view, double aspectRatio){
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//
////        int matchParentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getHeight(), View.MeasureSpec.EXACTLY);
////        int wrapContentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
////        view.measure(wrapContentMeasureSpec_width, matchParentMeasureSpec_width);
////        int width = view.getMeasuredWidth();
//
//        view.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
//            int width = v1.getWidth();
//            int height = v1.getHeight();
////            layoutParams.width = width;
//
//            v1.getLayoutParams().height = (int) (width * aspectRatio);
//            v1.requestLayout();
////            view.setLayoutParams(layoutParams);
////            view.setMinimumHeight((int) (width * aspectRatio));
//        });
//
//    }
//    //  <------------------------- ViewAspectRatio -------------------------


    //  ------------------------- ScaleMatchingParent ------------------------->
    public static void scaleMatchingParentWidth(View view){
        view.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            ViewGroup parent = (ViewGroup) v.getParent();
            int parentWidth = parent.getWidth();

            v.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED));
            final double targetHeight = v.getMeasuredHeight();
            final double targetWidth = v.getMeasuredWidth();

            double width = v.getWidth();
            double height = v.getHeight();
            if (width == 0)
                return;
            double ratio = targetHeight / targetWidth;
//            double ratio = height / width;
//            view.setLayoutParams(new LinearLayout.LayoutParams(targetWidth, targetHeight));
            view.setLayoutParams(new LinearLayout.LayoutParams(parentWidth, (int) (parentWidth * ratio)));
        });
    }
    //  <------------------------- ScaleMatchingParent -------------------------


    //  --------------- DrawableBuilder --------------->
    public static Drawable drawableBuilder_rectangle(int color, int corners, boolean ripple) {
        DrawableBuilder drawableBuilder = new DrawableBuilder()
                .rectangle()
                .solidColor(color)
                .cornerRadius(Utility.dpToPx(corners));
        if (ripple) drawableBuilder
                .ripple()
                .rippleColor(0xF8868686);
        return drawableBuilder
                .build();
    }

    public static Drawable drawableBuilder_oval(int color) {
        return new DrawableBuilder()
                .oval()
                .solidColor(color)
                .build();
    }
    //  <--------------- DrawableBuilder ---------------


    //  --------------- ConcatCollections --------------->
    public interface GetCollections<T, V> {
        Collection<V> runGetCollections(T t);
    }

    public static <T, V> List<V> concatenateCollections(Collection<T> tCollection, GetCollections<T, V> getCollections) {
        List<Collection<V>> collectionList = new ArrayList<>();
        tCollection.forEach(t -> collectionList.add(getCollections.runGetCollections(t)));
        return collectionList.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> concatenateCollections(Collection<Collection<T>> collections) {
        return collections.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> concatenateCollections(List<T>... collections) {
        return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toList());
    }
    //  <--------------- ConcatCollections ---------------
}
