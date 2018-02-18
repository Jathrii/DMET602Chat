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
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			while (true) {
				String type = inFromClient.readLine();
				if (type.equals("EXIT")) {
					socket.close();
					clients.remove(client);
					for (Client user : clients) {
						DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
						messageToClient.writeBytes("MSSG\n");
						messageToClient.writeBytes("User " + client.user_id + " left the server.\n");
					}
					break;
				} else if (type.equals("LIST")) {
					outToClient.writeBytes("LIST\n");
					outToClient.writeBytes(clients.size()+"\n");
					for (Client online: clients)
						outToClient.writeBytes(online.user_id+"\n");
				} else if (type.equals("MSSG")) {
					String source = inFromClient.readLine();
					String destination = inFromClient.readLine();
					String ttl = inFromClient.readLine();
					String message = inFromClient.readLine();
					boolean exists = false;
					for (Client user : clients)
						if (user.user_id.toLowerCase().equals(destination.toLowerCase())) {
							exists = true;
							DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
							messageToClient.writeBytes("MSSG\n");
							messageToClient.writeBytes(source + ": " + message + "\n");
							break;
						}
					if (!exists) {
						outToClient.writeBytes("MSSG\n");
						outToClient.writeBytes("ERROR\n");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
