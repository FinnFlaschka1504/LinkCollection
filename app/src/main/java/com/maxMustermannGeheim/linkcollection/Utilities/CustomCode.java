package com.maxMustermannGeheim.linkcollection.Utilities;

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
import android.widget.HorizontalScrollView;
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
import com.google.gson.GsonBuilder;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.scottyab.aescrypt.AESCrypt;

import org.liquidplayer.javascript.JSBaseArray;
import org.liquidplayer.javascript.JSContext;
import org.liquidplayer.javascript.JSException;
import org.liquidplayer.javascript.JSFunction;
import org.liquidplayer.javascript.JSValue;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
    protected RETURN_TYPE returnType = RETURN_TYPE.TEXT;
    protected transient CustomList<String> logList = new CustomList<>();

    enum RETURN_TYPE {
        TEXT, LIST, GROUPED_LIST, CODE, STATISTIC;
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
        public void applyHelpers(Context cont, JSContext js) {
            js.evaluateScript("var isEmpty = obj => obj && Object.keys(obj).length === 0 && Object.getPrototypeOf(obj) === Object.prototype;");

            JSFunction logTiming = new JSFunction(js, "logTiming") {
                public void logTiming() {
//                    CustomUtility.logD(null, "logTiming: vvv");
                    CustomUtility.logTiming("CustomCode", true);
                }
            };
            js.property("logTiming", logTiming);

            JSFunction log = new JSFunction(js, "log") {
                public void log(Object msg) {
                    String msgSting = msg != null ? ((JSValue) msg).toJSON() : "<NoParameter>";
                    logList.add(msgSting);
                    CustomUtility.logD(null, "log: %s", msgSting);
                }
            };
            js.property("log", log);

            JSFunction include = new JSFunction(js, "include") {
                public void include(String script) {
                    Optional<CustomCode_Video> customCode = Database.getInstance().customCodeVideoMap.values().stream().filter(cc -> cc.returnType == RETURN_TYPE.CODE && cc.name.equals(script)).findFirst();
                    if (customCode.isPresent())
                        js.evaluateScript(customCode.get().code);
                    else
                        Toast.makeText(cont, "'" + script + "'Script nicht Vorhanden", Toast.LENGTH_SHORT).show();
                }
            };
            js.property("include", include);

            js.evaluateScript("var allObj = undefined;" +
                    "var getAllObj = () => {\n" +
                    "    if (allObj)" +
                    "         return allObj;" +
                    "    return allObj = JSON.parse(fullMapJson);\n" +
                    "}\n");

            js.evaluateScript("var allList = undefined;" +
                    "var getAllList = () => {\n" +
                    "    if (allList)" +
                    "         return allList;" +
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
                    "    if (key != undefined && typeof key == \"string\" && key.match(UUID_REGEX)) {\n" +
                    "        return `\\\\idM${text == '' ? '' : ':' + text}{${key}}`\n" +
                    "    } else if (typeof key == 'object' && 'uuid' in key) {\n" +
                    "        return `\\\\idM${text == '' ? '' : ':' + text}{${key.uuid}}`\n" +
                    "    }\n" +
                    "    return `\\\\${key}${params == '' ? '' : ':' + params}{${text}}`\n" +
                    "}");

            js.evaluateScript("var getAllDays = (sorted) => {\n" +
                    "    let allDays = {};\n" +
                    "    getAllList().forEach(video => {\n" +
                    "        video.dateList.forEach(date => {\n" +
                    "            let newDate = new Date(date).setHours(0, 0, 0, 0);\n" +
                    "            let newItem;\n" +
                    "            if (sorted != undefined)\n" +
                    "                newItem = [video.uuid, new Date(date).getTime()];\n" +
                    "            else\n" +
                    "                newItem = video.uuid;\n" +
                    "\n" +
                    "            if (newDate in allDays) \n" +
                    "                allDays[newDate].push(newItem);\n" +
                    "            else \n" +
                    "                allDays[newDate] = [newItem];\n" +
                    "        });\n" +
                    "    });\n" +
                    "    if (sorted != undefined && sorted != null)\n" +
                    "        Object.values(allDays).forEach(a => a.sort((o1, o2) => (o1[1] - o2[1]) * (sorted ? -1 : 1)))\n" +
                    "    return allDays;\n" +
                    "}");

            JSFunction getById = new JSFunction(js, "getById") {
                public Map getById(String id) {
                    return map(getObjectById(id));
                }
            };
            js.property("getById", getById);

            js.evaluateScript("var UUID_REGEX = /\\w+_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b/;");

            JSFunction getByName = new JSFunction(js, "getByName") {
                public Map getByName(String category, String name) {
                    CategoriesActivity.CATEGORIES cat = CategoriesActivity.CATEGORIES.valueOf(category);
                    return map(getObjectByName(cat, name));
                }
            };
            js.property("getByName", getByName);
            js.evaluateScript("var VIDEO = 'VIDEO';\n" +
                    "var DARSTELLER = 'DARSTELLER';\n" +
                    "var STUDIOS = 'STUDIOS';\n" +
                    "var GENRE = 'GENRE';\n" +
                    "var COLLECTION = 'COLLECTION';\n" +
                    "var WATCH_LIST = 'WATCH_LIST';");

            js.evaluateScript("var getAllViews = (sorted = undefined) => {\n" +
                    "    let res = [];\n" +
                    "    let all = getAllList();\n" +
                    "    all.forEach(v => v.dateList.forEach(d => res.push([new Date(d).getTime(), v.uuid])))\n" +
                    "    if (sorted == undefined)\n" +
                    "        return res;\n" +
                    "    else\n" +
                    "        return res.sort((a1, a2) => (a2[0] - a1[0]) * (sorted ? 1 : -1))\n" +
                    "}");

            js.evaluateScript("var toGroup = (list, getKey, keyMapper, valueMapper, expanded) => {\n" +
                    "    let transformRes = o => {\n" +
                    "        if (expanded === null)\n" +
                    "            return Object.entries(o);\n" +
                    "        else if (typeof expanded == \"boolean\")\n" +
                    "            return Object.entries(o).map(a => [...a, expanded])\n" +
                    "        else if (typeof expanded == \"function\")\n" +
                    "            return Object.entries(o).map(a => [...a, expanded(a)])\n" +
                    "        else\n" +
                    "            return o;\n" +
                    "    }\n" +
                    "\n" +
                    "    let res = {};\n" +
                    "    \n" +
                    "    if (typeof getKey == \"string\") {\n" +
                    "        let prev = getKey;\n" +
                    "        getKey = o => o[prev];\n" +
                    "    }\n" +
                    "\n" +
                    "    if (typeof valueMapper == \"string\") {\n" +
                    "        let prev = valueMapper;\n" +
                    "        valueMapper = o => o[prev];\n" +
                    "    }\n" +
                    "        \n" +
                    "    list.forEach(o => {\n" +
                    "        let key = getKey(o);\n" +
                    "        if (valueMapper != undefined)\n" +
                    "            o = valueMapper(o);\n" +
                    "        if (key in res)\n" +
                    "            res[key].push(o);\n" +
                    "        else\n" +
                    "            res[key] = [o];\n" +
                    "    })\n" +
                    "    if (typeof keyMapper == \"function\") {\n" +
                    "        let newRet = {}\n" +
                    "        Object.entries(res).forEach(e => newRet[keyMapper(e[0], e[1])] = e[1])\n" +
                    "        return transformRes(newRet);\n" +
                    "    } else\n" +
                    "        return transformRes(res);\n" +
                    "}");

            js.evaluateScript("var DF_DATE = \"dd.MM.yyyy\";\n" +
                    "var DF_DATE_LONG = \"dd MMMM yyyy\";\n" +
                    "var DF_MONTH = \"MMMM yyyy\";\n" +
                    "var DF_DATE_TIME = \"dd.MM.yyyy HH:mm 'Uhr'\";\n" +
                    "var DF_DATE_TIME_FULL = \"dd.MM.yyyy HH:mm:ss 'Uhr'\";\n" +
                    "var DF_TIME = \"hh:mm 'Uhr'\";\n" +
                    "var DF_TIME_FULL = \"hh:mm:ss 'Uhr'\";");
            SimpleDateFormat parseDate = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.ENGLISH);
            JSFunction df = new JSFunction(js, "df") {
                public Object df(JSValue value, String format) {
                    try {
                        if (value.isDate()) {
                            if (format == null) {
                                Toast.makeText(cont, "Kein Format mitgegeben", Toast.LENGTH_SHORT).show();
                                return null;
                            }
                            return new SimpleDateFormat(format, Locale.getDefault()).format(parseDate.parse(value.toString().substring(4,24)));
                        } else if (value.isNumber()) {
                            if (format == null) {
                                Toast.makeText(cont, "Kein Format mitgegeben", Toast.LENGTH_SHORT).show();
                                return null;
                            }
                            new SimpleDateFormat(format, Locale.getDefault()).format(new Date(value.toNumber().longValue()));
                        } else if (value.isString()) {
                            String date = value.toString();
                            if (date.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{1,4}")) {
                                return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date);
                            } else {
                                if (format == null) {
                                    Toast.makeText(cont, "Kein Format mitgegeben", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                return new SimpleDateFormat(format, Locale.getDefault()).parse(date);
                            }
                        }
                    } catch (Exception ignored) {}
                    return null;
                }
            };
            js.property("df", df);

            JSFunction getConstants = new JSFunction(js, "getConstants") {
                public String getConstants() {
                    return Arrays.stream(js.propertyNames()).filter(s -> s.matches("[A-Z\\d_]+")).map(s -> s + ": " + js.property(s)).collect(Collectors.joining("\n"));
                }
            };
            js.property("getConstants", getConstants);

            js.evaluateScript("var getIntervall = (period, start, end, rolling = false) => {\n" +
                    "    if (typeof start == \"number\")\n" +
                    "        start = new Date(start);\n" +
                    "    start = new Date(start.getFullYear(), start.getMonth(), start.getDate())\n" +
                    "    \n" +
                    "    let amount = false;\n" +
                    "    if (typeof end == \"number\") {\n" +
                    "        if (end >= 10000000000) \n" +
                    "            end = new Date(end);\n" +
                    "         else\n" +
                    "            amount = true;\n" +
                    "    }\n" +
                    "        \n" +
                    "    let current = start;\n" +
                    "    let type = period.slice(-1);\n" +
                    "    period = +period.slice(0, -1);\n" +
                    "    if (!period || period == 0)\n" +
                    "        return []\n" +
                    "    let result = [];\n" +
                    "    Loop:\n" +
                    "    while ((amount && end > 0) || (!amount && (period > 0 ? end <= current : end >= current))) {\n" +
                    "        let next;\n" +
                    "        switch (type) {\n" +
                    "            case \"d\":\n" +
                    "                next = new Date(current.getFullYear(), current.getMonth(), current.getDate() - period);\n" +
                    "                break;\n" +
                    "            case \"w\":\n" +
                    "                next = new Date(current.getFullYear(), current.getMonth(), current.getDate() - (period * 7));\n" +
                    "                break;\n" +
                    "            case \"m\":\n" +
                    "                next = new Date(current.getFullYear(), current.getMonth() - period, current.getDate());\n" +
                    "                break;\n" +
                    "            case \"y\":\n" +
                    "                next = new Date(current.getFullYear() - period, current.getMonth(), current.getDate());\n" +
                    "                break;\n" +
                    "            default:\n" +
                    "                break Loop;\n" +
                    "        }\n" +
                    "        result.push([current.getTime(), next.getTime()]);\n" +
                    "        current = next;\n" +
                    "        if (amount)\n" +
                    "            end--;\n" +
                    "\n" +
                    "        if(result.length > 100000)\n" +
                    "            return result\n" +
                    "    }\n" +
                    "    return result;\n" +
                    "}");

            js.evaluateScript("var toIntervall = (list, period, start, end, filter = true) => {\n" +
                    "    if (!start)\n" +
                    "        start = new Date();\n" +
                    "    let decr = +period.slice(0, -1) > 0\n" +
                    "    if (!end && end != 0) {\n" +
                    "        let arr= list.sort((a1, a2) => a2[0] - a1[0])\n" +
                    "        end = arr[decr ? arr.length - 1 : 0][0]\n" +
                    "    } else if (end == 0 || end == -1)\n" +
                    "        end = list[end == -1 ? list.length - 1 : 0][0]\n" +
                    "    let interList = getIntervall(period, start, end).map(a => [new Date(a[0]), new Date(a[1])]).map(a => [a, []]);\n" +
                    "\n" +
                    "    list.forEach(view => {\n" +
                    "        interList.forEach(inter => {\n" +
                    "            if((decr && (view[0] < inter[0][0] && view[0] >= inter[0][1])) || (!decr && (view[0] >= inter[0][0] && view[0] < inter[0][1])))\n" +
                    "                inter[1].push(view);\n" +
                    "        })\n" +
                    "    })\n" +
                    "    if (!filter)\n" +
                    "        return interList\n" +
                    "    else\n" +
                    "        return interList.filter(a => a[1].length)\n" +
                    "}");

            js.evaluateScript("var removeTime = (d) => {\n" +
                    "    let time;\n" +
                    "    if (typeof d == \"object\")    \n" +
                    "        return new Date(d.getFullYear(), d.getMonth(), d.getDate());\n" +
                    "    else {\n" +
                    "        return removeTime(new Date(d)).getTime()\n" +
                    "    }\n" +
                    "        \n" +
                    "}");

            js.evaluateScript("var getByCat = (category, single, mapper) => {\n" +
                    "    let allVid = getAllList();\n" +
                    "\n" +
                    "    if(typeof mapper == \"string\") {\n" +
                    "        let oldMapper = mapper\n" +
                    "        mapper = v => v[oldMapper];\n" +
                    "    }\n" +
                    "\n" +
                    "    if (single && !single.match(UUID_REGEX))\n" +
                    "        single = getByName(category, single).uuid\n" +
                    "\n" +
                    "    let toList = (o) => {\n" +
                    "        switch (category) {\n" +
                    "            case DARSTELLER: \n" +
                    "                return o[\"darstellerList\"];\n" +
                    "            case STUDIOS: \n" +
                    "                return o[\"studioList\"];\n" +
                    "            case GENRE: \n" +
                    "                return o[\"genreList\"];\n" +
                    "            case WATCH_LIST: \n" +
                    "                return o[\"videoIdList\"];\n" +
                    "            case COLLECTION: \n" +
                    "                return o[\"filmIdList\"];\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    if (category == WATCH_LIST || category == COLLECTION) {\n" +
                    "        if (single) {\n" +
                    "            let vids = toList(getById(single)).map(id => allObj[id])\n" +
                    "            if (mapper)\n" +
                    "                return vids.map(v => mapper(v));\n" +
                    "            return vids;\n" +
                    "        } else {\n" +
                    "            let res = {};\n" +
                    "            Object.values(getAllCat(category)).forEach(c => res [c.uuid] = toList(c).map(id => mapper ? mapper(allObj[id]) : allObj[id]));\n" +
                    "            return res;\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    if (single) {\n" +
                    "        let res = allVid.filter(v => toList(v).includes(single))\n" +
                    "        if (mapper)\n" +
                    "            return res.map(v => mapper(v));\n" +
                    "        return res;\n" +
                    "    } else {\n" +
                    "        let res = {};\n" +
                    "        allVid.forEach(vid => {\n" +
                    "            let v = mapper ? mapper(vid) : vid;\n" +
                    "            toList(vid).forEach(id => {\n" +
                    "                if (id in res)\n" +
                    "                    res[id].push(v);\n" +
                    "                else\n" +
                    "                    res[id] = [v];\n" +
                    "            })\n" +
                    "        })\n" +
                    "        return res;\n" +
                    "    }\n" +
                    "}");

        }

        @Override
        public JSValue executeCode(Context context, String... params) {
            Database database = Database.getInstance();
            JSContext js = new JSContext();
            js.property("params", new CustomList<>(params));
            js.property("sortType", "undefined");
            js.property("hasFormatting", true);
            js.property("hasHighlight", false);
            js.property("prettyPrint", true);
            js.property("showLog", true);
            String json = new Gson().toJson(database.videoMap);
            js.property("fullMapJson", json);

//            CustomUtility.logTiming("CustomCode", true);
            try {
                applyHelpers(context, js);
            } catch (Exception e) {
                String message = e.getMessage();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                CustomUtility.logD(null, "executeCode: %s", message);
            }
//            CustomUtility.logTiming("CustomCode", true);
            if (CustomUtility.stringExists(code)) {
                String fullCode = "let code = () => {" +
                        applyCodeShortcuts(code) +
                        "\n}\n" +
                        "code()";
                try {
                    JSValue result;
                    tempResult = (result = js.evaluateScript(fullCode)).toJSON();
                    checkLogList(context, js);
                    JSValue sortType = js.property("sortType");
                    if (!sortType.equals("undefined"))
                        _setSortType(sortType.toString());
                    else
                        _setSortType(null);
                    return result;
                } catch (Exception e) {
                    String stackTrace = ((JSException) e).getError().stack();
                    String log = "";
                    if (!logList.isEmpty() && js.hasProperty("showLog") && js.property("showLog").toBoolean())
                        log = logList.join("\n\n") + "\n\n";
                    logList.clear();
                    return new JSValue(js, log + stackTrace);
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
                                else if (parentClass instanceof WatchList)
                                    search = String.format("{[w:%s]}", name);

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
        public void showHelpDialog(AppCompatActivity context, @Nullable CustomDialog editDialog) {
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

                        if (editDialog != null) {
                            EditText editCode = editDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code);
                            if (editCode.isFocused()) {
                                CustomUtility.applyToAllViews(view.findViewById(R.id.help_customCode_video_functions_layout), LinearLayout.class, linearLayout -> {
                                    if (!linearLayout.getClass().equals(LinearLayout.class))
                                        return;
                                    TextView methodNameTextView = (TextView) linearLayout.getChildAt(0);
                                    methodNameTextView.setOnClickListener(v -> {
                                        String text = ((TextView) v).getText().toString();
                                        editCode.getText().insert(editCode.getSelectionStart(), text.replaceAll("(?<=\\()[^)]+(?=\\))", ""));
                                        editCode.setSelection(editCode.getSelectionStart() - 1);
                                        customDialog.dismiss();
                                    });
                                    methodNameTextView.setTextColor(context.getColorStateList(R.color.clickable_text_color_normal));
                                });
                            }
                        }

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
    public abstract void applyHelpers(Context context, JSContext js);

    public abstract JSValue executeCode(Context context, String... params);

    public abstract CharSequence applyFormatting(AppCompatActivity context, CharSequence text);

    public abstract void showHelpDialog(AppCompatActivity context, @Nullable CustomDialog editDialog);

    protected void checkLogList(Context context, JSContext jsContext) {
        if (!logList.isEmpty() && jsContext.hasProperty("showLog") && jsContext.property("showLog").toBoolean()) {
            CustomDialog.Builder(context)
                    .enableTitleBackButton()
                    .setTitle("Log")
                    .setText(logList.join("\n\n"))
                    .show();
        }
        logList.clear();
    }
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
        CustomUtility.KeyboardChangeListener keyboardChangeListener = CustomUtility.KeyboardChangeListener.bindNoAuto(context);
        CustomDialog.Builder(context)
                .setTitle("CustomCode " + (isAdd ? "Hinzufügen" : "Bearbeiten"))
                .setView(R.layout.dialog_edit_custom_code)
                .enableTitleRightButton(R.drawable.ic_help, customDialog1 -> newCustomCode.showHelpDialog(context, customDialog1))
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


                    final boolean[] keyboardOpen = {false};
                    keyboardChangeListener.setOnChange(open -> {
                        keyboardOpen[0] = open;
                        boolean vis = open && editLayoutCode.getEditText().isFocused();
                        view.findViewById(R.id.dialog_edit_CustomCodeVideo_buttons_all_layout).setVisibility(vis ? View.VISIBLE : View.GONE);
                    }).apply();
                    editLayoutCode.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                        boolean vis = keyboardOpen[0] && editLayoutCode.getEditText().isFocused();
                        view.findViewById(R.id.dialog_edit_CustomCodeVideo_buttons_all_layout).setVisibility(vis ? View.VISIBLE : View.GONE);
                    });

                    LinearLayout buttonsNormalLayout = view.findViewById(R.id.dialog_edit_CustomCodeVideo_buttons_normal_layout);
                    LinearLayout buttonsMoreLayout = view.findViewById(R.id.dialog_edit_CustomCodeVideo_buttons_more_layout);
                    Runnable switchLayout = () -> {
                        HorizontalScrollView scrollViewNormal = (HorizontalScrollView) buttonsNormalLayout.getParent();
                        HorizontalScrollView scrollViewMore = (HorizontalScrollView) buttonsMoreLayout.getParent();
                        boolean normalVisible = scrollViewNormal.getVisibility() == View.VISIBLE;
                        scrollViewNormal.setVisibility(normalVisible ? View.GONE : View.VISIBLE);
                        scrollViewMore.setVisibility(normalVisible ? View.VISIBLE : View.GONE);
                    };
                    CustomUtility.GenericInterface<View> applyListener = button -> {
                        Utility.DoubleGenericInterface<CharSequence, CharSequence> applyText = (text, cursorText) -> {
                            EditText editText = editLayoutCode.getEditText();
                            if (editText.isFocused()) {
                                editText.getText().insert(editText.getSelectionStart(), text);
                                if (CustomUtility.stringExists(cursorText) && !text.equals(cursorText)) {
                                    int index = cursorText.toString().indexOf("#");
                                    editText.setSelection(editText.getSelectionStart() - (cursorText.toString().length() - index - 1));
                                }
                            }
                            if (((View) buttonsMoreLayout.getParent()).getVisibility() == View.VISIBLE)
                                switchLayout.run();
                        };


                        button.setOnClickListener(v -> {
                            CharSequence text = ((TextView) v).getText();
                            CharSequence cursorText = ((TextView) v).getHint();
                            applyText.run(text, cursorText);
                        });
                        button.setOnLongClickListener(v -> {
                            CharSequence text = v.getContentDescription();
                            if (!CustomUtility.stringExists(text))
                                return false;
                            applyText.run(text.toString().replaceAll("#", ""), text);
                            return true;
                        });
                    };
                    for (int i = 0; i < buttonsNormalLayout.getChildCount(); i++)
                        applyListener.run(buttonsNormalLayout.getChildAt(i));
                    for (int i = 0; i < buttonsMoreLayout.getChildCount(); i++)
                        applyListener.run(buttonsMoreLayout.getChildAt(i));


                    View moreButton = view.findViewById(R.id.dialog_edit_CustomCodeVideo_buttons_moreButton);
                    moreButton.setOnClickListener(v -> switchLayout.run());
                })
                .addButton("Testen", customDialog -> {
                    String newCode = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_code)).getText().toString().trim();
                    String newParams = ((EditText) customDialog.findViewById(R.id.dialog_edit_CustomCodeVideo_params)).getText().toString().trim();
                    newCustomCode.setCode(newCode);
                    newCustomCode.setDefaultParams(newParams);
                    JSValue jsValue = newCustomCode.executeCode(context, parseParams(newParams));
                    String text = jsValue == null || jsValue.isUndefined() ? null : (jsValue.isString() ? jsValue.toString() : CustomCode.toJson(jsValue, jsValue.getContext().property("prettyPrint").toBoolean()));
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
                                    Utility.applySelectionSearch(context, CategoriesActivity.CATEGORIES.VIDEO, textTextView);
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
                .disableScroll()
                .addOnDialogDismiss(customDialog -> keyboardChangeListener.unregister())
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
                    CustomList<CustomCode> customCodeList = map.values().stream().filter(customCode -> customCode.returnType != RETURN_TYPE.CODE).sorted(ParentClass::compareByName).collect(Collectors.toCollection(CustomList::new));

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
                        if (jsValue.isUndefined()) {
                            Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
                        } else if (recyclerProvider != null && jsValue.isArray() && customCode.getReturnType() == RETURN_TYPE.LIST) {
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
                                RecyclerView recyclerView = new CustomRecycler<CustomRecycler.Expandable>(context)
                                        .setGetActiveObjectList(customRecycler -> {
                                            List list = new ArrayList();
                                            for (Object o : jsArray) {
                                                List<String> extraStrings = new ArrayList<>();
                                                JSBaseArray jsv = ((JSValue) o).toJSArray();
                                                CustomRecycler.Expandable<Object> expandable = new CustomRecycler.Expandable<>(jsv.get(0).toString(),
                                                        ((CustomUtility.GenericReturnInterface) providerReturn.listMapper.run(extraStrings)).run(((JSValue) jsv.get(1)).toJSArray()));
                                                expandable.setExpended(jsv.size() <= 2 || Boolean.parseBoolean(jsv.get(2).toString()));
                                                expandable.setPayload(extraStrings);
                                                list.add(expandable);
                                            }
                                            return list;
                                        })
                                        .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper()
                                                .customizeRecycler((subRecycler, expandable, index) -> {
                                                    subRecycler
                                                            .setItemLayout(providerReturn.layoutId)
                                                            .setSetItemContent((CustomRecycler.SetItemContent) providerReturn.setItemContent.run(expandable.getPayload()))
                                                            .addOptionalModifications(customRecycler1 -> {
                                                                providerReturn.recyclerInterface.run(customRecycler1);
                                                                customRecycler1.disableFastscroll()
                                                                        .setPadding(0);
                                                            });
                                                }))
                                        .setPadding(16)
                                        .generateRecyclerView();
                                contentContainer.addView(recyclerView);
                            } catch (Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String text = jsValue.isString() ? jsValue.toString() : CustomCode.toJson(jsValue, jsValue.getContext().property("prettyPrint").toBoolean());
                            if (CustomUtility.stringExists(text)) {
                                CharSequence dialogText;
                                Boolean hasFormatting = jsValue.getContext().property("hasFormatting").toBoolean();
                                Boolean hasHighlight = jsValue.getContext().property("hasHighlight").toBoolean();
                                if (hasFormatting) {
                                    dialogText = customCode.applyFormatting(context, text);
                                } else
                                    dialogText = text;

                                TextView textView = new TextView(context);
                                Utility.applySelectionSearch(context, CategoriesActivity.CATEGORIES.VIDEO, textView);
                                ScrollView scrollView = new ScrollView(context);
                                scrollView.addView(textView);
                                textView.setText(dialogText);
                                CustomUtility.setPadding(textView, 16);
                                if (hasFormatting)
                                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                                if (!hasHighlight)
                                    textView.setLinkTextColor(textView.getTextColors());

                                contentContainer.addView(scrollView);
                            }

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

    public static String toJson(JSValue jsValue, boolean pretty) {
        String json = jsValue.toJSON();
        if (!pretty)
            return json;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Object o = gson.fromJson(json, Object.class);
        return gson.toJson(o);
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
