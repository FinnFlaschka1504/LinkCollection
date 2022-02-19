package com.maxMustermannGeheim.linkcollection.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomUtility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaEvent;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaTag;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

public class Database {
    private static final String TAG = "Database";

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    private static Database database;
    private static Map<String, Object> lastUploaded_contentMap;
    //    public static Set<Pair<Boolean,Object>> changePairSet = new HashSet<>();
    public static List<Utility.Quadruple<Boolean, String[], Object, Content>> updateList = new ArrayList<>();
    public static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private static List<OnInstanceFinishedLoading> onInstanceFinishedLoadingList = new ArrayList<>();
    private static SharedPreferences mySPR_daten;
    private boolean online = false;
    private boolean loaded = false;
    private int loadingCount = 0;
    private static boolean reload = false;
    private static List<DatabaseReloadListener> reloadListenerList = new ArrayList<>();
    private static boolean syncDatabaseToContentMap = true;
    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
    private Set<String> ignoreUpdateSet = new HashSet<>();


    //  ----- Content deklaration ----->
    public static final String ENCRYPTION = "ENCRYPTION";
    public static final String ENCRYPTION_PASSWORD = "ENCRYPTION_PASSWORD";
    public static final String ENCRYPTED_SPACES = "ENCRYPTED_SPACES";
    public static final String PASSWORD = "PASSWORD";
    public static final String DATABASE_CODE = "DATABASE_CODE";
    public static String databaseCode;


    public static final String VIDEOS = "VIDEOS";
    public static final String VIDEO_MAP = "VIDEO_MAP";
    public static final String STUDIO_MAP = "STUDIO_MAP";
    public static final String DARSTELLER_MAP = "DARSTELLER_MAP";
    public static final String GENRE_MAP = "GENRE_MAP";
    public static final String URL_PARSER_MAP = "URL_PARSER_MAP";
    public static final String CUSTOM_CODE_VIDEO_MAP = "CUSTOM_CODE_VIDEO_MAP";
    public static final String WATCH_LATER_LIST = "WATCH_LATER_LIST";
    public static final String COLLECTION_MAP = "COLLECTION_MAP";
    public static final String WATCH_LIST_MAP = "WATCH_LIST_MAP";
    public Map<String, Video> videoMap = new HashMap<>();
    public Map<String, Darsteller> darstellerMap = new HashMap<>();
    public Map<String, Studio> studioMap = new HashMap<>();
    public Map<String, Genre> genreMap = new HashMap<>();
    public Map<String, UrlParser> urlParserMap = new HashMap<>();
    public Map<String, CustomCode.CustomCode_Video> customCodeVideoMap = new HashMap<>();
    //    public List<String> watchLaterList = new ArrayList<>();
    public Map<String, Collection> collectionMap = new HashMap<>();
    public Map<String, WatchList> watchListMap = new HashMap<>();

    public static final String KNOWLEDGE = "KNOWLEDGE";
    public static final String KNOWLEDGE_MAP = "KNOWLEDGE_MAP";
    public static final String KNOWLEDGE_CATEGORY_MAP = "KNOWLEDGE_CATEGORY_MAP";
    public Map<String, Knowledge> knowledgeMap = new HashMap<>();
    public Map<String, KnowledgeCategory> knowledgeCategoryMap = new HashMap<>();

    public static final String OWE = "OWE";
    public static final String OWE_MAP = "OWE_MAP";
    public static final String PERSON_MAP = "PERSON_MAP";
    public Map<String, Owe> oweMap = new HashMap<>();
    public Map<String, Person> personMap = new HashMap<>();

    public static final String JOKE = "JOKE";
    public static final String JOKE_MAP = "JOKE_MAP";
    public static final String JOKE_CATEGORY_MAP = "JOKE_CATEGORY_MAP";
    public Map<String, Joke> jokeMap = new HashMap<>();
    public Map<String, JokeCategory> jokeCategoryMap = new HashMap<>();

    public static final String SHOWS = "SHOWS";
    public static final String SHOW_MAP = "SHOW_MAP";
    public static final String SHOW_GENRE_MAP = "SHOW_GENRE_MAP";
    public static final String SHOW_WATCH_LATER_LIST = "SHOW_WATCH_LATER_LIST";
    public Map<String, Show> showMap = new HashMap<>();
    public Map<String, ShowGenre> showGenreMap = new HashMap<>();
    public Map<String, Map<Integer, Map<String, Show.Episode>>> tempShowSeasonEpisodeMap = new HashMap<>();
    public List<String> showWatchLaterList = new ArrayList<>();

