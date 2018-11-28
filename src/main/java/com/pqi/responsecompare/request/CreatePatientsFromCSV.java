package com.pqi.responsecompare.request;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CreatePatientsFromCSV extends Request {
	static final Logger logger = Logger.getLogger(CreatePatientsFromCSV.class);

	public CreatePatientsFromCSV(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		CloseableHttpResponse response = null;
		post = new StringBuffer(builddir.toString());
		post.append(file);
		String fs = File.separator;

		try {

			logger.info("TestID: " + test.getTestCaseID());
			logger.info("POST Request: " + url + "/hie/patients/registrations");

			String csv_filename = PropertiesSingleton.Instance.getProps()
					.getProperty("csv_file");
			File csv_file = new File(
					Utilities.Instance.getResponseCompareRoot() + fs + "data"
							+ fs + "consult" + fs + "datadriven" + fs
							+ csv_filename);

			ArrayList<String> csv_contents = (ArrayList<String>) FileUtils
					.readLines(csv_file);
			ArrayList<String> headers = new ArrayList<String>();

			int counter = 0;
			for (String line : csv_contents) {
				if (counter == 0) {
					
					headers = new ArrayList<String>(Arrays.asList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1)));
					counter++;
				} else {

					HashMap<String, String> map = Utilities.Instance
							.createMapFromCSV(headers, line);
					
					GetMRN mrn = new GetMRN(test);
					mrn.makeRequests(map.get("firstname"), map.get("lastname"), map.get("gender"), map.get("birthdate"));
					
					CreateClinicalFromCSV req = new CreateClinicalFromCSV(test);
					
					if (map.get("allergies_alias") != null
							&& !map.get("allergies_alias").isEmpty()) {
						req.makeRequests(headers, line, "allergy_registrations", "visit_start_date",counter);
					} else {
						req.makeRequests(headers, line, "registrations","visit_start_date",counter);
					}
					
					if (map.get("care_plan_name") != null
							&& !map.get("care_plan_name").isEmpty()) {
						req.makeRequests(headers, line, "careplan","care_plan_start_date",counter);
					}
					
					if (map.get("func_stat_start_date") != null
							&& !map.get("func_stat_start_date").isEmpty()) {
						req.makeRequests(headers, line, "functionalStatus","func_stat_start_date",counter);
					}
					
					if (map.get("immunization_start_date") != null
							&& !map.get("immunization_start_date").isEmpty()) {
						req.makeRequests(headers, line, "immunizations","immunization_start_date",counter);
					}

					if (map.get("labs_procedure_obs_date") != null
							&& !map.get("labs_procedure_obs_date").isEmpty()) {
					
								if (map.get("labs_end_date") != null
										&& !map.get("labs_end_date").isEmpty()) {
									req.makeRequests(headers, line, "labs_discrete_results","labs_procedure_obs_date",counter);
								} else {
									req.makeRequests(headers, line, "labs","labs_procedure_obs_date",counter);
								}
					}

					if (map.get("soc_hist_community") != null
							&& !map.get("soc_hist_community").isEmpty()) {
						req.makeRequests(headers, line, "socialhistory","soc_hist_start_date",counter);
					}
																				
					if (map.get("meds_start_date") != null
							&& !map.get("meds_start_date").isEmpty()) {
						req.makeRequests(headers, line, "medications","meds_start_date",counter);
					}
					
					if (map.get("clinical_doc_start_date") != null
							&& !map.get("clinical_doc_start_date").isEmpty()) {
						req.makeRequests(headers, line, "clinicaldocuments","clinical_doc_start_date",counter);
					}
					counter++;
				}
			}
		} finally {
			if (response != null) {
				response.close();
			}
			if (test_request_counter + 1 == test.getRequests().size()) {
				test.httpClientClose();
			}
		}
	}
}