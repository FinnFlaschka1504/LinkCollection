package com.maxMustermannGeheim.linkcollection.Activities.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;

import com.finn.androidUtilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.R;

public class DialogActivity extends AppCompatActivity {
    public static final String ACTION_RANDOM = "ACTION_RANDOM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        CustomDialog.Builder(this)
                .setTitle("Das Ist ein Test")
                .addOnDialogDismiss(customDialog -> finish())
                .show();
    }
}