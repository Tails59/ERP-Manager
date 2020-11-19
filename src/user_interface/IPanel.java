package user_interface;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import drivers.CODES;
import drivers.Driver;

/**
 * Overrides the default paintComponent to make it easier setting an image
 * as the background of a JPanel
 * 
 * @author Taylor
 *
 */
public class IPanel extends JPanel implements Serializable {
	private static final long       serialVersionUID = -5168638171387965313L;
	private transient BufferedImage img;
	private String                  imagePath;

	public IPanel(String path) {
		this.setImage(path);
	}

	public IPanel() {

	}

	// Uses the default, "do nothing but say it went bad" error handling
	public void setImage(String path) {
		try {
			img = ImageIO.read(getClass().getResource(path));
			imagePath = path;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"An invalid path was tried to load as an image!");
		}
	}

	public String getImagePath() { return this.imagePath; }

	/**
	 * Loads an IPanel from a file
	 * 
	 * @param pageName
	 *            Name of the page (filename without the extension)
	 * @return The loaded IPanel
	 */
	public static IPanel loadPage(String pageName) {
		return Driver.DBManager().getPageObj(pageName);
	}

	/**
	 * Save an IPanel page to the database
	 * 
	 * 
	 * @param page
	 *            IPanel with all components added to the content panel
	 * @param pageName
	 *            Name of the page, will be used as the primary key
	 * 
	 * @return A code indicating if the operation was successful, or the error that happened if it was not
	 */
	public static CODES savePage(IPanel page, String pageName) {
		return Driver.DBManager().addPageObj(pageName, page);
	}
	
	/*
	 * Draws the background of the panel
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(this.img, 0, 0, this);
	}
}
