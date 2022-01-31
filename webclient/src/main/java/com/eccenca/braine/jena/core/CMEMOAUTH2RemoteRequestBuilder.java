package com.eccenca.braine.jena.core;

import org.apache.jena.rdflink.RDFLink;

/**
 * Class helper to build a {@link RDFConnectionRemoteBuilder} using eccenca Corporate Memory (CMEM). 
 * 
 * @author edgardmarx
 *
 */
public class CMEMOAUTH2RemoteRequestBuilder extends OAUTH2RemoteRequestBuilder {
	private static final String DEFAULT_UPDATE_ENDPOINT_SUFFIX = "/dataplatform/proxy/default/update";
	private static final String DEFAULT_QUERY_ENDPOINT_SUFFIX = "/dataplatform/proxy/default/sparql";
	private static final RuntimeException HOST_NOT_DEFINED_EXCEPTION = new RuntimeException("Host can not be null.");
	
	private String host = null;
	private String queryEndpoint = null;
	private String updateEndpoint = null;

	/**
	 * Construct a @link CMEMOAUTH2RemoteRequestBuilder} given the {@link CMEMOAUTH2Authenticator}.
	 * 
	 * @param authenticator the {@link CMEMOAUTH2Authenticator}.
	 */
	public CMEMOAUTH2RemoteRequestBuilder(CMEMOAUTH2Authenticator authenticator) {
		super(authenticator);
	}
	
	/**
	 * @return An {@link RDFLink} built with an OAUTH2 <code>Bearer</code> authorisation header
	 *  using the given {@link CMEMOAUTH2Authenticator}.
	 *  
	 * @exception {@link RuntimeException} in case an error occurs while retrieving 
	 * the access token such as authentication failure or the host and the query or update endpoints are unset.
	 */
	public CMEMOAUTH2RemoteRequestBuilder host(String host) {
		this.host = host;
		((CMEMOAUTH2Authenticator)authenticator).host(host);
		return this;
	}
	
	/**
	 * Set an query endpoint.
	 * 
	 * <p>The default query endpoint is {@link CMEMOAUTH2RemoteRequestBuilder#host} + <code>/dataplatform/proxy/default/query</code>.</p>
	 * 
	 * @return An {@link CMEMOAUTH2RemoteRequestBuilder} with the given query endpoint.
	 */
	public CMEMOAUTH2RemoteRequestBuilder queryEndpoint(String queryEndpoint) {
		this.queryEndpoint = queryEndpoint;
		return this;
	}
	
	/**
	 * Set the update endpoint.
	 * 
	 * <p>The default update endpoint is {@link CMEMOAUTH2RemoteRequestBuilder#host} + <code>/dataplatform/proxy/default/update</code>.</p>
	 * 
	 * @return An {@link CMEMOAUTH2RemoteRequestBuilder} with the given update endpoint.
	 *  
	 */
	public CMEMOAUTH2RemoteRequestBuilder updateEndpoint(String updateEndpoint) {
		this.updateEndpoint = updateEndpoint;
		return this;
	}

	/**
	 * Build an {@link RDFLink} using the update and query endpoints set by the user or the default ones.
	 * 
	 * @return An {@link RDFLink} built with an OAUTH2 <code>Bearer</code> authorisation header
	 *  using the given {@link CMEMOAUTH2Authenticator}.
	 *  
	 * @exception {@link RuntimeException} in case an error occurs while retrieving 
	 * the access token such as authentication failure or the host and the query or update endpoints are not set.
	 */
	public RDFLink buildLink() {
		if(host == null 
				&& (queryEndpoint == null
				|| updateEndpoint == null)) {
			throw HOST_NOT_DEFINED_EXCEPTION;
		}
		if(queryEndpoint != null) {
			super.destination(queryEndpoint);
		} else {
			super.destination(host + DEFAULT_QUERY_ENDPOINT_SUFFIX);
		}
		if(updateEndpoint != null) {
			super.updateEndpoint(updateEndpoint);
		} else {
			super.updateEndpoint(host + DEFAULT_UPDATE_ENDPOINT_SUFFIX);
		}
		return super.buildLink();
	}

}
