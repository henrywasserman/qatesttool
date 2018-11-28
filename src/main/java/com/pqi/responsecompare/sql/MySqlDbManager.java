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

public class MySqlDbManager extends DatabaseManager {
	static Logger logger = Logger.getLogger(MySqlDbManager.class);
	public MySqlDbManager(Properties props) throws Exception {
		super(props);

	}
	public Connection getConnection() throws Exception {
		Connection conn = null;
		try {

			BasicDataSource ds = new BasicDataSource();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String dbURL = properties.getProperty("URL.MYSQL");
			String user = properties.getProperty("UserId.MYSQL");
			String host = properties.getProperty("Host.MYSQL");
			String port = properties.getProperty("Port.MYSQL");
			String database = properties.getProperty("database.MYSQL");


            String pass = new String(Base64.getDecoder().decode(properties.getProperty("Password.MYSQL").getBytes()));

            String connectionString = MessageFormat.format(dbURL,
                    host,port,database);


            logger.debug("dbURL: " + dbURL);
			logger.debug("user: " + user);
			//logger.debug("pass: " + pass);
			ds.setInitialSize(20);
			ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUrl(dbURL);
			ds.setPassword(pass);
			ds.setUsername(user);

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