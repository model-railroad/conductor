# Ralf Automation phase 0
# vim: ai ts=4 sts=4 et sw=4 ft=python

import jmri
import sys
print sys.path      # debug

import java
import javax.swing
from javax.swing import *

import com.alfray.conductor.IJmriProvider as IJmriProvider
import com.alfray.conductor.IJmriThrottle as IJmriThrottle
import com.alfray.conductor.EntryPoint as ConductorEntryPoint


class JmriThrottleAdapter(IJmriThrottle):
    def __init__(self):
        pass

    def setSpeed(self, speed):
        """In: int speed; Out: void"""
        print "setSpeed", speed

    def setSound(self, on):
        """In: boolean on; Out: void"""
        print "setSound", speed

    def setLight(self, on):
        """In: boolean on; Out: void"""
        print "setLight", speed


class MyJmriProvider(IJmriProvider):
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

