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
import os
import time
import wifi
from digitalio import DigitalInOut, Direction, Pull

# See https://github.com/micropython/micropython/issues/573 for const() details
from micropython import const

# Bundle libraries
import neopixel
import adafruit_connection_manager
import adafruit_logging as logging
import adafruit_minimqtt.adafruit_minimqtt as MQTT
import adafruit_requests
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

# Core state machine
_CORE_INIT              = const(0)
_CORE_WIFI_CONNECTING   = const(1)
_CORE_WIFI_CONNECTED    = const(2)
_CORE_MQTT_CONNECTING   = const(3)
_CORE_MQTT_CONNECTED    = const(4)
_CORE_MQTT_FAILED       = const(5)
_CORE_MQTT_RECONNECTED  = const(6)
_CORE_MQTT_LOOP         = const(7)

# Google Analytics
_GA4_CLIENT_ID  = ""
_GA4_DEBUG      = const(False)
_GA4_BASE_URL   = const("https://www.google-analytics.com/debug/mp/collect") if _GA4_DEBUG else const("https://www.google-analytics.com/mp/collect")
_GA4_POST_URL   = const("%(GA4_BASE_URL)s?api_secret=%(GA4_API_SECRET)s&measurement_id=%(GA4_MEASURE_ID)s")
_GA4_WIFI_HB_SEC = const(15 * 60)  # 15 minutes
_GA4_EVENT_FILTER = "sr501"       # The base event filter name in GA4. Must not contain underscores. Overriden in settings.toml

# MQTT Topic used. These must match the JMRI expected names:
#    "<channel>/<topic>/<sensor name>"
# https://www.jmri.org/help/en/html/hardware/mqtt/index.shtml for details.
_MQTT_JMRI_CHANNEL = "jmri"            # Overriden in settings.toml
_MQTT_SENSOR_NAME  = "SR501"           # Overriden in settings.toml
_MQTT_SENSOR_TOPIC = "track/sensor"    # where T is MQTT_TURNOUT

_core_state = _CORE_INIT
_led = None
_ga4_post_url = "pending"
_ga4_requests = None
_ga4_events = []
_mqtt = None
_boot_btn = None
_input_a2 = None
_logger = logging.getLogger("Exp")
_logger.setLevel(logging.INFO)      # INFO or DEBUG
_mqtt_sub_topics = []
_last_published_motion = None
_last_motion_on_ts = 0


def init() -> None:
    print("@@ init")
    global _led

    _led = neopixel.NeoPixel(board.NEOPIXEL, 1)
    _led.brightness = 0.1


def init_buttons():
    global _boot_btn
    _boot_btn = digitalio.DigitalInOut(board.D0)
    _boot_btn.switch_to_input(pull = digitalio.Pull.UP)


def init_analytics():
    global _ga4_post_url, _ga4_requests, _GA4_CLIENT_ID
    _GA4_CLIENT_ID  = os.getenv("GA4_CLIENT_ID",  "").strip()
    _ga4_measure_id = os.getenv("GA4_MEASURE_ID", "").strip()
    _ga4_api_secret = os.getenv("GA4_API_SECRET", "").strip()
    if _GA4_CLIENT_ID and _ga4_measure_id and _ga4_api_secret:
        # Enable analytics
        _ga4_post_url =  _GA4_POST_URL % {
            "GA4_BASE_URL": _GA4_BASE_URL,
            "GA4_API_SECRET": _ga4_api_secret,
            "GA4_MEASURE_ID": _ga4_measure_id,
        }
        # Should we share the pool with MQTT?
        pool = adafruit_connection_manager.get_radio_socketpool(wifi.radio)
        ssl_context = adafruit_connection_manager.get_radio_ssl_context(wifi.radio)
        _ga4_requests = adafruit_requests.Session(pool, ssl_context)
        print("@@ GA4: Enabled")
    else:
        # This disables the analytics and prevents queueing events
        _ga4_post_url = ""
        _ga4_requests = None
        print("@@ GA4: Disabled")

    try:
        ga4_event_filter = os.getenv("GA4_EVENT_FILTER", "").strip()
        if ga4_event_filter:
            global _GA4_EVENT_FILTER
            _GA4_EVENT_FILTER = ga4_event_filter
            print("@@ Settings.toml: GA4_EVENT_FILTER set to", _GA4_EVENT_FILTER)
    except Exception as e:
        print("@@ Settings.toml: Invalid GA4_EVENT_FILTER variable ", e)


