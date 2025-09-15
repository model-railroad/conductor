# Experiment ESP32-S2 QT PY w/ HC-SR501 Sensor & MQTT publication
# 2025 (c) ralfoide at gmail
# License: MIT
#
# Target Platform: AdaFruit ESP32-S2 QT PY
#
# Hardware:
# - AdaFruit ESP32-S2 QT PY
# - Generic/clone HC-SR501 sensor.

# CircuitPython built-in libraries
import board
import digitalio
import time
from digitalio import DigitalInOut, Direction, Pull

# See https://github.com/micropython/micropython/issues/573 for const() details
from micropython import const

# Bundle libraries
import neopixel
import adafruit_logging as logging

# from script_loader import ScriptLoader
# from script_parser import ScriptParser, FONT_Y_OFFSET

# Main loop max idle time
_LOOP_IDLE_SEC = const(0.25)

# Possible colors for the status NeoPixel LED (not for the matrix display).
_COL_OFF    = const( (  0,   0,   0) )
_COL_RED    = const( (255,   0,   0) )
_COL_GREEN  = const( (  0, 255,   0) )
_COL_BLUE   = const( (  0,   0, 255) )
_COL_PURPLE = const( (255,   0, 255) )      # FF00FF
_COL_ORANGE = const( (255,  40,   0) )      # FF2800
_COL_YELLOW = const( (255, 112,   0) )      # FF7000

# We use the LED color to get init status
_CODE_OK = const("ok")
_CODE_FATAL = const("fatal")
_CODE_RETRY = const("retry")
_CODE_MOTION = const("mot")
_CODE_NO_MOTION = const("no-mot")
_COL_LED_ERROR = {
    _CODE_OK: _COL_GREEN,
    _CODE_RETRY: _COL_YELLOW,
    _CODE_FATAL: _COL_RED,
    _CODE_MOTION: _COL_YELLOW,
    _CODE_NO_MOTION: _COL_GREEN,
}


_led = None
_boot_btn = None
_input_a2 = None
_logger = logging.getLogger("Exp")
_logger.setLevel(logging.INFO)      # INFO or DEBUG


def init() -> None:
    print("@@ init")
    global _led

    _led = neopixel.NeoPixel(board.NEOPIXEL, 1)
    _led.brightness = 0.1


def init_buttons():
    global _boot_btn
    _boot_btn = digitalio.DigitalInOut(board.D0)
    _boot_btn.switch_to_input(pull = digitalio.Pull.UP)


def blink_led_error(error_code, num_loop=-1):
    _led.fill(_COL_LED_ERROR[error_code])
    _led.brightness = 0.1
    time.sleep(0.5)
    # For debugging purposes, we can exit the loop by using the boot button to continue
    while num_loop != 0 and _boot_btn.value:
        _led.brightness = 0
        time.sleep(0.25)
        _led.brightness = 0.1
        time.sleep(1)
        num_loop -= 1


_last_blink_led_ts = 0
_next_blink_led = 1
def blink_led() -> None:
    global _last_blink_led_ts, _next_blink_led
    _led.brightness = 0.1 if _next_blink_led else 0
    now = time.monotonic()
    if now - _last_blink_led_ts > 1:
        _last_blink_led_ts = now
        _next_blink_led = 1 - _next_blink_led


def init_sensor() -> None:
    global _input_a2
    # Can we treat A2 as a digital input?
    _input_a2 = digitalio.DigitalInOut(board.A2)
    _input_a2.switch_to_input(pull = None)


def read_sensor() -> bool:
    return _input_a2.value


if __name__ == "__main__":
    print("@@ loop")

    init()
    init_buttons()

    # # Sleep a few seconds at boot
    _led.fill(_COL_LED_ERROR[_CODE_OK])
    for i in range(0, 3):
        print(i)
        blink_led()
        time.sleep(1)

    blink_led()
    init_sensor()

    delta_ts = 0
    motion = None
    while True:
        start_ts = time.monotonic()
        blink_led()

        old_motion = motion
        motion = read_sensor()
        if motion != old_motion:
            if motion:
                _led.fill(_COL_LED_ERROR[_CODE_MOTION])
                _led.brightness = 0.1
            else:
                _led.fill(_COL_LED_ERROR[_CODE_NO_MOTION])
                _led.brightness = 0.1
            print("@@ loop motion", motion, ":", delta_ts, "s")

        end_ts = time.monotonic()
        delta_ts = end_ts - start_ts
        # prevent busy loop
        if delta_ts < _LOOP_IDLE_SEC: time.sleep(_LOOP_IDLE_SEC - delta_ts)
        # print("@@ loop", _core_state, ":", delta_ts, "s", wifi_rssi(), "dBm")

#~~
