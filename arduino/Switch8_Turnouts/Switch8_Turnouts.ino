/*
  Arduino Sketch to control an NCE Switch-8 from non-momentary rotary toggle switches.
  This replaces an NCE Button Board which does not properly deal with non-momentary rotary switches.
  
  This sketch is designed for an Arduino Nano.
  https://www.arduino.cc/en/Main/ArduinoBoardNano
  Connections to the Switch-8's Button Board terminal:
  - Pin 1 (TX1) to "DATA"
  - Pin VIN to "+" (6-20 V max, 7-12 V recommended)
  - Pin GND to "GND"
  Connections to the rotary toggles:
  - D2/D3  : Turnout 1 N/R
  - D4/D5  : Turnout 2 N/R
  - D6/D7  : Turnout 3 N/R
  - D8/D9  : Turnout 4 N/R
  - D10/D11: Turnout 5 N/R
  - A0/A1  : Turnout 6 N/R
  - A2/A3  : Turnout 7 N/R
  - A4/A5  : Turnout 8 N/R  

  Note that on the Arduino Nano, RX0/TX1 are linked to the USB chip.
  This means one can trivially check the output by using an USB cable
  and checking the Arduino IDE's serial console.
*/

// Pin number of the Arduino's onboard LED.
#define LED 13

// Time in milliseconds to wait at startup to let the Switch-8 initialize.
#define DELAY_MS_START 2000

// Time in milliseconds to wait after sending a serial command to let the Switch-8 throw the turnout.
#define DELAY_MS_SWITCH 1000

// Time in milliseconds before scanning next input.
// This crude rate-limiter makes the sketch scan each input roughtly once per second.
#define DELAY_MS_LOOP 10

// Blink the LED for 100 ms.
#define DELAY_MS_BLINK 100

// Time in milliseconds to wait before sending the button-released command to the Switch-8.
#define DELAY_MS_BTN_RELEASE 500

// Number of inputs scanned (8 turnouts, Normal/Release each).
#define MAX_INPUT 16

// When this digital I/O is grounded during setup, output debug statements instead of SW8 commands
#define DEBUG_PIN 12

// Pin numbers of the Digital I/O to scan for the 8 turnouts inputs.
// Each turnout uses 2 inputs: one for Normal and one for Reverse.
const int inputs[MAX_INPUT] = {
  2,3, 4,5, 6,7, 8,9, 10,11,
  A0,A1, A2,A3, A4,A5
};

// Last known state of each input, either LOW (active) or HIGH.
int states[MAX_INPUT];

boolean isDebug;

// Blinks the onboard LED.
void blink() {
  digitalWrite(LED, HIGH);
  delay(DELAY_MS_BLINK);
  digitalWrite(LED, LOW);
}

void sendByte(byte value) {
  Serial.write(value);
  Serial.write(0xFF ^ value);
}

void sendSwitch8Command(int index) {
  if (isDebug) {
    Serial.write("\nActivate ");
    String s = String(index);
    Serial.write(s.c_str());
  } else {
    sendByte(0x80 + index);
    delay(DELAY_MS_BTN_RELEASE);
    sendByte(0x40 + index);
  }
}

// Checks an input and sends a command to the Switch-8 if the input
// has changed since last read.
void checkInput(int index) {
  int state = digitalRead(inputs[index]);

  if (state != states[index]) {
    // State has changed.
    states[index] = state;

    if (isDebug) {
      Serial.write("\nChanged ");
      String s = String(index);
      Serial.write(s.c_str());
      Serial.write(" to ");
      Serial.write(state ? "HI" : "LO");
    }

    if (state == LOW) {
      // Inputs are active LOW since we use pull-up resistors.
      // For any turnout rotary switch, one of the inputs is LOW and the other one is HIGH.
      blink();
      sendSwitch8Command(index);
      delay(DELAY_MS_SWITCH);
    }
  }
}

void sleepStart() {
  for (int i = 0; i < 4; ++i) {
    blink();
    delay(DELAY_MS_START / 4);
  }
}

void setup() {
  // Configure the onboard LED pin to a digital output
  pinMode(LED, OUTPUT);
  // Configure the debug pin enabling its internal pull-up.
  pinMode(DEBUG_PIN, INPUT_PULLUP);
  for (int i = 0; i < MAX_INPUT; ++i) {
    // Configure the inputs, enabling their internal pull-up.
    pinMode(inputs[i], INPUT_PULLUP);
    // Set each memorized "previous" state to high. Since the states
    // are active LOW, this will force the initialization code to send
    // the current turnout state to the Switch-8 during setup.
    states[i] = HIGH;
  }
  
  // Initialize serial port.
  Serial.begin(9600, SERIAL_8N1);
  
  // Sleep to give time to the Switch-8 to start
  sleepStart();
  isDebug = digitalRead(DEBUG_PIN) == LOW;
  if (isDebug) {
    Serial.write("\nDEBUG MODE\n");
    sleepStart();
  }

  // Checks the inputs and updates the Switch-8 to match the rotary switches.
  for (int i = 0; i < MAX_INPUT; ++i) {
    blink();
    checkInput(i);
  }
}

void loop() {
  for (int i = 0; i < MAX_INPUT; ++i) {
    checkInput(i);    
    delay(DELAY_MS_LOOP);
  }  
}

