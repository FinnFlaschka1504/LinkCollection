package com.maxMustermannGeheim.linkcollection.Daten;

import java.util.ArrayList;
import java.util.UUID;

public class DatenObjekt {
    public enum OBJECT_TYPE{
        DARSTELLER, STUDIO, GENRE
    }
    protected String uuid;
    protected String name;

    public DatenObjekt() {
    }

    public DatenObjekt(OBJECT_TYPE object_type, String name) {
        switch (object_type) {
            default:
            case DARSTELLER:
                uuid = "darsteller_" + UUID.randomUUID().toString();
                break;
            case STUDIO:
                uuid = "studio_" + UUID.randomUUID().toString();
                break;
            case GENRE:
                uuid = "genre_" + UUID.randomUUID().toString();
                break;
        }
        this.name = name;
    }

    public DatenObjekt(OBJECT_TYPE object_type) {
        switch (object_type) {
            default:
            case DARSTELLER:
                uuid = "darsteller_" + UUID.randomUUID().toString();
                break;
            case STUDIO:
                uuid = "studio_" + UUID.randomUUID().toString();
                break;
            case GENRE:
                uuid = "genre_" + UUID.randomUUID().toString();
                break;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public DatenObjekt setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public DatenObjekt setName(String name) {
        this.name = name;
        return this;
    }

    public Video newVideo() {
        Video object = new Video();
        object.setName(this.getName());
        object.setUuid(this.getUuid());
        return object;
    }
    public Darsteller newDarsteller() {
        Darsteller object = new Darsteller();
        object.setName(this.getName());
        object.setUuid(this.getUuid());
        return object;
    }
    public Genre newGenre() {
        Genre object = new Genre();
        object.setName(this.getName());
        object.setUuid(this.getUuid());
        return object;
    }
    public Studio newStudio() {
        Studio object = new Studio();
        object.setName(this.getName());
        object.setUuid(this.getUuid());
        return object;
    }
}
