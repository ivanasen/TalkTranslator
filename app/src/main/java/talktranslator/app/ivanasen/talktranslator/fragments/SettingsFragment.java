package talktranslator.app.ivanasen.talktranslator.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;

import java.util.Locale;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.models.ChatTranslation;
import talktranslator.app.ivanasen.talktranslator.models.Translation;
import talktranslator.app.ivanasen.talktranslator.services.ClipboardService;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/27/2017.
 */

public class SettingsFragment extends PreferenceFragment {

    private ListPreference mCopyToTranslateLanguagePref;
    private SwitchPreference mCopyToTranslatePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        initCopyToTranslatePreference();
        initCopyToTranslateLanguagePreference();
        initClearHistoryPreference();
    }

    private void initCopyToTranslateLanguagePreference() {
        mCopyToTranslateLanguagePref =
                (ListPreference) findPreference(getString(R.string.pref_copy_to_translate_language));
        mCopyToTranslateLanguagePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newLanguage = Utility.getLanguageFromCode(getActivity(), (String) newValue);
                setLanguageChooseSummary(newLanguage);
                return true;
            }
        });

        String currentLanguage =
                Utility.getLanguageFromCode(getActivity(), mCopyToTranslateLanguagePref.getValue());
        setLanguageChooseSummary(currentLanguage);
        mCopyToTranslateLanguagePref.setEnabled(mCopyToTranslatePref.isChecked());
    }

    private void setLanguageChooseSummary(String value) {
        String summary = (value == null || value.equals("")) ?
                Locale.getDefault().getDisplayLanguage() : value;
        mCopyToTranslateLanguagePref.setSummary(summary);
    }

    private void initCopyToTranslatePreference() {
        mCopyToTranslatePref =
                (SwitchPreference) findPreference(getString(R.string.pref_copy_to_translate_key));
        mCopyToTranslatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mCopyToTranslateLanguagePref.setEnabled((boolean) newValue);
                toggleClipboardService(SettingsFragment.this.getActivity(), (boolean) newValue);
                return true;
            }
        });
    }

    private void initClearHistoryPreference() {
        Preference clearHistoryPref = findPreference(getString(R.string.clear_history_key));
        clearHistoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                askForHistoryDelete(SettingsFragment.this.getActivity());
                return true;
            }
        });
    }

    public static void toggleClipboardService(Context context, boolean shouldShow) {
        if (shouldShow) {
            Intent service = new Intent(context, ClipboardService.class);
            context.startService(service);
        } else {
            Intent service = new Intent(context, ClipboardService.class);
            context.stopService(service);
        }
    }

    public static void askForHistoryDelete(Context context) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Translation.deleteAll(Translation.class);
                        ChatTranslation.deleteAll(ChatTranslation.class);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.alert_clear_history_title)
                .setMessage(R.string.alert_clear_history_msg)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }
}