def init_wifi() -> bool:
    # Return true if wifi is connecting (which may or may not succeed)
    print("@@ WiFI setup")
    # Get wifi AP credentials from onboard settings.toml file
    wifi_ssid = os.getenv("CIRCUITPY_WIFI_SSID", "")
    wifi_password = os.getenv("CIRCUITPY_WIFI_PASSWORD", "")
    print("@@ WiFI SSID:", wifi_ssid)
    if wifi_ssid is None:
        print("@@ WiFI credentials are kept in settings.toml, please add them there!")
        raise ValueError("WiFI SSID not found in environment variables")

    try:
        wifi.radio.connect(wifi_ssid, wifi_password)
        print("@@ WiFI connecting for", wifi_ssid)
        return True
    except ConnectionError:
        print("@@ WiFI Failed to connect to WiFi with provided credentials")
        blink_led_error(_CODE_RETRY, num_loop=5)
        return False


def wifi_rssi() -> int|None:
    try:
        return wifi.radio.ap_info.rssi
    except:
        return None


def init_mqtt() -> None:
    # Modified the global core state if the MQTT connection succeeds
    global _core_state, _mqtt
    host = os.getenv("MQTT_BROKER_IP", "")
    if not host:
        print("@@ MQTT: disabled")
        # This is a core feature so do we not ignore this error in this project
        # and we'll retry again and again and again
        blink_led_error(_CODE_RETRY, num_loop=5)
        return
    port = int(os.getenv("MQTT_BROKER_PORT", 1883))
    user = os.getenv("MQTT_USERNAME", "")
    pasw = os.getenv("MQTT_PASSWORD", "")
    print("@@ MQTT: connect to", host, ", port", port, ", user", user)

    try:
        mqtt_jmri_channel = os.getenv("MQTT_JMRI_CHANNEL", "").strip()
        if mqtt_jmri_channel:
            global _MQTT_JMRI_CHANNEL
            _MQTT_JMRI_CHANNEL = mqtt_jmri_channel
            print("@@ Settings.toml: MQTT_JMRI_CHANNEL set to", _MQTT_JMRI_CHANNEL)
    except Exception as e:
        print("@@ Settings.toml: Invalid MQTT_JMRI_CHANNEL variable ", e)

    try:
        mqtt_sensor_name = os.getenv("MQTT_SENSOR_NAME", "").strip()
        if mqtt_sensor_name:
            global _MQTT_SENSOR_NAME
            _MQTT_SENSOR_NAME = mqtt_sensor_name
            print("@@ Settings.toml: MQTT_SENSOR_NAME set to", _MQTT_SENSOR_NAME)
    except Exception as e:
        print("@@ Settings.toml: Invalid MQTT_SENSOR_NAME variable ", e)


    # Source: https://adafruit-playground.com/u/justmobilize/pages/adafruit-connection-manager
    pool = adafruit_connection_manager.get_radio_socketpool(wifi.radio)

    # Source: https://learn.adafruit.com/mqtt-in-circuitpython/advanced-minimqtt-usage
    _mqtt = MQTT.MQTT(
        broker=host,
        port=port,
        username=user,
        password=pasw,
        is_ssl=False,
        socket_pool=pool,
    )
    _mqtt.logger = _logger

    _mqtt.on_connect = _mqtt_on_connected
    _mqtt.on_disconnect = _mqtt_on_disconnected
    _mqtt.on_message = _mqtt_on_message

    try:
        print("@@ MQTT: connecting...")
        _core_state = _CORE_MQTT_CONNECTING
        _mqtt.connect()
        # Note that on success, the _mqtt_on_connected() callback will have been
        # called before mqtt.connect() returns, which changes the global core state.
    except Exception as e:
        print("@@ MQTT: Failed Connecting with", e)
        blink_led_error(_CODE_RETRY, num_loop=5)
        del _mqtt
        _mqtt = None
        _core_state = _CORE_MQTT_FAILED


