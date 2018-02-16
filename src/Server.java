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
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connection.getOutputStream());
			String user_id = inFromClient.readLine();
			boolean taken = false;
			for (Client client : clients) {
				if (client.user_id.equals(user_id)) {
					taken = true;
					break;
				}
			}
			if (taken) {
				outToClient.writeBytes("NO\n");
				connection.close();
			} else {
				outToClient.writeBytes("YES\n");
				Client client = new Client(user_id, connection);
				clients.add(client);
				ServiceThread newUser = new ServiceThread(client, clients);
				newUser.start();
			}
		}
	}

}
