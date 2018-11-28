package com.pqi.responsecompare.sql;

import com.pqi.responsecompare.reports.CreateOutput;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public enum SQLToMap {

    Instance;

    static Logger logger = Logger.getLogger(SQLToMap.class);

    private HashMap<String,ArrayList<String>> sqlMap = null;
    private ArrayList<HashMap> sqlMapArray = null;
    private String column = "";

    private SQLToMap() {
        Properties responseCompare = PropertiesSingleton.Instance.getProps();
        sqlMap = new HashMap<String,ArrayList<String>>();
        sqlMapArray = new ArrayList<HashMap>();
        //combineMaps(new HashMap<String,Object>((Map)responseCompare));
    }

    public void cleanSQLMap() {
        sqlMap = new HashMap<String,ArrayList<String>>();
    }

    public void cleanSQLMapArray() {sqlMapArray = new ArrayList<HashMap>(); }

    public void appendMap(ResultSet rs) throws Exception{
        if (rs == null) { return; }
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        sqlMap.put("COLUMN_NAMES",new ArrayList<String>());
        int counter = 1;
        String columnName = "";
        for (int i = 1; i <= numberOfColumns; i++) {
            columnName = rsmd.getColumnName(i);
            if (columnName.isEmpty()) {
                columnName = "BlankColumnName" + Integer.toString(counter);
                counter++;
            }
            sqlMap.get("COLUMN_NAMES").add(columnName);
        }

        int maxRows = 50000;
        int rows = 0;
        counter = 1;
        while (rs.next() && rows <= maxRows) {
            rows++;
            for (int i = 1; i <= numberOfColumns; i++) {
                columnName = rsmd.getColumnName(i);
                if (columnName.isEmpty()) {
                    columnName = "BlankColumnName" + Integer.toString(counter);
                    counter++;
                }

                if (sqlMap.containsKey(columnName)){
                    if (rs.getObject(rsmd.getColumnName(i)) == null) {
                        sqlMap.get(columnName).add("null");
                    } else {
                        sqlMap.get(columnName).add(
                                rs.getObject(rsmd.getColumnName(i)).toString()
                                        .replaceAll("\"","").toLowerCase()
                                        .replaceAll("\n","").toLowerCase()
                                        .replaceAll("\r","")
                                        .replaceAll("\\\\",""));
                    }
                } else {
                    sqlMap.put(columnName,new ArrayList<String>());
                    if (rs.getObject(rsmd.getColumnName(i)) == null) {
                        sqlMap.get(columnName).add("null");
                    } else {
                        /*
                        if (System.getProperty("EDI") == null) {
                            sqlMap.get(columnName).add(
                                    rs.getObject(i).toString()
                                            .replaceAll("\"", "").toLowerCase()
                                            .replaceAll("\n", "")
                                            .replaceAll("\r", "")
                                            .replaceAll("\\\\", ""));
                        } else { */
                            sqlMap.get(columnName).add(
                                    rs.getObject(i).toString()
                                            .replaceAll("\"", "")
                                            .replaceAll("\n", "")
                                            .replaceAll("\r", "")
                                            .replaceAll("\\\\", ""));
                        //}
                    }
                }
            }
        }

        sqlMapArray.add(sqlMap);
        sqlMap = new HashMap<String,ArrayList<String>>();

    }

    public String getSQLHtml(int i) throws Exception {
        if (sqlMapArray.size() > 0 ) {
            return CreateOutput.Instance.sqlMapToHTML(sqlMapArray.get(i));
        } else {
            return "<html><body>SQL response was empty</body></html>";
        }
    }

    public String getSqlJSON(int i) throws Exception {
        if (sqlMapArray.size() > 0) {
            return CreateOutput.Instance.sqlMapToJSON(sqlMapArray.get(i));
        } else {
            return "{\"SQL RESPONSE WAS EMPTY\":0}";
        }
    }

    public HashMap<String,ArrayList<String>> getSqlMap() {
        return sqlMap;
    }

    public void combineMaps(HashMap<String,Object> source) {

    }
}
