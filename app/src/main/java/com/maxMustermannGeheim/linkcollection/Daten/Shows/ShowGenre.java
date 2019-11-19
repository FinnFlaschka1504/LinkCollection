package com.maxMustermannGeheim.linkcollection.Daten.Shows;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class ShowGenre extends ParentClass {

    private int tmdbGenreId;

    public ShowGenre(String name) {
        uuid = "showGenre_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public ShowGenre() {
    }

    public int getTmdbGenreId() {
        return tmdbGenreId;
    }

    public ShowGenre setTmdbGenreId(int tmdbGenreId) {
        this.tmdbGenreId = tmdbGenreId;
        return this;
    }
}
