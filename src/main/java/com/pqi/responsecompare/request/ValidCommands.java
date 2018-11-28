package com.pqi.responsecompare.request;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

public enum ValidCommands {

    Instance;

    static Logger logger = Logger.getLogger(ValidCommands.class);

    ArrayList<String> allValidCommands;
    ValidCommands() {
        allValidCommands = new ArrayList<String>(
                Arrays.asList(
                        "ADD_ROLE_TO_USER_FROM_CSV", "ASSIGN","BODY","BASIC_AUTHORIZATION",
                        "BODY_FILE", "COMPARE_TWO_SQL_STATEMENTS","COMPARE_VARIABLES", "CREATE_AUTOMATION_ROLE",
                        "CREATE_PATIENT_PORTAL_USER", "CREATE_AGNOSTIC_PROPERTIES",
                        "CREATE_JSON_FOR_ROLE", "CUSTOM_MAP", "CREATE_PATIENTS_FROM_CSV",
                        "CREATE_USERS", "EXPECT_ERROR", "EDI_TO_TALEND",
                        "EDI_TO_MYSQL","GENERATE_GUID",
                        "GET", "GET_CONFIRMATION_TOKEN", "GET_IMAGE", "GET_IMAGES",
                        "GET_WAPI_IMAGE", "GET_ADROTATION",
                        // "SAMPLE_KEYWORD",
                        "GETLIST_JASON", "GET_MINI","GET_WITH_AUTHCACHE","IGNORE",
                        "IGNORE_GLOBAL_HEADERS","INCREMENT_PATIENT_NUMBER","LOG","OPEN_BUG",
                        "RUN_PL_SQL","POST_IMAGE", "POST_IMAGES", "POST","RUN_ORACLE_SQL","RUN_SQL_FROM_FILE",
                        "RUN_SQLSERVER_SQL","RUN_SQLSERVER_EXECUTE_SQL","RUN_ORACLESERVER_EXECUTE_SQL",
                        "RUN_MYSQLSERVER_EXECUTE_SQL",
                        "RUN_TERADATA_EXECUTE_SQL","POST_MULTIPART","POST_WITH_AUTHCACHE",
                        "REMOTE_SHELL","RELOAD_ENVIRONMENT_AGNOSTIC_PROPERTIES","JAVASCRIPT",
                        "POST_WITH_FILE_BODY","POST_XML_SUBS", "PUT", "SET_DATETIME",
                        "VALIDATE_HEADER","TESTRAIL", "VALIDATE_HEADER_SET",
                        "VALIDATE_HEADER_NOTEXIST", "VALIDATE_IMAGE", "VALIDATE_IMAGES",
                        "TRANSFORM_RESPONSE_FILE","VALIDATE_RAW_RESPONSE",
                        "VALIDATE_STATUS_CODE", "SET_HEADER","COMPARE", "STATUS",
                        "VALIDATE_DATA", "VERIFY_ICD","VERIFY_RESPONSE_TEXT",
                        "VERIFY_MAP_VALUE","WAIT_FOR_DATA"));

    }

    public ArrayList<String> getAllValidCommands() {
        return allValidCommands;
    }
}
