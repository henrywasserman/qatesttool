package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.SSHTunnel;
import com.pqi.responsecompare.json.JSONToMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;

public class RemoteShell extends Request {
	static final Logger logger = Logger.getLogger(RemoteShell.class);

	public RemoteShell(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		Connection conn = null;

		String fs = File.separator;
		logger.info("TestID: " + test.getTestCaseID());

		try {
			logger.debug("Running Remote Shell");

			SSHTunnel.Instance.openTunnel();
			logger.debug("Finished Opening Tunnel");

			JSONToMap.Instance.put("consult-host", PropertiesSingleton.Instance.getProperty("consult-host"));

			String rshCommand = test.getRequests().get(test_request_counter).getRshCommand();

			rshCommand = InterpolateRequest.Instance.interpolateString(rshCommand);

			String result = SSHTunnel.Instance.sendCommand(rshCommand);

			JSONToMap.Instance.put("RSH_RESULT",result);
			if (result.toLowerCase().contains("xml")) {
				setupAndOutput(result, ".xml");
				//This is a hack to make sure a .json file is created as well.
				//Refactor this later
				setupAndOutput(result, ".json");
			}

		} catch (Exception e) {
			throw e;

		} finally {

		}
	}

}
