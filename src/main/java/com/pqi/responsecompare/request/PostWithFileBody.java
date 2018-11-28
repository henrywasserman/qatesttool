package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;

import java.io.File;
import java.lang.Integer;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

public class PostWithFileBody extends Request {
	static Logger logger = Logger.getLogger(PostWithFileBody.class);
	Integer TestNumber = null;
	static int testnumber = 0;
	private StringBuffer url1 = null;
	
	  public PostWithFileBody(TestCase test) throws Exception {
		  super(test);
	  }	
	
	public void sendRequest() throws Exception {
		post = new StringBuffer(builddir.toString());
		post.append(file);
		
		HttpClient httpclient = HttpClientBuilder.create().build();

		url1 = new StringBuffer("http://");
		
	    String request1 = test.getRequests().get(0).getURL();

		url1.append(request1.trim());


		try {
			logger.info("TestID: " + test.getTestCaseID());
			
			String postxmlfilename = Utilities.Instance.getResponseCompareRoot() + File.separator + "data"
			+ File.separator + "" + File.separator + "post_file_body" 
			+ File.separator +	test.getTestCaseID() + ".json";			
		
			String postxmlfile = FileUtils.readFileToString(
				new File(postxmlfilename));
			
			String replacementurl = url1.toString();
			
			logger.info("Here the request: " + replacementurl.trim());
			logger.info("And It's related post xml file: " + postxmlfile);
			
			HttpPost httppost = new HttpPost(replacementurl.trim());
			HttpEntity postentity = new StringEntity(postxmlfile);
			httppost.setEntity(postentity);

		    HttpResponse httpresponse = httpclient.execute(httppost);

		    setupAndOutput(httpresponse);
		    
		    validateHeaders(httpresponse,1);
		    
		    Utilities.Instance.logHeaders(httpresponse);
		} catch (Exception e) {
			Assert.fail(e.getMessage()+"\n "+StringUtils.join(e.getStackTrace()).substring(0,512));
		}
	}
}