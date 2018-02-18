import java.io.*;
import java.net.*;

public class ListenThread extends Thread {
	private Socket socket;
	private boolean alive;

	public ListenThread(Socket socket) {
		this.socket = socket;
		this.alive = true;
	}

	public void kill() {
		alive = false;
	}

	@Override
	public void run() {
		try {
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (alive) {
				String type = "";
				if (inFromServer.ready()) {
					type = inFromServer.readLine();
					if (type.equals("MSSG")) {
						String message = inFromServer.readLine();
						if (message.equals("ERROR")) {
							System.out.println("There is no user with this user id online.");
							System.out.println("Enter \"List\" at any time to get a list of connected users.");
						} else
							System.out.println(message);
					} else if (type.equals("LIST")) {
						int n = Integer.parseInt(inFromServer.readLine());
						for (int i = 0; i < n; i++)
							System.out.println(inFromServer.readLine() + " is online!");
					}
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
