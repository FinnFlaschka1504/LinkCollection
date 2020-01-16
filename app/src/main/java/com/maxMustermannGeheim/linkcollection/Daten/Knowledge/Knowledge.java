package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Knowledge extends ParentClass {

    private String content;
    private List<List<String>> sources = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private Float rating = -1f;
    private Date lastChanged;
    private List<Item> itemList = new ArrayList<>(Arrays.asList(new Item()));

    public Knowledge() {
    }

    public Knowledge(String name) {
        uuid = "knowledge_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
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


    //  ------------------------- ItemList ------------------------->
    public List<Item> getItemList() {
        return itemList;
    }

    public String itemListToString() {
        return itemList.stream().map(item -> "• " + item.getName()).collect(Collectors.joining("\n\n"));
    }

    public Knowledge setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    public Item newItem(Item previousItem) {
        Item newItem = new Item();
        itemList.add(itemList.indexOf(previousItem) + 1, newItem);
        return newItem;
    }

    public boolean removeItem(Item item) {
        return itemList.remove(item);
    }

    public boolean hasItems() {
        return itemList.stream().anyMatch(item -> !item.getName().isEmpty());
    }

    public void clearItemList() {
        itemList = new ArrayList<>(Arrays.asList(new Item()));
    }

    public static class Item extends ParentClass{
        private List<Item> children = new ArrayList<>();

        public Item() {
            uuid = "knowledgeItem_" + UUID.randomUUID().toString();
            name = "";
        }

        public Item(String name) {
            uuid = "knowledgeItem_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Item setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Item clone() {
            return (Item) super.clone();
        }
    }
    //  <------------------------- ItemList -------------------------

    @Override
    public Knowledge clone() {
        Knowledge clone = (Knowledge) super.clone();
        clone.setItemList(itemList.stream().map(Item::clone).collect(Collectors.toList()));
        return clone;
    }
}
