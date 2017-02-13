package talktranslator.app.ivanasen.talktranslator.models;

import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/11/2017.
 */

public class ChatTranslation {
    private String translatedText;
    private String originalText;
    private boolean isLeftTranslation;
    private String language;

    public ChatTranslation() {
    }

    public ChatTranslation(String translatedText, String originalText, boolean isLeftTranslation,
                           String translationLanguage) {
        this.translatedText = translatedText;
        this.originalText = originalText;
        this.isLeftTranslation = isLeftTranslation;
        this.language = translationLanguage;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public boolean isLeftTranslation() {
        return isLeftTranslation;
    }

    public void setLeftTranslation(boolean leftTranslation) {
        isLeftTranslation = leftTranslation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Locale getTranslatedLocaleFromTextToSpeechLocales(Set<Locale> locales) {
        String langCode = Utility.getTranslatedLanguage(language);
        Locale locale = Utility.getLocaleFromLangCode(langCode, locales);
        return locale;
    }
}
