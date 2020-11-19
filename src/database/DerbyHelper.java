package database;

import java.sql.SQLException;

import drivers.CODES;

class DerbyHelper {
	/**
	 * Returns true if the SQLState of the given SQLException e is X0Y32
	 * 
	 * @param e SQLException to compare
	 * @return true if the SQLState of the parameter e is X0Y32
	 * 
	 * @see
	 */
	static boolean valueExists(SQLException e) {
		return (e.getSQLState().equals(CODES.VAL_EXISTS.getCode()));
	}
	
	/**
	 * Returns true if the SQLState in the given SQLException e means
	 * the database shutdown successfully.
	 * 
	 * @param e SQLException to check
	 * @return True if the database shutdown properly
	 */
	static boolean isShutdown(SQLException e) {
		return (e.getSQLState().equals(CODES.SHUTDOWN_OK.getCode()));
	}
}
