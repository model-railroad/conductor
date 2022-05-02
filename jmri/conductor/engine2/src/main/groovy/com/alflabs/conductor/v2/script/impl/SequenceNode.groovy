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

package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class SequenceNode {
    private final Block mBlock
    private final SequenceNodeEvents mEvents

    SequenceNode(
            @NonNull Block block,
            @NonNull @DelegatesTo(SequenceNodeEvents) Closure cl) {
        this.mBlock = block
        this.mEvents = new SequenceNodeEvents()
        def code = cl.rehydrate(mEvents /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }

    @NonNull
    Block getBlock() {
        return mBlock
    }

    @NonNull
    SequenceNodeEvents getEvents() {
        return mEvents
    }
}
