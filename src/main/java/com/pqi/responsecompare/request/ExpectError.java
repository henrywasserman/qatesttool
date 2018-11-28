package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.io.File;

public class ExpectError extends Request {

  static final Logger logger = Logger.getLogger(ExpectError.class);
  Integer TestNumber = null;

  public ExpectError(TestCase test) throws Exception {
	  super(test);
  }
  
  public ExpectError() {
	  super();
  }
  
  public void sendRequest() throws Exception {

		post = new StringBuffer(builddir.toString());
		post.append(file);

		url = url + props.getProperty("consult-protocol") +
				"://" + test.getRequests().get(0).getURL().trim();
		
		HttpClient httpclient = HttpClientBuilder.create().build();
		
		try {
		
			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url);			

			httppost = new HttpPost(url.toString().trim());
			entity = test.getRequests().get(0).getBody();
			
			setPostHeaders(0);
			
			httppost.setEntity(entity);
			CloseableHttpResponse response = test.getHttpClient().execute(httppost);
			validateHeaders(response,0);
			
			Assert.assertTrue( "Status: "+ response.getStatusLine().getStatusCode()+" The request "+url+" was not successful",response.getStatusLine().getStatusCode() == 
					test.getRequests().get(0).getStatus());

			setupAndOutput(response);
			
		    if (isJSONRequest(0,response)) {
		        
		    	String jsonString = FileUtils.readFileToString(new File(outputfile.toString()));
		    	
		    	jsonString = "{\"root\":" + jsonString + "}"; 
		    
		    	String res = HandleJSONRequest.Instance.convertToXml(jsonString,"",true);

		    	logger.debug(res);

		    	FileUtils.writeStringToFile(new File(outputfile.toString().replace(".json", ".xml")), res);
		    }			

			Utilities.Instance.logHeaders(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
  }
