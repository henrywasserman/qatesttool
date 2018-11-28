package com.pqi.responsecompare.sql;

import com.pqi.responsecompare.configuration.Utilities;
import com.pqi.responsecompare.request.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.*;
import java.util.Properties;

public abstract class DatabaseManager {
	static Logger logger = Logger.getLogger(DatabaseManager.class);
	public static final int MSSQL = 1;
	public static final int ORACLE = 2;
	public String SQL = "";
	private Connection conn;
	private Statement stmt = null;
	private CallableStatement cs = null;
	private ResultSet rs = null;
	protected Properties properties;

	/**
	 * 
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

	public void setSQL(String SQL) {
		this.SQL = SQL;
	}

	public void clearSQL() {
		SQL = "";
	}

	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeStatement() {
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

	public void execute(String sql, Request request) throws SQLException, Exception {
		try {
			boolean result = false;
			Properties props = Utilities.Instance.getTestProperties();
			stmt = conn.createStatement();
			conn.setAutoCommit(false);
			result = stmt.execute(sql);
			int counter = 0;
			while (true) {
				if (result) {

					rs = stmt.getResultSet();
					SQLToMap.Instance.appendMap(rs);
					request.setupAndOutput(SQLToMap.Instance.getSQLHtml
							(request.test.getTestRequestCounter() + counter),".html");
					request.setupAndOutput(SQLToMap.Instance.getSqlJSON
							(request.test.getTestRequestCounter() + counter), ".json");
					counter++;
				} else {
					int updateCount = stmt.getUpdateCount();
					if (updateCount == -1) {
						break;
					}

				}
				result = stmt.getMoreResults();
			}
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
	}

	public void executePLSQL(String sql, Request request) throws SQLException, Exception {
		try {
			boolean result = false;
			Properties props = Utilities.Instance.getTestProperties();
			conn.setAutoCommit(false);

			EncodedResource er = new EncodedResource(
					new InputStreamResource(IOUtils.toInputStream(sql)),"UTF8");

			ScriptUtils.executeSqlScript(conn, er,
					false, false,
					"--", "/",
					"/*",
					"*/");

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.close();
				cs.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		}
	}

	public void executeBatch(String sql) throws SQLException, Exception {
		try {
			Properties props = Utilities.Instance.getTestProperties();
			stmt = conn.createStatement();
			conn.setAutoCommit(false);
			String[] sqlCommands = StringUtils.split(sql,";");
			for (String sqlCommand:sqlCommands) {
				if (sqlCommand.trim().isEmpty()) {
					continue;
				}
				logger.info(sqlCommand);
				stmt.addBatch(sqlCommand.trim());
			}
			int[] result = stmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
			try {
				conn.close();
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		}
	}
}