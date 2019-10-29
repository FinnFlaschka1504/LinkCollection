package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import android.util.Pair;

import com.maxMustermannGeheim.linkcollection.Daten.DatenObjekt;

import java.util.ArrayList;
import java.util.List;

public class Knowledge extends DatenObjekt {

    private String description;
    private List<Pair<String,String>> sources = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private Float rating = -1f;

    public Knowledge() {
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public DatenObjekt setName(String name) {
        return super.setName(name);
    }

    @Override
    public String getUuid() {
        return super.getUuid();
    }

    @Override
    public DatenObjekt setUuid(String uuid) {
        return super.setUuid(uuid);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Pair<String, String>> getSources() {
        return sources;
    }

    public void setSources(List<Pair<String, String>> sources) {
        this.sources = sources;
    }

    public List<String> getCategoryIdList() {
        return categoryIdList;
    }

    public void setCategoryIdList(List<String> categoryIdList) {
        this.categoryIdList = categoryIdList;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}
