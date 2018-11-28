package com.pqi.responsecompare.compare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.pqi.responsecompare.reports.CreateOutput;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.reports.PatchToHTML;
import com.pqi.responsecompare.request.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.skyscreamer.jsonassert.*;


import java.io.File;

import static org.skyscreamer.jsonassert.JSONParser.parseJSON;

public class SQLStatementsCompare extends Compare {
    static Logger logger = Logger.getLogger(SQLStatementsCompare.class);

    private String actualJSONResponse = "";
    private String actualHTMLResponse = "";
    private String actualExpectedJSONResponse = "";

    private String expectedJSONFileString = "";
    private String actualExpectedJSONFileString = "";
    private String actualJSONFileString = "";
    private String expectedHTMLFileString = "";
    private String actualHTMLFileString = "";
    private String differenceHTMLFileString = "";
    private String actualExpectedHTMLFileString = "";
    private String realExpectedHTMLFileString = "";
    private String realExpectedJSONFileString = "";

    public SQLStatementsCompare(TestCase test) {
        super(test);

    }

    public SQLStatementsCompare(String testMethodName) {
        super(testMethodName);
    }

    public SQLStatementsCompare(String testMethodName, String fileName) {
        super(testMethodName);
        this.fileName = fileName;
    }

    public void results() throws Exception {


        String actualName = test.getCurrentParsedRequest().getActualVariable();
        String expectedName = test.getCurrentParsedRequest().getExpectedVariable();
        String currentTestCounter = Integer.toString(test.getTestRequestCounter());
        String previousTestCounter = Integer.toString(test.getTestRequestCounter() - 1);

        if (previousTestCounter.equals("-1")) {
            previousTestCounter = "";
            actualExpectedJSONFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_0.json";

        }

        if (actualName.isEmpty()) {

            if (actualExpectedJSONFileString.isEmpty()) {
                actualExpectedJSONFileString = pathGenerator.getResponseDir()
                        + test.getTestCaseID() + "_" + previousTestCounter + ".json";
            }

            expectedJSONFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + previousTestCounter + ".json";

            actualJSONFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + ".json";

            expectedHTMLFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + previousTestCounter + ".html";

            actualExpectedHTMLFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + ".html";

            actualHTMLFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + ".html";

            differenceHTMLFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + "_diff.html";

            realExpectedHTMLFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + ".html";

            realExpectedJSONFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + currentTestCounter + ".json";

        } else {
            expectedJSONFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + expectedName + ".json";

            actualExpectedJSONFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + expectedName + ".json";


            actualJSONFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + actualName + ".json";

            expectedHTMLFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + expectedName + ".html";

            actualHTMLFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + actualName + ".html";

            differenceHTMLFileString = pathGenerator.getResponseDir()
                    + test.getTestCaseID() + "_" + expectedName + "_diff.html";

            actualExpectedHTMLFileString = pathGenerator.getExpectedDir()
                    + test.getTestCaseID() + "_" + actualName + ".html";

            realExpectedHTMLFileString = actualExpectedHTMLFileString;

            realExpectedJSONFileString = actualExpectedJSONFileString;
        }

        File expectedJSONResponseFile = new File(expectedJSONFileString);
        File actualJSONResponseFile = new File(actualJSONFileString);
        File expectedHTMLResponseFile = new File(expectedHTMLFileString);
        File actualHTMLResponseFile = new File(actualHTMLFileString);
        File differenceHTMLFile = new File(differenceHTMLFileString);
        File actualExpectedJSONResponseFile = new File(actualExpectedJSONFileString);
        File actualExpectedHTMLResponseFile = new File(actualExpectedHTMLFileString);
        File realExpectedHTMLResponseFile = new File(realExpectedHTMLFileString);
        File realExpectedJSONResponseFile = new File(realExpectedJSONFileString);

        //Make sure that all of the files we are working with exist
        Utilities.Instance.fileChecker(expectedJSONFileString);
        Utilities.Instance.fileChecker(actualJSONFileString);
        Utilities.Instance.fileChecker(expectedHTMLFileString);
        Utilities.Instance.fileChecker(actualHTMLFileString);
        Utilities.Instance.fileChecker(actualExpectedJSONFileString);
        Utilities.Instance.fileChecker(actualExpectedHTMLFileString);
        Utilities.Instance.fileChecker(realExpectedHTMLFileString);
        Utilities.Instance.fileChecker(realExpectedJSONFileString);


        actualJSONResponse = FileUtils.readFileToString(actualJSONResponseFile);

        if (actualJSONResponse.getBytes().length > 150000) {
            test.getCurrentParsedRequest().setResponseTooLargeForCompareLink(true);
        }

        actualHTMLResponse = FileUtils.readFileToString(actualHTMLResponseFile);

        if (!actualName.isEmpty() ||
                previousTestCounter.isEmpty() ||
                (Integer.valueOf(currentTestCounter) - Integer.valueOf(previousTestCounter)) == 1) {
            actualExpectedJSONResponse = FileUtils.readFileToString(actualExpectedJSONResponseFile);
        }

        String expectedHTMLResponse = FileUtils.readFileToString(expectedHTMLResponseFile);
        String expectedJSONResponse = FileUtils.readFileToString(expectedJSONResponseFile);
        String actualExpectedHTMLResponse = FileUtils.readFileToString(actualExpectedHTMLResponseFile);
        String realExpectedHTMLResponse = FileUtils.readFileToString(realExpectedHTMLResponseFile);
        String realExpectedJSONResponse = FileUtils.readFileToString(realExpectedJSONResponseFile);

        FileUtils.writeStringToFile(expectedJSONResponseFile, actualJSONResponse);
        FileUtils.writeStringToFile(actualExpectedJSONResponseFile, actualExpectedJSONResponse);
        FileUtils.writeStringToFile(expectedHTMLResponseFile, actualHTMLResponse);

        String html = "";
        if (actualExpectedJSONResponseFile.length() == 0) {
            html = CreateOutput.Instance.returnEmptyJsonStringHTML();
        } else {
            html = CreateOutput.Instance.JSONToHTML(actualExpectedJSONResponse,test);
        }
        FileUtils.writeStringToFile(realExpectedHTMLResponseFile, html);
        FileUtils.writeStringToFile(realExpectedJSONResponseFile, actualExpectedJSONResponse);

        if (actualExpectedJSONResponse.isEmpty()) {
            FileUtils.writeStringToFile(
                    differenceHTMLFile,
                    CreateOutput.Instance.returnEmptyJsonStringHTML()
            );
            Assert.fail("Expected json data has not been created yet.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualExpectedJSONResponseNode = mapper.readTree(actualExpectedJSONResponse);
        JsonNode actualJSONResponseNode = mapper.readTree(actualJSONResponse);
        JsonNode patch = JsonDiff.asJson(actualJSONResponseNode,actualExpectedJSONResponseNode);

        FileUtils.writeStringToFile(differenceHTMLFile,
            PatchToHTML.Instance.createPatchHTML(patch,
                    actualJSONResponseNode));


        JSONAssert.assertEquals(actualExpectedJSONResponse, actualJSONResponse, false);

    }
}