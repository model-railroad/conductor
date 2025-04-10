package com.alflabs.rtac.activity;

import android.hardware.usb.UsbDevice;
import androidx.annotation.WorkerThread;
import com.alflabs.dagger.ActivityScope;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.app.DigisparkHelper;
import com.alflabs.rtac.service.AnalyticsMixin;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.ISubscriber;
import com.alflabs.utils.ActivityMixin;
import com.alflabs.utils.IClock;

import javax.inject.Inject;

@ActivityScope
public class MotionSensorMixin extends ActivityMixin<MainActivity> {
    private static final String TAG = MotionSensorMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DigisparkHelper mDigispark;
    @Inject DataClientMixin mDataClientMixin;
    @Inject AnalyticsMixin mAnalyticsMixin;
    @Inject AppPrefsValues mAppPrefsValues;
    @Inject IClock mClock;

    private MotionSensorTask mTask;
    private boolean mIsConnected;
    private boolean mHasKeyChangedSubscriber;

    @Inject
    public MotionSensorMixin(MainActivity activity) {
        super(activity);
    }

    public void onCreate() {
        super.onCreate();
        getActivity().getComponent().inject(this);
    }

    public void onResume() {
        super.onResume();
        stopTask();
        mDataClientMixin.getConnectedStream().subscribe(AndroidSchedulers.mainThread(), mConnectedSubscriber);
        startTask();
    }

    public void onPause() {
        super.onPause();
        mDataClientMixin.getConnectedStream().remove(mConnectedSubscriber);
        mIsConnected = false;
        stopTask();
    }

    private void stopTask() {
        if (mTask != null) {
            mTask.cancel(true /*interrupt*/);
            mTask = null;
        }
        if (mHasKeyChangedSubscriber) {
            mDataClientMixin.getKeyChangedStream().remove(mKeyChangedSubscriber);
            mHasKeyChangedSubscriber = false;
        }
    }

    private void startTask() {
        boolean simulate = mAppPrefsValues.getDev_SimulMotionSensor();
        boolean runMonitor = mAppPrefsValues.getConductor_MonitorMotionSensor() || simulate;

        if (mTask == null && runMonitor && mIsConnected) {
            mTask = new MotionSensorTask(mDataClientMixin, mAnalyticsMixin, mClock, simulate);
            mTask.execute(mDigispark);

        } else if (mTask == null && runMonitor && !mIsConnected && mDataClientMixin.getKeyValueClient() != null) {
            mDataClientMixin.getKeyValueClient().putValue(Constants.RtacMotion, Constants.Disabled, true /* broadcast */);
            mDataClientMixin.setMotionStatus(false, "Motion Disconnected");

        } else if (!runMonitor && !mHasKeyChangedSubscriber) {
            mDataClientMixin.getKeyChangedStream().subscribe(AndroidSchedulers.mainThread(), mKeyChangedSubscriber);
            mHasKeyChangedSubscriber = true;
        }
    }

    private final ISubscriber<Boolean> mConnectedSubscriber = (stream, key) -> {
        mIsConnected = key;
        if (mIsConnected) {
            startTask();
        } else {
            stopTask();
        }
    };

    private final ISubscriber<String> mKeyChangedSubscriber = (stream, key) -> {
        if (!Constants.RtacMotion.equals(key)) {
            return;
        }
        if (mDataClientMixin.getKeyValueClient() == null) {
            return;
        }
        String value = mDataClientMixin.getKeyValueClient().getValue(key);
        if (value == null) {
            return;
        }

        mDataClientMixin.setMotionStatus(false,
                Constants.Disabled.equalsIgnoreCase(value) ? "Motion Disabled"
                : (Constants.On.equalsIgnoreCase(value) ? "Motion ON"
                    : "Motion Off"));
    };

    private static class MotionSensorTask extends DigisparkHelper.DigisparkTask {
        private final DataClientMixin mDataClientMixin;
        private final AnalyticsMixin mAnalyticsMixin;
        private final IClock mClock;
        private final boolean mSimulate;
        private boolean mLastMotion;
        private long mOnTS;
        private long mLastOnSec;

        private MotionSensorTask(
                DataClientMixin dataClientMixin,
                AnalyticsMixin analyticsMixin,
                IClock clock,
                boolean simulate) {
            super(true /* blinkRepeatedly */);
            mDataClientMixin = dataClientMixin;
            mAnalyticsMixin = analyticsMixin;
            mClock = clock;
            mSimulate = simulate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDataClientMixin.setMotionStatus(false, "");
        }

        @Override
        public boolean hasUsbDevice() {
            return mSimulate || super.hasUsbDevice();
        }

        @Override
        protected Void doInBackground(DigisparkHelper... params) {
            if (!mSimulate) {
                return super.doInBackground(params);
            }

            // Simulator: motion on for 5 seconds, then off for 10 seconds.
            onNewDevice();
            setUpdateUsb(true);
            publishProgress();
            setUpdateUsb(false);
            publishProgress();

            int counter = 0;
            while (!isCancelled()) {
                try {
                    onNewValue(counter++ <= 5);
                    if (counter == 15) {
                        counter = 0;
                    }
                    Thread.sleep(1000 /*ms*/);
                    publishProgress();
                } catch (InterruptedException e) {
                    return null; // interrupted on cancel
                }
            }

            return null;
        }

        @Override
        @WorkerThread
        protected void onNewDevice() {
            mAnalyticsMixin.sendEvent("Motion", "Start", "", "RTAC");
        }

        @Override
        @WorkerThread
        protected void onNewValue(boolean motion) {
            boolean changed = motion != mLastMotion;
            if (changed) {
                mLastMotion = motion;
                if (motion) {
                    // off to on
                    mAnalyticsMixin.sendEvent("Motion", "On", "", "RTAC");
                } else {
                    // on to off
                    mAnalyticsMixin.sendEvent("Motion", "Off", "", "RTAC", (int) mLastOnSec);
                }
                if (DEBUG && motion) {
                    mOnTS = mClock.elapsedRealtime();
                }
            }

            if (DEBUG) {
                if (motion) {
                    mLastOnSec = (mClock.elapsedRealtime() - mOnTS) / 1000;
                }
                mDataClientMixin.setMotionStatus(false, "[" + (motion ? '^' : '_') + " " + mLastOnSec + "]");
            } else if (changed) {
                mDataClientMixin.setMotionStatus(false, motion ? "[^]" : "[_]");
            }

            if (changed) {
                IKeyValue kvClient = mDataClientMixin.getKeyValueClient();
                if (kvClient != null) {
                    kvClient.putValue(Constants.RtacMotion, motion ? Constants.On : Constants.Off, true /* broadcast */);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (isUpdateUsb()) {
                boolean hwOK = hasUsbDevice();
                mDataClientMixin.setMotionStatus(!hwOK, !hwOK ? "Motion Disconnected" : "[*]");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            IKeyValue kvClient = mDataClientMixin.getKeyValueClient();
            if (kvClient != null) {
                kvClient.putValue(Constants.RtacMotion, Constants.Disabled, true /* broadcast */);
            }
            mDataClientMixin.setMotionStatus(false, "Motion Stopped");
            mAnalyticsMixin.sendEvent("Motion", "Stop", "", "RTAC");
        }
    }
}