    public static final String MEDIA = "MEDIA";
    public static final String MEDIA_MAP = "MEDIA_MAP";
    public static final String MEDIA_PERSON_MAP = "MEDIA_PERSON_MAP";
    public static final String MEDIA_CATEGORY_MAP = "MEDIA_CATEGORY_MAP";
    public static final String MEDIA_TAG_MAP = "MEDIA_TAG_MAP";
    public static final String MEDIA_EVENT_MAP = "MEDIA_EVENT_MAP";
    public Map<String, Media> mediaMap = new HashMap<>();
    public Map<String, MediaPerson> mediaPersonMap = new HashMap<>();
    public Map<String, MediaCategory> mediaCategoryMap = new HashMap<>();
    public Map<String, MediaTag> mediaTagMap = new HashMap<>();
    public Map<String, MediaEvent> mediaEventMap = new HashMap<>();

    private List<Content> contentList;
    {
        Content databaseCode_content;
        contentList = Arrays.asList(
                databaseCode_content = new Content<String, String>(String.class, "databaseCode", DATABASE_CODE).setSaveOnline(false),
                new Content<Map, Video>(Video.class, videoMap, databaseCode_content, VIDEOS, VIDEO_MAP),
                new Content<Map, Studio>(Studio.class, studioMap, databaseCode_content, VIDEOS, STUDIO_MAP),
                new Content<Map, Darsteller>(Darsteller.class, darstellerMap, databaseCode_content, VIDEOS, DARSTELLER_MAP),
                new Content<Map, Genre>(Genre.class, genreMap, databaseCode_content, VIDEOS, GENRE_MAP),
                new Content<Map, UrlParser>(UrlParser.class, urlParserMap, databaseCode_content, VIDEOS, URL_PARSER_MAP),
                new Content<Map, CustomCode.CustomCode_Video>(CustomCode.CustomCode_Video.class, customCodeVideoMap, databaseCode_content, VIDEOS, CUSTOM_CODE_VIDEO_MAP),
                new Content<Map, Collection>(Collection.class, collectionMap, databaseCode_content, VIDEOS, COLLECTION_MAP),
                new Content<Map, WatchList>(WatchList.class, watchListMap, databaseCode_content, VIDEOS, WATCH_LIST_MAP),

                new Content<Map, Knowledge>(Knowledge.class, knowledgeMap, databaseCode_content, KNOWLEDGE, KNOWLEDGE_MAP),
                new Content<Map, KnowledgeCategory>(KnowledgeCategory.class, knowledgeCategoryMap, databaseCode_content, KNOWLEDGE, KNOWLEDGE_CATEGORY_MAP),

                new Content<Map, Owe>(Owe.class, oweMap, databaseCode_content, OWE, OWE_MAP),
                new Content<Map, Person>(Person.class, personMap, databaseCode_content, OWE, PERSON_MAP),

                new Content<Map, Joke>(Joke.class, jokeMap, databaseCode_content, JOKE, JOKE_MAP),
                new Content<Map, JokeCategory>(JokeCategory.class, jokeCategoryMap, databaseCode_content, JOKE, JOKE_CATEGORY_MAP),

                new Content<Map, Show>(Show.class, showMap, databaseCode_content, SHOWS, SHOW_MAP).enableChangeListener(ShowActivity.getShowChangeHandler()),
                new Content<Map, ShowGenre>(ShowGenre.class, showGenreMap, databaseCode_content, SHOWS, SHOW_GENRE_MAP),
                new Content<List, String>(String.class, showWatchLaterList, databaseCode_content, SHOWS, SHOW_WATCH_LATER_LIST),

                new Content<Map, Media>(Media.class, mediaMap, databaseCode_content, MEDIA, MEDIA_MAP),
                new Content<Map, MediaPerson>(MediaPerson.class, mediaPersonMap, databaseCode_content, MEDIA, MEDIA_PERSON_MAP),
                new Content<Map, MediaCategory>(MediaCategory.class, mediaCategoryMap, databaseCode_content, MEDIA, MEDIA_CATEGORY_MAP),
                new Content<Map, MediaTag>(MediaTag.class, mediaTagMap, databaseCode_content, MEDIA, MEDIA_TAG_MAP),
                new Content<Map, MediaEvent>(MediaEvent.class, mediaEventMap, databaseCode_content, MEDIA, MEDIA_EVENT_MAP)

//                new Content<Map, MediaEvent>(MediaEvent.class, testMap, databaseCode_content, "TEST_MAP")
        );
    }
    //  <----- Content deklaration -----


    private static Map<String, Content> contentMap = new HashMap<>();
    {
        for (Content content : contentList) contentMap.put(content.key, content);
    }

    // ToDo: Offline Modus mit Firebase https://firebase.google.com/docs/database/android/offline-capabilities

    private Database(boolean online) {
        Log.d(TAG, "Database: Constructor");
        Database.database = Database.this;
        if (online)
            startLoadDataFromFirebase();
    }

    //  ----- getInstances ----->
    public static Database getInstance() {
//        Log.d(TAG, "getInstance: alt");
        return database;
    }

