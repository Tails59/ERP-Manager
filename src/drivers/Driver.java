package drivers;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import database.DBManager;
import user_interface.FrontendDriver;

public class Driver {
	public static final String VERSION = "0.0.1";
	
	private static DBManager dbman;
	private static boolean debugmode;
	
	/**
	 * Insertion point for the program. Initializes the database connection
	 * and then launches the user interface.
	 * 
	 * @param args launch parameters. See the user manual for acceptable inputs.
	 */
	public static void main(String[] args)  {
		handleLaunchParams(args);
		
		startDB();
		
		FrontendDriver.initialize();
	}
	
	/**
	 * Changes the administrator password
	 */
	private static void changePassword() {
		startDB(); //The DB must be started before we can attempt to UPDATE the password
		Scanner sc = new Scanner(System.in);
		String input;
		boolean valid;
		
		do {
			valid = true;
			System.out.print("ENTER OLD ADMINISTRATOR PASSWORD: ");
			input = sc.nextLine();
			
			if (! dbman.correctPassword(input)) {
				System.out.println("!!! INCORRECT PASSWORD !!!");
				valid = false;
			}
		} while (!valid);
		
		
		do {
			System.out.print("ENTER NEW PASSWORD: ");
			String in1 = sc.nextLine();
			System.out.print("CONFIRM PASSWORD: ");
			String in2 = sc.nextLine();
			
			if(in1.equals(in2)) {
				valid = true;
				System.out.println("PASSWORD CHANGED");
				
				dbman.setPassword(in1);
			}else {
				valid = false;
				System.out.println("PASSWORDS DO NOT MATCH");
			}
		} while (!valid);
		
		System.out.println("APPLICATION REQUIRES RESTART AFTER CHANGING PASSWORD");
		sc.close();
		dbman.shutdown();
		exit(0);
	}
	
	/**
	 * Handles different launch parameters given via the console.
	 * @param args
	 * 
	 * @return true if the application should proceed with a non-safemode start.
	 */
	private static void handleLaunchParams(String[] args) {
		List<String> arguments = Arrays.asList(args);
		
		if(arguments.contains("debug")) {		
			debugmode = true;
		}
		
		if (arguments.contains("newpass")) {
			changePassword();
		}
		
		if(arguments.contains("resetdb")) {
			resetDB();
		}
	}
	
	private static void resetDB() {
		boolean PASSWORD_OVERRIDE = true;
		startDB();
		Scanner sc = new Scanner(System.in);
		
		if(!PASSWORD_OVERRIDE) {
			boolean incorrectPassword = true;
			do {
				System.out.print("Enter the administrator password: ");
				String pass = sc.nextLine();
				if(dbman.correctPassword(pass)) {
					incorrectPassword = false;
				}
			}while(incorrectPassword);
		}
		
		System.out.print("Are you sure you wish to delete ALL ERPs? (Y/N): ");
		String in = sc.nextLine();
		
		sc.close();
		if(in.equals("Y")) {
			dbman.resetDatabase();
			exit(0);
		} else {
			System.out.println("Exiting...");
			exit(0);
		}
		
		return;
	}
	
	/**
	 * Returns whether the application is running in debug mode
	 * @return true if debugmode
	 */
	public static boolean debugmode() {
		return debugmode;
	}
	
	/**
	 * Prints a message to the console if the application is in debug mode
	 * 
	 * @param msg Message to print
	 */
	public static void Log(String msg) {
		if(debugmode) {
			System.out.println(msg);
		}
	}
	
	/**
	 * Starts and connects to the embedded database.
	 */
	private static void startDB() {
		//Initialize the database connection
		dbman = new DBManager();
		CODES c = dbman.initialize();
		
		//If it didn't start correctly, display an error code
		if (!c.equals(CODES.OK)) {
			Log("The database could not start");
			Log(c.getCode());
			JOptionPane.showMessageDialog(null, c.getMessage() + "\n Err: " + c.getCode(),
					"Error", JOptionPane.ERROR_MESSAGE);
			exit(1);
		}
		
		//Add the shutdown hook - causes the onShutdown method to be 
		//called before the JVM terminates.
		//This hook has no guarantee of being run if the JVM terminates
		//abnormally - e.g. by killing the application externally, or if it crashes
		//See https://www.geeksforgeeks.org/jvm-shutdown-hook-java/ for more information
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				onShutdown();
			}
		});		
	}
	
	/**
	 * Exit the program
	 * 
	 * @param statusCode An integer status code, non-zero values represent abnormal termination.
	 */
	public static void exit(int statusCode) {
		System.exit(statusCode);
	}
	
	/**
	 * Testing function used to start the embedded database
	 * Will throw an IllegalAccessError if access from outside of the
	 * GenericTests class
	 */
	static DBManager startDBExternal() {
		StackTraceElement[] elem = Thread.currentThread().getStackTrace();
		if (elem[2].getClassName().equals("drivers.GenericTests")
				&& elem[2].getMethodName().equals("setUpBeforeClass")) {
			startDB();
			return dbman;
		}

		throw new IllegalAccessError();
	}
	
	/**
	 * Get the DBManager object
	 * @return
	 */
	public static DBManager DBManager() {
		return dbman;
	}
	
	/**
	 * Called before the JVM terminates.
	 */
	private static void onShutdown() {
		Driver.Log("Application terminating...");
		dbman.shutdown();
		Driver.Log("Application terminated successfully");
	}
}
