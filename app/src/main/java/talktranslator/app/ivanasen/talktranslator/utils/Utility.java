package talktranslator.app.ivanasen.talktranslator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.R;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();
    public static final String PREFERENCES_NAME = "Translator_prefs";
    public static final String LEFT_TRANSLATOR_LANGUAGE = "leftTranslatorLang";
    public static final String RIGHT_TRANSLATOR_LANGUAGE = "rightTranslatorLang";

    public static String getTranslatorLanguage(Context context, String translatorLanguageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return prefs.getString(translatorLanguageName, Locale.ENGLISH.getDisplayLanguage());
    }

    public static void setTranslatorLanguage(Context context, String translatorLanguageName, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        prefs.edit().putString(translatorLanguageName, language).apply();
    }

    public static String getCodeFromLanguage(Context context, String language) {
        int i = -1;
        for (String currentLang : context.getResources().getStringArray(R.array.languages)) {
            i++;
            if (language.equals(currentLang))
                break;
        }
        return context.getResources().getStringArray(R.array.lang_codes)[i];
    }

    public static String getTranslatedLanguage(String fromLangToLang) {
        if (fromLangToLang == null || fromLangToLang.length() == 0) {
            return null;
        }
        String langCode = "";
        int i = fromLangToLang.length() - 1;
        while (fromLangToLang.charAt(i) != '-' && i >= 0) {
            langCode += fromLangToLang.charAt(i);
            i--;
        }
        return new StringBuilder(langCode).reverse().toString();
    }

    public static Locale getLocaleFromLangCode(String langCode, Set<Locale> locales) {
        for (Locale locale : locales) {
            if (locale.getISO3Language().equals(langCode) ||
                    locale.getISO3Language().contains(langCode)) {
                return locale;
            }
        }

        return null;
    }

}
