package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaTag;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ParentClass extends com.finn.androidUtilities.ParentClass implements Cloneable {
//    protected String uuid;
//    protected String name;
//
//    public ParentClass() {
//    }
//
//    public String getUuid() {
//        return uuid;
//    }
//
//    public ParentClass setUuid(String uuid) {
//        this.uuid = uuid;
//        return this;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public ParentClass setName(String name) {
//        this.name = name;
//        return this;
//    }

    public static ParentClass newCategory(CategoriesActivity.CATEGORIES object_type, String name) {
        switch (object_type) {
            case DARSTELLER:
                return new Darsteller(name);
            case GENRE:
                return new Genre(name);
            case STUDIOS:
                return new Studio(name);
            case KNOWLEDGE_CATEGORIES:
                return new KnowledgeCategory(name);
            case JOKE_CATEGORIES:
                return new JokeCategory(name);
            case SHOW_GENRES:
                return new ShowGenre(name);
            case MEDIA_PERSON:
                return new MediaPerson(name);
            case MEDIA_CATEGORY:
                return new MediaCategory(name);
            case MEDIA_TAG:
                return new MediaTag(name);
        }
        return null;
    }

//    public ParentClass clone() {
////        try {
//            return (ParentClass) super.clone();
////        } catch (CloneNotSupportedException e) {
////            return null;
////        }
//    }


    //  ------------------------- GetChangesFrom ------------------------->
    public ParentClass getChangesFrom(ParentClass newVersion) {
        name = newVersion.name;
        getLayer(newVersion, getClass());
        return this;
    }

    private void getLayer(ParentClass newVersion, Class aClass) {
        if (aClass.equals(ParentClass.class))
            return;
        try {
            for (Field field : aClass.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(this, field.get(newVersion));
            }

            if (aClass.getSuperclass() != null)
                getLayer(newVersion, aClass.getSuperclass());
        } catch (IllegalAccessException ignored) {
        }
    }
    //  <------------------------- GetChangesFrom -------------------------

    /**
     * ------------------------- Compare ------------------------->
     */
    public int compareByName(ParentClass parentClass) {
        return name.compareTo(parentClass.getName());
    }
    /**  <------------------------- Compare -------------------------  */


    //  ------------------------- Encryption ------------------------->
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------
}
