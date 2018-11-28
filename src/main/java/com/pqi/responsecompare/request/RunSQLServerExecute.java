package com.pqi.responsecompare.request;

import com.pqi.responsecompare.sql.DatabaseManager;
import com.pqi.responsecompare.sql.SqlServerDbManager;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.sql.SQLToMap;
import org.apache.http.HttpResponse;
import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class RunSQLServerExecute extends Request {
	Properties props = Utilities.Instance.getTestProperties();

	String creativeIds = new String();

	public RunSQLServerExecute(TestCase test) throws Exception {
		super(test);
	}

	@Override
	public void sendRequest() throws Exception {
		SqlServerDbManager dbManager = null;
		java.sql.Timestamp ts = null;

		try {
			if (!url.isEmpty()) {
				String[] properties = url.split(",");

				props.setProperty("mssql-host", properties[0].trim());
				props.setProperty("mssql-dbname", properties[1].trim());
				if (properties.length > 2) {
					props.setProperty("query_name", properties[2].trim());
				} else {
					props.setProperty("query_name",
							Integer.toString(test.getTestRequestCounter()));
				}
			}

			dbManager = new SqlServerDbManager(props);

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

		String sql = test.getRequests().get(test_request_counter).getSQL();
		ResultSet resultSet = null;
		logger.info(sql);
		dbManager.execute(sql, this);

		if (props.getProperty("query_name") != null) {
			if (!props.getProperty("query_name").isEmpty()) {
				setupAndOutput(SQLToMap.Instance.getSQLHtml(test.getTestRequestCounter())
						, ".html", props.getProperty("query_name"));
				setupAndOutput(SQLToMap.Instance.getSqlJSON(test.getTestRequestCounter())
						, ".json", props.getProperty("query_name"));
			}
		}


		dbManager.closeStatement();
		dbManager.closeConnection();
	}
}