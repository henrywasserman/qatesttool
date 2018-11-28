package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.io.File;

public class Delete extends Request {
	static final Logger logger = Logger.getLogger(Delete.class);

	public Delete(TestCase test) throws Exception {
		super(test);
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
			
			httpdelete = new HttpDelete(url.toString().trim());
	
			setDeleteHeaders(test_request_counter);
			
			logger.debug("Executing delete");
			test.setHttpClient();
			response = test.getHttpClient().execute(httpdelete);
			//SplunkManager.Instance.search();
			if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
				logger.debug("Here is tail: " + TailManager.Instance.getTail());
				FileUtils.writeStringToFile(new File(logoutputfile), TailManager.Instance.getTail(), false);
			}
			Assert.assertTrue("Status: "
					+ response.getStatusLine().getStatusCode()
					+ " The request " + url + " was not successful", response
					.getStatusLine().getStatusCode() < 300);
			
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

			logger.debug("Finished executing delete");
			validateHeaders(response, test_request_counter);

			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {

				HandleJSONRequest.Instance.handleJSON(outputfile, test);
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