package com.maxMustermannGeheim.linkcollection.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomUtility;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.innovattic.rangeseekbar.RangeSeekBar;
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
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.pixplicity.sharp.Sharp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import top.defaults.drawabletoolbox.DrawableBuilder;


public class Utility {

    //  --------------- isOnline --------------->
    static public boolean isOnline(Context context) {
        if (isOnline()) {
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void isOnline(Runnable onTrue, Runnable onFalse) {

    }

    public static void isOnline(OnResult onResult) {

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
                if (context != null)
                    Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
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

    public static String hash(String s) {
        return Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();
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

    public static void applyCategoriesLink(AppCompatActivity activity, CategoriesActivity.CATEGORIES category, TextView textView, List<String> idList, Map<String, ? extends ParentClass> map) {
        CustomList<ParentClass> list = idList.stream().map(map::get).collect(Collectors.toCollection(CustomList::new));


        SpannableStringBuilder builder = new SpannableStringBuilder();

        final Boolean[] longPress = {null};
        for (ParentClass parentClass : list) {
            String s = parentClass.getName();
            if (builder.length() != 0)
                builder.append(", ");

            ClickableSpan clickableSpan = new ClickableSpan() {

                @Override
                public void onClick(View textView) {
                    if (longPress[0] != null && longPress[0]) {
                        longPress[0] = false;
                        Runnable openCategoriesActivity = () -> {
                            Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                            activity.startActivity(new Intent(activity, CategoriesActivity.class)
                                    .putExtra(MainActivity.EXTRA_CATEGORY, category)
                                    .putExtra(CategoriesActivity.EXTRA_SEARCH, s));
                        };

                        if (!(parentClass instanceof Darsteller) || ((Darsteller) parentClass).getTmdbId() == 0)
                            openCategoriesActivity.run();
                        else {
                            CustomDialog.Builder(activity)
                                    .setTitle(s)
                                    .enableStackButtons()
                                    .disableButtonAllCaps()
                                    .addButton("Filme Suchen", customDialog -> activity.startActivity(new Intent(activity, activity.getClass())
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, category)
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH, s)))
                                    .addButton("Kategorie Öffnen", customDialog -> openCategoriesActivity.run())
                                    .addButton("Filme Aktuallisieren", customDialog ->
                                            VideoActivity.addActorToAll(activity, ((ParentClass_Tmdb) parentClass), category))
                                    .show();
                        }

                    } else if (longPress[0] != null && !longPress[0]) {
                        longPress[0] = null;
                    } else {
                        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                        activity.startActivity(new Intent(activity, activity.getClass())
                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, category)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH, s));
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            builder.append(s, clickableSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        GestureDetector detector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                longPress[0] = true;
                textView.onTouchEvent(e);
                e.setAction(MotionEvent.ACTION_UP);
                textView.onTouchEvent(e);
            }
        });
        textView.setOnTouchListener((v, event) -> detector.onTouchEvent(event));

        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setLinkTextColor(textView.getTextColors());
//                activity.getResources().getColorStateList(R.color.clickable_text_color, null));
    }


    //  ------------------------- TMDb Api ------------------------->
    public static void importTmdbGenre(Context context, boolean isVideo) {
        String requestUrl = "https://api.themoviedb.org/3/genre/" +
                (isVideo ? "movie" : "tv") +
                "/list?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();


        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("genres");

                if (results.length() == 0) {
                    Toast.makeText(context, "Keine Ergebnisse gefunden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isVideo) {
                    Map<String, Genre> genreMap = Database.getInstance().genreMap;
                    CustomList<Genre> list = new CustomList<>(genreMap.values());

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject object = results.getJSONObject(i);

                        int id = object.getInt("id");
                        String name = object.getString("name");

                        Optional<Genre> optional = list.stream().filter(genre -> genre.getName().toLowerCase().equals(name.toLowerCase())).findFirst();
                        if (optional.isPresent()) {
                            optional.get().setTmdbGenreId(id);
                        } else {
                            optional = list.stream().filter(genre -> genre.getTmdbGenreId() == id).findFirst();
                            if (optional.isPresent()) {
                                optional.get().setName(name);
                            } else {
                                Genre genre = new Genre(name).setTmdbGenreId(id);
                                genreMap.put(genre.getUuid(), genre);
                            }
                        }
                    }
                } else {
                    Map<String, ShowGenre> genreMap = Database.getInstance().showGenreMap;
                    CustomList<ShowGenre> list = new CustomList<>(genreMap.values());

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject object = results.getJSONObject(i);

                        int id = object.getInt("id");
                        String name = object.getString("name");

                        Optional<ShowGenre> optional = list.stream().filter(genre -> genre.getName().toLowerCase().equals(name.toLowerCase())).findFirst();
                        if (optional.isPresent()) {
                            optional.get().setTmdbGenreId(id);
                        } else {
                            optional = list.stream().filter(genre -> genre.getTmdbGenreId() == id).findFirst();
                            if (optional.isPresent()) {
                                optional.get().setName(name);
                            } else {
                                ShowGenre genre = new ShowGenre(name).setTmdbGenreId(id);
                                genreMap.put(genre.getUuid(), genre);
                            }
                        }
                    }
                }

                Toast.makeText(context, "Genre Importiert", Toast.LENGTH_SHORT).show();
                Database.saveAll();
            } catch (JSONException ignored) {
            }

        }, error -> Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);

    }
    //  <------------------------- TMDb Api -------------------------

    //  ------------------------- watchLater ------------------------->
    public static List<String> getWatchLaterList_uuid() {
        return getWatchLaterList().stream().map(ParentClass::getUuid).collect(Collectors.toList());
    }

    public static CustomList<Video> getWatchLaterList() {
        if (!Database.isReady())
            return new CustomList<>();
        Database database = Database.getInstance();

        return database.videoMap.values().stream().filter(Video::isWatchLater).collect(Collectors.toCollection(CustomList::new));
    }
    //  <------------------------- watchLater -------------------------

    //  --------------- Copy --------------->
    public static <T> T deepCopy(T t) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        return (T) gson.fromJson(gson.toJson(t), t.getClass());
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
    //  ----- ... in Videos ----->
    public static boolean containedInVideo(String query, Video video, HashSet<VideoActivity.FILTER_TYPE> filterTypeSet) {
        if (video.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.NAME)) {
            if (containedInVideo(video.getName(), query, filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.ACTOR)) {
            if (containedInActors(query, video.getDarstellerList(), filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.GENRE)) {
            if (containedInGenre(query, video.getGenreList(), filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.STUDIO)) {
            return containedInStudio(query, video.getStudioList(), filterTypeSet.size() == 1);
        }
        return false;
    }

    private static boolean containedInVideo(String all, String sub, boolean exact) {
        if (exact)
            return all.equals(sub);
        else
            return all.toLowerCase().contains(sub.toLowerCase());
    }

    private static boolean containedInActors(String query, List<String> actorUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (exact) {
                if (database.darstellerMap.get(actorUUid).getName().equals(query))
                    return true;
            } else {
                if (database.darstellerMap.get(actorUUid).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }

        }
        return false;
    }

    private static boolean containedInGenre(String query, List<String> genreUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String genreUUid : genreUuids) {
            if (exact) {
                if (database.genreMap.get(genreUUid).getName().equals(query))
                    return true;
            } else {
                if (database.genreMap.get(genreUUid).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    private static boolean containedInStudio(String query, List<String> studioUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String studioUUid : studioUuids) {
            if (exact) {
                if (database.studioMap.get(studioUUid).getName().equals(query))
                    return true;
            } else {
                if (database.studioMap.get(studioUUid).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }
        return false;
    }
    //  <----- ... in Videos -----

    //  ----- ... in Knowledge ----->
    public static boolean containedInKnowledge(String query, Knowledge knowledge, HashSet<KnowledgeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.NAME) && knowledge.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
            for (String categoryId : knowledge.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CONTENT)) {
            if (knowledge.hasContent() && knowledge.getContent().toLowerCase().contains(query.toLowerCase()))
                return true;
            if (knowledge.hasItems() && knowledge.itemListToString_complete(true).toLowerCase().contains(query.toLowerCase()))
                return true;
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

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.NAME) && owe.getName().toLowerCase().contains(query.toLowerCase()))
            return true;

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.DESCRIPTION) && owe.getDescription().toLowerCase().contains(query.toLowerCase()))
            return true;

        return filterTypeSet.contains(OweActivity.FILTER_TYPE.PERSON) && owe.getItemList().stream().anyMatch(item -> Database.getInstance().personMap.get(item.getPersonId()).getName().toLowerCase().contains(query.toLowerCase()));

//        if (filterTypeSet.containedInVideo(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
////            for (ParentClass category : getMapFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES).values()) {
//            for (String categoryId : owe.getCategoryIdList()) {
//                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().containedInVideo(query))
//                    return true;
//            }
//        }
    }
    //  <----- ... in Owe -----

    //  ----- ... in Joke ----->
    public static boolean containedInJoke(String query, Joke joke, HashSet<JokeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.NAME) && joke.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.PUNCHLINE) && joke.getPunchLine().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.CATEGORY)) {
            for (String categoryId : joke.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.JOKE_CATEGORIES, categoryId).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }

        return false;
    }
    //  <----- ... in Joke -----

    //  ----- ... in Show ----->
    public static boolean containedInShow(String query, Show show, HashSet<ShowActivity.FILTER_TYPE> filterTypeSet) {
        if (show.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.NAME) && show.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        Database database = Database.getInstance();
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.GENRE) && show.getGenreIdList().stream().anyMatch(uuid -> database.showGenreMap.get(uuid).getName().toLowerCase().contains(query.toLowerCase())))
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

        currentDate = Utility.removeTime(new Date());
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Database database = Database.getInstance();
        CustomRecycler customRecycler = new CustomRecycler<>(context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_video)
                .setSetItemContent((customRecycler1, itemView, object) -> {
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

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);

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

        currentDate = Utility.removeTime(new Date());
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Database database = Database.getInstance();
        CustomRecycler<Event> customRecycler = new CustomRecycler<Event>(context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_episode)
                .setSetItemContent((customRecycler1, itemView, event) -> {
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
                ((AppCompatActivity) context).startActivityForResult(new Intent(context, ShowActivity.class)
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
        TextView calender_noTrips = layout.findViewById(R.id.fragmentCalender_noViews);

        if (eventList.isEmpty()) {
            calender_noTrips.setVisibility(View.VISIBLE);
        } else {
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

        View dialog_editViews_previous = layout.findViewById(R.id.dialog_editViews_previous);
        dialog_editViews_previous.setOnClickListener(v -> {
            Date previous = dateList.stream().filter(date -> date.before(currentDate)).collect(Collectors.toCollection(CustomList::new)).getLast();
            if (previous != null) {
                calendarView.setCurrentDate(previous);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(previous));
                currentDate = previous;
                setButtons(layout, size, calendarView, list, customRecycler);
                loadVideoList(calendarView.getEvents(previous), layout, customRecycler);

            }
        });

        View dialog_editViews_next = layout.findViewById(R.id.dialog_editViews_next);
        dialog_editViews_next.setOnClickListener(v -> {
            Date next = dateList.stream().filter(date -> date.after(currentDate)).findFirst().orElse(null);
            if (next != null) {
                calendarView.setCurrentDate(next);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(next));
                currentDate = next;
                setButtons(layout, size, calendarView, list, customRecycler);
                loadVideoList(calendarView.getEvents(next), layout, customRecycler);
            }
        });

        dialog_editViews_previous.setAlpha(dateList.stream().anyMatch(date -> date.before(currentDate)) ? 1f : 0.5f);
        dialog_editViews_next.setAlpha(dateList.stream().anyMatch(date -> date.after(currentDate)) ? 1f : 0.5f);

        OnHorizontalSwipeTouchListener touchListener = new OnHorizontalSwipeTouchListener(layout.getContext()) {
            @Override
            public void onSwipeRight() {
                dialog_editViews_previous.callOnClick();
            }

            @Override
            public void onSwipeLeft() {
                dialog_editViews_next.callOnClick();
            }
        };
        layout.findViewById(R.id.fragmentCalender_viewLayout).setOnTouchListener(touchListener);
        customRecycler.getRecycler().setOnTouchListener(touchListener);
        layout.findViewById(R.id.fragmentCalender_noViews).setOnTouchListener(touchListener);
    }



    public static class OnHorizontalSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        Runnable cancelTouch;


        public OnHorizontalSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());

        }



        @Override
        public boolean onTouch(View v, MotionEvent event) {
            cancelTouch = () -> {
                event.setAction(MotionEvent.ACTION_CANCEL);
                v.onTouchEvent(event);
            };
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 200;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return true;
//            }



            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY(  );
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
//                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffY > 0) {
//                            onSwipeBottom();
//                        } else {
//                            onSwipeTop();
//                        }
//                        result = true;
//                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (!result && cancelTouch != null)
                    cancelTouch.run();
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

//        public void onSwipeTop() {
//        }
//
//        public void onSwipeBottom() {
//        }
    }


    //  ------------------------- Checks ------------------------->
    public static boolean isUrl(String text) {
        return text.matches("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$");
    }
    //  <------------------------- Checks -------------------------


    //  ------------------------- String ------------------------->
    public static String subString(String s, int start) {
        if (start < 0)
            start = s.length() + start;
        return s.substring(start);
    }

    public static String subString(String s, int start, int end) {
        if (start < 0)
            start = s.length() + start;
        if (end < 0)
            end = s.length() + end;

        return s.substring(start, end);
    }

    public static String stringReplace(String source, int start, int end, String replacement) {
        return source.substring(0, start) + replacement + source.substring(end);
    }
    //  <------------------------- String -------------------------

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

    public static boolean isUpcoming(Date date) {
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

        CustomDialog dialog_AddActorOrGenre = CustomDialog.Builder(context)
                .setTitle(editType_string + " Bearbeiten")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .disableScroll()
                .addButton("Hinzufügen", customDialog -> {
                    CustomDialog.Builder(context)
                            .setTitle(editType_string + " Hinzufügen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                ParentClass parentClass = ParentClass.newCategory(category, customDialog1.getEditText());
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
                                customDialog.dismiss();
                                showEditItemDialog(context, addOrEditDialog, selectedUuidList, o, category);
                                Database.saveAll();
                            })
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setHint(editType_string + "-Name")
                                    .setText(((SearchView) customDialog.findViewById(R.id.dialogEditCategory_search)).getQuery().toString()))
                            .show();

                }, false)
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (category) {
                        case DARSTELLER:
                            ((Video) o).setDarstellerList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.darstellerMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_actor)).setText(String.join(", ", nameList));
                            break;
                        case STUDIOS:
                            ((Video) o).setStudioList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.studioMap.get(uuid).getName()));
                            ((TextView) addOrEditDialog.findViewById(R.id.dialog_editOrAddVideo_studio)).setText(String.join(", ", nameList));
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


        CustomRecycler<ParentClass> customRecycler_selectList = new CustomRecycler<>(context, dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectCategories));

        CustomRecycler customRecycler_selectedList = new CustomRecycler<String>(context, dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectedCategories))
                .setItemLayout(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .hideDivider()
                .setSetItemContent((customRecycler, itemView, uuid) -> {
                    ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(getObjectFromDatabase(category, uuid).getName());
                    dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
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
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
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
                .setSetItemContent((customRecycler, itemView, parentClass) -> {
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
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectedList.reload();
                })
                .generate();

        SearchView searchView = dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_search);
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

        public A getFirst() {
            return first;
        }

        public Triple<A, B, C> setFirst(A first) {
            this.first = first;
            return this;
        }

        public B getSecond() {
            return second;
        }

        public Triple<A, B, C> setSecond(B second) {
            this.second = second;
            return this;
        }

        public C getThird() {
            return third;
        }

        public Triple<A, B, C> setThird(C third) {
            this.third = third;
            return this;
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

        public A getFirst() {
            return first;
        }

        public Quadruple<A, B, C, D> setFirst(A first) {
            this.first = first;
            return this;
        }

        public B getSecond() {
            return second;
        }

        public Quadruple<A, B, C, D> setSecond(B second) {
            this.second = second;
            return this;
        }

        public C getThird() {
            return third;
        }

        public Quadruple<A, B, C, D> setThird(C third) {
            this.third = third;
            return this;
        }

        public D getFourth() {
            return fourth;
        }

        public Quadruple<A, B, C, D> setFourth(D fourth) {
            this.fourth = fourth;
            return this;
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


    //  ------------------------- LoadImageFromUrl ------------------------->
//    public static void loadImageFromUrl(Context context, ImageView imageView, String url, boolean canFullsize){
//        requestBuilder = Glide.with(mActivity)
//                .using(Glide.buildStreamModelLoader(Uri.class, mActivity), InputStream.class)
//                .from(Uri.class)
//                .as(SVG.class)
//                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
//                .sourceEncoder(new StreamEncoder())
//                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
//                .decoder(new SvgDecoder())
//                .placeholder(R.drawable.ic_facebook)
//                .error(R.drawable.ic_web)
//                .animate(android.R.anim.fade_in)
//                .listener(new SvgSoftwareLayerSetter<Uri>());
//    }
    private static OkHttpClient httpClient;

    public static void fetchSvg(Context context, String url, final ImageView target) {
        if (httpClient == null) {
            // Use cache for performance and basic offline capability
            httpClient = new OkHttpClient.Builder()
                    .cache(new Cache(context.getCacheDir(), 5 * 1024 * 1014))
                    .build();
        }

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                target.setImageResource(R.drawable.ic_download);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream stream = response.body().byteStream();
                Sharp.loadInputStream(stream).into(target);
                stream.close();
            }
        });
    }
    //  <------------------------- LoadImageFromUrl -------------------------


    //  ------------------------- FilterStars ------------------------->
    public static CustomDialog showRangeSelectDialog(Context context, SearchView searchView) {
        boolean[] singleMode = {false};
        final int[] min = {0};
        final int[] max = {20};
        boolean preSelected = false;
        Matcher matcher = VideoActivity.pattern.matcher(searchView.getQuery());
        if (matcher.find()) {
            String[] range = matcher.group(0).replaceAll("\\*", "").replaceAll(",", ".").split("-");
            min[0] = Math.round(Float.parseFloat(range[0]) * 4);
            max[0] = Math.round(Float.parseFloat(range.length < 2 ? range[0] : range[1]) * 4);
            preSelected = true;
            singleMode[0] = range.length < 2;
        }

        boolean finalPreSelected = preSelected;
        return CustomDialog.Builder(context)
                .setTitle("Nach Bewertung Filtern")
                .setView(R.layout.dialog_filter_by_rating)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextView rangeText = customDialog.findViewById(R.id.dialog_filterByRating_range);
                    CustomUtility.GenericInterface<Pair<Integer, Integer>> setText = pair -> {
                        if (singleMode[0])
                            rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆", pair.first / 4d));
                        else
                            rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆ – %.2f ☆", pair.first / 4d, pair.second / 4d));
                    };
                    RangeSeekBar rangeBar = customDialog.findViewById(R.id.dialog_filterByRating_rangeBar);
                    SeekBar singleBar = customDialog.findViewById(R.id.dialog_filterByRating_singleBar);
                    singleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                rangeBar.setMinThumbValue(progress);
                                setText.runGenericInterface(Pair.create(progress, progress));
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
                        singleBar.setClickable(singleMode[0]);
                        setText.runGenericInterface(Pair.create(rangeBar.getMinThumbValue(), rangeBar.getMaxThumbValue()));
                    });
                    singleBar.setProgress(min[0]);
                    rangeBar.setMaxThumbValue(max[0]);
                    rangeBar.setMinThumbValue(min[0]);
                    setText.runGenericInterface(Pair.create(min[0], max[0]));

                    rangeBar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
                        @Override
                        public void onStartedSeeking() {

                        }

                        @Override
                        public void onStoppedSeeking() {
                        }

                        @Override
                        public void onValueChanged(int min, int max) {
                            setText.runGenericInterface(Pair.create(min, max));
                            singleBar.setProgress(min);
                        }
                    });
                })
                .addOptionalModifications(customDialog -> {
                    if (finalPreSelected) {
                        customDialog
                                .addButton("Reset", customDialog1 -> {
                                    String removedQuery = searchView.getQuery().toString().replaceAll(VideoActivity.pattern.pattern(), "").trim();
                                    searchView.setQuery(removedQuery, true);
                                    })
                                .alignPreviousButtonsLeft();
                    }
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                    RangeSeekBar rangeBar = customDialog.findViewById(R.id.dialog_filterByRating_rangeBar);

                    min[0] = rangeBar.getMinThumbValue();
                    max[0] = rangeBar.getMaxThumbValue();

                    String removedQuery = searchView.getQuery().toString().replaceAll(VideoActivity.pattern.pattern(), "").trim();
                    if (min[0] == 0 && max[0] == 20) {
                        if (finalPreSelected)
                            searchView.setQuery(removedQuery, true);
                        return;
                    }

                    String filter;
                    if (singleMode[0])
                        filter = String.format(Locale.getDefault(), "*%.2f*", min[0] / 4d);
                    else
                        filter = String.format(Locale.getDefault(), "*%.2f-%.2f*", min[0] / 4d, max[0] / 4d);
                    searchView.setQuery(removedQuery + (removedQuery.isEmpty() ? "" : " ") + filter, true);

                })
                .setOnTouchOutside(CustomDialog::dismiss)
                .setDismissWhenClickedOutside(false)
                .show();

    }
    //  <------------------------- FilterStars -------------------------


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
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view._getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//        int height = view.getMeasuredHeight();
//
//        layoutParams.width = (int) (height * aspectRatio);
//    }
//    public static void applyAspectRatioHeight(View view, double aspectRatio){
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//
////        int matchParentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(((View) view._getParent()).getHeight(), View.MeasureSpec.EXACTLY);
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
    public static void scaleMatchingParentWidth(View view) {
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


    //  --------------- Generated Visuals --------------->
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

    public static int setAlphaOfColor(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }
    //  <--------------- Generated Visuals ---------------


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


    //  ------------------------- ifNotNull ------------------------->
    public static <E> boolean ifNotNull(E e, ExecuteIfNotNull<E> executeIfNotNull) {
        if (e == null)
            return false;
        executeIfNotNull.runExecuteIfNotNull(e);
        return true;
    }

    public static <E> boolean ifNotNull(E e, ExecuteIfNotNull<E> executeIfNotNull, Runnable executeIfNull) {
        if (e == null) {
            executeIfNull.run();
            return false;
        }
        executeIfNotNull.runExecuteIfNotNull(e);
        return true;
    }

    public interface ExecuteIfNotNull<E> {
        void runExecuteIfNotNull(E e);
    }

    public static boolean ignoreNull(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
    //  <------------------------- ifNotNull -------------------------


    //  ------------------------- Reflections ------------------------->
    public static List<TextWatcher> removeTextListeners(TextView view) {
        List<TextWatcher> returnList = null;
        try {
            Field mListeners = TextView.class.getDeclaredField("mListeners");
            mListeners.setAccessible(true);
            returnList = (List<TextWatcher>) mListeners.get(view);
            mListeners.set(view, null);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return returnList;
    }
    //  <------------------------- Reflections -------------------------


    //  ------------------------- EasyLogic ------------------------->
    public static class NoArgumentException extends RuntimeException {
        public static final String DEFAULT_MESSAGE = "Keine Argumente mitgegeben";

        public NoArgumentException(String message) {
            super(message);
        }
    }

    public static <T> boolean boolOr(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (what.runGenericInterface(o))
                return true;
        }
        return false;

    }

    public static <T> boolean boolOr(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (Objects.equals(what, o))
                return true;
        }
        return false;
    }

    public static <T> boolean boolXOr(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        boolean found = false;
        for (T o : to) {
            if (what.runGenericInterface(o)) {
                if (found)
                    return false;
                found = true;
            }
        }
        return found;
    }

    public static <T> boolean boolXOr(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        boolean found = false;
        for (T o : to) {
            if (Objects.equals(what, o)) {
                if (found)
                    return false;
                found = true;
            }
        }
        return found;
    }

    public static <T> boolean boolAnd(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (!Objects.equals(what, o))
                return false;
        }
        return true;
    }

    public static <T> boolean boolAnd(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (!what.runGenericInterface(o))
                return false;
        }
        return true;
    }

    // ---

    public static boolean stringExists(CharSequence s) {
        return s != null && !s.toString().trim().isEmpty();
    }
    //  <------------------------- EasyLogic -------------------------


    //  ------------------------- Switch Expression ------------------------->
    public static class SwitchExpression<Input, Output> {
        private Input input;
        private CustomList<Pair<Input, Object>> caseList = new CustomList<>();
        private Object defaultCase;

        public SwitchExpression(Input input) {
            this.input = input;
        }

        public static <Input> SwitchExpression<Input, Object> setInput(Input input) {
            return new SwitchExpression<>(input);
        }

        //  ------------------------- Getters & Setters ------------------------->
        public Input getInput() {
            return input;
        }
        //  <------------------------- Getters & Setters -------------------------


        //  ------------------------- Cases ------------------------->
        public <Type> SwitchExpression<Input, Type> addCase(Input inputCase, ExecuteOnCase<Input, Type> executeOnCase) {
            caseList.add(new Pair<>(inputCase, executeOnCase));
            return (SwitchExpression<Input, Type>) this;
        }

        public <Type> SwitchExpression<Input, Type> addCase(Input inputCase, Type returnOnCase) {
            caseList.add(new Pair<>(inputCase, returnOnCase));
            return (SwitchExpression<Input, Type>) this;
        }

        public SwitchExpression<Input, Output> addCaseToLastCase(Input inputCase) {
            caseList.add(new Pair<>(inputCase, caseList.getLast().second));
            return this;
        }

        // ---------------

        public <Type> SwitchExpression<Input, Type> setDefault(ExecuteOnCase<Input, Type> defaultCase) {
            this.defaultCase = defaultCase;
            return (SwitchExpression<Input, Type>) this;
        }

        public <Type> SwitchExpression<Input, Type> setDefault(Type defaultCase) {
            this.defaultCase = defaultCase;
            return (SwitchExpression<Input, Type>) this;
        }

        public SwitchExpression<Input, Output> setLastCaseAsDefault() {
            defaultCase = caseList.getLast().second;
            return this;
        }

        // ---------------

        public interface ExecuteOnCase<Input, Output> {
            Output runExecuteOnCase(Input input);
        }
        //  <------------------------- Cases -------------------------


        public Output evaluate() {
            Optional<Pair<Input, Object>> optional = caseList.stream().filter(inputExecuteOnCasePair -> Objects.equals(input, inputExecuteOnCasePair.first)).findFirst();

            if (optional.isPresent()) {
                Object o = optional.get().second;
                if (o instanceof ExecuteOnCase)
                    return (Output) ((ExecuteOnCase) o).runExecuteOnCase(input);
                else
                    return (Output) o;
            } else if (defaultCase != null) {
                if (defaultCase instanceof ExecuteOnCase)
                    return (Output) ((ExecuteOnCase) defaultCase).runExecuteOnCase(input);
                else
                    return (Output) defaultCase;
            } else {
                return null;
            }
        }
    }
    //  <------------------------- Switch Expression -------------------------


    //  ------------------------- Interfaces ------------------------->
    public interface GenericInterface<T> {
        void runGenericInterface(T t);
    }

    public interface GenericReturnInterface<T, R> {
        R runGenericInterface(T t);
    }

    public interface GenericReturnOnlyInterface<T> {
        T runGenericInterface();
    }
    //  <------------------------- Interfaces -------------------------
}
