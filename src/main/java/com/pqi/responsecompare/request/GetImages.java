package com.pqi.responsecompare.request;

import org.apache.log4j.Logger;

public class GetImages extends Get {

	static final Logger logger = Logger.getLogger(GetImages.class);
	
	public GetImages(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {
		super.sendRequest();
		getImages();
	}
}