package talktranslator.app.ivanasen.talktranslator;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;

/**
 * Created by ivan on 2/11/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.TranslationViewHolder> {

    private static final int VIEW_TYPE_LEFT_TRANSLATION = 0;
    private static final int VIEW_TYPE_RIGHT_TRANSLATION = 1;
    private final Set<Locale> mLocales;
    private final boolean mIsTextToSpeechDisabled;

    private Context mContext;
    private List<ChatTranslation> mChatTranslations;
    private TextToSpeech mTextToSpeech;

    class TranslationViewHolder extends RecyclerView.ViewHolder {
        TextView translatedTextView;
        TextView originalTextView;
        ImageButton replayTranslationBtn;

        private TranslationViewHolder(View itemView) {
            super(itemView);
            translatedTextView = (TextView) itemView.findViewById(R.id.translated_textview);
            originalTextView = (TextView) itemView.findViewById(R.id.original_text_textview);
            replayTranslationBtn = (ImageButton) itemView.findViewById(R.id.replay_translation_btn);
        }
    }

    public ChatAdapter(Context context, List<ChatTranslation> chatTranslations, TextToSpeech textToSpeech) {
        mContext = context;
        mTextToSpeech = textToSpeech;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLocales = mTextToSpeech.getAvailableLanguages();
            mIsTextToSpeechDisabled = false;
        } else {
            mLocales = null;
            mIsTextToSpeechDisabled = true;
        }

        if (chatTranslations == null || chatTranslations.size() == 0) {
            chatTranslations = new ArrayList<>();
        }

        mChatTranslations = chatTranslations;
    }

    @Override
    public TranslationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }

        return new TranslationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TranslationViewHolder holder, int position) {
        final ChatTranslation chatTranslation = mChatTranslations.get(position);
        holder.translatedTextView.setText(chatTranslation.getTranslatedText());
        holder.originalTextView.setText(chatTranslation.getOriginalText());

        Locale langLocale = chatTranslation.getTranslatedLocaleFromTextToSpeechLocales(mLocales);
        if (langLocale == null) {
            holder.replayTranslationBtn.setEnabled(false);
            return;
        }

        mTextToSpeech.setLanguage(langLocale);

        holder.replayTranslationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTextToSpeech.speak(chatTranslation.getTranslatedText(),
                            TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTextToSpeech.speak(chatTranslation.getTranslatedText(),
                            TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        ChatTranslation chatTranslation = mChatTranslations.get(position);
        if (chatTranslation.isLeftTranslation()) {
            return VIEW_TYPE_LEFT_TRANSLATION;
        } else {
            return VIEW_TYPE_RIGHT_TRANSLATION;
        }
    }

    @Override
    public int getItemCount() {
        return mChatTranslations.size();
    }

    public void addTranslation(ChatTranslation chatTranslation) {
        mChatTranslations.add(chatTranslation);
        notifyItemInserted(mChatTranslations.size() - 1);
        notifyDataSetChanged();
    }
}
