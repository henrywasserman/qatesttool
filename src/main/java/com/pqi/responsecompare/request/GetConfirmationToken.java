package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.SSHTunnel;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.sql.OracleDbManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class GetConfirmationToken extends Request {

	static final Logger logger = Logger.getLogger(Get.class);
	
	private String confirmationToken = "";
	
	public GetConfirmationToken(TestCase test)  throws Exception {
		super(test);
	}

	@Override
	public void sendRequest() throws Exception  {
		OracleDbManager dbManager = null;
		ResultSet rs = null;
		String result = "";
		dbManager = new OracleDbManager(props);
	 	 // record the start time
		String query = InterpolateRequest.Instance.interpolateString(test.getSqlStrings().get(Integer.toString(test_request_counter)));
		rs = dbManager.executeQuery(query);
		
		while(rs.next()) {
			result = rs.getString("CONFIRMATION_TOKEN");
		}
		
		JSONToMap.Instance.put("confirmationToken",result);
		logger.debug("Added confirmationToken: " + result + " to JSONHash");
		
		dbManager.closeConnection();
		SSHTunnel.Instance.closeTunnel();
	}
}