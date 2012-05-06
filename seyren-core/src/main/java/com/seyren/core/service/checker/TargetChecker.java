package com.seyren.core.service.checker;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;

public interface TargetChecker {

	Alert check(Check check) throws Exception;
	
}
