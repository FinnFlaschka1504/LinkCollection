package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomList;

import java.util.List;
import java.util.UUID;

public class Genre extends ParentClass {

    private int tmdbGenreId;

    public Genre(String name) {
        uuid = "genre_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Genre() {
    }

    public int getTmdbGenreId() {
        return tmdbGenreId;
    }

    public Genre setTmdbGenreId(int tmdbGenreId) {
        this.tmdbGenreId = tmdbGenreId;
        return this;
    }
}
