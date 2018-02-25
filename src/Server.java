import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
	public int PORT;
	public int networkPort;
	public static final String IP = "127.0.0.1";
	public LinkedList<Client> localClients;
	private boolean second = false;

	public Server(int PORT, int networkPort) {
		this.PORT = PORT;
		this.networkPort = networkPort;
	}

	public void connect() {
		second = true;
	}

	@Override
	public void run() {
		localClients = new LinkedList<Client>();
		@SuppressWarnings("resource")
		ServerSocket welcomeSocket;
		try {
			welcomeSocket = new ServerSocket(PORT);
			Socket networkConnection = new Socket(IP, networkPort);
			if (second) {
				@SuppressWarnings("resource")
				DataOutputStream outToServer = new DataOutputStream(networkConnection.getOutputStream());
				outToServer.writeBytes("SERVER\n");
			}
			while (true) {
				Socket connection = welcomeSocket.accept();
				ServiceThread newUser = new ServiceThread(connection, networkConnection, localClients, second);
				newUser.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