def mqtt_publish_motion(motion: bool) -> None:
    global _last_published_motion, _last_motion_on_ts

    # On reconnect, this is called _last_published_motion to voluntarily send
    # the same value to the MQTT broker.
    # _last_published_motion is None at startup, and thus we avoid it here.
    if _mqtt is None or motion is None:
        return

    # Publish to MQTT.
    topic = f"{_MQTT_JMRI_CHANNEL}/{_MQTT_SENSOR_TOPIC}/{_MQTT_SENSOR_NAME}"
    msg   = "ACTIVE" if motion else "INACTIVE"
    _mqtt.publish(topic, msg, qos=1)  # QOS 1 means "at least once"
    print("@@ MQTT: Publish", topic, ":", msg)

    # Update the global state. Only send the GA4 stat if the state has changed.
    if _last_published_motion == motion:
        return
    _last_published_motion = motion

    # Send GA4 event with duration seconds ON as value.
    now = time.monotonic()
    if motion:
        action = "on"
        value = None
        _last_motion_on_ts = now
    else:
        action = "off"
        value = now - _last_motion_on_ts
        _last_motion_on_ts = now
    ga4_mk_event(category="motion", action=action, value=value)


def subscribe_mqtt_topics():
    if _mqtt is None:
        return

    # Unsub all topics
    for topic in _mqtt._subscribed_topics:
        _mqtt.unsubscribe(topic)

    # Subscribe to all changes.
    def _sub(t):
        if t:
            print("@@ MQTT: Subscribe to", t)
            _mqtt.subscribe(t, qos=1)
    for topic in _mqtt_sub_topics:
        _sub(topic)


def _mqtt_on_connected(client, userdata, flags, rc):
    # This function will be called when the client has successfully connected to the broker.
    global _core_state
    _core_state = _CORE_MQTT_CONNECTED
    # Actual subscription is handled by subscribe_mqtt_topics() called from main core state loop.
    print("@Q MQTT: Connected")
    blink_led_error(_CODE_OK, num_loop=0)
    ga4_mk_event(category="mqtt", action="connected", value=wifi_rssi())


def _mqtt_on_disconnected(client, userdata, rc):
    # This method is called when the client is disconnected
    print("@Q MQTT: Disconnected")
    ga4_mk_event(category="mqtt", action="disconnected", value=wifi_rssi())


def _mqtt_on_message(client, topic, message):
    """Method callled when a client's subscribed feed has a new
    value.
    :param str topic: The topic of the feed with a new value.
    :param str message: The new value
    """
    global _mqtt_pending_script, _mqtt_cnx_lost_reconnect_state
    print(f"@Q MQTT: New message on topic {topic}: {message}")
    try:
        # This project does not subscribe (listen) to MQTT topics.
        # if topic == "some/topic":
        #     doSomething()
        pass
    except Exception as e:
        print(f"@@ MQTT: Failed to process {topic}: {message}", e)
        blink_led_error(_CODE_RETRY, num_loop=0)


def mqtt_loop() -> None:
    global _core_state
    if _mqtt is None:
        return
    try:
        # This call has an integrated timeout and takes either 1 or 2 seconds
        # to complete with the default timeout = 1 value.
        _mqtt.loop()
    except Exception as e:
        print("@@ MQTT: Failed with", e)
        blink_led_error(_CODE_RETRY, num_loop=1)
        _core_state = _CORE_MQTT_FAILED


def mqtt_reconnect() -> None:
    global _core_state
    if _mqtt is None:
        return
    try:
        print("@@ MQTT: Reconnect attempt")
        _mqtt.reconnect()
        print("@@ MQTT: Reconnect succeed")
        blink_led_error(_CODE_OK, num_loop=0)
        _core_state = _CORE_MQTT_RECONNECTED
    except Exception as e:
        print("@@ MQTT: Reconnect failed with", e)


