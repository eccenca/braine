package com.eccenca.braine.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class ServiceRegistryService {
	
	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#ServiceRegistry";
	private final static TriplesToServiceRegistryMarshal objectToImage = new TriplesToServiceRegistryMarshal();
	private final static ServiceRegistryToTripleMarshal objectToTriple = new ServiceRegistryToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(ServiceRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ServiceRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ServiceRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
	}
	
	@Inject
    private SPARQLService sparqlService;

	public  List<ServiceRegistry> list() {
		return sparqlService.list(DATA_TYPE, objectToImage, attrMapping);
	}
	
	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}
	
	public SPARQLService getSparqlService() {
		return sparqlService;
	}
	
	public void update(ServiceRegistry serviceRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ServiceRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ServiceRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ServiceRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.update(serviceRegistry, objectToTriple, attrMapping);
	}
	
	public void insert(ServiceRegistry serviceRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ServiceRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ServiceRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ServiceRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.insert(serviceRegistry, objectToTriple, attrMapping);
	}
	
	public void delete(ServiceRegistry serviceRegistry) {
		sparqlService.delete(serviceRegistry.getUri());
	}
	
	public void delete(String uri) {
		sparqlService.delete(uri);
	}
}