package talktranslator.app.ivanasen.talktranslator.views;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import talktranslator.app.ivanasen.talktranslator.adapters.ChatAdapter;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/9/2017.
 */

public class TranslationPanel {

    private Context mContext;
    private View mRootView;
    private PulsatingButton mLeftTranslator;
    private PulsatingButton mRightTranslator;
    private SpeechRecognizer mSpeechRecognizer;
    private ChatAdapter mChatAdapter;

    private boolean mJustUsedLeftTranslator;
    private ListView mLeftLanguagesListView;
    private ListView mRightLanguagesListView;
    private ToggleButton mLeftLanguageSelectBtn;
    private ToggleButton mRightLanguageSelectBtn;
    private boolean mIsSpeechRecognitionOn;

    public TranslationPanel(Context context, View rootView, SpeechRecognizer speechRecognizer, ChatAdapter adapter) {
        mContext = context;
        mRootView = rootView;
        mSpeechRecognizer = speechRecognizer;
        mChatAdapter = adapter;
        setupTranslators();
    }

    private void promptSpeechInput(String langCode) {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getPackageName());
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                langCode);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        mIsSpeechRecognitionOn = true;
        mSpeechRecognizer.startListening(speechIntent);
    }

    private void setupTranslators() {
        mLeftTranslator = (PulsatingButton) mRootView.findViewById(R.id.left_translator);
        mRightTranslator = (PulsatingButton) mRootView.findViewById(R.id.right_translator);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mLeftTranslator.setPulseColor(mContext.getColor(R.color.leftTranslatorColor));
            mRightTranslator.setPulseColor(mContext.getColor(R.color.rightTranslatorColor));
        } else {
            mLeftTranslator.setPulseColor(
                    mContext.getResources().getColor(R.color.leftTranslatorColor));
            mRightTranslator.setPulseColor(
                    mContext.getResources().getColor(R.color.rightTranslatorColor));
        }

        final String leftTranslatorLang = Utility.getTranslatorLanguage
                (mContext, Utility.LEFT_TRANSLATOR_LANGUAGE);
        final String rightTranslatorLang = Utility.getTranslatorLanguage
                (mContext, Utility.RIGHT_TRANSLATOR_LANGUAGE);

        mLeftTranslator.setText(leftTranslatorLang);
        mRightTranslator.setText(rightTranslatorLang);

        mLeftTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSpeechRecognitionOn) {
                    mSpeechRecognizer.cancel();
                    mLeftTranslator.setAnimationOn(false);
                    mIsSpeechRecognitionOn = false;

                    return;
                }

                mJustUsedLeftTranslator = true;
                String lang = Utility.getTranslatorLanguage(mContext, Utility.LEFT_TRANSLATOR_LANGUAGE);
                String code = Utility.getCodeFromLanguage(mContext, lang, false);
                promptSpeechInput(code);
            }
        });

        mRightTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSpeechRecognitionOn) {
                    mSpeechRecognizer.cancel();
                    mRightTranslator.setAnimationOn(false);
                    mIsSpeechRecognitionOn = false;
                    return;
                }

                mJustUsedLeftTranslator = false;
                String lang = Utility.getTranslatorLanguage(mContext, Utility.RIGHT_TRANSLATOR_LANGUAGE);
                String code = Utility.getCodeFromLanguage(mContext, lang, false);
                promptSpeechInput(code);
            }
        });

        setUpLanguageSelection();
    }

    private void setUpLanguageSelection() {
        String[] languages = mContext.getResources().getStringArray(R.array.languages);
        ArrayAdapter languagesAdapter = new ArrayAdapter<>
                (mContext, R.layout.list_item_language, languages);

        mLeftLanguagesListView = (ListView) mRootView.findViewById(R.id.left_translator_langs_listview);
        mRightLanguagesListView = (ListView) mRootView.findViewById(R.id.right_translator_langs_listview);
        mLeftLanguagesListView.setAdapter(languagesAdapter);
        mRightLanguagesListView.setAdapter(languagesAdapter);

        mLeftLanguagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String language = (String) ((TextView) view).getText();
                Utility.setTranslatorLanguage(mContext, Utility.LEFT_TRANSLATOR_LANGUAGE, language);
                mLeftTranslator.setText(language);
                mLeftLanguageSelectBtn.setChecked(false);

                mChatAdapter.changeLanguages(language, (String) mRightTranslator.getText());

                collapseOrExpandLeftTranslator();
            }
        });

        mRightLanguagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String language = (String) ((TextView) view).getText();
                Utility.setTranslatorLanguage(mContext, Utility.RIGHT_TRANSLATOR_LANGUAGE, language);
                mRightTranslator.setText(language);
                mRightLanguageSelectBtn.setChecked(false);

                mChatAdapter.changeLanguages((String) mLeftTranslator.getText(), language);

                collapseOrExpandRightTranslator();
            }
        });

        mLeftLanguageSelectBtn = (ToggleButton)
                mRootView.findViewById(R.id.left_translator_language_select_btn);
        mLeftLanguageSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseOrExpandLeftTranslator();
            }
        });

        mRightLanguageSelectBtn = (ToggleButton)
                mRootView.findViewById(R.id.right_translator_language_select_btn);
        mRightLanguageSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseOrExpandRightTranslator();
            }
        });
    }

    private void collapseOrExpandLeftTranslator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition((ViewGroup) mRootView);
        }

        LinearLayout leftTranslatorContainer =
                (LinearLayout) mRootView.findViewById(R.id.left_translator_container);
        RelativeLayout.LayoutParams containerParams =
                (RelativeLayout.LayoutParams) leftTranslatorContainer.getLayoutParams();
        LinearLayout.LayoutParams listParams =
                (LinearLayout.LayoutParams) mLeftLanguagesListView.getLayoutParams();

        if (listParams.height == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                leftTranslatorContainer.setTranslationZ(mContext.getResources().getDimension(R.dimen.translation_bar_z));
            }
            containerParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            listParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                leftTranslatorContainer.setTranslationZ(0);
            }
            listParams.height = 0;
            containerParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }

        leftTranslatorContainer.setLayoutParams(containerParams);
        mLeftLanguagesListView.setLayoutParams(listParams);
    }

    private void collapseOrExpandRightTranslator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition((ViewGroup) mRootView);
        }

        LinearLayout rightTranslatorContainer =
                (LinearLayout) mRootView.findViewById(R.id.right_translator_container);
        RelativeLayout.LayoutParams containerParams =
                (RelativeLayout.LayoutParams) rightTranslatorContainer.getLayoutParams();
        LinearLayout.LayoutParams listParams =
                (LinearLayout.LayoutParams) mRightLanguagesListView.getLayoutParams();

        if (listParams.height == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rightTranslatorContainer.setTranslationZ(mContext.getResources().getDimension(R.dimen.translation_bar_z));
            }
            containerParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            listParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rightTranslatorContainer.setTranslationZ(0);
            }
            listParams.height = 0;
            containerParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }

        rightTranslatorContainer.setLayoutParams(containerParams);
        mRightLanguagesListView.setLayoutParams(listParams);
    }

    public Button getLeftTranslator() {
        return mLeftTranslator;
    }

    public Button getRightTranslator() {
        return mRightTranslator;
    }

    public boolean hasJustUsedLeftTranslator() {
        return mJustUsedLeftTranslator;
    }

    public void onRmsChanged(float rmsdB) {
        mLeftTranslator.onRmsChanged(rmsdB);
        mRightTranslator.onRmsChanged(rmsdB);
    }

    public void setAnimationOn(boolean isAnimationOn) {
        if (!isAnimationOn) {
            mIsSpeechRecognitionOn = false;
        }

        if (mJustUsedLeftTranslator) {
            mLeftTranslator.setAnimationOn(isAnimationOn);
        } else {
            mRightTranslator.setAnimationOn(isAnimationOn);
        }
    }

    public void setSpeechRecognizer(SpeechRecognizer mSpeechRecognizer) {
        this.mSpeechRecognizer = mSpeechRecognizer;
    }

    public void setEnabled(boolean enabled) {
        mLeftTranslator.setEnabled(enabled);
        mRightTranslator.setEnabled(enabled);
    }
}