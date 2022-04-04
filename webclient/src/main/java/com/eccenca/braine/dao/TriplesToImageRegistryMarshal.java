package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToImageRegistryMarshal extends TriplesToInstanceMarshal<ImageRegistry> {
	
	@Override
	public ImageRegistry marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		ImageRegistry image = new ImageRegistry(getFirstAsString(ImageRegistry.LABEL_ATTR, instance, attrMapping));
		image.setDescription(getFirstAsString(ImageRegistry.DESCRIPTION_ATTR, instance, attrMapping));
		image.setNetworkAddress(getFirstAsString(ImageRegistry.NETWORK_ADDRESS_ATTR, instance, attrMapping));
		image.setUri(uri);
		return image;
	}

}
