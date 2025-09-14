# MagTag MQTT Sensor
# 2025 (c) ralfoide at gmail
# License: MIT
#
# Target Platform: CircuitPython 9.x on AdaFruit MagTag

from adafruit_magtag.magtag import MagTag
import adafruit_vl53l0x
import board
import busio
import microcontroller
import os
import socketpool
import struct
import time
import wifi

_magtag: MagTag = None              # Global MagTag object (singleton)
_i2c = None
_tof_sensor: adafruit_vl53l0x.VL53L0X = None

COL_BLACK = (   0,    0,    0)
COL_BLINK = (0x80, 0x7F, 0x80)
COL_OK    = (   0, 0xFF,    0)
COL_WARN  = (0x80, 0x80,    0)
COL_ALERT = (0xFF,    0,    0)

REFRESH_DELAY_S = 1
DISPLAY_TIME_DELAY_S = 10

LIGHT_THRESHOLD = 600
_light_level = {
    "current": 0,
    "min": 10000,
    "max": 0,
    "changed": True,
}

# VL53L0X Timing Budget: 20ms = fast but medium accurate; 200ms = slow but more accurate.
VL53_TIMING_BUGDGET_MS = 200
# VL53L0X returns the distance in mm (integer).
# Max distance measurable is 2 meters (2000 mm). When nothing found, it seems to returns > 8000 mm.
_tof_distance_mm = {
    "current": 0,
    "min": 10000,
    "max": 0,
    "changed": True,
}

def init_display() -> None:
    global _magtag
    _magtag = MagTag()

    # Text index 0 + 1 : light sensor.
    _magtag.add_text(text_position=(35, 7,), text_scale=1 )
    _magtag.set_text("Ambiant Sensor", index = 0, auto_refresh = False)
    _magtag.add_text(text_position=(35, 32,), text_scale=3 )
    _magtag.set_text("----", index = 1, auto_refresh = False)

    # Text index 2 + 3 : light sensor.
    _magtag.add_text(text_position=(35, 64+7,), text_scale=1 )
    _magtag.set_text("VL503L0X Sensor", index = 2, auto_refresh = False)
    _magtag.add_text(text_position=(35, 64+32,), text_scale=3 )
    _magtag.set_text("----", index = 3, auto_refresh = False)

    # Reminder: AFAIK there's no hardware brightness control on a NeoPixel strip.
    # Instead the "brightness" value just sets a multiplier on the RGB colors provided.
    _magtag.peripherals.neopixel_disable = False
    _magtag.peripherals.neopixels.brightness = 1
    update_leds(blink = False)

def init_vl53l0x() -> None:
    global _i2c, _tof_sensor
    _i2c = busio.I2C(board.SCL, board.SDA)
    _tof_sensor = adafruit_vl53l0x.VL53L0X(_i2c)
    _tof_sensor.measurement_timing_budget = int(VL53_TIMING_BUGDGET_MS * 1000)

def init_wifi() -> None:
    global _wifi_channel
    print("@@ WiFI setup")
    # Get wifi AP credentials from onboard settings.toml file
    wifi_ssid = os.getenv("CIRCUITPY_WIFI_SSID")
    wifi_password = os.getenv("CIRCUITPY_WIFI_PASSWORD")

    print("@@ WiFI SSID:", wifi_ssid)
    if wifi_ssid is None:
        print("@@ WiFI credentials are kept in settings.toml, please add them there!")
        raise ValueError("WiFI SSID not found in environment variables")

    try:
        wifi.radio.connect(wifi_ssid, wifi_password)
    except ConnectionError:
        print("@@ WiFI Failed to connect to WiFi with provided credentials")
        raise
    print("@@ WiFI Station MAC Address:", wifi.radio.mac_address.hex())
    print("@@ WiFI OK for", wifi_ssid)

def display_values() -> None:
    str1 = level_to_str_compact(_light_level)
    _magtag.set_text(str1, index = 1, auto_refresh = False)
    str2 = level_to_str_compact(_tof_distance_mm)
    _magtag.set_text(str2, index = 3, auto_refresh = True)

