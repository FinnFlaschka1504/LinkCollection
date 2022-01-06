package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSValue;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class CustomCode extends ParentClass {
    protected String code;
    protected String description;
    protected String defaultParams;
    protected String tempResult;

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
        private transient String sortType;
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
            Gson gson = new Gson();
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


        /**  ------------------------- Function ------------------------->  */
        @Override
        public void applyHelpers(JSContext js) {
//            Database database = Database.getInstance();

            JSFunction logTiming = new JSFunction(js,"logTiming") {
                public void logTiming() {
                    CustomUtility.logD(null, "logTiming: vvv");
                    CustomUtility.logTiming("CustomCode", true);
                }
            };
            js.property("logTiming", logTiming);

/*
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
                    CustomUtility.logD(null, "getAllList: vvvvv");
                    CustomUtility.logTiming("CustomCode", true);
                    ArrayList arrayList = gson.fromJson(gson.toJson(new ArrayList<>(database.videoMap.values())), ArrayList.class);
                    CustomUtility.logTiming("CustomCode", true);
                    CustomUtility.logD(null, "getAllList: ^^^^^");
                    return arrayList;
                }
            };
            js.property("getAllList", getAllList);
*/

            js.evaluateScript("var allObj = undefined;" +
                    "var getAllObj = () => {\n" +
                    "    return allObj = JSON.parse(fullMapJson);\n" +
                    "}\n");

            js.evaluateScript("var allList = undefined;" +
                    "var getAllList = () => {\n" +
                    "    return allList = Object.values(getAllObj());\n" +
                    "}\n");

            js.evaluateScript(" var getRandomVideo = () => {\n" +
                    "   getAllList();\n" +
                    "   return allList[Math.floor(Math.random()*allList.length)];\n" +
                    "}\n");

            js.evaluateScript(" var format = (key, text) => {\n" +
                    "   return `\\\\${key}{${text}}`\n" +
                    "}\n");

            js.evaluateScript("var getAllDays = () => {\n" +
                    "    let allDays = {};\n" +
                    "    getAllList().forEach(video => {\n" +
                    "        video.dateList.forEach(date => {\n" +
                    "            let newDate = new Date(date).setHours(0, 0, 0, 0);\n" +
                    "            if (newDate in allDays) {\n" +
                    "                allDays[newDate].push(video.uuid);\n" +
                    "            } else {\n" +
                    "                allDays[newDate] = [video.uuid];\n" +
                    "            }\n" +
                    "        });\n" +
                    "    });\n" +
                    "    return allDays;\n" +
                    "}\n");

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
        public JSValue executeCode(Context context, String... params) {
            Database database = Database.getInstance();
            JSContext js = new JSContext();
            js.property("params", new CustomList<>(params));
            js.property("sortType", "undefined");
            js.property("hasFormatting", false);
            String json = new Gson().toJson(database.videoMap);
            js.property("fullMapJson", json);

//            CustomUtility.logTiming("CustomCode", true);
            try {
                applyHelpers(js);
            } catch (Exception e) {
                String message = e.getMessage();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                CustomUtility.logD(null, "executeCode: %s", message);
            }
//            CustomUtility.logTiming("CustomCode", true);
            if (CustomUtility.stringExists(code)) {
                String fullCode = "let code = () => {\n" +
                        code +
                        "\n}\n" +
                        "code()";
                try {
                    JSValue result;
                    tempResult = (result = js.evaluateScript(fullCode)).toJSON();
                    JSValue sortType = js.property("sortType");
                    if (!sortType.equals("undefined"))
                        _setSortType(sortType.toString());
                    else
                        _setSortType(null);
                    return result;
                } catch (Exception e) {
                    return new JSValue(js, "Err.: " + e.getMessage());
                }
            }
            return null;
        }

        @Override
        public CharSequence applyFormatting(AppCompatActivity context, CharSequence text) {
            List<CustomUtility.Triple<Integer, Integer, String>> idMatches = new ArrayList<>();
            List<CustomUtility.Triple<Integer, Integer, String>> idMapMatches = new ArrayList<>();
            List<CustomUtility.Triple<Integer, Integer, String>> dateMatches = new ArrayList<>();
//            List<Pair<Integer, Integer>> boldMatches = new ArrayList<>();

            Pattern pattern = Pattern.compile("\\\\(\\w+)\\{(.*?)\\}");
            while (true) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    String type = matcher.group(1);
                    String match = matcher.group(2);
                    switch (type) {
                        case "id":
                            idMatches.add(CustomUtility.Triple.create(matchResult.start(), matchResult.end() - 5, match));
                            break;
                        case "idM":
                            ParentClass parentClass = Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, match);
                            if (parentClass != null) {
                                String name = parentClass.getName();
                                idMapMatches.add(CustomUtility.Triple.create(matchResult.start(), matchResult.end() - (6 + match.length() - name.length()), match));
                                match = name;
                            } else
                                idMapMatches.add(CustomUtility.Triple.create(matchResult.start(), matchResult.end() - 6, match));
                            break;
                        case "dt":
                            Date date = null;
                            if (match.matches("\\d+"))
                                date = new Date(Long.parseLong(match));

                            if (date != null) {
                                String formatDate = Utility.formatDate(Utility.DateFormat.DATE_DOT, date);
                                dateMatches.add(CustomUtility.Triple.create(matchResult.start(), matchResult.end() - (5 + match.length() - formatDate.length()), String.format("{[dt:%s]}", formatDate)));
                                match = formatDate;
                            }
//                            else
//                                new SimpleDateFormat().parse()
                            break;
//                        case "*":
//                            boldMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
//                            break;
//                        case "~":
//                            strikeMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
//                            break;
//                        case "/":
//                            italicMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
//                            break;
//                        case "_":
//                            underlineMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
//                            break;
                    }
                    text = matcher.replaceFirst(match);
                } else
                    break;
            }
            SpannableString resultSpan = new SpannableString(text);

            CustomUtility.GenericReturnInterface<String, ClickableSpan> getClickableSpan = uuid -> {
                return new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        context.startActivity(new Intent(context, VideoActivity.class)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH, uuid)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO));
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
            };

            idMatches.forEach(triple -> resultSpan.setSpan(getClickableSpan.run(triple.third), triple.first, triple.second, Spannable.SPAN_COMPOSING));
            idMapMatches.forEach(triple -> resultSpan.setSpan(getClickableSpan.run(triple.third), triple.first, triple.second, Spannable.SPAN_COMPOSING));
            dateMatches.forEach(triple -> resultSpan.setSpan(getClickableSpan.run(triple.third), triple.first, triple.second, Spannable.SPAN_COMPOSING));

            return resultSpan;
        }
        /**  <------------------------- Function -------------------------  */



        /**  ------------------------- Getter & Setter ------------------------->  */
        public String _getSortType() {
            return sortType;
        }

        public CustomCode_Video _setSortType(String sortType) {
            this.sortType = sortType;
            return this;
        }
        /**  <------------------------- Getter & Setter -------------------------  */
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

    public String getDefaultParams() {
        return defaultParams;
    }

    public CustomCode setDefaultParams(String defaultParams) {
        this.defaultParams = defaultParams;
        return this;
    }

    public String _getTempResult() {
        return tempResult;
    }

    public CustomCode _setTempResult(String tempResult) {
        this.tempResult = tempResult;
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

    public abstract JSValue executeCode(Context context, String... params);

    public abstract CharSequence applyFormatting(AppCompatActivity context, CharSequence text);
    /**  <------------------------- Function -------------------------  */



    /** ------------------------- Edit -------------------------> */
    public static void showAllDialoge(AppCompatActivity context, String name, Map<String,  CustomCode> map, Utility.GenericReturnOnlyInterface<CustomCode> newProvider) {
        CustomDialog.Builder(context)
                .setTitle("CustomCode - " + name)
                .setView(customDialog -> new CustomRecycler<CustomRecycler.Expandable<CustomCode>>(context)
                        .setGetActiveObjectList(customRecycler -> new CustomRecycler.Expandable.ToExpandableList<CustomCode, CustomCode>()
                                .keepExpandedState(customRecycler)
                                .runToExpandableList(map.values().stream().sorted(ParentClass::compareByName).collect(Collectors.toList()), null))
                        .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<CustomCode>(R.layout.list_item_url_parser, (customRecycler1, itemView, customCode, expanded, index) -> {
                            ((TextView) itemView.findViewById(R.id.listItem_urlParser_name)).setText(customCode.getName());
                            ((TextView) itemView.findViewById(R.id.listItem_urlParser_codeType)).setText("Code:");
                            TextView codeTextView = itemView.findViewById(R.id.listItem_urlParser_code);
                            codeTextView.setText(customCode.getCode());
                            codeTextView.setSingleLine(!expanded);
                            if (CustomUtility.stringExists(customCode.getDescription())) {
                                itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.VISIBLE);
                                ((TextView) itemView.findViewById(R.id.listItem_urlParser_thumbnailCode)).setText(customCode.getDescription());
                            } else
                                itemView.findViewById(R.id.listItem_urlParser_thumbnailCode_layout).setVisibility(View.GONE);

                            itemView.findViewById(R.id.listItem_urlParser_delete).setOnClickListener(v -> {
                                CustomDialog.Builder(context)
                                        .setTitle("CustomCode Löschen")
                                        .setText(Utility.dialogDeleteText(customCode.getName()))
                                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                            Database.getInstance().customCodeVideoMap.remove(customCode.getUuid());
                                            Database.saveAll(context, "Gelöscht", null, null);
                                            customRecycler.reload();
                                        })
                                        .show();
                            });

                        }))
                        .setOnLongClickListener((customRecycler, view, expandable, index) -> showEditDialog(context, name, map, customDialog, expandable.getObject(), newProvider))
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
                    TextInputLayout editLayoutParams = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params_layout);

                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper(editLayoutName, editLayoutCode, editLayoutDescription, editLayoutParams);
                    helper.defaultDialogValidation(customDialog)
                            .setValidation(editLayoutName, "\\w+")
                            .warnIfEmpty(editLayoutDescription)
                            .setInputType(editLayoutCode, Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE)
                            .setValidation(editLayoutParams, "^$|[^,]+(, *[^,]+)*");

                    if (!isAdd) {
                        editLayoutName.getEditText().setText(newCustomCode.getName());
                        editLayoutCode.getEditText().setText(newCustomCode.getCode());
                        editLayoutParams.getEditText().setText(newCustomCode.getDefaultParams());
                        editLayoutDescription.getEditText().setText(newCustomCode.getDescription());
                    }
                })
                .addButton("Testen", customDialog -> {
                    String newCode = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    String newParams = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params)).getText().toString().trim();
                    newCustomCode.setCode(newCode);
                    newCustomCode.setDefaultParams(newParams);
                    JSValue jsValue = newCustomCode.executeCode(context, parseParams(newParams));
                    String text = jsValue != null ? (jsValue.isString() ? jsValue.toString() : jsValue.toJSON()) : null;
                    if (CustomUtility.stringExists(text)) {
                        CharSequence dialogText;
                        if (jsValue.getContext().property("hasFormatting").toBoolean()) {
                            dialogText = newCustomCode.applyFormatting(context, text);
                        } else
                            dialogText = text;

                        CustomDialog.Builder(context)
                                .setTitle("Ergebnis")
                                .enableTitleBackButton()
                                .setText(dialogText)
                                .addOptionalModifications(customDialog1 -> {
                                    customDialog1.getTextTextView().setMovementMethod(LinkMovementMethod.getInstance());
                                })
                                .show();
                    } else
                        Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
                }, false)
                .alignPreviousButtonsLeft()
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String newName = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_name)).getText().toString().trim();
                    String newCode = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    String newParams = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params)).getText().toString().trim();
                    String newDescription = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_description)).getText().toString().trim();

                    newCustomCode
                            .setDescription(CustomUtility.stringExistsOrElse(newDescription, null))
                            .setCode(newCode)
                            .setDefaultParams(CustomUtility.stringExistsOrElse(newParams, null))
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
                .enableDynamicWrapHeight(context)
                .enableAutoUpdateDynamicWrapHeight()
                .show();
    }
    /**  <------------------------- Edit -------------------------  */



    /**  ------------------------- Convenience ------------------------->  */
    public static String[] parseParams(String params) {
        return Arrays.stream(params.split(",")).map(String::trim).filter(CustomUtility::stringExists).toArray(String[]::new);
    }

    public static CustomCode getCustomCodeByName(CategoriesActivity.CATEGORIES category, String name) {
        return (CustomCode) Utility.findObjectByName(category, name);
    }
    /**  <------------------------- Convenience -------------------------  */



    /** ------------------------- Encryption -------------------------> */
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(code)) code = AESCrypt.encrypt(key, code);
            if (Utility.stringExists(defaultParams)) defaultParams = AESCrypt.encrypt(key, defaultParams);
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
            if (Utility.stringExists(defaultParams)) defaultParams = AESCrypt.decrypt(key, defaultParams);
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
