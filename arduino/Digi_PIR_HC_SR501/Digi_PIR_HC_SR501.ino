#define USB_CFG_DEVICE_NAME     'D','i','g','i','S','R','5','0','1'
#define USB_CFG_DEVICE_NAME_LEN 9
#include <DigiUSB.h>

#define LED     1    // on-board led on digital pin 1
#define ADC1    1    // ADC channel 1
#define ADC1_P2 2    // ADC1 is connected to digital pin 2
#define THRESHOLD 100
int value = 0;       // ADC value is 10-bits
char buf[2];

void setup() {
  DigiUSB.begin();
  pinMode(LED,     OUTPUT);
  pinMode(ADC1_P2, INPUT);
  for (int i = 0; i < 8; i++) {
    delay(150);
    blink();
  }
}

void blink() {
  digitalWrite(LED, LOW);
  delay(50 /*ms*/);
  digitalWrite(LED, HIGH);
  delay(50 /*ms*/);
  digitalWrite(LED, LOW);
}

void loop() {
  char in;
  DigiUSB.refresh();
  if (DigiUSB.available() > 0) {
    in = DigiUSB.read();
    if (in == 'r') {
      readPir();
    } else if (in == 'b') {
      blink();
    }
  }
}

void readPir() {
  // digitalRead() requires 3V on a 5V device to trigger.
  // When placing a LED on the output, it might be just a tad below.
  // Using analog read instead with a rather low threshold.

  // Take average of 4 reads to account for possible noise.
  // 10-bits = 1024.
  value  = analogRead(ADC1);
  value += analogRead(ADC1);
  value += analogRead(ADC1);
  value += analogRead(ADC1);
  value /= 4;

  int pir_on = value > THRESHOLD;
  digitalWrite(LED, pir_on ? HIGH : LOW);
  buf[0] = '0' + !!pir_on;
  buf[1] = 0;
  DigiUSB.println(buf);
}