    public static Database getInstance(SharedPreferences mySPR_daten, OnInstanceFinishedLoading onInstanceFinishedLoading) {
        return getInstance(mySPR_daten, onInstanceFinishedLoading, false);
    }

    public static Database getInstance(SharedPreferences mySPR_daten, OnInstanceFinishedLoading onInstanceFinishedLoading, boolean createNew) {
        Log.d(TAG, "getInstance: new " + onInstanceFinishedLoadingList.size());
        Database.mySPR_daten = mySPR_daten;

        databaseCode = mySPR_daten.getString(DATABASE_CODE, "");
        if (databaseCode.equals(""))
            return null;

        onInstanceFinishedLoadingList.add(onInstanceFinishedLoading);
        if (onInstanceFinishedLoadingList.size() > 1)
            return database;

        if (Database.exists())
            reload = true;

//        CustomUtility.isOnline(() -> {
//
//        }, () -> {
//
//        });
        if (Utility.isOnline()) {
            Log.d(TAG, "getInstance: online");
            if (reload) {
                database.startLoadDataFromFirebase();
            } else {
                new Database(true);
            }

            syncDatabaseToContentMap = true;
            return database;
        } else {
            Log.d(TAG, "getInstance: offline");
            if (!reload) {
                new Database(false);
            }

            for (Map.Entry<String, Content> entry : contentMap.entrySet()) {
                Content content = entry.getValue();
                if (!content.saveOffline) continue;

                String content_string = mySPR_daten.getString(content.key, "--Leer--");
                if (!content_string.equals("--Leer--")) {
                    if (content.content instanceof Map)
                        content.updateMap((HashMap) content.getMapFromString(content_string));
                    else if (content.content instanceof List)
                        content.updateList(content.getListFromString(content_string));
                    else if (content.tClass.getName().equals(String.class.getName()))
                        content.content = content_string;
                } else {
                    if (createNew)
                        return new Database(false);
                    else
                        return null;
                }

            }

            syncDatabaseToContentMap = false;
            database.online = false;
            finishedLoading();
            return database;
        }


    }
    //  <----- getInstances -----


    //  ----- Content management ----->
    public class Content<C, T> implements Cloneable {
        public String fieldName;
        public String key;
        public C content;
        public Boolean saveOffline = true;
        public Boolean saveOnline = true;
        public Class<T> tClass;
        private boolean reassign;
        public ChangeListener<T> changeListener;
        public List<Object> path;
        private ChangeHandler<C, T> changeHandler;

        private Content(Class<T> tClass, Object content, @NonNull Object... path) {
            reassign = false;
            this.content = (C) content;
            this.tClass = tClass;
            this.path = Arrays.asList(path);
            this.key = (String) this.path.get(this.path.size() - 1);
        }

        private Content(Class<T> tClass, String fieldName, @NonNull Object... path) {
            reassign = true;
            this.fieldName = fieldName;
            this.tClass = tClass;
            this.path = Arrays.asList(path);
            this.key = (String) this.path.get(this.path.size() - 1);
        }

