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

import com.alflabs.conductor.parser2.ConductorBaseListener;
import com.alflabs.conductor.parser2.ConductorLexer;
import com.alflabs.conductor.parser2.ConductorParser;
import com.alflabs.conductor.script.AnalyticEventAction;
import com.alflabs.conductor.script.AnalyticPageAction;
import com.alflabs.conductor.script.EnumFactory;
import com.alflabs.conductor.script.Enum_;
import com.alflabs.conductor.script.Event;
import com.alflabs.conductor.script.IConditional;
import com.alflabs.conductor.script.IIntFunction;
import com.alflabs.conductor.script.IIntValue;
import com.alflabs.conductor.script.IStringFunction;
import com.alflabs.conductor.script.IStringValue;
import com.alflabs.conductor.script.IntAction;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Sensor;
import com.alflabs.conductor.script.SensorFactory;
import com.alflabs.conductor.script.StringAction;
import com.alflabs.conductor.script.Throttle;
import com.alflabs.conductor.script.ThrottleFactory;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.TimerFactory;
import com.alflabs.conductor.script.Turnout;
import com.alflabs.conductor.script.TurnoutFactory;
import com.alflabs.conductor.script.Var;
import com.alflabs.conductor.script.VarFactory;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.Prefix;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.utils.FileOps;
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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Parses a script and fills a {@link Script}.
 */
public class ScriptParser2 {

    private final ThrottleFactory mThrottleFactory;
    private final TurnoutFactory mTurnoutFactory;
    private final SensorFactory mSensorFactory;
    private final TimerFactory mTimerFactory;
    private final EnumFactory mEnumFactory;
    private final VarFactory mVarFactory;
    private final Analytics mAnalytics;
    private final FileOps mFileOps;
    private final Script mScript;
    private final Reporter mReporter;
    private File mScriptDir;

    @Inject
    public ScriptParser2(
            Reporter reporter,
            Script script,
            ThrottleFactory throttleFactory,
            TurnoutFactory turnoutFactory,
            SensorFactory sensorFactory,
            TimerFactory timerFactory,
            EnumFactory enumFactory,
            VarFactory varFactory,
            Analytics analytics,
            FileOps fileOps) {
        mReporter = reporter;
        mScript = script;
        mThrottleFactory = throttleFactory;
        mTurnoutFactory = turnoutFactory;
        mSensorFactory = sensorFactory;
        mTimerFactory = timerFactory;
        mEnumFactory = enumFactory;
        mVarFactory = varFactory;
        mAnalytics = analytics;
        mFileOps = fileOps;
    }

    /**
     * Parses a script file.
     *
     * @param filepath The path of the file to be parsed.
     * @return A new {@link Script}.
     * @throws IOException if the file is not found or can't be read from.
     */
    public Script parse(File filepath) throws IOException {
        mScriptDir = filepath.getParentFile();
        String source = Files.toString(filepath, Charsets.UTF_8);
        return parse(source);
    }

    /**
     * Parses a script file.
     *
     * @param source   The content of the file to be parsed.
     * @return A new {@link Script}.
     * @throws IOException if the file is not found or can't be read from.
     */
    public Script parse(String source) throws IOException {
        LineCounter lineCounter = new LineCounter(source);
        CaseInsensitiveInputStream input = new CaseInsensitiveInputStream(source);
        ConductorLexer lexer = new ConductorLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ConductorParser parser = new ConductorParser(tokenStream);

        ReporterErrorListener errorListener = new ReporterErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ConductorParser.ScriptContext tree = parser.script();  // parse a full script
        ConductorListenerImpl listener = new ConductorListenerImpl(lineCounter);
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return mScript;
    }

    private class ConductorListenerImpl extends ConductorBaseListener {
        private final LineCounter mLineCounter;
        private Event mEvent;

        ConductorListenerImpl(LineCounter lineCounter) {
            mLineCounter = lineCounter;
        }

