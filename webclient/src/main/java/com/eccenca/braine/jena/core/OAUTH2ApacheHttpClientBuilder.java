package com.eccenca.braine.jena.core;

import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

/**
 * 
 * Class helper to build an {@link HttpClient} using an {@link OAUTH2Authenticator}.
 * 
 * @author edgardmarx
 *
 */
public class OAUTH2ApacheHttpClientBuilder {
	
	private static final String AUTHORIZATION_HEADER_PARAM = "Authorization";
	private static final String AUTHORIZATION_HEADER_PARAM_VALUE_PREFIX = "Bearer ";
	
	protected OAUTH2Authenticator authenticator;
	
	/**
	 * Constructs an {@link OAUTH2ApacheHttpClientBuilder} using the given {@link OAUTH2Authenticator}.
	 * 
	 * @param authenticator an {@link OAUTH2Authenticator}.
	 */
	public OAUTH2ApacheHttpClientBuilder(OAUTH2Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	/**
	 * 
	 * @return {@link HttpClient} built using the given {@link OAUTH2Authenticator}.
	 * 
	 * @throws Exception if an error occurs while retrieving an access token 
	 * using the given {@link OAUTH2Authenticator}.
	 */
	public HttpClient build() throws Exception {
		String token = authenticator.getToken();
		ArrayList<Header> defaultHeaders = new ArrayList<Header>();
    	Header header = new BasicHeader(AUTHORIZATION_HEADER_PARAM, 
    			AUTHORIZATION_HEADER_PARAM_VALUE_PREFIX + token);
    	defaultHeaders.add(header);
    	HttpClient httpClient = HttpClients.custom()
    		.setDefaultHeaders(defaultHeaders)
            .build();
        return httpClient;
	}
	
}
