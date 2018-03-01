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

import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.IStream;
import com.alflabs.utils.InjectionValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class MockFragmentComponent implements IFragmentComponent {

    @Mock IStream<DataClientMixin.DataClientStatus> mDataClientStatusStream;
    @Mock IStream<String> mKVChangedStream;
    @Mock IStream<Boolean> mConnectedStream;
    @Mock DataClientMixin mDataClientMixin;

    public MockFragmentComponent() {
        MockitoAnnotations.initMocks(this);
        when(mDataClientMixin.getStatusStream()).thenReturn(mDataClientStatusStream);
        when(mDataClientMixin.getKeyChangedStream()).thenReturn(mKVChangedStream);
        when(mDataClientMixin.getConnectedStream()).thenReturn(mConnectedStream);
    }

    @Override
    public void inject(RoutesFragment fragment) {
        fragment.mDataClientMixin = mDataClientMixin;
        InjectionValidator.check(fragment);
    }

    @Override
    public void inject(EStopFragment fragment) {
        fragment.mDataClientMixin = mDataClientMixin;
        InjectionValidator.check(fragment);
    }

    @Override
    public void inject(DebugFragment fragment) {
        fragment.mDataClientMixin = mDataClientMixin;
        InjectionValidator.check(fragment);
    }

    @Override
    public void inject(MapFragment fragment) {
        fragment.mDataClientMixin = mDataClientMixin;
        InjectionValidator.check(fragment);
    }

    @Override
    public void inject(StatusFragment fragment) {
        fragment.mDataClientMixin = mDataClientMixin;
        InjectionValidator.check(fragment);
    }
}