        @Override
        public void exitDefIdLine(ConductorParser.DefIdLineContext ctx) {
            // Note: we don't need to log errors, that would have been done already by the parser.
            // The tree walker will call every node even if it had parsing errors so we just need
            // to be defensive and give up early.
            if (ctx.defIdType() == null || ctx.ID().size() != 2) {
                return;
            }
            String varName  = ctx.ID(0).getText();
            String jmriName = ctx.ID(1).getText();

            if (mScript.isExistingName(varName)) {
                emitError(ctx, "Name '" + varName + "' is already defined.");
                return;
            }

            if (ctx.defIdType().KW_SENSOR() != null) {
                Sensor sensor = mSensorFactory.create(jmriName, varName.toLowerCase(Locale.US));
                mScript.addSensor(varName, sensor);

            } else if (ctx.defIdType().KW_TURNOUT() != null) {
                Turnout turnout = mTurnoutFactory.create(jmriName, varName.toLowerCase(Locale.US));
                mScript.addTurnout(varName, turnout);

            } else {
                String type = ctx.defIdType().getText().toLowerCase(Locale.US);
                emitError(ctx, "Unsupported type '" + type + "'.");
            }
        }

        @Override
        public void exitDefStrLine(ConductorParser.DefStrLineContext ctx) {
            if (ctx.defStrType() == null || ctx.ID() == null ||
                    (ctx.STR() == null && ctx.STR_BLOCK() == null)) {
                return;
            }
            String varName  = ctx.ID().getText();

            if (mScript.isExistingName(varName)) {
                emitError(ctx, "Name '" + varName + "' is already defined.");
                return;
            }

            String value;

            if (ctx.STR() != null) {
                value = ctx.STR().getText();

                // Remove start/end quotes from the string
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
            } else {
                value = ctx.STR_BLOCK().getText();

                // Remove start/end quotes from the string
                if (value.startsWith("'''") && value.endsWith("'''")) {
                    value = value.substring(3, value.length() - 3);
                }
            }

            if (ctx.defStrType().KW_STRING() != null) {
                Var var = mVarFactory.create(value, varName.toLowerCase(Locale.US));
                var.setExported(ctx.defStrType().KW_EXPORT() != null);
                mScript.addVar(varName, var);

            } else if (ctx.defStrType().KW_MAP() != null) {
                // Load the map SVG file
                File svgFile = mScriptDir == null ? new File(value) : new File(mScriptDir, value);
                if (!mFileOps.isFile(svgFile)) {
                    emitError(ctx, "Map '" + varName + "' has Invalid SVG file path: '" + svgFile.toString() + "'");
                    return;
                }

                String svg;
                try {
                    svg = mFileOps.toString(svgFile, Charsets.UTF_8);
                } catch (IOException e) {
                    emitError(ctx, "Map '" + varName + "', Failed to read SVG file '" + svgFile.toString() + "', Exception: " + e);
                    return;
                }

                mScript.addMap(varName, new MapInfo(varName, svg));

            } else {
                String type = ctx.defStrType().getText().toLowerCase(Locale.US);
                emitError(ctx, "Unsupported type '" + type + "'.");

            }
        }

        @Override
        public void exitDefIntLine(ConductorParser.DefIntLineContext ctx) {
            if (ctx.defIntType() == null || ctx.ID() == null || ctx.NUM() == null) {
                return;
            }
            String varName  = ctx.ID().getText();
            String num = ctx.NUM().getText();
            int value;
            try {
                value = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                emitError(ctx, "Expected integer but found '" + num + "'.");
                return;
            }

            if (mScript.isExistingName(varName)) {
                emitError(ctx, "Name '" + varName + "' is already defined.");
                return;
            }

            if (ctx.defIntType().KW_INT() != null) {
                Var var = mVarFactory.create(value, varName.toLowerCase(Locale.US));
                var.setExported(ctx.defIntType().KW_EXPORT() != null);
                mScript.addVar(varName, var);

            } else if (ctx.defIntType().KW_TIMER() != null) {
                Timer timer = mTimerFactory.create(value);
                mScript.addTimer(varName, timer);

            } else {
                String type = ctx.defIntType().getText().toLowerCase(Locale.US);
                emitError(ctx, "Unsupported type '" + type + "'.");
            }
        }

        @Override
        public void exitDefThrottleLine(ConductorParser.DefThrottleLineContext ctx) {
            if (ctx.ID() == null) {
                return;
            }
            String varName  = ctx.ID().getText();
            List<TerminalNode> nums = ctx.NUM();

            if (mScript.isExistingName(varName)) {
                emitError(ctx, "Name '" + varName + "' is already defined.");
                return;
            }

            List<Integer> values = new ArrayList<>(nums.size());
            for (TerminalNode num : nums) {
                try {
                    int value = Integer.parseInt(num.getText());
                    values.add(value);
                } catch (NumberFormatException e) {
                    emitError(ctx, "Expected integer but found '" + num + "'.");
                    return;
                }
            }

            Throttle throttle = mThrottleFactory.create(values);
            mScript.addThrottle(varName, throttle);
        }

