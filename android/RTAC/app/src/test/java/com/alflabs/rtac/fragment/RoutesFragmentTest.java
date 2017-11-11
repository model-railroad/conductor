/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.rtac.fragment;

import android.app.Fragment;
import android.view.View;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

/**
 * A version of the {@link RoutesFragment} test that uses the real IAppComponent and the real IMainActivityComponent.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class RoutesFragmentTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private MainActivity mActivity;
    private Fragment mFragment;

    @Before
    public void setUp() throws Exception {
        // Setup the activity including create() and visible()
        mActivity = Robolectric.buildActivity(MainActivity.class).setup().get();
        mFragment = mActivity.getFragmentManager().findFragmentById(R.id.routes_fragment);
        assertThat(mFragment).isNotNull();
    }

    @Test
    public void testFragmentVisible() throws Exception {
        assertThat(mFragment).isNotNull();
        assertThat(mFragment.getView()).isNotNull();
        assertThat(mFragment.getView().getVisibility()).isEqualTo(View.VISIBLE);
    }
}
