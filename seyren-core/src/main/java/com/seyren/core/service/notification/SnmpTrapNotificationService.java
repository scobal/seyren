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
package com.seyren.core.service.notification;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

@Named
public class SnmpTrapNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpTrapNotificationService.class);

    private final SeyrenConfig seyrenConfig;

    @Inject
    public SnmpTrapNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {

        LOGGER.info("Seyren notification '{}' changed state to '{}' @ '{}' with {} alert(s)",
                check.getName(),
                check.getState().name(),
                url(check),
                alerts.size());

        // Create the SNMP instance
        Snmp snmp = createSnmpConnection();

        // Specify receiver
        Address targetaddress = new UdpAddress(seyrenConfig.getSnmpHost() + "/" + seyrenConfig.getSnmpPort());
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(octetString(seyrenConfig.getSnmpCommunity()));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(targetaddress);


        for (Alert alert : alerts) {

            // Create PDU           
            PDU trap = new PDU();
            trap.setType(PDU.TRAP);

            OID oid = new OID(seyrenConfig.getSnmpOID());
            OID name = new OID(seyrenConfig.getSnmpOID()+".1");
            OID metric = new OID(seyrenConfig.getSnmpOID()+".2");
            OID state = new OID(seyrenConfig.getSnmpOID()+".3");
            OID value = new OID(seyrenConfig.getSnmpOID()+".4");
            OID error = new OID(seyrenConfig.getSnmpOID()+".5");
            OID warn = new OID(seyrenConfig.getSnmpOID()+".6");
            OID id = new OID(seyrenConfig.getSnmpOID()+".7");
	    OID checkUrl = new OID(seyrenConfig.getSnmpOID()+".8");

            trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
            trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));

            //Add Payload
            trap.add(variableBinding(name, check.getName()));
            trap.add(variableBinding(metric, alert.getTarget()));
            trap.add(variableBinding(state, check.getState().name()));
            trap.add(variableBinding(value, alert.getValue().toString()));
            trap.add(variableBinding(warn, check.getWarn().toString()));
            trap.add(variableBinding(error, check.getError().toString()));
            trap.add(variableBinding(id, check.getId()));
	    trap.add(variableBinding(checkUrl, String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId())));

            // Send
            sendAlert(check, snmp, target, trap);
        }

        // Cleanup
        closeSnmpConnection(snmp);
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SNMP;
    }

    private void closeSnmpConnection(Snmp snmp) {
        try {
            snmp.close();
        } catch (IOException e) {
            LOGGER.warn("Closing SNMP instance failed.", e);
        }
    }

    private void sendAlert(Check check, Snmp snmp, CommunityTarget target, PDU trap) {
        try {
            if (snmp != null) {
                snmp.send(trap, target, null, null);
            } else {
                LOGGER.info("Seyren notification failed for {} because the Snmp instance was null", check.getName());
            }
        } catch (IOException e) {
            throw new NotificationFailedException("Sending notification via SNMP trap failed.", e);
        }
    }

    private Snmp createSnmpConnection() {
        try {
            return new Snmp(new DefaultUdpTransportMapping());
        } catch (IOException e) {
            throw new NotificationFailedException("Sending notification via SNMP trap failed.", e);
        }
    }

    private VariableBinding variableBinding(OID oid, String value) {
        return new VariableBinding(oid, octetString(value));
    }

    private OctetString octetString(String value) {
        return new OctetString(value);
    }

    private String url(Check check) {
        return String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getName());
    }
}
