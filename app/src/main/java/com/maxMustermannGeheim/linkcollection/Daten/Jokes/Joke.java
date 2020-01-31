package com.maxMustermannGeheim.linkcollection.Daten.Jokes;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Joke extends ParentClass_Ratable {
    String punchLine = "";
    List<String> categoryIdList = new ArrayList<>();
    Date addedDate;

    public Joke() {
    }

    public Joke(String name) {
        uuid = "joke_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public String getPunchLine() {
        return punchLine;
    }

    public Joke setPunchLine(String punchLine) {
        this.punchLine = punchLine;
        return this;
    }

    public List<String> getCategoryIdList() {
        return categoryIdList;
    }

    public Joke setCategoryIdList(List<String> categoryIdList) {
        this.categoryIdList = categoryIdList;
        return this;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public Joke setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
        return this;
    }

//    public Joke cloneJoke() {
//        Joke joke = new Joke();
//        joke.name = this.name;
//        joke.punchLine = this.punchLine;
//        joke.uuid = this.uuid;
//        joke.categoryIdList = new ArrayList<>(this.categoryIdList);
//        joke.rating = this.rating;
//        joke.addedDate = this.addedDate;
//        return joke;
//    }

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(punchLine)) punchLine = AESCrypt.encrypt(key, punchLine);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(punchLine)) punchLine = AESCrypt.decrypt(key, punchLine);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------


    @Override
    public Joke clone() {
        return (Joke) super.clone();
    }
}
