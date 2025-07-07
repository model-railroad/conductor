Program for Button Board and non-momentary contacts, non-official version. 

--------
Summary:
This is intended to reprogram an NCE Button Board's PIC16F microcontroller
to properly support non-momentary contacts (e.g. rotary switches).

More information at http://www.alfray.com/trains/nce_button_board.html

-----------
Disclaimer:
This program was NOT designed by NCE nor is it endorsed or supported
by NCE in any way.
This program is provided as-is, in a source form, in the hope it may be
helpful. However if you have no idea how to compile, assemble and program a
PIC using this program, I will NOT help nor provide any kind of support.
Trying to alter your NCE boards will likely result in losing your warranty.
Please see the license below for more info.

------------------
Mode of operation:
- At startup, wait a certain time (see constants in user.c) before sending any
  command to the NCE Switch-8.
- After the startup delay, scan the 16 inputs and send commands for all
  grounded inputs. Since this is designed to work with non-momentary
  contacts, each turnout should have a LOW input and a HIGH input.
- After that, regularly scan the 16 inputs and send a command each time
  one transitions from a HIGH to LOW state (e.g. grounded).
- A command sent to the serial output for the Switch-8 is always comprised
  of both a push event followed by a release event, regardless of the state
  of the input. The release events are thus simulated, since they do not
  occur in any timely manner with non-momentary contacts.

This has been tested and works equally well with momentary contacts.

------------------
Building: This was done using MPLAB X IDE v3.51 and a PicKit3.
The MCC add-on was used to generate the PIC config (saved in MyConfig.mc3).
The program is composed of the following files:
- mcc boilerplate files. Please edit using the MCC.
- system.h just includes the mcc main header.
- main.c is a placeholder entry point.
- user.c is the main source file with all the logic. See comments in the code.

------------------
License: MIT License. See LICENSE.txt in this project.
