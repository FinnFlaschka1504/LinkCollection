package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class Studio extends ParentClass {
//    private String uuid = "studio_" + UUID.randomUUID().toString();
//
//    private String name;

    public Studio(String name) {
        uuid = "studio_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Studio() {
    }

//    public String getUuid() {
//        return uuid;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public Studio setName(String name) {
//        this.name = name;
//        return this;
//    }

}
