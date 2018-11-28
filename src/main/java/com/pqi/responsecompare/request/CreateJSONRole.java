package com.pqi.responsecompare.request;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class CreateJSONRole extends Request {
	static final Logger logger = Logger.getLogger(CreateJSONRole.class);

	private String feature = "";
	private String action = "";
	private StringBuffer json = new StringBuffer();


	public CreateJSONRole(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		HashMap<String,String> featureAction = new HashMap<String,String>();

		String postFileBodyDir = System.getProperty("user.dir") +
			File.separator + "data" +
			File.separator + "consult" +
			File.separator + "post_file_body" +
			File.separator;

		File role = new File(postFileBodyDir + "role.json");

		json.append("[\n  {\"feature\": \"ClinicalNotes\",\n    \"action\": \"Update\"\n  },\n"
			+ "  {\"feature\": \"EngageAccount\",\n    \"action\": \"View\"\n  },\n"
			+ "  {\"feature\": \"Inbox\",\n    \"action\": \"Share\"\n  },\n"
			+ "  {\"feature\": \"Inbox\",\n    \"action\": \"View\"\n  },\n"
			+ "  {\"feature\": \"PatientDirectives\",\n    \"action\": \"View\"\n  },\n"
			+ "  {\"feature\": \"PatientRelationship\",\n    \"action\": \"View\"\n  },\n"
			+ "  {\"feature\": \"ProgramManager\",\n    \"action\": \"Delete\"\n  },\n"
			+ "  {\"feature\": \"UserManager\",\n    \"action\": \"Delete\"\n  },\n"
			+ " {\"feature\": \"StickyNotes\",\n    \"action\": \"Create\"\n  },\n"
		);
		List<String> rolesList = FileUtils.readLines(new File(postFileBodyDir +
 			"roles_enums.coffee"));

		for (String line: rolesList) {
			if (line.trim().startsWith("#")) {
				continue;
			}

			if (line.trim().contains("permKey") || line.trim().contains("PermKeys"))
			{
				line = line.replace("permKey:", "").trim();
				line = line.replace("parentPermKeys", "").trim();
				line = line.replace("blockPermKeys", "").trim();
				line = line.replaceAll("\\[", "").trim();
				line = line.replaceAll("\\]", "").trim();
				line = line.replaceAll(":", "").trim();
				line = line.replaceAll("'", "");

				if (!featureAction.containsKey(line))
				{
					featureAction.put(line, "");

					String[] data = StringUtils.split(line.trim(), ".");
					feature = data[0];
					action = data[1];

					json.append("  {\n    \"feature\": \"" + feature + "\",\n");
					json.append("    \"action\": \"" + action + "\"\n  },\n");
				}
			}
		}

		//Don't forget to handle the extra comma at the end
		json.deleteCharAt(json.lastIndexOf(","));
		json.append("]");
		FileUtils.writeStringToFile(role,json.toString());

	}
}