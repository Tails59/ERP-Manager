package user_interface;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;

import drivers.Driver;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.SwingConstants;


import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
class Window extends JFrame {
	private Container contentPane;
	private IPanel    body;
	private JButton   btnBack;
	private JButton   btnChangeERP;
	private Component frame = this;
	private String currentERP = null;
	
	// Keeps a track of the order pages were accessed - used by the Back
	// button so
	// the user can return up their page history
	private Stack<IPanel> pageOrder = new Stack<IPanel>();
	
	Window() {
		super();
		getContentPane().setBackground(Color.GRAY);
		setResizable(false);
		setBackground(Color.GRAY);
		mainFrame();
		
		openPage("main");
	}

	private void backAPage() {
		remove(body);
		body = pageOrder.pop();

		if (pageOrder.size() == 1) {
			btnBack.setEnabled(false);
			btnBack.setToolTipText("There are no previous pages");
		}

		getContentPane().add(body);
	}

	void openPage(String page) {
		pageOrder.add(body);
		if (body != null)
			remove(body);

		body = IPanel.loadPage(page);

		btnBack.setEnabled(true);
		btnBack.setToolTipText("Go back a page");

		getContentPane().add(body);
		refresh();
	}

	private void mainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setBounds(10, 0, 1500, 912);
		contentPane = getContentPane();
		contentPane.setLayout(null);

		JPanel headerPnl = new JPanel();
		headerPnl.setBackground(Color.GRAY);
		headerPnl.setBounds(10, 11, 1462, 89);
		getContentPane().add(headerPnl);
		headerPnl.setLayout(null);

		JLabel lblHeader = new JLabel("Emergency Response Plan Management");
		lblHeader.setFont(new Font("Tahoma", Font.PLAIN, 32));
		lblHeader.setBounds(23, 11, 581, 67);
		headerPnl.add(lblHeader);

		JLabel lblVersion = new JLabel("Version " + Driver.VERSION);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblVersion.setBounds(1232, 11, 218, 19);
		headerPnl.add(lblVersion);

		JPanel sidebar = new JPanel();
		sidebar.setBackground(Color.GRAY);
		sidebar.setBounds(1317, 99, 165, 766);
		getContentPane().add(sidebar);
		sidebar.setLayout(null);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				backAPage();
			}
		});
		btnBack.setBounds(39, 728, 88, 26);
		btnBack.setBackground(SystemColor.inactiveCaption);
		btnBack.setEnabled(false);
		btnBack.setToolTipText("There are no previous pages");
		sidebar.add(btnBack);

		btnChangeERP = new JButton("Change ERP");
		btnChangeERP.setBackground(SystemColor.inactiveCaption);
		btnChangeERP.setBounds(36, 195, 103, 26);
		btnChangeERP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(currentERP == null) {
					JOptionPane.showMessageDialog(null, "You have not opened any ERP!");
					return;
				}
				Driver.Log("Updating ERP for " + currentERP);
				FrontendDriver.createFilePrompt(currentERP, frame);
			}
		});
		sidebar.add(btnChangeERP);
		
		JButton btnHome = new JButton("Home");
		btnHome.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		btnHome.setEnabled(true);
		btnHome.setBackground(SystemColor.inactiveCaption);
		btnHome.setBounds(36, 152, 103, 26);
		btnHome.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openPage("main");
			}
		});
		sidebar.add(btnHome);
	}

	public void refresh() {
		contentPane.invalidate();
		contentPane.validate();
		contentPane.repaint();
	}

	@Override
	/**
	 * Forces the frame to invalidate and repaint whenever new components
	 * are added
	 */
	public Component add(Component comp) {
		contentPane.add(comp);
		refresh();

		return comp;
	}

	@Override
	/**
	 * Forces the frame to invalidate and repaint whenever new components
	 * are removed
	 */
	public void remove(Component comp) {
		contentPane.remove(comp);
		refresh();
	}

	void close() {
		dispose();
	}

	public void setCurrentERP(String targetID) {
		this.currentERP = targetID;		
		btnChangeERP.setToolTipText("Change ERP for " + targetID);
	}
}
