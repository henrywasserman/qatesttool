package com.pqi.responsecompare.request;

import org.apache.log4j.Logger;

public class Log extends Request {
	static final Logger logger = Logger.getLogger(Log.class);

	public Log(TestCase test) throws Exception {
		super(test);
	}


	public void sendRequest() throws Exception
	{
		log = InterpolateRequest.Instance.interpolateString(log);
		logger.info(log);
	}

}