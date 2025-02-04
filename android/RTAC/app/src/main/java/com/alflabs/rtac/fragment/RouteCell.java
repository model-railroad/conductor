/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.rtac.fragment;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.rtac.R;

public class RouteCell {

    private static final String TAG = RouteCell.class.getSimpleName();

    private final RouteInfo mRouteInfo;
    private final View mRoot;
    private final TextView mToggleText;
    private final TextView mStatusText;
    private final TextView mSpeedText;
    private final TextView mDirText;
    private final TextView mCounterText;

    public static RouteCell inflate(
            LayoutInflater inflater,
            RouteInfo routeInfo,
            ViewGroup root,
            IKeyValue keyValue) {
        View cellView = inflater.inflate(R.layout.route_cell, root, false);
        RouteCell cell = new RouteCell(routeInfo, cellView);

        subscribe(cell, keyValue, routeInfo.getToggleKey());
        subscribe(cell, keyValue, routeInfo.getStatusKey());
        subscribe(cell, keyValue, routeInfo.getCounterKey());
        subscribe(cell, keyValue, routeInfo.getThrottleKey());
        return cell;
    }

    private static void subscribe(RouteCell cell, IKeyValue keyValue, String key) {
        if (key != null) {
            cell.onKVChanged(key, keyValue.getValue(key));
        }
    }

    RouteCell(RouteInfo routeInfo, View root) {
        mRouteInfo = routeInfo;
        mRoot = root;

        mToggleText = root.findViewById(R.id.route_toggle);
        mStatusText = root.findViewById(R.id.route_status);
        mCounterText = root.findViewById(R.id.route_counter);
        mSpeedText = root.findViewById(R.id.route_speed);
        mDirText = root.findViewById(R.id.route_dir);

        TextView titleText = root.findViewById(R.id.route_name);
        titleText.setText(routeInfo.getName());
    }

    @NonNull
    public View getView() {
        return mRoot;
    }

    @SuppressLint("SetTextI18n")
    public void onKVChanged(@NonNull String key, @Null String value) {
        if (value == null) {
            return;
        }
        if (key.equals(mRouteInfo.getToggleKey())) {
            mToggleText.setText(value);

            int color = mToggleText.getResources().getColor(
                    Constants.On.equals(value) ? R.color.red : R.color.green);
            mToggleText.setTextColor(color);

        } else if (key.equals(mRouteInfo.getStatusKey())) {
            mStatusText.setText(value);

        } else if (key.equals(mRouteInfo.getThrottleKey())) {
            try {
                int speed = Integer.parseInt(value);
                mSpeedText.setText(Integer.toString(Math.abs(speed)));
                mDirText.setText(speed < 0 ? "Rev" : (speed > 0 ? "Fwd" : "Stop"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse speed: '" + value + "'", e);
            }

        } else if (key.equals(mRouteInfo.getCounterKey())) {
            mCounterText.setText(value + " Activations");

        }
    }
}
