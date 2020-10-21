package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivityResultListener extends AppCompatActivity {
    public static final int FILE_SELECT_CODE = 12345;
    public static final String EXTRA_UUID = "EXTRA_UUID";

    private static Map<String, Pair<Utility.GenericInterface<AppCompatActivity>, Utility.GenericInterface<Object>[]>> requestMap = new HashMap<>();
    private String uuid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        String imeType = "text/plain";
        super.onCreate(savedInstanceState);

        uuid = getIntent().getStringExtra(EXTRA_UUID);

        if (!CustomUtility.stringExists(uuid))
            return;

        if (requestMap.containsKey(uuid))
            requestMap.get(uuid).first.runGenericInterface(this);
    }

    //  ------------------------- Generic ------------------------->
    @SafeVarargs
    public static void addGenericRequest(AppCompatActivity activity, Utility.GenericInterface<AppCompatActivity> startActivity, Utility.GenericInterface<Object>... onUriResult_Fail) {
        String uuid = UUID.randomUUID().toString();
        requestMap.put(uuid, Pair.create(startActivity, onUriResult_Fail));
        activity.startActivity(new Intent(activity, ActivityResultListener.class).putExtra(EXTRA_UUID, uuid));
    }
    //  <------------------------- Generic -------------------------


    //  ------------------------- FileChooser ------------------------->
    public static final String uriRegex = "^content:\\/\\/([\\w\\/%\\.]+)+$";
    @SafeVarargs
    public static void addFileChooserRequest(AppCompatActivity activity, String imeType, Utility.GenericInterface<Object>... onUriResult_Fail) {
        String uuid = UUID.randomUUID().toString();
        if (onUriResult_Fail.length > 0) {
            Utility.GenericInterface<Object> oldInterface = onUriResult_Fail[0];
            onUriResult_Fail[0] = o -> {
                Intent data = (Intent) o;
                Uri uri = data.getData();
                if (uri != null)
                    activity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                oldInterface.runGenericInterface(o);
            };
        }
        requestMap.put(uuid, Pair.create(dummyActivity -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType(imeType).addCategory(Intent.CATEGORY_OPENABLE);
            dummyActivity.startActivityForResult(Intent.createChooser(intent, "Export-Datei Auswählen"), FILE_SELECT_CODE);
        }, onUriResult_Fail));

        activity.startActivity(new Intent(activity, ActivityResultListener.class).putExtra(EXTRA_UUID, uuid));
    }
    //  <------------------------- FileChooser -------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                Utility.GenericInterface<Object>[] interfaces = requestMap.get(uuid).second;
                if (resultCode == RESULT_OK) {
                    if (interfaces.length > 0)
                        interfaces[0].runGenericInterface(data);
                } else {
                    if (interfaces.length > 1)
                        interfaces[1].runGenericInterface(null);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
