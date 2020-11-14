import java.net.*;
import java.io.*;

public class GameClient {

	private Socket clientSocket;
	
	private PrintWriter out;
	private BufferedReader in;
	
	private String hostName;
	private int portNumber;


	public GameClient() {}
	
	public boolean connectToServer(String hostName, int portNumber) {
		try {
			this.portNumber = portNumber;
			this.hostName = hostName;
			
			this.clientSocket = new Socket(hostName, portNumber);
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			System.out.println("Connected to server");
			return true;
		} catch (Exception e) {
			System.out.println("Could not connect to server with hostname " + hostName + " and port number " + portNumber + " (Server may not be running)");
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean closeConnection() {
		System.out.println("Client Closing");
		try {
			this.in.close();
			this.out.close();
			this.clientSocket.close();
			return true;
		} catch (Exception e) {
			System.out.println("Failed to close connection");
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void writeToServer(String message) {
		
		try {
			this.out.println(message);
			System.out.println("Sent the following message to the server: " + message);
		} catch (Exception e) {
			System.out.println("Writing failed");
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	public String readFromServer(int timeOutTime) {
		
		try {
			//set the timeout for user input
			this.clientSocket.setSoTimeout(timeOutTime);
			String input = this.in.readLine();
                        System.out.println("Received the following message from the server: " + input);
			return input;
		} catch (Exception e) {
			System.out.println("No input from server within specified time limit");
		}
		
		return null;

	}
	
	public int getPort() {
		return this.portNumber;
	}

	public String getHostName() {
		return this.hostName;
	}

}

