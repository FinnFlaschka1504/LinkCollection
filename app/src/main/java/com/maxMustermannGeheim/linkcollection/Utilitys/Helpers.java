package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Helpers {
    //  ----- TextInput ----->
    public static class TextInputHelper {
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
        public enum IME_ACTIONS{
            GO(0x00000002), SEARCH(0x00000003), SEND(0x00000004), NEXT(0x00000005)
            , DONE(0x00000006), PREVIOUS(0x00000007);

            int code;

            IME_ACTIONS(int code) {
                this.code = code;
            }
        }
        private CustomList<TextInputLayout> layoutList;
        private Map<TextInputLayout, Validator> inputValidationMap = new HashMap<>();
        private OnValidationResult onValidationResult;
        private boolean valid;
//        private boolean useStandardValidation = true;

        public TextInputHelper(OnValidationResult onValidationResult, TextInputLayout... inputLayouts) {
            this.onValidationResult = onValidationResult;
            this.layoutList = new CustomList<>(inputLayouts);
            inputValidationMap = this.layoutList.stream().collect(Collectors.toMap(o -> o, Validator::new));
            applyValidationListeners();
        }

        public TextInputHelper setOnValidationResult(OnValidationResult onValidationResult) {
            this.onValidationResult = onValidationResult;
            return this;
        }

        public TextInputHelper defaultDialogValidation(CustomDialog customDialog) {
            setOnValidationResult(customDialog.getActionButton()::setEnabled);
            return this;
        }

        //  ----- Validation ----->
        public boolean validate(TextInputLayout... layoutLists) {
            boolean all = false;
            if (layoutLists.length == 1 && layoutLists[0] == null)
                all = true;
            List<TextInputLayout> inputLayoutList = new CustomList<>(layoutLists);
            for (Map.Entry<TextInputLayout, Validator> entry : inputValidationMap.entrySet()) {
                if (!entry.getValue().validate(entry.getKey().getEditText().getText().toString().trim(), inputLayoutList.contains(entry.getKey()) || all)) {
                    valid = false;
                    if (onValidationResult != null)
                        onValidationResult.runOnValidationResult(false);
                    return false;
                }
            }
            valid = true;
            if (onValidationResult != null)
                onValidationResult.runOnValidationResult(true);
            return true;
        }

        public void changeValidation(TextInputLayout textInputLayout, TextValidation textValidation) {
            inputValidationMap.get(textInputLayout).setTextValidation(textValidation);
        }
        public void changeValidation(TextInputLayout textInputLayout, String regEx) {
            inputValidationMap.get(textInputLayout).setRegEx(regEx);
        }

//        public void addValidation (TextInputLayout textInputLayout, TextValidation textValidation)

        public TextInputHelper addValidator(@NonNull TextInputLayout... textInputLayouts) {
            for (TextInputLayout textInputLayout : textInputLayouts) {
                inputValidationMap.put(textInputLayout, new Validator(textInputLayout));
                applyValidationListerner(textInputLayout);
            }
            return this;
        }

        public void applyValidationListeners(TextInputLayout... inputLayouts) {
            if (inputLayouts.length > 0) {
                for (TextInputLayout inputLayout : inputLayouts)
                    applyValidationListerner(inputLayout);
            } else {
                for (TextInputLayout inputLayout : inputValidationMap.keySet())
                    applyValidationListerner(inputLayout);
            }
        }
        private void applyValidationListerner(TextInputLayout textInputLayout) {
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

        public interface TextValidation {
            void runTextValidation(Validator validator, String text);
        }

        public static class Validator {
            enum STATUS {
                NONE, VALID, INVALID
            }

            enum MODE {
                WHITE_LIST, BLACK_LIST
            }
            private boolean alwaysUseDefaultValidation = true;
            private STATUS status = STATUS.NONE;
            private MODE mode;
            private MODE defaultMode = MODE.BLACK_LIST;
            private String errorMessage = "<Fehler>";
            private TextValidation textValidation;
            private TextInputLayout textInputLayout;
            private TextWatcher textWatcher;
            private boolean useDefaultValidation = true;
            private boolean allowEmpty;
            private String regEx = "";

            public Validator(TextInputLayout textInputLayout) {
                this.textInputLayout = textInputLayout;
            }

            private void reset() {
                errorMessage = null;
                status = STATUS.NONE;
                useDefaultValidation = alwaysUseDefaultValidation;
                errorMessage = "<Fehler>";
                mode = defaultMode;
            }

            private void defaultValidation(String text, boolean changeErrorMessage) {
                if (text.isEmpty() && !allowEmpty) {
                    if (changeErrorMessage)
                        errorMessage = "Das Feld darf nicht leer sein!";
                    status = STATUS.INVALID;
                } else {
                    switch (mode) {
                        case BLACK_LIST:
                            if (changeErrorMessage)
                                errorMessage = null;
                            status = STATUS.VALID;
                            break;
                        case WHITE_LIST:
                            if (changeErrorMessage)
                                errorMessage = "Ungültige Eingabe";
                            status = STATUS.INVALID;
                            break;
                    }
                }
            }

            public boolean validate(String text, boolean changeErrorMessage) {
                reset();

                if (textValidation != null && !regEx.isEmpty())
                    textValidation.runTextValidation(this, text);

                if (!regEx.isEmpty()) {
                    if (text.matches(regEx)) {
                        status = STATUS.VALID;
                        errorMessage = null;
                    } else {
                        status = STATUS.INVALID;
                        errorMessage = "Ungültige Eingabe";
                    }
                }

                if (status == STATUS.NONE && useDefaultValidation)
                    defaultValidation(text, changeErrorMessage);

                if (changeErrorMessage)
                    textInputLayout.setError(errorMessage);

                switch (status) {
                    case VALID:
                        return true;

                    default:
                    case INVALID:
                        return false;
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
                errorMessage = null;
                status = STATUS.VALID;
            }
            public void setInalid(String errorMessage) {
                this.errorMessage = errorMessage;
                status = STATUS.INVALID;
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
        }
        public interface OnValidationResult {
            void runOnValidationResult(boolean result);
        }
        //  <----- Validation -----


        //  ----- Action ----->
        public void addActionListener(TextInputLayout textInputLayout, OnAction onAction, IME_ACTIONS... actions) {
            textInputLayout.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                boolean handled = false;
                if (actions.length == 0) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onAction.runOnAction(this, textInputLayout, actionId);
                        handled = true;
                    }
                } else {
                    if (new CustomList<>(actions).contains(actionId)) {
                        onAction.runOnAction(this, textInputLayout, actionId);
                        handled = true;
                    }
                }
                return handled;
            });
            if (actions.length == 0) {
                textInputLayout.getEditText().setImeOptions(IME_ACTIONS.DONE.code);
            } else {
                final int[] actionFlag = {actions[0].code};
                new CustomList<Integer>(actions).forEachCount((integer, count) -> {
                    if (count == 0) return;

                    actionFlag[0] = actionFlag[0]| integer;
                });
                textInputLayout.getEditText().setImeOptions(actionFlag[0]);
            }
        }

        public interface OnAction {
            void runOnAction(TextInputHelper textInputHelper, TextInputLayout textInputLayout, int actionId);
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
        //  <----- Convenience -----
    }
    //  <----- TextInput -----
}
