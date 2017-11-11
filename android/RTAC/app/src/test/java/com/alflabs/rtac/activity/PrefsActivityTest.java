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

package com.alflabs.rtac.activity;

import com.alflabs.rtac.BuildConfig;
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
 * Test for {@link PrefsActivity}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class PrefsActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private PrefsActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.buildActivity(PrefsActivity.class).setup().get();
    }

    @Test
    public void testSomething() throws Exception {
        assertThat(mActivity.isDestroyed()).isFalse();
    }

//    @Test
//    public void testPresenterValue() throws Exception {
//        TextView text = (TextView) mActivity.findViewById(R.id.text);
//        assertThat(text).isNotNull();
//        // The "42" is not in the layout XML or strings XML, it is added by the
//        // mPresenterSomething.present(..., mNetworkSomething.fetchAValue()) call
//        // in the activity onCreate and indicates there's a whole dagger graph
//        // operational at that point.
//        assertThat(text.getText().toString()).contains("42");
//    }
}