        @Override
        public void exitDefEnumLine(ConductorParser.DefEnumLineContext ctx) {
            if (ctx.ID() == null) {
                return;
            }
            String varName  = ctx.ID().getText();
            List<TerminalNode> ids = ctx.defEnumValues().ID();

            if (mScript.isExistingName(varName)) {
                emitError(ctx, "Name '" + varName + "' is already defined.");
                return;
            }

            List<String> values = new ArrayList<>(ids.size());
            for (TerminalNode id : ids) {
                values.add(id.getText().toLowerCase(Locale.US));
            }

            Enum_ enum_ = mEnumFactory.create(values, varName.toLowerCase(Locale.US));
            enum_.setExported(ctx.KW_EXPORT() != null);
            mScript.addEnum(varName, enum_);
        }

        public void exitDefRouteLine(ConductorParser.DefRouteLineContext ctx) {
            if (ctx.ID() == null) {
                return;
            }

            String routeName = ctx.ID().getText();

            final String TOGGLE = "toggle";
            final String STATUS = "status";
            final String COUNTER = "counter";
            final String THROTTLE = "throttle";
            Map<String, String> arguments = new TreeMap<>();
            arguments.put(TOGGLE, null);
            arguments.put(STATUS, null);
            arguments.put(COUNTER, null);
            arguments.put(THROTTLE, null);

            for (ConductorParser.RouteInfoContext routeCtx : ctx.routeInfoList().routeInfo()) {
                String key = null;
                String value = null;
                if (routeCtx.routeInfoOp() != null) {
                    key = routeCtx.routeInfoOp().getText();
                    value = routeCtx.ID().getText();
                }
                if (key == null || value == null) {
                    emitError(ctx, "Route '" + routeName + "': Unexpected error parsing " + routeCtx.getText());
                    return;
                }
                key = key.toLowerCase(Locale.US);
                if (!arguments.containsKey(key)) {
                    emitError(ctx, "Route '" + routeName + "': Unknown argument '" + key + "'. Expected: " +
                            Arrays.toString(arguments.keySet().toArray()));
                    return;
                }
                if (arguments.get(key) != null) {
                    emitError(ctx, "Route '" + routeName + "': Argument '" + key + "' is already defined.");
                    return;
                }

                String value2 = null;
                if (key.equals(THROTTLE)) {
                    // The throttle is a specific case: in the definition we want the DCC address
                    // of the throttle, not its script name. For a multi-throttle, the first
                    // address is used (since what matters is the speed and by definition all
                    // entries in a multi throttle have the same speed).
                    Throttle throttle = mScript.getThrottle(value);
                    if (throttle != null) {
                        value2 = Prefix.DccThrottle + Integer.toString(throttle.getDccAddresses().get(0));
                    }
                } else {
                    value2 = mScript.getKVKeyNameForId(value);
                }

                if (value2 == null) {
                    emitError(ctx, "Route '" + routeName + "': Id '" + value + "' for argument '" + key + "' is not defined.");
                    return;
                }

                Var var = mScript.getVar(value);
                if (var != null) {
                    var.setExported(true);
                }
                Enum_ enum_ = mScript.getEnum(value);
                if (enum_ != null) {
                    enum_.setExported(true);
                }

                arguments.put(key, value2);
            }

            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    emitError(ctx, "Route '" + routeName + "': Argument '" + entry.getKey() + "' is not defined");
                    return;
                }
            }

            if (mScript.isExistingName(routeName)) {
                emitError(ctx, "Name '" + routeName + "' is already defined.");
                return;
            }

