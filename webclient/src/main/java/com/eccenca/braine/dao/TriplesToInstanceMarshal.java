package com.eccenca.braine.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public abstract class TriplesToInstanceMarshal <T> implements  Marshal<T, List<Triple>> {

	@Override
	public T marshal(List<Triple> triples, Map<String, String> attrMapping) {
		Map<String, List<Node>> propertiesMap = new HashMap<String, List<Node>>();
		String subject = null;
		for(Triple triple : triples) {
			String property = triple.getPredicate().toString();
			List<Node> propertyList = propertiesMap.get(property);
			if(propertyList == null) {
				propertyList = new ArrayList<Node>();
				propertiesMap.put(property, propertyList);
			}
			propertyList.add(triple.getObject());
			subject = triple.getSubject().toString();
		}
		return marshal(subject, propertiesMap, attrMapping);
	}
	

	protected String getFirstAsString(String attr, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		List<Node> nodes = instance.get(attrMapping.get(attr));
		if(nodes != null && nodes.size() > 0) {
			Node node = nodes.get(0);
			if(node.isLiteral()) {
				return node.getLiteral().getLexicalForm();
			} else {
				return node.toString();
			}
		}
		return null;
	}
	
	protected Integer getFirstAsInt(String attr, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		List<Node> nodes = instance.get(attrMapping.get(attr));
		if(nodes != null && nodes.size() > 0) {
			Node node = nodes.get(0);
			return (Integer) node.getLiteral().getValue();
		}
		return null;
	}

	protected Boolean getFirstAsBoolean(String attr, Map<String, List<Node>> instance,
			Map<String, String> attrMapping) {
		List<Node> nodes = instance.get(attrMapping.get(attr));
		if(nodes != null && nodes.size() > 0) {
			Node node = nodes.get(0);
			return (Boolean) node.getLiteral().getValue();
		}
		return null;
	}

	
	protected List<String> getResourceList(String attr, Map<String, List<Node>> instance, Map<String, String> attrMapping) {
		List<Node> nodes = instance.get(attrMapping.get(attr));
		List<String> resources = new ArrayList<String>();
		if(nodes != null && nodes.size() > 0) {
			Node node = nodes.get(0);
			if(node.isLiteral()) {
				resources.add(node.getLiteral().getLexicalForm());
			} else {
				resources.add(node.toString());
			}
		}
		return resources;
	}
	
	public abstract T marshal(String uri, Map<String, List<Node>> instance, Map<String, String> attrMapping);

}
