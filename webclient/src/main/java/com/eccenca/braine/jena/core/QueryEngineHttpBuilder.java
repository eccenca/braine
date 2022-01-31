package com.eccenca.braine.jena.core;

import org.apache.http.client.HttpClient;
import org.apache.http.util.Asserts;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import com.eccenca.braine.jena.core.OAUTH2Authenticator.GrantType;

/**
 * Class helper to build a {@link QueryEngineHTTP}. 
 * 
 * @author edgardmarx
 *
 */
@SuppressWarnings("deprecation")
public class QueryEngineHttpBuilder {
	private String clientId;
	private String grantType = GrantType.CLIENT_CREDENTIALS.getType();
	private String clientSecret;
	private String host;
	private String queryPath = "/dataplatform/proxy/default/sparql";
	private String tokenPath = "/auth/realms/cmem/protocol/openid-connect/token";
	private String tokenAccessURL;
	
	/**
	 * Set the OAUTH2 grant type (<code>password</code> or <code>client_credentials</code>).
	 * 
	 * <p>Default grant type <code>client_credentials</code></p>.
	 * 
	 * @param grantType the grant type.
	 */
	public QueryEngineHttpBuilder grantType(String grantType) {
		this.grantType = grantType;
		return this;
	}
	
	/**
	 * Set the OAUTH2 {@link GrantType}.
	 * 
	 * <p>Default grant type {@link GrantType#CLIENT_CREDENTIALS}.</p>
	 * 
	 * @param grantType the {@link GrantType}.
	 */
	public QueryEngineHttpBuilder grantType(GrantType grantType) {
		this.grantType = grantType.getType();
		return this;
	}
	
	/**
	 * Set the user in case of OAUTH2 <code>grant_type = password</code>.
	 * 
	 * @param the user.
	 */
	public QueryEngineHttpBuilder user(String user) {
		this.clientId = user;
		return this;
	}
	
	/**
	 * Set the password in case of OAUTH2 <code>grant_type = password</code>.
	 * 
	 * @param password the password.
	 */
	public QueryEngineHttpBuilder password(String password) {
		this.clientSecret = password;
		return this;
	}
	
	/**
	 * Set the clientId (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = password</code> or <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientId the client id OAUTH2 parameter (<code>client_id</code>).
	 */
	public QueryEngineHttpBuilder clientId(String clientId) {
		this.clientId = clientId;
		return this;
	}
	
	/**
	 * Set the client secret (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientSecret the client secret (OAUTH2 parameter <code>client_secret</code>).
	 */
	public QueryEngineHttpBuilder clientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}
	
	/**
	 * Set the token access URL.
	 * 
	 * <p>In case this parameter is left without setting, the application will 
	 * use as default {@link QueryEngineHttpBuilder#host(String)} + <code>/auth/realms/cmem/protocol/openid-connect/token</code>.</p>
	 */
	public QueryEngineHttpBuilder tokenAccessURL(String tokenAccessURL) {
		this.tokenAccessURL = tokenAccessURL;
		return this;
	}
	
	public QueryEngineHttpBuilder host(String host) {
		this.host = host;
		return this;
	}
	
	/**
	 * Build a {@link QueryEngineHTTP} with a given query
	 * targeting the default endpoint ({@link QueryEngineHTTP#host} + <code>/dataplatform/proxy/default/sparql</code>).
	 * 
	 * @param endpoint the SPARQL query endpoint address. 
	 * @param query the {@link Query}.
	 * 
	 * @return the {@link QueryEngineHTTP} built using the given parameters.
	 * 
	 * @throws Exception if an error occurs while retrieving the token:
	 * <p>A) when encoding the OAUTH2 parameters.</p>
	 * <p>B) if an error occurs when parsing the response.
	 * <p>C) if and access token was not returned.
	 */
	@SuppressWarnings("deprecation")
	public QueryEngineHTTP build(Query query) throws Exception {
		Asserts.notNull(host, "host");
		return build(host + queryPath, query);
	}
	
	/**
	 * Build a {@link QueryEngineHTTP} targeting the endpoint with a given query.
	 * 
	 * @param endpoint the SPARQL query endpoint address. 
	 * @param query the {@link Query}
	 * 
	 * @return the {@link QueryEngineHTTP} built using the given parameters.
	 * 
	 * @throws Exception if an error occurs while retrieving the token:
	 * <p>A) when encoding the OAUTH2 parameters.</p>
	 * <p>B) if an error occurs when parsing the response.
	 * <p>C) if and access token was not returned.
	 */
	@SuppressWarnings("deprecation")
	public QueryEngineHTTP build(String endpoint, Query query) throws Exception {
		Asserts.notNull(endpoint, "endpoint");
		String tokenAccessUrl = tokenAccessURL;
		if(tokenAccessUrl == null) {
			Asserts.notNull(host, "host");
			tokenAccessUrl = host + tokenPath;
		}
		OAUTH2Authenticator authenticator = new OAUTH2Authenticator()
				.grantType(grantType)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.tokenAccessURL(tokenAccessUrl);
		OAUTH2ApacheHttpClientBuilder builder = new OAUTH2ApacheHttpClientBuilder(authenticator);
		HttpClient httpClient = builder
				.build();
		QueryEngineHTTP httpEngine = new QueryEngineHTTP(endpoint, query, httpClient);
		return httpEngine;
	}
}