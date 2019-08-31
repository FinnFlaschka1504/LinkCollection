package com.maxMustermannGeheim.linkcollection.Daten;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Video {
    private UUID uuid = UUID.randomUUID();

    private String titel;
    private String url;
    private Studio studio;
    private List<UUID> darstellerList = new ArrayList<>();
    private Float rating = -1f;
    private List<Date> dateList = new ArrayList<>();
    private List<Genre> genreList = new ArrayList<>();

    public Video(String titel) {
        this.titel = titel;
        dateList.add(new Date());
    }

    public Video() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTitel() {
        return titel;
    }

    public Video setTitel(String titel) {
        this.titel = titel;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Video setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<UUID> getDarstellerList() {
        return darstellerList;
    }

    public Video setDarstellerList(List<UUID> darstellerList) {
        this.darstellerList = darstellerList;
        return this;
    }

    public Video addDarsteller(Darsteller... darsteller) {
        this.darstellerList.addAll(
                Arrays.stream(darsteller)
                        .map(Darsteller::getUuid).collect(Collectors.toList()));
        return this;
    }

    public List<Genre> getGenreList() {
        return genreList;
    }

    public Video setGenreList(List<Genre> genreList) {
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

    public Float getRating() {
        return rating;
    }

    public Video setRating(Float rating) {
        this.rating = rating;
        return this;
    }

    public Video cloneVideo() {
        Video video = new Video();
        video.titel = this.titel;
        video.uuid = this.uuid;
        video.dateList = new ArrayList<>(this.dateList);
        video.darstellerList = new ArrayList<>(this.darstellerList);
        video.rating = this.rating;
        video.genreList = new ArrayList<>(this.genreList);
        video.studio = this.studio;
        video.url = this.url;
        return video;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(uuid, video.uuid) &&
                Objects.equals(titel, video.titel) &&
                Objects.equals(url, video.url) &&
                Objects.equals(studio, video.studio) &&
                Objects.equals(darstellerList, video.darstellerList) &&
                Objects.equals(rating, video.rating) &&
                Objects.equals(dateList, video.dateList) &&
                Objects.equals(genreList, video.genreList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, titel, url, studio, darstellerList, rating, dateList, genreList);
    }
}
