package com.pqi.responsecompare.sql;

import com.pqi.responsecompare.reports.CreateOutput;
import com.pqi.responsecompare.configuration.PropertiesSingleton;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public enum SQLResultsToOracle {

    Instance;

    static Logger logger = Logger.getLogger(SQLResultsToOracle.class);

    private ArrayList<String> columnNames = null;
    private ArrayList<HashMap> sqlMapArray = null;
    private String column = "";
    private String createTableString = "";

    private SQLResultsToOracle() {
        Properties responseCompare = PropertiesSingleton.Instance.getProps();
        columnNames = new ArrayList<String>();
        sqlMapArray = new ArrayList<HashMap>();
        //combineMaps(new HashMap<String,Object>((Map)responseCompare));
    }

    public void dropTable(String tableName) {
        //Make call to dropTable
    }

    public void cleanSQLMapArray() {sqlMapArray = new ArrayList<HashMap>(); }

    public void insertTableRows(ResultSet rs, String tableName) throws Exception{
        if (rs == null) { return; }
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        columnNames = new ArrayList<String>();
        int counter = 1;
        String columnName = "";
        for (int i = 1; i <= numberOfColumns; i++) {
            columnName = rsmd.getColumnName(i);
            if (columnName.isEmpty()) {
                columnName = "BlankColumnName" + Integer.toString(counter);
                counter++;
            }
            columnNames.add(columnName);
        }

        OracleTableManager.Instance.createTable(tableName);

        int maxRows = 1000000;
        int rows = 0;
        counter = 1;

        columnNames = new ArrayList<String>();
    }

    private void createTable(String tableName) {
        StringBuffer createTableString = new StringBuffer("CREATE TABLE " + tableName + " ( ");
        int totalColumns = columnNames.size();
        int columnCounter = 0;
        for (String columnName : columnNames) {
            columnCounter++;
            if (columnCounter < totalColumns) {
                createTableString.append(columnName + " VARCHAR2(4000), ");
            } else {
                createTableString.append(columnName + " VARCHAR2(4000) )");
            }
        }

        this.createTableString = createTableString.toString();

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

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public void combineMaps(HashMap<String,Object> source) {

    }
}