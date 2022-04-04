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
public class ImageRegistryService {
	
	private final static String DATA_TYPE = "https://braine.eccenca.dev/vocabulary/itops#DockerRegistry";
	private final static TriplesToImageRegistryMarshal tripleToImage = new TriplesToImageRegistryMarshal();
	private final static ImageRegistryToTripleMarshal imageToTriple = new ImageRegistryToTripleMarshal();
	private Map<String, String> attrMapping = new HashMap<String, String>();
	{
		attrMapping.put(ImageRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ImageRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ImageRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
	}
	
	@Inject
    private SPARQLService sparqlService;

	public  List<ImageRegistry> list() {
		return sparqlService.list(DATA_TYPE, tripleToImage, attrMapping);
	}
	
	public void setSparqlService(SPARQLService sparqlService) {
		this.sparqlService = sparqlService;
	}
	
	public SPARQLService getSparqlService() {
		return sparqlService;
	}
	
	public void update(ImageRegistry imageRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ImageRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ImageRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ImageRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.update(imageRegistry, imageToTriple, attrMapping);
	}
	
	public void insert(ImageRegistry imageRegistry) {
		Map<String, String> attrMapping = new HashMap<String, String>();
		attrMapping.put(ImageRegistry.DESCRIPTION_ATTR, "http://www.w3.org/2000/01/rdf-schema#comment");
		attrMapping.put(ImageRegistry.LABEL_ATTR, "http://www.w3.org/2000/01/rdf-schema#label");
		attrMapping.put(ImageRegistry.NETWORK_ADDRESS_ATTR, "https://braine.eccenca.dev/vocabulary/itops#address");
		sparqlService.insert(imageRegistry, imageToTriple, attrMapping);
	}
	
	public void delete(ImageRegistry imageRegistry) {
		sparqlService.delete(imageRegistry.getUri());
	}
	
	public void delete(String uri) {
		sparqlService.delete(uri);
	}
}