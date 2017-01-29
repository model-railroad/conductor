/**
  @Generated Pin Manager Header File

  @Company:
    Microchip Technology Inc.

  @File Name:
    pin_manager.h

  @Summary:
    This is the Pin Manager file generated using MPLAB(c) Code Configurator

  @Description:
    This header file provides implementations for pin APIs for all pins selected in the GUI.
    Generation Information :
        Product Revision  :  MPLAB(c) Code Configurator - 4.15
        Device            :  PIC16F1936
        Version           :  1.01
    The generated drivers are tested against the following:
        Compiler          :  XC8 1.35
        MPLAB             :  MPLAB X 3.40

    Copyright (c) 2013 - 2015 released Microchip Technology Inc.  All rights reserved.

    Microchip licenses to you the right to use, modify, copy and distribute
    Software only when embedded on a Microchip microcontroller or digital signal
    controller that is integrated into your product or third party product
    (pursuant to the sublicense terms in the accompanying license agreement).

    You should refer to the license agreement accompanying this Software for
    additional information regarding your rights and obligations.

    SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
    EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
    MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
    IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
    CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR
    OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
    INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR
    CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF
    SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
    (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.

*/


#ifndef PIN_MANAGER_H
#define PIN_MANAGER_H

#define INPUT   1
#define OUTPUT  0

#define HIGH    1
#define LOW     0

#define ANALOG      1
#define DIGITAL     0

#define PULL_UP_ENABLED      1
#define PULL_UP_DISABLED     0

// get/set IO_T1N aliases
#define IO_T1N_TRIS               TRISAbits.TRISA0
#define IO_T1N_LAT                LATAbits.LATA0
#define IO_T1N_PORT               PORTAbits.RA0
#define IO_T1N_ANS                ANSELAbits.ANSA0
#define IO_T1N_SetHigh()            do { LATAbits.LATA0 = 1; } while(0)
#define IO_T1N_SetLow()             do { LATAbits.LATA0 = 0; } while(0)
#define IO_T1N_Toggle()             do { LATAbits.LATA0 = ~LATAbits.LATA0; } while(0)
#define IO_T1N_GetValue()           PORTAbits.RA0
#define IO_T1N_SetDigitalInput()    do { TRISAbits.TRISA0 = 1; } while(0)
#define IO_T1N_SetDigitalOutput()   do { TRISAbits.TRISA0 = 0; } while(0)
#define IO_T1N_SetAnalogMode()  do { ANSELAbits.ANSA0 = 1; } while(0)
#define IO_T1N_SetDigitalMode() do { ANSELAbits.ANSA0 = 0; } while(0)

// get/set IO_T1R aliases
#define IO_T1R_TRIS               TRISAbits.TRISA1
#define IO_T1R_LAT                LATAbits.LATA1
#define IO_T1R_PORT               PORTAbits.RA1
#define IO_T1R_ANS                ANSELAbits.ANSA1
#define IO_T1R_SetHigh()            do { LATAbits.LATA1 = 1; } while(0)
#define IO_T1R_SetLow()             do { LATAbits.LATA1 = 0; } while(0)
#define IO_T1R_Toggle()             do { LATAbits.LATA1 = ~LATAbits.LATA1; } while(0)
#define IO_T1R_GetValue()           PORTAbits.RA1
#define IO_T1R_SetDigitalInput()    do { TRISAbits.TRISA1 = 1; } while(0)
#define IO_T1R_SetDigitalOutput()   do { TRISAbits.TRISA1 = 0; } while(0)
#define IO_T1R_SetAnalogMode()  do { ANSELAbits.ANSA1 = 1; } while(0)
#define IO_T1R_SetDigitalMode() do { ANSELAbits.ANSA1 = 0; } while(0)

// get/set IO_T2N aliases
#define IO_T2N_TRIS               TRISAbits.TRISA2
#define IO_T2N_LAT                LATAbits.LATA2
#define IO_T2N_PORT               PORTAbits.RA2
#define IO_T2N_ANS                ANSELAbits.ANSA2
#define IO_T2N_SetHigh()            do { LATAbits.LATA2 = 1; } while(0)
#define IO_T2N_SetLow()             do { LATAbits.LATA2 = 0; } while(0)
#define IO_T2N_Toggle()             do { LATAbits.LATA2 = ~LATAbits.LATA2; } while(0)
#define IO_T2N_GetValue()           PORTAbits.RA2
#define IO_T2N_SetDigitalInput()    do { TRISAbits.TRISA2 = 1; } while(0)
#define IO_T2N_SetDigitalOutput()   do { TRISAbits.TRISA2 = 0; } while(0)
#define IO_T2N_SetAnalogMode()  do { ANSELAbits.ANSA2 = 1; } while(0)
#define IO_T2N_SetDigitalMode() do { ANSELAbits.ANSA2 = 0; } while(0)

