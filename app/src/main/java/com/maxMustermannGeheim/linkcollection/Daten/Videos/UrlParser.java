package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import bsh.EvalError;
import bsh.Interpreter;

public class UrlParser extends ParentClass {

    private String thumbnailCode;
    private String code;
    private String exampleUrl;
    private TYPE type = TYPE.JAVA;
    public static Map<String,WebView> webViewMap = new HashMap<>();
    private int openJs;
    private boolean debug = false;
    private CustomDialog webViewDialog;


    public enum TYPE {
        JAVA("Java", 0), JAVA_SCRIPT("JavaScript", 1);

        private String name;
        private int index;

        TYPE() {
        }

        TYPE(String name, int index) {
            this.name = name;
            this.index = index;
        }

        //  ------------------------- Getter & Setter ------------------------->
        public String getName() {
            return name;
        }

        public TYPE setName(String name) {
            this.name = name;
            return this;
        }

        public int getIndex() {
            return index;
        }

        public TYPE setIndex(int index) {
            this.index = index;
            return this;
        }
        //  <------------------------- Getter & Setter -------------------------


        //  ------------------------- Convenience ------------------------->
        public static TYPE getTypeByIndex(int index){
            return Utility.SwitchExpression.setInput(index)
                    .addCase(JAVA.getIndex(), JAVA)
                    .setLastCaseAsDefault()
                    .addCase(JAVA_SCRIPT.getIndex(), JAVA_SCRIPT)
                    .evaluate();
        }
        //  <------------------------- Convenience -------------------------
    }


    public UrlParser() {
    }

    public UrlParser(String name) {
        uuid = "urlParser_" + UUID.randomUUID().toString();
        this.name = name;
    }

    //  ------------------------- Getter & Setter ------------------------->
    public String getCode() {
        return code;
    }

    public UrlParser setCode(String code) {
        this.code = code;
        return this;
    }

    public String getExampleUrl() {
        return exampleUrl;
    }

    public UrlParser setExampleUrl(String exampleUrl) {
        this.exampleUrl = exampleUrl;
        return this;
    }

    public TYPE getType() {
        return type;
    }

    public UrlParser setType(TYPE type) {
        this.type = type;
        return this;
    }

    public String getThumbnailCode() {
        return thumbnailCode;
    }

    public UrlParser setThumbnailCode(String thumbnailCode) {
        this.thumbnailCode = thumbnailCode;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------


    //  ------------------------- Convenience ------------------------->
    public String parseUrl(@Nullable Context context, String url, @NonNull Utility.GenericInterface<String> onParseNameResult, @Nullable Utility.GenericInterface<String> onParseThumbnailResult) {
        if (!url.contains(name)) {
            if (context != null) Toast.makeText(context, "Keine passende URL", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (type == TYPE.JAVA) {
            Interpreter interpreter = new Interpreter();
            try {
                interpreter.set("url", url);
                CustomList<String> splitList = new CustomList<>(url.split("/"));
                interpreter.set("split", splitList);
                CustomList<String> lastSplitList = new CustomList<>();
                String last = null;
                if (!splitList.isEmpty()) {
                    last = splitList.getLast();
                    lastSplitList.add(last.split("-"));
                }
                if (last.contains("?")){
                    lastSplitList.clear();
                    lastSplitList.add(last.split("\\?")[0].split("-"));
                }
                interpreter.set("last", last);
                interpreter.set("result", null);
                interpreter.set("lastSplit", lastSplitList);
                interpreter.set("customList", new CustomList<String>());
                Object resultO = interpreter.eval(code);
                if (resultO instanceof  CustomList)
                    resultO = String.join(" ", (CustomList<String>) resultO);
                if (resultO == null) {
                    if (context != null)
                        Toast.makeText(context, "Kein Ergebnis", Toast.LENGTH_SHORT).show();
                    onParseNameResult.runGenericInterface("");
                    return "";
                }
                String result = resultO.toString();
                onParseNameResult.runGenericInterface(result);
                return result;

            } catch (EvalError evalError) {
                if (context != null)
                    Toast.makeText(context, evalError.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }
        }
        if (type == TYPE.JAVA_SCRIPT || (Utility.stringExists(thumbnailCode) && onParseThumbnailResult != null)){
            if (context == null)
                return null;
            if (!webViewMap.containsKey(name)) {
                WebView webView = new WebView(context);
                webView.setAlpha(0f);
                webViewMap.put(name, webView);
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setUserAgentString(Helpers.WebViewHelper.USER_AGENT);
                settings.setUseWideViewPort(true);
                settings.setLoadWithOverviewMode(true);

                settings.setSupportZoom(true);
                settings.setBuiltInZoomControls(true);
                settings.setDisplayZoomControls(false);

                webView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (type == TYPE.JAVA_SCRIPT) {
                            openJs++;
                            evaluateJavaScript(true, onParseNameResult, 0);
                        }
                        if (Utility.stringExists(thumbnailCode) && onParseThumbnailResult != null) {
                            openJs++;
                            evaluateJavaScript(false, onParseThumbnailResult, 0);
                        }
                        super.onPageFinished(view, url);
                    }
                });
                webView.loadUrl(url);
            } else {
                if (type == TYPE.JAVA_SCRIPT) {
                    openJs++;
                    evaluateJavaScript(true, onParseNameResult, 0);
                }
                if (Utility.stringExists(thumbnailCode) && onParseThumbnailResult != null) {
                    openJs++;
                    evaluateJavaScript(false, onParseThumbnailResult, 0);
                }
            }

            if (debug || (Utility.stringExists(thumbnailCode) && onParseThumbnailResult != null)) {
                webViewDialog = CustomDialog.Builder(context)
                        .setView(webViewMap.get(name))
                        .setDimensions(false, false)
                        .setOnDialogDismiss(customDialog -> ((ViewGroup) customDialog.findViewById(R.id.dialog_custom_layout_view_interface)).removeAllViews())
                        .setSetViewContent((customDialog, view, reload) -> {

                        })
                        .disableScroll()
                        .enableDoubleClickOutsideToDismiss(customDialog -> true, "Thumbnail wird Geladen")
                        .removeBackground()
                        .show();
            }

            Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_LONG).show();
            return "JavaScript";
        } else
            return null;

    }

