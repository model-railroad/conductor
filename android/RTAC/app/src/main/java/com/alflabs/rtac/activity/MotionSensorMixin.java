package com.alflabs.rtac.activity;

import android.hardware.usb.UsbDevice;
import android.support.annotation.WorkerThread;
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
        mDataClientMixin.getConnectedStream().subscribe(mConnectedSubscriber, AndroidSchedulers.mainThread());
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
    }

    private void startTask() {
        if (mTask == null && mIsConnected && mAppPrefsValues.getConductor_MonitorMotionSensor()) {
            mTask = new MotionSensorTask(mDataClientMixin, mAnalyticsMixin, mClock);
            mTask.execute(mDigispark);
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

    private static class MotionSensorTask extends DigisparkHelper.DigisparkTask {
        private final DataClientMixin mDataClientMixin;
        private final AnalyticsMixin mAnalyticsMixin;
        private final IClock mClock;
        private final IKeyValue mKeyValue;
        private boolean mLastMotion;
        private long mOnTS;
        private long mLastOnSec;

        private MotionSensorTask(
                DataClientMixin dataClientMixin,
                AnalyticsMixin analyticsMixin,
                IClock clock) {
            super(true /* blinkRepeatedly */);
            mDataClientMixin = dataClientMixin;
            mAnalyticsMixin = analyticsMixin;
            mClock = clock;
            mKeyValue = mDataClientMixin.getKeyValueClient();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDataClientMixin.setMotionStatus(false, "");
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
                mKeyValue.putValue(Constants.RtacMotion, motion ? Constants.On : Constants.Off, true /* broadcast */);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (isUpdateUsb()) {
                UsbDevice dev = getUsbDevice();
                mDataClientMixin.setMotionStatus(dev == null, dev ==null ? "Motion Disconnected" : "[*]");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDataClientMixin.setMotionStatus(false, "Motion Stopped");
            mAnalyticsMixin.sendEvent("Motion", "Stop", "", "RTAC");
        }
    }
}
