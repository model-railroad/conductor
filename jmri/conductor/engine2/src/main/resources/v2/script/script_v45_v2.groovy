/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package v2.script


import com.alflabs.conductor.v2.script.RootScript
import groovy.transform.BaseScript

@BaseScript RootScript baseScript

// sensors

BLParked     = block  "NS752"      // 48:1
BLStation    = block  "NS753"      // 48:2
BLTunnel     = block  "NS754"      // 48:3
BLReverse    = block  "NS755"      // 48:4
BLRun        = block  "NS765"      // 48:14, Disconnected BL activation button
                      
B310         = block  "NS768"      // 49:1
B311         = block  "NS769"      // 49:2
B320         = block  "NS770"      // 49:3
B321         = block  "NS771"      // 49:4
B330         = block  "NS773"      // 49:6
B340         = block  "NS774"      // 49:7
B360         = block  "NS775"      // 49:8
B370         = block  "NS776"      // 49:9

B503a        = block  "NS786"      // 50:3
B503b        = block  "NS787"      // 50:4
AIU_Motion   = sensor "NS797"      // 50:14
                      
BL_Toggle    = sensor "NS828"      // 52:13
PA_Toggle    = sensor "NS829"      // 52:14


// turnouts

T311        = turnout "NT311"
T320        = turnout "NT320"
T321        = turnout "NT321"
T322        = turnout "NT322"
T324        = turnout "NT324"
T326        = turnout "NT326"
T330        = turnout "NT330"
T370        = turnout "NT370"
T504        = turnout "NT504"

// Maps

map {
    name = "Mainline"
    svg  = "Conductor Map Mainline 1.svg"
}

// JSON tracking

JSON_URL = "@~/bin/JMRI/rtac_json_url.txt"

// GA Tracking

GA_Tracking_Id = "@~/bin/JMRI/rtac_ga_tracking_id.txt"
GA_URL = "http://consist.alfray.com/train/"
