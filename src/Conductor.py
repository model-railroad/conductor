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


class JmriThrottleAdapter(IJmriThrottle):
    def __init__(self, address, throttle):
        self._address = address
        self._throttle = throttle
        self._throttle.setSpeedStepMode(self._throttle.SpeedStepMode128)

    def setSpeed(self, speed28):
        """In: int speed; Out: void"""
        print "[", self._address, "] Speed", speed28
        self._throttle.setIsForward(speed28 >= 0)
        absv28 = speed28
        if absv28 < 0:
            absv28 = -absv28
        # Script uses 0..28 steps; setSpeedSetting takes a float 0..1
        self._throttle.setSpeedSetting(absv28 / 28.)

    def setSound(self, on):
        """In: boolean on; Out: void"""
        print "[", self._address, "] Sound", on
        if self._address == 537:
            self._throttle.setF1(not on)  # F1 true to mute this LokSound decoder
        else:
            self._throttle.setF8(not on)  # F8 true to mute all others
        self._provider.waitMsec(100)

    def setLight(self, on):
        """In: boolean on; Out: void"""
        print "[", self._address, "] Light", on
        self._throttle.setF0(on)

    def horn(self):
        """In: void; Out: void"""
        print "[", self._address, "] Horn"
        self._throttle.setF2(True)
        self._provider.waitMsec(100)
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
        if normal:
            self._turnout.commandedState = jmri.Turnout.CLOSED
        else:
            self._turnout.commandedState = jmri.Turnout.THROWN


class JmriProvider(IJmriProvider):
    def __init__(self, provider):
        self._provider = provider

    def waitMsec(self, delayMs):
        self._provider.waitMsec(delayMs)

    def getThrotlle(self, dccAddress):
        """In: int dccAddress; Out: IJmriThrottle"""
        throttle = self._provider.getThrottle(dccAddress, True) #isLong
        return JmriThrottleAdapter(dccAddress, throttle)

    def getSensor(self, systemName):
        """In: String systemName; Out: IJmriSensor"""
        sensor = self._provider.provideSensor(systemName)
        return JmriSensorAdapter(systemName, sensor)

    def getTurnout(self, systemName):
        """In: String systemName; Out: IJmriTurnout"""
        turnout = self._provider.provideTurnout(systemName)
        return JmriTurnoutAdapter(systemName, turnout)


class Automation(AbstractAutomaton):
    def __init__(self):
        print "Conductor Automation __init"

    # Setup the automation instance, call at end of script.
    # noinspection PyAttributeOutsideInit
    def setup(self):
        print "Conductor Automation SETUP"
        self._provider = JmriProvider(self)
        self._entry = ConductorEntryPoint()
        if self._entry.setup(self._provider, "events.txt"):
            self.start()

    # handle() is called repeatedly as long as it returns true.
    def handle(self):
        # invoked after self.start ; stopped with self.stop
        print "Conductor Automation HANDLE"
        result = self._entry.handle()
        if not result:
            self.stop()
        return result

# create one of these
a = Automation()

# set the name, as a example of configuring it
a.setName("Conductor Test 0")

# and show the initial panel
a.setup()

