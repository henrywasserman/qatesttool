package com.pqi.responsecompare.request;

import com.pqi.responsecompare.compare.CompareResults;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.sql.SQLToMap;
import org.junit.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class TestMakeRequest extends junit.framework.TestCase {
	static Logger logger = Logger.getLogger(MakeRequests.class);
	
	private Request req = null;
	private String results = "";
	private static StringBuffer datadir = null;
	private static StringBuffer requestFile = null;
	private static ArrayList<TestCase> testcaseList = null;
	private int testNumber = 0;
	
	public TestMakeRequest(String testMethodName, int testNumber)
			throws Exception {

		super("MakeRequest");
		
		this.testNumber = testNumber;
	}

	public static void main(String args[]) {
		TestRunner.run(suite());
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		String testname = "";
        String responseCompareRoot = Utilities.Instance.getResponseCompareRoot();
        logger.info("Here is responseCompareRoot: " + responseCompareRoot);
        datadir = new StringBuffer(responseCompareRoot + 
				File.separator + "data");

		testcaseList = new ArrayList<TestCase>();
		requestFile = new StringBuffer(datadir.toString());
		
		requestFile.append(File.separator + "consult");
		
		if (System.getProperty("test.dir") == null ) {
			logger.info("test.dir is null");
		} else if (!System.getProperty("test.dir").isEmpty()) {
			//requestFile.append(File.separator + System.getProperty("test.dir"));
		}

		logger.info("Here is requestfile: " + requestFile);
		
		String[] extension = {"req"};
		LinkedList<File> responsecomparefiles = 
			(LinkedList<File>)FileUtils.listFiles(
					new File(requestFile.toString()), extension, false);
      
		
        ScriptParser scriptParser = new ScriptParser(responsecomparefiles);
		try {
			testcaseList = scriptParser.parse();
		}
		catch (Exception e ) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < testcaseList.size(); i++) {
			testname = testcaseList.get(i).getTestCaseID().toLowerCase();
			try {
				if (System.getProperty("tc") != null 
						&& !System.getProperty("tc").toLowerCase().equals("none")) {
					if (testname.equals(
						System.getProperty("tc"))) {
						suite.addTest((Test) new MakeRequests("MakeRequest",i));
						  System.out.println("ResponseCompare file path: "+testcaseList.get(i).getRequestFile());
						break;
					}
				}
				else if (System.getProperty("filelist") != null
						&& !System.getProperty("filelist").toLowerCase().equals("none")) {
					if (System.getProperty("filelist").toLowerCase().contains(
							testcaseList.get(i).getRequestFileName().toLowerCase())) {
						suite.addTest((Test) new MakeRequests("MakeRequest", i));
					}
				}
				else if (System.getProperty("excludefilelist") !=null
						&& !System.getProperty("excludefilelist").toLowerCase().equals("none")) {
					if (!System.getProperty("excludefilelist").toLowerCase().contains(
							testcaseList.get(i).getRequestFileName().toLowerCase())) {
						suite.addTest((Test) new MakeRequests("MakeRequest",i));
					}
				}
				else {
					suite.addTest((Test) new TestMakeRequest("MakeRequest", i));
				}
			} 
			catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}
		return suite;
	}

	public void setUp() throws Exception {
	}
	
	public void MakeRequest() throws Exception {
		for (ParsedRequest request : testcaseList.get(testNumber).getRequests()) {
			try {
				testcaseList.get(testNumber).incrementTestRequestCounter();
				req = RequestFactory.Instance.getRequest(testcaseList.get(testNumber));
				this.setName(testcaseList.get(testNumber).getTestCaseID() + ": \n" +
						testcaseList.get(testNumber).getTestCaseDescription());

				req.sendRequest();
				req.getTest().setComparisonType();

				if (!req.getTest().getCompare()) {
					new CompareResults(req.getTest());
				}

			} catch (AssertionFailedError aex) {
				// process all the junit assertions here;
				req.getTest().saveRequestURLs();
				logger.error(aex.getMessage());
				Assert.fail(req.getPathGenerator().getResponseFile() + ".xml" + "***" + aex.getMessage());
			} catch (java.lang.Exception e) {
				req.getTest().saveRequestURLs();
				results = req.getPathGenerator().getResponseFile() +
						".xml" + "***" + "TestID: " + req.getTest().getTestCaseID() + " failed: " +
						"\n " + e.getMessage() + "\n " + StringUtils.join(e.getStackTrace()).substring(0, 512);
				logger.info("TestID: " + req.getTest().getTestCaseID() + " failed: " + StringUtils.join(
						e.getStackTrace()).substring(0, 512));
				e.printStackTrace();
				Assert.fail(results);

			}
		}
		SQLToMap.Instance.cleanSQLMap();
		SQLToMap.Instance.cleanSQLMapArray();
	}
}
