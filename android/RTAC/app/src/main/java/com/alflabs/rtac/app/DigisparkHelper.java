package com.alflabs.rtac.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.rtac.BuildConfig;

import javax.inject.Inject;
import java.util.HashMap;

public class DigisparkHelper {
    private static final String TAG = DigisparkHelper.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG_VERBOSE = false;

    private static final int DIGISPARK_VID = 0x16C0;
    private static final int DIGISPARK_PID = 0x05DF;

    private static final int REQ_OUT = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_OUT;
    private static final int REQ_IN  = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_IN;

    @NonNull
    private final Context mContext;
    private UsbDevice mLastPermissionFor;

    @Inject
    public DigisparkHelper(@AppQualifier @NonNull Context context) {
        mContext = context;
    }

    @Nullable
    public UsbDevice findDigispark() {
        if (DEBUG_VERBOSE) Log.d(TAG, "@@ findDigispark");
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        assert manager != null;
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        for (UsbDevice device : devices.values()) {
            if (device.getVendorId() == DIGISPARK_VID && device.getProductId() == DIGISPARK_PID) {
                return device;
            }
        }
        return null;
    }

    @Nullable
    public UsbDevice checkPermission(@NonNull UsbDevice device) {
        if (DEBUG) Log.d(TAG, "@@ checkPermission: " + device);
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            if (DEBUG) Log.d(TAG, "@@ checkPermission: USB_SERVICE==null");
            return null;
        }
        if (manager.hasPermission(device)) {
            if (DEBUG) Log.d(TAG, "@@ checkPermission: " + device + " ==> has permission");
            return device;
        }

        // simplification: don't ask twice for the same permission.
        if (mLastPermissionFor == device || (mLastPermissionFor != null && mLastPermissionFor.equals(device))) {
            return null;
        }
        mLastPermissionFor = device;

        Uri u = new Uri.Builder().scheme("usb").path(device.getDeviceName()).build();
        Intent in = new Intent(BootReceiver.ACTION_OPEN_RTAC, u);
        in.putExtra("device", device);  // Parcel
        PendingIntent pi = PendingIntent.getBroadcast(
                mContext,
                0,
                in,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (DEBUG) Log.d(TAG, "@@ requestPermission: " + pi + " ==> " + in);
        manager.requestPermission(device, pi);
        return null;
    }

    @Deprecated
    public UsbDevice parseIntent(@Nullable Intent intent) {
        if (DEBUG) Log.d(TAG, "@@ parseIntent: " + intent);
        if (intent != null &&
                intent.getData() != null &&
                "usb".equals(intent.getData().getScheme())) {
            return intent.getParcelableExtra("device");
        }
        return null;
    }


    public boolean blink(@NonNull UsbDeviceConnection cnx) {
        int res = cnx.controlTransfer(REQ_OUT, 9, 0, 'b', null, 0, 1000);
        if (DEBUG_VERBOSE) Log.d(TAG, "@@ ==> B: " + res);
        return res >= 0;
    }

    /**
     * Reads digispark value.
     * Synchronous call, blocks till gets the reply.
     *
     * @return -1 on error. 0 or 1 on success.
     */
    public int readPirSync(@NonNull UsbDeviceConnection cnx) throws InterruptedException {
        // send t
        int res = cnx.controlTransfer(REQ_OUT, 9, 0, 'r', null, 0, 1000);
        if (DEBUG_VERBOSE) Log.d(TAG, "@@ ==> R: " + res);
        if (res < 0) return -1;

        // read back numbers till we get \n
        byte[] buffer = new byte[16];
        int value = -1;
        while (true) {
            buffer[0] = 4;
            res = cnx.controlTransfer(REQ_IN, 1, 0, 0, buffer, 1, 1000);
            char c = (char) buffer[0];
            if (DEBUG_VERBOSE) Log.d(TAG, "@@ READ: " + res + " = " + c);
            if (res < 0 || c == 4) {
                return -1;
            }
            if (c == '\n') break;
            if (value == -1 && Character.isDigit(c)) {
                value = c - '0';
            }
        }

        if (DEBUG_VERBOSE) Log.d(TAG, "@@ ==> Value: " + value);
        return value;
    }

    public abstract static class DigisparkTask extends AsyncTask<DigisparkHelper, Void, Void> {
        private final boolean mBlinkRepeatedly;
        private volatile boolean mUpdateUsb;
        private UsbDevice mUsbDevice;

        public DigisparkTask(boolean blinkRepeatedly) {
            mBlinkRepeatedly = blinkRepeatedly;
        }

        public boolean isUpdateUsb() {
            return mUpdateUsb;
        }

        public UsbDevice getUsbDevice() {
            return mUsbDevice;
        }

        @Override
        protected Void doInBackground(DigisparkHelper... params) {
            DigisparkHelper helper = params[0];
            while (!isCancelled()) {
                mUpdateUsb = true;
                publishProgress();
                while (mUsbDevice == null && !isCancelled()) {
                    UsbDevice device = helper.findDigispark();
                    if (device != null) {
                        mUsbDevice = helper.checkPermission(device);
                    }
                    if (mUsbDevice == null) {
                        try {
                            Thread.sleep(250 /*ms*/);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }

                if (isCancelled()) return null;

                UsbManager manager = (UsbManager) helper.mContext.getSystemService(Context.USB_SERVICE);
                UsbDeviceConnection cnx = manager.openDevice(mUsbDevice);
                try {
                    onNewDevice();
                    mUpdateUsb = true;
                    publishProgress();
                    mUpdateUsb = false;
                    publishProgress();

                    while (!isCancelled()) {
                        try {
                            Thread.sleep(250 /*ms*/);
                            if (mBlinkRepeatedly) {
                                if (!helper.blink(cnx)) {
                                    break;
                                }
                            }
                            int v = helper.readPirSync(cnx);
                            if (v == -1) {
                                break;
                            } else {
                                onNewValue(v != 0);
                            }
                            publishProgress();
                        } catch (InterruptedException e) {
                            return null; // interrupted on cancel
                        }
                    }
                } finally {
                    if (cnx != null) {
                        cnx.close();
                    }
                    mUsbDevice = null;
                }
            }
            return null;
        }

        /**
         * Notifies that a device has been found. This is called on the async task background thread.
         * <p/>
         * A call to {@link #publishProgress(Object[])} is done just after.
         * Callers can use {@link #onProgressUpdate(Object[])} and check {@link #getUsbDevice()} instead.
         */
        @WorkerThread
        protected abstract void onNewDevice();

        /**
         * Notifies that a new value has been red. This is called on the async task background thread.
         * <p/>
         * A call to {@link #publishProgress(Object[])} is done just after.
         * Callers can use {@link #onProgressUpdate(Object[])} to update anything on the UI thread.
         */
        @WorkerThread
        protected abstract void onNewValue(boolean value);
    }
}
