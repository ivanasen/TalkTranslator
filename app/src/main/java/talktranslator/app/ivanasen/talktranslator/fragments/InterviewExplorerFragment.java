package talktranslator.app.ivanasen.talktranslator.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
        final InterviewsAdapter adapter = new InterviewsAdapter(getActivity(), mInterviewPlayer);
        final RecyclerView interviewsView = (RecyclerView) mRootView.findViewById(R.id.interviews_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        interviewsView.setLayoutManager(layoutManager);
        interviewsView.setAdapter(adapter);

        final View emptyInterviewsView = mRootView.findViewById(R.id.empty_interviews_view);
        if (adapter.getItemCount() == 0) {
            interviewsView.setVisibility(View.GONE);
            emptyInterviewsView.setVisibility(View.VISIBLE);
        }

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
                        adapter.removeInterview(viewHolder.getAdapterPosition());
                        if (adapter.getItemCount() == 0) {
                            interviewsView.setVisibility(View.GONE);
                            emptyInterviewsView.setVisibility(View.VISIBLE);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(interviewsView);
    }

    public void setTextToSpeech(TextToSpeech textToSpeech) {
        mInterviewPlayer = new InterviewPlayer(textToSpeech, getContext());
        setupInterviews();
    }
}
