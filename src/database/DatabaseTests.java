package database;

import static org.junit.Assert.*;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

/*
 * Provides basic regression tests to ensure that a
 * database connection can still be successfully
 * made.
 */
public class DatabaseTests {
	static DBManager dbman;
	static Database database;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		dbman = new DBManager();
		dbman.initialize();

		database = dbman.getDatabase();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		dbman.shutdown();
		database = null;
		dbman = null;
	}
	
	@Test
	@DisplayName("Site Table Initialized")
	public void siteTableInit() {
		assertTrue(dbman.query("SELECT * FROM site"));
	}
	
	@Test
	@DisplayName("PDF Table Initialized")
	public void pdfTableInit() {
		assertTrue(dbman.query("SELECT * FROM erp"));
	}
	
	@Test
	@DisplayName("ERP Table Initialized")
	public void erpTableInit() {
		assertTrue(dbman.query("SELECT * FROM password"));
	}
}
