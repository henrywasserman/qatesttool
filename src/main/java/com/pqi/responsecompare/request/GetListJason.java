package com.pqi.responsecompare.request;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;


public class GetListJason extends Request {

	@Override
	public void sendRequest() throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();
	    
	    url = url + "http://"+test.getRequests().get(0).getURL().trim();
	    
	    logger.info("TestID: " + test.getTestCaseID());
		logger.info("GET Request: " + url.toString());
		
		test.getRequests().get(0).setURL(url.toString());
	  
		httpget = new HttpGet(url.toString().trim());
	    
	    setGetHeaders(0);
	    Utilities.Instance.logHeaders(httpget);
	    HttpResponse response = httpclient.execute(httpget);
	    
	    int status  = response.getStatusLine().getStatusCode();
	    
	    if (test.getStatus() > 0) {
		    Assert.assertTrue("Response was not equal to " + test.getStatus(), test.getStatus() == status);
	    }
	    else {
	    	Assert.assertTrue( "Status: "+ response.getStatusLine().getStatusCode()+" The request "+url+" was not successful",response.getStatusLine().getStatusCode()<300);
	    
	    
	    	validateHeaders(response,0);
	    
	    	setupAndOutput(response);

	    	String jsonString = FileUtils.readFileToString(new File(outputfile.toString()));
	    
	    	String res = HandleJSONRequest.Instance.convertToXml(jsonString,"",true);
	    
	    	logger.info(res);
		
	    	FileUtils.writeStringToFile(new File(outputfile.toString().replace(".json", ".xml")), res);
		
	    	Utilities.Instance.logHeaders(response);
	    }
	}
	
	public GetListJason(TestCase test) throws Exception {
		  super(test);
	}
	
	/*
	private String convertToXml(final String json, final String namespace, final boolean addTypeAttributes) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
		InputSource source = new InputSource(new StringReader(json));
		Result result = new StreamResult(out);
		transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes),source), result);
        return new String(out.toByteArray());
	}
	*/
}