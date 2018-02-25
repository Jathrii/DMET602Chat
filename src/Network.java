import java.io.IOException;
import java.net.UnknownHostException;

public class Network {
	public static final int PORT1 = 6789;
	public static final int PORT2 = 6790;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Server server1 = new Server(PORT1, PORT2);
		Server server2 = new Server(PORT2, PORT1);
		server1.start();
		server2.connect();
		server2.start();
	}

}
