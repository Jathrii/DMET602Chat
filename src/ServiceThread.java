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
					System.out.println("At remote: " + request);
					if (request.equals("FORWARD")) {
						String source = inFromClient.readLine();
						String destination = inFromClient.readLine();
						int ttl = Integer.parseInt(inFromClient.readLine());
						String message = inFromClient.readLine();

						ttl--;
						System.out.println("ttl remote: " + ttl);
						if (ttl == 0) {
							System.out.println("Should be done.");
							continue;
						}
						System.out.println("or should it?");

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
							System.out.println("here remote");
							outToNetwork.writeBytes("FORWARD\n");
							outToNetwork.writeBytes(source + "\n");
							outToNetwork.writeBytes(destination + "\n");
							outToNetwork.writeBytes("" + ttl + "\n");
							outToNetwork.writeBytes(message + "\n");
							System.out.println("sent remote");
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
							messageToClient.writeBytes("User " + new_user + " just joined the server.\n");
						}
					} else if (request.equals("EXIT")) {
						String old_user = inFromClient.readLine();
						for (Client user : clients) {
							DataOutputStream messageToClient = new DataOutputStream(user.socket.getOutputStream());
							messageToClient.writeBytes("MSSG\n");
							messageToClient.writeBytes("User " + old_user + " left the server.\n");
						}
					} else
						System.out.println("Unkown Network Communication. " + request);
				}
			} else if (connectionType.equals("CLIENT")) {
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
						outToNetwork.writeBytes("JOIN\n");
						outToNetwork.writeBytes(client_id + "\n");
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
						outToNetwork.writeBytes("EXIT\n");
						outToNetwork.writeBytes(client_id + "\n");
						break;
					} else if (type.equals("LIST")) {
						outToNetwork.writeBytes("LIST\n");
						int n = Integer.parseInt(inFromNetwork.readLine());
						outToClient.writeBytes("LIST\n");
						outToClient.writeBytes((clients.size() + n) + "\n");
						for (Client online : clients)
							outToClient.writeBytes(online.user_id + "\n");
						for (int i = 0; i < n; i++)
							outToClient.writeBytes(inFromNetwork.readLine() + "\n");

					} else if (type.equals("MSSG")) {
						while (true) {
							String source = inFromClient.readLine();
							System.out.println("origin source: " + source);
							System.out.println(source);
							String destination = inFromClient.readLine();
							int ttl = Integer.parseInt(inFromClient.readLine());
							String message = inFromClient.readLine();

							ttl--;
							System.out.println("ttl origin: " + ttl);
							if (ttl == 0) {
								System.out.println("What's going on?");
								break;
							}

							boolean exists = false;
							for (Client user : clients)
								if (user.user_id.toLowerCase().equals(destination.toLowerCase())) {
									exists = true;
									DataOutputStream messageToClient = new DataOutputStream(
											user.socket.getOutputStream());
									messageToClient.writeBytes("MSSG\n");
									messageToClient.writeBytes(source + ": " + message + "\n");
									break;
								}
							if (!exists) {
								System.out.println("here origin");
								outToNetwork.writeBytes("FORWARD\n");
								outToNetwork.writeBytes(source + "\n");
								outToNetwork.writeBytes(destination + "\n");
								outToNetwork.writeBytes("" + ttl + "\n");
								outToNetwork.writeBytes(message + "\n");
								System.out.println("sent origin");
								break;
								//continue;
							}
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
