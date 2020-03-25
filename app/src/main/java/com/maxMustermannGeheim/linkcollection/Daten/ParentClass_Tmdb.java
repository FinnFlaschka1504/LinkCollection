package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

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

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.encrypt(key, imagePath);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.decrypt(key, imagePath);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------

}
