package com.alflabs.rtac.activity;

import android.hardware.usb.UsbDevice;
import android.support.annotation.WorkerThread;
import com.alflabs.dagger.ActivityScope;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.app.DigisparkHelper;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.utils.ActivityMixin;

import javax.inject.Inject;

@ActivityScope
public class MotionSensorMixin extends ActivityMixin<MainActivity> {
    private static final String TAG = MotionSensorMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject DigisparkHelper mDigispark;
    @Inject DataClientMixin mDataClientMixin;
    @Inject AppPrefsValues mAppPrefsValues;

    private MotionSensorTask mTask;
    private volatile boolean mEstop;

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
        startTask();
    }

    public void onPause() {
        super.onPause();
        stopTask();
    }

    private void stopTask() {
        if (mTask != null) {
            mTask.cancel(true /*interrupt*/);
            mTask = null;
        }
    }

    private void startTask() {
        if (mTask == null && mAppPrefsValues.getConductor_MonitorMotionSensor()) {
            mTask = new MotionSensorTask(mDataClientMixin);
            mTask.execute(mDigispark);
        }
    }

    private static class MotionSensorTask extends DigisparkHelper.DigisparkTask {
        private final DataClientMixin mDataClientMixin;
        private boolean mValue;

        private MotionSensorTask(DataClientMixin dataClientMixin) {
            super(true /* blinkRepeatedly */);
            mDataClientMixin = dataClientMixin;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDataClientMixin.setMotionStatus(false, "");
        }

        @Override
        @WorkerThread
        protected void onNewDevice() {
            // no-op
        }

        @Override
        @WorkerThread
        protected void onNewValue(boolean value) {
            mValue = value;
            mDataClientMixin.setMotionStatus(false, mValue ? "[M]" : "[_]");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (isUpdateUsb()) {
                UsbDevice dev = getUsbDevice();
                mDataClientMixin.setMotionStatus(dev == null, dev ==null ? "Motion Disconnected" : "");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDataClientMixin.setMotionStatus(false, "Motion Stopped");
        }
    }
}
