package com.pqi.responsecompare.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public enum SSLSocketFactorySingleton {

	INSTANCE;

	private SSLSocketFactory SSLOnlySSLFactory = null;

	SSLSocketFactorySingleton() throws ExceptionInInitializerError {
	       try {
	            // Create an SSLSocketFactory configured to use TLS only
	            SSLContext sslContext = SSLContext.getInstance("TLSv1");
	            TrustManager[] byPassTrustManagers = new TrustManager[]{
	                    new X509TrustManager() {
	                        public X509Certificate[] getAcceptedIssuers() {
	                            return null;
	                        }

	                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
	                        }

	                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
	                        }
	                    }
	            };
        sslContext.init(null, byPassTrustManagers, new SecureRandom());
        SSLOnlySSLFactory = sslContext.getSocketFactory();
	
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		
	}

	public SSLSocketFactory getSSLConnectionSocketFactory() {
		return SSLOnlySSLFactory;
	}

}