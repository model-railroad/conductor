/*
 * Project: Conductor
 * Copyright (C) 2024 alf.labs gmail com,
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

package com.alfray.conductor.v2.script.dsl

import com.alflabs.conductor.util.MqttClient
import com.alfray.conductor.v2.dagger.Script2kScope
import javax.inject.Inject

/** Conductor engine exposed access to the MQTT Client. */
@Script2kScope
class MqttPublisher @Inject internal constructor(
    private val mqttClient: MqttClient,
) {
    fun configure(jsonConfigFile: String) {
        mqttClient.configure(jsonConfigFile)
    }

    fun publish(topic: String, message: String) {
        mqttClient.publish(topic, message)
    }
}
