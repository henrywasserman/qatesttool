package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.json.JSONToMap;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class CreateUsers extends Request {
	static final Logger logger = Logger.getLogger(CreateUsers.class);

	public CreateUsers(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		CloseableHttpResponse response = null;
		post = new StringBuffer(builddir.toString());
		post.append(file);
		String fs = File.separator;

		try {

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url + "/hie/patients/registrations");

			File csv_file = new File(System.getProperty("user.dir") + fs + ".."
					+ fs
					+ PropertiesSingleton.Instance.getProperty("users_csv"));

			ArrayList<String> csv_contents = (ArrayList<String>) FileUtils
					.readLines(csv_file);

			for (String line : csv_contents) {

				String[] line_string = line.split(",");

				JSONToMap.Instance.put("username", line_string[0]);
				JSONToMap.Instance.put("password", line_string[1]);

				Post req = new Post(test);
				req.sendRequest();
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