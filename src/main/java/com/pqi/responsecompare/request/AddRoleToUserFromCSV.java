package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AddRoleToUserFromCSV extends Request {
	static final Logger logger = Logger.getLogger(AddRoleToUserFromCSV.class);

	public AddRoleToUserFromCSV(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		CloseableHttpResponse response = null;
		post = new StringBuffer(builddir.toString());
		post.append(file);
		String fs = File.separator;

		try {

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url + "");

			String csv_filename = PropertiesSingleton.Instance.getProps()
					.getProperty("csv_file");
			File csv_file = new File(
					Utilities.Instance.getResponseCompareRoot() + fs + "data"
							+ fs + "consult" + fs + "datadriven" + fs
							+ csv_filename);

			ArrayList<String> csv_contents = (ArrayList<String>) FileUtils
					.readLines(csv_file);
			ArrayList<String> headers = new ArrayList<String>();

			int counter = 0;
			for (String line : csv_contents) {
				if (counter == 0) {
					
					headers = new ArrayList<String>(Arrays.asList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1)));
					counter++;
				} else {

					String[] creds = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1);
					
					Post sslpost = new Post(test);

					test.getRequests().get(0).setHeader("username:" + creds[0]);
					test.getRequests().get(0).setHeader("password:" + creds[1]);
					sslpost.sendRequest();

					AddRoleToPrincipalFromCSV req = new AddRoleToPrincipalFromCSV(test);
					
					String url = test.getRequests().get(0).getURL();
					req.makeRequests(url.replace("login", "organizations"));
					
					counter++;
				}
			}
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