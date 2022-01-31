package com.eccenca.braine.jena.core;

import com.eccenca.braine.jena.core.OAUTH2Authenticator.GrantType;

/**
 * Class used for building an {@link OAUTH2Authenticator} using eccenca Corporate Memory default parameters.
 * 
 * @author edgardmarx
 *
 */
public class CMEMOAUTH2Authenticator extends OAUTH2Authenticator {
	
	private static final String DEFAULT_OAUTH2_PREFIX = "/auth/realms/cmem/protocol/openid-connect/token";
	
	/**
	 * Set the OAUTH2 grant type (<code>password<code/> or <code>client_credentials</code>).
	 * 
	 * <p>Default grant type <code>client_credentials</code></p>.
	 * 
	 * @param grantType the grant type.
	 */
	public CMEMOAUTH2Authenticator host(String host) {
		tokenAccessURL(host + DEFAULT_OAUTH2_PREFIX);
		return this;
	}
	
	/**
	 * Set the OAUTH2 {@link GrantType}.
	 * 
	 * <p>Default grant type {@link GrantType#CLIENT_CREDENTIALS}.</p>
	 * 
	 * @param grantType the {@link GrantType}.
	 */
	public CMEMOAUTH2Authenticator grantType(String grantType) {
		super.grantType(grantType);
		return this;
	}

	/**
	 * Set the OAUTH2 {@link GrantType}.
	 * 
	 * <p>Default grant type {@link GrantType#CLIENT_CREDENTIALS}</p>
	 * 
	 * @param grantType the {@link GrantType}
	 */
	public CMEMOAUTH2Authenticator grantType(GrantType grantType) {
		super.grantType(grantType);
		return this;
	}

	/**
	 * Set the user in case of OAUTH2 <code>grant_type = password</code>.
	 * 
	 * @param user the OAUTH parameter <code>username</code>.
	 */
	public CMEMOAUTH2Authenticator user(String user) {
		super.user(user);
		return this;
	}

	/**
	 * Set the password in case of OAUTH2 <code>grant_type = password</code>.
	 * 
	 * @param password the password.
	 */
	public CMEMOAUTH2Authenticator password(String password) {
		super.password(password);
		return this;
	}

	/**
	 * Set the clientId (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = password</code> or <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientId the client ID OAUTH2 parameter (<code>client_id</code>).
	 */
	public CMEMOAUTH2Authenticator clientId(String clientId) {
		super.clientId(clientId);
		return this;
	}
	
	/**
	 * Set the client secret (HTTP parameter <code>client_id</code>) in case of 
	 * OAUTH2 <code>grant_type = client_credentials</code>.
	 * 
	 * @param clientSecret the client secret.
	 */
	public CMEMOAUTH2Authenticator clientSecret(String clientSecret) {
		super.clientSecret(clientSecret);
		return this;
	}
	
	/**
	 * Set the token access URL.
	 * 
	 * <p>In case this parameter is left without setting, the application will 
	 * use as default {@link CMEMOAUTH2Authenticator#host(String)} + <code>/auth/realms/cmem/protocol/openid-connect/token</code>.</p>
	 */
	public CMEMOAUTH2Authenticator tokenAccessURL(String tokenAccessURL) {
		super.tokenAccessURL(tokenAccessURL);
		return this;
	}
}
