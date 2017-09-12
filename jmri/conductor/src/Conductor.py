# Ralf Automation phase 0
# vim: ai ts=4 sts=4 et sw=4 ft=python

import sys
print "Jython Path:", sys.path      # debug

# noinspection PyUnresolvedReferences
import jmri
# noinspection PyUnresolvedReferences
import jmri.jmrit.automat.AbstractAutomaton as AbstractAutomaton

# noinspection PyUnresolvedReferences
import com.alflabs.conductor.IJmriProvider as IJmriProvider
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.IJmriThrottle as IJmriThrottle
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.IJmriTurnout as IJmriTurnout
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.IJmriSensor as IJmriSensor
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.EntryPoint as ConductorEntryPoint

# noinspection PyPep8Naming
class JmriThrottleAdapter(IJmriThrottle):
    def __init__(self, address, throttle, provider):
        self._address = address
        self._throttle = throttle
        self._provider = provider
        if self._throttle is not None:
            self._throttle.setSpeedStepMode(self._throttle.SpeedStepMode128)

    def repeat(self):
        if self._address == 537:
            return range(0, 3)
        else:
            return range(0, 2)

    def getDccAddress(self):
        """In: void; Out: int address"""
        return self._address

    def setSpeed(self, speed28):
        """In: int speed; Out: void"""
        print "[Conductor", self._address, "] Speed", speed28
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        self._throttle.setIsForward(speed28 >= 0)
        absv28 = speed28
        if absv28 < 0:
            absv28 = -absv28
        # Script uses 0..28 steps; setSpeedSetting takes a float 0..1
        self._throttle.setSpeedSetting(absv28 / 28.)

    def setSound(self, on):
        """In: boolean on; Out: void"""
        print "[Conductor", self._address, "] Sound", on
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        if self._address == 537:
            for i in self.repeat():
                self._throttle.setF1(on)      # F1 true to enable sound on this LokSound decoder
        elif self._address == 204 or self._address == 209 or self._address == 10:
            self._throttle.setF8(on)          # F8 true to sound on these LokSelect decoders
        else:
            self._throttle.setF8(not on)  # F8 true to mute all others
        self._provider.waitMsec(100)

    def setLight(self, on):
        """In: boolean on; Out: void"""
        print "[Conductor", self._address, "] Light", on
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        for i in self.repeat():
            self._throttle.setF0(on)

    def horn(self):
        """In: void; Out: void"""
        print "[Conductor", self._address, "] Horn"
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        self._throttle.setF2(True)
        self._provider.waitMsec(500)
        for i in self.repeat():
            self._throttle.setF2(False)
        self._provider.waitMsec(200)

    def triggerFunction(self, function, on):
        """In: int function, boolean on; Out: void"""
        # Dynamically invoke JMRI throttle method setF0..setF28(boolean).
        Fn = "F" + str(function)
        print "[Conductor", self._address, "]", Fn, on
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        try:
            for i in self.repeat():
                getattr(self._throttle, "set" + Fn)(on)
        except AttributeError as e:
            print "[Conductor", self._address, "]", Fn, "Error:", e


# noinspection PyPep8Naming
class JmriSensorAdapter(IJmriSensor):
    def __init__(self, name, sensor):
        self._name = name
        self._sensor = sensor

    def isActive(self):
        """In: void; Out: boolean"""
        if self._sensor is None:
            print "[Conductor] No Sensor for ", self._name
            return False
        return self._sensor.knownState == jmri.Sensor.ACTIVE

    def setActive(self, active):
        """In: boolean; Out: void"""
        print "[Conductor] Ignoring SetActive for sensor ", self._name

    def toString(self):
        """In: void; Out: String"""
        return self._name + (self.isActive() and ": ON" or ": OFF")


# noinspection PyPep8Naming
class JmriTurnoutAdapter(IJmriTurnout):
    def __init__(self, name, turnout):
        self._name = name
        self._turnout = turnout

    def setTurnout(self, normal):
        """In: boolean normal; Out: void"""
        print "[Conductor Turnout", self._name, "] set to ", normal
        if self._turnout is None:
            print "[Conductor] No Turnout for ", self._name
            return False
        if normal:
            self._turnout.commandedState = jmri.Turnout.CLOSED
        else:
            self._turnout.commandedState = jmri.Turnout.THROWN


# noinspection PyPep8Naming
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
        throttle = None
        try:
            throttle = self._provider.getThrottle(dccAddress, True) #isLong
        except:
            pass
        print "[Conductor] Get Throttle", dccAddress, throttle
        return JmriThrottleAdapter(dccAddress, throttle, self)

    def getSensor(self, systemName):
        """In: String systemName; Out: IJmriSensor"""
        sensor = None
        try:
            sensor = sensors.provideSensor(systemName)
        except:
            pass
        print "[Conductor] Get Sensor", systemName, sensor
        return JmriSensorAdapter(systemName, sensor)

    def getTurnout(self, systemName):
        """In: String systemName; Out: IJmriTurnout"""
        turnout = None
        try:
            turnout = turnouts.provideTurnout(systemName)
        except:
            pass
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

