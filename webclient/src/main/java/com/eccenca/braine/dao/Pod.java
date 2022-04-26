package com.eccenca.braine.dao;

import java.util.Map;

import io.kubernetes.client.openapi.models.V1Pod;

public class Pod {

	private V1Pod repoService = null;
	
	public Pod(V1Pod repoService) {
		this.repoService = repoService;
	}
	
	public String getRepoId() {
		return repoService.getMetadata().getUid();
	}
	
	public Map<String, String> getRepoLabels() {
		return repoService.getMetadata().getLabels();
	}

	public V1Pod getObject() {
		return repoService;
	}
	
	public void setObject(V1Pod profile) {
		this.repoService = profile;
	}
	
}
