package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import android.util.Pair;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Knowledge extends ParentClass {

    private String content;
    private List<List<String>> sources = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private Float rating = -1f;
    private Date lastChanged;

    public Knowledge() {
    }

    public Knowledge(String name) {
        uuid = "knowledge_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<List<String>> getSources() {
        return sources;
    }

    public void setSources(List<List<String>> sources) {
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

    public Date getLastChanged() {
        return lastChanged;
    }

    public Knowledge setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
        return this;
    }

//    public Knowledge cloneKnowledge() {
//        Knowledge knowledge = new Knowledge();
//        knowledge.name = this.name;
//        knowledge.uuid = this.uuid;
//        knowledge.categoryIdList = new ArrayList<>(this.categoryIdList);
//        knowledge.rating = this.rating;
//        knowledge.sources = new ArrayList<>(this.sources);
//        return knowledge;
//    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSources(), getCategoryIdList(), getRating(), getLastChanged());
    }

    @Override
    public Knowledge clone() {
        return (Knowledge) super.clone();
    }
}
