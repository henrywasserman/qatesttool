package com.pqi.responsecompare.compare;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.request.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;

public class TextCompare extends Compare {
	private Diff diff = null;
	private String responsejson = "";
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

	public TextCompare(TestCase test) {
		super(test);
	}

	public void testMyTest() {

	}

	public TextCompare(String testMethodName) {
		super(testMethodName);
	}

	public TextCompare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}
	
	public void results() throws Exception {

		try {

			responseFile = responseFile + ".json";

			//Make sure that all of the files we are working with exist
			Utilities.Instance.fileChecker(responseFile);

			responsejson = FileUtils
					.readFileToString(new File(responseFile));
			

			if (PropertiesSingleton.Instance.getProps().getProperty("only.create.transformations").trim().toLowerCase().equals("false")) {

				boolean result = false;

				StringBuffer results = new StringBuffer();

				ArrayList<String> stringArray = test.getCurrentParsedRequest().getResponseText();

				for (String response:stringArray)
				{

					result = StringUtils
						.contains(responsejson, response);
					results.append(responseFile);
					results.append(
						"  Could not find " + response + " in " + responsejson);

					if (results.length() > 512)
					{
						assertTrue(results.substring(0, 512)
							+ "...error messages truncated", result);
					}
					else
					{
						assertTrue(results.toString(), result);
					}

					results = new StringBuffer();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while comparing response with the expected result" + e.getMessage() ,e);
		}
	}
}
