/**
 * Serial Filter for an NCE Button Board.
 * 
 * The NCE Button Board does not work properly with non-momentary contacts.
 * This experiment uses an Arduino Mega to filter the serial DATA link out
 * of the NCE Button Board and feed proper values to an NCE Switch-8.
 * 
 * Wiring:
 * - Pin 21 SCL to I2C LCD 20x4 display
 * - Pin 20 SDA to I2C LCD 20x4 display
 * - Pin 19 RX1 to DATA output from NCE Button Board
 * - Pin 18 TX1 to DATA input from NCE Switch-8
 * 
 * The serial data output by the NCE Button Board is formatted as follow:
 * - Push    event is 2 bytes: 0x8n (with n 0..F) followed by its 0xFF XOR complement.
 * - Release event is 2 bytes: 0x4n (with n 0..F) followed by its 0xFF XOR complement.
 */
#include <LiquidCrystal.h>
#include <Wire.h>

#include "LiquidCrystal_I2C.h"

#include <stdlib.h>
#include <ctype.h>

#undef DEBUG
#define LED_PIN         13

/** Milliseconds to pause before generating the release event. */
#define PAUSE_RELEASE_MS 250

LiquidCrystal_I2C lcd(0x27, 20, 4);  // set the LCD address to 0x27 for a 20 chars and 4 lines display

char buf1[2] = { 0, 0 };

// --------------------------------

void blink() {
  digitalWrite(LED_PIN, HIGH);
  delay(100 /*ms*/);
  digitalWrite(LED_PIN, LOW);
}

void lcd_print(int col, int row, const char *s) {
  while (s != NULL && *s != 0) {
    lcd.setCursor(col++,row);
    *buf1 = *(s++);
    lcd.print(buf1);
  }
}

void setup_blink() {
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);
}

void setup_pause() {
  for (int i = 0; i < 4; i++) {
    blink();
    delay(250);
  }
}

void setup_lcd() {
  lcd.init();
  lcd.display();
  lcd.backlight();
  lcd_print(0,0, "Button Board");
  lcd_print(0,1, "Serial Xfer");

  // Print turnout numbers (1..8)
  for (int i = 0; i < 8; i++) {
    int col = 1 + i*2;
    *buf1 = '1' + i;
    lcd.setCursor(col,2);
    lcd.print(buf1);
    lcd.setCursor(col,3);
    *buf1 = '-';
    lcd.print(buf1);
  }
}

void setup_nce() {
  Serial1.begin(9600 /*SERIAL_8N1*/);
}

void setup() {
    setup_blink();
    setup_pause();
    setup_lcd();
    setup_nce();
}

// -------------------------------

unsigned int serial_data = 0;
unsigned int serial_last = 0;

/** Read incoming serial bytes in the buffer. */
void loop_read() {
  if (Serial1.available()) {
    int data = Serial1.read();
    serial_data = ((serial_data & 0xFF) << 8) | (data & 0xFF);
  } else {
    // 9600 bauds = 104 us per bit = 1.04 ms per byte.
    delay(1);
  }
}

/** Detect whether the 2 last bytes received are a proper push event. */
bool data_is_valid() {
  byte byte1 = (serial_data >> 8) & 0xFF;
  byte byte2 = (serial_data     ) & 0xFF;
  if ((byte1 & 0x80) == 0x80) {
    if ((byte1 ^ byte2) == 0xFF) {
      return true;
    }
  }
  return false;
}

/** Output the current push event followed by a simulated release event. */
bool output_command() {
  // Don't send the same command twice in a row
  if (serial_last == serial_data) {
    return false;
  }

  byte byte1 = (serial_data >> 8) & 0xFF;
  byte byte2 = (serial_data     ) & 0xFF;
  Serial1.write(byte1);
  Serial1.write(byte2);
  delay(PAUSE_RELEASE_MS);

  // Release event uses 0x40 instead of 0x80
//  byte1 = 0x40 | (byte1 & 0x00F);
//  Serial1.write(byte1);
//  Serial1.write(byte2);

  // Update last sent command
  serial_last = serial_data;
  return true;
}

/** Print command to LCD. Totally useless and thus absolutely needed. */
void decode_command_to_lcd() {
  // Input # trigger is the low nibble of the first byte.
  // Turnout number is input / 2.
  // Even numbers for N and Odd numbers for R.

  byte input = (serial_last >> 8) & 0x0F;
  int col = 1 + (input & 0x0E);

  lcd.setCursor(col,3);
  *buf1 = input & 0x01 ? 'R' : 'N';
  lcd.print(buf1);
}

// -------------------------------

void loop() {
  loop_read();
  if (data_is_valid()) {
    if (output_command()) {
      blink();
      decode_command_to_lcd();
    }
  }
}

