package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomUtility;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExternalCode {
    public enum ENTRY {
        GET_IMDB_EPISODE_DETAILS("getImdbEpisodeDetails");

        String name;

        ENTRY(String name) {
            this.name = name;
        }
    }

    private static final String URL_BASE = "https://www.googleapis.com/drive/v3/files/";
    private static final String URL_MEDIA_KEY = "?alt=media&key=" + "AIzaSyAO-j9mydwwiZ9S9iRVaB9lfkcmlM4Jgmk";
    private static final String URL_VERSIONS_ID = "1BmrME7G8whz8X4v-17bw0K8DV-gJAbGh";
    private static final String URL_SNIPPETS_ID = "1Qryp86OU8f_6oLkFaLuis2dNGG12k3kf";
    private static final String URL_VERSIONS = URL_BASE + URL_VERSIONS_ID + URL_MEDIA_KEY;
    private static final String URL_SNIPPETS = URL_BASE + URL_SNIPPETS_ID + URL_MEDIA_KEY;
    private static HashMap<ENTRY, CodeEntry> codeEntryMap = new HashMap<>();
    private static boolean initialized;
    private static SharedPreferences sharedPreferences;
    public static final String EXTERNAL_CODE = "EXTERNAL_CODE";

    public static void applyEntries() {
        Arrays.stream(ENTRY.values()).forEach(entry -> codeEntryMap.put(entry, new CodeEntry(entry)));
    }

    public static boolean initialize_ifNecessary(AppCompatActivity context) {
        if (initialized && CustomUtility.isOnline())
            return false;

        sharedPreferences = context.getSharedPreferences(EXTERNAL_CODE, Context.MODE_PRIVATE);
        applyEntries(); // ToDo: aus Shared Preferences laden und dann mit defaults mergen

        getJsonObject(context, URL_VERSIONS, jsonObject -> {
            initialized = true;
            List<CodeEntry> updateList = new ArrayList<>();
            for (CodeEntry codeEntry : codeEntryMap.values()) {
                try {
                    if (codeEntry.version != jsonObject.getInt(codeEntry.entry.name)) {
                        updateList.add(codeEntry);
                        CustomUtility.logD(null, "initialize_ifNecessary: %s needs Update", codeEntry.entry.name);
                    }
                } catch (JSONException e) {
                    CustomUtility.logD(null, "initialize_ifNecessary e1: %s", e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            if (!updateList.isEmpty())
                fetchUpdates(context, updateList);
        }, volleyError -> {
            CustomUtility.logD(null, "initialize_ifNecessary e2: %s", volleyError.getMessage());
            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
        });
        return true;
    }

    public static void fetchUpdates(AppCompatActivity context, List<CodeEntry> updateList) {
        getJsonObject(context, URL_SNIPPETS, resultObject -> {
            try {
                for (CodeEntry codeEntry : updateList) {
                    JSONObject jsonObject = resultObject.getJSONObject(codeEntry.entry.name);
                    codeEntry.version = jsonObject.getInt("version");
                    codeEntry.code = jsonObject.getJSONObject("code");
                    Gson gson = new Gson();
                    String json = gson.toJson(codeEntry);
                    CustomUtility.logD(null, "fetchUpdates: %s", json);
                    CustomUtility.logD(null, "fetchUpdates: %s", gson.fromJson(json, CodeEntry.class).code);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {
            CustomUtility.logD(null, "fetchUpdates e2: %s", volleyError.getMessage());
            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /** ------------------------- Convenience -------------------------> */
    public static void getJsonObject(AppCompatActivity context, String url, CustomUtility.GenericInterface<JSONObject> onResult, @Nullable CustomUtility.GenericInterface<VolleyError> onError) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> CustomUtility.runGenericInterface(onResult, response),
                error -> CustomUtility.runGenericInterface(onError, error));

        requestQueue.add(jsonArrayRequest);
    }

    /** <------------------------- Convenience ------------------------- */

    public static class CodeEntry {
        public ENTRY entry;
        public JSONObject code;
        public int version;

        public CodeEntry(ENTRY entry) {
            this.entry = entry;
        }
    }
}


/**
 * 2 Datein:
 * 1. Datei über Versionen
 * 2. Datei mit Code
 * <p>
 * Versionen:
 * Objekt mit Namen und deren Version
 */