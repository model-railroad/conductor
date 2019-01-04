/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
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

package com.alflabs.conductor.util;

import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonSender {
    private static final String TAG = JsonSender.class.getSimpleName();

    private static final boolean DEBUG = false;
    private static final boolean USE_GET = false; // default is POST

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final IClock mClock;
    private final OkHttpClient mOkHttpClient;
    private final ExecutorService mExecutorService;

    private String mJsonUrl;

    @Inject
    public JsonSender(ILogger logger,
                      FileOps fileOps,
                      IClock clock,
                     OkHttpClient okHttpClient) {
        mLogger = logger;
        mFileOps = fileOps;
        mClock = clock;
        mOkHttpClient = okHttpClient;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public void setJsonUrl(String idOrFile) throws IOException {
        if (idOrFile.startsWith("\"") && idOrFile.endsWith("\"") && idOrFile.length() > 2) {
            idOrFile = idOrFile.substring(1, idOrFile.length() - 1);
        }

        if (idOrFile.startsWith("@")) {
            idOrFile = idOrFile.substring(1);
            File file = new File(idOrFile);
            if (idOrFile.startsWith("~") && !mFileOps.isFile(file)) {
                file = new File(System.getProperty("user.home"), idOrFile.substring(1));
            }
            idOrFile = mFileOps.toString(file, Charsets.UTF_8);
            idOrFile = idOrFile.replaceAll("[^A-Z0-9-]", "");
        }
        mJsonUrl = idOrFile;
        mLogger.d(TAG, "JSON Sender URL: " + mJsonUrl);
    }

    public void sendDepart(String name) {

    }
}
