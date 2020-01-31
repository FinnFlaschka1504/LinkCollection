package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Video extends ParentClass_Ratable {
//    private String uuid = "video_" + UUID.randomUUID().toString();
//
//    private String name;
    private String url;
    private List<String> studioList = new ArrayList<>();
    private List<String> darstellerList = new ArrayList<>();
    private List<Date> dateList = new ArrayList<>();
    private List<String> genreList = new ArrayList<>();
    private Date release;
    private String imagePath;
    private List<String> translationList = new ArrayList<>();
    private int tmdId;
    private boolean watchLater;

    public Video(String name) {
        uuid = "video_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Video() {
    }

    public String getUrl() {
        return url;
    }

    public Video setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<String> getDarstellerList() {
        return darstellerList;
    }

    public Video setDarstellerList(List<String> darstellerList) {
        this.darstellerList = darstellerList;
        return this;
    }

    public Video addDarsteller(Darsteller... darsteller) {
        this.darstellerList.addAll(
                Arrays.stream(darsteller)
                        .map(Darsteller::getUuid).collect(Collectors.toList()));
        return this;
    }

    public List<String> getStudioList() {
        return studioList;
    }

    public Video setStudioList(List<String> studioList) {
        this.studioList = studioList;
        return this;
    }

    public List<String> getGenreList() {
        return genreList;
    }

    public Video setGenreList(List<String> genreList) {
        this.genreList = genreList;
        return this;
    }

    public Video addGenre(Genre... genre) {
        this.darstellerList.addAll(
                Arrays.stream(genre)
                        .map(Genre::getUuid).collect(Collectors.toList()));
        return this;
    }

    public List<Date> getDateList() {
        return dateList;
    }

    public Video setDateList(List<Date> dateList) {
        dateList = dateList.stream().map(Utility::removeMilliseconds).collect(Collectors.toList());
        this.dateList = dateList;
        return this;
    }

    public boolean addDate(Date date, boolean checkTime) {
        boolean isBefore = false;
        if (checkTime) {
            Calendar limitTime = Calendar.getInstance();
            limitTime.set(Calendar.HOUR_OF_DAY, 6);
            Calendar givenDate = Calendar.getInstance();
            givenDate.setTime(date);

            isBefore = givenDate.before(limitTime);

            givenDate = Calendar.getInstance();
            givenDate.setTime(date);
            givenDate.set(Calendar.MILLISECOND, 0);
            givenDate.add(Calendar.HOUR, -6);
            date = givenDate.getTime();
        }

        if (!checkTime && date.equals(Utility.removeTime(new Date())))
            date = Utility.shiftTime(new Date(), Calendar.HOUR, -6);

        this.dateList.add(date);

        return isBefore;
    }

    public void removeDate(Date removeDate_withTime) {
        Date removeDate = Utility.removeTime(removeDate_withTime);

        CustomList<Date> dateListCopy = new CustomList<>(dateList);
        dateListCopy.forEachCount((date, count) -> {
            if (!Utility.removeTime(date).equals(removeDate))
                return false;
            dateList.remove(count);
            return true;
        });
    }

    public Date getRelease() {
        return release;
    }

    public Video setRelease(Date release) {
        this.release = release;
        return this;
    }

    public boolean isUpcoming() {
        if (release != null)
            return new Date().before(release);
        else
            return false;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Video setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public List<String> getTranslationList() {
        return translationList;
    }

    public Video setTranslationList(List<String> translationList) {
        this.translationList = translationList;
        return this;
    }

    public int getTmdId() {
        return tmdId;
    }

    public Video setTmdId(int tmdId) {
        this.tmdId = tmdId;
        return this;
    }

    public boolean isWatchLater() {
        return watchLater;
    }

    public Video setWatchLater(boolean watchLater) {
        this.watchLater = watchLater;
        return this;
    }

    //    public Video cloneVideo() {
//        Video video = new Video();
//        video.name = this.name;
//        video.uuid = this.uuid;
//        video.dateList = new ArrayList<>(this.dateList);
//        video.darstellerList = new ArrayList<>(this.darstellerList);
//        video.rating = this.rating;
//        video.genreList = new ArrayList<>(this.genreList);
//        video.studioList = this.studioList;
//        video.url = this.url;
//        video.release = this.release;
//        return video;
//    }


    @Override
    public Video clone() {
        return (Video) super.clone();
    }


    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(url)) url = AESCrypt.encrypt(key, url);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.encrypt(key, imagePath);
            if (!translationList.isEmpty()) for (int i = 0; i < translationList.size(); i++) translationList.set(i, AESCrypt.encrypt(key, translationList.get(i)));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(url)) url = AESCrypt.decrypt(key, url);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.decrypt(key, imagePath);
            if (!translationList.isEmpty()) for (int i = 0; i < translationList.size(); i++) translationList.set(i, AESCrypt.decrypt(key, translationList.get(i)));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------

}
