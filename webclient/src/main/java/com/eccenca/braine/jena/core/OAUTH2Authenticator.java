package com.eccenca.braine.jena.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

/**
 * The {@link OAUTH2Authenticaticator} class is used to retrieve the access token.
 * 
 * @author edgardmarx
 *
 */
public class OAUTH2Authenticator {

	public enum GrantType {
		PASSWORD("password"), 
		CLIENT_CREDENTIALS("client_credentials");
		
		GrantType(String type) {
			this.type = type;
		}
		
		private final String type;
		
		public String getType() {
			return type;
		}
	}
	
	private static final String GRANT_TYPE_HEADER_PARAM = "grant_type";
	private static final String CLIENT_ID_HEADER_PARAM = "client_id";
	private static final String CLIENT_SECRET_HEADER_PARAM = "client_secret";
	private static final String USER_HEADER_PARAM = "username";
	private static final String PASSWORD_HEADER_PARAM = "password";
	private static final String CONTENT_TYPE_HEADER_PARAM = "Content-Type";

	private static final String DEFAULT_TOKEN_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String DEFAULT_TOKEN_REQUEST_RESPONSE_ENCODING = "UTF-8";
	private static final String DEFAULT_TOKEN_REQUEST_RESPONSE_ATT = "access_token";
	
	private static final String AUTHENTICATION_ERROR_MESSAGE = "Error when retriving access token: "
			+ "Authentication method, user or password "
			+ "must be invalid.\"";
	
	private String tokenAccessURL;
	private String clientId;
	private String grantType = GrantType.CLIENT_CREDENTIALS.getType();
	private String clientSecret;
	private String user;
	private String password;
	private String token;
	
	/**
	 * Set the OAUTH2 grant type (<code>password<code/> or <code>client_credentials</code>).
	 * 
	 * <p>Default grant type <code>client_credentials</code></p>.
	 * 
	 * @param grantType the grant type.
	 */
	public OAUTH2Authenticator grantType(String grantType) {
		this.grantType = grantType;
		return this;
	}
	
	/**
	 * Set the OAUTH2 {@link GrantType}.
	 * 
	 * <p>Default grant type {@link GrantType#CLIENT_CREDENTIALS}</p>
	 * 
	 * @param grantType the {@link GrantType}
	 */
	public OAUTH2Authenticator grantType(GrantType grantType) {
		this.grantType = grantType.getType();
		return this;
	}

	/**
	 * Set the user in case of OAUTH2 <code>grant_type = password</code>
	 * 
	 * @param user the OAUTH parameter <code>username</code>.
	 */
	public OAUTH2Authenticator user(String user) {
		this.user = user;
		return this;
	}
	
	/**
	 * Set the password in case of OAUTH2 <code>grant_type = password</code>
	 * 
	 * @param password the password.
	 */
	public OAUTH2Authenticator password(String password) {
		this.password = password;
		return this;
	}

	/**
	 * Set the clientId (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = password</code> or <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientId the client ID OAUTH2 parameter (<code>client_id</code>).
	 */
	public OAUTH2Authenticator clientId(String clientId) {
		this.clientId = clientId;
		return this;
	}
	
	/**
	 * Set the client secret (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientSecret the client secret (OAUTH2 parameter <code>client_secret</code>).
	 */
	public OAUTH2Authenticator clientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}
	
	/**
	 * Set the token access URL.
	 * 
	 * @param tokenAccessURL the URL from which the token should be retrieved.
	 */
	public OAUTH2Authenticator tokenAccessURL(String tokenAccessURL) {
		this.tokenAccessURL = tokenAccessURL;
		return this;
	}

	/**
	 * 
	 * Returns a token generated by the {@link OAUTH2Authenticator#tokenAccessURL}.
	 * 
	 * @return an access token generated from the target OAUTH2 service
	 * using the {@link OAUTH2Authenticator} parameters.
	 * 
	 * @throws Exception In case the following errors occurs in order:
	 * <p>A) {@link UnsupportedEncodingException} - when encoding the OAUTH2 parameters.</p>
	 * <p>B) {@link IOException} - if an error occurs when parsing the response.
	 * <p>C) {@link Exception} - if the token was not return.
	 */
	public String getToken() throws Exception {
		HttpClient httpClient = HttpClients.custom()
	            .build();
		HttpPost httpPost = new HttpPost(tokenAccessURL);
		httpPost.addHeader(CONTENT_TYPE_HEADER_PARAM, DEFAULT_TOKEN_REQUEST_CONTENT_TYPE);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	    nvps.add(new BasicNameValuePair(GRANT_TYPE_HEADER_PARAM, grantType));
	    if(grantType.equals(GrantType.CLIENT_CREDENTIALS.getType())) {
	    	nvps.add(new BasicNameValuePair(CLIENT_ID_HEADER_PARAM, clientId));
	    	nvps.add(new BasicNameValuePair(CLIENT_SECRET_HEADER_PARAM, clientSecret));
	    } else {
	    	nvps.add(new BasicNameValuePair(CLIENT_ID_HEADER_PARAM, clientId));
	    	nvps.add(new BasicNameValuePair(USER_HEADER_PARAM, user));
	    	nvps.add(new BasicNameValuePair(PASSWORD_HEADER_PARAM, password));
	    }
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		HttpResponse response = httpClient.execute(httpPost);
		String contentJSON = IOUtils.toString(response.getEntity().getContent(), 
				DEFAULT_TOKEN_REQUEST_RESPONSE_ENCODING);
		JSONObject responseJSON = new JSONObject(contentJSON);
		if(!responseJSON.has(DEFAULT_TOKEN_REQUEST_RESPONSE_ATT)) {
			throw new Exception(AUTHENTICATION_ERROR_MESSAGE + ": " + contentJSON);
		}
		token  = responseJSON.getString(DEFAULT_TOKEN_REQUEST_RESPONSE_ATT);
		return token;
	}
}
