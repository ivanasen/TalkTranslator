package talktranslator.app.ivanasen.talktranslator.translation;

import retrofit2.http.GET;
import talktranslator.app.ivanasen.talktranslator.BuildConfig;

public interface TranslationService {
    @GET("key=" + BuildConfig.YANDEX_TRANSLATE_API_KEY + "&text={textToTranslate}" +
            "&lang={fromLang}-{toLang}")
    String translate(String textToTranslate, String fromLang, String toLang);
}
