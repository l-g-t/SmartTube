package com.liskovsoft.smartyoutubetv2.tv.ui.browse;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment.OnHeaderViewSelectedListener;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.Row;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.SettingsGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.interfaces.CategoryFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.interfaces.SettingsCategoryFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.interfaces.VideoCategoryFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.settings.SettingsGridFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.MultiVideoGridFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.VideoGridFragment;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.VideoRowsFragment;

import java.util.HashMap;
import java.util.Map;

public class BrowseSectionFragmentFactory extends BrowseSupportFragment.FragmentFactory<Fragment> {
    private static final String TAG = BrowseSectionFragmentFactory.class.getSimpleName();
    private final OnHeaderViewSelectedListener mViewSelectedListener;
    private Fragment mCurrentFragment;
    private int mFragmentType = BrowseSection.TYPE_GRID;
    private int mSelectedItemIndex = -1;
    private final Map<Integer, Fragment> mFragmentMap = new HashMap<>();

    public BrowseSectionFragmentFactory() {
        this(null);
    }

    public BrowseSectionFragmentFactory(OnHeaderViewSelectedListener viewSelectedListener) {
        mViewSelectedListener = viewSelectedListener;

        initFragmentMap();
    }

    private void initFragmentMap() {
        mFragmentMap.put(BrowseSection.TYPE_ROW, new VideoRowsFragment());
        mFragmentMap.put(BrowseSection.TYPE_GRID, new VideoGridFragment());
        mFragmentMap.put(BrowseSection.TYPE_SETTINGS_GRID, new SettingsGridFragment());
        mFragmentMap.put(BrowseSection.TYPE_MULTI_GRID, new MultiVideoGridFragment());
    }

    /**
     * Called each time when header is changed.<br/>
     * So, no need to clear state.
     */
    @Override
    public Fragment createFragment(Object rowObj) {
        Log.d(TAG, "Creating PageRow fragment");

        Row row = (Row) rowObj;

        HeaderItem header = row.getHeaderItem();
        Fragment fragment;

        if (header instanceof CategoryHeaderItem) {
            mFragmentType = ((CategoryHeaderItem) header).getType();
        }

        fragment = mFragmentMap.get(mFragmentType);

        if (fragment != null) {
            mCurrentFragment = fragment;

            // give a chance to clear pending updates
            if (mViewSelectedListener != null) {
                mViewSelectedListener.onHeaderSelected(null, row);
            }
            
            setCurrentFragmentItemIndex(mSelectedItemIndex);

            return fragment;
        }

        throw new IllegalArgumentException(String.format("Invalid row %s", rowObj));
    }

    public void updateCurrentFragment(SettingsGroup group) {
        if (group == null) {
            return;
        }

        if (mCurrentFragment == null) {
            Log.e(TAG, "Page row fragment not initialized for group: " + group.getTitle());
            return;
        }

        if (mCurrentFragment instanceof SettingsCategoryFragment) {
            ((SettingsCategoryFragment) mCurrentFragment).update(group);
        } else {
            Log.e(TAG, "updateFragment: Page group fragment has incompatible type: " + mCurrentFragment.getClass().getSimpleName());
        }
    }

    public void updateCurrentFragment(VideoGroup group) {
        if (group == null) {
            return;
        }

        if (mCurrentFragment == null) {
            Log.e(TAG, "Page row fragment not initialized for group: " + group.getTitle());
            return;
        }

        updateVideoFragment(mCurrentFragment, group);
    }

    public void clearCurrentFragment() {
        if (mCurrentFragment != null) {
            clearFragment(mCurrentFragment);
        }
    }

    public boolean isEmpty() {
        if (mCurrentFragment instanceof CategoryFragment) {
            return ((CategoryFragment) mCurrentFragment).isEmpty();
        }

        return false;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public int getCurrentFragmentItemIndex() {
        if (mCurrentFragment instanceof VideoCategoryFragment) {
            return ((VideoCategoryFragment) mCurrentFragment).getPosition();
        }

        return -1;
    }

    public void setCurrentFragmentItemIndex(int index) {
        if (mCurrentFragment instanceof VideoCategoryFragment) {
            ((VideoCategoryFragment) mCurrentFragment).setPosition(index);
            mSelectedItemIndex = -1;
        } else {
            mSelectedItemIndex = index;
        }
    }

    private void updateVideoFragment(Fragment fragment, VideoGroup group) {
        if (fragment instanceof VideoCategoryFragment) {
            ((VideoCategoryFragment) fragment).update(group);
        } else {
            Log.e(TAG, "updateFragment: Page group fragment has incompatible type: " + fragment.getClass().getSimpleName());
        }
    }

    private void clearFragment(Fragment fragment) {
        if (fragment instanceof CategoryFragment) {
            ((CategoryFragment) fragment).clear();
        } else {
            Log.e(TAG, "clearFragment: Page group fragment has incompatible type: " + fragment.getClass().getSimpleName());
        }
    }
}
