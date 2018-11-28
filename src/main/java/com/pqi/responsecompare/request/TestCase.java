package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.security.SSLSingleton;
import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.util.*;

public class TestCase {
	
	private String testCaseID="", testCaseDescription="";
	private int lineNum;
	private List<ParsedRequest> requests;
	private int test_request_counter = -1;
	private int reqcounter = -1;
	private String comparisonType = "";
	private String imageType = "";
	private String requestfile = "";
	private int numImagesSaved = 0;
	private String xsl = "";
	private int testcasenumber = 0;
	private int status = 0;
	private boolean isWapi = false;
	private boolean isSQL = false;
	private boolean isBody = false;
	private boolean isAssign = false;
	private boolean isAppend = false;
	private boolean isJSONAssign = false;
	private HashMap<String,String> sqlStrings = new HashMap<String,String>();
	private List<String> dataValidators = new ArrayList<String>();
	private CloseableHttpClient httpclient = null;
	private boolean isIF = false;
	private boolean isELSE = false;
	private String ifCondition = "";

	private static final HashMap<String,String> commands;
	static
	{
		commands = new HashMap<String, String>();
		commands.put("ADD_ROLE_TO_USER_FROM_CSV", "");
		commands.put("CREATE_AUTOMATION_ROLE", "");
		commands.put("CREATE_PATIENT_PORTAL_USER", "");
		commands.put("CREATE_PATIENTS_FROM_CSV", "");
		commands.put("CREATE_USERS", "");
		commands.put("CREATE_AGNOSTIC_PROPERTIES", "");
		commands.put("CREATE_JSON_FOR_ROLE", "");
		commands.put("EDI_TO_MYSQL","");
		commands.put("EDI_TO_TALEND","");
		commands.put("REMOTE_SHELL", "");
		commands.put("DELETE", "");
		commands.put("LOG", "");
		commands.put("POST", "");
		commands.put("POST_MULTIPART", "");
		commands.put("EXPECT_ERROR", "");
		commands.put("GET_CONFIRMATION_TOKEN", "");
		commands.put("GET","");
		commands.put("GET_IMAGE","");
		commands.put("GET_IMAGES","");
		commands.put("GET_WITH_AUTHCACHE","");
		commands.put("GETLIST_JASON","");
		commands.put("POST_IMAGE","");
		commands.put("POST_IMAGES","");
		commands.put("POST","");
		commands.put("POST_WITH_AUTHCACHE","");
		commands.put("POST_WITH_FILE_BODY","");
		commands.put("PUT","");
		commands.put("RUN_SQL_FROM_FILE","");
		commands.put("RUN_ORACLE_SQL","");
		commands.put("RUN_PL_SQL","");
		commands.put("RUN_SQLSERVER_SQL","");
		commands.put("RUN_SQLSERVER_EXECUTE_SQL","");
		commands.put("RUN_TERADATA_EXECUTE_SQL","");
		commands.put("RUN_ORACLESERVER_EXECUTE_SQL","");
		commands.put("RUN_MYSQLSERVER_EXECUTE_SQL","");

	}

	public TestCase() {
		requests = new ArrayList<ParsedRequest>();
	}
	
	public void setHttpClient() {
		
		if (httpclient == null || test_request_counter == 0) {
			SSLConnectionSocketFactory sslsf = SSLSingleton.INSTANCE.getSSLConnectionSocketFactory();
			httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
		}
	}
	
	public boolean getIsBody() {
		return isBody;
	}

	public void setIsIF(boolean isIF) {
		this.isIF = isIF;
	}

	public void setIsELSE(boolean isELSE) {
		this.isELSE = isELSE;
	}

	public boolean getIF() {
		return isIF;
	}

	public void setIfCondition(String ifCondition) {
		this.ifCondition = ifCondition;
	}

	public boolean getELSE() {
		return isELSE;
	}

	public void setIsBody(boolean isBody) {
		this.isBody = isBody;
	}

	public void setIsSQL(boolean isSQL) {
	    this.isSQL = isSQL;
    }

	public boolean getIsAssign() {return isAssign; }

	public boolean getIsJSONAssign() {return isJSONAssign;}

	public boolean getIsSQL() {return isSQL;}

	public void resetTestList() {
		requests = new ArrayList<ParsedRequest>();
		reqcounter = -1;
		test_request_counter = 0;
		comparisonType = "";
		RequestFactory.Instance.resetReq();
	}

	public void setIsAssign(boolean isAssign) {
		this.isAssign = isAssign;
	}

	public void setIsJSONAssign(boolean isJSONAssign) {this.isJSONAssign = isJSONAssign;}
	
