package com.alflabs.conductor.parser;

import com.alflabs.conductor.parser2.ConductorBaseListener;
import com.alflabs.conductor.parser2.ConductorLexer;
import com.alflabs.conductor.parser2.ConductorParser;
import com.alflabs.conductor.script.IConditional;
import com.alflabs.conductor.script.IIntFunction;
import com.alflabs.conductor.script.IIntValue;
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

import java.io.File;
import java.io.IOException;
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

        ReporterErrorListener errorListener = new ReporterErrorListener(reporter);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ConductorParser.ScriptContext tree = parser.script();  // parse a full script
        ConductorListenerImpl listener = new ConductorListenerImpl(script, reporter);
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return script;
    }

    private class ConductorListenerImpl extends ConductorBaseListener {

        private final Script mScript;
        private final Reporter mReporter;
        private Script.Event mEvent;

        ConductorListenerImpl(Script script, Reporter reporter) {
            mScript = script;
            mReporter = reporter;
        }

        @Override
        public void exitDefStrLine(ConductorParser.DefStrLineContext ctx) {
            // Note: we don't need to log errors, that would have been done already by the parser.
            // The tree walker will call every node even if it had parsing errors so we just need
            // to be defensive and give up early.
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
        public void enterEventLine(ConductorParser.EventLineContext ctx) {
            mEvent = new Script.Event(mScript.getLogger(), getLine(ctx));
        }

        @Override
        public void exitEventLine(ConductorParser.EventLineContext ctx) {
            mScript.addEvent(mEvent);
            mEvent = null;
        }

        @Override
        public void exitCond(ConductorParser.CondContext ctx) {
            if (ctx.ID() == null) {
                return;
            }
            boolean negated = ctx.condNot() != null;
            String id = ctx.ID().getText();

            if (ctx.condThrottleOp() != null) {
                String op = ctx.condThrottleOp().getText();
                Throttle throttle = mScript.getThrottle(id);
                if (throttle == null) {
                    emitError(ctx, "Expected throttle ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                Throttle.Condition condition = Throttle.Condition.valueOf(op.toUpperCase(Locale.US));
                IConditional cond = throttle.createCondition(condition);
                mEvent.addConditional(cond, negated);
                return;
            }

            if (ctx.condTime() != null) {
                // TODO support event + time
            }

            IConditional cond = mScript.getConditional(id);
            if (cond != null) {
                mEvent.addConditional(cond, negated);
            } else {
                emitError(ctx, "Unknown event condition '" + id + "'.");
            }
        }

        @Override
        public void exitAction(ConductorParser.ActionContext ctx) {
            if (ctx.ID() == null) {
                return;
            }
            String id = ctx.ID().getText();

            // Parse optional value
            IIntValue value = null;
            if (ctx.funcValue() != null) {
                TerminalNode node = ctx.funcValue().NUM();
                if (node != null) {
                    value = new LiteralInt(Integer.parseInt(node.getText()));
                } else {
                    node = ctx.funcValue().ID();
                    if (node != null) {
                        value = mScript.getVar(node.getText());
                    }
                }
                if (value == null) {
                    String text = node != null ? node.getText() : ctx.funcValue().getText();
                    emitError(ctx, "Expected NUM or ID argument for '" + id + "' but found '" + text + "'.");
                    return;
                }
            }
            if (value == null) {
                value = new LiteralInt(0);
            }

            // Parse optional operation, which gives a specific function if valid.
            IIntFunction function = null;

            // Note: KW_REVERSE is used for both throttle op and turnout op.
            // When present, a throttle op gets evaluated first even though the id might be turnout.
            boolean isKwReverse = false;

            if (ctx.throttleOp() != null) {
                Throttle throttle = mScript.getThrottle(id);

                String op = ctx.throttleOp().getText();
                int fnIndex = 0;
                Throttle.Function opfn = null;
                if (ctx.throttleOp().KW_FN() != null) {
                    fnIndex = Integer.parseInt(op.substring(1));    // Skip "f" before index
                } else {
                    opfn = Throttle.Function.valueOf(op.toUpperCase(Locale.US));
                    isKwReverse = opfn == Throttle.Function.REVERSE;
                }

                if (throttle == null && !isKwReverse) {
                    emitError(ctx, "Expected throttle ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                if (throttle != null) {
                    if (opfn != null) {
                        function = throttle.createFunction(opfn);
                    } else {
                        function = throttle.createFnFunction(fnIndex);
                    }
                }
            }

            if (function == null && (isKwReverse || ctx.turnoutOp() != null)) {
                String op = isKwReverse ? null : ctx.turnoutOp().getText();
                Turnout turnout = mScript.getTurnout(id);

                Turnout.Function fn = isKwReverse ?
                        Turnout.Function.REVERSE :
                        Turnout.Function.valueOf(op.toUpperCase(Locale.US));

                if (turnout == null) {
                    if (isKwReverse) {
                        emitError(ctx, "Expected throttle or turnout ID for '" + op + "' but found '" + id + "'.");
                    } else {
                        emitError(ctx, "Expected turnout ID for '" + op + "' but found '" + id + "'.");
                    }
                    return;
                }

                function = turnout.createFunction(fn);
            }

            if (function == null && ctx.timerOp() != null) {
                String op = ctx.timerOp().getText();
                Timer timer = mScript.getTimer(id);

                Timer.Function fn = Timer.Function.valueOf(op.toUpperCase(Locale.US));

                if (timer == null) {
                    emitError(ctx, "Expected timer ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                function = timer.createFunction(fn);
            }

            if (function == null) {
                // If it's not an op for a throttle/turnout/timer, it must be a variable in the
                // syntax of "var = value".
                function = mScript.getVar(id);

                if (function == null) {
                    emitError(ctx, "Expected var ID but found '" + id + "'.");
                    return;
                }
            }

            mEvent.addAction(function, value);
        }

        @Override
        public void visitErrorNode(ErrorNode node) {
            int lineCount = node.getSymbol().getLine();
            if (mReporter.getLastReportLine() != lineCount) {
                // Since this is a generic error, don't report if we have already
                // something for the same line. This error isn't useful, the previous
                // one is probably much better.
                mReporter.report(getLine(node.getSymbol()), lineCount, "Unexpected symbol: '" + node.getText() + "'.");
            }
        }

        private void emitError(ParserRuleContext ctx, String message) {
            mReporter.report(getLine(ctx), ctx.getStart().getLine(), message);
        }

        /**
         * Hacky incorrect way to get the line.
         * This only works when parsing rules and not when visiting an error node (which doesn't
         * have a parse context).
         * Different approach is map line numbers to the source input stream directly.
         */
        private String getLine(ParserRuleContext ctx) {
            // TODO this work but doesn't recreate whitespace. Fix later.
            /*
            while (ctx != null && !(ctx instanceof ConductorParser.ScriptLineContext)) {
                ctx = ctx.getParent();
            }
            if (ctx != null) {
                return ctx.getText();
            }
            */
            return "";
        }

        private String getLine(Token token) {
            // TODO use token.getLine();
            return "";
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
