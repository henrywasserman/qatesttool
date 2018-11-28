package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.sql.DatabaseManager;
import com.pqi.responsecompare.sql.OracleDbManager;
import com.pqi.responsecompare.sql.SQLToMap;
import org.apache.http.HttpResponse;
import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class RunPLSQL extends Request {
	Properties props = Utilities.Instance.getTestProperties();

	String creativeIds = new String();

	public RunPLSQL(TestCase test) throws Exception {
		super(test);
	}

	@Override
	public void sendRequest() throws Exception {
		OracleDbManager dbManager = null;
		java.sql.Timestamp ts = null;

		try {

				props.setProperty("URL.ORACLE",props.getProperty("URL.MPSQA.ORACLE"));
				props.setProperty("UserID.ORACLE",props.getProperty("sc_base_user.UserID.ORACLE"));
				props.setProperty("Password.ORACLE",props.getProperty("sc_base_user.password"));

				dbManager = new OracleDbManager(props);
			// record the start time
			//ts = dbManager.getDatabaseTimeStamp();
		} catch (Exception ex) {
			logger.error("Could not obtain database connection ", ex);
			Assert.fail("Could not obtain database connection " + ex.getMessage());
		}
		this.executeAndValidateSql(ts, dbManager);
		//dbManager.closeConnection();
	}

	/**
	 * Gets a list of creativeIds out of the response object
	 *
	 * @param response
	 * @return
	 */
	List<String> getCreativeIdsFromResponse(HttpResponse response) {
		return Utilities.Instance.getAllNodeValuesFromResponse("creativeid", response);
	}

	protected void executeAndValidateSql(java.sql.Timestamp ts, DatabaseManager dbManager) throws SQLException, Exception {

		String sql = InterpolateRequest.Instance.interpolateString(
				test.getRequests().get(test_request_counter).getSQL());

		ResultSet resultSet = null;
		logger.info(sql);
		String memberQuery = "";

		dbManager.executePLSQL(sql, this);

		memberQuery = "SELECT *" +
			" FROM JENKINS.TEST_INPUT";

		resultSet = dbManager.executeQuery(memberQuery);

		SQLToMap.Instance.appendMap(resultSet);
		dbManager.closeStatement();
		dbManager.closeConnection();
		setupAndOutput(SQLToMap.Instance.getSQLHtml(test.getTestRequestCounter()),".html");
		setupAndOutput(SQLToMap.Instance.getSqlJSON(test.getTestRequestCounter()), ".json");

	}
}