            mScript.addRoute(routeName, new RouteInfo(
                    routeName,
                    arguments.get(TOGGLE),
                    arguments.get(STATUS),
                    arguments.get(COUNTER),
                    arguments.get(THROTTLE)));
        }

        @Override
        public void enterEventLine(ConductorParser.EventLineContext ctx) {
            mEvent = new Event(mScript.getLogger(), getLine(ctx));
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

            IConditional cond = null;

            if (ctx.condEnum() != null) {
                String op = ctx.condEnum().condEnumOp().getText(); // == or !=
                Enum_ enum_ = mScript.getEnum(id);
                if (enum_ == null) {
                    emitError(ctx, "Expected Enum ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                String rhs = ctx.condEnum().ID().getText().toLowerCase(Locale.US);
                if (!enum_.getValues().contains(rhs)) {
                    emitError(ctx, "Invalid value '" + rhs + "' for enum '" + id + "'. Expected: "
                            + Arrays.toString(enum_.getValues().toArray()));
                }

                cond = enum_.createCondition(op, rhs);

            } else if (ctx.condThrottleOp() != null) {
                String op = ctx.condThrottleOp().getText();
                Throttle throttle = mScript.getThrottle(id);
                if (throttle == null) {
                    emitError(ctx, "Expected throttle ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                Throttle.Condition condition = Throttle.Condition.valueOf(op.toUpperCase(Locale.US));
                cond = throttle.createCondition(condition);

            } else {
                cond = mScript.getConditional(id);
            }

            if (cond == null) {
                emitError(ctx, "Unknown event condition '" + id + "'.");
                return;
            }

            if (ctx.condTime() != null) {
                // Creates a timer that starts on the given (negated) condition.
                // Returns the new timer condition itself.
                cond = createDelayedConditional(
                        ctx.getText(),
                        cond,
                        negated,
                        Integer.parseInt(ctx.condTime().NUM().getText()));
                // The timer conditional is never negated.
                negated = false;
            }

            mEvent.addConditional(cond, negated);
        }

        @Override
        public void exitIdAction(ConductorParser.IdActionContext ctx) {
            if (ctx.ID() == null) {
                return;
            }
            String id = ctx.ID().getText();

            // Parse simplified case for enums
            Enum_ enum_ = mScript.getEnum(id);
            ConductorParser.FuncIntContext funcInt = ctx.funcInt();
            ConductorParser.FuncValueContext funcValue = ctx.funcValue();
            if (enum_ != null && funcInt != null) {
                emitError(ctx, "Invalid integer function after enum '" + id + "'. Expected '='");
                return;
            }
            if (enum_ != null && funcValue != null) {
                TerminalNode node = funcValue.ID();
                IStringValue stringValue = null;
                String value = node == null ? null : node.getText().toLowerCase(Locale.US);

                if (value != null) {
                    if (enum_.getValues().contains(value)) {
                        stringValue = new LiteralString(value);
                    } else {
                        stringValue = mScript.getEnum(value);
                    }
                }

                if (stringValue != null) {
                    String extra = ctx.throttleOp() == null ? null : ctx.throttleOp().getText();
                    extra = extra != null ? extra : (ctx.timerOp() == null ? null : ctx.timerOp().getText());
                    if (extra != null) {
                        emitError(ctx, "Unexpected extra after '" + id + " = " + value + "': " + extra);
                        return;
                    }

                    mEvent.addAction(StringAction.create(enum_, stringValue));
                    return;
                }

                if (node == null) {
                    node = funcValue.NUM();
                    value = node == null ? null : node.getText();
                }
                emitError(ctx, "Invalid value '" + value + "' for enum '" + id + "'. Expected: " +
                        Arrays.toString(enum_.getValues().toArray()));
                return;
            }

            // Parse optional value
            IIntValue intValue = null;
            IIntFunction intFunction = null;
            IStringValue strValue = null;

            if (funcValue != null) {
                TerminalNode node = funcValue.NUM();
                if (node != null) {
                    intValue = new LiteralInt(Integer.parseInt(node.getText()));
                }

                if (node == null) {
                    node = funcValue.ID();
                    if (node != null) {
                        intValue = mScript.getVar(node.getText());
                    }
                }

                if (node == null) {
                    node = funcValue.STR();
                    if (node != null) {
                        String value = node.getText();
                        // Remove start/end quotes from the string
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }

                        strValue = new LiteralString(value);
                    }
                }

                if (intValue == null && strValue == null) {
                    String text = node != null ? node.getText() : funcValue.getText();
                    emitError(ctx, "Expected NUM or ID or \"STR\" argument for '" + id + "' but found '" + text + "'.");
                    return;
                }
            } else if (funcInt != null) {
                TerminalNode node = funcInt.NUM();
                if (node != null) {
                    intValue = new LiteralInt(Integer.parseInt(node.getText()));
                } else {
                    node = funcInt.ID();
                    if (node != null) {
                        intValue = mScript.getVar(node.getText());
                    }
                }
                if (intValue == null) {
                    String text = node != null ? node.getText() : funcValue.getText();
                    emitError(ctx, "Expected NUM or ID argument for '" + id + "' but found '" + text + "'.");
                    return;
                }

                Var var = mScript.getVar(id);
                if (var != null) {
                    if (funcInt.KW_INC() != null) {
                        intFunction = var.createIncFunction();
                    } else if (funcInt.KW_DEC() != null) {
                        intFunction = var.createDecFunction();
                    } else {
                        emitError(ctx, "Unknown int operation. Expected += or -=.");
                        return;
                    }
                }

                if (intFunction == null) {
                    emitError(ctx, "Expected var ID but found '" + id + "'.");
                    return;
                }
            }
            if (intValue == null && strValue == null) {
                intValue = new LiteralInt(0);
            }

            // Parse optional operation, which gives a specific function if valid.

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
                        intFunction = throttle.createFunction(opfn);
                    } else {
                        intFunction = throttle.createFnFunction(fnIndex);
                    }
                }
            }

            if (intFunction == null && (isKwReverse || ctx.turnoutOp() != null)) {
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

                intFunction = turnout.createFunction(fn);
            }

            if (intFunction == null && ctx.timerOp() != null) {
                String op = ctx.timerOp().getText();
                Timer timer = mScript.getTimer(id);

                Timer.Function fn = Timer.Function.valueOf(op.toUpperCase(Locale.US));

                if (timer == null) {
                    emitError(ctx, "Expected timer ID for '" + op + "' but found '" + id + "'.");
                    return;
                }

                intFunction = timer.createFunction(fn);
            }

            if (intFunction == null && strValue != null) {
                // It must be a variable setter with the syntax "var = \"value\"".
                Var var = mScript.getVar(id);
                IStringFunction strFunction = var == null ? null : var.createSetStrFunction();

                if (strFunction == null) {
                    emitError(ctx, "Expected var ID but found '" + id + "'.");
                    return;
                }

                mEvent.addAction(StringAction.create(strFunction, strValue));
                return;
            }

            if (intFunction == null) {
                // If it's not an op or a throttle/turnout/timer, it must be a variable in the
                // syntax of "var = value".
                Var var = mScript.getVar(id);
                intFunction = var == null ? null : var.createSetIntFunction();

                if (intFunction == null) {
                    emitError(ctx, "Expected var ID but found '" + id + "'.");
                    return;
                }
            }

            mEvent.addAction(IntAction.create(intFunction, intValue));
        }

        @Override
        public void exitFnAction(ConductorParser.FnActionContext ctx) {
            if (ctx.KW_RESET() != null && ctx.KW_TIMERS() != null) {
                mEvent.addAction(
                        IntAction.create(mScript.getResetTimersFunction(), new LiteralInt(0)));
            } else {
                emitError(ctx, "Unexpected call.");
            }
        }

        @Override
        public void exitGaAction(ConductorParser.GaActionContext ctx) {
            ConductorParser.GaActionOpContext op = ctx.gaActionOp();
            boolean isEvent = op.KW_GA_EVENT() != null;
            boolean isPageView = op.KW_GA_PAGE() != null;
            String prefix = op.getText();
            Map<String, String> arguments = new TreeMap<>();

            if (isEvent) {
                arguments.put(AnalyticEventAction.CATEGORY, null);
                arguments.put(AnalyticEventAction.ACTION, null);
                arguments.put(AnalyticEventAction.LABEL, null);
                arguments.put(AnalyticEventAction.USER, null);
            } else if (isPageView) {
                arguments.put(AnalyticPageAction.URL, null);
                arguments.put(AnalyticPageAction.PATH, null);
                arguments.put(AnalyticPageAction.USER, null);
            }

            for (ConductorParser.GaParamContext paramCtx : ctx.gaParamList().gaParam()) {
                String key = null;
                String value = null;

                if (paramCtx.gaParamOp() != null) {
                    key = paramCtx.gaParamOp().getText();
                    TerminalNode terminal = paramCtx.ID();
                    if (terminal == null) {
                        terminal = paramCtx.KW_START();
                    }
                    if (terminal == null) {
                        terminal = paramCtx.KW_STOP();
                    }
                    value = terminal == null ? null : terminal.getText();
                }

                if (key == null || value == null) {
                    emitError(ctx, prefix + ": Unexpected error parsing " + paramCtx.getText());
                    return;
                }
                key = key.toLowerCase(Locale.US);
                if (!arguments.containsKey(key)) {
                    emitError(ctx, prefix + ": Unknown argument '" + key + "'. Expected: " +
                            Arrays.toString(arguments.keySet().toArray()));
                    return;
                }
                if (arguments.get(key) != null) {
                    emitError(ctx, prefix + ": Argument '" + key + "' is already defined.");
                    return;
                }

                arguments.put(key, value);
            }

            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    emitError(ctx, prefix + ": Argument '" + entry.getKey() + "' is not defined");
                    return;
                }
            }

            AnalyticEventAction.ValueResolver varResolver = varName -> {
                Var var = mScript.getVar(varName);
                return var == null ? varName : var.get();
            };
            AnalyticEventAction.ValueResolver labelResolver = varName -> {
                Enum_ enum_ = mScript.getEnum(varName);
                return enum_ == null ? varName : enum_.get();
            };

            if (isEvent) {
                mEvent.addAction(new AnalyticEventAction(
                        mAnalytics,
                        arguments,
                        // Resolver when a label is an Enum name
                        labelResolver,
                        // Resolver when a user is an Int Counter name
                        varResolver
                ));
            } else if (isPageView) {
                mEvent.addAction(new AnalyticPageAction(
                        mAnalytics,
                        arguments,
                        // Resolver when URL is a String var
                        varResolver,
                        // Resolver when a label is an Enum name
                        labelResolver,
                        // Resolver when a user is an Int Counter name
                        varResolver
                ));
            }
        }

        @Override
        public void exitDefGaIdLine(ConductorParser.DefGaIdLineContext ctx) {
            String idOrFile = ctx.STR().getText();
            try {
                mAnalytics.setTrackingId(idOrFile);
            } catch (IOException e) {
                emitError(ctx, "GA-Tracking-Id: Failed to read '" + idOrFile + "', Exception: " + e);
            }
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

        private String getLine(ParserRuleContext ctx) {
            return getLine(ctx.getStart());
        }

        private String getLine(Token token) {
            return mLineCounter.getLine(token.getLine()).trim();
        }

        /**
         * Creates a timer that starts on the given (negated) condition. <br/>
         * Returns the new timer condition itself.
         * <p/>
         * "name" is the ANTLR4 text representation of the full condition without spaces
         * e.g. "!My-Var+2". Convert it to "$~my-var$2$", which the script or test
         * can then reference. The syntax is deemed part of the API.
         * Both '~' and '$' are valid ID characters in a script.
         * <pre>
         * Example:
         *   ! My-var + 2 -> actions...
         * Internally this creates:
         *   Timer $~my-var$2$ = 2
         *   !my-var        -> $~my-var$2$ Start
         *   $~my-var$2$    -> actions...
         * </pre>
         * Note that the created timer is never a negated condition.
         * The syntax "!My-Var+2" is equivalent to "(!My-Var) + 2".
         */
        private IConditional createDelayedConditional(
                String name,
                IConditional cond,
                boolean negated,
                int delaySeconds) {
            String timerName = name.replace('!', '~').replace('+', '$');
            timerName = "$" + timerName + "$";
            timerName = timerName.toLowerCase(Locale.US);

            Timer timer = mScript.getTimer(timerName);
            if (timer == null) {
                timer = mTimerFactory.create(delaySeconds);
                mScript.addTimer(timerName, timer);

                // create an event that will trigger the timer
                Event triggerEvent = new Event(mScript.getLogger(), timerName + " trigger");
                triggerEvent.addConditional(cond, negated);
                triggerEvent.addAction(IntAction.create(
                        timer.createFunction(Timer.Function.START),
                        new LiteralInt(0)));
                mScript.addEvent(triggerEvent);

                // create an event that clears the timer
                Event endEvent = new Event(mScript.getLogger(), timerName + " end");
                endEvent.addConditional(timer, false);
                endEvent.addAction(IntAction.create(
                        timer.createFunction(Timer.Function.END),
                        new LiteralInt(0)));
                mScript.addEvent(endEvent);
            }

            return timer;
        }
    }

    private class ReporterErrorListener extends BaseErrorListener {

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
