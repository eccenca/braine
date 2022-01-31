package com.eccenca.braine.dao;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named
@ViewScoped
public class RedashView {
	
	@Value("${redash.url:#{null}}")
	private String redashUrl;
	
	public String getRedashUrl() {
		return redashUrl;
	}
	
	public void setRedashUrl(String redashUrl) {
		this.redashUrl = redashUrl;
	}

}
