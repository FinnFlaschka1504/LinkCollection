package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.maxMustermannGeheim.linkcollection.R;

import org.apmem.tools.layouts.FlowLayout;

public class CustomDialog_new {

    public enum BUTTON_CONFIGURATION {
        YES_NO, SAVE_CANCEL, BACK, OK, OK_CANCEL, CUSTOM;
    }

    public enum BUTTON_TYPE {
        YES_BUTTON("Ja"), NO_BUTTON("Nein"), SAVE_BUTTON("Speichern")
        , CANCEL_BUTTON("Abbrechen"), BACK_BUTTON("Zurück"), OK_BUTTON("Ok");

        String label;

        BUTTON_TYPE(String label) {
            this.label = label;
        }
    }

    private Dialog dialog;
    private Context context;
    private CharSequence title;
    private CharSequence text;
    private View view;
    private BUTTON_CONFIGURATION buttonConfiguration = BUTTON_CONFIGURATION.CUSTOM;
    private Pair<Boolean, Boolean> dimensions = new Pair<>(true, false);
    private boolean dividerVisibility = true;
    private boolean isDividerVisibilityCustom = false;
    private Object objectExtra;
    private boolean scroll = true;
    private EditBuilder editBuilder;
    private boolean showEdit;
    private boolean buttonLabelAllCaps = true;

    private SetViewContent setViewContent;
    private OnDialogDismiss onDialogDismiss;

    private CustomList<ButtonHelper> buttonHelperList = new CustomList<>();



    public CustomDialog_new(Context context) {
        this.context = context;
        dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.dialog_custom_new);
    }

    public static CustomDialog_new Builder(Context context) {
        CustomDialog_new customDialog = new CustomDialog_new(context);
        return customDialog;
    }
    

//  ----- Getters & Setters ----->
    public CustomDialog_new setContext(Context context) {
        this.context = context;
        return this;
    }

    public CustomDialog_new setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomDialog_new setText(CharSequence text) {
        this.text = text;
        return this;
    }

    public CustomDialog_new setView(int layoutId) {
        LayoutInflater li = LayoutInflater.from(context);
        this.view = li.inflate(layoutId, null);
        return this;
    }

    public CustomDialog_new setView(View view) {
        this.view = view;
        return this;
    }

    public CustomDialog_new setButtonConfiguration(BUTTON_CONFIGURATION buttonConfiguration) {
        this.buttonConfiguration = buttonConfiguration;
        return this;
    }

    public CustomDialog_new setSetViewContent(CustomDialog_new.SetViewContent setViewContent) {
        this.setViewContent = setViewContent;
        return this;
    }

    public CustomDialog_new setDimensions(boolean width, boolean height) {
        this.dimensions = new Pair<>(width, height);
        return this;
    }

    public CustomDialog_new setDividerVisibility(boolean dividerVisibility) {
        this.dividerVisibility = dividerVisibility;
        isDividerVisibilityCustom = true;
        return this;
    }

    public CustomDialog_new setEdit(EditBuilder editBuilder) {
        this.showEdit = true;
        this.editBuilder = editBuilder;
        return this;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public Object getObjectExtra() {
        return objectExtra;
    }

    public CustomDialog_new setObjectExtra(Object objectExtra) {
        this.objectExtra = objectExtra;
        return this;
    }

    public CustomDialog_new setOnDialogDismiss(OnDialogDismiss onDialogDismiss) {
        this.onDialogDismiss = onDialogDismiss;
        return this;
    }

    public CustomDialog_new disableScroll() {
        scroll = false;
        return this;
    }

    public CustomDialog_new disableButtonAllCaps() {
        buttonLabelAllCaps = false;
        return this;
    }
//  <----- Getters & Setters -----
    
    
//  ----- Interfaces ----->
    public interface OnClick {
        void runOnClick(CustomDialog_new customDialog);
    }

    public interface SetViewContent{
        void runSetViewContent(CustomDialog_new customDialog, View view);
    }

    public interface OnDialogDismiss {
        void runOnDialogDismiss(CustomDialog_new customDialog);
    }
//  <----- Interfaces -----
    

//  ----- Builder ----->
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
    private CustomDialog_new.EditBuilder.INPUT_TYPE inputType = CustomDialog_new.EditBuilder.INPUT_TYPE.CAP_SENTENCES;
//
//    public CustomDialog_new.EditBuilder setText(String text) {
//        this.text = text;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setHint(String hint) {
//        this.hint = hint;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setShowKeyboard(boolean showKeyboard) {
//        this.showKeyboard = showKeyboard;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setSelectAll(boolean selectAll) {
//        this.selectAll = selectAll;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setDiableButtonWhenEmpty(int buttonId) {
//        this.buttonId = buttonId;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setInputType(CustomDialog_new.EditBuilder.INPUT_TYPE inputType) {
//        this.inputType = inputType;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setCheckRegEx(int buttonId, String regEx) {
//        this.buttonId = buttonId;
//        this.regEx = regEx;
//        return this;
//    }
//
//    public CustomDialog_new.EditBuilder setFireButtonOnOK(int buttonId) {
//        this.buttonId = buttonId;
//        this.fireButtonOnOK = true;
//        return this;
//    }
}

    public static class TextBuilder{

    }
