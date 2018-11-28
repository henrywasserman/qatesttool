package com.pqi.responsecompare.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

public class SqlServerDbManager extends DatabaseManager {

	public SqlServerDbManager(Properties props) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {
		super(props);
	}

	public java.sql.Timestamp getDatabaseTimeStamp() throws SQLException, Exception {

		String sql = "select getdate()";
		ResultSet resultSet = this.executeQuery(sql);

		java.sql.Timestamp currTime = null;

		while (resultSet.next()) {
			currTime = resultSet.getTimestamp(1);
		}
		return currTime;
	}

	public Connection getConnection() throws SQLException, Exception {

		/**
		 * private static final String SERVER = "10.49.53.195"; private static
		 * final String PORT ="1186"; private static final String USER_NAME
		 * ="ILAPUser"; private static final String PASSWORD = "ILAPUser";
		 */

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		String mssqlUrl = properties.getProperty("mssql-url");
		String user = properties.getProperty("mssql-user");
		String password = properties.getProperty("mssql-password");
		String mssqlHost = properties.getProperty("mssql-host");
		String mssqlPort = properties.getProperty("mssql-port");
		String mssqlDbName = properties.getProperty("mssql-dbname");
		logger.info("java.library.path: " + System.getProperty("java.library.path"));

		String connectionString = MessageFormat.format(mssqlUrl, mssqlHost,
				mssqlPort, mssqlDbName);
		logger.info(connectionString);
		Connection conn = DriverManager.getConnection(connectionString, user,
				password);

		if (conn == null) {
			System.out.println("problem connecting to database");
		}
		return conn;
	}

}
