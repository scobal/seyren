package com.graphite.siren.core.checker;

import com.graphite.siren.core.domain.Alert;
import com.graphite.siren.core.domain.Check;

public interface TargetChecker {

	Alert check(Check check) throws Exception;
	
}