	public void httpClientClose() throws Exception {
		httpclient.close();
	}
	
	public CloseableHttpClient getHttpClient() {
		return httpclient;
	}
	
	public void incrementTestRequestCounter() {
		test_request_counter++;
        Utilities.Instance.setStartNumber(PropertiesSingleton.Instance.getProps().getProperty("increment-startnumber"));
        Utilities.Instance.setIncrementNumber();
        JSONToMap.Instance.put("increment",Utilities.Instance.getIncrementNumber());
	}
	
	public int getTestRequestCounter() {
		return test_request_counter;
	}
	
	public void addCommand(String command, String param) throws Exception {

		if (commands.containsKey(command)) {
			addParsedRequest(command, param);
		}

		else if (command.equals("ELSE") && isAppend == false) {
			setIsIF(false);
			setIsELSE(true);
		}
		else if (command.equals("IF") && isAppend == false) {
		  setIsIF(true);
		  setIsELSE(false);
		  setIfCondition(param);
		}
		else if (command.equals("GET_WAPI_IMAGE")) {
			addParsedRequest(command, param);
			isWapi = true;
		}
		//else if (commands.containsKey("SAMPLE_KEYWORD")) {
		//	addParsedRequest(command, param);
		//}
		else if (command.equals("VERIFY_ICD")) {
			addParsedRequest(command, param);
			requests.get(reqcounter).setICDFile(param);
		}
		else if (isAssign || command.equals("ASSIGN")) {
			isAssign = true;
			requests.get(reqcounter).setVariable(param);
			if (requests.get(reqcounter).getAssign() == null) {
				isAssign = false;
			}
		}
		else if (isBody || command.equals("BODY")) {
			isBody = true;
			requests.get(reqcounter).setBody(param);
		}
		else if (isSQL || command.matches("RUN_.*_SQL")) {
            isSQL = true;
            isAppend = true;
            if (ValidCommands.Instance.getAllValidCommands().contains(param)) {
                isAppend =  false;
            } else {
                requests.get(reqcounter).setSQL(param);
                isAppend = true;
            }
        }
		else if (command.equals("BODY_FILE")) {
			requests.get(reqcounter).setBodyFile(param);
		}
		else if (command.equals("CUSTOM_MAP")) {
			requests.get(reqcounter).setCustomMap(param);
		}
		else if (command.equals("GENERATE_GUID")) {
			requests.get(reqcounter).setCustomMap("guid," + UUID.randomUUID());
		}
		else if (command.equals("IGNORE_GLOBAL_HEADERS")) {
			requests.get(reqcounter).setIgnoreGlobalHeaders(true);
		}
		else if (command.equals("OPEN_BUG")) {
			requests.get(reqcounter).setOpenBug(param);
		}
		else if (command.equals("INCREMENT_PATIENT_NUMBER")) {
			requests.get(reqcounter).incrementPatientNumber(param);
		}
		else if (isJSONAssign == true || command.equals("JAVASCRIPT")) {
			isJSONAssign = true;
			requests.get(reqcounter).setJSONVariable(param);
			if (requests.get(reqcounter).getAssign() == null) {
				isJSONAssign = false;
			}
		}
		else if (command.equals("POST_XML_SUBS")) {
			requests.get(reqcounter).setPostXMLSubs(param);
		}
		else if (command.equals("RELOAD_ENVIRONMENT_AGNOSTIC_PROPERTIES")) {
			requests.get(reqcounter).setEnvironmentAgnosticReload();
		}
		else if (command.equals("STATUS")) {
			requests.get(reqcounter).setStatus(param);
		}
		else if (command.equals("TESTRAIL")) {
			requests.get(reqcounter).setTestRail(param);
		}
		else if (command.equals("VALIDATE_HEADER")) {
			requests.get(reqcounter).setValidation(command,param);
		}
		else if (command.equals("VALIDATE_HEADER_SET")) {
			requests.get(reqcounter).setValidation(command,param);
		}
		else if (command.equals("VALIDATE_HEADER_NOTEXIST")) {
			requests.get(reqcounter).setValidation(command,param);
		}
		else if (command.equals("TRANSFORM_RESPONSE_FILE")) {
			requests.get(reqcounter).setXSLFile(param.trim());
		}
		else if (command.equals("SET_HEADER")) {
			requests.get(reqcounter).setHeader(param);
		}
		else if (command.equals("VALIDATE_RAW_RESPONSE")) {
			requests.get(reqcounter).setValidation(command,param);
		}
		else if (command.equals("COMPARE")) {
			requests.get(requests.size() -1 ).setCompare(true);
			if (!param.isEmpty()) {
				requests.get(requests.size() - 1).setCompareString(param);
			}
		}
		else if (command.equals("IGNORE")) {
			requests.get(requests.size() -1).setIgnore(true);
			requests.get(requests.size() -1).setIgnoreString(param);
		}

		else if (command.equals("SET_DATETIME")) {
			requests.get(reqcounter).setDateTime(param);
		}
		else if (command.equals("SQL")) {
			setSqlStrings(Integer.valueOf(this.getRequests().size() -1).toString(), param);
		}
		else if (command.equals("VALIDATE_DATA")) {
			getDataValidators().add(param);		
		}
		else if (command.equals("VALIDATE_STATUS_CODE")) {
			requests.get(reqcounter).setStatus(param);
		}
		else if (command.equals("VERIFY_RESPONSE_TEXT")) {
			requests.get(reqcounter).setResponseText(param);
			requests.get(requests.size() -1 ).setCompare(true);
			comparisonType = "text";
		}
		else if (command.equals("COMPARE_VARIABLES")) {
			requests.get(reqcounter).setComparisonVariables(param);
			requests.get(requests.size() -1).setCompare(true);
			comparisonType = "variables";
		}
		else if (command.equals("COMPARE_TWO_SQL_STATEMENTS")) {
			requests.get(requests.size() -1).setCompare(true);
			comparisonType = "sql_statements";
			if (!param.isEmpty()) {
				requests.get(requests.size() - 1).setExpectedVariable(param.split(",")[0].trim());
				requests.get(requests.size() - 1).setActualVariable(param.split(",")[1].trim());
			}
		}
		else if (command.equals("VERIFY_MAP_VALUE")) {
			requests.get(reqcounter).setMapValue(param);
			requests.get(requests.size() -1 ).setCompare(true);
			comparisonType = "mapvalue";
		}
		else if (command.equals("WAIT_FOR_DATA")) {
			requests.get(reqcounter).setWaitTime(param);
		}
		else if (command.equals("BASIC_AUTHORIZATION")) {
			requests.get(reqcounter).setBasicAuthorization();
		}
		
		if (PropertiesSingleton.Instance.getProps().getProperty("skip.compare").toLowerCase().equals("true")) {
			requests.get(reqcounter).setCompare(false);
		}
	}

