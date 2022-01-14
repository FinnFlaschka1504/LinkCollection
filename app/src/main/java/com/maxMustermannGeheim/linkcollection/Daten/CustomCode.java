package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import org.liquidplayer.javascript.JSArray;
import org.liquidplayer.javascript.JSBaseArray;
import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSValue;

import java.security.GeneralSecurityException;
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
import java.util.stream.Stream;

public abstract class CustomCode extends ParentClass {
    protected String code;
    protected String description;
    protected String defaultParams;
    protected String tempResult;
    protected RETURN_TYPE returnType = RETURN_TYPE.TEXT;

    enum RETURN_TYPE {
        TEXT, LIST, GROUPED_LIST, STATISTIC;
        // ToDo: implementieren ^^
    }

    public abstract ParentClass getObjectById(String id);

    public abstract ParentClass getObjectByName(CategoriesActivity.CATEGORIES category, String name);

    /** ------------------------- Constructor -------------------------> */
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

        /** ------------------------- Constructor -------------------------> */
        public CustomCode_Video(String name) {
            uuid = "customCodeVideo_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public CustomCode_Video() {
        }

        /** <------------------------- Constructor ------------------------- */

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


        /** ------------------------- Function -------------------------> */
        @Override
        public void applyHelpers(JSContext js) {
            JSFunction logTiming = new JSFunction(js, "logTiming") {
                public void logTiming() {
                    CustomUtility.logD(null, "logTiming: vvv");
                    CustomUtility.logTiming("CustomCode", true);
                }
            };
            js.property("logTiming", logTiming);

            js.evaluateScript("var allObj = undefined;" +
                    "var getAllObj = () => {\n" +
                    "    return allObj = JSON.parse(fullMapJson);\n" +
                    "}\n");

            js.evaluateScript("var allList = undefined;" +
                    "var getAllList = () => {\n" +
                    "    return allList = Object.values(getAllObj());\n" +
                    "}\n");

            Gson gson = new Gson();
            JSFunction getAllCatJson = new JSFunction(js, "getAllCatJson") {
                public String getAllCatJson(String cat) {
                    CategoriesActivity.CATEGORIES category = CategoriesActivity.CATEGORIES.valueOf(cat);
                    Map<String, ? extends ParentClass> map = Utility.getMapFromDatabase(category);
                    return gson.toJson(map);
                }
            };
            js.property("getAllCatJson", getAllCatJson);
            js.evaluateScript("var getAllCat = (cat) => {\n" +
                    "    let json = getAllCatJson(cat);\n" +
                    "    return JSON.parse(json);\n" +
                    "}");


            js.evaluateScript("var getRandomVideo = () => {\n" +
                    "   getAllList();\n" +
                    "   return allList[Math.floor(Math.random() * allList.length)];\n" +
                    "}\n");

            js.evaluateScript("var format = (key, text = '', params = '') => {\n" +
                    "    if (key != undefined && typeof key == \"string\" && key.match(/\\w+_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b/)) {\n" +
                    "        return `\\\\idM${text == '' ? '' : ':' + text}{${key}}`\n" +
                    "    } else if (typeof key == 'object' && 'uuid' in key) {\n" +
                    "        return `\\\\idM${text == '' ? '' : ':' + text}{${key.uuid}}`\n" +
                    "    }\n" +
                    "    return `\\\\${key}${params == '' ? '' : ':' + params}{${text}}`\n" +
                    "}");

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

            JSFunction getById = new JSFunction(js, "getById") {
                public Map getById(String id) {
                    return map(getObjectById(id));
                }
            };
            js.property("getById", getById);

            JSFunction getByName = new JSFunction(js, "getByName") {
                public Map getByName(String category, String id) {
                    CategoriesActivity.CATEGORIES cat = CategoriesActivity.CATEGORIES.valueOf(category);
                    return map(getObjectByName(cat, id));
                }
            };
            js.property("getByName", getByName);
            js.evaluateScript("var VIDEO = 'VIDEO';\n" +
                    "var DARSTELLER = 'DARSTELLER';\n" +
                    "var STUDIOS = 'STUDIOS';\n" +
                    "var GENRE = 'GENRE';\n" +
                    "var COLLECTION = 'COLLECTION';\n" +
                    "var WATCH_LIST = 'WATCH_LIST';");


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
            js.property("hasHighlight", false);
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
                        applyCodeShortcuts(code) +
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
            List<CustomUtility.Triple<Integer, Integer, Object>> paramsList = new ArrayList<>();

            Pattern pattern = Pattern.compile("\\\\(\\w*)(?::(\\w+(?:\\|\\w+)*))*\\{(.*?)\\}");
            while (true) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    String type = matcher.group(1);
                    String params = matcher.group(2);
                    final String[] match = {matcher.group(3)};

                    int offset = type.length() + 3 + (params != null ? params.length() + 1 : 0);
                    switch (type) {
                        case "id":
                            addToList(idMatches, matchResult, match, offset, null, null, CustomUtility.Triple.create(context, params, paramsList));
                            break;
                        case "idM":
                            ParentClass parentClass = getObjectById(match[0]);
                            if (parentClass != null) {
                                String name = parentClass.getName();
                                String search = null;
                                if (parentClass instanceof Darsteller)
                                    search = String.format("{[a:%s]}", name);
                                else if (parentClass instanceof Studio)
                                    search = String.format("{[s:%s]}", name);
                                else if (parentClass instanceof Genre)
                                    search = String.format("{[g:%s]}", name);
                                else if (parentClass instanceof Collection)
                                    search = String.format("{[c:%s]}", name);

                                addToList(idMapMatches, matchResult, match, offset, name, search, CustomUtility.Triple.create(context, params, paramsList));
                            } else
                                addToList(idMapMatches, matchResult, match, offset, null, null, CustomUtility.Triple.create(context, params, paramsList));
                            break;
                        case "dt":
                            Date date = null;
                            if (match[0].matches("\\d+"))
                                date = new Date(Long.parseLong(match[0]));

                            if (date != null) {
                                String formatDate = Utility.formatDate(Utility.DateFormat.DATE_DOT, date);
                                addToList(dateMatches, matchResult, match, offset, formatDate, String.format("{[dt:%s]}", formatDate), CustomUtility.Triple.create(context, params, paramsList));
                            }
                            break;
                        case "":
                            Pair<String, Integer> pair = ellipseString(params, match[0]);
                            offset += pair.second;
                            match[0] = pair.first;
                            addToParamsList(CustomUtility.Triple.create(matchResult.start(), matchResult.end() - offset, match[0]), CustomUtility.Triple.create(context, params, paramsList));
                            break;
                    }
                    text = matcher.replaceFirst(match[0]);
                } else
                    break;
            }
            SpannableString resultSpan = new SpannableString(text);

            CustomUtility.GenericReturnInterface<String, ClickableSpan> getClickableSpan = query -> {
                return new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        context.startActivity(new Intent(context, VideoActivity.class)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH, query)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, Utility.isUuid(query) ? CategoriesActivity.CATEGORIES.getById(query) : CategoriesActivity.CATEGORIES.VIDEO));
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
            paramsList.forEach(triple -> resultSpan.setSpan(triple.third, triple.first, triple.second, Spannable.SPAN_COMPOSING));

            return resultSpan;
        }

        private Pair<String, Integer> ellipseString(String params, String match) {
            int ellipseOffset = 0;
            Matcher ellipseMatcher = Pattern.compile("\\be\\d+\\b").matcher(params != null ? params : "");
            if (ellipseMatcher.find()) {
                int max = Integer.parseInt(ellipseMatcher.group(0).substring(1));
                int prev = match.length();
                if (prev > max) {
                    String original;
                    match = (original = match.substring(0, max - 1)).trim();
                    ellipseOffset = prev - max + (original.length() - match.length());
                    match += "…";
                }
            }
            return Pair.create(match, ellipseOffset);
        }

        private CustomUtility.Triple<Integer, Integer, String> addToList(List<CustomUtility.Triple<Integer, Integer, String>> list, MatchResult matchResult, String[] match, int offset, @Nullable String replacement, @Nullable String search, CustomUtility.Triple<AppCompatActivity, String, List<CustomUtility.Triple<Integer, Integer, Object>>> paramsTriple) {
            CustomUtility.Triple<Integer, Integer, String> triple;
            if (CustomUtility.stringExists(replacement)) {
                Pair<String, Integer> pair = ellipseString(paramsTriple.second, replacement);
                offset += pair.second;
                triple = CustomUtility.Triple.create(matchResult.start(), matchResult.end() - (offset + match[0].length() - replacement.length()), search != null ? search : match[0]);
                replacement = pair.first;
                list.add(triple);
                match[0] = replacement;
            } else {
                Pair<String, Integer> pair = ellipseString(paramsTriple.second, match[0]);
                offset += pair.second;
                String original = match[0];
                match[0] = pair.first;
                triple = CustomUtility.Triple.create(matchResult.start(), matchResult.end() - offset, search != null ? search : original);
                list.add(triple);
            }
            addToParamsList(triple, paramsTriple);
            return triple;
        }

        private void addToParamsList(CustomUtility.Triple<Integer, Integer, String> triple, CustomUtility.Triple<AppCompatActivity, String, List<CustomUtility.Triple<Integer, Integer, Object>>> paramsTriple) {
            if (!CustomUtility.stringExists(paramsTriple.second))
                return;
            for (String param : paramsTriple.second.split("\\|")) {
                param = param.trim();
                Object span;
                if (param.equalsIgnoreCase("u"))
                    span = new UnderlineSpan();
                else if (param.equalsIgnoreCase("b"))
                    span = new StyleSpan(Typeface.BOLD);
                else if (param.equalsIgnoreCase("i"))
                    span = new StyleSpan(Typeface.ITALIC);
                else if (param.equalsIgnoreCase("s"))
                    span = new StrikethroughSpan();
                else if (param.startsWith("0x"))
                    span = new ForegroundColorSpan((Integer.parseUnsignedInt(param.substring(2), 16) + (param.length() == 8 ? 0xff000000 : 0)));
                else if (param.equalsIgnoreCase("nh"))
                    span = new ForegroundColorSpan(paramsTriple.first.getColor(R.color.colorText));
                else if (param.equalsIgnoreCase("h"))
                    span = new ForegroundColorSpan(paramsTriple.first.getColor(R.color.colorAccent));
                else
                    continue;
                paramsTriple.third.add(CustomUtility.Triple.create(triple.first, triple.second, span));
            }
        }

        @Override
        public void showHelpDialog(AppCompatActivity context) {
            CustomDialog.Builder(context)
                    .setTitle("Hilfe")
                    .setView(R.layout.help_custom_code_video)
                    .setSetViewContent((customDialog, view, reload) -> {
                        CustomUtility.applyToAllViews((ViewGroup) view, TableLayout.class, tableLayout -> {
                            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                                TableRow row = (TableRow) tableLayout.getChildAt(i);
                                TextView child = (TextView) row.getChildAt(1);
                                child.setText(KnowledgeActivity.applyFormatting_text(child.getText()));
                            }
                        });

                        LinearLayout objectsContainer = view.findViewById(R.id.help_customCode_video_objectsContainer);

                        String videoObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"translationList\": [String],\n" +
                                "  \"darstellerList\": [uuid],\n" +
                                "  \"studioList\": [uuid],\n" +
                                "  \"genreList\": [uuid],\n" +
                                "  \"dateList\": [Date],\n" +
                                "  \"release\": Date,\n" +
                                "  \"url\": String,\n" +
                                "  \"ageRating\": int,\n" +
                                "  \"rating\": double,\n" +
                                "  \"imagePath\": String,\n" +
                                "  \"imdbId\": String,\n" +
                                "  \"tmdbId\": int,\n" +
                                "  \"length\": int,\n" +
                                "  \"watchLater\": boolean\n" +
                                "}";

                        String actorObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"tmdbId\": int,\n" +
                                "  \"imagePath\": String\n" +
                                "}";

                        String studioObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"tmdbId\": int,\n" +
                                "  \"imagePath\": String\n" +
                                "}";

                        String genreObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"tmdbGenreId\": int\n" +
                                "}";

                        String collectionObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"filmIdList\": [uuid],\n" +
                                "  \"imagePath\": String,\n" +
                                "  \"listId\": String\n" +
                                "}";

                        String watchListObject = "{\n" +
                                "  \"name\": String,\n" +
                                "  \"uuid\": String,\n" +
                                "  \"videoIdList\": [uuid],\n" +
                                "  \"watchedVideoIdList\": [uuid],\n" +
                                "  \"lastModified\": Date\n" +
                                "}";

                        CustomList<Pair<String, String>> pairList = new CustomList<>();
                        pairList.add(Pair.create("Video", videoObject), Pair.create("Darsteller", actorObject), Pair.create("Studio", studioObject), Pair.create("Genre", genreObject), Pair.create("Sammlung", collectionObject), Pair.create("WatchList", watchListObject));

                        pairList.forEach(pair -> {
                            TextView title = new TextView(context);
                            title.setText(pair.first);
                            title.setTypeface(Typeface.DEFAULT_BOLD);

                            TextView content = new TextView(context);
                            content.setText(pair.second);
                            content.setTextSize(12);

                            if (objectsContainer.getChildCount() > 0) {
                                View divider = new View(context);
                                divider.setBackgroundColor(context.getColor(R.color.colorDivider));
                                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CustomUtility.dpToPx(1));
                                layoutParams.setMargins(0, CustomUtility.dpToPx(5), 0, CustomUtility.dpToPx(5));
                                divider.setLayoutParams(layoutParams);
                                objectsContainer.addView(divider);
                            }
                            objectsContainer.addView(title);
                            objectsContainer.addView(content);
                        });
                    })
                    .setDimensionsFullscreen()
                    .removeMargin()
                    .enableTitleBackButton()
                    .show();
        }
        /**  <------------------------- Function -------------------------  */


        /** ------------------------- Getter & Setter -------------------------> */
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


    /** ------------------------- Getter & Setter -------------------------> */
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

    public RETURN_TYPE getReturnType() {
        return returnType;
    }

    public CustomCode setReturnType(RETURN_TYPE returnType) {
        this.returnType = returnType;
        return this;
    }
    /**  <------------------------- Getter & Setter -------------------------  */


    /** ------------------------- Function -------------------------> */
    public abstract void applyHelpers(JSContext js);

    public abstract JSValue executeCode(Context context, String... params);

    public abstract CharSequence applyFormatting(AppCompatActivity context, CharSequence text);

    public abstract void showHelpDialog(AppCompatActivity context);
    /**  <------------------------- Function -------------------------  */


    /** ------------------------- Dialog -------------------------> */
    public static void showAllDialoge(AppCompatActivity context, String name, Map<String, CustomCode> map, Utility.GenericReturnOnlyInterface<CustomCode> newProvider) {
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
                        .setOnLongClickListener((customRecycler, view, expandable, index) -> showEditDialog(context, map, customDialog, expandable.getObject(), newProvider))
                        .addOptionalModifications(customDialog::setPayload)
                        .generateRecyclerView())
                .setDimensionsFullscreen()
                .disableScroll()
                .addButton(R.drawable.ic_add, customDialog -> {
                    showEditDialog(context, map, customDialog, null, newProvider);
                }, false)
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                .show();
    }

    private static void showEditDialog(AppCompatActivity context, Map<String, CustomCode> map, @Nullable CustomDialog parentDialog, @Nullable CustomCode oldCustomCode, Utility.GenericReturnOnlyInterface<CustomCode> newProvider) {
        boolean isAdd = oldCustomCode == null;
        CustomCode newCustomCode = isAdd ? newProvider.run() : (CustomCode) oldCustomCode.clone();
        CustomDialog.Builder(context)
                .setTitle("CustomCode " + (isAdd ? "Hinzufügen" : "Bearbeiten"))
                .setView(R.layout.dialog_edit_custom_code)
                .enableTitleRightButton(R.drawable.ic_help, customDialog1 -> newCustomCode.showHelpDialog(context))
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout editLayoutName = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_name_layout);
                    TextInputLayout editLayoutCode = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code_layout);
                    TextInputLayout editLayoutDescription = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_description_layout);
                    TextInputLayout editLayoutParams = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params_layout);
                    Spinner editReturnType = customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_returnType);

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
                        editReturnType.setSelection(Arrays.asList(RETURN_TYPE.values()).indexOf(newCustomCode.getReturnType()));
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
                        Boolean hasFormatting = jsValue.getContext().property("hasFormatting").toBoolean();
                        Boolean hasHighlight = jsValue.getContext().property("hasHighlight").toBoolean();
                        if (hasFormatting) {
                            dialogText = newCustomCode.applyFormatting(context, text);
                        } else
                            dialogText = text;

                        CustomDialog.Builder(context)
                                .setTitle("Ergebnis")
                                .enableTitleBackButton()
                                .setText(dialogText)
                                .addOptionalModifications(customDialog1 -> {
                                    TextView textTextView = customDialog1.getTextTextView();
                                    if (hasFormatting)
                                        textTextView.setMovementMethod(LinkMovementMethod.getInstance());
                                    if (!hasHighlight)
                                        textTextView.setLinkTextColor(textTextView.getTextColors());
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
                    int newReturnType = ((Spinner) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_returnType)).getSelectedItemPosition();

                    newCustomCode
                            .setDescription(CustomUtility.stringExistsOrElse(newDescription, null))
                            .setCode(newCode)
                            .setReturnType(RETURN_TYPE.values()[newReturnType])
                            .setDefaultParams(CustomUtility.stringExistsOrElse(newParams, null))
                            .setName(newName);

                    if (isAdd)
                        map.put(newCustomCode.getUuid(), newCustomCode);
                    else
                        oldCustomCode.getChangesFrom(newCustomCode);

                    if (parentDialog != null)
                        ((CustomRecycler) parentDialog.getPayload()).reload();
                    Database.saveAll(context);
                    // ToDo: warum sagt auch gespeichert wenn nix verändert
                })
                .disableLastAddedButton()
                .enableDynamicWrapHeight(context)
                .enableAutoUpdateDynamicWrapHeight()
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String newName = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_name)).getText().toString().trim();
                    String newCode = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    String newParams = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params)).getText().toString().trim();
                    String newDescription = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_description)).getText().toString().trim();
                    if (isAdd)
                        return CustomUtility.stringExists(newName) || CustomUtility.stringExists(newCode) || CustomUtility.stringExists(newParams) || CustomUtility.stringExists(newDescription);
                    else
                        return !newName.equals(oldCustomCode.getName()) || !newCode.equals(oldCustomCode.getCode()) || !newParams.equals(CustomUtility.stringExistsOrElse(oldCustomCode.getDefaultParams(), "")) || !newDescription.equals(CustomUtility.stringExistsOrElse(oldCustomCode.getDescription(), ""));
                })
                .show();
    }

    // ---------------

    public static class RecyclerProviderReturn<T> {
        @LayoutRes
        int layoutId;
        CustomUtility.GenericReturnInterface<List<String>, CustomUtility.GenericReturnInterface<List<JSValue>, List<T>>> listMapper;
        CustomUtility.GenericReturnInterface<List<String>, CustomRecycler.SetItemContent<T>> setItemContent;
        CustomUtility.GenericReturnInterface<String, Object> test;
        CustomRecycler.CustomRecyclerInterface<T> recyclerInterface;

        public RecyclerProviderReturn(int layoutId, CustomUtility.GenericReturnInterface<List<String>, CustomUtility.GenericReturnInterface<List<JSValue>,
                List<T>>> listMapper, CustomUtility.GenericReturnInterface<List<String>, CustomRecycler.SetItemContent<T>> setItemContent,
                                      CustomRecycler.CustomRecyclerInterface<T> recyclerInterface) {
            this.layoutId = layoutId;
            this.listMapper = listMapper;
            this.setItemContent = setItemContent;
            this.recyclerInterface = recyclerInterface;
        }
    }

    public interface RecyclerProvider {
        RecyclerProviderReturn run(CustomCode customCode, List<JSValue> list);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void showDetailDialog(AppCompatActivity context, Map<String, CustomCode> map, @Nullable RecyclerProvider recyclerProvider) {
        CustomDialog.Builder(context)
                .setTitle("CustomCode Details")
                .setView(R.layout.dialog_detail_custom_code)
                .setSetViewContent((customDialog, view, reload) -> {
                    CustomList<CustomCode> customCodeList = map.values().stream().sorted(ParentClass::compareByName).collect(Collectors.toCollection(CustomList::new));

                    TextInputLayout editParameters = view.findViewById(R.id.dialog_detail_customCode_parameter_layout);
                    Spinner selectCustomCode = view.findViewById(R.id.dialog_detail_customCode_select);
                    FrameLayout contentContainer = view.findViewById(R.id.dialog_detail_customCode_content);

                    CustomList<String> selectionList = customCodeList.map(com.finn.androidUtilities.ParentClass::getName);
                    selectionList.add(0, "- Keins -");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, selectionList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    selectCustomCode.setAdapter(adapter);

                    View executeButton = view.findViewById(R.id.dialog_detail_customCode_execute);
                    executeButton.setOnClickListener(v -> {
                        if (selectCustomCode.getSelectedItemPosition() == 0) {
                            Toast.makeText(context, "Einen CustomCode auswählen", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        contentContainer.removeAllViews();
                        CustomCode customCode = customCodeList.get(selectCustomCode.getSelectedItemPosition() - 1);
                        String newParams = editParameters.getEditText().getText().toString().trim();
                        JSValue jsValue = customCode.executeCode(context, parseParams(newParams));
                        if (jsValue == null)
                            return;
                        if (recyclerProvider != null && jsValue.isArray() && customCode.getReturnType() == RETURN_TYPE.LIST) {
                            try {
                                JSBaseArray jsArray = jsValue.toJSArray();
                                RecyclerProviderReturn providerReturn = recyclerProvider.run(customCode, jsArray);
                                List<String> extraStrings = new ArrayList<>();
                                RecyclerView recyclerView =
                                        new CustomRecycler<>(context)
                                                .setItemLayout(providerReturn.layoutId)
                                                .setGetActiveObjectList(customRecycler -> (List) ((CustomUtility.GenericReturnInterface) providerReturn.listMapper.run(extraStrings)).run(jsArray))
                                                .setSetItemContent((CustomRecycler.SetItemContent) providerReturn.setItemContent.run(extraStrings))
                                                .addOptionalModifications(customRecycler -> providerReturn.recyclerInterface.run(customRecycler))
                                                .generateRecyclerView();

                                contentContainer.addView(recyclerView);
                            } catch (Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else if (recyclerProvider != null && jsValue.isArray() && customCode.getReturnType() == RETURN_TYPE.GROUPED_LIST) {

                            try {
                                RecyclerProviderReturn providerReturn = recyclerProvider.run(customCode, jsValue.toJSArray());
                                JSBaseArray jsArray = jsValue.toJSArray();
                                List<String> extraStrings = new ArrayList<>();
                                RecyclerView recyclerView = new CustomRecycler<CustomRecycler.Expandable>(context)
                                        .setGetActiveObjectList(customRecycler -> {
                                            List list = new ArrayList();
                                            for (Object o : jsArray) {
                                                JSBaseArray jsv = ((JSValue) o).toJSArray();
                                                CustomRecycler.Expandable<Object> expandable = new CustomRecycler.Expandable<>(jsv.get(0).toString(), ((CustomUtility.GenericReturnInterface) providerReturn.listMapper.run(extraStrings)).run(((JSValue) jsv.get(1)).toJSArray()));
                                                expandable.setExpended(true);
                                                list.add(expandable);
                                            }
                                            return list;
                                        })
                                        .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper()
                                                .customizeRecycler(subRecycler -> {
                                                    subRecycler
                                                            .setItemLayout(providerReturn.layoutId)
                                                            .setSetItemContent((CustomRecycler.SetItemContent) providerReturn.setItemContent.run(extraStrings))
                                                            .addOptionalModifications(customRecycler1 -> {
                                                                providerReturn.recyclerInterface.run(customRecycler1);
                                                                customRecycler1.disableFastscroll();
                                                            });
                                                }))
                                        .generateRecyclerView();
                                contentContainer.addView(recyclerView);
                            } catch (Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String text = jsValue.isString() ? jsValue.toString() : jsValue.toJSON();
                            if (CustomUtility.stringExists(text)) {
                                CharSequence dialogText;
                                Boolean hasFormatting = jsValue.getContext().property("hasFormatting").toBoolean();
                                Boolean hasHighlight = jsValue.getContext().property("hasHighlight").toBoolean();
                                if (hasFormatting) {
                                    dialogText = customCode.applyFormatting(context, text);
                                } else
                                    dialogText = text;

                                TextView textView = new TextView(context);
                                ScrollView scrollView = new ScrollView(context);
                                scrollView.addView(textView);
                                textView.setText(dialogText);
                                CustomUtility.setPadding(textView, 16);
                                if (hasFormatting)
                                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                                if (!hasHighlight)
                                    textView.setLinkTextColor(textView.getTextColors());

                                contentContainer.addView(scrollView);
                            } else
                                Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
                        }
                    });
                    executeButton.setOnLongClickListener(v -> {
                        if (selectCustomCode.getSelectedItemPosition() == 0) {
                            Toast.makeText(context, "Einen CustomCode auswählen", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        CustomCode customCode = customCodeList.get(selectCustomCode.getSelectedItemPosition() - 1);
                        showEditDialog(context, map, null, customCode, null);
                        return true;
                    });
                })
                .disableScroll()
                .setDimensionsFullscreen()
                .removeMargin()
                .enableTitleBackButton()
                .show();
    }
    /**  <------------------------- Dialog -------------------------  */


    /** ------------------------- Convenience -------------------------> */
    public static String[] parseParams(String params) {
        return Arrays.stream(params.split(",")).map(String::trim).filter(CustomUtility::stringExists).toArray(String[]::new);
    }

    public static CustomCode getCustomCodeByName(CategoriesActivity.CATEGORIES category, String name) {
        return (CustomCode) Utility.findObjectByName(category, name);
    }

    public boolean returnsList() {
        return returnType == RETURN_TYPE.LIST;
    }

    public static String applyCodeShortcuts(String code) {
        // ToDo: kann gefixt werden?
        if (true)
            return code;
        // \jn \m() \f() \hf
        Pattern pattern = Pattern.compile("(?<!\\\\)\\\\(\\w+)(?:\\(([^\\)]+)\\))?");
        while (true) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                String full = matcher.group(0);
                String type = matcher.group(1);
                String content = matcher.group(2);
                String replacement;
                switch (type) {
                    case "jn":
                        replacement = "join(\"\\n\")";
                        break;
                    case "hf":
                        replacement = "hasFormatting = true;";
                        break;
                    default:
                        replacement = full;
                        break;
                }
                code = matcher.replaceFirst(replacement);
            } else
                break;
        }
        return code;
    }
    /**  <------------------------- Convenience -------------------------  */


    /** ------------------------- Encryption -------------------------> */
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(code)) code = AESCrypt.encrypt(key, code);
            if (Utility.stringExists(defaultParams))
                defaultParams = AESCrypt.encrypt(key, defaultParams);
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
            if (Utility.stringExists(defaultParams))
                defaultParams = AESCrypt.decrypt(key, defaultParams);
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
