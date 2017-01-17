package talktranslator.app.ivanasen.talktranslator;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

public class MainActivityFragment extends Fragment implements RecognitionListener {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private View mRootView;
    private Button mLeftTranslator;
    private Button mRightTranslator;
    private Translator mTranslator;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mRecognizerIntent;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        setUpSpeechRecognizer();

        mTranslator = new Translator(getContext());

        mLeftTranslator = (Button) mRootView.findViewById(R.id.left_translator);
        mRightTranslator = (Button) mRootView.findViewById(R.id.right_translator);

        final String leftTranslatorLang = Utility.getTranslatorLanguage
                (getContext(), Utility.LEFT_TRANSLATOR_LANGUAGE);
        final String rightTranslatorLang = Utility.getTranslatorLanguage
                (getContext(), Utility.RIGHT_TRANSLATOR_LANGUAGE);
        mLeftTranslator.setText(leftTranslatorLang);
        mRightTranslator.setText(rightTranslatorLang);

        mLeftTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = Utility.getCodeFromLanguage(getContext(), leftTranslatorLang);
                promptSpeechInput(code);
            }
        });

        mRightTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = Utility.getCodeFromLanguage(getContext(), rightTranslatorLang);
                promptSpeechInput(code);
            }
        });

        return mRootView;
    }

    private void setUpSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        mSpeechRecognizer.setRecognitionListener(this);
        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getActivity().getPackageName());

    }

    private void promptSpeechInput(String langCode) {
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                langCode);
        mSpeechRecognizer.startListening(mRecognizerIntent);
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
        String text = "";
        if (matches != null)
            for (String result : matches)
                text += result + "\n";

        Log.d(LOG_TAG, text);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

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
