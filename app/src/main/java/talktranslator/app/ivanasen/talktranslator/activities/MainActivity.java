package talktranslator.app.ivanasen.talktranslator.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import talktranslator.app.ivanasen.talktranslator.R;
import talktranslator.app.ivanasen.talktranslator.fragments.ConversationFragment;
import talktranslator.app.ivanasen.talktranslator.fragments.InterviewFragment;
import talktranslator.app.ivanasen.talktranslator.fragments.KeyboardTranslateFragment;
import talktranslator.app.ivanasen.talktranslator.fragments.SettingsFragment;
import talktranslator.app.ivanasen.talktranslator.utils.Utility;

public class MainActivity extends AppCompatActivity {

    public static final String UTTERANCE_ID = "translator utterance id";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;
    private AppBarLayout mAppBar;

    private TextToSpeech mTextToSpeech;
    private Set<Locale> mLocales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mLocales = mTextToSpeech.getAvailableLanguages();
                }
            }
        });

        mAppBar = (AppBarLayout) findViewById(R.id.appbar);

        mPagerAdapter = new TranslatorPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        setupTabNavigation();
        checkInternetConnection();
    }

    private void checkInternetConnection() {
        boolean isConnected = Utility.isNetworkConnected(this);

        if (!isConnected) {
            TextView noConnectionTextView = (TextView) findViewById(R.id.no_connection_textview);
            noConnectionTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
    }

    private void setupTabNavigation() {
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        Drawable conversationDrawable;
        Drawable interviewDrawable;
        Drawable translateDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            conversationDrawable = getDrawable(R.drawable.ic_forum_black_36dp);
            interviewDrawable = getDrawable(R.drawable.ic_microphone_variant_black_36dp);
            translateDrawable = getDrawable(R.drawable.ic_translate_black_36dp);
        } else {
            conversationDrawable = getResources().getDrawable(R.drawable.ic_forum_black_36dp);
            interviewDrawable = getResources().getDrawable(R.drawable.ic_microphone_variant_black_36dp);
            translateDrawable = getResources().getDrawable(R.drawable.ic_translate_black_36dp);
        }
        if (conversationDrawable != null && interviewDrawable != null && translateDrawable != null) {
            conversationDrawable.setAlpha(getResources().getInteger(R.integer.unselected_icon_alpha));
            interviewDrawable.setAlpha(getResources().getInteger(R.integer.unselected_icon_alpha));
            translateDrawable.setAlpha(getResources().getInteger(R.integer.unselected_icon_alpha));
        }

        tabLayout.getTabAt(0).setIcon(conversationDrawable).setText(R.string.tab_text);
        tabLayout.getTabAt(1).setIcon(interviewDrawable).setText(R.string.tab_text);
        tabLayout.getTabAt(2).setIcon(translateDrawable).setText(R.string.tab_text);

        TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        markSelectedTab(selectedTab);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String title = (String) mPagerAdapter.getPageTitle(tab.getPosition());
                setTitle(title);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (title.equals(getString(R.string.translate_title)) ||
                            title.equals(getString(R.string.interview_title))) {
                        mAppBar.setElevation(0);

                    } else {
                        mAppBar.setElevation(getResources().getDimension(R.dimen.app_elevation_default));
                    }
                }
                markSelectedTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setAlpha(getResources().getInteger(R.integer.unselected_icon_alpha));
                tab.getIcon().clearColorFilter();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String title = (String) mPagerAdapter.getPageTitle(tab.getPosition());
                setTitle(title);
                markSelectedTab(tab);
            }
        });
    }

    private void markSelectedTab(TabLayout.Tab tab) {
        int pos = tab.getPosition();
        setTitle(mPagerAdapter.getPageTitle(pos));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tab.getIcon().setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        }
        tab.getIcon().setAlpha(getResources().getInteger(R.integer.selected_icon_alpha));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_clear_history:
                SettingsFragment.askForHistoryDelete(this);
                return true;
            case R.id.action_view_interviews:
                Intent interviewsIntent = new Intent(this, InterviewExplorerActivity.class);
                startActivity(interviewsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void speakText(String text, String language, @Nullable UtteranceProgressListener listener) {
        if (mTextToSpeech == null || mTextToSpeech.isSpeaking() || mLocales == null) {
            return;
        }

        String langCode = Utility.getTranslatedLanguage(language);

        if (langCode.equals(getString(R.string.lang_code_bg))) {
            langCode = getString(R.string.lang_code_ru);
            text = Utility.editBulgarianTextForRussianReading(text);
        }

        Locale locale = Utility.getLocaleFromLangCode(langCode, mLocales);
        if (locale == null) {
            Log.d(LOG_TAG, "Language not supported by TextToSpeech.");
            return;
        }

        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        mTextToSpeech.setLanguage(locale);

        mTextToSpeech.setOnUtteranceProgressListener(listener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params, UTTERANCE_ID);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, map);
        }
    }

    public Set<Locale> getAvailableTextToSpeechLangs() {
        return mLocales;
    }

    public TextToSpeech getTextToSpeech() {
        return mTextToSpeech;
    }

    public void onTranslationFailure() {
        Log.e(LOG_TAG, "Translation failed");
        Toast.makeText(this, getString(R.string.translation_failed), Toast.LENGTH_LONG).show();
    }

    private class TranslatorPagerAdapter extends FragmentPagerAdapter {
        private final int TAB_COUNT = 3;

        public TranslatorPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ConversationFragment();
                case 1:
                    return new InterviewFragment();
                case 2:
                    return new KeyboardTranslateFragment();
                default:
                    throw new IllegalArgumentException("Invalid fragment position");
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.conversation_title);
                case 1:
                    return getString(R.string.interview_title);
                case 2:
                    return getString(R.string.translate_title);
                default:
                    throw new IllegalArgumentException("Invalid fragment position");
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }
}
