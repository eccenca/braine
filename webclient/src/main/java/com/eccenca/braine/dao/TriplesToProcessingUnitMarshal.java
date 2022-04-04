package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToProcessingUnitMarshal extends TriplesToInstanceMarshal<ProcessingUnit> {
	
	@Override
	public ProcessingUnit marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		ProcessingUnit processingUnit = new ProcessingUnit(getFirstAsString(ProcessingUnit.LABEL_ATTR, instance, attrMapping));
		processingUnit.setDescription(getFirstAsString(ProcessingUnit.DESCRIPTION_ATTR, instance, attrMapping));
		processingUnit.setUri(uri);
		return processingUnit;
	}

}
