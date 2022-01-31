package com.eccenca.braine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTPUtils {
	/*
	 * This is very bad practice and should NOT be used in production.
	 */
	private static final TrustManager[] trustAllCerts = new TrustManager[] {
	    new X509TrustManager() {
	        @Override
	        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
	        }

	        @Override
	        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
	        }

	        @Override
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	          return new java.security.cert.X509Certificate[]{};
	        }	
	    }
	};
	private static final SSLContext trustAllSslContext;
	static {
	    try {
	        trustAllSslContext = SSLContext.getInstance("SSL");
	        trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	    } catch (NoSuchAlgorithmException | KeyManagementException e) {
	        throw new RuntimeException(e);
	    }
	}

	private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

	/*
	 * This should not be used in production unless you really don't care
	 * about the security. Use at your own risk.
	 */
	public static OkHttpClient trustAllSslClient(OkHttpClient client) {
	    okhttp3.OkHttpClient.Builder builder = client.newBuilder();
	    builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0]);
	    builder.hostnameVerifier(new HostnameVerifier() {
	      @Override
	      public boolean verify(String hostname, SSLSession session) {
	        return true;
	      }
	    });
	    return builder.build();
	}
	
	public static String httpGet(String url,
			Map<String, String> header,
			OkHttpClient client) throws IOException {
		Builder requestBuilder = new Request.Builder().url(url).get();
		return call(client, header, requestBuilder);
	}
	
	public static OkHttpClient createClient() {
		return createClient(null, null);
	}
	
	public static OkHttpClient createClient(String proxyHost, Integer proxyPort) {
		OkHttpClient client = new OkHttpClient();
		client = HTTPUtils.trustAllSslClient(client);
		okhttp3.OkHttpClient.Builder clientBuilder = client.newBuilder();
		if(proxyHost != null) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
			clientBuilder = clientBuilder.proxy(proxy);
		}
		client = clientBuilder.build();
		return client;
	}
	public static String httpPost(String url,
			Map<String, String> header,
			OkHttpClient client) throws IOException {
		RequestBody body = RequestBody.create("", MediaType.get("application/json"));
		return httpPost(url, header, client, body);
	}
	
	public static String httpPost(String url,
			Map<String, String> header,
			OkHttpClient client,
			RequestBody content) throws IOException {
		Builder requestBuilder = new Request.Builder().url(url).post(content);
		return call(client, header, requestBuilder);
	}
	
	public static String call(OkHttpClient client, Map<String, String> header, Builder requestBuilder) throws IOException {
		for(String key : header.keySet()) {
			requestBuilder.addHeader(key, header.get(key));
		}
		Request request = requestBuilder.build();
		return call(client, request);
	}
	
	public static String call(OkHttpClient client, Request request) throws IOException {
		Response response = client.newCall(request).execute();
		String responseObject = response.body().string();
		if(response.code() == 401) {
			throw new IOException("401 Service Unavailable: There should be a problem in your authorization.");
		}
		if(response.code() == 503) {
			throw new IOException("503 Service Unavailable: You permission might have been expired or the request is invalid.");
		}
		if(response.code() == 500) {
			throw new IOException("500 Error processing your request: You permission might have been expired or the request is invalid.");
		}
		return responseObject;
	}
}