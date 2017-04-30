package talktranslator.app.ivanasen.talktranslator.translation;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import talktranslator.app.ivanasen.talktranslator.BuildConfig;
import talktranslator.app.ivanasen.talktranslator.R;

public class Translator {
    private final String LOG_TAG = Translator.class.getSimpleName();

    private TranslationService mService;

    public Translator(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = retrofit.create(TranslationService.class);
    }

    public void translate(String textToTranslate, String fromLanguageCodeToLanguageCode,
                          Callback<TranslationResult> callback) {
        Call<TranslationResult> translationCall =
                mService.translate(BuildConfig.YANDEX_TRANSLATE_API_KEY, textToTranslate, fromLanguageCodeToLanguageCode);
        translationCall.enqueue(callback);
    }

    public void detectLanguage(String text, Callback<LanguageWrapper> callback) {
        Call<LanguageWrapper> languageCall =
                mService.detectLanguage(BuildConfig.YANDEX_TRANSLATE_API_KEY, text);
        languageCall.enqueue(callback);
    }

}