//  <----- Builder -----
    
    
//  ----- Convenience ----->
    public CustomDialog_new dismiss() {
    dialog.dismiss();
    return this;
}

    public View findViewById(int id) {
        return dialog.findViewById(id);
    }

    public String getEditText() {
        TextInputEditText editText = dialog.findViewById(R.id.dialog_custom_edit);
        if (editText == null)
            return null;
        else
            return editText.getText().toString().trim();
    }

    public static void changeText(Dialog dialog, CharSequence text) {
        ((TextView) dialog.findViewById(R.id.dialog_custom_text)).setText(text);
    }
//  <----- Convenience -----


//  ----- Buttons ----->
    class ButtonHelper {
        private Integer id;
        private String label;
        private BUTTON_TYPE buttonType;
        private OnClick onClick;
        private boolean dismiss;
        private Button button;
//        private boolean gravityLeft;

        public ButtonHelper(BUTTON_TYPE buttonType) {
            this.buttonType = buttonType;
            label = buttonType.label;
            dismiss = true;
            button = new Button(context);
            button.setBackground(dialog.findViewById(R.id.dialog_custom_Button1).getBackground().getConstantState().newDrawable());
            button.setTextColor(((Button)dialog.findViewById(R.id.dialog_custom_Button1)).getTextColors());
        }

        public ButtonHelper(String label, BUTTON_TYPE buttonType, OnClick onClick, Integer id, boolean dismiss) {
            this.id = id;
            this.label = label;
            this.buttonType = buttonType;
            this.onClick = onClick;
            this.dismiss = dismiss;
//            gravityLeft = true;
        }

        public Button generateButton() {
            Button button = new Button(context);
            button.setBackground(dialog.findViewById(R.id.dialog_custom_Button1).getBackground().getConstantState().newDrawable());
            button.setTextColor(((Button)dialog.findViewById(R.id.dialog_custom_Button1)).getTextColors());

            if (label != null)
                button.setText(label);
            else if (buttonType != null)
                button.setText(buttonType.label);

            if (id != null)
                button.setId(id);

            if (!buttonLabelAllCaps)
                button.setAllCaps(false);

//            if (gravityLeft) {
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                params.gravity = Gravity.START;
//
//                button.setLayoutParams(params);
//
//                LinearLayout linearLayout = new LinearLayout(context);
//
//                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
//                linearLayout.setLayoutParams(params);
//                linearLayout.addView(button);
//                linearLayout.setBackgroundColor(Color.RED);
//                ((FlowLayout) dialog.findViewById(R.id.dialog_custom_buttonLayout)).addView(linearLayout);
//
//            }
//            else
            ((FlowLayout) dialog.findViewById(R.id.dialog_custom_buttonLayout)).addView(button);
            this.button = button;

            button.setOnClickListener(v -> {
                if (dismiss)
                    dialog.dismiss();

                if (onClick != null)
                    onClick.runOnClick(CustomDialog_new.this);
            });
            return button;
        }
    }

    public CustomDialog_new addButton(String buttonName) {
        return addButton_complete(buttonName, null, null, null, true);
    }
    public CustomDialog_new addButton(String buttonName, OnClick onClick) {
        return addButton_complete(buttonName, null, onClick, null, true);
    }
    public CustomDialog_new addButton(String buttonName, OnClick onClick, int buttonId) {
        return addButton_complete(buttonName, null, onClick, buttonId, true);
    }
    public CustomDialog_new addButton(String buttonName, OnClick onClick, boolean dismissDialog){
        return addButton_complete(buttonName, null, onClick, null, dismissDialog);
    }
    public CustomDialog_new addButton(String buttonName, OnClick onClick, int buttonId, boolean dismissDialog){
        return addButton_complete(buttonName, null, onClick, buttonId, dismissDialog);
    }

    public CustomDialog_new addButton(BUTTON_TYPE button_type) {
        return addButton_complete(null , button_type, null, null, true);
    }
    public CustomDialog_new addButton(BUTTON_TYPE button_type, OnClick onClick) {
        return addButton_complete(null , button_type, onClick, null, true);
    }
    public CustomDialog_new addButton(BUTTON_TYPE button_type, OnClick onClick, int buttonId) {
        return addButton_complete(null , button_type, onClick, buttonId, true);
    }
    public CustomDialog_new addButton(BUTTON_TYPE button_type, OnClick onClick, boolean dismissDialog){
        return addButton_complete(null , button_type, onClick, null, dismissDialog);
    }
    public CustomDialog_new addButton(BUTTON_TYPE button_type, OnClick onClick, int buttonId, boolean dismissDialog){
        return addButton_complete(null , button_type, onClick, buttonId, dismissDialog);
    }


    private CustomDialog_new addButton_complete(String buttonName, BUTTON_TYPE button_type, OnClick onClick, Integer buttonId, boolean dismissDialog) {
        ButtonHelper buttonHelper = new ButtonHelper(buttonName, button_type, onClick, buttonId, dismissDialog);
        buttonHelperList.add(buttonHelper);
        return this;
    }

    public CustomDialog_new hideLastAddedButton(){
        // ToDo:
        return this;
    }
    public CustomDialog_new disableLastAddedButton(){
        // ToDo:
        return this;
    }
    // ToDo: mit LayoutGravity nach links verschieben
