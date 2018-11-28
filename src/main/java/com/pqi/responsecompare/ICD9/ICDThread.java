package com.pqi.responsecompare.ICD9;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import com.pqi.responsecompare.sql.OracleDbManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class ICDThread implements Runnable
{
    private static final Logger logger = Logger.getLogger(ICDThread.class);
    private boolean found = false;
    private List<String> icdList = null;
    private ResultSet rs = null;
    private String errors = "";
    private boolean finalResult = true;
    private List<String> errorList = new ArrayList<String>();
    private OracleDbManager db = null;

    public ICDThread(List<String> icdListParam)
    {
        icdList = icdListParam;

        try
        {
            db = new OracleDbManager(PropertiesSingleton.Instance.getProps());

            runQuery("alter session set current_schema=" + PropertiesSingleton.Instance.getProperty("database.schema"));
        }
        catch (Exception e) {
            //e.printStackTrace();
            logger.debug(e.getMessage());
        }

    }

    public String getErrors() {
        return errors;
    }

    public void run()
    {

        boolean result = false;
        String ICDCode = "";
        String MNEMONIC = "";
        String CODE_ID = "";
        String query = "";
        String CODE_SET_ID = "";
        String ICD_Chronic_Flag = "";
        String pcode_query = "";
        String chronic_query = "";
        String unique_query;
        String flag_result = "";

        try
        {
            int counter = 0;
            for (String line : icdList)
            {
                counter++;
                logger.debug(Integer.valueOf(counter) + " line: "    + line);
                if (line.toLowerCase().contains("new codes are introduce"))
                {
                    continue;
                }
                if (line.toLowerCase().contains("icd-9-cm code"))
                {
                    continue;
                }
                String[] splitLine = line.split(",");
                ICDCode = splitLine[0].replace("'", "").trim();
                ICD_Chronic_Flag = splitLine[2].replace("'", "").trim();
                if (ICDCode.length() > 3)
                {
                    ICDCode = StringUtils.left(ICDCode, 3) + "." + StringUtils.substring(ICDCode, 3);
                }
                pcode_query =
                    "SELECT * FROM P_CODE WHERE MNEMONIC = '" + ICDCode + "' AND CODE_ID = '" + ICDCode
                        + "_-2001' AND CODE_SET_ID = '-2001' AND AUTHORITY_ID = 'ICD-9' AND DELETE_IND = 0";
                //logger.debug("pcode_query: " + pcode_query);
                runQuery(pcode_query);
                if (!found)
                {
                    logger.debug(query);
                    logger.debug("MNEMONIC " + ICDCode
                        + " does not exist in the P_CODE Table or CODE_ID does not contain MNEMONIC or CODE_SET_ID is not set to -2001 or authority is not ICD9");
                    result = false;
                    finalResult = false;
                    errorList.add(query + "\nMNEMONIC " + ICDCode
                        + " does not exist in the P_CODE Table or CODE_ID does not contain MNEMONIC or CODE_SET_ID is not set to -2001 or authority is not ICD9");
                    rs.close();
                    db.closeStatement();

                }
                else
                {
                    CODE_ID = rs.getString("CODE_ID");
                    CODE_SET_ID = rs.getString("CODE_SET_ID");
                    rs.close();
                    //db.closeStatement();
                    unique_query = "SELECT COUNT(*) FROM P_CODE WHERE MNEMONIC = '" + ICDCode
                        + "' AND DELETE_IND = 0 and AUTHORITY_ID = 'ICD-9'";
                    runQuery(unique_query);
                    if (rs.getInt(1) > 1)
                    {
                        errorList.add(query + "\nThere are duplicate ICD-9 mnemonics in the P_CODE table");
                    }
                    rs.close();
                    //db.closeStatement();
                    chronic_query = "SELECT * FROM P_CODE_CHRONIC_INDICATOR WHERE CODE_ID = '" + CODE_ID + "'";
                    //logger.debug("chronic_query: " + chronic_query);
                    runQuery(chronic_query);

                    if (found)
                    {
                        result = ICD_Chronic_Flag.equals("1");
                        flag_result = "Record appeared in P_CODE_CHRONIC_INDICATOR table";
                    }
                    else
                    {
                        result = ICD_Chronic_Flag.equals("0");
                        flag_result = "Record did not appear in P_CODE_CHRONIC_INDICATOR table";
                    }
                    if (result)
                    {
                        assertTrue(chronic_query + " line: " + line + "CODE_ID: " + CODE_ID, result);
                    }
                    else
                    {
                        errorList.add("pcode_query: " + pcode_query + " line: " + line + "CODE_ID: " + CODE_ID
                            + " Chronic Flag did not match up");
                        errorList
                            .add("chronic_query: " + chronic_query + " line: " + line + "CODE_ID: " + CODE_ID
                                + " Chronic Flag did not match up");
                        errorList.add("chronic query result: " + flag_result);

                        finalResult = false;
                    }
                    rs.close();
                    //db.closeStatement();
                }
                rs.close();
                db.closeStatement();
                chronic_query = "";
                pcode_query = "";
                CODE_ID = "";
                CODE_SET_ID = "";
            }

            if (!finalResult)
            {
                for (String error : errorList)
                {
                    errors = errors + error + "\n";
                }

                assertTrue(errors, finalResult);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (errors.isEmpty())
            {
                errors = "All Tests Passed.";
            }
        }
    }


    private ResultSet runQuery(String query) throws Exception
    {
        rs = db.executeQuery(query);
        try
        {
            found = rs.next();
        }
        catch (SQLException e)
        {
            if (e.getMessage().contains("fetch out of sequence"))
            {
                return rs;
            }
            else
            {
                throw e;
            }
        }
        catch (Exception e)
        {
            logger.debug(e.getMessage());
        }
        return rs;
    }
}
