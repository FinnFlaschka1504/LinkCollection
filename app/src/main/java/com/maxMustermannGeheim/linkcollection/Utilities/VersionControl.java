package com.maxMustermannGeheim.linkcollection.Utilities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomRecycler;
import com.google.gson.JsonObject;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.R;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static android.content.Context.DOWNLOAD_SERVICE;

public class VersionControl {
    private static final String TAG = "VersionControl";

    private static final String URL_APK = "https://www.googleapis.com/drive/v3/files/" + "1wGPMttpXXoqH6C8KI4P4A4Unec-4DyU2" + "?alt=media&key=" + "AIzaSyAO-j9mydwwiZ9S9iRVaB9lfkcmlM4Jgmk";
    private static final String URL_JSON = "https://www.googleapis.com/drive/v3/files/" + "1Ia1IlO0I0NypvstIbXDZbdxb4tMOmCUp" + "?alt=media&key=" + "AIzaSyAO-j9mydwwiZ9S9iRVaB9lfkcmlM4Jgmk";//"https://docs.google.com/uc?export=download&id=1k6Chaa7ghHuGO-2KMgNhbl3BQUNZO-rs";
    private static final String FILE_NAME = "SecondMind";

    private static RequestQueue requestQueue;

    /**
     * 'URL_APK' ändern
     * 'FILE_NAME' ändern
     * sicherstellen, dass 'REQUEST_INSTALL_PACKAGES' Berechtigungen erteilt sind
     * Provider in Manifest hinterlegt?
     * 'provider_paths.xml' existiert?
     * Speicherberechtigungen überprüfen & 'onRequestPermissionsResult' implementieren
     * Sicherstellen, dass die App Parkete installieren darf
     */

    public static void checkForUpdate(Activity activity, boolean visible) {
        if (!Utility.isOnline(activity))
            return;

        String fileName = Settings.getSingleSetting(activity, Settings.UPDATE_FILE_NAME);
        if (Utility.stringExists(fileName)) {
            File file = new File(fileName);
            if (file.exists())
                file.delete();
            Settings.changeSetting(Settings.UPDATE_FILE_NAME, "");
        }

        requestQueue = Volley.newRequestQueue(activity);

        if (visible)
            Toast.makeText(activity, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, URL_JSON, null, response -> {
            try {
                final String version = getVersion(activity);
                final String newVersion = response.getJSONArray("elements").getJSONObject(0).getString("versionName");

                if (version.equals(newVersion)) {
                    if (visible)
                        CustomDialog.Builder(activity)
                                .setTitle("Kein Update Verfügbar")
                                .setText("Die App ist bereits auf dem neusten Stand \n(Aktuelle Version: " + version + ")")
                                .addButton("Trotzdem Herunterladen", dialog -> updateApp(activity, newVersion))
                                .alignPreviousButtonsLeft()
                                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                                .show();
                } else
                    CustomDialog.Builder(activity)
                            .setTitle("Update Verfügbar")
                            .setText("Die Version " + newVersion + " steht zum Download bereit. \n(Aktuelle Version: " + version + ")")
                            .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                            .addButton("Herunterladen", dialog -> {
                                if (hasPermissions(activity, true)) {
                                    updateApp(activity, newVersion);
                                    dialog.dismiss();
                                }
                            },false)
                            .colorLastAddedButton()
                            .show();


            } catch (JSONException ignored) {
            }

        }, error -> {
            if (visible)
                Toast.makeText(activity, "Fehler", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);
    }

    public static void updateApp(Activity activity, String newVersion) {
        Toast.makeText(activity, "Updaten...", Toast.LENGTH_SHORT).show();

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL_APK));
        request.setDescription("Die neuste Version der App wird heruntergeladen");
        request.setTitle("Update herunterladen");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME + " (Vers. " + newVersion + ").apk");

