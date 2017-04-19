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

import com.seyren.api.jaxrs.FiltersResource;
import com.seyren.core.domain.Filter;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.store.FiltersStore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Named
public class FiltersBean implements FiltersResource {

    @Inject
    private FiltersStore filtersStore;

    @Override
    public Response getFilters() {
        SeyrenResponse<Filter> filter = filtersStore.getFilters();
        return Response.ok(filter).build();
    }

    @Override
    public Response saveFilter(Filter filter) {
        Filter stored = filtersStore.createFilter(filter);
        return Response.created(uri(stored.getId())).build();
    }

    @Override
    public Response deleteFilter(String filterId) {
        filtersStore.deleteFilter(filterId);
        return Response.noContent().build();
    }

    private URI uri(String filterId) {
        try {
            return new URI("checks/filters/" + filterId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
