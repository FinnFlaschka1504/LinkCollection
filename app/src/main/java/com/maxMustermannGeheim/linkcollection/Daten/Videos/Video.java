package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Video extends ParentClass {
//    private String uuid = "video_" + UUID.randomUUID().toString();
//
//    private String name;
    private String url;
    private List<String> studioList = new ArrayList<>();
    private List<String> darstellerList = new ArrayList<>();
    private Float rating = -1f;
    private List<Date> dateList = new ArrayList<>();
    private List<String> genreList = new ArrayList<>();
    private Date release;

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

    public boolean addDate(Date date, boolean checkTime) {
        boolean isBefore = false;
        if (checkTime) {
            Calendar limitTime = Calendar.getInstance();
            limitTime.set(Calendar.HOUR_OF_DAY, 6);
            Calendar givenDate = Calendar.getInstance();
            givenDate.setTime(date);

            isBefore = givenDate.before(limitTime);
            if (isBefore)
                givenDate.add(Calendar.DATE, -1);
            date = givenDate.getTime();
        }
        this.dateList.add(Utility.removeTime(date));

        return isBefore;
    }

    public Float getRating() {
        return rating;
    }

    public Video setRating(Float rating) {
        this.rating = rating;
        return this;
    }

    public Date getRelease() {
        return release;
    }

    public Video setRelease(Date release) {
        this.release = release;
        return this;
    }

    public boolean isUpcomming() {
        if (release != null)
            return new Date().before(release);
        else
            return false;
    }

    public Video cloneVideo() {
        Video video = new Video();
        video.name = this.name;
        video.uuid = this.uuid;
        video.dateList = new ArrayList<>(this.dateList);
        video.darstellerList = new ArrayList<>(this.darstellerList);
        video.rating = this.rating;
        video.genreList = new ArrayList<>(this.genreList);
        video.studioList = this.studioList;
        video.url = this.url;
        video.release = this.release;
        return video;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(uuid, video.uuid) &&
                Objects.equals(name, video.name) &&
                Objects.equals(url, video.url) &&
                Objects.equals(studioList, video.studioList) &&
                Objects.equals(darstellerList, video.darstellerList) &&
                Objects.equals(rating, video.rating) &&
                Objects.equals(dateList, video.dateList) &&
                Objects.equals(genreList, video.genreList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, url, studioList, darstellerList, rating, dateList, genreList);
    }
}
