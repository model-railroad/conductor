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

package com.alflabs.conductor.v2;

import com.alflabs.kv.KeyValueServer;
import com.alflabs.manifest.Constants;
import com.alflabs.utils.ILogger;

import java.net.InetSocketAddress;

public class KVServerController {
    private static final String TAG = KVServerController.class.getSimpleName();

    private ILogger mLogger;
    private KeyValueServer mKvServer;

    public void start(ILogger logger, KeyValueServer kvServer) {
        mLogger = logger;
        mKvServer = kvServer;
        InetSocketAddress address = kvServer.start(Constants.KV_SERVER_PORT);
        mLogger.d(TAG, "KV Server available at " + address);
    }

    public void stop() {
        if (mKvServer != null) {
            mKvServer.stopSync();
        }
    }
}
