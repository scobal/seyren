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
package com.seyren.api.bean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import com.seyren.api.jaxrs.AlertsResource;
import com.seyren.api.util.DateTimeParam;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.store.AlertsStore;

@Named
public class AlertsBean implements AlertsResource {
    
    private AlertsStore alertsStore;
    
    @Inject
    public AlertsBean(AlertsStore alertsStore) {
        this.alertsStore = alertsStore;
    }
    
    @Override
    public Response getAlertsForCheck(String checkId, int start, int items) {
        if (start < 0 || items < 0) {
            return Response.status(400).build();
        }
        SeyrenResponse<Alert> response = alertsStore.getAlerts(checkId, start, items);
        return Response.ok(response).build();
    }
    
    @Override
    public Response deleteAlertsForCheck(String checkId, DateTimeParam before) {
        alertsStore.deleteAlerts(checkId, before.value());
        return Response.noContent().build();
    }
    
    @Override
    public Response getAlerts(int start, int items) {
        if (start < 0 || items < 0) {
            return Response.status(400).build();
        }
        SeyrenResponse<Alert> response = alertsStore.getAlerts(start, items);
        return Response.ok(response).build();
    }
    
}
