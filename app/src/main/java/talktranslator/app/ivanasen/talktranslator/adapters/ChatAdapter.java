package talktranslator.app.ivanasen.talktranslator.adapters;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/11/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int VIEW_TYPE_LEFT_TRANSLATION = 0;
    private static final int VIEW_TYPE_RIGHT_TRANSLATION = 1;
    private static final int VIEW_TYPE_CHANGED_LANGUAGES = 2;
    private final MainActivity mActivity;
    private Context mContext;

    private List<Object> mListItems;
    private boolean mJustAddedItem;
    private boolean mShouldScrollToBottom;
    private boolean mScrollingEnabled;
    private boolean mIsInterviewFragment;

    public boolean shouldScroll() {
        return mScrollingEnabled;
    }

    private class TranslationViewHolder extends RecyclerView.ViewHolder {
        TextView translatedTextView;
        TextView originalTextView;
        ImageButton replayTranslationBtn;
        LinearLayout chatBubble;
        ProgressBar progressBar;

        private TranslationViewHolder(View itemView) {
            super(itemView);
            translatedTextView = (TextView) itemView.findViewById(R.id.translated_textview);
            originalTextView = (TextView) itemView.findViewById(R.id.original_text_textview);
            replayTranslationBtn = (ImageButton) itemView.findViewById(R.id.speak_translation_btn);
            chatBubble = (LinearLayout) itemView.findViewById(R.id.chat_bubble);
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

    public ChatAdapter(Context context,
                       MainActivity activity, boolean isInterviewFragment) {
        mContext = context;
        mActivity = activity;
        mIsInterviewFragment = isInterviewFragment;

        if (mIsInterviewFragment) {
            mListItems = new ArrayList<>();
        } else {
            List<ChatTranslation> translations = ChatTranslation.listAll(ChatTranslation.class);
            mListItems = new ArrayList<Object>(translations);
        }
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
                if (mIsInterviewFragment) {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_chat_right_white_bck, parent, false);
                } else {
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_chat_right, parent, false);
                }
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

        final ChatTranslation chatTranslation = (ChatTranslation) mListItems.get(position);
        final TranslationViewHolder translationViewHolder = (TranslationViewHolder) holder;

        translationViewHolder.translatedTextView.setText(chatTranslation.getTranslatedText());
        translationViewHolder.originalTextView.setText(chatTranslation.getOriginalText());

        if (mActivity.getAvailableTextToSpeechLangs() != null) {
            setupReplayTranslationButton(translationViewHolder, chatTranslation);
        }

        final CharSequence translatedText = translationViewHolder.translatedTextView.getText();
        ((TranslationViewHolder) holder).chatBubble.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Utility.copyTextToClipboard(mContext, translatedText);
                        return true;
                    }
                });

    }

    @Override
    public int getItemViewType(int position) {
        Object listItem = mListItems.get(position);
        if (listItem instanceof LanguagesChange) {
            return VIEW_TYPE_CHANGED_LANGUAGES;
        } else if (listItem instanceof ChatTranslation) {
            ChatTranslation tr = (ChatTranslation) listItem;
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

    private void setupReplayTranslationButton(final TranslationViewHolder holder,
                                              final ChatTranslation chatTranslation) {
        String languages = chatTranslation.getLanguages();
        String langCode = Utility.getTranslatedLanguage(languages);
        String translation = chatTranslation.getTranslatedText();

        boolean isBulgarianTTSEnabled = Utility.isBulgarianTextToSpeechEnabled(mContext);
        if (langCode.equals(mContext.getString(R.string.lang_code_bg)) && isBulgarianTTSEnabled) {
            langCode = mContext.getString(R.string.lang_code_ru);
            translation = Utility.editBulgarianTextForRussianReading(translation);
        }

        Set<Locale> locales = mActivity.getAvailableTextToSpeechLangs();
        Locale locale = null;
        if (locales != null) {
            locale = Utility.getLocaleFromLangCode(langCode, locales);
        }

        if (locale == null) {
            holder.replayTranslationBtn.setVisibility(View.GONE);
        } else {
            holder.replayTranslationBtn.setVisibility(View.VISIBLE);
        }

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                holder.progressBar.setVisibility(View.GONE);
                holder.replayTranslationBtn.setVisibility(View.VISIBLE);
            }
        };

        final UtteranceProgressListener progressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                mActivity.runOnUiThread(r);
            }

            @Override
            public void onDone(String utteranceId) {
                mActivity.runOnUiThread(r);
                mShouldScrollToBottom = false;
                mScrollingEnabled = true;
            }

            @Override
            public void onError(String utteranceId) {
                mActivity.runOnUiThread(r);
                mShouldScrollToBottom = false;
                mScrollingEnabled = true;
            }
        };

        final String finalTranslation = translation;
        if (mJustAddedItem && locale != null) {
            mShouldScrollToBottom = true;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.replayTranslationBtn.setVisibility(View.GONE);
                }
            });

            mActivity.speakText(finalTranslation, chatTranslation.getLanguages(), progressListener);
        }
        mJustAddedItem = false;
        holder.replayTranslationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.replayTranslationBtn.setVisibility(View.GONE);
                    }
                });
                mScrollingEnabled = false;
                mActivity.speakText(finalTranslation, chatTranslation.getLanguages(), progressListener);
            }
        });
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void addTranslation(ChatTranslation chatTranslation) {
        mJustAddedItem = true;
        mListItems.add(chatTranslation);
        notifyDataSetChanged();
        chatTranslation.save();
    }

    public void removeTranslation(int index) {
        if (getItemViewType(index) != VIEW_TYPE_CHANGED_LANGUAGES) {
            ChatTranslation.delete(mListItems.get(index));
            mListItems.remove(index);
            notifyItemRemoved(index);
           }
    }

    public void changeLanguages(String language1, String language2) {
        LanguagesChange change = new LanguagesChange(language1, language2);
        mListItems.add(change);
        notifyDataSetChanged();
    }

    public boolean shouldScrollToBottom() {
        return mShouldScrollToBottom;
    }
}
