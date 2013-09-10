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
package com.seyren.core.store;

import com.seyren.core.domain.GraphiteInstance;
import com.seyren.core.domain.SeyrenResponse;

/**
 * Data access interface for Graphite instances.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public interface GraphiteInstancesStore {

    /**
     * Creates a new Graphite instance in the persistent store.
     * 
     * @param graphiteInstance
     *            Graphite instance to create
     * @return newly-created Graphite instance
     */
    GraphiteInstance createGraphiteInstance(GraphiteInstance graphiteInstance);

    /**
     * Returns all Graphite instances from the persistent store.
     * 
     * @return all Graphite instances
     */
    SeyrenResponse<GraphiteInstance> getGraphiteInstances();

    /**
     * Returns a specific Graphite instance from the persistent store.
     * 
     * @param graphiteInstanceId
     *            Graphite instance ID
     * @return the requested Graphite instance
     */
    GraphiteInstance getGraphiteInstance(String graphiteInstanceId);

    /**
     * Updates the given Graphite instance in the persistent store.
     * 
     * @param graphiteInstance
     *            Graphite instance to update
     * @return the updated Graphite instance
     */
    GraphiteInstance updateGraphiteInstance(GraphiteInstance graphiteInstance);

    /**
     * Deletes the given Graphite instance.
     * 
     * @param graphiteInstanceId
     *            Graphite instance ID
     */
    void deleteGraphiteInstance(String graphiteInstanceId);
}
