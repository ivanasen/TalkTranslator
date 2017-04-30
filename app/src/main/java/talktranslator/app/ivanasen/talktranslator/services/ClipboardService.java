package talktranslator.app.ivanasen.talktranslator.services;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;

import talktranslator.app.ivanasen.talktranslator.activities.CopyToTranslateActivity;
import talktranslator.app.ivanasen.talktranslator.R;

/**
 * Created by ivan on 4/27/2017.
 */

public class ClipboardService extends Service {

    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipboardManager.OnPrimaryClipChangedListener mListener =
                new ClipboardManager.OnPrimaryClipChangedListener() {
                    @Override
                    public void onPrimaryClipChanged() {
                        String copiedText = (String) getPrimaryClipText();
                        startCopyTranslator(copiedText);
                    }
                };
        mClipboardManager.addPrimaryClipChangedListener(mListener);
    }

    private void startCopyTranslator(String text) {
        //Start translator
        Intent intent = new Intent(this, CopyToTranslateActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                R.transition.fade_in, R.transition.fade_out).toBundle();
        intent.putExtra(CopyToTranslateActivity.EXTRA_TEXT_TO_TRANSLATE, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, bundle);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private CharSequence getPrimaryClipText() {
        if (mClipboardManager.hasPrimaryClip()) {
            ClipData data = mClipboardManager.getPrimaryClip();
            return data.getItemAt(0).getText();
        }
        return null;
    }
}
