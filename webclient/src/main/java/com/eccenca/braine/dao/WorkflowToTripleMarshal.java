package com.eccenca.braine.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

/**
 * 
 * @author edgardmarx
 *
 */
public class WorkflowToTripleMarshal extends AbstractObjectToTripleMarshal
		implements Marshal<List<Triple>, Workflow> {

	@Override
	public List<Triple> marshal(Workflow instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();

		addLiteral(triples, instance.getUri(), attrMapping.get(Workflow.LABEL_ATTR), instance.getName(), "en");

		addLiteral(triples, instance.getUri(), attrMapping.get(Workflow.MANIFEST_ATTR), instance.getManifest(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(Workflow.VARIABLES_ATTR), instance.getVariables(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(Workflow.DESCRIPTION_ATTR),
				instance.getDescription(), "en");

		addResource(triples, instance.getUri(), RDF.type.toString(),
				"https://braine.eccenca.dev/vocabulary/itops#Workflow");

		return triples;
	}
}
