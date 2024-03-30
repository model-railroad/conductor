# Conductor

## What is it

__Conductor__ is a Java application that drives the automation of the
HO-scale model train layout at the Randall Museum in San Francisco.

It is designed to integrate with JMRI and not to run as a standalone application.

Features of Conductor:

- Conductor is a Java app that integrates with JMRI.
- It has access to the turnouts, sensors and throttles defined in JMRI.
- Conductor is controlled using an automation script with a custom
  language that I developed for that purpose.
- The script responds to sensor events and controls DCC turnouts and DCC
  engines via JMRI.
- This allows the automation to be easily modified without recompiling
  the program.
- It acts as a data server for the "RTAC" Android application to display
  the automation state on remote tablet displays.

The Conductor app is invoked from JMRI using its Jython extension bridge.
Conductor’s main role is to run a script that drives the automation.
It uses an event-based language I created for that purpose that updates
turnouts and engines in reaction to a combination of sensors and timer
inputs. Sensors can be either activation buttons or track occupancy
sensors defined in JMRI. Depending on the state of the automation and
the location of the engines, the script can change their speed, change
the lights, blow the horn, set turnouts as required. It is essentially
a fully automated DCC cab.

The Randall museum uses an NCE command station and boosters. Sensors are
detected using NCE AIU modules. Turnouts are controlled using NCE Switch-8
or Digitrax DS64 modules. Since Conductor interfaces with JMRI, it is
not specially tied to NCE hardware. It can interface with any throttle,
sensor or turnout that can be controlled via JMRI.

One key feature of the automation script is that it is both timer-based
and sensor-based. Model train motors are analog and their speed and
running is not precise enough for automation. The only way for an
automation to be reliable is to physically know the location of the
train on the track, which is typically done using block occupancy sensors.
The track is divided in blocks and electrical sensors detect when a
train enters and leaves specific blocks. These events are precise as
they are directly related to specific locations on the track that are
known and never change. Such location events are ideal to control the
speed of the train, to tell it to start, accelerate, stop, etc.
Timer events are good enough for less critical tasks like blowing the
train horns. Usually both techniques are combined, which results in
excellent control, for example a train can be set to stop after entering
a block with a delay so that it actually stops in front of a station
when taking momentum into account and can then leave after a specific time.

For ease of developing and testing, the Conductor app also has a second
custom language to simulate the automation. This simulates the progression
of the train through the layout, simulating the activation of track
occupancy sensors as if the actual engines were moving along.

## Implementation

__Conductor__ is a Java application designed to run on computers, not mobile.

It needs JMRI to get access to the block detectors or other sensors, the
DCC throttles and the DCC turnouts.

