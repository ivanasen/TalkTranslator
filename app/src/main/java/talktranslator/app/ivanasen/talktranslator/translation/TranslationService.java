package talktranslator.app.ivanasen.talktranslator.translation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface TranslationService {
    @GET("api/v1.5/tr.json/translate")
    Call<TranslationResult> translate(@Query("key") String apiKey,
                                      @Query("text") String textToTranslate,
                                      @Query("lang") String fromLangToLang);
}
