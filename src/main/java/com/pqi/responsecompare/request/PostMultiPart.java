package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.File;

public class PostMultiPart extends Request {
	static final Logger logger = Logger.getLogger(PostMultiPart.class);

	public PostMultiPart(TestCase test) throws Exception {
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
			logger.info("POST Request: " + url);
			
			httppost = new HttpPost(url.toString().trim());

			
			//Test for a body file.
			if (test.getRequests().get(test_request_counter).getBodyFile() != null) {
				File postfile = test.getRequests().get(test_request_counter).getBodyFile();
				
				String post_file_contents = FileUtils.readFileToString(postfile);
				
				post_file_contents = InterpolateRequest.Instance.interpolateString(new StringBuffer(post_file_contents)).toString();
				
				test.getRequests().get(test_request_counter).setBody(post_file_contents);
			}

			
			/*
			entity = test.getRequests().get(test_request_counter).getBody();
			
			if (entity != null) {
				logger.info("Here is entity: \n" + IOUtils.readLines(test.getRequests().get(test_request_counter).getBody().getContent()));
				httppost.setEntity(entity);
			}
			*/
			 
			HttpEntity entity = test.getRequests().get(test_request_counter).getMultipartEntity();
			httppost.setEntity(entity);
			
			if (entity != null) {
				logger.info("Here is entity: \n" + IOUtils.readLines(test.getRequests().get(test_request_counter).getBody().getContent()));
				httppost.setEntity(entity);
			}
		
			setPostHeaders(test_request_counter);
			
			logger.debug("Executing post");
			test.setHttpClient();
			
			response = test.getHttpClient().execute(httppost);
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

			logger.debug("Finished executing post");
			validateHeaders(response, test_request_counter);

			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {
				HandleJSONRequest.Instance.handleJSON(outputfile, test);
			}
			
			Utilities.Instance.logHeaders(response);
		} catch (AssertionError ae) {
			throw ae;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
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