#if defined(__XC)
    #include <xc.h>         /* XC8 General Include File */
#elif defined(HI_TECH_C)
    #include <htc.h>        /* HiTech General Include File */
#endif

#include <stdint.h>         /* For uint8_t definition */
#include <stdbool.h>        /* For true/false definition */

#include "user.h"
#include "mcc_generated_files/mcc.h"

void InitApp(void) {
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
