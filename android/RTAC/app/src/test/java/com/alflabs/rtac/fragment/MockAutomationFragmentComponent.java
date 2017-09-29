package com.alflabs.rtac.fragment;

import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.IStream;
import com.alflabs.utils.InjectionValidator;
import com.alflabs.utils.RPair;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAutomationFragmentComponent implements IAutomationFragmentComponent {

    @Mock IStream<DataClientMixin.DataClientStatus> mDataClientStatusStream;
    @Mock IStream<RPair<String, String>> mKVChangedStream;

    public MockAutomationFragmentComponent() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    public void inject(AutomationFragment automationFragment) {
        automationFragment.mDataClientMixin = mock(DataClientMixin.class);
        when(automationFragment.mDataClientMixin.getStatusStream()).thenReturn(mDataClientStatusStream);
        when(automationFragment.mDataClientMixin.getKVChangedStream()).thenReturn(mKVChangedStream);
        InjectionValidator.check(automationFragment);
    }
}
