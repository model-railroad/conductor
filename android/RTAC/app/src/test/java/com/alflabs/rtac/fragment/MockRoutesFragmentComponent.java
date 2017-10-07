package com.alflabs.rtac.fragment;

import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.IStream;
import com.alflabs.utils.InjectionValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockRoutesFragmentComponent implements IRoutesFragmentComponent {

    @Mock IStream<DataClientMixin.DataClientStatus> mDataClientStatusStream;
    @Mock IStream<String> mKVChangedStream;

    public MockRoutesFragmentComponent() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    public void inject(RoutesFragment routesFragment) {
        routesFragment.mDataClientMixin = mock(DataClientMixin.class);
        when(routesFragment.mDataClientMixin.getStatusStream()).thenReturn(mDataClientStatusStream);
        when(routesFragment.mDataClientMixin.getKeyChangedStream()).thenReturn(mKVChangedStream);
        InjectionValidator.check(routesFragment);
    }
}
