import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

public class ClientInterface {
	public static final String IP = "127.0.0.1";
	private static Socket socket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;
	private static String user_id;
	private static int PORT;
	private static ListenThread listen;
	private JFrame frmDmetChat;
	private JTextField txtChooseUserId;
	private JTextField txtMessage;
	private JTextField txtDest;

	public ClientInterface() {
		initialize();
	}

	private void initialize() {
		frmDmetChat = new JFrame();
		frmDmetChat.setTitle("DMET 602 Chat App");
		frmDmetChat.setBounds(100, 100, 720, 480);
		frmDmetChat.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showOptionDialog(null, "Are You Sure you want to quit the DMET 602 Chat App?",
						"Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				if (confirm == 0) {
					if (listen != null) {
						listen.kill();
						try {
							listen.join();
							outToServer.writeBytes("EXIT");
							socket.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					System.exit(0);
				}
			}
		});
		frmDmetChat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel pnlMain = new JPanel();
		frmDmetChat.getContentPane().add(pnlMain, BorderLayout.CENTER);
		pnlMain.setLayout(new CardLayout(0, 0));

		JPanel pnlStart = new JPanel();
		pnlMain.add(pnlStart, "start");
		SpringLayout sl_pnlStart = new SpringLayout();
		pnlStart.setLayout(sl_pnlStart);

