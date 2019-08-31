package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxMustermannGeheim.linkcollection.Daten.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Studio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    public static final String SUCCSESS = "SUCCSESS";
    public static final String FAILED = "FAILED";

    public static final String VIDEO_MAP = "VIDEO_MAP";
    public static final String STUDIO_MAP = "STUDIO_MAP";
    public static final String DARSTELLER_MAP = "DARSTELLER_MAP";
    public static final String GENRE_MAP = "GENRE_MAP";
    public static final String DATABASE_CODE = "DATABASE_CODE";

    private static Database database;
    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private OnInstanceFinishedLoading onInstanceFinishedLoading;
    private boolean loaded = false;
    private int loadingCount = 0;
    public static String databaseCode = "";

    public Map<String, Video> videoMap = new HashMap<>();
    public Map<String, Darsteller> darstellerMap = new HashMap<>();
    public Map<String, Studio> studioMap = new HashMap<>();
    public Map<String, Genre> genreMap = new HashMap<>();

    public static final Database getInstance() {
        return database;
    }

    public static final Database getInstance(SharedPreferences mySPR_daten, OnInstanceFinishedLoading onInstanceFinishedLoading, boolean createNew) {
        databaseCode = mySPR_daten.getString(DATABASE_CODE, "");
        if (databaseCode.equals(""))
            return null;

        if (Utility.isOnline()) {
            return new Database(onInstanceFinishedLoading);
        } else {
            Gson gson = new Gson();

            database = new Database();

            String videoMap_string = mySPR_daten.getString(VIDEO_MAP, "--Leer--");
            if (!videoMap_string.equals("--Leer--")) {
                database.videoMap = gson.fromJson(
                        videoMap_string, new TypeToken<HashMap<String, Video>>() {
                        }.getType()
                );
            } else {
                if (createNew)
                    return new Database();
                else
                    return null;
            }

            String darstellerMap_string = mySPR_daten.getString(DARSTELLER_MAP, "--Leer--");
            if (!darstellerMap_string.equals("--Leer--")) {
                database.darstellerMap = gson.fromJson(
                        darstellerMap_string, new TypeToken<HashMap<String, Darsteller>>() {
                        }.getType()
                );
            } else {
                if (createNew)
                    return new Database();
                else
                    return null;
            }

            String studioMap_string = mySPR_daten.getString(STUDIO_MAP, "--Leer--");
            if (!studioMap_string.equals("--Leer--")) {
                database.studioMap = gson.fromJson(
                        studioMap_string, new TypeToken<HashMap<String, Studio>>() {
                        }.getType()
                );
            } else {
                if (createNew)
                    return new Database();
                else
                    return null;
            }

            String genreMap_string = mySPR_daten.getString(GENRE_MAP, "--Leer--");
            if (!genreMap_string.equals("--Leer--")) {
                database.genreMap = gson.fromJson(
                        genreMap_string, new TypeToken<HashMap<String, Genre>>(){
                        }.getType()
                );
            } else {
                if (createNew)
                    return new Database();
                else
                    return null;
            }
            database.loaded = true;
            onInstanceFinishedLoading.onFinishedLoading(database);
            return database;
        }


    }

    public void writeAllToFirebase() {
        databaseReference.child(databaseCode).child(VIDEO_MAP).setValue(videoMap);
        databaseReference.child(databaseCode).child(DARSTELLER_MAP).setValue(darstellerMap);
        databaseReference.child(databaseCode).child(STUDIO_MAP).setValue(studioMap);
        databaseReference.child(databaseCode).child(GENRE_MAP).setValue(genreMap);
    }

    private Database(OnInstanceFinishedLoading onInstanceFinishedLoading) {
        Database.database = Database.this;
        this.onInstanceFinishedLoading = onInstanceFinishedLoading;
//        this.onInstanceFinishedLoading.onFinishedLoading(Database.this);
//        writeAllToFirebase();
        loadVideosFromFirebase();
        loadActorFromFirebase();
        loadStudioFromFirebase();
        loadGenreFromFirebase();
    }
    private Database() {
        Database.database = Database.this;
//        this.loggedInUser_Id = loggedInUser_Id;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void generateData() {
        videoMap.clear();
        darstellerMap.clear();
        studioMap.clear();
        genreMap.clear();

        Darsteller darsteller1 = new Darsteller("Darsteller1");
        Darsteller darsteller2 = new Darsteller("Darsteller2");
        Darsteller darsteller3 = new Darsteller("Darsteller3");
        Darsteller darsteller4 = new Darsteller("Darsteller4");

        darstellerMap.put(darsteller1.getUuid(), darsteller1);
        darstellerMap.put(darsteller2.getUuid(), darsteller2);
        darstellerMap.put(darsteller3.getUuid(), darsteller3);
        darstellerMap.put(darsteller4.getUuid(), darsteller4);

        List<Video> videoList = Arrays.asList(
                new Video("Film 7")
                        .setUrl("http://google.de")
                        .addDarsteller(darsteller1, darsteller2),
                new Video("Film 1")
                        .setUrl("http://youtube.com")
                        .addDarsteller(darsteller2),
                new Video("Film 2")
                        .setUrl("facebook.com"),
                new Video("Film 3")
                        .addDarsteller(darsteller3, darsteller1),
                new Video("Film 4"),
                new Video("Film 5")
                        .addDarsteller(darsteller1, darsteller2, darsteller3, darsteller4),
                new Video("Film 6")
                        .addDarsteller(darsteller4)
        );
        videoList.forEach(video -> videoMap.put(video.getUuid(), video));
    }

    //  ----- Get data from database ----->
    public interface OnInstanceFinishedLoading {
        void onFinishedLoading(Database database);
    }

    private void loadVideosFromFirebase() {
        loadingCount++;
        Database.databaseCall_read(dataSnapshot -> {
            loadingCount--;
            if (dataSnapshot.getValue() == null)
                return;
            HashMap<String, Video> newMap = new HashMap<>();
            for (DataSnapshot snapshot :  dataSnapshot.getChildren()){
                Video video = snapshot.getValue(Video.class);
                newMap.put(video.getUuid(), video);
            }
            videoMap = newMap;
            if (loadingCount == 0)
                onInstanceFinishedLoading.onFinishedLoading(database);
        }, databaseCode, Database.VIDEO_MAP);
    }
    private void loadActorFromFirebase() {
        loadingCount++;
        Database.databaseCall_read(dataSnapshot -> {
            loadingCount--;
            if (dataSnapshot.getValue() == null)
                return;
            HashMap<String, Darsteller> newMap = new HashMap<>();
            for (DataSnapshot snapshot :  dataSnapshot.getChildren()){
                Darsteller darsteller = snapshot.getValue(Darsteller.class);
                newMap.put(darsteller.getUuid(), darsteller);
            }
            darstellerMap = newMap;
            if (loadingCount == 0)
                onInstanceFinishedLoading.onFinishedLoading(database);
        }, databaseCode, Database.DARSTELLER_MAP);
    }
    private void loadStudioFromFirebase() {
        loadingCount++;
        Database.databaseCall_read(dataSnapshot -> {
            loadingCount--;
            if (dataSnapshot.getValue() == null)
                return;
            HashMap<String, Studio> newMap = new HashMap<>();
            for (DataSnapshot snapshot :  dataSnapshot.getChildren()){
                Studio studio = snapshot.getValue(Studio.class);
                newMap.put(studio.getUuid(), studio);
            }
            studioMap = newMap;
            if (loadingCount == 0)
                onInstanceFinishedLoading.onFinishedLoading(database);
        }, databaseCode, Database.STUDIO_MAP);
    }
    private void loadGenreFromFirebase() {
        loadingCount++;
        Database.databaseCall_read(dataSnapshot -> {
            loadingCount--;
            if (dataSnapshot.getValue() == null)
                return;
            HashMap<String, Genre> newMap = new HashMap<>();
            for (DataSnapshot snapshot :  dataSnapshot.getChildren()){
                Genre genre = snapshot.getValue(Genre.class);
                newMap.put(genre.getUuid(), genre);
            }
            genreMap = newMap;
            if (loadingCount == 0)
                onInstanceFinishedLoading.onFinishedLoading(database);
        }, databaseCode, Database.GENRE_MAP);
    }

