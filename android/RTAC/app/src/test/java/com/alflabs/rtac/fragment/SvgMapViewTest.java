package com.alflabs.rtac.fragment;

import android.app.Activity;
import com.alflabs.rtac.BuildConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;


/**
 * Test for {@link SvgMapView}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class SvgMapViewTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private Activity mActivity;
    private SvgMapView mView;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.buildActivity(Activity.class).setup().get();
        mView = new SvgMapView(mActivity);
        assertThat(mView).isNotNull();
    }

    @Test
    public void testLoadSvg() throws Exception {
        String svgSource = getResource("test1.svg");

        mView.loadSvg(svgSource);

    }

    private String getResource(String fileName) throws IOException {
        String dirName = this.getClass().getPackage().getName().replace('.', File.separatorChar);
        fileName = dirName + File.separator + fileName;
        String data = Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
        assertThat(data).isNotEmpty();
        return data;
    }
}
