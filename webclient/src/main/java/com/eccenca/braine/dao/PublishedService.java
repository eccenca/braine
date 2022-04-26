package com.eccenca.braine.dao;

import java.util.Map;

import io.kubernetes.client.openapi.models.V1Service;

public class PublishedService {

	private V1Service repoService = null;
	private ServiceProfile profile = null;
	
	public PublishedService(V1Service repoService) {
		this.repoService = repoService;
	}
	
	public String getRepoId() {
		return repoService.getMetadata().getUid();
	}
	
	
	public Map<String, String> getRepoLabels() {
		return repoService.getMetadata().getLabels();
	}

	public V1Service getRepoImage() {
		return repoService;
	}
	
	public void setRepoImage(V1Service profile) {
		this.repoService = profile;
	}

	public ServiceProfile getImage() {
		return profile;
	}
	
	public String getUri() {
		if(profile != null) {
			return profile.getUri();
		}
		return null;
	}
	
	public void setService(ServiceProfile profile) {
		this.profile = profile;
	}
	
	public String getDescription()  {
		if(profile != null) {
			return profile.getDescription();
		}
		return null;
	}
	
	public String getName()  {
		if(profile != null) {
			return profile.getName();
		}
		return null;
	}
	
}
