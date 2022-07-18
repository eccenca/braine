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

		addLiteral(triples, instance.getUri(), attrMapping.get(Image.LABEL_ATTR), instance.getName(), "en");

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.MANIFEST_ATTR), instance.getManifest(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.VARIABLES_ATTR), instance.getVariables(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.DESCRIPTION_ATTR),
				instance.getDescription(), "en");

		addResource(triples, instance.getUri(), RDF.type.toString(),
				"https://braine.eccenca.dev/vocabulary/itops#ServiceProfile");

		return triples;
	}
}
