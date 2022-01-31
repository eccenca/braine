package com.eccenca.braine.jena.core;

import java.util.Map;

import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

/**
 * The {@link RDFRemoteRequestBuilder} extends the original {@link RDFConnectionRemoteBuilder}
 * adding header parameter and using the {@link RDFLinkHttpBuilder} as builder.
 * 
 * @author edgardmarx
 *
 */
public class RDFRemoteRequestBuilder extends RDFConnectionRemoteBuilder {
	
	/**
	 * Constructs an {@link RDFRemoteRequestBuilder} using the {@link RDFLinkHttpBuilder}.
	 */
	public RDFRemoteRequestBuilder() {
		super(new RDFLinkHttpBuilder());
	}
	
	/**
	 * 
	 * @param header the headers to be used in the request.
	 * 
	 * @return a {@link RDFRemoteRequestBuilder}
	 */
	public RDFRemoteRequestBuilder header(Map<String, String> header) {
		((RDFLinkHttpBuilder)builder).header(header);
		return this;
	}
}
