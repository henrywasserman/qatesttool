package com.pqi.testng;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.sql.SQLToMap;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.pqi.responsecompare.sql.OracleDbManager;

import org.skyscreamer.jsonassert.JSONAssert;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import org.apache.log4j.Logger;

public class TestExample {
    static final Logger logger = Logger.getLogger(TestExample.class);
    protected Properties props = null;
    protected String dataDir = "";
    protected String slash = "";
    protected String sql = "";
    protected String actual = "";
    protected String expected = "";
    protected String testName = "";

    @BeforeClass
    public void setUp() throws Exception {
        slash = System.getProperty("file.separator");
        dataDir = System.getProperty("user.dir") + slash +"data" + slash + "consult" + slash;
        props = Utilities.Instance.getTestProperties();
    }

    @BeforeTest
    public void setup () throws Exception {
        sql = "SELECT * FROM SC_BASE.NAME_ADDRESS_MEDICARE WHERE CITY = 'Lynn'";
    }

    @BeforeMethod
    public void handleTestMethodName(Method method)
    {
        testName = method.getName();
    }

    @Test
    public void runSQL() {
        OracleDbManager dbManager = null;
        Timestamp ts = null;
        try {
            dbManager = new OracleDbManager(props);

            // record the start time
            ts = dbManager.getDatabaseTimeStamp();
            executeAndValidateSql(ts, dbManager);
        } catch (Exception ex) {
            logger.error("Test Error ", ex);
            Assert.fail("Test Error " + ex.getMessage());
        }
        dbManager.closeConnection();
    }

    protected void executeAndValidateSql(Timestamp ts, OracleDbManager dbManager) throws SQLException, Exception {

        ResultSet resultSet = null;
        logger.info(sql);
        resultSet = dbManager.executeQuery(sql);
        SQLToMap.Instance.appendMap(resultSet);
        dbManager.closeStatement();
        dbManager.closeConnection();
        setupAndOutput(SQLToMap.Instance.getSQLHtml(0),".html");
        setupAndOutput(SQLToMap.Instance.getSqlJSON(0),".json");
        compare(actual,expected);
    }

    public void setupAndOutput(String response, String extension) throws Exception {

        if (extension.equals(".json")) {
            actual = (new JSONObject(response)).toString(2);
        }

        StringBuffer outputfile = new StringBuffer(dataDir + "response" + slash + testName);
        outputfile.append(extension);
        logger.debug("Output file is: " + outputfile.toString().replace(" ",slash));
        FileUtils.writeStringToFile(new File(outputfile.toString()), response);

        File expectedResponseFile = new File (dataDir + "expectedresponse" + slash + testName + ".json");
        expected = FileUtils.readFileToString(expectedResponseFile);
    }

    protected void compare(String actual, String expected) throws Exception {
        JSONAssert.assertEquals(expected, actual, false);
    }
}