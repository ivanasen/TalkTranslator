package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.adapters.TranslationHistoryAdapter;
import talktranslator.app.ivanasen.talktranslator.models.Translation;
import talktranslator.app.ivanasen.talktranslator.translation.TranslationResult;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.translation.Translator;
import talktranslator.app.ivanasen.talktranslator.utils.ItemClickSupport;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

public class KeyboardTranslateFragment extends Fragment {
    private static final String LOG_TAG = KeyboardTranslateFragment.class.getSimpleName();

    private Translator mTranslator;

    private View mRootView;
    private Spinner mTranslateFromLanguageSpinner;
    private Spinner mTranslateToLanguageSpinner;
    private TextView mTextInputLanguageTextView;
    private TextView mTranslationTextView;
    private TextView mTranslateLanguageTextView;
    private ImageButton mSwapLanguagesBtn;

    private CharSequence mTranslateFromLanguage;
    private CharSequence mTranslateToLanguage;
    private EditText mTextInput;
    private ImageButton mClearBtn;
    private RecyclerView mHistoryRecyclerView;
    private TranslationHistoryAdapter mAdapter;

    private ImageButton mSpeakInputTextBtn;
    private ProgressBar mInputProgressBar;
    private ImageButton mSpeakTranslationBtn;
    private ProgressBar mTranslationProgressBar;

    private Set<Locale> mLocales;
    private boolean isTextToSpeechInit;

    public KeyboardTranslateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTranslator = new Translator(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_keyboard_translate, container, false);

        setupLanguageSelection();
        setupTextInput();
        setupHeadTranslationView();
        setupTranslationHistory();

        Handler waitForTextToSpeechToInitHandler = new Handler();
        waitForTextToSpeechToInitHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocales = ((MainActivity) KeyboardTranslateFragment.this.getActivity()).getLocales();
                if (mLocales == null) {
                    return;
                }

