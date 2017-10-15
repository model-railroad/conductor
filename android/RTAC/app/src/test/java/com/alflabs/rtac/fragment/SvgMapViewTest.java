package com.alflabs.rtac.fragment;

import android.app.Activity;
import com.alflabs.rtac.BuildConfig;
import com.caverock.androidsvg.Colour;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SvgElement;
import com.caverock.androidsvg.text.TSpan;
import com.caverock.androidsvg.text.Text;
import com.caverock.androidsvg.text.TextSequence;
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
        assertThat(ids.toString()).isEqualTo(new TreeSet<>(Arrays.asList(
                "Arrow1Lend", "LS-b320", "LS-b321", "LS-b322", "LT-t320", "LT-t322", "LT-t326", "LT-t330",
                "S-b320", "S-b321", "S-b322", "T-t330", "T-t330N", "T-t330R", "bg-t330", "defs5429",
                "layer1", "layer10", "layer11", "layer12", "layer13", "layer2", "layer3", "layer4", "layer5",
                "marker9548", "marker9582", "marker9628", "marker9686",
                "path4403", "path5704", "path6042-0", "path6044-6-4-1", "path8054", "path9275", "path9550", "path9584",
                "path9630", "path9688", "rect5385", "svg5427",
                "tspan6071-78-3", "tspan6071-92-4", "tspan6071-98-5", "tspan7252-6-7-1", "tspan7260-7",
                "tspan7268-8", "tspan7272-0", "tunnel-320"
        )).toString());
    }

    @Test
    public void testBlockOccupancy() throws Exception {
        String svgSource = getResource("test1.svg");
        SVG svg = mView.loadSvg(svgSource);

        SvgElement e = svg.getElementById("S-b321");
        assertThat(e).isNotNull();
        assertThat(e.style).isNotNull();
        assertThat(e.style.fill).isNull(); // fill:none
        assertThat(e.style.stroke).isInstanceOf(Colour.class);
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);

        SvgElement t = svg.getElementById("LS-b321");
        assertThat(t).isInstanceOf(Text.class);
        assertThat(((Text) t).getChildren().get(0)).isInstanceOf(TSpan.class);
        assertThat(((TSpan)((Text) t).getChildren().get(0)).getChildren().get(0)).isInstanceOf(TextSequence.class);
        TextSequence textSequence = (TextSequence) ((TSpan) ((Text) t).getChildren().get(0)).getChildren().get(0);
        assertThat(textSequence.text).isEqualTo("321");

        // set to OFF (default)... no change ==> green
        assertThat(mView.setBlockOccupancy("S/b321", false)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);
        assertThat(textSequence.text).isEqualTo("321");

        // set to ON... changed ==> red
        assertThat(mView.setBlockOccupancy("S/b321", true)).isTrue();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFFFF0000);
        assertThat(textSequence.text).isEqualTo("< 321 >");

        // set to ON... no change ==> red
        assertThat(mView.setBlockOccupancy("S/b321", true)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFFFF0000);
        assertThat(textSequence.text).isEqualTo("< 321 >");

        // set to OFF... changed ==> green
        assertThat(mView.setBlockOccupancy("S/b321", false)).isTrue();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);
        assertThat(textSequence.text).isEqualTo("321");

        // set to OFF... no change ==> green
        assertThat(mView.setBlockOccupancy("S/b321", false)).isFalse();
        assertThat(((Colour) e.style.stroke).colour).isEqualTo(0xFF00FF00);
        assertThat(textSequence.text).isEqualTo("321");
    }

    @Test
    public void testTurnoutVisibility() throws Exception {
        String svgSource = getResource("test1.svg");
        SVG svg = mView.loadSvg(svgSource);

        // When loading the SVG, we change all these elements to visibile=false
        SvgElement n = svg.getElementById("T-t330N");
        assertThat(n).isNotNull();
        assertThat(n.style).isNotNull();
        assertThat(n.style.display).isFalse();

        SvgElement r = svg.getElementById("T-t330R");
        assertThat(r).isNotNull();
        assertThat(r.style).isNotNull();
        assertThat(r.style.display).isFalse();

        // Changing the wrong element does nothing
        assertThat(mView.setTurnoutVisibility("T/t370", true)).isFalse();
        assertThat(n.style.display).isFalse();
        assertThat(r.style.display).isFalse();

        assertThat(mView.setTurnoutVisibility("T/t330", true)).isTrue();
        assertThat(n.style.display).isTrue();
        assertThat(r.style.display).isFalse();

        assertThat(mView.setTurnoutVisibility("T/t330", false)).isTrue();
        assertThat(n.style.display).isFalse();
        assertThat(r.style.display).isTrue();
    }

    private String getResource(String fileName) throws IOException {
        String dirName = this.getClass().getPackage().getName().replace('.', File.separatorChar);
        fileName = dirName + File.separator + fileName;
        String data = Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
        assertThat(data).isNotEmpty();
        return data;
    }
}
