import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
				if (inFromServer.ready()) {
					String message = inFromServer.readLine();
					System.out.println(message);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
