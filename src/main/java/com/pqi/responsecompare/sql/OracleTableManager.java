package com.pqi.responsecompare.sql;

import com.pqi.responsecompare.configuration.PropertiesSingleton;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.Properties;

public enum OracleTableManager {

    Instance;

    static Logger logger = Logger.getLogger(OracleTableManager.class);

    private Properties properties = null;
    private OracleTableManager() {
        properties = PropertiesSingleton.Instance.getProps();
        //combineMaps(new HashMap<String,Object>((Map)responseCompare));
    }

    public void createTable(String tableName) throws Exception {

        properties.setProperty("Oracle.Server",
                PropertiesSingleton.Instance.getProperty("oracle.compare.server"));

        properties.setProperty("Oracle.port",
                PropertiesSingleton.Instance.getProperty("oracle.compare.port"));

        properties.setProperty("Oracle.Schema",
                PropertiesSingleton.Instance.getProperty("oracle.compare.sid"));

        properties.setProperty("UserID.ORACLE",
                PropertiesSingleton.Instance.getProperty("oracle.compare.user"));

        properties.setProperty("Password.ORACLE",
                PropertiesSingleton.Instance.getProperty("oracle.compare.password"));

        OracleDbManager db = new OracleDbManager(properties);
        db.getConnection();
        db.closeConnection();
    }

    public void dropTable(String tableName) {
        //Make call to dropTable
    }

    public void insertTableRows(ResultSet rs, String tableName) throws Exception{
    }
}