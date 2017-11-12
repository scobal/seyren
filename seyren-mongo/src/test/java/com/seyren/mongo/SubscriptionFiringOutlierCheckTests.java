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
package com.seyren.mongo;

import com.seyren.core.domain.*;
import com.seyren.core.service.checker.DefaultValueChecker;
import com.seyren.core.service.checker.TargetChecker;
import com.seyren.core.service.notification.NotificationService;
import com.seyren.core.service.schedule.CheckRunner;
import com.seyren.core.service.schedule.OutlierCheckRunner;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by akharbanda on 09/11/17.
 */
public class SubscriptionFiringOutlierCheckTests extends AbstractCheckRunTest
{
    /** The values to be used for the target checker */
    private ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
    private MockOutlierDetector mockOutlierDetector = new MockOutlierDetector(new ArrayList<String>());

    @Override
    protected Check getCheck() {
        return this.getDefaultOutlierCheck();
    }

    protected Alert getDefaultErrorAlert(){
        OutlierAlert alert = this.getDefaultOutlierAlert();
        alert.setToType(AlertType.ERROR);
        alert.setConsecutiveAlertCount(1);
        return alert;
    }

    protected Alert getDefaultWarnAlert(){
        OutlierAlert alert = this.getDefaultOutlierAlert();
        alert.setToType(AlertType.WARN);
        return alert;
    }

    protected Alert getDefaultOKAlert(){
        OutlierAlert alert = this.getDefaultOutlierAlert();
        alert.setToType(AlertType.OK);
        alert.setConsecutiveAlertCount(0);
        return alert;
    }

    @Override
    protected CheckRunner getCheckRunner(List<NotificationService> notificationServices , TargetChecker checker)
    {
        return new OutlierCheckRunner(this.check, mongoStore, mongoStore, checker,  new DefaultValueChecker(),
                notificationServices,mockOutlierDetector,"60000");
    }

    @Override
    protected Alert getPreviousAlert() {
        return previousAlert;
    }

    @Override
    protected Subscription getSubscription() {
        return this.getDefaultSubscription();
    }

    @Override
    protected List<BigDecimal> getValues() {
        return values;
    }

    @Override
    protected void additionalSetup()
    {
        OutlierCheckRunner.flushLastAlerts();
    }

    protected void setDefaultValues(){
        values.clear();
        values.add(new BigDecimal(80.0));
    }

    protected void setUnhealthyTarget()
    {
        mockOutlierDetector.setUnhealthyTargets(Arrays.asList(new String[]{"com.launch.check.machine1.target1"}));
    }

    protected void setEmptyUnhealthyTarget()
    {
        mockOutlierDetector.setUnhealthyTargets(new ArrayList<String>());
    }

    @Test
    public void testNewOKAlert(){
        setDefaultValues();
        initialize();
        setEmptyUnhealthyTarget();
        runner.run();
        assertTrue(!((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testNewErrorAlert(){
        setDefaultValues();
        initialize();
        setUnhealthyTarget();
        runner.run();
        assertTrue(((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testOKAfterOKAlert(){
        setDefaultValues();
        this.previousAlert = this.getDefaultOKAlert();
        initialize();
        setEmptyUnhealthyTarget();
        runner.run();
        assertTrue(!((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testErrorAfterOKAlert(){
        setDefaultValues();
        this.previousAlert = this.getDefaultOKAlert();
        initialize();
        setUnhealthyTarget();
        runner.run();
        assertTrue(((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testOKAfterErrorAlert(){
        setDefaultValues();
        this.previousAlert = this.getDefaultErrorAlert();
        initialize();
        setEmptyUnhealthyTarget();
        runner.run();
        assertTrue(((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testErrorAfterErrorAlert(){
        setDefaultValues();
        this.previousAlert = this.getDefaultErrorAlert();
        initialize();
        setUnhealthyTarget();
        runner.run();
        assertTrue(((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testErrorAlertLessThanMinConsecAlertCount()
    {
        setDefaultValues();
        this.previousAlert = this.getDefaultErrorAlert();
        ((OutlierCheck)this.check).setMinConsecutiveViolations(3);
        initialize();
        setUnhealthyTarget();
        runner.run();
        assertTrue(!((MockSubscription)subscription).notificationSent());
    }

    @Test
    public void testErrorAlertEqualToMinConsecAlertCount()
    {
        setDefaultValues();
        this.previousAlert = this.getDefaultErrorAlert();
        ((OutlierCheck)this.check).setMinConsecutiveViolations(2);
        initialize();
        setUnhealthyTarget();
        runner.run();
        assertTrue(((MockSubscription)subscription).notificationSent());
    }

}

