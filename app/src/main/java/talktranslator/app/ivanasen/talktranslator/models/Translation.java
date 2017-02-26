package talktranslator.app.ivanasen.talktranslator.models;

import com.orm.SugarRecord;

/**
 * Created by ivan on 2/25/2017.
 */

public class Translation extends SugarRecord {
    private String translatedText;
    private String originalText;
    private String language;

    public Translation() {
    }

    public Translation(String translatedText, String originalText, String language) {
        this.translatedText = translatedText;
        this.originalText = originalText;
        this.language = language;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
