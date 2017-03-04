package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.adapters.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
import talktranslator.app.ivanasen.talktranslator.utils.ITextToSpeechUser;
import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.translation.TranslationResult;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;
import talktranslator.app.ivanasen.talktranslator.views.TranslationPanel;

public class ConversationFragment extends Fragment implements RecognitionListener, ITextToSpeechUser {

    private static final String LOG_TAG = ConversationFragment.class.getSimpleName();
    protected static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    protected Translator mTranslator;
    protected SpeechRecognizer mSpeechRecognizer;
    protected View mRootView;
    protected TranslationPanel mTranslationPanel;
    protected RecyclerView mChatView;
    protected View mEmptyConversationView;

    protected ChatAdapter mChatAdapter;
    protected boolean mRecogntionSuccess;
    protected boolean mPermissionGranted;

    public ConversationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        mTranslator = new Translator(getContext());
        setupSpeechRecognizer();
        setupChat();

        mTranslationPanel = new TranslationPanel(getContext(), mRootView, mSpeechRecognizer, mChatAdapter);

        checkMicrophonePermission();

        Handler waitForTextToSpeechToInitHandler = new Handler();
        waitForTextToSpeechToInitHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.notifyDataSetChanged();
            }
        }, getResources().getInteger(R.integer.wait_for_text_to_speech_to_init_millis));

        return mRootView;
    }

    protected void setupChat() {
        mEmptyConversationView = mRootView.findViewById(R.id.empty_conversation_view);
        mChatView = (RecyclerView) mRootView.findViewById(R.id.conversation_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        mChatView.setLayoutManager(layoutManager);

        List<ChatTranslation> chatTranslations = ChatTranslation.listAll(ChatTranslation.class);
        mChatAdapter = new ChatAdapter(getContext(), chatTranslations, (MainActivity) getActivity(), false);
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
        if (chatTranslations == null || chatTranslations.size() == 0) {
            mChatView.setVisibility(View.GONE);
            mEmptyConversationView.setVisibility(View.VISIBLE);
        }

//        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
//                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//
//                    @Override
//                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                        return false;
//                    }
//
//                    @Override
//                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
//                        mChatAdapter.removeTranslation(viewHolder.getAdapterPosition());
//
//                        if (mChatAdapter.getItemCount() == 0) {
//                            mChatView.setVisibility(View.GONE);
//                            mEmptyConversationView.setVisibility(View.VISIBLE);
//                        }
//                    }
//                };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
//        itemTouchHelper.attachToRecyclerView(mChatView);
    }

    public void checkMicrophonePermission() {
        int microphonePermission =
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO);
        if (microphonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        } else {
            mPermissionGranted = true;
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

    protected void setupSpeechRecognizer() {
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
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    private void translate(final String text) {
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

        String errorMessage = Utility.getSpeechRecognitionErrorText(getContext(), error);
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


    @Override
    public void setTextToSpeechReadyForUsing(boolean isReady) {
    }
}
