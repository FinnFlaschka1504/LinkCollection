package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class ParentClass_Tmdb extends ParentClass_Image {
    private int tmdbId;

    public int getTmdbId() {
        return tmdbId;
    }

    public ParentClass_Tmdb setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }

    public void tryUpdateData(Context context, Runnable ifSuccessful) {
        if (tmdbId == 0)
            return;

        String requestUrl = "https://api.themoviedb.org/3/" +
                (this instanceof Darsteller ? "person" : "company") + "/" + tmdbId +
                "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            try {

                if (response.has("logo_path"))
                    setImagePath(response.getString("logo_path"));
                if (response.has("profile_path"))
                    setImagePath(response.getString("profile_path"));

                ifSuccessful.run();
            } catch (JSONException ignored) {}

        }, error -> {
        });

        requestQueue.add(jsonArrayRequest);
    }
}
