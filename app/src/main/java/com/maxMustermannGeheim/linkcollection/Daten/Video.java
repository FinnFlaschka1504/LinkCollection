package com.maxMustermannGeheim.linkcollection.Daten;

import android.media.Rating;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Video {
    private UUID uuid = UUID.randomUUID();

    private String Titel;
    private String link;
    private Studio studio;
    private List<UUID> darstellerList = new ArrayList<>();
    private Rating rating;
    private List<Date> dateList = new ArrayList<>();
    private List<Genre> genreList = new ArrayList<>();

    public Video(String titel) {
        Titel = titel;
        dateList.add(new Date());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTitel() {
        return Titel;
    }

    public Video setTitel(String titel) {
        Titel = titel;
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

}