//  <----- Buttons -----
    
    
//  ----- Actions ----->
    public Dialog show_dialog() {
    show();
    return dialog;
}

    public CustomDialog_new show() {
        TextView dialog_custom_title = dialog.findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_text = dialog.findViewById(R.id.dialog_custom_text);

        // ToDo: TextBuilder

        if (title != null)
            dialog_custom_title.setText(this.title);
        else
            dialog_custom_title.setVisibility(View.GONE);

        if (text != null)
            dialog_custom_text.setText(this.text);
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
            } else {
                LinearLayout dialog_custom_layout_view_interface_restrained = dialog.findViewById(R.id.dialog_custom_layout_view_interface_restrained);
                dialog_custom_layout_view_interface_restrained.addView(view, layoutParams);
                dialog_custom_layout_view_interface_restrained.setVisibility(View.VISIBLE);
            }
        } else {
            dialog.findViewById(R.id.dialog_custom_layout_view).setVisibility(View.GONE);
        }

        // ToDo: Divider

        switch (buttonConfiguration) {
            case BACK:
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.BACK_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.BACK_BUTTON));
                break;
            case OK:
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.OK_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.OK_BUTTON));
                break;
            case OK_CANCEL:
                if (buttonHelperList.stream().anyMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.OK_BUTTON)
                        && buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.CANCEL_BUTTON)) {
                    Integer index = buttonHelperList.indexOf(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.OK_BUTTON);
                    buttonHelperList.add(index, new ButtonHelper(BUTTON_TYPE.CANCEL_BUTTON));
                    break;
                }
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.CANCEL_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.CANCEL_BUTTON));
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.OK_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.OK_BUTTON));
                break;
            case YES_NO:
                if (buttonHelperList.stream().anyMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.YES_BUTTON)
                        && buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.NO_BUTTON)) {
                    Integer index = buttonHelperList.indexOf(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.YES_BUTTON);
                    buttonHelperList.add(index, new ButtonHelper(BUTTON_TYPE.NO_BUTTON));
                    break;
                }
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.NO_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.NO_BUTTON));
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.YES_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.YES_BUTTON));
                break;
            case SAVE_CANCEL:
                if (buttonHelperList.stream().anyMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.SAVE_BUTTON)
                        && buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.CANCEL_BUTTON)) {
                    Integer index = buttonHelperList.indexOf(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.SAVE_BUTTON);
                    buttonHelperList.add(index, new ButtonHelper(BUTTON_TYPE.CANCEL_BUTTON));
                    break;
                }
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.CANCEL_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.CANCEL_BUTTON));
                if (buttonHelperList.stream().noneMatch(buttonHelper -> buttonHelper.buttonType == BUTTON_TYPE.SAVE_BUTTON))
                    buttonHelperList.add(new ButtonHelper(BUTTON_TYPE.SAVE_BUTTON));
                break;
        }

        buttonHelperList.forEach(ButtonHelper::generateButton);

        // ToDo: EditBuilder

        setDialogLayoutParameters(dialog, dimensions.first, dimensions.second);
        dialog.show();
        return this;
    }

    public CustomDialog_new reloadView() {
        setViewContent.runSetViewContent(this, view);
        return this;
    }
//  <----- Actions -----

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
        dialog.getWindow().setAttributes(lp);
    }

}
