package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class ParentClass_Tmdb extends ParentClass_Image {
    private int tmdbId;

    public int getTmdbId() {
        return tmdbId;
    }

    public ParentClass_Tmdb setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }
}
