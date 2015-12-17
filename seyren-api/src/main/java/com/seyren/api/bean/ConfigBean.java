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

import com.seyren.api.jaxrs.ConfigResource;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class ConfigBean implements ConfigResource {
    
    private final SeyrenConfig seyrenConfig;
    
    @Inject
    public ConfigBean(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }
    
    @Override
    public Response getSeyrenConfig() {
        return Response.ok(seyrenConfig).build();
    }
    
}
