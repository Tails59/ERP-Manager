package database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import drivers.CODES;
import drivers.Driver;
import user_interface.FrontendDriver;
import user_interface.IPanel;

/*
 * Acts as a wrapper for the Database class Contains methods for querying,
 * creating prepared queries and calling startup/shutdown of the database.
 * For actual implementation of these methods, see the database.Database
 * class
 */
public final class DBManager {
	private static final String ADD_PAGE_OBJECT = "INSERT INTO site(id, panel) VALUES(?, ?)";
	private static final String UPDATE_PAGE_OBJECT = "UPDATE site SET panel = ? WHERE id = ?";
	private static final String GET_PAGE_OBJECT = "SELECT panel FROM site WHERE id=?";

	private static final String ADD_ERP_OBJECT = "INSERT INTO erp(id, pdf) VALUES(?, ?)";
	private static final String UPDATE_ERP_OBJECT = "UPDATE erp SET pdf = ? WHERE id = ?";
	private static final String GET_ERP_OBJECT = "SELECT pdf FROM erp WHERE id=?";
	
	private static final String SET_PASS = "INSERT INTO password(id, pass) VALUES(1, ?)";
	private static final String UPDATE_PASS = "UPDATE password SET pass = ? WHERE id = 1";
	private static final String GET_PASS = "SELECT pass FROM password WHERE id = 1";
	
	public static final int MAX_PDF_SIZE = 2048; //Maximum pdf size in megabytes
	
	private String hashedPassword;
	private Database database;

	/**
	 * Initializes the database connection, and creates necessary tables if
	 * not already created
	 * 
	 * @param url
	 *            Url of the database to connect to
	 * @throws Connection
	 *             object if successful
	 * @return True if the connection has been established successfully
	 */
	public CODES initialize() {
		try {
			database = new Database();
			boolean nopass = database.createTables();
			
			if (nopass) {
				FrontendDriver.setPasswordPrompt(); //If the password table didnt exist, prompt the user to create a password
			}
			
			getPassword();
			
			return CODES.OK;
		} catch (ClassNotFoundException e) {
			return CODES.NO_DRIVER;
		} catch (SQLTimeoutException e) {
			return CODES.CONNECT_TIMEOUT;
		} catch (SQLException e) {
			if (e.getSQLState() == CODES.SYNTAX_ERROR.getCode()) {
				if(Driver.debugmode()) {
					System.out.println(e);
				}
				return CODES.SYNTAX_ERROR;
			}
			
			if (Driver.debugmode())
				e.printStackTrace();
			
			return CODES.CONNECT_FAIL;
		}
	}
	
	/**
	 * Retrieve the password from the database
	 * This is called from the initialize() method, and the retrieved password is stored
	 * in the hashedPassword field. To compare an input with the password, use the
	 * correctPassword() method
 	 */
	private void getPassword() {
		ResultSet test = executeQuery(GET_PASS);
		
		try {
			test.first();
			String test2 = test.getString(1);
			this.hashedPassword = test2;			
		} catch (SQLException e) {
			Driver.Log("An SQLException occured when trying to retrieve the password");
			
			if(Driver.debugmode())
				e.printStackTrace();
		}
	}
	
	/**
	 * Perform a SHA256 transformation on parameter in
	 * 
	 * Code partly inspired by https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
	 * 
	 * @param in String to be transformed
	 * @return a SHA256 representation of the input string
	 */
	public String hash(String in) {
		try {
			MessageDigest dig = MessageDigest.getInstance("SHA-256");
			dig.update(in.getBytes());
			byte[] test = dig.digest();
			
			StringBuilder sb = new StringBuilder();
            for(int i=0; i< test.length ;i++)
            {
                sb.append(Integer.toString((test[i] & 0xff) + 0x100, 16).substring(1));
            }
			
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			/* We know for sure the SHA-256 algorithm exists, its ok */
			e.printStackTrace();
		}
		
		return null; //stop dumb compilation problems
	}
	
