package com.eccenca.braine.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToImageMarshal extends TriplesToInstanceMarshal<Image> {
	
	@Override
	public Image marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		Image image = new Image(getFirstAsString(Image.LABEL_ATTR, instance, attrMapping));
		image.setDescription(getFirstAsString(Image.DESCRIPTION_ATTR, instance, attrMapping));
		image.setManifest(getFirstAsString(Image.MANIFEST_ATTR, instance, attrMapping));
		image.setVariables(getFirstAsString(Image.VARIABLE_ATTR, instance, attrMapping));
		image.setVariableFile(getFirstAsString(Image.VARIABLE_FILE_ATTR, instance, attrMapping));
		image.setManifestFile(getFirstAsString(Image.MANIFEST_FILE_ATTR, instance, attrMapping));
		List<String> imageFiles = getResourceList(Image.FILES_ATTR, instance, attrMapping);
		List<String> uncreepyPrefix = new ArrayList<String>();
		for(String imageFile : imageFiles) {
			uncreepyPrefix.add(imageFile.replace("file:///data/", "")); // for some reason the prefix file://data/ is being 
																		// added to uris without explicit protocols
		}
		image.setFiles(uncreepyPrefix);
		image.setUri(uri);
		return image;
	}

}
