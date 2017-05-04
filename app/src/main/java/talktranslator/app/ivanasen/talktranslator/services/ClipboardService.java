package talktranslator.app.ivanasen.talktranslator.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

import talktranslator.app.ivanasen.talktranslator.activities.CopyToTranslateActivity;
import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.activities.SettingsActivity;

/**
 * Created by ivan on 4/27/2017.
 */

public class ClipboardService extends Service {

    private static final int CLIPBOARD_SERVICE_NOTIFICATION_ID = 1;

    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String copiedText = getPrimaryClipText();
                if (copiedText != null && !copiedText.equals("")) {
                    startCopyTranslator(copiedText);
                } else {
                    notifyUserForInvalidText();
                }
            }
        };
        mClipboardManager.addPrimaryClipChangedListener(mListener);

        showClipboardNotification(this);
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

    @Override
    public void onDestroy() {
        mClipboardManager.removePrimaryClipChangedListener(mListener);
        hideClipboardNotification(this);
    }

    private void notifyUserForInvalidText() {
        Toast.makeText(this, getString(R.string.invalid_text_toast), Toast.LENGTH_SHORT).show();
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

    private static void showClipboardNotification(Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_translate)
                .setContentTitle(context.getString(R.string.clipboard_notification_title))
                .setContentText(context.getString(R.string.clipboard_notification_text))
                .setOngoing(true);
        Intent resultIntent = new Intent(context, SettingsActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(SettingsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(CLIPBOARD_SERVICE_NOTIFICATION_ID, builder.build());
    }

    private String getPrimaryClipText() {
        if (mClipboardManager.hasPrimaryClip()) {
            ClipData data = mClipboardManager.getPrimaryClip();
            ClipData.Item copiedItem = data.getItemAt(0);

            CharSequence text = copiedItem.getText();
            if (copiedItem.getUri() == null && text != null) {
                return text.toString();
            } else {
                notifyUserForInvalidText();
            }
        }
        return null;
    }

    private static void hideClipboardNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CLIPBOARD_SERVICE_NOTIFICATION_ID);
    }

    public static boolean checkURL(CharSequence input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isURL = URL_PATTERN.matcher(input).matches();
        if (!isURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isURL = true;
                } catch (Exception ignored) {
                }
            }
        }
        return isURL;
    }
}
