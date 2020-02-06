package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import bsh.EvalError;
import bsh.Interpreter;

public class UrlParser extends ParentClass {
    private String code;
    private String exampleUrl;
    private TYPE type = TYPE.JAVA;
    public static Map<String,WebView> webViewMap = new HashMap<>();


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
    //  <------------------------- Getter & Setter -------------------------


    //  ------------------------- Convenience ------------------------->
    public String parseUrl(@Nullable Context context, String url, @Nullable Utility.GenericInterface<String> onParseResult) {
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
                    if (onParseResult != null) onParseResult.runGenericInterface("");
                    return "";
                }
                String result = resultO.toString();
                if (onParseResult != null) onParseResult.runGenericInterface(result);
                return result;

            } catch (EvalError evalError) {
                if (context != null)
                    Toast.makeText(context, evalError.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }
        } else if (type == TYPE.JAVA_SCRIPT){
            if (context == null)
                return null;
            if (!webViewMap.containsKey(name)) {
                WebView webView = new WebView(context);
                webViewMap.put(name, webView);
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                String newUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
                settings.setUserAgentString(newUA);

                final boolean[] loadingFinished = {true};
                final boolean[] redirect = {false};
//                webView.setWebViewClient(new WebViewClient() {
//
//                    @Override
//                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                        if (!loadingFinished[0]) {
//                            redirect[0] = true;
//                        }
//
//                        loadingFinished[0] = false;
//                        webView.loadUrl(request.getUrl().toString());
//                        return true;
//                    }
//
//                    @Override
//                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                        super.onPageStarted(view, url, favicon);
//                        loadingFinished[0] = false;
//                    }
//
//                    @Override
//                    public void onPageFinished(WebView view, String url) {
//                        if (!redirect[0]) {
//                            loadingFinished[0] = true;
//                            evaluateJavaScript(onParseResult, 0);
//                        } else {
//                            redirect[0] = false;
//                        }
//                    }
//                });
                webView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        evaluateJavaScript(onParseResult, 0);
                        super.onPageFinished(view, url);
                    }
                });
                webView.loadUrl(url);
            } else
                evaluateJavaScript(onParseResult, 0);

//            CustomDialog.Builder(context)
//                    .setView(webViewMap.get(name))
//                    .setDimensions(true, true)
//                    .setOnDialogDismiss(customDialog -> ((ViewGroup) customDialog.findViewById(R.id.dialog_custom_layout_view_interface)).removeAllViews())
//                    .show();
            Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_LONG).show();
            return "JavaScript";
        } else
            return null;

    }

    private void evaluateJavaScript(Utility.GenericInterface<String> onParseResult, int tryCount) {
        if (!webViewMap.containsKey(name))
            return;

        webViewMap.get(name).evaluateJavascript(code, t -> {
            String test = code;
            if (t.startsWith("\"") && t.endsWith("\""))
                t = Utility.subString(t, 1, -1);

            if (t.equals("null") && tryCount < 100) {
                evaluateJavaScript(onParseResult, tryCount + 1);
            } else {
                onParseResult.runGenericInterface(t + (tryCount < 100 ? "" : " (" + tryCount + ")"));
                webViewMap.get(name).destroy();
                webViewMap.clear();
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
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------

}
