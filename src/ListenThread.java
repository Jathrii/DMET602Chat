import java.io.*;
import java.net.*;
import javax.swing.JTextArea;

public class ListenThread extends Thread {
	private Socket socket;
	private JTextArea txtChat;
	private boolean alive;

	public ListenThread(Socket socket, JTextArea txtChat) {
		this.socket = socket;
		this.txtChat = txtChat;
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
						txtChat.append(inFromServer.readLine() + "\n");
					} else if (type.equals("LIST")) {
						int n = Integer.parseInt(inFromServer.readLine());
						for (int i = 0; i < n; i++)
							txtChat.append(inFromServer.readLine() + " is online!\n");
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
