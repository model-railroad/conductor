# Ralf Automation phase 2
# vim: ai ts=4 sts=4 et sw=4 ft=python
#
# Project: Conductor
# Copyright (C) 2017 alf.labs gmail com,
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

CONDUCTOR_VERSION = 2
CONDUCTOR_EVENTS = "events.txt"

import sys
print "Jython Path:", sys.path      # debug

# noinspection PyUnresolvedReferences
import jmri
# noinspection PyUnresolvedReferences
import jmri.jmrit.automat.AbstractAutomaton as AbstractAutomaton

# noinspection PyUnresolvedReferences
import com.alflabs.conductor.jmri.IJmriProvider as IJmriProvider
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.jmri.IJmriThrottle as IJmriThrottle
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.jmri.IJmriTurnout as IJmriTurnout
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.jmri.IJmriSensor as IJmriSensor
# noinspection PyUnresolvedReferences
import com.alflabs.conductor.EntryPoint as ConductorEntryPoint

# noinspection PyPep8Naming
class JmriThrottleAdapter(IJmriThrottle):
    def __init__(self, address, throttle, provider):
        self._address = address
        self._throttle = throttle
        self._provider = provider
        # Note that JMRI NceThrottle.java ignores setSpeedStepMode (see setSpeed below for details).
        if self._throttle is not None:
            self._throttle.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128)
        # How many times to repeat commands.
        self._repeat = range(0, 1)

    def repeat(self):
        return self._repeat

    def getDccAddress(self):
        """In: void; Out: int address"""
        return self._address

    def eStop(self):
        """In: int void; Out: void"""
        print "[Conductor", self._address, "] E-STOP"
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        # Any negative value sends an E-Stop.
        for i in self.repeat():
            self._throttle.setSpeedSetting(-1.)
            self._provider.waitMsec(250)
        self._throttle.setSpeedSetting(-1.)

    def setSpeed(self, speed28):
        """In: int speed; Out: void

        Speed is defined as -28 ... 0 ... 28. Forward is >= 0.
        The speed setting is repeated twice, with a 250 ms delay in between.
        """

        # From JMRI NceThrottle.java:
        # - The DccThrottle.setSpeedStepMode() is actually improperly used.
        # - setSpeedSetting() takes a float which is then multiplied by 126 and _then_ that value
        #   is used either with a "128 speed" or "28 speed" command, which is clearly wrong.
        # - The only sane way to set a speed with JMRI and an NceThrottle is to use the 128 speed
        #   mode _and_ not send anything more than 1.0.
        # - Negative values send an E-Stop FWD or REV command (0xA2 0x06 or 0xA2 0x05 respectively).

        # (covered in C2 event logger) # print "[Conductor", self._address, "] Speed", speed28
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        if speed28 != 0:
            self._throttle.setIsForward(speed28 >= 0)
        absv28 = speed28
        if absv28 < 0:
            absv28 = -absv28
        # Script uses 0..28 steps; setSpeedSetting takes a float 0..1
        for i in self.repeat():
            self._throttle.setSpeedSetting(absv28 / 28.)
            self._provider.waitMsec(250)
        self._throttle.setSpeedSetting(absv28 / 28.)

    def setSound(self, on):
        """In: boolean on; Out: void

        This implementation only toggles F8 has a MUTE option, which is the default for
        Tsunami DCC decoders. There is a 100 ms delay after the action.
        Conductor 2 Throttles can override onSound to change the behavior in script.
        """
        # (covered in C2 event logger) # print "[Conductor", self._address, "] Sound", on
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        self._throttle.setF8(not on)  # F8 true to mute all others
        self._provider.waitMsec(100)

    def setLight(self, on):
        """In: boolean on; Out: void

        This implementation only toggles F0, which seems fairly universal among all DCC decoders.
        There is a 100 ms delay after the action.
        Conductor 2 Throttles can override onLight to change the behavior in script.
        """
        # (covered in C2 event logger) # print "[Conductor", self._address, "] Light", on
        if self._throttle is None:
            print "[Conductor] No Throttle for ", self._address
            return
        for i in self.repeat():
            self._throttle.setF0(on)
            self._provider.waitMsec(100)

    def horn(self):
        """In: void; Out: void

        This implementation only toggles F2, which seems fairly universal among all DCC decoders.
        The horn action takes 500 ms, followed by a 200 ms delay.
        Conductor 2 Throttles can override onLight to change the behavior in script.
        """
        # (covered in C2 event logger) # print "[Conductor", self._address, "] Horn"
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
        # (covered in C2 event logger) # print "[Conductor", self._address, "]", Fn, on
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
        print "[Conductor Sensor", self._name, "] set to ", active
        if self._sensor is None:
            print "[Conductor] No Sensor for ", self._name
        elif active:
            self._sensor.knownState = jmri.Sensor.ACTIVE
        elif not active:
            self._sensor.knownState = jmri.Sensor.INACTIVE

    def toString(self):
        """In: void; Out: String"""
        return self._name + (self.isActive() and ": ON" or ": OFF")


# noinspection PyPep8Naming
class JmriTurnoutAdapter(IJmriTurnout):
    def __init__(self, name, turnout):
        self._name = name
        self._turnout = turnout

    def isNormal(self):
        """In: void; Out: boolean normal. """
        if self._turnout is None:
            print "[Conductor] No Turnout for ", self._name
            return True
        # Note: JMRI has 2 states for turnouts; Conductor always sets the "commanded state"
        # and it always reads the "known state". The latter can reflect turnout feedback.
        return self._turnout.knownState == jmri.Turnout.CLOSED

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

    def d(self, tag, msg, tr=None):
        """Overload:
            In: String tag, String msg; Out: void -- interface ILogger
            In: String tag, String msg, Throwable tr; Out: void -- interface ILogger"""
        if tr is not None:
            print tag, msg, str(tr)
        else:
            print tag, msg

    def getThrottle(self, dccAddress):
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
        if self._entry.setup(CONDUCTOR_VERSION, self._provider, CONDUCTOR_EVENTS):
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

# set the name, as an example of configuring it
a.setName("Conductor Automation")

# and show the initial panel
a.setup()

