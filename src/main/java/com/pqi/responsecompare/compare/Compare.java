package com.pqi.responsecompare.compare;

import org.custommonkey.xmlunit.XMLTestCase;

import com.pqi.responsecompare.configuration.PathGenerator;
import com.pqi.responsecompare.request.TestCase;

public abstract class Compare extends XMLTestCase {
	protected String responseFile = "";
	protected String expectedTransformed = "";
	protected String expected = "";
	protected String fileName = "";
	protected TestCase test = null;
	protected PathGenerator pathGenerator = null;

	public Compare(TestCase test) {
		this.test = test;
		pathGenerator = new PathGenerator(test);
		responseFile = pathGenerator.getResponseFile();
		expectedTransformed = pathGenerator.getExpectedTransformed();
		expected = pathGenerator.getExpected();
	}
	
	abstract void results() throws Exception;
	
	public Compare(String testMethodName) {
		super(testMethodName);
	}

	public Compare(String testMethodName, String fileName) {
		super(testMethodName);
		this.fileName = fileName;
	}

	public String getResponseFile() {
		return responseFile;
	}
	
	public String getExpectedTransformed() {
		return expectedTransformed;
	}
	
	public String getExpected() {
		return expected;
	}

}