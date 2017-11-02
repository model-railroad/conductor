package com.alflabs.conductor.parser;

import com.alflabs.conductor.ConductorModule;
import com.alflabs.conductor.DaggerIConductorComponent;
import com.alflabs.conductor.IConductorComponent;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.utils.FileOps;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for both {@link ScriptParser2} *and* {@link Script} execution engine
 * using full script files.
 */
public class ScriptParserFullTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;
    @Mock FileOps mFileOps;

    private TestReporter mReporter;
    private IScriptComponent mScriptComponent;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getThrotlle(42)).thenReturn(mJmriThrottle);

        mReporter = new TestReporter();

        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        IConductorComponent realNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider) {
                    @Override
                    public FileOps provideFileOps() {
                        return mFileOps;
                    }
                })
                .scriptFile(file)
                .build();

        mScriptComponent = realNowComponent.newScriptComponent(
                new ScriptModule(mReporter, realNowComponent.getKeyValueServer()));
    }

    @Test
    public void testScript1() throws Exception {
        String source = getFileSource("script1.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript2() throws Exception {
        String source = getFileSource("script2.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript4() throws Exception {
        String source = getFileSource("script4.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript5() throws Exception {
        when(mFileOps.isFile(new File("maps\\filename1.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("maps\\filename1.svg"), Charsets.UTF_8)).thenReturn("svg1");

        when(mFileOps.isFile(new File("maps\\filename2.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("maps\\filename2.svg"), Charsets.UTF_8)).thenReturn("svg2");

        String source = getFileSource("script5.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    private String getFileSource(String fileName) throws IOException {
        String path = new File("v2", fileName).getPath();
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }
}
