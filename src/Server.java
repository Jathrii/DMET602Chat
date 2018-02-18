import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	public static final int PORT = 6789;
	public static LinkedList<Client> clients;

	public static void main(String[] args) throws IOException {
		clients = new LinkedList<Client>();
		@SuppressWarnings("resource")
		ServerSocket welcomeSocket = new ServerSocket(PORT);
		while (true) {
			Socket connection = welcomeSocket.accept();
			ServiceThread newUser = new ServiceThread(connection, clients);
			newUser.start();
		}
	}

}
