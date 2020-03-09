package com.maxMustermannGeheim.linkcollection.Daten;

public class ParentClass_Tmdb extends ParentClass {
    private int tmdbId;
    private String imagePath;

    public int getTmdbId() {
        return tmdbId;
    }

    public ParentClass_Tmdb setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public ParentClass_Tmdb setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }
}
