package talktranslator.app.ivanasen.talktranslator.utils;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.Interview;

/**
 * Created by ivan on 3/4/2017.
 */

public class InterviewMaker {

    public static final String INTERVIEW_TEXT_TO_SPEECH_PAUSE = "\\.";

    private Context mContext;
    private StringBuilder mBuilder;
    private boolean mHasJustAddedInterviewerText;
    private boolean mHasJustStartedInterview;

    public InterviewMaker(Context activityContext) {
        mContext = activityContext;
        mBuilder = new StringBuilder();
        mHasJustStartedInterview = true;
        mHasJustAddedInterviewerText = false;
    }

    public void addInterviewerText(String text) {
        if (!mHasJustAddedInterviewerText && !mHasJustStartedInterview) {
            mBuilder.append(INTERVIEW_TEXT_TO_SPEECH_PAUSE);
        }

        mBuilder.append(text);
        mBuilder.append(" ");
        mHasJustAddedInterviewerText = true;
        if (mHasJustStartedInterview) {
            mHasJustStartedInterview = false;
        }
    }

    public void addIntervieweeText(String text) {
        if (mHasJustAddedInterviewerText && !mHasJustStartedInterview) {
            mBuilder.append(INTERVIEW_TEXT_TO_SPEECH_PAUSE);
        } else if (!mHasJustStartedInterview) {
            mBuilder.append(" ");
        }
        mBuilder.append(text);
        mHasJustAddedInterviewerText = false;
        if (mHasJustStartedInterview) {
            mHasJustStartedInterview = false;
        }
    }

    public void saveInterview(final long length) {
        final String interviewText = mBuilder.toString();
        mBuilder.setLength(0);
        final String language =
                Utility.getTranslatorLanguage(mContext, Utility.LEFT_TRANSLATOR_LANGUAGE);
        InterviewTitleCallback callback = new InterviewTitleCallback() {
            @Override
            public void onTitleEntered(String title) {
                Interview interview = new Interview(title, interviewText, language, length);
                interview.save();
                Toast.makeText(mContext,
                        mContext.getString(R.string.interview_saved_toast),
                        Toast.LENGTH_LONG)
                        .show();
            }
        };
        askUserForInterviewTitle(callback);
    }

    private void askUserForInterviewTitle(final InterviewTitleCallback callback) {
        String interviewName = mContext.getString(R.string.interview_default_name);
        long interviewCount = Interview.count(Interview.class);
        if (interviewCount > 0) {
            interviewName += "(" + interviewCount + ")";
        }

        final View interviewTitleView = LayoutInflater.from(mContext)
                .inflate(R.layout.interview_title_dialog, null, false);
        final EditText titleEditText = (EditText)
                interviewTitleView.findViewById(R.id.title_edittext);
        titleEditText.setText(interviewName);

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.save_interview_dialog_title)
                .setMessage(R.string.save_itnerview_dialog_msg)
                .setView(interviewTitleView)
                .setPositiveButton(R.string.save_interview_dialog_positive_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onTitleEntered(titleEditText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.save_interview_dialog_negative_btn, null)
                .show();
    }

    public String getCurrentInterviewText() {
        return mBuilder.toString();
    }

    private interface InterviewTitleCallback {
        public void onTitleEntered(String title);
    }
}
