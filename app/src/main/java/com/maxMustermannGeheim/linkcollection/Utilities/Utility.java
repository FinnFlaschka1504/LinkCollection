package com.maxMustermannGeheim.linkcollection.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.finn.androidUtilities.CustomRecycler;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.MediaActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Show.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.CollectionActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.WatchListActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaTag;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image_I;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.externalCode.ExternalCode;
//import com.pixplicity.sharp.Sharp;

import org.jetbrains.annotations.NotNull;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengraph.OpenGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import top.defaults.drawabletoolbox.DrawableBuilder;


public class Utility {

    // region ------------------------- isOnline ------------------------->
    static public boolean isOnline(Context context) {
        if (isOnline()) {
            return true;
        } else {
            if (isActivityInForeground((AppCompatActivity) context))
                Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    static public boolean isOnline() {
//        if (true)
//            return true;

        if (Build.MODEL.contains("Emulator"))
            return true;
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void isOnline(Runnable onTrue, Runnable onFalse) {

    }

    public static void isOnline(OnResult onResult) {

    }

    public interface OnResult {
        void runOnResult(boolean status);
    }

    private class PingThread extends Thread {
        //        String name;
        Runnable onTrue;
        Runnable onFalse;
        OnResult onResult;
        Context context;

        /** ------------------------- Constructors -------------------------> */
        public PingThread(Runnable onTrue, Runnable onFalse) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
        }

        public PingThread(Runnable onTrue, Runnable onFalse, Context context) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
            this.context = context;
        }

        public PingThread(OnResult onResult) {
            this.onResult = onResult;
        }

        public PingThread(OnResult onResult, Context context) {
            this.onResult = onResult;
            this.context = context;
        }

        /** <------------------------- Constructors ------------------------- */

        @Override
        public void run() {
            if (checkConnection()) {
                if (onTrue != null) onTrue.run();
                if (onResult != null) onResult.runOnResult(true);
            } else {
                if (onFalse != null) onFalse.run();
                if (onResult != null) onResult.runOnResult(false);
                if (context != null)
                    Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean checkConnection() {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int exitValue = ipProcess.waitFor();


                Thread.sleep(2000);

                return false;
//                return (exitValue == 0);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    // test
    // endregion <------------------------- isOnline -------------------------
    // test


    /** ------------------------- Internet Dialog -------------------------> */
    public static CustomDialog showInternetDialog(Context context, String url, @NonNull CustomDialog[] internetDialog, boolean mobileView, boolean disableForwarding, @Nullable DoubleGenericInterface<CustomDialog, String> onImageSelected, @Nullable DoubleGenericInterface<CustomDialog, String> onVideoSelected, @Nullable DoubleGenericInterface<CustomDialog, String> onTextSelected, @Nullable TripleGenericInterface<WebView, Boolean, DoubleGenericInterface<List<String>, Boolean>> optionalParser) {
        if (internetDialog[0] == null) {
            internetDialog[0] = CustomDialog.Builder(context)
                    .setView(R.layout.dialog_video_internet)
                    .removeMargin()
                    .setSetViewContent((customDialog, view, reload) -> {
                        WebView webView = view.findViewById(R.id.dialog_videoInternet_webView);
                        webView.loadUrl(CustomUtility.stringExists(url) ? url : "https://www.google.de/");
//                        webView.setWebViewClient(new WebViewClient() {
//                            @Override
//                            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
////                                return super.shouldOverrideUrlLoading(view, request);
//                                CustomUtility.logD(null, "shouldOverrideUrlLoading: %s", request.getUrl().toString());
//                                return true;
//                            }
//                        });
                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        if (!mobileView)
                            webSettings.setUserAgentString(com.maxMustermannGeheim.linkcollection.Utilities.Helpers.WebViewHelper.USER_AGENT);
                        else
                            webSettings.setUserAgentString(null);
                        webSettings.setUseWideViewPort(true);
                        webSettings.setLoadWithOverviewMode(true);

                        webSettings.setSupportZoom(true);
                        webSettings.setBuiltInZoomControls(true);
                        webSettings.setDisplayZoomControls(false);

                        // ---------------

                        com.finn.androidUtilities.CustomList<String> videoUrls = onVideoSelected != null ? new com.finn.androidUtilities.CustomList<>() : null;
                        Runnable getVideos = () -> {
                            if (videoUrls != null) {
                                if (videoUrls.isEmpty()) {
                                    Toast.makeText(context, "Nichts Gefunden", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String domainName = CustomUtility.getDomainName(webView.getUrl());
                                CustomDialog.Builder(context)
                                        .setTitle("Gefundene Videos")
                                        .setView(customDialog1 -> new CustomRecycler<String>((AppCompatActivity) context)
                                                .setGetActiveObjectList(customRecycler1 -> videoUrls.stream().sorted((o1, o2) -> {
                                                    if (domainName == null)
                                                        return 0;
                                                    if (o1.contains(domainName) && !o2.contains(domainName))
                                                        return -1;
                                                    if (!o1.contains(domainName) && o2.contains(domainName))
                                                        return 1;
                                                    return 0;
                                                }).collect(Collectors.toList()))
                                                .setItemLayout(R.layout.list_item_select_next_episode)
                                                .setSetItemContent((customRecycler1, itemView, videoUrl, index) -> {
                                                    Utility.simpleLoadUrlIntoImageView(context, itemView.findViewById(R.id.listItem_selectNextEpisode_image), videoUrl, videoUrl, 4);

                                                    ((TextView) itemView.findViewById(R.id.listItem_selectNextEpisode_name)).setText(videoUrl);
                                                })
                                                .disableCustomRipple()
                                                .enableDivider(12)
                                                .setOnClickListener((customRecycler1, itemView, videoUrl, index) -> onVideoSelected.run(customDialog, videoUrl))
                                                .generateRecyclerView())
                                        .show();
                            }
                        };


                        FloatingActionButton dialog_videoInternet_getThumbnails = view.findViewById(R.id.dialog_videoInternet_getThumbnails);
                        if (onImageSelected != null || onVideoSelected != null) {
                            dialog_videoInternet_getThumbnails.setVisibility(View.VISIBLE);
                            if (onImageSelected != null) {
                                dialog_videoInternet_getThumbnails.setOnClickListener(v1 -> showSelectThumbnailDialog(context, webView, null, s -> onImageSelected.run(customDialog, s), optionalParser != null ? (webView1, onResult) -> optionalParser.run(webView1, true, onResult) : null));
                                if (onVideoSelected != null)
                                    dialog_videoInternet_getThumbnails.setOnLongClickListener(v -> {
                                        getVideos.run();
                                        return true;
                                    });
                            } else
                                dialog_videoInternet_getThumbnails.setOnClickListener(v -> getVideos.run());

                        } else
                            dialog_videoInternet_getThumbnails.setVisibility(View.GONE);

                        // ---------------

                        FloatingActionButton dialog_videoInternet_getSelection = view.findViewById(R.id.dialog_videoInternet_getSelection);
                        if (onTextSelected != null) {
                            dialog_videoInternet_getSelection.setVisibility(View.VISIBLE);
                            dialog_videoInternet_getSelection.setOnClickListener(v1 -> {
                                Runnable runDefault = () -> {
                                    webView.evaluateJavascript("(function(){return window.getSelection().toString()})()", value -> {
                                        value = Utility.subString(value, 1, -1);
                                        onTextSelected.run(customDialog, value);
                                    });
                                };

                                if (optionalParser != null) {
                                    optionalParser.run(webView, false, (strings, consumed) -> {
                                        onTextSelected.run(customDialog, strings.get(0));
                                        if (!consumed)
                                            runDefault.run();
                                    });
                                } else
                                    runDefault.run();
                            });

                        } else
                            dialog_videoInternet_getSelection.setVisibility(View.GONE);

                        // ---------------

                        TextInputLayout dialog_videoInternet_url_layout = customDialog.findViewById(R.id.dialog_videoInternet_url_layout);
                        ImageView dialog_videoInternet_goButton = customDialog.findViewById(R.id.dialog_videoInternet_goButton);
                        TextInputEditText dialog_videoInternet_url = (TextInputEditText) dialog_videoInternet_url_layout.getEditText();
                        dialog_videoInternet_url.setOnEditorActionListener((v12, actionId, event) -> {
                            if (actionId == EditorInfo.IME_ACTION_GO) {
                                dialog_videoInternet_goButton.callOnClick();
                                CustomUtility.changeWindowKeyboard(context, dialog_videoInternet_url, false);
                                dialog_videoInternet_url.requestFocus();
                                return true;
                            }
                            return false;
                        });
                        dialog_videoInternet_url.setText(webView.getUrl());

                        dialog_videoInternet_goButton.setOnClickListener(v1 -> {
                            String urlOrSearch = dialog_videoInternet_url.getText().toString();
                            if (CustomUtility.stringExists(urlOrSearch)) {
                                if (CustomUtility.isUrl(urlOrSearch)) {
                                    webView.loadUrl(urlOrSearch);
                                } else {
                                    try {
                                        urlOrSearch = URLEncoder.encode(urlOrSearch, "UTF-8");
                                    } catch (UnsupportedEncodingException ignored) {
                                    }
                                    webView.loadUrl("https://www.google.de/search?q=" + urlOrSearch);
                                }
                            }
                        });

                        boolean[] mobileViewCopy = {mobileView};
                        dialog_videoInternet_goButton.setOnLongClickListener(v -> {
                            mobileViewCopy[0] = !mobileViewCopy[0];
//                            if (!mobileViewCopy[0])
//                                webSettings.setUserAgentString(com.maxMustermannGeheim.linkcollection.Utilities.Helpers.WebViewHelper.USER_AGENT);
//                            else
//                                webSettings.setUserAgentString(null);
                            setDesktopMode(webView, !mobileViewCopy[0]);
                            webView.reload();
                            Toast.makeText(context, (mobileViewCopy[0] ? "Mobile" : "Desktop") + " Ansicht", Toast.LENGTH_SHORT).show();
                            return true;
                        });

                        customDialog.findViewById(R.id.dialog_videoInternet_close).setOnClickListener(v1 -> customDialog.dismiss());

                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                dialog_videoInternet_url.setText(url);
                                if (videoUrls != null && view.getProgress() == 100)
                                    videoUrls.clear();
                            }

                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                                return super.shouldOverrideUrlLoading(view, request);
                                CustomUtility.logD(null, "shouldOverrideUrlLoading: %s", request.getUrl().toString());
                                return disableForwarding;
                            }

                            @Nullable
                            @Override
                            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                                String url = request.getUrl().toString();
                                if (onVideoSelected != null && url.contains(".mp4")) {
//                                    CustomUtility.logD(null, "shouldInterceptRequest: %s", url);
                                    videoUrls.add(url);
                                    videoUrls.distinct();
                                }
                                return super.shouldInterceptRequest(view, request);
                            }
                        });

                    })
                    .setOnBackPressedListener(customDialog -> {
                        WebView webView = customDialog.findViewById(R.id.dialog_videoInternet_webView);
                        if (webView != null && webView.canGoBack()) {
                            webView.goBack();
                            return true;
                        }
                        return false;
                    })
                    .disableScroll()
                    .setDimensions(true, true)
                    .show();
        } else {
            internetDialog[0].show();
            WebView webView = internetDialog[0].findViewById(R.id.dialog_videoInternet_webView);

            if (CustomUtility.stringExists(url) && !Objects.equals(webView.getOriginalUrl(), url))
                webView.loadUrl(CustomUtility.stringExists(url) ? url : "https://www.google.de/");
        }
        return internetDialog[0];
    }

    static public void setDesktopMode(WebView webView, boolean enabled) { // ToDo: in WebViewHelper integrieren
        String newUserAgent = webView.getSettings().getUserAgentString();
        if (enabled) {
            try {
                String ua = webView.getSettings().getUserAgentString();
                String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
                newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
                newUserAgent = com.maxMustermannGeheim.linkcollection.Utilities.Helpers.WebViewHelper.USER_AGENT;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.getSettings().setUserAgentString(newUserAgent);
        webView.getSettings().setUseWideViewPort(enabled);
        webView.getSettings().setLoadWithOverviewMode(enabled);
        webView.reload();
    }

    private static void showSelectThumbnailDialog(Context context, @Nullable WebView webView, @Nullable String text, GenericInterface<String> onImageSelected, @Nullable DoubleGenericInterface<WebView, DoubleGenericInterface<List<String>, Boolean>> optionalParser) {
        final int[] count = {3};
        CustomList<String> imageUrlList = new CustomList<>();
        CustomList<String> imageFromUrlParser = new CustomList<>();
        CustomList<String> imageFromOpenGraph = new CustomList<>();
        CustomList<String> imagesFromText = new CustomList<>();

        Runnable connectLists = () -> {
            imageUrlList.addAll(imageFromUrlParser);
            imageUrlList.addAll(imageFromOpenGraph);
            imageUrlList.addAll(imagesFromText);
            imageUrlList.replaceAll(path -> path.startsWith("//") ? "https:" + path : path);
            String sourceUrlRoot = Optional.ofNullable(webView).map(webView1 -> {
                try {
                    return new URI(webView.getUrl()).getHost();
                } catch (URISyntaxException e) {
                    return null;
                }
            }).orElse(null);
            if (sourceUrlRoot != null) {
                imageUrlList.replaceAll(path -> path.startsWith("/") ? "https://" + sourceUrlRoot + path : path);
            }
            showSelectImageDialog(context, imageUrlList, sourceUrlRoot, onImageSelected);
//            showDialog.run();
        };

        Runnable lowerCount = () -> {
            count[0]--;
            if (count[0] <= 0)
                connectLists.run();
        };

        if (webView != null) {
            String url = webView.getUrl();

            //       -------------------- Getter -------------------->
            Runnable getFromUrlParser = () -> {
                Utility.ifNotNull(UrlParser.getMatchingParser(url), urlParser -> {
                    String script = urlParser.getThumbnailCode();
                    if (!Utility.stringExists(script)) {
                        lowerCount.run();
                        return;
                    }
                    if (script.startsWith("{") && script.endsWith("}")) {
                        script = "(function() " + script + ")();";

                    } else {
                        if (!script.endsWith(";"))
                            script += ";";
                    }
                    webView.evaluateJavascript(script, s -> {
                        if (s.startsWith("\"") && s.endsWith("\""))
                            s = Utility.subString(s, 1, -1);

                        if (s.matches(CategoriesActivity.pictureRegex))
                            imageFromUrlParser.add(s);
                        lowerCount.run();

                    });
                }, lowerCount);
            };

            Runnable getFromOpenGraph = () -> {
                Utility.getOpenGraphFromWebsite(url, openGraph -> {
                    String path;
                    if (openGraph != null && (path = openGraph.getContent("image")) != null && path.matches(CategoriesActivity.pictureRegex)) {
                        imageFromOpenGraph.add(path);
                    }

                    lowerCount.run();

                });
            };

            final int[] htmlTries = {3};
            Runnable[] getFromHtml = {null};
            getFromHtml[0] = () -> {
                webView.evaluateJavascript(CustomUtility.SwitchExpression.setInput(htmlTries[0])
                        .addCase(3, "document.getElementsByTagName('html')[0].innerHTML;")
                        .addCase(2, "$('html').html();")
                        .addCase(1, "$('html').innerHTML;")
                        .addCase(0, "$('html').outerHTML;").evaluate(), value -> {
                    if (!value.equals("null") || htmlTries[0] <= 0) {
                        imagesFromText.addAll(Utility.getImageUrlsFromText(value));
                        lowerCount.run();
                    } else {
                        htmlTries[0]--;
                        getFromHtml[0].run();
                    }
                });
            };
            //       <-------------------- Getter --------------------

            Runnable runDefault = () -> {
                getFromUrlParser.run();
                getFromOpenGraph.run();
                getFromHtml[0].run();
            };

            if (optionalParser != null) {
                count[0]++;
                optionalParser.run(webView, (results, consumed) -> {
                    imageFromUrlParser.addAll(results);
                    if (consumed) {
                        count[0] = 1;
                        lowerCount.run();
                    } else {
                        lowerCount.run();
                        runDefault.run();
                    }
                });
            } else
                runDefault.run();


        } else if (text != null) {
            imagesFromText.addAll(Utility.getImageUrlsFromText(text));
            connectLists.run();
        }
    }

    public static void showSelectImageDialog(Context context, CustomList<String> imageUrlList, @Nullable String sourceUrlRoot, GenericInterface<String> onImageSelected) {
        CustomDialog.Builder(context)
                .setTitle("Bild Auswählen")
                .enableTitleBackButton()
                .setView(customDialog -> new CustomRecycler<String>((AppCompatActivity) context)
                        .setItemLayout(R.layout.list_item_select_thumbnail)
                        .setObjectList(imageUrlList)
                        .setSetItemContent((customRecycler, itemView, imageUrl, index) -> {
                            ImageView thumbnail = itemView.findViewById(R.id.listItem_selectThumbnail_thumbnail);
                            thumbnail.setVisibility(View.VISIBLE);
                            View editUrlLayout = itemView.findViewById(R.id.listItem_selectThumbnail_editLayout);
                            editUrlLayout.setVisibility(View.GONE);
                            TextInputLayout listItem_selectThumbnail_url_layout = itemView.findViewById(R.id.listItem_selectThumbnail_url_layout);
                            EditText listItem_selectThumbnail_url = listItem_selectThumbnail_url_layout.getEditText();
                            listItem_selectThumbnail_url.setText(imageUrl);
                            Button listItem_selectThumbnail_test = itemView.findViewById(R.id.listItem_selectThumbnail_test);

                            Runnable showFailedTextBox = () -> {
                                thumbnail.setVisibility(View.GONE);
                                editUrlLayout.setVisibility(View.VISIBLE);
                                new Helpers.TextInputHelper(listItem_selectThumbnail_test::setEnabled, listItem_selectThumbnail_url_layout)
                                        .setValidation(listItem_selectThumbnail_url_layout, (validator, text1) -> validator.setRegEx(CategoriesActivity.pictureRegexAll));
                            };

                            if (imageUrl.startsWith("https")) {
                                Utility.loadUrlIntoImageView(context, thumbnail, imageUrl, null, showFailedTextBox);
                            } else {
                                /*imageUrl.startsWith("http://") || imageUrl.startsWith("data:image")*/
                                showFailedTextBox.run();
                            }

                            listItem_selectThumbnail_test.setOnClickListener(v2 -> {
                                String newUrl = listItem_selectThumbnail_url.getText().toString().trim();
                                int position = customRecycler.getRecycler().getChildAdapterPosition(itemView);
                                imageUrlList.remove(position);
                                imageUrlList.add(position, newUrl);
                                customRecycler.update(position);
                            });
                        })
                        .addSubOnClickListener(R.id.listItem_selectThumbnail_thumbnail, (customRecycler, itemView, s, index) -> {
                            String result = ((EditText) itemView.findViewById(R.id.listItem_selectThumbnail_url)).getText().toString().trim();
                            onImageSelected.run(result);
                            customDialog.dismiss();
                        })
                        .addSubOnLongClickListener(R.id.listItem_selectThumbnail_thumbnail, (customRecycler, itemView, s, index) -> {
                            ImageView thumbnail = itemView.findViewById(R.id.listItem_selectThumbnail_thumbnail);
                            thumbnail.setVisibility(View.GONE);
                            View editUrlLayout = itemView.findViewById(R.id.listItem_selectThumbnail_editLayout);
                            editUrlLayout.setVisibility(View.VISIBLE);
                        })
                        .generateRecyclerView())
                .addOptionalModifications(customDialog -> {
                    if (imageUrlList.size() > 1)
                        customDialog.setDimensionsFullscreen();
                })
                .disableScroll()
                .show();
    }

    public static CustomList<String> getImageUrlsFromText(String text) {
        Matcher matcher = Pattern.compile(CategoriesActivity.pictureRegex).matcher(text);
        CustomList<String> urlList = new CustomList<>();
        while (matcher.find()) {
            urlList.add(matcher.group());
        }
        return urlList;
    }

    /** <------------------------- Internet Dialog ------------------------- */


    public static void openFileChooser(AppCompatActivity activity, String imeType, int... requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(imeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            activity.startActivity(new Intent(activity, ActivityResultHelper.class)); //.
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(activity, "Bitte einen Dateimanager installieren", Toast.LENGTH_SHORT).show();
        }
    }

    public static void restartApp(AppCompatActivity context) {
        Intent mStartActivity = new Intent(context, context.getClass());
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static boolean isActivityInForeground(AppCompatActivity activity) {
        return activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    public static String hash(String s) {
        return Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();
    }

    public static void changeWindowKeyboard(Window window, boolean show) {
        if (show)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        else
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void changeWindowKeyboard(Context context, View view, boolean show) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        else
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void openUrl(Context context, String url, boolean select) {
        if (!url.contains("http://") && !url.contains("https://"))
            url = "https://".concat(url);
        if (!select) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            Intent chooser = Intent.createChooser(i, "Öffnen mit...");
            if (chooser.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(chooser);
        }
    }

    public static String formatToEuro(double amount) {
        if (amount == 0)
            return "N/A";
        if (amount % 1 == 0)
            return String.format(Locale.GERMANY, "%.0f €", amount);
        else
            return String.format(Locale.GERMANY, "%.2f €", amount);
    }

    public static void applyCategoriesLink(AppCompatActivity activity, CategoriesActivity.CATEGORIES category, TextView textView, List<String> idList) {
        CustomList<ParentClass> list;
        switch (category) {
            default:
                Map<String, ? extends ParentClass> map = getMapFromDatabase(category);
                list = idList.stream().map(map::get).collect(Collectors.toCollection(CustomList::new));
                break;
            case MEDIA_CATEGORY:
            case MEDIA_EVENT:
                list = idList.stream().map(id -> (ParentClass) ParentClass_Tree.findObjectById(category, id)).collect(Collectors.toCollection(CustomList::new));
                break;
        }


        SpannableStringBuilder builder = new SpannableStringBuilder();

        final Boolean[] longPress = {null};
        for (ParentClass parentClass : list) {
            String s = parentClass.getName();
            if (builder.length() != 0)
                builder.append(", ");

            ClickableSpan clickableSpan = new ClickableSpan() {

                @Override
                public void onClick(View textView) {
                    if (longPress[0] != null && longPress[0]) {
                        longPress[0] = false;
                        Runnable openCategoriesActivity = () -> {
                            Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                            activity.startActivity(new Intent(activity, CategoriesActivity.class)
                                    .putExtra(MainActivity.EXTRA_CATEGORY, category)
                                    .putExtra(CategoriesActivity.EXTRA_SEARCH, /*CategoriesActivity.escapeForSearchExtra(*/s));
                        };

                        if (!(parentClass instanceof Darsteller) || ((Darsteller) parentClass).getTmdbId() == 0)
                            openCategoriesActivity.run();
                        else {
                            CustomDialog.Builder(activity)
                                    .setTitle(s)
                                    .enableStackButtons()
                                    .disableButtonAllCaps()
                                    .addButton("Filme Suchen", customDialog -> activity.startActivity(new Intent(activity, activity.getClass())
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, category)
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH, s)))
                                    .addButton("Kategorie Öffnen", customDialog -> openCategoriesActivity.run())
                                    .addButton("Filme Aktualisieren", customDialog ->
                                            VideoActivity.addActorToAll(activity, ((ParentClass_Tmdb) parentClass), category))
                                    .enableButtonDividerAll()
                                    .show();
                        }

                    } else if (longPress[0] != null && !longPress[0]) {
                        longPress[0] = null;
                    } else {
                        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                        activity.startActivity(new Intent(activity, activity.getClass())
                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, category)
                                .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(s)));
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            builder.append(s, clickableSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        GestureDetector detector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                longPress[0] = true;
                textView.onTouchEvent(e);
                e.setAction(MotionEvent.ACTION_UP);
                textView.onTouchEvent(e);
            }
        });
        textView.setOnTouchListener((v, event) -> detector.onTouchEvent(event));

        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
//        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setLinkTextColor(textView.getTextColors());
//                activity.getResources().getColorStateList(R.color.clickable_text_color, null));
    }

    public static void addSelectionMenuItem(TextView textView, String itemName, Utility.GenericInterface<CharSequence> onClick) {
        if (textView == null)
            return;

        textView.setTextIsSelectable(true);
        textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.add(itemName);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getTitle().equals(itemName)) {
                    int min = 0;
                    int max = textView.getText().length();
                    if (textView.isFocused()) {
                        final int selStart = textView.getSelectionStart();
                        final int selEnd = textView.getSelectionEnd();

                        min = Math.max(0, Math.min(selStart, selEnd));
                        max = Math.max(0, Math.max(selStart, selEnd));
                    }
                    final CharSequence selectedText = textView.getText().subSequence(min, max);
                    onClick.run(selectedText);
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                String BREAKPOINT = null;
            }
        });
    }

    public static void searchQueryOnInternet(AppCompatActivity activity, CharSequence query) {
        query = encodeTextForUrl(query);
        String url = "https://www.google.de/search?q=" + query;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        Intent chooser = Intent.createChooser(intent, "Suchen mit...");
        if (chooser.resolveActivity(activity.getPackageManager()) != null)
            activity.startActivity(chooser);
        else
            Toast.makeText(activity, "Fehler", Toast.LENGTH_SHORT).show();
    }

    public static CharSequence encodeTextForUrl(CharSequence text) {
        try {
            text = URLEncoder.encode(text.toString(), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return text;
    }

    public static void applySelectionSearch(CategoriesActivity.CATEGORIES category, CustomDialog customDialog) {
        List<TextView> list = new ArrayList<>();
        if (CustomUtility.stringExists(customDialog.getTitle()))
            list.add(customDialog.getTitleTextView());
        if (CustomUtility.stringExists(customDialog.getText()))
            list.add(customDialog.getTextTextView());
        if (customDialog.isShowEdit())
            list.add(customDialog.getEditLayout().getEditText());

        applySelectionSearch((AppCompatActivity) customDialog.getContext(), category, list.toArray(new TextView[0]));
    }

    public static void applySelectionSearch(AppCompatActivity context, CategoriesActivity.CATEGORIES category, TextView... textViews) {
        GenericInterface<CharSequence> onSelection = charSequence -> {
            CustomDialog.Builder(context)
                    .setTitle("Text Suchen")
                    .setText(com.maxMustermannGeheim.linkcollection.Utilities.Helpers.SpannableStringHelper.Builder(spanBuilder -> spanBuilder.appendBold("Text: ").append(charSequence)))
                    .addButton("Websuche", customDialog1 -> {
                        Utility.searchQueryOnInternet(context, charSequence);
                    })
                    .addButton("Lokal", customDialog1 -> {
                        context.startActivity(new Intent(context, category.getSearchIn())
                                .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(charSequence.toString()))
                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, category));
                    })
                    .enableButtonDividerAll()
                    .enableStackButtons()
                    .show();
        };
        for (TextView textView : textViews)
            Utility.addSelectionMenuItem(textView, "Suche", onSelection);
    }

    public static void colorMenuItemIcon(Menu menu, @IdRes int id, int color) {
        menu.findItem(id).setIconTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{color}));
    }