    private void evaluateJavaScript(boolean isName, Utility.GenericInterface<String> onParseResult, int tryCount) {
        if (!webViewMap.containsKey(name))
            return;

        String script = isName ? code : thumbnailCode;

        if (script.startsWith("{") && script.endsWith("}")) {
            script = "(function() " + script + ")();";

        } else {
            if (!script.endsWith(";"))
                script += ";";
        }

        webViewMap.get(name).evaluateJavascript(script, t -> {
            if (t.startsWith("\"") && t.endsWith("\""))
                t = Utility.subString(t, 1, -1);

            if (t.matches("null|") && tryCount < 50) {
                Handler handler = new Handler();
                handler.postDelayed(() -> evaluateJavaScript(isName, onParseResult, tryCount + 1), 100);
            } else {
                onParseResult.runGenericInterface(t + (tryCount < 50 ? "" : " (" + tryCount + ")"));
                if (openJs <= 1 && !debug) {
                    openJs--;
                    webViewMap.get(name).destroy();
                    webViewMap.remove(name);
                    if (webViewDialog != null) {
                        webViewDialog.dismiss();
                        webViewDialog = null;
                    }
                } else
                    openJs--;
            }
        });
    }

    public static UrlParser getMatchingParser(String url) {
        Database instance = Database.getInstance();
        if (!Database.isReady() || !Utility.isUrl(url)) {
            return null;
        }
        else
            return instance.urlParserMap.values().stream().filter(urlParser -> url.contains(urlParser.getName())).findFirst().orElse(null);
    }
    //  <------------------------- Convenience -------------------------


    @Override
    public UrlParser clone() {
        return (UrlParser) super.clone();
    }


    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(exampleUrl)) exampleUrl = AESCrypt.encrypt(key, exampleUrl);
            if (Utility.stringExists(code)) code = AESCrypt.encrypt(key, code);
            if (Utility.stringExists(thumbnailCode)) thumbnailCode = AESCrypt.encrypt(key, thumbnailCode);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(exampleUrl)) exampleUrl = AESCrypt.decrypt(key, exampleUrl);
            if (Utility.stringExists(code)) code= AESCrypt.decrypt(key, code);
            if (Utility.stringExists(thumbnailCode)) thumbnailCode= AESCrypt.decrypt(key, thumbnailCode);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------

}
