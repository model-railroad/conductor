# Experiment: AdaFruit MagTag ESP32 w/ Sensor

An experiment using an AdaFruit MagTag ESP32, a sensor, and an MQTT publication.

Main device is an Adafruit MagTag (ESP32, E-Ink display).
Sensor is onboard ambiant light sensor, or external VL53L0X, or external BD20.
Communication uses WiFi and MQTT.


## Main MagTag Setup

Follow the Adafruit MagTag instructions to load CircuitPython on the MagTag:
https://circuitpython.org/board/adafruit_magtag_2.9_grayscale/

The required libraires can be found in the `lib` folder.

The source is loated in `src/code.py`.
Use `_upload.sy` to copy it automatically.
```shell
cd src
./_lib_upload.sh simpleio
./_lib_upload.sh adafruit_io
./_lib_upload.sh adafruit_ticks
./_lib_upload.sh adafruit_bitmap_font
./_lib_upload.sh adafruit_minimqtt
./_lib_upload.sh adafruit_magtag
```


## VL53L0X Sensor

VL53L0X reference:
https://learn.adafruit.com/adafruit-vl53l0x-micro-lidar-distance-sensor-breakout

```shell
cd src
./_lib_upload.sh adafruit_vl53l0x
# ./_lib_upload.sh adafruit_bus_device (not needed on the MagTag)
```

## MQTT Setup

TBD

~~
