package com.eccenca.braine.dao;

import java.util.Map;

import org.apache.jena.query.QuerySolution;


public class QuerySolutionToStringMarshal implements Marshal<String, QuerySolution>{
	
	public final static String STRING_ATTR = "STRING_ATTR";

	@Override
	public String marshal(QuerySolution instance, Map<String, String> mapping) {
		String varName =  mapping.get(STRING_ATTR);
		return instance.getResource(varName).toString();
	}
}
