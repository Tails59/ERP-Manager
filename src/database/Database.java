package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

import drivers.Driver;

/*
 * Class to control the implementation to connect to the database, as well
 * as the implementation of querying and shutting down the database. As the
 * DB is embedded the database must be shutdown properly before exiting the
 * program
 * 
 * This class should only be used through the database.DBManager class
 * 
 * Syntax for the Apache Embedded Database can be found here: https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/sqlref/src/tpc/db2z_sql_createtable.html
 */
final class Database {
	private static final String  DATABASE_NAME = "erp_manager";
	private static final String DEFAULT_URL   = "jdbc:derby:"+DATABASE_NAME+";create=true;";
	
	private Connection   connection;
	private Statement    statement;
	private SQLException shutdownExcp;

	/**
	 * A predefined string to create the database tables if they dont
	 * already exist this is always queried when a new Database instance is
	 * created
	 * 
	 * After reconsidering the use case of the database it was decided to
	 * use 3 non-relational tables.
	 */
	private String initSite = "CREATE TABLE site("
			+ "id VARCHAR(255) PRIMARY KEY, panel BLOB)";

	private String initERP = "CREATE TABLE erp("
			+ "id VARCHAR(255) PRIMARY KEY, pdf BLOB)";
	
	private String initPass = "CREATE TABLE password("
			+ "id INT PRIMARY KEY,"
			+ "pass VARCHAR(255))";

	/**
	 * Creates a new Database instance .We throw these exceptions instead of
	 * catching them to stop the Database object being created without a
	 * connection
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	Database()
			throws SQLException, SQLTimeoutException, ClassNotFoundException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver"); //Load the embedded driver

		connection = DriverManager.getConnection(DEFAULT_URL);
		statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Runs a query on the Connection
	 * 
	 * @param query
	 *            self-explanatory
	 * @return true if the first result returned by the query is a
	 *         ResultSet. The respective ResultSet can be retrieved with
	 *         the getResults() method. If the query is guaranteed to
	 *         return a ResultSet, executeQuery might be a better method.
	 */
	boolean query(String query) {
		try {
			return statement.execute(query);
		} catch (SQLException e) {
			System.out.println("You have an error in your SQL syntax");
			System.out.println(e.getSQLState());
			System.out.println(e);
			return false;
		}
	}

	/**
	 * Get the ResultSet from the most recent query
	 * 
	 * @return The ResultSet from the query, or null if the query causes an
	 *         error.
	 */
	ResultSet getResults() {
		try {
			return statement.getResultSet();
		} catch (SQLException e) {
			System.out.println(e.getSQLState());
			System.out.println(e);
			return null;
		}
	}

	/**
	 * Execute a query and return the ResultSet object
	 * 
	 * @param query
	 *            Query to be run
	 * @return the ResultSet object if one was created, otherwise null.
	 */
	ResultSet executeQuery(String query) {
		if (query(query)) { return getResults(); }

		return null;
	}

	/**
	 * Creates a PreparedStatement with the supplied query
	 * 
	 * @param query
	 *            Query to prepared
	 * @return The created PreparedStatement object or null if an exception
	 *         was thrown
	 */
	PreparedStatement prepare(String query) {
		try {
			return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Close JDBC resources
	 */
	private void close() {
		try {
			statement.close();
			connection.close();
		} catch (Exception e) {
			/* ignore the exception */
		}
	}
	
	/**
	 * Shutdown the database. Should be called before the application is
	 * terminated
	 * 
	 * @return True if the database shutdown without issue. False if it
	 *         shutdown abnormally - the exception can be retrievedwith the
	 *         getShutdownException() method call.
	 */
	boolean shutdown() {
		close();
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (DerbyHelper.isShutdown(e)) { 
				Driver.Log("Database shutdown successfully");
				return true; 
			}
			
			this.shutdownExcp = e;
		}

		Driver.Log("Database suffered an abnormal shutdown");
		if(Driver.debugmode())
			this.shutdownExcp.printStackTrace();
		
		return false;
	}

	/**
	 * @return SQLException the exception caused when trying to shutdown if
	 *         there is one.
	 */
	SQLException getShutdownException() { return this.shutdownExcp; }

	/**
	 * Called at initialization to create the tables If they already exist,
	 * an SQLException with SQLState X0Y32 should be thrown - this will be
	 * caught and checked, if the SQLState is not X0Y32 then it is rethrown
	 * back to the DBManager
	 * 
	 * @return
	 * @throws SQLException
	 */
	boolean createTables()
			throws SQLException {
		boolean passCreated = false;
		
		try {
			statement.execute(initERP);
		} catch (SQLException e) {
			if (!DerbyHelper.valueExists(e)) { throw e; }
		}

		try {
			statement.execute(initSite);
		} catch (SQLException e) {
			if (!DerbyHelper.valueExists(e)) { throw e; }
		}
		
		try {
			statement.execute(initPass);
			passCreated = true;
		} catch (SQLException e) {
			if (!DerbyHelper.valueExists(e)) { throw e; }
		}

		initERP = null;

		return passCreated; //Return whether the password table had to be created
	}
}
