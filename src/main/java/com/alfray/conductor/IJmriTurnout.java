package com.alfray.conductor;

/** Abstraction of a JMRI turnout. */
public interface IJmriTurnout {
    boolean NORMAL = true;
    boolean REVERSE = false;

    /** Sets the turnout position: true is normal, false is diverted/reverse. */
    void setTurnout(boolean normal);
}
