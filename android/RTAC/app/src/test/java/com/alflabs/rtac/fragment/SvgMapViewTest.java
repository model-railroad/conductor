package com.alflabs.rtac.fragment;

import android.app.Activity;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

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
        String svgSource = getSvgResource("test1.svg");
        assertThat(svgSource).isNotEmpty();
    }

    private String getSvgResource(String fileName) throws IOException {
        //return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);

        ClassLoader classLoader = this.getClass().getClassLoader();

        URLClassLoader ucl = (URLClassLoader) classLoader;
        URL[] urLs = ucl.getURLs();
        assertThat(urLs).isEqualTo("");

        InputStream stream = classLoader.getResourceAsStream(fileName);
        assertThat(stream).isNotNull();

        return "";
    }
}
