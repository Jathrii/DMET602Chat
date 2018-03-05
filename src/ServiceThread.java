import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class ServiceThread extends Thread {
	private String client_id;
	private Socket socket;
	private Socket networkSocket;
	private LinkedList<Client> clients;
	private boolean second;

	public ServiceThread(Socket socket, Socket networkSocket, LinkedList<Client> clients, boolean second) {
		this.socket = socket;
		this.networkSocket = networkSocket;
		this.clients = clients;
		this.second = second;
	}

	@Override
	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

			String connectionType = inFromClient.readLine();

			BufferedReader inFromNetwork = new BufferedReader(new InputStreamReader(networkSocket.getInputStream()));
			DataOutputStream outToNetwork = new DataOutputStream(networkSocket.getOutputStream());

			if (connectionType.equals("SERVER")) {
				if (!second) {
					outToNetwork.writeBytes("SERVER\n");
				}

				while (true) {
					String request = inFromClient.readLine();
					if (request.equals("FORWARD")) {
						String source = inFromClient.readLine();
						String destination = inFromClient.readLine();
						int ttl = Integer.parseInt(inFromClient.readLine());
						String message = inFromClient.readLine();

						if (ttl == 0) {
							boolean exists = false;
							for (Client user : clients)
								if (user.user_id.toLowerCase().equals(source.toLowerCase())) {
									exists = true;
									DataOutputStream messageToClient = new DataOutputStream(
											user.socket.getOutputStream());
									messageToClient.writeBytes("MSSG\n");
									messageToClient.writeBytes("User " + destination + " could not be reached.\n");
									break;
								}
							if (!exists) {
								outToNetwork.writeBytes("FORWARD\n");
								outToNetwork.writeBytes(source + "\n");
								outToNetwork.writeBytes(destination + "\n");
								outToNetwork.writeBytes("" + ttl + "\n");
								outToNetwork.writeBytes(message + "\n");
							}
							continue;
						}

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
							ttl--;
							outToNetwork.writeBytes("FORWARD\n");
							outToNetwork.writeBytes(source + "\n");
							outToNetwork.writeBytes(destination + "\n");
							outToNetwork.writeBytes("" + ttl + "\n");
							outToNetwork.writeBytes(message + "\n");
						}
					} else if (request.equals("LIST")) {
						outToClient.writeBytes(clients.size() + "\n");
						for (Client online : clients)
							outToClient.writeBytes(online.user_id + "\n");
					} else if (request.equals("JOIN")) {
						String new_user = inFromClient.readLine();
						for (Client user : clients) {
							DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
							messageToClient.writeBytes("MSSG\n");
							messageToClient.writeBytes(
									"User " + new_user + " (Server " + (socket.getLocalPort() == Network.PORT1 ? 2 : 1)
											+ ") " + " just joined the network.\n");
						}
					} else if (request.equals("EXIT")) {
						String old_user_id = inFromClient.readLine();
						Client old_user = null;
						for (Client user : clients) {
							if (user.user_id.toLowerCase().equals(old_user_id.toLowerCase())) {
								old_user = user;
								continue;
							}
							DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
							messageToClient.writeBytes("MSSG\n");
							messageToClient.writeBytes("User " + old_user_id + " (Server "
									+ (socket.getLocalPort() == Network.PORT1 ? 2 : 1) + ") left the network.\n");
						}
						clients.remove(old_user);
					} else
						System.out.println("Unkown Network Communication. " + request);
				}
			} else if (connectionType.equals("CLIENT")) {
				Client client;

				while (true) {
					String user_id = inFromClient.readLine();
					boolean takenLocal = false;

					for (Client user : clients)
						if (user.user_id.toLowerCase().equals(user_id.toLowerCase())) {
							takenLocal = true;
							break;
						}

					if (takenLocal) {
						outToClient.writeBytes("NO\n");
					} else {
						outToNetwork.writeBytes("LIST\n");
						int n = Integer.parseInt(inFromNetwork.readLine());
						boolean takenRemote = false;

						for (int i = 0; i < n; i++)
							if (inFromNetwork.readLine().toLowerCase().equals(user_id.toLowerCase())) {
								takenRemote = true;
								break;
							}

						if (takenRemote)
							outToClient.writeBytes("NO\n");
						else {
							outToClient.writeBytes("YES\n");
							for (Client user : clients) {
								DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
								messageToClient.writeBytes("MSSG\n");
								messageToClient.writeBytes("User " + user_id + " (Server "
										+ (socket.getLocalPort() == Network.PORT1 ? 1 : 2) + ") "
										+ " just joined the network.\n");
							}
							client_id = user_id;
							client = new Client(client_id, socket);
							clients.add(client);
							outToNetwork.writeBytes("JOIN\n");
							outToNetwork.writeBytes(client_id + "\n");
							break;
						}
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
							messageToClient.writeBytes("User " + client_id + " (Server "
									+ (socket.getLocalPort() == Network.PORT1 ? 1 : 2) + ") left the network.\n");
						}
						outToNetwork.writeBytes("EXIT\n");
						outToNetwork.writeBytes(client_id + "\n");
						break;
					} else if (type.equals("LIST")) {
						outToNetwork.writeBytes("LIST\n");
						int n = Integer.parseInt(inFromNetwork.readLine());
						outToClient.writeBytes("LIST\n");
						outToClient.writeBytes((clients.size() + n) + "\n");
						for (Client online : clients)
							outToClient.writeBytes(online.user_id + " (Server "
									+ (socket.getLocalPort() == Network.PORT1 ? 1 : 2) + ")\n");
						for (int i = 0; i < n; i++)
							outToClient.writeBytes(inFromNetwork.readLine() + " (Server "
									+ (socket.getLocalPort() == Network.PORT1 ? 2 : 1) + ")\n");

					} else if (type.equals("MSSG")) {

						String source = inFromClient.readLine();
						String destination = inFromClient.readLine();
						int ttl = Integer.parseInt(inFromClient.readLine());
						String message = inFromClient.readLine();

						if (ttl == 0) {
							outToClient.writeBytes("MSSG\n");
							outToClient.writeBytes("User " + destination + " could not be reached.\n");
							continue;
						}

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
							ttl--;
							outToNetwork.writeBytes("FORWARD\n");
							outToNetwork.writeBytes(source + "\n");
							outToNetwork.writeBytes(destination + "\n");
							outToNetwork.writeBytes("" + ttl + "\n");
							outToNetwork.writeBytes(message + "\n");
						}

					} else
						System.out.println("Unknown Client Communication. " + type);
				}
			} else
				System.out.println("Unknown Connection Type. " + connectionType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
