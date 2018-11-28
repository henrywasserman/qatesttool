package com.pqi.responsecompare.data;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.json.JSONToMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum DataDriven {
	
	Instance;

	static Logger logger = Logger.getLogger(DataDriven.class);
	
	private String patient_number_filename = "data/consult/datadriven/patient_number.txt";
	private String patient_csv_filename = "data/consult/datadriven/patient.csv";
	private String csv_string = "";
	private String p_number = "";
	private File patient_number_file = null;
	private File patient_csv_file = null;
	private int patient_number = 0;
	private Properties props = null;
	private String email = "";
	private String password = "";
	private String previous_firstname = "";
	private String previous_lastname = "";
	private String previous_timestamp = "";
	private String visit_account_number = "";
	JsonNode templateObj = null;
	private boolean mrn = false;

	DataDriven() {

		try {
			patient_number_file = new File(patient_number_filename);
			patient_csv_file = new File(patient_csv_filename);
			p_number = FileUtils.readFileToString(patient_number_file);
			p_number = p_number.replaceAll("\\n", "");
			patient_number = new Integer(p_number).intValue();
			props = PropertiesSingleton.Instance.getProps();
			FileUtils.deleteQuietly(patient_csv_file);
			FileUtils.writeStringToFile(patient_csv_file,
					"firstname,lastname,username,password\n", "UTF8", true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setMRN(boolean mrn) {
		this.mrn = mrn;
	}

	public String getPatient_number_filename() {
		return patient_number_filename;
	}

	public boolean getMRN() {
		return mrn;
	}

	public String dataDriveContents(String body) throws Exception {
		patient_number++;
		String firstname = props.getProperty("patient.portal.firstname");
		String lastname = props.getProperty("patient.portal.lastname");
		password = props.getProperty("patient.portal.password");
		String p_number = Integer.toString(patient_number);
		FileUtils.writeStringToFile(patient_number_file, p_number);
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(Calendar.getInstance().getTime());
		email = "virttesting+" + timeStamp + "@gmail.com";
		body = StringUtils.replace(body, "${number}", p_number);
		body = StringUtils.replace(body, "${mrn}", timeStamp);
		body = StringUtils.replace(body, "${account_number}", timeStamp);
		body = StringUtils.replace(body, "${firstname}", firstname);
		body = StringUtils.replace(body, "${lastname}", lastname);

		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();

		map.put("email", email);
		map.put("patient_portal_username", email);
		map.put("mrn",mrn);
		map.put("datestring", "19700102");
		map.put("patient_portal_password", password);

		csv_string = firstname + p_number + "," + lastname + p_number + ","
				+ email + "," + password + "\n";

		JSONToMap.Instance.put("portaluser",email);
		JSONToMap.Instance.put("portalpassword", password);

		FileUtils.writeStringToFile(patient_csv_file, csv_string, "UTF8", true);

		JSONToMap.Instance.combineMaps(map);
		return body;
	}

	private void setPreviousNames(String header, String value) {
		if (header.equals("firstname")) {
			previous_firstname = value;
			return;
		}
		if (header.equals("lastname")) {
			previous_lastname = value;
			return;
		}
	}

	private boolean theFullNameIsTheSameAsThePreviousLine(
			ArrayList<String> headers, HashMap<String,String> line) {

		boolean result = false;
		String firstname = "";
		String lastname = "";

		firstname = line.get("firstname");

		lastname = line.get("lastname");

		if (firstname.equals(previous_firstname)
				&& lastname.equals(previous_lastname)) {
			result = true;
		}

		return result;
	}

	public String getVisitAccountNumber() {
		return visit_account_number;
	}

	public String interpolateBodyTemplate(String body,
			ArrayList<String> headers, HashMap<String,String> line, String startdate,
			boolean mrn_found, String endpoint, String endpoint_filename) throws Exception {
		patient_number++;
		String p_number = Integer.toString(patient_number);
		FileUtils.writeStringToFile(patient_number_file, p_number);
		String timeStamp = "";
		String account_number = "";
		String encounter = "";
		csv_string = "";

		if (theFullNameIsTheSameAsThePreviousLine(headers, line)) {
			timeStamp = previous_timestamp;
		} else {
			timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(Calendar.getInstance().getTime());
		}

		ArrayList mrn_array = null;
		LinkedHashMap<String, String> mrn_linked_hashmap = null;

		String mrn = "";
		mrn_array = (ArrayList) JSONToMap.Instance.getMap().get("identifiers");
		if (mrn_array != null && mrn_found) {
			mrn_linked_hashmap = (LinkedHashMap<String, String>) mrn_array
					.get(0);
			mrn = mrn_linked_hashmap.get("value");
			body = StringUtils.replace(body, "${mrn}", mrn);
		} else {
			body = StringUtils.replace(body, "${mrn}", timeStamp);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);

		int counter = 0;

		for (String header : headers) {

		    templateObj = mapper.readTree(body);
			// This is used in the case of an Excel cell that contains a comma.
			// Excel wraps the csv string with doubles quotes
			// We need to pull those double quotes back out.
			if (line.get(header).startsWith("\"")) {

				line.put(header, StringUtils.removeStart(line.get(header), "\""));
				line.put(header,StringUtils.removeEnd(line.get(header), "\""));
			}

			if (line.get(header).isEmpty()) {

				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("race_code_mnemonic")) {

					((ObjectNode)templateObj.get("patient")).remove("race_code");
						
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("language_code_mnemonic")) {

					((ObjectNode)templateObj.get("patient")).remove("language_code");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("ethnic_group_code_mnemonic")) {
					((ObjectNode)templateObj.get("patient")).remove("ethnic_group_code");
					
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("admitting_provider_alias")) {
					((ObjectNode)templateObj.get("visit")).remove("admitting_provider");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("address_street_1")) {
					((ObjectNode)templateObj.get("patient")).remove("addresses");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint.toLowerCase().equals("registrations")
						&& header.toLowerCase().equals("phone_number")) {
					((ObjectNode)templateObj.get("patient")).remove("phones");
					
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_cd_desc")) {
					
					((ObjectNode)templateObj.get("procedures").get(0).get(0).get("lab_cd")).remove("description");
					
					body = mapper.writeValueAsString(templateObj);
				}

				
				if (endpoint.toLowerCase().equals("labs")
						&& header.toLowerCase().equals("labs_proc_result_code_authority")) { 
					((ObjectNode)templateObj.get("procedures").get(0).get("proc_result_sts_cd")).remove("authority");
					
					body = mapper.writeValueAsString(templateObj);
				}

				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_units_results_status_authority")) { 
					((ObjectNode)templateObj.get("procedures").get(0).get("discrete_results").get(0).get("result_status")).remove("authority");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_proc_start_date")) {
					((ObjectNode)templateObj.get("procedures").get(0)).remove("proc_start_date");
					
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_scheduled_proc_start_date")) {
					((ObjectNode)templateObj.get("procedures").get(0)).remove("scheduled_proc_start_date");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_units_result_status_mnemonic")) {
					((ObjectNode)templateObj.get("procedures").get(0).get("discrete_results").get(0)).remove("result_status");
					
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_units_authority")) {
					((ObjectNode)templateObj.get("procedures").get(0).get("discrete_results").get(0).get("units")).remove("authority");
					
					body = mapper.writeValueAsString(templateObj);
					
				}
				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
						&& header.toLowerCase().equals("labs_discrete_results_obs_date")) {
					((ObjectNode)templateObj.get("procedures").get(0).get("discrete_results").get(0)).remove("obs_date");
						
					body = mapper.writeValueAsString(templateObj);
				}
				
				if (endpoint_filename.toLowerCase().equals("labs_discrete_results")
					&& header.toLowerCase().equals("labs_performed_by_person_authority")) {
						((ObjectNode)templateObj.get("procedures").get(0).get("discrete_results").get(0).get("performed_by_person")).remove("authority");
						body = mapper.writeValueAsString(templateObj);
					}
				
				if (endpoint.toLowerCase().equals("medications")
						&& header.toLowerCase().equals("meds_ordering_provider")) {
					((ObjectNode)templateObj.get("medications").get(0)).remove("ordering_provider_alias");
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint.toLowerCase().equals("socialhistory")
						&& header.toLowerCase().equals("soc_hist_value_code_title")) {
					((ObjectNode)templateObj.get("social_histories").get(0)).remove("value_cd");
					
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint.toLowerCase().equals("socialhistory")
						&& header.toLowerCase().equals("soc_hist_free_txt")) {
					String pattern = "\"free_text\"\\:\\s+\"\\$\\{soc_hist_value_txt\\}\"\\,\n\\s\\s\\s\\s\\s\\s";
					((ObjectNode)templateObj.get("social_histories").get(0)).remove("free_text");
					
					body = mapper.writeValueAsString(templateObj);
				}

				if (endpoint.toLowerCase().equals("socialhistory")
						&& header.toLowerCase().equals("soc_hist_value_txt")) {
					((ObjectNode)templateObj.get("social_histories").get(0)).remove("value_text");
					
					body = mapper.writeValueAsString(templateObj);
				}

				counter++;
				continue;
			}

			if (header.toLowerCase().equals(startdate)) {
				if (line.get(header).length() == 8) {
					encounter = Long.toString(new SimpleDateFormat("yyyyMMdd")
							.parse(line.get(header)).getTime());
				} else if (line.get(header).length() == 11 && line.get(header).contains("-")) {
					encounter = Long.toString(new SimpleDateFormat(
							"yyyy-MMM-dd").parse(line.get(header)).getTime());
				} else {
					encounter = Long.toString(new SimpleDateFormat(
							"yyyyMMddHHmm").parse(line.get(header)).getTime());
				}
			}

			if (header.toLowerCase().equals("lastname")) {
				account_number = line.get(header);
			} else if (header.toLowerCase().equals("firstname")) {
				account_number = account_number + "-" + line.get(header);
			} else if (header.toLowerCase().equals("visit_start_date")) {
				account_number = account_number + "-" + line.get(header);
			} else if (header.toLowerCase().equals("gender")) {
				account_number = account_number + "-" + line.get(header);
			}

			if (header.equals("lastname") || header.equals("firstname")) {
				setPreviousNames(header, line.get(header));
				if (PropertiesSingleton.Instance.getProps()
						.getProperty("add_numbers_to_names").toLowerCase()
						.equals("true")) {
					body = StringUtils.replace(body, "${" + header + "}", line.get(header)
							+ p_number);
					csv_string = csv_string + line.get(header) + p_number + ",";
				} else {
					body = StringUtils
							.replace(body, "${" + header + "}", line.get(header));
					csv_string = csv_string + line.get(header) + ",";
				}
			} else {
				//This is if is only because excel turns "" into """"""
				if (line.get(header).equals("\"\"\"\"")) {
					line.put(header, "");
				}
				body = StringUtils.replace(body, "${" + header + "}", line.get(header));
			}
			counter++;
		}
		visit_account_number = account_number;
		body = StringUtils.replace(body, "${account_number}", account_number);
		// body = StringUtils.replace(body, "${encounter}", encounter);
		body = StringUtils.replace(body, "${encounter}", account_number);
		csv_string = csv_string.replaceFirst(",$", "\n");

		FileUtils.writeStringToFile(patient_csv_file, csv_string, "UTF8", true);

		previous_timestamp = timeStamp;
		counter = 0;		
		return body;
	}
}