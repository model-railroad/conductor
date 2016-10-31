package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class Script {

    private final Throttle mThrottle = new Throttle();
    private final TreeMap<String, Var> mVars = new TreeMap<>();
    private final TreeMap<String, Sensor> mSensors = new TreeMap<>();
    private final TreeMap<String, Timer> mTimers = new TreeMap<>();
    private final List<Event> mEvents = new ArrayList<>();

    public Script() {}

    public void addVar(String name, Var var) {
        mVars.put(name.toLowerCase(Locale.US), var);
    }

    public void addSensor(String name, Sensor sensor) {
        mSensors.put(name.toLowerCase(Locale.US), sensor);
    }

    public void addTimer(String name, Timer timer) {
        mTimers.put(name.toLowerCase(Locale.US), timer);
    }

    public void addEvent(Event event) {
        mEvents.add(event);
    }

    public Var getVar(String name) {
        return mVars.get(name.toLowerCase(Locale.US));
    }

    public Sensor getSensor(String name) {
        return mSensors.get(name.toLowerCase(Locale.US));
    }

    public Timer getTimer(String name) {
        return mTimers.get(name.toLowerCase(Locale.US));
    }

    public Throttle getThrottle() {
        return mThrottle;
    }

    public IConditional getConditional(String name) {
        name = name.toLowerCase(Locale.US);
        if (mVars.containsKey(name)) {
            return mVars.get(name);

        } else if (mSensors.containsKey(name)) {
            return mSensors.get(name);

        } else if (mTimers.containsKey(name)) {
            return mTimers.get(name);
        }

        return null;
    }

    public ArrayList<String> getVarNames() {
        return new ArrayList<>(mVars.keySet());
    }

    public boolean setup(IJmriProvider provider) {
        Var dccVar = mVars.get("dcc");
        if (dccVar == null) {
            System.out.println("Script Error: DCC variable is not defined");
            return false;
        }
        int dccAddress = dccVar.getValue();

        mThrottle.init(provider, dccAddress);

        for (Sensor sensor : mSensors.values()) {
            sensor.init(provider);
        }

        return true;
    }

    public void handle() {
        for (Event event : mEvents) {
            if (event.evalConditions()) {
                event.performActions();
            }
        }
    }

    private static class Action {
        private final IFunction.Int mFunction;
        private final IValue.Int mValue;

        public Action(IFunction.Int function, IValue.Int value) {
            mFunction = function;
            mValue = value;
        }

        public void execute() {
            mFunction.setValue(mValue.getValue());
        }
    }

    private static class Cond {
        private final IConditional mConditional;
        private final boolean mNegated;

        public Cond(IConditional conditional, boolean negated) {
            mConditional = conditional;
            mNegated = negated;
        }

        public boolean eval() {
            boolean status = mConditional.isActive();
            if (mNegated) {
                status = !status;
            }
            return status;
        }
    }

    public static class Event {
        private final List<Cond> mConditions = new ArrayList<>();
        private final List<Action> mActions = new ArrayList<>();

        public void addConditional(IConditional condition, boolean negated) {
            mConditions.add(new Cond(condition, negated));
        }

        public void addAction(IFunction.Int function, IValue.Int value) {
            mActions.add(new Action(function, value));
        }

        public boolean evalConditions() {
            if (mConditions.isEmpty()) {
                return false;
            }

            for (Cond condition : mConditions) {
                if (!condition.eval()) {
                    return false;
                }
            }

            return true;
        }

        public void performActions() {
            for (Action action : mActions) {
                try {
                    action.execute();

                } catch (Exception e) {
                    System.out.println("Action failed [" + action + "]: " + e);
                }
            }
        }
    }
}
