package talktranslator.app.ivanasen.talktranslator.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import talktranslator.app.ivanasen.talktranslator.models.Interview;

/**
 * Created by ivan on 3/3/2017.
 */

public class VoiceRecorder {
    private static final String LOG_TAG = VoiceRecorder.class.getSimpleName();

    private MediaRecorder mRecorder;
    private String mFileName;
    private String mFilePath;

    public VoiceRecorder(Context context) {
        mFilePath = context.getFilesDir().getAbsolutePath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String configureFileName(@Nullable String fileName) {
        if (fileName == null || fileName.equals("")) {
            long interviewIndex = Interview.count(Interview.class) + 1;
            fileName = mFilePath + "Interview" + interviewIndex;
        } else {
            fileName = mFilePath + fileName;
            List<Interview> interviewNames =
                    Interview.find(Interview.class, "internalPath = ?", fileName);
            if (interviewNames.size() > 0) {
                fileName += "(" + interviewNames.size() + ")";
            }
        }

        mFileName = fileName;
        return fileName;
    }

    public void startRecording(@Nullable String interviewName) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(configureFileName(interviewName));
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public String getFileName() {
        return mFileName;
    }
}
