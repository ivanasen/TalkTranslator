package talktranslator.app.ivanasen.talktranslator.adapters;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.activities.MainActivity;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
import talktranslator.app.ivanasen.talktranslator.models.Translation;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/25/2017.
 */
public class TranslationHistoryAdapter extends RecyclerView.Adapter<TranslationHistoryAdapter.TranslationViewHolder> {

    private final Context mContext;
    private List<Translation> mTranslations;

    class TranslationViewHolder extends RecyclerView.ViewHolder {
        private TextView translationTextView;
        private TextView originalTextView;
        private TextView languagesTextView;
        private ImageButton copyTranslationButton;

        private TranslationViewHolder(View itemView) {
            super(itemView);
            translationTextView = (TextView) itemView.findViewById(R.id.translation_textview);
            originalTextView = (TextView) itemView.findViewById(R.id.original_text_textview);
            languagesTextView = (TextView) itemView.findViewById(R.id.languages_textview);
            copyTranslationButton = (ImageButton) itemView.findViewById(R.id.copy_translation_btn);
        }
    }

    public TranslationHistoryAdapter(Context context, List<Translation> translations) {
        mContext = context;
        mTranslations = translations;
    }

    @Override
    public TranslationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_history_translation, parent, false);
        return new TranslationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TranslationViewHolder holder, int position) {
        final Translation translation = mTranslations.get(position);

        holder.translationTextView.setText(translation.getTranslatedText());
        holder.originalTextView.setText(translation.getOriginalText());
        holder.languagesTextView.setText(translation.getLanguage());
        holder.copyTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.copyTextToClipboard(mContext, translation.getTranslatedText());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTranslations.size();
    }

    public void addTranslation(Translation translation) {
        if (mTranslations.size() > 0) {
            String lastTranslationText = mTranslations.get(0).getTranslatedText();
            if (lastTranslationText.equals(translation.getTranslatedText())) {
                return;
            }
        }

        mTranslations.add(0, translation);
        notifyItemInserted(0);
        translation.save();
    }

    public void removeTranslation(int index) {
        Translation.delete(mTranslations.get(index));
        mTranslations.remove(index);
        notifyItemRemoved(index);
    }

    public Translation getTranslationAt(int index) {
        return mTranslations.get(index);
    }
}
