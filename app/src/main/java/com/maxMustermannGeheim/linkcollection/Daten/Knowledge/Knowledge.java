package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import android.util.Pair;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Knowledge extends ParentClass {

    private String content;
    private List<Pair<String,String>> sources = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private Float rating = -1f;

    public Knowledge() {
    }

    public Knowledge(String name) {
        uuid = "knowledge_" + UUID.randomUUID().toString();
        this.name = name;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public ParentClass setName(String name) {
        return super.setName(name);
    }

    @Override
    public String getUuid() {
        return super.getUuid();
    }

    @Override
    public ParentClass setUuid(String uuid) {
        return super.setUuid(uuid);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Knowledge cloneKnowledge() {
        Knowledge knowledge = new Knowledge();
        knowledge.name = this.name;
        knowledge.uuid = this.uuid;
        knowledge.categoryIdList = new ArrayList<>(this.categoryIdList);
        knowledge.rating = this.rating;
        knowledge.sources = new ArrayList<>(this.sources);
        return knowledge;
    }

}
