/**
 * 
 * 
 * Note for PIC I/O ports:
 * - Write to LATch
 * - Read from PORT.
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
// any defined here. The default used is 2 seconds.

// Number & Time in milliseconds to wait at startup to let the Switch-8 initialize.
#define DELAY_Nx_START 8
#define DELAY_MS_START 1000

// Number & Time in milliseconds to wait after sending a serial command to let
// the Switch-8 throw the turnout.
#define DELAY_Nx_SWITCH 3
#define DELAY_MS_SWITCH 1000

#define NUM_REPEAT_CMD 3

// Time in milliseconds before scanning next input.
// This crude rate-limiter makes the sketch scan each input roughly once per second.
#define DELAY_MS_LOOP 62

// Blink the LED for 100 ms.
#define DELAY_MS_BLINK 100

// Time in milliseconds to wait before sending the button-released command to the Switch-8.
#define DELAY_MS_BTN_RELEASE 250

// Number of inputs scanned (8 turnouts, Normal/Release each).
#define MAX_INPUT 16

// Enable this to get human-readable UART output
#undef DEBUG

// Last known state of each input.
uint8_t states[MAX_INPUT];

// Blinks the on-board LED.
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
        sendByte(0x80 + index);
        __delay_ms(DELAY_MS_BTN_RELEASE);
        sendByte(0x40 + index);
        __delay_ms(DELAY_MS_BTN_RELEASE);
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
void checkInput(uint8_t index) {
    uint8_t state = readInput(index);

    if (state != states[index]) {
        // State has changed.
        states[index] = state;

        if (state == 0) {
            // Inputs are active low (when grounded).
            // For any turnout rotary switch, one of the inputs is LOW and the other one is HIGH.
            blink();
            sendSwitch8Command(index);
            sleepAfterSwitch();
        }
    }
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
    
    // Send a dummy character to start the UART output when debugging
    EUSART_Write('\n');

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
        CLRWDT();
    }
}

void LoopApp(void) {
    while (1) {
        for (uint8_t i = 0; i < MAX_INPUT; ++i) {
            checkInput(i);
            __delay_ms(DELAY_MS_LOOP);
            CLRWDT();
        }
    }
}
