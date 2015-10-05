/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.service.schedule;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

import com.seyren.core.store.ChecksStore;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.SeyrenResponse;

public class CheckSchedulerTest {
    
    private ChecksStore mockChecksStore;
    private CheckRunnerFactory mockCheckRunnerFactory;
    private CheckRunner mockCheckRunner;
    private SeyrenConfig mockSeyrenConfig;
    private List<Check> mockChecks;
    private Check atBeginningMockCheck;
    private Check atEndMockCheck;
    private Check nearBeginningMockCheck;
    private Check justPastMiddleMockCheck;
    private Check at82PercentMockCheck;
    
    @Before
    public void before() {
        mockChecksStore = mock(ChecksStore.class);
        mockCheckRunnerFactory = mock(CheckRunnerFactory.class);
        mockCheckRunner = mock(CheckRunner.class);
        mockSeyrenConfig = mock(SeyrenConfig.class);
        
        when(mockSeyrenConfig.getNoOfThreads()).thenReturn(1);
        
        // Mock checks        
        mockChecks = new ArrayList<Check>();

        // Mock check at beginning of range
        atBeginningMockCheck = mock(Check.class);
        when(atBeginningMockCheck.getId()).thenReturn("3f25f8d7-657b-4c41-a43f-19f6340000ec");
        mockChecks.add(atBeginningMockCheck); 
        
        // Mock check at end of range
        atEndMockCheck = mock(Check.class);
        when(atEndMockCheck.getId()).thenReturn("3f25f8d7-657b-4c41-a43f-19f634ffffec");
        mockChecks.add(atEndMockCheck);
        
        // Mock check near beginning of range
        nearBeginningMockCheck = mock(Check.class);
        when(nearBeginningMockCheck.getId()).thenReturn("3f25f8d7-657b-4c41-a43f-19f634000aec");
        mockChecks.add(nearBeginningMockCheck);
        
        // Mock check just past middle of range
        justPastMiddleMockCheck = mock(Check.class);
        when(justPastMiddleMockCheck.getId()).thenReturn("3f25f8d7-657b-4c41-a43f-19f634800aec");
        mockChecks.add(justPastMiddleMockCheck);
        
        // Mock check at 82% of range
        at82PercentMockCheck = mock(Check.class);
        when(at82PercentMockCheck.getId()).thenReturn("3f25f8d7-657b-4c41-a43f-19f634d1eaec");
        mockChecks.add(at82PercentMockCheck);
        
        SeyrenResponse<Check> checks = new SeyrenResponse<Check>().withValues(mockChecks);
        when(mockChecksStore.getChecks(true, false)).thenReturn(checks);
    }

    @SuppressWarnings("unused")
    @Test
    public void verifyInstanceIndexAndTotalInstancesIsRetrieved() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(1);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(1);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStore,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);
        
        verify(mockSeyrenConfig, times(1)).getCheckExecutorInstanceIndex();
        verify(mockSeyrenConfig, times(1)).getCheckExecutorTotalInstances();
    }

    @Test
    public void verifySingleWorkerExecutesAllChecks() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(1);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(1);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStore,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(atBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(atEndMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(nearBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(justPastMiddleMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(at82PercentMockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();
        
        verify(mockChecksStore, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(1)).create(atBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(atEndMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(nearBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(justPastMiddleMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(at82PercentMockCheck);
    }
    
    @Test
    public void verifyFirstWorkerGetsBeginningChecks() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(1);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(5);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStore,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(atBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(atEndMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(nearBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(justPastMiddleMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(at82PercentMockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();
        
        verify(mockChecksStore, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(1)).create(atBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(atEndMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(nearBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(justPastMiddleMockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(at82PercentMockCheck);
    }

    @Test
    public void verifyFifthWorkerGetsLastChecks() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(5);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(5);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStore,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(atBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(atEndMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(nearBeginningMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(justPastMiddleMockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(at82PercentMockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();
        
        verify(mockChecksStore, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(0)).create(atBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(atEndMockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(nearBeginningMockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(justPastMiddleMockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(at82PercentMockCheck);
    }
}
