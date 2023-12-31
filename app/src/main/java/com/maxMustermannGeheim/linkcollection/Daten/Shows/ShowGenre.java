package com.maxMustermannGeheim.linkcollection.Daten.Shows;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class ShowGenre extends ParentClass {

    private Integer tmdbGenreId;

    public ShowGenre(String name) {
        uuid = "showGenre_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public ShowGenre() {
    }

    public Integer getTmdbGenreId() {
        return tmdbGenreId;
    }

    public ShowGenre setTmdbGenreId(Integer tmdbGenreId) {
        this.tmdbGenreId = tmdbGenreId;
        return this;
    }
}
