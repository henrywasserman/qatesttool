package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.data.DataDriven;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateClinicalFromCSV extends Request {
	static final Logger logger = Logger.getLogger(CreateClinicalFromCSV.class);

	public CreateClinicalFromCSV(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() {
	};

	public void makeRequests(ArrayList<String> headers, String line,
			String endpoint, String startdate, int counter) throws Exception {

		CloseableHttpResponse response = null;
		post = new StringBuffer(builddir.toString());
		post.append(file);
		if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
			TailManager.Instance.setStartLogLineCount();
		}
		String fs = File.separator;
		String endpoint_filename = "";

		try {

			if (endpoint.equals("allergy_registrations")) {
				endpoint_filename = "allergy_registrations";
			} else {
				endpoint_filename = endpoint;
			}
			
			if (endpoint.equals("labs_discrete_results")) {
				endpoint_filename = "labs_discrete_results";
			} else {
				endpoint_filename = endpoint;
			}

			if (endpoint.contains("registrations")) {
				endpoint = "registrations";
			}
			
			if (endpoint.contains("labs")) {
				endpoint = "labs";
			}

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url + "/hie/patients/" + endpoint);

			String visit_account_number = DataDriven.Instance
					.getVisitAccountNumber();

			String user_dir = System.getProperty("user.dir");

			File template_postfile = new File(user_dir + fs + "data" + fs
					+ "consult" + fs + "post_file_body" + fs + "templates" + fs
					+ endpoint_filename + ".json");

			String template_file_contents = FileUtils
					.readFileToString(template_postfile);
			
			HashMap<String, String> map = Utilities.Instance
					.createMapFromCSV(headers, line);

			String post_file_contents = "";

			post_file_contents = DataDriven.Instance.interpolateBodyTemplate(
					template_file_contents, headers, map, startdate, DataDriven.Instance.getMRN(), endpoint, endpoint_filename);

			post_file_contents = StringUtils.replace(post_file_contents,
					"${visit_account_number}", visit_account_number);
			
			logger.debug(post_file_contents);

			String post_file_location = System.getProperty("user.dir") + fs
					+ "data" + fs + "consult" + fs + "post_file_body" + fs
					+ "generated";

			String post_file_name = test.getRequestFileName();
			File post_file = new File(post_file_location + fs + post_file_name
					+ "." + map.get("firstname") + "." + map.get("lastname") + "." + endpoint_filename + "_" + Integer.toString(counter) +  ".json");

			FileUtils.writeStringToFile(post_file, post_file_contents);

			test.getRequests().get(test_request_counter)
					.setBody(post_file_contents);

			httppost = new HttpPost(url.toString().trim() + "/hie/patients/"
					+ endpoint);
			// Todo: make sure not to hard code endpoint here..
			// Todo: Account Number might want to set account_number in csv -
			// usecase

			entity = test.getRequests().get(test_request_counter).getBody();

			if (entity != null) {
				httppost.setEntity(entity);
			}

			setPostHeaders(test_request_counter);

			logger.debug("Executing post");
			test.setHttpClient();
			response = test.getHttpClient().execute(httppost);
			//SplunkManager.Instance.search("search * | reverse");
			if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
				logger.debug("Here is tail: " + TailManager.Instance.getTail());
				FileUtils.writeStringToFile(new File(logoutputfile), TailManager.Instance.getTail(), false);
			}
			logger.debug("Finished executing post");
			validateHeaders(response, test_request_counter);

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