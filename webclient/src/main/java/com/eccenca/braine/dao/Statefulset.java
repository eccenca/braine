package com.eccenca.braine.dao;

import java.util.Map;

import io.kubernetes.client.openapi.models.V1StatefulSet;

public class Statefulset {

	private V1StatefulSet object = null;
	
	public Statefulset(V1StatefulSet repoService) {
		this.object = repoService;
	}
	
	public String getRepoId() {
		return object.getMetadata().getUid();
	}
	
	public Map<String, String> getRepoLabels() {
		return object.getMetadata().getLabels();
	}

	public V1StatefulSet getObject() {
		return object;
	}
	
	public void setObject(V1StatefulSet profile) {
		this.object = profile;
	}
	
}