    public static CharSequence dialogDeleteText(String name) {
        return new com.maxMustermannGeheim.linkcollection.Utilities.Helpers.SpannableStringHelper().append("Möchtest du wirklich '").appendBold(name).append("' löschen?").get();
    }

    public static void applyDialogChangeCallback(Object o, CustomDialog customDialog, GenericInterface<Boolean> changeCallback) {
        int oldHash = o.hashCode();
        customDialog.addOnDialogDismiss(customDialog1 -> changeCallback.run(oldHash != o.hashCode()));
    }

    public static void applyDialogChangeCallback(Object o, CustomDialog customDialog, Runnable changeCallback) {
        applyDialogChangeCallback(o, customDialog, hasChange -> {
            CustomUtility.logD(null, "applyDialogChangeCallback: %s", hasChange);
            if (hasChange)
                changeCallback.run();
        });
    }


    /** ------------------------- Api -------------------------> */
    public static void importTmdbGenre(Context context, boolean direct, boolean isVideo) {
        Runnable executeImport = () -> {
            String requestUrl = "https://api.themoviedb.org/3/genre/" +
                    (isVideo ? "movie" : "tv") +
                    "/list?api_key=09e015a2106437cbc33bf79eb512b32d&language=de";
            RequestQueue requestQueue = Volley.newRequestQueue(context);

            Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();


            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
                JSONArray results;
                try {
                    results = response.getJSONArray("genres");

                    if (results.length() == 0) {
                        Toast.makeText(context, "Keine Ergebnisse gefunden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isVideo) {
                        Map<String, Genre> genreMap = Database.getInstance().genreMap;
                        CustomList<Genre> list = new CustomList<>(genreMap.values());

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject object = results.getJSONObject(i);

                            int id = object.getInt("id");
                            String name = object.getString("name");

                            Optional<Genre> optional = list.stream().filter(genre -> genre.getName().toLowerCase().equals(name.toLowerCase())).findFirst();
                            if (optional.isPresent()) {
                                optional.get().setTmdbGenreId(id);
                            } else {
                                optional = list.stream().filter(genre -> genre.getTmdbGenreId() == id).findFirst();
                                if (optional.isPresent()) {
                                    optional.get().setName(name);
                                } else {
                                    Genre genre = new Genre(name).setTmdbGenreId(id);
                                    genreMap.put(genre.getUuid(), genre);
                                }
                            }
                        }
                    } else {
                        Map<String, ShowGenre> genreMap = Database.getInstance().showGenreMap;
                        CustomList<ShowGenre> list = new CustomList<>(genreMap.values());

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject object = results.getJSONObject(i);

                            int id = object.getInt("id");
                            String name = object.getString("name");

                            Optional<ShowGenre> optional = list.stream().filter(genre -> genre.getName().toLowerCase().equals(name.toLowerCase())).findFirst();
                            if (optional.isPresent()) {
                                optional.get().setTmdbGenreId(id);
                            } else {
                                optional = list.stream().filter(genre -> genre.getTmdbGenreId() == id).findFirst();
                                if (optional.isPresent()) {
                                    optional.get().setName(name);
                                } else {
                                    ShowGenre genre = new ShowGenre(name).setTmdbGenreId(id);
                                    genreMap.put(genre.getUuid(), genre);
                                }
                            }
                        }
                    }

                    Toast.makeText(context, "Genre Importiert", Toast.LENGTH_SHORT).show();
                    Database.saveAll();
                } catch (JSONException ignored) {
                }

            }, error -> Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show());

            requestQueue.add(jsonArrayRequest);
        };

        if (direct)
            executeImport.run();
        else
            CustomDialog.Builder(context)
                    .setTitle("Genre Importieren")
                    .setText("Willst du wirklich alle THDb Genres importieren?")
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog -> {
                        executeImport.run();
                    })
                    .show();
    }

    // --------------- Trakt

    public static void getImdbIdFromTmdbId(Context context, int tmdbId, String type, CustomUtility.GenericInterface<String> onResult) {
        String requestUrl = "https://api.trakt.tv/search/tmdb/" + tmdbId + "?type=" + type;

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null, response -> {
            try {
                String string = response.getJSONObject(0).getJSONObject(type).getJSONObject("ids").getString("imdb");
                onResult.run(Utility.stringExists(string) && !string.equals("null") ? string : null);
            } catch (JSONException ignored) {
                onResult.run(null);
            }

        }, error -> {
            Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show();
            onResult.run(null);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("trakt-api-version", "2");
                headers.put("trakt-api-key", "b14293841cd183ea33e42dd61070e57b82d4f71e22a4dab89691290ba7e0be83");
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);

    }

    public static <T> void traktApiRequest(Context context, String url, Class<T> returnClass, CustomUtility.GenericInterface<T> onResult) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        Request request = null;
        if (JSONArray.class.equals(returnClass)) {
            request = new JsonArrayRequest(Request.Method.GET, url, null, response -> onResult.run((T) response), error -> Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("trakt-api-version", "2");
                    headers.put("trakt-api-key", "b14293841cd183ea33e42dd61070e57b82d4f71e22a4dab89691290ba7e0be83");
                    return headers;
                }
            };
        } else if (JSONObject.class.equals(returnClass)) {
            request = new JsonObjectRequest(Request.Method.GET, url, null, response -> onResult.run((T) response), error -> Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("trakt-api-version", "2");
                    headers.put("trakt-api-key", "b14293841cd183ea33e42dd61070e57b82d4f71e22a4dab89691290ba7e0be83");
                    return headers;
                }
            };
        }

        if (request != null)
            requestQueue.add(request);

    }

    // --------------- WerStreamtEs

    public static void doWerStreamtEsRequest(AppCompatActivity context, String query) {
        ExternalCode.CodeEntry codeEntry = ExternalCode.getEntry(ExternalCode.ENTRY.GET_WER_STREAMT_ES);
        new com.maxMustermannGeheim.linkcollection.Utilities.Helpers.WebViewHelper(context, "https://www.werstreamt.es/filme-serien/?q=" + query.replaceAll(" ", "+"))
                .enableLoadImages()
                .addRequest(codeEntry.getString("getProviderApp"), s -> {
                    try {
                        JSONArray resArr = new JSONArray(s);
                        List<JSONObject> resList = new ArrayList<>();
                        for (int i = 0; i < resArr.length(); i++) {
                            try {
                                resList.add(resArr.getJSONObject(i));
                            } catch (JSONException ignored) {
                            }
                        }

                        JSONObject[] currentSelected = {resList.stream().filter(jsonObject -> {
                            try {
                                return jsonObject.get("provider") instanceof JSONObject;
                            } catch (JSONException ignored) {
                                return false;
                            }
                        }).findFirst().orElse(resArr.getJSONObject(0))};
                        CustomDialog dialog = CustomDialog.Builder(context);

                        Runnable showProviders = () -> {
                            try {
                                RecyclerView recycler = dialog.findViewById(R.id.dialog_werStreamtEs_providers);
                                TextView empty = dialog.findViewById(R.id.dialog_werStreamtEs_empty);
                                if (currentSelected[0].get("provider") instanceof JSONObject) {
                                    recycler.setVisibility(View.VISIBLE);
                                    empty.setVisibility(View.GONE);
                                    JSONObject provider = currentSelected[0].getJSONObject("provider");
                                    new com.finn.androidUtilities.CustomRecycler<CharSequence>((AppCompatActivity) context, recycler)
                                            .enableDivider(12)
                                            .setGetActiveObjectList((customRecycler) -> {
                                                CustomList<Pair<String, Boolean>> list = new CustomList<>();
                                                Iterator<String> keys = provider.keys();
                                                while (keys.hasNext()) {
                                                    String key = keys.next();
                                                    try {
                                                        list.add(Pair.create(key, provider.getBoolean(key)));
                                                    } catch (JSONException ignored) {
                                                    }
                                                }
                                                return list.sorted((o1, o2) -> o1.second.compareTo(o2.second) * -1).map(pair -> {
                                                    return new com.maxMustermannGeheim.linkcollection.Utilities.Helpers.SpannableStringHelper()
                                                            .appendBold(getStreamingProviderById(Integer.parseInt(pair.first)))
                                                            .append("   ")
                                                            .appendColor(pair.second ? "Verfügbar" : "Nicht Verfügbar", pair.second ? context.getColor(R.color.colorGreen) : Color.RED)
                                                            .get();
                                                });
                                            })
                                            .generate();
                                } else {
                                    empty.setVisibility(View.VISIBLE);
                                    recycler.setVisibility(View.GONE);
                                }
                            } catch (JSONException ignored) {
                            }
                        };

                        dialog
                                .setTitle("WerStreamt.es")
                                .disableScroll()
                                .setView(R.layout.dialog_wer_streamt_es)
                                .setSetViewContent((customDialog2, view1, reload1) -> {
                                    new com.finn.androidUtilities.CustomRecycler<JSONObject>(context, customDialog2.findViewById(R.id.dialog_werStreamtEs_results))
                                            .setObjectList(resList)
                                            .setOrientation(com.finn.androidUtilities.CustomRecycler.ORIENTATION.HORIZONTAL)
                                            .setItemLayout(R.layout.list_item_wer_streamt_es_results)
                                            .setSetItemContent((customRecycler, itemView, jsonObject, index) -> {
                                                try {
                                                    String img = jsonObject.getString("img");
                                                    CustomUtility.loadUrlIntoImageView(context, itemView.findViewById(R.id.listItem_werStreamtEs_results_image), img, img);
                                                    TextView nameTextView = itemView.findViewById(R.id.listItem_werStreamtEs_results_name);
                                                    if (currentSelected[0] == jsonObject)
                                                        nameTextView.setText(new com.finn.androidUtilities.Helpers.SpannableStringHelper().appendUnderlined(jsonObject.getString("name")).get());
                                                    else
                                                        nameTextView.setText(jsonObject.getString("name"));
                                                } catch (JSONException ignored) {
                                                }
                                            })
                                            .setOnClickListener((customRecycler, itemView, jsonObject, index) -> {
                                                currentSelected[0] = jsonObject;
                                                customRecycler.reload();
                                                showProviders.run();
                                            })
                                            .generate();
                                })
                                .setDimensionsFullscreen()
                                .addButton(R.drawable.ic_open_in_new, customDialog2 -> {
                                    try {
                                        Utility.openUrl(context, "https://www.werstreamt.es/film/details/" + currentSelected[0].getString("id"), true);
                                    } catch (JSONException e) {
                                        Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show();
                                    }
                                }, false)
                                .show();
                        showProviders.run();

                    } catch (JSONException e) {
                        Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show();
                    }
                })
//                                                .setDebug(true)
                .go();
    }

    private static String getStreamingProviderById(int id) {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "iTunes");
        map.put(3, "maxdome");
        map.put(4, "Google Play");
        map.put(10, "Amazon");
        map.put(11, "Netflix");
        map.put(12, "Sky Go");
        map.put(13, "Microsoft");
        map.put(14, "VIDEOBUSTER");
        map.put(15, "videociety");
        map.put(16, "Sony");
        map.put(18, "Sky Ticket");
        map.put(20, "Rakuten TV");
        map.put(23, "CHILI");
        map.put(30, "Sky Store");
        map.put(31, "Pantaflix");
        map.put(32, "Crunchyroll");
        map.put(33, "Anime On Demand");
        map.put(34, "MagentaTV");
        map.put(35, "freenet Video");
        map.put(42, "Disney+");
        map.put(44, "Cineplex Home");
        map.put(52, "Animax Plus");

        return CustomUtility.stringExistsOrElse(map.get(id), String.valueOf(id));
    }
    /**  <------------------------- Api -------------------------  */


    /** ------------------------- watchLater -------------------------> */
    public static List<String> getWatchLaterList_uuid() {
        return getWatchLaterList().stream().map(ParentClass::getUuid).collect(Collectors.toList());
    }

    public static com.finn.androidUtilities.CustomList<Video> getWatchLaterList() {
        if (!Database.isReady())
            return new com.finn.androidUtilities.CustomList<>();
        Database database = Database.getInstance();

        return database.videoMap.values().stream().filter(Video::isWatchLater).collect(Collectors.toCollection(com.finn.androidUtilities.CustomList::new));
    }
    /**  <------------------------- watchLater -------------------------  */


    /** ------------------------- Copy -------------------------> */
    public static <T> T deepCopy(T t) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        return (T) gson.fromJson(gson.toJson(t), t.getClass());
    }
    /**  <------------------------- Copy -------------------------  */


    /** ------------------------- OnClickListener -------------------------> */
    public static View.OnClickListener getOnClickListener(View view) {
        View.OnClickListener retrievedListener = null;
        String viewStr = "android.view.View";
        String lInfoStr = "android.view.View$ListenerInfo";

        try {
            Field listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnClickListener");

            if (clickListenerField != null && listenerInfo != null) {
                retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
            }
        } catch (NoSuchFieldException ex) {
            Log.e("Reflection", "No Such Field.");
        } catch (IllegalAccessException ex) {
            Log.e("Reflection", "Illegal Access.");
        } catch (ClassNotFoundException ex) {
            Log.e("Reflection", "Class Not Found.");
        }
        return retrievedListener;
    }

    public static void interceptOnClick(View view, InterceptOnClick interceptOnClick) {
        View.OnClickListener oldListener = getOnClickListener(view);
        view.setOnClickListener(v -> {
            if (!interceptOnClick.runInterceptOnClick(view))
                oldListener.onClick(view);
        });
    }

    interface InterceptOnClick {
        boolean runInterceptOnClick(View view);
    }
    /**  <------------------------- OnClickListener -------------------------  */


    /**  ------------------------- Filter ------------------------->  */
    /** ------------------------- ... in Videos -------------------------> */
    public static boolean containedInVideo(String query, Video video, HashSet<VideoActivity.FILTER_TYPE> filterTypeSet) {
        if (video.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.NAME)) {
            if (containedInVideo(video.getName(), query, filterTypeSet.size() == 1))
                return true;
            if (containedInTranslation(video.getTranslationList(), query, filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.ACTOR)) {
            if (containedInActors(query, video.getDarstellerList(), filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.GENRE)) {
            if (containedInGenre(query, video.getGenreList(), filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.STUDIO)) {
            if (containedInStudio(query, video.getStudioList(), filterTypeSet.size() == 1))
                return true;
        }
        if (filterTypeSet.contains(VideoActivity.FILTER_TYPE.COLLECTION)) {
            if (containedInCollection(query, video.getUuid(), filterTypeSet.size() == 1))
                return true;
        }
        return false;
    }

    private static boolean containedInVideo(String all, String sub, boolean exact) {
        if (exact)
            return all.equals(sub);
        else
            return all.toLowerCase().contains(sub.toLowerCase());
    }

    private static boolean containedInTranslation(List<String> all, String sub, boolean exact) {
        if (exact)
            return all.stream().anyMatch(s -> s.equals(sub));
        else
            return all.stream().anyMatch(s -> s.toLowerCase().contains(sub.toLowerCase()));
    }

    private static boolean containedInActors(String query, List<String> actorUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String actorUUid : actorUuids) {
            if (exact) {
                if (database.darstellerMap.get(actorUUid).getName().equals(query))
                    return true;
            } else {
                if (ParentClass_Alias.containsQuery(database.darstellerMap.get(actorUUid), query))
                    return true;
            }

        }
        return false;
    }

    private static boolean containedInGenre(String query, List<String> genreUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String genreUUid : genreUuids) {
            if (exact) {
                if (database.genreMap.get(genreUUid).getName().equals(query))
                    return true;
            } else {
                if (ParentClass_Alias.containsQuery(database.genreMap.get(genreUUid), query))
                    return true;
            }
        }
        return false;
    }

    private static boolean containedInStudio(String query, List<String> studioUuids, boolean exact) {
        Database database = Database.getInstance();
        for (String studioUUid : studioUuids) {
            if (exact) {
                if (database.studioMap.get(studioUUid).getName().equals(query))
                    return true;
            } else {
                if (ParentClass_Alias.containsQuery(database.studioMap.get(studioUUid), query))
                    return true;
            }
        }
        return false;
    }

    public static boolean containedInCollection(String query, String uuid, boolean exact) {
        Database database = Database.getInstance();
        for (com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection collection : database.collectionMap.values()) {
            if (exact) {
                if (collection.getName().equals(query) && collection.getFilmIdList().contains(uuid))
                    return true;
            } else {
                if (collection.getName().toLowerCase().contains(query.toLowerCase()) && collection.getFilmIdList().contains(uuid))
                    return true;
            }
        }
        return false;
    }

    public static boolean containedInCollection(String query, com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection collection, HashSet<CollectionActivity.FILTER_TYPE> filterTypeSet) {
        Database database = Database.getInstance();
        String lowerCase = query.toLowerCase();
        if (filterTypeSet.contains(CollectionActivity.FILTER_TYPE.NAME) && collection.getName().toLowerCase().contains(lowerCase))
            return true;
        if (filterTypeSet.contains(CollectionActivity.FILTER_TYPE.FILM) && collection.getFilmIdList().stream().map(uuid -> database.videoMap.get(uuid)).anyMatch(video -> video.getName().toLowerCase().contains(lowerCase)))
            return true;
        return false;
    }

    public static boolean containedInWatchList(String query, String uuid, boolean exact) {
        Database database = Database.getInstance();
        for (WatchList watchList : database.watchListMap.values()) {
            if (exact) {
                if (watchList.getName().equals(query) && watchList.getVideoIdList().contains(uuid))
                    return true;
            } else {
                if (watchList.getName().toLowerCase().contains(query.toLowerCase()) && watchList.getVideoIdList().contains(uuid))
                    return true;
            }
        }
        return false;
    }
    /**  <------------------------- ... in Videos -------------------------  */

    /** ------------------------- ... in Knowledge -------------------------> */
    public static boolean containedInKnowledge(String query, Knowledge knowledge, HashSet<KnowledgeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.NAME) && knowledge.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
            for (String categoryId : knowledge.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }
        if (filterTypeSet.contains(KnowledgeActivity.FILTER_TYPE.CONTENT)) {
            if (knowledge.hasContent() && knowledge.getContent().toLowerCase().contains(query.toLowerCase()))
                return true;
            if (knowledge.hasItems() && knowledge.itemListToString_complete(true).toLowerCase().contains(query.toLowerCase()))
                return true;
        }

        return false;
    }
    /**  <------------------------- ... in Knowledge -------------------------  */

    /** ------------------------- ... in Owe -------------------------> */
    public static boolean containedInOwe(String query, Owe owe, HashSet<OweActivity.FILTER_TYPE> filterTypeSet) {
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OWN) && owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
            return false;
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OTHER) && owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OTHER)
            return false;

        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.OPEN) && owe.getItemList().stream().anyMatch(Owe.Item::isOpen))
            return false;
        if (!filterTypeSet.contains(OweActivity.FILTER_TYPE.CLOSED) && owe.getItemList().stream().noneMatch(Owe.Item::isOpen))
            return false;

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.NAME) && owe.getName().toLowerCase().contains(query.toLowerCase()))
            return true;

        if (filterTypeSet.contains(OweActivity.FILTER_TYPE.DESCRIPTION) && owe.getDescription().toLowerCase().contains(query.toLowerCase()))
            return true;

        return filterTypeSet.contains(OweActivity.FILTER_TYPE.PERSON) && owe.getItemList().stream().anyMatch(item -> Database.getInstance().personMap.get(item.getPersonId()).getName().toLowerCase().contains(query.toLowerCase()));

