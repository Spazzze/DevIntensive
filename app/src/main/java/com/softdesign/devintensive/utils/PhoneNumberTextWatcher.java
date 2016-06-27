package com.softdesign.devintensive.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneNumberTextWatcher implements TextWatcher {

    private static final String TAG = ConstantManager.TAG_PREFIX + PhoneNumberTextWatcher.class
            .getSimpleName();
    private EditText edTxt;

    public PhoneNumberTextWatcher(EditText edTxtPhone) {
        this.edTxt = edTxtPhone;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    public void afterTextChanged(Editable s) {
        String val = s.toString();
        if (val.length() < 1) {
            edTxt.removeTextChangedListener(this);
            edTxt.setText("");
            edTxt.addTextChangedListener(this);
            return;
        }
        int cursorPosition = edTxt.getSelectionStart();
        Boolean setCursorPosToEnd = false;
        if (cursorPosition == s.toString().length()) {
            setCursorPosToEnd = true;
        }
        //запрещаем дальнейший ввод если количество символов уже достаточное
        //а именно: если формат +7(123)222-3333 то количество = 15
        //+380(22)333-3232 (украина), +375(33)333-3333 (беларусь), количество = 16
        //если формат (333)333-3333, количество = 13 и нововведенный символ не 7, не 3 и не + в начале номера перед (.
        if (val.length() == 16 && val.startsWith("+7") ||
                val.length() == 17 && (val.startsWith("+37") || val.startsWith("+38"))||
                val.length() == 14 && (val.startsWith("(") ||
                        val.charAt(1) == '(' && !val.startsWith("3") && !val.startsWith("7") && !val.startsWith("+"))){
            if (setCursorPosToEnd) {
                val = val.substring(0,val.length()-1);
            } else {
                if (cursorPosition > 0){
                    String s2 = val.substring(cursorPosition);
                    val = val.substring(0,cursorPosition-1) + s2;
                    cursorPosition -= 1;
                } else {
                    val = val.substring(0,val.length()-1);
                }
            }
        }
        val = val.replaceAll("[\\-\\(\\)\\,\\.\\/\\*\\#\\;N\\s]", "");
        String countryCode = "";
        String cityCode = "";
        String firstNumberPart = "";
        String secondNumberPart = "";
        int index = 0;
        if (val != null && val.length() > 0) {
            //ishem countryCode
            if (val.charAt(0) == '+' || val.charAt(0) == '7' || val.charAt(0) == '3') {
                if (val.length() == 1) { // + или 7 или 3
                    index = 1;
                    if (val.charAt(0) == '+') {
                        countryCode = "+";
                    } else {
                        countryCode = "+" + val;
                    }
                } else { //length > 1
                    int searchIndex = 0;
                    if (val.charAt(0) == '+') {
                        index = 1;
                        searchIndex = 1;
                    }
                    if (val.charAt(searchIndex) == '7') {
                        countryCode = "+7";
                        index = searchIndex + 1;
                    } else if (val.charAt(searchIndex) == '3') {
                        if (val.length() > 2) {
                            if (val.charAt(searchIndex + 1) == '7' || val.charAt(searchIndex + 1) == '8') { //ishem 37 ili 38
                                if (val.length() > 3) {
                                    countryCode = "+" + val.substring(searchIndex, searchIndex + 3); //+37x ili +38x
                                    index = searchIndex + 3;
                                } else {
                                    countryCode = "+" + val.substring(searchIndex); //+37 ili +38
                                    index = searchIndex + 2;
                                }
                            }
                        } else {
                            countryCode = "+3";
                            index = searchIndex + 1;
                        }
                    }
                }
            }
            if (index < val.length()) {
                val = val.substring(index);
                int a = 0;
                if (countryCode.length() > 0) {
                    if (countryCode.startsWith("+3")){
                        a = -1;
                    }
                }
                if (val.length() <= 3+a) {
                    cityCode = val.substring(0, val.length());
                } else {
                    cityCode = val.substring(0, 3+a);
                    if (val.length() <= 6+a) {
                        firstNumberPart = val.substring(3+a, val.length());
                    } else {
                        firstNumberPart = val.substring(3+a, 6+a);
                        secondNumberPart = val.substring(6+a, val.length());
                    }
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (countryCode.length() > 0) {
                stringBuilder.append(countryCode);
            }
            if (cityCode.length() > 0) {
                stringBuilder.append("(");
                stringBuilder.append(cityCode);
            }
            if (firstNumberPart.length() > 0) {
                stringBuilder.append(")");
                stringBuilder.append(firstNumberPart);
            }
            if (secondNumberPart.length() > 0) {
                stringBuilder.append("-");
                stringBuilder.append(secondNumberPart);
            }
            edTxt.removeTextChangedListener(this);
            edTxt.setText(stringBuilder.toString());
            if (setCursorPosToEnd || cursorPosition > edTxt.getText().toString().length()) {
                edTxt.setSelection(edTxt.getText().toString().length());
            } else {
                edTxt.setSelection(cursorPosition);
            }
            edTxt.addTextChangedListener(this);
        } else {
            edTxt.removeTextChangedListener(this);
            edTxt.setText("");
            edTxt.addTextChangedListener(this);
        }
    }
}