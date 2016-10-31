# Ralf Automation phase 0
# vim: ai ts=4 sts=4 et sw=4 ft=python

import sys
print "Jython Path:", sys.path      # debug

import java
import javax.swing
from javax.swing import *

import jmri
import jmri.jmrit.automat.AbstractAutomaton as AbstractAutomaton

import com.alfray.conductor.IJmriProvider as IJmriProvider
import com.alfray.conductor.IJmriThrottle as IJmriThrottle
import com.alfray.conductor.IJmriSensor as IJmriSensor
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
        self._throttle.setSpeedSetting(absv28 / 28.)

    def setSound(self, on):
        """In: boolean on; Out: void"""
        print "[", self._address, "] Sound", on
        self._throttle.setF8(not on)  # F8 true to mute
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


class Automation(AbstractAutomaton):
    def __init__(self):
        print "AutomationPhase0 __init"

    # routine to show the panel, starting the whole process
    def setup(self):
        print "AutomationPhase0 SETUP"
        self._provider = JmriProvider()
        self._entry = ConductorEntryPoint()
        self._entry.setup(self._provider)

    # handle() will only execute once here, to run a single test
    def handle(self):
        # invoked after self.start ; stopped with self.stop
        print "AutomationPhase0 HANDLE"
        return self._entry.handle()

# create one of these
a = Automation()

# set the name, as a example of configuring it
a.setName("Conductor Test 0")

# and show the initial panel
a.setup()

