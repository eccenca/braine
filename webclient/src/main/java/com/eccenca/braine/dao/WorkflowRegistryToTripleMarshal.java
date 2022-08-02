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
public class WorkflowRegistryToTripleMarshal extends AbstractObjectToTripleMarshal implements Marshal<List<Triple>, WorkflowRegistry> {

	@Override
	public List<Triple> marshal(WorkflowRegistry instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(WorkflowRegistry.LABEL_ATTR),
				instance.getName(),
				"en"
		);
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(WorkflowRegistry.DESCRIPTION_ATTR),
				instance.getDescription(), "en");
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(WorkflowRegistry.NETWORK_ADDRESS_ATTR),
				instance.getNetworkAddress(), null);

		addResource(triples, 
					instance.getUri(),
					RDF.type.toString(),
					"https://braine.eccenca.dev/vocabulary/itops#WorkflowRegistry");

		return triples;
	}
}
