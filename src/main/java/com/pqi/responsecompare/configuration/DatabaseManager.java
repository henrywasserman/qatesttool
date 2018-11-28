package com.pqi.responsecompare.configuration;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

public abstract class DatabaseManager {
	static Logger logger = Logger.getLogger(DatabaseManager.class);
	public static final int MSSQL = 1;
	public static final int ORACLE = 2;
	private Connection conn;
	private Statement stmt = null;
	private ResultSet rs = null;
	protected Properties properties;

	/**
	 * 
	 * @param dbFlag
	 *            1 for MSSQL; 2 for Oracle
	 * @param props
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public DatabaseManager(Properties props) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, Exception {
		logger.debug("Getting properities");
		this.properties = props;
		logger.debug("Getting connection");
		conn = getConnection();
	}

	public abstract Connection getConnection() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, Exception;

	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void CloseStatement() {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException, Exception {
		try {
			Properties props = Utilities.Instance.getTestProperties();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.close();
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		}
		return rs;
	}
}