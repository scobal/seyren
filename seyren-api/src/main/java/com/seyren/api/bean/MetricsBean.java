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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import com.google.common.base.Optional;
import com.seyren.api.jaxrs.MetricsResource;
import com.seyren.core.domain.Check;
import com.seyren.core.service.checker.GraphiteTargetChecker;

@Named
public class MetricsBean implements MetricsResource {
    private final GraphiteTargetChecker graphiteTargetChecker;

    @Inject
    public MetricsBean(GraphiteTargetChecker graphiteTargetChecker) {
        this.graphiteTargetChecker = graphiteTargetChecker;
    }

    @Override
    public Response totalMetric(@PathParam("target") String target) {
        try {
            Map<String, Optional<BigDecimal>> targetValues = graphiteTargetChecker.check(new Check().withTarget(target).withName(target));
            String result = convertToJson(target, targetValues);
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    private String convertToJson(String newTarget, Map<String, Optional<BigDecimal>> targetValues) throws IOException {
        JsonFactory jf = new JsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator jg = jf.createJsonGenerator(baos, JsonEncoding.UTF8);
        jg.writeStartObject();
        jg.writeNumberField(newTarget, targetValues.size());
        jg.writeEndObject();
        jg.close();
        String result = baos.toString("UTF8");
        baos.close();
        return result;
    }

}
