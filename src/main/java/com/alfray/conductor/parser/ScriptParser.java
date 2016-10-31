package com.alfray.conductor.parser;

import com.alfray.conductor.script.IConditional;
import com.alfray.conductor.script.IFunction;
import com.alfray.conductor.script.IValue;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.script.Sensor;
import com.alfray.conductor.script.Throttle;
import com.alfray.conductor.script.Timer;
import com.alfray.conductor.script.Var;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class ScriptParser {

    private static final char[] ActionToken = new char[] { '-', '>' };
    private static final String CharTokens = ":=;!";

    private enum ThrottleCondition {
        FORWARD,
        REVERSE,
        STOPPED
    }

    private enum TimerAction {
        START,
        END
    }

    public static class Reporter {
        public void report(String line, int lineCount, String error) {
            System.out.println(String.format("Error at line %d: %s\n  Line: '%s'",
                    lineCount, error, line));
        }
    }

    public static Script parse(File filepath, Reporter reporter) throws IOException {
        try (BufferedReader reader = Files.newReader(filepath, Charsets.UTF_8)) {
            return parse(reader, reporter);
        }
    }

    public static Script parse(String source, Reporter reporter) throws IOException {
        try (StringReader sr = new StringReader(source)) {
            try (BufferedReader reader = new BufferedReader(sr)) {
                return parse(reader, reporter);
            }
        }
    }

    private static Script parse(BufferedReader reader, Reporter reporter) throws IOException {
        Script script = new Script();
        int counter = 0;
        for (;;) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            counter++;

            int comment = line.indexOf('#');
            if (comment >= 0) {
                line = line.substring(comment);
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

    private static String parseLine(Script script, String line) {
        line = line.toLowerCase(Locale.US);
        List<String> tokens = splitLine(line);
        if (tokens.isEmpty()) {
            return null;
        }

        // Initializations for var, sensor and timer
        if (tokens.size() == 4 && tokens.get(2).equals("=")) {
            String name = tokens.get(1);
            if (tokens.get(0).equals("var")) {
                Var var = new Var(Integer.parseInt(tokens.get(3)));
                if (script.getVar(name) != null) {
                    return "Var " + name + " is already defined.";
                }
                script.addVar(name, var);
                return null;
            }

            if (tokens.get(0).equals("sensor")) {
                Sensor sensor = new Sensor(tokens.get(3));
                if (script.getSensor(name) != null) {
                    return "Sensor " + name + " is already defined.";
                }
                script.addSensor(name, sensor);
                return null;
            }

            if (tokens.get(0).equals("timer")) {
                Timer timer = new Timer(Integer.parseInt(tokens.get(3)));
                if (script.getTimer(name) != null) {
                    return "Timer " + name + " is already defined.";
                }
                script.addTimer(name, timer);
                return null;
            }
        }

        // Otherwise it must be an event line with a -> keyword
        int sep = indexOf(tokens, "->");
        if (sep < 0) {
            return "Missing ->";
        }

        Script.Event event = new Script.Event();
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

    private static List<String> splitLine(String line) {
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
    private static String parseConditions(
            Script.Event event,
            Script script,
            List<String> tokens,
            int sep) {

        Throttle throttle = script.getThrottle();
        EnumMap<ThrottleCondition, IConditional> throttleCondEnumMap =
                new EnumMap<>(ThrottleCondition.class);
        throttleCondEnumMap.put(ThrottleCondition.FORWARD, throttle.createIsForward());
        throttleCondEnumMap.put(ThrottleCondition.REVERSE, throttle.createIsReverse());
        throttleCondEnumMap.put(ThrottleCondition.STOPPED, throttle.createIsStopped());

        boolean negated = false;
        for (int i = 0; i < sep; i++) {
            String token = tokens.get(i);
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
                try {
                    ThrottleCondition value = ThrottleCondition.valueOf(token.toUpperCase(Locale.US));
                    cond = throttleCondEnumMap.get(value);
                } catch (IllegalArgumentException ignore) {} // enum not found
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

    private static String parseAction(
            Script.Event event,
            Script script,
            List<String> tokens,
            int sep) {

        Throttle throttle = script.getThrottle();
        TreeMap<String, IFunction.Int> funcMap = new TreeMap<>();
        funcMap.put("forward", throttle.createFunctionForward());
        funcMap.put("reverse", throttle.createFunctionReverse());
        funcMap.put("stop", throttle.createFunctionStop());
        funcMap.put("light", throttle.createFunctionLight());
        funcMap.put("sound", throttle.createFunctionSound());
        funcMap.put("horn", throttle.createFunctionHorn());
        for (String varName : script.getVarNames()) {
            funcMap.put(varName, script.getVar(varName));
        }

        int n = tokens.size() - sep - 2; // num tokens after index i
        for (int i = sep + 1; n >= 0; ) {
            // Expected forms:
            // Function|VarId = Value [;]
            // Function [;]
            // The ; is optional for the last action.
            String i0 = tokens.get(i);

            // n is the number of tokens *after* i0; it can be zero
            // when using the "function ; " form with the last ; not present

            // First token should be followed by either = or ; or nothing
            String i1 = n < 1 ? null : tokens.get(i+1);
            if (!(i1 == null || i1.equals(";") || i1.equals("="))) {
                return String.format("Expected ; or = after %s but found %s", i0, i1);
            }

            boolean shortForm = i1 == null || i1.equals(";");

            String i2 = "0";
            if (!shortForm) {
                i2 = n < 2 ? null : tokens.get(i+2);
                String i3 = n < 3 ? null : tokens.get(i+3);

                if (!i1.equals("=") || i2 == null || !(i3 == null || i3.equals(";"))) {
                    return String.format("Expected '= value ;' after %s", i0);
                }
            }

            // i2 should be:
            // - a variable name or an integer
            // - a timer name, as a special case for timers

            IFunction.Int function = null;
            IValue.Int value = null;
            try {
                TimerAction timerAction = TimerAction.valueOf(i0.toUpperCase(Locale.US));
                Timer timer = script.getTimer(i2);
                if (timer == null) {
                    return String.format("Expected timer name after %s but found %s", i0, i2);
                }
                switch (timerAction) {
                case START:
                    function = timer.createFunctionStart();
                    break;
                case END:
                    function = timer.createFunctionEnd();
                    break;
                }
            } catch (IllegalArgumentException ignore) {} // enum not found

            if (function == null) {
                function = funcMap.get(i0);
                if (function == null) {
                    return String.format("Expected action but got %s", i0);
                }

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


    private static int indexOf(List<String> tokens, String search) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals(search)) {
                return i;
            }
        }
        return -1;
    }

    private static class LiteralInt implements IValue.Int {
        private final int mValue;

        public LiteralInt(int value) {
            mValue = value;
        }

        @Override
        public Integer getValue() {
            return mValue;
        }
    }
}
