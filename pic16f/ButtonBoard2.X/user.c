/**
 * Program for Button Board and non-momentary contacts, non-official version.
 *
 * --------
 * Summary:
 * This is intended to reprogram an NCE Button Board's PIC16F microcontroller
 * to properly support non-momentary contacts (e.g. rotary switches).
 *
 * More information at http://ralf.alfray.com/trains/nce_button_board.html
 *
 * -----------
 * Disclaimer:
 * This program was NOT designed by NCE nor is it endorsed or supported
 * by NCE in any way.
 * This program is provided as-is, in a source form, in the hope it may be
 * helpful. However if you have no idea how to compile, assemble and program a
 * PIC using this program, I will NOT help nor provide any kind of support.
 * Trying to alter your NCE boards will likely result in losing your warranty.
 * Please see the license below for more info.
 *
 * ------------------
 * Mode of operation:
 * - At startup, wait a certain time (see constants below) before sending any
 *   command to the NCE Switch-8.
 * - After the startup delay, scan the 16 inputs and send commands for all
 *   grounded inputs. Since this is designed to work with non-momentary
 *   contacts, each turnout should have a LOW input and a HIGH input.
 * - After that, regularly scan the 16 inputs and send a command each time
 *   one transitions from a HIGH to LOW state (e.g. grounded).
 * - A command sent to the serial output for the Switch-8 is always comprised
 *   of both a push event followed by a release event, regardless of the state
 *   of the input. The release events are thus simulated, since they do not
 *   occur in any timely manner with non-momentary contacts.
 *
 * This has been tested and works equally well with momentary contacts.
 *
 * ------------------
 * PIC Configuration:
 * - Configuration is done using the MCC. Do not edit mcc.c directly.
 * - Lowest frequency usable is 500 kHz for a reasonable 9600 baud rate.
 * - WDT is activated, as well as STVREN, and both are probably overkill here.
 * - PWRT MUST be disabled. Enabling it would make sense but somehow
 *   prevents the proper detection of the serial link by the Switch-8.
 * - Detection of the serial data by the Switch-8 seems oddly finicky. It
 *   seems to work better when the Switch-8 is powered after the Button Board,
 *   which is the reverse of the current hardware design.
 *
 * -----------------------
 * Note for PIC I/O ports:
 * - Write to LATch
 * - Read from PORT.
 *
 * ---------------------
 * License: MIT License.
 *
 * Copyright 2017, Raphael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
#if defined(__XC)
    #include <xc.h>         /* XC8 General Include File */
#elif defined(HI_TECH_C)
    #include <htc.h>        /* HiTech General Include File */
#endif

#include <stdint.h>         /* For uint8_t definition */
#include <stdbool.h>        /* For true/false definition */

#include "user.h"
#include "mcc_generated_files/mcc.h"

// Note: if WDT is enabled, the WDTPS must be set to a larger timeout than
// any defined here. The default used is 2 seconds, which is why some
// timeouts have a count multiplier and clear WDT at every iteration.

// Number & Time in milliseconds to wait at startup to let the Switch-8
// initialize before sending the initial turnout states.
#define DELAY_Nx_START 6
#define DELAY_MS_START 1000

// Number & Time in milliseconds to wait after sending a serial command to let
// the Switch-8 throw the turnout.
#define DELAY_Nx_SWITCH 2
#define DELAY_MS_SWITCH 500

// Number of times to repeat the serial command
#define NUM_REPEAT_CMD 1

// Time in milliseconds before scanning next input.
#define DELAY_MS_LOOP 10

// Time in milliseconds to blink the LED.
#define DELAY_MS_BLINK 100

// Time in milliseconds to wait before sending the button-released command.
#define DELAY_MS_BTN_RELEASE (500 - 2 * DELAY_MS_BLINK)

// How many input check cycles to count before blinking for the IDLE state.
#define IDLE_BLINK_COUNT 32

// Number of inputs scanned (8 turnouts, Normal/Release each, 16 inputs total).
// This value MUST not be larger than 16 without modifiy readInput().
#define MAX_INPUT 16

// Enable this to get human-readable UART output
#undef DEBUG

// Last known state of each input.
uint8_t states[MAX_INPUT];

