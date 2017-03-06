package talktranslator.app.ivanasen.talktranslator.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.adapters.InterviewsAdapter;
import talktranslator.app.ivanasen.talktranslator.utils.InterviewPlayer;

/**
 * Created by ivan on 3/5/2017.
 */

public class InterviewExplorerFragment extends Fragment {

    private View mRootView;
    private InterviewPlayer mInterviewPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_interviews, container, false);
        return mRootView;
    }

    private void setupInterviews() {
        InterviewsAdapter adapter = new InterviewsAdapter(getActivity(), mInterviewPlayer);
        RecyclerView interviewsView = (RecyclerView) mRootView.findViewById(R.id.interviews_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        interviewsView.setLayoutManager(layoutManager);
        interviewsView.setAdapter(adapter);

        if (adapter.getItemCount() == 0) {
            View emptyInterviewsView = mRootView.findViewById(R.id.empty_interviews_view);
            interviewsView.setVisibility(View.GONE);
            emptyInterviewsView.setVisibility(View.VISIBLE);
        }
    }

    public void setTextToSpeech(TextToSpeech textToSpeech) {
        mInterviewPlayer = new InterviewPlayer(textToSpeech, getContext());
        setupInterviews();
    }
}
