package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class MediaTag extends ParentClass {
    public MediaTag() {
    }

    public MediaTag(String name) {
        uuid = "mediaTag_" + UUID.randomUUID().toString();
        this.name = name;
    }

}
