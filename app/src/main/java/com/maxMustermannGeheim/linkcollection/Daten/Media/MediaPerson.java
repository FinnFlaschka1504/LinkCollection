package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Utilities.ImageCropUtility;

import java.util.UUID;

public class MediaPerson extends ParentClass_Image implements ImageCropUtility {
    private ImageCrop imageCrop;

    public MediaPerson() {
    }

    public MediaPerson(String name) {
        uuid = "mediaPerson_" + UUID.randomUUID().toString();
        this.name = name;
    }

}
