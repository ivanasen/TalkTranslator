package talktranslator.app.ivanasen.talktranslator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.models.Translation;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/11/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LEFT_TRANSLATION = 0;
    private static final int VIEW_TYPE_RIGHT_TRANSLATION = 1;
    private static final int VIEW_TYPE_CHANGED_LANGUAGES = 2;

    private Set<Locale> mLocales;
    private Context mContext;
    private List<Object> mListItems;
    private TextToSpeech mTextToSpeech;
    private boolean mIsTextToSpeechDisabled;

    private class TranslationViewHolder extends RecyclerView.ViewHolder {
        TextView translatedTextView;
        TextView originalTextView;
        ImageButton replayTranslationBtn;
        LinearLayout mChatBubble;
        ProgressBar progressBar;

        private TranslationViewHolder(View itemView) {
            super(itemView);
            translatedTextView = (TextView) itemView.findViewById(R.id.translated_textview);
            originalTextView = (TextView) itemView.findViewById(R.id.original_text_textview);
            replayTranslationBtn = (ImageButton) itemView.findViewById(R.id.replay_translation_btn);
            mChatBubble = (LinearLayout) itemView.findViewById(R.id.chat_bubble);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
        }
    }

    private class ChangedLanguagesViewHolder extends RecyclerView.ViewHolder {
        TextView changedLangsTextView;

        private ChangedLanguagesViewHolder(View itemView) {
            super(itemView);
            changedLangsTextView = (TextView) itemView.findViewById(R.id.changed_languages_textview);
        }
    }

    private class LanguagesChange {
        String language1;
        String language2;

        private LanguagesChange(String language1, String language2) {
            this.language1 = language1;
            this.language2 = language2;
        }
    }

    public ChatAdapter(Context context, List<Translation> translations, TextToSpeech mTextToSpeech) { //Language changes aren't saved in db
        mContext = context;

        if (translations == null) {
            translations = new ArrayList<>();
        }

        mListItems = new ArrayList<Object>(translations);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_LEFT_TRANSLATION:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_chat_left, parent, false);
                break;
            case VIEW_TYPE_RIGHT_TRANSLATION:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_chat_right, parent, false);
                break;
            case VIEW_TYPE_CHANGED_LANGUAGES:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_changed_languages, parent, false);
                return new ChangedLanguagesViewHolder(itemView);
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }

        return new TranslationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_CHANGED_LANGUAGES) {
            LanguagesChange languagesChange = (LanguagesChange) mListItems.get(position);
            ChangedLanguagesViewHolder changeViewHolder = (ChangedLanguagesViewHolder) holder;

            changeViewHolder.changedLangsTextView.setText(String.format(
                    mContext.getString(R.string.changed_langs_label),
                    languagesChange.language1, languagesChange.language2));
            return;
        }

        final Translation translation = (Translation) mListItems.get(position);
        final TranslationViewHolder translationViewHolder = (TranslationViewHolder) holder;

        translationViewHolder.translatedTextView.setText(translation.getTranslatedText());
        translationViewHolder.originalTextView.setText(translation.getOriginalText());

        if (mTextToSpeech != null) {
            setupReplayTranslationButton(translationViewHolder, translation);
        }

        final CharSequence translatedText = translationViewHolder.translatedTextView.getText();
        ((TranslationViewHolder) holder).mChatBubble.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        copyTextToClipboard(translatedText);
                        return true;
                    }
                });

    }

    private void copyTextToClipboard(CharSequence translatedText) {
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(mContext.getString(R.string.app_name),
                translatedText);
        clipboard.setPrimaryClip(clip);

        Toast msg = Toast.makeText(mContext,
                mContext.getString(R.string.notify_text_copied), Toast.LENGTH_SHORT);
        msg.show();
    }

    @Override
    public int getItemViewType(int position) {
        Object listItem = mListItems.get(position);
        if (listItem instanceof LanguagesChange) {
            return VIEW_TYPE_CHANGED_LANGUAGES;
        } else if (listItem instanceof Translation) {
            Translation tr = (Translation) listItem;
            if (tr.isLeftTranslation()) {
                return VIEW_TYPE_LEFT_TRANSLATION;
            } else {
                return VIEW_TYPE_RIGHT_TRANSLATION;
            }
        } else {
            throw new IllegalArgumentException("Not valid view type");
        }
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    private void setupReplayTranslationButton(TranslationViewHolder holder,
                                              final Translation chatTranslation) {
        String langCode = Utility.getTranslatedLanguage(chatTranslation.getLanguages());
        String translation = chatTranslation.getTranslatedText();

        if (langCode.equals(mContext.getString(R.string.lang_code_bg))) {
            langCode = mContext.getString(R.string.lang_code_ru);
            translation = Utility.editBulgarianTextForRussianReading(translation);
        }

        final Locale locale = Utility.getLocaleFromLangCode(langCode, mLocales);

        if (locale == null) {
            holder.replayTranslationBtn.setVisibility(View.GONE);
            return;
        }

        final String finalTranslation = translation;
        holder.replayTranslationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextToSpeech.setLanguage(locale);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTextToSpeech.speak(finalTranslation,
                            TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTextToSpeech.speak(finalTranslation,
                            TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    public void addTranslation(Translation translation) {
        mListItems.add(translation);
        notifyDataSetChanged();
    }

    public void changeLanguages(String language1, String language2) {
        LanguagesChange change = new LanguagesChange(language1, language2);
        mListItems.add(change);
        notifyDataSetChanged();
    }

    public void setTextToSpeech(TextToSpeech mTextToSpeech) {
        this.mTextToSpeech = mTextToSpeech;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLocales = mTextToSpeech.getAvailableLanguages();
            mIsTextToSpeechDisabled = false;
        } else {
            mLocales = null;
            mIsTextToSpeechDisabled = true;
        }
    }
}
