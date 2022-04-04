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
public class ImageToTripleMarshal extends AbstractObjectToTripleMarshal implements Marshal<List<Triple>, Image> {

	@Override
	public List<Triple> marshal(Image instance, Map<String, String> attrMapping) {
		List<Triple> triples = new ArrayList<Triple>();
		
		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(Image.LABEL_ATTR),
				instance.getName(),
				"en"
		);
		
		String manifestFile = instance.getManifestFile();
		
		if(manifestFile != null && !manifestFile.isEmpty()) {
			addLiteral(triples,
					instance.getUri(),
					attrMapping.get(Image.MANIFEST_FILE_ATTR),
					instance.getManifestFile(), null);
		} else {
			addLiteral(triples,
					instance.getUri(),
					attrMapping.get(Image.MANIFEST_ATTR),
					instance.getManifest(), null);
		}

		String variablesFile = instance.getVariableFile();
		if(variablesFile != null && !variablesFile.isEmpty()) {
			addLiteral(triples,
					instance.getUri(),
					attrMapping.get(Image.VARIABLE_FILE_ATTR),
					variablesFile, null);
		} else {
			addLiteral(triples,
					instance.getUri(),
					attrMapping.get(Image.VARIABLE_ATTR),
					instance.getVariables(), null);
		}

		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(Image.FILES_ATTR),
				instance.getFiles());

		addLiteral(triples,
				instance.getUri(),
				attrMapping.get(Image.DESCRIPTION_ATTR),
				instance.getDescription(), "en");

		addResource(triples, 
					instance.getUri(),
					RDF.type.toString(),
					"https://braine.eccenca.dev/vocabulary/itops#DockerImage");

		return triples;
	}
}
