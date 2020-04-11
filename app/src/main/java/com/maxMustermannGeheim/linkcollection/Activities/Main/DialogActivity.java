package com.maxMustermannGeheim.linkcollection.Activities.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.finn.androidUtilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;

public class DialogActivity extends AppCompatActivity {
    public static final String ACTION_RANDOM = "ACTION_RANDOM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        showSelectRandomDialog();
    }

    private void showSelectRandomDialog() {
        if (!Database.isReady())
            Database.getInstance(getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE), database -> Toast.makeText(this, "Datenbank geladen", Toast.LENGTH_SHORT).show());
        CustomDialog.Builder(this)
                .setTitle("Bereich auswählen")
                .addOnDialogDismiss(customDialog -> finish())
                .setView(R.layout.dialog_select_random_dialog)
                .setSetViewContent((customDialog, view, reload) -> {
                    view.findViewById(R.id.dialog_selectRandomDialog_videos).setOnClickListener(v -> {
                        startActivity(new Intent(this, VideoActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true));
                        finish();
                    });
                    view.findViewById(R.id.dialog_selectRandomDialog_knowledge).setOnClickListener(v -> {
                        startActivity(new Intent(this, KnowledgeActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true));
                        finish();
                    });
                    view.findViewById(R.id.dialog_selectRandomDialog_jokes).setOnClickListener(v -> {
                        startActivity(new Intent(this, JokeActivity.class).setAction(MainActivity.ACTION_SHOW_AS_DIALOG).putExtra(MainActivity.EXTRA_SHOW_RANDOM, true));
                        finish();
                    });
                })
                .show();
    }
}