//    private void getGroupsfromUser() {
//        List<String> loggedInUser_groupsIdList = loggedInUser.getGroupIdList();
//        if (loggedInUser_groupsIdList.size() == 0) {
//            return;
//        }
//        for (String groupId : loggedInUser_groupsIdList) {
//            Database.databaseCall_read(Arrays.asList(Database.GROUPS, groupId), dataSnapshot -> {
//                if (dataSnapshot.getValue() == null) {
//                    return;
//                }
//                Group foundGroup = Database.getFromData_group(dataSnapshot);
//                groupsMap.put(foundGroup.getGroup_id(), foundGroup);
//                if (groupsMap.size() == loggedInUser_groupsIdList.size()) {
//                    getGroupPassengers();
//                }
//            });
//            hasGroupChangeListener.put(groupId, false);
//            addOnGroupChangeListener_database(groupId);
//        }
//    }
//
//    private void getGroupPassengers() {
//        final int[] loggedInUser_passengerCount = {0};
//        groupsMap.values().forEach(group -> loggedInUser_passengerCount[0] += group.getUserIdList().size());
//
//        for (Group group : groupsMap.values()) {
//            for (String user : group.getUserIdList()) {
//                Database.databaseCall_read(Arrays.asList(Database.USERS, user), dataSnapshot -> {
//                    if (dataSnapshot.getValue() == null)
//                        return;
//                    User foundUser = Database.getFromData_user(dataSnapshot);
//                    groupPassengerMap.put(foundUser.getUser_id(), foundUser);
//                    loggedInUser_passengerCount[0]--;
//                    if (loggedInUser_passengerCount[0] == 0) {
//                        getGroupTrips();
//                    }
//                });
//            }
//        }
//    }
//
//    private void getGroupTrips() {
//        groupTripMap.clear();
//        for (final String groupId : loggedInUser.getGroupIdList()) {
//            if (groupsMap.get(groupId).getTripIdList().size() == 0) {
//                groupTripMap.put(groupId, new HashMap<>());
//                if (groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
//                    Database.this.loaded = true;
//                    onInstanceFinishedLoading.onFinishedLoading(Database.this); // --> fertig
//                    return;
//                }
//                else
//                    continue;
//            }
//            Database.databaseCall_read(Arrays.asList(Database.TRIPS, groupId), dataSnapshot -> {
//                if (dataSnapshot.getValue() == null)
//                    return;
//                groupTripMap.put(groupId, Database.getFromData_tripMap(dataSnapshot));
//                if (groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
//                    Database.this.loaded = true;
//                    onInstanceFinishedLoading.onFinishedLoading(Database.this); // --> fertig
//                }
//            });
//        }
//    }
//  <----- Get data from database -----


