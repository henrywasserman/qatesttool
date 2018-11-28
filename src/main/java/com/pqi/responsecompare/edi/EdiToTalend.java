package com.pqi.responsecompare.edi;

import com.pqi.responsecompare.configuration.PropertiesSingleton;


import com.pqi.responsecompare.edi.mp.*;
import com.pqi.responsecompare.edi.mp.enums.Maps;


import com.pqi.responsecompare.edi.mp.enums.ISO639;
import com.pqi.responsecompare.request.Request;
import com.pqi.responsecompare.request.TestCase;
import com.pqi.responsecompare.sql.SqlServerDbManager;
import com.pqi.responsecompare.tail.TailManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.payload.StringResult;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class EdiToTalend extends Request {

    static final Logger logger = Logger.getLogger(com.pqi.responsecompare.edi.EdiToTalend.class);

    private void resetResults() {
        for (String key:Maps.Instance.ediResults.keySet()) {
            Maps.Instance.ediResults.put(key, "null");
        }
        Maps.Instance.ediResults.put("x_primary_language_written","English");
        Maps.Instance.ediResults.put("x_primary_language_spoken","English");
         Maps.Instance.ediResults.put("x_confidentiality_code","good cause");
        Maps.Instance.ediResults.put("x_plan_end_date","2299-12-31 00:00:00.0");
    }

    private String formatDate(String date, String oldFormat, String newFormat) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
        Date d = sdf.parse(date);
        sdf.applyPattern(newFormat);
        return sdf.format(d);
    }

    public EdiToTalend(TestCase test) throws Exception {
        super(test);
    }

    public EdiToTalend() throws Exception {

    }

    public void sendRequest() throws Exception {
        CloseableHttpResponse response = null;
        Smooks smooks = null;

        String table = PropertiesSingleton.Instance.getProperty("talend_table");

        props.setProperty("mssql-host", "SRVMLBSQLT05\\Talend");
        props.setProperty("mssql-dbname", "tmdm_611_master");
        SqlServerDbManager dbManager = new SqlServerDbManager(props);
        ResultSet resultSet = null;
        String query = "SELECT TOP 1 x_filename FROM dbo." + table + " ORDER BY x_file_create_date DESC";
        logger.info(query);
        resultSet = dbManager.executeQuery(query);
        String filename = "";
        while (resultSet.next()) {
            filename = resultSet.getObject("x_filename").toString();
        }

        if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
            TailManager.Instance.setStartLogLineCount();
        }

        try {
            StringBuilder json = new StringBuilder();
            logger.info("TestID: " + test.getTestCaseID());

            if (url.isEmpty()) {
                url = "\\\\cca-fs1\\groups\\System\\EIX\\DEV\\834\\Automation\\Inbound\\.camel\\" + filename + ".txt";

            }

            logger.info("Edi File: " + url);

            byte[] messageIn = StreamUtils.readStream(new FileInputStream(url));
            StringResult result = new StringResult();

            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(new Locale("en", "IE"));

            // Instantiate Smooks with the config...
            smooks = new Smooks("src/main/resources/smooks-config.xml");

            ExecutionContext executionContext = smooks.createExecutionContext();
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), result);

            FileUtils.writeStringToFile(new File("cca-mp-output.xml"),result.toString());

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<EdiMpCssType> unmarshalledObject =
                    (JAXBElement<EdiMpCssType>)unmarshaller.unmarshal(new File("cca-mp-output.xml"));

            EdiMpCssType ediMpCss = unmarshalledObject.getValue();

            ArrayList<INSITEMSType> insItems = (ArrayList<INSITEMSType>)ediMpCss.getINSITEMS();

            FileUtils.writeStringToFile(new File("data\\consult\\mp\\edi_testcases.req"),
                    "#EDI TESTS\n",false);
            StringBuilder sql = new StringBuilder();
            String key = "";

            dbManager = new SqlServerDbManager(props);
            dbManager.getConnection();
            ResultSet rs = dbManager.executeQuery("SELECT COLUMN_NAME FROM" +
                    " INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table + "'");

            String ignore = "";
            if(test.getCurrentParsedRequest().getIgnore()) {
                ignore = test.getCurrentParsedRequest().getIgnoreString();
            }

            StringBuilder sb = new StringBuilder();
            while(rs.next()) {
                if (!ignore.isEmpty()
                        && !ignore.contains((String) rs.getObject((1)))) {
                    sb.append(rs.getObject(1) + ",");
                }
            }
            dbManager.closeConnection();

            //Delete the last comma
            sb.deleteCharAt(sb.lastIndexOf(","));

            String [] columns = getColumns(table);
            int counter = 0;
            for (INSITEMSType insItem:insItems) {
                counter++;
                List<Object> insOrRefOrDtp = insItem.getINSOrREFOrDTP();
                for (Object object : insOrRefOrDtp) {
                    if (object instanceof NM1Type) {
                        NM1Type nm1 = (NM1Type) object;
                        assignEDIResults("x_last_name",nm1.getNameLastOrOrganizationName());
                        assignEDIResults("x_ssn",nm1.getIdentificationCode());
                        key = nm1.getEntityIDCode();
                        Maps.Instance.ediResults.put("x_entity_identification_code", "IL");


                        if (!nm1.getEntityIDCode().equals("70")) {
                            assignEDIResults("x_first_name", nm1.getNameFirst());
                            assignEDIResults("x_middle_name", nm1.getNameMiddle());
                        }
                        key = nm1.getEntityTypeQualifier();
                        if (Maps.Instance.entityTypeQualifier.containsKey(key)) {
                            Maps.Instance.ediResults.put("x_entity_type_qualifier",
                                    Maps.Instance.entityTypeQualifier.get(nm1.getEntityTypeQualifier()));
                        } else {
                            Maps.Instance.ediResults.put("x_entity_type_qualifier",
                                    key + " not found in entityTypeQualifier hash");
                        }
                    }
                    if (object instanceof DMGType) {
                        DMGType dmg = (DMGType) object;
                        if (dmg.getDateTimePeriod()!= null) {
                            Maps.Instance.ediResults.put("x_dob",
                                    formatDate(dmg.getDateTimePeriod(),
                                            "yyyyMMdd", "yyyy-MM-dd 00:00:00.0"));
                        }

                        assignEDIResults("x_gender_code", dmg.getGenderCode());
                        key = dmg.getRaceOrEthnicityCode();
                        if (Maps.Instance.raceOrEthnicity.containsKey(key) &&
                                (Maps.Instance.ediResults.get("x_race_or_ethnicity_code").equals("null"))
                                || Maps.Instance.ediResults.get("x_race_or_ethnicity_code").equals("Unknown")) {
                            Maps.Instance.ediResults.put("x_race_or_ethnicity_code",
                                    Maps.Instance.raceOrEthnicity.get(key));
                        }

                    }
                    if (object instanceof LUIType) {
                        LUIType lui = (LUIType) object;
                        key = lui.getIdentificationCode().toLowerCase();
                        if (lui.getUseOfLanguageIndicator().equals("5")) {
                            if (key.equals("UND")) {
                                Maps.Instance.ediResults.put("x_primary_language_written",
                                    "Undetermined");
                            } else {
                                Maps.Instance.ediResults.put("x_primary_language_written",
                                        ISO639.Instance.getISO639(key));
                            }
                        }

                        if (lui.getUseOfLanguageIndicator().equals("6")) {
                            Maps.Instance.ediResults.put("x_primary_language_written",
                                    ISO639.Instance.getISO639(key));
                        } else {
                            Maps.Instance.ediResults.put("x_primary_language_written",
                                    ISO639.Instance.getISO639(key));
                        }

                        if (lui.getUseOfLanguageIndicator().equals("7")) {
                            Maps.Instance.ediResults.put("x_primary_language_spoken",
                                  ISO639.Instance.getISO639(key));
                        }
                        if (lui.getUseOfLanguageIndicator().equals("8")) {
                            Maps.Instance.ediResults.put("x_primary_language_spoken",
                                  ISO639.Instance.getISO639(key));
                        }
                    }

                    if (object instanceof INSType) {
                        INSType ins = (INSType) object;
                        //ediResults.put("x_handicap_indicator", ins.getYesNoConditionOrResponseCode()
                        Maps.Instance.ediResults.put("x_handicap_indicator", "Y");
                    }

                    if (object instanceof REFType) {
                        REFType ref = (REFType) object;
                        String id = ref.getReferenceIdentificationQualifier();
                        if (id.equals("0F")) {
                            Maps.Instance.ediResults.put("x_mass_health_id", ref.getReferenceIdentification());
                        } else if (id.equals("F6")) {
                            Maps.Instance.ediResults.put("x_hic_number", ref.getReferenceIdentification());
                        } else if (id.equals("DX")) {
                            Maps.Instance.ediResults.put("x_enrolled_by", ref.getReferenceIdentification()
                                    .replaceAll("[\\d]+([\\w|\\d]+)", "$1"));
                        }
                    }

                    if (object instanceof DTPType) {
                        DTPType dtp = (DTPType) object;
                        key = dtp.getDateTimeQualifier();
                        if (Maps.Instance.dateTimeQualifier.containsKey(key)) {
                            if (key.equals("357")) {
                                Maps.Instance.ediResults.put("x_plan_end_date",
                                        dtp.getDateTimePeriodFormat());
                            } else if (key.equals("356")) {
//                                Maps.Instance.ediResults.put("x_plan_start_date",
//                                            dtp.getDateTimePeriodFormat());
                                Maps.Instance.ediResults.put("x_plan_start_date",
                                        "20181201");
                            } else if (key.equals("007")) {
                                Maps.Instance.ediResults.put("x_file_create_date",
                                        dtp.getDateTimePeriodFormat());
                            } else if (key.equals("348")) {
                                //todo
                            }
                        }
                    }

                    if (object instanceof HDType) {
                        HDType hd = (HDType) object;
                        key = hd.getMaintenanceTypeCode();
                        if (Maps.Instance.maintenanceTypeCode.containsKey(key)) {
                            Maps.Instance.ediResults.put("x_maintenance_type_code",
                                    Maps.Instance.maintenanceTypeCode.get(key));
                        }
                        Maps.Instance.ediResults.put("x_rating_category",hd.getCoverageLevel());
                    }
                }

                Maps.Instance.ediResults.put("x_filename",filename);

                sql.append("TESTCASE " + Maps.Instance.ediResults.get("x_last_name") +
                        Maps.Instance.ediResults.get("x_ssn") +
                        Maps.Instance.ediResults.get("x_plan_start_date") +", compare " +
                        Maps.Instance.ediResults.get("x_last_name") +
                        Maps.Instance.ediResults.get("x_ssn") +
                        " against expected ediResults from edi file\n" +
                        "  RUN_SQLSERVER_EXECUTE_SQL SRVMLBSQLT05\\Talend, tmdm_611_master\n" +
                        "  SELECT TOP 1 " + sb.toString()  +
                        "  FROM dbo." +  table +
                        "  WHERE x_last_name = '" +
                        Maps.Instance.ediResults.get("x_last_name") + "' " +
                        "  AND x_ssn = '" +
                        Maps.Instance.ediResults.get("x_ssn") + "' " +
                        "  AND x_dob = '" +
                        Maps.Instance.ediResults.get("x_dob") + "' " +
                        "  AND x_plan_start_date = '" +
                        Maps.Instance.ediResults.get("x_plan_start_date") + "' " +
                        "  AND x_plan_end_date = '" +
                        Maps.Instance.ediResults.get("x_plan_end_date") + "'" +
                        //"  AND x_filename = '" + filename + "'" +
                        "  ORDER BY x_file_create_date DESC\n" +
                        "  COMPARE\n");

                if (test.getCurrentParsedRequest().getIgnore()) {
                    sql.append("  IGNORE " +
                            test.getCurrentParsedRequest().getIgnoreString() + "\n");
                }

                sql.append("\n");

                FileUtils.writeStringToFile(new File("data\\consult\\mp\\edi_testcases.req"),
                        sql.toString(), true);

                json.append("{");

                for (String column:columns) {
                    json.append("\"" + column + "\":[\"" +
                            Maps.Instance.ediResults.get(column) + "\"],\n");
                }

                json.deleteCharAt(json.lastIndexOf("\n"));
                json.deleteCharAt(json.lastIndexOf(","));

                json.append("}");

                FileUtils.writeStringToFile(new File
                    ("data\\consult\\mp\\expectedresponse\\" +
                            Maps.Instance.ediResults.get("x_last_name") +
                            Maps.Instance.ediResults.get("x_ssn") +
                            Maps.Instance.ediResults.get("x_plan_start_date") + "_0.json")
                        ,json.toString());

                resetResults();
                json.setLength(0);
                sql.setLength(0);
            }

            logger.debug("Executing...");

            if (PropertiesSingleton.Instance.getProperty("tail").toLowerCase().equals("true")) {
                logger.debug("Here is tail: " + TailManager.Instance.getTail());
                FileUtils.writeStringToFile(new File(logoutputfile), TailManager.Instance.getTail(), false);
            }

            logger.debug("Finished executing get");

        } catch (AssertionError ae) {
            ae.printStackTrace();
            throw ae;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getCause().toString());
            throw e;
        } finally {
            if (smooks != null) {
                smooks.close();
            }
            if (response != null) {
                response.close();
            }
            if (test_request_counter + 1 == test.getRequests().size()) {
                //test.httpClientClose();
            }
        }
    }

    private String [] getColumns(String table) throws Exception {
        SqlServerDbManager dbManager = new SqlServerDbManager(props);
        dbManager.getConnection();
        ResultSet rs = dbManager.executeQuery("SELECT COLUMN_NAME FROM" +
                " INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table + "'");

        String ignore = "";
        if(test.getCurrentParsedRequest().getIgnore()) {
            ignore = test.getCurrentParsedRequest().getIgnoreString();
        }

        StringBuilder sb = new StringBuilder();
        while(rs.next()) {
            if (!ignore.isEmpty()
                    && !ignore.contains((String) rs.getObject((1)))) {
                sb.append(rs.getObject(1) + ",");
            }
        }
        dbManager.closeConnection();

        //Delete the last comma
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString().split(",");
    }

    private void assignEDIResults(String key, String value) {
        if (value != null) {
            Maps.Instance.ediResults.put(key, value);
        }
    }
}