//        if (filterTypeSet.containedInVideo(KnowledgeActivity.FILTER_TYPE.CATEGORY)) {
////            for (ParentClass category : getMapFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES).values()) {
//            for (String categoryId : owe.getCategoryIdList()) {
//                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, categoryId).getName().toLowerCase().containedInVideo(query))
//                    return true;
//            }
//        }
    }
    /**  <------------------------- ... in Owe -------------------------  */

    /** ------------------------- ... in Joke -------------------------> */
    public static boolean containedInJoke(String query, Joke joke, HashSet<JokeActivity.FILTER_TYPE> filterTypeSet) {
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.NAME) && joke.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.PUNCHLINE) && joke.getPunchLine().toLowerCase().contains(query.toLowerCase()))
            return true;
        if (filterTypeSet.contains(JokeActivity.FILTER_TYPE.CATEGORY)) {
            for (String categoryId : joke.getCategoryIdList()) {
                if (getObjectFromDatabase(CategoriesActivity.CATEGORIES.JOKE_CATEGORIES, categoryId).getName().toLowerCase().contains(query.toLowerCase()))
                    return true;
            }
        }

        return false;
    }
    /**  <------------------------- ... in Joke -------------------------  */

    /** ------------------------- ... in Show -------------------------> */
    public static boolean containedInShow(String query, Show show, HashSet<ShowActivity.FILTER_TYPE> filterTypeSet) {
        if (show.getUuid().equals(query)) return true;
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.NAME) && show.getName().toLowerCase().contains(query.toLowerCase()))
            return true;
        Database database = Database.getInstance();
        if (filterTypeSet.contains(ShowActivity.FILTER_TYPE.GENRE) && show.getGenreIdList().stream().anyMatch(uuid -> database.showGenreMap.get(uuid).getName().toLowerCase().contains(query.toLowerCase())))
            return true;
        return false;
    }
    /**  <------------------------- ... in Show -------------------------  */

    /** ------------------------- ... in Media -------------------------> */
    public static boolean containedInMedia(String query, Media media, HashSet<MediaActivity.FILTER_TYPE> filterTypeSet) {
//        if (media.getUuid().equals(query)) return true;
//        if (filterTypeSet.contains(MediaActivity.FILTER_TYPE.PERSON) && media.getName().toLowerCase().contains(query.toLowerCase()))
//            return true;
        Database database = Database.getInstance();
        if (filterTypeSet.contains(MediaActivity.FILTER_TYPE.PERSON) && media.getPersonIdList().stream().anyMatch(uuid -> database.mediaPersonMap.get(uuid).getName().toLowerCase().contains(query.toLowerCase())))
            return true;
        if (filterTypeSet.contains(MediaActivity.FILTER_TYPE.CATEGORY) && media.getCategoryIdList().stream().anyMatch(uuid -> ParentClass_Tree.findObjectById(CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, uuid).getName().toLowerCase().contains(query.toLowerCase())))
            return true;
        return false;
    }
    /**  <------------------------- ... in Media -------------------------  */
/**  <------------------------- Filter -------------------------  */


    /** ------------------------- Calender -------------------------> */
    private static Date currentDate;

    /** ------------------------- FilmCalender -------------------------> */
    public static void setupFilmCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Video> videoList, boolean openVideo) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

        currentDate = Utility.removeTime(new Date());
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        Boolean showImages = Settings.getSingleSetting_boolean(context, Settings.SETTING_VIDEO_SHOW_IMAGES);

        Database database = Database.getInstance();
        CustomRecycler<Event> customRecycler = new CustomRecycler<Event>((AppCompatActivity) context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_video)
                .setSetItemContent((customRecycler1, itemView, event, index) -> {
                    itemView.findViewById(R.id.listItem_video_internetOrDetails).setVisibility(View.GONE);
                    itemView.findViewById(R.id.listItem_video_Views_layout).setVisibility(View.VISIBLE);

                    Video video = ((Video) event.getData());

                    MinDimensionLayout listItem_video_image_layout = itemView.findViewById(R.id.listItem_video_image_layout);
                    if (showImages && CustomUtility.stringExists(video.getImagePath())) {
                        listItem_video_image_layout.setVisibility(View.VISIBLE);
                        Utility.simpleLoadUrlIntoImageView(context, itemView.findViewById(R.id.listItem_video_image), video.getImagePath(), video.getImagePath(), 4);
                    } else
                        listItem_video_image_layout.setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.listItem_video_Titel)).setText(video.getName());
                    TextView daysTextView = (TextView) itemView.findViewById(R.id.listItem_video_Views);
                    daysTextView.setText("");
                    Runnable setDaysText = () -> {
                        String daysText;
                        long days = Days.daysBetween(new LocalDate(event.getTimeInMillis()), new LocalDate(new Date())).getDays();
                        if (daysTextView.getText().toString().matches("\\d+ d")) {
                            daysText = com.maxMustermannGeheim.linkcollection.Utilities.Helpers.DurationFormatter.formatDefault(Duration.ofDays(days), "'%y% y~, ~''%w% w~, ~''%d% d~, ~'");
                        } else {
                            daysText = days + " d";
                        }
                        daysTextView.setText(daysText);
                    };
                    itemView.findViewById(R.id.listItem_video_Views_layout).setOnClickListener(v -> setDaysText.run());
                    daysTextView.setTextColor(ContextCompat.getColorStateList(context, R.color.clickable_text_color_normal));
                    setDaysText.run();

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    itemView.findViewById(R.id.listItem_video_Darsteller).setSelected(true);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    itemView.findViewById(R.id.listItem_video_Studio).setSelected(true);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) itemView.findViewById(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    itemView.findViewById(R.id.listItem_video_Genre).setSelected(true);

                    if (video.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_video_rating)).setText(String.valueOf(video.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_video_rating_layout).setVisibility(View.GONE);
                    ParentClass_Ratable.applyRatingTendencyIndicator(itemView.findViewById(R.id.listItem_video_ratingTendency), video, true, false);

                })
                .addOptionalModifications(customRecycler1 -> {
                    if (openVideo)
                        customRecycler1.setOnClickListener((customRecycler2, view, object, index) -> {
                            ((MainActivity) context).startActivityForResult(new Intent(context, VideoActivity.class)
                                    .putExtra(CategoriesActivity.EXTRA_SEARCH, ((ParentClass) ((Event) object).getData()).getUuid())
                                    .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO), MainActivity.START_VIDEO_FROM_CALENDER);
                        });
                    else
                        customRecycler1.setOnClickListener((customRecycler2, itemView, event, index) -> Toast.makeText(context, "Zum Bearbeiten lange drücken", Toast.LENGTH_SHORT).show());
                })
                .setOnLongClickListener((customRecycler1, view, event, index) -> showEditTimeDialog((AppCompatActivity) context, event, calendarView, newView -> {
                    List<Date> dateList = ((Video) event.getData()).getDateList();
                    Date oldView = new Date(event.getTimeInMillis());
                    dateList.remove(oldView);
                    dateList.add(newView);

                    List<Event> events = calendarView.getEvents(currentDate);
                    loadVideoList(events, layout, customRecycler1);
                    setButtons(layout, events.isEmpty() ? 0 : 1, calendarView, videoList, customRecycler1, true);
                    Database.saveAll(context);
                }));


        for (Video video : videoList) {
            for (Date date : video.getDateList()) {
                Event ev1 = new Event(context.getColor(R.color.colorDayNightContent)
                        , date.getTime(), video);
                calendarView.addEvent(ev1);

            }

        }

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
                calendarView.setCurrentDate(currentDate);
                setButtons(layout, calendarView.getEvents(dateClicked).size(), calendarView, videoList, customRecycler, true);
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        setButtons(layout, calendarView.getEvents(new Date()).size(), calendarView, videoList, customRecycler, true);

        if (videoList.size() != 1)
            return;

        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            Runnable addView = () -> {
                if (currentDate.equals(Utility.removeTime(new Date())))
                    videoList.get(0).addDate(new Date(), true, context);
                else
                    videoList.get(0).addDate(currentDate, false, context);
                calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                        , currentDate.getTime(), videoList.get(0)));
                loadVideoList(calendarView.getEvents(currentDate), layout, customRecycler);
                setButtons(layout, 1, calendarView, videoList, customRecycler, true);
                Database.saveAll();
            };
            WatchListActivity.checkWatchList(context, videoList.get(0), addView);
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            videoList.get(0).removeDate(currentDate);
            calendarView.removeEvents(currentDate);
            loadVideoList(calendarView.getEvents(currentDate), layout, customRecycler);
            setButtons(layout, 0, calendarView, videoList, customRecycler, true);
            Database.saveAll();
        });
    }
    /**  <------------------------- FilmCalender -------------------------  */

    // ToDo: Alignment von den Buttons ändern

    /** ------------------------- EpisodeCalender -------------------------> */
    public static void setupEpisodeCalender(Context context, CompactCalendarView calendarView, FrameLayout layout, List<Show.Episode> episodeList, boolean openEpisode, boolean editMode) {
        calendarView.removeAllEvents();
        TextView calender_month = layout.findViewById(R.id.fragmentCalender_month);
        ImageView calender_previousMonth = layout.findViewById(R.id.fragmentCalender_previousMonth);
        ImageView calender_nextMonth = layout.findViewById(R.id.fragmentCalender_nextMonth);

        currentDate = Utility.removeTime(new Date());
        calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        calender_previousMonth.setOnClickListener(view -> calendarView.scrollLeft());
        calender_nextMonth.setOnClickListener(view -> calendarView.scrollRight());

        int showPreviewSetting = Settings.getSingleSetting_int(context, Settings.SETTING_SHOW_EPISODE_PREVIEW);

        Database database = Database.getInstance();
        CustomRecycler<Event> customRecycler = new CustomRecycler<Event>((AppCompatActivity) context, layout.findViewById(R.id.fragmentCalender_videoList))
                .setItemLayout(R.layout.list_item_episode)
                .setSetItemContent((customRecycler1, itemView, event, index) -> {
                    itemView.findViewById(R.id.listItem_episode_seen).setVisibility(View.GONE);
                    Show.Episode episode = ((Show.Episode) event.getData());

                    ImageView imageView = itemView.findViewById(R.id.listItem_episode_image);
                    if (Utility.stringExists(episode.getStillPath()) && (showPreviewSetting == 0 || showPreviewSetting == 1 && episode.isWatched())) {
                        imageView.setVisibility(View.VISIBLE);
                        Utility.simpleLoadUrlIntoImageView(context, imageView, episode.getStillPath(), episode.getStillPath(), 3);
                    } else
                        imageView.setVisibility(View.GONE);

                    if (openEpisode) {
                        itemView.findViewById(R.id.listItem_episode_extraInfo).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_episode_showName)).setText(database.showMap.get(episode.getShowId()).getName());
                        ((TextView) itemView.findViewById(R.id.listItem_episode_seasonNumber)).setText(String.valueOf(episode.getSeasonNumber()));
                    }

                    ((TextView) itemView.findViewById(R.id.listItem_episode_number)).setText(String.valueOf(episode.getEpisodeNumber()));
                    ((TextView) itemView.findViewById(R.id.listItem_episode_name)).setText(episode.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_episode_rating)).setText(episode.getRating() != -1 ? episode.getRating() + " ☆" : "");
                    ParentClass_Ratable.applyRatingTendencyIndicator(itemView.findViewById(R.id.listItem_episode_ratingTendency), episode, episode.isWatched(), false);
                    ((TextView) itemView.findViewById(R.id.listItem_episode_release)).setText(Utility.isNullReturnOrElse(episode.getAirDate(), "", date -> new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)));

                    TextView daysTextView = itemView.findViewById(R.id.listItem_episode_viewCount);
                    daysTextView.setText("");
                    Runnable setDaysText = () -> {
                        String daysText;
                        long days = Days.daysBetween(new LocalDate(event.getTimeInMillis()), new LocalDate(new Date())).getDays();
                        if (daysTextView.getText().toString().matches("\\| \\d+ d")) {
                            daysText = com.maxMustermannGeheim.linkcollection.Utilities.Helpers.DurationFormatter.formatDefault(Duration.ofDays(days), "'%y% y~, ~''%w% w~, ~''%d% d~, ~'");
                        } else {
                            daysText = days + " d";
                        }
                        daysTextView.setText("| " + daysText);
                    };
                    daysTextView.setOnClickListener(v -> setDaysText.run());
                    daysTextView.setTextColor(ContextCompat.getColorStateList(context, R.color.clickable_text_color_normal));
                    setDaysText.run();


                })
                .addOptionalModifications(customRecycler1 -> {
                    if (openEpisode)
                        customRecycler1.setOnClickListener((customRecycler2, view, event, index) -> {
                            Show.Episode episode = (Show.Episode) event.getData();
                            if (context instanceof ShowActivity) {
                                ShowActivity.showEpisodeDetailDialog((AppCompatActivity) context, null, episode);
                            } else {
                                ((AppCompatActivity) context).startActivityForResult(new Intent(context, ShowActivity.class)
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, episode.getShowId())
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.EPISODE)
                                        .putExtra(ShowActivity.EXTRA_EPISODE, new Gson().toJson(episode)), MainActivity.START_SHOW_FROM_CALENDER);
                            }
                        });
                    else if (editMode)
                        customRecycler1.setOnClickListener((customRecycler2, itemView, event, index) -> Toast.makeText(context, "Zum Bearbeiten lange drücken", Toast.LENGTH_SHORT).show());

                    if (editMode)
                        customRecycler1.setOnLongClickListener((customRecycler2, view, event, index) -> showEditTimeDialog((AppCompatActivity) context, event, calendarView, newView -> {
                            List<Date> dateList = ((Show.Episode) event.getData()).getDateList();
                            Date oldView = new Date(event.getTimeInMillis());
                            dateList.remove(oldView);
                            dateList.add(newView);

                            List<Event> events = calendarView.getEvents(currentDate);
                            loadVideoList(events, layout, customRecycler1);
                            setButtons(layout, events.isEmpty() ? 0 : 1, calendarView, episodeList, customRecycler1, editMode);
                            Database.saveAll(context);
                        }));

                });


        for (Show.Episode episode : episodeList) {
            for (Date date : episode.getDateList()) {
                Event ev1 = new Event(context.getColor(R.color.colorDayNightContent)
                        , date.getTime(), episode);
                calendarView.addEvent(ev1);
            }
        }

//        final Date[] selectedDate = {removeTime(new Date())};
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
//                selectedDate[0] = dateClicked;
//                if (episodeList.size() == 1)
                setButtons(layout, calendarView.getEvents(dateClicked).size(), calendarView, episodeList, customRecycler, editMode);
                loadVideoList(calendarView.getEvents(dateClicked), layout, customRecycler);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calender_month.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfNewMonth));
            }
        });

        loadVideoList(calendarView.getEvents(new Date()), layout, customRecycler);

        setButtons(layout, calendarView.getEvents(new Date()).size(), calendarView, episodeList, customRecycler, editMode);

        if (!editMode)
            return;

        Show.Episode episode = episodeList.get(0);
        layout.findViewById(R.id.dialog_editViews_add).setOnClickListener(view -> {
            if (currentDate.equals(Utility.removeTime(new Date())))
                episode.addDate(new Date(), true, context);
            else {
                Show.Episode previousEpisode = episode._getPrevious_ifLoaded();
                if (previousEpisode != null) {
                    Optional<Date> previousDate = previousEpisode.getDateList().stream().filter(date -> Utility.removeTime(date).equals(currentDate)).findFirst();
                    if (previousDate.isPresent()) {
                        CustomDialog.Builder(context)
                                .setTitle("Zeitlich Einordnen?")
                                .setText(String.format(Locale.getDefault(), "Die Vorherige Episode (S:%d/E:%d) hat bereits eine Ansicht an diesem Tag.\nSoll die neue dahinter eingeordnet werden? ", episode.getSeasonNumber(), episode.getEpisodeNumber()))
                                .addButton(CustomDialog.BUTTON_TYPE.NO_BUTTON, customDialog -> episode.addDate(currentDate, false, context))
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog -> episode.addDate(new Date(previousDate.get().getTime() + 1000), false, context))
                                .show();
                    } else
                        episode.addDate(currentDate, false, context);
                } else
                    episode.addDate(currentDate, false, context);
            }
            calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent)
                    , currentDate.getTime(), episode));
            loadVideoList(calendarView.getEvents(currentDate), layout, customRecycler);
            setButtons(layout, 1, calendarView, episodeList, customRecycler, editMode);
        });
        layout.findViewById(R.id.dialog_editViews_remove).setOnClickListener(view -> {
            List<Event> events = calendarView.getEvents(currentDate);
            events.stream().max(Comparator.comparingLong(Event::getTimeInMillis)).ifPresent(event -> {
                episode.removeDate(new Date(event.getTimeInMillis()));
                calendarView.removeEvent(event);
                List<Event> eventList = calendarView.getEvents(currentDate);
                loadVideoList(eventList, layout, customRecycler);
                setButtons(layout, eventList.size(), calendarView, episodeList, customRecycler, editMode);
            });
        });
    }

    /** <------------------------- EpisodeCalender ------------------------- */

    private static void showEditTimeDialog(AppCompatActivity context, Event event, CompactCalendarView calendarView, GenericInterface<Date> applyDate) {
        SwitchDateTimeDialogFragment dateTimePicker = SwitchDateTimeDialogFragment.newInstance(
                "Zeit Auswählen",
                "OK",
                "Abbrechen",
                null,
                "de"
        );
//        CustomUtility.logD(null, "showEditTimeDialog: %", );
        int hourOffset = Settings.getSingleSetting_int(context, Settings.SETTING_MORE_HOUR_OFFSET);
        dateTimePicker.startAtTimeView();
        dateTimePicker.set24HoursMode(true);
        dateTimePicker.setDefaultDateTime(Utility.shiftTime(new Date(event.getTimeInMillis()), Calendar.HOUR, hourOffset));
        try {
            dateTimePicker.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException ignored) {
        }
        dateTimePicker.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date selectedDate) {
                Date shiftedDate = Utility.shiftTime(selectedDate, Calendar.HOUR, -hourOffset);
                calendarView.removeEvent(event);
                calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent), shiftedDate.getTime(), event.getData()));
                applyDate.run(shiftedDate);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
        dateTimePicker.showNow(context.getSupportFragmentManager(), "dialog_time");
        Dialog pickerDialog = dateTimePicker.getDialog();
        if (pickerDialog != null) {
            dateTimePicker.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    try {
                        pickerDialog.getWindow().getDecorView().findViewById(R.id.datetime_picker).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 2000));
                        View buttonPanel = pickerDialog.getWindow().getDecorView().findViewById(R.id.buttonPanel);
                        Button button = (Button) ((ViewGroup) ((ScrollView) buttonPanel).getChildAt(0)).getChildAt(3);
                        button.setBackground(context.getDrawable(R.drawable.abc_btn_colored_material));
                        button.setTextColor(context.getColor(R.color.colorTileBackground));
                        ViewFlipper viewFlipper = pickerDialog.getWindow().getDecorView().findViewById(R.id.dateSwitcher);
                        viewFlipper.getInAnimation().setDuration(100);
                        viewFlipper.getOutAnimation().setDuration(100);
//                        MaterialCalendarView calendarView1 = pickerDialog.getWindow().getDecorView().findViewById(R.id.datePicker);
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    DefaultLifecycleObserver.super.onResume(owner);
                }
            });
        } else
            CustomUtility.logD(null, "showEditTimeDialog: no");
