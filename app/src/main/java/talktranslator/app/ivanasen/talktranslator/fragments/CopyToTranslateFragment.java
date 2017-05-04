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
import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.activities.SettingsActivity;
import talktranslator.app.ivanasen.talktranslator.models.Translation;
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

    private String mCopiedText;
    private String mCopiedLang;
    private String mTranslationText;
    private String mTranslationLang;
    private Translation mTranslation;

    private ImageButton mSettingsBtn;
    private TextView mTranslationLangView;
    private TextView mTranslationView;
    private TextView mOriginalLangView;
    private TextView mOriginalTextView;
    private Button mOpenAppBtn;

    public CopyToTranslateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        mRootView = inflater.inflate(R.layout.fragment_copy_to_translate, container, false);

        mTranslator = new Translator(mContext);
        translateCopiedText();
        initViews();

        return mRootView;
    }

    private void translateCopiedText() {
        final String translationLangCode = Utility.getPreferredTranslationLanguage(mContext);
        mTranslationLang = Utility.getLanguageFromCode(mContext, translationLangCode);
        mCopiedText = getActivity()
                .getIntent()
                .getStringExtra(CopyToTranslateActivity.EXTRA_TEXT_TO_TRANSLATE);

        final Callback<TranslationResult> translationCallback = new Callback<TranslationResult>() {
            @Override
            public void onResponse(Call<TranslationResult> call, Response<TranslationResult> response) {
                TranslationResult result = response.body();
                if (result == null) {
                    return;
                }
                mTranslationText = result.getText()[0];
                if (mTranslationText != null && !mTranslationText.equals("")) {
                    mTranslationView.setText(mTranslationText);
                    saveTranslation();
                } else {
                    closeActivity();
                }
            }

            @Override
            public void onFailure(Call<TranslationResult> call, Throwable t) {
                Log.e(LOG_TAG, "Failed translation");
                closeActivity();
            }
        };

        mTranslator.detectLanguage(mCopiedText, new Callback<LanguageWrapper>() {
            @Override
            public void onResponse(Call<LanguageWrapper> call,
                                   Response<LanguageWrapper> response) {
                LanguageWrapper languageWrapper = response.body();
                if (languageWrapper == null) {
                    return;
                }
                String langCode = languageWrapper.getLang();
                if (langCode == null) {
                    return;
                }

                mCopiedLang = Utility.getLanguageFromCode(mContext, langCode);
                mOriginalLangView.setText(String.format(
                        getString(R.string.translated_from_string_format), mCopiedLang));

                String fromLangToLang = langCode + "-" + translationLangCode;
                mTranslator.translate(mCopiedText, fromLangToLang, translationCallback);
            }

            @Override
            public void onFailure(Call<LanguageWrapper> call, Throwable t) {
                Log.e(LOG_TAG, "Failed language detection");
                closeActivity();
            }
        });

    }

    private void closeActivity() {
        getActivity().onBackPressed();
        getActivity().onBackPressed();
    }

    private void initViews() {
        mSettingsBtn = (ImageButton) mRootView.findViewById(R.id.settings_btn);
        mTranslationLangView = (TextView) mRootView.findViewById(R.id.translation_lang_textview);
        mTranslationView = (TextView) mRootView.findViewById(R.id.translation_textview);
        mOriginalLangView = (TextView) mRootView.findViewById(R.id.original_language_textview);
        mOriginalTextView = (TextView) mRootView.findViewById(R.id.original_text_textview);
        mOpenAppBtn = (Button) mRootView.findViewById(R.id.open_in_app_btn);

        mTranslationLangView.setText(mTranslationLang);
        mOriginalLangView.setText(mCopiedLang);
        mOriginalTextView.setText(mCopiedText);

        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        mOpenAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInApp();
            }
        });
    }

    private void saveTranslation() {
        String fromLangCode = Utility.getCodeFromLanguage(mContext, mCopiedLang, false);
        String toLangCode = Utility.getCodeFromLanguage(mContext, mTranslationLang, false);

        mTranslation = new Translation(mTranslationText,
                mCopiedText, fromLangCode + "-" + toLangCode);
        mTranslation.save();
    }

    private void openSettings() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivity(intent);
    }

    private void openInApp() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(MainActivity.TAB_SELECTED_EXTRA, MainActivity.TAB_POSITION_KEYBOARD);
        intent.putExtra(MainActivity.TRANSLATION_EXTRA, mTranslation.getId());
        startActivity(intent);
    }
}
