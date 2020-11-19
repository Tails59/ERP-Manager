package drivers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import database.DBManager;
import user_interface.FrontendDriver;

class GenericTests {
	private static final String PASSWORD_GOOD = "password"; //The correct administrator password
	private static final String PASSWORD_BAD = "pasw0rd"; //Any incorrect administrator password
	
	static DBManager dbman;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		dbman = Driver.startDBExternal();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		dbman.shutdown();
		Driver.exit(0);
	}	
	
	//Test whether an incorrect password is correctly detected
	@Test
	void checkIncorrectPassword() {
		assertFalse(dbman.correctPassword(PASSWORD_BAD));
	}
	
	//Test whether a correct password is correctly detected
	@Test
	void checkCorrectPassword() {
		assertTrue(dbman.correctPassword(PASSWORD_GOOD));
	}
	
	//Test if the SHA-256 hashing method functions correctly
	@Test
	void checkSHA256HashWorks() {
		String unhashed = "Test12345";
		String hashed = "106ac304ae39bc4029db0faf0d1734bd5a1dc2474331e8e17039365847536d73";
		
		assertTrue(dbman.hash(unhashed).equals(hashed));
	}
	
	//Test whether non pdf files are rejected
	@Test
	void checkRejectNonPDF() {
		File invalid = new File("src/drivers/testInvalidPDF.png");
		assertFalse(FrontendDriver.isValidFileType(invalid));
	}
	
	//Test whether pdf files are accepted
	@Test
	void checkAcceptPDF() {
		File valid = new File("src/drivers/testValidPDF.pdf");
		assertTrue(FrontendDriver.isValidFileType(valid));
	}

}
