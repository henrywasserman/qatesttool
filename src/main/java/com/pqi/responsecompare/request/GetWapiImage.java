package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetWapiImage extends Get {

	static final Logger logger = Logger.getLogger(Get.class);
	
	public GetWapiImage(TestCase test) throws Exception {
		super(test);
	}
	
	public GetWapiImage() throws Exception {
		super();
	}

	@Override
	public void sendRequest() throws Exception {
		super.sendRequest();

		if (test.getRequests().size() == 2) {
			String url2 = test.getRequests().get(1).getURL();
			String variables = Utilities.Instance.extractVariablesFromRequest(url2);
			String resultContents = FileUtils.readFileToString(new File(outputfile.toString()));
			
			setIDs(resultContents, variables);
			String fixreplace = "";
			for (String replace : replacements.keySet()) {
				fixreplace = replace.replace("$","\\$");
				fixreplace = fixreplace.replace("{","\\{");
				fixreplace = fixreplace.replace("}","\\}");
				url2 = url2.replaceAll(fixreplace, replacements.get(replace));
			}
			url2 = "http://" + url2.trim();
			doGetRequest(url2);
			requestAndSaveWapiImage(url2);
		} else if (test.getRequests().size() == 1) {
			requestAndSaveWapiImage(url.toString());
		}
	}

	protected void requestAndSaveWapiImage(String originalWapiRequestUrl) throws Exception {
		Pattern patRequestType = Pattern.compile("http:\\/\\/.*\\/([a-zA-Z]*)"),
	    patImgSrc = Pattern.compile(".*<img.*src=\"(\\S+)\\\".*"),
	    patImgLandingPage = Pattern.compile("LANDINGPAGE\\\"\\:\\{\\\"mainimage\\\"\\:\\\"(.*)\\\""),
	    patJsonImg = Pattern.compile("\\\"hyperlocal\\\":\\\"([^\"]*)\\\"");

		Matcher m = patRequestType.matcher(originalWapiRequestUrl);
		
		boolean result = m.find();
		if (!result) throw new Exception("A request type could not be dervied from this URL: " + originalWapiRequestUrl);
		
		String resultContents = FileUtils.readFileToString(new File(outputfile.toString()));
		
		String wapiRequestType = m.group(1).toLowerCase(), imageUrl = "";
		if ((wapiRequestType.equals("checkin")) || (wapiRequestType.equals("dealfinder"))) {
			m = patImgSrc.matcher(resultContents);
			result = m.find();
			if (!result) {
				logger.debug("An image url was not found in the wapi checkin response");
			} else {
				imageUrl = m.group(1);
				
				String filename = URLDecoder.decode(imageUrl, "UTF-8");
				filename = StringUtils.substringAfterLast(filename, "/");
				filename = FilenameUtils.removeExtension(filename);
				String type = StringUtils.substringAfterLast(imageUrl, "elementcode=");
				type = StringUtils.substringBefore(type,"&");
				
				if (type.isEmpty()) {
					requestAndSaveImageToFile(this, imageUrl, false, "");
				} else {
					requestAndSaveImageToFile(this, imageUrl, false, 
						"_" + filename + "_type_" + type);
				}
			}
		} else if (wapiRequestType.equals("getpage")) {
			m = patImgLandingPage.matcher(resultContents);
			result = m.find();
			if (!result) {
				logger.debug("An image url was not found in the wapi checkin response");
			} else {
				imageUrl = m.group(1);
				requestAndSaveImageToFile(this, imageUrl, false, "");
			}
		} else if (wapiRequestType.equals("jsoncheckin")) {
			m = patJsonImg.matcher(resultContents);
			result = m.find();
			if (!result) {
				logger.debug("An image url was not found in the wapi jsoncheckin response");
			} else {
				imageUrl = m.group(1);
				
				String filename = URLDecoder.decode(imageUrl, "UTF-8");
				filename = StringUtils.substringAfterLast(filename, "/");
				filename = FilenameUtils.removeExtension(filename);
				String type = StringUtils.substringAfterLast(imageUrl, "elementcode=");
				type = StringUtils.substringBefore(type,"&");
				
				if (imageUrl.toLowerCase().contains("proximitybanner")) {
				
					requestAndSaveImageToFile(this, imageUrl, false, "_" + filename + "_type_" + type);
				}
				else { 
					requestAndSaveImageToFile(this, imageUrl, false, "");
				}
			}
		} else {
			logger.debug("An unknown request type of '" + wapiRequestType + "' was encountered in the URL: " + originalWapiRequestUrl);
		}
	}
	
	protected void doGetRequest(String url) throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();

	    logger.info("TestID: " + test.getTestCaseID());
		logger.info("GET Request: " + url.toString());
		
	    httpget = new HttpGet(url);
	    
	    setGetHeaders(0);
	    Utilities.Instance.logHeaders(httpget);
	    
	    HttpResponse response = httpclient.execute(httpget);
	    validateHeaders(response,0);

	    setupAndOutput(response);

	    Utilities.Instance.logHeaders(response);
	}
	
	protected void setIDs(String contents, String nodes) {
		Pattern patTransactionId = Pattern.compile("transactionid=([A-Z0-9]*)&");
		Matcher m = null;

		nodes = nodes.replace("${", "");
		nodes = nodes.replace("}", "");
		
		String[] nodenames = nodes.split(",");
		
		for (String node : nodenames) {
			if (node.equalsIgnoreCase("transactionid")) {
				m = patTransactionId.matcher(contents);
				if (m.find()) {
					replacements.put("${" + node + "}", m.group(1));
				}
			}
		}

		try {
			BufferedReader br = new BufferedReader(new StringReader(contents));

			while ((output = br.readLine()) != null) {
			//	logger.info(output);
				for (String node : nodenames) {
					if (output.contains(node)) {
						output = output.trim();
						output = output.replace("<![CDATA[", "");
						output = output.replace("]]>", "");
						output = output.substring(("<" + node + ">").length(),
								output.indexOf("</" + node + ">"));
						replacements.put("${" + node + "}", output);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

}
