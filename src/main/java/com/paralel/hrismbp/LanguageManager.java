package com.paralel.hrismbp;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static Locale currentLocale = new Locale("id","ID");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static Locale getCurrentLocal(){
        return currentLocale;
    }
    public static void setLanguage(String langCode){
        if ("en".equalsIgnoreCase(langCode)){
            currentLocale = new Locale("en","US");
        }else {
            currentLocale = new Locale("id", "ID");
        }
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static String get(String key){
        try {
            return bundle.getString(key);
        }catch (Exception e){
            return key;    //fallback key
        }
    }

    public static ResourceBundle getBundle(){
        return bundle;
    }

}
