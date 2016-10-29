# Ralf Automation phase 0
# vim: ai ts=4 sts=4 et sw=4 ft=python

import jmri
import sys
print sys.path      # debug

import java
import javax.swing
from javax.swing import *

import com.alfray.conductor.JmriProvider as JmriProvider
import com.alfray.conductor.ThrottleAdapter as ThrottleAdapter
import com.alfray.conductor.EntryPoint as ConductorEntryPoint


class MyThrottleAdapter(ThrottleAdapter):
    def __init__(self):
        self._speed = 0

    def setDccAddress(self, dccAddress):
        """In: int dccAddress; Out: void"""
        print "setDccAddress", dccAddress

    def setSpeed(self, speed):
        """In: int speed; Out: void"""
        self._speed = speed
        print "setSpeed", speed

    def getSpeed(self):
        """In: void; Out: int"""
        print "getSpeed", self._speed
        return self._speed



class MyJmriProvider(JmriProvider):
    def getThrotlle(self, dccAddress):
        """In: int dccAddress; Out: ThrottleAdapter"""
        return MyThrottleAdapter()


class AutomationPhase0(jmri.jmrit.automat.AbstractAutomaton):
    def __init__(self):
        print "AutomationPhase0 __init"

    # routine to show the panel, starting the whole process
    def setup(self):
        print "AutomationPhase0 SETUP"
        self._provider = MyJmriProvider()
        self._entry = ConductorEntryPoint()
        self._entry.setup(self._provider)

    # handle() will only execute once here, to run a single test
    def handle(self):
        # invoked after self.start ; stopped with self.stop
        print "AutomationPhase0 HANDLE"
        return True

# create one of these
a = AutomationPhase0()

# set the name, as a example of configuring it
a.setName("Conductor Test 0")

# and show the initial panel
a.setup()

