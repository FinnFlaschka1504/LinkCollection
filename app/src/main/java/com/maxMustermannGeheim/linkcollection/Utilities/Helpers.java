package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;
import com.maxMustermannGeheim.linkcollection.R;
import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.CustomAutoCompleteAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter.ImageAdapterItem;

import org.intellij.lang.annotations.Language;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
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
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class Helpers {
    //  ----- TextInput ----->
    public static class TextInputHelper {
        // https://github.com/HITGIF/TextFieldBoxes
        public enum INPUT_TYPE {
            TEXT(0x00000001), NUMBER(0x00000002), NUMBER_DECIMAL(0x00002002), CAP_SENTENCES(0x00004001),
            CAPS_LOCK(0x00001001), CAPS_WORD(0x00002001), MULTI_LINE(0x00020001), E_MAIL(0x00000021), PASSWORD(0x00000081), NUMBER_PASSWORD(0x00000012), DATE_TIME(0x00000004), DATE(0x00000014), TIME(0x00000024);

            int code;

            INPUT_TYPE(int code) {
                this.code = code;
            }
        }

        public enum IME_ACTION {
            GO(0x00000002), SEARCH(0x00000003), SEND(0x00000004), NEXT(0x00000005), DONE(0x00000006), PREVIOUS(0x00000007);

            int code;

            IME_ACTION(int code) {
                this.code = code;
            }

            public int getCode() {
                return code;
            }
        }

        private CustomList<TextInputLayout> layoutList;
        private Map<TextInputLayout, Validator> inputValidationMap = new HashMap<>();
        private OnValidationResult onValidationResult;
        private boolean valid;
        private INPUT_TYPE defaultInputType = INPUT_TYPE.CAP_SENTENCES;
        private Validator.STATUS status;


        public TextInputHelper(Button buttonToBind, TextInputLayout... inputLayouts) {
            this.onValidationResult = buttonToBind::setEnabled;
            this.layoutList = new CustomList<>(inputLayouts);
            inputValidationMap = this.layoutList.stream().collect(Collectors.toMap(o -> o, Validator::new));
            applyValidationListeners();
            layoutList.forEach(textInputLayout -> {
                if (textInputLayout.getEditText().getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE))
                    textInputLayout.getEditText().setInputType(defaultInputType.code);
            });
        }

        public TextInputHelper(OnValidationResult onValidationResult, TextInputLayout... inputLayouts) {
            this.onValidationResult = onValidationResult;
            this.layoutList = new CustomList<>(inputLayouts);
            inputValidationMap = this.layoutList.stream().collect(Collectors.toMap(o -> o, Validator::new));
            applyValidationListeners();
            layoutList.forEach(textInputLayout -> {
                if (textInputLayout.getEditText().getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE))
                    textInputLayout.getEditText().setInputType(defaultInputType.code);
            });
        }

        public TextInputHelper() {
        }

        //  ----- Validation ----->
        public Validator.STATUS validate(TextInputLayout... layoutLists) {
            List<TextInputLayout> inputLayoutList = new CustomList<>(layoutLists);
            status = Validator.STATUS.VALID;
            for (Map.Entry<TextInputLayout, Validator> entry : inputValidationMap.entrySet()) {
                Validator.STATUS validate = entry.getValue().validate(entry.getKey().getEditText().getText().toString().trim(),
                        inputLayoutList.contains(entry.getKey()) || layoutLists.length == 0);
                if (validate.getLevel() > status.getLevel()) {
                    status = validate;
                }
            }
            valid = status.isValid();
            if (onValidationResult != null)
                onValidationResult.runOnValidationResult(status.isValid());
            return status;
        }

        public TextInputHelper setValidation(TextInputLayout textInputLayout, TextValidation textValidation) {
            inputValidationMap.get(textInputLayout).setTextValidation(textValidation);
            return this;
        }

        public TextInputHelper setValidation(TextInputLayout textInputLayout, String regEx) {
            inputValidationMap.get(textInputLayout).setRegEx(regEx);
            return this;
        }

        public TextInputHelper addValidator(@NonNull TextInputLayout... textInputLayouts) {
            for (TextInputLayout textInputLayout : textInputLayouts) {
                inputValidationMap.put(textInputLayout, new Validator(textInputLayout));
                applyValidationListener(textInputLayout);
                if (textInputLayout.getEditText().getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE))
                    textInputLayout.getEditText().setInputType(defaultInputType.code);
            }
            return this;
        }

        public void applyValidationListeners(TextInputLayout... inputLayouts) {
            if (inputLayouts.length > 0) {
                for (TextInputLayout inputLayout : inputLayouts)
                    applyValidationListener(inputLayout);
            } else {
                for (TextInputLayout inputLayout : inputValidationMap.keySet())
                    applyValidationListener(inputLayout);
            }
        }

        private void applyValidationListener(TextInputLayout textInputLayout) {
            textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    validate(textInputLayout);
                }
            });
        }

        public boolean isValid() {
            return valid;
        }

        public TextInputHelper removeStandardValidation(TextInputLayout... textInputLayouts) {
            for (TextInputLayout textInputLayout : textInputLayouts) {
                inputValidationMap.get(textInputLayout).alwaysUseDefaultValidation = false;
            }
            return this;
        }

        public TextInputHelper allowEmpty(@NonNull TextInputLayout... textInputLayouts) {
            for (TextInputLayout textInputLayout : textInputLayouts) {
                inputValidationMap.get(textInputLayout).allowEmpty = true;
            }
            return this;
        }

        public TextInputHelper warnIfEmpty(@NonNull TextInputLayout... textInputLayouts) {
            for (TextInputLayout textInputLayout : textInputLayouts) {
                inputValidationMap.get(textInputLayout).warnIfEmpty = true;
            }
            return this;
        }

        public interface TextValidation {
            void runTextValidation(Validator validator, String text);
        }

        public static class Validator {
            enum STATUS {
                NONE(false, false, 3), VALID(true, true, 0), INVALID(false, false, 2), WARNING(true, false, 1);

                private boolean valid;
                private boolean alwaysValid;
                private int level;

                STATUS(boolean valid, boolean alwaysValid, int level) {
                    this.valid = valid;
                    this.alwaysValid = alwaysValid;
                    this.level = level;
                }

                public boolean isValid() {
                    return valid;
                }

                public boolean isAlwaysValid() {
                    return alwaysValid;
                }

                public int getLevel() {
                    return level;
                }
            }

            enum MODE {
                WHITE_LIST, BLACK_LIST
            }

            private boolean alwaysUseDefaultValidation = true;
            private STATUS status = STATUS.NONE;
            private MODE mode;
            private MODE defaultMode = MODE.BLACK_LIST;
            private String message = "<Fehler>";
            private TextValidation textValidation;
            private TextInputLayout textInputLayout;
            private TextWatcher textWatcher;
            private boolean useDefaultValidation = true;
            private boolean allowEmpty;
            private boolean warnIfEmpty;
            private String regEx = "";
            private int warningColor = 0xffFFB300;
            private int errorColor = 0xFFFF7043;

            public Validator(TextInputLayout textInputLayout) {
                this.textInputLayout = textInputLayout;
            }

            private void reset() {
                message = null;
                status = STATUS.NONE;
                useDefaultValidation = alwaysUseDefaultValidation;
                message = "<Fehler>";
                mode = defaultMode;
            }

            private void defaultValidation(String text, boolean changeErrorMessage) {
                if (text.isEmpty() && !allowEmpty && !warnIfEmpty) {
                    if (changeErrorMessage)
                        message = "Das Feld darf nicht leer sein!";
                    status = STATUS.INVALID;
                } else if (text.isEmpty() && !allowEmpty && warnIfEmpty) {
                    if (changeErrorMessage)
                        message = "Das Feld ist leer!";
                    status = STATUS.WARNING;
                } else {
                    switch (mode) {
                        case BLACK_LIST:
                            if (changeErrorMessage)
                                message = null;
                            status = STATUS.VALID;
                            break;
                        case WHITE_LIST:
                            if (changeErrorMessage)
                                message = "Ungültige Eingabe";
                            status = STATUS.INVALID;
                            break;
                    }
                }
            }

            public STATUS validate(String text, boolean changeErrorMessage) {
                reset();

                if (textValidation != null && regEx.isEmpty())
                    textValidation.runTextValidation(this, text);

                if (!regEx.isEmpty()) {
                    if (text.matches(regEx)) {
                        status = STATUS.VALID;
                        message = null;
                    } else {
                        status = STATUS.INVALID;
                        message = "Ungültige Eingabe";
                    }
                }

                if (status == STATUS.NONE && useDefaultValidation)
                    defaultValidation(text, changeErrorMessage);

                if (changeErrorMessage)
                    textInputLayout.setError(message);


                switch (status) {
                    case WARNING:
                        setMessageColor(warningColor);
                        return STATUS.WARNING;
                    case VALID:
                        return STATUS.VALID;

                    default:
                    case INVALID:
                        setMessageColor(errorColor);
                        return STATUS.INVALID;
                }
            }

            private void setMessageColor(int color) {
                int[][] states = {{android.R.attr.state_enabled}};
                int[] colors = {color};
                textInputLayout.setErrorTextColor(new ColorStateList(states, colors));
                if (textInputLayout.hasFocus()) {
                    Pair<Integer, Integer> selection = Pair.create(textInputLayout.getEditText().getSelectionStart(), textInputLayout.getEditText().getSelectionEnd());
                    textInputLayout.setVisibility(View.GONE);
                    textInputLayout.setVisibility(View.VISIBLE);
                    textInputLayout.requestFocus();
                    textInputLayout.getEditText().setSelection(selection.first, selection.second);
                } else {
                    textInputLayout.setEnabled(false);
                    textInputLayout.setEnabled(true);
                }

            }

            public Validator setTextValidation(TextValidation textValidation) {
                this.textValidation = textValidation;
                return this;
            }

            public TextWatcher getTextWatcher() {
                return textWatcher;
            }

            public Validator setTextWatcher(TextWatcher textWatcher) {
                this.textWatcher = textWatcher;
                return this;
            }

            public void setValid() {
                message = null;
                status = STATUS.VALID;
            }

            public void setInvalid(String errorMessage) {
                this.message = errorMessage;
                status = STATUS.INVALID;
            }

            public void setWarning(String warningMessage) {
                this.message = warningMessage;
                status = STATUS.WARNING;
            }

            public void asWhiteList() {
                mode = MODE.WHITE_LIST;
            }

            public void asBlackList() {
                mode = MODE.BLACK_LIST;
            }

            public Validator setDefaultMode(MODE defaultMode) {
                this.defaultMode = defaultMode;
                return this;
            }

            public Validator allowEmpty() {
                this.allowEmpty = true;
                return this;
            }

            public Validator setRegEx(String regEx) {
                this.regEx = regEx;
                return this;
            }

            public Validator setErrorColor(int errorColor) {
                this.errorColor = errorColor;
                return this;
            }

            public Validator setWarningColor(int warningColor) {
                this.warningColor = warningColor;
                return this;
            }

            public String getMessage() {
                return message;
            }
        }

        public interface OnValidationResult {
            void runOnValidationResult(boolean result);
        }
        //  <----- Validation -----


        //  ----- Action ----->
        public TextInputHelper addActionListener(TextInputLayout textInputLayout, OnAction onAction, IME_ACTION... actions) {
            textInputLayout.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                boolean handled = false;
                if (actions.length == 0) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                        onAction.runOnAction(this, textInputLayout, actionId, textInputLayout.getEditText().getText().toString().trim());
                        handled = true;
                    }
                } else {
                    if (new CustomList<IME_ACTION>(actions).map(IME_ACTION::getCode).contains(actionId)) {
                        onAction.runOnAction(this, textInputLayout, actionId, textInputLayout.getEditText().getText().toString().trim());
                        handled = true;
                    }
                }
                return handled;
            });
            if (actions.length == 0) {
                textInputLayout.getEditText().setImeOptions(IME_ACTION.DONE.code);
            } else {
                final int[] actionFlag = {actions[0].code};
                new CustomList<IME_ACTION>(actions).forEachCount((imeAction, count) -> {
                    if (count == 0) return;

                    actionFlag[0] = actionFlag[0] | imeAction.code;
                });
                textInputLayout.getEditText().setImeOptions(actionFlag[0]);
            }
            return this;
        }

        public interface OnAction {
            void runOnAction(TextInputHelper textInputHelper, TextInputLayout textInputLayout, int actionId, String text);
        }
        //  <----- Action -----


        //  ----- Convenience ----->
        public TextInputHelper disableSelectAll(TextInputLayout textInputLayout) {
            textInputLayout.getEditText().setSelectAllOnFocus(false);
            return this;
        }

        public TextInputHelper setInputType(TextInputLayout textInputLayout, INPUT_TYPE inputType) {
            textInputLayout.getEditText().setInputType(inputType.code);
            return this;
        }

        public TextInputHelper setDefaultInputType(INPUT_TYPE defaultInputType) {
            this.defaultInputType = defaultInputType;
            return this;
        }

        public TextInputHelper setOnValidationResult(OnValidationResult onValidationResult) {
            this.onValidationResult = onValidationResult;
            return this;
        }

        public TextInputHelper bindButton(Button buttonToBind) {
            onValidationResult = buttonToBind::setEnabled;
            buttonToBind.callOnClick();
            View.OnClickListener onClickListener = v -> {
            };
            return this;
        }

        public TextInputHelper bindButton(CustomDialog.ButtonHelper buttonToBind) {
            onValidationResult = buttonToBind::setEnabled;
            return this;
        }

        public TextInputHelper bindButton(com.finn.androidUtilities.CustomDialog.ButtonHelper buttonToBind) {
            onValidationResult = buttonToBind::setEnabled;
            return this;
        }

        public TextInputHelper defaultDialogValidation(CustomDialog customDialog) {
            bindButton(customDialog.getActionButton());
            return this;
        }

        public TextInputHelper defaultDialogValidation(com.finn.androidUtilities.CustomDialog customDialog) {
            bindButton(customDialog.getActionButton());
            return this;
        }

        public TextInputHelper setText(TextInputLayout textInputLayout, String text) {
            if (textInputLayout.getEditText() != null) {
                textInputLayout.getEditText().setText(text);
            }
            return this;
        }

        public String getText(TextInputLayout textInputLayout) {
            if (textInputLayout.getEditText() != null) {
                return textInputLayout.getEditText().getText().toString();
            }
            return null;
        }

        public TextInputHelper interceptDialogActionForValidation(CustomDialog customDialog, Runnable... onIntercept_onAllow) {
            return interceptForValidation(customDialog.getActionButton().getButton(), onIntercept_onAllow);
        }

        public TextInputHelper interceptForValidation(View view, Runnable... onIntercept_onAllow) {
            final long[] time = {0};
            Utility.interceptOnClick(view, view1 -> {
                long currentTime = System.currentTimeMillis();
                boolean alwaysValid = validate().isAlwaysValid();
                if (currentTime - time[0] > 500 && !alwaysValid) {
                    time[0] = currentTime;
                    if (onIntercept_onAllow.length > 0)
                        onIntercept_onAllow[0].run();
                    return true;
                }
                if (onIntercept_onAllow.length > 1)
                    onIntercept_onAllow[1].run();
                return false;
            });
            return this;
        }

        public List<String> getMessage(TextInputLayout... textInputLayouts) {
            List<String> messageList = new ArrayList<>();
            if (textInputLayouts.length == 0)
                textInputLayouts = inputValidationMap.keySet().toArray(new TextInputLayout[0]);
            for (TextInputLayout textInputLayout : textInputLayouts) {
                String message = inputValidationMap.get(textInputLayout).getMessage();
                if (message != null)
                    messageList.add(message);
            }
            return messageList;
        }
        //  <----- Convenience -----
    }
    //  <----- TextInput -----


    //  --------------- SpannableString --------------->
    public static class SpannableStringHelper {
        public enum SPAN_TYPE {
            BOLD(o -> new StyleSpan(Typeface.BOLD)), ITALIC(o -> new StyleSpan(Typeface.ITALIC)), BOLD_ITALIC(o -> new StyleSpan(Typeface.BOLD_ITALIC)), STRIKE_THROUGH(o -> new StrikethroughSpan()),
            UNDERLINED(o -> new UnderlineSpan()), COLOR(color -> new ForegroundColorSpan((Integer) color)), RELATIVE_SIZE(size -> new RelativeSizeSpan((Float) size)), NONE(o -> null);

            CustomUtility.GenericReturnInterface<Object, ParcelableSpan> what;

            SPAN_TYPE(CustomUtility.GenericReturnInterface<Object, ParcelableSpan> what) {
                this.what = what;
            }

            public ParcelableSpan getWhat() {
                return what.run(null);
            }

            public ParcelableSpan getWhat(Object o) {
                return what.run(o);
            }
        }

        public static class MultipleSpans {
            Set<ParcelableSpan> spanSet = new HashSet<>();

            public MultipleSpans bold() {
                spanSet.add(SPAN_TYPE.BOLD.getWhat());
                return this;
            }

            public MultipleSpans italic() {
                spanSet.add(SPAN_TYPE.ITALIC.getWhat());
                return this;
            }

            public MultipleSpans boldItalic() {
                spanSet.add(SPAN_TYPE.BOLD_ITALIC.getWhat());
                return this;
            }

            public MultipleSpans strikeThrough() {
                spanSet.add(SPAN_TYPE.STRIKE_THROUGH.getWhat());
                return this;
            }

            public MultipleSpans underlined() {
                spanSet.add(SPAN_TYPE.UNDERLINED.getWhat());
                return this;
            }

            public MultipleSpans color(@ColorInt int color) {
                spanSet.add(SPAN_TYPE.COLOR.getWhat(color));
                return this;
            }

            public MultipleSpans relativeSize(float size) {
                spanSet.add(SPAN_TYPE.RELATIVE_SIZE.getWhat(size));
                return this;
            }

            // ---------------

            private void apply(CharSequence text, SpannableStringBuilder builder) {
                int previousLength = builder.length();
                int textLength = text.length();
                builder.append(text);
                for (ParcelableSpan span : spanSet) {
                    builder.setSpan(span, previousLength, previousLength + textLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        private SpannableStringBuilder builder = new SpannableStringBuilder();


        //  ------------------------- Append ------------------------->
        public SpannableStringHelper append(CharSequence text) {
            builder.append(text);
            return this;
        }

        public SpannableStringHelper append(CharSequence text, SPAN_TYPE span) {
            builder.append(text, span.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper append(CharSequence text, ParcelableSpan span) {
            builder.append(text, span, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendMultiple(CharSequence text, CustomUtility.GenericReturnInterface<MultipleSpans, MultipleSpans> multipleSpans) {
            multipleSpans.run(new MultipleSpans()).apply(text, builder);
            return this;
        }

        public SpannableStringHelper appendBold(CharSequence text) {
            builder.append(text, SPAN_TYPE.BOLD.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendItalic(CharSequence text) {
            builder.append(text, SPAN_TYPE.ITALIC.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendBoldItalic(CharSequence text) {
            builder.append(text, SPAN_TYPE.BOLD_ITALIC.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendStrikeThrough(CharSequence text) {
            builder.append(text, SPAN_TYPE.STRIKE_THROUGH.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendUnderlined(CharSequence text) {
            builder.append(text, SPAN_TYPE.UNDERLINED.getWhat(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendColor(CharSequence text, @ColorInt int color) {
            builder.append(text, SPAN_TYPE.COLOR.getWhat(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendRelativeSize(CharSequence text, float size) {
            builder.append(text, SPAN_TYPE.RELATIVE_SIZE.getWhat(size), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return this;
        }

        public SpannableStringHelper appendCustom(CustomUtility.GenericInterface<SpannableStringBuilder> applyCustomSpan) {
            applyCustomSpan.run(builder);
            return this;
        }
        //  <------------------------- Append -------------------------

        public SpannableStringBuilder get() {
            return builder;
        }


        //  --------------- Quick... --------------->
        private Object quickWhat;

        public SpannableStringHelper setQuickWhat(Object quickWhat) {
            this.quickWhat = quickWhat;
            return this;
        }

        public SpannableStringBuilder quick(CharSequence text) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            spannableStringBuilder.setSpan(quickWhat, 0, text.length(), 0);
            return spannableStringBuilder;
        }

        public SpannableStringBuilder quick(CharSequence text, Object what) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            spannableStringBuilder.setSpan(what, 0, text.length(), 0);
            return spannableStringBuilder;
        }

        public SpannableStringBuilder quickBold(CharSequence text) {
            SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
            spannableString.setSpan(SPAN_TYPE.BOLD.getWhat(), 0, text.length(), 0);
            return spannableString;
        }

        public SpannableStringBuilder quickItalic(CharSequence text) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            spannableStringBuilder.setSpan(SPAN_TYPE.ITALIC.getWhat(), 0, text.length(), 0);
            return spannableStringBuilder;
        }

        public SpannableStringBuilder quickStrikeThrough(CharSequence text) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            spannableStringBuilder.setSpan(SPAN_TYPE.STRIKE_THROUGH.getWhat(), 0, text.length(), 0);
            return spannableStringBuilder;
        }
        //  <--------------- Quick... ---------------

        //  ------------------------- Builder ------------------------->
        public interface SpannableStringHelperInterface {
            SpannableStringHelper get(SpannableStringHelper spanBuilder);
        }

        public static SpannableStringBuilder Builder(SpannableStringHelperInterface spanBuilderInterface) {
            return spanBuilderInterface.get(new SpannableStringHelper()).get();
        }
        //  <------------------------- Builder -------------------------

        public static CharSequence highlightText(String search, String originalText) {
            if (CustomUtility.stringExists(search) && CustomUtility.stringExists(originalText)) {
                search = search.toLowerCase();
                String normalizedText = originalText.toLowerCase(); //Normalizer.normalize(originalText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                int start = normalizedText.indexOf(search);
                if (start < 0) {
                    return originalText;
                } else {
                    Spannable highlighted = new SpannableString(originalText);
                    while (start >= 0) {
                        int spanStart = Math.min(start, originalText.length());
                        int spanEnd = Math.min(start + search.length(), originalText.length());
                        highlighted.setSpan(new BackgroundColorSpan(0x40FFD03F), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        start = normalizedText.indexOf(search, spanEnd);
                    }
                    return highlighted;
                }
            }
            return originalText;
        }
    }
    //  <--------------- SpannableString ---------------


    //  ------------------------- SortHelper ------------------------->
    public static class SortHelper<T> {
        private List<T> list;
        private List<Sorter> sorterList = new ArrayList<>();
        private Boolean allReversed;

        public SortHelper() {
        }

        public SortHelper(List<T> list) {
            this.list = list;
        }

        public SortHelper<T> setList(List<T> list) {
            this.list = list;
            return this;
        }

        public Sorter<T> addSorter(Object type, Comparator<T> comparator) {
            return addSorter(comparator).setType(type);
        }

        public Sorter<T> addSorter(Object type) {
            return addSorter().setType(type);
        }

        public Sorter<T> addSorter() {
            Sorter<T> sorter = new Sorter<T>();
            sorter.parent = this;
            sorterList.add(sorter);
            return sorter;
        }

        public Sorter<T> addSorter(Comparator<T> comparator) {
            return addSorter().addCondition(comparator);
        }


        //  ------------------------- Sort ------------------------->
        public List<T> sort(Object type) {
            Optional<Sorter> optionalSorter = sorterList.stream().filter(sorter -> sorter.type.equals(type)).findFirst();
            return optionalSorter.map(sorter -> sorter.sort_private(list)).orElseGet(() -> list);
        }

        public List<T> sort_new(Object type) {
            Optional<Sorter> optionalSorter = sorterList.stream().filter(sorter -> sorter.type.equals(type)).findFirst();
            return optionalSorter.map(sorter -> sorter.sort_private(new ArrayList<T>(list))).orElseGet(() -> new ArrayList<>(list));
        }

        public interface GetSortType {
            Object runGetSortType();
        }

        public List<T> sort(GetSortType getSortType) {
            return sort(getSortType.runGetSortType());
        }

        public List<T> sort_new(GetSortType getSortType) {
            return sort_new(getSortType.runGetSortType());
        }
        //  <------------------------- Sort -------------------------

        public SortHelper<T> setAllReversed(boolean reversed) {
            allReversed = reversed;
            return this;
        }

        public class Sorter<E> {
            private SortHelper<T> parent;
            private boolean reversed;
            private boolean reverseDefaultComparable;
            private boolean reverseParentClass;
            private boolean nullToBottom = true;
            private List<Comparator<E>> comparatorList = new ArrayList<>();
            private List<Comparator<T>> comparatorUnchangedList = new ArrayList<>();
            private Object type;
            private ChangeType<T, E> changeType1 = t -> (E) t;
            private ChangeType<T, E> changeType2 = t -> (E) t;

            //  ------------------------- Sort ------------------------->
            public List<T> sort() {
                return sort_private(list);
            }

            public List<T> sort_new() {
                return sort_private(new ArrayList<>(list));
            }

            private List<T> sort_private(List<T> list) {
//                if (allReversed)
                if (allReversed != null)
                    sorterList.forEach(sorter -> sorter.reversed = allReversed);

                list.sort((o1, o2) -> {
                    int result = 0;

                    for (Comparator<T> comparator : comparatorUnchangedList) {
                        result = comparator.compare(o1, o2);

                        if (result != 0)
                            return result;
                    }

                    E newO1 = changeType1.runChangeType(o1);
                    E newO2 = changeType2.runChangeType(o2);

                    for (Comparator<E> comparator : comparatorList) {
//                        if (newO1 == null && newO2 == null && o1 instanceof ParentClass && o2 instanceof ParentClass)
//                            result = ((ParentClass) o1).getName().compareTo(((ParentClass) o2).getName());
//                        else if (newO1 == null)
//                            result = reversed ^ !nullToBottom ? -1 : 1;
//                        else if (newO2 == null)
//                            result = reversed ^ !nullToBottom ? 1 : -1;
//                        else
                        result = comparator.compare(newO1, newO2);

                        if (result != 0)
                            return result;
                    }

                    if (newO1 == null && newO2 == null && o1 instanceof ParentClass && o2 instanceof ParentClass)
                        result = ((ParentClass) o1).getName().compareTo(((ParentClass) o2).getName());
                    else if (newO1 == null)
                        result = reversed ^ !nullToBottom ? -1 : 1;
                    else if (newO2 == null)
                        result = reversed ^ !nullToBottom ? 1 : -1;

                    if (result == 0 && newO1 instanceof Comparable && newO2 instanceof Comparable) {
                        result = ((Comparable<E>) newO1).compareTo(newO2) * (reversed ^ reverseDefaultComparable ? -1 : 1);
                    }
                    if (result == 0 && newO1 instanceof ParentClass && newO2 instanceof ParentClass) {
                        result = ((ParentClass) newO1).getName().compareTo(((ParentClass) newO2).getName()) * (reversed ^ reverseParentClass ? -1 : 1);
                    }
                    if (result == 0 && o1 instanceof Comparable && o2 instanceof Comparable) {
                        result = ((Comparable<T>) o1).compareTo(o2) * (reversed ^ reverseDefaultComparable ? -1 : 1);
                    }
                    if (result == 0 && o1 instanceof ParentClass && o2 instanceof ParentClass) {
                        result = ((ParentClass) o1).getName().compareTo(((ParentClass) o2).getName()) * (reversed ^ reverseParentClass ? -1 : 1);
                    }

                    return result;
                });

                return list;
            }
            //  <------------------------- Sort -------------------------

            public Sorter<E> addCondition(Comparator<E> comparator) {
                comparatorList.add(comparator);
                return this;
            }

            public Sorter<E> addUnchangedCondition(Comparator<T> comparator) {
                comparatorUnchangedList.add(comparator);
                return this;
            }

            public Sorter<E> enableReversed() {
                this.reversed = true;
                return this;
            }

            public Sorter<E> enableReverseDefaultComparable() {
                this.reverseDefaultComparable = true;
                return this;
            }

            public Sorter<E> enableReverseParentClass() {
                this.reverseParentClass = true;
                return this;
            }

            public Sorter<E> disableNullToBottom() {
                this.nullToBottom = false;
                return this;
            }

            public Sorter<E> setType(Object type) {
                this.type = type;
                return this;
            }

            public Sorter<T> addSorter(Object type) {
                return parent.addSorter(type);
            }

            public Sorter<T> addSorter(Comparator<T> comparator) {
                return parent.addSorter(comparator);
            }

            public Sorter<T> addSorter(Object type, Comparator<T> comparator) {
                return parent.addSorter(type, comparator);
            }

            public SortHelper<T> finish() {
                return parent;
            }

            public List<T> finish_and_sort(Object type) {
                return parent.sort(type);
            }

            public <N> Sorter<N> changeType(ChangeType<T, N> changeType1, ChangeType<T, N> changeType2) {
                Sorter<N> nSorter = clone();
                nSorter.changeType1 = changeType1;
                nSorter.changeType2 = changeType2;
                return nSorter;
            }

            public <N> Sorter<N> changeType(ChangeType<T, N> changeType1) {
                Sorter<N> nSorter = clone();
                nSorter.changeType1 = changeType1;
                nSorter.changeType2 = changeType1;
                return nSorter;
            }

            @NonNull
            @Override
            protected Sorter clone() {
                try {
                    return (Sorter) super.clone();
                } catch (CloneNotSupportedException e) {
                    return this;
                }
            }
        }

        public interface ChangeType<T, N> {
            N runChangeType(T t);
        }

    }
    //  <------------------------- SortHelper -------------------------


    //  ------------------------- RatingHelper ------------------------->
    public static class RatingHelper {
        private FrameLayout layout;
        private SeekBar seekBar;
        private MaterialRatingBar ratingBar;
        private float rating;
        private int stars = 5;
        private float stepSize = 0.25f;
        private RatingBar.OnRatingBarChangeListener onRatingBarChangeListener;

        //  ------------------------- Constructors ------------------------->
        public RatingHelper(FrameLayout layout) {
            this.layout = layout;
            seekBar = layout.findViewById(R.id.customRating_seekBar);
            ratingBar = layout.findViewById(R.id.customRating_ratingBar);

            if (seekBar == null || ratingBar == null)
                return;

            initialize();
        }

        public RatingHelper(FrameLayout layout, SeekBar seekBar, MaterialRatingBar ratingBar) {
            this.layout = layout;
            this.seekBar = seekBar;
            this.ratingBar = ratingBar;
        }
        //  <------------------------- Constructors -------------------------


        //  ------------------------- Getters & Setters ------------------------->
        public FrameLayout getLayout() {
            return layout;
        }

        public RatingHelper setLayout(FrameLayout layout) {
            this.layout = layout;
            return this;
        }

        public SeekBar getSeekBar() {
            return seekBar;
        }

        public RatingHelper setSeekBar(SeekBar seekBar) {
            this.seekBar = seekBar;
            return this;
        }

        public MaterialRatingBar getRatingBar() {
            return ratingBar;
        }

        public RatingHelper setRatingBar(MaterialRatingBar ratingBar) {
            this.ratingBar = ratingBar;
            return this;
        }

        public RatingHelper setRating(float rating) {
            seekBar.setProgress((int) (rating * factor()));
            return this;
        }

        public float getRating() {
            return rating;
        }

        public int getStars() {
            return stars;
        }

        public RatingHelper setStars(int stars) {
            this.stars = stars;
            ratingBar.setNumStars(stars);
            seekBar.setMax((int) (stars * factor()));
            return this;
        }

        public float getStepSize() {
            return stepSize;
        }

        public RatingHelper setStepSize(float stepSize) {
            this.stepSize = stepSize;
            seekBar.setMax((int) (stars * factor()));
            return this;
        }

        public RatingHelper setOnRatingBarChangeListener(RatingBar.OnRatingBarChangeListener onRatingBarChangeListener) {
            this.onRatingBarChangeListener = onRatingBarChangeListener;
            return this;
        }
        //  <------------------------- Getters & Setters -------------------------


        RatingHelper initialize() {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    rating = ((float) progress) / factor();
                    ratingBar.setRating(rating);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (onRatingBarChangeListener != null)
                        onRatingBarChangeListener.onRatingChanged(ratingBar, rating, true);
                }
            });

            return this;
        }

        //  ------------------------- Convenience ------------------------->
        private float factor() {
            return 1f / stepSize;
        }

        public RatingHelper wrapContent() {
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ratingBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
            seekBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            return this;
        }

        public RatingHelper matchParent() {
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ratingBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            seekBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            return this;
        }

        public RatingHelper imitateOriginalSize() {
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ratingBar.setLayoutParams(new FrameLayout.LayoutParams(Utility.dpToPx(240), FrameLayout.LayoutParams.MATCH_PARENT));
            seekBar.setLayoutParams(new FrameLayout.LayoutParams(Utility.dpToPx(272), FrameLayout.LayoutParams.MATCH_PARENT));
            return this;
        }
        //  <------------------------- Convenience -------------------------

        public static RatingHelper inflate(Context context) {
            LinearLayout inflate = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.custom_rating, null);
            FrameLayout frameLayout = inflate.findViewById(R.id.customRating_layout);
            inflate.removeAllViews();
            return new RatingHelper(frameLayout);
        }
    }
    //  <------------------------- RatingHelper -------------------------


    //  ------------------------- WebViewHelper ------------------------->
    public static class WebViewHelper {
        public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";

        private String userAgent = USER_AGENT;
        private boolean debug;
        private boolean loadInvisibleDialog;
        private boolean destroyOnSuccess = true;
        private WebView webView;
        private com.finn.androidUtilities.CustomDialog customDialog;
        private Context context;
        private boolean mobileVersion;
        private String[] urls;
        private int openJs;
        private boolean dialogCanceled;
        private List<Pair<String, Utility.GenericInterface<String>>> requestList = new ArrayList<>();
        private ExecuteBeforeJavaScript executeBeforeJavaScript;
        private boolean alreadyLoaded;
        private boolean isRedirected;
        private Utility.GenericInterface<WebSettings> setSettings;
        private boolean showToasts = true;
        private int urlsIndex;
        private Runnable onAllComplete;
        private boolean destroyed;
        private boolean loadImages;

        //  ------------------------- Constructor ------------------------->
        public WebViewHelper(Context context, String... urls) {
            this.context = context;
            this.urls = urls;
        }

        public WebViewHelper(Context context, WebView webView, String... urls) {
            this.webView = webView;
            this.context = context;
            this.urls = urls;
        }
        //  <------------------------- Constructor -------------------------


        private void buildWebView() {
            if (showToasts)
                Toast.makeText(context, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();

            webView = new WebView(context);
            if (!debug)
                webView.setAlpha(0f);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            if (!mobileVersion) {
//                settings.setUserAgentString(userAgent);
                settings.setUseWideViewPort(true);
                settings.setLoadWithOverviewMode(true);
            }
//            if (!userAgent.equals(USER_AGENT))
            settings.setUserAgentString(userAgent);

            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            settings.setLoadWithOverviewMode(true);
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            settings.setDomStorageEnabled(true);

            if (!loadImages) {
                settings.setLoadsImagesAutomatically(false);
                settings.setBlockNetworkImage(true);
            }

            Utility.runGenericInterface(setSettings, settings);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) { // ToDo: https://stackoverflow.com/questions/18282892/android-webview-onpagefinished-called-twice
//                    boolean earlyExecution = false;
//                    CustomUtility.logD(null, "onPageFinished: %d", view.getProgress());
                    if (!dialogCanceled && !alreadyLoaded) {
                        if (view.getProgress() == 100) {
//                            CustomUtility.logTiming("WEB", false);
                            alreadyLoaded = true;
                            if (executeBeforeJavaScript == null)
                                onPageLoaded();
                            else
                                executeBeforeJavaScript();
                        } /*else {
                            CustomUtility.logTiming("WEB", true);
                            view.evaluateJavascript("document.querySelector(\"[data-testid='hero-title-block__metadata']\").innerText;", value -> {
                                CustomUtility.logTiming("WEB", true);
                                CustomUtility.logD(null, "onPageFinished: %s", value);
                            });
                        }*/
                    }
                }
            });

            loadNextPage();

            if (debug || loadInvisibleDialog)
                buildDialog();
        }

        private void loadNextPage() {
            if (destroyed) return;
            openJs = requestList.size();
            alreadyLoaded = false;
//            CustomUtility.logTiming("WEB", null);
            webView.loadUrl(urls[urlsIndex]);
            urlsIndex++;
        }

        public WebViewHelper addRequest(String javaScript, Utility.GenericInterface<String> onParseResult) {
            requestList.add(Pair.create(javaScript, onParseResult));
            return this;
        }

        public WebViewHelper addCommand(String javaScript) {
            requestList.add(Pair.create(javaScript, null));
            return this;
        }

        private void onPageLoaded() {
            Iterator<Pair<String, Utility.GenericInterface<String>>> iterator = requestList.iterator();
            new Runnable() {
                @Override
                public void run() {
                    if (destroyed) return;
                    if (iterator.hasNext()) {
                        Pair<String, Utility.GenericInterface<String>> pair = iterator.next();
                        WebViewHelper.this.evaluateJavaScript(pair.first, pair.second, this, 0);
                    }
                }
            }.run();

//            for (Pair<String, Utility.GenericInterface<String>> pair : requestList) {
//            }
        }

        private void evaluateJavaScript(String rawScript, Utility.GenericInterface<String> onParseResult, Runnable onComplete, int tryCount) {
            if (destroyed) return;

            Runnable onSuccess = () -> {
                if (openJs <= 1) {
                    openJs--;
                    if (urlsIndex < urls.length) {
                        loadNextPage();
                    } else {
                        if (onAllComplete != null)
                            onAllComplete.run();
                        if (!debug && destroyOnSuccess) {
                            webView.destroy();
                            if (customDialog != null) {
                                customDialog.dismiss();
                                customDialog = null;
                            }
                        }
                    }
                } else {
                    openJs--;
                    onComplete.run();
                }
            };

            webView.evaluateJavascript(WebViewHelper.wrapScript(rawScript), result -> {
                if (destroyed) return;
                if (onParseResult != null) {

                    boolean isNull = result.matches("null");
                    if (isNull && tryCount < 10) {
                        new Handler().postDelayed(() -> evaluateJavaScript(rawScript, onParseResult, onComplete, tryCount + 1), 100);
                    } else {
                        if (result.startsWith("\"") && result.endsWith("\""))
                            result = new Gson().fromJson(result, Object.class).toString();
                        onParseResult.run(isNull ? "<!NO_RESULT!>" : result);
                        onSuccess.run();
                    }
                } else
                    onSuccess.run();
            });

        }

        private void buildDialog() {
            customDialog = com.finn.androidUtilities.CustomDialog.Builder(context)
                    .setView(webView)
                    .addOptionalModifications(customDialog1 -> {
                        if (debug) {
                            customDialog1
                                    .setDimensions(true, true);
                        } else {
                            customDialog1
                                    .setDimensions(true, true)
                                    .removeBackground_and_margin()
                                    .enableDoubleClickOutsideToDismiss(customDialog -> true, "Daten werden geladen");
                        }
                    })
                    .setOnDialogDismiss(customDialog -> {
                        dialogCanceled = true;
                        ((ViewGroup) customDialog.findViewById(R.id.dialog_custom_layout_view_interface)).removeAllViews();
                    })
                    .disableScroll()
                    .show();

            Toast toast = Toast.makeText(context, "Daten werden geladen", Toast.LENGTH_SHORT);
            com.finn.androidUtilities.Helpers.DoubleClickHelper doubleClickHelper = com.finn.androidUtilities.Helpers.DoubleClickHelper.create()
                    .setOnFailed(toast::show)
                    .setOnSuccess(() -> {
                        toast.cancel();
                        Toast.makeText(context, "Abgebrochen", Toast.LENGTH_SHORT).show();
                    });
            webView.setOnTouchListener((v, event) -> {
                if (!debug && event.getAction() == MotionEvent.ACTION_UP) {
                    if (doubleClickHelper.check()) {
                        customDialog.dismiss();
                        dialogCanceled = true;
                    }
                    return true;
                } else
                    return false;
            });
        }

        public WebViewHelper go() {
            dialogCanceled = false;
            if (webView == null)
                buildWebView();
            else {
                if (debug || loadInvisibleDialog)
                    buildDialog();

                if (executeBeforeJavaScript == null)
                    onPageLoaded();
                else
                    executeBeforeJavaScript();
            }
            return this;
        }

        public void destroy() {
            destroyed = true;
            if (!debug) {
                webView.destroy();
                if (customDialog != null) {
                    customDialog.dismiss();
                    customDialog = null;
                }
            }
        }


        //  ------------------------- ExecuteBeforeJavaScript ------------------------->
        public interface ExecuteBeforeJavaScript {
            void runExecuteBeforeJavaScript(com.finn.androidUtilities.CustomDialog internetDialog, WebView webView, Runnable resume);
        }

        private void executeBeforeJavaScript() {
            executeBeforeJavaScript.runExecuteBeforeJavaScript(customDialog, webView, this::onPageLoaded);
        }
        //  <------------------------- ExecuteBeforeJavaScript -------------------------


        //  ------------------------- Getter & Setter ------------------------->
        public String getUserAgent() {
            return userAgent;
        }

        public WebViewHelper setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public boolean isDebug() {
            return debug;
        }

        public WebViewHelper setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public boolean isLoadInvisibleDialog() {
            return loadInvisibleDialog;
        }

        public WebViewHelper setLoadInvisibleDialog(boolean loadInvisibleDialog) {
            this.loadInvisibleDialog = loadInvisibleDialog;
            return this;
        }

        public boolean isDestroyOnSuccess() {
            return destroyOnSuccess;
        }

        public WebViewHelper setDestroyOnSuccess(boolean destroyOnSuccess) {
            this.destroyOnSuccess = destroyOnSuccess;
            return this;
        }

        public WebView getWebView() {
            return webView;
        }

        public WebViewHelper setWebView(WebView webView) {
            this.webView = webView;
            return this;
        }

        public com.finn.androidUtilities.CustomDialog getCustomDialog() {
            return customDialog;
        }

        public WebViewHelper setCustomDialog(com.finn.androidUtilities.CustomDialog customDialog) {
            this.customDialog = customDialog;
            return this;
        }

        public Context getContext() {
            return context;
        }

        public WebViewHelper setContext(Context context) {
            this.context = context;
            return this;
        }

        public boolean isMobileVersion() {
            return mobileVersion;
        }

        public WebViewHelper setMobileVersion(boolean mobileVersion) {
            this.mobileVersion = mobileVersion;
            return this;
        }

        public String[] getUrls() {
            return urls;
        }

        public WebViewHelper setUrls(String... urls) {
            this.urls = urls;
            return this;
        }

        public WebViewHelper setExecuteBeforeJavaScript(ExecuteBeforeJavaScript executeBeforeJavaScript) {
            this.executeBeforeJavaScript = executeBeforeJavaScript;
            return this;
        }

        public WebViewHelper setSetSettings(Utility.GenericInterface<WebSettings> setSettings) {
            this.setSettings = setSettings;
            return this;
        }

        public Runnable getOnAllComplete() {
            return onAllComplete;
        }

        public WebViewHelper setOnAllComplete(Runnable onAllComplete) {
            this.onAllComplete = onAllComplete;
            return this;
        }

        public WebViewHelper enableLoadImages() {
            this.loadImages = true;
            return this;
        }
        //  <------------------------- Getter & Setter -------------------------


        //  ------------------------- Convenience ------------------------->
        public static String wrapScript(String script) {
            if (script.startsWith("{") && script.endsWith("}")) {
                script = "(function() " + script + ")();";
            } else {
                if (!script.endsWith(";"))
                    script += ";";
            }
            return script;
        }

        public WebViewHelper addOptional(Utility.GenericInterface<WebViewHelper> addOptional) {
            addOptional.run(this);
            return this;
        }
        //  <------------------------- Convenience -------------------------
    }
    //  <------------------------- WebViewHelper -------------------------


    /** ------------------------- AdvancedSearch -------------------------> */
    public static class AdvancedQueryHelper<T> {
        public static final String ADVANCED_SEARCH_HISTORY = "ADVANCED_SEARCH_HISTORY";
        public static final String ADVANCED_SEARCH_CRITERIA_NAME = "n";
        @Language("RegExp")
        public static final String PARENT_CLASS_PATTERN = "([^|&\\[\\]]+?)([|&][^|&\\[\\]]+?)*";
        private static final Pattern advancedQueryPattern = Pattern.compile("\\{.*\\}");
        private SearchView searchView;
        public String fullQuery, advancedQuery, restQuery;
        private CustomList<SearchCriteria> criteriaList = new CustomList<>();
        private Utility.DoubleGenericInterface<String, CustomList<T>> restFilter;
        private int dialogLayoutId;
        private com.finn.androidUtilities.CustomDialog.OnDialogCallback optionalModifications;
        private AppCompatActivity context;
        private EditText editText;
        private TextWatcher colorationWatcher;
        private String historyKey;
        private CustomUtility.EventThrottler<Pair<String, CustomList<T>>> requestThrottler;

        /** ------------------------- Constructor -------------------------> */
        public AdvancedQueryHelper(AppCompatActivity context, SearchView searchView) {
            this.searchView = searchView;
            editText = searchView.findViewById(Resources.getSystem().getIdentifier("search_src_text", "id", "android"));
            if (editText instanceof AutoCompleteTextView && false) {
                AutoCompleteTextView autoComplete = (AutoCompleteTextView) editText;
                CustomList<ImageAdapterItem> itemList = Database.getInstance().darstellerMap.values().stream().map(darsteller -> {
                    ImageAdapterItem adapterItem = new ImageAdapterItem(darsteller.getName()).setPayload(darsteller);
                    if (darsteller.getImagePath() != null) {
                        adapterItem.setImagePath(darsteller.getImagePath());
                    }
                    return adapterItem;
                }).collect(Collectors.toCollection(CustomList::new));

                CustomAutoCompleteAdapter autoCompleteAdapter = new CustomAutoCompleteAdapter(context, itemList);
                autoComplete.setOnItemClickListener((parent, view, position, id) -> {
                    ParentClass_Tmdb payload = (ParentClass_Tmdb) autoCompleteAdapter.getItem(position).getPayload();
                    Toast.makeText(context, payload.getName() + " erfolgreich importiert", Toast.LENGTH_SHORT).show();
                });
                autoComplete.setDropDownHeight(CustomUtility.dpToPx(2 * 75));
//                autoComplete.setDropDownWidth(CustomUtility.dpToPx(75));
                autoComplete.setAdapter(autoCompleteAdapter);
                View viewById = context.findViewById(R.id.dropdownAnchor);
                if (viewById != null)
                    autoComplete.setDropDownAnchor(R.id.dropdownAnchor);
            }
            if (editText != null)
                applySelectionHelper(context);
        }
        /**  <------------------------- Constructor -------------------------  */


        /** ------------------------- Getter & Setter -------------------------> */
        public <Result> AdvancedQueryHelper<T> addCriteria(Utility.GenericReturnInterface<AdvancedQueryHelper<T>, SearchCriteria<T, Result>> getCriteria) {
            criteriaList.add(getCriteria.run(this));
            return this;
        }

        public AdvancedQueryHelper<T> addCriteria_defaultName() {
            return addCriteria_defaultName(null, null);
        }

        public AdvancedQueryHelper<T> addCriteria_defaultName(@Nullable @IdRes Integer editTextId, @Nullable @IdRes Integer negatedLayoutId) {
            return addCriteria_defaultName(editTextId, negatedLayoutId, t -> ((ParentClass) t).getName());
        }

        public AdvancedQueryHelper<T> addCriteria_defaultName(@Nullable @IdRes Integer editTextId, @Nullable @IdRes Integer negatedLayoutId, Utility.GenericReturnInterface<T, String> toString) {
            SearchCriteria<T, String> criteria = new SearchCriteria<T, String>(ADVANCED_SEARCH_CRITERIA_NAME, "[^]]+?")
                    .setParser(s -> s)
                    .setBuildPredicate(sub -> {
                        sub = sub.toLowerCase();
                        String finalSub = sub;
                        return t -> toString.run(t).toLowerCase().contains(finalSub);
                    });
            if (editTextId != null) {
                criteria.setApplyDialog((customDialog, s, criteria1) -> {
                    boolean[] negated = {false};
                    if (negatedLayoutId != null) {
                        negated[0] = criteria1.isNegated();
                        Helpers.AdvancedQueryHelper.applyNegationButton(customDialog.findViewById(negatedLayoutId), negated);
                    }
                    EditText editText = customDialog.findViewById(editTextId);
                    if (editText != null) {
                        editText.setText(s);
                        return customDialog1 -> {
                            String text = editText.getText().toString();
                            if (CustomUtility.stringExists(text))
                                return (negated[0] ? "!" : "") + ADVANCED_SEARCH_CRITERIA_NAME + ":" + text;
                            else
                                return null;
                        };
                    }
                    return null;
                });
            }
            criteriaList.add(criteria);
            return this;
        }

        public AdvancedQueryHelper<T> addCriteria_ParentClass(String key, CategoriesActivity.CATEGORIES category, Utility.GenericReturnInterface<T, List<String>> getList, @Nullable Context context, @Nullable @IdRes Integer textViewId, @Nullable @IdRes Integer spinnerId, @Nullable @IdRes Integer editButtonId) {
            SearchCriteria<T, Pair<String, CustomList<ParentClass>>> criteria = new SearchCriteria<T, Pair<String, CustomList<ParentClass>>>(key, PARENT_CLASS_PATTERN)
                    .setParser(sub -> {
                        CustomList<ParentClass> list = new CustomList<>();
                        if (sub.contains("|") || sub.contains("&")) {
                            for (String name : sub.split("(?<!\\\\)[|&]")) {
                                name = CategoriesActivity.deEscapeForSearchExtra(name);
                                CustomUtility.ifNotNull(Utility.findObjectByName(category, name.trim()), list::add);
                            }
                        } else
                            CustomUtility.ifNotNull(Utility.findObjectByName(category, sub.trim()), list::add);

                        if (list.isEmpty())
                            return null;
                        return Pair.create(sub.matches(".*(?<!\\\\)&.*") ? "&" : sub.matches(".*(?<!\\\\)\\|.*") ? "|" : "", list);
                    })
                    .setBuildPredicate(pair -> {
                        if (pair == null)
                            return null;
                        CustomList<String> idList = pair.second.map(com.finn.androidUtilities.ParentClass::getUuid);
                        return t -> {
                            switch (pair.first) {
                                case "|":
                                    return getList.run(t).stream().anyMatch(idList::contains);
                                default:
                                case "&":
                                    return getList.run(t).containsAll(idList);
                            }
                        };
                    });
            if (context != null && textViewId != null && spinnerId != null && editButtonId != null) {
                criteria.setApplyDialog((customDialog, pair, criteria1) -> {
                    CustomList<String> selectedIdList = CustomUtility.isNullReturnOrElse(pair, new CustomList<>(), pair1 -> pair1.second.map(com.finn.androidUtilities.ParentClass::getUuid));
                    TextView textView = customDialog.findViewById(textViewId);

                    int currentSpinnerPosition = CustomUtility.isNullReturnOrElse(pair, 0, pair1 -> !CustomUtility.stringExists(pair1.first) || pair1.first.equals("&") ? 0 : 1);
                    Spinner spinner = customDialog.findViewById(spinnerId);
                    spinner.setSelection(currentSpinnerPosition);

                    Runnable setTextView = () -> textView.setText(CategoriesActivity.joinCategoriesIds(selectedIdList, category));
                    setTextView.run();

                    customDialog.findViewById(editButtonId).setOnClickListener(v -> {
                        Utility.showEditItemDialog(context, selectedIdList, category, (customDialog1, selectedIds) -> {
                            selectedIdList.replaceWith(selectedIds);
                            setTextView.run();
                        });
                    });


                    return customDialog1 -> selectedIdList.isEmpty() ? null : String.format(Locale.getDefault(), "%s:%s", key, CategoriesActivity.joinCategoriesIds(selectedIdList, category, spinner.getSelectedItemPosition() == 0 ? " & " : " | ", true));
                });
            }
            criteria.setCategory(category);
            criteriaList.add(criteria);
            return this;

        }

        public AdvancedQueryHelper<T> addCriteria_ParentClass(String key, CategoriesActivity.CATEGORIES category, Utility.GenericReturnInterface<T, List<String>> getList) {
            addCriteria_ParentClass(key, category, getList, null, null, null, null);
            return this;
        }

        public AdvancedQueryHelper<T> setRestFilter(Utility.DoubleGenericInterface<String, CustomList<T>> restFilter) {
            this.restFilter = restFilter;
            return this;
        }

        public AdvancedQueryHelper<T> setDialogOptions(@LayoutRes int dialogLayoutId, @Nullable com.finn.androidUtilities.CustomDialog.OnDialogCallback optionalModifications) {
            this.dialogLayoutId = dialogLayoutId;
            this.optionalModifications = optionalModifications;
            return this;
        }

        public AdvancedQueryHelper<T> setSearchView(SearchView searchView) {
            this.searchView = searchView;
            return this;
        }

        public SearchView getSearchView() {
            return searchView;
        }

        public AdvancedQueryHelper<T> enableHistory(String historyKey) {
            this.historyKey = historyKey;
            return this;
        }

        public AdvancedQueryHelper<T> enableThrottle(int minDelayMillis) {
            requestThrottler = new CustomUtility.EventThrottler<Pair<String, CustomList<T>>>((eventThrottler, eventBuffer, event, time) -> {
                clean().splitQuery(event[0].first).filter(event[0].second);
            }, minDelayMillis)
            .enableOnlyKeepLastEvent();
            return this;
        }

        public AdvancedQueryHelper<T> disableThrottle(int minDelayMillis) {
            requestThrottler = null;
            return this;
        }
        /**  <------------------------- Getter & Setter -------------------------  */


        /** <------------------------- Convenience ------------------------- */
        public AdvancedQueryHelper<T> optionalModification(Utility.GenericInterface<AdvancedQueryHelper<T>> optional) {
            optional.run(this);
            return this;
        }

        public String getQuery() {
            return searchView.getQuery().toString().trim();
        }

        public AdvancedQueryHelper<T> clean() {
            fullQuery = advancedQuery = restQuery = "";
            criteriaList.forEach(SearchCriteria::clear);
            return this;
        }

        public static String removeAdvancedSearch(CharSequence fullQuery) {
            return fullQuery.toString().replaceAll(advancedQueryPattern.pattern(), "").trim();
        }

        public String getFreeSearchOrName() {
            String freeSearch = removeAdvancedSearch(getQuery());
            String nameCriteria = CustomUtility.stringExistsOrElse(((String) parse(ADVANCED_SEARCH_CRITERIA_NAME)), "");
            if (CustomUtility.stringExists(freeSearch) && CustomUtility.stringExists(nameCriteria))
                freeSearch += " ";
            return freeSearch + nameCriteria;
        }

        public boolean istExtraSearch(String extraSearch) {
            String query = getQuery();
            if (query.equals(extraSearch))
                return true;
            Matcher matcher = Pattern.compile("\\{\\[\\w+:([^\\]]+)\\]\\}").matcher(query);
            return matcher.find() && extraSearch.equals(matcher.group(1));
        }

        public boolean handleBackPress(AppCompatActivity context) {
            String query = getQuery();
            String extraSearch = context.getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
            if (Utility.stringExists(query) || CustomUtility.stringExists(extraSearch)) {
                if (Objects.equals(extraSearch, query))
                    return false;
                if (CustomUtility.stringExists(extraSearch)/* && istExtraSearch(query)*/) {
                    CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) context.getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
                    String wrappedExtraSearch;
                    if (extraSearchCategory != null && !query.equals((wrappedExtraSearch = wrapExtraSearch(extraSearchCategory, extraSearch)).trim())) {
                        searchView.setQuery(wrappedExtraSearch, false);
                        return true;
                    } else
                        return false;

                } else {
                    searchView.setQuery("", false);
                    return true;
                }
            }
            return false;
        }

        public boolean wrapAndSetExtraSearch(CategoriesActivity.CATEGORIES category, String extraSearch) {
            String search = wrapExtraSearch(category, extraSearch);
            if (CustomUtility.stringExists(search)) {
                searchView.setQuery(search, false);
                return true;
            }
            return false;
        }

        public String wrapExtraSearch(CategoriesActivity.CATEGORIES category, String extraSearch) {
            Optional<SearchCriteria> optional = criteriaList.stream().filter(criteria -> Objects.equals(criteria.getCategory(), category)).findFirst();
            if (optional.isPresent()) {
                return String.format(Locale.getDefault(), "{[%s:%s]} ", optional.get().key, extraSearch);
            }
            return "";
        }


        public boolean hasAdvancedSearch() {
            return criteriaList.stream().anyMatch(SearchCriteria::has);
        }

        public boolean has(String... keys) {
            for (String key : keys) {
                SearchCriteria criteria = getSearchCriteriaByKey(key);
                if (criteria == null)
                    continue;
                if (criteria.has())
                    return true;
            }
            return false;
        }

        public Object parse(String... keys) {
            for (String key : keys) {
                SearchCriteria criteria = getSearchCriteriaByKey(key);
                if (criteria == null)
                    continue;
                Object parse = criteria.parse();
                if (parse != null)
                    return parse;
            }
            return null;
        }

        public boolean isNegated(String... keys) {
            for (String key : keys) {
                SearchCriteria criteria = getSearchCriteriaByKey(key);
                if (criteria == null)
                    continue;
                boolean negated = criteria.isNegated();
                if (negated)
                    return true;
            }
            return false;
        }

        public SearchCriteria getSearchCriteriaByKey(String key) {
            return criteriaList.stream().filter(criteria -> criteria.key.equals(key)).findFirst().orElse(null);
        }

        private void applySelectionHelper(AppCompatActivity context) {
            int[] oldSelection = {0, 0};
            editText.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void sendAccessibilityEvent(View host, int eventType) {
                    super.sendAccessibilityEvent(host, eventType);
                    if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                        int selectionStart = editText.getSelectionStart();
                        int selectionEnd = editText.getSelectionEnd();
                        if (selectionStart != selectionEnd && oldSelection[0] == oldSelection[1]) {
                            oldSelection[0] = selectionStart;
                            oldSelection[1] = selectionEnd;
                            String selectionString = editText.getText().toString().substring(selectionStart, selectionEnd);
                            Matcher matcher = Pattern.compile("(\\w+:)([^\\]]+)").matcher(selectionString);
                            if (matcher.find()) {
                                new Handler(Looper.myLooper()).postDelayed(() -> {
                                    editText.setSelection(selectionStart + matcher.group(1).length(), selectionEnd);
                                }, 500);
                            }
                        } else {
                            oldSelection[0] = selectionStart;
                            oldSelection[1] = selectionEnd;
                        }
                    }
                }
            });
            this.context = context;
        }
        /**  ------------------------- Convenience ------------------------->  */


        /**  ------------------------- Function -------------------------> */
        public AdvancedQueryHelper<T> splitQuery() {
            return splitQuery(null);
        }

        public AdvancedQueryHelper<T> splitQuery(String query) {
            fullQuery = query == null ? getQuery() : query;
            if (fullQuery.contains("{")) {
                Matcher advancedQueryMatcher = advancedQueryPattern.matcher(fullQuery);

                if (advancedQueryMatcher.find()) {
                    advancedQuery = advancedQueryMatcher.group(0);
                } else {
                    restQuery = fullQuery;
                    return this;
                }

                criteriaList.forEach(criteria -> criteria.matchQuery(advancedQuery));

                restQuery = advancedQueryMatcher.replaceAll("").trim();

            } else
                restQuery = fullQuery;
            return this;
        }

        public AdvancedQueryHelper<T> filterAdvanced(CustomList<T> list) {
            CustomList<SearchCriteria> filteredCriteriaList = criteriaList.filter(SearchCriteria::has, false);
            filteredCriteriaList.forEach(SearchCriteria::buildPredicate);
            // ToDo: kann optimiert werden? immer wieder einen Stream filtern?
//            CustomUtility.GenericInterface<Boolean> logTiming = CustomUtility.logTiming();
            if (false) {
                list.filter(t -> {
                    for (SearchCriteria criteria : filteredCriteriaList) {
                        boolean matchResult = criteria.matchObject(t) ^ criteria.isNegated();
                        if (!matchResult)
                            return false;
                    }
                    return true;
                }, true);
            } else {
                Stream<T> stream = list.stream();
                for (SearchCriteria criteria : filteredCriteriaList) {
                    boolean negated = criteria.isNegated();
                    stream = stream.filter(t -> criteria.matchObject(t) ^ negated);
                }
                list.replaceWith(stream.collect(Collectors.toList()));
            }
//            logTiming.run(false);
            return this;
        }

        public AdvancedQueryHelper<T> filterRest(CustomList<T> list) {
            if (restFilter != null && CustomUtility.stringExists(restQuery))
                restFilter.run(restQuery, list);
            return this;
        }

        public AdvancedQueryHelper<T> filter(CustomList<T> list) {
            return filterAdvanced(list).filterRest(list);
        }

        public AdvancedQueryHelper<T> filterFull(CustomList<T> list) {
            if (requestThrottler != null) {
                requestThrottler.call(Pair.create(getQuery(), list));
                return this;
            } else
                return clean().splitQuery().filter(list);
        }
        /**  <------------------------- Function -------------------------  */


        /** ------------------------- Dialog -------------------------> */
        public AdvancedQueryHelper<T> showAdvancedSearchDialog() {
            clean().splitQuery();
            CustomList<Utility.GenericReturnInterface<com.finn.androidUtilities.CustomDialog, String>> onSaveList = new CustomList<>();

            boolean preSelected = hasAdvancedSearch();

            com.finn.androidUtilities.CustomDialog.Builder(context)
                    .setTitle("Erweiterte Suche")
                    .setView(dialogLayoutId)
                    .setSetViewContent((customDialog, view, reload) -> criteriaList.forEach(criteria -> {
                        if (criteria.applyDialog == null)
                            return;
                        Object o;
                        if (CustomUtility.stringExists(criteria.sub))
                            o = criteria.tempResult = criteria.parser.run(criteria.sub);
                        else
                            o = null;
                        Utility.GenericReturnInterface onSave = criteria.applyDialog.runApplyDialog(customDialog, o, criteria);
                        if (onSave != null)
                            onSaveList.add(onSave);
                    }))
                    .addOptionalModifications(customDialog -> {
                        if (CustomUtility.stringExists(historyKey))
                            customDialog
                                    .addButton(R.drawable.ic_time, customDialog1 -> {
                                        if (showHistoryDialog(context, historyKey, this))
                                            customDialog1.dismiss();
                                    }, false)
                                    .alignPreviousButtonsLeft();
                        if (preSelected) {
                            customDialog
                                    .addButton(R.drawable.ic_reset, customDialog1 -> {
                                        String removedQuery = AdvancedQueryHelper.removeAdvancedSearch(searchView.getQuery());
                                        searchView.setQuery(removedQuery, false);
                                        Toast.makeText(context, "Erweiterte Suche zurückgesetzt", Toast.LENGTH_SHORT).show();
                                    })
                                    .alignPreviousButtonsLeft();
                        }
                    })
                    .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                    .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                        String restQuery = removeAdvancedSearch(getQuery());
                        CustomList<String> filterList = onSaveList.map(onSave -> onSave.run(customDialog)).filter(Objects::nonNull, true).map(s -> "[" + s + "]");
                        String newQuery = Utility.isNotValueReturnOrElse(restQuery, "", s -> s + " ", null);
                        String advancedQuery = filterList.isEmpty() ? "" : String.format("{%s}", String.join(" ", filterList));
                        if (CustomUtility.stringExists(historyKey) && CustomUtility.stringExists(advancedQuery)) {
                            SharedPreferences sharedPreferences = context.getSharedPreferences(ADVANCED_SEARCH_HISTORY, Context.MODE_PRIVATE);
                            String historyString = sharedPreferences.getString(historyKey, null);
                            CustomList<String> list;
                            Gson gson = new Gson();
                            if (CustomUtility.stringExists(historyString))
                                list = gson.fromJson(historyString, CustomList.class);
                            else
                                list = new CustomList<>();

                            list.add(0, advancedQuery);
                            list.distinct();
                            list.sorted(CustomUtility.comparatorSimpleBool(s1 -> s1.startsWith("☆")));
                            historyString = gson.toJson(list);
                            sharedPreferences.edit().putString(historyKey, historyString).apply();
                        }
                        newQuery += advancedQuery;
                        searchView.setQuery(newQuery, false);
                    })
                    .addOptionalModifications(customDialog -> {
                        if (optionalModifications != null)
                            optionalModifications.runOnDialogCallback(customDialog);
                    })
                    .enableDynamicWrapHeight(context)
                    .show();


            return this;
        }

        public static void applyNegationButton(LinearLayout parentLayout, boolean[] isNegated) {
            if (parentLayout.getChildCount() >= 2) {
                View child0 = parentLayout.getChildAt(0);
                View child1 = parentLayout.getChildAt(1);
                if (child0 instanceof TextView && child1 instanceof TextView) {
                    child1.setVisibility(isNegated[0] ? View.VISIBLE : View.GONE);
                    child0.setOnClickListener(v -> {
                        isNegated[0] ^= true;
                        child1.setVisibility(isNegated[0] ? View.VISIBLE : View.GONE);
                    });
                }
            }
        }

        private static <T> boolean showHistoryDialog(AppCompatActivity context, String historyKey, AdvancedQueryHelper<T> helper) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(ADVANCED_SEARCH_HISTORY, Context.MODE_PRIVATE);
            String historyString = sharedPreferences.getString(historyKey, null);
            if (!CustomUtility.stringExists(historyString) || historyString.equals("[]")) {
                Toast.makeText(context, "Kein Verlauf", Toast.LENGTH_SHORT).show();
                return false;
            }
            ArrayList<String> list = new Gson().fromJson(historyString, ArrayList.class);
            com.finn.androidUtilities.CustomDialog.Builder(context)
                    .setTitle("Verlauf")
                    .setDimensionsFullscreen()
                    .disableScroll()
                    .setView(customDialog -> new CustomRecycler<String>(context)
                            .setObjectList(list)
                            .enableDivider(12)
                            .enableSwiping((customRecycler, objectList, direction, s, index) -> {
                                if (direction == ItemTouchHelper.START) {
                                    if (s.startsWith("☆"))
                                        objectList.set(index, s.substring(2));
                                    else
                                        objectList.set(index, "☆ " + s);
                                    objectList.sort(CustomUtility.comparatorSimpleBool(s1 -> s1.startsWith("☆")));
                                } else if (direction == ItemTouchHelper.END && s.startsWith("☆")) {
                                    Toast.makeText(context, "Favoriten können nicht gelöscht werden", Toast.LENGTH_SHORT).show();
                                    objectList.add(index, s);
                                    customRecycler.reload();
                                }
                                String newHistoryString = new Gson().toJson(list);
                                sharedPreferences.edit().putString(historyKey, newHistoryString).apply();
                                if (objectList.isEmpty())
                                    customDialog.dismiss();
                            }, true, true)
                            .setSwipeBackgroundHelper(new CustomRecycler.SwipeBackgroundHelper<String>(R.drawable.ic_delete, Color.RED)
                                    .enableBouncyThreshold(2, true, false)
                                    .setDynamicResources((swipeBackgroundHelper, episode) -> {
                                        swipeBackgroundHelper
                                                .setIconResId_left(/*episode.isWatched() ? R.drawable.ic_notification_active :*/ R.drawable.ic_star)
                                                .setFarEnoughColor_circle_left(context.getColor(R.color.colorYellow));
                                    })
                            )
                            .setOnClickListener((customRecycler, itemView, s, index) -> {
                                String restQuery = removeAdvancedSearch(helper.getQuery());
                                String newQuery = Utility.isNotValueReturnOrElse(restQuery, "", s1 -> s1 + " ", null);
                                if (s.startsWith("☆"))
                                    s = s.substring(2);
                                newQuery += s;
                                helper.searchView.setQuery(newQuery, false);
                                customDialog.dismiss();
                            })
                            .generateRecyclerView())
                    .show();
            return true;
        }
        /**
         * <------------------------- Dialog -------------------------
         */


        /** ------------------------- Color -------------------------> */
        public AdvancedQueryHelper<T> enableColoration() {
//            CharSequence query = searchView.getQuery();
//            searchView.
//            searchView.setQuery(new Helpers.SpannableStringHelper().appendColor(query, Color.RED).get(), true);
//            searchView.
//            advancedSearchPattern.matcher(query)

//            EditText edit = searchView.findViewById(R.id.search_src_text);
//            ((LinearLayout) ((LinearLayout) ((LinearLayout) searchView.getChildAt(0)).getChildAt(2)).getChildAt(1)).getChildAt(0);

//            Utility.RecursiveGenericReturnInterface getEditText =
            editText.addTextChangedListener(colorationWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    applyColoration(s);
                }
            });
            return this;
        }

        public AdvancedQueryHelper<T> disableColoration() {
            if (colorationWatcher != null)
                editText.removeTextChangedListener(colorationWatcher);
            return this;
        }

        private void applyColoration(Editable editable) { // ToDo: negation Fett markieren
            new CustomList<>(editable.getSpans(0, editable.length(), StyleSpan.class)).forEach(editable::removeSpan);
            new CustomList<>(editable.getSpans(0, editable.length(), ForegroundColorSpan.class)).forEach(editable::removeSpan);

            Matcher queryMatcher = advancedQueryPattern.matcher(editable);
            if (queryMatcher.find()) {
                MatchResult queryResult = queryMatcher.toMatchResult();
                int queryStart = queryResult.start();
                int queryEnd = queryResult.end();
                editable.setSpan(new StyleSpan(Typeface.ITALIC), queryStart, queryEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                Matcher criteriaMatcher = Pattern.compile("\\[[^\\[]*?\\]").matcher(queryResult.group());
//                while (criteriaMatcher.find()) {
//                    MatchResult criteriaResult = criteriaMatcher.toMatchResult();
                if ((queryEnd - 1) - (queryStart + 1) > 0)
                    editable.setSpan(new ForegroundColorSpan(Color.RED), queryStart + 1, queryEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                }
                int normalTextColor = editText.getTextColors().getDefaultColor();
                int incompleteTextColor = context.getColor(R.color.colorPrimary);

                for (SearchCriteria criteria : criteriaList) {
                    Matcher criteriaMatcher = Pattern.compile(String.format("\\[!?%s: ?(%s)\\s*\\]", criteria.key, criteria.regEx)).matcher(queryResult.group());
                    if (criteriaMatcher.find()) {
                        MatchResult criteriaResult = criteriaMatcher.toMatchResult();
                        editable.setSpan(new ForegroundColorSpan(criteria.tempResult == null ? incompleteTextColor : normalTextColor), criteriaResult.start() + queryStart, criteriaResult.end() + queryStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        /** <------------------------- Color ------------------------- */

        // ---------------

        public static class SearchCriteria<T, Result> {
            public Result tempResult;
            public String key;
            public String regEx;
            public String full;
            public String sub;
            public Boolean negated;
            private Utility.GenericReturnInterface<String, Result> parser;
            private Utility.GenericReturnInterface<Result, Predicate<T>> buildPredicate;
            public Predicate<T> predicate;
            private ApplyDialogInterface<T, Result> applyDialog;
            private CategoriesActivity.CATEGORIES category;
            // ToDo: In und aus Dialog

            /**  ------------------------- Constructor -------------------------> */
            public SearchCriteria(String key, @Language("RegExp") String regEx) {
                this.key = key;
                this.regEx = regEx;
            }
            /**  <------------------------- Constructor -------------------------  */


            /**  ------------------------- Getter & Setter -------------------------> */
            public SearchCriteria<T, Result> setParser(Utility.GenericReturnInterface<String, Result> parser) {
                this.parser = parser;
                return this;
            }

            public SearchCriteria<T, Result> setBuildPredicate(Utility.GenericReturnInterface<Result, Predicate<T>> buildPredicate) {
                this.buildPredicate = buildPredicate;
                return this;
            }

            public <A> SearchCriteria<T, Result> setBuildPredicate_fromLastAdded(AdvancedQueryHelper<A> advancedQueryHelper) {
                SearchCriteria last = advancedQueryHelper.criteriaList.getLast();
                if (last != null)
                    buildPredicate = (Utility.GenericReturnInterface<Result, Predicate<T>>) last.buildPredicate;
                return this;
            }

            public SearchCriteria<T, Result> setApplyDialog(ApplyDialogInterface<T, Result> applyDialog) {
                this.applyDialog = applyDialog;
                return this;
            }

            public CategoriesActivity.CATEGORIES getCategory() {
                return category;
            }

            public SearchCriteria<T, Result> setCategory(CategoriesActivity.CATEGORIES category) {
                this.category = category;
                return this;
            }
            /**  <------------------------- Getter & Setter -------------------------  */


            /**  ------------------------- Convenience -------------------------> */
            public Pattern getPattern() {
                return Pattern.compile(String.format("\\[!?%s: ?(%s)\\s*\\]", key, regEx));
            }

            public String matchQuery(String query) { // ToDo: vielleicht auch mehrere gleiche Filter erlauben
                if (query.contains("[" + key + ":") || query.contains("[!" + key + ":")) {
                    Matcher matcher = getPattern().matcher(query);
                    if (matcher.find()) {
                        full = matcher.group();
                        return sub = matcher.group(1);
                    }
                }
                return "";
            }

            public SearchCriteria<T, Result> buildPredicate() {
                if (CustomUtility.stringExists(sub) && parser != null && buildPredicate != null) {
                    predicate = buildPredicate.run(tempResult = parser.run(sub));
                }
                return this;
            }

            public boolean matchObject(T t) {
                if (predicate == null)
                    return true;
                return predicate.test(t);
            }

            public boolean has() {
                return CustomUtility.stringExists(sub);
            }

            public String getSub() {
                return sub;
            }

            public Result parse() {
                return tempResult = parse(sub);
            }

            public Result parse(String text) {
                if (CustomUtility.stringExists(text) && parser != null)
                    return parser.run(text);
                else
                    return null;
            }

            public boolean isNegated() {
                return full != null && ((negated != null && negated) || (negated = full.startsWith("[!")));
            }

            public void clear() {
                full = "";
                sub = "";
                predicate = null;
                tempResult = null;
                negated = null;
            }
            /** <------------------------- Convenience ------------------------- */

            public interface ApplyDialogInterface<T, Result> {
                Utility.GenericReturnInterface<com.finn.androidUtilities.CustomDialog, String> runApplyDialog(com.finn.androidUtilities.CustomDialog customDialog, Result result, SearchCriteria<T, Result> criteria);
            }
        }
    }
    /** <------------------------- AdvancedSearch ------------------------- */


    //  ------------------------- DurationFormatter ------------------------->
    public static class DurationFormatter {
        private String pattern = "'%y% Jahr§e§~ ~''%M% Monat§e§~ ~''%w% Woche§n§~ ~''%d% Tag§e§~ ~''%h% Stunde§n§~ ~''%m% Minute§n§~ ~''%s% Sekunde§n§~ ~'";
        private boolean lastZeroIfEmpty = true;
        private String patternIfEmpty;
        private String textIfEmpty;
        private Duration lastDuration;

        //  ------------------------- Constructor ------------------------->
        public DurationFormatter(String pattern) {
            if (CustomUtility.stringExists(pattern))
                this.pattern = pattern;
        }

        public DurationFormatter() {
        }
        //  <------------------------- Constructor -------------------------


        //  ------------------------- Static ------------------------->
        public static String formatDefault(Duration duration, @Nullable String pattern) {
            return new DurationFormatter().format(duration, pattern);
        }

        public static String formatDefault(Duration duration) {
            return new DurationFormatter().format(duration);
        }

        public static String formatDefault(Date from, Date to, @Nullable String pattern) {
            return new DurationFormatter().format(from, to, pattern);
        }

        public static String formatDefault(Date from, Date to) {
            return new DurationFormatter().format(from, to);
        }
        //  <------------------------- Static -------------------------


        //  ------------------------- Getter & Setter ------------------------->
        public String getPattern() {
            return pattern;
        }

        public DurationFormatter setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public boolean isLastZeroIfEmpty() {
            return lastZeroIfEmpty;
        }

        public DurationFormatter setLastZeroIfEmpty(boolean lastZeroIfEmpty) {
            this.lastZeroIfEmpty = lastZeroIfEmpty;
            return this;
        }

        public DurationFormatter disableLastZeroIfEmpty() {
            this.lastZeroIfEmpty = false;
            return this;
        }

        public String getPatternIfEmpty() {
            return patternIfEmpty;
        }

        public DurationFormatter setPatternIfEmpty(String patternIfEmpty) {
            this.patternIfEmpty = patternIfEmpty;
            return this;
        }

        public String getTextIfEmpty() {
            return textIfEmpty;
        }

        public DurationFormatter setTextIfEmpty(String textIfEmpty) {
            this.textIfEmpty = textIfEmpty;
            return this;
        }

        public Duration getLastDuration() {
            return lastDuration;
        }
        //  <------------------------- Getter & Setter -------------------------


        //  ------------------------- Convenience ------------------------->
        public static Duration getDurationBetweenDates(Date from, Date to) {
            return Duration.between(
                    from.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    to.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        //  <------------------------- Convenience -------------------------


        //  ------------------------- Format ------------------------->
        public String format(Duration duration, @Nullable String pattern) {
            return formatDuration(duration, pattern);
        }

        public String format(Duration duration) {
            return formatDuration(duration, null);
        }

        public String format(Date from, Date to, @Nullable String pattern) {
            return format(getDurationBetweenDates(from, to), pattern);
        }

        public String format(Date from, Date to) {
            return format(getDurationBetweenDates(from, to));
        }

        // ---------------

        private String formatDuration(Duration duration, @Nullable String format) {
            lastDuration = duration;
            format = CustomUtility.stringExistsOrElse(format, this.pattern);
            String fullFormat = format;
            CustomList<Pair<String, Integer>> patternList = new CustomList<>(Pair.create("%y%", 31557600), Pair.create("%M%", 2565000), Pair.create("%w%", 604800), Pair.create("%d%", 86400), Pair.create("%h%", 3600), Pair.create("%m%", 60), Pair.create("%s%", 1));
            patternList.filter(pair -> fullFormat.contains(pair.first), true);
            int seconds = (int) (duration.getSeconds());
            Pair<String, Integer> tiniest = null;
            boolean empty = true;
            while (true) {
                Matcher segments = Pattern.compile("'.+?'").matcher(format);
                if (!segments.find())
                    break;
                String segment = segments.group();
                Iterator<Pair<String, Integer>> iterator = patternList.iterator();
                while (iterator.hasNext()) {
                    Pair<String, Integer> pair = iterator.next();
                    if (segment.contains(pair.first)) {
                        if (tiniest == null || tiniest.second > pair.second)
                            tiniest = Pair.create(segment, pair.second);
                        int amount = seconds / pair.second;
                        if (amount > 0) {
                            empty = false;
                            seconds = seconds % pair.second;
                            Matcher matcher = Pattern.compile(pair.first).matcher(segment);
                            String replacement = matcher.replaceFirst(String.valueOf(amount));
                            if (replacement.contains("§")) {
                                Matcher removePlural = Pattern.compile("§\\w+?§").matcher(replacement);
                                if (removePlural.find())
                                    replacement = removePlural.replaceFirst(amount > 1 ? CustomUtility.subString(removePlural.group(), 1, -1) : "");
                            }
                            format = segments.replaceFirst(CustomUtility.subString(replacement, 1, -1));
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
                    String replacement = CustomUtility.subString(segments.group(), 1, -1);

                    format = CustomUtility.stringReplace(format, start, end, segments.find() ? replacement : "");
                }
            }

            if (empty) {
                if (CustomUtility.stringExists(patternIfEmpty) && !fullFormat.equals(patternIfEmpty)) {
                    format = formatDuration(duration, patternIfEmpty);
                    if (CustomUtility.stringExists(format))
                        return format;
                }
                if (CustomUtility.stringExists(textIfEmpty))
                    return textIfEmpty;
                if (lastZeroIfEmpty) {
                    if (tiniest == null)
                        return "0 Sekunden";
                    else {
                        format = CustomUtility.subString(tiniest.first.replaceAll("%.+?%", "0").replaceAll("~.+?~", ""), 1, -1);
                        if (format.contains("§")) {
                            Matcher removePlural = Pattern.compile("§\\w+?§").matcher(format);
                            if (removePlural.find())
                                format = removePlural.replaceFirst(CustomUtility.subString(removePlural.group(), 1, -1));
                        }

                    }
                }
            }

            return format;
        }
        //  <------------------------- Format -------------------------
    }
    //  <------------------------- DurationFormatter -------------------------
}
