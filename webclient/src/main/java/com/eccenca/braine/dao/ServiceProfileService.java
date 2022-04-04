package com.eccenca.braine.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author edgardmarx
 *
 */
@Named
@SessionScoped
public class ServiceProfileService {
	
	protected static final Logger logger = LogManager.getLogger();

	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#ServiceProfile";
	private final static TriplesToServceProfileMarshal tripleToObject = new TriplesToServceProfileMarshal();
	private final static ServiceProfileToTripleMarshal objectToTriple = new ServiceProfileToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(ServiceProfile.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ServiceProfile.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ServiceProfile.MANIFEST_ATTR, "https://braine.eccenca.dev/vocabulary/itops#manifest");
		attrMapping.put(ServiceProfile.VARIABLES_ATTR, "https://braine.eccenca.dev/vocabulary/itops#variables");
		attrMapping.put(ServiceProfile.NUMBER_OF_CPUS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#cpus");
		attrMapping.put(ServiceProfile.NUMBER_OF_PODS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#pods");
		attrMapping.put(ServiceProfile.NUMBER_OF_SLICES_ATTR, "https://braine.eccenca.dev/vocabulary/itops#numberOfNetworkSlices");
		attrMapping.put(ServiceProfile.NUMBER_OF_TENANTS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#numberOfNetworkTenants");
		attrMapping.put(ServiceProfile.STORAGE_ATTR, "https://braine.eccenca.dev/vocabulary/itops#ephemeral-storage");
		attrMapping.put(ServiceProfile.PROCESSING_UNIT_ATTR, "https://braine.eccenca.dev/vocabulary/itops#hasProcessingUnitType");
		attrMapping.put(ServiceProfile.USE_ENCRYPTION, "https://braine.eccenca.dev/vocabulary/itops#hasEncryption");
	}

	@Inject
	private SPARQLService sparqlService;

	public List<ServiceProfile> list() {
		return sparqlService.list(DATA_TYPE, tripleToObject, attrMapping);
	}

	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}

	public SPARQLService getSparqlService() {
		return sparqlService;
	}

	public void update(ServiceProfile profile) {
		sparqlService.update(profile, objectToTriple, attrMapping);
	}

	public void insert(ServiceProfile profile) {
		sparqlService.insert(profile, objectToTriple, attrMapping);
	}

	public void delete(ServiceProfile profile) {
		sparqlService.delete(profile.getUri());
	}

	public void delete(String uri) {
		sparqlService.delete(uri);
	}

	public ServiceProfile get(String uri) {
		return sparqlService.get(uri, tripleToObject, attrMapping);
	}
}