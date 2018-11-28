package com.pqi.responsecompare.compare;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.request.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

import java.io.File;

public class VariableCompare extends Compare {
	private Diff diff = null;
	private String responsexml = "";
	private String responseresult = "";
	private Document responsedoc = null;
	private String searchString = "";

	static class TestData {
		Document response = null;
		Document expectedresponse = null;

		public TestData(Document expectedresponse,
				Document response) {
			this.expectedresponse = expectedresponse;
			this.response = response;
		}

		public Document getResponse() {
			return response;
		}

		public Document getResponseGold() {
			return expectedresponse;
		}
	}

	public VariableCompare(TestCase test) {
		super(test);
	}

	public void testMyTest() {

	}

	public VariableCompare(String testMethodName) {
		super(testMethodName);
	}

	public VariableCompare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}
	
	public void results() throws Exception {

		try {

			responseFile = responseFile + ".xml";

			//Make sure that all of the files we are working with exist
			Utilities.Instance.fileChecker(responseFile);

			responsexml = FileUtils
					.readFileToString(new File(responseFile));
			


			
			if (PropertiesSingleton.Instance.getProps().getProperty("only.create.transformations").trim().toLowerCase().equals("false")) {

				boolean result = false;

				StringBuffer results = new StringBuffer();

				String actualResults = test.getCurrentParsedRequest().getActualVariable();
				String expectedResults = test.getCurrentParsedRequest().getExpectedVariable();

				String actualResultsValue = JSONToMap.Instance.getMap().get(actualResults).toString();
				String expectedResultsValue = JSONToMap.Instance.getMap().get(expectedResults).toString();

				result = StringUtils.equals(actualResultsValue,expectedResultsValue);
				results.append(responseFile);
				results.append("  Actual Results " + actualResultsValue + " did not equal Expected Results " + expectedResultsValue);

				if (results.length() > 512) {
					assertTrue(results.substring(0, 512)
							+ "...error messages truncated", result);
				} else {
					assertTrue(results.toString(), result);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while comparing response with the expected result" + e.getMessage() ,e);
		}
	}
}
