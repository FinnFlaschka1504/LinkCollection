package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activitys.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Video;

import java.io.IOException;
import java.util.List;

public class Utility {

    static public boolean isOnline(Context context) {
        boolean isOnleine = isOnline();
        if (isOnleine) {
            return true;
        } else {
            Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    static public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void restartApp(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void changeDialogKeyboard(Dialog dialog, boolean show) {
        if (show)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        else
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void saveDatabase(SharedPreferences mySPR_daten) {
        Gson gson = new Gson();
        Database database = Database.getInstance();
        if (database == null)
            return;

        mySPR_daten.edit()
                .putString(Database.VIDEO_MAP, gson.toJson(database.videoMap))
                .putString(Database.DARSTELLER_MAP, gson.toJson(database.darstellerMap))
                .putString(Database.STUDIO_MAP, gson.toJson(database.studioMap))
                .putString(Database.GENRE_MAP, gson.toJson(database.genreMap))
                .apply();
    }

    public static boolean containedInVideo(String query, Video video) {
        return contains(video.getTitel(), query) || containedInActors(query, video.getDarstellerList());
    }

    private static boolean contains(String all, String sub) {
        return all.toLowerCase().contains(sub.toLowerCase());
    }

    private static boolean  containedInActors(String query, List<String> actorUuids) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (contains(database.darstellerMap.get(actorUUid).getName(), query))
                return true;
        }
        return false;

    }

}
