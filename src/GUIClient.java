import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class GUIClient extends JFrame {

	JPanel contentPane;
	JLabel label1;
	JTextArea nameArea;
	JButton joinB;
	JLabel afterName;
	JTextArea chat;
	JButton allMembers;
	JTextArea textArea;
    JTextArea destination;
	JLabel destinationLabel;
	JLabel messageLabel;
	JTextArea message;
	JButton send;
	JButton quitButton;
	JButton serverMembers;
	JScrollPane chatS;
	JScrollBar scrollBar;
	static Socket clientSocket;
	DataOutputStream outToServer;
	BufferedReader inFromServer; 
	String serveroutput;
	boolean joinFlag;
	

	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIClient frame = new GUIClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public GUIClient() throws UnknownHostException, IOException {
		joinFlag = false;
		clientSocket = new Socket("localhost", 6002);             
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromUser =  new BufferedReader(new InputStreamReader(System.in));	
		String clientinp;
		Thread t1 =	new Thread(new Runnable(){
			public void run()
			{

				String serveroutput = null;
				while(true)
				{
					try 
					{
						inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						serveroutput = inFromServer.readLine();
						if(serveroutput!=null){
							if(serveroutput.equals("Not joined")){
								afterName.setText("Name already in use, choose another one");
								afterName.setVisible(true);
							}
							else{
								if(serveroutput.equals("joined")){
									joinB.setVisible(false);
									afterName.setVisible(false);
									label1.setVisible(false);
									nameArea.setVisible(false);
									chatS.setVisible(true);
									quitButton.setVisible(true);
									allMembers.setVisible(true);
									destinationLabel.setVisible(true);
									messageLabel.setVisible(true);
									destination.setVisible(true);
									message.setVisible(true);
									serverMembers.setVisible(true);
									send.setVisible(true);
									contentPane.repaint();
									contentPane.revalidate();
								}
								else{
									if(serveroutput.contains("Members:")){
										chat.append(serveroutput+"\n");
										chat.repaint();
										chat.revalidate();
									}
									else if(serveroutput.length()>0)
										chat.append(serveroutput+"\n");
								}
							}
							
							
							System.out.println(serveroutput);
						}
					} 
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						return;
					}
				}
			}

		});
		t1.start();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(200, 200, 500, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		label1 = new JLabel("Enter your name here to sign in :)");
		label1.setBounds(6, 82, 219, 31);
		contentPane.add(label1);
		
		nameArea = new JTextArea();
		nameArea.setBounds(9, 138, 186, 43);
		contentPane.add(nameArea);
		
		joinB = new JButton("Join");
		joinB.setBounds(282, 148, 117, 29);
		joinB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(nameArea.getText().contains(",")){
					afterName.setText("Your name can not contain a','");
					afterName.setVisible(true);
				}
				else if(nameArea.getText().length()==0){
					afterName.setText("You must enter a name");
					afterName.setVisible(true);
				}
				else{
				try {
					outToServer.writeBytes("join("+nameArea.getText()+")\n");

					}
				 catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
				
			}
		});
		contentPane.add(joinB);
		
		afterName = new JLabel("Name already in use, choose another one");
		afterName.setBounds(6, 221, 258, 33);
		afterName.setForeground(Color.BLACK);
		afterName.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		afterName.setVisible(false);
		contentPane.add(afterName);
		
		chat = new JTextArea("");
		chat.setBounds(16, 18, 308, 228);
		chat.setEditable(false);
		chat.setVisible(true);
		
		chatS = new JScrollPane();
		chatS.setBounds(16, 20, 320, 250);
		

		
		scrollBar = new JScrollBar();
 	   	chatS.setViewportView(chat);;
 	   	chatS.setVisible(false);
		contentPane.add(chatS);

		
		allMembers = new JButton("All members");
		allMembers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					outToServer.writeBytes("GetMemberList()\n");

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		allMembers.setBounds(342, 29, 96, 22);
		allMembers.setVisible(false);
		contentPane.add(allMembers);
		
		quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					outToServer.writeBytes("Quit\n");
					System.exit(0);
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		quitButton.setBounds(336, 95, 102, 29);
		quitButton.setVisible(false);
		contentPane.add(quitButton);
		
		destination = new JTextArea();
		destination.setBounds(16, 295, 93, 33);
		destination.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		destination.setVisible(false);
		contentPane.add(destination);
		
		destinationLabel = new JLabel("To:");
		destinationLabel.setBounds(16, 277, 61, 16);
		destinationLabel.setVisible(false);
		contentPane.add(destinationLabel);
		
		messageLabel = new JLabel("Message:");
		messageLabel.setBounds(130, 277, 61, 16);
		messageLabel.setVisible(false);
		contentPane.add(messageLabel);
		
		message = new JTextArea();
		message.setBounds(129, 295, 231, 33);
		message.setVisible(false);
		contentPane.add(message);
		
		send = new JButton("Send");
		send.setBounds(366, 299, 72, 29);
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(message.getText().length()==0)
					chat.append("You must enter a message\n");
				else if(destination.getText().length()==0)
					chat.append("You must enter a destination\n");
				
				else{
				try {
					outToServer.writeBytes("Chat("+destination.getText()+","+message.getText()+","+"2)\n");
					chat.append("To: " + destination.getText() + " Message: " + message.getText() + "\n" );
					destination.setText(null);

					message.setText("");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
			}
		});
		send.setVisible(false);
		contentPane.add(send);
		
		serverMembers = new JButton("Server Members");
		serverMembers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					outToServer.writeBytes("GetMembers()\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		serverMembers.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		serverMembers.setBounds(342, 66, 102, 29);
		serverMembers.setVisible(false);
		contentPane.add(serverMembers);
		


	}
}









