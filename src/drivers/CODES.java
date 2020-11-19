package drivers;

/**
 * Error code enums. The field {@code String message} is not user friendly,
 * and should be overriden when passing a CODE enum to the
 * {@code FrontendDriver}
 * 
 * @author Taylor
 *
 */
public enum CODES {
	// Common Apache Derby error codes
	SHUTDOWN_OK("XJ015", "The database shutdown successfully"), 
	VAL_EXISTS("X0Y32", "Value already exists"), 
	SYNTAX_ERROR("42X01", "There is an error in your SQL Syntax"),

	// Database codes
	CONNECT_FAIL("DB001", "Failed to connect to the database"), 
	CONNECT_TIMEOUT("DB002", "Timed out connecting to the database"), 
	NO_DRIVER("DB003", "The Database Driver could not be found"),
	DUPLICATE_KEY("DB004", "The statement was aborted as it would have created a duplicate key value in a unique or primary key constraint"),

	// Validation and misc
	BAD_TYPE("VAL001", "The object is of the wrong type"), 
	NO_PDF("VAL002", "Only PDF objects can be uploaded"),
	BYTE_ARRAY_ERROR("VAL003", "A byte array transform failed"),
	CORRUPT_ERP("VAL004", "The ERP could not be converted to a viewable PDF format, this usually means it is corrupt"),
	FILESIZE_LIMIT("VAL005", "The selected file is too large"),
	
	// Generic codes
	OK("0000", "The operation performed successfully"),
	ERROR("0001", "An unknown error has occured");

	private String code;
	private String message;

	private CODES(String code, String message) {
		this.code = code;
		this.message = message;
	}

	private CODES(String code) {
		this(code, "");
	}

	public String getCode() { return this.code; }

	public String getMessage() { return this.message; }

	public String toString() {
		return this.message + " (" + this.code + ")";
	}
}
