package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String TAG = "Database";

    public static final String SUCCSESS = "SUCCSESS";
    public static final String FAILED = "FAILED";

    private static Database database;
    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private static List<OnInstanceFinishedLoading> onInstanceFinishedLoadingList = new ArrayList<>();
    private static SharedPreferences mySPR_daten;
    private boolean online = false;
    private boolean loaded = false;
    private int loadingCount = 0;
    private static boolean reload = false;
    private static List<DatabaseReloadListener> reloadListenerList = new ArrayList<>();
    private static boolean syncDatabaseToContentMap = true;


//  ----- Content deklaration ----->
    public static final String VIDEOS = "VIDEOS";
    public static final String VIDEO_MAP = "VIDEO_MAP";
    public static final String STUDIO_MAP = "STUDIO_MAP";
    public static final String DARSTELLER_MAP = "DARSTELLER_MAP";
    public static final String GENRE_MAP = "GENRE_MAP";
    public static final String WATCH_LATER_LIST = "WATCH_LATER_LIST";
    public static final String DATABASE_CODE = "DATABASE_CODE";
    public static String databaseCode;
    public Map<String, Video> videoMap = new HashMap<>();
    public Map<String, Darsteller> darstellerMap = new HashMap<>();
    public Map<String, Studio> studioMap = new HashMap<>();
    public Map<String, Genre> genreMap = new HashMap<>();
    public List<String> watchLaterList = new ArrayList<>();
    
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
    
    private List<Content> contentList;
    {
        Content databaseCode_Content = new Content<String, String>(String.class, "databaseCode", DATABASE_CODE).setSaveOnline(false);
        contentList = Arrays.asList(
                databaseCode_Content
                , new Content<Map,Video>(Video.class, videoMap, databaseCode_Content, VIDEOS, VIDEO_MAP)
                , new Content<Map,Studio>(Studio.class, studioMap, databaseCode_Content, VIDEOS, STUDIO_MAP)
                , new Content<Map,Darsteller>(Darsteller.class, darstellerMap, databaseCode_Content, VIDEOS, DARSTELLER_MAP)
                , new Content<Map,Genre>(Genre.class, genreMap, databaseCode_Content, VIDEOS, GENRE_MAP)
                , new Content<List,String>(String.class, watchLaterList, databaseCode_Content, VIDEOS, WATCH_LATER_LIST)

                , new Content<Map, Knowledge>(Knowledge.class, knowledgeMap, databaseCode_Content, KNOWLEDGE, KNOWLEDGE_MAP)
                , new Content<Map, KnowledgeCategory>(KnowledgeCategory.class, knowledgeCategoryMap, databaseCode_Content, KNOWLEDGE, KNOWLEDGE_CATEGORY_MAP)

                , new Content<Map, Owe>(Owe.class, oweMap, databaseCode_Content, OWE, OWE_MAP)
                , new Content<Map, Person>(Person.class, personMap, databaseCode_Content, OWE, PERSON_MAP)
        );
    }
//  <----- Content deklaration -----

    private static Map<String, Content> contentMap = new HashMap<>();
    {for (Content content : contentList) contentMap.put(content.key, content);}


    private Database(boolean online) {
        Log.d(TAG, "Database: Construktor");
        Database.database = Database.this;
        if (online)
            startLoadDataFromFirebase();
    }

