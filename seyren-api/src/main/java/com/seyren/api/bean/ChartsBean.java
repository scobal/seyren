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

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.util.StringUtils;

import com.seyren.api.jaxrs.ChartsResource;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;
import com.seyren.core.util.graphite.AxesState;
import com.seyren.core.util.graphite.GraphiteHttpClient;
import com.seyren.core.util.graphite.LegendState;

@Named
public class ChartsBean implements ChartsResource {
    
    private final ChecksStore checksStore;
    private final GraphiteHttpClient graphiteHttpClient;
    
    @Inject
    public ChartsBean(ChecksStore checksStore, GraphiteHttpClient graphiteHttpClient) {
        this.checksStore = checksStore;
        this.graphiteHttpClient = graphiteHttpClient;
    }
    
    @Override
    public Response getChart(String checkId, int width, int height, String from, String to, boolean hideThresholds, boolean hideLegend, boolean hideAxes) {
        
        Check check = checksStore.getCheck(checkId);
        if (check == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        String target = check.getTarget();
        
        if (hideThresholds) {
            return getChart(target, width, height, from, to, null, null, hideLegend, hideAxes);
        } else {
            return getChart(target, width, height, from, to, check.getWarn(), check.getError(), hideLegend, hideAxes);
        }
        
    }
    
    @Override
    public Response getCustomChart(String target, int width, int height, String from, String to, String warnThreshold, String errorThreshold, boolean hideLegend,
            boolean hideAxes) {
        
        BigDecimal warn;
        if (StringUtils.isEmpty(warnThreshold)) {
            warn = null;
        } else {
            warn = new BigDecimal(warnThreshold);
        }
        
        BigDecimal error;
        if (StringUtils.isEmpty(errorThreshold)) {
            error = null;
        } else {
            error = new BigDecimal(errorThreshold);
        }
        
        return getChart(target, width, height, from, to, warn, error, hideLegend, hideAxes);
        
    }
    
    private Response getChart(String target, int width, int height, String from, String to, BigDecimal warnThreshold, BigDecimal errorThreshold, boolean hideLegend,
            boolean hideAxes) {
        
        LegendState legendState;
        if (hideLegend) {
            legendState = LegendState.HIDE;
        } else {
            legendState = LegendState.SHOW;
        }
        
        AxesState axesState;
        if (hideAxes) {
            axesState = AxesState.HIDE;
        } else {
            axesState = AxesState.SHOW;
        }
        
        try {
            byte[] bytes = graphiteHttpClient.getChart(target, width, height, from, to, legendState, axesState, warnThreshold, errorThreshold);
            return Response.ok(bytes, "image/png").build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
        
    }
    
}