//        CustomDateTimePicker timePicker = new CustomDateTimePicker(context, new CustomDateTimePicker.ICustomDateTimeListener() {
//            @Override
//            public void onSet(@NonNull Dialog dialog, @NonNull Calendar calendar, @NonNull Date selectedDate, int i, @NonNull String s, @NonNull String s1, int i1, int i2, @NonNull String s2, @NonNull String s3, int i3, int i4, int i5, int i6, @NonNull String s4) {
//                Date shiftedDate = Utility.shiftTime(selectedDate, Calendar.HOUR, -6);
//                calendarView.removeEvent(event);
//                calendarView.addEvent(new Event(context.getColor(R.color.colorDayNightContent), shiftedDate.getTime(), event.getData()));
//                applyDate.run(shiftedDate);
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//        });
//        timePicker.set24HourFormat(false);
//        timePicker.setDate(Utility.shiftTime(new Date(event.getTimeInMillis()), Calendar.HOUR, 6));
//        timePicker.showDialog();
    }

    private static void loadVideoList(List<Event> eventList, FrameLayout layout, CustomRecycler<Event> customRecycler) {
        eventList = new ArrayList<>(eventList);
        eventList.sort(Comparator.comparingLong(Event::getTimeInMillis));
        TextView calender_noTrips = layout.findViewById(R.id.fragmentCalender_noViews);

        if (eventList.isEmpty()) {
            calender_noTrips.setVisibility(View.VISIBLE);
        } else {
            calender_noTrips.setVisibility(View.GONE);
        }

        if (customRecycler.getObjectList().isEmpty()) {
            customRecycler.setObjectList(eventList).generate();
        } else
            customRecycler.reload(eventList);

    }

    private static void setButtons(FrameLayout layout, int size, CompactCalendarView calendarView, List list, CustomRecycler<Event> customRecycler, boolean allowEdit) {
        if (allowEdit && list.size() == 1) {
            layout.findViewById(R.id.dialog_editViews_add).setVisibility(size == 0 ? View.VISIBLE : View.GONE);
            layout.findViewById(R.id.dialog_editViews_remove).setVisibility(size != 0 ? View.VISIBLE : View.GONE);
        }

        CustomList<Date> dateList = new CustomList<>();
        for (Object o : list) {
            if (o instanceof Video) {
                Video video = (Video) o;
                dateList.addAll(video.getDateList());
            } else if (o instanceof Show.Episode) {
                Show.Episode episode = (Show.Episode) o;
                dateList.addAll(episode.getDateList());
            }
        }
        dateList.sorted(Date::compareTo).replaceAll(Utility::removeTime);
        dateList.distinct();

        View dialog_editViews_previous = layout.findViewById(R.id.dialog_editViews_previous);
        dialog_editViews_previous.setOnClickListener(v -> {
            Date previous = dateList.stream().filter(date -> date.before(currentDate)).collect(Collectors.toCollection(CustomList::new)).getLast();
            if (previous != null) {
                calendarView.setCurrentDate(previous);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(previous));
                currentDate = previous;
                setButtons(layout, 1, calendarView, list, customRecycler, allowEdit);
                loadVideoList(calendarView.getEvents(previous), layout, customRecycler);

            }
        });

        View dialog_editViews_next = layout.findViewById(R.id.dialog_editViews_next);
        dialog_editViews_next.setOnClickListener(v -> {
            Date next = dateList.stream().filter(date -> date.after(currentDate)).findFirst().orElse(null);
            if (next != null) {
                calendarView.setCurrentDate(next);
                ((TextView) layout.findViewById(R.id.fragmentCalender_month)).setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(next));
                currentDate = next;
                setButtons(layout, 1, calendarView, list, customRecycler, allowEdit);
                loadVideoList(calendarView.getEvents(next), layout, customRecycler);
            }
        });

        dialog_editViews_previous.setAlpha(dateList.stream().anyMatch(date -> date.before(currentDate)) ? 1f : 0.5f);
        dialog_editViews_next.setAlpha(dateList.stream().anyMatch(date -> date.after(currentDate)) ? 1f : 0.5f);

        OnSwipeTouchListener touchListener = new OnSwipeTouchListener(layout.getContext()) {
            @Override
            public boolean onSwipeRight() {
                dialog_editViews_previous.callOnClick();
                return true;
            }

            @Override
            public boolean onSwipeLeft() {
                dialog_editViews_next.callOnClick();
                return true;
            }
        };
        layout.findViewById(R.id.fragmentCalender_viewLayout).setOnTouchListener(touchListener);
        customRecycler.getRecycler().setOnTouchListener(touchListener);
        layout.findViewById(R.id.fragmentCalender_noViews).setOnTouchListener(touchListener);
    }

    /** <------------------------- Calender ------------------------- */


    public static class OnSwipeTouchListener implements View.OnTouchListener {
        private boolean isMultiTouch;
        private final GestureDetector gestureDetector;
//        Runnable cancelTouch;


        public OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            cancelTouch = () -> {
//                event.setAction(MotionEvent.ACTION_CANCEL);
//                v.onTouchEvent(event);
//            };
            if (event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN) isMultiTouch = true;
            boolean result = gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) isMultiTouch = false;
            return result;
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 200;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return true;
//            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isMultiTouch)
                    return false;
                boolean result = false;
                try {
                    float diffY = e2.getRawY() - e1.getRawY();
                    float diffX = e2.getRawX() - e1.getRawX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                result = onSwipeRight();
                            } else {
                                result = onSwipeLeft();
                            }
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            result = onSwipeBottom();
                        } else {
                            result = onSwipeTop();
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
//                if (!result && cancelTouch != null)
//                    cancelTouch.run();
                return result;
            }
        }

        public boolean onSwipeRight() {
            return false;
        }

        public boolean onSwipeLeft() {
            return false;
        }

        public boolean onSwipeTop() {
            return false;
        }

        public boolean onSwipeBottom() {
            return false;
        }
    }


    /** ------------------------- Checks -------------------------> */
    public static final String urlPattern = "(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$";

    public static boolean isUrl(String text) {
        return text.matches(urlPattern);
    }

    public static boolean isImdbId(String imdbId) {
        if (imdbId == null) return false;
        return imdbId.matches(imdbPattern_full);
    }

    public static final String imdbPattern_full = "^tt\\d{7,8}$";
    public static final String imdbPattern = "tt\\d{7,8}";

    public static boolean isUuid(String id) {
        return id.matches("\\w+_[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");
    }

    public static boolean isPortrait(AppCompatActivity activity) {
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    /**  <------------------------- Checks -------------------------  */


    /** ------------------------- Text -------------------------> */
    public static String removeTrailingZeros(double d) {
        return removeTrailingZeros(String.valueOf(d));
    }

    public static String removeTrailingZeros(String source, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(source);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group(0);
            matcher.appendReplacement(buffer, removeTrailingZeros(match));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String removeTrailingZeros(String s) {
        return (s.contains(".") || s.contains(",")) ? s.replaceAll("0*$", "").replaceAll("[,.]$", "") : s;
    }

    public static Pair<Integer, Integer> getTextWithAndHeight(Context context, String text, int size, int... typefaces) {
        TextView textView = new TextView(context);
        textView.setTextSize(size);
        for (int typeface : typefaces)
            textView.setTypeface(textView.getTypeface(), typeface);
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();
        int height = bounds.height();
        return Pair.create(width, height);
    }

    public static String getEllipsedString(Context context, String text, int maxWidth_px, int size, int... typefaces) {
        TextView textView = new TextView(context);
        textView.setTextSize(size);
        for (int typeface : typefaces)
            textView.setTypeface(textView.getTypeface(), typeface);
        Paint textPaint = textView.getPaint();
        for (int i = 0; i < text.length(); i++) {
            Rect bounds = new Rect();
            String sub = Utility.subString(text, 0, i == 0 ? text.length() : -i) + (i == 0 ? "" : "…");
            textPaint.getTextBounds(sub, 0, sub.length(), bounds);
            int width = bounds.width();
            if (width < maxWidth_px)
                return sub;
        }
        return "";
    }

    public static String subString(String text, int start, int ende) {
        if (start < 0)
            start = text.length() + start;
        if (ende < 0)
            ende = text.length() + ende;
        return text.substring(start, ende);
    }

    public static String subString(String text, int start) {
        if (start < 0)
            start = text.length() + start;
        return text.substring(start);
    }

    public static String stringReplace(String source, int start, int end, String replacement) {
        return source.substring(0, start) + replacement + source.substring(end);
    }

    public static String formatDuration(Duration duration, @Nullable String format) {
        if (format == null)
            format = "'%j% Jahr§e§~, ~''%w% Woche§n§~, ~''%d% Tag§e§~, ~''%h% Stunde§n§~, ~''%m% Minute§n§~, ~''%s% Sekunde§n§~, ~'";
        com.finn.androidUtilities.CustomList<Pair<String, Integer>> patternList = new com.finn.androidUtilities.CustomList<>(Pair.create("%j%", 31557600), Pair.create("%w%", 604800), Pair.create("%d%", 86400), Pair.create("%h%", 3600), Pair.create("%m%", 60), Pair.create("%s%", 1));
        int seconds = (int) (duration.toMillis() / 1000);
        while (true) {
            Matcher segments = Pattern.compile("'.+?'").matcher(format);
            if (!segments.find())
                break;
            String segment = segments.group();
            Iterator<Pair<String, Integer>> iterator = patternList.iterator();
            while (iterator.hasNext()) {
                Pair<String, Integer> pair = iterator.next();
                if (segment.contains(pair.first)) {
                    int amount = seconds / pair.second;
                    if (amount > 0) {
                        seconds = seconds % pair.second;
                        Matcher matcher = Pattern.compile(pair.first).matcher(segment);
                        String replacement = matcher.replaceFirst(String.valueOf(amount));
                        if (replacement.contains("§")) {
                            Matcher removePlural = Pattern.compile("§\\w+?§").matcher(replacement);
                            if (removePlural.find())
                                replacement = removePlural.replaceFirst(amount > 1 ? Utility.subString(removePlural.group(), 1, -1) : "");
                        }
                        format = segments.replaceFirst(Utility.subString(replacement, 1, -1));
                    } else
                        format = segments.replaceFirst("");

                    patternList.remove(pair);
                    break;
                }
            }
        }

        if (format.contains("~")) {
            while (true) {
                Matcher segments = Pattern.compile("~.+?~").matcher(format);
                if (!segments.find())
                    break;

                int start = segments.start();
                int end = segments.end();
                String replacement = Utility.subString(segments.group(), 1, -1);

                format = Utility.stringReplace(format, start, end, segments.find() ? replacement : "");
            }
        }

        return format;
    }

    @NotNull
    public static StringBuffer getTextFromUri(AppCompatActivity activity, Uri uri) {
        StringBuffer buf = new StringBuffer();
        try {
            String str = "";
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            if (inputStream != null) {
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                }
            }
        } catch (IOException e) {
            Toast.makeText(activity, "Fehler", Toast.LENGTH_SHORT).show();
        }
        return buf;
    }
    /**  <------------------------- Text -------------------------  */


    /** ------------------------- Time -------------------------> */
    public static Date removeTime(Date date) {
        return new Date(date.getYear(), date.getMonth(), date.getDate());
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//        return cal.getTime();
    }

    public static Date removeMilliseconds(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date shiftTime(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();

    }

    public static boolean isUpcoming(Date date) {
        if (date == null)
            return false;
        return new Date().before(date);
    }

    public static long getTimezoneOffsetMillis() {
        return Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET);
    }
    /**  <------------------------- Time -------------------------  */


    /** ------------------------- Toast -------------------------> */
    public static Toast centeredToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        if (toastView == null) {
            if (!CustomUtility.stringExists(text))
                text = " ";
            Spannable centeredText = new SpannableString(text);
            centeredText.setSpan(
                    new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    0, text.length() - 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            );
            toast = Toast.makeText(context, centeredText, Toast.LENGTH_SHORT);
        } else {
            TextView v = toastView.findViewById(android.R.id.message);
            if (v != null) v.setGravity(Gravity.CENTER);
        }
        return toast;
    }

    public static void showCenteredToast(Context context, String text) {
        centeredToast(context, text).show();
    }

    public static void showOnClickToast(Context context, String text, View.OnClickListener onClickListener) {
        Toast toast = centeredToast(context, text);
        View toastView = toast.getView();
        if (toastView == null)
            return;
        TextView view = toastView.findViewById(android.R.id.message);
        if (view != null) view.setOnClickListener(onClickListener);
        toast.show();
    }
    /**  <------------------------- Toast -------------------------  */


    /** ------------------------- EditItem -------------------------> */
    public static CustomDialog showEditItemDialog(Context context, List<String> preSelectedUuidList, CategoriesActivity.CATEGORIES category, CustomUtility.DoubleGenericInterface<CustomDialog, List<String>> onSaved) {
        return showEditItemDialog(context, preSelectedUuidList, category, null, onSaved, null, null, null, null);
    }

    public static CustomDialog showEditItemDialog(Context context, List<String> preSelectedUuidList, CategoriesActivity.CATEGORIES category, @Nullable CustomUtility.GenericReturnOnlyInterface<List<? extends ParentClass>> getAllObjectsList, CustomUtility.DoubleGenericInterface<CustomDialog, List<String>> onSaved, @Nullable CustomUtility.GenericReturnInterface<String, ParentClass> getItemById, @Nullable CustomUtility.DoubleGenericReturnInterface<String, String, ParentClass> saveNew, @Nullable TripleGenericInterface<CustomDialog, CustomRecycler<ParentClass>, ParentClass> onClick, @Nullable CustomUtility.TripleGenericInterface<CustomDialog, CustomRecycler<ParentClass>, ParentClass> onLongClick) {
        Database database = Database.getInstance();

        if (preSelectedUuidList == null)
            preSelectedUuidList = new ArrayList<>();

        List<String> selectedUuidList = new ArrayList<>(preSelectedUuidList);
        List<ParentClass> allObjectsList;
        final String[] searchQuery = {""};

        allObjectsList = getAllObjectsList != null ? (List<ParentClass>) getAllObjectsList.run() : new ArrayList<>(getMapFromDatabase(category).values());

        int saveButtonId = View.generateViewId();

        CustomDialog dialog_AddActorOrGenre = CustomDialog.Builder(context)
                .setTitle(category.getPlural() + (onClick == null ? " Bearbeiten" : ""))
                .setView(R.layout.dialog_edit_item)
                .setDimensionsFullscreen()
                .disableScroll()
                .addOptionalModifications(customDialog0 -> {
                    if (CustomUtility.boolOr(category, CategoriesActivity.CATEGORIES.VIDEO, CategoriesActivity.CATEGORIES.SHOW))
                        return;
                    customDialog0
                            .addButton(R.drawable.ic_add, customDialog -> {
                                showEditEditItemDialog(context, category, getAllObjectsList, onSaved, getItemById, saveNew, onClick, onLongClick, database, selectedUuidList, allObjectsList, customDialog);

                            }, false)
                            .alignPreviousButtonsLeft();
                })
                .addOptionalModifications(customDialog -> {
                    if (onClick == null) {
                        customDialog
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                    onSaved.run(customDialog1, selectedUuidList);
                                }, saveButtonId);
                    } else
                        customDialog.setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.BACK);
                })
                .show();

        SearchView searchView = dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_search);
        TextView emptyTextView = dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_empty);

        if (onClick != null) {
            dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectedCategories_layout).setVisibility(View.GONE);
            dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectedDivider).setVisibility(View.GONE);
        }

        CustomRecycler<ParentClass> customRecycler_selectList = new CustomRecycler<>((AppCompatActivity) context, dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectCategories));

        CustomRecycler<String> customRecycler_selectedList = new CustomRecycler<String>((AppCompatActivity) context, dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_selectedCategories))
                .setItemLayout(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .enableDragAndDrop((customRecycler, objectList) -> {
                })
                .setSetItemContent((customRecycler, itemView, uuid, index) -> {
                    ParentClass parentClass = getItemById != null ? getItemById.run(uuid) : getObjectFromDatabase(category, uuid);
                    String imagePath;
                    if ((parentClass instanceof ParentClass_Image_I && CustomUtility.stringExists(imagePath = ((ParentClass_Image_I) parentClass).getImagePath())) || (parentClass instanceof Video) && CustomUtility.stringExists(imagePath = ((Video) parentClass).getImagePath())) {
                        ImageView imageView = itemView.findViewById(R.id.list_bubble_image);
                        loadUrlIntoImageView(context, imageView, ImageCropUtility.applyCropTransformation(parentClass), getTmdbImagePath_ifNecessary(imagePath, false), null, null, () -> Utility.roundImageView(imageView, 3));
                        imageView.setVisibility(View.VISIBLE);
                    } else
                        itemView.findViewById(R.id.list_bubble_image).setVisibility(View.GONE);
                    ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(CustomUtility.getEllipsedString(parentClass.getName(), 15));
                    dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((customRecycler, view, object, index) -> Toast.makeText(context, "Swipe nach Oben zum abwählen", Toast.LENGTH_SHORT).show())
                .enableSwiping((customRecycler, objectList, direction, s, index) -> {
                    customRecycler_selectList.reload();
                    if (selectedUuidList.isEmpty())
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.VISIBLE);
                }, true, false)
                .generate();


        customRecycler_selectList
                .setItemLayout(R.layout.list_item_select)
                .setGetActiveObjectList(customRecycler -> {
                    List<ParentClass> resultList;
                    if (searchQuery[0].equals("")) {
                        allObjectsList.sort(ParentClass::compareByName);
                        resultList = allObjectsList;
                    } else
                        resultList = allObjectsList.stream().filter(parentClass -> ParentClass_Alias.containsQuery(parentClass, searchQuery[0]))
                                .sorted(ParentClass::compareByName).collect(Collectors.toList());

                    if (resultList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText(String.format("Keine %s %s", category.getPlural(), allObjectsList.isEmpty() ? "hinzugefügt" : "für diese Suche"));
                    } else
                        emptyTextView.setVisibility(View.GONE);

                    return resultList;
                })
                .enableDivider(12)
                .setSetItemContent((customRecycler, itemView, parentClass, index) -> {
                    ImageView thumbnail = itemView.findViewById(R.id.selectList_thumbnail);
                    String imagePath;
                    if (parentClass instanceof ParentClass_Image_I) {
                        if ((parentClass instanceof ParentClass_Image_I && CustomUtility.stringExists(imagePath = ((ParentClass_Image_I) parentClass).getImagePath()))
                                || (parentClass instanceof Video) && CustomUtility.stringExists(imagePath = ((Video) parentClass).getImagePath())) {
                            Utility.loadUrlIntoImageView(context, thumbnail, ImageCropUtility.applyCropTransformation(parentClass), getTmdbImagePath_ifNecessary(imagePath, false),
                                    getTmdbImagePath_ifNecessary(imagePath, true), null, () -> roundImageView(thumbnail, 4), searchView::clearFocus);
                            thumbnail.setVisibility(View.VISIBLE);
                        } else
                            thumbnail.setVisibility(View.INVISIBLE);
                    } else
                        thumbnail.setVisibility(View.GONE);

                    ((TextView) itemView.findViewById(R.id.selectList_name)).setText(parentClass.getName());

                    if (onClick == null) {
                        ((CheckBox) itemView.findViewById(R.id.selectList_selected)).setChecked(selectedUuidList.contains(parentClass.getUuid()));
                    } else {
                        itemView.findViewById(R.id.selectList_selected).setVisibility(View.GONE);
                    }
                })
                .setOnClickListener((customRecycler, view, parentClass, index) -> {
                    if (onClick != null) {
                        onClick.run(dialog_AddActorOrGenre, customRecycler, parentClass);
                        return;
                    }
                    CheckBox checkBox = view.findViewById(R.id.selectList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedUuidList.contains(parentClass.getUuid()))
                        selectedUuidList.remove(parentClass.getUuid());
                    else
                        selectedUuidList.add(parentClass.getUuid());

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectedList.reload();
                })
                .addOptionalModifications(customRecycler -> {
                    if (onLongClick == null)
                        return;
                    customRecycler.setOnLongClickListener((customRecycler1, view, parentClass, index) -> onLongClick.run(dialog_AddActorOrGenre, customRecycler, parentClass));
                })
                .enableFastScroll((parentClassCustomRecycler, parentClass, integer) -> parentClass.getName().substring(0, 1).toUpperCase())
                .generate();

        searchView.setQueryHint(category.getPlural() + " durchsuchen");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchQuery[0] = s.trim();
                customRecycler_selectList.reload();

                return true;
            }
        });

        return dialog_AddActorOrGenre;
    }

    private static void showEditEditItemDialog(Context context, CategoriesActivity.CATEGORIES category, @Nullable CustomUtility.GenericReturnOnlyInterface<List<? extends ParentClass>> getAllObjectsList, CustomUtility.DoubleGenericInterface<CustomDialog, List<String>> onSaved, @Nullable CustomUtility.GenericReturnInterface<String, ParentClass> getItemById, @Nullable CustomUtility.DoubleGenericReturnInterface<String, String, ParentClass> saveNew, TripleGenericInterface<CustomDialog, CustomRecycler<ParentClass>, ParentClass> onClick, @Nullable CustomUtility.TripleGenericInterface<CustomDialog, CustomRecycler<ParentClass>, ParentClass> onLongClick, Database database, List<String> selectedUuidList, List<ParentClass> allObjectsList, CustomDialog customDialog) {
        CustomDialog.Builder(context)
                .setTitle(category.getSingular() + " Hinzufügen")
                .enableDynamicWrapHeight((AppCompatActivity) context)
                .enableAutoUpdateDynamicWrapHeight()
                .addOptionalModifications(customDialog1 -> {
                    if (ParentClass_Image_I.class.isAssignableFrom(category.getObjectClass())) {
                        customDialog1
                                .addButton("Testen", customDialog2 -> {
                                    String url = ((EditText) customDialog2.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                                    ImageView preview = customDialog2.findViewById(R.id.dialog_editTmdbCategory_preview);
                                    if (CustomUtility.stringExists(url)) {
                                        Utility.loadUrlIntoImageView(context, preview, Utility.getTmdbImagePath_ifNecessary(url, true), null);
                                        preview.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "Nichts eingegeben", Toast.LENGTH_SHORT).show();
                                        preview.setVisibility(View.GONE);
                                    }
                                }, false)
                                .alignPreviousButtonsLeft();
                    }
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                    String newName = ((EditText) customDialog1.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim();
                    String newImgUrl = ((EditText) customDialog1.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                    ParentClass newParentClass;

                    if (saveNew != null) {
                        newParentClass = saveNew.run(newName, newImgUrl);
                    } else {
                        newParentClass = ParentClass.newCategory(category, "");

                        ParentClass_Alias.applyNameAndAlias(newParentClass, newName);
                        if (ParentClass_Image_I.class.isAssignableFrom(category.getObjectClass())) {
                            ((ParentClass_Image_I) newParentClass).setImagePath(CustomUtility.stringExists(newImgUrl) ? newImgUrl : null);
                        }

                        switch (category) {
                            case DARSTELLER:
                                database.darstellerMap.put(newParentClass.getUuid(), (Darsteller) newParentClass);
                                break;
                            case STUDIOS:
                                database.studioMap.put(newParentClass.getUuid(), (Studio) newParentClass);
                                break;
                            case GENRE:
                                database.genreMap.put(newParentClass.getUuid(), (Genre) newParentClass);
                                break;
                            case KNOWLEDGE_CATEGORIES:
                                database.knowledgeCategoryMap.put(newParentClass.getUuid(), (KnowledgeCategory) newParentClass);
                                break;
                            case JOKE_CATEGORIES:
                                database.jokeCategoryMap.put(newParentClass.getUuid(), (JokeCategory) newParentClass);
                                break;
                            case SHOW_GENRES:
                                database.showGenreMap.put(newParentClass.getUuid(), (ShowGenre) newParentClass);
                                break;
                            case MEDIA_PERSON:
                                database.mediaPersonMap.put(newParentClass.getUuid(), (MediaPerson) newParentClass);
                                break;
                            case MEDIA_TAG:
                                database.mediaTagMap.put(newParentClass.getUuid(), (MediaTag) newParentClass);
                                break;
                        }
                    }


                    selectedUuidList.add(newParentClass.getUuid());
                    customDialog1.dismiss();
                    customDialog.dismiss();
                    showEditItemDialog(context, selectedUuidList, category, getAllObjectsList, onSaved, getItemById, saveNew, onClick, onLongClick);
                    Database.saveAll();
                })
                .setView(R.layout.dialog_edit_tmdb_category)
                .setSetViewContent((customDialog1, view1, reload) -> {

                    TextInputLayout dialog_editTmdbCategory_name_layout = view1.findViewById(R.id.dialog_editTmdbCategory_name_layout);
                    dialog_editTmdbCategory_name_layout.setHint(category.getSingular() + "-Name");
                    String searchText = ((SearchView) customDialog.findViewById(R.id.dialogEditCategory_search)).getQuery().toString();
                    EditText dialog_editTmdbCategory_name = dialog_editTmdbCategory_name_layout.getEditText();

                    Helpers.TextInputHelper helper = new Helpers.TextInputHelper(dialog_editTmdbCategory_name_layout)
                            .defaultDialogValidation(customDialog1)
                            .setValidation(dialog_editTmdbCategory_name_layout, (validator, text) -> {
                                if (allObjectsList.stream().anyMatch(parentClass -> parentClass.getName().equalsIgnoreCase(text)))
                                    validator.setInvalid(category.getSingular() + " bereits vorhanden");
                            });
//                                            helper.validate();
                    if (CustomUtility.stringExists(searchText))
                        dialog_editTmdbCategory_name.setText(searchText);
                    else
                        helper.validate((TextInputLayout[]) null);
                    dialog_editTmdbCategory_name.requestFocus();

                    if (ParentClass_Alias.class.isAssignableFrom(category.getObjectClass()))
                        dialog_editTmdbCategory_name.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);

                    if (ParentClass_Image_I.class.isAssignableFrom(category.getObjectClass())) {
                        CustomUtility.setMargins(view1.findViewById(R.id.dialog_editTmdbCategory_nameLayout), -1, -1, -1, 0);
                        view1.findViewById(R.id.dialog_editTmdbCategory_urlLayout).setVisibility(View.VISIBLE);
                        TextInputLayout dialog_editTmdbCategory_url_layout = view1.findViewById(R.id.dialog_editTmdbCategory_url_layout);
//                                                dialog_editTmdbCategory_url_layout.getEditText().setText(((ParentClass_Image_I) newParentClass).getImagePath());
                        helper.addValidator(dialog_editTmdbCategory_url_layout).setValidation(dialog_editTmdbCategory_url_layout, (validator, text) -> {
                            validator.asWhiteList();
                            if (text.isEmpty() || text.matches(CategoriesActivity.pictureRegexAll) || text.matches(ActivityResultHelper.uriRegex))
                                validator.setValid();
                            if (text.toLowerCase().contains("http") && !text.toLowerCase().contains("https"))
                                validator.setInvalid("Die URL muss 'https' sein!");
                        });

                        view1.findViewById(R.id.dialog_editTmdbCategory_localStorage).setOnClickListener(v -> {
                            ActivityResultHelper.addFileChooserRequest((AppCompatActivity) context, "image/*", o1 -> {
                                String path = ActivityResultHelper.getPath(context, ((Intent) o1).getData());
                                dialog_editTmdbCategory_url_layout.getEditText().setText(path);
                            });
                        });

                    }
                })
                .show();
    }

    public static CustomDialog showEditTreeItemDialog(Context context, List<String> preSelectedIdList, GenericInterface<List<String>> onSaved, CategoriesActivity.CATEGORIES category) {
        com.finn.androidUtilities.CustomList<String> selectedIds = new com.finn.androidUtilities.CustomList<>(preSelectedIdList);
        String categoryName = category.getPlural();
        String[] searchQuery = {""};

        return CustomDialog.Builder(context)
                .setTitle(categoryName + " Bearbeiten")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .setView(R.layout.dialog_edit_item)
                .setDimensionsFullscreen()
                .disableScroll()
                .setSetViewContent((customDialog, view, reload) -> {
                    FrameLayout editCategoryLayout = view.findViewById(R.id.dialogEditCategory_selectLayout);
                    TextView emptyTextView = view.findViewById(R.id.dialogEditCategory_empty);

                    // vvvvvvvvvvvvvvv Selected
                    CustomRecycler<String> selectedRecycler = new CustomRecycler<>((AppCompatActivity) context, view.findViewById(R.id.dialogEditCategory_selectedCategories));
                    final Runnable updateSelectedRecycler = () -> {
                        view.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(selectedIds.isEmpty() ? View.VISIBLE : View.GONE);
                        selectedRecycler.reload();
                    };
                    Comparator<ParentClass> treeComparator = (parentClass1, parentClass2) -> ((ParentClass) parentClass1).getName().compareTo(((ParentClass) parentClass2).getName());

                    selectedRecycler
                            .setItemLayout(R.layout.list_item_bubble)
                            .setObjectList(selectedIds)
                            .enableDragAndDrop((customRecycler, objectList) -> {
                            })
                            .setSetItemContent((customRecycler, itemView, uuid, index) -> {
                                ((TextView) itemView.findViewById(R.id.list_bubble_name)).setText(ParentClass_Tree.findObjectById(category, uuid).getName());
                                view.findViewById(R.id.dialogEditCategory_nothingSelected).setVisibility(View.GONE);
                            })
                            .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                            .setOnClickListener((customRecycler, view1, object, index) -> {
                                Toast.makeText(context,
                                        "Swipe nach Oben zum abwählen", Toast.LENGTH_SHORT).show();
                            })
                            .enableSwiping((customRecycler, objectList, direction, s, index) -> {
                                ParentClass_Tree.buildTreeView(editCategoryLayout, category, selectedIds, searchQuery[0], updateSelectedRecycler, treeComparator, emptyTextView, null, null);
                            }, true, false)
                            .generate();

                    // vvvvvvvvvvvvvvv Tree
                    ParentClass_Tree.buildTreeView(editCategoryLayout, category, selectedIds, searchQuery[0], updateSelectedRecycler, treeComparator, emptyTextView, null, null);

                    // vvvvvvvvvvvvvvv Search
                    SearchView searchView = view.findViewById(R.id.dialogEditCategory_search);
                    searchView.setQueryHint(category.getPlural() + " durchsuchen");
                    searchView.requestFocus();
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            searchQuery[0] = s.trim();
                            ParentClass_Tree.buildTreeView(editCategoryLayout, category, selectedIds, searchQuery[0], updateSelectedRecycler, treeComparator, emptyTextView, null, null);
                            return true;
                        }
                    });

                    // vvvvvvvvvvvvvvv Add
                    customDialog.getButtonByType(CustomDialog.BUTTON_TYPE.ADD_BUTTON)
                            .getButton().setOnClickListener(v -> {
                                ParentClass_Tree.addNew(context, null, searchQuery[0], category, newObject -> {
                                    selectedIds.add(((ParentClass) newObject).getUuid());
                                    updateSelectedRecycler.run();
                                    ParentClass_Tree.buildTreeView(editCategoryLayout, category, selectedIds, searchQuery[0], updateSelectedRecycler, treeComparator, emptyTextView, null, null);
                                });
                            });
                })
                .addButton(CustomDialog.BUTTON_TYPE.ADD_BUTTON)
                .transformLastAddedButtonToImageButton()
                .alignPreviousButtonsLeft()
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    onSaved.run(selectedIds);
                })
                .show();
    }
    /**  <------------------------- EditItem -------------------------  */


    /** <------------------------- Objects from Database ------------------------- */
    public static ParentClass getObjectFromDatabase(CategoriesActivity.CATEGORIES category, String uuid) {
        switch (category) {
            case MEDIA_CATEGORY:
                return (ParentClass) ParentClass_Tree.findObjectById(category, uuid);
            default:
                return getMapFromDatabase(category).get(uuid);
        }
    }

    public static Map<String, ? extends ParentClass> getMapFromDatabase(CategoriesActivity.CATEGORIES category) {
        Database database = Database.getInstance();
        switch (category) {
            case VIDEO:
                return database.videoMap;
            case DARSTELLER:
                return database.darstellerMap;
            case STUDIOS:
                return database.studioMap;
            case GENRE:
                return database.genreMap;
            case PERSON:
                return database.personMap;
            case WATCH_LIST:
                return database.watchListMap;
            case CUSTOM_CODE_VIDEO:
                return database.customCodeVideoMap;
            case SHOW:
                return database.showMap;
            case KNOWLEDGE_CATEGORIES:
                return database.knowledgeCategoryMap;
            case JOKE_CATEGORIES:
                return database.jokeCategoryMap;
            case SHOW_GENRES:
                return database.showGenreMap;
            case COLLECTION:
                return database.collectionMap; // von videoMap geändert
            case MEDIA:
                return database.mediaMap;
            case MEDIA_PERSON:
                return database.mediaPersonMap;
            case MEDIA_CATEGORY:
                return database.mediaCategoryMap;
            case MEDIA_TAG:
                return database.mediaTagMap;
            case MEDIA_EVENT:
                return database.mediaEventMap;
        }
        return null;
    }

    public static ParentClass findObjectByName(CategoriesActivity.CATEGORIES category, String name) {
        return findObjectByName(category, name, false);
    }

    public static ParentClass findObjectByName(CategoriesActivity.CATEGORIES category, String name, boolean ignoreCase) {
        switch (category) {
            case MEDIA_CATEGORY:
                return (ParentClass) ParentClass_Tree.findObjectByName(category, name, ignoreCase);
            default:
                return getMapFromDatabase(category).values().stream().filter(parentClass -> ParentClass_Alias.equalsQuery(parentClass, name, ignoreCase)).findFirst().orElse(null);
        }
    }

    public static ParentClass findObjectById(CategoriesActivity.CATEGORIES category, String id) {
        switch (category) {
            case MEDIA_CATEGORY:
                return ParentClass_Tree.findObjectById(category, id);
            default:
                return getMapFromDatabase(category).values().stream().filter(parentClass -> parentClass.getUuid().equals(id)).findFirst().orElse(null);
        }
    }

    public static com.finn.androidUtilities.CustomList<? extends ParentClass> findAllObjectById(CategoriesActivity.CATEGORIES category, Collection<String> ids) {
        return ids.stream().map(id -> findObjectById(category, id)).collect(Collectors.toCollection(com.finn.androidUtilities.CustomList::new));
    }

    /** ------------------------- Objects from Database -------------------------> */


    public static class Triple<A, B, C> {
        public A first;
        public B second;
        public C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public A getFirst() {
            return first;
        }

        public Triple<A, B, C> setFirst(A first) {
            this.first = first;
            return this;
        }

        public B getSecond() {
            return second;
        }

        public Triple<A, B, C> setSecond(B second) {
            this.second = second;
            return this;
        }

        public C getThird() {
            return third;
        }

        public Triple<A, B, C> setThird(C third) {
            this.third = third;
            return this;
        }
    }

    public static class Quadruple<A, B, C, D> {
        public A first;
        public B second;
        public C third;
        public D fourth;

        public Quadruple(A first, B second, C third, D fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        public A getFirst() {
            return first;
        }

        public Quadruple<A, B, C, D> setFirst(A first) {
            this.first = first;
            return this;
        }

        public B getSecond() {
            return second;
        }

        public Quadruple<A, B, C, D> setSecond(B second) {
            this.second = second;
            return this;
        }

        public C getThird() {
            return third;
        }

        public Quadruple<A, B, C, D> setThird(C third) {
            this.third = third;
            return this;
        }

        public D getFourth() {
            return fourth;
        }

        public Quadruple<A, B, C, D> setFourth(D fourth) {
            this.fourth = fourth;
            return this;
        }
    }


    /** ------------------------- Pixels -------------------------> */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void setMargins(View v, int links, int oben, int rechts, int unten) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(dpToPx(links), dpToPx(oben), dpToPx(rechts), dpToPx(unten));
            v.requestLayout();
        }
    }

    public static Pair<Integer, Integer> getScreenSize(AppCompatActivity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int width = size.x;
        int height = size.y;
        return Pair.create(width, height);
    }

    public static int getScreenAvailableWidth(AppCompatActivity context) {
        Integer fullWidth = getScreenSize(context).first;
        boolean isPortrait = Utility.isPortrait(context);
        return fullWidth - (isPortrait ? 0 : 100);
    }
    /**  <------------------------- Pixels -------------------------  */


    /**  ------------------------- Advanced Search ------------------------->  */
