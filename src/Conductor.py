# Ralf Automation phase 0
# vim: ai ts=4 sts=4 et sw=4 ft=python

import sys
print "Jython Path:", sys.path      # debug

# noinspection PyUnresolvedReferences
import jmri
# noinspection PyUnresolvedReferences
import jmri.jmrit.automat.AbstractAutomaton as AbstractAutomaton

# noinspection PyUnresolvedReferences
import com.alfray.conductor.IJmriProvider as IJmriProvider
# noinspection PyUnresolvedReferences
import com.alfray.conductor.IJmriThrottle as IJmriThrottle
# noinspection PyUnresolvedReferences
import com.alfray.conductor.IJmriTurnout as IJmriTurnout
# noinspection PyUnresolvedReferences
import com.alfray.conductor.IJmriSensor as IJmriSensor
# noinspection PyUnresolvedReferences
import com.alfray.conductor.EntryPoint as ConductorEntryPoint

REPEAT = range(0, 3)

class JmriThrottleAdapter(IJmriThrottle):
    def __init__(self, address, throttle, provider):
        self._address = address
        self._throttle = throttle
        self._provider = provider
        self._throttle.setSpeedStepMode(self._throttle.SpeedStepMode128)

    def setSpeed(self, speed28):
        """In: int speed; Out: void"""
        print "[Conductor", self._address, "] Speed", speed28
        self._throttle.setIsForward(speed28 >= 0)
        absv28 = speed28
        if absv28 < 0:
            absv28 = -absv28
        # Script uses 0..28 steps; setSpeedSetting takes a float 0..1
        self._throttle.setSpeedSetting(absv28 / 28.)

    def setSound(self, on):
        """In: boolean on; Out: void"""
        print "[Conductor", self._address, "] Sound", on
        if self._address == 537:
            for i in REPEAT:
                self._throttle.setF1(on)      # F1 true to enable sound on this LokSound decoder
        elif self._address == 204 or self._address == 209:
            self._throttle.setF8(on)  # F8 true to sound
        else:
            self._throttle.setF8(not on)  # F8 true to mute all others
        self._provider.waitMsec(100)

    def setLight(self, on):
        """In: boolean on; Out: void"""
        print "[Conductor", self._address, "] Light", on
        r = [1]
        if self._address == 537:
            r = REPEAT
        for i in r:
            self._throttle.setF0(on)

    def horn(self):
        """In: void; Out: void"""
        print "[Conductor", self._address, "] Horn"
        self._throttle.setF2(True)
        self._provider.waitMsec(500)
        r = [1]
        if self._address == 537:
            r = REPEAT
        for i in r:
            self._throttle.setF2(False)
        self._provider.waitMsec(200)


class JmriSensorAdapter(IJmriSensor):
    def __init__(self, name, sensor):
        self._name = name
        self._sensor = sensor

    def isActive(self):
        """In: void; Out: boolean"""
        return self._sensor.knownState == jmri.Sensor.ACTIVE


class JmriTurnoutAdapter(IJmriTurnout):
    def __init__(self, name, turnout):
        self._name = name
        self._turnout = turnout

    def setTurnout(self, normal):
        """In: boolean normal; Out: void"""
        print "[Conductor Turnout", self._name, "] set to ", normal
        if normal:
            self._turnout.commandedState = jmri.Turnout.CLOSED
        else:
            self._turnout.commandedState = jmri.Turnout.THROWN


class JmriProvider(IJmriProvider):
    def __init__(self, provider):
        self._provider = provider

    def waitMsec(self, delayMs):
        self._provider.waitMsec(delayMs)

    def log(self, msg):
        """In: String msg; Out: void"""
        print msg

    def getThrotlle(self, dccAddress):
        """In: int dccAddress; Out: IJmriThrottle"""
        throttle = self._provider.getThrottle(dccAddress, True) #isLong
        print "[Conductor] Get Throttle", dccAddress, throttle
        return JmriThrottleAdapter(dccAddress, throttle, self)

    def getSensor(self, systemName):
        """In: String systemName; Out: IJmriSensor"""
        sensor = sensors.provideSensor(systemName)
        print "[Conductor] Get Sensor", systemName, sensor
        return JmriSensorAdapter(systemName, sensor)

    def getTurnout(self, systemName):
        """In: String systemName; Out: IJmriTurnout"""
        turnout = turnouts.provideTurnout(systemName)
        print "[Conductor] Get Turnout", systemName, turnout
        return JmriTurnoutAdapter(systemName, turnout)


class Automation(AbstractAutomaton):
    def __init__(self):
        print "[Conductor] Automation __init"

    # Setup the automation instance, call at end of script.
    # noinspection PyAttributeOutsideInit
    def setup(self):
        print "[Conductor] Automation SETUP"
        self._provider = JmriProvider(self)
        self._entry = ConductorEntryPoint()
        if self._entry.setup(self._provider, "events.txt"):
            print "[Conductor] Automation START"
            self.start()

    # handle() is called repeatedly as long as it returns true.
    # Invoked after self.start; stopped with self.stop.
    def handle(self):
        # print "Conductor Automation HANDLE"
        result = self._entry.handle()
        if not result:
            print "[Conductor] Automation STOP"
            self.stop()
        return result

# create one of these
a = Automation()

# set the name, as a example of configuring it
a.setName("Conductor Automation")

# and show the initial panel
a.setup()

