package user_interface;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import database.DBManager;
import drivers.CODES;
import drivers.Driver;

public final class FrontendDriver {
	private static Window window;

	/**
	 * Launches a new Window, the main frontend for the application.
	 * 
	 * @return True if a window was already open (and subsequently closed)
	 */
	public static boolean initialize() {
		if (window instanceof Window) {
			window.close();
			window = new Window();

			return true;
		}

		window = new Window();
		return false;
	}
	
	/**
	 * Check whether a file is a PDF
	 * @param file
	 * @return
	 */
	public static boolean isValidFileType(File file) {
		return file.getName().matches("^.+[.]{1}[Pp]{1}[Dd]{1}[Ff]{1}$");
	}
	/**
	 * Upload a file to the internal database
	 * @param file
	 * @return
	 */
	public static File uploadFile(String id, File file) {
		if (!isValidFileType(file)) {
			return (File) FrontendDriver.showErrCode(CODES.NO_PDF, CODES.NO_PDF.getMessage());
		}else if(file.length() / 1024 > DBManager.MAX_PDF_SIZE){
				return (File) FrontendDriver.showErrCode(CODES.FILESIZE_LIMIT, "The selected file (" + file.length()/1024 + ") is "
						+ "larger than the allowed maximum ("+DBManager.MAX_PDF_SIZE+")");
		}else {
			return Driver.DBManager().setERP(id, file);
		}
	}
	
	/**
	 * Opens a stored page with the ID targetID
	 * @param targetID Unique ID of the IPanel to open
	 */
	public static void openPage(String targetID) {
		window.openPage(targetID);
	}
	
	/**
	 * Opens an ERP PDF in the Operating System's default application.
	 * 
	 * @param file to open
	 */
	public static void openExternalFile(File file) {
		if(file == null) {
			Driver.Log("Couldnt open null file");
			return;
		}
		
		try {
			Desktop.getDesktop().browse(file.toURI());
		} catch (IOException e) {
			Driver.Log("Could not open file " + file.getAbsolutePath());
			if (Driver.debugmode())
				e.printStackTrace();
		}
	}
	
	/**
	 * Prompts the user to change the administrator password
	 */
	public static void setPasswordPrompt() {
		boolean valid;
		
		do {
			String in1 = JOptionPane.showInputDialog("You must set an administrator password");
			String in2 = JOptionPane.showInputDialog("Re-enter password to confirm");
			
			if(in1.equals(in2) && in1 != null && in2 != null) {
				valid = true;
				JOptionPane.showMessageDialog(null, "Password set successfully");
				
				Driver.DBManager().setPassword(in1);
			}else {
				valid = false;
				JOptionPane.showMessageDialog(null, "Passwords do not match");
			}
		} while (!valid);
	}
	
	/**
	 * Lets the user know that no ERP was found for the given area, and gives them the option to add one
	 * @param id for the area that no ERP was found
	 * 
	 * @return a newly uploaded ERP, or null if none was uploaded
	 */
	public static File noERPFound(String id) {
		if (JOptionPane.showConfirmDialog(null, "No ERP plan exists for this zone, would you like to add one?", "No ERP Exists", 
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			createFilePrompt(id, window);
		}
		
		return null;
	}
	
	/**
	 * Creates a prompt in the form of a JFileChooser, allowing the user to upload a new ERP to the system
	 * Before the FileChooser is created, the user must input the administrator password.
	 * 
	 * @param parent Parent component, can be null.
	 * @param id for the ERP the uploaded file will correlate to
	 * @return true if the user selected a file, false if they did not (for example, pressing cancel or closing the window before uploading)
	 */
	public static boolean createFilePrompt(String id, Component parent) {
		boolean incorrectPassword = true;
		do {
			Driver.Log("Requesting password");
			String input = JOptionPane.showInputDialog("Enter the administrator password");
			
			if(!Driver.DBManager().correctPassword(input)) {
				Driver.Log("Incorrect password entered");
				JOptionPane.showMessageDialog(null, "The password you entered is incorrect");
				incorrectPassword = true;
			} else {
				Driver.Log("Correct password entered.");
				incorrectPassword = false;
			}
		}while(incorrectPassword);
		
		JFileChooser jfc = new JFileChooser();
		FileNameExtensionFilter wtf = new FileNameExtensionFilter("PDF", "pdf");
		jfc.setFileFilter(wtf);
		jfc.updateUI();
		int returnVal = jfc.showOpenDialog(parent);
		if(returnVal == JFileChooser.APPROVE_OPTION) { //If the user selected to upload a file, handle it
			File newFile = jfc.getSelectedFile();
			FrontendDriver.uploadFile(id, newFile);
			JOptionPane.showMessageDialog(null, "ERP Uploaded Successfully.");
			return true;
		}
		
		// If they didnt select to upload a file - i.e. by pressing
		// "Cancel" or closing the window, do nothing
		return false;
	}
	
	/**
	 * Shows a custom error message that must be acknowledged by the user before closing
	 * This method always returns null, allowing for neater code by the calling method (as this is usually the last
	 * statement in a method if something goes wrong)
	 * 
	 * @param code Error code
	 * @param message Custom message string
	 */
	public static Object showErrCode(CODES code, String message) {
		JOptionPane.showConfirmDialog(null, message, "Error " + code.getCode(), JOptionPane.DEFAULT_OPTION);
		return null;
	}
	
	/**
	 * Shows a default error message that must be acknowledged by the user before closing
	 * 
	 * @param code Error code the message will be based on
	 */
	public static void showErrCode(CODES code) {
		showErrCode(code,
				"An error has occured (Error code: " + code.getCode() + ")");
	}

	public static void setCurrentERP(String targetID) {
		window.setCurrentERP(targetID);		
	}

}
