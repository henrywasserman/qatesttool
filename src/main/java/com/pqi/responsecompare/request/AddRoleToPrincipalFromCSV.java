package com.pqi.responsecompare.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.json.HandleJSONRequest;
import com.pqi.responsecompare.json.JSONToMap;
import com.pqi.responsecompare.tail.TailManager;
import nu.xom.Builder;
import nu.xom.Serializer;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class AddRoleToPrincipalFromCSV extends Request {
	static final Logger logger = Logger.getLogger(AddRoleToPrincipalFromCSV.class);

	public AddRoleToPrincipalFromCSV(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() {};

	public void makeRequests(String url) throws Exception {

		CloseableHttpResponse response = null;
		post = new StringBuffer(builddir.toString());
		post.append(file);
		String fs = File.separator;
		
		ArrayList<String> zelda_array = new ArrayList<String>();
		//select app_permission_id, app_feature_id, app_action_id from p_app_permission order by app_feature_id
		
		zelda_array.add("2532C6875ADA0767E053B425660A0973");//	AdminSummary	View
		zelda_array.add("254CBEDB54B0078EE053B425660ABED2");//	Alerts	View
		zelda_array.add("2532C6875ADD0767E053B425660A0973");//	Allergies	View
		zelda_array.add("2532C6875AE00767E053B425660A0973");//	CareAlerts	View
		zelda_array.add("2532C6875AE30767E053B425660A0973");//	CarePreferences	View
		zelda_array.add("254CBEDB54B3078EE053B425660ABED2");//	ClinicalNotes	View
		zelda_array.add("2532C6875AE90767E053B425660A0973");//	ClinicalSummary	View
		zelda_array.add("2532C6875AEC0767E053B425660A0973");//	DiagnosticImages	View
		zelda_array.add("2532C6875AEF0767E053B425660A0973");//	EngageAccount	Invite
		zelda_array.add("2532C6875B2D0767E053B425660A0973");//	EngageAccount	Update
		zelda_array.add("254CBEDB54B1078EE053B425660ABED2");//	Flowsheet	Update
		zelda_array.add("2532C6875AF20767E053B425660A0973");//	Flowsheet	View
		zelda_array.add("2532C6875AF50767E053B425660A0973");//	Immunizations	View
		zelda_array.add("2532C6875AF80767E053B425660A0973");//	Inbox	Share
		zelda_array.add("2532C6875AFB0767E053B425660A0973");//	Inbox	View
		zelda_array.add("2532C6875AFE0767E053B425660A0973");//	Labs	View
		zelda_array.add("2532C6875B010767E053B425660A0973");//	Medications	View
		zelda_array.add("2532C6875B1D0767E053B425660A0973");//	PatientConsent	Update
		zelda_array.add("2532C6875B040767E053B425660A0973");//	PatientConsent	View
		zelda_array.add("2532C6875B070767E053B425660A0973");//	PatientDirectives	View
		zelda_array.add("2532C6875B0A0767E053B425660A0973");//	PatientRelationship	Create
		zelda_array.add("2532C6875B0D0767E053B425660A0973");//	PatientRelationship	View
		zelda_array.add("2532C6875B100767E053B425660A0973");//	PatientSearch	View
		zelda_array.add("2532C6875B130767E053B425660A0973");//	Pharmacies	View
		zelda_array.add("254CBEDB54B2078EE053B425660ABED2");//	Problems	View
		zelda_array.add("2532C6875B160767E053B425660A0973");//	SensitiveData	View
		zelda_array.add("2532C6875B190767E053B425660A0973");//	Vitals	View		
		try {
			logger.info("TestID: " + test.getTestCaseID());
			
			httpget = new HttpGet(url);

			setGetHeaders(test_request_counter);

			logger.debug("Executing get");
			test.setHttpClient();
			JSONToMap.Instance.clearArrayList();
			response = test.getHttpClient().execute(httpget);
			//SplunkManager.Instance.search("search * | reverse");
			if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
				logger.debug("Here is tail: " + TailManager.Instance.getTail());
				FileUtils.writeStringToFile(new File(logoutputfile), TailManager.Instance.getTail(), false);
			}
			logger.debug("Finished executing get");
			validateHeaders(response, test_request_counter);
			
			String permissions = PropertiesSingleton.Instance.getProps()
					.getProperty("permissions_file");
			
			File permissions_file = new File(
					Utilities.Instance.getResponseCompareRoot() + fs + "data"
							+ fs + "consult" + fs + "datadriven" + fs
							+ permissions);
			
			String permissions_json = FileUtils.readFileToString(permissions_file, "UTF8");
			
			setupAndOutput(response);

			if (isJSONRequest(test_request_counter,response)) {

				String jsonString = FileUtils.readFileToString(new File(
						outputfile.toString()));

				if (!jsonString.isEmpty()) {

					jsonString = "{\"root\":" + jsonString + "}";
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.readTree(jsonString);
					JSONToMap.Instance.setArrayList(node,"", "id");

					//LinkedHashMap<String, Object> variable_map = JSONToMap.Instance
					//		.setResponseMap(jsonString);
					//JSONToMap.Instance.combineMaps(variable_map);
					logger.debug(jsonString);
					ArrayList<String> org_list = JSONToMap.Instance.getArrayList();

					String url_string = Utilities.Instance.getEndPoint("consult");
					///POST principals/PRINCIPAL-ID-1/roles/ROLE-ID-1
					//For Zelda
					url_string = url_string + "principals/${principal_id}/roles/5cc007b6-cfe1-4539-8aed-943b6a7abf2f";
					//For Virtua Cert
					//url_string = url_string + "principals/${principal_id}/roles/df2ba7a6-ee5a-4c04-bc98-0c3d974ce5d1";
					//For Whittier Dev
					//{{host}}/principals/795ead23-08ca-4894-a911-cfc271a10ffa/roles/2ea2e143-b33f-4d70-972d-69d9340517e0
					//url_string = url_string + "principals/${principal_id}/roles/2ea2e143-b33f-4d70-972d-69d9340517e0";
					url_string = InterpolateRequest.Instance.interpolateString(url_string);
					httppost= new HttpPost(url_string);
					setPostHeaders(test_request_counter);
					response = test.getHttpClient().execute(httppost);
					setupAndOutput(response);
					jsonString = FileUtils.readFileToString(new File(outputfile.toString()));
					
					for (String id:org_list) {
						
						url_string = Utilities.Instance.getEndPoint("consult");
						url_string = url_string +
								//for Zelda
								"organizations/" + id + "/permissions";
						
						test.getCurrentParsedRequest().setBody(
								"  	{" +
										"\"id\": \"2532C6875ADD0767E053B425660A0973\"," +
										"\"feature\": \"Allergies\"," +
										"\"action\": \"View\"" +
									"}");
						
						entity = test.getRequests().get(test_request_counter).getBody();
						httppost = new HttpPost(url_string);
						setPostHeaders(test_request_counter);
						httppost.setEntity(entity);
						response = test.getHttpClient().execute(httppost);
						setupAndOutput(response);
						jsonString = FileUtils.readFileToString(new File(outputfile.toString()));

						
								
						test.getCurrentParsedRequest().setBody(permissions_json);
						entity = test.getRequests().get(test_request_counter).getBody();
						httpput = new HttpPut(url_string);
						setPutHeaders(test_request_counter);
						httpput.addHeader("Content-Type","application/JSON");
						httpput.setEntity(entity);
						response = test.getHttpClient().execute(httpput);
						setupAndOutput(response);
						jsonString = FileUtils.readFileToString(new File(outputfile.toString()));

						for (int counter = 0; counter < zelda_array.size(); counter++) {
						
							url_string = Utilities.Instance.getEndPoint("consult");
							url_string = url_string +
							//http://confluence.wellogic.com/display/AGTS/How+to+add+a+permission+X+to+a+user+A
							//POST /roles/ROLE-ID-1/permissions/PERMISSION-ID-1?orgId=ORG-ID-1
							//roles/{roleId}/permissions/{permissionId}
							//For Zelda
							"roles/5cc007b6-cfe1-4539-8aed-943b6a7abf2f/permissions/"
							//For Virtua Cert
							//"roles/df2ba7a6-ee5a-4c04-bc98-0c3d974ce5d1/permissions/"
							//For dev.whittier
							//"roles/2ea2e143-b33f-4d70-972d-69d9340517e0/permissions/"
							//For Zelda
							+ zelda_array.get(counter) + "?orgId=" + id;
							//+ "12354D95B957776FE0536693490A135D?orgId=" + id;
							//for Virtua Cert
							//+ "74CB0331-6CCC-42D4-8D4F-D6546AC831BA?orgId=" + id;
							//for dev.whittier
							//+ "19287621E83752CAE05308F1460A47F5?orgId=" + id;

								httppost = new HttpPost(url_string);
								setPostHeaders(test_request_counter);
								response = test.getHttpClient().execute(httppost);
								setupAndOutput(response);
								jsonString = FileUtils.readFileToString(new File(outputfile.toString()));
						}
					}

					String res = HandleJSONRequest.Instance.convertToXml(jsonString, "", true);

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Serializer serializer = new Serializer(out);
					serializer.setIndent(2); // or whatever you like
					serializer.write(new Builder().build(res, ""));
					res = out.toString("UTF-8");

					logger.debug(res);
					FileUtils.writeStringToFile(new
					File(outputfile.toString().replace(".json", ".xml")), res);
				}
			}
			
			Utilities.Instance.logHeaders(response);
		} catch (Exception e) {
			e.printStackTrace();

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