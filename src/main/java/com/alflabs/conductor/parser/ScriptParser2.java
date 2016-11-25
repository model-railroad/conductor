package com.alflabs.conductor.parser;

import com.alflabs.conductor.parser2.ConductorBaseListener;
import com.alflabs.conductor.parser2.ConductorLexer;
import com.alflabs.conductor.parser2.ConductorListener;
import com.alflabs.conductor.parser2.ConductorParser;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Sensor;
import com.alflabs.conductor.script.Throttle;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.Turnout;
import com.alflabs.conductor.script.Var;
import com.alflabs.conductor.util.NowProvider;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

/**
 * Parses a script and produces a new {@link Script}.
 */
public class ScriptParser2 {

    /**
     * Helper to create a timer, used to be overridden in tests.
     */
    Timer createTimer(int durationSec, NowProvider nowProvider) {
        return new Timer(durationSec, nowProvider);
    }

    /**
     * Parses a script file.
     *
     * @param filepath The path of the file to be parsed.
     * @param reporter A non-null reporter to report errors.
     * @return A new {@link Script}.
     * @throws IOException if the file is not found or can't be read from.
     */
    public Script parse(File filepath, Reporter reporter) throws IOException {
        String source = Files.toString(filepath, Charsets.UTF_8);
        return parse(source, reporter);
    }

    /**
     * Parses a script file.
     *
     * @param source   The content of the file to be parsed.
     * @param reporter A non-null reporter to report errors.
     * @return A new {@link Script}.
     * @throws IOException if the file is not found or can't be read from.
     */
    public Script parse(String source, Reporter reporter) throws IOException {
        Script script = new Script(reporter);

        CaseInsensitiveInputStream input = new CaseInsensitiveInputStream(source);
        ConductorLexer lexer = new ConductorLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        ConductorParser parser = new ConductorParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ReporterErrorListener(reporter));

        ConductorParser.ScriptContext tree = parser.script();  // parse a full script
        ConductorListenerImpl listener = new ConductorListenerImpl(script, reporter);
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return script;
    }

    private class ConductorListenerImpl extends ConductorBaseListener {

        private final Script mScript;
        private final Reporter mReporter;

        ConductorListenerImpl(Script script, Reporter reporter) {
            mScript = script;
            mReporter = reporter;
        }


        @Override
        public void exitDefStrLine(ConductorParser.DefStrLineContext ctx) {
            if (ctx.defStrType() == null || ctx.ID().size() != 2) {
                return;
            }
            String type = ctx.defStrType().getText().toLowerCase(Locale.US);
            String name  = ctx.ID(0).getText();
            String value = ctx.ID(1).getText();

            if (mScript.isExistingName(name)) {
                emitError(ctx, "Name '" + name + "' is already defined.");
                return;
            }

            switch (type) {
            case "sensor":
                Sensor sensor = new Sensor(value);
                mScript.addSensor(name, sensor);
                break;
            case "turnout":
                Turnout turnout = new Turnout(value);
                mScript.addTurnout(name, turnout);
                break;
            default:
                emitError(ctx, "Unsupported type '" + type + "'.");
                break;
            }
        }

        @Override
        public void exitDefIntLine(ConductorParser.DefIntLineContext ctx) {
            if (ctx.defIntType() == null || ctx.ID() == null || ctx.NUM() == null) {
                return;
            }
            String type = ctx.defIntType().getText().toLowerCase(Locale.US);
            String name  = ctx.ID().getText();
            String num = ctx.NUM().getText();
            int value;
            try {
                value = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                emitError(ctx, "Expected integer but found '" + num + "'.");
                return;
            }

            if (mScript.isExistingName(name)) {
                emitError(ctx, "Name '" + name + "' is already defined.");
                return;
            }

            switch (type) {
            case "throttle":
                Throttle throttle = new Throttle(value);
                mScript.addThrottle(name, throttle);
                break;
            case "var":
                Var var = new Var(value);
                mScript.addVar(name, var);
                break;
            case "timer":
                Timer timer = createTimer(value, mScript);
                mScript.addTimer(name, timer);
                break;
            default:
                emitError(ctx, "Unsupported type '" + type + "'.");
                break;
            }
        }

        @Override
        public void visitErrorNode(ErrorNode node) {
            int lineCount = node.getSymbol().getLine();
            if (mReporter.getLastReportLine() != lineCount) {
                // Since this is a generic error, don't report if we have already
                // something for the same line. This error isn't useful, the previous
                // one is probably much better.
                mReporter.report(node.getText(), lineCount, "Unexpected error.");
            }
        }

        private void emitError(ParserRuleContext ctx, String message) {
            mReporter.report("", ctx.getStart().getLine(), message);
        }
    }

    private static class ReporterErrorListener extends BaseErrorListener {
        private final Reporter mReporter;

        public ReporterErrorListener(Reporter reporter) {
            mReporter = reporter;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            if (!msg.equals(".")) {
                msg += ".";
            }
            mReporter.report("", line, msg);
        }
    }
}
