package com.seyren.core.checker;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;

public interface TargetChecker {

	Alert check(Check check) throws Exception;
	
}
