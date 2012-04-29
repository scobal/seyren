package com.graphite.siren.core.store;

import java.util.List;

import com.graphite.siren.core.domain.Check;

public interface ChecksStore {

	List<Check> getChecks();

	Check getCheck(String checkId);

	void deleteCheck(String checkId);

	Check createCheck(Check check);

	Check saveCheck(Check check);

}