	public List<ParsedRequest> getRequests() {
		return requests;
	}

	public ParsedRequest getCurrentParsedRequest() {
		return requests.get(test_request_counter);
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public void saveRequestURLs ()  {
		String requestdir = Utilities.Instance.getResponseCompareRoot() +
			File.separator + "data" + File.separator +
			"consult" + File.separator + PropertiesSingleton.Instance.getProperty("test.dir") +
				File.separator + "request" +
			File.separator;
		String requestTestFile = new File(requestfile).getName();
		StringBuffer requestr = new StringBuffer("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">" +
							"<title>Response Compare Tester</title></head><body><DL>" +

                            "<style>" +
                            "    .button {" +
                            "    display: inline-block;" +
                            "    padding: 15px 25px;" +
                            "    font-size: 24px;" +
                            "    cursor: pointer;" +
                            "    text-align: center;" +
                            "    text-decoration: none;" +
                            "    outline: none;" +
                            "    color: #fff;" +
                            "    background-color: #4CAF50;" +
                            "    border: none;" +
                            "    border-radius: 1 +5px;" +
                            "    box-shadow: 0 9px #999;" +
                            "}" +

                            ".button:hover {background-color: #3e8e41}" +

                            ".button:active {" +
                            "background-color: #3e8e41;" +
                            "box-shadow: 0 5px #666;" +
                            "transform: translateY(4px);" +
                            "}" +
                            "</style>" +
                            "<style>" +
                                ".align { text-align: center; vertical-align: text-top;" +
                                "}" +
                            "</style>" +


                            "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\"></script>" +
							"<script type=\"text/javascript\">" +
                                "function callJenkins () {" +
                                    "var form = document.createElement('form');" +
                                    "form.setAttribute('method', 'post');" +
                                    "form.setAttribute('action', 'http://sqadevws02:8080/job/Single_testcase_execution/buildWithParameters?token=single_SQL&TESTCASENAME=" + getTestCaseID() + "');" +
                                    "form.style.display = 'hidden';" +
                                    "document.body.appendChild(form);" +
                                    "form.submit();" +
                                "}" +
                            "</script>");

        try {
            File file = new File(getRequestFile());
            List<String> lines = FileUtils.readLines(file, "UTF-8");

            boolean foundline = false;
            for (String line : lines) {
                if (line.contains(getTestCaseID() + ",")) {
                    requestr.append("<DT><b>" + line + "</b>");
                    foundline = true;
                    continue;
                }

                if (foundline && !line.contains("TESTCASE")) {
                    requestr.append( "<DD>" + line + "</DD>");
                } else if (foundline) {
                    break;
                }
            }

            requestr.append( "<br><br><button id=\"myButton\" class=\"button\" onclick=\"callJenkins()\">Run</button>");
            requestr.append( "<DL></body></html>");
			FileUtils.writeStringToFile(new File(
				requestdir + testCaseID + "_" + Integer.valueOf(test_request_counter) + ".html"), requestr.toString());
			requestr.delete(0,requestr.length());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getXSL() {
		return xsl;
	}
	
	public void setXSL(String xsl) {
		this.xsl = xsl;
	}

	public boolean getIsAppend() {
	    return isAppend;
    }

    public void setIsAppend(boolean isAppend) {
	    this.isAppend = isAppend;
    }

	public String getTestCaseID() {
		return testCaseID;
	}

	public void setTestCaseID(String testCaseID) {
		this.testCaseID = testCaseID;
	}
	
	public void setStatus(int statuscode) {
		this.status = statuscode;
	}
	
	public int getStatus() {
		return this.status;
	}

	public String getTestCaseDescription() {
		return testCaseDescription;
	}

	public void setTestCaseDescription(String testCaseDescription) throws Exception {
		this.testCaseDescription =
			InterpolateRequest.Instance.interpolateString(testCaseDescription);
	}
	
	public boolean getCompare() {
		return requests.get(reqcounter).getCompare();
	}

	@Override
	public String toString() {
		String prettyPrint = "Line#: " + lineNum + " TESTCASE: " + testCaseID + ", " + testCaseDescription + "\n";
		for(ParsedRequest req : requests) {
			prettyPrint += req.toString() + "\n";
		}
		return prettyPrint;
	}

	public String getComparisonType() {
		return comparisonType;
	}

	public void setComparisonType() {
		String lastRequest = requests.get(requests.size() - 1).getRequestType();
		if (lastRequest.toLowerCase().contains("image")) {
			comparisonType = "image";
		} else if (lastRequest.toLowerCase().equals("run_sqlserver_execute_sql")
			&& comparisonType.isEmpty()) {
			comparisonType = "sql_statements";
		} else if (lastRequest.toLowerCase().equals("run_oracleserver_execute_sql")
			&& comparisonType.isEmpty()) {
			comparisonType = "sql_statements";
        } else if (lastRequest.toLowerCase().contains("sql")
				&& comparisonType.isEmpty()) {
            comparisonType = "sql";
		} else if (comparisonType.isEmpty()) {
            comparisonType = "xml";
        }
	}
	
	public void setRequestFile(String requestfile) {
		this.requestfile = requestfile; 
	}
	
	public String getRequestFile() {
		return requestfile;
	}
	
	public String getRequestFileName() {
		File file = new File(requestfile);
		return file.getName();
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public int getNumImagesSaved() {
		return numImagesSaved;
	}

	public void setNumImagesSaved(int numImagesSaved) {
		this.numImagesSaved = numImagesSaved;
	}
	
	public void setTestCaseNumber(int number) {
		testcasenumber = number;
	}
	public int getTestCaseNumber() {
		return testcasenumber;
	}

	public HashMap<String,String> getSqlStrings() {
		return sqlStrings;
	}

	public void setSqlStrings(String key, String value) {
		this.sqlStrings.put(key, value);
	}


	public List<String> getDataValidators() {
        return dataValidators;
    }

	public void setDataValidators(List<String> dataValidators) {
		this.dataValidators = dataValidators;
	}

	public boolean isWapi() {
		return isWapi;
	}
	
	private String makeHeaderString(HashMap<String, String> headers) {
		String headerstring = "";
		for (Map.Entry<String, String> header : headers.entrySet()) {
			headerstring = headerstring + header.getKey() + ":" +
			header.getValue() + String.valueOf("\n");
		}
		return headerstring; 
	}

	private void addParsedRequest(String command, String param) throws Exception {
		reqcounter++;
		requests.add(new ParsedRequest(command,param));
		if (isIF) {
			requests.get(requests.size() - 1).setIsIF(true);
			requests.get(requests.size() -1 ).setConditional(ifCondition);
		}
		if (isELSE) {
			requests.get(requests.size() - 1).setIsELSE(true);
			requests.get(requests.size() -1 ).setConditional(ifCondition);
		}

		if (command.matches("RUN_.*_SQL.*")) {
		    isSQL = true;
		    isAppend = true;
        } else {
		    isSQL = false;
        }
	}
}
