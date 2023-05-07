/*
 * Project: Conductor
 * Copyright (C) 2018 alf.labs gmail com,
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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A formal, asynchronous, event logger for Conductor.
 * <p/>
 * The purpose of this logger is to formally record input sensor changes and output action
 * changes performed by Conductor. Events are to be stored locally on files.
 * <p/>
 * This is designed to be invoked from the script engine. To ensure that storage does not affect
 * the performance, the logging calls are asynchronous. A standalone task purges the event buffer
 * to a file.
 */
public class EventLogger {
    private static final String TAG = EventLogger.class.getSimpleName();

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final ILocalDateTimeNowProvider mLocalDateTimeNow;
    private final ExecutorService mExecutorService;

    private static final Event END_LOOP = new Event(LocalTime.of(0, 0, 0, 0), Type.Variable, "Stop", "Internal");

    private volatile boolean mStarted;

    /** A thread-safe queue (safe for single operations with weakly consistent iterators). */
    private final BlockingQueue<Event> mEvents = new LinkedBlockingQueue<>();
    /** The file being written. */
    private File mFile;

    /** Logged type. The log uses the first letter of the Type name. */
    public enum Type {
        // Sort alphabetically. Only the first letter is written in the log,
        Block,
        DccThrottle,
        Sensor,
        Timer,
        Turnout,            // Both Timer and Turnout are logged as T, yet are easily distinguished.
        Variable,
    }

    @Inject
    public EventLogger(
            ILogger logger,
            FileOps fileOps,
            ILocalDateTimeNowProvider localDateTimeNow) {
        mLogger = logger;
        mFileOps = fileOps;
        mLocalDateTimeNow = localDateTimeNow;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public ILogger getLogger() {
        return mLogger;
    }

    /**
     * Generates an async log event. The call time is recorded and printed in the log.
     *
     * @param type The type name. Only the first letter is printed.
     * @param name The object name. Must be a non-empty string without any spaces.
     * @param value The non-empty event value.
     *              If value contains spaces, it is double-quoted in the written log entry.
     */
    public void logAsync(@NonNull Type type, @NonNull String name, @NonNull String value) {
        LocalTime now = mLocalDateTimeNow.getNow().toLocalTime();
        // Add an event (non-blocking)
        Event e = new Event(now, type, name, value);
        mEvents.add(e);
        // Conductor 2 DEBUG
        mLogger.d(TAG, e.toString().trim());
    }

    /**
     * Starts logging to disk.
     * <p/>
     * Can be called multiple times before {@link #shutdown()}, in which case the following calls
     * are no-ops that just return the filename.
     * <p/>
     * Calling this after {@link #shutdown()} should fail and will not restart it.
     *
     * @param logDirectory The directory to use for logging or null for current directory.
     * @return The name of the file being logged to.
     * @throws RejectedExecutionException if trying to call this after {@link #shutdown()}.
     */
    public String start(@Null File logDirectory) {
        mLogger.d(TAG, "Start");

        if (!mStarted) {
            if (logDirectory == null) {
                mLogger.d(TAG, "Hint: customize event log dir by exporting env var $CONDUCTOR_EVENT_LOG_DIR");
                String envDir = System.getenv("CONDUCTOR_EVENT_LOG_DIR");
                if (envDir == null) {
                    envDir = System.getProperty("CONDUCTOR_EVENT_LOG_DIR");
                }
                if (envDir != null) {
                    File dir = new File(envDir);
                    if (mFileOps.isDir(dir)) {
                        logDirectory = dir;
                        mLogger.d(TAG, "Using CONDUCTOR_EVENT_LOG_DIR=" + logDirectory.getPath());
                    } else {
                        mLogger.d(TAG, "Not using invalid directory CONDUCTOR_EVENT_LOG_DIR=" + dir.getPath());
                    }
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String time = mLocalDateTimeNow.getNow().format(formatter);
            String filename = "conductor-log-" + time + ".txt";
            mFile = logDirectory == null ? new File(filename) : new File(logDirectory, filename);

            mStarted = true;

            mExecutorService.execute(() -> {
                mLogger.d(TAG, "Writer thread started for " + mFile.getPath());

                try (Writer w = mFileOps.openFileWriter(mFile, true /*append*/)) {
                    // BlockingQueue.take() waits till an element is available or is interrupted.
                    // We use a special "marker" event to request to end the loop.
                    Event event;
                    while ((event = mEvents.take()) != END_LOOP) {
                        w.write(event.toString());
                        w.flush();
                    }
                } catch (IOException e) {
                    mLogger.d(TAG, "Write Failed", e);
                } catch (InterruptedException e) {
                    mLogger.d(TAG, "Writer thread interrupted");
                }

                mLogger.d(TAG, "Writer thread finished");
                mStarted = false;
            });
        }

        return mFile.getPath();
    }

    /**
     * Shutdown the executor service and sync/wait for it to terminate.
     * This ensures the log file is flushed and closed properly.
     * Attempts to {@link #start(File)} again after this will fail.
     */
    public void shutdown() throws InterruptedException {
        mEvents.add(END_LOOP);
        mExecutorService.shutdown();
        mExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        mLogger.d(TAG, "Shutdown");
    }

    private static class Event {
        private final LocalTime mLocalTime;
        private final Type mType;
        private final String mName;
        private final String mValue;

        public Event(@NonNull LocalTime localTime, @NonNull Type type, @NonNull String name, @NonNull String value) {
            mLocalTime = localTime;
            mType = type;
            mName = name;
            mValue = value;
        }

        public LocalTime getLocalTime() {
            return mLocalTime;
        }

        @NonNull
        public Type getType() {
            return mType;
        }

        @NonNull
        public String getName() {
            return mName;
        }

        @NonNull
        public String getValue() {
            return mValue;
        }

        @Override
        public String toString() {
            String value = mValue;
            if (value.indexOf(' ') != -1) {
                value = '"' + value + '"';
            }
            return String.format("%02d:%02d:%02d.%03d %c %s %s\n",
                    mLocalTime.getHour(),
                    mLocalTime.getMinute(),
                    mLocalTime.getSecond(),
                    mLocalTime.getNano() / 1000000, // from ns to ms
                    mType.name().charAt(0),
                    mName,
                    value);
        }
    }

}
