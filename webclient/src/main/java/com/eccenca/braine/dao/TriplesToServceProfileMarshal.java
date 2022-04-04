package com.eccenca.braine.dao;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;

public class TriplesToServceProfileMarshal extends TriplesToInstanceMarshal<ServiceProfile> {
	
	@Override
	public ServiceProfile marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		ServiceProfile profile = new ServiceProfile(getFirstAsString(ServiceProfile.LABEL_ATTR, instance, attrMapping));
		profile.setDescription(getFirstAsString(ServiceProfile.DESCRIPTION_ATTR, instance, attrMapping));
		profile.setManifest(getFirstAsString(ServiceProfile.MANIFEST_ATTR, instance, attrMapping));
		profile.setVariables(getFirstAsString(ServiceProfile.VARIABLES_ATTR, instance, attrMapping));
		profile.setNumberOfPODs(getFirstAsInt(ServiceProfile.NUMBER_OF_PODS_ATTR, instance, attrMapping));
		profile.setNumberOfCPUs(getFirstAsInt(ServiceProfile.NUMBER_OF_CPUS_ATTR, instance, attrMapping));
		profile.setTenants(getFirstAsInt(ServiceProfile.NUMBER_OF_TENANTS_ATTR, instance, attrMapping));
		profile.setProcessingUnit(getFirstAsString(ServiceProfile.PROCESSING_UNIT_ATTR, instance, attrMapping));
		profile.setStorage(getFirstAsInt(ServiceProfile.STORAGE_ATTR, instance, attrMapping));
		profile.setSlices(getFirstAsInt(ServiceProfile.NUMBER_OF_SLICES_ATTR, instance, attrMapping));
		profile.setUseEncryption(getFirstAsBoolean(ServiceProfile.USE_ENCRYPTION, instance, attrMapping));
		profile.setUri(uri);
		return profile;
	}

}
