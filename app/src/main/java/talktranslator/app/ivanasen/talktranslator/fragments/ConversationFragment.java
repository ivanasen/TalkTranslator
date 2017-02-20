package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.Translation;
import talktranslator.app.ivanasen.talktranslator.translation.TranslationResult;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;
import talktranslator.app.ivanasen.talktranslator.views.TranslationPanel;

public class ConversationFragment extends Fragment implements RecognitionListener {

    private final String LOG_TAG = ConversationFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private Translator mTranslator;

    private SpeechRecognizer mSpeechRecognizer;
    private TextToSpeech mTextToSpeech;
    private View mRootView;
    private TranslationPanel mTranslationPanel;
    private RecyclerView mConversationView;
    private TextView mEmptyConversationView;

    private ChatAdapter mConversationAdapter;
    private Set<Locale> mLocales;
    private boolean mRecogntionSuccess;

    public ConversationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        mTranslator = new Translator(getContext());
        setupSpeechRecognizer();

        mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mLocales = mTextToSpeech.getAvailableLanguages();
                }

                mConversationAdapter.setTextToSpeech(mTextToSpeech);
            }
        });

        mEmptyConversationView = (TextView) mRootView.findViewById(R.id.empty_conversation_textview);
        mConversationView = (RecyclerView) mRootView.findViewById(R.id.conversation_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);

        mConversationView.setLayoutManager(layoutManager);

        List<Translation> translations = Translation.listAll(Translation.class);
        mConversationAdapter = new ChatAdapter(getContext(), translations, mTextToSpeech);
        mConversationView.setAdapter(mConversationAdapter);
        mConversationView.scrollToPosition(mConversationAdapter.getItemCount() - 1);

        if (translations == null || translations.size() == 0) {
            mConversationView.setVisibility(View.GONE);
            mEmptyConversationView.setVisibility(View.VISIBLE);
        }

        mTranslationPanel = new TranslationPanel(getContext(), mRootView, mSpeechRecognizer, mConversationAdapter);
        checkMicrophonePermission();

        return mRootView;
    }

    public void checkMicrophonePermission() {
        int microphonePermission =
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO);
        if (microphonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTranslationPanel.getLeftTranslator().setEnabled(true);
                    mTranslationPanel.getRightTranslator().setEnabled(true);
                } else {
                    mTranslationPanel.getLeftTranslator().setEnabled(false);
                    mTranslationPanel.getRightTranslator().setEnabled(false);
                }
        }
    }


    private void setupSpeechRecognizer() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
        }

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        mSpeechRecognizer.setRecognitionListener(this);
        if (mTranslationPanel != null) {
            mTranslationPanel.setSpeechRecognizer(mSpeechRecognizer);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    private void translate(final String text) {
        String leftLanguageCode = Utility.getCodeFromLanguage(getContext(),
                Utility.getTranslatorLanguage(getContext(), Utility.LEFT_TRANSLATOR_LANGUAGE));
        String rightLanguageCode = Utility.getCodeFromLanguage(getContext(),
                Utility.getTranslatorLanguage(getContext(), Utility.RIGHT_TRANSLATOR_LANGUAGE));

        Callback<TranslationResult> callback = new Callback<TranslationResult>() {
            @Override
            public void onResponse(Call<TranslationResult> call, Response<TranslationResult> response) {
                final TranslationResult translation = response.body();

                final String lang = translation.getLang();

                String translatedText = translation.getText()[0];

                Translation chatTranslation = mTranslationPanel.hasJustUsedLeftTranslator() ?
                        new Translation(translatedText, text, true, lang) :
                        new Translation(translatedText, text, false, lang);

                if (mConversationView.getVisibility() == View.GONE) {
                    mConversationView.setVisibility(View.VISIBLE);
                    mEmptyConversationView.setVisibility(View.GONE);
                }

                mConversationAdapter.addTranslation(chatTranslation);

                chatTranslation.save();

                if (mConversationAdapter.getItemCount() > 0) {
                    mConversationView.scrollToPosition(mConversationAdapter.getItemCount() - 1);
                }

                speakText(translatedText, lang);

            }

            @Override
            public void onFailure(Call<TranslationResult> call, Throwable t) {
                Log.e(LOG_TAG, "Something went wrong.");
            }
        };

        if (mTranslationPanel.hasJustUsedLeftTranslator()) {
            mTranslator.translate(text, leftLanguageCode + "-" + rightLanguageCode, callback);
        } else {
            mTranslator.translate(text, rightLanguageCode + "-" + leftLanguageCode, callback);
        }
    }

    private void speakText(String text, String language) {
        if (mTextToSpeech == null || mTextToSpeech.isSpeaking() || mLocales == null) {
            return;
        }

        String langCode = Utility.getTranslatedLanguage(language);

        if (langCode.equals(getString(R.string.lang_code_bg))) {
            langCode = getString(R.string.lang_code_ru);
            text = Utility.editBulgarianTextForRussianReading(text);
        }

        Locale locale = Utility.getLocaleFromLangCode(langCode, mLocales);
        if (locale == null) {
            Log.d(LOG_TAG, "Language not supported by TextToSpeech.");
            return;
        }

        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        mTextToSpeech.setLanguage(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(LOG_TAG, "onReadyforSpeach");
    }

    @Override
    public void onBeginningOfSpeech() {
        mTranslationPanel.setAnimationOn(true);
        Log.d(LOG_TAG, "onBeginningOfSpeach");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        mTranslationPanel.onRmsChanged(rmsdB);
        Log.d(LOG_TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(LOG_TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        mTranslationPanel.setAnimationOn(false);
        Log.d(LOG_TAG, "onEndOfSpeach");
    }

    @Override
    public void onError(int error) {
        if (mRecogntionSuccess) {
            return;
        }

        String errorMessage = getErrorText(error);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        mTranslationPanel.setAnimationOn(false);

    }

    @Override
    public void onResults(Bundle results) {
        mRecogntionSuccess = true;

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = null;
        if (matches != null) {
            text = matches.get(0);
        }
        Log.d(LOG_TAG, text);

        refreshTranslationAnimation(); //Because there is a bug in SpeechRecognizer
        translate(text);
    }

    private void refreshTranslationAnimation() {
        AudioManager manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        mRecogntionSuccess = false;
        mSpeechRecognizer.startListening(new Intent());
        mSpeechRecognizer.cancel();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(LOG_TAG, "onEvent");
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = getString(R.string.error_audio);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = getString(R.string.error_something_wrong);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = getString(R.string.error_permissions);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = getString(R.string.error_network);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = getString(R.string.error_server);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = getString(R.string.error_no_match);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = getString(R.string.error_recognition_service);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = getString(R.string.error_server);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = getString(R.string.error_no_speech);
                break;
            default:
                message = getString(R.string.error_no_match);
        }
        return message;
    }

}