// get/set IO_T2R aliases
#define IO_T2R_TRIS               TRISAbits.TRISA3
#define IO_T2R_LAT                LATAbits.LATA3
#define IO_T2R_PORT               PORTAbits.RA3
#define IO_T2R_ANS                ANSELAbits.ANSA3
#define IO_T2R_SetHigh()            do { LATAbits.LATA3 = 1; } while(0)
#define IO_T2R_SetLow()             do { LATAbits.LATA3 = 0; } while(0)
#define IO_T2R_Toggle()             do { LATAbits.LATA3 = ~LATAbits.LATA3; } while(0)
#define IO_T2R_GetValue()           PORTAbits.RA3
#define IO_T2R_SetDigitalInput()    do { TRISAbits.TRISA3 = 1; } while(0)
#define IO_T2R_SetDigitalOutput()   do { TRISAbits.TRISA3 = 0; } while(0)
#define IO_T2R_SetAnalogMode()  do { ANSELAbits.ANSA3 = 1; } while(0)
#define IO_T2R_SetDigitalMode() do { ANSELAbits.ANSA3 = 0; } while(0)

// get/set IO_T3N aliases
#define IO_T3N_TRIS               TRISAbits.TRISA4
#define IO_T3N_LAT                LATAbits.LATA4
#define IO_T3N_PORT               PORTAbits.RA4
#define IO_T3N_ANS                ANSELAbits.ANSA4
#define IO_T3N_SetHigh()            do { LATAbits.LATA4 = 1; } while(0)
#define IO_T3N_SetLow()             do { LATAbits.LATA4 = 0; } while(0)
#define IO_T3N_Toggle()             do { LATAbits.LATA4 = ~LATAbits.LATA4; } while(0)
#define IO_T3N_GetValue()           PORTAbits.RA4
#define IO_T3N_SetDigitalInput()    do { TRISAbits.TRISA4 = 1; } while(0)
#define IO_T3N_SetDigitalOutput()   do { TRISAbits.TRISA4 = 0; } while(0)
#define IO_T3N_SetAnalogMode()  do { ANSELAbits.ANSA4 = 1; } while(0)
#define IO_T3N_SetDigitalMode() do { ANSELAbits.ANSA4 = 0; } while(0)

// get/set IO_T3R aliases
#define IO_T3R_TRIS               TRISAbits.TRISA5
#define IO_T3R_LAT                LATAbits.LATA5
#define IO_T3R_PORT               PORTAbits.RA5
#define IO_T3R_ANS                ANSELAbits.ANSA5
#define IO_T3R_SetHigh()            do { LATAbits.LATA5 = 1; } while(0)
#define IO_T3R_SetLow()             do { LATAbits.LATA5 = 0; } while(0)
#define IO_T3R_Toggle()             do { LATAbits.LATA5 = ~LATAbits.LATA5; } while(0)
#define IO_T3R_GetValue()           PORTAbits.RA5
#define IO_T3R_SetDigitalInput()    do { TRISAbits.TRISA5 = 1; } while(0)
#define IO_T3R_SetDigitalOutput()   do { TRISAbits.TRISA5 = 0; } while(0)
#define IO_T3R_SetAnalogMode()  do { ANSELAbits.ANSA5 = 1; } while(0)
#define IO_T3R_SetDigitalMode() do { ANSELAbits.ANSA5 = 0; } while(0)

// get/set IO_T4N aliases
#define IO_T4N_TRIS               TRISAbits.TRISA6
#define IO_T4N_LAT                LATAbits.LATA6
#define IO_T4N_PORT               PORTAbits.RA6
#define IO_T4N_SetHigh()            do { LATAbits.LATA6 = 1; } while(0)
#define IO_T4N_SetLow()             do { LATAbits.LATA6 = 0; } while(0)
#define IO_T4N_Toggle()             do { LATAbits.LATA6 = ~LATAbits.LATA6; } while(0)
#define IO_T4N_GetValue()           PORTAbits.RA6
#define IO_T4N_SetDigitalInput()    do { TRISAbits.TRISA6 = 1; } while(0)
#define IO_T4N_SetDigitalOutput()   do { TRISAbits.TRISA6 = 0; } while(0)

