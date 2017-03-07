package talktranslator.app.ivanasen.talktranslator.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.SpeechRecognizer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.adapters.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
import talktranslator.app.ivanasen.talktranslator.translation.TranslationResult;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.InterviewMaker;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;
import talktranslator.app.ivanasen.talktranslator.views.TranslationPanel;

public class InterviewFragment extends ConversationFragment {
    private static final String LOG_TAG = InterviewFragment.class.getSimpleName();

    private FloatingActionButton mRecordBtn;
    private FloatingActionButton mPauseBtn;
    private FloatingActionButton mStopBtn;
    private Chronometer mInterviewChronometer;

    private InterviewMaker mInterviewMaker;
    private boolean mIsInterviewing;
    private long mTtimeWhenStopped;

    private View mRecordingView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_interview, container, false);
        mTranslator = new Translator(getContext());
        mInterviewMaker = new InterviewMaker(getActivity());

        setupSpeechRecognizer();
        setupChat();

        mTranslationPanel = new TranslationPanel(
                getContext(), mRootView, mSpeechRecognizer, mChatAdapter, true);
        InterviewerLanguageChangedCallback mLanguageCallback = new InterviewerLanguageChangedCallback() {
            @Override
            public void onLanguageChanged() {
                onInterviewChronometerStart(false, true);
                long seconds = Utility.getSecondsFromChronometer(
                        (String) mInterviewChronometer.getText());
                if (mIsInterviewing && seconds > 0) {
                    long elapsedSeconds =
                            (SystemClock.elapsedRealtime() - mInterviewChronometer.getBase()) / 1000;
                    mInterviewMaker.saveInterview(elapsedSeconds);
                }
            }
        };
        mTranslationPanel.setInterviewerCallback(mLanguageCallback);

        checkMicrophonePermission();

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            setupMediaControls();
        }

        return mRootView;
    }

    @Override
    protected void translate(final String text) {
        String leftLanguageCode = Utility.getCodeFromLanguage(getContext(),
                Utility.getTranslatorLanguage(getContext(), Utility.LEFT_TRANSLATOR_LANGUAGE),
                false);
        String rightLanguageCode = Utility.getCodeFromLanguage(getContext(),
                Utility.getTranslatorLanguage(getContext(), Utility.RIGHT_TRANSLATOR_LANGUAGE),
                false);

        Callback<TranslationResult> callback = new Callback<TranslationResult>() {
            @Override
            public void onResponse(Call<TranslationResult> call, Response<TranslationResult> response) {
                final TranslationResult translation = response.body();
                final String lang = translation.getLang();
                String translatedText = translation.getText()[0];

                ChatTranslation chatTranslation = mTranslationPanel.hasJustUsedLeftTranslator() ?
                        new ChatTranslation(translatedText, text, true, lang) :
                        new ChatTranslation(translatedText, text, false, lang);

                if (mChatView.getVisibility() == View.GONE) {
                    mChatView.setVisibility(View.VISIBLE);
                    mEmptyConversationView.setVisibility(View.GONE);
                }

                mChatAdapter.addTranslation(chatTranslation);

                if (!mTranslationPanel.hasJustUsedLeftTranslator()) {
                    mInterviewMaker.addIntervieweeText(chatTranslation.getTranslatedText());
                    Log.d(LOG_TAG, mInterviewMaker.getCurrentInterviewText());
                }

                if (mChatAdapter.getItemCount() > 0) {
                    mChatView.scrollToPosition(mChatAdapter.getItemCount() - 1);
                }
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

    private void setupMediaControls() {
        mRecordingView = mRootView.findViewById(R.id.recording_view);
        mRecordBtn = (FloatingActionButton) mRootView.findViewById(R.id.start_recording_btn);
        mPauseBtn = (FloatingActionButton) mRootView.findViewById(R.id.pause_recording_btn);
        mStopBtn = (FloatingActionButton) mRootView.findViewById(R.id.stop_recording_btn);

        final ImageView recordDotImageView = (ImageView) mRecordingView.findViewById(R.id.record_dot);
        recordDotImageView.setBackgroundResource(R.drawable.record_animation);
        final AnimationDrawable recordingAnimation = (AnimationDrawable) recordDotImageView.getBackground();

        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordingAnimation.start();
                mRecordingView.setVisibility(View.VISIBLE);
                mIsInterviewing = true;
                mRecordBtn.setVisibility(View.GONE);
                mPauseBtn.setVisibility(View.VISIBLE);
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsInterviewing = false;
                mRecordingView.setVisibility(View.GONE);
                mPauseBtn.setBackgroundColor(getResources().getColor(R.color.materialRed));
                mPauseBtn.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.VISIBLE);
                if (mInterviewChronometer != null) {
                    long elapsedSeconds =
                            Utility.getSecondsFromChronometer((String) mInterviewChronometer.getText());
                    if (elapsedSeconds > 0) {
                        mInterviewMaker.saveInterview(elapsedSeconds);
                    }
                    onInterviewChronometerStart(false, true);
                }

            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordingAnimation.stop();
                mPauseBtn.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.VISIBLE);
                onInterviewChronometerStart(false, false);
            }
        });
    }

    private void onInterviewChronometerStart(boolean shouldStart, boolean shouldReset) {
        if (mInterviewChronometer == null) {
            mInterviewChronometer = (Chronometer)
                    mRootView.findViewById(R.id.interview_length_chronometer);
            shouldReset = true;
        }

        if (shouldStart) {
            if (shouldReset) {
                mInterviewChronometer.setBase(SystemClock.elapsedRealtime());
                mTtimeWhenStopped = 0;
            } else {
                mInterviewChronometer.setBase(SystemClock.elapsedRealtime() + mTtimeWhenStopped);
            }

            mInterviewChronometer.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mInterviewChronometer.setTextColor(getResources()
                        .getColor(R.color.materialBlue, null));
            } else {
                mInterviewChronometer.setTextColor(getResources()
                        .getColor(R.color.materialBlue));
            }
        } else {
            if (mInterviewChronometer != null) {
                mTtimeWhenStopped = mInterviewChronometer.getBase() - SystemClock.elapsedRealtime();
                mInterviewChronometer.stop();
                mInterviewChronometer.setTextColor(Color.DKGRAY);
            }

            if (shouldReset) {
                mInterviewChronometer.setBase(SystemClock.elapsedRealtime());
                mTtimeWhenStopped = 0;
            }
        }
    }

    @Override
    protected void setupChat() {
        mEmptyConversationView = mRootView.findViewById(R.id.empty_conversation_view);
        mChatView = (RecyclerView) mRootView.findViewById(R.id.conversation_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        mChatView.setLayoutManager(layoutManager);

        mChatAdapter = new ChatAdapter(getContext(), (MainActivity) getActivity(), true);
        mChatView.setAdapter(mChatAdapter);
        mChatView.scrollToPosition(mChatAdapter.getItemCount() - 1);
        mChatView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (mChatAdapter.shouldScrollToBottom()) {
                    int position = mChatAdapter.getItemCount() - 1;
                    mChatView.scrollToPosition(position);
                }

                if (mChatAdapter.shouldScroll()) {
                    mChatView.setNestedScrollingEnabled(true);
                } else {
                    mChatView.setNestedScrollingEnabled(false);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
            }
        });

        mChatView.setVisibility(View.GONE);
        mEmptyConversationView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResults(Bundle results) {
        super.onResults(results);
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = null;
        if (matches != null) {
            text = matches.get(0);
            if (mTranslationPanel.hasJustUsedLeftTranslator()) {
                mInterviewMaker.addInterviewerText(text);
                Log.d(LOG_TAG, mInterviewMaker.getCurrentInterviewText());
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        super.onBeginningOfSpeech();

        if (mIsInterviewing) {
            onInterviewChronometerStart(true, false);
        }
    }

    @Override
    public void onEndOfSpeech() {
        super.onEndOfSpeech();

        if (mIsInterviewing) {
            onInterviewChronometerStart(false, false);
        }
    }

    public interface InterviewerLanguageChangedCallback {
        public void onLanguageChanged();
    }
}
