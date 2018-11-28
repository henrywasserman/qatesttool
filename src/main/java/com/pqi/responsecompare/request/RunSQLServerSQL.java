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

public class RunSQLServerSQL extends Request {
	Properties props = Utilities.Instance.getTestProperties();

	String creativeIds = new String();

	public RunSQLServerSQL(TestCase test)  throws Exception {
		super(test);
	}

	@Override
	public void sendRequest() throws Exception  {
		SqlServerDbManager dbManager = null;
		java.sql.Timestamp ts = null; 
		try{
		  dbManager = new SqlServerDbManager(props);

	 	 // record the start time
		 //ts = dbManager.getDatabaseTimeStamp();
	     }catch(Exception ex){
	    	 logger.error("Could not obtain database connection ", ex);
		     Assert.fail("Could not obtain database connection "+ ex.getMessage()); 
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

	protected void executeAndValidateSql (java.sql.Timestamp ts, DatabaseManager dbManager) throws SQLException, Exception {

		String sql = test.getRequests().get(test_request_counter).getSQL();
		ResultSet resultSet = null;
		logger.info(sql);
		resultSet = dbManager.executeQuery(sql);
		SQLToMap.Instance.appendMap(resultSet);

		setupAndOutput(SQLToMap.Instance.getSQLHtml(test.getTestRequestCounter()),".html");
		setupAndOutput(SQLToMap.Instance.getSqlJSON(test.getTestRequestCounter()), ".json");
	}
}
