import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ServiceThread extends Thread {
	private String client_id;
	private Socket socket;
	private LinkedList<Client> clients;

	public ServiceThread(Socket socket, LinkedList<Client> clients) {
		this.socket = socket;
		this.clients = clients;
	}

	@Override
	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			
			Client client;
			
			while (true) {
				String user_id = inFromClient.readLine();
				boolean taken = false;
				
				for (Client user : clients) {
					if (user.user_id.toLowerCase().equals(user_id.toLowerCase())) {
						taken = true;
						break;
					}
				}

				if (taken) {
					outToClient.writeBytes("NO\n");
				} else {
					outToClient.writeBytes("YES\n");
					for (Client user : clients) {
						DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
						messageToClient.writeBytes("MSSG\n");
						messageToClient.writeBytes("User " + user_id + " just joined the server.\n");
					}
					client_id = user_id;
					client = new Client(client_id, socket);
					clients.add(client);
					break;
				}
			}

			while (true) {
				String type = inFromClient.readLine();
				if (type.equals("EXIT")) {
					socket.close();
					clients.remove(client);
					for (Client user : clients) {
						DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
						messageToClient.writeBytes("MSSG\n");
						messageToClient.writeBytes("User " + client_id + " left the server.\n");
					}
					break;
				} else if (type.equals("LIST")) {
					outToClient.writeBytes("LIST\n");
					outToClient.writeBytes(clients.size() + "\n");
					for (Client online : clients)
						outToClient.writeBytes(online.user_id + "\n");
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
