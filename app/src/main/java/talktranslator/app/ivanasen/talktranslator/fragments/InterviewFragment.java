package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.adapters.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.Interview;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.VoiceRecorder;
import talktranslator.app.ivanasen.talktranslator.views.TranslationPanel;

public class InterviewFragment extends ConversationFragment {
    private static final String LOG_TAG = InterviewFragment.class.getSimpleName();

    private VoiceRecorder mRecorder;
    private FloatingActionButton mRecordBtn;
    private FloatingActionButton mPauseBtn;
    private FloatingActionButton mStopBtn;
    private TextView mInterviewLengthView;

    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_interview, container, false);
        mTranslator = new Translator(getContext());
        setupSpeechRecognizer();
        setupChat();

        mTranslationPanel = new TranslationPanel(getContext(), mRootView, mSpeechRecognizer, mChatAdapter);

        checkMicrophonePermission();
        if (mPermissionGranted) {
            setupMediaControls();
        }

        Handler waitForTextToSpeechToInitHandler = new Handler();
        waitForTextToSpeechToInitHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mChatAdapter.notifyDataSetChanged();
                    }
                },
                getResources().getInteger(R.integer.wait_for_text_to_speech_to_init_millis));

        return mRootView;
    }

    private void setupMediaControls() {
        mRecorder = new VoiceRecorder(getContext());

        mRecordBtn = (FloatingActionButton) mRootView.findViewById(R.id.start_recording_btn);
        mPauseBtn = (FloatingActionButton) mRootView.findViewById(R.id.pause_recording_btn);
        mStopBtn = (FloatingActionButton) mRootView.findViewById(R.id.stop_recording_btn);
        mInterviewLengthView = (TextView) mRootView.findViewById(R.id.record_length_textview);

        final Runnable updateTimerThread = new Runnable() {
            @Override
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

                updatedTime = timeSwapBuff + timeInMilliseconds;

                int secs = (int) (updatedTime / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                if (mInterviewLengthView != null)
                    mInterviewLengthView.setText("" + String.format("%02d", mins) + ":"
                            + String.format("%02d", secs));
                customHandler.postDelayed(this, 0);
            }
        };

        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecordBtn.setVisibility(View.GONE);
                mStopBtn.setVisibility(View.VISIBLE);
                mRecorder.startRecording(null);
                startHTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });


        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopBtn.setVisibility(View.GONE);
                mRecordBtn.setVisibility(View.VISIBLE);
                mRecorder.stopRecording();
                Interview interview =
                        new Interview(mRecorder.getFileName(), (String) mInterviewLengthView.getText());
                interview.save();
                Toast.makeText(InterviewFragment.this.getContext(), "Interview saved", Toast.LENGTH_LONG).show();

                timeSwapBuff = 0;
                mInterviewLengthView.setText(R.string.interview_length_start);
                customHandler.removeCallbacks(updateTimerThread);
            }
        });
    }

    @Override
    protected void setupChat() {
        mEmptyConversationView = mRootView.findViewById(R.id.empty_conversation_view);
        mChatView = (RecyclerView) mRootView.findViewById(R.id.conversation_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        mChatView.setLayoutManager(layoutManager);

        mChatAdapter = new ChatAdapter(getContext(), null, (MainActivity) getActivity(), true);
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
}
