package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WatchList extends ParentClass {
    private Date lastModified;
    private List<String> videoIdList = new ArrayList<>();
    private List<String> watchedVideoIdList = new ArrayList<>();

    /**  <------------------------- Constructor -------------------------  */
    public WatchList() {
    }

    public WatchList(String name) {
        uuid = "watchList_" + UUID.randomUUID().toString();
        this.name = name;
    }
    /**  ------------------------- Constructor ------------------------->  */



    /**  ------------------------- Getter & Setter ------------------------->  */
    public List<String> getVideoIdList() {
        return videoIdList;
    }

    public WatchList setVideoIdList(List<String> videoIdList) {
        this.videoIdList = videoIdList;
        return this;
    }

    public List<String> getWatchedVideoIdList() {
        return watchedVideoIdList;
    }

    public WatchList setWatchedVideoIdList(List<String> watchedVideoIdList) {
        this.watchedVideoIdList = watchedVideoIdList;
        return this;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public WatchList setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }
    /**  <------------------------- Getter & Setter -------------------------  */



    /**  ------------------------- Convenience ------------------------->  */
    public boolean _isWatched(int index) {
        return _isWatched(videoIdList.get(index));
    }

    public boolean _isWatched(String videoId) {
        return watchedVideoIdList.contains(videoId);
    }

    public int _getHeight() {
//        if (videoIdList.isEmpty() && description == null)
//            return 132 + 36;
//        else if (!hasContent())
//            return 191 + 36;
//        else if (description == null)
//            return 504 + 36;
//        else
//            return 563 + 36;
        return 1;
    }

    public boolean _isAllWatched() {
        return watchedVideoIdList.size() == videoIdList.size();
    }
    /**  <------------------------- Convenience -------------------------  */
}
