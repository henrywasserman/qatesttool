package com.pqi.responsecompare.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.pqi.responsecompare.configuration.PathGenerator;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.reports.PatchToHTML;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Transform;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Request {

	static final Logger logger = Logger.getLogger(Request.class);
	
	protected String output = "";
	protected String file = "";
	protected StringBuffer outputfile = null;
	protected String logoutputfile = "";
	protected StringBuffer fullpath = null;
	protected StringBuffer requestprops = null;
	protected StringBuffer builddir = null;
	protected StringBuffer post = null;
	protected StringBuffer get = null;
	protected StringBuffer postXML = null;
	protected StringBuffer response = new StringBuffer();
	protected Properties props = null;
	protected String url = "";
	protected String log = "";
	protected StringBuffer results = null;
	protected boolean passfail = true;
	protected boolean result = true;
	protected HashMap<String, String> replacements = new HashMap<String, String>();
	public TestCase test = null;
	protected HttpGet httpget = null;
	protected HttpHead httphead = null;
	protected HttpGetWithEntity httpgetwithbody = null;
	protected HttpDelete httpdelete = null;
	protected HttpPost httppost = null;
	protected HttpPut httpput = null;
	protected String requestType = "";
	protected String validateHeaderRegEx = "";
	protected PathGenerator pathGenerator = null;
	protected StringEntity entity = null;
	protected Properties headerprops = null;
	protected int test_request_counter;
	
	protected Request() {
		pathGenerator = new PathGenerator(test);
		props = Utilities.Instance.getRequestProperties();
		results = new StringBuffer(10);
		passfail = true;
		result = true;
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + File.separator +
				"properties" + File.separator + "log4j.properties");
		if (builddir!=null) {
			response = new StringBuffer(builddir.toString());
		}

		response.append(Utilities.Instance.getResponseCompareRoot()+"/data"+"/response/");
		
		headerprops = PropertiesSingleton.Instance.getHeaderProps();
	}
	
	public TestCase getTest() {
		return test;
	}
	
	public PathGenerator getPathGenerator() {
		return pathGenerator;
	}

	protected Request(TestCase test) throws Exception {
		pathGenerator = new PathGenerator(test);
		results = new StringBuffer(10);
		passfail = true;
		result = true;
		
		headerprops = PropertiesSingleton.Instance.getHeaderProps();

		this.test = test;

		test_request_counter = test.getTestRequestCounter();
		//parsedrequests = test.getRequests();

		log = test.getRequests().get(test_request_counter).getLog();
		url = test.getRequests().get(test_request_counter).getURL().trim();
		url = InterpolateRequest.Instance.interpolateString(url);
		if (!test.getRequests().get(test_request_counter).getRequestType().contains("SQL")) {
			url = url.replace(" ", "%20");
		}

		if (!url.isEmpty() && url.startsWith("http")) {
			URL javaUrl = new URL(url);

			url = javaUrl.toString();
		}
		
		props = new Properties();

		builddir = new StringBuffer(System.getProperty("user.dir") +
				File.separator + ".." + File.separator);
		
		requestprops = new StringBuffer(System.getProperty("user.dir") +
				File.separator + 
				"properties" + File.separator +
				((System.getProperties().getProperty("propfile") == null) ? "responsecompare.properties" :
					System.getProperties().getProperty("propfile") + ".properties"));
		
		response.append(Utilities.Instance.getResponseCompareRoot() + File.separator+ "data"+File.separator + "response" + File.separator);
		logoutputfile = pathGenerator.getResponseDir() + test.getTestCaseID() + "_" + Integer.valueOf(test.getTestRequestCounter()).toString() + ".log";

		if (requestprops != null) {
			try {
				FileInputStream fis = new FileInputStream(
						requestprops.toString());
				props.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected String getFileExtFromContentType(Request request, HttpResponse response) {
		String extension = ".json"; // Default to an extension of .json
		String contentType = "";
		if (response.getFirstHeader("Content-Type") != null) {
			contentType = response.getFirstHeader("Content-Type").getValue().trim();
		}
		Pattern extension_pattern = Pattern.compile("^application/(\\w+).*");
		
		if (response.containsHeader("Content-Type")) {
			Matcher match = extension_pattern.matcher(contentType);
		
			// Note: we ignore Content-Type if it's text/html because this is set incorrectly in some cases
			if (match.find()) {
				//The only one currently tested here is json, others are legacy code
				if (match.group(1).toLowerCase().equals("png")) {
					extension = ".png";
					request.getTest().setImageType("png");
				} else if (match.group(1).toLowerCase().equals("jpeg")) {
					extension = ".jpg";
					request.getTest().setImageType("jpg");
				} else if (match.group(1).toLowerCase().equals("json")) {
					extension = ".json";
				} else if (match.group(0).toLowerCase().contains("application/soap+xml")) {
					extension = ".xml";
				}
			}
			else if (contentType.equals("text/plain")) {
				extension = ".txt";
			}
			
		}
		return extension;
	}
	
	protected void cleanResults() {
		results = new StringBuffer(10);
	}

	protected String getFilename(String filename) {
		int sep = filename.lastIndexOf(System.getProperties().getProperty(
				"file.separator"));
		int dot = filename.lastIndexOf('.');
		return filename.substring(sep + 1, dot);
	}

	protected boolean getPassFail() {
		return passfail;
	}

	protected boolean getResult() {
		return result;
	}

	protected String getResults() {
		return results.toString();
	}

	public abstract void sendRequest() throws Exception;

	protected void resetOut(PrintStream out) {
		System.setOut(out);
	}

	protected void setOut() {
		System.setOut(new PrintStream(new BufferedOutputStream(
				new FileOutputStream(java.io.FileDescriptor.out), 128), true));
	}

	public void setupAndOutput(String response, String extension, String name) throws Exception {
        //if (extension.equals(".json")) {
        //    response = (new JSONObject(response)).toString(2);
        //}

        fullpath = new StringBuffer(file);

        if (name.isEmpty()) {
            outputfile = new StringBuffer(pathGenerator.getResponseDir() + test.getTestCaseID()
                    + "_" + Integer.valueOf(test.getTestRequestCounter()).toString());
        } else {

              outputfile = new StringBuffer(pathGenerator.getResponseDir() + test.getTestCaseID()
                        + "_" + name);

        }
        outputfile.append(extension);
        logger.debug("Output file is: " + outputfile.toString());

        FileUtils.writeStringToFile(new File(outputfile.toString()), response);
    }

	public void setupAndOutput(String response, String extension) throws Exception {

        //if (extension.equals(".json")) {
        //    response = (new JSONObject(response)).toString(2);
        //}

        fullpath = new StringBuffer(file);
        outputfile = new StringBuffer(pathGenerator.getResponseDir()
				+ test.getTestCaseID() + "_" +
				Integer.valueOf(test.getTestRequestCounter()).toString());

        StringBuffer outputfileNoExt = new StringBuffer(outputfile);
        outputfile.append(extension);
        logger.debug("Output file is: " + outputfile.toString().replace(" ","\\ "));
        FileUtils.writeStringToFile(new File(outputfile.toString()), response);
    }

	protected void setupAndOutput(HttpResponse response) throws Exception {
		fullpath = new StringBuffer(file);
		outputfile = new StringBuffer(pathGenerator.getResponseDir() + test.getTestCaseID() + "_" + Integer.valueOf(test.getTestRequestCounter()).toString());

		// First, let getFileExtFromContentType try to figure out what kind of file this is
		String extension = getFileExtFromContentType(this, response);
		StringBuffer outputfileNoExt = new StringBuffer(outputfile);
		outputfile.append(extension);

		logger.debug("Output file is: " + outputfile.toString().replace(" ","\\ "));

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			ObjectMapper mapper = new ObjectMapper();
			String content = EntityUtils.toString(entity);
			JsonNode jsonNode = mapper.readTree(content);
			content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
			FileUtils.writeStringToFile(new File(outputfile.toString()),content);
		} else {
			FileUtils.writeStringToFile(new File(outputfile.toString()), "");
		}

		//If it is a text file - turn it into json.
		if (extension.equals(".txt")) {
			String fileString = FileUtils.readFileToString(new File (outputfile.toString()));
			//escape all commas
			fileString = StringEscapeUtils.escapeJson(fileString);
			outputfile = new StringBuffer(outputfile.toString().replace(".txt", ".json"));
			FileUtils.write(new File(outputfile.toString()), "[\"" + fileString + "\"]");
		}
		
		// Second, since we could have defaulted to an extension incorrectly,
		// examine the file content to see what it really is
		String fileContents = FileUtils.readFileToString(new File(outputfile.toString()));
		if (fileContents.contains("<html ")) {
			outputfileNoExt.append(".html");
			new File(outputfileNoExt.toString()).delete();
			new File(outputfile.toString()).renameTo(new File(outputfileNoExt.toString()));
			outputfile = outputfileNoExt;
		} //else if (fileContents.matches("^[\\s]*\\{.*")) {
			//outputfileNoExt.append(".json");
			//new File(outputfile.toString()).renameTo(new File(outputfileNoExt.toString()));
			//new File(outputfileNoExt.toString()).delete();
			//outputfile = outputfileNoExt;
		//}
		
		if (fileContents != null) {
		//	logger.debug("File: " + outputfile + " before any transformation: ");
		//	logger.debug(fileContents);
		}
		
	}
	
	protected void requestAndSaveImageToFile(Request request, String imageUrl, boolean doPost, String fileNamePostfix) throws Exception {
		imageUrl = StringEscapeUtils.unescapeXml(imageUrl.trim());
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		if (doPost) {
			HttpPost httppost = new HttpPost(imageUrl);
			request.addHeaders();
			response = httpclient.execute(httppost);
		} else {
			HttpGet httpget = new HttpGet(imageUrl);
			request.addHeaders();
			response = httpclient.execute(httpget);
		}
		request.validateHeaders(response,0);
		HttpEntity entity = response.getEntity();

		StringBuffer outputfile = new StringBuffer(request.getPathGenerator().getResponseDir() +
				request.getTest().getTestCaseID() + fileNamePostfix);
		
		outputfile.append(getFileExtFromContentType(request, response));
		OutputStream outputstream = new FileOutputStream(outputfile.toString());
		entity.writeTo(outputstream);
		outputstream.close();
		logger.info("Image URL: " + imageUrl);
		logger.info("Image file: " + outputfile.toString());
	}	
	
	protected String getResponseType(String url) {
		String type = "";
		
		if (url.contains("enduserregistration")) {
			type = "enduserid";
		}
		else if (url.contains("enduseractions")) {
			type = "enduserid";
		}
		else if (url.contains("endusercheckin")) {
			type = "endusercheckin";
		}
		else if (url.contains("enduserplaceidcheckin")) {
			type = "placeid";
		}
		else if (url.contains("enduserareacheckin")) {
			type = "endusercheckin";
		}
		//Right now when the url is Empty it means we are doing an
		//XML POST
		else if (url.contains("api2") || url.contains("adserver2")) {
			type = "enduserid";
		}
		return type;
	}
	
	protected void setValidateHeaderRegex(String regex) {
		validateHeaderRegEx = regex;
	}
	
	protected String getValidateHeaderRegex() {
		return validateHeaderRegEx;
	}
	
	public void addHeaders() {
	    HashMap <String,String> getheaders = test.getRequests().get(0).getHeaders();
	    for (Map.Entry<String,String> header : getheaders.entrySet()) {
			httpget.addHeader(header.getKey(),header.getValue());
		}
	}
	
	protected void setRequestType(String type) {
		requestType = type;
	}
	
	protected String getRequestType() {
		return requestType;
	}

	protected void setDeleteHeaders (int request) throws Exception {

		setGlobalHeaders(request, httpdelete);

		HashMap<String,String> headers = test.getRequests().get(request).getHeaders();
		
		for (Map.Entry<String,String> header: headers.entrySet()) {
			httpdelete.addHeader(header.getKey(), header.getValue());
		}

		String encoding = test.getRequests().get(request).getEncoding();
		if (!encoding.isEmpty()) {

			String authHeader = new String(encoding);
			httpget.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}

	}	
	
	
	private void setGlobalHeaders(int request, HttpRequestBase requestbase) throws Exception {

		String key = "";
		if (test.getRequests().get(request).getIgnoreGlobalHeaders() == false) {

			if (test.getRequests().get(request).getBasicAuthorization()) {
				String auth = headerprops.getProperty("username") + ":" + headerprops.getProperty("password");
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
				String authHeader = "Basic " + new String(encodedAuth);
				requestbase.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
			}

			Enumeration<?> e = headerprops.propertyNames();
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				if (key.equals("username") || key.equals("password")) {

					if (test.getRequests().get(request).getURL().contains("/login") && !test.getRequests().get(request).getURL().contains("pat")) {
						requestbase.addHeader(key, headerprops.getProperty(key));
					}
					continue;
				}

				if (JSONToMap.Instance.getMap().get(key) != null) {
					StringBuffer value = InterpolateRequest.Instance.interpolateString(new StringBuffer(headerprops.getProperty(key)));
					requestbase.addHeader(key,value.toString());
				} else {
					if (!headerprops.getProperty(key).contains("${")) {
						requestbase.addHeader(key,headerprops.getProperty(key));
					} else {
						logger.debug("Warning: Header Contains " + headerprops.getProperty(key));
					}
				}
			}
		}
	}


	protected void setGetHeadersWithBody(int request) throws Exception {

		setGlobalHeaders(request, httpgetwithbody);

		HashMap<String,String> headers = test.getRequests().get(request).getHeaders();

		for (Map.Entry<String,String> header: headers.entrySet()) {
			httpgetwithbody.addHeader(header.getKey(), header.getValue());
		}

		String encoding = test.getRequests().get(request).getEncoding();
		if (!encoding.isEmpty()) {

			String authHeader = new String(encoding);
			httpget.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}

	}


	protected void setPostHeaders(int request) throws Exception {

		HashMap<String, String> headers = test.getRequests().get(request).getHeaders();

		for (Map.Entry<String, String> header : headers.entrySet()) {
			if (header.getKey().equals("Accept") && entity != null) {
				entity.setContentType(header.getValue());
			}

			StringBuffer value = InterpolateRequest.Instance.interpolateString(new StringBuffer(header.getValue()));
			httppost.addHeader(header.getKey(), value.toString());
		}

		String encoding = test.getRequests().get(request).getEncoding();
		if (!encoding.isEmpty()) {

			String authHeader = new String(encoding);
			httppost.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}

		setGlobalHeaders(request, httppost);
	}


	protected void setGetHeaders(int request) throws Exception {

		setGlobalHeaders(request, httpget);

		HashMap<String,String> headers = test.getRequests().get(request).getHeaders();

		for (Map.Entry<String,String> header: headers.entrySet()) {
			httpget.addHeader(header.getKey(), header.getValue());
		}

		String encoding = test.getRequests().get(request).getEncoding();
		if (!encoding.isEmpty()) {

			String authHeader = new String(encoding);
			httpget.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}

	}


	protected void setPutHeaders(int request) throws Exception {

		setGlobalHeaders(request, httpput);

		HashMap<String,String> headers = test.getRequests().get(request).getHeaders();
		
		for (Map.Entry<String,String> header: headers.entrySet()) {
			StringBuffer value = InterpolateRequest.Instance.interpolateString(new StringBuffer(header.getValue()));
			httpput.addHeader(header.getKey(), value.toString());
		}
		String encoding = test.getRequests().get(request).getEncoding();
		if (!encoding.isEmpty()) {

			String authHeader = new String(encoding);
			httpput.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}
	}

	protected boolean isJSONRequest(int request, CloseableHttpResponse response) {
		boolean isjason = false;
		
        if (response.getEntity() == null) {
        	isjason = true;
        }
        else if (response.getEntity().getContentType() == null ) {
			isjason = true;
		}
		else if (response.getEntity().getContentType().getValue().contains("application/json")) {
		
			isjason = true;
		}
		//If it's text we turn it into json for convenience
		else if (response.getEntity().getContentType().getValue().contains("text/plain")) {
			isjason = true;
		}
		
		return isjason;
	}

	protected void getImages() {
		try {
			String xmloutput = FileUtils.readFileToString(new File(outputfile.toString()));
			File xslfile = new File (pathGenerator.getXslDir() + "imageurls_response.xsl");
			Transform transform = new Transform(xmloutput, xslfile);
			String transformedxml = transform.getResultString();
			
			String[] lines = transformedxml.split("\n");
			
			boolean foundImageNode = false;
			
			int counter = 0;
			String type = "";
			String filename = "";
			
			for (String output : lines) {
				counter++;
				if (output.contains("<image>")) foundImageNode = true;
				if (output.contains("</image>")) foundImageNode = false;
				if ((output.contains("<url>")) && foundImageNode) {
					output = output.trim();
					output = output.replace("<![CDATA[", "");
					output = output.replace("]]>", "");
					output = output.replace("<url>", "");
					output = output.replace("</url>", "");

					if (output.toString().trim().equals("")) continue;
					
					type = lines[counter];
					type = type.replace("<type>","");
					type = type.replace("</type>","").trim();
					
					filename = URLDecoder.decode(output, "UTF-8");
					filename = StringUtils.substringAfterLast(filename, "/");
					filename = FilenameUtils.removeExtension(filename);
					
					requestAndSaveImageToFile(this, output, false, 
							"_" + filename + "_type_" + type);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CloseableHttpResponse executeRequest(HttpRequestBase requestBase) throws Exception {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String time = test.getRequests().get(test_request_counter).getWaitTime();
		int counter = 0;
		if (!time.isEmpty()) {
			counter = Integer.valueOf(time);
		}
		int loopCounter = 0;
		while(true) {
			response = test.getHttpClient().execute(requestBase);
			if (time.isEmpty() || (loopCounter == counter)) {
				break;
			} else if (response.getEntity() != null) {
				entity = response.getEntity();
				if (entity.getContentLength() > 2) {
					break;
				}
			}
			Thread.sleep(1000);
			loopCounter++;
			response.close();
			logger.debug("counter is " + Integer.toString(counter) + " and loopCounter is " + Integer.toString(loopCounter));
		}
		return response;
	}

	public void validateHeaders(HttpResponse response, int request) throws Exception {
		Pattern p = null;
		Matcher m = null;
		Header[] headers = response.getAllHeaders();
		HashMap<String, HashMap<String,String>> validations = null;
		HashMap<String,String> headerValidations = null;
		validations = test.getRequests().get(request).getValidations();
		boolean found = false;

		for (Map.Entry<String,HashMap<String,String>> validation : validations.entrySet()) {
			if (validation.getKey().toLowerCase().equals("validate_header")) {
				headerValidations = validation.getValue();
				for (Map.Entry<String,String> validate : headerValidations.entrySet()) {
					for (Header httpheader : headers) {
						logger.debug(httpheader.getName());
						logger.debug(httpheader.getValue());
						logger.debug(validate.getKey());
						logger.debug(validate.getValue());
						if (httpheader.getName().equals(validate.getKey())) {
						
							p = Pattern.compile(validate.getValue());
							m = p.matcher(httpheader.getValue());
							found = m.find();
							break;
						}
					}
					passfail = found;
					results = new StringBuffer(pathGenerator.getResponseDir() + test.getTestCaseID() + ".xml" + " Did not find Header: " + validate.getKey()
							+ ":" + validate.getValue() + " Here are the headers: " +
							StringUtils.join(headers)
					); 
					found = false;
					Assert.assertTrue(results.toString(), passfail);
				}
				
			}
			else if (validation.getKey().toLowerCase().equals("validate_header_notexist")) {
				headerValidations = validation.getValue();
				for (Map.Entry<String,String> validate : headerValidations.entrySet()) {
					for (Header httpheader : headers) {
						logger.debug(httpheader.getName());
						logger.debug(httpheader.getValue());
						logger.debug(validate.getKey());
						logger.debug(validate.getValue());
						if (httpheader.getName().equals(validate.getKey())) {
						
							p = Pattern.compile(validate.getValue());
							m = p.matcher(httpheader.getValue());
							found = m.find();
							break;
						}
					}
					passfail = !found;
					results = new StringBuffer(pathGenerator.getResponseDir() + test.getTestCaseID() + ".xml" + " Found Header: " + validate.getKey()
							+ ":" + validate.getValue());
					
					found = false;
					Assert.assertTrue(results.toString(), passfail);
				}
			}
			else if (validation.getKey().toLowerCase().equals("validate_header_set")) {
				List<String> listHeaders = new ArrayList<String>();
				for (Header httpheader : headers) {
					listHeaders.add(httpheader.getName().toLowerCase());
				}
				headerValidations = validation.getValue();
				for (Map.Entry<String,String> validate : headerValidations.entrySet()) {
					passfail = false;
					results = new StringBuffer ("The header: " + validate.getKey() + " is missing from the set of headers" +
							listHeaders.contains(validate.getKey().toLowerCase().trim()));
					listHeaders.remove(validate.getKey().toLowerCase().trim());
				}
				
				if (!listHeaders.isEmpty()) {
					passfail = false; 
					results = new StringBuffer("There are unexpected headers: " + listHeaders.toString());
					Assert.assertTrue(results.toString(), passfail);
				}
			}
		}
	}
}
