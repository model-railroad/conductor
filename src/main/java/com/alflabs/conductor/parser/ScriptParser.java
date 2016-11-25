package com.alflabs.conductor.parser;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Parses a script and produces a new {@link Script}.
 */
public class ScriptParser {

    /** The "->" symbol. */
    private static final char[] ActionToken = new char[] { '-', '>' };
    /** Possible keyword delimiters, excluding the -> symbol and whitespace. */
    private static final String CharTokens = ":=;!";

    /**
     * Possible keywords for a throttle action.
     * Must match IIntFunction in the {@link Throttle} implementation.
     */
    private enum ThrottleCondition {
        FORWARD,
        REVERSE,
        STOPPED
    }

    /**
     * Possible keywords for a timer action.
     * Must match IIntFunction in the {@link Timer} implementation.
     */
    private enum TimerAction {
        START,
        END
    }

    /**
     * Possible keywords for a turnout action.
     * Must match IIntFunction in the {@link Turnout} implementation.
     */
    private enum TurnoutAction {
        NORMAL,
        REVERSE
    }

    /** Helper to create a timer, used to be overridden in tests. */
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
        try (BufferedReader reader = Files.newReader(filepath, Charsets.UTF_8)) {
            return parse(reader, reporter);
        }
    }

    /**
     * Parses a script file.
     *
     *
     * @param source The content of the file to be parsed.
     * @param reporter A non-null reporter to report errors.
     * @return A new {@link Script}.
     * @throws IOException if the file is not found or can't be read from.
     */
    public Script parse(String source, Reporter reporter) throws IOException {
        try (StringReader sr = new StringReader(source)) {
            try (BufferedReader reader = new BufferedReader(sr)) {
                return parse(reader, reporter);
            }
        }
    }

    private Script parse(BufferedReader reader, Reporter reporter) throws IOException {
        Script script = new Script(reporter);
        int counter = 0;
        for (;;) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            counter++;

            int comment = line.indexOf('#');
            if (comment >= 0) {
                line = line.substring(0, comment);
            }

            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            String error = parseLine(script, line);
            if (error != null) {
                reporter.report(line, counter, error);
            }
        }
        return script;
    }

    private String parseLine(Script script, String line) {
        List<String> tokens = splitLine(line);
        if (tokens.isEmpty()) {
            return null;
        }

        // Initializations for throttle, var, sensor and timer
        if (tokens.size() == 4 && tokens.get(2).equals("=")) {
            String type = tokens.get(0).toLowerCase(Locale.US);
            String name = tokens.get(1);

            if (script.isExistingName(name)) {
                return "Name " + name + " is already defined.";
            }

            if (type.equals("throttle")) {
                Throttle throttle = new Throttle(Integer.parseInt(tokens.get(3)));
                script.addThrottle(name, throttle);
                return null;
            }

            if (type.equals("var")) {
                Var var = new Var(Integer.parseInt(tokens.get(3)));
                script.addVar(name, var);
                return null;
            }

            if (type.equals("sensor")) {
                Sensor sensor = new Sensor(tokens.get(3));
                script.addSensor(name, sensor);
                return null;
            }

            if (type.equals("turnout")) {
                Turnout turnout = new Turnout(tokens.get(3));
                script.addTurnout(name, turnout);
                return null;
            }

            if (type.equals("timer")) {
                int durationSec = Integer.parseInt(tokens.get(3));
                Timer timer = createTimer(durationSec, script);
                script.addTimer(name, timer);
                return null;
            }
        }

        // Otherwise it must be an event line with a -> keyword
        int sep = indexOf(tokens, "->");
        if (sep < 0) {
            return "Missing ->";
        }

        Script.Event event = new Script.Event(script.getLogger(), line);
        String error = parseConditions(event, script, tokens, sep);
        if (error != null) {
            return error;
        }

        error = parseAction(event, script, tokens, sep);
        if (error != null) {
            return error;
        }

        script.addEvent(event);
        return null;
    }

    private List<String> splitLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        int length = line.length();
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) { continue; }

            // Special -> token
            if (c == ActionToken[0] && (i+1) < length && line.charAt(i+1) == ActionToken[1]) {
                tokens.add(line.substring(i, i+2));
                i++;
                continue;
            }

            // Special characters are full tokens
            if (CharTokens.indexOf(c) != -1) {
                tokens.add(Character.toString(c));
                continue;
            }

            // Otherwise it's an identifier or keyword or literal
            sb.setLength(0);
            for(;;) {
                sb.append(c);
                int i1 = i + 1;
                if (i1 >= length) {
                    break;
                } else {
                    char c1 = line.charAt(i1);
                    if (Character.isWhitespace(c1)
                            || CharTokens.indexOf(c1) != -1
                            || (c1 == ActionToken[0] && (i1+1) < length && line.charAt(i1+1) == ActionToken[1])) {
                        break;
                    } else {
                        c = c1;
                        i = i1;
                    }
                }
            }
            tokens.add(sb.toString());
        }

        return tokens;
    }

    // Parse conditions : ([ cond | ! cond] +? ...)
    private String parseConditions(
            Script.Event event,
            Script script,
            List<String> tokens,
            int sep) {

        TreeMap<String, IConditional> throttleMap = new TreeMap<>();
        for (String name : script.getThrottleNames()) {
            name = name.toLowerCase(Locale.US);
            Throttle throttle = script.getThrottle(name);
            throttleMap.put(
                    name + "!" + ThrottleCondition.FORWARD.toString().toLowerCase(Locale.US),
                    throttle.createIsForward());
            throttleMap.put(
                    name + "!" + ThrottleCondition.REVERSE.toString().toLowerCase(Locale.US),
                    throttle.createIsReverse());
            throttleMap.put(
                    name + "!" + ThrottleCondition.STOPPED.toString().toLowerCase(Locale.US),
                    throttle.createIsStopped());
        }


        boolean negated = false;
        for (int i = 0; i < sep; i++) {
            String token = tokens.get(i).toLowerCase(Locale.US);
            if (token.equals("+")) {
                // no-op, skip
                continue;
            }

            if (token.equals("!")) {
                negated = true;
                continue;
            }

            // Might be an id: var, timer, sensor
            IConditional cond = script.getConditional(token);
            // Might be a throttle state
            if (cond == null) {
                Throttle throttle = script.getThrottle(token);
                if (throttle != null) {
                    i++;
                    if (i >= sep) {
                        return String.format("Expected throttle condition after %s", token);
                    }
                    String condName = tokens.get(i).toLowerCase(Locale.US);
                    cond = throttleMap.get(token + "!" + condName);
                    if (cond == null) {
                        return String.format("Expected throttle condition after %s but found %s",
                                token, condName);
                    }
                }
            }
            if (cond != null) {
                event.addConditional(cond, negated);
            } else {
                return "Unknown event condition " + token;
            }
            negated = false;
        }

        return null;
    }

    private String parseAction(
            Script.Event event,
            Script script,
            List<String> tokens,
            int sep) {

        TreeMap<String, IIntFunction> throttleMap = new TreeMap<>();
        for (String name : script.getThrottleNames()) {
            Throttle throttle = script.getThrottle(name);
            throttleMap.put(name + "!forward", throttle.createFunctionForward());
            throttleMap.put(name + "!reverse", throttle.createFunctionReverse());
            throttleMap.put(name + "!stop", throttle.createFunctionStop());
            throttleMap.put(name + "!light", throttle.createFunctionLight());
            throttleMap.put(name + "!sound", throttle.createFunctionSound());
            throttleMap.put(name + "!horn", throttle.createFunctionHorn());
        }

        TreeMap<String, IIntFunction> turnoutMap = new TreeMap<>();
        for (String name : script.getTurnoutNames()) {
            Turnout turnout = script.getTurnout(name);
            turnoutMap.put(name + "!normal", turnout.createFunctionNormal());
            turnoutMap.put(name + "!reverse", turnout.createFunctionReverse());
        }

        int n = tokens.size() - sep - 1; // num tokens including index i
        for (int i = sep + 1; n > 0; ) {
            // Expected forms:
            // [Throttle] Function|VarId = Value|Timer|TurnoutAction [;]
            // [Throttle] Function [;]
            // The ; is optional for the last action.
            String i0 = tokens.get(i).toLowerCase(Locale.US);

            IIntFunction function = null;
            IIntValue value = null;

            Throttle throttle = script.getThrottle(i0);

            if (throttle != null) {
                // Throttles have 2 leading words (throttle name + action) before =
                // so capture the second action word.
                i++; n--;
                if (n <= 0) {
                    return String.format("Expected throttle action after %s", i0);
                }
                String i1 = tokens.get(i).toLowerCase(Locale.US);

                function = throttleMap.get(i0 + "!" + i1);
                if (function == null) {
                    return String.format("Expected throttle action after %s but found %s", i0, i1);
                }

                i0 = i1;
            }

            // Initial token should be followed by either = or ; or nothing
            String i1 = n <= 1 ? null : tokens.get(i+1);
            if (!(i1 == null || i1.equals(";") || i1.equals("="))) {
                return String.format("Expected ; or = after %s but found %s", i0, i1);
            }

            boolean shortForm = i1 == null || i1.equals(";");

            String i2 = "0";
            if (!shortForm) {
                i2 = n <= 2 ? null : tokens.get(i+2);
                String i3 = n <= 3 ? null : tokens.get(i+3);

                if (!i1.equals("=") || i2 == null || !(i3 == null || i3.equals(";"))) {
                    return String.format("Expected '= value ;' after %s", i0);
                }
            }

            // i2 should be:
            // - a throttle action (already found)
            // - a variable name or an integer
            // - a timer name, as a special case for timers
            // - a turnout action
            boolean isTimer = false;
            if (function == null) {
                try {
                    TimerAction timerAction = TimerAction.valueOf(i0.toUpperCase(Locale.US));
                    Timer timer = script.getTimer(i2);
                    if (timer == null) {
                        return String.format("Expected timer name after %s but found %s", i0, i2);
                    }
                    switch (timerAction) {
                    case START:
                        function = timer.createFunctionStart();
                        isTimer = true;
                        break;
                    case END:
                        function = timer.createFunctionEnd();
                        isTimer = true;
                        break;
                    }
                } catch (IllegalArgumentException ignore) {
                } // enum not found
            }

            boolean isTurnout = false;
            if (function == null) {
                try {
                    TurnoutAction turnoutAction = TurnoutAction.valueOf(i2.toUpperCase(Locale.US));
                    Turnout turnout = script.getTurnout(i0);
                    if (turnout == null) {
                        return String.format("Expected turnout name before %s but found %s", i2, i0);
                    }
                    switch (turnoutAction) {
                    case NORMAL:
                        function = turnout.createFunctionNormal();
                        isTurnout = true;
                        break;
                    case REVERSE:
                        function = turnout.createFunctionReverse();
                        isTurnout = true;
                        break;
                    }
                } catch (IllegalArgumentException ignore) {
                } // enum not found
            }

            if (function == null) {
                // Function is not a timer or a throttle, must be a variable name
                function = script.getVar(i0);
            }

            if (function == null) {
                return String.format("Expected timer, turnout, throttle, or variable but found %s", i0);
            }

            if (!isTurnout && !isTimer && !shortForm) {
                // Value is either a variable or a literal number
                value = script.getVar(i2);
                if (value == null) {
                    try {
                        value = new LiteralInt(Integer.parseInt(i2));
                    } catch (NumberFormatException ignore) {
                        return String.format("Expected number or variable but got %s", i2);
                    }
                }
            }

            event.addAction(function, value == null ? new LiteralInt(0) : value);

            int used = shortForm ? 2 : 4;
            i += used;
            n -= used;
        }
        return null;
    }


    private int indexOf(List<String> tokens, String search) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals(search)) {
                return i;
            }
        }
        return -1;
    }

    private static class LiteralInt implements IIntValue {
        private final int mValue;

        public LiteralInt(int value) {
            mValue = value;
        }

        @Override
        public int getAsInt() {
            return mValue;
        }
    }
}