//    public static CustomDialog showAdvancedSearchDialog(Context context, SearchView searchView, Collection<? extends ParentClass_Ratable> ratables) {
//        boolean preSelected = false;
//        /**  ------------------------- Rating ------------------------->  */
//        boolean[] singleMode = {false};
//        final int[] min = {0};
//        final int[] max = {20};
//        /**  <------------------------- Rating -------------------------  */
//
//
//        /**  ------------------------- Watched ------------------------->  */
//        final Date[] from = {null};
//        final Date[] to = {null};
//
//        // ---------------
//
//        final String[] pivot = {""};
//        final String[] duration = {""};
//        /**  <------------------------- Watched -------------------------  */
//
//        /**  ------------------------- Length ------------------------->  */
//        final Integer[] minLength = {null};
//        final Integer[] maxLength = {null};
//        /**  <------------------------- Length -------------------------  */
//
//        AdvancedQueryHelper advancedQueryHelper = AdvancedQueryHelper.getAdvancedQuery(searchView.getQuery().toString());
//        if (advancedQueryHelper.hasAdvancedSearch()) {
//            preSelected = true;
//
//            if (advancedQueryHelper.hasRatingSub()) {
//                Pair<Float, Float> ratingMinMax = advancedQueryHelper.getRatingMinMax();
//                min[0] = Math.round(ratingMinMax.first * 4);
//                max[0] = Math.round(ratingMinMax.second * 4);
//                singleMode[0] = ratingMinMax.first.equals(ratingMinMax.second);
//            }
//
//            // ---------------
//
//            if (advancedQueryHelper.hasDateSub()) {
//                Pair<Date, Date> dateMinMax = advancedQueryHelper.getDateMinMax();
//                from[0] = dateMinMax.first;
//                to[0] = dateMinMax.second;
//            }
//
//            // ---------------
//
//            if (advancedQueryHelper.hasDurationSub()) {
//                String[] split = advancedQueryHelper.durationSub.split(";");
//                if (split.length == 1) {
//                    duration[0] = split[0];
//                } else {
//                    pivot[0] = split[0];
//                    duration[0] = split[1];
//                }
//            }
//
//            // ---------------
//
//            if (advancedQueryHelper.hasLengthSub()) {
//                Pair<Integer, Integer> lengthMinMax = advancedQueryHelper.getLengthMinMax();
//                minLength[0] = lengthMinMax.first;
//                maxLength[0] = lengthMinMax.second;
//            }
//        }
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
//        boolean finalPreSelected = preSelected;
//        int timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000;
//
//        return CustomDialog.Builder(context)
//                .setTitle("Erweiterte Suche")
//                .setView(R.layout.dialog_advanced_search_video)
//                .setSetViewContent((customDialog, view, reload) -> {
//                    final Runnable[] applyStrings = {() -> {
//                    }};
//
//                    /**  ------------------------- Rating ------------------------->  */
//                    TextView rangeText = customDialog.findViewById(R.id.dialog_advancedSearch_video_range);
//                    RangeSeekBar rangeBar = customDialog.findViewById(R.id.dialog_advancedSearch_video_rangeBar);
//                    SeekBar singleBar = customDialog.findViewById(R.id.dialog_advancedSearch_video_singleBar);
//                    CustomUtility.GenericInterface<Pair<Integer, Integer>> setText = pair -> {
//                        singleBar.setEnabled(singleMode[0]);
//                        if (singleMode[0])
//                            rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆", pair.first / 4d));
//                        else
//                            rangeText.setText(String.format(Locale.getDefault(), "%.2f ☆ – %.2f ☆", pair.first / 4d, pair.second / 4d));
//                    };
//                    rangeBar.setVisibility(singleMode[0] ? View.INVISIBLE : View.VISIBLE);
//                    singleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                        @Override
//                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                            if (fromUser) {
//                                rangeBar.setMinThumbValue(progress);
//                                setText.run(Pair.create(progress, progress));
//                            }
//                        }
//
//                        @Override
//                        public void onStartTrackingTouch(SeekBar seekBar) {
//
//                        }
//
//                        @Override
//                        public void onStopTrackingTouch(SeekBar seekBar) {
//
//                        }
//                    });
//                    rangeText.setOnClickListener(v -> {
//                        if (singleMode[0] && singleBar.getProgress() == 20) {
//                            singleBar.setProgress(19);
//                            rangeBar.setMaxThumbValue(20);
//                        }
//                        singleMode[0] = !singleMode[0];
//                        rangeBar.setVisibility(singleMode[0] ? View.INVISIBLE : View.VISIBLE);
//                        setText.run(Pair.create(rangeBar.getMinThumbValue(), rangeBar.getMaxThumbValue()));
//                    });
//                    singleBar.setProgress(min[0]);
//                    rangeBar.setMaxThumbValue(max[0]);
//                    rangeBar.setMinThumbValue(min[0]);
//                    setText.run(Pair.create(min[0], max[0]));
//
//                    rangeBar.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
//                        @Override
//                        public void onStartedSeeking() {
//
//                        }
//
//                        @Override
//                        public void onStoppedSeeking() {
//                        }
//
//                        @Override
//                        public void onValueChanged(int min, int max) {
//                            setText.run(Pair.create(min, max));
//                            singleBar.setProgress(min);
//                        }
//                    });
//                    /**  <------------------------- Rating -------------------------  */
//
//
//                    /**  ------------------------- DateRange ------------------------->  */
//                    TextView dialog_advancedSearch_viewed_text = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_text);
//                    Runnable setDateRangeTextView = () -> {
//                        if (from[0] != null && to[0] != null) {
//                            dialog_advancedSearch_viewed_text.setText(String.format("%s - %s", dateFormat.format(from[0]), dateFormat.format(to[0])));
//                        } else if (from[0] != null) {
//                            dialog_advancedSearch_viewed_text.setText(dateFormat.format(from[0]));
//                        } else {
//                            dialog_advancedSearch_viewed_text.setText("Nicht ausgewählt");
//                        }
//                    };
//                    setDateRangeTextView.run();
//
//                    MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
//                    builder.setTitleText("Zeitraum Auswählen");
//                    if (from[0] != null && to[0] != null) {
//                        builder.setSelection(androidx.core.util.Pair.create(from[0].getTime() - timezoneOffset, to[0].getTime() - timezoneOffset));
//                    } else if (from[0] != null) {
//                        builder.setSelection(androidx.core.util.Pair.create(from[0].getTime() - timezoneOffset, from[0].getTime() - timezoneOffset));
//                    }
//                    MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
//
//                    picker.addOnPositiveButtonClickListener(selection -> {
//                        from[0] = new Date(selection.first + timezoneOffset);
//                        if (!Objects.equals(selection.first, selection.second))
//                            to[0] = new Date(selection.second + timezoneOffset);
//                        else
//                            to[0] = null;
//                        setDateRangeTextView.run();
//                        pivot[0] = "";
//                        duration[0] = "";
//                        applyStrings[0].run();
//                    });
//
//                    customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_change).setOnClickListener(v -> picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString()));
//                    Runnable resetDateRange = () -> {
//                        if (from[0] != null || to[0] != null) {
//                            from[0] = null;
//                            to[0] = null;
//                            setDateRangeTextView.run();
//                        }
//                    };
//
//                    customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_change).setOnLongClickListener(v -> {
//                        resetDateRange.run();
//                        return true;
//                    });
//                    /**  <------------------------- DateRange -------------------------  */
//
//
//                    /**  ------------------------- Duration ------------------------->  */
//                    TextInputEditText since_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_since_edit);
//                    Spinner since_unit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_since_unit);
//                    TextInputEditText duration_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_duration_edit);
//                    Spinner duration_unit = customDialog.findViewById(R.id.dialog_advancedSearch_video_viewed_duration_unit);
//                    Map<String, Integer> modeMap = new HashMap<>();
//                    modeMap.put("d", 0);
//                    modeMap.put("m", 1);
//                    modeMap.put("y", 2);
//                    modeMap.put("_m", 3);
//                    modeMap.put("_y", 4);
//
//                    applyStrings[0] = () -> {
//                        if (CustomUtility.stringExists(duration[0])) {
//                            duration_edit.setText(CustomUtility.subString(duration[0], 0, -1));
//                            String durationMode = CustomUtility.subString(duration[0], -1);
//                            duration_unit.setSelection(modeMap.get(durationMode));
//                        } else {
//                            if (!duration_edit.getText().toString().equals(""))
//                                duration_edit.setText("");
//                            if (duration_unit.getSelectedItemPosition() != 0)
//                                duration_unit.setSelection(0);
//                        }
//
//                        if (CustomUtility.stringExists(pivot[0])) {
//                            boolean floored = pivot[0].contains("_");
//                            since_edit.setText(CustomUtility.subString(pivot[0], floored ? 1 : 0, -1));
//                            String sinceMode = CustomUtility.subString(pivot[0], -1);
//                            since_unit.setSelection(modeMap.get((floored ? "_" : "") + sinceMode));
//                        } else {
//                            if (!since_edit.getText().toString().equals(""))
//                                since_edit.setText("");
//                            if (since_unit.getSelectedItemPosition() != 0)
//                                since_unit.setSelection(0);
//                        }
//                    };
//                    applyStrings[0].run();
//
//                    Runnable updateStrings = () -> {
//                        Set<String> since_keysByValue = getKeysByValue(modeMap, since_unit.getSelectedItemPosition());
//                        String since_mode = since_keysByValue.toArray(new String[0])[0];
//                        String since_text = since_edit.getText().toString();
//
//                        Set<String> duration_keysByValue = getKeysByValue(modeMap, duration_unit.getSelectedItemPosition());
//                        String duration_mode = duration_keysByValue.toArray(new String[0])[0];
//                        String duration_text = duration_edit.getText().toString();
//
//                        boolean sinceExists = CustomUtility.stringExists(since_text) || since_mode.contains("_");
//                        boolean durationExists = CustomUtility.stringExists(duration_text);
//
//                        if (durationExists) {
//                            duration[0] = (duration_mode.contains("_") ? "_" : "") + duration_text + duration_mode.replaceAll("_", "");
//                            if (sinceExists)
//                                pivot[0] = (since_mode.contains("_") ? "_" : "") + since_text + since_mode.replaceAll("_", "");
//                            else
//                                pivot[0] = "";
//                        } else {
//                            pivot[0] = "";
//                            duration[0] = "";
//                        }
//                    };
//
//                    since_edit.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (CustomUtility.stringExists(s.toString())) {
//                                resetDateRange.run();
//                            } else
//                                pivot[0] = "";
//
//                            updateStrings.run();
//                        }
//                    });
//                    duration_edit.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (CustomUtility.stringExists(s.toString()))
//                                resetDateRange.run();
//
//                            updateStrings.run();
//                        }
//                    });
//
//                    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            updateStrings.run();
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> parent) {
//
//                        }
//                    };
//                    since_unit.setOnItemSelectedListener(spinnerListener);
//                    duration_unit.setOnItemSelectedListener(spinnerListener);
//                    /**  <------------------------- Duration -------------------------  */
//
//
//                    /**  ------------------------- Length ------------------------->  */
//                    TextInputEditText minLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_length_min_edit);
//                    TextInputEditText maxLength_edit = customDialog.findViewById(R.id.dialog_advancedSearch_video_length_max_edit);
//
//                    if (minLength[0] != null) {
//                        minLength_edit.setText(CustomUtility.isNotValueReturnOrElse(minLength[0], -1, String::valueOf, integer -> null));
//                        maxLength_edit.setText(CustomUtility.isNotValueReturnOrElse(maxLength[0], -1, String::valueOf, integer -> null));
//                    }
//
//                    /**  <------------------------- Length -------------------------  */
//
//                    // ---------------
//                    /**/
//                    // --------------- Chart
//
////                    PieChart pieChart = customDialog.findViewById(R.id.dialog_filterByRating_chart);
////                    pieChart.setUsePercentValues(true);
////                    pieChart.getDescription().setEnabled(false);
////
//////                    pieChart.setCenterTextTypeface(tfLight);
//////                    pieChart.setCenterText(generateCenterSpannableText());
////
////                    pieChart.setDrawHoleEnabled(true);
////                    pieChart.setHoleColor(Color.WHITE);
////
////                    pieChart.setTransparentCircleColor(Color.WHITE);
////                    pieChart.setTransparentCircleAlpha(110);
////
////                    pieChart.setHoleRadius(58f);
////                    pieChart.setTransparentCircleRadius(61f);
////
////                    pieChart.setDrawCenterText(true);
////
////                    pieChart.setRotationEnabled(false);
////                    pieChart.setHighlightPerTapEnabled(true);
////
////                    pieChart.setMaxAngle(180f); // HALF CHART
////                    pieChart.setRotationAngle(180f);
////                    pieChart.setCenterTextOffset(0, -20);
////
////                    ArrayList<PieEntry> values = new ArrayList<>();
////                    String[] parties = new String[] {
////                            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
////                            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
////                            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
////                            "Party Y", "Party Z"
////                    };
////                    for (int i = 0; i < 4; i++) {
////                        values.add(new PieEntry((float) ((Math.random() * 100) + 100 / 5), parties[i % parties.length]));
////                    }
////
////                    PieDataSet dataSet = new PieDataSet(values, "Election Results");
////                    dataSet.setSliceSpace(3f);
////                    dataSet.setSelectionShift(5f);
////
////                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
////                    //dataSet.setSelectionShift(0f);
////
////                    PieData data = new PieData(dataSet);
////                    data.setValueFormatter(new PercentFormatter());
////                    data.setValueTextSize(11f);
////                    data.setValueTextColor(Color.WHITE);
//////                    data.setValueTypeface(tfLight);
////                    pieChart.setData(data);
////
////                    pieChart.invalidate();
////
////
////                    pieChart.animateY(1400, Easing.EaseInOutQuad);
////
////                    Legend l = pieChart.getLegend();
////                    l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
////                    l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
////                    l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
////                    l.setDrawInside(false);
////                    l.setXEntrySpace(7f);
////                    l.setYEntrySpace(0f);
////                    l.setYOffset(0f);
////
////                    // entry label styling
////                    pieChart.setEntryLabelColor(Color.WHITE);
//////                    pieChart.setEntryLabelTypeface(tfRegular);
////                    pieChart.setEntryLabelTextSize(12f); // ToDo: https://www.youtube.com/watch?v=iS7EgKnyDeY
//
//                    // --------------- Chart 2
//
////                    PieChart pieChart = customDialog.findViewById(R.id.dialog_filterByRating_chart);
////                    pieChart.setUsePercentValues(true);
////                    pieChart.getDescription().setEnabled(false);
////
//////                    pieChart.setCenterTextTypeface(tfLight);
//////                    pieChart.setCenterText(generateCenterSpannableText());
////
////                    pieChart.setDrawHoleEnabled(true);
////                    pieChart.setHoleColor(Color.TRANSPARENT);
////
////                    pieChart.setTransparentCircleColor(Color.WHITE);
////                    pieChart.setTransparentCircleAlpha(110);
////
////                    pieChart.setHoleRadius(58f);
////                    pieChart.setTransparentCircleRadius(61f);
////
////                    pieChart.setDrawCenterText(true);
////
//////                    pieChart.setRotationEnabled(false);
////                    pieChart.setHighlightPerTapEnabled(true);
////
//////                    pieChart.setMaxAngle(180f); // HALF CHART
//////                    pieChart.setRotationAngle(180f);
//////                    pieChart.setCenterTextOffset(0, -20);
////
////
////                    List<PieEntry> pieEntryList = new ArrayList<>();
////
////                    Map<Float, ? extends List<? extends ParentClass_Ratable>> map = ratables.stream().collect(Collectors.groupingBy(ParentClass_Ratable::getRating));
////
////                    for (int i = 1; i < 20; i++) {
////                        float rating = i / 4f;
////                        pieEntryList.add(new PieEntry(Utility.returnIfNull(map.get(rating), new ArrayList<ParentClass_Ratable>()).size(), Utility.removeTrailingZeros(i / 4d) + " ☆"));
////                    }
////
////                    PieDataSet dataSet = new PieDataSet(pieEntryList, "Filme Verteilung");
////                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
////                    PieData pieData = new PieData(dataSet);
////
////                    pieChart.setData(pieData);
////                    pieChart.invalidate();
//                })
//                .addOptionalModifications(customDialog -> {
//                    if (finalPreSelected) {
//                        customDialog
//                                .addButton(R.drawable.ic_reset, customDialog1 -> {
//                                    String removedQuery = AdvancedQueryHelper.removeAdvancedSearch(searchView.getQuery());
//                                    searchView.setQuery(removedQuery, false);
//                                    Toast.makeText(context, "Erweiterte Suche zurückgesetzt", Toast.LENGTH_SHORT).show();
//                                })
//                                .alignPreviousButtonsLeft();
//                    }
//                })
//                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
//                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
//                    List<String> filter = new ArrayList<>();
//                    String removedQuery = AdvancedQueryHelper.removeAdvancedSearch(searchView.getQuery());
//
//                    /**  ------------------------- Rating ------------------------->  */
//                    RangeSeekBar rangeBar = customDialog.findViewById(R.id.dialog_advancedSearch_video_rangeBar);
//                    min[0] = rangeBar.getMinThumbValue();
//                    max[0] = rangeBar.getMaxThumbValue();
//
//                    if (min[0] != 0 || max[0] != 20) {
//                        String ratingFilter;
//                        if (singleMode[0])
//                            ratingFilter = String.format(Locale.getDefault(), "r:%.2f", min[0] / 4d);
//                        else
//                            ratingFilter = String.format(Locale.getDefault(), "r:%.2f-%.2f", min[0] / 4d, max[0] / 4d);
//
//                        filter.add(ratingFilter);
//                    }
//                    /**  <------------------------- Rating -------------------------  */
//
//
//                    /**  ------------------------- DateRange ------------------------->  */
//                    if (from[0] != null) {
//                        String dateFilter;
//                        if (to[0] != null)
//                            dateFilter = String.format(Locale.getDefault(), "d:%s-%s", dateFormat.format(from[0]), dateFormat.format(to[0]));
//                        else
//                            dateFilter = String.format(Locale.getDefault(), "d:%s", dateFormat.format(from[0]));
//
//                        filter.add(dateFilter);
//                    }
//
//                    // ---------------
//
//                    if (CustomUtility.stringExists(duration[0])) {
//                        String dateDurationFilter;
//                        if (CustomUtility.stringExists(pivot[0]))
//                            dateDurationFilter = String.format(Locale.getDefault(), "d:%s;%s", pivot[0], duration[0]);
//                        else
//                            dateDurationFilter = String.format(Locale.getDefault(), "d:%s", duration[0]);
//                        filter.add(dateDurationFilter);
//                    }
//                    /**  <------------------------- DateRange -------------------------  */
//
//
//                    /**  ------------------------- Length ------------------------->  */
//                    String  minLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_video_length_min_edit)).getText().toString().trim();
//                    String maxLength_str = ((TextInputEditText) customDialog.findViewById(R.id.dialog_advancedSearch_video_length_max_edit)).getText().toString().trim();
//
//                    if (CustomUtility.stringExists(minLength_str) && CustomUtility.stringExists(maxLength_str)) {
//                        if (Objects.equals(minLength_str, maxLength_str))
//                            filter.add(String.format(Locale.getDefault(), "l:%s", minLength_str));
//                        else
//                            filter.add(String.format(Locale.getDefault(), "l:%s-%s", minLength_str, maxLength_str));
//                    } else if (CustomUtility.stringExists(minLength_str))
//                        filter.add(String.format(Locale.getDefault(), "l:%s-", minLength_str));
//                    else if (CustomUtility.stringExists(maxLength_str))
//                        filter.add(String.format(Locale.getDefault(), "l:-%s", maxLength_str));
//                    /**  <------------------------- Length -------------------------  */
//
//                    String newQuery = Utility.isNotValueReturnOrElse(removedQuery, "", s -> s + " ", null);
//                    newQuery += filter.isEmpty() ? "" : String.format("{%s}", String.join(" ", filter));
//                    searchView.setQuery(newQuery, false);
//
//                })
////                .setOnTouchOutside(CustomDialog::dismiss)
////                .setDismissWhenClickedOutside(false)
//                .show();
//
//    }
//
//    public static class AdvancedQueryHelper {
//        private static final Pattern advancedSearchPattern = Pattern.compile("\\{.*\\}");
//        public static final Pattern ratingPattern = Pattern.compile("(?<=r: ?)(([0-4]((.|,)\\d{1,2})?)|5((.|,)00?)?)(-(([0-4]((\\4|\\6)(?<=[,.])\\d{1,2})?)|5((\\4|\\6)(?<=[,.])00?)?))?(?=\\s*(\\}|\\w:))");
//        public static final Pattern datePattern = Pattern.compile("(?<=d: ?)(\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2}))(-\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2}))?(?=\\s*(\\}|\\w:))");
//        public static final Pattern durationPattern = Pattern.compile("(?<=d: ?)((-?\\d+[dmy])|(-?\\d+[dmy]|_(-?\\d+)?[my])(;-?\\d+[dmy]))(?=\\s*(\\}|\\w:))");
//        public static final Pattern lengthPattern = Pattern.compile("(?<=l: ?)(\\d+)?-?(\\d+)?(?=\\s*(\\}|\\w:))");
////        public static final Pattern durationModePattern = Pattern.compile("(?<=\\d[dmy])[ba]");
//
//        public String advancedQuery, restQuery, fullQuery, ratingSub, dateSub, durationSub, lengthSub;
//
//        public Pair<Date, Date> datePair;
//
//        public static AdvancedQueryHelper getAdvancedQuery(String fullQuery) {
//            AdvancedQueryHelper advancedQueryHelper = new AdvancedQueryHelper();
//            advancedQueryHelper.fullQuery = fullQuery;
//
//            if (!fullQuery.contains("{")) {
//                advancedQueryHelper.restQuery = fullQuery;
//                return advancedQueryHelper;
//            }
//
//            Matcher advancedQueryMatcher = advancedSearchPattern.matcher(fullQuery);
//
//            if (advancedQueryMatcher.find())
//                advancedQueryHelper.advancedQuery = advancedQueryMatcher.group(0);
//
//            if (advancedQueryHelper.hasAdvancedSearch()) {
//                if (advancedQueryHelper.advancedQuery.contains("r:")) {
//                    Matcher ratingMatcher = ratingPattern.matcher(advancedQueryHelper.advancedQuery);
//                    if (ratingMatcher.find()) {
//                        advancedQueryHelper.ratingSub = ratingMatcher.group(0);
//                    }
//                }
//                if (advancedQueryHelper.advancedQuery.contains("d:")) {
//                    Matcher dateMatcher = datePattern.matcher(advancedQueryHelper.advancedQuery);
//                    if (dateMatcher.find()) {
//                        advancedQueryHelper.dateSub = dateMatcher.group(0);
//                    } else {
//                        Matcher durationMatcher = durationPattern.matcher(advancedQueryHelper.advancedQuery);
//                        if (durationMatcher.find()) {
//                            advancedQueryHelper.durationSub = durationMatcher.group(0);
//                        }
//                    }
//                }
//                if (advancedQueryHelper.advancedQuery.contains("l:")) {
//                    Matcher lengthMatcher = lengthPattern.matcher(advancedQueryHelper.advancedQuery);
//                    if (lengthMatcher.find()) {
//                        advancedQueryHelper.lengthSub = lengthMatcher.group(0);
//                    }
//                }
//            }
//
//
//            advancedQueryHelper.restQuery = advancedQueryMatcher.replaceAll("");
//
//            return advancedQueryHelper;
//        }
//
//
//        /**  ------------------------- Checks ------------------------->  */
//        public boolean hasAdvancedSearch() {
//            return advancedQuery != null;
//        }
//
//        public boolean hasAnyAdvancedQuery() {
//            return hasDateSub() || hasRatingSub() || hasDurationSub() || hasLengthSub();
//        }
//
//        public boolean hasRatingSub() {
//            return ratingSub != null;
//        }
//
//        public boolean hasDateSub() {
//            return dateSub != null;
//        }
//
//        public boolean hasDurationSub() {
//            return durationSub != null;
//        }
//
//        public boolean hasDateOrDurationSub() {
//            return hasDateSub() || hasDurationSub();
//        }
//
//        public boolean hasLengthSub() {
//            return lengthSub != null;
//        }
//        /**  <------------------------- Checks -------------------------  */
//
//
//        /**  ------------------------- Convenience ------------------------->  */
//        public Pair<Float, Float> getRatingMinMax() {
//            if (ratingSub == null)
//                return null;
//
//            String[] range = ratingSub.replaceAll(",", ".").split("-");
//            float min = Float.parseFloat(range[0]);
//            float max = Float.parseFloat(range.length < 2 ? range[0] : range[1]);
//
//            return Pair.create(min, max);
//        }
//
//        public Pair<Date, Date> getDateMinMax() {
//            if (dateSub == null)
//                return null;
//
//            String[] range = dateSub.split("-");
//            Date min = null;
//            Date max = null;
//            try {
//                GenericReturnInterface<String, String> expandYear = s -> {
//                    String[] split = s.split("\\.");
//                    if (split[2].length() == 2) {
//                        split[2] = "20" + split[2];
//                        return String.join(".", split);
//                    } else
//                        return s;
//                };
//                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
//                min = dateFormat.parse(expandYear.run(range[0]));
//                if (range.length > 1)
//                    max = dateFormat.parse(expandYear.run(range[1]));
//            } catch (ParseException ignored) {
//            }
//
//            return datePair = Pair.create(min, CustomUtility.isNotNullOrElse(max, min));
//        }
//
//        public Pair<Date, Date> getDurationMinMax() {
//            if (durationSub == null)
//                return null;
//
//            String[] range = durationSub.split(";"); // ToDo: evl. Flags hinzufügen?
//            Date pivot;
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.SECOND, 0);
//            cal.set(Calendar.MILLISECOND, 0);
//            Map<String, Integer> modeMap = new HashMap<>();
//            modeMap.put("d", Calendar.DAY_OF_MONTH);
//            modeMap.put("m", Calendar.MONTH);
//            modeMap.put("y", Calendar.YEAR);
//
//
//            if (range.length > 1) {
//                String pivotString = range[0];
//                if (pivotString.startsWith("_")) {
//                    if (pivotString.endsWith("m")) {
//                        if (pivotString.length() > 2)
//                            cal.add(Calendar.MONTH, Integer.parseInt(CustomUtility.subString(pivotString, 1, -1)) * -1);
//                        cal.set(Calendar.DAY_OF_MONTH, 1);
//                        pivot = cal.getTime();
//                    } else {
//                        if (pivotString.length() > 2)
//                            cal.add(Calendar.YEAR, Integer.parseInt(CustomUtility.subString(pivotString, 1, -1)) * -1);
//                        cal.set(Calendar.DAY_OF_YEAR, 1);
//                        pivot = cal.getTime();
//                    }
//                } else {
//                    cal.add(modeMap.get(CustomUtility.subString(pivotString, -1)), Integer.parseInt(CustomUtility.subString(pivotString, 0, -1)) * -1);
//                    pivot = cal.getTime();
//                }
//            } else
//                pivot = cal.getTime();
//
//            String durationString = range.length > 1 ? range[1] : range[0];
//            int durationInt = Integer.parseInt(CustomUtility.subString(durationString, 0, -1));
//            String durationMode = CustomUtility.subString(durationString, -1);
//            if (durationMode.equals("d"))
//                durationInt = (Math.abs(durationInt) - 1) * (durationInt < 0 ? -1 : 1);
//            cal.add(modeMap.get(durationMode), durationInt * -1);
////            cal.add(Calendar.DAY_OF_MONTH, durationInt < 0 ? -1 : 1);
//
//            Date duration = cal.getTime();
//
//            Pair<Date, Date> datePair = pivot.before(duration) ? Pair.create(pivot, duration) : Pair.create(duration, pivot);
//
//            if (!durationMode.equals("d")) {
//                cal.setTime(datePair.second);
//                cal.add(Calendar.DAY_OF_MONTH, -1);
//                datePair = Pair.create(datePair.first, cal.getTime());
//            }
//
//            return this.datePair = datePair;
//        }
//
//        public Pair<Date, Date> getDateOrDurationMinMax() {
//            if (hasDateSub())
//                return getDateMinMax();
//            else
//                return getDurationMinMax();
//        }
//
//        public Pair<Integer, Integer> getLengthMinMax() {
//            String[] range = lengthSub.split("-");
//            int min = range.length > 0 ? Integer.parseInt(CustomUtility.isNotValueOrElse(range[0], "", "-1")) : -1;
//            int max = range.length > 1 ? Integer.parseInt(CustomUtility.isNotValueOrElse(range[1], "", "-1")) : (lengthSub.endsWith("-") ? -1 : min);
//
//            return Pair.create(min,max);
//        }
//
//        public static String removeAdvancedSearch(CharSequence fullQuery) {
//            return fullQuery.toString().replaceAll(AdvancedQueryHelper.advancedSearchPattern.pattern(), "").trim();
//        }
//        /**  <------------------------- Convenience -------------------------  */
//    }

    /** <------------------------- Advanced Search ------------------------- */


    public static void sendText(AppCompatActivity activity, String text) {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, text);//
            activity.startActivity(Intent.createChooser(waIntent, "App auswählen"));
        } else {
            Toast.makeText(activity, "WhatsApp not found", Toast.LENGTH_SHORT).show();
        }

    }

    /** ------------------------- Date & String -------------------------> */
    public static Date getDateFromJsonString(String key, JSONObject jsonObject) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(jsonObject.getString(key));
        } catch (ParseException | JSONException e) {
            return null;
        }
    }

    public static String formatDate(String format, Date date) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public enum DateFormat {
        DATE_DASH("dd-MM-yyyy"), DATE_DOT("dd.MM.yyyy"), DATE_DASH_REVERSE("yyyy-MM-dd"), DATE_DOT_REVERSE("yyyy.MM.dd"), DATE_TIME_DASH("HH:mm 'Uhr' dd-MM-yyyy"), DATE_TIME_DOT("HH:mm 'Uhr' dd.MM.yyyy"),
        ;

        public final String format;

        DateFormat(String format) {
            this.format = format;
        }
    }

    public static String formatDate(DateFormat format, Date date) {
        return new SimpleDateFormat(format.format, Locale.getDefault()).format(date);
    }
    /**  <------------------------- Date & String -------------------------  */


    /** ------------------------- SetDimensions -------------------------> */
    public static void setDimensions(View view, boolean width, boolean height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            view.setLayoutParams(
                    new ViewGroup.LayoutParams(width ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT, height ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        if (width)
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        else
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (height)
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        else
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(lp);
    }
    /**  <------------------------- SetDimensions -------------------------  */


    /** ------------------------- getViews -------------------------> */
    public static <T extends View> ArrayList<T> getViewsByType(ViewGroup root, Class<T> tClass) {
        final ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup)
                result.addAll(getViewsByType((ViewGroup) child, tClass));

            if (tClass.isInstance(child))
                result.add(tClass.cast(child));
        }
        return result;
    }

    public static <T extends View> void applyToAllViews(ViewGroup root, Class<T> tClass, ApplyToAll<T> applyToAll) {
        getViewsByType(root, tClass).forEach(applyToAll::runApplyToAll);
    }

    public interface ApplyToAll<T extends View> {
        void runApplyToAll(T t);
    }

    public static <S extends View, T extends View> void replaceView(S oldView, T newView, @Nullable TransferState<S, T> transferState) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        int index = parent.indexOfChild(oldView);
        parent.removeView(oldView);
        parent.addView(newView, index);
        if (transferState != null)
            transferState.runTransferState(oldView, newView);
    }

    public static <S extends View, T extends View> void replaceView_children(S oldView, T newView, @Nullable TransferState<S, T> transferState) {
        replaceView(oldView, newView, transferState);
        if (oldView instanceof ViewGroup && newView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) oldView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                viewGroup.removeView(child);
                ((ViewGroup) newView).addView(child);
            }
        }
    }

    public interface TransferState<S, T> {
        void runTransferState(S source, T target);
    }

    public static EditText getEditTextFromSearchView(SearchView searchView) {
        return searchView.findViewById(Resources.getSystem().getIdentifier("search_src_text", "id", "android"));
    }
    /**  <------------------------- getViews -------------------------  */


    /** ------------------------- SquareView -------------------------> */
    enum EQUAL_MODE {
        HEIGHT, WIDTH, MAX, MIN
    }

    public static void squareView(View view) {
        squareView(view, EQUAL_MODE.MAX);
    }

    public static void squareView(View view, EQUAL_MODE equal_mode) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        int height = view.getMeasuredHeight();

        int matchParentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getHeight(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(wrapContentMeasureSpec_width, matchParentMeasureSpec_width);
        int width = view.getMeasuredWidth();
        switch (equal_mode) {
            case WIDTH:
                layoutParams.height = width;
                break;
            case HEIGHT:
                layoutParams.width = height;
                break;
            case MIN:
                int min = width < height ? width : height;
                layoutParams.width = min;
                layoutParams.height = min;
                break;
            case MAX:
                int max = width > height ? width : height;
                layoutParams.width = max;
                layoutParams.height = max;
                break;
        }
    }
    /**  <------------------------- SquareView -------------------------  */


