package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
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
    private ChatAdapter mConversationAdapter;
    private Set<Locale> mLocales;

    public ConversationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        mTranslator = new Translator(getContext());
        setupSpeechRecognizer();
        mTranslationPanel = new TranslationPanel(getContext(), mRootView, mSpeechRecognizer);

        mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mLocales = mTextToSpeech.getAvailableLanguages();
                }

                //TODO: add translations from database
                mConversationAdapter = new ChatAdapter(getContext(), null, mTextToSpeech);
                mConversationView.setAdapter(mConversationAdapter);
            }
        });

        mConversationView = (RecyclerView) mRootView.findViewById(R.id.conversation_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);

        mConversationView.setLayoutManager(layoutManager);

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
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        mSpeechRecognizer.setRecognitionListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = null;
        if (matches != null) {
            text = matches.get(0);
        }
        Log.d(LOG_TAG, text);
        translate(text);
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

                ChatTranslation chatTranslation = mTranslationPanel.hasJustUsedLeftTranslator() ?
                        new ChatTranslation(translatedText, text, true, lang) :
                        new ChatTranslation(translatedText, text, false, lang);
                mConversationAdapter.addTranslation(chatTranslation);

                mConversationView.scrollTo(0, 0);

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

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void speakText(String text, String language) {
        if (mTextToSpeech == null || mTextToSpeech.isSpeaking() || mLocales == null) {
            return;
        }

        String langCode = Utility.getTranslatedLanguage(language);
        Locale locale = Utility.getLocaleFromLangCode(langCode, mLocales);
        if (locale == null) {
            Log.d(LOG_TAG, "Language not supported by TextToSpeech.");
            return;
        }

        mTextToSpeech.setLanguage(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}
