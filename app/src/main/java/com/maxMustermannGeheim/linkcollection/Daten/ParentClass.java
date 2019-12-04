package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;

import java.lang.reflect.Field;
import java.util.Objects;

public class ParentClass implements Cloneable {
    protected String uuid;
    protected String name;

    public ParentClass() {
    }

    public String getUuid() {
        return uuid;
    }

    public ParentClass setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public ParentClass setName(String name) {
        this.name = name;
        return this;
    }

    public static ParentClass newCategoy(CategoriesActivity.CATEGORIES object_type, String name) {
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
        }
        return null;
    }

    public ParentClass clone() {
        try {
            return (ParentClass) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentClass that = (ParentClass) o;
        return dynamicEqual(that);
//        return Objects.equals(getUuid(), that.getUuid()) &&
//                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getName());
    }

    //  --------------- DynamicEqual --------------->
    protected boolean dynamicEqual(Object o) {
        return compareClassLayer(o.getClass(), o);
    }

    private boolean compareClassLayer(Class aClass, Object o) {
        boolean result = true; // ToDo: sicherstellen, dass immer wirklich alle überprüft werden (herkömmliches equals bricht zu früh ab)
        try {
            for (Field field : aClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object get = field.get(o);
                Object get2 = field.get(this);
                if (!Objects.equals(get, get2)) {
                    Database.changeSet.add(o);
                    result = false;
//                    return false;
                }
            }

            if (!aClass.equals(ParentClass.class) && aClass.getSuperclass() != null) {
                if (!compareClassLayer(aClass.getSuperclass(), o)) {
                    result = false;
//                    return false;
                }
            }
        } catch (IllegalAccessException e) {
            String BREAKPOINT = null;
        }
        return result;
//        return true;
    }
    //  <--------------- DynamicEqual ---------------

}
