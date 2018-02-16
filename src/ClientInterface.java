import java.io.*;
import java.net.*;

public class ClientInterface {
	public static final int PORT = 6789;
	public static final String IP = "127.0.0.1";
	private static Socket socket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;

	private static boolean join(String user_id) throws IOException {
		outToServer = new DataOutputStream(socket.getOutputStream());
		outToServer.writeBytes(user_id + "\n");
		String response = inFromServer.readLine();
		if (response.equals("YES"))
			return true;
		System.out.println("Sorry, this user id is already in use. Please choose another one.");
		return false;
	}

	private static void chat(String source, String destination, int ttl, String message) throws IOException {
		outToServer.writeBytes(source + "\n");
		outToServer.writeBytes(destination + "\n");
		outToServer.writeBytes("" + ttl + "\n");
		outToServer.writeBytes(message + "\n");
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String user_id;

		System.out.println("Welcome the DMET 602 chatting application!\n");

		do {
			socket = new Socket(IP, PORT);
			outToServer = new DataOutputStream(socket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Please enter your user id for this session:\n");
			user_id = inFromUser.readLine().trim();
		} while (!join(user_id));

		System.out.printf("You're now signed in as %s.\n", user_id);
		System.out.println("Please enter messages using the following format:");
		System.out.println("to recipient_id: message\n");
		System.out.println("Enter \"MemberList\" at any time to get a list of connected users.");
		System.out.println("Enter \"Quit\" or \"Bye\" at any time to sign out.");
		System.out.println("Enter \"Help\" at any time to review these instructions.\n");

		ListenThread listen = new ListenThread(socket);
		listen.start();

		while (true) {
			String input = inFromUser.readLine().trim();
			if (input.toUpperCase().equals("QUIT") || input.toUpperCase().equals("BYE")) {
				listen.kill();
				listen.join();
				chat("EXIT", "", 0, "");
				socket.close();
				inFromUser.close();
				break;
			} else if (input.toUpperCase().equals("HELP")) {
				System.out.println("Please enter messages using the following format:");
				System.out.println("to recipient_id: message\n");
				System.out.println("Enter \"MemberList\" at any time to get a list of connected users.");
				System.out.println("Enter \"Quit\" or \"Bye\" at any time to sign out.");
				System.out.println("Enter \"Help\" at any time to review these instructions.\n");
			} else {
				String[] info = input.split(":", 2);
				chat(user_id, info[0].trim().split(" ")[1], 1, info[1].trim());
			}
		}

		System.out.println("Thanks for using the DMET 602 chatting application, have a nice day!");

	}

}