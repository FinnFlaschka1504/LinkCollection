package com.maxMustermannGeheim.linkcollection.Daten.Shows;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_I;

import java.util.UUID;

public class ShowLabel extends ParentClass implements ParentClass_I {
    public ShowLabel(String name) {
        uuid = "showLabel_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public ShowLabel() {
    }
}
