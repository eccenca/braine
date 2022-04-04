package com.eccenca.braine.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *  ProcessingUnitService
 * 
 * @author edgardmarx
 *
 */
@Named
@SessionScoped
public class ProcessingUnitService {
	
	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#ProcessingUnit";
	private final static String GRAPH = "https://braine.eccenca.dev/vocabulary/itops#";
	private final static TriplesToProcessingUnitMarshal tripleToObject = new TriplesToProcessingUnitMarshal();
	private final static ProcessingUnitToTripleMarshal objectToTriple = new ProcessingUnitToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(ProcessingUnit.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ProcessingUnit.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
	}
	
	@Inject
    private SPARQLService sparqlService;

	public  List<ProcessingUnit> list() {
		return sparqlService.list(GRAPH, DATA_TYPE, tripleToObject, attrMapping);
	}
	
	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}
	
	public SPARQLService getSparqlService() {
		return sparqlService;
	}
	
	public void update(ProcessingUnit imageRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ProcessingUnit.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ProcessingUnit.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		sparqlService.update(imageRegistry, objectToTriple, attrMapping);
	}
	
	public void insert(ProcessingUnit imageRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ProcessingUnit.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ProcessingUnit.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		sparqlService.insert(imageRegistry, objectToTriple, attrMapping);
	}
	
	public void delete(ImageRegistry imageRegistry) {
		sparqlService.delete(imageRegistry.getUri());
	}
	
	public void delete(String uri) {
		sparqlService.delete(uri);
	}
}