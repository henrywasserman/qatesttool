package com.pqi.responsecompare.request;

import com.pqi.responsecompare.ICD9.VerifyICD;
import com.pqi.responsecompare.edi.EdiToMySql;
import com.pqi.responsecompare.edi.EdiToTalend;

public enum RequestFactory {

	Instance;
	
	private Request req = null;


	public Request getRequest(TestCase test) throws Exception {
		String type = getRequestType(test).toLowerCase();

		if (type.toLowerCase().equals("body")) {
		} else if (type.equals("add_role_to_user_from_csv")) {
			req = new AddRoleToUserFromCSV(test);
		} else if (type.equals("create_patient_portal_user")) {
			req = new CreatePatientPortalUser(test);
		} else if (type.equals("create_automation_role")) {
			req = new CreateAutomationRole(test);
		} else if (type.equals("create_patients_from_csv")) {
			req = new CreatePatientsFromCSV(test);
		} else if (type.equals("create_users")) {
			req = new CreateUsers(test);
		} else if (type.equals("create_json_for_role")) {
			req = new CreateJSONRole(test);
		} else if (type.equals("create_agnostic_properties")) {
			req = new CreateAgnosticProperties(test);
		} else if (type.equals("edi_to_talend")) {
			req = new EdiToTalend(test);
		} else if (type.equals("edi_to_mysql")) {
			req = new EdiToMySql(test);
		} else if (type.equals("get")) {
			req = new Get(test);
		} else if (type.equals("delete")) {
			req = new Delete(test);
		} else if (type.equals("log")) {
			req = new Log(test);
		} else if (type.equals("post")) {
			req = new Post(test);
		} else if (type.equals("post_multipart")) {
			req = new PostMultiPart(test);
		} else if (type.equals("put")) {
			req = new Put(test);
		} else if (type.equals("remote_shell")) {
			req = new RemoteShell(test);
		} else if (type.equals("run_oracle_sql")) {
			req = new RunOracleSQL(test);
		} else if (type.equals("run_pl_sql")) {
			req = new RunPLSQL(test);
		} else if (type.equals("run_sqlserver_sql")) {
			req = new RunSQLServerSQL(test);
		} else if (type.equals("run_mysqlserver_execute_sql")) {
			req = new RunMySqlServerExecute(test);
		} else if (type.equals("run_sqlserver_execute_sql")) {
			req = new RunSQLServerExecute(test);
		} else if (type.equals("run_oracleserver_execute_sql")) {
			req = new RunOracleServerExecute(test);
		} else if (type.equals("run_teradata_execute_sql")) {
			req = new RunTeradataExecute(test);
		} else if (type.equals("run_sql_from_file")) {
			req = new RunSQLFromFile(test);
//		} else if (type.toLowerCase().equals("sample_keyword")) {
//			req = new SampleKeywordClassfile(test);
		} else if (type.equals("expect_error")) {
			req = new ExpectError(test);
		} else if (type.equals("getlist_jason")) {
			req = new GetListJason(test);
		} else if (type.equals("get_image")) {
			req = new GetImage(test);
		} else if (type.equals("post_image")) {
			req = new PostImage(test);
		} else if (type.equals("get_images")) {
			req = new GetImages(test);
		} else if (type.equals("post_images")) {
			req = new PostImages(test);
		} else if (type.equals("post_with_authcache")) {
			req = new PostWithAuthCache(test);
		} else if (type.equals("get_wapi_image")) {
			req = new GetWapiImage(test);
		} else if (type.equals("get_with_authcache")) {
			req = new GetWithAuthCache(test);
		} else if (type.equals("get_confirmation_token")) {
			req = new GetConfirmationToken(test);
		} else if (type.equals("verify_icd")) {
			req = new VerifyICD(test);
		}

		return req;
	}

	public void resetReq() {
		req = null;
	}

	private String getRequestType(TestCase testcase) {
		String requesttype = "";
		try {
			requesttype = testcase.getRequests().get(testcase.getTestRequestCounter()).getRequestType()
					.toLowerCase();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return requesttype;
	}
}