// get/set IO_T4R aliases
#define IO_T4R_TRIS               TRISAbits.TRISA7
#define IO_T4R_LAT                LATAbits.LATA7
#define IO_T4R_PORT               PORTAbits.RA7
#define IO_T4R_SetHigh()            do { LATAbits.LATA7 = 1; } while(0)
#define IO_T4R_SetLow()             do { LATAbits.LATA7 = 0; } while(0)
#define IO_T4R_Toggle()             do { LATAbits.LATA7 = ~LATAbits.LATA7; } while(0)
#define IO_T4R_GetValue()           PORTAbits.RA7
#define IO_T4R_SetDigitalInput()    do { TRISAbits.TRISA7 = 1; } while(0)
#define IO_T4R_SetDigitalOutput()   do { TRISAbits.TRISA7 = 0; } while(0)

// get/set IO_T5N aliases
#define IO_T5N_TRIS               TRISBbits.TRISB0
#define IO_T5N_LAT                LATBbits.LATB0
#define IO_T5N_PORT               PORTBbits.RB0
#define IO_T5N_WPU                WPUBbits.WPUB0
#define IO_T5N_ANS                ANSELBbits.ANSB0
#define IO_T5N_SetHigh()            do { LATBbits.LATB0 = 1; } while(0)
#define IO_T5N_SetLow()             do { LATBbits.LATB0 = 0; } while(0)
#define IO_T5N_Toggle()             do { LATBbits.LATB0 = ~LATBbits.LATB0; } while(0)
#define IO_T5N_GetValue()           PORTBbits.RB0
#define IO_T5N_SetDigitalInput()    do { TRISBbits.TRISB0 = 1; } while(0)
#define IO_T5N_SetDigitalOutput()   do { TRISBbits.TRISB0 = 0; } while(0)
#define IO_T5N_SetPullup()      do { WPUBbits.WPUB0 = 1; } while(0)
#define IO_T5N_ResetPullup()    do { WPUBbits.WPUB0 = 0; } while(0)
#define IO_T5N_SetAnalogMode()  do { ANSELBbits.ANSB0 = 1; } while(0)
#define IO_T5N_SetDigitalMode() do { ANSELBbits.ANSB0 = 0; } while(0)

// get/set IO_T5R aliases
#define IO_T5R_TRIS               TRISBbits.TRISB1
#define IO_T5R_LAT                LATBbits.LATB1
#define IO_T5R_PORT               PORTBbits.RB1
#define IO_T5R_WPU                WPUBbits.WPUB1
#define IO_T5R_ANS                ANSELBbits.ANSB1
#define IO_T5R_SetHigh()            do { LATBbits.LATB1 = 1; } while(0)
#define IO_T5R_SetLow()             do { LATBbits.LATB1 = 0; } while(0)
#define IO_T5R_Toggle()             do { LATBbits.LATB1 = ~LATBbits.LATB1; } while(0)
#define IO_T5R_GetValue()           PORTBbits.RB1
#define IO_T5R_SetDigitalInput()    do { TRISBbits.TRISB1 = 1; } while(0)
#define IO_T5R_SetDigitalOutput()   do { TRISBbits.TRISB1 = 0; } while(0)
#define IO_T5R_SetPullup()      do { WPUBbits.WPUB1 = 1; } while(0)
#define IO_T5R_ResetPullup()    do { WPUBbits.WPUB1 = 0; } while(0)
#define IO_T5R_SetAnalogMode()  do { ANSELBbits.ANSB1 = 1; } while(0)
#define IO_T5R_SetDigitalMode() do { ANSELBbits.ANSB1 = 0; } while(0)

