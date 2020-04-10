package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class ParentClass_Image extends ParentClass {
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public ParentClass_Image setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        super.encrypt(key);
        try {
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.encrypt(key, imagePath);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        super.decrypt(key);
        try {
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.decrypt(key, imagePath);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------

}
