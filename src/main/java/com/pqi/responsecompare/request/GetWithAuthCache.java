package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.junit.Assert;

public class GetWithAuthCache extends Request {

	static final Logger logger = Logger.getLogger(GetWithAuthCache.class);
	Integer TestNumber = null;

	public GetWithAuthCache(TestCase test) throws Exception {
		super(test);
	}

	public GetWithAuthCache() {
		super();
	}

	public void sendRequest() throws Exception {
		CloseableHttpResponse response = null;

		post = new StringBuffer(builddir.toString());
		post.append(file);		if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
			TailManager.Instance.setStartLogLineCount();
		}

		try {

			
			HttpHost target = new HttpHost(PropertiesSingleton.Instance.getProps().getProperty("consult-host"), 80, "http");
			CloseableHttpClient httpclient = test.getHttpClient();
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			String username = (String) JSONToMap.Instance.getMap().get("email");
			String password = (String) JSONToMap.Instance.getMap().get("password");
			credsProvider.setCredentials(new AuthScope(target.getHostName(),
					target.getPort()), new UsernamePasswordCredentials(
					"username", "password"));
			httpclient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider).build();
			AuthCache authCache = new BasicAuthCache();

			BasicScheme basicAuth = new BasicScheme();
			authCache.put(target, basicAuth);

			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("GET Request: " + url.toString());

			httpget = new HttpGet(url.toString().trim());

			setGetHeaders(test_request_counter);
			logger.debug("Executing get");
			Thread.sleep(3000);
			response = httpclient.execute(target, httpget, localContext);
			logger.debug("Finished executing get");
			Utilities.Instance.logHeaders(httpget);
			Assert.assertTrue("Status: "
					+ response.getStatusLine().getStatusCode()
					+ " The request " + url + " was not successful", response
					.getStatusLine().getStatusCode() < 300);

			validateHeaders(response, 0);

			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {
				HandleJSONRequest.Instance.handleJSON(outputfile, test);
			}

			// FileUtils.writeStringToFile(new
			// File(outputfile.toString().replace(".json", ".xml")), res);

			Utilities.Instance.logHeaders(response);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				response.close();
			}
			if (test_request_counter + 1 == test.getRequests().size()) {
				test.httpClientClose();
			}
		}
	}
}