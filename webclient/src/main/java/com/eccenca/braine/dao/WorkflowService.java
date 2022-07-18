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
public class WorkflowService {
	
	protected static final Logger logger = LogManager.getLogger();

	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#ServiceProfile";
	private final static TriplesToWorkflowMarshal tripleToObject = new TriplesToWorkflowMarshal();
	private final static WorkflowToTripleMarshal objectToTriple = new WorkflowToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(ServiceProfile.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ServiceProfile.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ServiceProfile.MANIFEST_ATTR, "https://braine.eccenca.dev/vocabulary/itops#manifest");
		attrMapping.put(ServiceProfile.VARIABLES_ATTR, "https://braine.eccenca.dev/vocabulary/itops#variables");
	}

	@Inject
	private SPARQLService sparqlService;

	public List<Workflow> list() {
		return sparqlService.list(DATA_TYPE, tripleToObject, attrMapping);
	}

	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}

	public SPARQLService getSparqlService() {
		return sparqlService;
	}

	public void update(Workflow profile) {
		sparqlService.update(profile, objectToTriple, attrMapping);
	}

	public void insert(Workflow profile) {
		sparqlService.insert(profile, objectToTriple, attrMapping);
	}

	public void delete(Workflow profile) {
		sparqlService.delete(profile.getUri());
	}

	public void delete(String uri) {
		sparqlService.delete(uri);
	}

	public Workflow get(String uri) {
		return sparqlService.get(uri, tripleToObject, attrMapping);
	}
}