package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToServiceRegistryMarshal extends TriplesToInstanceMarshal<ServiceRegistry> {
	
	@Override
	public ServiceRegistry marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		ServiceRegistry serviceRegistry = new ServiceRegistry(getFirstAsString(ServiceRegistry.LABEL_ATTR, instance, attrMapping));
		serviceRegistry.setDescription(getFirstAsString(ServiceRegistry.DESCRIPTION_ATTR, instance, attrMapping));
		serviceRegistry.setNetworkAddress(getFirstAsString(ServiceRegistry.NETWORK_ADDRESS_ATTR, instance, attrMapping));
		serviceRegistry.setUri(uri);
		return serviceRegistry;
	}

}
