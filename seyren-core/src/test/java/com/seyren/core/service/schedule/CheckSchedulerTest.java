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
    private ChecksStore mockChecksStoreByObjectId;
    private CheckRunnerFactory mockCheckRunnerFactory;
    private CheckRunner mockCheckRunner;
    private SeyrenConfig mockSeyrenConfig;
    private List<Check> mockChecks;
    private Check atBeginningMockCheck;
    private Check atEndMockCheck;
    private Check nearBeginningMockCheck;
    private Check justPastMiddleMockCheck;
    private Check at82PercentMockCheck;
    private List<Check> mockChecksByObjectId;
    private Check index1MockCheck;
    private Check index2MockCheck;
    private Check index3MockCheck;
    private Check index4MockCheck;
    private Check index5MockCheck;

    @Before
    public void before() {
    	// Disabled the check concurrency prvention mechanisms for the purposes of testing
    	CheckConcurrencyGovernor.setEnabled(false);
    	
        mockChecksStore = mock(ChecksStore.class);
        mockCheckRunnerFactory = mock(CheckRunnerFactory.class);
        mockCheckRunner = mock(CheckRunner.class);
        mockSeyrenConfig = mock(SeyrenConfig.class);
        
        when(mockSeyrenConfig.getNoOfThreads()).thenReturn(1);
        
        // Mock checks for Guid-based id values 
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

        // Mock checks for Mongo ObjectId-based id values
        mockChecksStoreByObjectId = mock(ChecksStore.class);

        mockChecksByObjectId = new ArrayList<Check>();
        
        /* MongoDB ObjectId-based ids
         * From http://docs.mongodb.org/manual/reference/object-id/
         * Format is [Timestamp, 4 bytes (8 hex chars)][Machine identifier, 3 bytes][Process id, 2 bytes][randomized counter, 3 bytes]
         * e.g. 000000001111112222333333
         * where 0 is the timestamp portion, 1 is the machine identifier portion,
         * 2 is the process id portion, 3 is the counter portion
         * 
         * After review, the timestamp portion gives the best work distribution for our data
         */
        index1MockCheck = mock(Check.class);
        when(index1MockCheck.getId()).thenReturn("000000010000000000000000");
        mockChecksByObjectId.add(index1MockCheck);
        
        index2MockCheck = mock(Check.class);
        when(index2MockCheck.getId()).thenReturn("000000020000000000000000");
        mockChecksByObjectId.add(index2MockCheck);
        
        index3MockCheck = mock(Check.class);
        when(index3MockCheck.getId()).thenReturn("000000030000000000000000");
        mockChecksByObjectId.add(index3MockCheck);
        
        index4MockCheck = mock(Check.class);
        when(index4MockCheck.getId()).thenReturn("000000040000000000000000");
        mockChecksByObjectId.add(index4MockCheck);

        index5MockCheck = mock(Check.class);
        when(index5MockCheck.getId()).thenReturn("000000050000000000000000");
        mockChecksByObjectId.add(index5MockCheck);
        
        SeyrenResponse<Check> checksByObjectId = new SeyrenResponse<Check>().withValues(mockChecksByObjectId);
        when(mockChecksStoreByObjectId.getChecks(true, false)).thenReturn(checksByObjectId);
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

    @Test
    public void verifySingleWorkerExecutesAllChecks_ObjectIdBased() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(1);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(1);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStoreByObjectId,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(index1MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index2MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index3MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index4MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index5MockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();
        
        verify(mockChecksStoreByObjectId, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(1)).create(index1MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index2MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index3MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index4MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index5MockCheck);
    }
    
    @Test
    public void verifyFirstWorkerOfThreeGetsCorrectChecks_ObjectIdBased() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(1);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(3);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStoreByObjectId,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(index1MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index2MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index3MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index4MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index5MockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();
        
        // With instance index of 1, should run any task whose characters, mod 3, equals 0
        // 01 = 1, 02 = 2, 03 = 0, 04 = 1, 05 = 2, so worker 1 should run check 03
        verify(mockChecksStoreByObjectId, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(0)).create(index1MockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(index2MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index3MockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(index4MockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(index5MockCheck);
    }

    @Test
    public void verifyThirdWorkerOfThreeGetsCorrectChecks_ObjectIdBased() {
        when(mockSeyrenConfig.getCheckExecutorInstanceIndex()).thenReturn(3);
        when(mockSeyrenConfig.getCheckExecutorTotalInstances()).thenReturn(3);
        
        CheckScheduler checkScheduler = new CheckScheduler(
        		mockChecksStoreByObjectId,
        		mockCheckRunnerFactory,
        		mockSeyrenConfig);

        when(mockCheckRunnerFactory.create(index1MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index2MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index3MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index4MockCheck)).thenReturn(mockCheckRunner);
        when(mockCheckRunnerFactory.create(index5MockCheck)).thenReturn(mockCheckRunner);
        
        checkScheduler.performChecks();

        // With instance index of 3, should run any task whose characters, mod 3, equals 2
        // 01 = 1, 02 = 2, 03 = 0, 04 = 1, 05 = 2, so worker 3 should run checks 02 and 05
        
        verify(mockChecksStoreByObjectId, times(1)).getChecks(true, false);
        verify(mockCheckRunnerFactory, times(0)).create(index1MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index2MockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(index3MockCheck);
        verify(mockCheckRunnerFactory, times(0)).create(index4MockCheck);
        verify(mockCheckRunnerFactory, times(1)).create(index5MockCheck);
    }
}