// get/set IO_T6N aliases
#define IO_T6N_TRIS               TRISBbits.TRISB2
#define IO_T6N_LAT                LATBbits.LATB2
#define IO_T6N_PORT               PORTBbits.RB2
#define IO_T6N_WPU                WPUBbits.WPUB2
#define IO_T6N_ANS                ANSELBbits.ANSB2
#define IO_T6N_SetHigh()            do { LATBbits.LATB2 = 1; } while(0)
#define IO_T6N_SetLow()             do { LATBbits.LATB2 = 0; } while(0)
#define IO_T6N_Toggle()             do { LATBbits.LATB2 = ~LATBbits.LATB2; } while(0)
#define IO_T6N_GetValue()           PORTBbits.RB2
#define IO_T6N_SetDigitalInput()    do { TRISBbits.TRISB2 = 1; } while(0)
#define IO_T6N_SetDigitalOutput()   do { TRISBbits.TRISB2 = 0; } while(0)
#define IO_T6N_SetPullup()      do { WPUBbits.WPUB2 = 1; } while(0)
#define IO_T6N_ResetPullup()    do { WPUBbits.WPUB2 = 0; } while(0)
#define IO_T6N_SetAnalogMode()  do { ANSELBbits.ANSB2 = 1; } while(0)
#define IO_T6N_SetDigitalMode() do { ANSELBbits.ANSB2 = 0; } while(0)

// get/set IO_T6R aliases
#define IO_T6R_TRIS               TRISBbits.TRISB3
#define IO_T6R_LAT                LATBbits.LATB3
#define IO_T6R_PORT               PORTBbits.RB3
#define IO_T6R_WPU                WPUBbits.WPUB3
#define IO_T6R_ANS                ANSELBbits.ANSB3
#define IO_T6R_SetHigh()            do { LATBbits.LATB3 = 1; } while(0)
#define IO_T6R_SetLow()             do { LATBbits.LATB3 = 0; } while(0)
#define IO_T6R_Toggle()             do { LATBbits.LATB3 = ~LATBbits.LATB3; } while(0)
#define IO_T6R_GetValue()           PORTBbits.RB3
#define IO_T6R_SetDigitalInput()    do { TRISBbits.TRISB3 = 1; } while(0)
#define IO_T6R_SetDigitalOutput()   do { TRISBbits.TRISB3 = 0; } while(0)
#define IO_T6R_SetPullup()      do { WPUBbits.WPUB3 = 1; } while(0)
#define IO_T6R_ResetPullup()    do { WPUBbits.WPUB3 = 0; } while(0)
#define IO_T6R_SetAnalogMode()  do { ANSELBbits.ANSB3 = 1; } while(0)
#define IO_T6R_SetDigitalMode() do { ANSELBbits.ANSB3 = 0; } while(0)

// get/set IO_T7N aliases
#define IO_T7N_TRIS               TRISBbits.TRISB4
#define IO_T7N_LAT                LATBbits.LATB4
#define IO_T7N_PORT               PORTBbits.RB4
#define IO_T7N_WPU                WPUBbits.WPUB4
#define IO_T7N_ANS                ANSELBbits.ANSB4
#define IO_T7N_SetHigh()            do { LATBbits.LATB4 = 1; } while(0)
#define IO_T7N_SetLow()             do { LATBbits.LATB4 = 0; } while(0)
#define IO_T7N_Toggle()             do { LATBbits.LATB4 = ~LATBbits.LATB4; } while(0)
#define IO_T7N_GetValue()           PORTBbits.RB4
#define IO_T7N_SetDigitalInput()    do { TRISBbits.TRISB4 = 1; } while(0)
#define IO_T7N_SetDigitalOutput()   do { TRISBbits.TRISB4 = 0; } while(0)
#define IO_T7N_SetPullup()      do { WPUBbits.WPUB4 = 1; } while(0)
#define IO_T7N_ResetPullup()    do { WPUBbits.WPUB4 = 0; } while(0)
#define IO_T7N_SetAnalogMode()  do { ANSELBbits.ANSB4 = 1; } while(0)
#define IO_T7N_SetDigitalMode() do { ANSELBbits.ANSB4 = 0; } while(0)

