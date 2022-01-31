package com.eccenca.braine.dao;

import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

public abstract class AbstractObjectToTripleMarshal {
	

	protected void addLiteral(List<Triple> triples, String resourceUri, String propertyURI, String propertyValue, String lang) {
		if(propertyValue != null) {
			Model model = ModelFactory.createDefaultModel();
			org.apache.jena.rdf.model.Resource resource = model.createResource(resourceUri);
			Property property = model.createProperty(propertyURI);
			Literal labelValue = null;
			if(lang != null) {
				labelValue = model.createLiteral(propertyValue, "en");
			} else {
				labelValue = model.createLiteral(propertyValue);
			}
			triples.add(new Triple(resource.asNode(), property.asNode(), labelValue.asNode()));
		}
	}
	
	protected void addResource(List<Triple> triples, String resourceUri, String propertyURI, String propertyValueURI) {
		Model model = ModelFactory.createDefaultModel();
		org.apache.jena.rdf.model.Resource resource = model.createResource(resourceUri);
		Property property = model.createProperty(propertyURI);
		org.apache.jena.rdf.model.Resource	propertyValue = model.createResource(propertyValueURI);
		triples.add(new Triple(resource.asNode(), property.asNode(), propertyValue.asNode()));
	}
	
	protected void addResource(List<Triple> triples, String resourceURI, String propertyURI, List<String> propertyValueURIs) {
		for(String propertyValueURI : propertyValueURIs) {
			addResource(triples, resourceURI, propertyURI, propertyValueURI);
		}
	}
	
	protected void addLiteral(List<Triple> triples, String resourceURI, String propertyURI, List<String> propertyValues) {
		for(String propertyValue : propertyValues) {
			addLiteral(triples, resourceURI, propertyURI, propertyValue, null);
		}
	}

}
