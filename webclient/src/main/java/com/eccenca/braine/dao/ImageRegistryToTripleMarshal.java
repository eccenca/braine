package com.eccenca.braine.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

public class ImageRegistryToTripleMarshal extends AbstractObjectToTripleMarshal implements Marshal<List<Triple>, ImageRegistry> {

	@Override
	public List<Triple> marshal(ImageRegistry instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(ImageRegistry.LABEL_ATTR),
				instance.getName(),
				"en"
		);
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(ImageRegistry.DESCRIPTION_ATTR),
				instance.getDescription(), "en");
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(ImageRegistry.NETWORK_ADDRESS_ATTR),
				instance.getNetworkAddress(), null);

		addResource(triples, 
					instance.getUri(),
					RDF.type.toString(),
					"https://braine.eccenca.dev/vocabulary/itops#DockerRegistry");

		return triples;
	}
}
