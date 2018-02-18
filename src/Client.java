import java.net.*;

public class Client {
	public String user_id;
	public Socket socket;

	public Client(String user_id, Socket socket) {
		this.user_id = user_id;
		this.socket = socket;
	}

}
