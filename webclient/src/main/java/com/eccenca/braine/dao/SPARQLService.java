package com.eccenca.braine.dao;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.eccenca.braine.jena.core.CMEMOAUTH2Authenticator;
import com.eccenca.braine.jena.core.CMEMOAUTH2RemoteRequestBuilder;
import com.eccenca.braine.jena.core.OAUTH2Authenticator.GrantType;

@Named("sparqlService")
@SessionScoped
public class SPARQLService {
	@Value("${client.oauth.user:#{null}}")
	public String user;
	
	@Value("${client.oauth.clientId:#{null}}")
	public String clientId;
	
	@Value("${client.oauth.type:#{null}}")
	public String type;
	
	@Value("${client.oauth.password:#{null}}")
	public String secret;
	
	@Value("${client.server.address:#{null}}")
	public String endpoint;
	
	@Value("${client.triplestore.graph:#{null}}")
	public String graph;
	
	@Value("${client.triplestore.query.select:#{null}}")
	public String selectQueryTemplate;
	
	@Value("${client.triplestore.query.delete:#{null}}")
	public String deleteQueryTemplate;
	
	@Value("${client.triplestore.query.insert:#{null}}")
	public String insertQueryTemplate;
	
	@Value("${client.triplestore.query.list:#{null}}")
	public String listQueryTemplate;
	
	@Value("${client.triplestore.query.update:#{null}}")
	public String updateQueryTemplate;
	
	private static final String URI_TEMPLATE_VAR = "!{uri}";
	private static final String GRAPH_TEMPLATE_VAR = "!{graph}";
	private static final String DATA_TEMPLATE_VAR = "!{data}";
	private static final String FILTER_TEMPLATE_VAR = "!{filter}";
	private static final String TYPE_TEMPLATE_VAR = "!{type}";
	
	private RDFConnectionRemoteBuilder connectionBuilder;
	
	@PostConstruct
	public void init() {
		if(type == null || type.equals("CLIENT_CREDENTIALS")) {
			CMEMOAUTH2Authenticator authenticator = new CMEMOAUTH2Authenticator()
					.clientId(clientId)
					.clientSecret(secret)
					.grantType(GrantType.CLIENT_CREDENTIALS);
			connectionBuilder = new CMEMOAUTH2RemoteRequestBuilder(authenticator)
		            .host(endpoint);
		} else {
			CMEMOAUTH2Authenticator authenticator = new CMEMOAUTH2Authenticator()
					.clientId(clientId)
					.user(user)
					.password(secret)
					.grantType(GrantType.PASSWORD);
			connectionBuilder = new CMEMOAUTH2RemoteRequestBuilder(authenticator)
		            .host(endpoint);
		}
	}
	
	public <T> List<T> query(String query, Marshal<T, QuerySolution> marshal, Map<String, String> attrMapping) {
		List<T> list = new ArrayList<T>();
        try (RDFConnection conn = connectionBuilder.build()) {
	        try(QueryExecution qExec = conn.query(query);) {
	        	ResultSet rs = qExec.execSelect();
	        	while (rs.hasNext()) {
	 				QuerySolution qs = rs.next();
	 				list.add(marshal.marshal(qs, attrMapping));
	 			}
	        }
        }
		return list;
	}
	
	public <T> List<T> list(String type, Marshal<T, List<Triple>> marshal, Map<String, String> attrMapping) {
		return list(graph, type, marshal, attrMapping);
	}
	
	public <T> List<T> list(String graph, String type, Marshal<T, List<Triple>> marshal, Map<String, String> attrMapping) {
		String query = listQueryTemplate.replace(TYPE_TEMPLATE_VAR, type);
		query = query.replace(GRAPH_TEMPLATE_VAR, graph);
		QuerySolutionToStringMarshal entityIndentifiersMarshal = new QuerySolutionToStringMarshal();
		Map<String, String> entityIndentifiersMapping = new HashMap<String, String>();
		entityIndentifiersMapping.put(QuerySolutionToStringMarshal.STRING_ATTR, "s");
		List<String> entitiesIdentifiers = query(query, entityIndentifiersMarshal, entityIndentifiersMapping);
		List<T> entities = new ArrayList<T>();
		for(String entityURI : entitiesIdentifiers) {
			T entity = get(graph, entityURI, marshal, attrMapping);
			entities.add(entity);
		}
		return entities;
	}
	
