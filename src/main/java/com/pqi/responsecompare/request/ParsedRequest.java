package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.data.DataDriven;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.json.SetCustomJSONMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedRequest {

	static Logger logger = Logger.getLogger(ParsedRequest.class);

	private String requestType = "";
	private String encoding = "";
	private String url = "";
	private String log = "";
	private String finalurl = "";
	private HashMap<String, String> headers = null;
	private HashMap<String, HashMap<String, String>> validations = null;
	private String postXMLSubs = "";
	private String compareString = "";
	private String ignoreString = "";
	private String xslfile = "";
	private int status = 0;
    private int increment = 0;
	private int patientNumber = 0;
	private Properties props = null;
	private StringEntity body = null;
	private String SQL = "";
	private String assign = null;
	private File body_file = null;
	private boolean ignore_global_headers = false;
	private boolean compare = false;
	private boolean ignore = false;
	private boolean basicAuthorization = false;
	private boolean openBug = false;
	private String openBugUrl = "";
	private String mailboxFolderName = "";
	private ArrayList<String> responsetext = new ArrayList<String>();
	private ArrayList<String> mapvalue = new ArrayList<String>();
	private HashMap<String,String> variableHash = new HashMap<String, String>();
	private String expectedVariable = "";
	private String actualVariable = "";
	private String waitTime = "";
	private String ICDFile = "";
	private String rshCommand = "";
	private boolean isIF = false;
	private boolean isELSE = false;
	private boolean runRequest = true;
	private boolean isSQL = false;
	private String conditional = "";
	private boolean testrail = false;
	private boolean reloadEnvironmentAgnosticProperties = false;
	private boolean responseTooLargeForCompareLink = false;

	public ParsedRequest(String requestType, String param) throws Exception {

		props = PropertiesSingleton.Instance.getProps();
		this.requestType = requestType;

		if (requestType.equals("REMOTE_SHELL")) {
			this.rshCommand = param;
		}

		if ((param.length() > 0 && param.substring(0, 1).equals("/")) && testrail == false) {
			param = "${consult-protocol}://${consult-host}:${consult-port}/${consult-end-point}" + param;
		}

		this.log = param;

		param = formatURL(param);
		this.url = param;

		if (requestType.matches("RUN_.*_SQL.*")) {
			isSQL = true;
		}

		headers = new HashMap<String, String>();
		validations = new HashMap<String, HashMap<String, String>>();
	}

	public void setEnvironmentAgnosticReload() {
		reloadEnvironmentAgnosticProperties = true;
	}

	public void reloadEnvironmentAgnosticProperties() throws Exception {
		if (reloadEnvironmentAgnosticProperties) {
			PropertiesSingleton.Instance.reloadEnvironmentAgnosticProps();
		}
		reloadEnvironmentAgnosticProperties = false;
	}

	public void setIsIF(boolean isIF) {
		this.isIF = isIF;
	}

	public boolean getRunRequest() {
		return runRequest;
	}

	public void setCompareString(String compare) {
		compareString = compare;
	}

	public String getCompareString() {
		return compareString;
	}

	public void setIgnoreString(String ignore) {ignoreString = ignore; }

	public String getIgnoreString() {return ignoreString; }

	public void setConditional(String conditional) {
		this.conditional = conditional;
	}

	public void setIsSQL() {
		this.isSQL = true;
	}

	public void setResponseTooLargeForCompareLink(boolean set) {
		responseTooLargeForCompareLink = set;
	}

	public boolean getResponseTooLargeForCompareLink() {
		return responseTooLargeForCompareLink;
	}

	public void testConditional() throws Exception {

		if(!conditional.isEmpty()) {
			String[] leftRight = StringUtils.split(conditional,"=");
			String left = leftRight [0].trim();
			String right = leftRight[1].trim();

			if (JSONToMap.Instance.getMap().get(left) == null) {
				throw new Exception(left + " was not found in the map");
			}

			if(isIF && !JSONToMap.Instance.getMap().get(left).toString().equals(right))
			{

				runRequest = false;
			}

			if (isELSE && JSONToMap.Instance.getMap().get(left).toString().equals(right))
			{
				runRequest = false;

			}
		}
	}

	public boolean getIsIF() {
		return isIF;
	}

	public void setIsELSE(boolean isELSE) {
		this.isELSE = isELSE;
	}

	public boolean getIsELSE() {
		return isELSE;
	}

	public boolean getIsSQL() {
		return isSQL;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setResponseText(String param) {
		responsetext.add(param);
	}

	public String getRshCommand() {
		return rshCommand;
	}

	public void setWaitTime(String param) {
		waitTime = param.trim();
	}

	public void setBasicAuthorization() {
		basicAuthorization = true;
	}

	public boolean getBasicAuthorization() {
		return basicAuthorization;
	}

	public String getWaitTime() {
		return waitTime;
	}

	public String getAssign() {
		return assign;
	}

	public void setICDFile(String icdFile) {
		ICDFile = icdFile;
	}

	public String getICDFile() {
		return ICDFile;
	}

	public void setComparisonVariables(String param) {
		expectedVariable = StringUtils.split(param, ",", 2)[0].trim();
		actualVariable = StringUtils.split(param, ",", 2)[1].trim();
	}

	public String getExpectedVariable() {
		return expectedVariable;
	}

	public String getActualVariable() {
		return actualVariable;
	}

	public void setActualVariable(String actual) {
		actualVariable = actual;
	}

	public void setExpectedVariable(String expected) {
		expectedVariable = expected;
	}

	public void setMapValue(String param) {
		mapvalue.add(param);
	}

	public ArrayList<String> getMapValue() { return mapvalue; }

	public ArrayList<String> getResponseText() {
		return responsetext;
	}

	public void setMailBoxFolderName(String folder) {
		mailboxFolderName = folder;
	}

	public void setCustomVariables() throws Exception {
		SetCustomJSONMap.Instance.setCustomVariables(variableHash);
	}

    public HashMap<String,String> getVariableHash() {
		return variableHash;
	}

	public String getMailBoxFolderName() {
		return mailboxFolderName;
	}

	public void setCompare(boolean compare) {
		this.compare = compare;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean getCompare() {
		return compare;
	}

	public boolean getIgnore() {return ignore; }

	public boolean getOpenBug() {
		return openBug;
	}

	public String getOpenBugUrl() {
		return openBugUrl;
	}

	public void setOpenBug(String param) {
		openBug = true;
		openBugUrl = param;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setXSLFile(String xsl_file) {
		xslfile = xsl_file;
	}

	public String getXSLFile() {
		return xslfile;
	}

	public String getLog() {return log;}

	public void setLog(String comment) {log = comment; }

	public String getURL() {
		String returnurl = "";

		if (finalurl.isEmpty()) {
			returnurl = url;
		} else {
			returnurl = finalurl;
		}

		return returnurl;
	}

	public String getSQL() {
		return SQL;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public void setFinalURL(String url) {
		finalurl = url;
	}

	public String getFinalURL() {
		return finalurl.replace("http://", "");
	}

	public void setIgnoreGlobalHeaders(boolean ignore) {
		this.ignore_global_headers = ignore;
	}

	public boolean getIgnoreGlobalHeaders() {
		return this.ignore_global_headers;
	}

	public void setHeader(String header) {
		String name, value = null;
		String[] headerstring = header.split(":");

		if (headerstring.length == 1) {
			name = headerstring[0];
			value = "";
		} else {
			name = headerstring[0].trim();
			value = headerstring[1].trim();
		}
		headers.put(name, value);
	}

	public void incrementPatientNumber(String param) throws Exception {
		File patient_number_file = new File(DataDriven.Instance.getPatient_number_filename());
		String p_number = FileUtils.readFileToString(patient_number_file);
		patientNumber = new Integer(p_number.trim()).intValue();
		patientNumber++;
		p_number = Integer.toString(patientNumber);
		FileUtils.writeStringToFile(patient_number_file, p_number);
		JSONToMap.Instance.getMap().put(param,p_number);
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setPostXMLSubs(String subs) {
		postXMLSubs = subs;
	}

	public String getPostXMLSubs() {
		return postXMLSubs;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = Integer.valueOf(status);
	}

	public void setCustomMap(String parameters) {
		String[] key_value_pair = parameters.split(",");
		JSONToMap.Instance.put(key_value_pair[0], key_value_pair[1]);
	}

	public void setDateTime(String param) {
		String timeStamp = new SimpleDateFormat(param).format(Calendar.getInstance().getTime());
		JSONToMap.Instance.put("current_datetime",timeStamp);
	}

	public void setValidation(String validationtype, String param) {
		if (validations.get(validationtype) == null) {
			validations.put(validationtype, new HashMap<String, String>());
		}

		String[] params = { "", "" };
		if (param.trim().matches("\\(.*\\)")) {
			String headerSet = param.replaceAll("[\\(\\)]", "");
			for (String next : headerSet.split(",")) {
				validations.get(validationtype).put(next.toLowerCase(), "");
			}
		} else if (param.split(":", 2).length == 1) {
			params[0] = param.trim();
			validations.get(validationtype).put(params[0].trim(), params[1].trim());
		} else {
			params = param.split(":", 2);
			validations.get(validationtype).put(params[0].trim(), params[1].trim());
		}
	}

	public HashMap<String, HashMap<String, String>> getValidations() {
		return validations;
	}

	public void setBody(String entity) throws UnsupportedEncodingException, IOException {
		if (this.body != null) {
			String append = IOUtils.toString(this.body.getContent());
			this.body = new StringEntity(append + entity);
		}
		else {
			this.body = new StringEntity(entity);
		}
	}

	public void setSQL(String SQL) throws Exception {
		StringBuffer sb = new StringBuffer(this.SQL);
		sb.append(InterpolateRequest.Instance.interpolateString(SQL));
		sb.append(" ");
		this.SQL = sb.toString();
	}

	public void setJSONVariable(String pair) throws Exception {
		String name = null;
		String value = null;
		String[] variableString = null;

		Pattern namePat = Pattern.compile("(\\,\\s*[\\w|\\-|\\_]+$)");
		Pattern valuePat = Pattern.compile("\\,\\s*([\\w|\\-|\\_]+$)");
		Pattern variablePat = Pattern.compile("\\,\\s*(\\$\\{[\\w|\\_|\\-]+\\}$)");
		Matcher nameMatch = namePat.matcher(pair);
		Matcher valueMatch = valuePat.matcher(pair);
		Matcher variableMatch = variablePat.matcher(pair);

		if (this.assign == null) {

			if (nameMatch.find()) {
				name = StringUtils.removeEnd(pair, nameMatch.group(0)).trim();
			} else if (variableMatch.find())
			{
				name = StringUtils.removeEnd(pair, variableMatch.group(0)).trim();
				variableMatch = variablePat.matcher(pair);
			} else {
				assign = pair;
				return;
			}
		} else {
			if(nameMatch.find()) {
				name = assign.concat(StringUtils.removeEnd(pair, nameMatch.group(0)).trim());
			} else {
				assign = assign.concat(pair);
				return;
			}
		}

		if (valueMatch.find()) {
			value = valueMatch.group(1);
		}

		if (variableMatch.find()) {
			value = variableMatch.group(1);
		}

		value = InterpolateRequest.Instance.interpolateString(value);
		variableHash.put(name, value);
		assign = null;
	}

	public void setVariable(String pair) throws Exception {
		String name = null;
		String value = null;
		String[] variableString = null;

		Pattern namePat = Pattern.compile("(\\,\\s*[\\w|\\-|\\_]+$)");
		Pattern valuePat = Pattern.compile("\\,\\s*([\\w|\\-|\\_]+$)");
		Pattern variablePat = Pattern.compile("\\,\\s*(\\$\\{[\\w|\\_|\\-]+\\}$)");
		Pattern variablePat2 = Pattern.compile("\\,\\s*(.*\\$\\{[\\w|\\_|\\-||\\,]+\\})");
		Matcher nameMatch = namePat.matcher(pair);
		Matcher valueMatch = valuePat.matcher(pair);
		Matcher variableMatch = variablePat.matcher(pair);
		Matcher variableMatch2 = variablePat2.matcher(pair);

		if (this.assign == null) {

			if (nameMatch.find()) {
				name = StringUtils.removeEnd(pair, nameMatch.group(0)).trim();
			} else if (variableMatch.find())
			{
				name = StringUtils.removeEnd(pair, variableMatch.group(0)).trim();
				variableMatch = variablePat.matcher(pair);
			} else if (variableMatch2.find())
			{
				name = StringUtils.removeEnd(pair, variableMatch2.group(0)).trim();
				variableMatch2 = variablePat2.matcher(pair);

			} else {
				assign = pair;
				return;
			}
		} else {
			if(nameMatch.find()) {
				name = assign.concat(StringUtils.removeEnd(pair, nameMatch.group(0)).trim());
			} else {
				assign = assign.concat(pair);
				return;
			}
		}

		if (valueMatch.find()) {
			value = valueMatch.group(1);
		}

		if (variableMatch.find()) {
			value = variableMatch.group(1);
		}

		if (variableMatch2.find()) {
			value = variableMatch2.group(1);
		}

		value = InterpolateRequest.Instance.interpolateString(value);
		variableHash.put(name, value);
		JSONToMap.Instance.put(name, value);
		assign = null;
	}

	public void setBodyFile(String filename) {
		String location = System.getProperty("user.dir");
		this.body_file = new File(location + "/data/consult/post_file_body/" + filename);
	}

	public void setBasicAuthentication(String param) throws Exception {
		byte[] encodedAuth = Base64.encodeBase64(param.getBytes(Charset.forName("ISO-8859-1")));
		encoding = "Basic " + new String(encodedAuth);
	}

	public void setTestRail(String param) throws Exception {
		testrail = true;
		setBasicAuthentication(param);
		String testRailProtocol = PropertiesSingleton.Instance.getProperty("testrail-protocol");
		String testRailHost  = PropertiesSingleton.Instance.getProperty("testrail-host");
		String testrailTestrunName = PropertiesSingleton.Instance.getProperty("testrail-testrun-name");
		JSONToMap.Instance.put("testrail-testrun-name",testrailTestrunName);
		url = StringUtils.replacePattern(url,".*\\:\\/\\/[\\d|\\w|\\.\\-]+", testRailProtocol + "\\:\\/\\/" + testRailHost);
	}

	public File getBodyFile() {
		return this.body_file;
	}

	public HttpEntity getMultipartEntity() {

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addBinaryBody("upfile", body_file, ContentType.MULTIPART_FORM_DATA, body_file.getName());
		return builder.build();
	}

	public StringEntity getBody() throws Exception {

		String bodyString = "";
		// If we have a body, interpolate the string.
		if (body != null) {
			String inputLine = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(body.getContent()));
			while ((inputLine = in.readLine()) != null) {
				bodyString = bodyString + inputLine;
			}
			in.close();
			bodyString = InterpolateRequest.Instance.interpolateString(bodyString);
			this.body = new StringEntity(bodyString);
		}
		return this.body;
	}

	private String formatURL(String param) throws Exception {

		param = replaceParam(param);
		return param;
	}

	private String replaceParam(String param) throws Exception {
		Pattern pattern = Pattern.compile("\\$\\{([\\w|-]+)\\}");
		Matcher matcher = pattern.matcher(param);

		while (matcher.find()) {
			// logger.debug("Here is matcher 0: " + matcher.group(0));
			// logger.debug("Here is matcher 1: " + matcher.group(1));
			// logger.debug("Here is property: " +
			// props.getProperty(matcher.group(1)));
			if (props.getProperty(matcher.group(1)) == null) {
			} else if (props.getProperty(matcher.group(1)).equals("/")) {
				param = param.replace("/" + matcher.group(0), "");
			} else if (props.getProperty(matcher.group(1)).isEmpty()) {
				// should only be empty if it is for port
				param = param.replace(":" + matcher.group(0), "");
			} else if (!props.getProperty(matcher.group(1)).isEmpty()) {
				param = param.replace(matcher.group(0), props.getProperty(matcher.group(1)));
			}
		}

		// logger.debug("Here is param: " + param);
		return param;

	}

}