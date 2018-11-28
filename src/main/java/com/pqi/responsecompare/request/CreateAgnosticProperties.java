package com.pqi.responsecompare.request;

import com.pqi.responsecompare.sql.OracleDbManager;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.configuration.SSHTunnel;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class CreateAgnosticProperties extends Request {
	static final Logger logger = Logger.getLogger(CreateAgnosticProperties.class);
	private ResultSet rs = null;
	private String query = "";
	private Properties agnosticProps = null;
	private OracleDbManager db = null;
	private final Formatter formatter = new BasicFormatterImpl();
	private final File sqlTable = new File ("reports/sql_table.html");
	private String description = "";

	public CreateAgnosticProperties(TestCase test) throws Exception {
		super(test);
	}

	public void sendRequest() throws Exception {

		boolean req_result = false;
		String fs = File.separator;
		logger.info("TestID: " + test.getTestCaseID());
		agnosticProps = PropertiesSingleton.Instance.getEnvironmentAgnosticProps();

		try {
			logger.debug("Getting db Oracle Manager");
			SSHTunnel.Instance.openTunnel();
			db = new OracleDbManager(PropertiesSingleton.Instance.getProps());

			logger.debug("runQuery alter session set current_schema");
			runQuery("alter session set current_schema=" + PropertiesSingleton.Instance.getProperty("database.schema"));
			logger.debug("Finished query");
			setProperty("database-schema",PropertiesSingleton.Instance.getProperty("database.schema"));

			startCreateHTML();

			//Any patient
			description = "Query to get a simple mpi";
			logger.debug(description);
			//req_result = runQuery("select pat_mpi from w_patient where pat_mpi is not null and data_domain_id != '-99'");
			req_result = runQuery("SELECT DISTINCT pat_mpi FROM w_patient WHERE pat_mpi NOT IN (SELECT pat_mpi FROM w_patient WHERE gender_cd IS NULL AND pat_mpi IS NOT NULL AND data_domain_id != '99' ) AND sts_cd = '137' AND LENGTH(pat_mpi) > 4");
			if (req_result)
			{
				setProperty("generic-mpi", rs.getString("pat_mpi"));
				setProperty("unlock-patient-mpi", rs.getString("pat_mpi"));
			}
			logger.debug("Calling rs.close");
			rs.close();
			logger.debug("Calling db.closeStatement");
			db.closeStatement();
			logger.debug("Finished calling db.closeStatement");

			//Any patient that has pharmacy data
            description = "Query to get any patient that has pharmacy data";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi, nm_first, nm_last, lifecycle_cd " +
					"FROM w_patient WHERE pat_id in " +
					"(SELECT pat_id FROM w_patient_pharmacy WHERE delete_ind = 0) AND pat_mpi IS NOT NULL");
			if (req_result) {
				setProperty("view-pharmacies-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-pharmacies-mpi", "");
			}
			rs.close();
			db.closeStatement();


			//Any patient with allergies
			description = "Query For Any patient with allergies";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi FROM w_patient WHERE pat_id IN " +
					"(SELECT pat_id FROM w_patient_dx WHERE dx_typ_cd IN " +
					"(SELECT code_id FROM p_code WHERE code_set_id IN " +
					"(SELECT code_set_id FROM p_code_set WHERE " +
					"LOWER(code_set_name) = 'clinical event type') " +
					"AND LOWER(title) LIKE '%allergy') " +
					"AND sts_cd = 137 AND lifecycle_cd IN " +
					"(SELECT code_id FROM p_code " +
					"WHERE code_set_id IN " +
					"(SELECT code_set_id FROM p_code_set " +
					"WHERE LOWER(code_set_name) = 'allergy status') " +
					"AND LOWER(mnemonic) = 'active')" +
					"AND v_org_id != 'null'" +
					")");
			if (req_result)
			{
				setProperty("view-allergies-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-allergies-mpi", "");
			}
			rs.close();
			db.closeStatement();


			//Any patient with medications
			description = "Query For Any patient with medications";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi FROM w_patient WHERE pat_id IN (SELECT pat_id FROM w_patient_rx WHERE rx_typ_cd = '1474')	");
			if (req_result)
			{
				setProperty("view-medications-mpi", rs.getString("pat_mpi"));
			}  else {
				setProperty("view-medications-mpi", "");
			}
			rs.close();
			db.closeStatement();


			//Any patient with non visit items
			description = "Query For Any Patient with non visit items";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi FROM w_patient " +
					"WHERE pat_id IN " +
					"  (SELECT pat_id FROM w_patient_document WHERE " +
					"service_start_at IS NULL AND " +
					"doc_category_cd IN (SELECT code_id " +
					"FROM p_code " +
					"WHERE delete_ind=0 " +
					"AND authority_id='WELL_WELLOGIC' " +
					"AND code_set_id =-18 " +
					"AND title       ='Clinical Documents'" +
					") AND pat_visit_id IS NULL AND rownum < 10) AND LENGTH(pat_mpi) > 2");
			if (req_result)
			{
				setProperty("view-non-visit-items-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-non-visit-items-mpi", "");
			}
			rs.close();
			db.closeStatement();


			//Any patient with diagnosis
			description = "Query For Any Patient with diagnosis";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi " +
					"FROM w_patient WHERE pat_id IN " +
					"(SELECT pat_id " +
					"     FROM w_patient_dx " +
					"            WHERE " +
					"              dx_typ_cd IN " +
					"              (select code_id FROM p_code WHERE " +
					"      code_id IN (SELECT DISTINCT dx_typ_cd FROM w_patient_dx WHERE ROWNUM < 100000) " +
					"            AND LOWER(title) = 'problem') " +
					"AND sts_cd         ='137' " +
					"AND lifecycle_cd IN " +
					"(SELECT code_id FROM p_code WHERE code_set_id IN " +
					"    (SELECT code_set_id FROM p_code_set WHERE code_set_name = 'Life Cycle Status') " +
					"   AND authority_id = 'WELL_WELLOGIC' " +
					"   AND title = 'Active' " +
					"   AND delete_ind = 0)" +
					"   AND ROWNUM < 100000" +
					"   ) AND ROWNUM < 11");
			if (req_result)
			{
				setProperty("view-diagnosis-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-diagnosis-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with immunizations
			description = "Query for Any Patient with immunizations";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi " +
					"  FROM w_patient WHERE pat_id IN " +
					"    (SELECT pat_id " +
					"      FROM w_patient_rx " +
					"        WHERE rx_typ_cd IN " +
					"         (SELECT code_id FROM p_code WHERE code_id IN " +
					"  (SELECT DISTINCT rx_typ_cd FROM w_patient_rx) " +
					"     AND LOWER(title) = 'immunization') " +
					"     AND sts_cd ='137'" +
					"               AND ROWNUM < 100000)" +
					"               AND ROWNUM < 11");
			if (req_result)
			{
				setProperty("view-immunizations-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-immunizations-mpi", "");
			}
			rs.close();
			db.closeStatement();

			description = "Query For any patient with an emergency contact";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi FROM w_patient WHERE pat_id IN \n" +
					"  (SELECT pat_rel.pat_id   AS related_pat_id " +
					"    FROM w_patient pat " +
					"      JOIN w_device device " +
					"       ON (pat.crte_src_id = device.software_nm " +
					"       AND device.sts_cd   = '137' " +
					" AND device.dra_ind  = 1) " +
					"     JOIN w_patient_relationship pat_rel " +
					"ON pat_rel.entity_id = pat.pat_id " +
					"LEFT OUTER JOIN w_patient_alias pat_mrn " +
					"ON pat.pat_id            = pat_mrn.pat_id " +
					"AND pat_mrn.alias_typ_cd = '992' " +
					"  WHERE pat_rel.rltn_cd     <> '1159' " +
					"AND pat_rel.entity_id   IN " +
					"  (SELECT entity_id " +
					"  FROM w_patient_relationship " +
					"  WHERE rltn_cd   = '1159' " +
					"  ) " +
					"AND pat.sts_cd = '137' " +
					"AND ROWNUM <= 11)" +
					"  AND ROWNUM < 11");
			if (req_result)
			{
				setProperty("view-emergency-contacts-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-emergency-contacts-mpi", "");
			}
			rs.close();
			db.closeStatement();


			//Any patient with an insurance plan w_patient_plan aka Payors and Plans
			description = "Query For Any patient with an insurance plan w_patient_plan aka Payors and Plans";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi, nm_first, nm_last, lifecycle_cd " +
					"FROM w_patient WHERE pat_id IN " +
					"(SELECT pat_id FROM w_patient_plan)");
			if (req_result)
			{
				setProperty("view-insurance-plan-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-insurance-plan-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with labs
			description = "Query For Any Patient with labs";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi " +
					"FROM w_patient WHERE pat_id IN " +
					"(SELECT pat_id FROM w_patient_lab) ");
			if (req_result)
			{
				setProperty("view-laboratory-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-laboratory-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with vitals
			description = "Query for any patient with vitals";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi " +
					"FROM w_patient WHERE pat_id IN " +
					"(SELECT pat_id FROM p_patient_vital) "
				+ "AND STS_CD = '137'");
			if (req_result)
			{
				setProperty("view-vitals-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-vitals-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with diagnostic imaging
			description = "Query for Any patient with diagnostic imaging";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi " +
					"  FROM w_patient WHERE pat_id IN " +
					"   (SELECT pat_id FROM w_patient_procedure " +
					"    WHERE proc_typ_cd IN " +
					"     (SELECT code_id FROM p_code WHERE code_set_id IN " +
					"       (SELECT code_set_id FROM p_code_set WHERE " +
					"        LOWER(code_set_name)  = 'clinical event type') " +
					"      AND LOWER(title) = 'radiology')" +
					"            AND ROWNUM < 100000)");
			if (req_result)
			{
				setProperty("view-radialogy-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-radialogy-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with blood pressure
			//Any patient with multigraph data - in this case diastolic
			description = "Query for Any Patient with blood pressure";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi "
				+ "FROM w_patient WHERE pat_id IN "
				+ "(SELECT pat_id FROM p_patient_vital "
				+ "WHERE vital_type_code IN   (SELECT code_id FROM p_code WHERE LOWER(title) LIKE '%diastolic blood pressure%' AND authority_id = 'WELL_WELLOGIC' "
				+ "          AND vital_unit_type_code IN (SELECT code_id FROM p_code WHERE LOWER(title) = 'mm hg') "
				+ "          AND org_id IN (select org_id FROM w_organization WHERE org_nm IN (SELECT org_nm "
				+ "FROM w_organization "
				+ "WHERE org_id IN"
				+ "  (SELECT entity_id"
				+ "  FROM w_person_relationship"
				+ "  WHERE pers_id IN"
				+ "    (SELECT v_entity_id"
				+ "    FROM v_principal_entities"
				+ "    WHERE v_principal_id IN"
				+ "      ( SELECT principal_id FROM v_principal WHERE user_nm = '" + PropertiesSingleton.Instance.getHeaderProps().getProperty("username") + "'"
				+ "      )"
				+ "    )"
				+ "  )))"
				+ "          )) AND nm_last != 'LOADER-DEMO'");
			if (req_result)
			{
				setProperty("view-bloodpressure-mpi", rs.getString("pat_mpi"));
				setProperty("view-multigraph-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-bloodpressure-mpi", "");
				setProperty("view-multigraph-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//Any patient with multiple multigraph data (in this case body weight and height)
			description = "Query for any patient with multiple multigraph data - in this case body weight and height";
			logger.debug(description);
			req_result = runQuery("SELECT "
				+ "    pat_mpi "
				+ "FROM"
				+ "    w_patient "
				+ "WHERE "
				+ "    pat_id IN ( "
				+ "        SELECT "
				+ "            pat_id "
				+ "        FROM "
				+ "            p_patient_vital "
				+ "        WHERE "
				+ "            vital_type_code IN ( "
				+ "                SELECT "
				+ "                    code_id "
				+ "                FROM "
				+ "                    p_code "
				+ "                WHERE "
				+ "                    ("
				+ "                        LOWER(title) = 'body height'"
				+ "                        OR LOWER(title) = 'body weight measured'"
				+ "                    )"
				+ "                    AND authority_id = 'WELL_WELLOGIC'"
				+ "            )"
				+ "            AND ROWNUM < 100000"
				+ "        GROUP"
				+ "        BY"
				+ "            pat_id"
				+ "        HAVING"
				+ "            COUNT(pat_id) > 1"
				+ "    )"
				+ "    AND pat_id NOT IN ("
				+ "        SELECT PAT_ID"
				+ "        FROM"
				+ "            P_PATIENT_DATA_ERROR"
				+ "    )"
				+ "    AND opt_out_status IN ("
				+ "        SELECT"
				+ "            code_id"
				+ "        FROM"
				+ "            p_code"
				+ "        WHERE"
				+ "            authority_id = 'WELL_WELLOGIC'"
				+ "            AND LOWER(title) = 'opted in'"
				+ "    )"
				+ "    AND STS_CD = '137'");
			if (req_result)
			{
				setProperty("view-multigraph-single-and-multiple-mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("view-multigraph-single-and-multiple-mpi", "");
			}
			rs.close();
			db.closeStatement();

			//consulting physician code id
			description = "Query For consulting physician code id";
			logger.debug(description);
			req_result = runQuery("SELECT code_id FROM p_code WHERE code_set_id IN (SELECT code_set_id FROM p_code_set WHERE LOWER(code_set_name) = 'provider-patient relationship') " +
					"AND authority_id = 'WELL_WELLOGIC' " +
					"AND LOWER(title) = 'consulting physician' ");
			if (req_result)
			{
				setProperty("most-recently-viewed-codeId", rs.getString("code_id"));
			} else {
				setProperty("most-recently-viewed-codeId", "");
			}
			rs.close();
			db.closeStatement();

			//Any Community
			description = "Query For Any Community";
			logger.debug(description);
			req_result = runQuery("SELECT org_id FROM w_organization WHERE org_typ_cd IN " +
					"(SELECT code_id FROM p_code WHERE LOWER(title) = 'core service agency' and code_set_id IN " +
					"(SELECT code_set_id FROM p_code_set WHERE LOWER(code_set_name) = 'organization standard code')) " +
					"AND sts_cd = '137' ");

			String community_id = "";
			if (req_result)
			{
				community_id = rs.getString("org_id");
				setProperty("view-community-manager-community-id", community_id);
			} else {
				setProperty("view-community-manager-community-id", "");
			}
			rs.close();
			db.closeStatement();

			//Any organization within that Community
			description = "Query For Any organization with that Community";
			logger.debug(description);
			req_result = runQuery("SELECT w_organization.org_id " +
			  "FROM w_organization JOIN w_location_site " +
			  "ON W_ORGANIZATION.ORG_ID = W_LOCATION_SITE.ORG_ID " +
			  "WHERE w_organization.parent_org_id = '" + community_id + "' " +
			  "AND w_organization.sts_cd             = '137' " +
			  "AND w_organization.org_typ_cd IN ('934','971','972','973') " +
			  "AND w_location_site.is_main_facility = 1");

			String organization_id = "";
			if (req_result)
			{
				organization_id = rs.getString("org_id");
				setProperty("view-community-manager-organization-id", organization_id);
			} else {
				setProperty("view-community-manager-organization-id", "");
			}
			rs.close();
			db.closeStatement();

			//Any Facility within that organization
			description = "Query For Any Facility within that organization";
			logger.debug(description);
			req_result = runQuery("SELECT loc_site_id " +
					"FROM w_location_site " +
					"WHERE org_id    ='" + organization_id + "' " +
					"AND site_typ_cd IN (SELECT code_id FROM p_code WHERE code_set_id IN " +
					"(SELECT code_set_id FROM p_code_set WHERE LOWER(code_set_name) = 'site type') " +
					"AND LOWER(title) = 'facility' " +
					") " +
					"AND sts_cd      = '137' ");
			if (req_result)
			{
				setProperty("view-community-manager-facility-id", rs.getString("loc_site_id"));
			} else {
				setProperty("view-community-manager-facility-id", "");
			}
			rs.close();
			db.closeStatement();

			//Provider Role ID
			description = "Query For the Provider Role ID";
			logger.debug(description);
			req_result = runQuery("SELECT entity_role_id FROM w_entity_roles WHERE LOWER(role_name) = 'provider'");
			if (req_result)
			{
				setProperty("provider_id", rs.getString("entity_role_id"));
			} else {
				setProperty("provider_id", "");
			}
			rs.close();
			db.closeStatement();

			//Top 2 organizations with the most patients
			description = "Query For Top 2 organizations with the most patients";
			logger.debug(description);
			req_result = runQuery("SELECT v_org_id, COUNT(*) as \"counter\" FROM w_patient GROUP BY v_org_id ORDER BY \"counter\" desc");
			String mostPatientsOrgID1 = "";
			String mostPatientsOrgID2 = "";
			if (req_result)
			{
				mostPatientsOrgID1 = rs.getString("v_org_id");

				setProperty("most_patients_orgID1", mostPatientsOrgID1);
				if (rs.next())
				{
					mostPatientsOrgID2 = rs.getString("v_org_id");
					setProperty("most_patients_orgID2", mostPatientsOrgID2);
				} else {
					setProperty("most_patients_orgID2", "");
				}
			} else {
				setProperty("most_patients_orgID1", "");
			}

			String mostPatientOrgNM1 = "";
			if (!agnosticProps.getProperty("most_patients_orgID1").isEmpty()) {
				req_result = runQuery("SELECT org_nm, org_abbr, logo_resource_name FROM w_organization WHERE org_id = '" + agnosticProps.getProperty("most_patients_orgID1") + "'");
				mostPatientOrgNM1 = rs.getString("org_nm");
				if (req_result) {
					setProperty("most_patients_org_nm1", mostPatientOrgNM1);
					setProperty("most_patients_org_abbr1", rs.getString("org_abbr"));
					setProperty("most_patients_log_resource_name1", rs.getString("logo_resource_name") == null ? "" : rs.getString("logo_resource_name"));
				}
			}

			String mostPatientOrgNM2 = "";
			if (!agnosticProps.getProperty("most_patients_orgID2").isEmpty()) {
				req_result = runQuery("SELECT org_nm, org_abbr, logo_resource_name FROM w_organization where org_id = '" + agnosticProps.getProperty("most_patients_orgID2") + "'");

				if (req_result) {
					mostPatientOrgNM2 = rs.getString("org_nm");
					setProperty("most_patients_org_nm2", mostPatientOrgNM2);
					setProperty("most_patients_org_abbr2", rs.getString("org_abbr"));
					setProperty("most_patients_log_resource_name2", rs.getString("logo_resource_name") == null ? "" : rs.getString("logo_resource_name"));
				}
			}
			rs.close();
			db.closeStatement();

			//Get NHS number from most used organization
			description = "Query for getting NHS Number";
			logger.debug(description);


			req_result = runQuery("SELECT UPDT_SRC_ID, COUNT(*) as \"counter\" FROM W_PATIENT_ALIAS GROUP BY v_org_id ORDER BY \"counter\" desc");


			if(!agnosticProps.getProperty("most_patients_org_abbr1").isEmpty()) {
				req_result = runQuery("select crte_src_id, count(*) as \"counter\" from w_patient where crte_src_id not like '%WELLOGIC' group by crte_src_id order by \"counter\" desc");

				String sourceId = "";

				if (req_result) {
					sourceId = rs.getString("CRTE_SRC_ID");
				}

				if(!agnosticProps.getProperty("most_patients_org_abbr1").isEmpty())
				{
					req_result = runQuery("select * from W_PATIENT_ALIAS where ALIAS_TYP_CD IN "
						+ "                                    (select CODE_ID from P_CODE where upper(TITLE) = 'NHS NUMBER') "
						+ "AND"
						+ " CRTE_SRC_ID = '" + sourceId + "'");
					if (req_result)
					{
						setProperty("nhs_number", rs.getString("PAT_ALIAS"));
					}
				}
			}

			//Get a Provider from orgId2 that is not in OrgId1
			description = "Query for getting a provider from orgId2 that is not in OrgId1";
			logger.debug(description);

			req_result = runQuery("SELECT nm_last "
				+ "FROM w_person "
				+ "WHERE pers_id IN "
				+ "  (SELECT entity_id FROM v_principal "
				+ "  ) "
				+ "AND pers_id IN "
				+ "  (SELECT pers_id "
				+ "  FROM w_person_relationship "
				+ "  WHERE entity_id = '" + mostPatientsOrgID2 + "' "
				+ "  ) "
				+ "AND pers_id NOT IN "
				+ "  (SELECT pers_id "
				+ "  FROM w_person_relationship "
				+ "  WHERE entity_id = '" + mostPatientsOrgID1 + "' "
				+ "  ) "
				+ "AND ROWNUM < 10 "
				+ "AND nm_last != 'Demo'"
			);

			if (req_result)
			{
				setProperty("provider_from_orgID2", rs.getString("nm_last"));
			} else {
				setProperty("provider_from_orgID2", "");
			}
			rs.close();
			db.closeStatement();

			//Get a Patient to Search on
			description = "Query for getting a patient to search on";
			logger.debug(description);
			req_result = runQuery("SELECT pat_mpi, MIN(nm_first), MIN(nm_last), COUNT(*) FROM w_patient p JOIN w_patient_visit v " +
				"ON p.pat_id = v.pat_id " +
				"GROUP BY pat_mpi " +
				"ORDER BY COUNT(*) DESC");
			if (req_result)
			{
				setProperty("last_name_search", rs.getString("min(nm_last)"));
			} else {
				setProperty("last_name_search", "");
			}
			rs.close();
			db.closeStatement();

			//Get a Patient with Sensitive Data
			description = "Query for getting a patient with sensitive data";
			logger.debug(description);
			req_result = runQuery("SELECT PAT_MPI\n"
				+ "FROM W_PATIENT\n"
				+ "WHERE PAT_ID IN (SELECT PAT_ID\n"
				+ "                FROM W_PATIENT_PROCEDURE\n"
				+ "                WHERE CONFIDENTIALITY_CD IN (SELECT CODE_ID\n"
				+ "                     FROM P_CODE\n"
				+ "                          WHERE upper(TITLE) = 'SENSITIVE'))");

			if (req_result)
			{
				setProperty("sensitive_mpi", rs.getString("pat_mpi"));
			} else {
				setProperty("sensitive_mpi", "");
			}
			rs.close();
			db.closeStatement();

			FileOutputStream io = FileUtils.openOutputStream(new File("properties/environmentagnostic.properties"));
			PrintWriter pw = new PrintWriter(io);
			agnosticProps.list(pw);
			pw.close();
			io.close();

		} catch (Exception e) {
			throw e;

		} finally {
			endCreateHTML();
			db.closeStatement();
			db.closeConnection();
			SSHTunnel.Instance.closeTunnel();
		}
	}

	private void setProperty(String key, String value) {
		logger.debug("key: " + key + " value: " + value);
		agnosticProps.setProperty(key, value);
	}

	private boolean runQuery(String query) throws Exception {
		query = org.apache.commons.lang3.StringUtils.replacePattern(query,"[\\s]+"," ");
		logger.debug(query);
		addHTML(query);
		boolean result = false;
		try {
			rs = db.executeQuery(query);
			result = rs.next();
		} catch (SQLException e) {
			if (e.getMessage().contains("fetch out of sequence")) {
				logger.debug("This query returned 0 records: " + query);
			}
			else {
				throw e;
			}
		} catch (Exception e) {
			logger.debug("Inside runQuery Exception");
			logger.debug(e.toString());
		}
		finally {
			return result;
		}

	}

	private void startCreateHTML() throws Exception {
		FileUtils.writeStringToFile(sqlTable,
			"<html>\n"+
  				"  <title></title>\n" +
  				"  <body>\n" +
				"    <style>\n" +
				"      table,th,td\n" +
				"      {\n" +
				"        border:1px solid black;\n" +
				"        border-collapse:collapse;\n" +
				"        text-align:left;\n" +
				"        vertical-align: text-top;\n" +
				"        margin: 20px;\n" +
				"        padding: 5px;\n" +
				"      }\n" +
				"      th\n" +
				"    {\n" +
				"      background-color: #f1f1f1;\n" +
				"    }\n" +
				"    </style>\n");
	}

	private void addHTML(String query) throws Exception {
		FileUtils.writeStringToFile(sqlTable,
			"    <table style=\"width:1000px\">\n"
			    +"     <tr>\n"
				+ "      <th>Description</th>\n"
				+ "      <th>SQL</th>\n"
				+ "    </tr>\n"
				+ "    <tr>\n"
				+ "      <td>" + description + "</td>\n"
				+ "      <td><pre>"
				+ formatter.format(query)
				+ "</pre></td>\n"
				+ "      </tr>\n"
				+ "    </table>\n"
			,true);
	}

	private void endCreateHTML() throws Exception {
		FileUtils.writeStringToFile(sqlTable,
			"  </body>\n" +
				"</html>",true);
	}
}