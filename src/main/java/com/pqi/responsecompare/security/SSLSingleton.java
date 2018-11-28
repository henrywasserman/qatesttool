package com.pqi.responsecompare.security;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

public enum SSLSingleton {

	INSTANCE;

	private SSLConnectionSocketFactory sslsf = null;

	SSLSingleton() throws ExceptionInInitializerError {

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };

			try {
			    SSLContext sslcontext = SSLContext.getInstance("SSL");
			    sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
			    HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			    sslsf = new SSLConnectionSocketFactory(sslcontext);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
		}
	}

	public SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
		return sslsf;
	}

}