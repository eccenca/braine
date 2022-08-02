package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToWorkflowRegistryMarshal extends TriplesToInstanceMarshal<WorkflowRegistry> {
	
	@Override
	public WorkflowRegistry marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		WorkflowRegistry workflowRegistry = new WorkflowRegistry(getFirstAsString(WorkflowRegistry.LABEL_ATTR, instance, attrMapping));
		workflowRegistry.setDescription(getFirstAsString(ImageRegistry.DESCRIPTION_ATTR, instance, attrMapping));
		workflowRegistry.setNetworkAddress(getFirstAsString(ImageRegistry.NETWORK_ADDRESS_ATTR, instance, attrMapping));
		workflowRegistry.setUri(uri);
		return workflowRegistry;
	}

}