def ga4_mk_event(category:str, action:str, extra:str="", value:int|None=None) -> None:
    if not _ga4_post_url:
        # Note that we start with _ga4_post_url set to a dummy value, This allows
        # us to start queuing events before the wifi has connected.
        # However once we initialize the GA4 service, we may disable _ga4_post_url
        # to prevent further events from being queued.
        return
    if extra:
        # Sanitize extra, only keep a-z 0-9;
        extra = extra.lower()
        extra = re.sub("[^a-z0-9]", "", extra)
        extra = "___" + extra

    # Only a-z 0-9 and _ are allowed in the event name
    name = f"{_GA4_EVENT_FILTER}__{category}_{action}{extra}"

    if value is None:
        payload = {
            "client_id": _GA4_CLIENT_ID,
            "events": [ {
                "name": name
            } ] }
    else:
        payload = {
            "client_id": _GA4_CLIENT_ID,
            "events": [ {
                "name": name,
                "params": {
                    "items": [ ],
                    "value": value,
                    "currency": "USD" }
            } ] }
    # Queue the event, don't send it immediately.
    _ga4_events.append(payload)


def ga4_process_queue() -> None:
    if _ga4_requests is None or not _ga4_post_url or not _ga4_events:
        return
    # Sends one event, if any.
    payload = _ga4_events.pop(0)
    try:
        print("@@ GA4: POST payload", payload)
        with _ga4_requests.post(_ga4_post_url, json=payload) as response:
            # 204 is the expected response code and we don't need to know about it
            if response.status_code != 204:
                print("@@ GA4: POST status", response.status_code)
            if _GA4_DEBUG:
                # Note: using response.content or response.json() is only useful
                # with the debug URL to get details on success/failures.
                print("@@ GA4: POST response", response.content.decode())
    except Exception as e:
        print("@@ GA4: Failed with", e)
        blink_led_error(_CODE_RETRY)


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

    _next_wifi_hb_ts = 0
    _old_cs = None
    delta_ts = 0
    motion = None
    while True:
        start_ts = time.monotonic()
        blink_led()

        # Handle core state
        if _core_state == _CORE_INIT:
            if init_wifi():
                _core_state = _CORE_WIFI_CONNECTING
        elif _core_state == _CORE_WIFI_CONNECTING:
            if wifi.radio.connected:
                _core_state = _CORE_WIFI_CONNECTED
        elif _core_state == _CORE_WIFI_CONNECTED:
            init_analytics()
            ga4_mk_event(category="wifi", action="connected", value=wifi_rssi())
            # This sets the core state to either _CORE_MQTT_FAILED or _CORE_MQTT_CONNECTING
            init_mqtt()
        elif _core_state == _CORE_MQTT_CONNECTING:
            # wait for the _mqtt_on_connected() callback to be invoked
            # which changes core state to _CORE_MQTT_CONNECTED
            pass
        elif _core_state == _CORE_MQTT_CONNECTED:
            subscribe_mqtt_topics()
            # Also automatically re-publish the last motion state, if known.
            mqtt_publish_motion(_last_published_motion)
            _core_state = _CORE_MQTT_LOOP
        elif _core_state == _CORE_MQTT_FAILED:
            if _mqtt is None:
                init_mqtt()
            else:
                mqtt_reconnect()
        elif _core_state == _CORE_MQTT_RECONNECTED:
            _core_state = _CORE_MQTT_LOOP
        elif _core_state == _CORE_MQTT_LOOP:
            # The MQTT library loop takes exactly 1 or 2 seconds to complete
            mqtt_loop()
            if start_ts > _next_wifi_hb_ts:
                ga4_mk_event(category="wifi", action="hb", value=wifi_rssi())
                _next_wifi_hb_ts = start_ts + _GA4_WIFI_HB_SEC
            ga4_process_queue()
        if _old_cs != _core_state:
            print("@@ CORE STATE:", _old_cs, "=>", _core_state)
            _old_cs = _core_state

        old_motion = motion
        motion = read_sensor()
        if motion != old_motion:
            mqtt_publish_motion(motion)
            if motion:
                _led.fill(_COL_LED_ERROR[_CODE_MOTION])
                _led.brightness = 0.1
            else:
                _led.fill(_COL_LED_ERROR[_CODE_NO_MOTION])
                _led.brightness = 0.1
            print("@@ MOTION", motion, ":", delta_ts, "s")

        end_ts = time.monotonic()
        delta_ts = end_ts - start_ts
        # prevent busy loop
        if delta_ts < _LOOP_IDLE_SEC: time.sleep(_LOOP_IDLE_SEC - delta_ts)

#~~
