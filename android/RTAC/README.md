# Randall Train Automation Controller

## What is it

"RTAC" (the "Randall Train Automation Controller") is an Android
application that displays the state of the Randall Automation software.

It works by connecting to a server hosted by the Conductor application.

Features of RTAC:
- It is a custom Android app that runs on one or more tablets.
- It is synchronized with Conductor to display the automation status
  and the track occupancy.
- It automatically finds and connects to the Conductor server using
  the Zeroconf protocol.
- It features an “Emergency Stop” which users can use to stop the
  automation, for example if a train derails. Once the Conductor software
  goes in emergency stop, either tablet can be used to reset the system
  with some clear indication on screen of what to do.

Both tablets are synchronized so that the display is the same on both
and either can be used to trigger the emergency stop or reset the
automation.

## Implementation

__RTAC__ is an Android application designed for tablets with API 19 or greater.
The XML layout files are optimized for 10 inch tablets.

It uses __Dagger__ and __[LibUtils](https://bitbucket.org/ralfoide/libutils)' RX__ (my own implementation of RX Java).

Communication with __Conductor__ is done using the __KV Client/Server__
available in LibUtils using regular TCP sockets over wifi.

The layout map is achieved by displaying SVG maps produced with InkScape
and rendered using the [AndroidSVG](https://github.com/ralfoide/androidsvg)
library.

The app works as a service (to keep the socket communication open at all
times) and can work in "kiosk" mode by replacing the Home application.
This makes it difficult (but not impossible) to dismiss the app and close it.
The app is currently designed to be used by trusted museum staff and is not
designed to be handled by untrusted public.

Testing relies on __Robolectric__ 3.1, __Mockito__, and __Truth__.

Building the app can be done either using gradle on the command line or
by importing the project into Android Studio 3.


## Reusability and Future Improvements

The application is generic enough that it could be reused for other layouts.

For example the SVG map and the automation routes information are served
by the Conductor server.

Currently the Randall implementation features two automated routes and
a single map. The original design called for more routes and more maps.
The current state is an MVP (minimum viable product) and the goal is to
expand the features of the application as the project matures.


## Dependencies

__RTAC__ depends on 2 additional git submodules:
- The [LibUtils](https://bitbucket.org/ralfoide/libutils) module.
- A fork of the [AndroidSVG](https://github.com/ralfoide/androidsvg) project.


## Building

Before building, the git submodules must be imported:

    $ android/RTAC/_init.sh

This script will checkout the
[LibUtils](https://bitbucket.org/ralfoide/libutils) submodule which
is used by both __RTAC__ and __Conductor__.


To build this from the command-line:

    $ cd android/RTAC
    $ ./gradlew assemble

This works under Linux or under Windows using Cygwin 64.

## License

__RTAC__ is licensed under the __GNU GPL v3 license__.

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
