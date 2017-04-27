import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
public class TCPServerD implements  Runnable
{
	static ArrayList<String> members = new ArrayList<>();
	static ArrayList<String> Allmembers = new ArrayList<>();
	static ArrayList<Socket> sockets= new ArrayList<>();
	String serverInput = ""; //input to server from client 
	String clientOutput = ""; //output to client
	DataOutputStream outToClient;
	Socket connectionSocket;
	static ServerSocket welcomeSocket;
	static String ServerMessage;
	static ArrayList<Thread> t = new ArrayList<>();
	boolean joinFlag = false;
	static DataOutputStream outToServer;
	static Socket clientSocket;
	String inFromA;
	static BufferedReader inFromServer;
	

	public TCPServerD(Socket conn)
	{
		connectionSocket = conn;
	}
	public static void main(String argv[]) throws Exception
	{

		clientSocket = new Socket("localhost", 6002);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromUser =  new BufferedReader(new InputStreamReader(System.in));	

		
		outToServer.writeBytes("join(serverD)"+ "\n");
		
		
		 welcomeSocket=new ServerSocket(6003);
			
		 Thread th  =	new Thread(new Runnable(){
			 public void run()
			 {
				 String Message = null;
				 String inFromC = null;
				 while(true)
				 { 
					 try 
					 {
						
						  inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						 inFromC = inFromServer.readLine();
						 
					
						  

					 } catch (Exception e) 
					 {
						 // TODO Auto-generated catch block
						 return;
					 }
					 if(inFromC.length()>0)
					 {
						
						 System.out.println("ServerC: " + inFromC + '\n' );

									if(inFromC.contains("Chat(")){
										
										
											StringTokenizer st = new StringTokenizer(inFromC, ",");
											String d = st.nextToken();
											String d1 = d.substring(5);
											String m = st.nextToken();
											String t = st.nextToken();
											int ttl = Integer.parseInt(t.substring(0, t.length()-1));
											String x = "";
											String y = " "+m;
											
											try {
												Route(y,d1,ttl);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										
									}
									else if(inFromC.contains("join(") && !inFromC.contains("serverD")){
										
										String x = inFromC.substring(5);
										Allmembers.add(x);
									}
									else if(inFromC.contains("RE:")){
										String x = inFromC.substring(3);
										for(int i=0; i<Allmembers.size();i++){
											if(Allmembers.get(i).equals(x)){
												Allmembers.remove(i);
											
											}
											
										}
										
									}
									
								}
						 
					 }
				 
				 
				 
				 
			 }

				public void Route(String Message, String Destination, int TTL) throws IOException
				{


					
						for(int i=0; i<members.size();i++)
						{
							if(members.get(i).equals(Destination))
							{
								System.out.println("SERVER: To "+Destination+" Message "+Message);
								OutputStream os = (sockets.get(i)).getOutputStream();
								DataOutputStream outToOtherClient = new DataOutputStream(os);
								outToOtherClient.writeBytes(Message + '\n');
							

							}
						}
					
				}
			 
			 

		 });
		 th.start();
		 while(true)
		 {
			
			 Socket connectionSocket=welcomeSocket.accept();
			 
			 Thread t1=new Thread(new TCPServerD(connectionSocket));
			 sockets.add(connectionSocket);
			 t.add(t1);
			 t1.start();

			 
		 }
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while(true) {

			try
			{
				BufferedReader inFromClient = 
						new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				outToClient = 
						new DataOutputStream(connectionSocket.getOutputStream());



				if(serverInput!=null){
					serverInput = inFromClient.readLine();//this line often causes an exception to be thrown so i surrounded it with a try and catch

					System.out.println("CLIENT: "+serverInput);
					if(serverInput.startsWith("join(") && serverInput.endsWith(")"))
						JoinResponse();
					else
						if(serverInput.contains("GetMemberList()")){
								MemberListResponse();
						}

						else if(serverInput.contains("GetMembers()")){
								MemberResponse();
											}

						else {
							if(serverInput.contains("Chat(")){
								if(joinFlag==false){
									clientOutput = "SERVER: You are not signed in yet";
									outToClient.writeBytes(clientOutput);
								}
								else{
									StringTokenizer st = new StringTokenizer(serverInput, ",");
									String d = st.nextToken();
									String d1 = d.substring(5);
									String m = st.nextToken();
									String t = st.nextToken();
									int ttl = Integer.parseInt(t.substring(0,t.length()-1));
									String x = "";
									for(int i=0; i<sockets.size();i++){
										if(sockets.get(i) == connectionSocket){
											x = members.get(i);
										}
									}
									String y = " "+m+" From: "+x;
									Route(y,d1,ttl);
								}
							}
						}

					String serverInput1 = serverInput.toUpperCase();
					if((serverInput1.contains("QUIT")||serverInput1.contains("BYE")) && !serverInput.contains("Chat"))
					{
						for(Socket s: sockets)
						{
							DataOutputStream PendingChats = new DataOutputStream(s.getOutputStream());
							PendingChats.writeBytes(clientOutput);

						}

						clientOutput= "TERMINATED";
						String x = "";
						System.out.println("SERVER: "+clientOutput);
						for(int i=0; i<sockets.size();i++){
							if(connectionSocket.equals(sockets.get(i))){
								x = members.get(i);
								members.remove(i);
								sockets.remove(i);
							}	
						}
						for(int i =0; i<Allmembers.size();i++){
							if(Allmembers.get(i).equals(x))
								Allmembers.remove(i);
						}
						outToServer.writeBytes("RE:"+x+"\n");

						connectionSocket = welcomeSocket.accept();
					}
					else
					{

						clientOutput =  "" + '\n';
						System.out.print(""+clientOutput);

					}

					outToClient.writeBytes(clientOutput + '\n');

				}
			}
			catch(Exception e){
				
			}
		}
	}
				

	public void MemberListResponse() throws IOException
	{
		
		clientOutput="Members: ";
		for(int i=0; i<Allmembers.size();i++){
			if(!(Allmembers.get(i).contains("server")))
				clientOutput+=Allmembers.get(i)+", ";
				
		}
		outToClient.writeBytes(clientOutput+"\n");
		System.out.println(clientOutput);
	}
	public void MemberResponse() throws IOException
	{
		clientOutput="Members: ";
		for(int i=0; i<members.size();i++){
			if(!(members.get(i).contains("server")))
			clientOutput+=members.get(i)+", ";
		
		}
		outToClient.writeBytes(clientOutput+"\n");
		System.out.println(clientOutput);

	}
	public void JoinResponse() throws IOException
	{

		
			String x = serverInput.substring(5,serverInput.length()-1);
			if(x.contains(",")){
				clientOutput = "SERVER: Can not enter a name that includes a ','";
				outToClient.writeBytes(clientOutput);
				System.out.println(clientOutput);

			}
			else
			{
				for(int i=0; i<Allmembers.size(); i++){
					if(Allmembers.get(i).equals(x)){
						clientOutput = "Not joined";
						outToClient.writeBytes(clientOutput+"\n");
						System.out.println(clientOutput);
						return;
					}
				}
				Allmembers.add(x);
				members.add(x);
				joinFlag = true;
				clientOutput = "joined";
				outToClient.writeBytes(clientOutput+"\n");
				outToServer.writeBytes("join(FromServer"+x+")\n");
				
			}
		
	}
	

			
	public void Route(String Message, String Destination, int TTL) throws IOException
	{

		if(!(Allmembers.contains(Destination)))
		{
		
				ServerMessage="SERVER: Member doesn't exist or is currently offline";
				System.out.println(ServerMessage);
				outToClient.writeBytes(ServerMessage+"\n");				
			}
		
			else {if(!members.contains(Destination)){
				
				outToServer.writeBytes("Chat("+Destination+","+Message+","+(TTL-1)+")\n");
			     }
		
		else{
			for(int i=0; i<members.size();i++)
			{
				if(members.get(i).equals(Destination))
				{
					System.out.println("SERVER: To "+Destination+" Message "+Message);
					OutputStream os = (sockets.get(i)).getOutputStream();
					DataOutputStream outToOtherClient = new DataOutputStream(os);
					outToOtherClient.writeBytes(Message+"\n");

				}
			}
		}
	}
	}
}




