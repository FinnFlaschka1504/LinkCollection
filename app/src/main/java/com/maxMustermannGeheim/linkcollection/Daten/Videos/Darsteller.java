package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Utilities.ImageCropUtility;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.UUID;

public class Darsteller extends ParentClass_Tmdb implements ParentClass_Alias, ImageCropUtility {
    private String nameAliases;
    private ImageCropUtility.ImageCrop imageCrop;

    public Darsteller(String name) {
        uuid = "darsteller_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Darsteller() {
    }


//    public String getUuid() {
//        return uuid;
//    }

    public String getNameAliases() {
        return nameAliases;
    }

    public Darsteller setNameAliases(String nameAliases) {
        this.nameAliases = nameAliases;
        return this;
    }

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(nameAliases)) nameAliases = AESCrypt.encrypt(key, nameAliases);
        } catch (GeneralSecurityException e) {
            return false;
        }
        return super.encrypt(key);
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(nameAliases)) nameAliases = AESCrypt.decrypt(key, nameAliases);
        } catch (GeneralSecurityException e) {
            return false;
        }
        return super.decrypt(key);
    }
    //  <------------------------- Encryption -------------------------

}
