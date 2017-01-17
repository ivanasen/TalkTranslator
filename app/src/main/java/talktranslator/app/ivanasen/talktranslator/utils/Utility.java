package talktranslator.app.ivanasen.talktranslator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

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

    public static String getCodeFromLanguage(Context context, String language) {
        int i = -1;
        for (String currentLang : context.getResources().getStringArray(R.array.languages)) {
            i++;
            if (language.equals(currentLang))
                break;
        }
        return context.getResources().getStringArray(R.array.lang_codes)[i];
    }
}
