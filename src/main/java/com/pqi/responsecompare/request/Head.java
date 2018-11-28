package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.io.File;
import java.util.Map;


public class Head extends Request {
	static final Logger logger = Logger.getLogger(Head.class);

	public Head(TestCase test) throws Exception {
		super(test);
	}

	public Head() throws Exception {
		
	}

	public void sendRequest() throws Exception {
		CloseableHttpResponse response = null;
		
		post = new StringBuffer(builddir.toString());
		post.append(file);
		if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
			TailManager.Instance.setStartLogLineCount();
		}


		try {

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("GET Request: " + url);
			
			httphead = new HttpHead(url.toString().trim());

			//Test for a body file.
			if (test.getRequests().get(test_request_counter).getBodyFile() != null) {
				File postfile = test.getRequests().get(test_request_counter).getBodyFile();

				String get_file_contents = FileUtils.readFileToString(postfile);

				get_file_contents = InterpolateRequest.Instance.interpolateString(new StringBuffer(get_file_contents)).toString();

				test.getRequests().get(test_request_counter).setBody(get_file_contents);
			}

			logger.debug("Executing head");
			test.setHttpClient();

			entity = test.getRequests().get(test_request_counter).getBody();

			setGetHeaders(test_request_counter);
			response = test.getHttpClient().execute(httphead);

			//SplunkManager.Instance.search();
			if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
				logger.debug("Here is tail: " + TailManager.Instance.getTail());
				FileUtils.writeStringToFile(new File(logoutputfile), TailManager.Instance.getTail(), false);
			}

			
			if (test.getRequests().get(test_request_counter).getStatus() == 0) {
				Assert.assertTrue("Status: "
					+ response.getStatusLine().getStatusCode()
					+ " The request " + url + " was not successful", response
					.getStatusLine().getStatusCode() < 300);
			}
			
			else {
				Assert.assertTrue("Status: " + response.getStatusLine().getStatusCode() + " did not equal expected result of " + Integer.valueOf(test.getRequests().get(test_request_counter).getStatus()).toString(), 
				response.getStatusLine().getStatusCode() == test.getRequests().get(test_request_counter).getStatus());
			}

			logger.debug("Finished executing get");
			validateHeaders(response, test_request_counter);

			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {
				//HandleJSONRequest.Instance.handleJSON(outputfile);
				HandleJSONRequest.Instance.handleJSON(outputfile, test);
				Map<String, String> vairableMap = test.getRequests().get(test_request_counter).getVariableHash();
				String key = "";
				String value = "";
				for (Map.Entry<String,String> entry: vairableMap.entrySet()) {
					key = entry.getKey();
					value = entry.getValue().replace("${","").replace("}","");
					JSONToMap.Instance.getMap().get(value);
				}
			}

		Utilities.Instance.logHeaders(response); 
		
		} catch (AssertionError ae) {
			ae.printStackTrace();
			throw ae;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (response != null) {
				response.close();
			}
			if (test_request_counter + 1 == test.getRequests().size()) {
				test.httpClientClose();
			}
		}
	}
}