//    /**  ------------------------- ViewAspectRatio ------------------------->  */
//    public static void applyAspectRatioWidth(View view, double aspectRatio){
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view._getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//        int height = view.getMeasuredHeight();
//
//        layoutParams.width = (int) (height * aspectRatio);
//    }
//    public static void applyAspectRatioHeight(View view, double aspectRatio){
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//
////        int matchParentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(((View) view._getParent()).getHeight(), View.MeasureSpec.EXACTLY);
////        int wrapContentMeasureSpec_width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
////        view.measure(wrapContentMeasureSpec_width, matchParentMeasureSpec_width);
////        int width = view.getMeasuredWidth();
//
//        view.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
//            int width = v1.getWidth();
//            int height = v1.getHeight();
////            layoutParams.width = width;
//
//            v1.getLayoutParams().height = (int) (width * aspectRatio);
//            v1.requestLayout();
////            view.setLayoutParams(layoutParams);
////            view.setMinimumHeight((int) (width * aspectRatio));
//        });
//
//    }
//    /**  <------------------------- ViewAspectRatio -------------------------  */


    /** ------------------------- ScaleMatchingParent -------------------------> */
    public static void scaleMatchingParentWidth(View view) {
        view.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            ViewGroup parent = (ViewGroup) v.getParent();
            int parentWidth = parent.getWidth();

            v.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED));
            final double targetHeight = v.getMeasuredHeight();
            final double targetWidth = v.getMeasuredWidth();

            double width = v.getWidth();
            double height = v.getHeight();
            if (width == 0)
                return;
            double ratio = targetHeight / targetWidth;
