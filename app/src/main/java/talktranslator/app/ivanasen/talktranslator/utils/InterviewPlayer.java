package talktranslator.app.ivanasen.talktranslator.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import talktranslator.app.ivanasen.talktranslator.models.Interview;

import static talktranslator.app.ivanasen.talktranslator.activities.MainActivity.UTTERANCE_ID;

/**
 * Created by ivan on 3/5/2017.
 */

public class InterviewPlayer {
    private static final long SILENCE_DURATION = 10;
    private TextToSpeech mTextToSpeech;
    private Context mContext;

    public InterviewPlayer(TextToSpeech textToSpeech, Context context) {
        mTextToSpeech = textToSpeech;
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playInterview(Interview interview,
                              @Nullable UtteranceProgressListener onUterrenceListener) {
        String interviewText = interview.getText();
        Locale language = getLocale(interview);
        mTextToSpeech.setLanguage(language);

        Iterator<Voice> voicesIterator = mTextToSpeech.getVoices().iterator();
        List<Voice> voicesForLanguage = getVoicesForLanguage(voicesIterator, language);
        Voice voice1 = voicesForLanguage.get(0);
        Voice voice2 = null;
        if (voicesForLanguage.size() > 1) {
            voice2 = voicesForLanguage.get(1);
        }

        mTextToSpeech.setOnUtteranceProgressListener(onUterrenceListener);

        boolean voice1ShouldSpeak = true;
        String[] parts = interviewText.split(InterviewMaker.INTERVIEW_TEXT_TO_SPEECH_PAUSE);
        for (String part : parts) {
            if (voice2 != null) {
                mTextToSpeech.setVoice(voice1ShouldSpeak ? voice1 : voice2);
                voice1ShouldSpeak = !voice1ShouldSpeak;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
                mTextToSpeech.speak(part, TextToSpeech.QUEUE_ADD, params, UTTERANCE_ID);
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
                mTextToSpeech.speak(part, TextToSpeech.QUEUE_ADD, map);
            }

            mTextToSpeech.playSilentUtterance(SILENCE_DURATION, TextToSpeech.QUEUE_ADD, null);
        }
    }

    private List<Voice> getVoicesForLanguage(Iterator<Voice> voicesIterator, Locale language) {
        List<Voice> voices = new ArrayList<>();
        while (voicesIterator.hasNext()) {
            Voice voice = voicesIterator.next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (voice.getLocale().equals(language)) {
                    voices.add(voice);
                }
            }
        }

        return voices;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Locale getLocale(Interview interview) {
        String language = interview.getLanguage();
        String langCode = Utility.getCodeFromLanguage(mContext, language, true);
        return Utility.getLocaleFromLangCode(langCode, mTextToSpeech.getAvailableLanguages());
    }
}
