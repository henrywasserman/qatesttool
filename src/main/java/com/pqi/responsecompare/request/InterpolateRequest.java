package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.JSONToMap;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum InterpolateRequest {
	Instance;

	private HashMap<String, Object> map = null;
	private String old_contents = "";

	public String interpolateString(String contents) throws Exception {
		return interpolateString(new StringBuffer(contents)).toString();
	}

	public StringBuffer interpolateString(StringBuffer contents) throws Exception {

		String new_contents = contents.toString();
		String matcherGroup1 = "";
		map = JSONToMap.Instance.getMap();

		Pattern pattern = Pattern.compile("\\$\\{([\\w|\\_|\\-|\\,]+)\\}");
		Matcher matcher = pattern.matcher(contents);

		while (matcher.find()) {

			if (matcher.group(1).toLowerCase().equals("random")) {

				Random rand = new Random();
				long long_rand = rand.nextLong();
				if (long_rand < 0) {
					long_rand = long_rand * -1;
				}

				String random = Long.valueOf(long_rand).toString();
				map.put("RANDOM", random);
			}

			if (matcher.group(1).toLowerCase().contains("random,")) {
				Random rand = new Random();
				long long_rand = rand.nextLong();
				String[] number = StringUtils.split(matcher.group(1), ",");
				Integer length = Integer.valueOf(number[1]);
				String random = Long.valueOf(long_rand).toString().substring(0, length.intValue());
				map.put("RANDOM," + number[1], random);
			}
			
			if (matcher.group(1).toLowerCase().contains("format_rand_birthdate")) {
				String format = StringUtils.split(matcher.group(1), ",")[1];
				String dateString = Utilities.Instance.getRandomDate(format);
				map.put("format_rand_birthdate," + format,dateString);
			}

			if (map.get(matcher.group(1)) == null) {
				throw new Exception("Could not find " + matcher.group(0) + " in the map.");
			} else {
				matcherGroup1 = map.get(matcher.group(1)).toString();
				if (matcherGroup1.isEmpty()) {
					matcherGroup1 = matcher.group(0).replace("$","");
					PropertiesSingleton.Instance.setProperty("emptyVariable",matcherGroup1);
				}
				new_contents = StringUtils.replace(new_contents, matcher.group(0), matcherGroup1);

			}

		}
		//This was added for the case where the body is a variable ${body} and inside that variable there are also variables.
		if (new_contents.contains("${") && !new_contents.equals(old_contents)) {
			old_contents = new_contents;
			new_contents = interpolateString(new StringBuffer(new_contents)).toString();
		}
		old_contents = "";
		return new StringBuffer(new_contents);
	}
}