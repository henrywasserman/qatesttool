package com.pqi.responsecompare.request;

import org.apache.log4j.Logger;

public class PostImages extends Post {

	static final Logger logger = Logger.getLogger(PostImages.class);
	
	public PostImages(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {
		super.sendRequest();
		
		getImages();
	}
}