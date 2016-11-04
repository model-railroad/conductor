package com.alfray.conductor;

/** Abstraction of a JMRI turnout. */
public interface IJmriTurnout {
    public static final boolean NORMAL = true;
    public static final boolean REVERSE = false;

    /** Sets the turnout position: true is normal, false is diverted/reverse. */
    boolean setTurnout(boolean normal);
}
