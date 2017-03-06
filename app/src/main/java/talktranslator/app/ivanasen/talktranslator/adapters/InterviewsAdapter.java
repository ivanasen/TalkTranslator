package talktranslator.app.ivanasen.talktranslator.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.Interview;
import talktranslator.app.ivanasen.talktranslator.utils.InterviewMaker;
import talktranslator.app.ivanasen.talktranslator.utils.InterviewPlayer;

/**
 * Created by ivan on 3/5/2017.
 */

public class InterviewsAdapter extends RecyclerView.Adapter<InterviewsAdapter.InterviewViewHolder> {
    private static final String LOG_TAG = InterviewsAdapter.class.getSimpleName();
    private final InterviewPlayer mInterviewPlayer;
    private final Context mContext;

    private List<Interview> mInterviews;

    class InterviewViewHolder extends RecyclerView.ViewHolder {
        private TextView interviewTitle;
        private TextView interviewLanguage;
        private TextView interviewLength;
        private ImageButton playInterviewBtn;
        private final ImageButton interviewBeingPlayedBtn;
        private ProgressBar progressBar;

        private InterviewViewHolder(View itemView) {
            super(itemView);
            interviewTitle = (TextView) itemView.findViewById(R.id.interview_title);
            interviewLanguage = (TextView) itemView.findViewById(R.id.interview_language);
            interviewLength = (TextView) itemView.findViewById(R.id.interview_length_textview);
            playInterviewBtn = (ImageButton) itemView.findViewById(R.id.play_interview_btn);
            interviewBeingPlayedBtn =
                    (ImageButton) itemView.findViewById(R.id.interview_being_played_btn);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
        }
    }

    public InterviewsAdapter(Context activityContext, InterviewPlayer interviewPlayer) {
        mContext = activityContext;
        mInterviewPlayer = interviewPlayer;
        long count = Interview.count(Interview.class);
        mInterviews = Interview.listAll(Interview.class);
    }

    @Override
    public InterviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_interview, parent, false);
        return new InterviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final InterviewViewHolder holder, int position) {
        final Interview interview = mInterviews.get(position);
        holder.interviewTitle.setText(interview.getTitle());
        holder.interviewLanguage.setText(interview.getLanguage());

        long lengthInSeconds = interview.getLength();
        long minutes = lengthInSeconds / 60;
        long seconds = lengthInSeconds % 60;
        holder.interviewLength.setText("" + String.format("%02d", minutes) + ":"
                + String.format("%02d", seconds));

        final Runnable setBeingPlayedRunnable = new Runnable() {
            @Override
            public void run() {
                holder.playInterviewBtn.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                holder.interviewBeingPlayedBtn.setVisibility(View.VISIBLE);
            }
        };
        final Runnable setStopPlayingRunnable = new Runnable() {
            @Override
            public void run() {
                holder.playInterviewBtn.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.interviewBeingPlayedBtn.setVisibility(View.GONE);
            }
        };

        final UtteranceProgressListener onUterrenceListener = new UtteranceProgressListener() {
            int interviewParts =
                    interview.getText().split(InterviewMaker.INTERVIEW_TEXT_TO_SPEECH_PAUSE).length;
            int index = 0;

            @Override
            public void onStart(String utteranceId) {
                if (index == 0) {
                    ((Activity) mContext).runOnUiThread(setBeingPlayedRunnable);
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (index >= interviewParts - 1) {
                    ((Activity) mContext).runOnUiThread(setStopPlayingRunnable);
                    index = 0;
                }
                index++;
            }

            @Override
            public void onError(String utteranceId) {
                ((Activity) mContext).runOnUiThread(setStopPlayingRunnable);
                index = 0;
            }
        };

        holder.playInterviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.playInterviewBtn.setVisibility(View.GONE);
                            holder.progressBar.setVisibility(View.VISIBLE);
                            holder.interviewBeingPlayedBtn.setVisibility(View.GONE);
                        }
                    });
                    mInterviewPlayer.playInterview(interview, onUterrenceListener);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInterviews.size();
    }
}
