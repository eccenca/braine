package com.eccenca.braine.dao;

import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

public class QuerySolutionToTripleMarshal implements Marshal<Triple, QuerySolution> {
	
	public static final String SUBJECT_ATTR = "SUBJECT_ATTR";
	public static final String PREDICATE_ATTR = "PREDICATE_ATTR";
	public static final String OBJECT_ATTR = "OBJECT_ATTR";

	@Override
	public Triple marshal(QuerySolution instance, Map<String, String> attrMapping) {
		RDFNode s = instance.get(attrMapping.get(SUBJECT_ATTR));
		RDFNode p = instance.get(attrMapping.get(PREDICATE_ATTR));
		RDFNode o = instance.get(attrMapping.get(OBJECT_ATTR));
		Triple t = new Triple(s.asNode(), p.asNode(), o.asNode());
		return t;
	}
}
