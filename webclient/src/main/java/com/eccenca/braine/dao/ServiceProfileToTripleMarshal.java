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
public class ServiceProfileToTripleMarshal extends AbstractObjectToTripleMarshal
		implements Marshal<List<Triple>, ServiceProfile> {

	@Override
	public List<Triple> marshal(ServiceProfile instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();

		addLiteral(triples, instance.getUri(), attrMapping.get(Image.LABEL_ATTR), instance.getName(), "en");

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.MANIFEST_ATTR), instance.getManifest(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.VARIABLES_ATTR), instance.getVariables(),
				null);

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.DESCRIPTION_ATTR),
				instance.getDescription(), "en");

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.NUMBER_OF_CPUS_ATTR),
				instance.getNumberOfCPUs());

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.NUMBER_OF_TENANTS_ATTR),
				instance.getTenants());

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.NUMBER_OF_PODS_ATTR),
				instance.getNumberOfPODs());

		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.NUMBER_OF_SLICES_ATTR),
				instance.getSlices());
		
		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.STORAGE_ATTR), 
				instance.getStorage());
		
		addLiteral(triples, instance.getUri(), attrMapping.get(ServiceProfile.USE_ENCRYPTION), 
				instance.getUseEncryption());
		
		addResource(triples, instance.getUri(), attrMapping.get(ServiceProfile.PROCESSING_UNIT_ATTR), 
				instance.getProcessingUnit());

		addResource(triples, instance.getUri(), RDF.type.toString(),
				"https://braine.eccenca.dev/vocabulary/itops#ServiceProfile");

		return triples;
	}
}
