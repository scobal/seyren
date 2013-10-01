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
package com.seyren.core.util.graphite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.seyren.core.util.config.GraphiteInstanceConfig;
import com.seyren.core.util.config.SeyrenConfig;

/**
 * Component to manage Graphite HTTP clients for multiple Graphite instances.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Named
public class GraphiteManager {
	
	private final Map<String, GraphiteHttpClient> graphiteHttpClients = new HashMap<String, GraphiteHttpClient>();
	
	@Inject
	public GraphiteManager(SeyrenConfig seyrenConfig) {
		List<GraphiteInstanceConfig> graphiteInstanceConfigs = seyrenConfig.getGraphiteInstanceConfigs();
		for (GraphiteInstanceConfig graphiteInstanceConfig : graphiteInstanceConfigs) {
			graphiteHttpClients.put(graphiteInstanceConfig.getId(), new GraphiteHttpClient(graphiteInstanceConfig));
		}
	}
	
	public GraphiteHttpClient getGraphiteHttpClient(String graphiteInstanceId) {
		return graphiteHttpClients.get(graphiteInstanceId);
	}
}
