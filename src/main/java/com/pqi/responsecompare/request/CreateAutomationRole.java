package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.json.JSONToNashorn;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.File;

public class CreateAutomationRole extends Request {
	static final Logger logger = Logger.getLogger(CreateAutomationRole.class);

	private String feature = "";
	private String action = "";
	private StringBuffer json = new StringBuffer();


	public CreateAutomationRole(TestCase test) throws Exception {
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

            if (JSONToMap.Instance.getMap().get("automation").toString().equals("automation role not found"))
            {

                logger.info("TestID: " + test.getTestCaseID());
                logger.info("POST Request: " + url);

                test.getRequests().get(test_request_counter).setBody("{\"type\":\"Person\",\"name\":\"automation\",\"description\":\"automation\"}");
                //test.getRequests().get(test_request_counter).setBody("{\"type\":\"Person\",\"name\":\"autotest6\",\"description\":\"autotest6\"}");

                httppost = new HttpPost(url.toString().trim());

                entity = test.getRequests().get(test_request_counter).getBody();

                if (entity != null)
                {
                    logger.info("Here is entity: \n" + IOUtils
                        .readLines(test.getRequests().get(test_request_counter).getBody().getContent()));
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

                if (test.getRequests().get(test_request_counter).getStatus() == 0)
                {
                    Assert.assertTrue("Status: "
                        + response.getStatusLine().getStatusCode()
                        + " The request " + url + " was not successful", response
                        .getStatusLine().getStatusCode() < 300);
                }

                else
                {
                    Assert.assertTrue(
                        "Status: " + response.getStatusLine().getStatusCode() + " did not equal expected result of "
                            + Integer.valueOf(test.getRequests().get(test_request_counter).getStatus()).toString(),
                        response.getStatusLine().getStatusCode() == test.getRequests().get(test_request_counter)
                            .getStatus());
                }

                logger.debug("Finished executing post");
                validateHeaders(response, test_request_counter);

                setupAndOutput(response);

                if (isJSONRequest(test_request_counter, response))
                {
                    HandleJSONRequest.Instance.handleJSON(outputfile, test);
                }

                String json = FileUtils.readFileToString(new File(outputfile.toString()));

                JSONToNashorn.Instance.setJsonResponse(json);
                JSONToMap.Instance.put("automation",JSONToNashorn.Instance.evaluateJSON("json_object.id"));

                Utilities.Instance.logHeaders(response);
            }
		} catch (AssertionError ae) {
			throw ae;
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