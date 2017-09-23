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

import com.seyren.core.domain.*;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Named
public class SnmpTrapNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpTrapNotificationService.class);

    private final SeyrenConfig seyrenConfig;

    private String oidPrefix;
    private String trapOID;
    
    @Inject
    public SnmpTrapNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        
        trapOID = seyrenConfig.getSnmpOID();
        oidPrefix = trapOID.substring(0,trapOID.lastIndexOf('.')+1)+"1";
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
        
        String hostname = "Seyren";

        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            LOGGER.info(addr.toString());
            hostname = addr.getHostName()+":SEYREN";
        }
        catch (UnknownHostException ex)
        {
            LOGGER.error("Hostname can not be resolved");
        }


        for (Alert alert : alerts) {

            // Create PDU           
            PDU trap = new PDU();
            trap.setType(PDU.TRAP);

            trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
            trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(trapOID)));

            //Add Payload
            trap.add(new VariableBinding(new OID(oidPrefix+".1"), new OctetString(alert.getTimestamp().toString())));
            trap.add(new VariableBinding(new OID(oidPrefix+".2"), new OctetString(hostname.toString())));
            trap.add(new VariableBinding(new OID(oidPrefix+".3"), new OctetString(check.getName())));
            trap.add(new VariableBinding(new OID(oidPrefix+".4"), new OctetString(alert.getTarget())));
            trap.add(new VariableBinding(new OID(oidPrefix+".5"), new OctetString(alert.getValue().toString())));

            if(alert instanceof ThresholdAlert)
            {
                ThresholdAlert thresholdAlert = (ThresholdAlert)alert;
                trap.add(new VariableBinding(new OID(oidPrefix+".6"), new OctetString(thresholdAlert.getWarn().toString())));
                trap.add(new VariableBinding(new OID(oidPrefix+".7"), new OctetString(thresholdAlert.getError().toString())));
            }

            else
            {
                OutlierAlert outlierAlert = (OutlierAlert)alert;
                trap.add(new VariableBinding(new OID(oidPrefix+".6"), new OctetString(outlierAlert.getAbsoluteDiff().toString())));
                trap.add(new VariableBinding(new OID(oidPrefix+".7"), new OctetString(outlierAlert.getRelativeDiff().toString())));
            }
            trap.add(new VariableBinding(new OID(oidPrefix+".8"), new OctetString(alert.getToType().toString())));
            trap.add(new VariableBinding(new OID(oidPrefix+".9"), new OctetString(alert.getFromType().toString())));
            trap.add(new VariableBinding(new OID(oidPrefix+".10"), new OctetString(seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId())));
            trap.add(new VariableBinding(new OID(oidPrefix+".11"), new OctetString(check.getDescription() == null ? "" : check.getDescription())));
            
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