// get/set IO_T7R aliases
#define IO_T7R_TRIS               TRISBbits.TRISB5
#define IO_T7R_LAT                LATBbits.LATB5
#define IO_T7R_PORT               PORTBbits.RB5
#define IO_T7R_WPU                WPUBbits.WPUB5
#define IO_T7R_ANS                ANSELBbits.ANSB5
#define IO_T7R_SetHigh()            do { LATBbits.LATB5 = 1; } while(0)
#define IO_T7R_SetLow()             do { LATBbits.LATB5 = 0; } while(0)
#define IO_T7R_Toggle()             do { LATBbits.LATB5 = ~LATBbits.LATB5; } while(0)
#define IO_T7R_GetValue()           PORTBbits.RB5
#define IO_T7R_SetDigitalInput()    do { TRISBbits.TRISB5 = 1; } while(0)
#define IO_T7R_SetDigitalOutput()   do { TRISBbits.TRISB5 = 0; } while(0)
#define IO_T7R_SetPullup()      do { WPUBbits.WPUB5 = 1; } while(0)
#define IO_T7R_ResetPullup()    do { WPUBbits.WPUB5 = 0; } while(0)
#define IO_T7R_SetAnalogMode()  do { ANSELBbits.ANSB5 = 1; } while(0)
#define IO_T7R_SetDigitalMode() do { ANSELBbits.ANSB5 = 0; } while(0)

// get/set IO_T8R aliases
#define IO_T8R_TRIS               TRISCbits.TRISC0
#define IO_T8R_LAT                LATCbits.LATC0
#define IO_T8R_PORT               PORTCbits.RC0
#define IO_T8R_SetHigh()            do { LATCbits.LATC0 = 1; } while(0)
#define IO_T8R_SetLow()             do { LATCbits.LATC0 = 0; } while(0)
#define IO_T8R_Toggle()             do { LATCbits.LATC0 = ~LATCbits.LATC0; } while(0)
#define IO_T8R_GetValue()           PORTCbits.RC0
#define IO_T8R_SetDigitalInput()    do { TRISCbits.TRISC0 = 1; } while(0)
#define IO_T8R_SetDigitalOutput()   do { TRISCbits.TRISC0 = 0; } while(0)

// get/set IO_T8N aliases
#define IO_T8N_TRIS               TRISCbits.TRISC1
#define IO_T8N_LAT                LATCbits.LATC1
#define IO_T8N_PORT               PORTCbits.RC1
#define IO_T8N_SetHigh()            do { LATCbits.LATC1 = 1; } while(0)
#define IO_T8N_SetLow()             do { LATCbits.LATC1 = 0; } while(0)
#define IO_T8N_Toggle()             do { LATCbits.LATC1 = ~LATCbits.LATC1; } while(0)
#define IO_T8N_GetValue()           PORTCbits.RC1
#define IO_T8N_SetDigitalInput()    do { TRISCbits.TRISC1 = 1; } while(0)
#define IO_T8N_SetDigitalOutput()   do { TRISCbits.TRISC1 = 0; } while(0)

// get/set IO_LED aliases
#define IO_LED_TRIS               TRISCbits.TRISC3
#define IO_LED_LAT                LATCbits.LATC3
#define IO_LED_PORT               PORTCbits.RC3
#define IO_LED_SetHigh()            do { LATCbits.LATC3 = 1; } while(0)
#define IO_LED_SetLow()             do { LATCbits.LATC3 = 0; } while(0)
#define IO_LED_Toggle()             do { LATCbits.LATC3 = ~LATCbits.LATC3; } while(0)
#define IO_LED_GetValue()           PORTCbits.RC3
#define IO_LED_SetDigitalInput()    do { TRISCbits.TRISC3 = 1; } while(0)
#define IO_LED_SetDigitalOutput()   do { TRISCbits.TRISC3 = 0; } while(0)

// get/set RC6 procedures
#define RC6_SetHigh()    do { LATCbits.LATC6 = 1; } while(0)
#define RC6_SetLow()   do { LATCbits.LATC6 = 0; } while(0)
#define RC6_Toggle()   do { LATCbits.LATC6 = ~LATCbits.LATC6; } while(0)
#define RC6_GetValue()         PORTCbits.RC6
#define RC6_SetDigitalInput()   do { TRISCbits.TRISC6 = 1; } while(0)
#define RC6_SetDigitalOutput()  do { TRISCbits.TRISC6 = 0; } while(0)

/**
   @Param
    none
   @Returns
    none
   @Description
    GPIO and peripheral I/O initialization
   @Example
    PIN_MANAGER_Initialize();
 */
void PIN_MANAGER_Initialize (void);

/**
 * @Param
    none
 * @Returns
    none
 * @Description
    Interrupt on Change Handling routine
 * @Example
    PIN_MANAGER_IOC();
 */
void PIN_MANAGER_IOC(void);



#endif // PIN_MANAGER_H
/**
 End of File
*/