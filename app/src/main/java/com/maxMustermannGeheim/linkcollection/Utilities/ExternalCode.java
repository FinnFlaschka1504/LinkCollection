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
import java.util.Map;

public class ExternalCode {
    public enum ENTRY {
        GET_IMDB_EPISODE_DETAILS("getImdbEpisodeDetails"),
        GET_NEXT_EPISODE_IMDB_ID("getNextEpisodeImdbId"),
        GET_SEASON_IMDB_IDS("getSeasonImdbIds"),
        GET_WER_STREAMT_ES("getWerStreamtEs"),
        ;

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

    /**  ------------------------- Initialization ------------------------->  */
    private static void applyEntries() {
        Arrays.stream(ENTRY.values()).forEach(entry -> codeEntryMap.putIfAbsent(entry, new CodeEntry(entry)));
    }

    public static boolean initialize_ifNecessary(AppCompatActivity context) {
        if (initialized || !CustomUtility.isOnline())
            return false;

        sharedPreferences = context.getSharedPreferences(EXTERNAL_CODE, Context.MODE_PRIVATE);
        loadEntries(context);
        applyEntries();
        saveEntries(context);

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
                    CustomUtility.logD(null, "initialize_ifNecessary <json err.>: %s", e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            if (!updateList.isEmpty())
                fetchUpdates(context, updateList);
        }, volleyError -> {
            CustomUtility.logD(null, "initialize_ifNecessary <volley err.>: %s", volleyError.getMessage());
            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
        });
        return true;
    }

    private static void fetchUpdates(AppCompatActivity context, List<CodeEntry> updateList) {
        getJsonObject(context, URL_SNIPPETS, resultObject -> {
            for (CodeEntry codeEntry : updateList) {
                try {
                    JSONObject jsonObject = resultObject.getJSONObject(codeEntry.entry.name);
                    codeEntry.version = jsonObject.getInt("version");
                    codeEntry.code = jsonObject.getJSONObject("code");
                    CustomUtility.logD(null, "fetchUpdates: %s", codeEntry.entry.name);
                } catch (JSONException e) {
                    CustomUtility.logD(null, "fetchUpdates: <err.> %s: %s", codeEntry.entry.name, e.getMessage());
                }
            }
            saveEntries(context);
        }, volleyError -> {
            CustomUtility.logD(null, "fetchUpdates e2: %s", volleyError.getMessage());
            Toast.makeText(context, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    /**  <------------------------- Initialization -------------------------  */


    /** ------------------------- Convenience -------------------------> */
    public static CodeEntry getEntry(ENTRY entry) {
        return codeEntryMap.get(entry);
    }

    public static void getJsonObject(AppCompatActivity context, String url, CustomUtility.GenericInterface<JSONObject> onResult, @Nullable CustomUtility.GenericInterface<VolleyError> onError) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> CustomUtility.runGenericInterface(onResult, response),
                error -> CustomUtility.runGenericInterface(onError, error));

        requestQueue.add(jsonArrayRequest);
    }
    /** <------------------------- Convenience ------------------------- */



    /** ------------------------- Save and Load -------------------------> */
    private static void saveEntries(AppCompatActivity context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(EXTERNAL_CODE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        for (CodeEntry entry : codeEntryMap.values()) {
            String json = gson.toJson(entry);
            editor.putString(entry.entry.name, json);
        }
        editor.apply();
    }

    private static void loadEntries(AppCompatActivity context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(EXTERNAL_CODE, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        for (Map.Entry<String, ?> sprEntry : sharedPreferences.getAll().entrySet()) {
            Object value = sprEntry.getValue();
            if (value instanceof String) {
                String json = (String) value;
                CodeEntry codeEntry = gson.fromJson(json, CodeEntry.class);
                codeEntryMap.put(codeEntry.entry, codeEntry);
            }
        }
    }
    /** <------------------------- Save and Load ------------------------- */

    public static class CodeEntry {
        public ENTRY entry;
        public JSONObject code;
        public int version;

        public CodeEntry(ENTRY entry) {
            this.entry = entry;
        }

        /** ------------------------- Convenience -------------------------> */
        public String getString(String key) {
            return getString(key, "{}");
        }

        /** ------------------------- Convenience -------------------------> */
        public String getString(String key, String defaultValue) {
            try {
                return code.getString(key);
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        /**  <------------------------- Convenience -------------------------  */
    }
}