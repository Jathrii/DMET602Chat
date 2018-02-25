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
						System.out.println(inFromServer.readLine());
					} else if (type.equals("LIST")) {
						int n = Integer.parseInt(inFromServer.readLine());
						for (int i = 0; i < n; i++)
							System.out.println(inFromServer.readLine() + " is online!");
					} else
						System.out.println("Unknown Network Communication.");
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
