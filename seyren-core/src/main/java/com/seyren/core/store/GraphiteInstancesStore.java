/* 
 * Copyright (c) 2013 Expedia, Inc. All rights reserved.
 */
package com.seyren.core.store;

import com.seyren.core.domain.GraphiteInstance;
import com.seyren.core.domain.SeyrenResponse;

/**
 * @author Willie Wheeler (wwheeler@expedia.com)
 */
public interface GraphiteInstancesStore {
	
	SeyrenResponse<GraphiteInstance> getGraphiteInstances();
}
