package com.eccenca.braine.jena.core;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryType;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecApp;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.http.QueryExecHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecHTTP;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * The {@link RDFLinkHTTP} extends the
 * {@link org.apache.jena.rdflink.RDFLinkHTTP} with OAUTH2 capabilities.
 * 
 * <p>
 * The class re-implements different methods for adding headers to the HTTP
 * client.
 * </p>
 * <p>
 * The re-implementation was necessary because the
 * {@link org.apache.jena.rdflink.RDFLinkHTTP#updateExec} methods are not
 * protected nor public and can not be override.
 * </p>
 * 
 * @author edgardmarx
 *
 */
public class RDFLinkHTTP extends org.apache.jena.rdflink.RDFLinkHTTP {

	protected Map<String, String> header;

	protected RDFLinkHTTP(Transactional txnLifecycle, HttpClient httpClient, String destination, String queryURL,
			String updateURL, String gspURL, RDFFormat outputQuads, RDFFormat outputTriples, String acceptDataset,
			String acceptGraph, String acceptSparqlResults, String acceptSelectResult, String acceptAskResult,
			boolean parseCheckQueries, boolean parseCheckUpdates, Map<String, String> header) {
		super(txnLifecycle, httpClient, destination, queryURL, updateURL, gspURL, outputQuads, outputTriples,
				acceptDataset, acceptGraph, acceptSparqlResults, acceptSelectResult, acceptAskResult, parseCheckQueries,
				parseCheckUpdates);
		this.header = header;
	}

	/**
	 * Operation that passed down the query type so the accept header can be set
	 * without parsing the query string.
	 * 
	 * @param queryString
	 * @param queryType
	 * @return QueryExecution
	 */
	protected QueryExec query(String queryString, QueryType queryType) {
		Objects.requireNonNull(queryString);
		return queryExec(null, queryString, queryType);
	}

	public QueryExec query(String queryString) {
		Objects.requireNonNull(queryString);
		return queryExec(null, queryString, null);
	}

	public QueryExec query(Query query) {
		Objects.requireNonNull(query);
		return queryExec(query, null, null);
	}

	public QueryExecBuilder newQuery() {
		return createQExecBuilder();
	}

	private QueryExec queryExec(Query query, String queryString, QueryType queryType) {
		checkQuery();
		if (query == null && queryString == null)
			throw new InternalErrorException("Both query and query string are null");
		if (query == null) {
			if (parseCheckQueries)
				// Don't retain the query.
				QueryFactory.create(queryString);
		}

		// Use the query string as provided if possible, otherwise serialize the query.
		String queryStringToSend = (queryString != null) ? queryString : query.toString();
		return createQExec(query, queryStringToSend, queryType);
	}

	// Create the QExec

	/** Create a builder, configured with the link setup. */
	private QueryExecHTTPBuilder createQExecBuilder() {
		return QueryExecHTTPBuilder.create()
				.endpoint(svcQuery)
				.httpHeaders(header)
				.httpClient(httpClient);
	}

	private QueryExec createQExec(Query query, String queryStringToSend, QueryType queryType) {
		// [QExec] NO QUERY - delya parse?
		QueryExecHTTPBuilder builder = createQExecBuilder()
				.queryString(queryStringToSend);

		// [QExec] Can this go in QueryExecHTTP at he point the query type is known?
		QueryType qt = queryType;
		if (query != null && qt == null)
			qt = query.queryType();
		if (qt == null)
			qt = QueryType.UNKNOWN;
		// Set the accept header - use the most specific method.
		String requestAcceptHeader = null;
		switch (qt) {
		case SELECT:
			if (acceptSelectResult != null)
				requestAcceptHeader = acceptSelectResult;
			break;
		case ASK:
			if (acceptAskResult != null)
				requestAcceptHeader = acceptAskResult;
			break;
		case DESCRIBE:
		case CONSTRUCT:
			if (acceptGraph != null)
				requestAcceptHeader = acceptGraph;
			break;
		case UNKNOWN:
			// All-purpose content type.
			if (acceptSparqlResults != null)
				requestAcceptHeader = acceptSparqlResults;
			else
				// No idea! Set an "anything" and hope.
				// (Reasonable chance this is going to end up as HTML though.)
				requestAcceptHeader = "*/*";
		default:
			break;
		}

		// Make sure it was set somehow.
		if (requestAcceptHeader == null)
			throw new JenaConnectionException("No Accept header");
		if (requestAcceptHeader != null)
			builder.acceptHeader(requestAcceptHeader);
		// Delayed creation so QueryExecution.setTimeout works.

		builder.queryString(queryStringToSend);
		return QueryExecApp.create(builder, null, query, queryStringToSend);
	}

	public void update(String updateString) {
		Objects.requireNonNull(updateString);
		updateExec(null, updateString);
	}

	public void update(UpdateRequest update) {
		Objects.requireNonNull(update);
		updateExec(update, null);
	}

	protected void updateExec(UpdateRequest update, String updateString) {
		checkUpdate();
		if (update == null && updateString == null)
			throw new InternalErrorException("Both update request and update string are null");
		if (update == null) {
			if (parseCheckUpdates)
				UpdateFactory.create(updateString);
		}
		// Use the update string as provided if possible, otherwise serialize the
		// update.
		String updateStringToSend = (updateString != null) ? updateString : update.toString();
		UpdateExecHTTP
			.newBuilder()
			.endpoint(svcUpdate)
			.httpHeaders(header)
			.httpClient(httpClient)
			.updateString(updateStringToSend).build().execute();
	}
}