	/**
	 * Converts any object that implements the Serializable interface to a byte array
	 * 
	 * @param obj an object that implements the Serializable interface to be converted into a byte array
	 * @return an array of bytes representing the parameter obj
	 * @throws IOException if the object could not be written to the output stream
	 */
	public byte[] toByteArray(Serializable obj) {
		byte[] stream = null;
		
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);){
					
			oos.writeObject(obj);
			stream = baos.toByteArray();
		} catch (IOException e) {
			FrontendDriver.showErrCode(CODES.BYTE_ARRAY_ERROR, "The object could not be converted to a byte array");
			e.printStackTrace();
		}
		
		return stream;
	}
	
	/**
	 * Transforms a byte array into an object
	 * 
	 * @param stream byte array input to convert
	 * 
	 * @return the byte array parsed to an object
	 */
	public Object fromByteArray(byte[] stream) {
		if (stream != null) {
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(stream))){
				return ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				FrontendDriver.showErrCode(CODES.BYTE_ARRAY_ERROR, "The stream could not be converted to an object");
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * Adds an IPanel to the SITE table
	 * If the specified ID already exists, it will be updated with the given panel
	 * 
	 * Code modified from
	 * https://javapapers.com/core-java/serialize-de-serialize-java-object-from-database/
	 * 
	 * @param name
	 *            Page name to be INSERT'd or UPDATE'd
	 * @param panel
	 *            IPanel page object
	 * @return true if the insert completed successfully
	 */
	public CODES addPageObj(String name, IPanel panel) {
		PreparedStatement prep = database.prepare(ADD_PAGE_OBJECT);

		byte[] stream = toByteArray(panel);

		try {
			prep.setString(1, name);
			prep.setBytes(2, stream);

			prep.executeUpdate();
			return CODES.OK;
		}catch (DerbySQLIntegrityConstraintViolationException e ) {
			Driver.Log("Panel " + name + " already exists, updating record...");
			prep = database.prepare(UPDATE_PAGE_OBJECT);
			
			try {
				prep.setString(2, name);
				prep.setBytes(1, stream);
				
				prep.executeUpdate();
				
				return CODES.OK;
			} catch (SQLException e1) {
				if(Driver.debugmode()) {
					e1.printStackTrace();
				}
				
				return CODES.ERROR;
			}
		} catch (SQLException e) {
			if(Driver.debugmode()) {
				e.printStackTrace();
			}
			return CODES.ERROR;
		}
	}

	/**
	 * Pulls an IPanel with the id of name from the database
	 * 
	 * @param name
	 *            id of the object in the database
	 * @return an IPanel object
	 */
	public IPanel getPageObj(String name) {
		IPanel panel = null;
		PreparedStatement prep = database.prepare(GET_PAGE_OBJECT);

		try {
			prep.setString(1, name);
			ResultSet rs = prep.executeQuery();

			rs.next();

			panel = (IPanel) fromByteArray(rs.getBytes(1));

		} catch (SQLException e) {
			FrontendDriver.showErrCode(CODES.ERROR);
			e.printStackTrace();
		}

		panel.setImage(panel.getImagePath());
		return panel;
	}

	/**
	 * Pulls an ERP from the database with the given id
	 * If no ERP exists for the given ID, it will prompt the user to upload one
	 * 
	 * @param siteid
	 * @return
	 */
	public File getERP(String id) {
		PreparedStatement prep = database.prepare(GET_ERP_OBJECT);
		ResultSet results = null;
		boolean valid = false;
		byte[] stream = null;
		
		try {
			prep.setString(1, id);
			results = prep.executeQuery();
			
			valid = results.first();
			if (! valid) {
				Driver.Log("No ERP found for " + id);
				FrontendDriver.noERPFound(id);
				return null;
			}
			
			stream = results.getBytes(1);
			
			if(stream != null) { 
				File test = (File) fromByteArray(stream);
				System.out.println(test);
				return test;
			}
		} catch (SQLException e) {
			Driver.Log(e.toString());
			if(Driver.debugmode())
				e.printStackTrace();
			
			return (File) FrontendDriver.showErrCode(CODES.ERROR, CODES.ERROR.getMessage());
		} catch (ClassCastException e) {
			return (File) FrontendDriver.showErrCode(CODES.CORRUPT_ERP, CODES.CORRUPT_ERP.getMessage());
		}
		
		Driver.Log("NULL STREAM");
		return null;
	}

	/**
	 * Add an ERP to the database with the associated id
	 * 
	 * @param id
	 * 
	 * @return the uploaded file
	 */
	public File setERP(String id, File file) {
		PreparedStatement prep = database.prepare(ADD_ERP_OBJECT);
		
		byte[] stream = toByteArray(file);

		try {
			prep.setString(1, id);
			prep.setBytes(2, stream);

			prep.executeUpdate();
		}catch (DerbySQLIntegrityConstraintViolationException e) {
			Driver.Log("ERP " + file + " already exists, updating record...");
			prep = database.prepare(UPDATE_ERP_OBJECT);
			
			try {
				prep.setString(2, id);
				prep.setBytes(1, stream);
				
				prep.executeUpdate();
				
			} catch (SQLException e1) {
				if(Driver.debugmode()) {
					e1.printStackTrace();
				}
				
			}
		}catch (SQLException e) {
			Driver.Log("An error occured trying to set an ERP");
			e.printStackTrace();
		} 
		
		return file;
	}
	
	/**
	 * Performs an INSERT or UPDATE query to change the password
	 * 
	 * @param in
	 */
	public void setPassword(String in) {
		String hashed = hash(in);
		Driver.Log("Attempting to set/update password");
		
		PreparedStatement prep = database.prepare(SET_PASS);
		
		try {
			prep.setString(1, hashed);
			prep.executeUpdate();
		}catch (DerbySQLIntegrityConstraintViolationException e) {
			Driver.Log("Updating password");
			prep = database.prepare(UPDATE_PASS);
			
			try {
				prep.setString(1, hashed);
				prep.executeUpdate();
			} catch (SQLException e1) {
				if(Driver.debugmode()) {
					e1.printStackTrace();
				}
				
			}
		}catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Executes a query on the database
	 * @param query SQL Query
	 * @return True if the query returned a ResultSet, which can be retrieved with the getResults() method
	 */
	public boolean query(String query) {
		return database.query(query);
	}
	
	/**
	 * Execute a query on the database
	 * 
	 * @param query SQL Query
	 * @return A ResultSet from the query
	 */
	public ResultSet executeQuery(String query) {
		return database.executeQuery(query);
	}

	/**
	 * Shutdown the database and close any active connections.
	 * 
	 * @return True if the database shutdown as expected
	 */
	public boolean shutdown() {
		if (!database.shutdown()) {
			System.out.println(database.getShutdownException());
		}
		return true;
	}
	
	/**
	 * Resets the database to the original state
	 * This will remove ALL ERPs from the system.
	 * It will also remove the administrator password.
	 */
	public void resetDatabase() {
		database.query("DROP TABLE password");
		database.query("DROP TABLE erp");
	}
	
	/**
	 * Testing function used to get the main database object (which
	 * includes the Connection) only accessible when called from
	 * setUpBeforeClass() from the database.DatabaseTests class.
	 * 
	 * @throws IllegalAccessError if any other method attempts to call this method.
	 * @return null
	 */
	Database getDatabase() {
		StackTraceElement[] elem = Thread.currentThread().getStackTrace();
		if (elem[2].getClassName().equals("database.DatabaseTests")
				&& elem[2].getMethodName().equals("setUpBeforeClass")) {
			return this.database;
		}

		throw new IllegalAccessError();
	}


	/**
	 * Check whether an input equals the adminstrator password
	 * @param input Input password to compare with the stored hashed password
	 * @return True if the input, when hashed, is equal to the stored password
	 */
	public boolean correctPassword(String input) {		
		return input == null ? false : hash(input).equals(this.hashedPassword);
	}
}