//  ----- getInstances ----->
    public static Database getInstance() {
//        Log.d(TAG, "getInstance: alt");
        return database;
    }

    public static Database getInstance(SharedPreferences mySPR_daten, OnInstanceFinishedLoading onInstanceFinishedLoading, boolean createNew) {
        Log.d(TAG, "getInstance: new " + onInstanceFinishedLoadingList.size());
        Database.mySPR_daten = mySPR_daten;

        databaseCode = mySPR_daten.getString(DATABASE_CODE, "");
        if (databaseCode.equals(""))
            return null;

        onInstanceFinishedLoadingList.add(onInstanceFinishedLoading);
        if (onInstanceFinishedLoadingList.size() > 1)
            return null;

        if (Database.exists())
            reload = true;

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
                if (!content.saveLocal) continue;

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
    public class Content<T, V> {
        public String fieldName;
        public String key;
        public Object content;
        public Boolean saveLocal = true;
        public Boolean saveOnline = true;
        public Class<V> tClass;
        private boolean reassign;
        public ChangeListener changeListener;
        public List<Object> path;

        private Content(Class<V> tClass, Object content, @NonNull Object... path) {
            reassign = false;
            this.content = content;
            this.tClass = tClass;
            this.path = Arrays.asList(path);
            this.key = (String) this.path.get(this.path.size() - 1);
        }

        private Content(Class<V> tClass, String fieldName, @NonNull Object... path) {
            reassign = true;
            this.fieldName = fieldName;
            this.tClass = tClass;
            this.path = Arrays.asList(path);
            this.key = (String) this.path.get(this.path.size() - 1);
        }

        //  ----- Get Map From ... ----->
        public HashMap<String, V> getMapFromDataSnapshot(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    V value = snapshot.getValue(tClass);
                    ((Map) content).put(((ParentClass) value).getUuid(), value);
                }
                return ((HashMap) content);
            }
            return null;
        }

        public Map<String, V> getMapFromString(String mapString) {
            Gson gson = new Gson();
            return gson.fromJson(mapString, TypeToken.getParameterized(HashMap.class, String.class, tClass).getType());
        }
        //  <----- Get Map From ... -----
        public List<V> getListFromDataSnapshot(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    V value = snapshot.getValue(tClass);
                    ((List) content).add(value);
                }
                return ((List) content);
            }
            return null;
        }

        public List<V> getListFromString(String mapString) {
            Gson gson = new Gson();
            return gson.fromJson(mapString, TypeToken.getParameterized(List.class, tClass).getType());
        }

    //  <----- Get List From ... -----

        //  ----- ChangeListener ----->
        public Content addChangeListener() {
            changeListener = new ChangeListener<>(tClass, (dataSnapshot, database1, changeListener1) -> {
                getMapFromDataSnapshot(dataSnapshot);
            }, key);
            return this;
        }

        public void removeChangeListener() {
            changeListener.removeOnChangeListener_firebase();
        }
        //  <----- ChangeListener -----


        public Map<String, V> updateMap(HashMap<String, V> contentMap) {
            ((HashMap) content).clear();
            ((HashMap) content).putAll(contentMap);
            return (HashMap<String, V>) content;
        }

        public List<V> updateList(List<V> contentMap) {
            ((List) content).clear();
            ((List) content).addAll(contentMap);
            return (List<V>) content;
        }

        public T getContent() {
            return (T) content;
        }

        private Object getCorrespondingValue() {
            Class<? extends Database> aClass = Database.this.getClass();
            try {
                Field field = aClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                content = field.get(this);
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalAccessException ignored) {
            }
            return content;
        }

        public ChangeListener getChangeListener() {
            return changeListener;
        }

        public String[] getPathArray() {
            return path.stream().map(o -> {
                if (o instanceof String)
                    return o;
                else if (o instanceof Content) {
                    return ((Content) o).getCorrespondingValue();
                }
                else
                    return null;
            }).toArray(String[]::new);
        }

        public Content setSaveLocal(Boolean saveLocal) {
            this.saveLocal = saveLocal;
            return this;
        }

        public Content setSaveOnline(Boolean saveOnline) {
            this.saveOnline = saveOnline;
            return this;
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
                    field.set(this,content.content);
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

    public static void saveAll() {
        Log.d(TAG, "saveAll: ");

        if (!Database.isReady() || !database.isOnline())
            return;

        database.saveDatabase_offline(mySPR_daten);
        if (Utility.isOnline())
            database.writeAllToFirebase();
    }

    private void saveDatabase_offline(SharedPreferences mySPR_daten) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = mySPR_daten.edit();

        for (Content content : getContentMap(true).values()) {
            if (!content.saveLocal) continue;

            if (content.content instanceof String)
                editor.putString(content.key, (String) content.content);
            else
                editor.putString(content.key, gson.toJson(content.content));
        }

        editor.apply();
    }

    private void writeAllToFirebase() {
        for (Content content : getContentMap(true).values()) {
            if (content.saveOnline)
                databaseCall_write(content.content, content.getPathArray());
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
                    if (dataSnapshot.getValue() != null) {
                        content.getMapFromDataSnapshot(dataSnapshot);
                    }
                    if (loadingCount == 0) {
                        online = true;
                        finishedLoading();
                    }
                }, content.getPathArray());
            }
            else if (content.getContent() instanceof List) {
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
        database.syncDatabaseAndContentMap();
        database.loaded = true;

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
    interface OnDatabaseCallFinished {
        void onFinished(DataSnapshot dataSnapshot);
    }

    interface OnDatabaseCallFailed {
        void onFailed(DatabaseError databaseError);
    }

    public static void databaseCall_read(OnDatabaseCallFinished onDatabaseCallFinished, String... stepList){
        accessChilds(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void databaseCall_read(OnDatabaseCallFinished onDatabaseCallFinished, OnDatabaseCallFailed onDatabaseCallFailed, String... stepList){
        accessChilds(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void databaseCall_write(Object object, String... stepList){
        accessChilds(databaseReference, stepList).setValue(object);
    }

    public static void databaseCall_delete(String... stepList){
        accessChilds(databaseReference, stepList).removeValue();
    }

    public static DatabaseReference accessChilds(DatabaseReference databaseReference, String... steps) {
        List<String> newSteps = new ArrayList<>(Arrays.asList(steps));
        if (newSteps.size() > 0) {
            DatabaseReference newDatabaseReference = databaseReference.child(newSteps.remove(0));
            return accessChilds(newDatabaseReference, newSteps.toArray(new String[newSteps.size()]));
        }
        return databaseReference;
    }

    public static Database.OnDatabaseCallFailed getStandardFail(Context context) {
        return databaseError -> Toast.makeText(context, "Datenbankabfrage gescheitert", Toast.LENGTH_SHORT).show();
    }
//  <----- Firebase Call -----


//  ----- Change Listener ----->
    public interface OnChangeListener {
        void runOnChangeListener();
    }
    public interface OnChangeListener_updateData {
        void runOnChangeListener_updateData(DataSnapshot dataSnapshot, Database database, ChangeListener<ParentClass> changeListener);
    }

    public class ChangeListener<T> {
        private final Class<T> tClass;
        private List<OnChangeListener> onChangeListenerList = new ArrayList<>();
        private boolean listenerApplied;
        private List<String> listenerPath;
        private OnChangeListener_updateData onChangeListener_updateData;
        private ChangeListener that = this;
        private ValueEventListener onChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!listenerApplied) {
                    listenerApplied = true;
                    if (!reload)
                        return;
                }

                onChangeListener_updateData.runOnChangeListener_updateData(dataSnapshot, database, that);

                fireOnChangeListeners();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };


        public ChangeListener(Class<T> tClass, OnChangeListener_updateData onChangeListener_updateData, String... listenerPath) {
            this.tClass = tClass;
            this.listenerPath = Arrays.asList(listenerPath);
            this.onChangeListener_updateData = onChangeListener_updateData;
            addOnChangeListener_firebase();
        }

        private void addOnChangeListener_firebase() {
            accessChilds(databaseReference, (String[]) this.listenerPath.toArray()).addValueEventListener(onChangeListener);
        }
        private void removeOnChangeListener_firebase() {
            accessChilds(databaseReference, (String[]) this.listenerPath.toArray()).removeEventListener(onChangeListener);
        }
        public OnChangeListener addOnChangeListener(OnChangeListener onChangeListener) {
            onChangeListenerList.add(onChangeListener);
            return onChangeListener;
        }
        public boolean removeOnChangeListener(OnChangeListener onChangeListener) {
            return onChangeListenerList.remove(onChangeListener);
        }
        private void fireOnChangeListeners(){
            saveDatabase_offline(mySPR_daten);
            onChangeListenerList.forEach(OnChangeListener::runOnChangeListener);
        }

        private Map<String, T> updateMap(DataSnapshot dataSnapshot) {
            HashMap<String, T> newMap = new HashMap<>();

            if (dataSnapshot.getValue() == null)
                return newMap;

            for (DataSnapshot snapshot :  dataSnapshot.getChildren()){
                T standardArticle = snapshot.getValue(tClass);
                newMap.put(((ParentClass) standardArticle).getUuid(), standardArticle);
            }
            return newMap;
        }
    }
//  <----- Change Listener -----


//  ----- Database-Reload-Listener ----->
    public interface DatabaseReloadListener {
        void runDatabaseReloadListener(Database database);
    }

    private static void fireDatabaseReloadListener() {
        reloadListenerList.forEach(databaseReloadListener ->  databaseReloadListener.runDatabaseReloadListener(database));
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


//  ----- Sonstige ----->
    public void generateData() {
    }
//  <----- Sonstige -----

}
