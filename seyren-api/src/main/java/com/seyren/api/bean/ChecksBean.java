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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.seyren.api.jaxrs.ChecksResource;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.store.ChecksStore;

@Named
public class ChecksBean implements ChecksResource {
    
    private ChecksStore checksStore;
    
    @Inject
    public ChecksBean(ChecksStore checksStore) {
        this.checksStore = checksStore;
    }
    
    @Override
    public Response getChecks(Set<String> states, Boolean enabled, String name, List<String> fields, List<String> regexes) {
        SeyrenResponse<Check> checks;
        if (states != null && !states.isEmpty()) {
            checks = checksStore.getChecksByState(states, enabled);
        } else if (fields != null && !fields.isEmpty() &&
                regexes != null && !regexes.isEmpty()) {
            List<Pattern> patterns = Lists.transform(regexes, new Function<String, Pattern>() {
                @Override
                public Pattern apply(String regex) {
                    return Pattern.compile(regex);
                }
            });

            checks = checksStore.getChecksByPattern(fields, patterns, enabled);
        } else {
            checks = checksStore.getChecks(enabled, null);
        }
        return Response.ok(checks).build();
    }

    @Override
    public Response createCheck(Check check) {
        if (check.getState() == null) {
            check.setState(AlertType.OK);
        }
        Check stored = checksStore.createCheck(check);
        return Response.created(uri(stored.getId())).build();
    }
    
    @Override
    public Response updateCheck(String checkId, Check check) {
        Check stored = checksStore.getCheck(checkId);
        if (stored == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        stored = checksStore.saveCheck(check);
        return Response.ok(stored).build();
    }
    
    @Override
    public Response getCheck(String checkId) {
        Check check = checksStore.getCheck(checkId);
        if (check == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(check).build();
    }
    
    @Override
    public Response deleteCheck(String checkId) {
        checksStore.deleteCheck(checkId);
        return Response.noContent().build();
    }
    
    private URI uri(String checkId) {
        try {
            return new URI("checks/" + checkId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
}
