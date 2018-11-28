package com.pqi.responsecompare.compare;

import com.pqi.responsecompare.request.TestCase;

public enum CompareFactory {
	
	Instance;
	
	private Compare compare = null;
	
	public Compare getCompare(TestCase test) {
		if (test.getComparisonType().equals("xml")) {
			compare = new XmlCompare(test);
		} else if (test.getComparisonType().equals("sql")) {
			compare = new SQLCompare(test);
		} else if (test.getComparisonType().equals("image")) {
			compare = new ImageCompare(test);
		} else if (test.getComparisonType().equals("text")) {
			compare = new TextCompare(test);
		} else if (test.getComparisonType().equals("mapvalue")) {
			compare = new MapValueCompare(test);
		} else if (test.getComparisonType().equals("variables")) {
			compare = new VariableCompare(test);
		} else if (test.getComparisonType().equals("sql_statements")) {
			compare = new SQLStatementsCompare(test);
		}

		return compare;
	}
}
