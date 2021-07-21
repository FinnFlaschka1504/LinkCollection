package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.maxMustermannGeheim.linkcollection.R;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
    }
}

/*
KONZEPT:
• Sammlung von Fotos und Videos

• Kategorien:
    • Personen
    • Tags
    • Gruppen (Automatische Tags?) (Bsp. Feiern, Reisen, Freizeit)
        • Untergruppen


Features:
• Mehrere Tags auf mehrere Medien

Libraries:
• https://github.com/VEINHORN/ScrollGalleryView (ScrollGallery)
• https://android-arsenal.com/details/1/7803 (Media Slider)
 */