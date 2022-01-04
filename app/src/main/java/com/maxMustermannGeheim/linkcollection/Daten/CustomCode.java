package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSValue;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class CustomCode extends ParentClass {
    protected String code;
    protected String description;

    public abstract ParentClass getObjectById(String id);

    public abstract ParentClass getObjectByName(CategoriesActivity.CATEGORIES category, String name);

    /**  ------------------------- Constructor ------------------------->  */
    public CustomCode(String name) {
        uuid = "<err.>";
        this.name = name;
    }

    public CustomCode() {
    }
    /**  <------------------------- Constructor -------------------------  */


    /** <------------------------- Implementations ------------------------- */
    public static class CustomCode_Video extends CustomCode {
        private transient final Gson gson = new Gson();
//        private final ObjectMapper objectMapper = new ObjectMapper();

        /**  ------------------------- Constructor ------------------------->  */
        public CustomCode_Video(String name) {
            uuid = "customCodeVideo_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public CustomCode_Video() {
        }
        /**  <------------------------- Constructor -------------------------  */

        private Map<String, Object> map(Object o) {
//            return objectMapper.convertValue(o, Map.class);
            return gson.fromJson(gson.toJson(o), Map.class);
        }

        @Override
        public ParentClass getObjectById(String id) {
            switch (id.split("_")[0]) {
                case "video":
                    return Database.getInstance().videoMap.get(id);
                case "darsteller":
                    return Database.getInstance().darstellerMap.get(id);
                case "studio":
                    return Database.getInstance().studioMap.get(id);
                case "genre":
                    return Database.getInstance().genreMap.get(id);
                case "collection":
                    return Database.getInstance().collectionMap.get(id);
                case "watchList":
                    return Database.getInstance().watchListMap.get(id);
            }
            return null;
        }

        @Override
        public ParentClass getObjectByName(CategoriesActivity.CATEGORIES category, String name) {
            return Utility.findObjectByName(category, name);
        }

        @Override
        public void applyHelpers(JSContext js) {
            Database database = Database.getInstance();

            JSFunction getAll = new JSFunction(js,"getAll") {
                public String[] getAll() {
                    return database.videoMap.keySet().toArray(new String[]{});
                }
            };
            js.property("getAll", getAll);

            JSFunction getAllObj = new JSFunction(js,"getAllObj") {
                public Map getAllObj() {
                    return map(database.videoMap);
                }
            };
            js.property("getAllObj", getAllObj);

            JSFunction getAllList = new JSFunction(js,"getAllList") {
                public List<Object> getAllList() {
                    return gson.fromJson(gson.toJson(new ArrayList<>(database.videoMap.values())), ArrayList.class);
                }
            };
            js.property("getAllList", getAllList);

            JSFunction getById = new JSFunction(js,"getById") {
                public Map getById(String id) {
                    return map(getObjectById(id));
                }
            };
            js.property("getById", getById);

            JSFunction getByName = new JSFunction(js,"getByName") {
                public Map getByName(String category, String id) {
                    CategoriesActivity.CATEGORIES cat = CategoriesActivity.CATEGORIES.valueOf(category);
                    return map(getObjectByName(cat, id));
                }
            };
            js.property("getByName", getByName);

//            js.evaluateScript("var all = getAll()");
//            js.evaluateScript("var all = getAllList()\n" +
//                    "all = all.filter(video => video.rating >= 4.75)");
//            JSValue all = js.property("all");
//            all.toJSArray();
        }

        @Override
        public JSValue executeCode(Context context) {
            JSContext js = new JSContext();
            applyHelpers(js);
            if (CustomUtility.stringExists(code)) {
                String fullCode = "let code = () => {\n" +
                        code +
                        "\n}\n" +
                        "code()";
                try {
                    return js.evaluateScript(fullCode);
                } catch (Exception e) {
                    return new JSValue(js, "Err.: " + e.getMessage());
                }
            }
            return null;
        }

//        @Override
//        public void executeCode(Context context) {
//            Interpreter interpreter = new Interpreter();
//            try {
//                Database database = Database.getInstance();
//                interpreter.set("videoList", new CustomList<>(database.videoMap.values()));
////            CustomList<String> splitList = new CustomList<>(url.split("/"));
////            interpreter.set("split", splitList);
////            CustomList<String> lastSplitList = new CustomList<>();
////            String last = null;
////            if (!splitList.isEmpty()) {
////                last = splitList.getLast();
////                lastSplitList.add(last.split("-"));
////            }
////            if (last.contains("?")) {
////                lastSplitList.clear();
////                lastSplitList.add(last.split("\\?")[0].split("-"));
////            }
////            interpreter.set("last", last);
//                interpreter.set("result", null);
////            interpreter.set("lastSplit", lastSplitList);
//                interpreter.set("customList", new CustomList<String>());
//                Object resultO = interpreter.eval(replaceLambdas(code));
//                resultO.toString();
////            if (resultO instanceof CustomList)
////                resultO = String.join(" ", (CustomList<String>) resultO);
////            if (resultO == null) {
////                if (context != null)
////                    Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
////                onParseNameResult.run("");
////                return "";
////            }
////            String result = resultO.toString();
////            onParseNameResult.run(result);
////            return result;
//
//            } catch (EvalError evalError) {
//                if (context != null)
//                    Toast.makeText(context, evalError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
    }
    /**  ------------------------- Implementations ------------------------->  */



    /**  ------------------------- Getter & Setter ------------------------->  */
    public String getCode() {
        return code;
    }

    public CustomCode setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CustomCode setDescription(String description) {
        this.description = description;
        return this;
    }
    /**  <------------------------- Getter & Setter -------------------------  */


    /** ------------------------- Function -------------------------> */
//    public String replaceLambdas(String originalCode) {
//        originalCode = Pattern.compile("\\(\\) ?-> ?(.*?);").matcher(originalCode).replaceAll("new Runnable() {public void run() {$1;}};");
//        originalCode = Pattern.compile("(?<=.filter\\()<(\\w+?)>\\s*(\\w+) ?-> ?(.*),( ?true|false)\\);").matcher(originalCode).replaceAll("new Predicate<$1>() {public boolean test($1 $2) {return $3;}}, $4);");
//        originalCode = Pattern.compile("(?<=.filter\\()<(\\w+?)>\\s*(\\w+) ?-> ?\\{(.*)\\},( *true|false)\\);", Pattern.DOTALL).matcher(originalCode).replaceAll("new Predicate<$1>() {public boolean test($1 $2) {$3}}, $4);");
//        return originalCode;
//    }
    
    public abstract void applyHelpers(JSContext js);

    public abstract JSValue executeCode(Context context);
    /**  <------------------------- Function -------------------------  */


    /** ------------------------- Edit -------------------------> */
    public static void showAllDialoge(AppCompatActivity context, String name, Map<String,  CustomCode> map, Utility.GenericReturnOnlyInterface<CustomCode> newProvider) {
        CustomDialog.Builder(context)
                .setTitle("CustomCode - " + name)
                .setView(customDialog -> new CustomRecycler<CustomCode>(context)
                        .setGetActiveObjectList(customRecycler -> map.values().stream().sorted(ParentClass::compareByName).collect(Collectors.toList()))
                        .setItemLayout(R.layout.list_item_url_parser)
                        .setSetItemContent((customRecycler, itemView, customCode, index) -> {
                            ((TextView) itemView.findViewById(R.id.listItem_urlParser_name)).setText(customCode.getName());
                            ((TextView) itemView.findViewById(R.id.listItem_urlParser_codeType)).setText("Code:");
                            ((TextView) itemView.findViewById(R.id.listItem_urlParser_code)).setText(customCode.getCode());
                            if (CustomUtility.stringExists(customCode.getDescription())) {
                                itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.VISIBLE);
                                ((TextView) itemView.findViewById(R.id.listItem_urlParser_thumbnailCode)).setText(customCode.getDescription());
                            } else
                                itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.GONE);
                        })
                        .setOnLongClickListener((customRecycler, view, customCode_video, index) -> showEditDialog(context, name, map, customDialog, customCode_video, newProvider))
                        .addOptionalModifications(customDialog::setPayload)
                        .generateRecyclerView())
                .setDimensionsFullscreen()
                .disableScroll()
                .addButton(R.drawable.ic_add, customDialog -> {
                    showEditDialog(context, name, map, customDialog, null, newProvider);
                }, false)
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                .show();
    }

    private static void showEditDialog(AppCompatActivity context, String name, Map<String, CustomCode> map, CustomDialog paretDialog, @Nullable CustomCode oldCustomCode, Utility.GenericReturnOnlyInterface<CustomCode> newProvider) {
        boolean isAdd = oldCustomCode == null;
        CustomCode newCustomCode = isAdd ? newProvider.run() : (CustomCode) oldCustomCode.clone();
        CustomDialog.Builder(context)
                .setTitle("CustomCode " + (isAdd ? "Hinzufügen" : "Bearbeiten"))
                .setView(R.layout.dialog_edit_custom_code_video)
                .enableTitleRightButton(R.drawable.ic_help, customDialog1 -> Toast.makeText(context, "ToDo", Toast.LENGTH_SHORT).show())
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout editLayoutName = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_name_layout);
                    TextInputLayout editLayoutCode = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code_layout);
                    TextInputLayout editLayoutDescription = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_description_layout);

                    // ToDo: herausfinden warum InputType verändert wird
                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper(editLayoutName, editLayoutCode, editLayoutDescription);
                    helper.defaultDialogValidation(customDialog)
                            .warnIfEmpty(editLayoutDescription)
                            .setInputType(editLayoutName, Helpers.TextInputHelper.INPUT_TYPE.CAP_SENTENCES)
                            .setInputType(editLayoutCode, Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE);

                    if (!isAdd) {
                        editLayoutName.getEditText().setText(newCustomCode.getName());
                        editLayoutCode.getEditText().setText(newCustomCode.getCode());
                        editLayoutDescription.getEditText().setText(newCustomCode.getDescription());
                    }
                })
                .addButton("Testen", customDialog1 -> {
                    String newCode = ((EditText) customDialog1.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    newCustomCode.setCode(newCode);
                    JSValue jsValue = newCustomCode.executeCode(context);
                    String text = jsValue != null ? jsValue.toJSON() : null;
                    if (CustomUtility.stringExists(text)) {
                        CustomDialog.Builder(context)
                                .setTitle("Ergebnis")
                                .enableTitleBackButton()
                                .setText(text)
                                .show();
                    } else
                        Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
                }, false)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String newName = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_name)).getText().toString().trim();
                    String newCode = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    String newDescription = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_description)).getText().toString().trim();

                    newCustomCode
                            .setDescription(CustomUtility.stringExistsOrElse(newDescription, null))
                            .setCode(newCode)
                            .setName(newName);

                    if (isAdd)
                        map.put(newCustomCode.getUuid(), newCustomCode);
                    else
                        oldCustomCode.getChangesFrom(newCustomCode);

                    ((CustomRecycler) paretDialog.getPayload()).reload();
                    Database.saveAll(context);
                    // ToDo: warum sagt auch gespeichert wenn nix verändert
                })
                .disableLastAddedButton()
                .show();
    }
    /**  <------------------------- Edit -------------------------  */


    /** ------------------------- Encryption -------------------------> */
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(code)) code = AESCrypt.encrypt(key, code);
            if (Utility.stringExists(description)) description = AESCrypt.encrypt(key, description);
//            if (Utility.stringExists(thumbnailCode))
//                thumbnailCode = AESCrypt.encrypt(key, thumbnailCode);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(code)) code = AESCrypt.decrypt(key, code);
            if (Utility.stringExists(description)) description = AESCrypt.decrypt(key, description);
//            if (Utility.stringExists(thumbnailCode))
//                thumbnailCode = AESCrypt.decrypt(key, thumbnailCode);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    /**  <------------------------- Encryption -------------------------  */
}
