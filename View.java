import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.mysql.jdbc.exceptions.*;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.list.MessageList;


public class View{
	private JFrame mainWindow;
	private JFrame newContactWindow;
	private JFrame twilioAccountWindow;

	private final int FRAME_WIDTH = 1000;
	private final int FRAME_HEIGHT = 800;
	private final int NEW_CONTACT_FRAME_WIDTH = 300;
	private final int NEW_CONTACT_FRAME_HEIGHT = 500;
	private final int TWILIO_ACCOUNT_FRAME_WIDTH = 500;
	private final int TWILIO_ACCOUNT_FRAME_HEIGHT = 150;
	private final int CONTACT_PANEL_WIDTH = 300;
	private final int CONVERSATION_PANEL_WIDTH = FRAME_WIDTH - CONTACT_PANEL_WIDTH;

	private String twilioFile = "twilioInfo.txt";
	private String ACCOUNT_SID;
	private String AUTH_TOKEN;
	private String twilioServiceNumber;

	private JButton newContactSave, newContactCancel, twilioAccountUpdate, twilioAccountEdit, twilioAccountClose;

	private JTextField twilioAccountSIDField, twilioAuthTokenField, twilioContactField;
	private JTextField clientIDField, firstNameField, lastNameField, phoneNumberField;

	private JPanel contactArea = new JPanel();
	private JPanel messageArea = new JPanel();
	private JScrollPane scrollPane;

	private ArrayList<Contact> activeContacts = new ArrayList<Contact>();
	private ArrayList<JButton> contactButtons = new ArrayList<JButton>();

	private JMenuBar menuBar = new JMenuBar();
	private JMenu messageMenu = new JMenu("Messages");
	private JMenu contactMenu = new JMenu("Contacts");
	private JMenu setttingsMenu = new JMenu("Settings");
	private JMenuItem newMessage = new JMenuItem("New Message");
	private JMenuItem newGroupMessage = new JMenuItem("New Group Message");
	private JMenuItem newContact = new JMenuItem("New Contact");
	private JMenuItem twilioAccount = new JMenuItem("Twilio Account Credentials");
	private JMenuItem databaseAccount = new JMenuItem("Database Account Credentials");

	//Database connection information
	private Connection conn;
	private Statement st;
	private ResultSet res;
	private final String URL = "jdbc:mysql://localhost:3306/";
	private final String DB_NAME = "Texting_App";
	private final String DRIVER = "com.mysql.jdbc.Driver";
	private final String USER_NAME = "root"; 
	private final String PASSWORD = "textingappv2";

	public View(){
		mainWindow = new JFrame("Texting App");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setLayout(null);

		mainWindow.setJMenuBar(menuBar);
		menuBar.add(messageMenu);
		menuBar.add(contactMenu);
		menuBar.add(setttingsMenu);
		messageMenu.add(newMessage);
		messageMenu.add(newGroupMessage);
		contactMenu.add(newContact);
		setttingsMenu.add(twilioAccount);
		setttingsMenu.add(databaseAccount);
		newMessage.addActionListener(new menuListener());
		newGroupMessage.addActionListener(new menuListener());
		newContact.addActionListener(new menuListener());
		twilioAccount.addActionListener(new menuListener());
		databaseAccount.addActionListener(new menuListener());

		readContactsFromDatabase();
		addContactButtons();
		scrollPane = new JScrollPane(contactArea);

		scrollPane.setLocation(0, 0);
		scrollPane.setSize(CONTACT_PANEL_WIDTH, FRAME_HEIGHT - 50);
		messageArea.setLocation(CONTACT_PANEL_WIDTH, 0);
		messageArea.setSize(CONVERSATION_PANEL_WIDTH, FRAME_HEIGHT);

		mainWindow.add(messageArea);
		mainWindow.add(scrollPane);

		mainWindow.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);
		mainWindow.setLocationRelativeTo(null);
		readFile();

