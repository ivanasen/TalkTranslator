package talktranslator.app.ivanasen.talktranslator.views;

import android.content.Context;
import android.content.Intent;
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

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.fragments.ConversationFragment;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/9/2017.
 */

public class TranslationPanel {

    private Context mContext;
    private View mRootView;
    private Button mLeftTranslator;
    private Button mRightTranslator;
    private SpeechRecognizer mSpeechRecognizer;

    private boolean mJustUsedLeftTranslator;
    private ListView mLeftLanguagesListView;
    private ListView mRightLanguagesListView;
    private ToggleButton mLeftLanguageSelectBtn;
    private ToggleButton mRightLanguageSelectBtn;

    public TranslationPanel(Context context, View rootView, SpeechRecognizer speechRecognizer) {
        mContext = context;
        mRootView = rootView;
        mSpeechRecognizer = speechRecognizer;

        setupTranslators();
    }

    private void promptSpeechInput(String langCode) {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getPackageName());
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                langCode);
        mSpeechRecognizer.startListening(speechIntent);
    }

    private void setupTranslators() {
        mLeftTranslator = (Button) mRootView.findViewById(R.id.left_translator);
        mRightTranslator = (Button) mRootView.findViewById(R.id.right_translator);

        final String leftTranslatorLang = Utility.getTranslatorLanguage
                (mContext, Utility.LEFT_TRANSLATOR_LANGUAGE);
        final String rightTranslatorLang = Utility.getTranslatorLanguage
                (mContext, Utility.RIGHT_TRANSLATOR_LANGUAGE);

        mLeftTranslator.setText(leftTranslatorLang);
        mRightTranslator.setText(rightTranslatorLang);

        mLeftTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJustUsedLeftTranslator = true;
                String lang = Utility.getTranslatorLanguage(mContext, Utility.LEFT_TRANSLATOR_LANGUAGE);
                String code = Utility.getCodeFromLanguage(mContext, lang);
                promptSpeechInput(code);
            }
        });

        mRightTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJustUsedLeftTranslator = false;
                String lang = Utility.getTranslatorLanguage(mContext, Utility.RIGHT_TRANSLATOR_LANGUAGE);
                String code = Utility.getCodeFromLanguage(mContext, lang);
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
        TransitionManager.beginDelayedTransition((ViewGroup) mRootView);

        LinearLayout leftTranslatorContainer =
                (LinearLayout) mRootView.findViewById(R.id.left_translator_container);
        RelativeLayout.LayoutParams containerParams =
                (RelativeLayout.LayoutParams) leftTranslatorContainer.getLayoutParams();
        LinearLayout.LayoutParams listParams =
                (LinearLayout.LayoutParams) mLeftLanguagesListView.getLayoutParams();

        if (listParams.height == 0) {
            leftTranslatorContainer.setTranslationZ(mContext.getResources().getDimension(R.dimen.translation_bar_z));
            containerParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            listParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        } else {
            leftTranslatorContainer.setTranslationZ(0);
            listParams.height = 0;
            containerParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }

        leftTranslatorContainer.setLayoutParams(containerParams);
        mLeftLanguagesListView.setLayoutParams(listParams);
    }

    private void collapseOrExpandRightTranslator() {
        TransitionManager.beginDelayedTransition((ViewGroup) mRootView);

        LinearLayout rightTranslatorContainer =
                (LinearLayout) mRootView.findViewById(R.id.right_translator_container);
        RelativeLayout.LayoutParams containerParams =
                (RelativeLayout.LayoutParams) rightTranslatorContainer.getLayoutParams();
        LinearLayout.LayoutParams listParams =
                (LinearLayout.LayoutParams) mRightLanguagesListView.getLayoutParams();

        if (listParams.height == 0) {
            rightTranslatorContainer.setTranslationZ(mContext.getResources().getDimension(R.dimen.translation_bar_z));
            containerParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            listParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        } else {
            rightTranslatorContainer.setTranslationZ(0);
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
}
