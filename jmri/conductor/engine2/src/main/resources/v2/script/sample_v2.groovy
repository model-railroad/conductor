package v2.script


import com.alflabs.conductor.v2.script.RootScript
import groovy.transform.BaseScript

@BaseScript RootScript baseScript

// sensors

B310        = block  "NS768"      // 49:1
B311        = block  "NS769"      // 49:2

Toggle      = sensor "NS829"      // 52:14


// turnouts

T311        = turnout "NT311"

// Maps

map {
    name = "Mainline"
    svg  = "Map 1.svg"
}

// JSON tracking

JSON_URL = "@~/bin/JMRI/rtac_json_url.txt"

// GA Tracking

GA_Tracking_Id = "@~/bin/JMRI/rtac_ga_tracking_id.txt"
GA_URL = "http://consist.alfray.com/train/"
