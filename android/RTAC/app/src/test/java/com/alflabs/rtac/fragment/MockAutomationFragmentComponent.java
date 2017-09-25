package com.alflabs.rtac.fragment;

import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rx.IStream;
import com.alflabs.utils.InjectionValidator;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAutomationFragmentComponent implements IAutomationFragmentComponent {

    @Mock IStream<DataClientMixin.DataClientStatus> mDataClientStatusStream;

    public MockAutomationFragmentComponent() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    public void inject(AutomationFragment automationFragment) {
        automationFragment.mDataClientMixin = mock(DataClientMixin.class);
        when(automationFragment.mDataClientMixin.getStatusStream()).thenReturn(mDataClientStatusStream);
        InjectionValidator.check(automationFragment);
    }
}
