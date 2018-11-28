package com.pqi.responsecompare.sql;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Properties;

public class TeradataDbManager extends DatabaseManager {
	static Logger logger = Logger.getLogger(TeradataDbManager.class);
	public TeradataDbManager(Properties props) throws Exception {
		super(props);

	}
	public Connection getConnection() throws Exception {
		Connection conn = null;
		try {

			BasicDataSource ds = new BasicDataSource();
			Class.forName("com.teradata.jdbc.TeraDriver");
			String dbURL = properties.getProperty("teradata-url");
			String user = properties.getProperty("teradata-user");
			String teradataHost = properties.getProperty("teradata-host");
			//String test = Base64.encodeBase64String(properties.getProperty("Password.ORACLE").getBytes());
			//String pass = Base64.decodeBase64(properties.getProperty("Password.ORACLE").getBytes()).toString();

            String pass = new String(Base64.getDecoder().decode(properties.getProperty("teradata-password").getBytes()));

            String connectionString = MessageFormat.format(dbURL, teradataHost);

			ds.setInitialSize(20);
			ds.setDriverClassName("com.teradata.jdbc.TeraDriver");
			ds.setUrl(connectionString);
			ds.setPassword(pass);
			ds.setUsername(user);

			logger.debug("dbURL: " + dbURL);
			logger.debug("user: " + user);

			//logger.debug("Starting SSH Tunnel");
			//SSHTunnel.Instance.openTunnel();
			//logger.debug("Finished Opening Tunnel");
			conn = DriverManager.getConnection(connectionString, user, pass);
			//conn = ds.getConnection();
			logger.debug("Got a connection");
			logger.debug(conn.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());
		}
	   return conn;
	}
	public java.sql.Timestamp getDatabaseTimeStamp() throws SQLException, Exception {

		String sql = "SELECT SYSTIMESTAMP FROM DUAL";
		ResultSet resultSet = this.executeQuery(sql);

		java.sql.Timestamp currTime = null;

		while (resultSet.next()) {
			currTime = resultSet.getTimestamp(1);
		}
		return currTime;
	}


	//public void closeSSHTunnel() {
		//SSHTunnel.Instance.closeTunnel();
	//}
}