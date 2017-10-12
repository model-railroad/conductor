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
    @Mock DataClientMixin mDataClientMixin;

    public MockFragmentComponent() {
        MockitoAnnotations.initMocks(this);
        when(mDataClientMixin.getStatusStream()).thenReturn(mDataClientStatusStream);
        when(mDataClientMixin.getKeyChangedStream()).thenReturn(mKVChangedStream);
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
}
