package com.pqi.responsecompare.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.pqi.responsecompare.sql.OracleDbManager;
import com.pqi.responsecompare.sql.SQLToMap;
import org.junit.Assert;

import org.apache.http.HttpResponse;
import com.pqi.responsecompare.configuration.Utilities;

public class RunOracleSQL extends Request {
	Properties props = Utilities.Instance.getTestProperties();

	String creativeIds = new String();

	public RunOracleSQL(TestCase test)  throws Exception {
		super(test);
	}

	@Override
	public void sendRequest() throws Exception  {
		OracleDbManager dbManager = null;
		java.sql.Timestamp ts = null; 
		try{
		  dbManager = new OracleDbManager(props);

	 	 // record the start time
		 //ts = dbManager.getDatabaseTimeStamp();
	     }catch(Exception ex){
	    	 logger.error("Could not obtain database connection ", ex);
		     Assert.fail("Could not obtain database connection "+ ex.getMessage()); 
          }
		this.executeAndValidateSql(ts, dbManager);
		dbManager.closeConnection();
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

	protected void executeAndValidateSql (java.sql.Timestamp ts, OracleDbManager dbManager) throws SQLException, Exception {

		String sql = test.getRequests().get(test_request_counter).getSQL();
		ResultSet resultSet = null;
		logger.info(sql);
		resultSet = dbManager.executeQuery(sql);
		SQLToMap.Instance.appendMap(resultSet);
		dbManager.closeStatement();
		dbManager.closeConnection();
		setupAndOutput(SQLToMap.Instance.getSQLHtml(test.getTestRequestCounter()),".html");
		setupAndOutput(SQLToMap.Instance.getSqlJSON(test.getTestRequestCounter()), ".json");

		/*
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while (resultSet.next()) {
			for (int i = 1; i <= numberOfColumns; i++) {
				logger.info(rsmd.getColumnName(i) + ":" +
						resultSet.getObject(rsmd.getColumnName(i)));
			}
		}
		*/
	}
}
