import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.Timer;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.MessageList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class View extends JFrame
{
	private final int FRAME_WIDTH = 600;
	private final int FRAME_HEIGHT = 400;
	private final int CONTACT_PANEL_WIDTH = 150;
	private int previousContact = 0;
	private int currentContact = 0;
	private String ACCOUNT_SID;
	private String AUTH_TOKEN;
	private String contactsFile = "info.txt";
	private String conversationHistory = "history.txt";
	private int numOfContacts;
	private String serviceNumber;
	private int count = 0;

	private JPanel contactArea = new JPanel();
	private JPanel textingArea = new JPanel();

	private String[] contacts;
	private String[] contactNumbers;
	private JButton[] contactButtons;
	private JTextArea[] conversationsDisplay;
	private JScrollPane[] scrollPane;
	private Date[] lastMessage;
	private boolean[] newMessage;

	private JPanel contactNamePanel = new JPanel();
	private JLabel contactNameDisplay = new JLabel("");
	private JPanel conversationPanel = new JPanel();
	private JPanel messagePanel = new JPanel();
	private JButton sendMessageButton = new JButton("Send");
	private JTextArea message = new JTextArea();

	private TwilioRestClient client;

	private Date date;
	private long previousTime;

	private Timer timer = new Timer(30000, new TimerListener());
	private Timer blinkTimer = new Timer(1000, new blinkTimerListener());

	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private Runtime runtime = Runtime.getRuntime();
	private Thread thread = new Thread(new ShutDownListener());

	private ArrayList<String> history = new ArrayList<String>();

	public View(){
		super("Texting App");
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setLayout(null);
		textingArea.setLayout(null);
		conversationPanel.setLayout(null);
		messagePanel.setLayout(null);

		contactArea.setLocation(0, 0);
		contactArea.setSize(CONTACT_PANEL_WIDTH, FRAME_HEIGHT);

		textingArea.setLocation(contactArea.getWidth(), 0);
		textingArea.setSize(FRAME_WIDTH - contactArea.getWidth(), FRAME_HEIGHT);

		contactNamePanel.setLocation(0,0);
		contactNamePanel.setSize(textingArea.getWidth(), 30);

		conversationPanel.setLocation(0,contactNamePanel.getHeight());
		conversationPanel.setSize(textingArea.getWidth(),textingArea.getHeight() - contactNamePanel.getHeight() - 50);

		messagePanel.setLocation(0, contactNamePanel.getHeight() + conversationPanel.getHeight());
		messagePanel.setSize(textingArea.getWidth() - 2, FRAME_HEIGHT - 2 - contactNamePanel.getHeight() - conversationPanel.getHeight());
		messagePanel.setBackground(Color.black);

		message.setLocation(2,2);
		message.setSize(messagePanel.getWidth() - 86, messagePanel.getHeight() - 4);
		message.setLineWrap(true);

		sendMessageButton.setLocation(message.getWidth() + 5,2);
		sendMessageButton.setSize(75, messagePanel.getHeight() - 4);

		contactNamePanel.add(contactNameDisplay);

		date = new Date();

		getInfo();

		contactArea.setLayout(new GridLayout(contacts.length,1));

		for(int x = 0; x < contacts.length; x++)
		{
			conversationsDisplay[x] = new JTextArea();
			conversationsDisplay[x].setLineWrap(true);
			conversationsDisplay[x].setEditable(false);
			scrollPane[x] = new JScrollPane(conversationsDisplay[x]);
			scrollPane[x].setLocation(0,0);
			scrollPane[x].setSize(conversationPanel.getWidth(), conversationPanel.getHeight());
			scrollPane[x].setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane[x].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			contactButtons[x] = new JButton(contacts[x]);
			contactButtons[x].addActionListener(new buttonListener());
			contactButtons[x].setBackground(Color.white);
			contactButtons[x].setForeground(Color.black);
			contactArea.add(contactButtons[x]);

			lastMessage[x] = date;
			newMessage[x] = false;
		}

		printHistoryToScreen();

		sendMessageButton.addActionListener(new buttonListener());

		messagePanel.add(message);
		messagePanel.add(sendMessageButton);

		textingArea.add(contactNamePanel);
		textingArea.add(conversationPanel);
		textingArea.add(messagePanel);
		textingArea.setVisible(false);

		super.add(contactArea);
		super.add(textingArea);

		super.setSize(FRAME_WIDTH, FRAME_HEIGHT + 26);
		super.setResizable(false);
		super.setVisible(true);
		super.setLocationRelativeTo(null);

		client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

		timer.start();
		blinkTimer.start();

		runtime.addShutdownHook(thread);
	}

	private void sendMessage(){
		date = new Date();

		conversationsDisplay[currentContact].append("Trinity (" + dateFormat.format(date) + "): " + message.getText() + "\n");
		history.add(currentContact + "-" + "Trinity (" + dateFormat.format(date) + "): " + message.getText());
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("Body", message.getText()));
		params.add(new BasicNameValuePair("To", "+1" + contactNumbers[currentContact]));
		params.add(new BasicNameValuePair("From", serviceNumber));

		try{
			MessageFactory messageFactory = client.getAccount().getMessageFactory();
			Message message = messageFactory.create(params);
		}catch(TwilioRestException e){
			System.out.println("Error");
			System.out.println(e.getErrorMessage());
			System.out.println(params);
		}
		message.setText("");
	}

	private void receiveMessages(){
		long start = System.currentTimeMillis();
		MessageList messages = client.getAccount().getMessages();

		for (Message message : messages) 
		{
			if(message.getDirection().equals("inbound"))
			{
				for(int x = 0; x < contacts.length; x++)
				{
					if(message.getFrom().equals("+1" + contactNumbers[x]))
					{
						if(lastMessage[x].compareTo(message.getDateSent()) < 0)
						{
							conversationsDisplay[x].append(contacts[x] + " (" + dateFormat.format(message.getDateSent()) + "): " + message.getBody() + "\n");
							lastMessage[x] = message.getDateSent();
							newMessage[x] = true;
							history.add(x + "-" + contacts[x] + " (" + dateFormat.format(message.getDateSent()) + "): " + message.getBody());
							repaint();
						}
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("message search completed - " + (end - start) + " ms");
	}

	private void changeContact()
	{
		contactNameDisplay.setText("");
		conversationPanel.remove(scrollPane[previousContact]);
		contactNameDisplay.setText(contacts[currentContact]);
		conversationPanel.add(scrollPane[currentContact]);
		newMessage[currentContact] = false;
		contactButtons[currentContact].setBackground(Color.white);
		contactButtons[currentContact].setForeground(Color.black);
		repaint();
	}

	private class buttonListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			textingArea.setVisible(true);
			for(int x = 0; x < contacts.length; x++)
			{
				if(e.getSource() == contactButtons[x]){
					previousContact = currentContact;
					currentContact = x;
					changeContact();
				}
			}
			if(e.getSource() == sendMessageButton){
				if(message.getText().length() > 0)
					sendMessage();
				else
					JOptionPane.showMessageDialog(null,"Message empty");
			}
		}
	}

	private class TimerListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			receiveMessages();
		}
	}

	private class blinkTimerListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			for(int x = 0; x < contacts.length; x++)
			{
				if(newMessage[x] == true)
				{
					if(count % 2 == 0)
					{
						contactButtons[x].setBackground(Color.blue);
						contactButtons[x].setForeground(Color.white);
					}
					else
					{
						contactButtons[x].setBackground(Color.white);
						contactButtons[x].setForeground(Color.black);
					}
				}
			}
			count++;
			repaint();
		}
	}

	private void getInfo(){
		String temp;
		File file;
		Scanner scan;
		try {
			file = new File(contactsFile);
			scan = new Scanner(file);

			ACCOUNT_SID = scan.nextLine();
			AUTH_TOKEN = scan.nextLine();
			serviceNumber = scan.nextLine();

			temp = scan.nextLine();
			try{
				previousTime = Long.parseLong(temp);
			}catch(NumberFormatException e){
				System.out.println("Error converting time from file");
			}
			date.setTime(previousTime);

			temp = scan.nextLine();
			try{
				numOfContacts = Integer.parseInt(temp);
			}catch(NumberFormatException e){
				System.out.println("Error converting number of contacts from file");
			}

			contacts = new String[numOfContacts];
			contactNumbers = new String[numOfContacts];
			contactButtons = new JButton[numOfContacts];
			conversationsDisplay = new JTextArea[numOfContacts];
			scrollPane = new JScrollPane[numOfContacts];
			lastMessage = new Date[numOfContacts];
			newMessage = new boolean[numOfContacts];

			for(int x = 0; x < numOfContacts; x++){
				contacts[x] = scan.nextLine();
				contactNumbers[x] = scan.nextLine();
			}

			scan.close();
		} 
		catch (FileNotFoundException e){e.printStackTrace();}

		try {
			file = new File(conversationHistory);
			scan = new Scanner(file);

			while(scan.hasNext())
			{
				history.add(scan.nextLine());
			}

			scan.close();
		} 
		catch (FileNotFoundException e){e.printStackTrace();}
	}

	private void printHistoryToScreen(){
		int num;
		String temp;
		for(int x = 0; x < history.size(); x++){
			try{
				temp = "" + history.get(x).charAt(0);
			num = Integer.parseInt(temp + "");
			conversationsDisplay[num].append(history.get(x).substring(2, history.get(x).length()) + "\n");
			}
			catch(NumberFormatException e){
				
			}
		}
		repaint();
	}

	private void writeToFile(){
		FileWriter outstream;
		BufferedWriter out;
		try{
			outstream  = new FileWriter(contactsFile);

			out = new BufferedWriter(outstream);

			out.write(ACCOUNT_SID);
			out.newLine();
			out.write(AUTH_TOKEN);
			out.newLine();
			out.write(serviceNumber);
			out.newLine();
			date = new Date();
			out.write(date.getTime() + "");
			out.newLine();
			out.write(contacts.length + "");
			out.newLine();
			for(int x = 0; x < numOfContacts; x++){
				out.write(contacts[x]);
				out.newLine();
				out.write(contactNumbers[x]);
				out.newLine();
			}

			out.close();
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}

		try{
			outstream  = new FileWriter(conversationHistory);

			out = new BufferedWriter(outstream);

			for(int x = 0; x < history.size(); x++){
				out.write(history.get(x));
				out.newLine();
			}

			out.close();
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	private class ShutDownListener implements Runnable
	{
		public void run()
		{
			writeToFile();
		}
	}
}