	public boolean delete(String... uris) {
		uris = preffix(uris, "<");
		uris = suffix(uris, ">");
		String[] filters = preffix(uris, " ?s = ");
		String filter = StringUtils.join(filters, " || ");
		String query = deleteQueryTemplate.replace(FILTER_TEMPLATE_VAR, filter);
		query = query.replace(GRAPH_TEMPLATE_VAR, graph);
		return update(query);
	}
	
	public static String[] preffix(String[] strings, String append) {
	    int i = 0;
	    for(i = 0; i < strings.length; i ++) {
	    	strings[i] = append + strings[i];
	    }
	    return strings;                           
	}
	
	public static String[] suffix(String[] strings, String append) {
	    int i = 0;
	    for(i = 0; i < strings.length; i ++) {
	    	strings[i] = strings[i] + append;
	    }
	    return strings;                           
	}
	
	public <I> boolean insert(I instance, Marshal<List<Triple>, I> instantiator, Map<String, String> attrMapping) {
		List<Triple> triples = instantiator.marshal(instance, attrMapping);
		String data = triplesToNTriple(triples);
		String query = insertQueryTemplate.replace(DATA_TEMPLATE_VAR, data);
		query = query.replace(GRAPH_TEMPLATE_VAR, graph);
		return update(query);
	}
	
	public <I> boolean update(I instance, Marshal<List<Triple>, I> instantiator, Map<String, String> attrMapping) {
		List<Triple> triples = instantiator.marshal(instance, attrMapping);
		String data = triplesToNTriple(triples);
		String resourceURI = triples.get(0).getSubject().toString();
		String query = updateQueryTemplate.replace(URI_TEMPLATE_VAR, resourceURI);
		query = query.replace(DATA_TEMPLATE_VAR, data);
		query = query.replace(GRAPH_TEMPLATE_VAR, graph);
		return update(query);
	}
	
	private String triplesToNTriple(List<Triple> triples) {
		Model m2 = ModelFactory.createDefaultModel();
		for(Triple triple : triples) {
			m2.add(m2.asStatement(triple));
		}
		StringWriter writer = new StringWriter();
		m2.write(writer, "N-triple");
		return writer.toString();
	}
	
	public boolean update(String query) {
		try (RDFConnection conn = connectionBuilder.build()) {
	       	conn.update(query);
	    }
		return true;
	}
	
	public <T> T get(String graph, String uri, Marshal<T, List<Triple>> instantiator, Map<String, String> attrMapping) {
		String query = selectQueryTemplate.replace(URI_TEMPLATE_VAR, uri);
		query = query.replace(GRAPH_TEMPLATE_VAR, graph);
		
		QuerySolutionToTripleMarshal tripleMarshal = new QuerySolutionToTripleMarshal();
		Map<String, String> tripleMapping = new HashMap<String, String>();
		tripleMapping.put(QuerySolutionToTripleMarshal.SUBJECT_ATTR, "s");
		tripleMapping.put(QuerySolutionToTripleMarshal.PREDICATE_ATTR, "p");
		tripleMapping.put(QuerySolutionToTripleMarshal.OBJECT_ATTR, "o");
		List<Triple> triples = query(query, tripleMarshal, tripleMapping);
		
		return instantiator.marshal(triples, attrMapping);
	}
	
	public <T> T get(String uri, Marshal<T, List<Triple>> instantiator, Map<String, String> attrMapping) {
		return get(graph, uri, instantiator, attrMapping);
	}
}