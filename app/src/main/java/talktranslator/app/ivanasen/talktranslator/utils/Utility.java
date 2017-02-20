package talktranslator.app.ivanasen.talktranslator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.google.common.primitives.Chars;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            String currentLang = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                currentLang = locale.toLanguageTag();
            } else {
                currentLang = locale.getDisplayLanguage();
            }

            if (currentLang.contains(langCode)) {
                return locale;
            }
        }

        return null;
    }

    public static String editBulgarianTextForRussianReading(String text) {
        List<Character> charsList = new ArrayList<>( Chars.asList(text.toCharArray()) );

        for (int i = 0; i < charsList.size(); i++) {
            switch (charsList.get(i)) {
                case 'e':
                    if (i > 0 && charsList.get(i - 1) == ' ') {
                        charsList.remove(i - 1);
                    }
                    break;
                case 'ъ':
                    charsList.remove(i);
                    charsList.add(i, 'э');
                    break;
                case 'щ':
                    if (i == charsList.size() - 1) {
                        charsList.add('т');
                    } else {
                        charsList.add(i + 1, 'т');
                    }
                    break;
                case 'о':
                    charsList.remove(i);
                    charsList.add(i, 'у');
                    break;
            }
        }

        String result = listToString(charsList);

        return result;
    }

    private static String listToString(List<Character> charsList) {
        StringBuilder builder = new StringBuilder();

        for(Character aChar : charsList) {
            builder.append(aChar);
        }

        return builder.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
