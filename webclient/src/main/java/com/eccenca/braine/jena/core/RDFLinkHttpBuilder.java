package com.eccenca.braine.jena.core;

import java.util.Map;

import org.apache.jena.rdflink.RDFLinkHTTPBuilder;

/**
 * 
 * The {@link RDFLinkHttpBuilder} extends the original {@link org.apache.jena.rdflink.RDFLinkHTTPBuilder}
 * with header parameter and overriding the {@link org.apache.jena.rdflink.RDFLinkHTTPBuilder#buildConnection}
 * returning the {@link RDFLinkHTTP} with header parameter.
 * 
 * 
 * @author edgardmarx
 *
 */
public class RDFLinkHttpBuilder extends RDFLinkHTTPBuilder {
	
	protected Map<String, String> header;
	
	/**
	 * Set the header parameters to be used in the HTTP call.
	 * 
	 * @param header the header to be used in the HTTP call.
	 * 
	 * @return an {@link RDFLinkHttpBuilder}.
	 */
	public RDFLinkHttpBuilder header(Map<String, String> header) {
		this.header = header;
		return this;
	}

	@Override
	protected RDFLinkHTTP buildConnection() {
		RDFLinkHTTP oauth2RDFLinkHTTP = new RDFLinkHTTP(
				txnLifecycle,
				httpClient,
				destination,
				queryURL,
				updateURL,
				gspURL,
				outputQuads,
				outputTriples,
				acceptDataset,
				acceptGraph,
				acceptSparqlResults,
				acceptSelectResult,
				acceptAskResult,
				parseCheckQueries, 
				parseCheckUpdates,
				header);
		return oauth2RDFLinkHTTP;
	}
}
