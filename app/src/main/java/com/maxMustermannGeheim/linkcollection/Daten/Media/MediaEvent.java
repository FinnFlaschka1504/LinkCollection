package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MediaEvent extends ParentClass implements ParentClass_Tree {
    private List<MediaEvent> children = new ArrayList<>();
    private String parentId;
    private Date beginning;
    private Date end;
    private List<String> mediaIdList = new ArrayList<>();
    private String description;
    private boolean dummy;
    private List<String> personIdList = new ArrayList<>();


    /**  <------------------------- Constructor -------------------------  */
    public MediaEvent () {
    }

    public MediaEvent(String name) {
        uuid = "mediaEvent_" + UUID.randomUUID().toString();
        this.name = name;
    }
    /**  ------------------------- Constructor ------------------------->  */


    /**  ------------------------- Getter & Setter ------------------------->  */
    public Date getBeginning() {
        return beginning;
    }

    public MediaEvent setBeginning(Date beginning) {
        this.beginning = beginning;
        return this;
    }

    public Date getEnd() {
        return end;
    }

    public MediaEvent setEnd(Date end) {
        this.end = end;
        return this;
    }

    public List<String> getMediaIdList() {
        return mediaIdList;
    }

    public MediaEvent setMediaIdList(List<String> mediaIdList) {
        this.mediaIdList = mediaIdList;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MediaEvent setDescription(String description) {
        this.description = description;
        return this;
    }

//    public List<MediaEvent> getChildren() {
//        return children;
//    }
//
//    public MediaEvent setChildren(List<MediaEvent> children) {
//        this.children = children;
//        return this;
//    }

    public MediaEvent _enableDummy() {
        dummy = true;
        return this;
    }

    public boolean _isDummy() {
        return dummy;
    }

    public List<String> getPersonIdList() {
        return personIdList;
    }

    public MediaEvent setPersonIdList(List<String> personIdList) {
        this.personIdList = personIdList;
        return this;
    }

    /**  <------------------------- Getter & Setter -------------------------  */

    /**
     * ------------------------- Convenience ------------------------->
     */
    public String _getThumbnailPath() {
        String thumbnailId = null;
        if (!mediaIdList.isEmpty())
            thumbnailId = mediaIdList.get(0);
        else if (!children.isEmpty()) {
            for (MediaEvent child : children) {
                if ((thumbnailId = child._getThumbnailPath()) != null)
                    break;
            }
        }
        if (thumbnailId == null)
            return null;
        else
            return ((Media) Utility.findObjectById(CategoriesActivity.CATEGORIES.MEDIA, thumbnailId)).getImagePath();
    }
    /**  <------------------------- Convenience -------------------------  */
}
