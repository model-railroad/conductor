package com.alfray.conductor;

/**
 * Abstraction of a JMRI throttle that focuses on actions.
 * <p/>
 * The script never reads the JMRI throttle state, it only sets it.
 * All the DCC quirks are completly abstracted here and it's up to the Jython adapter to
 * match the executed script. For example the speed is only an integer and there is no
 * expectation on whether this is 28 or 128 steps or something else totally custom.
 * The sound, light and horn ignore which DCC function is to be used by the throttle.
 */
public interface IJmriThrottle {
    /**
     * Sets the throttle speed and direction.
     * Positive speed is forward, negative is reverse and 0 is stopped.
     * <p/>
     * No assumption is made on 28 vs 128 steps for speed. This is left to be decided by the
     * script and the Jython implementation that must match. Note that internally JMRI uses a
     * float to (incorrectly) abstract this issue. It default to 128 steps on an NCE system.
     */
    void setSpeed(int speed);

    /**
     * Sets the sound.  <br/>
     * Most decoders default to sound on and have a mute function (e.g. F8 on Digitrax/SoundTraxx
     * or F1 on LokSound.) It's up to the Jython adapter to transform  this into the proper
     * throttle function toggle.
     */
    void setSound(boolean on);

    /**
     * Sets the light. <br/>
     * It's up to the Jython adapter to transform this into the proper throttle function toggle;
     * the NMRA spec calls for this to be F0 so that's less ambiguous than the other ones.
     */
    void setLight(boolean on);

    /**
     * Blows the horn. <br/>
     * It's up to the Jython adapter to transform this into the proper throttle function toggle;
     * on most decoder that means turning F2 on and off for a short period of time, e.g. 100 ms.
     * <p/>
     * Possible extension is to pass an integer for a number of horns or a duration. The script
     * interprets the lack of argument as a silent zero value.
     */
    void horn();
}
