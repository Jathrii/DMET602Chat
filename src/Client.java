import java.io.Serializable;
import java.net.*;

@SuppressWarnings("serial")
public class Client implements Serializable {
	public String user_id;
	public transient Socket socket;

	public Client(String user_id, Socket socket) {
		this.user_id = user_id;
		this.socket = socket;
	}

}