def update_leds(blink: int, rgb1 = None, rgb2 = None, bright: bool = True) -> None:
    _magtag.peripherals.neopixels[3] = fade1(bright, COL_BLINK) if blink else COL_BLACK
    _magtag.peripherals.neopixels[2] = COL_BLACK
    _magtag.peripherals.neopixels[1] = fade2(bright, rgb2) if rgb2 else COL_BLACK
    _magtag.peripherals.neopixels[0] = fade2(bright, rgb1) if rgb1 else COL_BLACK

def fade1(bright: bool, rgb):
    (r, g, b) = rgb
    if bright:
        return (r >> 3, g >> 3, b >> 3)
    else:
        return (r >> 6, g >> 6, b >> 6)

def fade2(bright: bool, rgb):
    if bright:
        return rgb
    else:
        (r, g, b) = rgb
        return (r >> 6, g >> 6, b >> 6)

def update_tof_distance_mm() -> None:
    dist_mm = _tof_sensor.range
    _tof_distance_mm["current"] = dist_mm
    if dist_mm < _tof_distance_mm["min"]:
        _tof_distance_mm["min"] = dist_mm
    if dist_mm > _tof_distance_mm["max"]:
        _tof_distance_mm["max"] = dist_mm
    return dist_mm

def update_light_level() -> int:
    lvl = _magtag.peripherals.light
    _light_level["changed"] = _light_level["changed"] or (_light_level["current"] != lvl)
    _light_level["current"] = lvl
    if lvl < _light_level["min"]:
        _light_level["min"] = lvl
    if lvl > _light_level["max"]:
        _light_level["max"] = lvl
    return lvl

def level_to_str(level) -> str:
    return "%(min)d < %(current)d < %(max)d" % level

def level_to_str_compact(level) -> str:
    return "%(min)d %(current)d %(max)d" % level

def level_to_rgb(level, rgb):
    (r, g, b) = rgb

    if level["max"] > level["min"]:
        val255 = int(255 * (level["current"] - level["min"]) / (level["max"] - level["min"]))
        if r < 0: r = val255
        if g < 0: g = val255
        if b < 0: b = val255

    return (r, g, b)

def loop() -> None:
    # Sleep a few seconds at boot
    for i in range(0, 3):
        print(i)
        update_leds(blink = i % 2 == 0)
        time.sleep(1)

    start_s = time.time()
    display_next_s = 0
    display_on = True
    while True:
        now_s = time.time()
        light_lvl = update_light_level()
        update_tof_distance_mm()
        loop_s = now_s - start_s
        start_s = time.time()

        # Only update display if we have some light (otherwise it's unreadable anyway)
        if display_on:
            if now_s > display_next_s:
                if _light_level["changed"] or _tof_distance_mm["changed"]:
                    display_values()
                    _light_level["changed"] = False
                    _tof_distance_mm["changed"] = False
                display_next_s = now_s + DISPLAY_TIME_DELAY_S - REFRESH_DELAY_S
                # This ensures we write one last time before turning display off
                display_on = light_lvl >= LIGHT_THRESHOLD
        else:
            # Turn display on when light comes back
            display_on = light_lvl >= LIGHT_THRESHOLD

        blink_rate = 2 if display_on else 4
        rgb1 = level_to_rgb(_light_level, (0x00, -1, 0x00))
        rgb2 = level_to_rgb(_tof_distance_mm, (0x00, 0x00, -1))
        update_leds(now_s % blink_rate == 0, rgb1, rgb2, bright=display_on)

        print(now_s, "+", loop_s, ", lvl:", level_to_str(_light_level), ", tof:", level_to_str(_tof_distance_mm))

        time.sleep(REFRESH_DELAY_S)

if __name__ == "__main__":
    init_display()
    init_vl53l0x()
    # TBD: don't loop if wifi / NTP could not connect, and display error.
    init_wifi()
    loop()



