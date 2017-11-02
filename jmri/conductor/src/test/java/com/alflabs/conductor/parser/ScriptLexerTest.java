package com.alflabs.conductor.parser;

import com.alflabs.conductor.parser2.ConductorLexer;
import com.alflabs.conductor.parser2.ConductorParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ScriptLexerTest {

    private CommonTokenStream createStream(String source) {
        CaseInsensitiveInputStream input = new CaseInsensitiveInputStream(source);
        ConductorLexer lexer = new ConductorLexer(input);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        return stream;
    }

    @Test
    public void testLexerComment() throws Exception {
        CommonTokenStream stream = createStream("# Comment");
        assertThat(stream.size()).isEqualTo(2);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.SB_COMMENT);
        assertThat(stream.get(0).getText()).isEqualTo("# Comment");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.EOF);
    }

    @Test
    public void testLexerNum() throws Exception {
        CommonTokenStream stream = createStream("42");
        assertThat(stream.size()).isEqualTo(2);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.NUM);
        assertThat(stream.get(0).getText()).isEqualTo("42");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.EOF);
    }

    @Test
    public void testLexerId() throws Exception {
        CommonTokenStream stream = createStream("SomeIdentifier");
        assertThat(stream.size()).isEqualTo(2);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.ID);
        assertThat(stream.get(0).getText()).isEqualTo("SomeIdentifier");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.EOF);
    }

    @Test
    public void testLexerIdArrow() throws Exception {
        CommonTokenStream stream = createStream("SomeIdentifier->");
        assertThat(stream.size()).isEqualTo(3);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.ID);
        assertThat(stream.get(0).getText()).isEqualTo("SomeIdentifier");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.KW_ARROW);
        assertThat(stream.get(1).getText()).isEqualTo("->");

        assertThat(stream.get(2).getType()).isEqualTo(ConductorParser.EOF);
    }

    @Test
    public void testLexerStr() throws Exception {
        CommonTokenStream stream = createStream("\"This is a string\"");
        assertThat(stream.size()).isEqualTo(2);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.STR);
        assertThat(stream.get(0).getText()).isEqualTo("\"This is a string\"");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.EOF);
    }

    @Test
    public void testLexerStr2() throws Exception {
        CommonTokenStream stream = createStream("\"C:\\path\\to/../file name.svg\"");
        assertThat(stream.size()).isEqualTo(2);

        assertThat(stream.get(0).getType()).isEqualTo(ConductorParser.STR);
        assertThat(stream.get(0).getText()).isEqualTo("\"C:\\path\\to/../file name.svg\"");

        assertThat(stream.get(1).getType()).isEqualTo(ConductorParser.EOF);
    }
}
