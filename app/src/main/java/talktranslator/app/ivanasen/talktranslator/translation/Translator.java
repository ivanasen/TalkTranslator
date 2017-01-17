package talktranslator.app.ivanasen.talktranslator.translation;

import android.content.Context;

import java.util.Locale;

import retrofit2.Retrofit;
import talktranslator.app.ivanasen.talktranslator.R;

public class Translator {
    private final String LOG_TAG = Translator.class.getSimpleName();

    private TranslationService mService;

    public Translator(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .build();
        mService = retrofit.create(TranslationService.class);
    }

    public String translate(String textToTranslate, Locale fromLanguage, Locale toLanguage) {
        return mService.translate(textToTranslate, fromLanguage.getDisplayLanguage(), toLanguage.getDisplayLanguage());
    }
}
