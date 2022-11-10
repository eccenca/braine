package com.eccenca.braine.dao;

public class ServiceRegistry extends Resource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7178756098128692843L;
	
	public static final String LABEL_ATTR = "LABEL_ATTR";
	public static final String DESCRIPTION_ATTR = "DESCRIPTION_ATTR";

	public static final String NETWORK_ADDRESS_ATTR = "NETWORK_ADDRESS_ATTR";
	public static final String CONFIG_ATTR = "CONFIG_ATTR";
	
	private String networkAddress = null;
	private String config = null;
	
	public ServiceRegistry(String name) {
		super(name);
	}

	public void setNetworkAddress(String networkAddress) {
		this.networkAddress = networkAddress;
	}
	
	public String getNetworkAddress() {
		return networkAddress;
	}
	
	public String getConfig() {
		return config;
	}
	
	public void setConfig(String config) {
		this.config = config;
	}
	
}
