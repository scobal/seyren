package com.seyren.api.bean;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.seyren.api.jaxrs.ChecksResource;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.store.ChecksStore;

@Named
public class ChecksBean implements ChecksResource {

	private ChecksStore checksStore;

	@Inject
	public ChecksBean(ChecksStore checksStore) {
		this.checksStore = checksStore;
	}
	
	@Override
	public Response getChecks() {
		return Response.ok(checksStore.getChecks()).build();
	}

	@Override
	public Response createCheck(Check check) {
		check.setState(AlertType.OK);
		Check stored = checksStore.createCheck(check);
		return Response.created(uri(stored.getId())).build();
	}
	
	@Override
	public Response updateCheck(String checkId, Check check) {
		Check stored = checksStore.getCheck(checkId);
		if (stored == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		stored = checksStore.saveCheck(check);
		return Response.ok(stored).build();
	}
	
	@Override
	public Response getCheck(String checkId) {
		Check check = checksStore.getCheck(checkId);
		if (check == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(check).build();
	}
	
	@Override
	public Response deleteCheck(String checkId) {
		checksStore.deleteCheck(checkId);
		return Response.noContent().build();
	}
	
	private URI uri(String checkId) {
		try {
			return new URI("checks/" + checkId);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