                isTextToSpeechInit = true;
                String fromLangCode = Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(),
                        Utility.getTranslatorLanguage(KeyboardTranslateFragment.this.getContext(), Utility.LEFT_TRANSLATOR_LANGUAGE),
                        true);
                String toLangCode = Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(),
                        Utility.getTranslatorLanguage(KeyboardTranslateFragment.this.getContext(), Utility.RIGHT_TRANSLATOR_LANGUAGE),
                        true);
                Locale fromLocale =
                        Utility.getLocaleFromLangCode(fromLangCode, mLocales);
                Locale toLocale =
                        Utility.getLocaleFromLangCode(toLangCode, mLocales);
                if (fromLocale == null) {
                    mSpeakInputTextBtn.setVisibility(View.GONE);
                } else {
                    mSpeakInputTextBtn.setVisibility(View.VISIBLE);
                }
                if (toLocale == null) {
                    mSpeakTranslationBtn.setVisibility(View.GONE);
                } else {
                    mSpeakTranslationBtn.setVisibility(View.VISIBLE);
                }
            }
        }, getResources().getInteger(R.integer.wait_for_text_to_speech_to_init_millis));


        return mRootView;
    }

    private void setupHeadTranslationView() {
        mTranslationTextView = (TextView) mRootView.findViewById(R.id.translated_text);
        mTranslateLanguageTextView = (TextView) mRootView.findViewById(R.id.translation_language);
        mTranslateLanguageTextView.setText(mTranslateToLanguage);

        ImageButton copyTranslationBtn =
                (ImageButton) mRootView.findViewById(R.id.copy_translation_btn);
        copyTranslationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.copyTextToClipboard(KeyboardTranslateFragment.this.getContext(), mTranslationTextView.getText());
            }
        });

        final UtteranceProgressListener listener = new UtteranceProgressListener() {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    toggleTranslationPlayBtnVisibility();
                }
            };

            @Override
            public void onStart(String utteranceId) {
                getActivity().runOnUiThread(r);
            }

            @Override
            public void onDone(String utteranceId) {
            }

            @Override
            public void onError(String utteranceId) {
            }
        };

        mTranslationProgressBar = (ProgressBar) mRootView.findViewById(R.id.translation_progressbar);
        mSpeakTranslationBtn =
                (ImageButton) mRootView.findViewById(R.id.speak_translation_btn);
        mSpeakTranslationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTranslationPlayBtnVisibility();

                String fromLangCode =
                        Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(), (String) mTranslateFromLanguage,
                                true);
                String toLangCode =
                        Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(), (String) mTranslateToLanguage,
                                true);
                String fromLangToLang = fromLangCode + "-" + toLangCode;
                ((MainActivity) KeyboardTranslateFragment.this.getActivity()).speakText(
                        (String) mTranslationTextView.getText(), fromLangToLang, listener);
            }
        });
    }

    private void setupTranslationHistory() {
        mHistoryRecyclerView =
                (RecyclerView) mRootView.findViewById(R.id.translation_history_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mHistoryRecyclerView.setLayoutManager(layoutManager);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);

        List<Translation> translations = Translation.listAll(Translation.class);
        if (translations == null || translations.size() == 0) {
            ((View) mHistoryRecyclerView.getParent()).setVisibility(View.GONE);
        }

        mAdapter = new TranslationHistoryAdapter(getContext(), translations);
        mHistoryRecyclerView.setAdapter(mAdapter);

        ItemClickSupport.addTo(mHistoryRecyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        TextView translationTextView =
                                (TextView) v.findViewById(R.id.translation_textview);
                        TextView originalTextView =
                                (TextView) v.findViewById(R.id.original_text_textview);
                        TextView languagesTextView =
                                (TextView) v.findViewById(R.id.languages_textview);
                        String fromLanguageCode =
                                Utility.getTranslateFromLanguage((String) languagesTextView.getText());
                        String toLanguageCode =
                                Utility.getTranslatedLanguage((String) languagesTextView.getText());
                        String fromLanguage =
                                Utility.getLanguageFromCode(KeyboardTranslateFragment.this.getContext(), fromLanguageCode);
                        String toLanguage =
                                Utility.getLanguageFromCode(KeyboardTranslateFragment.this.getContext(), toLanguageCode);

                        mTranslateFromLanguageSpinner.setSelection(
                                Arrays.asList(KeyboardTranslateFragment.this.getResources().getStringArray(R.array.languages))
                                        .indexOf(fromLanguage)
                        );
                        mTranslateToLanguageSpinner.setSelection(
                                Arrays.asList(KeyboardTranslateFragment.this.getResources().getStringArray(R.array.languages))
                                        .indexOf(toLanguage)
                        );

                        mTranslationTextView.setText(translationTextView.getText());
                        mTextInput.setText(originalTextView.getText());
                    }
                });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        mAdapter.removeTranslation(viewHolder.getAdapterPosition());
                        if (mAdapter.getItemCount() == 0) {
                            ((View) mHistoryRecyclerView.getParent()).setVisibility(View.GONE);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mHistoryRecyclerView);
    }

    private void setupTextInput() {
        mTextInputLanguageTextView = (TextView) mRootView.findViewById(R.id.text_input_language);
        String leftLanguage = (String) mTranslateFromLanguageSpinner.getSelectedItem();
        mTextInputLanguageTextView.setText(leftLanguage);

        mTextInput = (EditText) mRootView.findViewById(R.id.translate_text_input);
        mTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    KeyboardTranslateFragment.this.translate();
                    InputMethodManager imm = (InputMethodManager) KeyboardTranslateFragment.this.getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(KeyboardTranslateFragment.this.getView().getWindowToken(), 0);
                }

                return true;
            }
        });

        mTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.equals("")) {
                    mClearBtn.setVisibility(View.GONE);
                } else {
                    mClearBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mClearBtn = (ImageButton) mRootView.findViewById(R.id.clear_btn);
        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextInput.setText("");
                mTranslationTextView.setText("");
                mClearBtn.setVisibility(View.GONE);
            }
        });

        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen) {
                    mTextInput.setCursorVisible(true);
                } else {
                    KeyboardTranslateFragment.this.translate();
                    mTextInput.setCursorVisible(false);
                }
            }
        });

        final UtteranceProgressListener listener = new UtteranceProgressListener() {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    toggleInputPlayBtnVisibility();
                }
            };

            @Override
            public void onStart(String utteranceId) {
                getActivity().runOnUiThread(r);
            }

            @Override
            public void onDone(String utteranceId) {
            }

            @Override
            public void onError(String utteranceId) {
            }
        };

        mInputProgressBar = (ProgressBar) mRootView.findViewById(R.id.input_progressbar);
        mSpeakInputTextBtn = (ImageButton) mRootView.findViewById(R.id.speak_text_btn);
        mSpeakInputTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleInputPlayBtnVisibility();

                String fromLangCode =
                        Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(), (String) mTranslateToLanguage,
                                true);
                String toLangCode =
                        Utility.getCodeFromLanguage(KeyboardTranslateFragment.this.getContext(), (String) mTranslateFromLanguage,
                                true);
                String fromLangToLang = fromLangCode + "-" + toLangCode;
                ((MainActivity) KeyboardTranslateFragment.this.getActivity()).speakText(
                        mTextInput.getText().toString(), fromLangToLang, listener);
            }
        });
    }

    private void toggleInputPlayBtnVisibility() {
        if (mSpeakInputTextBtn.getVisibility() == View.VISIBLE) {
            mSpeakInputTextBtn.setVisibility(View.GONE);
            mInputProgressBar.setVisibility(View.VISIBLE);
        } else {
            mSpeakInputTextBtn.setVisibility(View.VISIBLE);
            mInputProgressBar.setVisibility(View.GONE);
        }
    }

    private void toggleTranslationPlayBtnVisibility() {
        if (mSpeakTranslationBtn.getVisibility() == View.VISIBLE) {
            mSpeakTranslationBtn.setVisibility(View.GONE);
            mTranslationProgressBar.setVisibility(View.VISIBLE);
        } else {
            mSpeakTranslationBtn.setVisibility(View.VISIBLE);
            mTranslationProgressBar.setVisibility(View.GONE);
        }
    }

    private void translate() {
        final String text = mTextInput.getText().toString();
        if (text.equals("")) {
            return;
        }

        String fromLangCode =
                Utility.getCodeFromLanguage(getContext(), (String) mTranslateFromLanguage, false);
        String toLangCode =
                Utility.getCodeFromLanguage(getContext(), (String) mTranslateToLanguage, false);
        String fromLangCodeToLangCode = fromLangCode + "-" + toLangCode;
        Callback<TranslationResult> callback = new Callback<TranslationResult>() {
            @Override
            public void onResponse(Call<TranslationResult> call, Response<TranslationResult> response) {
                final TranslationResult translation = response.body();
                if (translation == null) {
                    ((MainActivity) getActivity()).onTranslationFailure();
                    return;
                }

                final String lang = translation.getLang();
                String translatedText = translation.getText()[0];

                mTranslationTextView.setText(translatedText);

                ((View) mHistoryRecyclerView.getParent()).setVisibility(View.VISIBLE);

                Translation tr = new Translation(translatedText, text, lang);
                mAdapter.addTranslation(tr);
            }

            @Override
            public void onFailure(Call<TranslationResult> call, Throwable t) {
                ((MainActivity) getActivity()).onTranslationFailure();
            }
        };
        mTranslator.translate(text, fromLangCodeToLangCode, callback);
    }

    private void setupLanguageSelection() {
        mTranslateFromLanguageSpinner = (Spinner) mRootView.findViewById(R.id.translate_from_language_spinner);
        mTranslateToLanguageSpinner = (Spinner) mRootView.findViewById(R.id.translate_to_language_spinner);
        List<String> langs = Arrays.asList(getResources().getStringArray(R.array.languages));

        ArrayAdapter<String> leftAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_language_light, langs);
        ArrayAdapter<String> rightAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_language_light, langs);
        mTranslateFromLanguageSpinner.setAdapter(leftAdapter);
        mTranslateToLanguageSpinner.setAdapter(rightAdapter);

        mTranslateFromLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    mTranslateFromLanguage = ((TextView) view).getText();
                    mTextInputLanguageTextView.setText(mTranslateFromLanguage);
                    Utility.setTranslatorLanguage(getContext(),
                            Utility.LEFT_TRANSLATOR_LANGUAGE, (String) mTranslateFromLanguage);

                    if (isTextToSpeechInit) {
                        String fromLangCode = Utility.getCodeFromLanguage(getContext(),
                                (String) mTranslateFromLanguage, true);
                        Locale fromLocale =
                                Utility.getLocaleFromLangCode(fromLangCode, mLocales);
                        if (fromLocale == null) {
                            mSpeakInputTextBtn.setVisibility(View.GONE);
                        } else {
                            mSpeakInputTextBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    translate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mTranslateToLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    mTranslateToLanguage = ((TextView) view).getText();
                    mTranslateLanguageTextView.setText(mTranslateToLanguage);
                    Utility.setTranslatorLanguage(getContext(),
                            Utility.RIGHT_TRANSLATOR_LANGUAGE, (String) mTranslateToLanguage);

                    if (isTextToSpeechInit) {
                        String toLangCode = Utility.getCodeFromLanguage(getContext(),
                                (String) mTranslateToLanguage, true);
                        Locale toLocale =
                                Utility.getLocaleFromLangCode(toLangCode, mLocales);
                        if (toLocale == null) {
                            mSpeakTranslationBtn.setVisibility(View.GONE);
                        } else {
                            mSpeakTranslationBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    translate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String translateFromLang =
                Utility.getTranslatorLanguage(getContext(), Utility.LEFT_TRANSLATOR_LANGUAGE);
        String translateToLang =
                Utility.getTranslatorLanguage(getContext(), Utility.RIGHT_TRANSLATOR_LANGUAGE);
        mTranslateFromLanguageSpinner.setSelection(langs.indexOf(translateFromLang));
        mTranslateToLanguageSpinner.setSelection(langs.indexOf(translateToLang));

        mSwapLanguagesBtn = (ImageButton) mRootView.findViewById(R.id.swap_languages_btn);
        mSwapLanguagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int leftSelection = mTranslateFromLanguageSpinner.getSelectedItemPosition();
                int rightSelection = mTranslateToLanguageSpinner.getSelectedItemPosition();
                mTranslateFromLanguageSpinner.setSelection(rightSelection);
                mTranslateToLanguageSpinner.setSelection(leftSelection);

                String temp = mTextInput.getText().toString();
                mTextInput.setText(mTranslationTextView.getText());
                mTranslationTextView.setText(temp);

                translate();
            }
        });
    }
}