		JLabel lblWelcome = new JLabel("Welcome the DMET 602 chatting application!");
		sl_pnlStart.putConstraint(SpringLayout.WEST, lblWelcome, 0, SpringLayout.WEST, pnlStart);
		sl_pnlStart.putConstraint(SpringLayout.EAST, lblWelcome, 0, SpringLayout.EAST, pnlStart);
		lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 20));
		pnlStart.add(lblWelcome);
		sl_pnlStart.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblWelcome, 0, SpringLayout.HORIZONTAL_CENTER,
				pnlStart);

		JLabel lblPleaseChooseThe = new JLabel("Please choose the server you'd like to connect to.");
		sl_pnlStart.putConstraint(SpringLayout.SOUTH, lblWelcome, -40, SpringLayout.NORTH, lblPleaseChooseThe);
		sl_pnlStart.putConstraint(SpringLayout.WEST, lblPleaseChooseThe, 0, SpringLayout.WEST, lblWelcome);
		sl_pnlStart.putConstraint(SpringLayout.EAST, lblPleaseChooseThe, 0, SpringLayout.EAST, lblWelcome);
		sl_pnlStart.putConstraint(SpringLayout.NORTH, lblPleaseChooseThe, 207, SpringLayout.NORTH, pnlStart);
		lblPleaseChooseThe.setHorizontalAlignment(SwingConstants.CENTER);
		lblPleaseChooseThe.setFont(new Font("Tahoma", Font.BOLD, 20));
		pnlStart.add(lblPleaseChooseThe);
		sl_pnlStart.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblPleaseChooseThe, 0, SpringLayout.HORIZONTAL_CENTER,
				pnlStart);

		JPanel pnlContainerServerBox = new JPanel();
		sl_pnlStart.putConstraint(SpringLayout.NORTH, pnlContainerServerBox, 49, SpringLayout.SOUTH,
				lblPleaseChooseThe);
		sl_pnlStart.putConstraint(SpringLayout.WEST, pnlContainerServerBox, 280, SpringLayout.WEST, pnlStart);
		sl_pnlStart.putConstraint(SpringLayout.EAST, pnlContainerServerBox, -274, SpringLayout.EAST, pnlStart);
		pnlStart.add(pnlContainerServerBox);
		sl_pnlStart.putConstraint(SpringLayout.HORIZONTAL_CENTER, pnlContainerServerBox, 0,
				SpringLayout.HORIZONTAL_CENTER, pnlStart);

		JComboBox<String> serverBox = new JComboBox<String>();
		pnlContainerServerBox.add(serverBox);
		sl_pnlStart.putConstraint(SpringLayout.NORTH, serverBox, 157, SpringLayout.SOUTH, lblPleaseChooseThe);
		sl_pnlStart.putConstraint(SpringLayout.SOUTH, serverBox, -96, SpringLayout.SOUTH, pnlStart);
		sl_pnlStart.putConstraint(SpringLayout.EAST, serverBox, -59, SpringLayout.EAST, pnlStart);
		serverBox.setModel(new DefaultComboBoxModel<String>(new String[] { "Server 1", "Server 2" }));
		sl_pnlStart.putConstraint(SpringLayout.WEST, serverBox, 117, SpringLayout.EAST, pnlContainerServerBox);

		JButton btnNextUserId = new JButton("Next");
		pnlContainerServerBox.add(btnNextUserId);
		sl_pnlStart.putConstraint(SpringLayout.NORTH, btnNextUserId, 46, SpringLayout.SOUTH, lblPleaseChooseThe);
		sl_pnlStart.putConstraint(SpringLayout.WEST, btnNextUserId, 409, SpringLayout.WEST, pnlStart);
		sl_pnlStart.putConstraint(SpringLayout.SOUTH, btnNextUserId, -207, SpringLayout.SOUTH, pnlStart);
		sl_pnlStart.putConstraint(SpringLayout.EAST, btnNextUserId, -267, SpringLayout.EAST, pnlStart);
		btnNextUserId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (serverBox.getSelectedItem().toString().equals("Server 1"))
					PORT = Network.PORT1;
				else
					PORT = Network.PORT2;
				((CardLayout) pnlMain.getLayout()).show(pnlMain, "userID");
			}
		});

		JPanel pnlUserId = new JPanel();
		pnlMain.add(pnlUserId, "userID");
		SpringLayout sl_pnlUserId = new SpringLayout();
		pnlUserId.setLayout(sl_pnlUserId);

		JLabel lblChooseUserId = new JLabel("Please enter your user id for this session:");
		lblChooseUserId.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblChooseUserId.setHorizontalAlignment(SwingConstants.CENTER);
		sl_pnlUserId.putConstraint(SpringLayout.NORTH, lblChooseUserId, 139, SpringLayout.NORTH, pnlUserId);
		sl_pnlUserId.putConstraint(SpringLayout.WEST, lblChooseUserId, 0, SpringLayout.WEST, pnlUserId);
		sl_pnlUserId.putConstraint(SpringLayout.EAST, lblChooseUserId, 702, SpringLayout.WEST, pnlUserId);
		pnlUserId.add(lblChooseUserId);
		sl_pnlStart.putConstraint(SpringLayout.HORIZONTAL_CENTER, lblChooseUserId, 0, SpringLayout.HORIZONTAL_CENTER,
				pnlUserId);

		JPanel pnlContainerUserId = new JPanel();
		sl_pnlUserId.putConstraint(SpringLayout.NORTH, pnlContainerUserId, 47, SpringLayout.SOUTH, lblChooseUserId);
		sl_pnlUserId.putConstraint(SpringLayout.WEST, pnlContainerUserId, 201, SpringLayout.WEST, pnlUserId);
		sl_pnlUserId.putConstraint(SpringLayout.SOUTH, pnlContainerUserId, 112, SpringLayout.SOUTH, lblChooseUserId);
		sl_pnlUserId.putConstraint(SpringLayout.EAST, pnlContainerUserId, -199, SpringLayout.EAST, pnlUserId);
		pnlUserId.add(pnlContainerUserId);
		sl_pnlStart.putConstraint(SpringLayout.HORIZONTAL_CENTER, pnlContainerUserId, 0, SpringLayout.HORIZONTAL_CENTER,
				pnlUserId);

		txtChooseUserId = new JTextField();
		pnlContainerUserId.add(txtChooseUserId);
		txtChooseUserId.setColumns(10);

		JPanel pnlChat = new JPanel();
		pnlMain.add(pnlChat, "chat");
		SpringLayout sl_pnlChat = new SpringLayout();
		pnlChat.setLayout(sl_pnlChat);

		JButton btnNextChat = new JButton("Next");

		JSplitPane splitPane = new JSplitPane();
		sl_pnlChat.putConstraint(SpringLayout.NORTH, splitPane, 327, SpringLayout.NORTH, pnlChat);
		sl_pnlChat.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, pnlChat);
		splitPane.setDividerSize(0);
		sl_pnlChat.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, pnlChat);
		sl_pnlChat.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, pnlChat);
		splitPane.setResizeWeight(0.88);
		pnlChat.add(splitPane);

		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setDividerSize(0);
		splitPane_1.setResizeWeight(0.8);
		splitPane.setLeftComponent(splitPane_1);

		JPanel panel_1 = new JPanel();
		splitPane_1.setLeftComponent(panel_1);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);

		JLabel lblNewLabel = new JLabel("Message:");
		sl_panel_1.putConstraint(SpringLayout.NORTH, lblNewLabel, 0, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, lblNewLabel, 0, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, lblNewLabel, 25, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, lblNewLabel, 440, SpringLayout.WEST, panel_1);
		panel_1.add(lblNewLabel);

		txtMessage = new JTextField();
		sl_panel_1.putConstraint(SpringLayout.NORTH, txtMessage, 6, SpringLayout.SOUTH, lblNewLabel);
		sl_panel_1.putConstraint(SpringLayout.WEST, txtMessage, 0, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, txtMessage, 0, SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, txtMessage, 0, SpringLayout.EAST, lblNewLabel);
		panel_1.add(txtMessage);
		txtMessage.setColumns(10);

		JPanel panel_2 = new JPanel();
		splitPane_1.setRightComponent(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);

		JLabel lblNewLabel_1 = new JLabel("Send to:");
		sl_panel_2.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 0, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, lblNewLabel_1, 0, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, lblNewLabel_1, 31, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, lblNewLabel_1, 111, SpringLayout.WEST, panel_2);
		panel_2.add(lblNewLabel_1);

		txtDest = new JTextField();
		sl_panel_2.putConstraint(SpringLayout.NORTH, txtDest, 0, SpringLayout.SOUTH, lblNewLabel_1);
		sl_panel_2.putConstraint(SpringLayout.WEST, txtDest, 0, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, txtDest, 71, SpringLayout.SOUTH, lblNewLabel_1);
		sl_panel_2.putConstraint(SpringLayout.EAST, txtDest, 0, SpringLayout.EAST, lblNewLabel_1);
		panel_2.add(txtDest);
		txtDest.setColumns(10);

		JButton btnList = new JButton("List");
		btnList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					getMemberList();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		panel.add(btnList);

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setDividerSize(0);
		sl_pnlChat.putConstraint(SpringLayout.NORTH, splitPane_2, -327, SpringLayout.NORTH, splitPane);
		sl_pnlChat.putConstraint(SpringLayout.WEST, splitPane_2, 0, SpringLayout.WEST, pnlChat);
		sl_pnlChat.putConstraint(SpringLayout.SOUTH, splitPane_2, -6, SpringLayout.NORTH, splitPane);
		sl_pnlChat.putConstraint(SpringLayout.EAST, splitPane_2, 0, SpringLayout.EAST, splitPane);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		pnlChat.add(splitPane_2);

		JLabel lblLoggedInAs = new JLabel("Logged in as: ");
		splitPane_2.setLeftComponent(lblLoggedInAs);

		JScrollPane scrollPane = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane);

		JTextArea txtChat = new JTextArea();
		scrollPane.setViewportView(txtChat);

		btnNextChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new Socket(IP, PORT);
					outToServer = new DataOutputStream(socket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					user_id = txtChooseUserId.getText();

					if (user_id.equals("")) {
						JOptionPane.showMessageDialog(null, "Your user id must be at least 1 character long.");
						user_id = "IGNORE";
					} else if (user_id.toUpperCase().equals("LIST")) {
						JOptionPane.showMessageDialog(null, "Your user id can't be \"List\".");
						user_id = "IGNORE";
					} else if (user_id.toUpperCase().equals("QUIT")) {
						JOptionPane.showMessageDialog(null, "Your user id can't be \"Quit\".");
						user_id = "IGNORE";
					} else if (user_id.toUpperCase().equals("HELP")) {
						JOptionPane.showMessageDialog(null, "Your user id can't be \"Help\".");
						user_id = "IGNORE";
					}

					if (join(user_id)) {
						listen = new ListenThread(socket, txtChat);
						listen.start();
						lblLoggedInAs.setText("Logged in as: " + user_id);
						((CardLayout) pnlMain.getLayout()).show(pnlMain, "chat");
					} else
						txtChooseUserId.setText("");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		pnlContainerUserId.add(btnNextChat);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = txtMessage.getText();
				String dest = txtDest.getText();
				if (message.equals("") || dest.equals(""))
					return;
				try {
					chat(user_id, dest, 2, message);
					txtChat.append("to " + dest + ": " + message + "\n");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				txtMessage.setText("");
			}
		});
		panel.add(btnSend);

	}

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientInterface window = new ClientInterface();
					window.frmDmetChat.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static boolean join(String user_id) throws IOException {
		if (user_id.toUpperCase().equals("IGNORE"))
			return false;
		outToServer = new DataOutputStream(socket.getOutputStream());
		outToServer.writeBytes("CLIENT\n");
		outToServer.writeBytes(user_id + "\n");
		String response = inFromServer.readLine();
		if (response.equals("YES"))
			return true;
		JOptionPane.showMessageDialog(null, "Sorry, this user id is already in use. Please choose another one.");
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
}