		Runtime runtime = Runtime.getRuntime();
		Thread thread = new Thread(new ShutDownListener());
		runtime.addShutdownHook(thread);
	}

	private void addContactButtons(){
		contactArea.removeAll();
		contactArea.setLayout(new GridLayout(contactButtons.size(),1));

		for(int x = 0; x < contactButtons.size(); x++){
			contactArea.add(contactButtons.get(x));
		}
		contactArea.repaint();
	}

	private void newContactGUI(){
		newContactWindow = new JFrame("New Contact");
		newContactWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		newContactWindow.setLayout(null);

		JLabel clientIDLabel = new JLabel("Client ID:");
		JLabel firstNameLabel = new JLabel("First Name:");
		JLabel lastNameLabel = new JLabel("Last Name:");
		JLabel phoneNumberLabel = new JLabel("Phone Number:");

		clientIDField = new JTextField(30);
		firstNameField = new JTextField(30);
		lastNameField = new JTextField(30);
		phoneNumberField = new JTextField(30);

		clientIDLabel.setLocation(10, 10);
		clientIDLabel.setSize(100, 20);
		clientIDField.setLocation(clientIDLabel.getX() + clientIDLabel.getWidth(), clientIDLabel.getY());
		clientIDField.setSize(100, 20);

		firstNameLabel.setLocation(clientIDLabel.getX(), clientIDLabel.getY() + clientIDLabel.getHeight() + 20 );
		firstNameLabel.setSize(100, 20);
		firstNameField.setLocation(clientIDField.getX(), clientIDField.getY() + clientIDField.getHeight() + 20 );
		firstNameField.setSize(100, 20);

		lastNameLabel.setLocation(clientIDLabel.getX(), firstNameLabel.getY() + firstNameLabel.getHeight() + 20 );
		lastNameLabel.setSize(100, 20);
		lastNameField.setLocation(clientIDField.getX(), firstNameField.getY() + firstNameField.getHeight() + 20 );
		lastNameField.setSize(100, 20);

		phoneNumberLabel.setLocation(clientIDLabel.getX(), lastNameLabel.getY() + lastNameLabel.getHeight() + 20 );
		phoneNumberLabel.setSize(100, 20);
		phoneNumberField.setLocation(clientIDField.getX(), lastNameField.getY() + lastNameField.getHeight() + 20 );
		phoneNumberField.setSize(100, 20);

		newContactSave = new JButton("Save");
		newContactSave.setLocation(45, phoneNumberLabel.getY() + phoneNumberLabel.getHeight() + 20);
		newContactSave.setSize(100, 20);
		newContactSave.addActionListener(new buttonListener());

		newContactCancel = new JButton("Cancel");
		newContactCancel.setLocation(newContactSave.getX() + newContactSave.getWidth() + 20, newContactSave.getY());
		newContactCancel.setSize(newContactSave.getWidth(), newContactSave.getHeight());
		newContactCancel.addActionListener(new buttonListener());

		newContactWindow.add(clientIDLabel);
		newContactWindow.add(firstNameLabel);
		newContactWindow.add(lastNameLabel);
		newContactWindow.add(phoneNumberLabel);
		newContactWindow.add(clientIDField);
		newContactWindow.add(firstNameField);
		newContactWindow.add(lastNameField);
		newContactWindow.add(phoneNumberField);
		newContactWindow.add(newContactSave);
		newContactWindow.add(newContactCancel);

		newContactWindow.setSize(NEW_CONTACT_FRAME_WIDTH, NEW_CONTACT_FRAME_HEIGHT);
		newContactWindow.setResizable(false);
		newContactWindow.setVisible(true);
		newContactWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(false);
	}

	private void newMessage(){
		//activeContacts.add(new Contact(clientID, firstName,lastName,phoneNumber));
		//contactButtons.add(new JButton(firstName + " " + lastName + " - Client ID: " + clientID));
	}

	private void addNewContact(){
		int clientID = Integer.parseInt(clientIDField.getText());
		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();
		String phoneNumber = phoneNumberField.getText();

		openDatabaseConnection();
		databaseQuery("INSERT INTO clients values(" + clientID + ", '" + firstName + "', '" 
				+ lastName + "', '" + phoneNumber + "');");
		closeDatabaseConnection();
	}

	private void showTwilioAccountInfo(){
		twilioAccountWindow = new JFrame("Enter Twilio Account Information");
		twilioAccountWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		twilioAccountWindow.setLayout(null);

		JLabel twilioAccountSID = new JLabel("Account SID:");
		JLabel twilioAuthToken = new JLabel("Auth Token:");
		JLabel twilioContactNumber = new JLabel("Contact Number:");
		JLabel twilioAccountSIDdisplay = new JLabel(ACCOUNT_SID);
		JLabel twilioAuthTokendisplay = new JLabel(AUTH_TOKEN);
		JLabel twilioContactNumberdisplay = new JLabel(twilioServiceNumber);

		twilioAccountSID.setLocation(10, 10);
		twilioAccountSID.setSize(100, 20);
		twilioAuthToken.setLocation(twilioAccountSID.getX(), twilioAccountSID.getY() + 25);
		twilioAuthToken.setSize(100, 20);
		twilioContactNumber.setLocation(twilioAuthToken.getX(), twilioAuthToken.getY() + 25);
		twilioContactNumber.setSize(100, 20);

		twilioAccountSIDdisplay.setLocation(twilioAccountSID.getX() + twilioAccountSID.getWidth() + 10, twilioAccountSID.getY());
		twilioAccountSIDdisplay.setSize(300, 20);
		twilioAuthTokendisplay.setLocation(twilioAccountSIDdisplay.getX(), twilioAccountSIDdisplay.getY() + 25);
		twilioAuthTokendisplay.setSize(300, 20);
		twilioContactNumberdisplay.setLocation(twilioAuthTokendisplay.getX(), twilioAuthTokendisplay.getY() + 25);
		twilioContactNumberdisplay.setSize(300, 20);

		twilioAccountEdit = new JButton("Edit Account Information");
		twilioAccountEdit.setLocation(twilioContactNumber.getX(), twilioContactNumber.getY() + 25);
		twilioAccountEdit.setSize(200, 20);
		twilioAccountEdit.addActionListener(new buttonListener());

		twilioAccountClose = new JButton("Close");
		twilioAccountClose.setLocation(twilioAccountEdit.getX() + twilioAccountEdit.getWidth() + 10, twilioAccountEdit.getY());
		twilioAccountClose.setSize(100, 20);
		twilioAccountClose.addActionListener(new buttonListener());

		twilioAccountWindow.add(twilioAccountSID);
		twilioAccountWindow.add(twilioAuthToken);
		twilioAccountWindow.add(twilioContactNumber);
		twilioAccountWindow.add(twilioAccountSIDdisplay);
		twilioAccountWindow.add(twilioAuthTokendisplay);
		twilioAccountWindow.add(twilioContactNumberdisplay);
		twilioAccountWindow.add(twilioAccountEdit);
		twilioAccountWindow.add(twilioAccountClose);

		twilioAccountWindow.setSize(TWILIO_ACCOUNT_FRAME_WIDTH, TWILIO_ACCOUNT_FRAME_HEIGHT);
		twilioAccountWindow.setResizable(false);
		twilioAccountWindow.setVisible(true);
		twilioAccountWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(false);
	}

	private void updateTwilioAccountInfo(){
		twilioAccountWindow = new JFrame("Enter Twilio Account Information");
		twilioAccountWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		twilioAccountWindow.setLayout(null);

		JLabel twilioAccountSID = new JLabel("Account SID:");
		JLabel twilioAuthToken = new JLabel("Auth Token:");
		JLabel twilioContactNumber = new JLabel("Contact Number:");

		int textLength = 300;

		twilioAccountSIDField = new JTextField(textLength);
		twilioAccountSIDField.setText(ACCOUNT_SID);
		twilioAuthTokenField = new JTextField(textLength);
		twilioAuthTokenField.setText(AUTH_TOKEN);
		twilioContactField = new JTextField(textLength);
		twilioContactField.setText(twilioServiceNumber);

		twilioAccountSID.setLocation(10, 10);
		twilioAccountSID.setSize(100, 20);

		twilioAuthToken.setLocation(twilioAccountSID.getX(), twilioAccountSID.getY() + 25);
		twilioAuthToken.setSize(100, 20);

		twilioContactNumber.setLocation(twilioAuthToken.getX(), twilioAuthToken.getY() + 25);
		twilioContactNumber.setSize(100, 20);

		twilioAccountSIDField.setLocation(twilioAccountSID.getX() + twilioAccountSID.getWidth() + 10, twilioAccountSID.getY());
		twilioAccountSIDField.setSize(textLength, 20);
		twilioAuthTokenField.setLocation(twilioAccountSIDField.getX(), twilioAccountSIDField.getY() + 25);
		twilioAuthTokenField.setSize(textLength, 20);
		twilioContactField.setLocation(twilioAuthTokenField.getX(), twilioAuthTokenField.getY() + 25);
		twilioContactField.setSize(textLength, 20);

		twilioAccountUpdate = new JButton("Update Account Information");
		twilioAccountUpdate.setLocation(twilioContactNumber.getX(), twilioContactNumber.getY() + 25);
		twilioAccountUpdate.setSize(200, 20);
		twilioAccountUpdate.addActionListener(new buttonListener());

		twilioAccountWindow.add(twilioAccountSID);
		twilioAccountWindow.add(twilioAuthToken);
		twilioAccountWindow.add(twilioContactNumber);
		twilioAccountWindow.add(twilioAccountSIDField);
		twilioAccountWindow.add(twilioAuthTokenField);
		twilioAccountWindow.add(twilioContactField);
		twilioAccountWindow.add(twilioAccountUpdate);

		twilioAccountWindow.setSize(TWILIO_ACCOUNT_FRAME_WIDTH, TWILIO_ACCOUNT_FRAME_HEIGHT);
		twilioAccountWindow.setResizable(false);
		twilioAccountWindow.setVisible(true);
		twilioAccountWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(false);
	}

	private void twilioAccountCheck(){
		boolean infoCorrect = true;
		ACCOUNT_SID = twilioAccountSIDField.getText();
		AUTH_TOKEN = twilioAuthTokenField.getText();
		twilioServiceNumber = twilioContactField.getText();

		try{
			TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

			Map<String, String> filters = new HashMap<String, String>();
			@SuppressWarnings("unused")
			MessageList messages = client.getAccount().getMessages(filters);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Twilio credentials incorrect.");
			infoCorrect = false;
		}

		if(infoCorrect){
			mainWindow.setVisible(true);
			twilioAccountWindow.dispose();
		}else{
			twilioAccountWindow.dispose();
			updateTwilioAccountInfo();
		}
	}

	private class buttonListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == newContactSave){
				addNewContact();
				mainWindow.setVisible(true);
				newContactWindow.dispose();
			}
			if(e.getSource() == newContactCancel){
				mainWindow.setVisible(true);
				newContactWindow.dispose();
			}
			if(e.getSource() == twilioAccountUpdate){
				twilioAccountCheck();
			}
			if(e.getSource() == twilioAccountEdit){
				twilioAccountWindow.dispose();
				updateTwilioAccountInfo();
			}
			if(e.getSource() == twilioAccountClose){
				mainWindow.setVisible(true);
				twilioAccountWindow.dispose();
			}
		}
	}

	private class menuListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == newContact){
				newContactGUI();
			}
			if(e.getSource() == twilioAccount){
				showTwilioAccountInfo();
			}
			if(e.getSource() == newMessage){
				JOptionPane.showMessageDialog(null,"New Message Selected");
			}
			if(e.getSource() == newGroupMessage){
				JOptionPane.showMessageDialog(null,"New Group Message Selected");
			}
			if(e.getSource() == databaseAccount){
				System.out.println("Database account menu item selected");
			}
		}
	}

	private void readFile(){
		File file;
		Scanner scan;
		try {
			file = new File(twilioFile);
			scan = new Scanner(file);

			ACCOUNT_SID = scan.nextLine();
			AUTH_TOKEN = scan.nextLine();
			twilioServiceNumber = scan.nextLine();

			scan.close();
		}
		catch (FileNotFoundException e){
			JOptionPane.showMessageDialog(null,"No saved Twilio Credentials");
			updateTwilioAccountInfo();
		}
	}

	private void writeToFile(){
		FileWriter outstream;
		BufferedWriter out;
		try{
			outstream  = new FileWriter(twilioFile);
			out = new BufferedWriter(outstream);

			out.write(ACCOUNT_SID);
			out.newLine();
			out.write(AUTH_TOKEN);
			out.newLine();
			out.write(twilioServiceNumber);
			out.newLine();

			out.close();
		}
		catch (Exception e){
			System.err.println(ACCOUNT_SID);
			System.err.println(AUTH_TOKEN);
			System.err.println(twilioServiceNumber);
		}
	}

	private class ShutDownListener implements Runnable
	{
		public void run()
		{
			writeToFile();
		}
	}

	private void openDatabaseConnection(){
		try {
			Class.forName(DRIVER).newInstance();
			conn = DriverManager.getConnection(URL + DB_NAME, USER_NAME, PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeDatabaseConnection(){
		try { 
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void databaseQuery(String query){
		try { 
			st = conn.createStatement();
			st.executeUpdate(query);
		} catch (MySQLIntegrityConstraintViolationException e) {
			JOptionPane.showMessageDialog(null,"Duplicate Client ID entered\nContact not added");
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Error adding new contact\nContact not added");
		}
	}

	private void readContactsFromDatabase(){
		openDatabaseConnection();
		try { 
			st = conn.createStatement();
			res = st.executeQuery("SELECT * FROM clients");
			while (res.next()) {
				int clientID = res.getInt("clientID");
				String firstName = res.getString("clientFirstName");
				String lastName = res.getString("clientLastName");
				String phoneNumber = res.getString("clientPhoneNumber");

				activeContacts.add(new Contact(clientID, firstName, lastName, phoneNumber));
				contactButtons.add(new JButton(firstName + " " + lastName + " - Client ID: " + clientID));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeDatabaseConnection();
	}
}