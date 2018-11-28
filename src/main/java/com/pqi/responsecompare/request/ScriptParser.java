package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.sql.SQLToMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptParser {

	static final Logger logger = Logger.getLogger(Get.class);

	private Collection<File> responsecomparefiles = new ArrayList<File>();
	private boolean isBody = false;
	private boolean avoidMailCommands = false;

	public ScriptParser(Collection<File> responsecomparefiles) {
		this.responsecomparefiles = responsecomparefiles;
	}

	public ArrayList<TestCase> parse() throws Exception {

		ArrayList<TestCase> allTestCases = new ArrayList<TestCase>();
		int counter = Integer.valueOf(PropertiesSingleton.Instance.getProps().getProperty("repeat-testcase"));
		for (int i = 0; i < counter; i++) {

			for (File responsecomparefile : responsecomparefiles) {

				if (!PropertiesSingleton.Instance.getProperty("filelist").equals(responsecomparefile.getName())) {
					continue;
				}

				BufferedReader in = new BufferedReader(new FileReader(
						responsecomparefile));
				String line;
				int lineNum = 0;
				Pattern ptnCommand = Pattern.compile("^([a-zA-Z_]+)(.*)"); // Expects "COMMAND and then other stuff"
				Pattern ptnTestCase = Pattern.compile("^TESTCASE+\\s+(.*)\\,(.*)");// Expects "TESTCASE my_id, some description"
				Pattern ptnAPIMethods = Pattern.compile("POST|PUT|GET|DELETE|PATCH");

				ArrayList<String> allValidCommands = ValidCommands.Instance.getAllValidCommands();
				Matcher match = null;

				TestCase currentTestCase = null;

				boolean append = false;
				while ((line = in.readLine()) != null) {
					//logger.debug(line);
					line = line.trim();
					lineNum++;
					match = ptnAPIMethods.matcher(line);
					if (avoidMailCommands && !match.find()) {
						continue;
					} else {
						avoidMailCommands = false;
					}

					if (currentTestCase == null) {
						append = false;
					} else if (currentTestCase.getIsJSONAssign()) {
						append = true;
					} else if (ValidCommands.Instance.getAllValidCommands()
							.contains(line.split(" " )[0])) {
						append = false;
					} else if (currentTestCase.getIsAppend()) {
						append = true;
					} else {
						append = false;
					}

					if (line.contains("TESTCASE")) {
						append = false;
					}

					if (line.startsWith("#") || line.equals("") || line.trim().startsWith("--"))
					{
						continue; // Skip comments and blanks
					}

					if (line.contains("--")) {
						logger.info(line);
						line = line.split("--")[0];
					}

					if (PropertiesSingleton.Instance.getProperty("user.hasmail").equals("false") && line.contains("mailboxes")) {
						avoidMailCommands=true;
						continue;
					}

					match = ptnCommand.matcher(line);
					if (match.find() && (!append)) {
						if (match.group(1).equals("TESTCASE")) { // We found a new TESTCASE line
							if (currentTestCase != null) {
								currentTestCase.setIsBody(false);
								currentTestCase.setIsAssign(false);
								currentTestCase.setIsJSONAssign(false);
								currentTestCase.setIsSQL(false);
								currentTestCase.setIsAppend(false);

								append = false;
							}
							match = ptnTestCase.matcher(line);
							if (match.find()) {
								currentTestCase = new TestCase();
								currentTestCase
										.setRequestFile(responsecomparefile
												.toString());
								currentTestCase.setLineNum(lineNum);
								currentTestCase.setTestCaseID(StringUtils.replace(match.group(1)
										.trim(),"/","_"));
								currentTestCase.setTestCaseDescription(match
										.group(2).trim());
								allTestCases.add(currentTestCase);
							} else {
								throwParseError(lineNum, line,
										"Incorrect TESTCASE line");
							}
						} else {
							if (allValidCommands.contains(match.group(1))) {
								// Found supported commands so add them to the
								// testcase object
								if (currentTestCase != null) {
									currentTestCase.setIsBody(false);
									currentTestCase.setIsAssign(false);
									currentTestCase.setIsJSONAssign(false);
									currentTestCase.setIsSQL(false);


									append = false;
									avoidMailCommands=false;
								}
								currentTestCase.addCommand(match.group(1)
										.trim(), match.group(2).trim());
							} else {

								throwParseError(lineNum, line,
										"Invalid command: " + line + " Supported commands are: "
												+ allValidCommands.toString());
							}
						}
					} else {
						if (currentTestCase != null) {
							currentTestCase.addCommand("",line);
							continue;
						} else {
						throwParseError(lineNum, line,
								"Could not match a command: " + line + " on line using regex pattern: "
										+ ptnCommand.pattern());
						}
					}
				}
				in.close();
			}
		}
		return allTestCases;
	}

	private void throwParseError(int lineNum, String line, String message)
			throws Exception {
		throw new Exception("Parse error on line " + lineNum + ": \"" + line
				+ "\" -- message is: " + message);
	}

}