        //  ----- Get Map From ... ----->
        public HashMap<String, T> getMapFromDataSnapshot(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    T value = snapshot.getValue(tClass);
                    ((Map) content).put(((ParentClass) value).getUuid(), value);
                }
                return ((HashMap) content);
            }
            return null;
        }

        public T getChildFromDataSnapshot(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                T value = dataSnapshot.getValue(tClass);
                if (changeHandler == null || !changeHandler.run(value, this))
                    ((Map) content).put(((ParentClass) value).getUuid(), value);
                return value;
            }
            return null;
        }

        public Map<String, T> getMapFromString(String mapString) {
            return gson.fromJson(mapString, TypeToken.getParameterized(HashMap.class, String.class, tClass).getType());
        }

        public List<T> getListFromDataSnapshot(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    T value = snapshot.getValue(tClass);
                    ((List) content).add(value);
                }
                return ((List) content);
            }
            return null;
        }

        public List<T> getListFromString(String mapString) {
            return gson.fromJson(mapString, TypeToken.getParameterized(List.class, tClass).getType());
        }
        //  <----- Get List From ... -----


        //  ----- ChangeListener ----->
        public Content<C, T> enableChangeListener() {
            return enableChangeListener(null);
        }

        public Content<C, T> enableChangeListener(ChangeHandler<C, T> changeHandler) {
            this.changeHandler = changeHandler;
            changeListener = new ChangeListener<>(tClass,
                    (dataSnapshot, database, changeListener) -> {
                        T change = getChildFromDataSnapshot(dataSnapshot);
                        if (content instanceof Map && change instanceof ParentClass)
                            change = (T) ((Map<?, ?>) content).get(((ParentClass) change).getUuid());
                        return change;
                    },
                    getPathArray());
            return this;
        }

        public void removeChangeListener() {
            changeListener.removeOnChangeListener_firebase();
        }
        //  <----- ChangeListener -----


        public Map<String, T> updateMap(HashMap<String, T> contentMap) {
            ((HashMap) content).clear();
            ((HashMap) content).putAll(contentMap);
            return (HashMap<String, T>) content;
        }

        public List<T> updateList(List<T> contentMap) {
            ((List) content).clear();
            ((List) content).addAll(contentMap);
            return (List<T>) content;
        }

        public C getContent() {
            return content;
        }

        private Object getCorrespondingValue() {
            Class<? extends Database> aClass = Database.this.getClass();
            try {
                Field field = aClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                content = (C) field.get(this);
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalAccessException ignored) {
            }
            return content;
        }

        public ChangeListener<T> getChangeListener() {
            return changeListener;
        }

        public String[] getPathArray() {
            return path.stream().map(o -> {
                if (o instanceof String)
                    return o;
                else if (o instanceof Content) {
                    return ((Content) o).getCorrespondingValue();
                } else
                    return null;
            }).toArray(String[]::new);
        }

        public Content<C, T> setSaveOffline(Boolean saveOffline) {
            this.saveOffline = saveOffline;
            return this;
        }

        public Content<C, T> setSaveOnline(Boolean saveOnline) {
            this.saveOnline = saveOnline;
            return this;
        }

        @NonNull
        @Override
        protected Content<C, T> clone() {
            try {
                return (Content) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    public Map<String, Content> syncDatabaseAndContentMap() {
        for (Content content : contentMap.values()) {
            if (!content.reassign) continue;

            Class<? extends Database> aClass = this.getClass();
            try {
                Field field = aClass.getDeclaredField(content.fieldName);
                field.setAccessible(true);
                if (syncDatabaseToContentMap)
                    content.content = field.get(this);
                else
                    field.set(this, content.content);
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalAccessException ignored) {
            }
        }
        return contentMap;
    }

    public Map<String, Content> getContentMap(boolean sync) {
        if (sync)
            syncDatabaseAndContentMap();
        return contentMap;
    }

    public Map<String, Object> getSimpleContentMap(boolean includeOnline, boolean includeOffline) {
        Map<String, Content> contentMap = getContentMap(true);
        Map<String, Object> simpleContentMap = new HashMap<>();
        for (Map.Entry<String, Content> contentEntry : contentMap.entrySet()) {
            if ((includeOnline && contentEntry.getValue().saveOnline) || (includeOffline && contentEntry.getValue().saveOffline))
                simpleContentMap.put(contentEntry.getKey(), contentEntry.getValue().getContent());
        }
        return simpleContentMap;
    }

    public Map<String, Content> deepCopyContentMap(boolean includeOnline, boolean includeOffline) {
        Map<String, Content> deepCopy = new HashMap<>();
        Map<String, Object> simpleContentMap = database.deepCopySimpleContentMap(includeOnline, includeOffline);

        for (Map.Entry<String, Content> entry : contentMap.entrySet()) {
            Content contentClone = entry.getValue().clone();
            contentClone.content = simpleContentMap.get(entry.getKey());
            deepCopy.put(entry.getKey(), contentClone);
        }

        return deepCopy;
    }

    public Map<String, Object> deepCopySimpleContentMap(boolean includeOnline, boolean includeOffline) {
        Map<String, Object> deepCopy = new HashMap<>();
//        CustomUtility.GenericInterface<Boolean> logTiming = CustomUtility.logTiming();
        Map<String, Object> simpleContentMap = database.getSimpleContentMap(includeOnline, includeOffline);
//        logTiming.run(true);
        HashMap<String, Object> hashMap = gson.fromJson(gson.toJson(simpleContentMap, HashMap.class), HashMap.class);
//        logTiming.run(true);
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            if (!(entry.getValue() instanceof LinkedTreeMap)) {
                deepCopy.put(entry.getKey(), entry.getValue());
            } else {
                Content content = contentMap.get(entry.getKey());
                String json = gson.toJson(entry.getValue());
                HashMap map = gson.fromJson(json, TypeToken.getParameterized(HashMap.class, String.class, content.tClass).getType());
                deepCopy.put(entry.getKey(), map);
            }
        }
//        logTiming.run(false);
        return deepCopy;
    }

    // ---------------

    public static Boolean saveAll_simple() { // ToDo: alle if abfragen für null rückgabe verbessern
        Boolean aBoolean = saveAll(false);
        if (aBoolean == null)
            return false;
        return aBoolean;
    }

    public static Boolean saveAll_simple(Context context) {
        Boolean aBoolean = saveAll(context);
        if (aBoolean == null)
            return false;
        return aBoolean;
    }

    @CheckForNull public static Boolean saveAll() { // ToDo: alle if abfragen für null rückgabe verbessern
        return saveAll(false);
    }

    @CheckForNull public static Boolean saveAll(boolean forceAll) {
        Log.d(TAG, "saveAll: ");

        if (!Database.isReady() || !database.isOnline())
            return null;
        else if (!forceAll && !Database.hasChanges())
            return false;

        CustomUtility.runMinPriorityThread(() -> database.saveDatabase_offline(mySPR_daten));

        if (Utility.isOnline()) {
            if (updateList.isEmpty() || forceAll)
                database.writeAllToFirebase(forceAll);
            else
                database.writeAllToFirebase(updateList);
        } else {
            updateList.clear();
            return null;
        }

        updateList.clear();

        CustomUtility.runMinPriorityThread(() -> lastUploaded_contentMap = database.deepCopySimpleContentMap(true, true));
//        logTiming.run(false);
        return true;
    }

    @CheckForNull public static Boolean saveAll(@Nullable Runnable... onSavedNothingFailed) {
        Boolean result = saveAll();
        if (onSavedNothingFailed != null && onSavedNothingFailed.length > 0) {
            if (result != null && result)
                Utility.runVarArgRunnable(0, onSavedNothingFailed);
            else if (result != null && !result)
                Utility.runVarArgRunnable(1, onSavedNothingFailed);
            else if (result == null)
                Utility.runVarArgRunnable(2, onSavedNothingFailed);
        }
        return result;
    }

    @CheckForNull public static Boolean saveAll(Context context, @Nullable String textSaved, @Nullable String textNothing, @Nullable String textFailed , @Nullable Runnable... onSavedNothingFailed) {
        return saveAllTextOnly(context, saveAll(onSavedNothingFailed), textSaved, textNothing, textFailed);
    }

    @CheckForNull public static Boolean saveAll(Context context, @Nullable Runnable... onSavedNothingFailed) {
        return saveAllTextOnly(context, saveAll(onSavedNothingFailed));
    }

    @CheckForNull public static Boolean saveAll(Context context, @Nullable CustomUtility.Triple<String, String, String> textSavedNothingFailed, @Nullable Runnable... onSavedNothingFailed){
        Boolean result = saveAll(onSavedNothingFailed);
        if (textSavedNothingFailed != null)
            saveAllTextOnly(context, result, textSavedNothingFailed.first, textSavedNothingFailed.second, textSavedNothingFailed.third);
        else
            saveAllTextOnly(context, result);
        return result;
    }

    @CheckForNull private static Boolean saveAllTextOnly(Context context, Boolean result, @Nullable String... textSavedNothingFailed){
        String text = "Err.";

        if (result != null && result) {
            if ((text = Utility.easyVarArgsOrElse(0, null, textSavedNothingFailed)) == null)
                text = "Gespeichert";
        } else if (result != null && !result) {
            if ((text = Utility.easyVarArgsOrElse(1, null, textSavedNothingFailed)) == null)
                text = "Nichts Gespeichert";
        } else if (result == null) {
            if ((text = Utility.easyVarArgsOrElse(2, null, textSavedNothingFailed)) == null)
                text = "Speichern Fehlgeschlagen";
        }

        if (CustomUtility.stringExists(text))
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        return result;
    }

    // ---------------

    private static boolean hasChanges() {
//        if (true)
//            return true;
        Log.d(TAG, "hasChanges: ");
        final boolean[] result = {false};
        for (Map.Entry<String, Content> contentEntry : contentMap.entrySet()) {
            Content content = contentEntry.getValue();
            Object contentObject_new = content.getContent();
            Object contentObject_old = lastUploaded_contentMap.get(contentEntry.getKey());
            if (contentObject_new instanceof Map && contentObject_old instanceof Map) {
                Map<String, Object> contentObjectMap_new = (Map) contentObject_new;
                Map<String, Object> contentObjectMap_old = (Map) contentObject_old;

                Set<Map.Entry<String, Object>> newEntries = contentObjectMap_new.entrySet();
                Set<Map.Entry<String, Object>> addedOrChangedEntries = new HashSet<>(newEntries);
                Set<Map.Entry<String, Object>> oldEntries = contentObjectMap_old.entrySet();
                Set<Map.Entry<String, Object>> removedEntries = new HashSet<>(oldEntries);
                Set<Map.Entry<String, Object>> changedEntries = new HashSet<>();
                Set<Map.Entry<String, Object>> addedEntries = new HashSet<>();

                addedOrChangedEntries.removeAll(oldEntries);
                addedOrChangedEntries.forEach(stringObjectEntry -> {
                    if (contentObjectMap_old.containsKey(stringObjectEntry.getKey())) {
                        changedEntries.add(stringObjectEntry);

                    } else {
                        addedEntries.add(stringObjectEntry);
                    }
                });

                removedEntries = removedEntries.stream().filter(stringObjectEntry -> !contentObjectMap_new.containsKey(stringObjectEntry.getKey())).collect(Collectors.toSet());

                if (!addedOrChangedEntries.isEmpty() || !removedEntries.isEmpty()) {
                    result[0] = true;

                    if (content.saveOnline) {
                        changedEntries.forEach(stringObjectEntry -> addChangeToList(content, null, stringObjectEntry.getValue()));
                        addedEntries.forEach(stringObjectEntry -> addChangeToList(content, true, stringObjectEntry.getValue()));
                        removedEntries.forEach(stringObjectEntry -> addChangeToList(content, false, stringObjectEntry.getValue()));
                    }
                }

            } else if (contentObject_new instanceof List && contentObject_old instanceof List) {
                List<Objects> contentObjectList_new = (List) contentObject_new;
                List<Objects> contentObjectList_old = (List) contentObject_old;

                List<Object> addedOrChangedList = new ArrayList<>(contentObjectList_new);
                List<Object> addedList = new ArrayList<>();
                List<Object> removedList = new ArrayList<>(contentObjectList_old);


                addedOrChangedList.removeAll(contentObjectList_old);

//                if (!addedOrChangedList.isEmpty() && addedOrChangedList.get(0) instanceof ParentClass) {
//                    for (Object o : addedOrChangedList) {
//                        ParentClass parent = (ParentClass) o;
//                    }
//                }
                addedList.addAll(addedOrChangedList);
                removedList.removeAll(contentObjectList_new);

                if (!addedOrChangedList.isEmpty() || !removedList.isEmpty()) {
                    result[0] = true;

                    if (content.saveOnline) {
//                addedList.forEach(o -> addChangeToList(content, true, ));
                        addChangeToList(content, null, contentObjectList_new);
                    }
                }

                // ToDo: ^^^^ Irgendwann beenden
            } else {
                if (!Objects.equals(contentObject_new, contentObject_old))
                    result[0] = true;
            }
        }

        return result[0];
    }

    private static void addChangeToList(Content content, Boolean type, Object o) {
        CustomList<String> path = new CustomList<>(content.getPathArray());
        if (o instanceof ParentClass) {
            path.add(new String[]{((ParentClass) o).getUuid()});
            if (Settings.Space.needsToBeEncrypted(o.getClass())) {
                o = gson.fromJson(gson.toJson(o), o.getClass());
                ((ParentClass) o).encrypt(Settings.mySPR_settings.getString(Settings.SETTING_SPACE_ENCRYPTION_PASSWORD, Settings.SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD));
            }
        }

        updateList.add(new Utility.Quadruple<>(type, path.toArray(new String[0]), o, content));
    }

    private void saveDatabase_offline(SharedPreferences mySPR_daten) {
        SharedPreferences.Editor editor = mySPR_daten.edit();

        for (Content content : getContentMap(true).values()) {
            if (!content.saveOffline) continue;

            if (content.content instanceof String)
                editor.putString(content.key, (String) content.content);
            else
                editor.putString(content.key, gson.toJson(content.content));
        }

        editor.apply();
    }

    private void writeAllToFirebase(List<Utility.Quadruple<Boolean, String[], Object, Content>> updateList) {
        for (Utility.Quadruple<Boolean, String[], Object, Content> quadruple : updateList) {
            if (quadruple.first == null || quadruple.first) {
                if (quadruple.fourth.changeListener != null)
                    ignoreUpdateSet.add("/" + String.join("/", quadruple.second));
                databaseCall_write(quadruple.third, quadruple.second);
            } else {
                databaseCall_delete(quadruple.second);
            }
        }
    }

    private void writeAllToFirebase(boolean copy) {
        Map<String, Content> contentMap;
        if (copy) {
            contentMap = deepCopyContentMap(true, false);

            String key = Settings.mySPR_settings.getString(Settings.SETTING_SPACE_ENCRYPTION_PASSWORD, Settings.SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD);
            Settings.Space.allSpaces.stream().filter(Settings.Space::isEncrypted).forEach(space -> {
                space.getAssociatedClasses().forEach(aClass -> {
                    contentMap.values().stream().filter(content -> content.tClass.isAssignableFrom(aClass)).findFirst().ifPresent(contentObject -> {
                        Object content = contentObject.getContent();
                        if (content instanceof Map) {
                            ((Map) content).values().forEach(o -> ((ParentClass) o).encrypt(key));
                        } else if (content instanceof List) {
                        }
                    });
                });
            });

        } else
            contentMap = getContentMap(true);

        for (Content content : contentMap.values()) {
            if (content.saveOnline) {
                databaseCall_write(content.content, content.getPathArray());
            }
        }
    }
    //  <----- Content management -----


    //  ----- checks ----->
    public boolean isOnline() {
        return online;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public static boolean exists() {
        return Database.getInstance() != null;
    }

    public static boolean isReady() {
        return exists() && database.isLoaded();
    }

    public static void destroy() {
        database = null;
    }
    //  <----- checks -----


    //  ----- Get data from Firebase ----->
    public interface OnInstanceFinishedLoading {
        void runOnInstanceFinishedLoading(Database database);
    }

    private void startLoadDataFromFirebase() {
        loaded = false;
        loadContentFromFirebase();
    }

    private void loadContentFromFirebase() {
        for (Content content : contentMap.values()) {
            if (!content.saveOnline) continue;

            if (content.getContent() instanceof Map) {
                Map map = (Map) content.getContent();
                map.clear();
                loadingCount++;
                Database.databaseCall_read(dataSnapshot -> {
                    loadingCount--;

                    content.getMapFromDataSnapshot(dataSnapshot);

                    if (loadingCount == 0) {
                        online = true;
                        finishedLoading();
                    }
                }, content.getPathArray());
            } else if (content.getContent() instanceof List) {
                List list = (List) content.getContent();
                list.clear();
                loadingCount++;
                Database.databaseCall_read(dataSnapshot -> {
                    loadingCount--;
                    if (dataSnapshot.getValue() != null) {
                        content.getListFromDataSnapshot(dataSnapshot);
                    }
                    if (loadingCount == 0) {
                        online = true;
                        finishedLoading();
                    }
                }, content.getPathArray());
            }
        }
    }

    private static void finishedLoading() {
        Log.d(TAG, "finishedLoading: " + reload);

        // decrypt
        if (database.online && Settings.isLoaded()) {
            String key = Settings.mySPR_settings.getString(Settings.SETTING_SPACE_ENCRYPTION_PASSWORD, Settings.SETTING_SPACE_ENCRYPTION_DEFAULT_PASSWORD);
            Settings.Space.allSpaces.stream().filter(Settings.Space::isEncrypted).forEach(space -> {
                space.getAssociatedClasses().forEach(aClass -> {
                    contentMap.values().stream().filter(content -> content.tClass.isAssignableFrom(aClass)).findFirst().ifPresent(contentObject -> {
                        Object content = contentObject.getContent();
                        if (content instanceof Map) {
                            ((Map) content).values().forEach(o -> ((ParentClass) o).decrypt(key));
                        } else if (content instanceof List) {
                            // ToDo: Auch für Listen implementieren
                        }
                    });
                });
            });
        }

        database.syncDatabaseAndContentMap();
        database.loaded = true;

        lastUploaded_contentMap = database.deepCopySimpleContentMap(true, true);

        if (reload)
            fireDatabaseReloadListener();

        new ArrayList<>(onInstanceFinishedLoadingList).forEach(onInstanceFinishedLoading -> {
            if (!reload)
                onInstanceFinishedLoading.runOnInstanceFinishedLoading(database);
            onInstanceFinishedLoadingList.remove(onInstanceFinishedLoading);
        });

    }
    //  <----- Get data from Firebase -----


    //  ----- Firebase Call ----->
    public interface OnDatabaseCallFinished {
        void onFinished(DataSnapshot dataSnapshot);
    }

    public interface OnDatabaseCallFailed {
        void onFailed(DatabaseError databaseError);
    }

    public static void databaseCall_read(OnDatabaseCallFinished onDatabaseCallFinished, String... stepList) {
        accessChildren(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDatabaseCallFinished.onFinished(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String test = null;
            }
        });
    }

    public static void databaseCall_read(OnDatabaseCallFinished onDatabaseCallFinished, OnDatabaseCallFailed onDatabaseCallFailed, String... stepList) {
        accessChildren(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDatabaseCallFinished.onFinished(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onDatabaseCallFailed.onFailed(databaseError);
            }
        });
    }

    public static void databaseCall_write(Object object, String... stepList) {
        accessChildren(databaseReference, stepList).setValue(object);
    }

    public static void databaseCall_delete(String... stepList) {
        accessChildren(databaseReference, stepList).removeValue();
    }

    public static DatabaseReference accessChildren(DatabaseReference databaseReference, String... steps) {
        List<String> newSteps = new ArrayList<>(Arrays.asList(steps));
        if (newSteps.size() > 0) {
            DatabaseReference newDatabaseReference = databaseReference.child(newSteps.remove(0));
            return accessChildren(newDatabaseReference, newSteps.toArray(new String[0]));
        }
        return databaseReference;
    }

    public static Database.OnDatabaseCallFailed getStandardFail(Context context) {
        return databaseError -> Toast.makeText(context, "Datenbankabfrage gescheitert", Toast.LENGTH_SHORT).show();
    }
    //  <----- Firebase Call -----


    //  ----- Change Listener ----->
    public interface ChangeHandler<C, T> {
        /**
         * @param newValue The new Object fom Firebase
         * @param content The contentWrapper
         * @return {@code true} if the update was handled, {@code false} otherwise
         */
        boolean run(T newValue, Content<C, T> content);
    }

    public interface OnChangeListener<T> {
        void run(T change);
    }

    public interface OnChangeListener_updateData<T> {
        T run(DataSnapshot dataSnapshot, Database database, ChangeListener<T> changeListener);
    }

    public class ChangeListener<T> {
        private final Class<T> tClass;
        private List<OnChangeListener<T>> onChangeListenerList = new ArrayList<>();
        private boolean listenerApplied;
        private List<String> listenerPath;
        private OnChangeListener_updateData<T> onChangeListener_updateData;
        private ChildEventListener childEventListener = new ChildEventListener() {

            // ToDo: Alle Listener (Add, Remove) Vervollständigen

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String BREAKPOINT = null;
                // ToDo: vielleicht hiermit die Map initialisieren
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (checkIgnoreUpdate(snapshot)/* && false*/) return;
                T change = onChangeListener_updateData.run(snapshot, database, ChangeListener.this);
                fireOnChangeListeners(change);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String BREAKPOINT = null;
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String BREAKPOINT = null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String BREAKPOINT = null;
            }
        };


        public ChangeListener(Class<T> tClass, OnChangeListener_updateData<T> onChangeListener_updateData, String... listenerPath) {
            this.tClass = tClass;
            this.listenerPath = Arrays.asList(listenerPath);
            this.onChangeListener_updateData = onChangeListener_updateData;
            addOnChangeListener_firebase();
        }

        private void addOnChangeListener_firebase() {
//            accessChildren(databaseReference, (String[]) this.listenerPath.toArray()).addValueEventListener(onChangeListener);
            accessChildren(databaseReference, (String[]) this.listenerPath.toArray()).addChildEventListener(childEventListener);
        }

        private void removeOnChangeListener_firebase() {
//            accessChildren(databaseReference, (String[]) this.listenerPath.toArray()).removeEventListener(onChangeListener);
            accessChildren(databaseReference, (String[]) this.listenerPath.toArray()).removeEventListener(childEventListener);
        }

        public ChangeListener<T> addOnChangeListener(OnChangeListener<T> onChangeListener) {
            onChangeListenerList.add(onChangeListener);
            return this;
        }

        public boolean removeOnChangeListener(OnChangeListener<T> onChangeListener) {
            return onChangeListenerList.remove(onChangeListener);
        }

        private void fireOnChangeListeners(T change) {
            CustomUtility.runMinPriorityThread(() -> database.saveDatabase_offline(mySPR_daten));
            onChangeListenerList.forEach(listener -> listener.run(change));
        }

        private Map<String, T> updateMap(DataSnapshot dataSnapshot) {
            HashMap<String, T> newMap = new HashMap<>();

            if (dataSnapshot.getValue() == null)
                return newMap;

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                T standardArticle = snapshot.getValue(tClass);
                newMap.put(((ParentClass) standardArticle).getUuid(), standardArticle);
            }
            return newMap;
        }

        private boolean checkIgnoreUpdate(DataSnapshot dataSnapshot) {
            @SuppressLint("RestrictedApi") String path = dataSnapshot.getRef().getPath().toString();
            return ignoreUpdateSet.remove(path);
        }
    }

    public static <T> OnChangeListener<T> addOnChangeListener(String contentKey, OnChangeListener<T> onChangeListener) {
        ChangeListener<T> changeListener = contentMap.get(contentKey).changeListener;
        if (changeListener != null)
            changeListener.addOnChangeListener(onChangeListener);
        return onChangeListener;
    }

    public static <T> OnChangeListener<T> removeOnChangeListener(String contentKey, OnChangeListener<T> onChangeListener) {
        ChangeListener<T> changeListener = contentMap.get(contentKey).changeListener;
        if (changeListener != null)
            changeListener.removeOnChangeListener(onChangeListener);
        return onChangeListener;
    }
    //  <----- Change Listener -----


    //  ----- Database-Reload-Listener ----->
    public interface DatabaseReloadListener {
        void runDatabaseReloadListener(Database database);
    }

    private static void fireDatabaseReloadListener() {
        reloadListenerList.forEach(databaseReloadListener -> databaseReloadListener.runDatabaseReloadListener(database));
    }

    public static DatabaseReloadListener addDatabaseReloadListener(DatabaseReloadListener databaseReloadListener) {
        reloadListenerList.add(databaseReloadListener);
        return databaseReloadListener;
    }

    public static void removeDatabaseReloadListener(DatabaseReloadListener databaseReloadListener) {
        if (databaseReloadListener == null)
            reloadListenerList.clear();
        else
            reloadListenerList.remove(databaseReloadListener);
    }
    //  <----- Database-Reload-Listener -----
}
