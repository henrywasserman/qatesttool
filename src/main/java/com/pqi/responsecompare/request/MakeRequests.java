package com.pqi.responsecompare.request;

import com.pqi.responsecompare.compare.CompareResults;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.reports.JunitReport;
import com.pqi.responsecompare.reports.XmlWritingListener;
import com.pqi.responsecompare.splunk.SplunkManager;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class MakeRequests extends junit.framework.TestCase {

	static final Logger logger = Logger.getLogger(MakeRequests.class);
	private static boolean runonce = false;
	private Request req = null;
	private static ArrayList<TestCase> testcaseList = null;
	private int testNumber;
	private boolean testresult = true;
	private boolean testrailUpdated = false;

	public MakeRequests(String testMethodName, int testNumber) throws Exception {

		super("MakeRequest");
		this.testNumber = testNumber;
	}

	public static void main(String args[]) throws Exception {
		PropertyConfigurator.configure("properties/log4j.properties");
		JUnitCore runner = new JUnitCore();
		File reportDirectory = new File("reports");
		XmlWritingListener writingListener = new XmlWritingListener(reportDirectory);
		runner.addListener(new TextListener(System.out));
		runner.addListener(writingListener);
		writingListener.startFile(suite().getClass());
		runner.run(suite());
		writingListener.closeFile();
		JunitReport.Instance.createReports();
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		String testname;
		String responseCompareRoot = Utilities.Instance.getResponseCompareRoot();
		logger.info("Here is responseCompareRoot: " + responseCompareRoot);
		StringBuffer datadir = new StringBuffer(responseCompareRoot + File.separator + "data");

		testcaseList = new ArrayList<TestCase>();
		StringBuffer requestFile = new StringBuffer(datadir.toString());

		requestFile.append(File.separator + "consult");

		if (System.getProperty("test.dir") == null) {
			logger.info("test.dir is null");
		} //else if (!System.getProperty("test.dir").isEmpty()) {
			// requestFile.append(File.separator +
			// System.getProperty("test.dir"));
		//}

		logger.info("Here is requestfile: " + requestFile);

		String[] extension = { "req" };
		LinkedList<File> responsecomparefiles = (LinkedList<File>) FileUtils.listFiles(new File(requestFile.toString()),
				extension, false);

		if (runonce)  {
			ScriptParser scriptParser = new ScriptParser(responsecomparefiles);
			try {
				testcaseList = scriptParser.parse();
				runonce = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			runonce = true;
		}

		if (!PropertiesSingleton.Instance.getProperty("testcase").isEmpty())
			System.setProperty("tc", PropertiesSingleton.Instance.getProperty("testcase"));


		if (!PropertiesSingleton.Instance.getProperty("filelist").isEmpty()) {
			System.setProperty("filelist", PropertiesSingleton.Instance.getProperty("filelist"));
		}

		for (int i = 0; i < testcaseList.size(); i++) {
			testname = testcaseList.get(i).getTestCaseID();
			if (PropertiesSingleton.Instance.getProperty("user.hasmail").toLowerCase().equals("false")) {
				if (testname.toLowerCase().contains("inbox")
					|| testname.toLowerCase().contains("messaging")
					|| testname.toLowerCase().contains("mail"))
				{
					continue;
				}
			}
			try {
				if (System.getProperty("tc") != null && System.getProperty("filelist") != null) {

					if (System.getProperty("filelist").toLowerCase()
							.contains(testcaseList.get(i).getRequestFileName().toLowerCase())
							&& testname.equals(System.getProperty("tc")))
						suite.addTest((Test) new MakeRequests("MakeRequest", i));
				} else if (System.getProperty("tc") != null && !System.getProperty("tc").toLowerCase().equals("none")) {
					if (testname.equals(System.getProperty("tc"))) {
						suite.addTest((Test) new MakeRequests("MakeRequest", i));
						System.out.println("ResponseCompare file path: " + testcaseList.get(i).getRequestFile());
						break;
					}
				} else if (System.getProperty("filelist") != null
						&& !System.getProperty("filelist").toLowerCase().equals("none")) {
					if (System.getProperty("filelist").toLowerCase()
							.equals(testcaseList.get(i).getRequestFileName().toLowerCase())) {
						suite.addTest((Test) new MakeRequests("MakeRequest", i));
					}
				} else if (System.getProperty("excludefilelist") != null
						&& !System.getProperty("excludefilelist").toLowerCase().equals("none")) {
					if (!System.getProperty("excludefilelist").toLowerCase()
							.contains(testcaseList.get(i).getRequestFileName().toLowerCase())) {
						suite.addTest((Test) new MakeRequests("MakeRequest", i));
					}
				} else {
					suite.addTest((Test) new MakeRequests("MakeRequest", i));
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}
		return suite;
	}

	public void setUp() throws Exception {
	}

	public void MakeRequest() throws Exception {
		Date date = new Date();
		String dateTime = DateFormatUtils.format(new Date(), "hh:mm:ss a");
		String testRunName = PropertiesSingleton.Instance.getProperty("testrail-testrun-name") + " " + dateTime;
		JSONToMap.Instance.put("testrail-testrun-name", testRunName);
		ParsedRequest pr;
		for (ParsedRequest request : testcaseList.get(testNumber).getRequests()) {
			try {
				testcaseList.get(testNumber).incrementTestRequestCounter();
				req = RequestFactory.Instance.getRequest(testcaseList.get(testNumber));

				this.setName(testcaseList.get(testNumber).getTestCaseID() + ": \n"
					+ testcaseList.get(testNumber).getTestCaseDescription());
				SplunkManager.Instance.getLastEventTime();
				pr = req.getTest().getRequests().get(req.getTest().getTestRequestCounter());
				pr.testConditional();
				pr.reloadEnvironmentAgnosticProperties();
				if (pr.getRunRequest())
				{
					req.sendRequest();
					req.getTest().setComparisonType();
					if (pr.getCompare())
					{
						new CompareResults(req.getTest());
					}
					Assert.assertFalse("Test contains an Open Bug: " + pr.getOpenBugUrl(), pr.getOpenBug());
				}

			} catch (AssertionFailedError aex) {
				// process all the junit assertions here;
				req.getTest().saveRequestURLs();
				logger.error(aex.getMessage());
				testresult=false;
				updateTestrailRun();
				Assert.fail(req.getPathGenerator().getResponseFile() + ".xml" + "***" + aex.getMessage());
			} catch (java.lang.AssertionError ae) {
				req.getTest().saveRequestURLs();
				logger.error(ae.getMessage());
				testresult=false;
				updateTestrailRun();
				Assert.fail(req.getPathGenerator().getResponseFile() + ".xml" + "***" + ae.getMessage());
			} catch (java.lang.Exception e) {
				e.printStackTrace();
				req.getTest().saveRequestURLs();
				String results = req.getPathGenerator().getResponseFile() + ".xml" + "***" + "TestID: "
						+ req.getTest().getTestCaseID() + " failed: " + "\n " + e.getMessage() + "\n "
						+ StringUtils.join(e.getStackTrace()).substring(0, 1024);
				logger.info("TestID: " + req.getTest().getTestCaseID() + " failed: "
						+ StringUtils.join(e.getStackTrace()).substring(0, 1024));
				e.printStackTrace();
				testresult=false;
				updateTestrailRun();
				Assert.fail(results);
			} finally {
				if (PropertiesSingleton.Instance.getProperty("call-testrail").toLowerCase().equals("true")) {
					String testcaseName = req.getTest().getTestCaseID();
					if (testcaseName.equals("setup-testrail-run") || testcaseName.equals("create-agnostic-properties")) {
						continue;
					}
					TestCase test = req.getTest();
					int requestcountersize = test.getRequests().size();
					int requestcounter = req.getTest().getTestRequestCounter();

					if (requestcounter == requestcountersize - 1 && testrailUpdated == false) {
						updateTestrailRun();
					}
				}
				testrailUpdated = false;
			}
		}
	}

	private void updateTestrailRun() throws Exception {
		if (PropertiesSingleton.Instance.getProperty("call-testrail").toLowerCase().equals("true")) {
			TestCase test = req.getTest();
			String testcaseName = req.getTest().getTestCaseID();
			String testcaseNumber;
			if (JSONToMap.Instance.getTestRailDescriptions().get(testcaseName) != null) {
				testcaseNumber = JSONToMap.Instance.getTestRailDescriptions().get(testcaseName).toString();
			}
			else {
				return;
			}
			String testrail_protocall = PropertiesSingleton.Instance.getProperty("testrail-protocol");
			String testrail_host = PropertiesSingleton.Instance.getProperty("testrail-host");
			String testrailusername = PropertiesSingleton.Instance.getProperty("testrail-username");
			String testrailpassword = PropertiesSingleton.Instance.getProperty("testrail-password");
			String status;
			String commandBody = "";
			String mpi;
			String emptyVariable = PropertiesSingleton.Instance.getProperty("emptyVariable");
			if (JSONToMap.Instance.getMap().get("static_mpi") != null) {
				mpi = JSONToMap.Instance.getMap().get("static_mpi").toString();
			} else {
				mpi = "NA";
			}


			try {
				status = JSONToMap.Instance.getMap().get("static_status").toString();
			}
			catch (Exception e) {
				status = "static_status not found";
			}

			test.resetTestList();
			test.addCommand("POST", "/index.php?/api/v2/add_result/" + testcaseNumber);

			/*
				statuses[1] = {"id":1,"name":"passed","system_name":"passed","label":"Passed","color_dark":"65bb63","color_medium":"95d96d","color_bright":"c1edc1","color_gradient_a":"559e54","color_gradient_b":"468245"};
				statuses[2] = {"id":2,"name":"blocked","system_name":"blocked","label":"Blocked","color_dark":"b85306","color_medium":"fa720a","color_bright":"f7c39c","color_gradient_a":"9c4605","color_gradient_b":"803a04"};
				statuses[3] = {"id":3,"name":"untested","system_name":"untested","label":"Untested","color_dark":"b0b0b0","color_medium":"eaeaea","color_bright":"f0f0f0","color_gradient_a":"959595","color_gradient_b":"7b7b7b"};
				statuses[4] = {"id":4,"name":"retest","system_name":"retest","label":"Retest","color_dark":"c6c634","color_medium":"edee80","color_bright":"fafab6","color_gradient_a":"a8a82c","color_gradient_b":"8a8a24"};
				statuses[5] = {"id":5,"name":"failed","system_name":"failed","label":"Failed","color_dark":"d97373","color_medium":"f1888f","color_bright":"fdc7c7","color_gradient_a":"b86161","color_gradient_b":"975050"};
				statuses[6] = {"id":6,"name":"custom_ignore","system_name":"custom_status1","label":"Ignore","color_dark":"000000","color_medium":"a0a0a0","color_bright":"d0d0d0","color_gradient_a":"404040","color_gradient_b":"101010"};
				statuses[7] = {"id":7,"name":"custom_question","system_name":"custom_status2","label":"Question","color_dark":"66089c","color_medium":"9f0af5","color_bright":"dba5fa","color_gradient_a":"560684","color_gradient_b":"47056d"};
			*/

			if (status.equals("static_status not found")) {
				if (testresult) {
				commandBody = "{\"status_id\": 1, \"comment\" : \"mpi used: " + mpi + "\"}";

				} else if (!emptyVariable.isEmpty()) {
					commandBody = "{\"status_id\": 2, \"comment\" : \"this agnostic variable was empty: " + emptyVariable + "\"}";

				} else if (mpi.isEmpty()) {
					commandBody = "{\"status_id\": 2, \"comment\" : \"mpi was empty - check environmentagnostic.properites\"}";
				} else {
					commandBody = "{\"status_id\": 5, \"comment\" : \"mpi used: " + mpi + "\"}";
				}
			}

			if (!status.equals("static_status not found")) {
				if (testresult)
				{
					commandBody = "{\"status_id\": 1, \"comment\" : \"mpi used: " + mpi + "\"}";
				} else if (PropertiesSingleton.Instance.getProps().keySet().contains(mpi)) {
					commandBody = "{\"status_id\": 2, \"comment\" : \"environment agnostic sql could not find: " + mpi + "\"}";
				} else if (mpi.isEmpty()) {
					commandBody = "{\"status_id\": 2, \"comment\" : \"mpi was empty - check environmentagnostic.properites\"}";
				} else if (status.toLowerCase().equals("blocked")) {
						commandBody = "{\"status_id\": 2, \"comment\" : \"mpi used: " + mpi + "\"}";
				} else {
						commandBody = "{\"status_id\": 5}";
				}
			}

			test.addCommand("BODY", commandBody);
			test.setIsBody(false);
			test.addCommand("TESTRAIL", testrailusername + ":" + testrailpassword);
			req = RequestFactory.Instance.getRequest(testcaseList.get(testNumber));
			this.setName(testcaseName);
			req.sendRequest();
			testresult = true;
			testrailUpdated = true;
			PropertiesSingleton.Instance.setProperty("emptyVariable","");
		}
	}
}