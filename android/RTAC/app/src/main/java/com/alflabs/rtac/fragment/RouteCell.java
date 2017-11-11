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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.alflabs.annotations.NonNull;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.rtac.R;

public class RouteCell {

    private final RouteInfo mRouteInfo;
    private final View mRoot;
    private final TextView mToggleText;
    private final TextView mStatusText;
    private final TextView mSpeedText;

    public static RouteCell inflate(
            LayoutInflater inflater,
            RouteInfo routeInfo,
            ViewGroup root,
            IKeyValue keyValue) {
        View cellView = inflater.inflate(R.layout.route_cell, root, false);
        RouteCell cell = new RouteCell(routeInfo, cellView);
        cell.onKVChanged(routeInfo.getToggleKey(), keyValue.getValue(routeInfo.getToggleKey()));
        cell.onKVChanged(routeInfo.getStatusKey(), keyValue.getValue(routeInfo.getStatusKey()));
        cell.onKVChanged(routeInfo.getThrottleKey(), keyValue.getValue(routeInfo.getThrottleKey()));
        return cell;
    }

    RouteCell(RouteInfo routeInfo, View root) {
        mRouteInfo = routeInfo;
        mRoot = root;

        mToggleText = root.findViewById(R.id.route_toggle);
        mStatusText = root.findViewById(R.id.route_status);
        mSpeedText = root.findViewById(R.id.route_speed);

        TextView titleText = root.findViewById(R.id.route_name);
        titleText.setText(routeInfo.getName());
    }

    @NonNull
    public View getView() {
        return mRoot;
    }

    public void onKVChanged(String key, String value) {
        if (key.equals(mRouteInfo.getToggleKey())) {
            mToggleText.setText(value);

            int color = mToggleText.getResources().getColor(
                    Constants.On.equals(value) ? R.color.red : R.color.green);
            mToggleText.setTextColor(color);

        } else if (key.equals(mRouteInfo.getStatusKey())) {
            mStatusText.setText(value);

        } else if (key.equals(mRouteInfo.getThrottleKey())) {
            mSpeedText.setText(value);
        }
    }
}