        long enq = downloadManager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enq);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                            if (uriString.substring(0, 7).matches("file://")) {
                                uriString = uriString.substring(7);
                            }

                            uriString = uriString.replaceAll("%20", " ");
                            File file = new File(uriString);

                            Settings.changeSetting(Settings.UPDATE_FILE_NAME, uriString);

                            Uri uriFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                            String mimetype = downloadManager.getMimeTypeForDownloadedFile(downloadId);
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.setDataAndType(uriFile, mimetype);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            myIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);
                            activity.startActivity(myIntent);
                            context.unregisterReceiver(this);
                        }
                    }
                }
            }
        };

        activity.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //  ----- Berechtigungen ----->
    public static boolean isStoragePermissionGranted(Activity activity, boolean act) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted");
            return true;
        } else if (act) {

            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return false;
    }

    public static boolean isInstallPermissionGranted(Activity activity, boolean act) {
        if (activity.getPackageManager().canRequestPackageInstalls()) {
            Log.v(TAG, "Permission is granted");
            return true;
        } else if (act) {

            Log.v(TAG, "Permission is revoked");
            Toast.makeText(activity, "Installation aus unbekannter Quelle zulassen", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + activity.getApplicationInfo().packageName));
            activity.startActivity(intent);
            return false;
        }
        return false;
    }

    public static boolean hasPermissions(Activity activity, boolean act) {
        return isStoragePermissionGranted(activity, act) && isInstallPermissionGranted(activity, act);
    }
    //  <----- Berechtigungen -----


    //  ----- Version ----->
    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            Log.d("MyApp", "Version Name : " + version + "\n Version Code : " + versionCode);
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d("MyApp", "PackageManager Catch : " + e.toString());
        }
        return "";
    }

    public static int compareVersions(String thisVers, String thatVers) {
        return thisVers.compareTo(thatVers);
    }
    //  <----- Version -----


    //  ----- Change-Log ----->
    private static List<Pair<String, List<String>>> changeList = new ArrayList<>();

    private static Runnable addChanges = () -> {
        addChange("1.0", "Fast alles");
        addChange("2.0", "VersionControl hinzugefügt");
        addChange("2.1", "Bei der Datenbank anmelden begonnen");
        addChange("2.2", "Bei der Datenbank anmelden abgeschlossen",
                "Datenbanken können gelöscht und umbenannt werden");
        addChange("2.3", "Bezeichnungen in den Datenbankeinstellungen überarbeitet",
                "Film- und Serien-Genres können aus der TMDb importiert werden");
        addChange("2.4", "Das Verschlüsseln der Bereiche ist jetzt 'idiotensicher' - beim Anmelden, oder Wechseln von Datenbanken können keine Probleme mehr auftreten",
                "Die Schloss-Icons in den Einstellungen öffnen nun auch die Verschlüsselungs-Einstellungen",
                "Die Update-Datei wird beim nächsten Öffnen der App automatisch gelöscht",
                "BugFix: Episoden-Ansichten können nun auch über die Detailansicht hinzugefügt werden");
        addChange("2.5", "Darsteller und Studios können aus der TMDb importiert werden",
                "Design der Listenelemente in Videos, Serien, Wissen und Schulden verbessert");
        addChange("2.6", "Edit-Dialoge können jetzt nicht mehr ausversehen abgebrochen werden",
                "Detail-Dialoge überarbeitet",
                "Beim Öffnen der Tastatur passt sich automatisch die Höhe des EditVideo-Dialog an");
        addChange("2.7", "Dropdown in EditVideos past sich dynamisch der verfügbaren Höhe an",
                "Nach dem Löschen eines Eintrags werden sofort alle dazugehörigen Dialoge gelöscht",
                "Dropdown in EditShow bleibt geschlossen nach der Auswahl eines Items",
                "Bilder zur Darsteller- und Studio-Übersicht hinzugefügt");
        addChange("3.0", "Medien können verwaltet werden",
                "Fast-Scroll zu einzelnen Aktivitäten hinzugefügt",
                "Viele weitere kleine Änderungen");
        addChange("3.1", "Fast-Scroll-Popup hinzugefügt",
                "Fast-Scroll zu allen Aktivitäten hinzugefügt");
        addChange("3.2", "Bottom-Padding zu Recyclern hinzugefügt",
                "Verschlüsselungs-Bug behoben",
                "Divider in Listen angepasst");
    };

    private static void addChange(String version, String... changes) {
        if (changes.length == 0)
            return;
        changeList.add(new Pair<>(version, Arrays.asList(changes)));
    }

    public static void showChangeLog(AppCompatActivity activity, boolean force) {
        Settings.startSettings_ifNeeded(activity);

        String lastVersion = Settings.settingsMap.get(Settings.LAST_VERSION);
        String version = getVersion(activity);
        if (!force && (lastVersion.equals(version)))
            return;

        if (changeList.isEmpty()) {
            addChanges.run();

            Collections.reverse(changeList);

            boolean hasUpcomming = compareVersions(changeList.get(0).first, version) == 1;
            if (hasUpcomming) {
                Pair<String, List<String>> upcommingChange = changeList.remove(0);
                changeList.add(0, new Pair<>(upcommingChange.first + " <bald>", upcommingChange.second));
            }

            if (!force || hasUpcomming) {
                Optional<Pair<String, List<String>>> thisChange_optional = changeList.stream().filter(stringStringPair -> stringStringPair.first.equals(version)).findFirst();
                if (thisChange_optional.isPresent()) {
                    Pair<String, List<String>> thisChange = thisChange_optional.get();
                    int indexNew = changeList.indexOf(thisChange);
                    changeList.remove(thisChange);
                    changeList.add(indexNew, new Pair<>(thisChange.first + " <neu>", thisChange.second));
                }

                if (!version.equals(lastVersion)) {
                    Optional<Pair<String, List<String>>> lastChange_optional = changeList.stream().filter(stringStringPair -> stringStringPair.first.equals(lastVersion)).findFirst();
                    if (lastChange_optional.isPresent()) {
                        Pair<String, List<String>> lastChange = lastChange_optional.get();
                        int indexOld = changeList.indexOf(lastChange);
                        changeList.remove(lastChange);
                        changeList.add(indexOld, new Pair<>(lastChange.first + " <alt>", lastChange.second));
                    }
                }
            }
        }

        CustomDialog.Builder(activity)
                .setTitle("Change-Log")
                .setView(new CustomRecycler<Pair<String, List<String>>>(activity)
                        .setObjectList(changeList)
                        .setItemLayout(R.layout.list_item_change_log_version)
                        .setSetItemContent((customRecycler, itemView, change, index) -> {
                            ((TextView) itemView.findViewById(R.id.changeLogList_Version)).setText(change.first);
                            new CustomRecycler<String>(activity, itemView.findViewById(R.id.changeLogList_changeList))
                                    .setItemLayout(R.layout.list_item_change_log_change)
                                    .setObjectList(change.second)
                                    .setSetItemContent((customRecycler1, itemView1, changeText, index1) -> ((TextView) itemView1.findViewById(R.id.changeLogList_change))
                                            .setText(changeText))
                                    .generate();
                        })
                        .removeLastDivider()
                        .enableDivider()
                        .generateRecyclerView())
                .enableTitleBackButton()
                .disableScroll()
                .show_dialog().setOnDismissListener(dialog -> Settings.changeSetting(Settings.LAST_VERSION, version));

    }
    //  <----- Change-Log -----
}
