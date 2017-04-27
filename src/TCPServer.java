import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
public class TCPServer implements  Runnable
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


	public TCPServer(Socket conn)
	{
		connectionSocket = conn;
	}
	public static void main(String argv[]) throws Exception
	{	

		welcomeSocket = new ServerSocket(6000);
		while(true){
			Socket connectionSocket = welcomeSocket.accept();
			Thread t1 = new Thread(new TCPServer(connectionSocket));
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

				serverInput = inFromClient.readLine();//this line often causes an exception to be thrown so i surrounded it with a try and catch

				if(serverInput!=null){


					System.out.println("CLIENT: "+serverInput);
					if(serverInput.startsWith("join(") && serverInput.endsWith(")"))
						JoinResponse();
					else
						if(serverInput.contains("GetMemberList()")){
							if(joinFlag==false){
								clientOutput = "SERVER: You are not signed in yet";
								outToClient.writeBytes(clientOutput);
							}
							else 
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
									if(x.equals("serverB")){
										Route(m,d1,ttl);
									}
									else{
										String y = " "+m+" From: "+x;
										Route(y,d1,ttl); 
									}
								}
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
					for(int i=0; i<members.size();i++){
						if(members.get(i).equals("serverB")){
							OutputStream os = (sockets.get(i)).getOutputStream();
							DataOutputStream outToOtherClient = new DataOutputStream(os);
							outToOtherClient.writeBytes("RE:"+x+"\n");
							

						}
					}

					connectionSocket = welcomeSocket.accept();
				}
				else if(serverInput.contains("RE:")){
					String x = serverInput.substring(3);
					for(int i=0; i<Allmembers.size();i++){
						if(Allmembers.get(i).equals(x))
							Allmembers.remove(i);
					}
					for(int i=0; i<members.size();i++){
						if(members.get(i).equals("serverB")){
							OutputStream os = (sockets.get(i)).getOutputStream();
							DataOutputStream outToOtherClient = new DataOutputStream(os);
							outToOtherClient.writeBytes("RE:"+x+"\n");
							

						}
					}
					
				}
				else
				{

					clientOutput =  "" + '\n';
					System.out.print(""+clientOutput);

				}

				outToClient.writeBytes(clientOutput);

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
			if(!(members.get(i).equals("serverB")))
				clientOutput+=members.get(i)+", ";

		}
		outToClient.writeBytes(clientOutput+"\n");
		System.out.println(clientOutput);

	}

	public void JoinResponse() throws IOException
	{
		String x = serverInput.substring(5,serverInput.length()-1);

		
			
				if(x.contains(",")){
					clientOutput = "Not Joined";
					outToClient.writeBytes(clientOutput);
					System.out.println(clientOutput);

				}
			

			
			if(x.contains("FromServer"))
			{
				x=x.substring(10);
				for(int i=0; i<Allmembers.size(); i++){
					if(Allmembers.get(i).equals(x)){
						clientOutput = "Not joined";
						outToClient.writeBytes(clientOutput);
						System.out.println(clientOutput);
						return;
					}

				}
				Allmembers.add(x);
			}
			else{
				for(int i=0; i<Allmembers.size(); i++)
				{
					if(Allmembers.get(i).equals(x))
					{
						clientOutput = "Not joined";
						outToClient.writeBytes(clientOutput);
						System.out.println(clientOutput);
						return;
					}
				}
				Allmembers.add(x);
				members.add(x);
				joinFlag = true;
				clientOutput = "joined";
				outToClient.writeBytes(clientOutput);
				System.out.println(clientOutput);
				for(int i=0; i<members.size();i++){
					if(members.get(i).equals("serverB")){
						OutputStream os = (sockets.get(i)).getOutputStream();
						DataOutputStream outToOtherClient = new DataOutputStream(os);
						outToOtherClient.writeBytes("join("+x+"\n");
						

					}
				}
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

		else{ if(members.contains(Destination)){
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
		else {
							for(int i=0; i<members.size();i++)
							{
								if(members.get(i).equals("serverB"))
								{
									OutputStream os = (sockets.get(i)).getOutputStream();
									DataOutputStream outToOtherClient = new DataOutputStream(os);
									
									outToOtherClient.writeBytes("Chat("+Destination+","+Message+","+(TTL-1)+")\n");
									
			
								}
							}
		}
		}
	}
}