It uses __Dagger__ and __[LibUtils](https://bitbucket.org/ralfoide/libutils)' RX__ (my own implementation of RX Java).

The __Conductor__ scripting language is implemented using __ANTRL v4__.
The grammar is available in
[src/antlr/antlr/Conductor.g4](https://bitbucket.org/randall-railroad/automation/src/HEAD/jmri/conductor/src/antlr/antlr/Conductor.g4)
and here is an example of actual script as being run on the Randall layout:
[resources/v2/script.txt](https://bitbucket.org/randall-railroad/automation/src/ed80d43f218daaef0a3e18b08506b630a99da705/jmri/conductor/src/test/resources/v2/script_pa+bl_11.txt).

The __Conductor__ simulation language does not have its own grammar file yet.
The syntax is trivial enough to be handled by a
[handwritten parser](https://bitbucket.org/randall-railroad/automation/src/ed80d43f218daaef0a3e18b08506b630a99da705/jmri/conductor/src/main/java/com/alflabs/conductor/simulator/Simulator.java?#Simulator.java-118)
in a few lines of Java.
Here is an example simulation script:
[resources/v2/simul1.txt](https://bitbucket.org/randall-railroad/automation/src/ed80d43f218daaef0a3e18b08506b630a99da705/jmri/conductor/src/test/resources/v2/simul1.txt).


Communication with __RTAC__ is done using the __KV Client/Server__
available in LibUtils using regular TCP sockets over wifi.

Building the app can be done either using gradle on the command line or
by importing the project into IntelliJ IDEA Community 2017.


## Reusability and Future Improvements

The application is generic enough that it could be reused for other layouts.

For example the SVG map and the automation routes information are served
by the Conductor server.

Currently the Randall implementation features two automated routes and
a single map. The original design called for more routes and more maps.
The current state is an MVP (minimum viable product) and the goal is to
expand the features of the application as the project matures.


## Dependencies

__Conductor__ depends on one additional git submodule:

- The [LibUtils](https://bitbucket.org/ralfoide/libutils) module.


## License

__Conductor__ is licensed under the __GNU GPL v3 license__.

    Copyright (C) 2008-2017 alf.labs gmail com,

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

The full GPL license is available in the file "LICENSE-gpl-3.0.txt".


## Building

Java JDK 11 is the minimum version supported.

Before building, the git submodules must be imported:

    $ android/RTAC/_init.sh -f

This script will checkout the
[LibUtils](https://bitbucket.org/ralfoide/libutils) submodule which
is used by both __RTAC__ and __Conductor__.


To build this from the command-line:

    $ cd jmri/conductor
    $ ./gradlew test

This works under Linux or under Windows using Cygwin 64.

To build and use this from IntelliJ using the Community Edition:

- Open Existing Project located in the `jmri/conductor` directory.
- Build > Build Project at least once (to load dependencies and build the main classes).
- File > Project Structure > Project > Project SDK: JRE or JDK 1.8.
- Create Run/Debug Configurations:
  - Application > Name `DevEntryPoint2`
    - Check Single Instance Only.
    - Main Class: `com.alflabs.conductor.v2.DevEntryPoint2`.
    - Workding dir: Set to `.../automation/jmri/conductor`.
    - Use classpath of module: `-cp conductor`.
    - JRE: 11.
    - Before launch: Build.
  - Gradle > Name "All Tests"
    - Check Single Instance Only.
    - Project: `conductor` (use the project icon, not the ... button)
    - Run Tasks `test`
  - Gradle tests for individual modules.
    - For example expand `engine2k/src`.
    - Right-click on `test/kotlin` and select `Run Tests in conductor...`
  - Gradle > Name "Assemble App"
    - Check Single Instance Only.
    - Project: Conductor (use the project icon, not the ... button)
    - Tasks: assemble
  - Junit > Name "All Tests" (does not always work, use Gradle instead)
    - Check Single Instance Only.
    - Test kind: all in package.
    - Package: com.alflabs.conductor
    - Search for tests: in single module.
    - Working dir: Set to ".../automation/jmri/conductor" (or $MODULE_DIR).
    - Use classpath of module: conductor_test.
    - JRE: 11.
    - Before launch: Build.

Issue: On Windows with DPI scaling, the Java UI does not scale properly.
Solution from SO:

- For JRE 1.6, add -Dsun.java2d.dpiaware=false or -Dsun.java2d.uiScale=2.5
- For JRE 1.8, find the JRE/bin/java.exe > Properties > Compatibility > override dpi scaling. Meh.
- My solution is to just run it under Linux and avoid Windows' broken DPI scaling.


## Installation and Usage with JMRI

__Conductor__ is a plugin for JMRI. First, Java and JMRI need to be installed for your platform:

- [Java Download Page](https://www.java.com/en/download/manual.jsp).
- [JMRI Download Page](https://www.jmri.org/download/).

Next, build __Conductor__:

    $ git clone git clone https://bitbucket.org/randall-railroad/automation.git
    $ cd automation
    $ ./_init.sh -f
    $ ./gradlew assemble fatJar

Two files need to be copied into the JMRI installation directory:

    $ cp src/Conductor.py                          JMRI-DIR/jython/Conductor.py
    $ cp build/libs/conductor-0.3-SNAPSHOT-all.jar JMRI-DIR/lib/conductor.jar

where _JMRI-DIR_ is the location where JMRI was installed.
On Windows, this defaults to `C:\Program\ Files (x86)\JMRI` although if you choose this and the
default "Install for all users" mode, you will need Administrator priviledges to copy the files in
the `lib/` and `jython/` sub-folders.

Once the files are copied, open `PanelPro` and create or select an existing profile.

To perform a one-time test, use the menu `Scripting > Run Script` and select `jython/Conductor.py`.
If it works, the __Conductor__ window will open.

Tips:
- Next time `Scripting > Run Script` is used, the default file name is the last one used, in this
  case `Conductor.py`.
- Open the default log using `Help > System Console`. Any error loading the jython file is printed
  there. A typical error is forgetting to install the JAR file in the `lib/` folder.
- On Windows, the `lib/jar` file gets cached once open by JMRI. Even if you update the file
  on disk, the new version is sometimes not used till JMRI is closed and restarted.

By default, __Conductor__ tries to open a script named "events.txt" in the JMRI executable directory.
Instead use the "Load Script" button to load your own script.

For automation purposes, we want __Conductor__ to be opened automatically when the JMRI profile
gets loaded. This is achieved via the Preference panel. In JMRI:

- Open `Edit > Preferences`.
- In the preference panel, select the `Start Up` entry on the side list.
- At the bottom of the Start Up panel, select `Add > Run script`.
- Select `Conductor.py` in the `jython` directory.
- Click the `Save` button at the bottom of the preference panel.

When JMRI restarts, the __Conductor__ window will open automatically.
If you place your automation script in `JMRI/events.txt`, the script starts to run automatically.
Closing JMRI also closes __Conductor__.

~~
