package com.eccenca.braine.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *  ImageRegistryService
 * 
 * @author edgardmarx
 *
 */
@Named
@SessionScoped
public class WorkflowRegistryService {
	
	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#WorkflowRegistry";
	private final static TriplesToWorkflowRegistryMarshal tripleToWorkflow = new TriplesToWorkflowRegistryMarshal();
	private final static WorkflowRegistryToTripleMarshal workflowToTriple = new WorkflowRegistryToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(WorkflowRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(WorkflowRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(WorkflowRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
	}
	
	@Inject
    private SPARQLService sparqlService;

	public  List<WorkflowRegistry> list() {
		return sparqlService.list(DATA_TYPE, tripleToWorkflow, attrMapping);
	}
	
	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}
	
	public SPARQLService getSparqlService() {
		return sparqlService;
	}
	
	public void update(WorkflowRegistry workflowRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(WorkflowRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(WorkflowRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(WorkflowRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.update(workflowRegistry, workflowToTriple, attrMapping);
	}
	
	public void insert(WorkflowRegistry workflowRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(WorkflowRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(WorkflowRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(WorkflowRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.insert(workflowRegistry, workflowToTriple, attrMapping);
	}
	
	public void delete(WorkflowRegistry workflowRegistry) {
		sparqlService.delete(workflowRegistry.getUri());
	}
	
	public void delete(String uri) {
		sparqlService.delete(uri);
	}
}