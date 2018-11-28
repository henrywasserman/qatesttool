package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.junit.Assert;

public class GetMRN extends Request {

	static final Logger logger = Logger.getLogger(GetMRN.class);
	Integer TestNumber = null;

	public GetMRN(TestCase test) throws Exception {
		super(test);
	}

	public GetMRN() {
		super();
	}

	public void sendRequest() {
	};

	public void makeRequests(String firstname, String lastname, String gender,
			String birthdate) throws Exception {

		CloseableHttpResponse response = null;

		try {

			if (StringUtils.isNumeric(birthdate)) {
				birthdate = birthdate.substring(0, 8);
			}

			url = url + "/patients/fixedsearch?lastname=" + lastname
					+ "&firstname=" + firstname + "&gender=" + gender + "&dob="
					+ birthdate;

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("GET Request: " + url.toString());

			httpget = new HttpGet(url.toString().trim());

			setGetHeaders(test_request_counter);
			logger.debug("Executing get");
			test.setHttpClient();
			response = test.getHttpClient().execute(httpget);
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

			Utilities.Instance.logHeaders(response);

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