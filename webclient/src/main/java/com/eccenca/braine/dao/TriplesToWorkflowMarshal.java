package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToWorkflowMarshal extends TriplesToInstanceMarshal<Workflow> {
	
	@Override
	public Workflow marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		Workflow profile = new Workflow(getFirstAsString(Workflow.LABEL_ATTR, instance, attrMapping));
		profile.setDescription(getFirstAsString(Workflow.DESCRIPTION_ATTR, instance, attrMapping));
		profile.setManifest(getFirstAsString(Workflow.MANIFEST_ATTR, instance, attrMapping));
		profile.setVariables(getFirstAsString(Workflow.VARIABLES_ATTR, instance, attrMapping));
		profile.setUri(uri);
		return profile;
	}

}
