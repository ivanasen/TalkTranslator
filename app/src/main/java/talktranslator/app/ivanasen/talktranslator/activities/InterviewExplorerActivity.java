package talktranslator.app.ivanasen.talktranslator.activities;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.fragments.InterviewExplorerFragment;

public class InterviewExplorerActivity extends AppCompatActivity {

    private TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interviews);

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                InterviewExplorerFragment fragment = (InterviewExplorerFragment)
                        getSupportFragmentManager().findFragmentById(R.id.interviews);
                fragment.setTextToSpeech(mTextToSpeech);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
    }
}
