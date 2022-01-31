package com.eccenca.braine.jena.core;

import java.util.Map;

import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 *  Class helper to build a {@link RDFConnectionRemoteBuilder} using OAUTH2 authentication. 
 * 
 * @author edgardmarx
 *
 */
@SuppressWarnings({ "deprecation", "unused" })
public class OAUTH2RemoteRequestBuilder extends RDFRemoteRequestBuilder {

	private static final String AUTHORIZATION_HEADER_PARAM = "Authorization";
	private static final String AUTHORIZATION_HEADER_PARAM_VALUE_PREFIX = "Bearer ";
	
	protected OAUTH2Authenticator authenticator;
	
	/**
	 * Creates an {@link OAUTH2RemoteRequestBuilder} using a given {@link OAUTH2Authenticator}.
	 * 
	 * @param authenticator an {@link OAUTH2Authenticator}.
	 */
	public OAUTH2RemoteRequestBuilder(OAUTH2Authenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	/**
	 * @return An {@link RDFLink} built with an OAUTH2 <code>Bearer</code> authorisation header
	 *  using the given {@link OAUTH2Authenticator}.
	 *  
	 * @exception {@link RuntimeException} in case an error occurs while retrieving 
	 * the access token such as authentication failure.
	 */
	public RDFLink buildLink() {
		String token;
		try {
			token = authenticator.getToken();
	    	Map<String, String> header = new java.util.HashMap<String, String>();
	    	header.put(AUTHORIZATION_HEADER_PARAM, AUTHORIZATION_HEADER_PARAM_VALUE_PREFIX + token);
			header(header);
			return super.buildLink();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
