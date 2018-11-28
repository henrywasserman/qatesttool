package com.pqi.responsecompare.compare;

import com.pqi.responsecompare.request.TestCase;

public class CompareResults {
	public CompareResults(TestCase test) throws Exception {
		CompareFactory cf = CompareFactory.Instance;
		Compare compare = cf.getCompare(test);
		compare.results();
	}
}