//  ----- Get data from datSnapshot ----->
//    private static User getFromData_user(DataSnapshot dataSnapshot) {
//    return dataSnapshot.getValue(User.class);
//}
//
//    private static Group getFromData_group(DataSnapshot dataSnapshot) {
//        return dataSnapshot.getValue(Group.class);
//    }
//
//    private static Map<String, Trip> getFromData_tripMap(DataSnapshot dataSnapshot) {
//        Map<String, Trip> newMap = new HashMap<>();
//        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
//            Trip foundTrip = messageSnapshot.getValue(Trip.class);
//            newMap.put(foundTrip.getTrip_id(), foundTrip);
//        }
//        return newMap;
//    }
//  <----- Get data from datSnapshot -----


//  ----- Database Call ----->
    interface OnDatabaseCallFinished {
        void onFinished(DataSnapshot dataSnapshot);
    }

    interface OnDatabaseCallFailed {
        void onFailed(DatabaseError databaseError);
    }

    public static void databaseCall_read(OnDatabaseCallFinished onDatabaseCallFinished, String... stepList){
        accessChilds(databaseReference, Arrays.asList(stepList)).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void databaseCall_read(List<String> stepList, OnDatabaseCallFinished onDatabaseCallFinished,
                                         OnDatabaseCallFailed onDatabaseCallFailed){
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

    public static void databaseCall_write(List<String> stepList, Object object){
        accessChilds(databaseReference, stepList).setValue(object);
    }

    public static void databaseCall_delete(List<String> stepList){
        accessChilds(databaseReference, stepList).removeValue();
    }

    public static DatabaseReference accessChilds(DatabaseReference databaseReference, List<String> steps) {
        List<String> newSteps = new ArrayList<>(steps);
        if (newSteps.size() > 0) {
            DatabaseReference newDatabaseReference = databaseReference.child(newSteps.remove(0));
            return accessChilds(newDatabaseReference, newSteps);
        }
        return databaseReference;
    }

    public static Database.OnDatabaseCallFailed getStandardFail(Context context) {
        return databaseError -> Toast.makeText(context, "Datenbankabfrage gescheitert", Toast.LENGTH_SHORT).show();
    }
//  <----- Database Call -----


//  <----- Updater -----
//    public static void updateGroup(Group group) {
//        databaseCall_write(Arrays.asList(GROUPS, group.getGroup_id()), group);
//    }
//
//    public static void updateUser(User user) {
//        databaseCall_write(Arrays.asList(USERS, user.getUser_id()), user);
//    }
//
//    public static void removeTrip(Group group, Trip trip) {
//        databaseCall_delete(Arrays.asList(GROUPS, group.getGroup_id(), trip.getTrip_id()));
//    }
//
//    public static void removeCar(User user, Car car) {
//        databaseCall_delete(Arrays.asList(CARS, user.getUser_id(), car.getCar_id()));
//    }
//  ----- Updater ----->


//  ----- Change Listener ----->
    // ToDo: onLoggedInUserChange
    interface OnChangeListener {
        void onChangeListener();
    }

//    private void fireOnGroupChangeListeners(){
//            onGroupChangeListenerList.forEach(OnChangeListener::onChangeListener);
//    }
//    public OnChangeListener addOnGroupChangeListener(OnChangeListener onChangeListener) {
//        onGroupChangeListenerList.add(onChangeListener);
//        return onChangeListener;
//    }
//    public boolean removeOnGroupChangeListener(OnChangeListener onChangeListener) {
//        return onGroupChangeListenerList.remove(onChangeListener);
//    }
//    public void addOnGroupChangeListener_database(final String groupId) {
//        databaseReference.child(Database.GROUPS).child(groupId).addValueEventListener(onGroupChangeListener);
//    }
//    private ValueEventListener onGroupChangeListener = new ValueEventListener() {
//    @Override
//    public void onDataChange(DataSnapshot dataSnapshot) {
//        if (dataSnapshot.getValue() == null) {
//            final String removedGroup = dataSnapshot.getKey();
////                for (String user : groupsMap.get(removedGroup).getUserIdList()) {
////                    databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            if (dataSnapshot.getValue() == null)
////                                return;
////                            User foundUser = dataSnapshot.getValue(User.class);
////                            foundUser.getGroupIdList().remove(removedGroup);
////                            databaseReference.child("Users").child(foundUser.getUser_id()).setValue(foundUser);
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////                        }
////                    });
////                }
//
//            groupsMap.remove(removedGroup);
//            fireOnGroupChangeListeners();
//            return;
//        }
//        Group foundGroup = dataSnapshot.getValue(Group.class);
//        if (!hasGroupChangeListener.get(foundGroup.getGroup_id())) {  // neue Gruppe?
//            hasGroupChangeListener.replace(foundGroup.getGroup_id(), true);
//            return;
//        }
//
////            Findet heraus, ob ein Nutzer eingetreten, oder ausgetreten ist
//        if (!foundGroup.getUserIdList().equals(groupsMap.get(foundGroup.getGroup_id()).getUserIdList())) {
//            onChangedUsers(foundGroup);
//            return;
//        }
//
////            Findet heraus, ob sich bei den Lesezeichen was verändert hat
//        if (!foundGroup.getBookmarkList().equals(groupsMap.get(foundGroup.getGroup_id()).getBookmarkList()))
//            groupsMap.get(foundGroup.getGroup_id()).setBookmarkList(foundGroup.getBookmarkList());
//
//
////            Findet heraus, ob sich bei den Trips was verändert hat
//        if (!foundGroup.getTripIdList().equals(groupsMap.get(foundGroup.getGroup_id()).getTripIdList())) {
//            onChangedTrip(foundGroup);
//            return;
//        }
//
//        groupsMap.replace(foundGroup.getGroup_id(), foundGroup); // Gruppe wird aktuallisiert
//
//        fireOnGroupChangeListeners();
//        // ToDo: group trip map updaten
//
//    }
//
//    @Override
//    public void onCancelled(DatabaseError databaseError) {
//    }
//
//    void onChangedTrip(final Group foundGroup) {
//        final List<List<String>> changeList = foundGroup.getChangedTripsLists(groupTripMap.get(foundGroup.getGroup_id()).keySet());
//
//        if (changeList.get(0) != null) {
//            for (String trip : new ArrayList<>(changeList.get(0))) {
//                databaseReference.child("Trips").child(foundGroup.getGroup_id()).child(trip).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.getValue() == null)
//                            return;
//                        Trip foundTrip = dataSnapshot.getValue(Trip.class);
//                        changeList.get(0).remove(foundTrip.getTrip_id());
//                        groupTripMap.get(foundGroup.getGroup_id()).put(foundTrip.getTrip_id(), foundTrip);
//                        groupsMap.get(foundGroup.getGroup_id()).getTripIdList().add(foundTrip.getTrip_id());
////                        groupPassengerMap.put(foundTrip.getUser_id(), foundTrip);
//                        if (changeList.get(0).size() <= 0)
//                            fireOnGroupChangeListeners();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                    }
//                });
//            }
//        }
//        if (changeList.get(1) != null) {
//            for (String tripId : changeList.get(1)) {
//                groupTripMap.get(foundGroup.getGroup_id()).remove(tripId);
//                groupsMap.get(foundGroup.getGroup_id()).getTripIdList().remove(tripId);
//            }
//            fireOnGroupChangeListeners();
//        }
//    }
//    void onChangedUsers(Group foundGroup) {
//        final List<List<String>> changeList = foundGroup.getChangedUserLists(groupsMap.get(foundGroup.getGroup_id()));
//
//        for (String user : changeList.get(1)) {
//            groupPassengerMap.remove(user);
//        }
//
//        for (String user : new ArrayList<>(changeList.get(0))) {
//            changeList.get(0).remove(user);
//            databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.getValue() == null)
//                        return;
//                    User foundUser = dataSnapshot.getValue(User.class);
//                    groupPassengerMap.put(foundUser.getUser_id(), foundUser);
//                    groupsMap.get(foundGroup.getGroup_id()).getUserIdList().add(foundUser.getUser_id());
//                    if (changeList.get(0).size() <= 0)
//                        fireOnGroupChangeListeners();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            });
//        }
//    }
//};
//  <----- Change Listener -----

}
