package com.pqi.responsecompare.compare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.reports.PatchToHTML;
import com.pqi.responsecompare.request.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class XmlCompare extends Compare {

	private Diff diff = null;
	private String responseTransformed = "";
	private String expectedTransformed = "";
	private String jsonResponse = "";
	private String jsonExpected = "";
	private String responsexml = "";
	private String responseresult = "";
	private String patchResponse = "";
	private Document responsedoc = null;
	private Document expectedresponsedoc = null;
	private DocumentBuilderFactory dbFactory = null;
	private DocumentBuilder dBuilder = null;
	

	static class TestData {
		org.w3c.dom.Document response = null;
		org.w3c.dom.Document expectedresponse = null;

		public TestData(org.w3c.dom.Document expectedresponse,
				org.w3c.dom.Document response) {
			this.expectedresponse = expectedresponse;
			this.response = response;
		}

		public org.w3c.dom.Document getResponse() {
			return response;
		}

		public org.w3c.dom.Document getResponseGold() {
			return expectedresponse;
		}
	}

	public XmlCompare(TestCase test) {
		super(test);
		
	}
	
	public void testMyTest() {
		
	}
	
	public XmlCompare(String testMethodName) {
		super(testMethodName);
	}

	public XmlCompare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}
	
	public void results() throws Exception {
		
		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
		XMLUnit.setTransformerFactory("org.apache.xalan.processor.TransformerFactoryImpl");
		
		try {

			File xsl = null;
			
			responseTransformed =  pathGenerator.getResponseTransformed()
				+ ".xml";

			jsonResponse = pathGenerator.getResponseFile()
					+ ".json";

			patchResponse = pathGenerator.getResponseFile() + "_diff.html";

			expectedTransformed = pathGenerator.getExpectedTransformed()
				+ ".xml";

			jsonExpected = pathGenerator.getExpected()
					+ "_" + test.getTestRequestCounter() + ".json";

			responseFile = responseFile + ".xml";

			//Make sure that all of the files we are working with exist
			Utilities.Instance.fileChecker(responseTransformed);
			Utilities.Instance.fileChecker(expectedTransformed);
			Utilities.Instance.fileChecker(responseFile);

			if (test.getCurrentParsedRequest().getXSLFile().isEmpty()) {
				xsl = new File(pathGenerator.getXslDir() + "response.xsl");
			}
			else {
				xsl = new File(pathGenerator.getXslDir() + test.getCurrentParsedRequest().getXSLFile());
			}

			responsexml = FileUtils
					.readFileToString(new File(responseFile));


			//Special case for when patients are imported not found.
			if (PropertiesSingleton.Instance.getProperty("empi").equals("true")) {
				responsexml = StringUtils.removePattern(responsexml, "\\<Patient\\s+\\[id.*\\]\\>");
			}

			responsexml = responsexml.substring(responsexml.indexOf("<"));
			
			responseresult = new Transform(responsexml, xsl)
					.getResultString();
			
			try {
				FileUtils.writeStringToFile(new File(responseTransformed),
						responseresult);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (PropertiesSingleton.Instance.getProps().getProperty("only.create.transformations").trim().toLowerCase().equals("false")) {
			
				TestData td = null;

				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
			
				responsedoc = dBuilder.parse(responseTransformed);
			
				boolean result = false;
			
				StringBuffer results = new StringBuffer();
			
				if (new File(expectedTransformed).exists() &&  new File(expectedTransformed).length() == 0) {
					assertTrue("Expected Response File is Missing: " + expectedTransformed,false);
				}

				else {
					expectedresponsedoc = dBuilder.parse(expectedTransformed);
			
					td = new TestData(expectedresponsedoc,responsedoc);

					diff = new DetailedDiff(compareXML(td.getResponseGold(),
					td.getResponse()));

					result = diff.similar();
					results.append(responseFile);

					if (result == false && new File(jsonResponse).exists()) {
						String filepath = jsonResponse;
						String expectedJson = FileUtils.readFileToString(new File(jsonExpected));
						String responseJson = FileUtils.readFileToString(new File(jsonResponse));
						ObjectMapper mapper = new ObjectMapper();
						JsonNode actualExpectedJSONResponseNode = mapper.readTree(expectedJson);
						JsonNode actualJSONResponseNode = mapper.readTree(responseJson);
						JsonNode patch = JsonDiff.asJson(actualJSONResponseNode,actualExpectedJSONResponseNode);

						FileUtils.writeStringToFile(new File(patchResponse),
								PatchToHTML.Instance.createPatchHTMLForJSON(patch,
										actualJSONResponseNode));
					}


					diff.appendMessage(results);
					if (results.length() > 512) {
						assertTrue(results.substring(0, 512)
								+ "...error messages truncated", result);
					} else {
						assertFalse("File: " + results.toString() + " is zero bytes long.", new File(responseTransformed).length() == 0);
						assertFalse("File: " + results.toString() + " is zero bytes long.", new File(expectedTransformed).length() == 0);
						assertTrue(results.toString(), result);
					}
				}
			}


		} catch (SAXParseException e) {
			throw new Exception("Error while parsing the response",e);
	
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while comparing response with the expected result" + e.getMessage() ,e);
		}
	}
}
