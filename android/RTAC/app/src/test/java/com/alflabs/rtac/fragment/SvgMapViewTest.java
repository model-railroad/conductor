package com.alflabs.rtac.fragment;

import android.app.Activity;
import com.alflabs.rtac.BuildConfig;
import com.caverock.androidsvg.Colour;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SvgElement;
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
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

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
        SVG svg = mView.loadSvg(svgSource);

        Set<String> ids = svg.getAllElementIds();
        assertThat(ids).isEqualTo(new TreeSet<>(Arrays.asList(
                "Arrow1Lend", "S-b320", "S-b321", "S-b322", "T-t330", "defs5429",
                "layer1", "layer10", "layer13", "layer2", "layer3", "layer4", "layer5",
                "marker9548", "marker9582", "marker9628", "marker9686",
                "path4403", "path5470-5", "path5704", "path6042-0", "path6044-6-4-1", "path6612-31-2",
                "path9275", "path9550", "path9584", "path9630", "path9688",
                "rect5385", "svg5427", "tspan6071-78-3", "tspan6071-92-4", "tspan6071-98-5", "tspan7252-6-7-1",
                "tspan7260-7", "tspan7268-8", "tspan7272-0"
        )));
    }

    @Test
    public void testSensorColor() throws Exception {
        String svgSource = getResource("test1.svg");
        SVG svg = mView.loadSvg(svgSource);

        SvgElement e = svg.getElementById("S-b321");
        assertThat(e).isNotNull();
        assertThat(e.style).isNotNull();
        assertThat(e.style.fill).isNull(); // fill:none
        assertThat(e.style.stroke).isInstanceOf(Colour.class);
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);

        // set to OFF (default)... no change ==> green
        assertThat(mView.setSensorColor("S/b321", false)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);

        // set to ON... changed ==> red
        assertThat(mView.setSensorColor("S/b321", true)).isTrue();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFFFF0000);

        // set to ON... no change ==> red
        assertThat(mView.setSensorColor("S/b321", true)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFFFF0000);

        // set to OFF... changed ==> green
        assertThat(mView.setSensorColor("S/b321", false)).isTrue();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);

        // set to OFF... no change ==> green
        assertThat(mView.setSensorColor("S/b321", false)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);
    }

    private String getResource(String fileName) throws IOException {
        String dirName = this.getClass().getPackage().getName().replace('.', File.separatorChar);
        fileName = dirName + File.separator + fileName;
        String data = Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
        assertThat(data).isNotEmpty();
        return data;
    }
}
