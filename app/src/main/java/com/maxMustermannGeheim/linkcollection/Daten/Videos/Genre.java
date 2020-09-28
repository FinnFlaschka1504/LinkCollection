package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.UUID;

public class Genre extends ParentClass implements ParentClass_Alias{

    private int tmdbGenreId;
    private String nameAliases;

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

//    @Override
//    public String getName() {
//        if (name.contains("\n"))
//            return name.split("\\n")[0];
//        return super.getName();
//    }

    public String getNameAliases() {
        return nameAliases;
    }

    public Genre setNameAliases(String nameAliases) {
        this.nameAliases = nameAliases;
        return this;
    }


    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(nameAliases)) nameAliases = AESCrypt.encrypt(key, nameAliases);
        } catch (GeneralSecurityException e) {
            return false;
        }
        return super.encrypt(key);
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(nameAliases)) nameAliases = AESCrypt.decrypt(key, nameAliases);
        } catch (GeneralSecurityException e) {
            return false;
        }
        return super.decrypt(key);
    }
    //  <------------------------- Encryption -------------------------

}
