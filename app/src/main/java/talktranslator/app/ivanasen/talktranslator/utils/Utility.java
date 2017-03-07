package talktranslator.app.ivanasen.talktranslator.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import com.google.common.primitives.Chars;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;

import talktranslator.app.ivanasen.talktranslator.R;

public class Utility {


    private static final String LOG_TAG = Utility.class.getSimpleName();
    public static final String PREFERENCES_NAME = "Translator_prefs";
    public static final String LEFT_TRANSLATOR_LANGUAGE = "leftTranslatorLang";
    public static final String RIGHT_TRANSLATOR_LANGUAGE = "rightTranslatorLang";

    public static String getTranslatorLanguage(Context context, String translatorLanguageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return prefs.getString(translatorLanguageName,
                translatorLanguageName.equals(LEFT_TRANSLATOR_LANGUAGE) ? "English" : "Bulgarian");
    }

    public static void setTranslatorLanguage(Context context, String translatorLanguageName, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        prefs.edit().putString(translatorLanguageName, language).apply();
    }

    public static String getLanguageFromCode(Context context, String langCode) {
        int i = -1;
        for (String currentLang : context.getResources().getStringArray(R.array.lang_codes)) {
            i++;
            if (currentLang.equals(langCode)) {
                return context.getResources().getStringArray(R.array.languages)[i];
            }
        }
        return null;
    }

    public static String getCodeFromLanguage(Context context, String language, boolean forTextToSpeech) {
        boolean isBulgarianTTSEnabled = isBulgarianTextToSpeechEnabled(context);
        int i = -1;
        for (String currentLang : context.getResources().getStringArray(R.array.languages)) {
            i++;
            if (currentLang.equals(language)) {
                String langCode = context.getResources().getStringArray(R.array.lang_codes)[i];
                if (langCode.equals(context.getResources().getString(R.string.lang_code_bg))
                        && forTextToSpeech && isBulgarianTTSEnabled) {
                    return context.getResources().getString(R.string.lang_code_ru);
                }
                return langCode;
            }
        }
        return null;
    }

    public static boolean isBulgarianTextToSpeechEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_enable_bg_text_to_speech_key);
        return prefs.getBoolean(prefKey, true);
    }

    public static String getTranslateFromLanguage(String fromLangToLang) {
        if (fromLangToLang == null || fromLangToLang.length() == 0) {
            return null;
        }
        String langCode = "";
        char[] chars = fromLangToLang.toCharArray();
        for (char aChar : chars) {
            if (aChar == '-') {
                break;
            }
            langCode += aChar;
        }
        return langCode;
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
        Locale resultLocale = null;
        for (Locale locale : locales) {
            String currentLang;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                currentLang = locale.toLanguageTag();
            } else {
                currentLang = locale.getDisplayLanguage();
            }

            if (currentLang.contains(langCode)) {
                resultLocale = locale;
                break;
            }
        }
        return resultLocale;
    }

    public static String editBulgarianTextForRussianReading(String text) {
        List<Character> charsList = new ArrayList<>(Chars.asList(text.toCharArray()));

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

        return listToString(charsList);
    }

    private static String listToString(List<Character> charsList) {
        StringBuilder builder = new StringBuilder();
        for (Character aChar : charsList) {
            builder.append(aChar);
        }
        return builder.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return cm.getActiveNetworkInfo() != null && info.isConnectedOrConnecting();
    }

    public static void copyTextToClipboard(Context context, CharSequence translatedText) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name),
                translatedText);
        clipboard.setPrimaryClip(clip);

        Toast msg = Toast.makeText(context,
                context.getString(R.string.notify_text_copied), Toast.LENGTH_SHORT);
        msg.show();
    }

    public static String getSpeechRecognitionErrorText(Context context, int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = context.getString(R.string.error_audio);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = context.getString(R.string.error_something_wrong);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = context.getString(R.string.error_permissions);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = context.getString(R.string.error_network);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = context.getString(R.string.error_server);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = context.getString(R.string.error_no_match);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = context.getString(R.string.error_recognition_service);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = context.getString(R.string.error_server);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = context.getString(R.string.error_no_speech);
                break;
            default:
                message = context.getString(R.string.error_no_match);
        }
        return message;
    }

    public static long getSecondsFromChronometer(String chronoText) {
        String[] minsAndSeconds = chronoText.split(":");
        int mins = Integer.parseInt(minsAndSeconds[0]);
        return Integer.parseInt(minsAndSeconds[1]) + (mins * 60);
    }
}
