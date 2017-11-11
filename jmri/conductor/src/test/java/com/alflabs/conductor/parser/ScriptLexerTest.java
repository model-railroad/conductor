/*
 * Project: Conductor
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
