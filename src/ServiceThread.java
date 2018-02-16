import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ServiceThread extends Thread {
	private Client client;
	private LinkedList<Client> clients;

	public ServiceThread(Client client, LinkedList<Client> clients) {
		this.client = client;
		this.clients = clients;
	}

	@Override
	public void run() {
		try {
			Socket socket = client.socket;
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true) {
				String source = inFromClient.readLine();
				String destination = inFromClient.readLine();
				String ttl = inFromClient.readLine();
				String message = inFromClient.readLine();
				if (source.equals("EXIT")) {
					socket.close();
					clients.remove(client);
					break;
				} else {
					for (Client user : clients) {
						if (user.user_id.equals(destination)) {
							DataOutputStream outToClient = new DataOutputStream(user.socket.getOutputStream());
							outToClient.writeBytes(source + ": " + message + "\n");
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