//            double ratio = height / width;
//            view.setLayoutParams(new LinearLayout.LayoutParams(targetWidth, targetHeight));
            view.setLayoutParams(new LinearLayout.LayoutParams(parentWidth, (int) (parentWidth * ratio)));
        });
    }
    /**  <------------------------- ScaleMatchingParent -------------------------  */


    /** ------------------------- Generated Visuals -------------------------> */
    public static Drawable drawableBuilder_rectangle(int color, int corners, boolean ripple) {
        DrawableBuilder drawableBuilder = new DrawableBuilder()
                .rectangle()
                .solidColor(color)
                .cornerRadius(Utility.dpToPx(corners));
        if (ripple) drawableBuilder
                .ripple()
                .rippleColor(0xF8868686);
        return drawableBuilder
                .build();
    }

    public static Drawable drawableBuilder_oval(int color) {
        return new DrawableBuilder()
                .oval()
                .solidColor(color)
                .build();
    }

    public static int setAlphaOfColor(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }
    /**  <------------------------- Generated Visuals -------------------------  */


    /** ------------------------- ConcatCollections -------------------------> */
    public interface GetCollections<T, V> {
        Collection<V> runGetCollections(T t);
    }

    public static <T, V> List<V> concatenateCollections(Collection<T> tCollection, GetCollections<T, V> getCollections) {
        List<Collection<V>> collectionList = new ArrayList<>();
        tCollection.forEach(t -> collectionList.add(getCollections.runGetCollections(t)));
        return collectionList.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> concatenateCollections(Collection<Collection<T>> collections) {
        return collections.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> concatenateCollections(List<T>... collections) {
        return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> addAllDistinct(List<T> from, List<T> into) {
        com.finn.androidUtilities.CustomList<T> customList = new com.finn.androidUtilities.CustomList<>(into).addAllDistinct(from);
        into.clear();
        into.addAll(customList);
        return into;
    }
    /**  <------------------------- ConcatCollections -------------------------  */


    /** ------------------------- ifNotNull -------------------------> */
    public static <E> boolean ifNotNull(E e, ExecuteIfNotNull<E> executeIfNotNull) {
        if (e == null)
            return false;
        executeIfNotNull.runExecuteIfNotNull(e);
        return true;
    }

    public static <E> boolean ifNotNull(E e, ExecuteIfNotNull<E> executeIfNotNull, Runnable executeIfNull) {
        if (e == null) {
            executeIfNull.run();
            return false;
        }
        executeIfNotNull.runExecuteIfNotNull(e);
        return true;
    }

    public interface ExecuteIfNotNull<E> {
        void runExecuteIfNotNull(E e);
    }

    public static boolean ignoreNull(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static <T> T returnIfNull(T object, T returnIfNull) {
        return object != null ? object : returnIfNull;
    }
    /**  <------------------------- ifNotNull -------------------------  */


    /** ------------------------- Reflections -------------------------> */
    public static List<TextWatcher> removeTextListeners(TextView view) {
        List<TextWatcher> returnList = null;
        try {
            Field mListeners = TextView.class.getDeclaredField("mListeners");
            mListeners.setAccessible(true);
            returnList = (List<TextWatcher>) mListeners.get(view);
            mListeners.set(view, null);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return returnList;
    }

    public static <T, V> void reflectionSet(T t, String fieldName, V value) {
        try {
            Field field = t.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(t, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    public static <T> void reflectionCall(T t, String methodName, Pair<Class<?>, Object>... paramTypesAndValues) {
        try {
            Class<?>[] classes = new Class<?>[paramTypesAndValues.length];
            Object[] objects = new Object[paramTypesAndValues.length];
            for (int i = 0; i < paramTypesAndValues.length; i++) {
                classes[i] = paramTypesAndValues[i].first;
                objects[i] = paramTypesAndValues[i].second;
            }

            Method field = t.getClass().getDeclaredMethod(methodName, classes);
            field.setAccessible(true);
            field.invoke(t, objects);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
    }
    /**  <------------------------- Reflections -------------------------  */


    /** ------------------------- EasyLogic -------------------------> */
    public static class NoArgumentException extends RuntimeException {
        public static final String DEFAULT_MESSAGE = "Keine Argumente mitgegeben";

        public NoArgumentException(String message) {
            super(message);
        }
    }

    public static <T> boolean boolOr(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (what.run(o))
                return true;
        }
        return false;

    }

    public static <T> boolean boolOr(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (Objects.equals(what, o))
                return true;
        }
        return false;
    }

    public static <T> boolean boolXOr(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        boolean found = false;
        for (T o : to) {
            if (what.run(o)) {
                if (found)
                    return false;
                found = true;
            }
        }
        return found;
    }

    public static <T> boolean boolXOr(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        boolean found = false;
        for (T o : to) {
            if (Objects.equals(what, o)) {
                if (found)
                    return false;
                found = true;
            }
        }
        return found;
    }

    public static <T> boolean boolAnd(T what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (!Objects.equals(what, o))
                return false;
        }
        return true;
    }

    public static <T> boolean boolAnd(GenericReturnInterface<T, Boolean> what, T... to) {
        if (to.length == 0)
            throw new NoArgumentException(NoArgumentException.DEFAULT_MESSAGE);

        for (T o : to) {
            if (!what.run(o))
                return false;
        }
        return true;
    }

    // ---

    public static boolean containsIgnoreCase(String in, String query) {
        if (in == null || query == null)
            return false;
        return in.toLowerCase().contains(query.toLowerCase());
    }

    // ---

    public static boolean stringExists(CharSequence s) {
        return s != null && !s.toString().trim().isEmpty();
    }

    public static <T extends CharSequence> T stringExistsOrElse(T s, T orElse) {
        return stringExists(s) ? s : orElse;
    }

    public static <T> T isNotNullOrElse(T input, T orElse) {
        return input != null ? input : orElse;
    }

    public static <T> T isNotNullOrElse(T input, GenericReturnOnlyInterface<T> orElse) {
        return input != null ? input : orElse.run();
    }

    public static <T> T isNotValueOrElse(T input, T value, T orElse) {
        return !Objects.equals(input, value) ? input : orElse;
    }

    public static <T> T isNotValueOrElse(T input, T value, GenericReturnOnlyInterface<T> orElse) {
        return !Objects.equals(input, value) ? input : orElse.run();
    }

    public static <T> T isValueOrElse(T input, T value, T orElse) {
        return Objects.equals(input, value) ? input : orElse;
    }

    public static <T> T isValueOrElse(T input, T value, GenericReturnOnlyInterface<T> orElse) {
        return Objects.equals(input, value) ? input : orElse.run();
    }

    public static <T, R> R isNotValueReturnOrElse(T input, T value, GenericReturnInterface<T, R> returnValue, @Nullable GenericReturnOnlyInterface<R> orElse) {
        return !Objects.equals(input, value) ? returnValue.run(input) : orElse != null ? orElse.run() : (R) input;
    }

    public static <T, R> R isValueReturnOrElse(T input, T value, GenericReturnInterface<T, R> returnValue, GenericReturnOnlyInterface<R> orElse) {
        return Objects.equals(input, value) ? returnValue.run(input) : orElse.run();
    }

    public static <T, R> R isNullReturnOrElse(T input, R returnValue, GenericReturnInterface<T, R> orElse) {
        return Objects.equals(input, null) ? returnValue : orElse.run(input);
    }

    public static <T, R> R isCheckReturnOrElse(T input, CustomUtility.GenericReturnInterface<T, Boolean> check, @Nullable CustomUtility.GenericReturnInterface<T, R> returnValue, CustomUtility.GenericReturnInterface<T, R> orElse) {
        if (check.run(input)) {
            if (returnValue == null)
                return (R) input;
            else
                return returnValue.run(input);
        } else
            return orElse.run(input);
    }
    /**  <------------------------- EasyLogic -------------------------  */


    /** ------------------------- Switch Expression -------------------------> */
    public static class SwitchExpression<Input, Output> {
        private Input input;
        private CustomList<Pair<Input, Object>> caseList = new CustomList<>();
        private Object defaultCase;

        public SwitchExpression(Input input) {
            this.input = input;
        }

        public static <Input> SwitchExpression<Input, Object> setInput(Input input) {
            return new SwitchExpression<>(input);
        }

        /** ------------------------- Getters & Setters -------------------------> */
        public Input getInput() {
            return input;
        }
        /**  <------------------------- Getters & Setters -------------------------  */


        /** ------------------------- Cases -------------------------> */
        public <Type> SwitchExpression<Input, Type> addCase(Input inputCase, ExecuteOnCase<Input, Type> executeOnCase) {
            caseList.add(new Pair<>(inputCase, executeOnCase));
            return (SwitchExpression<Input, Type>) this;
        }

        public <Type> SwitchExpression<Input, Type> addCase(Input inputCase, Type returnOnCase) {
            caseList.add(new Pair<>(inputCase, returnOnCase));
            return (SwitchExpression<Input, Type>) this;
        }

        public SwitchExpression<Input, Output> addCaseToLastCase(Input inputCase) {
            caseList.add(new Pair<>(inputCase, caseList.getLast().second));
            return this;
        }

        // ---------------

        public <Type> SwitchExpression<Input, Type> setDefault(ExecuteOnCase<Input, Type> defaultCase) {
            this.defaultCase = defaultCase;
            return (SwitchExpression<Input, Type>) this;
        }

        public <Type> SwitchExpression<Input, Type> setDefault(Type defaultCase) {
            this.defaultCase = defaultCase;
            return (SwitchExpression<Input, Type>) this;
        }

        public SwitchExpression<Input, Output> setLastCaseAsDefault() {
            defaultCase = caseList.getLast().second;
            return this;
        }

        // ---------------

        public interface ExecuteOnCase<Input, Output> {
            Output runExecuteOnCase(Input input);
        }

        /** <------------------------- Cases ------------------------- */


        public Output evaluate() {
            Optional<Pair<Input, Object>> optional = caseList.stream().filter(inputExecuteOnCasePair -> Objects.equals(input, inputExecuteOnCasePair.first)).findFirst();

            if (optional.isPresent()) {
                Object o = optional.get().second;
                if (o instanceof ExecuteOnCase)
                    return (Output) ((ExecuteOnCase) o).runExecuteOnCase(input);
                else
                    return (Output) o;
            } else if (defaultCase != null) {
                if (defaultCase instanceof ExecuteOnCase)
                    return (Output) ((ExecuteOnCase) defaultCase).runExecuteOnCase(input);
                else
                    return (Output) defaultCase;
            } else {
                return null;
            }
        }
    }
    /**  <------------------------- Switch Expression -------------------------  */


    /** ------------------------- Interfaces -------------------------> */
    public interface GenericInterface<T> {
        void run(T t);
    }

    public interface GenericReturnInterface<T, R> {
        R run(T t);
    }

    public interface DoubleGenericInterface<T, T2> {
        void run(T t, T2 t2);
    }

    public interface DoubleGenericReturnInterface<T, T2, R> {
        R run(T t, T2 t2);
    }

    public interface TripleGenericInterface<T, T2, T3> {
        void run(T t, T2 t2, T3 t3);
    }

    public interface TripleGenericReturnInterface<T, T2, T3, R> {
        R run(T t, T2 t2, T3 t3);
    }

    public interface GenericReturnOnlyInterface<T> {
        T run();
    }

    public static <T> boolean runGenericInterface(GenericInterface<T> genericInterface, T parameter) {
        if (genericInterface != null) {
            genericInterface.run(parameter);
            return true;
        }
        return false;
    }

    public static <T, T2> boolean runDoubleGenericInterface(DoubleGenericInterface<T, T2> genericInterface, T parameter, T2 parameter2) {
        if (genericInterface != null) {
            genericInterface.run(parameter, parameter2);
            return true;
        }
        return false;
    }

    public static <T, T2, T3> boolean runTripleGenericInterface(TripleGenericInterface<T, T2, T3> genericInterface, T parameter, T2 parameter2, T3 parameter3) {
        if (genericInterface != null) {
            genericInterface.run(parameter, parameter2, parameter3);
            return true;
        }
        return false;
    }

    public static boolean runRunnable(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
            return true;
        }
        return false;
    }

    public static boolean runVarArgRunnable(int index, Runnable... varArg) {
        if (varArg != null && index >= 0) {
            if (varArg.length > index && varArg[index] != null)
                varArg[index].run();
            else
                return false;
        } else
            return false;
        return true;
    }

    public static <T> boolean runVarArgGenericInterface(int index, T input, GenericInterface<T>... varArg) {
        if (varArg != null && index >= 0) {
            if (varArg.length > index && varArg[index] != null)
                varArg[index].run(input);
            else
                return false;
        } else
            return false;
        return true;
    }

    // --------------- Recursion

    public interface RecursiveGenericInterface<T> {
        void run(T t, RecursiveGenericInterface<T> recursiveInterface);
    }

    public static <T> void runRecursiveGenericInterface(T t, RecursiveGenericInterface<T> recursiveInterface) {
        recursiveInterface.run(t, recursiveInterface);
    }

    public interface RecursiveGenericReturnInterface<T, R> {
        R run(T t, RecursiveGenericReturnInterface<T, R> recursiveInterface);
    }

    public static <T, R> R runRecursiveGenericReturnInterface(T t, Class<R> returnType, RecursiveGenericReturnInterface<T, R> recursiveInterface) {
        return recursiveInterface.run(t, recursiveInterface);
    }
    /**  <------------------------- Interfaces -------------------------  */


    /** ------------------------- ImageView -------------------------> */
    private static Bitmap getBitmapFromUri(Uri uri, Context context) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            Toast.makeText(context, "Fehler beim Bild", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void simpleLoadUrlIntoImageView(Context context, ImageView imageView, @Nullable Boolean visible, String imagePath, @Nullable String fullScreenPath, int roundedRadiusDp, Runnable... onFail_onSuccess_onFullscreen) {
        if ((visible == null && stringExists(imagePath)) || (visible != null && visible)) {
            imageView.setVisibility(View.VISIBLE);
            Utility.simpleLoadUrlIntoImageView(context, imageView, imagePath, fullScreenPath, roundedRadiusDp, onFail_onSuccess_onFullscreen);
        } else
            imageView.setVisibility(View.GONE);
    }

    public static void simpleLoadUrlIntoImageView(Context context, ImageView imageView, @NonNull String imagePath, @Nullable String fullScreenPath, int roundedRadiusDp, Runnable... onFail_onSuccess_onFullscreen) {
        Runnable round = () -> roundImageView(imageView, roundedRadiusDp);

        Runnable[] runnables = onFail_onSuccess_onFullscreen;
        if (roundedRadiusDp > 0) {
            if (runnables.length == 0) {
                runnables = new Runnable[2];
                runnables[1] = round;
            } else if (runnables.length == 1) {
                Runnable old = runnables[0];
                runnables = new Runnable[2];
                runnables[0] = old;
                runnables[1] = round;
            } else if (runnables.length >= 2 && runnables[1] != null) {
                Runnable old = runnables[1];
                runnables[1] = () -> {
                    round.run();
                    old.run();
                };
            } else if (runnables.length >= 2)
                runnables[0] = round;
        } else {
            runnables = onFail_onSuccess_onFullscreen;
        }

        if (imagePath == null)
            imagePath = "";

        loadUrlIntoImageView(context, imageView, null, getTmdbImagePath_ifNecessary(imagePath, false), CustomUtility.stringExists(fullScreenPath) ? getTmdbImagePath_ifNecessary(fullScreenPath, true) : null, runnables);
    }

    public static void loadUrlIntoImageView(Context context, ImageView imageView, @NonNull String imagePath, @Nullable String fullScreenPath, Runnable... onFail_onSuccess_onFullscreen) {
        loadUrlIntoImageView(context, imageView, null, imagePath, fullScreenPath, onFail_onSuccess_onFullscreen);
    }

    // ToDo: Rounded Corners direkt mit Glide umsetzen
    public static void loadUrlIntoImageView(Context context, ImageView imageView, @Nullable GenericInterface<RequestBuilder<Drawable>> requestModifications, @NonNull String imagePath, @Nullable String fullScreenPath, Runnable... onFail_onSuccess_onFullscreen) {
//        if (imagePath.matches(ActivityResultHelper.uriRegex)) {
////            CustomUtility.ifNotNull(getBitmapFromUri(Uri.parse(imagePath), context), imageView::setImageBitmap);
//            Uri uri = Uri.parse(imagePath);
////            context.getContentResolver().takePersistableUriPermission(uri, (Intent.FLAG_GRANT_READ_URI_PERMISSION| Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
//            imageView.setImageURI(uri);
//        } else {
        if (imagePath.endsWith(".svg") && !imagePath.contains("base64")) {
            Utility.fetchSvg(context, imagePath, imageView, onFail_onSuccess_onFullscreen);
        } else {
            RequestBuilder<Drawable> requestBuilder;
            if (imagePath.isEmpty()) {
                requestBuilder = Glide.with(context).load(R.drawable.ic_no_image);
            } else if (imagePath.startsWith(context.getFilesDir().getAbsolutePath())) {
                Uri imageUri = Uri.fromFile(new File(imagePath));
                requestBuilder = Glide.with(context).load(imageUri);
            } else
                requestBuilder = Glide.with(context).load(imagePath);
            if (requestModifications != null)
                requestModifications.run(requestBuilder);
            requestBuilder
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (onFail_onSuccess_onFullscreen.length > 0 && onFail_onSuccess_onFullscreen[0] != null)
                                onFail_onSuccess_onFullscreen[0].run();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }

                    })
                    .error(R.drawable.ic_broken_image)
                    .placeholder(R.drawable.ic_download)
                    .into(new DrawableImageViewTarget(imageView) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            if (resource == null)
                                return;
                            super.setResource(resource);

                            if (imagePath.contains(".png"))
                                setImageViewBackgroundColor(imageView, 0x77ffffff, 24);

                            if (onFail_onSuccess_onFullscreen.length > 1 && onFail_onSuccess_onFullscreen[1] != null)
                                onFail_onSuccess_onFullscreen[1].run();
                        }
                    });
//                    .into(imageView);
        }
//        }
        if (fullScreenPath == null) {
//            imageView.setOnClickListener(null);
            return;
        }
        imageView.setOnClickListener(v -> {
            if (onFail_onSuccess_onFullscreen.length > 2 && onFail_onSuccess_onFullscreen[2] != null)
                onFail_onSuccess_onFullscreen[2].run();
            showFullScreenImage(context, requestModifications, fullScreenPath, onFail_onSuccess_onFullscreen);
        });
    }

    public static void showFullScreenImage(Context context, @Nullable GenericInterface<RequestBuilder<Drawable>> requestModifications, @NonNull String fullScreenPath, Runnable... onFail_onSuccess_onFullscreen) {
        CustomDialog.Builder(context)
                .setView(R.layout.dialog_poster)
                .setSetViewContent((customDialog1, view1, reload1) -> {
                    PhotoView dialog_poster_poster = view1.findViewById(R.id.dialog_poster_poster);
                    dialog_poster_poster.setMaximumScale(6f);
                    dialog_poster_poster.setMediumScale(2.5f);
//                        if (fullScreenPath.endsWith(".png") || fullScreenPath.endsWith(".svg"))
//                            dialog_poster_poster.setPadding(0, 0, 0, 0);

                    if (fullScreenPath.endsWith(".svg")) {
                        Utility.fetchSvg(context, fullScreenPath, dialog_poster_poster, onFail_onSuccess_onFullscreen);
                    } else {
                        RequestBuilder<Drawable> requestBuilder;
                        if (fullScreenPath.startsWith(context.getFilesDir().getAbsolutePath())) {
                            Uri imageUri = Uri.fromFile(new File(fullScreenPath));
                            requestBuilder = Glide.with(context).load(imageUri);
                        } else
                            requestBuilder = Glide.with(context).load(fullScreenPath);
                        if (requestModifications != null)
                            requestModifications.run(requestBuilder);
                        requestBuilder
                                .error(R.drawable.ic_broken_image)
                                .placeholder(R.drawable.ic_download)
                                .into(dialog_poster_poster);
//                                    .into(new DrawableImageViewTarget(dialog_poster_poster) {
//                                        @Override
//                                        protected void setResource(@Nullable Drawable resource) {
//                                            if (resource == null)
//                                                return;
//                                            super.setResource(resource);
//
//                                            if (fullScreenPath.contains(".png")) {
////                                                setImageViewBackgroundColor(dialog_poster_poster, 0x99ffffff, 0);
////                                                Utility.roundImageView(dialog_poster_poster, 18);
//                                            }
//                                        }
//                                    });
//                                    .into(dialog_poster_poster/*new DrawableImageViewTarget(dialog_poster_poster) {
//                                        @Override
//                                        protected void setResource(@Nullable Drawable resource) {
//                                            if (resource == null)
//                                                return;
//                                            super.setResource(resource);
//
//                                            if (fullScreenPath.contains(".png")) {
////                                                setImageViewBackgroundColor(dialog_poster_poster, 0x99ffffff, 0);
////                                                Utility.roundImageView(dialog_poster_poster, 18);
//                                            }
//                                        }
//                                    }*/);
                    }

                    dialog_poster_poster.setOnContextClickListener(v1 -> {
                        customDialog1.dismiss();
                        return true;
                    });
                    dialog_poster_poster.setOnOutsidePhotoTapListener(imageView1 -> customDialog1.dismiss());
                })
                .addOptionalModifications(customDialog -> {
                    if ((!fullScreenPath.contains(".png") && !fullScreenPath.contains(".svg"))) {
                        customDialog.removeBackground_and_margin();
                    }
                })
                .addOnDialogShown(customDialog -> {
                    if ((fullScreenPath.contains(".png") || fullScreenPath.contains(".svg"))) {
                        Window window = customDialog.getDialog().getWindow();
                        if (window != null) {
                            window.setBackgroundDrawable(new ColorDrawable(0x99ffffff));
                        }
                    }
                })
                .disableScroll()
                .setDimensionsFullscreen()
                .show();
    }

    private static void setImageViewBackgroundColor(ImageView imageView, int color, int borderPx) {
        imageView.setDrawingCacheEnabled(true);
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
        imageView.buildDrawingCache(true);
        Bitmap bitmap;
        bitmap = imageView.getDrawingCache();
        if (bitmap == null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BitmapDrawable)
                bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        if (bitmap == null)
            return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            CustomUtility.GenericInterface<Boolean> logTiming = CustomUtility.logTiming();
            // ToDo: Optimieren (Vielleicht nur Rand?) und auch für große Version speichern
            boolean hasAlpha = hasAlpha(bitmap);
//            CustomUtility.logD(null, "setImageViewBackgroundColor: %s", hasAlpha);
//            logTiming.run(false);
            if (!hasAlpha)
                return;
        }

        Bitmap old = Bitmap.createBitmap(bitmap);
        Bitmap newBitmap = Bitmap.createBitmap(old.getWidth() + borderPx, old.getHeight() + borderPx, old.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(color);
        float centreX = (canvas.getWidth() - old.getWidth()) / 2F;
        float centreY = (canvas.getHeight() - old.getHeight()) / 2F;
        canvas.drawBitmap(old, centreX, centreY, null);
        imageView.setDrawingCacheEnabled(false);
//        newBitmap.getByteCount()
        imageView.setImageBitmap(newBitmap);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static boolean hasAlpha(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int i = 0; i < width; i++) {
            if (bitmap.getColor(i, 0).alpha() != 1f)
                return true;
            if (bitmap.getColor(i, height - 1).alpha() != 1f)
                return true;
        }
        for (int i = 0; i < height; i++) {
            if (bitmap.getColor(0, i).alpha() != 1f)
                return true;
            if (bitmap.getColor(width - 1, i).alpha() != 1f)
                return true;
        }
//        for (int i = 0; i < bitmap.getWidth(); i++) {
//            for (int i1 = 0; i1 < bitmap.getHeight(); i1++) {
//                if (bitmap.getColor(i, i1).alpha() != 1f)
//                    return true;
//            }
//        }
        return false;
    }

    private static OkHttpClient httpClient;

    public static void fetchSvg(Context context, String url, final ImageView target, Runnable... onFail_onSuccess) {
        if (httpClient == null) {
            // Use cache for performance and basic offline capability
            httpClient = new OkHttpClient.Builder()
                    .cache(new Cache(context.getCacheDir(), 5 * 1024 * 1014))
                    .build();
        }

        if (!url.startsWith("http"))
            url = "https://" + url;
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        Runnable runOnFailure = () -> {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    target.setImageResource(R.drawable.ic_broken_image);
                    if (onFail_onSuccess.length > 0 && onFail_onSuccess[0] != null) {
                        onFail_onSuccess[0].run();
                    }
                });
            }
        };

        Runnable runOnSuccess = () -> {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    if (onFail_onSuccess.length > 1 && onFail_onSuccess[1] != null) {
                        onFail_onSuccess[1].run();
                    }
                });
            }
        };

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnFailure.run();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream stream = response.body().byteStream();
                try {
//                    Sharp.loadInputStream(stream).into(target);
                } catch (Exception e) {
                    runOnFailure.run();
                }
                stream.close();
                runOnSuccess.run();
            }
        });
    }

    // ---------------

    public static class ImageHelper {
        public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
    }

    public static void roundImageView(ImageView imageView, int dp) {
        int radius;
        if (dp == -1) {
            imageView.measure(0, 0);
            radius = Math.max(imageView.getMeasuredWidth(), imageView.getMeasuredHeight()) / 2;
        } else
            radius = dpToPx(dp);

//        Bitmap oldBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        imageView.setDrawingCacheEnabled(true);
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0,
                imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
        imageView.buildDrawingCache(true);
        Bitmap bitmap;
        bitmap = imageView.getDrawingCache();
        if (bitmap == null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BitmapDrawable)
                bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        if (bitmap == null)
            return;
        Bitmap oldBitmap = Bitmap.createBitmap(bitmap);
        imageView.setDrawingCacheEnabled(false);
        imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(oldBitmap, radius));
    }

    // ---------------

    public static String getTmdbImagePath_ifNecessary(String imagePath, String size) {
        if (imagePath == null || imagePath.matches(ActivityResultHelper.uriRegex))
            return imagePath;
        return (!imagePath.matches("\\/\\w+\\.\\w+") ? "" : "https://image.tmdb.org/t/p/" + size + "/") + imagePath;
    }

    public static String getTmdbImagePath_ifNecessary(String imagePath, boolean original) {
        return getTmdbImagePath_ifNecessary(imagePath, (original ? "original" : "w92"));
    }
    /**  <------------------------- ImageView -------------------------  */


    /** ------------------------- GetOpenGraphFromWebsite -------------------------> */
    public static void getOpenGraphFromWebsite(String url, GenericInterface<OpenGraph> onResult) {
        @SuppressLint("StaticFieldLeak") AsyncTask<GenericInterface<OpenGraph>, Object, OpenGraph> task = new AsyncTask<Utility.GenericInterface<OpenGraph>, Object, OpenGraph>() {
            Utility.GenericInterface<OpenGraph> onResult;

            @Override
            protected OpenGraph doInBackground(Utility.GenericInterface<OpenGraph>... onResults) {
                try {
                    OpenGraph openGraph = new OpenGraph(url, true);
                    if (onResults.length > 0)
                        onResult = onResults[0];
                    return openGraph;
                } catch (Exception e) {
                    if (onResults.length > 0)
                        onResult = onResults[0];
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OpenGraph openGraph) {
                if (onResult != null) {
                    onResult.run(openGraph);
                }
            }
        };

        task.execute(onResult);

    }
    /**  <------------------------- GetOpenGraphFromWebsite -------------------------  */

    /** ------------------------- Videos Aktualisieren -------------------------> */
    public static void updateVideos(Context context, CustomList<Video> updateList) {
        Database database = Database.getInstance();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        updateList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
//        updateList.filter(video -> video.getAgeRating() == -1, true);
        List<Video> failedList = new ArrayList<>();
        int allCount = updateList.size();
        if (allCount == 0) {
            Toast.makeText(context, "Keine Videos vorhanden", Toast.LENGTH_SHORT).show();
            return;
        }
        final Video[] currentVideo = {updateList.get(0)};
        final int[] finishedCount = {0};
        int closeButtonId = View.generateViewId();
        final boolean[] doubleClick = {true};

        Runnable[] loadDetails = {null};
        CustomDialog progressDialog = CustomDialog.Builder(context)
                .setTitle("Fortschritt")
                .setText(finishedCount[0] + "/" + allCount + " wurden aktualisiert")
                .addButton("Schließen", customDialog2 -> {
                }, closeButtonId)
                .hideLastAddedButton()
                .enableDoubleClickOutsideToDismiss(customDialog2 -> doubleClick[0])
                .setOnDialogDismiss(customDialog3 -> loadDetails[0] = () -> {
                })
                .show();


        Runnable isFinished = () -> {
            finishedCount[0]++;
            if (finishedCount[0] >= allCount) {
                String failedString = failedList.stream().map(com.finn.androidUtilities.ParentClass::getName).collect(Collectors.joining(", "));
                progressDialog.setText("Fertig!" + (Utility.stringExists(failedString) ? "\n" + failedString + "\nkonnten nicht aktualisiert werden" : "")).getButton(closeButtonId).setVisibility(View.VISIBLE);
                Database.saveAll();
                doubleClick[0] = false;
            } else {
                progressDialog.setText(finishedCount[0] + "/" + allCount + " wurden aktualisiert");
                Video nextVideo = updateList.next(currentVideo[0]);
                if (updateList.isFirst(nextVideo)) return;
                currentVideo[0] = nextVideo;
                loadDetails[0].run();
            }
        };
        loadDetails[0] = () -> {
            int tmdbId = currentVideo[0].getTmdbId();
            if (tmdbId == 0) {
                failedList.add(currentVideo[0]);
                isFinished.run();
                return;
            }
            String requestUrl = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&append_to_response=credits%2Crelease_dates";

            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
                boolean isFailed = false;
                try {
                    if (response.has("runtime"))
                        currentVideo[0].setLength(response.getInt("runtime"));
                    if (response.has("imdb_id"))
                        currentVideo[0].setImdbId(response.getString("imdb_id"));
                } catch (JSONException ignored) {
                    isFailed = true;
                }

                try {
                    if (response.has("release_dates")) {
                        JSONArray array = response.getJSONObject("release_dates").getJSONArray("results");
                        for (int i = 0; i < array.length(); i++) {
                            if (array.getJSONObject(i).getString("iso_3166_1").equals("DE")) {
                                JSONArray releaseDates = array.getJSONObject(i).getJSONArray("release_dates");
                                for (int i1 = 0; i1 < releaseDates.length(); i1++) {
                                    if (Utility.stringExists(releaseDates.getJSONObject(i1).get("certification").toString()))
                                        currentVideo[0].setAgeRating(releaseDates.getJSONObject(i1).getInt("certification"));
                                }
                                break;
                            }
                        }
                    }
                } catch (JSONException ignored) {
                    isFailed = true;
                }

                try {
                    if (response.has("production_companies")) {
                        JSONArray companies = response.getJSONArray("production_companies");
                        com.finn.androidUtilities.CustomList<ParentClass_Tmdb> tempStudioList = new com.finn.androidUtilities.CustomList<>();

                        for (int i = 0; i < companies.length(); i++) {
                            JSONObject object = companies.getJSONObject(i);
                            String name = object.getString("name");

                            Optional<Studio> optional = database.studioMap.values().stream().filter(studio -> studio.getName().equals(name)).findFirst();

                            if (optional.isPresent()) {
                                if (!currentVideo[0].getStudioList().contains(optional.get().getUuid()))
                                    currentVideo[0].getStudioList().add(optional.get().getUuid());
                            } else {
                                ParentClass_Tmdb studio = (ParentClass_Tmdb) new Studio(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("logo_path"));
                                if (studio.getImagePath().equals("null"))
                                    studio.setImagePath(null);

                                tempStudioList.add(studio);
                            }
                        }
                        currentVideo[0]._setTempStudioList(tempStudioList);
                    }

                } catch (JSONException ignored) {
                    isFailed = true;
                }

                try {
                    if (response.has("runtime"))
                        currentVideo[0].setLength(response.getInt("runtime"));
                    if (response.has("imdb_id"))
                        currentVideo[0].setImdbId(response.getString("imdb_id"));
                    if (response.has("release_date"))
                        currentVideo[0].setRelease(new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(response.getString("release_date")));
                    if (response.has("poster_path"))
                        currentVideo[0].setImagePath(response.getString("poster_path"));
                    if (response.has("genres")) {
                        JSONArray genre_ids = response.getJSONArray("genres");
                        CustomList<Integer> integerList = new CustomList<>();
                        for (int i = 0; i < genre_ids.length(); i++) {
                            integerList.add(genre_ids.getJSONObject(i).getInt("id"));
                        }
                        Map<Integer, String> idUuidMap = database.genreMap.values().stream().filter(genre -> genre.getTmdbGenreId() != 0).collect(Collectors.toMap(Genre::getTmdbGenreId, ParentClass::getUuid));

                        CustomList<String> uuidList = integerList.map(idUuidMap::get).filter(Objects::nonNull, false);
                        uuidList.removeAll(currentVideo[0].getGenreList());
                        currentVideo[0].getGenreList().addAll(uuidList);
                    }
                } catch (Exception e) {
                    isFailed = true;
                }

                try {
                    JSONArray actors = response.getJSONObject("credits").getJSONArray("cast");
                    com.finn.androidUtilities.CustomList<ParentClass_Tmdb> tempCastList = new com.finn.androidUtilities.CustomList<>();

                    if (actors.length() != 0) {
                        for (int i = 0; i < actors.length(); i++) {
                            JSONObject object = actors.getJSONObject(i);
                            String name = object.getString("name");

                            Optional<Darsteller> optional = database.darstellerMap.values().stream().filter(darsteller -> darsteller.getName().equals(name)).findFirst();

                            if (optional.isPresent()) {
                                if (!currentVideo[0].getDarstellerList().contains(optional.get().getUuid()))
                                    currentVideo[0].getDarstellerList().add(optional.get().getUuid());
                            } else {
                                ParentClass_Tmdb actor = (ParentClass_Tmdb) new Darsteller(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("profile_path"));
                                if (actor.getImagePath().equals("null"))
                                    actor.setImagePath(null);

                                tempCastList.add(actor);
                            }
                        }

                        currentVideo[0]._setTempCastList(tempCastList);
                    }
                } catch (JSONException ignored) {
                    isFailed = true;
                }

//                if (updateCast) {
//                    String castRequestUrl = "https://api.themoviedb.org/3/movie/" +
//                            tmdbId +
//                            "/credits?api_key=09e015a2106437cbc33bf79eb512b32d";
//
//                    CustomList<ParentClass_Tmdb> tempCastList = new CustomList<>();
//
//                    JsonObjectRequest castJsonArrayRequest = new JsonObjectRequest(Request.Method.GET, castRequestUrl, null, castRresponse -> {
//                        JSONArray results;
//                        try {
//                            results = castRresponse.getJSONArray("cast");
//
//                            if (results.length() == 0) {
//                                return;
//                            }
//                            for (int i = 0; i < results.length(); i++) {
//                                JSONObject object = results.getJSONObject(i);
//                                String name = object.getString("name");
//
//                                Optional<Darsteller> optional = database.darstellerMap.values().stream().filter(darsteller -> darsteller.getName().equals(name)).findFirst();
//
//                                if (optional.isPresent()) {
//                                    if (!currentVideo[0].getDarstellerList().contains(optional.get().getUuid()))
//                                        currentVideo[0].getDarstellerList().add(optional.get().getUuid());
//                                } else {
//                                    ParentClass_Tmdb actor = (ParentClass_Tmdb) new Darsteller(name).setTmdbId(object.getInt("id")).setImagePath(object.getString("profile_path"));
//                                    if (actor.getImagePath().equals("null"))
//                                        actor.setImagePath(null);
//
//                                    tempCastList.add(actor);
//                                }
//                            }
//
//                            currentVideo[0]._setTempCastList(tempCastList);
//                        } catch (JSONException ignored) {
//                        }
//
////                        new Handler().postDelayed(isFinished, 1000);
//                        isFinished.run();
//
//                    }, error -> isFinished.run());
//
//                    requestQueue.add(castJsonArrayRequest);
//
//                } else
////                    new Handler().postDelayed(isFinished, 1000);


                if (isFailed)
                    failedList.add(currentVideo[0]);

                isFinished.run();


            }, error -> {
                failedList.add(currentVideo[0]);
                isFinished.run();
            });

            requestQueue.add(jsonArrayRequest);

        };

        loadDetails[0].run();

    }
    /**  <------------------------- Videos Aktualisieren -------------------------  */


    /** ------------------------- ExpendableToolbar -------------------------> */
    public static Runnable applyExpendableToolbar_recycler(Context context, RecyclerView recycler, Toolbar toolbar, AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout, TextView noItem, String title) {
        final boolean[] canExpand = {true};
        int tolerance = 50;
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    canExpand[0] = recycler.computeVerticalScrollOffset() <= tolerance;
                    recycler.setNestedScrollingEnabled(canExpand[0]);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (canExpand[0] && recycler.computeVerticalScrollOffset() > tolerance) {
                    canExpand[0] = false;
                    recycler.setNestedScrollingEnabled(canExpand[0]);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (params.getBehavior() == null)
            params.setBehavior(new AppBarLayout.Behavior());
        AppBarLayout.Behavior behaviour = (AppBarLayout.Behavior) params.getBehavior();
        behaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return canExpand[0];
            }
        });

        return generateApplyToolBarTitle(context, toolbar, appBarLayout, collapsingToolbarLayout, noItem, title);
    }

    public static void applyExpendableToolbar_scrollView(Context context, NestedScrollView scrollView, AppBarLayout appBarLayout) {
        final boolean[] canExpand = {true};
        final boolean[] touched = {false};
        final boolean[] scrolled = {false};
        Runnable[] check = {() -> {
        }};
        NestedScrollView newScrollView = new NestedScrollView(context) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN)
                    touched[0] = true;
                else if (ev.getAction() == MotionEvent.ACTION_UP)
                    touched[0] = false;

                check[0].run();
                return super.dispatchTouchEvent(ev);
            }

        };
        Utility.replaceView(scrollView, newScrollView, (source, target) -> {
            target.setId(source.getId());
            target.setLayoutParams(source.getLayoutParams());
            while (source.getChildCount() > 0) {
                View child = source.getChildAt(0);
                source.removeViewAt(0);
                target.addView(child);
            }
        });

        int tolerance = 50;
        check[0] = () -> {
            if (!canExpand[0] && !scrolled[0] && !touched[0]) {
                canExpand[0] = true;
                newScrollView.setNestedScrollingEnabled(canExpand[0]);
            } else if (canExpand[0] && scrolled[0] && touched[0]) {
                canExpand[0] = false;
                newScrollView.setNestedScrollingEnabled(canExpand[0]);
            }
        };

        newScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            scrolled[0] = scrollY > tolerance;
            check[0].run();
        });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (params.getBehavior() == null)
            params.setBehavior(new AppBarLayout.Behavior());
        AppBarLayout.Behavior behaviour = (AppBarLayout.Behavior) params.getBehavior();
        behaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return canExpand[0];
            }
        });
    }

    private static Runnable generateApplyToolBarTitle(Context context, Toolbar toolbar, AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout, TextView noItem, String title) {
        return () -> {
            final float[] maxOffset = {-1};
            float distance = noItem.getY() - appBarLayout.getBottom();
            int stepCount = 5;
            final int[] prevPart = {-1};

            List<String> ellipsedList = new ArrayList<>();

            appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
                if (maxOffset[0] == -1) {
                    maxOffset[0] = -appBarLayout.getTotalScrollRange();
                    int maxWidth = Utility.getViewsByType(toolbar, ActionMenuView.class).get(0).getLeft() - Utility.getViewsByType(toolbar, AppCompatImageButton.class).get(0).getRight(); //320
                    for (int i = 0; i <= stepCount; i++)
                        ellipsedList.add(Utility.getEllipsedString(context, title, maxWidth - CustomUtility.dpToPx(3) - (int) (55 * ((stepCount - i) / (double) stepCount)), 18 + (int) (16 * (i / (double) stepCount))));
                }

                int part = stepCount - Math.round(verticalOffset / (maxOffset[0] / stepCount));
                if (part != prevPart[0])
                    collapsingToolbarLayout.setTitle(ellipsedList.get(prevPart[0] = part));

                float alpha = 1f - ((verticalOffset - maxOffset[0]) / distance);
                noItem.setAlpha(Math.max(alpha, 0f));
            });
        };
    }
    /**  <------------------------- ExpendableToolbar -------------------------  */

    /** ------------------------- Arrays -------------------------> */
    public static int getIndexByString(Context context, int arrayId, String language) {
        String[] array = context.getResources().getStringArray(arrayId);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(language))
                return i;
        }
        return 0;
    }

    public static String getStringByIndex(Context context, int arrayId, int index) {
        return context.getResources().getStringArray(arrayId)[index];
    }

    // --------------- VarArgs

    //    private static <T> T get0(Class<T> t,T[] test) {
//        return (T) test[0];
//    }
//
//    public static <T> boolean easyVarArgs(T[] varArg, int index, CustomUtility.GenericInterface<T> ifExists) {
//        if (varArg.length >= (index + 1)) {
//            T t;
//            if ((t = varArg[index]) != null) {
//                ifExists.run(t);
//                return true;
//            }
//        }
//        return false;
//    }
//
    public static <T> T easyVarArgsOrElse(int index, @Nullable CustomUtility.GenericReturnOnlyInterface<T> orElse, T... varArg) {
        if (varArg != null) {
            if (varArg.length > index) {
                T t;
                if ((t = varArg[index]) != null)
                    return t;
            }
        }
        return orElse == null ? null : orElse.run();
    }
    /**  <------------------------- Arrays -------------------------  */

    /** ------------------------- Maps -------------------------> */
    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    /**  <------------------------- Maps -------------------------  */
}
