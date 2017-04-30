package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.activities.CopyToTranslateActivity;
import talktranslator.app.ivanasen.talktranslator.activities.SettingsActivity;
import talktranslator.app.ivanasen.talktranslator.translation.LanguageWrapper;
import talktranslator.app.ivanasen.talktranslator.translation.TranslationResult;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class CopyToTranslateFragment extends Fragment {
    private static final String LOG_TAG = CopyToTranslateActivity.class.getSimpleName();

    private Context mContext;
    private View mRootView;
    private Translator mTranslator;

    private ImageButton mSettingsBtn;
    private TextView mTranslationLangView;
    private TextView mTranslationView;
    private TextView mOriginalLangView;
    private TextView mOriginalTextView;
    private Button mOpenAppBtn;

    private String mCopiedText;
    private String mCopiedLang;
    private String mTranslationText;
    private String mTranslationLang;

    public CopyToTranslateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        mRootView = inflater.inflate(R.layout.fragment_copy_to_translate, container, false);

        mTranslator = new Translator(mContext);

        mTranslationLang = "bg";//TODO:fix
        mCopiedText = getActivity().getIntent()
                .getStringExtra(CopyToTranslateActivity.EXTRA_TEXT_TO_TRANSLATE);
        translateCopiedText();

        initViews();

        return mRootView;
    }

    private void translateCopiedText() {
        mTranslator.detectLanguage(mCopiedText, new Callback<LanguageWrapper>() {
            @Override
            public void onResponse(Call<LanguageWrapper> call,
                                   Response<LanguageWrapper> response) {
                LanguageWrapper languageWrapper = response.body();
                String langCode = languageWrapper.getLang();

                mCopiedLang = Utility.getLanguageFromCode(mContext, langCode);
                mOriginalLangView.setText(mCopiedLang);

                String fromLangToLang = langCode + "-" + mTranslationLang;
                mTranslator.translate(mCopiedText, fromLangToLang, new Callback<TranslationResult>() {
                    @Override
                    public void onResponse(Call<TranslationResult> call, Response<TranslationResult> response) {
                        TranslationResult result = response.body();
                        mTranslationText = result.getText()[0];
                        mTranslationView.setText(mTranslationText);
                    }

                    @Override
                    public void onFailure(Call<TranslationResult> call, Throwable t) {
                        Log.e(LOG_TAG, "Failed translation");
                    }
                });
            }

            @Override
            public void onFailure(Call<LanguageWrapper> call, Throwable t) {
                Log.e(LOG_TAG, "Failed language detection");
            }
        });

    }

    private void initViews() {
        mSettingsBtn = (ImageButton) mRootView.findViewById(R.id.settings_btn);
        mTranslationLangView = (TextView) mRootView.findViewById(R.id.translation_lang_textview);
        mTranslationView = (TextView) mRootView.findViewById(R.id.translation_textview);
        mOriginalLangView = (TextView) mRootView.findViewById(R.id.original_language_textview);
        mOriginalTextView = (TextView) mRootView.findViewById(R.id.original_text_textview);
        mOpenAppBtn = (Button) mRootView.findViewById(R.id.open_in_app_btn);

        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                startActivity(intent);
            }
        });
        mTranslationLangView.setText(mTranslationLang);

        mOriginalLangView.setText(mCopiedLang);
        mOriginalTextView.setText(mCopiedText);

        mOpenAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Open in app
            }
        });
    }
}
