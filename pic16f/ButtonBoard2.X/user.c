/******************************************************************************/
/* Files to Include                                                           */
/******************************************************************************/

#if defined(__XC)
    #include <xc.h>         /* XC8 General Include File */
#elif defined(HI_TECH_C)
    #include <htc.h>        /* HiTech General Include File */
#endif

#include <stdint.h>         /* For uint8_t definition */
#include <stdbool.h>        /* For true/false definition */

#include "user.h"
#include "mcc_generated_files/mcc.h"

/******************************************************************************/
/* User Functions                                                             */
/******************************************************************************/

/* <Initialize variables in user.h and insert code for user algorithms.> */

void InitApp(void) {
    /* TODO Initialize User Ports/Peripherals/Project here */
    /* Setup analog functionality and port direction */
    /* Initialize peripherals */
    /* Enable interrupts */
    
    /* Init via MCC generated code */
    SYSTEM_Initialize();
}

void LoopApp(void) {
    while (1) {
        __delay_ms(1000); // 1 Second Delay
        IO_LED_SetHigh(); // or IO_LED_LAT = 1;
        __delay_ms(1000); // 1 Second Delay
        IO_LED_SetLow();
    }
}
