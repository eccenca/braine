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
public class ProcessingUnitToTripleMarshal extends AbstractObjectToTripleMarshal implements Marshal<List<Triple>, ProcessingUnit> {

	@Override
	public List<Triple> marshal(ProcessingUnit instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(ProcessingUnit.LABEL_ATTR),
				instance.getName(),
				"en"
		);

		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(ProcessingUnit.DESCRIPTION_ATTR),
				instance.getDescription(), "en");

		addResource(triples, 
					instance.getUri(),
					RDF.type.toString(),
					"https://braine.eccenca.dev/vocabulary/itops#ProcessingUnit");

		return triples;
	}
}
