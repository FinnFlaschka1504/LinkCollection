package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxMustermannGeheim.linkcollection.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialog {

    public enum BUTTON_CONFIGURATION {
        YES_NO, SAVE_CANCEL, BACK, OK, OK_CANCEL, CUSTOM;
    }

    public enum BUTTON {
        YES_BUTTON("YES_BUTTON"), NO_BUTTON("NO_BUTTON"), SAVE_BUTTON("SAVE_BUTTON"), CANCEL_BUTTON("CANCEL_BUTTON"), BACK_BUTTON("BACK_BUTTON")
        , OK_BUTTON("OK_BUTTON");

        String code;

        BUTTON(String code) {
            this.code = code;
        }
    }
    // ToDo: EditText zum standard hinzufügen

    private EditBuilder editBuilder;
    private boolean showKeyboard = false;
    private boolean showEdit;
    private Context context;
    private Dialog dialog;
    private String title;
    private CharSequence text;
    private View view;
    private BUTTON_CONFIGURATION buttonConfiguration = BUTTON_CONFIGURATION.BACK;
    private Pair<Boolean, Boolean> dimensions = new Pair<>(true, false);
    private boolean dividerVisibility = true;
    private boolean isDividerVisibilityCustom = false;
    private int titleTextAlignment = View.TEXT_ALIGNMENT_CENTER;
    private boolean isTextBold = false;
    private SetViewContent setViewContent;
    private Object objectExtra;
    private OnDialogDismiss onDialogDismiss;
    private boolean scroll = true;
//    private int childRecyclerId;

    private List<Boolean> dismissDialogList = new ArrayList<>();
    private List<Pair<String, OnClick>> pairList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<Button> buttonList = new ArrayList<>();
    private List<Integer> buttonIdList = new ArrayList<>();

    public static final String YES_BUTTON = "YES_BUTTON";
    public static final String NO_BUTTON = "NO_BUTTON";
    public static final String SAVE_BUTTON = "SAVE_BUTTON";
    public static final String CANCEL_BUTTON = "CANCEL_BUTTON";
    public static final String BACK_BUTTON = "BACK_BUTTON";
    public static final String OK_BUTTON = "OK_BUTTON";

    public CustomDialog(Context context) {
        this.context = context;
        dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.dialog_custom);
    }

    public static CustomDialog Builder(Context context) {
        CustomDialog customDialog = new CustomDialog(context);
        return customDialog;
    }



    public CustomDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public CustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomDialog setText(CharSequence text) {
        this.text = text;
        return this;
    }

    public CustomDialog setView(int layoutId) {
        LayoutInflater li = LayoutInflater.from(context);
        this.view = li.inflate(layoutId, null);
        return this;
    }

    public CustomDialog setView(View view) {
        this.view = view;
        return this;
    }

    public CustomDialog setButtonConfiguration(BUTTON_CONFIGURATION buttonConfiguration) {
        this.buttonConfiguration = buttonConfiguration;
        return this;
    }

    public CustomDialog addButton(String buttonName, OnClick onClick) {
        return addButton_complete(buttonName, onClick, -1, true);
    }
    public CustomDialog addButton(String buttonName, OnClick onClick, int buttonId) {
        return addButton_complete(buttonName, onClick, buttonId, true);
    }

    public CustomDialog addButton(String buttonName, OnClick onClick, boolean dismissDialog){
        return addButton_complete(buttonName, onClick, -1, dismissDialog);
    }
    public CustomDialog addButton(String buttonName, OnClick onClick, int buttonId, boolean dismissDialog){
        return addButton_complete(buttonName, onClick, buttonId, dismissDialog);
    }

    private CustomDialog addButton_complete(String buttonName, OnClick onClick, int buttonId, boolean dismissDialog) {
        Pair<String, OnClick> pair = new Pair<>(buttonName, onClick);
        dismissDialogList.add(dismissDialog);
        pairList.add(pair);
        nameList.add(pair.first);
        buttonIdList.add(buttonId);
        return this;
    }

    public interface OnClick {
        void runOnClick(CustomDialog customDialog, Dialog dialog);
    }

    public interface SetViewContent{
        void runSetViewContent(CustomDialog customDialog, View view);
    }

    public CustomDialog setSetViewContent(SetViewContent setViewContent) {
        this.setViewContent = setViewContent;
        return this;
    }

    public CustomDialog setDimensions(boolean width, boolean height) {
        this.dimensions = new Pair<>(width, height);
        return this;
    }

    public CustomDialog setDividerVisibility(boolean dividerVisibility) {
        this.dividerVisibility = dividerVisibility;
        isDividerVisibilityCustom = true;
        return this;
    }

    public CustomDialog setTitleTextAlignment(int titleTextAlignment) {
        this.titleTextAlignment = titleTextAlignment;
        return this;
    }

    public CustomDialog setTextBold(boolean textBold) {
        isTextBold = textBold;
        return this;
    }

    public CustomDialog setEdit(EditBuilder editBuilder) {
        this.showEdit = true;
        this.editBuilder = editBuilder;
        return this;
    }

    public static class EditBuilder {
        public enum INPUT_TYPE {
            TEXT(0x00000001), NUMBER(0x00000002), NUMBER_DECIMAL(0x00002002), CAP_SENTENCES(0x00004001),
            CAPS_LOCK(0x00001001), CAPS_WORD(0x00002001),MULTI_LINE(0x00040001), E_MAIL(0x00000021)
            , PASSWORD(0x00000081), NUMBER_PASSWORD(0x00000012), DATE_TIME(0x00000004)
            , DATE(0x00000014), TIME(0x00000024);

            int code;

            INPUT_TYPE(int code) {
                this.code = code;
            }
        }
        private String text;
        private String hint;
        private boolean showKeyboard = true;
        private boolean selectAll = true;
        private boolean disableWhenEmpty = true;
        private boolean fireButtonOnOK = false;
        private int buttonId;
        private String regEx = ".*";
        private INPUT_TYPE inputType = INPUT_TYPE.CAP_SENTENCES;

        public EditBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public EditBuilder setHint(String hint) {
            this.hint = hint;
            return this;
        }

        public EditBuilder setShowKeyboard(boolean showKeyboard) {
            this.showKeyboard = showKeyboard;
            return this;
        }

        public EditBuilder setSelectAll(boolean selectAll) {
            this.selectAll = selectAll;
            return this;
        }

        public EditBuilder setDiableButtonWhenEmpty(int buttonId) {
            this.buttonId = buttonId;
            return this;
        }

        public EditBuilder setInputType(INPUT_TYPE inputType) {
            this.inputType = inputType;
            return this;
        }

        public EditBuilder setCheckRegEx(int buttonId, String regEx) {
            this.buttonId = buttonId;
            this.regEx = regEx;
            return this;
        }

        public EditBuilder setFireButtonOnOK(int buttonId) {
            this.buttonId = buttonId;
            this.fireButtonOnOK = true;
            return this;
        }
    }

    public static class TextBuilder{

    }

    private void setButtons() {
        switch (buttonConfiguration) {
            case YES_NO:
                addNewButton("Nein");
                addNewButton("Ja");
                break;
            case SAVE_CANCEL:
                addNewButton("Abbrechen");
                addNewButton("Speichern");
                break;
            case BACK:
                addNewButton("Zurück");
                break;
            case OK:
                addNewButton("OK");
                break;
            case OK_CANCEL:
                addNewButton("Abbrechen");
                addNewButton("OK");
                break;
            case CUSTOM:
                for (Pair<String, OnClick> pair : pairList) {
                    addNewButton(pair.first).setId(buttonIdList.get(pairList.indexOf(pair)));
                }
                break;
        }

    }

    private Button addNewButton(String text) {
        Button button = new Button(context);
        button.setBackground(dialog.findViewById(R.id.dialog_custom_Button1).getBackground().getConstantState().newDrawable());
        button.setTextColor(((Button)dialog.findViewById(R.id.dialog_custom_Button1)).getTextColors());
        button.setText(text);
        ((FlowLayout) dialog.findViewById(R.id.dialog_custom_buttonLayout)).addView(button);
        buttonList.add(button);
        return button;
    }

    private void setOnClickListeners() {
        Map<String, OnClick> stringOnClickMap = new HashMap<>();
        for (Pair<String, OnClick> pair : pairList) stringOnClickMap.put(pair.first, pair.second);

        switch (buttonConfiguration) {
            case YES_NO:
                if (stringOnClickMap.keySet().contains(NO_BUTTON)) {
                    int index = nameList.indexOf(NO_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
//                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                if (stringOnClickMap.keySet().contains(YES_BUTTON)) {
                    int index = nameList.indexOf(YES_BUTTON);
                    buttonList.get(1).setId(buttonIdList.get(index));
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                return;

            case SAVE_CANCEL:
                if (stringOnClickMap.keySet().contains(CANCEL_BUTTON)) {
                    int index = nameList.indexOf(CANCEL_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                if (stringOnClickMap.keySet().contains(SAVE_BUTTON)) {
                    int index = nameList.indexOf(SAVE_BUTTON);
                    buttonList.get(1).setId(buttonIdList.get(index));
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                return;

            case BACK:
                if (stringOnClickMap.keySet().contains(BACK_BUTTON)) {
                    int index = nameList.indexOf(BACK_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });
                return;

            case OK_CANCEL:
                if (stringOnClickMap.keySet().contains(CANCEL_BUTTON)) {
                    int index = nameList.indexOf(CANCEL_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                if (stringOnClickMap.keySet().contains(OK_BUTTON)) {
                    int index = nameList.indexOf(OK_BUTTON);
                    buttonList.get(1).setId(buttonIdList.get(index));
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                return;

            case OK:
                if (stringOnClickMap.keySet().contains(OK_BUTTON)) {
                    int index = nameList.indexOf(OK_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.runOnClick(this, dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                return;
        }

        int count = 0;
        for (Pair<String, OnClick> pair : pairList) {
            int finalCount = count;
            buttonList.get(count).setOnClickListener(view1 -> {
                if (dismissDialogList.get(finalCount))
                    dialog.dismiss();
                pair.second.runOnClick(this, dialog);
            });
            count++;
        }
    }

    private void applySetEdit() {
        EditText editText = dialog.findViewById(R.id.dialog_custom_edit);
        if (editBuilder.text != null)
            editText.setText(editBuilder.text);
        if (editBuilder.hint != null)
            editText.setHint(editBuilder.hint);
        editText.setSelectAllOnFocus(editBuilder.selectAll);
        this.showKeyboard = editBuilder.showKeyboard;
        Button button = dialog.findViewById(editBuilder.buttonId);
        if (button != null) {
            if (editBuilder.disableWhenEmpty && editBuilder.text == null)
                button.setEnabled(false);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().equals("") || !charSequence.toString().matches(editBuilder.regEx))
                        button.setEnabled(false);
                    else
                        button.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        editText.setInputType(editBuilder.inputType.code);
        if (editBuilder.fireButtonOnOK) {
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    Button button1 = dialog.findViewById(editBuilder.buttonId);
                    if (button1.isEnabled())
                        button1.callOnClick();
                }
                return true;
            });

        }

    }

    public Dialog getDialog() {
        return dialog;
    }

    public Object getObjectExtra() {
        return objectExtra;
    }

    public CustomDialog setObjectExtra(Object objectExtra) {
        this.objectExtra = objectExtra;
        return this;
    }

    public interface OnDialogDismiss {
        void runOnDialogDismiss(CustomDialog customDialog);
    }

    public CustomDialog setOnDialogDismiss(OnDialogDismiss onDialogDismiss) {
        this.onDialogDismiss = onDialogDismiss;
        return this;
    }

    public CustomDialog disableScroll() {
        scroll = false;
        return this;
    }

//    public CustomDialog onlyScrollChildRecycler(int recyclerId) {
//        childRecyclerId = recyclerId;
//        return this;
//    }

    public CustomDialog show_custom() {
        show();
        return this;
    }

    public Dialog show() {
        TextView dialog_custom_title = dialog.findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_text = dialog.findViewById(R.id.dialog_custom_text);

        dialog_custom_title.setTextAlignment(titleTextAlignment);

        if (title != null)
            dialog_custom_title.setText(this.title);
        else
            dialog_custom_title.setVisibility(View.GONE);

        if (text != null) {
            dialog_custom_text.setText(this.text);
            if (isTextBold)
                dialog_custom_text.setTypeface(null, Typeface.BOLD);
        }
        else
            dialog.findViewById(R.id.dialog_custom_layout_text).setVisibility(View.GONE);

        if (!showEdit)
            dialog.findViewById(R.id.dialog_custom_layout_edit).setVisibility(View.GONE);

        if (view != null) {
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            if (scroll) {
                ScrollView dialog_custom_layout_view_interface = dialog.findViewById(R.id.dialog_custom_layout_view_interface);
                dialog_custom_layout_view_interface.addView(view, layoutParams);
                dialog_custom_layout_view_interface.setVisibility(View.VISIBLE);
//                dialog_custom_layout_view_interface.setScrollable(false);
//                dialog_custom_layout_view_interface.scroll
//                dialog_custom_layout_view_interface.setNestedScrollingEnabled(false);
            } else {
                LinearLayout dialog_custom_layout_view_interface_restrained = dialog.findViewById(R.id.dialog_custom_layout_view_interface_restrained);
                dialog_custom_layout_view_interface_restrained.addView(view, layoutParams);
                dialog_custom_layout_view_interface_restrained.setVisibility(View.VISIBLE);
            }
        } else {
            dialog.findViewById(R.id.dialog_custom_layout_view).setVisibility(View.GONE);
        }

        if (text != null && showEdit)
            dialog.findViewById(R.id.dialog_custom_divider3).setVisibility(View.INVISIBLE);

        if (text != null && view != null || showEdit && view != null) {
            dialog.findViewById(R.id.dialog_custom_divider5).setVisibility(View.GONE);
        }

        if (text == null && view == null) {
            if (isDividerVisibilityCustom && dividerVisibility)
                dialog.findViewById(R.id.dialog_custom_divider).setVisibility(View.VISIBLE);
        }

        if (title == null && text != null)
            dialog.findViewById(R.id.dialog_custom_divider1).setVisibility(View.INVISIBLE);

        if (title == null && text == null && view != null)
            dialog.findViewById(R.id.dialog_custom_divider5).setVisibility(View.GONE);

        // ToDo: eventuell connectDown implementieren

        if (isDividerVisibilityCustom) {
            dialog.findViewById(R.id.dialog_custom_divider1).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider2).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider3).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider4).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider5).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider6).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
        }


        setDialogLayoutParameters(dialog, dimensions.first, dimensions.second);
        setButtons();
        setOnClickListeners();
        if (showEdit)
            applySetEdit();
        if (showKeyboard) {
            Utility.changeWindowKeyboard(dialog.getWindow(), true);
            dialog.findViewById(R.id.dialog_custom_edit).requestFocus();
        }

        if (setViewContent != null)
            setViewContent.runSetViewContent(this, view);

        if (onDialogDismiss != null)
            dialog.setOnDismissListener(dialog1 -> onDialogDismiss.runOnDialogDismiss(this));

        return dialog;
    }

    public Dialog reloadView() {
        setViewContent.runSetViewContent(this, view);
        return dialog;
    }

    public CustomDialog dismiss() {
        dialog.dismiss();
        return this;
    }

    public View findViewById(int id) {
        return dialog.findViewById(id);
    }

    static void setDialogLayoutParameters(Dialog dialog, boolean width, boolean height) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        if (width)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        else
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        if (height)
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        else
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public static String getEditText(Dialog dialog) {
        EditText editText = dialog.findViewById(R.id.dialog_custom_edit);
        if (editText == null)
            return null;
        else
            return editText.getText().toString().trim();
    }

    public String getEditText() {
        EditText editText = dialog.findViewById(R.id.dialog_custom_edit);
        if (editText == null)
            return null;
        else
            return editText.getText().toString().trim();
    }

    public static void changeText(Dialog dialog, CharSequence text) {
        ((TextView) dialog.findViewById(R.id.dialog_custom_text)).setText(text);
    }

}


