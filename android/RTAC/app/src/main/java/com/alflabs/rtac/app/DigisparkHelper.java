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

/**
 * Helper to find and communicate with a Digispark attached via an USB OTG cable.
 * <p/>
 * The USB service requires the user to authorize access to the device. It will popup a dialog
 * asking for permission for the device to be used. The {@link #checkPermission(UsbDevice)} method
 * below provides a <em>session-only</em> authorization, that does not survives accross reboot or
 * even the app being closed.
 * <p/>
 * For a more permanent grant of the permission, the following must be added to the Android Manifest
 * in the <em>activity</em> that uses this:
 * <pre>
 *   &lt;intent-filter>
 *     &lt;action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
 *   &lt;/intent-filter>
 *   &lt;meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
 *              android:resource="@xml/usb_device_filter" / >
 * </pre>
 * and the corresponding usb_device_filter.xml should have the VID/PID in decimal:
 * <pre>
 *   &lt;resources>
 *     &lt;usb-device vendor-id="5824" product-id="1503" />
 *   &lt;/resources>
 * </pre>
 */
public class DigisparkHelper {
    private static final String TAG = DigisparkHelper.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG_VERBOSE = false;

    private static final int DIGISPARK_VID = 0x16C0;
    private static final int DIGISPARK_PID = 0x05DF;

    private static final int REQ_OUT = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_OUT;
    private static final int REQ_IN  = UsbConstants.USB_TYPE_CLASS + UsbConstants.USB_DIR_IN;
    private static final int POLL_INTERVAL_MS = 1000;

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

    /**
     * Issues a "blink" command by printing 'b' on the USB channel.
     * In the sketch on the Digispark, this takes 100 ms to execute (50 ms each on/off on the on-board LED).
     */
    public boolean blink(@NonNull UsbDeviceConnection cnx) {
        int res = cnx.controlTransfer(REQ_OUT, 9, 0, 'b', null, 0, 1000);
        if (DEBUG_VERBOSE) Log.d(TAG, "@@ ==> B: " + res);
        return res >= 0;
    }

    /**
     * Synchronous call that reads the sensor value from the Digispark by printing a 'r' on the USB channel and
     * blocking till it gets the reply.
     *
     * @return -1 on error. 0 (no motion) or 1 (motion) on success.
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

        protected void setUpdateUsb(boolean updateUsb) {
            mUpdateUsb = updateUsb;
        }

        public UsbDevice getUsbDevice() {
            return mUsbDevice;
        }

        public boolean hasUsbDevice() {
            return mUsbDevice != null;
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
                        _sleepMs(500 /*ms*/);
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

                    // Read the sensor about every second till we're done.
                    // (This is not going to be extremely precise, and there's no need for more control
                    // on the loop iteration). Remove 100 ms since the blink operation takes 100 ms.
                    final long pause = POLL_INTERVAL_MS - (mBlinkRepeatedly ? 100 /*ms*/ : 0);
                    while (!isCancelled()) {
                        try {
                            Thread.sleep(pause /*ms*/);
                            if (mBlinkRepeatedly) {
                                if (!helper.blink(cnx)) {
                                    break;
                                }
                                Thread.sleep(100 /*ms*/);
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
                        try {
                            // Blink at the end to reset the LED state. This can fail.
                            helper.blink(cnx);
                            helper.blink(cnx);
                        } catch (Exception ignore) {}

                        cnx.close();
                    }
                    mUsbDevice = null;
                }
            }
            return null;
        }

        private void _sleepMs(long timeMs) {
            try {
                Thread.sleep(timeMs);
            } catch (InterruptedException ignore) {
            }
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
