package com.pqi.responsecompare.compare;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.request.TestCase;
import org.apache.maven.surefire.shade.org.apache.maven.shared.utils.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;

import java.util.ArrayList;

public class MapValueCompare extends Compare {
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

	public MapValueCompare(TestCase test) {
		super(test);
	}

	public void testMyTest() {

	}

	public MapValueCompare(String testMethodName) {
		super(testMethodName);
	}

	public MapValueCompare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}
	
	public void results() throws Exception {

		try
		{

			responseFile = responseFile + ".xml";

			if (PropertiesSingleton.Instance.getProps().getProperty("only.create.transformations").trim().toLowerCase()
				.equals("false"))
			{

				boolean result = false;

				StringBuffer results = new StringBuffer();

				ArrayList<String> mapValues = test.getCurrentParsedRequest().getMapValue();

				for (String testString : mapValues)
				{
					String[] map = StringUtils
						.split(testString, ",");

					String key = map[0];
					String value = map[1].trim();
					String actualValue = "";

					if (JSONToMap.Instance.getMap().get(key) != null)
					{
						actualValue = JSONToMap.Instance.getMap().get(key).toString();
						result = actualValue.equals(value);
					}
					else
					{
						result = false;
						actualValue = key + " not found in map, so it ";
					}

					results.append(responseFile);
					results.append("\n" + key + " value: " + actualValue + " did not equal " + value);

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

		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error while comparing response with the expected result" + e.getMessage(), e);
		}
	}
}
