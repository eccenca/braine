package com.eccenca.braine.dao;

import java.util.Arrays;
import java.util.Map;

public class PublishedImage {

	private com.github.dockerjava.api.model.Image repoImage = null;
	private Image image = null;
	
	public PublishedImage(com.github.dockerjava.api.model.Image repoImage) {
		this.repoImage = repoImage;
	}
	
	public String getRepoId() {
		return repoImage.getId();
	}
	
	public String getBeautifulRepoId() {
		String id = repoImage.getId().split(":")[1];
		return id.substring(0, 12);
	}
	
	public Map<String, String> getRepoLabels() {
		return repoImage.getLabels();
	}
	
	public com.github.dockerjava.api.model.Image getRepoImage() {
		return repoImage;
	}
	
	public void setRepoImage(com.github.dockerjava.api.model.Image image) {
		this.repoImage = image;
	}
	
	public String getRepoTags() {
		String[] tags = repoImage.getRepoTags();
		if(tags != null) {
			return Arrays.toString(tags);
		}
		return null;
	}
		
	public Image getImage() {
		return image;
	}
	
	public String getUri() {
		if(image != null) {
			return image.getUri();
		}
		return null;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}
	
	public String getDescription()  {
		if(image != null) {
			return image.getDescription();
		}
		return null;
	}
	
	public String getName()  {
		if(image != null) {
			return image.getName();
		}
		return null;
	}
	
}
