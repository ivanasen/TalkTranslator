package talktranslator.app.ivanasen.talktranslator;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Filter;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerItemViewHelper;

import java.security.Permission;
import java.util.List;
import java.util.jar.Manifest;

import talktranslator.app.ivanasen.talktranslator.fragments.ConversationFragment;
import talktranslator.app.ivanasen.talktranslator.fragments.InterviewFragment;
import talktranslator.app.ivanasen.talktranslator.fragments.KeyboardTranslateFragment;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPagerAdapter = new TranslatorPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withTranslucentStatusBar(false)
                .withHeader(R.layout.nav_header_main)
                .inflateMenu(R.menu.activity_main_drawer)
                .build();

        setupTabNavigation();
    }

    private void setupTabNavigation() {
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);

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

        mTabLayout.getTabAt(0).setIcon(conversationDrawable).setText(R.string.tab_text);
        mTabLayout.getTabAt(1).setIcon(interviewDrawable).setText(R.string.tab_text);
        mTabLayout.getTabAt(2).setIcon(translateDrawable).setText(R.string.tab_text);

        TabLayout.Tab selectedTab = mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition());
        markSelectedTab(selectedTab);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String title = (String) mPagerAdapter.getPageTitle(tab.getPosition());
                setTitle(title);
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
        mTabLayout.getTabAt(pos).getIcon()
                .setColorFilter(getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
        mTabLayout.getTabAt(pos).getIcon()
                .setAlpha(getResources().getInteger(R.integer.selected_icon_alpha));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
