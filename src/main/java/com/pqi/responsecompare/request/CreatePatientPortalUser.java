package com.pqi.responsecompare.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.data.DataDriven;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.json.JSONToMap;
import nu.xom.Builder;
import nu.xom.Serializer;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class CreatePatientPortalUser extends Request {
	static final Logger logger = Logger.getLogger(CreatePatientPortalUser.class);

	public CreatePatientPortalUser(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		CloseableHttpResponse response = null;
		
		post = new StringBuffer(builddir.toString());
		post.append(file);
		
		try {

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url);
			
			File postfile = test.getRequests().get(test_request_counter).getBodyFile();
			
			String post_file_contents = FileUtils.readFileToString(postfile);
			
			post_file_contents = DataDriven.Instance.dataDriveContents(post_file_contents);
			
			test.getRequests().get(test_request_counter).setBody(post_file_contents);

			httppost = new HttpPost(url.toString().trim());
			
			entity = test.getRequests().get(test_request_counter).getBody();

			if (entity != null) {
				httppost.setEntity(entity);
			}
		
			setPostHeaders(test_request_counter);
			
			logger.debug("Executing post");
			test.setHttpClient();
			response = test.getHttpClient().execute(httppost);
			logger.debug("Finished executing post");
			validateHeaders(response, test_request_counter);

			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {

				String jsonString = FileUtils.readFileToString(new File(
						outputfile.toString()));

				if (!jsonString.isEmpty()) {
				
					jsonString = "{\"root\":" + jsonString + "}";
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.readTree(jsonString);
					JSONToMap.Instance.setResponseMap(node,"");
					//LinkedHashMap<String,Object> variable_map = JSONToMap.Instance.setResponseMap(jsonString);
					//JSONToMap.Instance.combineMaps(variable_map);
					logger.debug(jsonString);
				
					String res = HandleJSONRequest.Instance.convertToXml(jsonString, "", true);
				
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Serializer serializer = new Serializer(out);
					serializer.setIndent(2);  // or whatever you like
					serializer.write(new Builder().build(res, ""));
					res = out.toString("UTF-8");

					logger.debug(res);
				}
			}

				Utilities.Instance.logHeaders(response);
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