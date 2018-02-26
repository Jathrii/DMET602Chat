import java.io.*;
import java.net.*;

public class ClientInterface {
	public static final String IP = "127.0.0.1";
	private static Socket socket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;

	private static boolean join(String user_id) throws IOException {
		if (user_id.toUpperCase().equals("IGNORE"))
			return false;
		outToServer = new DataOutputStream(socket.getOutputStream());
		outToServer.writeBytes("CLIENT\n");
		outToServer.writeBytes(user_id + "\n");
		String response = inFromServer.readLine();
		if (response.equals("YES"))
			return true;
		System.out.println("Sorry, this user id is already in use. Please choose another one.");
		return false;
	}

	private static void getMemberList() throws IOException, ClassNotFoundException {
		outToServer.writeBytes("LIST\n");
	}

	private static void chat(String source, String destination, int ttl, String message) throws IOException {
		outToServer.writeBytes("MSSG\n");
		outToServer.writeBytes(source + "\n");
		outToServer.writeBytes(destination + "\n");
		outToServer.writeBytes(ttl + "\n");
		outToServer.writeBytes(message + "\n");
	}

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String user_id;

		System.out.println("Welcome the DMET 602 chatting application!\n");

		int PORT = 0;

		while (true) {
			System.out.println("Please choose the server you'd like to connect to.");
			System.out.println("Enter 1 for Server 1.");
			System.out.println("Enter 2 for Server 2.");
			System.out.println("Chosen server:");
			String choice = inFromUser.readLine().trim();
			if (choice.equals("1")) {
				PORT = Network.PORT1;
				break;
			} else if (choice.equals("2")) {
				PORT = Network.PORT2;
				break;
			} else
				System.out.println("Invalid server number!");
		}

		do {
			socket = new Socket(IP, PORT);
			outToServer = new DataOutputStream(socket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Please enter your user id for this session:\n");
			user_id = inFromUser.readLine().trim();
			if (user_id.equals("")) {
				System.out.println("Your user id must be at least 1 character long.");
				user_id = "IGNORE";
			} else if (user_id.toUpperCase().equals("LIST")) {
				System.out.println("Your user id can't be \"List\".");
				user_id = "IGNORE";
			} else if (user_id.toUpperCase().equals("QUIT")) {
				System.out.println("Your user id can't be \"Quit\".");
				user_id = "IGNORE";
			} else if (user_id.toUpperCase().equals("HELP")) {
				System.out.println("Your user id can't be \"Help\".");
				user_id = "IGNORE";
			}
		} while (!join(user_id));

		System.out.printf("You're now signed in as %s.\n", user_id);
		System.out.println("Please enter messages using the following format:");
		System.out.println("to recipient_id: message\n");
		System.out.println("Enter \"List\" at any time to get a list of connected users.");
		System.out.println("Enter \"Quit\" or \"Bye\" at any time to sign out.");
		System.out.println("Enter \"Help\" at any time to review these instructions.\n");

		ListenThread listen = new ListenThread(socket);
		listen.start();

		while (true) {
			String input = inFromUser.readLine().trim();
			if (input.equals(""))
				continue;

			if (input.toUpperCase().equals("QUIT") || input.toUpperCase().equals("BYE")) {
				listen.kill();
				listen.join();
				outToServer.writeBytes("EXIT");
				socket.close();
				inFromUser.close();
				break;
			} else if (input.toUpperCase().equals("LIST")) {
				getMemberList();
			} else if (input.toUpperCase().equals("HELP")) {
				System.out.println("Please enter messages using the following format:");
				System.out.println("to recipient_id: message\n");
				System.out.println("Enter \"MemberList\" at any time to get a list of connected users.");
				System.out.println("Enter \"Quit\" or \"Bye\" at any time to sign out.");
				System.out.println("Enter \"Help\" at any time to review these instructions.\n");
			} else {
				String[] info = input.split(":", 2);
				chat(user_id, info[0].trim().split(" ", 2)[1], 4, info[1].trim());
			}
		}

		System.out.println("Thanks for using the DMET 602 chatting application, have a nice day!");

	}

}
