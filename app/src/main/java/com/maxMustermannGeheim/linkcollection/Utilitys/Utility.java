package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
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
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class Utility {

    //  --------------- isOnline --------------->
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
            return String.format(Locale.GERMANY,"%.0f €", amount);
        else
            return String.format(Locale.GERMANY,"%.2f €", amount);
    }


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

    interface InterceptOnClick{
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

    private static boolean  containedInActors(String query, List<String> actorUuids) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (database.darstellerMap.get(actorUUid).getName().equals(query))
                return true;
        }
        return false;
    }
    private static boolean  containedInGenre(String query, List<String> genreUuids) {
        Database database = Database.getInstance();
        for (String genreUUid : genreUuids) {
            if (database.genreMap.get(genreUUid).getName().equals(query))
                return true;
        }
        return false;
    }
    private static boolean  containedInStudio(String query, List<String> studioUuids) {
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


    //  --------------- FilmCalender --------------->
    public static void setupFilmCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Video> videoList, boolean openVideo) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

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
                selectedDate[0] = dateClicked;
                if (videoList.size() == 1)
                    setButtons(layout, calendarView.getEvents(dateClicked).size());
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        if (videoList.size() == 1)
            setButtons(layout, calendarView.getEvents(new Date()).size());

        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            videoList.get(0).addDate(selectedDate[0], false);
            calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                    , selectedDate[0].getTime(), videoList.get(0)));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1);
            Database.saveAll();
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            videoList.get(0).removeDate(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0);
            Database.saveAll();
        });
    }
    //  <--------------- FilmCalender ---------------

    //  --------------- EpisodeCalender --------------->
    public static void setupEpisodeCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Show.Episode> episodeList, boolean openEpisode) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

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
                selectedDate[0] = dateClicked;
                if (episodeList.size() == 1)
                    setButtons(layout, calendarView.getEvents(dateClicked).size());
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        if (episodeList.size() != 1)
            return;

        setButtons(layout, calendarView.getEvents(new Date()).size());

        Show.Episode episode = episodeList.get(0);
        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            episode.addDate(selectedDate[0], false);
            calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                    , selectedDate[0].getTime(), episode));
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 1);
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            episode.removeDate(selectedDate[0]);
            calendarView.removeEvents(selectedDate[0]);
            loadVideoList(calendarView.getEvents(selectedDate[0]), layout, customRecycler);
            setButtons(layout, 0);
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
        }
        else
            customRecycler.reload(eventList);

    }

    private static void setButtons(FrameLayout layout, int size) {
        layout.findViewById(R.id.dialog_editViews_add).setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.dialog_editViews_remove).setVisibility(size != 0 ? View.VISIBLE : View.GONE);
    }


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

    public static Date shiftTime(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();

    }
    //  <--------------- Time ---------------


    //  --------------- Toast --------------->
    public static Toast centeredToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        TextView v = toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
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

        List<String> selectedUuidList = new  ArrayList<>(preSelectedUuidList);
        List<ParentClass> allObjectsList;
        String editType_string = category.getPlural();
        final String[] searchQuery = {""};
        switch (category){
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
                                switch (category){
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
                                showEditItemDialog(context, addOrEditDialog,selectedUuidList, o, category);
                                Database.saveAll();
                            })
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setHint(finalEditType_string + "-Name")
                                    .setText(((SearchView) customDialog.findViewById(R.id.dialogAddPassenger_search)).getQuery().toString()))
                            .show();

                }, false)
                .addButton("Abbrechen", customDialog -> {})
                .addButton("Speichern", customDialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (category){
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
                            "Halten zum abwählen" , Toast.LENGTH_SHORT).show();
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


    public static class Triple<A,B,C> {
        public A first;
        public B second;
        public C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    public static class Quadruple<A,B,C,D> {
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

    public static void setMargins (View v, int links, int oben, int rechts, int unten) {
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
}
