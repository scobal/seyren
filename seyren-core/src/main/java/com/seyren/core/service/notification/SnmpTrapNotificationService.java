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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Date;

import java.math.BigDecimal;
import java.io.*;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

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
		Snmp snmp = null;

		// Create the SNMP instance
		try{
			snmp = new Snmp(new DefaultUdpTransportMapping());
		}catch(IOException e){
			throw new NotificationFailedException("Sending notification via SNMP trap failed.", e);
		}

		// Specify receiver
		Address targetaddress = new UdpAddress(seyrenConfig.getSnmpHost() + "/" + seyrenConfig.getSnmpPort());
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString( seyrenConfig.getSnmpCommunity() ));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(targetaddress);


	 	for (Alert alert: alerts) {
			
			// Create PDU           
			PDU trap = new PDU();
			trap.setType(PDU.TRAP);

			OID oid = new OID( seyrenConfig.getSnmpOID() );
			trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
			trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000))); // put your uptime here
			trap.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString("Seyren Alarm"))); 

			//Add Payload
			// Add check name
			Variable var = new OctetString( check.getName() );
			trap.add(new VariableBinding(oid, var));          
			// Add target name
			var = new OctetString( alert.getTarget() );
			trap.add(new VariableBinding(oid, var));          
			// Add check state/severity
			var = new OctetString( check.getState().name() );          
			trap.add(new VariableBinding(oid, var));          
			// Add current check value
			var = new OctetString( alert.getValue().toString() );
			trap.add(new VariableBinding(oid, var));          
			// Add check warn value
			var = new OctetString( check.getWarn().toString() );
			trap.add(new VariableBinding(oid, var));          
			// Add check error value
			var = new OctetString( check.getError().toString() );
			trap.add(new VariableBinding(oid, var));          

			// Send
			try{
				if ( snmp != null ) {
					snmp.send(trap, target, null, null);                    
				} else {
					LOGGER.info("Seyren notification failed for {} because the Snmp instance was null",
						check.getName());
				}

			}catch(IOException e){
				throw new NotificationFailedException("Sending notification via SNMP trap failed.", e);
			}
		}

		// Cleanup
		try{
			snmp.close();
		}catch(IOException e){
			throw new NotificationFailedException("Closing SNMP instance failed.", e);
		}
    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SNMP;
    }

    String url(Check check) {
        return String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getName());
    }
}