// Blinks the on-board LED.
// This method takes 2 x DELAY_MS_BLINK time.
void blink() {
    IO_LED_SetLow();
    __delay_ms(DELAY_MS_BLINK);
    IO_LED_SetHigh();
    __delay_ms(DELAY_MS_BLINK);
    CLRWDT();
}

void sendByte(uint8_t value) {
#ifdef DEBUG
    EUSART_Write('0' + (value >> 4));
    uint8_t a = value & 0xF;
    EUSART_Write(a < 10 ? ('0' + a) : ('A' + a - 10));
    EUSART_Write('\n');
#else
    EUSART_Write(value);
    EUSART_Write(0xFF ^ value);
#endif
    CLRWDT();
}

void sendSwitch8Command(uint8_t index) {
    for (uint8_t i = 0; i < NUM_REPEAT_CMD; ++i) {
        blink();
        sendByte(0x80 + index);
        __delay_ms(DELAY_MS_BTN_RELEASE);
        blink();
        sendByte(0x40 + index);
    }
}

uint8_t readInput(uint8_t index) {
    if (index < 6) {
        // Input Turnouts 1..3 N/R are on RA0..RA5
        unsigned char mask = (1 << index);
        return (PORTA & mask) != 0;
    } else if (index == 6) {
        // Inputs Turnout 4 N/R are on RA7..RA6 in reverse order.
        return IO_RA7_PORT != 0;
    } else if (index == 7) {
        // Inputs Turnout 4 N/R are on RA7..RA6 in reverse order.
        return IO_RA6_PORT != 0;
    } else if (index < 14) {
        // Inputs Turnouts 5..7 N/R are on RB0..RB5
        unsigned char mask = (1 << (index - 8));
        return (PORTB & mask) != 0;
    } else if (index == 14) {
        // Inputs Turnout 8 N/R are on RC1..RC0 in reverse order.
        return IO_RC1_PORT != 0;
    } else if (index == 15) {
        // Inputs Turnout 8 N/R are on RC1..RC0 in reverse order.
        return IO_RC0_PORT != 0;
    }
    return 0;
}

void sleepAfterSwitch() {
    for (uint8_t i = 0; i < DELAY_Nx_SWITCH; ++i) {
        __delay_ms(DELAY_MS_SWITCH);
        CLRWDT();
    }
}

// Checks an input and sends a command to the Switch-8 if the input
// has changed since last read.
uint8_t checkInput(uint8_t index) {
    uint8_t state = readInput(index);

    if (state != states[index]) {
        // State has changed.
        states[index] = state;

        if (state == 0) {
            // Inputs are active low (when grounded).
            // For any turnout rotary switch, one of the inputs is LOW and
            // the other one is HIGH.
            sendSwitch8Command(index);
            sleepAfterSwitch();
            return 1;
        }
    }
    return 0;
}

void initialSleep() {
    for (uint8_t i = 0; i < DELAY_Nx_START; ++i) {
        blink();
        __delay_ms(DELAY_MS_START);
        CLRWDT();
    }
}

void InitApp(void) {
    // Initialize using the MCC generated code
    SYSTEM_Initialize();

#ifdef DEBUG
    // Send a dummy character to start the UART output when debugging
    EUSART_Write('\n');
#endif

    // Sleep a few seconds to give time to the Switch-8 to start
    initialSleep();

    for (uint8_t i = 0; i < MAX_INPUT; ++i) {
        // Set each memorized "previous" state to high. Since the states
        // are active LOW, this will force the initialization code to send
        // the current turnout state to the Switch-8 during setup.
        states[i] = 1;
        // Checks the input and updates the Switch-8 to match the rotary
        // switches at startup.
        blink();
        checkInput(i);
        __delay_ms(DELAY_MS_LOOP);
        CLRWDT();
    }
}

void LoopApp(void) {
    uint8_t counter = 0;
    while (1) {
        for (uint8_t i = 0; i < MAX_INPUT; ++i) {
            if (checkInput(i)) {
                counter = 0;
            }
            __delay_ms(DELAY_MS_LOOP);
            CLRWDT();
        }
        if (++counter == IDLE_BLINK_COUNT) {
            blink();
            counter = 0;
        }
    }
}
