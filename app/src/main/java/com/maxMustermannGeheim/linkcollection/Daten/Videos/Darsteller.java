package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;

import java.util.UUID;

public class Darsteller extends ParentClass_Tmdb {
    public Darsteller(String name) {
        uuid = "darsteller_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Darsteller() {
    }

//    public String getUuid() {
//        return uuid;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public DARSTELLER setName(String name) {
//        this.name = name;
//        return this;
//    }
}
