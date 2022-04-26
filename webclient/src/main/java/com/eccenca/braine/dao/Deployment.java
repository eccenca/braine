package com.eccenca.braine.dao;

import java.util.Map;

import io.kubernetes.client.openapi.models.V1Deployment;

public class Deployment {

	private V1Deployment repoService = null;
	
	public Deployment(V1Deployment repoService) {
		this.repoService = repoService;
	}
	
	public String getRepoId() {
		return repoService.getMetadata().getUid();
	}
	
	public Map<String, String> getRepoLabels() {
		return repoService.getMetadata().getLabels();
	}

	public V1Deployment getRepoImage() {
		return repoService;
	}
	
	public void setRepoImage(V1Deployment profile) {
		this.repoService = profile;
	}
	
}
