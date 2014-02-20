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
import com.seyren.core.util.config.SeyrenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

@Named
public class SNMPNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SNMPNotificationService.class);
    private final SeyrenConfig seyrenConfig;

    private TransportMapping transport;
    private Snmp snmp = null;
    private String oidPrefix;
    private String trapOID;

    @Inject
    public SNMPNotificationService(SeyrenConfig seyrenConfig) {

        this.seyrenConfig = seyrenConfig;
        trapOID = seyrenConfig.getSnmpTrapOID();
        oidPrefix = trapOID.substring(0,trapOID.lastIndexOf('.')+1)+"1";

        try{
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
        }catch (Exception e){
            LOGGER.warn("Error: ", e);
        }
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) {
        PDU pdu;
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(seyrenConfig.getSnmpCommunity()));
        comtarget.setVersion(SnmpConstants.version2c);
        comtarget.setAddress(new UdpAddress(subscription.getTarget()));
        comtarget.setRetries(seyrenConfig.getSnmpRetries());
        comtarget.setTimeout(seyrenConfig.getSnmpTimeout());

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

        LOGGER.debug("Sending snmp trap to "+subscription.getTarget());

        for (Alert alert : alerts) {
            pdu = new PDU();
            pdu.setType(PDU.NOTIFICATION);
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(trapOID)));
            pdu.add(new VariableBinding(new OID(oidPrefix+".1"), new OctetString(alert.getTimestamp().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".2"), new OctetString(hostname.toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".3"), new OctetString(check.getName().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".4"), new OctetString(alert.getTarget())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".5"), new OctetString(alert.getValue().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".6"), new OctetString(alert.getWarn().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".7"), new OctetString(alert.getError().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".8"), new OctetString(alert.getToType().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".9"), new OctetString(alert.getFromType().toString())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".10"), new OctetString(seyrenConfig.getBaseUrl() + "/#/checks/" + check.getId())));
            pdu.add(new VariableBinding(new OID(oidPrefix+".11"), new OctetString(check.getDescription().toString())));
            try {
                snmp.send(pdu,comtarget);
            } catch (IOException e) {
                LOGGER.warn("Error: ", e);
            }
//
        }

    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SNMP;
    }

}
