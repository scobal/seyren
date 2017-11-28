
package com.seyren.core.detector;

import com.google.common.base.Optional;
import com.seyren.core.domain.OutlierCheck;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by akharbanda on 02/09/17.
 */
public abstract class AbstractOutlierDetector implements OutlierDetector
{
    protected final OutlierDetectionAlgorithm outlierDetectionAlgorithm;

    public AbstractOutlierDetector(OutlierDetectionAlgorithm outlierDetectionAlgorithm)
    {
        this.outlierDetectionAlgorithm = outlierDetectionAlgorithm;
    }

    @Override
    public abstract List<String> getUnhealthyTargets(Map<String, Optional<BigDecimal>> targetValues ,OutlierCheck check);

}
