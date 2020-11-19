package user_interface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;

import drivers.Driver;

public class IButton extends JButton implements Serializable, ActionListener {
	private static final long serialVersionUID = -2886567124491492815L;
	private String            targetID;
	boolean                   isLeaf;

	public IButton(String text, String targetID, boolean isLeaf) {
		super(text);
		System.out.println("ibutton created");
		
		this.targetID = targetID;
		this.isLeaf = isLeaf;
	}

	public String getTarget() { return this.targetID; }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.isLeaf) {
			FrontendDriver.openExternalFile(Driver.DBManager().getERP(this.targetID));
			FrontendDriver.setCurrentERP(this.targetID);
		}else {
			FrontendDriver.openPage(targetID);
		}
	}
}
